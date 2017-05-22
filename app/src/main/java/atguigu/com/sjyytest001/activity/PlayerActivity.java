package atguigu.com.sjyytest001.activity;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import atguigu.com.sjyytest001.R;
import atguigu.com.sjyytest001.domain.LocalVideoInfo;
import atguigu.com.sjyytest001.util.Utils;
import atguigu.com.sjyytest001.view.VideoView;

public class PlayerActivity extends AppCompatActivity implements View.OnClickListener {

    private VideoView vv;
    private Uri uri;
    private Utils utils;

    private LinearLayout llTop;
    private TextView tvName;
    private ImageView ivBattery;
    private TextView tvSystemTime;
    private Button btnVoice;
    private SeekBar seekbarVoice;
    private Button btnSwitchPlayer;
    private LinearLayout llBottom;
    private TextView tvCurrentTime;
    private SeekBar seekbarVideo;
    private TextView tvDuration;
    private Button btnExit;
    private Button btnPre;
    private Button btnStartPause;
    private Button btnNext;
    private Button btnSwitchScreen;
    private final int  PROGRESS = 0;
    private MyBroadcastReceiver recevier;
    private int position;
    private ArrayList<LocalVideoInfo> videoInfos;
    private GestureDetector gd;
    public final int HIDE = 1;

    private final int NORMAL = 2;
    private final int FULL = 3;

    private boolean isFullScreen ;   // 设置是否全屏
    private int screenWidth;  // 屏幕的宽和高
    private int screenHight;
    private int normalWidth;  // 视频的宽和高
    private int normalHight;

    private int currentVoice ; //当前的音量    让seekbar 关联最大音量  需要在 初始化 seekbar
    private int maxVoice;
    private AudioManager am;  // 这是音频的一个类
    private boolean isMute = false; //是否静音
    private boolean isNetUri = true;

    private LinearLayout ll_loading;
    private LinearLayout ll_buffering;
    private TextView tv_loading_net_speed;
    private TextView tv_net_speed;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        findViews();

        initData(); //在这里设置电量的变化
        
        vv = (VideoView)findViewById(R.id.vv);
      /*  uri = getIntent().getData(); //得到地址*/
        
        getUriList();   //得到视屏的列表
        setUri();   //设置播放地址

        setVideoType(NORMAL); //设置播放地址为默认




        //播放 准备工作  完毕 时 调用
        vv.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                //当视频准备好了的时候  有个方法  得到视频要显示的  宽 和 高
                normalWidth = mp.getVideoWidth();
                normalHight = mp.getVideoHeight();

                int duration = vv.getDuration();//得到视频的总长度
                seekbarVideo.setMax(duration);

                tvDuration.setText(utils.stringForTime(duration)); //设置视频的总时长，显示在文本上

                vv.start();

                handler.sendEmptyMessage(PROGRESS); // 统一发消息，让handler 持续更新
                ll_loading.setVisibility(View.GONE);//  让加载的页面隐藏
                hideMediaController(); // 让控制面板一进来的时候隐藏

