package com.xinhua.videoandaudiotranscribe;

import android.content.Context;
import android.database.Cursor;
import android.os.Environment;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static android.provider.MediaStore.Audio.Media.*;

/**
 * @author : 李康利
 *         创建日期 :  2017/6/22.
 *         类的作用 : 随便写的一个工具类
 */

public class Utils {

    public static String getOutputPath(Context context, String fileName){
        File saveDir;
        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)
                || !Environment.isExternalStorageRemovable()){
            saveDir = context.getExternalFilesDir(Environment.DIRECTORY_DCIM);
        }else{
            saveDir = context.getFilesDir();
        }
        if(saveDir != null && !saveDir.exists()){
            saveDir.mkdirs();
        }
        File saveFile = new File(saveDir, fileName);
        return saveFile.getAbsolutePath();
    }

    /**
     * 获取系统时间
     */
    public static String getDate() {
        Calendar ca = Calendar.getInstance();
        int year = ca.get(Calendar.YEAR);           // 获取年份
        int month = ca.get(Calendar.MONTH);         // 获取月份
        int day = ca.get(Calendar.DATE);            // 获取日
        int minute = ca.get(Calendar.MINUTE);       // 分
        int hour = ca.get(Calendar.HOUR);           // 小时
        int second = ca.get(Calendar.SECOND);       // 秒

        return "" + year + (month + 1) + day + "-" + hour + minute + second;
    }

    /**
     * 为了格式化音乐时长
     * @param duration 传入毫秒数，转为MM:SS格式输出
     */
    public static String formatTiem(long duration){
        int second = 60; //1秒
        int minute = 60 * 60;  //1分钟
        if(duration < second){
            return "00:01";
        }
        if(duration < minute){
            int time = (int) (duration / second + 1);
            return "00:" + time;
        }
        int minuteTime = (int) (duration / minute);
        int secondTime = (int) ((duration%minute) /second + 1);

        return minuteTime + ":" + secondTime;
    }

    /**
     * 获取手机中的音乐文件
     * 注意：在子线程操作
     */
    public static List<Music> getMusics(Context context){
        Cursor cursor=context.getContentResolver().query(
                EXTERNAL_CONTENT_URI, null, null, null,
                DEFAULT_SORT_ORDER);
        List<Music> musicList =new ArrayList<>();
        if(cursor!=null){
            while(cursor.moveToNext()) {
                long id = cursor.getLong(cursor
                        .getColumnIndex(_ID)); // 音乐id
                String title = cursor.getString((cursor
                        .getColumnIndex(TITLE))); // 音乐标题
                String artist = cursor.getString(cursor
                        .getColumnIndex(ARTIST)); // 艺术家
                String album = cursor.getString(cursor
                        .getColumnIndex(ALBUM)); // 专辑
                String displayName = cursor.getString(cursor
                        .getColumnIndex(DISPLAY_NAME)); //显示名称
                long albumId = cursor.getInt(cursor
                        .getColumnIndex(ALBUM_ID));  //专辑id
                long duration = cursor.getLong(cursor
                        .getColumnIndex(DURATION)); // 时长
                long size = cursor.getLong(cursor
                        .getColumnIndex(SIZE)); // 文件大小
                String url = cursor.getString(cursor
                        .getColumnIndex(DATA)); // 文件路径
                int isMusic = cursor.getInt(cursor
                        .getColumnIndex(IS_MUSIC)); // 是否为音乐
                if (isMusic != 0) {
                    Music music = new Music();
                    music.setId(id);
                    music.setTitle(title);
                    music.setArtist(artist);
                    music.setAlbum(album);
                    music.setDisplayName(displayName);
                    music.setAlbumId(albumId);
                    music.setDuration(duration);
                    music.setSize(size);
                    music.setUrl(url);
                    musicList.add(music);
                }
            }
        }
        return musicList;
    }
}
