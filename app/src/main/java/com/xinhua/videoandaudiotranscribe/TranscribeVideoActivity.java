package com.xinhua.videoandaudiotranscribe;

import android.hardware.Camera;
import android.media.AudioManager;
import android.media.CamcorderProfile;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.widget.Chronometer;
import android.widget.Toast;

/**
 *
 */
public class TranscribeVideoActivity extends AppCompatActivity {

    private static final String TAG = "TranscribeVideoActivity";

    private SurfaceView surfaceView;
    private Chronometer chronometer;
    private boolean mIsPlay = false;  //是否正在播放
    private boolean mStartedFlg = false;  //是否正在录制
    private MediaRecorder mRecorder;  //录制视频对象
    private MediaPlayer mediaPlayer;  //播放视频对象
    private Camera camera;
    private SurfaceHolder mSurfaceHolder;
    private String path;  //录制文件的存储路径

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_transcribe_video);
        initView();
    }

    private void initView() {
        surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        chronometer = (Chronometer) findViewById(R.id.chronometer);
        chronometer.setBase(SystemClock.elapsedRealtime());  //设置计时器的起始时间
        mSurfaceHolder = surfaceView.getHolder();
        mSurfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                mSurfaceHolder = holder;
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                mSurfaceHolder = holder;
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                surfaceView = null;
                mSurfaceHolder = null;
                chronometer.stop();

                if (mRecorder != null) {
                    mRecorder.release();
                    mRecorder = null;
                }
                if (camera != null) {
                    camera.stopPreview();
                    camera.release();
                    camera = null;
                }
                if (mediaPlayer != null){
                    mediaPlayer.release();
                    mediaPlayer = null;
                }
            }
        });
        // setType必须设置，要不出错.
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }


    /**
     * 开始/停止录制
     */
    public void onToggle(View v){

        if (mIsPlay) {  //如果正在播放停止播放去录制
            if (mediaPlayer != null) {
                mIsPlay = false;
                mediaPlayer.stop();
                mediaPlayer.reset();
                mediaPlayer.release();
                mediaPlayer = null;
            }
        }
        if (!mStartedFlg) { //不是在录制则开始录制

            if (mRecorder == null) {
                mRecorder = new MediaRecorder();
            }
            try {
                camera = Camera.open(0);
                if (camera != null) {
                    camera.setPreviewDisplay(mSurfaceHolder);
                    camera.cancelAutoFocus();//此句加上 可自动聚焦 必须加
                    camera.setDisplayOrientation(90); //控制摄像头竖屏拍摄
                    camera.startPreview();
                    camera.unlock();
                    mRecorder.setCamera(camera);
                    //如果需要设置摄像头参数可有获取摄像头参数对象去设置
                }

                // 这两项需要放在setOutputFormat之前
                mRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
                mRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

                // Set output file format
                mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);

                // 这两项需要放在setOutputFormat之后
                mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                mRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.MPEG_4_SP);

                CamcorderProfile profile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
                mRecorder.setVideoSize(profile.videoFrameWidth, profile.videoFrameHeight);//视频分辨率
                mRecorder.setVideoFrameRate(30);  //播放时的帧/s
                //对视频编码的大小，值越大视频越清晰，这种大小录制20s大小为6.28MB
                mRecorder.setVideoEncodingBitRate(3 * 1024 * 1024);
                mRecorder.setOrientationHint(90); //设置竖屏

                mRecorder.setMaxDuration(30 * 60 * 1000); //设置记录会话的最大持续时间（毫秒）
                mRecorder.setPreviewDisplay(mSurfaceHolder.getSurface());

                //获取SD卡根目录
                path = Utils.getOutputPath(this,"video" + Utils.getDate() + ".mp4");
                mRecorder.setOutputFile(path);//设置录制的文件输出路径
                mRecorder.prepare(); //准备录制
                mRecorder.start(); //开始录制
                mStartedFlg = true;  //设置当前状态为正在录制
                chronometer.setBase(SystemClock.elapsedRealtime());
                chronometer.start(); //开始计时

            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "onToggle: " + e.toString() );
                mStartedFlg = false;
            }
        } else {
            //stop
            if (mStartedFlg) {  //正在录制则停止
                try {
                    chronometer.stop(); //停止计时
                    mRecorder.stop();
                    mRecorder.reset();
                    mRecorder.release();
                    mRecorder = null;
                    if (camera != null) {
                        camera.release();
                        camera = null;
                    }
                    mStartedFlg = false;
                } catch (Exception e) {
                    e.printStackTrace();
                    mStartedFlg = true;
                }
            }
        }
    }

    /**
     * 播放
     */
    public void onPlay(View v){
        if(!mStartedFlg && path!=null){ //没有在录制并且Path不为空
            mIsPlay = true;
            if (mediaPlayer == null) {
                mediaPlayer = new MediaPlayer();

            }
            mediaPlayer.reset();
            Uri uri = Uri.parse(path);
            mediaPlayer = MediaPlayer.create(this, uri);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setDisplay(mSurfaceHolder);  //关联MediaPlayer和SurfaceView;
            try{
                mediaPlayer.prepare();
            }catch (Exception e){
                e.printStackTrace();
            }
            mediaPlayer.start(); //开始播放
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    chronometer.stop();
                }
            });
            chronometer.setBase(SystemClock.elapsedRealtime());//开始计时
            chronometer.start();
        }else{
            Toast.makeText(this, "正在录制...", Toast.LENGTH_SHORT).show();
        }
    }

}