                //准备完成的时候  设置下视频拖动完成的土司  可能以后用的到
                mp.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
                    @Override
                    public void onSeekComplete(MediaPlayer mp) {
                        Toast.makeText(PlayerActivity.this, "拖动完成了", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        //播放完了调用   播放完成
        vv.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
               //设置播放下一个
                setNextVideo();
                finish();
            }
        });

        // 播放出错的时候调用  可以设置万能播放器
        vv.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                startVitamioPlayer();
                return false;
            }
        });


        //视频进度的更新
        seekbarVideo.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                 if(fromUser) {  //如果是用户拖动的话
                     vv.seekTo(progress);
                 }
            }

            @Override   //刚触碰的时候
            public void onStartTrackingTouch(SeekBar seekBar) {
                 handler.removeCallbacksAndMessages(null);
            }

            @Override   //离开屏幕的时候
            public void onStopTrackingTouch(SeekBar seekBar) {
                 handler.sendEmptyMessageDelayed(HIDE,3000) ;
            }
        });

        //声音进度更新
        seekbarVoice.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override  //  进度条改变的时候监听
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser) {
                    updateVoiceProgress(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                 handler.removeCallbacksAndMessages(null);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                handler.sendEmptyMessageDelayed(HIDE,3000);
            }
        });

    }

    private void startVitamioPlayer() {
        if(vv != null){
            vv.stopPlayback();
        }
        Intent intent = new Intent(this, VitamioVideoPlayerActivity.class);
        if(videoInfos != null && videoInfos.size() >0){
            Bundle bunlder = new Bundle();
            bunlder.putSerializable("videolist",videoInfos);
            intent.putExtra("position",position);
            //放入Bundler
            intent.putExtras(bunlder);
        }else if(uri != null){
            intent.setData(uri);
        }
        startActivity(intent);
        finish();//关闭系统播放器
    }

    //  更新音量进度条
    private void updateVoiceProgress(int progress) {
        currentVoice = progress;
        am.setStreamVolume(AudioManager.STREAM_MUSIC,currentVoice,0);
        seekbarVoice.setProgress(currentVoice);

        if(currentVoice <=0){
            isMute = true;
        }else {
            isMute = false;
        }

    }

    //得到屏幕的宽和高的方法
    private void getWindowWidthHight(){
        //得到屏幕的宽和高
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        screenHight = metrics.heightPixels;
        screenWidth = metrics.widthPixels;
    }

    private boolean isHideShow = false;  // 是否显示隐藏控制面板
    /*隐藏控制面板*/
    private void hideMediaController(){
        llBottom.setVisibility(View.GONE);
        llTop.setVisibility(View.GONE);
        isHideShow = false ;
    };
    /*显示控制面板*/
    private void showMediaController(){
        llBottom.setVisibility(View.VISIBLE);
        llTop.setVisibility(View.VISIBLE);
        isHideShow = true ;

    }

    private float downX;
    private float downY;

    private int maxY;  //可移动的最大距离
    private int mVoice; // 当前按下的音量
    @Override   //把事件交给手势识别器  这样双击长按就可以了
    public boolean onTouchEvent(MotionEvent event) {
        gd.onTouchEvent(event);

        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
   //按下去的时候，记录起始坐标，将handler 消息移除
                downX = event.getX();
                downY = event.getY();
                mVoice = am.getStreamVolume(AudioManager.STREAM_MUSIC);
                handler.removeMessages(HIDE);
                maxY = Math.min(screenHight,screenWidth); // 两个数中  小的那个
                break;
            case MotionEvent.ACTION_MOVE:
//得到滑动的距离   计算改变的声音 = （移动的距离/总距离）* 总音量
                float newY = event.getY();
                float distanceY = downY - newY;
                float dalta = (distanceY/maxY)*maxVoice;  //改变的声音
                //当前的音量为： 改变的音量加 + 原来的音量

                int cVoice = (int) Math.min(Math.max(mVoice + dalta,0),maxY);
                if(cVoice != 0) {
                   /* if(isMute) {  //如果是静音的话   把音量设置为0
                        am.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
                        seekbarVoice.setProgress(0);
                    }else {
                        am.setStreamVolume(AudioManager.STREAM_MUSIC,cVoice,0);
                        seekbarVoice.setProgress(cVoice);
                    }*/

                    //因为使用了系统的音量工具  到0 自动静音  就不用判断了
                    updateVoiceProgress(cVoice);
                }

                break;
            case MotionEvent.ACTION_UP:
                handler.sendEmptyMessageDelayed(HIDE,3000);
                break;
        }
        return true;
    }

    //设置开始或者暂停
    private void setStartOrPause() {
        if(vv.isPlaying()){  //如果正在播放的话
            //暂停
            vv.pause();
            //按钮状态-播放
            btnStartPause.setBackgroundResource(R.drawable.btn_start_selector);
        }else {
            //播放
            vv.start();
            //按钮状态-暂停
            btnStartPause.setBackgroundResource(R.drawable.btn_pause_selector);
        }
    }

    private void setUri() {   //这个方法是设置从  getUri方法中得到的uri  或者 path  设置进去

        if(videoInfos != null && videoInfos.size() > 0) {

            LocalVideoInfo videoInfo = videoInfos.get(position);
            tvName.setText(videoInfo.getName());
            vv.setVideoPath(videoInfo.getData());

            isNetUri = utils.isNetUri(videoInfo.getData());   //  判断是否为网络连接可以放在Utils 类中
                                                                //返回的是boolean类型的
                                                                //然后再handler中更新

        }else if(uri != null){   //如果有视频，但不是列表，而是只有一个的话，那就不是得到视频列表路径
                                  //而是得到单独的一条uri
            vv.setVideoURI(uri);
            isNetUri = utils.isNetUri(uri.toString());
        }
        setButtonStatus();
    }
    private void getUriList() {   // 这个方法是从LocalVideoFragment中得到 相应的 urilist列表  和点击的事实坐标
        uri = getIntent().getData();   //得到点击的对象的 实例  的 uri
        videoInfos = (ArrayList<LocalVideoInfo>) getIntent().getSerializableExtra("videolist");
        position = getIntent().getIntExtra("position",0);

    }
    //播放下一个
    public void setNextVideo() {

        position++;
        if(position < videoInfos.size()) {

            LocalVideoInfo localVideoInfo = videoInfos.get(position);
            isNetUri = utils.isNetUri(localVideoInfo.getData());
            ll_loading.setVisibility(View.VISIBLE);

            vv.setVideoPath(localVideoInfo.getData());
            tvName.setText(localVideoInfo.getName());


            setButtonStatus();  //设置按钮状态

        }else {
            Toast.makeText(this, "退出播放器", Toast.LENGTH_SHORT).show();
            finish();
        }

    }

    private void setButtonStatus() {

        if(videoInfos != null && videoInfos.size() >0){
            //有视频播放
            setEnable(true);

            if(position ==0){
                btnPre.setBackgroundResource(R.drawable.btn_pre_gray);
                btnPre.setEnabled(false);
            }

            if(position ==videoInfos.size()-1){
                btnNext.setBackgroundResource(R.drawable.btn_next_gray);
                btnNext.setEnabled(false);
            }

        }else if(uri != null){
            //上一个和下一个不可用点击
            setEnable(false);
        }

    }

    //设置按钮是否可以点击
    private void setEnable(boolean b) {
        if( b){
            //上一个和下一个都可以点击
            btnPre.setBackgroundResource(R.drawable.btn_pre_selector);
            btnNext.setBackgroundResource(R.drawable.btn_next_selector);
        }else {
            //上一个和下一个灰色，并且不可用点击
            btnPre.setBackgroundResource(R.drawable.btn_pre_gray);
            btnNext.setBackgroundResource(R.drawable.btn_next_gray);
        }
        btnPre.setEnabled(b);
        btnNext.setEnabled(b);

    }

    //  更新信息
    private void initData() {
        recevier = new MyBroadcastReceiver(); // 注册监听电量的广播
        IntentFilter intent = new IntentFilter();

        intent.addAction(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(recevier,intent);

        //实力花手势识别器
        gd = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener(){
            @Override  //长按
            public void onLongPress(MotionEvent e) {
                setStartOrPause();   //长按开始或者暂停；
                super.onLongPress(e);
            }

            @Override  //双击
            public boolean onDoubleTap(MotionEvent e) {

                return super.onDoubleTap(e);
            }

            @Override //单击
            public boolean onSingleTapConfirmed(MotionEvent e) {
                if(isHideShow) {
                    hideMediaController();
                    handler.removeCallbacksAndMessages(null);  //将所有消息移除  避免重复
                }else {
                    showMediaController();
                    handler.sendEmptyMessageDelayed(HIDE,3000);   //一显示的话就发消息，3秒后隐藏
                }
                return super.onSingleTapConfirmed(e);
            }
        });

        getWindowWidthHight();  //得到屏幕的宽和高

        seekbarVoice.setMax(maxVoice);   //设置  最大音量
        seekbarVoice.setProgress(currentVoice);

    }

    // 广播接收者
    class MyBroadcastReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            int level = intent.getIntExtra("level", 0);
            setBattery(level);
        }
    }
    //设置电量的变化
    private void setBattery(int level) {
        if(level <=0){
            ivBattery.setImageResource(R.drawable.ic_battery_0);
        }else if(level <= 10){
            ivBattery.setImageResource(R.drawable.ic_battery_10);
        }else if(level <=20){
            ivBattery.setImageResource(R.drawable.ic_battery_20);
        }else if(level <=40){
            ivBattery.setImageResource(R.drawable.ic_battery_40);
        }else if(level <=60){
            ivBattery.setImageResource(R.drawable.ic_battery_60);
        }else if(level <=80){
            ivBattery.setImageResource(R.drawable.ic_battery_80);
        }else if(level <=100){
            ivBattery.setImageResource(R.drawable.ic_battery_100);
        }else {
            ivBattery.setImageResource(R.drawable.ic_battery_100);
        }
    }

    private int preCurrentPosition ;
    private final int SHOW_NET_SPEED = 2;  //显示网速
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
             switch (msg.what){
                 case SHOW_NET_SPEED:
                     if(isNetUri) {
                         String netSpeed = utils.getNetSpeed(PlayerActivity.this);
                         tv_loading_net_speed.setText(netSpeed);
                         tv_net_speed.setText(netSpeed);
                     }
                     sendEmptyMessageDelayed(SHOW_NET_SPEED,1000); //如果卡就一直显示网速
                     break;

                 case PROGRESS:
                     int currentPosition = vv.getCurrentPosition();// 得到实时进度
                     seekbarVideo.setProgress(currentPosition);  //让进度条更新
                     tvCurrentTime.setText(utils.stringForTime(currentPosition));//文本更新播放的时长
                     tvSystemTime.setText(getSystemTime()); //设置系统时间
                     // /设置视频缓存效果

                     if(isNetUri){  //如果是网络的  就缓存下
                         int bufferPercentage = vv.getBufferPercentage();//0~100;
                         int totalBuffer = bufferPercentage*seekbarVideo.getMax();
                         int secondaryProgress =totalBuffer/100;
                         seekbarVideo.setSecondaryProgress(secondaryProgress);
                     }else{   //  不是网络的话 ，就设置缓存条为0；
                         seekbarVideo.setSecondaryProgress(0);
                     }
             //设置视频卡顿时候的效果
                     if(isNetUri && vv.isPlaying()) {
                         int duration = currentPosition - preCurrentPosition;
                         if(duration < 500) {
                             //卡
                             ll_buffering.setVisibility(View.VISIBLE);
                         }else{
                             //不卡
                             ll_buffering.setVisibility(View.GONE);
                         }
                         preCurrentPosition = currentPosition;
                     }

                     sendEmptyMessageDelayed(PROGRESS,1000);  //循环发送
                     break;
                 case HIDE:
                     hideMediaController();
                     break;
             }
        }
    };

    //得到系统的时间
    private String getSystemTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        Date date = new Date();

        return sdf.format(date);
    }

    private void findViews() {

        llTop = (LinearLayout)findViewById( R.id.ll_top );
        tvName = (TextView)findViewById( R.id.tv_name );
        ivBattery = (ImageView)findViewById( R.id.iv_battery );
        tvSystemTime = (TextView)findViewById( R.id.tv_system_time );
        btnVoice = (Button)findViewById( R.id.btn_voice );
        seekbarVoice = (SeekBar)findViewById( R.id.seekbar_voice );
        btnSwitchPlayer = (Button)findViewById( R.id.btn_switch_player );
        llBottom = (LinearLayout)findViewById( R.id.ll_bottom );
        tvCurrentTime = (TextView)findViewById( R.id.tv_current_time );
        seekbarVideo = (SeekBar)findViewById( R.id.seekbar_video );
        tvDuration = (TextView)findViewById( R.id.tv_duration );
        btnExit = (Button)findViewById( R.id.btn_exit );
        btnPre = (Button)findViewById( R.id.btn_pre );
        btnStartPause = (Button)findViewById( R.id.btn_start_pause );
        btnNext = (Button)findViewById( R.id.btn_next );
        btnSwitchScreen = (Button)findViewById( R.id.btn_switch_screen );
        utils = new Utils();
        vv = (VideoView)findViewById(R.id.vv);
        ll_buffering = (LinearLayout)findViewById(R.id.ll_buffering);
        tv_net_speed = (TextView)findViewById(R.id.tv_net_speed);
        ll_loading = (LinearLayout)findViewById(R.id.ll_loading);
        tv_loading_net_speed = (TextView)findViewById(R.id.tv_loading_net_speed);


        btnVoice.setOnClickListener( this );
        btnSwitchPlayer.setOnClickListener( this );
        btnExit.setOnClickListener( this );
        btnPre.setOnClickListener( this );
        btnStartPause.setOnClickListener( this );
        btnNext.setOnClickListener( this );
        btnSwitchScreen.setOnClickListener( this );

        am = (AudioManager) getSystemService(AUDIO_SERVICE);  //音频对象赋值
        currentVoice = am.getStreamVolume(AudioManager.STREAM_MUSIC);
        maxVoice = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

        //初始化完成的时候加载网速
        handler.sendEmptyMessage(SHOW_NET_SPEED);

    }

    @Override  // 最下面五个控件  加 声音 左右两个控件的 点击事件
    public void onClick(View v) {
        if ( v == btnVoice ) {  //点击声音

            isMute = !isMute;
            updateVoice();  //设置声音

        } else if ( v == btnSwitchPlayer ) {
            //切换 播放器
            switchPlayer();

        } else if ( v == btnExit ) {  //点击退出
            finish();
        } else if ( v == btnPre ) {   //点击上一个
            setProVideo();
            btnStartPause.setBackgroundResource(R.drawable.btn_pause_selector);
        } else if ( v == btnStartPause ) {  //暂停 播放 按钮的点击

            setStartOrPause();

        } else if ( v == btnNext ) {
            setNextVideo();
            btnStartPause.setBackgroundResource(R.drawable.btn_pause_selector);
        } else if ( v == btnSwitchScreen ) {  //点击全屏按钮的时候

            if(isFullScreen) {   //点击的时候  如果是全屏
                setVideoType(NORMAL);  //设置为默认的
            }else {
                setVideoType(FULL);  //设置为全屏
            }
        }
        handler.removeCallbacksAndMessages(null);   //触发点击事件的时候   就移除所有消息   避免与 单击（隐藏显示控制栏） 冲突；
        handler.sendEmptyMessageDelayed(HIDE,3000); //重新发送
    }

    //切换播放器
    private void switchPlayer(){
        new AlertDialog.Builder(this)
                    .setTitle("切换播放器")
                    .setMessage("是否需要切换服务器？")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                             startVitamioPlayer();
                        }
                    })
                    .setNegativeButton("取消", null)
                    .show();
    }

    //设置音量
    private void updateVoice() {
        if(isMute) {  //如果是静音的话   把音量设置为0
            am.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
            seekbarVoice.setProgress(0);
        }else {
            am.setStreamVolume(AudioManager.STREAM_MUSIC,currentVoice,0);
          seekbarVoice.setProgress(currentVoice);
        }
    }
    //设置屏幕类型
    private void setVideoType(int videoType) {
        switch (videoType){
            case FULL:
                isFullScreen = true;
                //把按钮设置为默认的
                btnSwitchScreen.setBackgroundResource(R.drawable.btn_switch_screen_default_selector);
                //设置画面为全屏显示
                vv.setVideoSize(screenWidth,screenHight);

                break;
            case NORMAL:
                isFullScreen = false;
                //把按钮设置为全屏的
                btnSwitchScreen.setBackgroundResource(R.drawable.btn_switch_screen_full_selector);
                //把画面设置为  默认 尺寸

                int height = screenHight;
                int width = screenWidth;
                int mVideoWidth = normalWidth;
                int mVideoHeight = normalHight;

                if (mVideoWidth * height < width * mVideoHeight) {
                    //Log.i("@@@", "image too wide, correcting");
                    width = height * mVideoWidth / mVideoHeight;
                } else if (mVideoWidth * height > width * mVideoHeight) {
                    //Log.i("@@@", "image too tall, correcting");
                    height = width * mVideoHeight / mVideoWidth;
                }
                vv.setVideoSize(width, height);

                break;
        }
    }

    //设置播放前一个
    private void
    setProVideo() {
        position--;
        if(position >= 0) {
            LocalVideoInfo localVideoInfo = videoInfos.get(position);

            isNetUri = utils.isNetUri(localVideoInfo.getData());
            ll_loading.setVisibility(View.VISIBLE);

            vv.setVideoPath(localVideoInfo.getData());
            tvName.setText(localVideoInfo.getName());

            setButtonStatus();  //设置按钮状态
        }
    }
    //监听系统按键的调节音量
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            currentVoice--;
            updateVoiceProgress(currentVoice);
            handler.removeMessages(HIDE);
            handler.sendEmptyMessageDelayed(HIDE, 4000);
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            currentVoice++;
            updateVoiceProgress(currentVoice);
            handler.removeMessages(HIDE);
            handler.sendEmptyMessageDelayed(HIDE, 4000);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    @Override
    protected void onDestroy() {

        if(handler != null){
            //把所有消息移除
            handler.removeCallbacksAndMessages(null);
            handler = null;
        }
        //取消注册
        if(recevier != null){
            unregisterReceiver(recevier);
            recevier = null;
        }
        super.onDestroy();
    }
}
