package com.jeejio.autorecorder.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bigkoo.pickerview.builder.OptionsPickerBuilder;
import com.bigkoo.pickerview.listener.OnOptionsSelectListener;
import com.bigkoo.pickerview.view.OptionsPickerView;
import com.google.gson.Gson;
import com.jeejio.autorecorder.R;
import com.jeejio.autorecorder.bean.JsonBean;
import com.jeejio.autorecorder.config.IConstance;
import com.jeejio.autorecorder.dialog.BaseNiceDialog;
import com.jeejio.autorecorder.dialog.NiceDialog;
import com.jeejio.autorecorder.dialog.ViewConvertListener;
import com.jeejio.autorecorder.dialog.ViewHolder;
import com.jeejio.autorecorder.utils.AnimalUtils;
import com.jeejio.autorecorder.utils.Cn2Spell;
import com.jeejio.autorecorder.utils.FileUtils;
import com.jeejio.autorecorder.utils.GetJsonDataUtil;
import com.jeejio.autorecorder.utils.LogUtils;
import com.jeejio.autorecorder.utils.MyButton;
import com.jeejio.autorecorder.utils.OSSUtils;
import com.jeejio.autorecorder.utils.PackageUtils;
import com.jeejio.autorecorder.utils.ScreenParam;
import com.jeejio.autorecorder.utils.record.AudioPlayer;
import com.jeejio.autorecorder.utils.record.AudioRecorder;

import org.json.JSONArray;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import me.samlss.lighter.Lighter;
import me.samlss.lighter.interfaces.OnLighterListener;
import me.samlss.lighter.parameter.Direction;
import me.samlss.lighter.parameter.LighterParameter;
import me.samlss.lighter.parameter.MarginOffset;

public class MainActivity extends BaseActivity implements View.OnClickListener, View.OnTouchListener, OSSUtils.DataBindListener {

    private List<JsonBean> options1Items = new ArrayList<>();
    private ArrayList<ArrayList<String>> options2Items = new ArrayList<>();
    private ArrayList<ArrayList<ArrayList<String>>> options3Items = new ArrayList<>();

    private Thread thread;
    private static final int MSG_LOAD_DATA = 0x0001;
    private static final int MSG_LOAD_SUCCESS = 0x0002;
    private static final int MSG_LOAD_FAILED = 0x0003;
    private static final int PLAY_RECORD = 0x0004;

    private static boolean isLoaded = false;

    private CountDownTimer countDownTimer;
    private AudioRecorder audioRecorder;

    private LinearLayout ll_choice;
    private ImageView img_animation;
    private TextView file_name, file_name_item;
    private MyButton btn_record;
    private TextView txt, txt_state, version_code;
    private TextView tv_setting;
    private OSSUtils ossUtils;
    //录入者名字
    private String recorder_name = "";
    //本地保存目录名
    private String save_dir_name = "";
    private String opt1tx = "", opt2tx = "";
    private String realPath = "";
    private String ossPath = "";
    //录音是否超时标识
    private boolean isTimeOut = false;
    private int time_out = 3;


    @SuppressLint("HandlerLeak")
    private android.os.Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_LOAD_DATA:
                    if (thread == null) {//如果已创建就不再重新创建子线程了
                        final String jsonData = (String) msg.obj;
                        thread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                // 拉取数据失败,则加载assets中数据
                                if (TextUtils.isEmpty(jsonData)) {
                                    initJsonData(new GetJsonDataUtil().getJson(MainActivity.this, "data_chinese.json"));
                                } else {
                                    initJsonData(jsonData);
                                }
                            }
                        });
                        thread.start();
                    }
                    break;

                case MSG_LOAD_SUCCESS:
//                    Toast.makeText(MainActivity.this, "Parse Succeed", Toast.LENGTH_SHORT).show();
                    isLoaded = true;
                    break;

                case MSG_LOAD_FAILED:
                    isLoaded = false;
                    Toast.makeText(MainActivity.this, "Parse Failed", Toast.LENGTH_SHORT).show();
                    break;
                case PLAY_RECORD:
                    if (!TextUtils.isEmpty(realPath)) {
                        txt_state.setText("录入成功");
                        txt_state.setTextColor(Color.GREEN);
                        startAnimal();
                        AudioPlayer.getInstance().startPlayWav(new File(realPath));//播放
                    }
                    break;

            }
        }
    };

    @Override
    void initView() {
        ll_choice = findViewById(R.id.ll_choice);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) ll_choice.getLayoutParams();
        params.height = ScreenParam.height / 4;
        version_code = findViewById(R.id.version_code);
        ll_choice.setLayoutParams(params);
        file_name = findViewById(R.id.file_name);
        file_name_item = findViewById(R.id.file_name_item);
        txt = findViewById(R.id.dcp_txt);
        txt_state = findViewById(R.id.txt_state);
        btn_record = findViewById(R.id.tv_record);
        btn_record.setOnTouchListener(this);
        img_animation = findViewById(R.id.img_animation);
        tv_setting = findViewById(R.id.setting);
        tv_setting.setOnClickListener(this);
        ll_choice.setOnClickListener(this);
