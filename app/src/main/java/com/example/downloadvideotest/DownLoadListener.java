package com.example.downloadvideotest;

/**
 * 下载状态的监听接口
 */
public interface DownLoadListener {
    void onProgress(int progress);
    void onSuccess();
    void onFailed();
    void onPaused();
    void onCanceled();
}
