package com.xinhua.videoandaudiotranscribe;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int TRANSCRIBE_VIDEO = 1;
    private static final int REQUEST_CAMERA_PERMISSION = 2;
    private static final int SELECT_VIDEO = 3;
    private static final int SELECT_IMAGE = 4;
    private static final int CAMERA_PICTURE1 = 5;
    private static final int CAMERA_PICTURE2 = 6;
    private ImageView image;
    private SurfaceView surfaceView;
    private SurfaceHolder mHolder;
    private MediaPlayer mediaPlayer;
    private String videoPath;
    private String picPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }
    private void initView() {
        image = (ImageView) findViewById(R.id.image);
        surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        mHolder = surfaceView.getHolder();
        mHolder.setKeepScreenOn(true);
        surfaceView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mediaPlayer != null){
                    if(mediaPlayer.isPlaying()){
                        mediaPlayer.pause();
                    }else{
                        mediaPlayer.start();
                    }
                }
            }
        });

        // mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    @Override
    protected void onStart() {
        super.onStart();
        //每次页面跳转之后SurfaceHolder就会被销毁，所有把它放到onStart()中
        mHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                mHolder = holder;
                if(videoPath!=null){
                    playVideo();
                }
            }
            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                mHolder = holder;
            }
            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
            }
        });
    }

    /**
     * 录制视频
     */
    public void transcribeVideo(View view) {
        //如果当前版本大于等于6.0，申请运行时权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkPermission(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO)) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO}, REQUEST_CAMERA_PERMISSION);
            } else {
                //方法一：调用系统的Camera
                //startCameraIntent();
                //方法二：使用MediaRecorder类
                startActivity(new Intent(this, TranscribeVideoActivity.class));
            }
        } else {  //不大于6.0直接调用
            //方法一：调用系统的Camera
            //startCameraIntent();
            //方法二：使用MediaRecorder类
            startActivity(new Intent(this, TranscribeVideoActivity.class));
        }
    }

    /**
     * 录制音频
     */
    public void transcribeAudio(View view) {
        startActivity(new Intent(this, TranscribeAudioActivity.class));
    }

    /**
     * 拍摄照片 两种方法
     */
    public void cameraPhoto(View view) {
        startCamera();

    }

    /**
     * 播放视频
     */
    public void playVideo(View view) {

        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, SELECT_VIDEO);
    }

    /**
     * 播放音乐
     *
     */
    public void playAudio(View view) {
        List<Music> musics = Utils.getMusics(this);
        Log.e(TAG, "playAudio: " + musics.toString() );
        //得到手机上的音乐列表，下面有listView展示，如果要播放可以用
        //android 上带着的app 或者自己用MediaPlayer播放
    }

    /**
     * 选择照片
     */
    public void choosePhoto(View view) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, SELECT_IMAGE);

    }

    /**
     * 开启录像
     */
    private void startCameraIntent() {
        //开启Camera的intent
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        intent.putExtra (MediaStore.EXTRA_DURATION_LIMIT,30);//设置视频录制的最长时间
        //如果保存到自己指定的目录，那么拍摄后的uri是不能通过内容提供者解析的，可以把保存路径创建为成员变量
//        File saveFile = getOutputPath("temp" + new Date().getTime() + ".mp4");
//        Uri contentUri;
//        if (Build.VERSION.SDK_INT > 23) {
//            /**Android 7.0以上得到保存文件Uri的方式**/
//            contentUri = FileProvider.getUriForFile(this, "com.xinhua.videoandaudiotranscribe", saveFile);
//            grantUriPermission(getPackageName(), contentUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
//        }else{
//           contentUri =  Uri.fromFile(saveFile);
//        }
//        intent.putExtra(MediaStore.EXTRA_OUTPUT, contentUri); //创建保存图片的文件
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1); //设置视频录制的画质
        startActivityForResult(intent, TRANSCRIBE_VIDEO);
    }

    /**
     * 开启相机
     */
    private void startCamera() {
        //方法一：直接启动相机，得到的是一个缩略图
        Intent intent1 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent1, CAMERA_PICTURE1);

        //方法二：为拍摄的照片指定路径，得到的是原图
//        Intent intent2 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        picPath = Utils.getOutputPath(this,"pic" + Utils.getDate() + ".png");
//        Uri uri;
//        if (Build.VERSION.SDK_INT > 23) {
//            /**Android 7.0以上得到保存文件Uri的方式**/
//            uri = FileProvider.getUriForFile(this, "com.xinhua.videoandaudiotranscribe", new File(picPath));
//            grantUriPermission(getPackageName(), uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
//        }else{
//           uri =  Uri.fromFile(new File(picPath));
//        }
//        //为拍摄的图片指定一个存储的路径
//        intent2.putExtra(MediaStore.EXTRA_OUTPUT, uri);
//        startActivityForResult(intent2, CAMERA_PICTURE2);
    }

    /**
     * 检查权限是否授予
     */
    private boolean checkPermission(String... permissions) {

        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(MainActivity.this, permission) != PackageManager.PERMISSION_GRANTED) {
                return true;
            }
        }
        return false;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(data == null){
            return;
        }

        if (resultCode == Activity.RESULT_OK){
            switch (requestCode) {
                case TRANSCRIBE_VIDEO:
                    //调用系统拍摄后的视频存储路径
                    videoPath = GetPathFromUri4kitkat.getPath(this, data.getData());
                    break;
                case SELECT_VIDEO:
                    videoPath = GetPathFromUri4kitkat.getPath(this, data.getData());
                    break;
                case CAMERA_PICTURE1:
                    Bundle bundle = data.getExtras();
                    Bitmap camera_pic = (Bitmap) bundle.get("data");
                    image.setImageBitmap(camera_pic);
                    break;
                case CAMERA_PICTURE2:
                    Bitmap camera_pic2 = BitmapFactory.decodeFile(picPath);
                    image.setImageBitmap(camera_pic2);
                    break;
                case SELECT_IMAGE:
                    String imagePath = GetPathFromUri4kitkat.getPath(this, data.getData());
                    Bitmap select_pic = BitmapFactory.decodeFile(imagePath);
                    image.setImageBitmap(select_pic);
                    break;
            }
        }
    }

    /**
     * 播放视频,必须确定SurfaceHolder已经创建出来
     */
    private void playVideo() {
        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
        }else{
            mediaPlayer.reset();
        }
        try{
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setDataSource(videoPath);
            // 把视频输出到SurfaceView上
            mediaPlayer.setDisplay(mHolder);
            mediaPlayer.prepare();
            mediaPlayer.start();
        }catch (Exception e){
            Log.e(TAG, "onActivityResult: " + e.toString() );
        }
    }

    /**
     * 请求授权结果处理
     */
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CAMERA_PERMISSION:
                if (grantResults.length >= 2) {
                    if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, "你未授权相机权限，无法拍摄", Toast.LENGTH_SHORT).show();
                    }
                    if (grantResults[1] != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, "你未授权存储权限，录像无法存储", Toast.LENGTH_SHORT).show();
                    }
                    if (grantResults[2] != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, "你未授权话筒权限，录像无法录音", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    //方法一：调用系统的Camera
                    //startCameraIntent();
                    //方法二：使用MediaRecorder类
                    startActivity(new Intent(this, TranscribeVideoActivity.class));
                }
                break;
            default:

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        surfaceView = null;
        mHolder = null;

        if (mediaPlayer != null){
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
