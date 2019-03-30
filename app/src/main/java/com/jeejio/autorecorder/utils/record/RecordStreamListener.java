package com.jeejio.autorecorder.utils.record;

/**
 * @ClassName: RecordStreamListener.java
 * @Description:获取录音的音频流,用于拓展的处理
 * @Author: victory
 * @Date: 2018/10/16 16:16
 */
public interface RecordStreamListener {
    void recordOfByte(byte[] data, int begin, int end);
}
