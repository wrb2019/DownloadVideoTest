package com.example.downloadvideotest;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.util.Log;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.logging.Logger;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 下载的异步任务
 */
public class DownLoadTask extends AsyncTask<String, Integer, Integer> {

    public static final int TYPE_SUCCESS = 0;
    public static final int TYPE_FAILED = 1;
    public static final int TYPE_PAUSED= 2;
    public static final int TYPE_CANCELED = 3;

    private boolean isPaused = false;
    private boolean isCanceled = false;
    private int lastProgress;

    private DownLoadListener downLoadListener;

    public DownLoadTask(DownLoadListener downLoadListener) {
        this.downLoadListener = downLoadListener;
    }

    public void setDownLoadListener(DownLoadListener downLoadListener) {
        this.downLoadListener = downLoadListener;
    }

    @Override
    protected Integer doInBackground(String... strings) {
        InputStream inputStream = null;
        RandomAccessFile randomAccessFile = null;
        File file = null;
        try {
            long downLoadLength = 0;//记录已下载的文件长度
            String downLoadUrl = strings[0];
            //获取文件名称
            String fileName = downLoadUrl.substring(downLoadUrl.lastIndexOf("/"));
            //外部存储的公共文件夹目录
            String directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();

            file = new File(directory, fileName);
            if (file.exists()) {
                downLoadLength = file.length();
            }

            long contentLength = getContentLength(downLoadUrl);
            if (contentLength == 0) {
                return TYPE_FAILED;
            } else if (contentLength == downLoadLength) {
                //如果已下载的字节和文件总字节相等，说明已经下载完成了
                return TYPE_SUCCESS;
            }

            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .addHeader("RANGE", "bytes=" + downLoadLength + "-") //断点继续下载
                    .url(downLoadUrl)
                    .build();
            Response response = client.newCall(request).execute();
            if (response != null) {
                inputStream = response.body().byteStream();
                randomAccessFile = new RandomAccessFile(file, "rw");
                randomAccessFile.seek(downLoadLength);//跳过已下载的字节

                byte[] bytes = new byte[1024];
                int total = 0;
                int len;
                //inputStream.read(bytes)--读取多个字节写到bytes中
                while ((len = inputStream.read(bytes)) != -1) {
                    if (isCanceled) {
                        return TYPE_CANCELED;
                    } else if (isPaused) {
                        return TYPE_PAUSED;
                    } else {
                        total += len;
                        randomAccessFile.write(bytes, 0, len);
                        //计算已经下载的百分比
                        int progress = (int)((total + downLoadLength) * 100 / contentLength);
                        publishProgress(progress);
                    }
                }
            }
            response.body().close();
            return TYPE_SUCCESS;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (randomAccessFile != null) {
                    randomAccessFile.close();
                }
                if (isCanceled && file != null) {
                    file.delete();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return TYPE_FAILED;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        int progress = values[0];
        if (progress > lastProgress) {
            downLoadListener.onProgress(progress);
            lastProgress = progress;
        }
    }

    @Override
    protected void onPostExecute(Integer integer) {

        Log.e("onPostExecute","integer="+integer);
        switch (integer) {
            case TYPE_SUCCESS: {
                downLoadListener.onSuccess();
                break;
            }
            case TYPE_FAILED: {
                downLoadListener.onFailed();
                break;
            }
            case TYPE_PAUSED: {
                downLoadListener.onPaused();
                break;
            }
            case TYPE_CANCELED: {
                downLoadListener.onCanceled();
                break;
            }
            default:break;
        }
    }

    /**
     * 暂停下载
     */
    public void pauseDownLoad() {
        this.isPaused = true;
    }

    /**
     * 取消下载
     */
    public void cancelDownLoad() {
        this.isCanceled = true;
    }

    /**
     * 获取文件长度
     *
     * @param downLoadUrl
     * @return
     */
    private long getContentLength(String downLoadUrl) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(downLoadUrl).build();
        try {
            Response response = client.newCall(request).execute();
            if (response != null && response.isSuccessful()) {
                long contentLength = response.body().contentLength();
                response.body().close();
                return contentLength;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
        return 0;
    }
}