//
        txt.setText(getDcpTxt());
        version_code.setText(PackageUtils.getVersionName(MainActivity.this));

        //蒙层引导页
        showGuide();

    }

    @Override
    void initParams() {
        ossUtils = new OSSUtils(this);
        ossUtils.loadJsonData(IConstance.OSS_JSON_DATA_PTAH);

        audioRecorder = AudioRecorder.getInstance();
        audioRecorder.releaseAudioRecord();

        AudioPlayer.getInstance().setOnStateListener(new AudioPlayer.OnState() {
            @Override
            public void onStateChanged(AudioPlayer.WindState currentState) {
                switch (currentState) {
                    case PLAYING:
                        break;
                    case STOP_PLAY:
                        stopAnimal();
                        showConfirmDialog();
                        LogUtils.d("播放完成...");
                        break;
                }
            }
        });

//        AudioPlayer.getInstance().startPlayWav(new File(fileAbsoultePath));//播放
//        mHandler.sendEmptyMessage(MSG_LOAD_DATA);
    }

    @Override
    void startService() {

    }

    @Override
    int getLayoutId() {
        return R.layout.activity_main;
    }


    private void showConfirmDialog() {
        //    通过AlertDialog.Builder这个类来实例化我们的一个AlertDialog的对象
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        //    设置Title的图标
        builder.setIcon(R.mipmap.jeejio);
        //    设置Title的内容
        builder.setTitle("上传");
        //    设置Content来显示一个信息
        builder.setMessage("是否上传该录音文件？");
        //    设置一个PositiveButton
        builder.setPositiveButton("上传", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        ossUtils.uploadRecordFile(ossPath);
                    }
                }).start();
            }
        });
        //    设置一个NegativeButton
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }


    private void showPickerView() {// 弹出选择器

        OptionsPickerView pvOptions = new OptionsPickerBuilder(this, new OnOptionsSelectListener() {
            @Override
            public void onOptionsSelect(int options1, int options2, int options3, View v) {
                //返回的分别是三个级别的选中位置
                opt1tx = options1Items.size() > 0 ?
                        options1Items.get(options1).getPickerViewText() : "";

                opt2tx = options2Items.size() > 0
                        && options2Items.get(options1).size() > 0 ?
                        options2Items.get(options1).get(options2) : "";

//                String opt3tx = options2Items.size() > 0
//                        && options3Items.get(options1).size() > 0
//                        && options3Items.get(options1).get(options2).size() > 0 ?
//                        options3Items.get(options1).get(options2).get(options3) : "";
                file_name.setText(opt2tx);
                file_name_item.setText(opt1tx);

                save_dir_name = Cn2Spell.getPinYin(opt1tx) + File.separator + Cn2Spell.getPinYin(opt2tx) + File.separator;

//                String tx = opt1tx + opt2tx;
//                Toast.makeText(MainActivity.this, save_dir_name, Toast.LENGTH_SHORT).show();
            }
        })

                .setTitleText("Key Choice")
                .setDividerColor(Color.BLACK)
                .setTextColorCenter(Color.BLACK) //设置选中项文字颜色
                .setContentTextSize(20)
                .build();

//        pvOptions.setPicker(options1Items);//一级选择器
        pvOptions.setPicker(options1Items, options2Items);//二级选择器
//        pvOptions.setPicker(options1Items, options2Items, options3Items);//三级选择器
        pvOptions.show();
    }


    private void initJsonData(String jsonData) {
        //解析数据

        /**
         * 注意：assets 目录下的Json文件仅供参考，实际使用可自行替换文件
         * 关键逻辑在于循环体
         *
         * */
//        String JsonData = new GetJsonDataUtil().getJson(this, "data_chinese.json");//获取assets目录下的json文件数据
        LogUtils.d(jsonData);

        List<JsonBean> jsonBeanList = parseData(jsonData);//用Gson 转成实体

        LogUtils.d("size:" + jsonBeanList.size());

        /**
         * 添加省份数据
         *
         * 注意：如果是添加的JavaBean实体，则实体类需要实现 IPickerViewData 接口，
         * PickerView会通过getPickerViewText方法获取字符串显示出来。
         */
        options1Items = jsonBeanList;

        for (int i = 0; i < jsonBeanList.size(); i++) {//遍历省份
            ArrayList<String> cityList = new ArrayList<>();//该省的城市列表（第二级）
            cityList = jsonBeanList.get(i).getCity_list();
            options2Items.add(cityList);
        }

        mHandler.sendEmptyMessage(MSG_LOAD_SUCCESS);

    }


    public ArrayList<JsonBean> parseData(String result) {//Gson 解析
        ArrayList<JsonBean> detail = new ArrayList<>();
        try {
            JSONArray data = new JSONArray(result);
            Gson gson = new Gson();
            for (int i = 0; i < data.length(); i++) {
                JsonBean entity = gson.fromJson(data.optJSONObject(i).toString(), JsonBean.class);
                detail.add(entity);
            }
        } catch (Exception e) {
            e.printStackTrace();
            mHandler.sendEmptyMessage(MSG_LOAD_FAILED);
        }
        return detail;
    }

    private void showGuide() {
        TranslateAnimation translateAnimation = new TranslateAnimation(-500, 0, 0, 0);
        translateAnimation.setDuration(500);
        translateAnimation.setInterpolator(new BounceInterpolator());

        Lighter.with(this)
                .setOnLighterListener(new OnLighterListener() {
                    @Override
                    public void onShow(int index) {
//                        Toast.makeText(getApplicationContext(), "正在显示第" + (index+1) + "高亮", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onDismiss() {
                        //检查SD卡权限
                        checkSDPermission();
//                        Toast.makeText(getApplicationContext(), "高亮已全部显示完毕", Toast.LENGTH_SHORT).show();
                    }
                })
                .setBackgroundColor(0xB3000000)
                .addHighlight(new LighterParameter.Builder()
                        .setHighlightedViewId(R.id.ll_choice)
                        .setTipLayoutId(R.layout.layout_tip_1)
                        .setTipViewRelativeDirection(Direction.BOTTOM)
//                        .setTipViewDisplayAnimation(LighterHelper.getScaleAnimation())
                        .setTipViewRelativeOffset(new MarginOffset(300, 0, 0, 0))
                        .build())
                .addHighlight(new LighterParameter.Builder()
                        .setHighlightedViewId(R.id.tv_record)
                        .setTipLayoutId(R.layout.layout_tip_2)
                        .setTipViewRelativeDirection(Direction.TOP)
                        .setShapeXOffset(10)
                        .setShapeYOffset(10)
                        .setTipViewDisplayAnimation(translateAnimation)
                        .setTipViewRelativeOffset(new MarginOffset(0, 10, 0, 20))
                        .build())
                .show();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_choice:
                if (isLoaded) {
                    if (TextUtils.isEmpty(recorder_name)) {

                        NiceDialog.init()
                                .setLayoutId(R.layout.dialog_show)
                                .setConvertListener(new ViewConvertListener() {
                                    @Override
                                    public void convertView(final ViewHolder holder, final BaseNiceDialog dialog) {
                                        holder.setOnClickListener(R.id.btn_dialog_show, new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                EditText editText = holder.getView(R.id.et_dialog_show);
                                                recorder_name = editText.getText().toString();
                                                if (TextUtils.isEmpty(recorder_name)) {
                                                    Toast.makeText(MainActivity.this, "请键入您的名字..", Toast.LENGTH_SHORT).show();
                                                    return;
                                                }
                                                if (dialog.isVisible()) {
                                                    dialog.dismiss();
                                                }
                                                showPickerView();
                                            }
                                        });
                                    }
                                })
                                .setMargin(20)
                                .setOutCancel(false)
                                .setShowBottom(false)
                                .setDimAmount(0.3f)
                                .show(getSupportFragmentManager());

                    } else {
                        showPickerView();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Please waiting until the data is parsed", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.setting:
                NiceDialog.init()
                        .setLayoutId(R.layout.dialog_show_time_setting)
                        .setConvertListener(new ViewConvertListener() {
                            @Override
                            public void convertView(final ViewHolder holder, final BaseNiceDialog dialog) {
                                holder.setOnClickListener(R.id.btn_dialog_show_time_setting, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        EditText editText = holder.getView(R.id.et_dialog_show_time_setting);
                                        String time = editText.getText().toString();
                                        if (TextUtils.isEmpty(time)) {
                                            Toast.makeText(MainActivity.this, "请设置录音超时时长..", Toast.LENGTH_SHORT).show();
                                            return;
                                        }
                                        time_out = Integer.parseInt(time);
                                        if (dialog.isVisible()) {
                                            dialog.dismiss();
                                        }
                                        Toast.makeText(MainActivity.this, "设置成功..", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        })
                        .setMargin(20)
                        .setOutCancel(false)
                        .setShowBottom(false)
                        .setDimAmount(0.3f)
                        .show(getSupportFragmentManager());

                break;
        }
    }


    @Override
    public void sendData(String data) {
        LogUtils.d("接收到的数据：" + data);
        if (data.contains("oss_success")) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this, "音频数据上传成功～", Toast.LENGTH_SHORT).show();
                }
            });
            return;
        }
        Message message = mHandler.obtainMessage();
        message.what = MSG_LOAD_DATA;
        message.obj = data.replaceAll("/n", "");
        mHandler.sendMessage(message);
    }

    @Override
    public void error(String info) {
        LogUtils.d("加载数据失败信息：" + info);
        if (info.contains("oss_failure")) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this, "音频数据上传失败～", Toast.LENGTH_SHORT).show();
                }
            });
            return;
        }
        Message message = mHandler.obtainMessage();
        message.what = MSG_LOAD_DATA;
        message.obj = "";
        mHandler.sendMessage(message);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, "数据列表更新失败～", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        thread = null;
        AudioPlayer.getInstance().stopPlay();
        if (audioRecorder != null)
            audioRecorder.releaseAudioRecord();

    }

    private long down_time;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (v.getId() == R.id.tv_record) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    //检查录音权限
                    if (checkAudioRecordPermission()) {
                        if (!TextUtils.isEmpty(recorder_name)) {
                            down_time = System.currentTimeMillis();
                            isTimeOut = false;
                            LogUtils.d("按下时间：" + down_time);
                            txt_state.setText("");
                            txt_state.setTextColor(Color.GREEN);
                            btn_record.setText("松开结束");
                            //停止播放
                            AudioPlayer.getInstance().stopPlay();
                            realPath = "";
                            ossPath = "";
                            startAnimal();
                            startTimer();
                            String fileName = Cn2Spell.getPinYin(recorder_name) + "_" + FileUtils.getFilename();
                            String wavFileName = save_dir_name;
                            realPath = IConstance.RECORD_WAV_PATH + wavFileName + fileName + ".wav";
                            ossPath = wavFileName + fileName + ".wav";
                            LogUtils.d("音频保存完整路径为：" + realPath);
                            audioRecorder.createDefaultAudio(wavFileName, fileName);
                            audioRecorder.startRecord(null);
                        } else {
                            txt_state.setText("请选择上面的录音类型");
                            txt_state.setTextColor(Color.RED);
//                            Toast.makeText(MainActivity.this, "请选择上面的录音类型..", Toast.LENGTH_LONG).show();
                        }
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    long up_time = (System.currentTimeMillis() - down_time);
                    LogUtils.d("抬起时间：" + up_time);
                    if (checkAudioRecordPermission() && !TextUtils.isEmpty(recorder_name) && !isTimeOut) {
                        stopAnimal();
                        stopTimer();
                        btn_record.setText("按住说话");
                        if (audioRecorder != null)
                            audioRecorder.stopRecord();
                        if (up_time < 200) {
                            txt_state.setText("录音太短");
                            txt_state.setTextColor(Color.RED);
                            mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if (FileUtils.deleteFile(realPath)) {
                                        LogUtils.d("short delete success");
                                    }
                                }
                            }, 1000);

                        } else {
                            mHandler.sendEmptyMessage(PLAY_RECORD);
                        }
                    }
                    break;
            }
        }
        return false;
    }

    public void startTimer() {
        countDownTimer = new CountDownTimer(time_out * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                isTimeOut = true;
                stopAnimal();
                stopTimer();
                btn_record.setText("按住说话");
                if (audioRecorder != null)
                    audioRecorder.stopRecord();
                txt_state.setText("录音超时");
                txt_state.setTextColor(Color.RED);
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (FileUtils.deleteFile(realPath)) {
                            LogUtils.d("timeout delete success");
                        }
                    }
                }, 1000);
            }
        };
        countDownTimer.start();
    }

    /**
     * 停止倒计时
     */
    public void stopTimer() {
        if (countDownTimer != null)
            countDownTimer.cancel();
    }

    private void startAnimal() {
        img_animation.setVisibility(View.VISIBLE);
        AnimalUtils.init(MainActivity.this, img_animation).startAnimals();
    }

    private void stopAnimal() {
        img_animation.setVisibility(View.INVISIBLE);
        AnimalUtils.init(MainActivity.this, img_animation).stopAnimals();
    }

    private String getDcpTxt() {
        return "      注意事项：\n"
                + "1、此软件所有采集信息均为中文录音" + "\n"
                + "2、为确保采集源质量，所有录音时长均做了限制，请您在规定时间内完成音源采集";
    }

}
