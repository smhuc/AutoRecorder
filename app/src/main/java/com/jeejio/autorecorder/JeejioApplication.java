package com.jeejio.autorecorder;

import android.app.Application;

import com.alibaba.sdk.android.oss.ClientConfiguration;
import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.OSSClient;
import com.alibaba.sdk.android.oss.common.auth.OSSCredentialProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSPlainTextAKSKCredentialProvider;
import com.jeejio.autorecorder.config.IConstance;

public class JeejioApplication extends Application {
    public static OSS oss;

    @Override
    public void onCreate() {
        super.onCreate();
        initOSS();
    }

    /**
     * 初始化阿里云配置
     */
    private void initOSS() {

        OSSCredentialProvider credentialProvider = new OSSPlainTextAKSKCredentialProvider(IConstance.ACCESSKEYID, IConstance.ACCESSKEYSECRET);
        ClientConfiguration conf = new ClientConfiguration();
        conf.setConnectionTimeout(15 * 1000); // 连接超时，默认15秒
        conf.setSocketTimeout(15 * 1000); // socket超时，默认15秒
        conf.setMaxConcurrentRequest(8); // 最大并发请求数，默认5个
        conf.setMaxErrorRetry(2); // 失败后最大重试次数，默认2次

        // oss为全局变量，endpoint是一个OSS区域地址
        oss = new OSSClient(getApplicationContext(), IConstance.ENDPOINT, credentialProvider, conf);
    }
}
