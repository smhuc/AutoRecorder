package com.jeejio.autorecorder.utils;

import android.text.TextUtils;
import android.util.Log;

import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback;
import com.alibaba.sdk.android.oss.callback.OSSProgressCallback;
import com.alibaba.sdk.android.oss.internal.OSSAsyncTask;
import com.alibaba.sdk.android.oss.model.GetObjectRequest;
import com.alibaba.sdk.android.oss.model.GetObjectResult;
import com.alibaba.sdk.android.oss.model.PutObjectRequest;
import com.alibaba.sdk.android.oss.model.PutObjectResult;
import com.jeejio.autorecorder.JeejioApplication;
import com.jeejio.autorecorder.config.IConstance;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class OSSUtils {
    private DataBindListener dataBindListener;

    public interface DataBindListener {
        void sendData(String data);

        void error(String info);
    }


    public OSSUtils(DataBindListener dataBindListener) {
        this.dataBindListener = dataBindListener;
    }

    /**
     * 阿里oss上传
     */
    public void uploadRecordFile(String path) {
        LogUtils.d(path);
        File file = new File(IConstance.RECORD_WAV_PATH + path);
        if (!file.exists()) {
            return;
        }
        //jeejioo/26 letter/A/ muweilin_2019-03-27 18-48-02 909.wav
        // 构造上传请求
//                PutObjectRequest put = new PutObjectRequest(IConstance.BUCKETNAME, "andord_test.mp3", path);
        String objectKey = "jeejioo/" + path;// u_ 文件夹，用来区别用户上传，m_ 用来区别物端上传
        LogUtils.d("本地上传的阿里云服务器地址:" + objectKey);
        PutObjectRequest put = new PutObjectRequest(IConstance.BUCKETNAME, objectKey, IConstance.RECORD_WAV_PATH + path);
        // 异步上传时可以设置进度回调
        put.setProgressCallback(new OSSProgressCallback<PutObjectRequest>() {
            @Override
            public void onProgress(PutObjectRequest request, long currentSize, long totalSize) {
//                LogUtils.v("currentSize: " + currentSize + " totalSize: " + totalSize);
            }
        });
        OSSAsyncTask task = JeejioApplication.oss.asyncPutObject(put, new OSSCompletedCallback<PutObjectRequest, PutObjectResult>() {
            @Override
            public void onSuccess(final PutObjectRequest request, final PutObjectResult result) {
                String alyObjectKey = request.getObjectKey();
                dataBindListener.sendData("oss_success:" + alyObjectKey);
                LogUtils.d("UploadSuccess...ObjectKey:" + alyObjectKey);

            }

            @Override
            public void onFailure(PutObjectRequest request, ClientException clientExcepion, ServiceException serviceException) {
                // 请求异常
                if (clientExcepion != null) {
                    // 本地异常如网络异常等
                    clientExcepion.printStackTrace();
                }
                if (serviceException != null) {
                    // 服务异常
                    LogUtils.v(serviceException.getErrorCode());
                    LogUtils.v(serviceException.getRequestId());
                    LogUtils.v(serviceException.getHostId());
                    LogUtils.v(serviceException.getRawMessage());
                    dataBindListener.error("oss_failure:" + serviceException.getErrorCode() + "," + serviceException.getRequestId() + "," + serviceException.getHostId() + "," + serviceException.getRawMessage());

                }
            }
        });
    }

    public void loadJsonData(String ossFilePath) {
        // 构造下载文件请求
        GetObjectRequest get = new GetObjectRequest(IConstance.BUCKETNAME, ossFilePath);
        get.setProgressListener(new OSSProgressCallback<GetObjectRequest>() {
            @Override
            public void onProgress(GetObjectRequest request, long currentSize, long totalSize) {
                LogUtils.v("currentSize: " + currentSize + " totalSize: " + totalSize);
            }
        });
        OSSAsyncTask task = JeejioApplication.oss.asyncGetObject(get, new OSSCompletedCallback<GetObjectRequest, GetObjectResult>() {
            @Override
            public void onSuccess(GetObjectRequest request, GetObjectResult result) {
                // 请求成功
                InputStream inputStream = result.getObjectContent();
                String jsonData = FileUtils.convertStreamToString(inputStream);

                dataBindListener.sendData(jsonData);

//                    try {
//                        FileUtils.writeToLocal(IConstance.DOWNLOAD_PATH + "test.wav", inputStream);
//                        Log.d("Aming", "onSuccess");
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
            }

            @Override
            public void onFailure(GetObjectRequest request, ClientException clientExcepion, ServiceException serviceException) {
                // 请求异常
                if (clientExcepion != null) {
                    // 本地异常如网络异常等
                    clientExcepion.printStackTrace();
                }
                if (serviceException != null) {
                    // 服务异常
                    LogUtils.v(serviceException.getErrorCode());
                    LogUtils.v(serviceException.getRequestId());
                    LogUtils.v(serviceException.getHostId());
                    LogUtils.v(serviceException.getRawMessage());
                    dataBindListener.error(serviceException.getErrorCode() + "," + serviceException.getRequestId() + "," + serviceException.getHostId() + "," + serviceException.getRawMessage());
                }
            }
        });
    }

}
