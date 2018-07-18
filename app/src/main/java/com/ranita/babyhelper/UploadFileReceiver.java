package com.ranita.babyhelper;

import android.content.Context;
import android.util.Log;

import net.gotev.uploadservice.ServerResponse;
import net.gotev.uploadservice.UploadInfo;
import net.gotev.uploadservice.UploadServiceBroadcastReceiver;

public class UploadFileReceiver extends UploadServiceBroadcastReceiver {
    private static final String TAG = UploadFileReceiver.class.getName();
    @Override
    public void onProgress(Context context, UploadInfo uploadInfo) {
    }

    @Override
    public void onError(Context context, UploadInfo uploadInfo, ServerResponse serverResponse, Exception exception) {
        Log.d(TAG, "onError, uploadInfo: " + uploadInfo.getUploadId() + ", server resp: " + serverResponse + ", except: " + exception.toString());
    }

    @Override
    public void onCompleted(Context context, UploadInfo uploadInfo, ServerResponse serverResponse) {
        Log.d(TAG, "onCompleted, uploadInfo: " + uploadInfo.getUploadId() + ", server resp: " + serverResponse);
    }

    @Override
    public void onCancelled(Context context, UploadInfo uploadInfo) {
        Log.d(TAG, "onCancelled");
    }
}
