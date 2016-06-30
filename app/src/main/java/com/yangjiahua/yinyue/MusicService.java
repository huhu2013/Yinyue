package com.yangjiahua.yinyue;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;

public class MusicService extends Service{
    public MyBinder binder = new MyBinder();
    public MediaPlayer player = new MediaPlayer();
    public MusicService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
       return binder;
    }



    public class MyBinder extends Binder{
       public MusicService getService(){
           return MusicService.this;
       }
    }

    public void play(String fpath){
        player.reset();
        try
        {
            player.setDataSource(fpath);
            player.prepare();
            player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    player.start();
                }
            });
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    public void pause(){
        if (player.isPlaying()){
            player.pause();
        }
    }
    public void continu(){
        player.start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }
}
