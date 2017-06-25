package com.xinhua.videoandaudiotranscribe;

import android.content.ContentValues;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;

public class TranscribeAudioActivity extends AppCompatActivity {

    private static final String TAG = "TranscribeAudioActivity";

    private ImageView mIv_microphone;
    private Chronometer chronometer;
    private MediaRecorder mRecorder;
    private MediaPlayer mPlayer;
    private String path;
    private boolean isTranscribe = false;
    private boolean isPlay = false;
    private Uri fileUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transcribe_audio);
        initView();
    }

    private void initView() {
        mIv_microphone = (ImageView) findViewById(R.id.iv_microphone);
        chronometer = (Chronometer) findViewById(R.id.chronometer);
        mIv_microphone.setEnabled(false);
        chronometer.setBase(SystemClock.elapsedRealtime());
    }

    public void onToggle(View view){
        if(isPlay){  //如果正在播放，停止播放，去录音
            if(mPlayer != null){
                mPlayer.stop();
                mPlayer.reset();
                mIv_microphone.setEnabled(false);
                isPlay = false;
            }
        }
        if(!isTranscribe){
            if(mRecorder == null){
                mRecorder = new MediaRecorder();
            }
            try {
                //这些设置操作不能放在new MediaRecorder的if块中，否则在播放音频的时候开启录音会报错
                mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);  //音频来源
                mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP); //支持格式
                mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);  //音频编码
                path = Utils.getOutputPath(this,"audio" + Utils.getDate() + ".3gp");
                mRecorder.setOutputFile(path); //设置音频文件输出位置
                mRecorder.prepare();
                mRecorder.start(); //开始录制
                chronometer.setBase(SystemClock.elapsedRealtime());
                chronometer.start();//启动计时
                isTranscribe = true;
                mIv_microphone.setEnabled(true);
            } catch (Exception e) {
                Log.e(TAG, "onToggle: " + e.toString() );
                isTranscribe = false;
            }
        }else{ //正在录制，点击停止录制
            mRecorder.stop(); //停止录制
            isTranscribe = false;
            chronometer.stop(); //停止计时
            mIv_microphone.setEnabled(false);

            //在系统存放录音的地方也保存一份
            ContentValues values = new ContentValues();
            values.put(MediaStore.Audio.Media.TITLE, "this is my first record-audio");
            values.put(MediaStore.Audio.Media.DATE_ADDED, System.currentTimeMillis());
            values.put(MediaStore.Audio.Media.DATA, path);
            fileUri = this.getContentResolver().insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values);
        }
    }

    public void onPlay(View view){
        if(isTranscribe){
            Toast.makeText(this, "正在录音...", Toast.LENGTH_SHORT).show();
            return;
        }
        if(mPlayer == null) mPlayer = new MediaPlayer();
        else mPlayer.reset();
        try {
            mPlayer.setDataSource(path); //播放自己存放路径的音频
//            mPlayer = MediaPlayer.create(this, fileUri); //播放系统存放路径的音频
            mPlayer.prepare();
            mPlayer.start();
            isPlay = true;
            mIv_microphone.setEnabled(true);
            chronometer.setBase(SystemClock.elapsedRealtime());
            chronometer.start();
        } catch (Exception e) {
            Log.e(TAG, "onPlay: " + e.toString() );
        }

        mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                isPlay = false;
                chronometer.stop();
                mIv_microphone.setEnabled(false);
            }
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mRecorder != null){
            mRecorder.release();
            mRecorder = null;
        }

        if(mPlayer != null){
            mPlayer.release();
            mPlayer = null;
        }


    }
}
