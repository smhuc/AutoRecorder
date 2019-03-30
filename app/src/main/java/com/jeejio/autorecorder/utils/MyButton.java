package com.jeejio.autorecorder.utils;

import android.content.Context;
import android.os.CountDownTimer;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.Timer;
import java.util.TimerTask;

public class MyButton extends android.support.v7.widget.AppCompatButton implements View.OnTouchListener {

    private static boolean isLongClickModule = false;
    float startX = 0;
    float startY = 0;
    Timer timer = null;
    private BtnLongClickListener longClickListener;
    private CountDownTimer countDownTimer;
    private long countDownSeconds = 5;

    public MyButton(Context context) {
        super(context);
        setOnTouchListener(this);
    }

    public MyButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOnTouchListener(this);
    }

    public MyButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOnTouchListener(this);
    }

    @Override
    public boolean onTouch(View v, MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:

                startX = ev.getX();
                startY = ev.getY();
                LogUtils.d("Aming", "按下......(" + startX + ":" + startY + ")");
                timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        isLongClickModule = true;
                    }
                }, 1000); // 按下时长设置
                break;
            case MotionEvent.ACTION_MOVE:
                double deltaX = Math.sqrt((ev.getX() - startX) * (ev.getX() - startX) + (ev.getY() - startY) * (ev.getY() - startY));
                if (deltaX > 20 && timer != null) { // 移动大于20像素
                    LogUtils.d("Aming", "移动瓶子瓶子＝......(" + ev.getX() + ":" + ev.getY() + ")");
//                    timer.cancel();
//                    timer = null;
//                    stop();
                }
                LogUtils.d("Aming", "移动......(" + ev.getX() + ":" + ev.getY() + ")");
//                if (isLongClickModule) {
//                    //添加你长按之后的方法
//                    longClickListener.longClickAction_DOWN();
//                    //按下超过1秒，是一个长按操作，开始计时
//                    start();
//                    timer = null;
//                }
                break;
            case MotionEvent.ACTION_UP:
                LogUtils.d("Aming", "抬起......(" + ev.getX() + ":" + ev.getY() + ")");
//                if (isLongClickModule) {
//                    //长按事件
//                    longClickListener.longClickAction_UP();
//                    stop();
//                } else {
//                    //单击事件
//                    longClickListener.longClickAction_SHORT_DOWN();
//                }
//                isLongClickModule = false;
//                if (timer != null) {
//                    timer.cancel();
//                    timer = null;
//                }
                break;
            default:
                isLongClickModule = false;
                if (timer != null) {
                    timer.cancel();
                    timer = null;
                }
        }

        return false;
    }

    public interface BtnLongClickListener {
        void longClickAction_DOWN();

        void longClickAction_UP();

        void onTimerFinished();

        void longClickAction_SHORT_DOWN();
    }

    public void setCountDownSeconds(long countDownSeconds) {
        this.countDownSeconds = countDownSeconds;
    }

    public void setBtnLongClickListener(final BtnLongClickListener longClickListener) {
        this.longClickListener = longClickListener;
    }

//    @Override
//    public boolean onTouchEvent(MotionEvent ev) {
//        switch (ev.getAction()) {
//            case MotionEvent.ACTION_DOWN:
//
//                startX = ev.getX();
//                startY = ev.getY();
//                timer = new Timer();
//                timer.schedule(new TimerTask() {
//                    @Override
//                    public void run() {
//                        isLongClickModule = true;
//                    }
//                }, 1000); // 按下时长设置
//                break;
//            case MotionEvent.ACTION_MOVE:
//                double deltaX = Math.sqrt((ev.getX() - startX) * (ev.getX() - startX) + (ev.getY() - startY) * (ev.getY() - startY));
//                if (deltaX > 20 && timer != null) { // 移动大于20像素
//                    timer.cancel();
//                    timer = null;
//                }
//                if (isLongClickModule) {
//                    //添加你长按之后的方法
//                    longClickListener.longClickAction_DOWN();
//                    //是一个长按操作，开始计时
//                    start();
//                    timer = null;
//                }
//                break;
//            case MotionEvent.ACTION_UP:
//                if (isLongClickModule) {
//                    longClickListener.longClickAction_UP();
//                    stop();
//                }
//                isLongClickModule = false;
//                if (timer != null) {
//                    timer.cancel();
//                    timer = null;
//                }
//                break;
//            default:
//                isLongClickModule = false;
//                if (timer != null) {
//                    timer.cancel();
//                    timer = null;
//                }
//        }
//
//        return true;
//    }

    public void start() {
        countDownTimer = new CountDownTimer(countDownSeconds * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                longClickListener.onTimerFinished();
            }
        };
        countDownTimer.start();
    }

    /**
     * 停止倒计时
     */
    public void stop() {
        if (countDownTimer != null)
            countDownTimer.cancel();
    }

}
