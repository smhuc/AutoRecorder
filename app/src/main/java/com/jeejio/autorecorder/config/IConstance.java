package com.jeejio.autorecorder.config;

import android.os.Environment;

import java.io.File;

/**
 * author：victory
 * description: 这个类主要放置一些应用配置的常量.
 */

public interface IConstance {

    String OSS_JSON_DATA_PTAH = "json/data_chinese.json";

    /**
     * 本地储存文件夹名
     */
    String SDPATH = Environment.getExternalStorageDirectory().getAbsolutePath();
    String SEPARATOR = File.separator;
    String JEEJIO = "JeejioRecord";

    String VIDEO = "video";
    String IMAGE = "image";
    String RECORD = "record";
    String DOWNLOAD = "download";
    String WAV = "wav";
    String PCM = "pcm";
    /**
     * 录音储存路径
     */
    String RECORD_WAV_PATH = SDPATH + SEPARATOR + JEEJIO  + SEPARATOR;

    String RECORD_PCM_PATH = SDPATH + SEPARATOR + JEEJIO + SEPARATOR + PCM + SEPARATOR;
    /**
     * 录音下载路径
     */
    String DOWNLOAD_PATH = SDPATH + SEPARATOR + JEEJIO + SEPARATOR + DOWNLOAD + SEPARATOR;
    /**
     * 图片存储路径
     */
    String IMAGE_TEMP_PATH = SDPATH + SEPARATOR + JEEJIO + SEPARATOR + IMAGE + SEPARATOR;
    /**
     * 阿里云的AccessKeyID
     */
    String ACCESSKEYID = "LTAID368dVaZ0otl";
    /**
     * 阿里云的AccessKeySecret
     */
    String ACCESSKEYSECRET = "LOGq7SFXv85oOCpJHtlOtW7JwTp7l2";
    /**
     * 阿里云的endpoint
     */
    String ENDPOINT = "http://oss-cn-beijing.aliyuncs.com";
    /**
     * 阿里云的bucketName
     */
    String BUCKETNAME = "jeejio-test-hu";
}
