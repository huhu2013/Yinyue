package com.yangjiahua.yinyue;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


public class YinyueActivity extends Activity implements View.OnClickListener,MediaPlayer.OnCompletionListener{
    private ImageButton fenxiang;
    private TextView gqming;
    private TextView zuozhe;
    private ImageButton huxiantuichu;
    private TextView guocheng;
    private TextView zongji;
    private ImageButton qiehuan;
    private ImageButton shangyiqu;
    private ImageButton bofanghuozanting;
    private ImageButton xiayiqu;
    private ImageButton liebiao;
    private DiscreteSeekBar discreteSeekBar;
    private List<MusicInfo> infos;
    private int index;
    private MyServiceConnection conn;
    private MusicService mService;
    private MediaPlayer musicPlayer;
    private Animation animationRight;
    private int state=1;
    private int cishu=0;
    private boolean shouci = true;
    private LrcView mLrcView;
    Handler mHandler = new Handler();
    Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
               discreteSeekBar.setProgress(musicPlayer.getCurrentPosition());
                guocheng.setText(getTimeFromInt(musicPlayer.getCurrentPosition()));
                mHandler.postDelayed(mRunnable, 1000);

        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.content_yinyue);
            initView();
            Intent intent = new Intent(this,MusicService.class);
            infos = new ArrayList<MusicInfo>();
            startService(intent);
            conn = new MyServiceConnection();
            bindService(intent, conn, Context.BIND_AUTO_CREATE);

            Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);

            while(cursor.moveToNext())
            {
                MusicInfo info = new MusicInfo();
                String fname = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                info.setfName(fname);
                String fpath = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                info.setfPath(fpath);
                int duration = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
                info.setDuration(getTimeFromInt(duration));
                String author = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                info.setAuthor(author);
                infos.add(info);
            }
            discreteSeekBar.setOnProgressChangeListener(new DiscreteSeekBar.OnProgressChangeListener() {
                @Override
                public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {
                        discreteSeekBar.setIndicatorFormatter(getTimeFromInt(value));
                        mLrcView.seekTo(value, true, fromUser);
                        if(fromUser)
                    {
                        musicPlayer.seekTo(discreteSeekBar.getProgress());
                    }
                }

                @Override
                public void onStartTrackingTouch(DiscreteSeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(DiscreteSeekBar seekBar) {

                }
            });
        DiscreteSeekBar discreteSeekBar = (DiscreteSeekBar) findViewById(R.id.seekBar1);
        discreteSeekBar.setNumericTransformer(new DiscreteSeekBar.NumericTransformer() {
            @Override
            public int transform(int value) {
                return value * 100;
            }
        });
    }

    private void initView() {
           fenxiang = (ImageButton) findViewById(R.id.btn_fenxiang);
           gqming = (TextView) findViewById(R.id.edt_gqming);
           huxiantuichu = (ImageButton) findViewById(R.id.btn_huxiantuichu);
           guocheng = (TextView) findViewById(R.id.textViewGuocheng);
           zongji = (TextView) findViewById(R.id.textViewZongji);
           qiehuan = (ImageButton) findViewById(R.id.btn_qiehuan);
           shangyiqu = (ImageButton) findViewById(R.id.btn_shangyiqu);
           bofanghuozanting = (ImageButton) findViewById(R.id.btn_bofanghuozanting);
           xiayiqu = (ImageButton) findViewById(R.id.btn_xiayiqu);
           liebiao = (ImageButton) findViewById(R.id.btn_liebiao);
           discreteSeekBar = (DiscreteSeekBar) findViewById(R.id.seekBar1);
           mLrcView = (LrcView) findViewById(R.id.lrcView);
           mLrcView.setOnSeekToListener(onSeekToListener);
           mLrcView.setOnLrcClickListener(onLrcClickListener);
           bofanghuozanting.setOnClickListener(this);
           shangyiqu.setOnClickListener(this);
           xiayiqu.setOnClickListener(this);
           liebiao.setOnClickListener(this);
           animationRight = AnimationUtils.loadAnimation(this,R.anim.right);
           gqming.setAnimation(animationRight);
           animationRight.start();
           animationRight.setRepeatCount(Animation.INFINITE);
           qiehuan.setOnClickListener(this);

    }
    LrcView.OnLrcClickListener onLrcClickListener = new LrcView.OnLrcClickListener() {
        @Override
        public void onClick() {
           // Toast.makeText(getApplicationContext(),"亲，您点击了歌词哦",Toast.LENGTH_SHORT).show();
        }
    };
    LrcView.OnSeekToListener onSeekToListener = new LrcView.OnSeekToListener() {
        @Override
        public void onSeekTo(int progress) {
            musicPlayer.seekTo(progress);
        }
    };
    public static String getTimeFromInt(int time) {

        if (time <= 0) {
            return "00:00";
        }
        int million = (time / 1000) / 60;
        int second = (time / 1000) % 60;
        String m = million>=10? String.valueOf(million):"0"+String.valueOf(million);
        String s = second >=10 ? String.valueOf(second):"0"+String.valueOf(second);
        return m + ":" + s;
    }
    public void playMusic(int status){
        switch (status)
        {
            case 1://上一曲
                bofanghuozanting.setBackgroundResource(R.drawable.btn_bofanglv);
                index--;
                if (index<0){
                    index = infos.size()-1;
                }
                break;
            case 2://下一曲
                bofanghuozanting.setBackgroundResource(R.drawable.btn_bofanglv);
                index++;
                if (index>infos.size()-1)
                {
                    index = 0;
                }
                break;

        }

        if (musicPlayer!=null){

                    mService.play(infos.get(index).getfPath());
                    mLrcView.setLrcRows(getLrcRows(infos.get(index).getfPath()));
                    zongji.setText(getTimeFromInt(musicPlayer.getDuration()));
                    discreteSeekBar.setMax(musicPlayer.getDuration());
                    gqming.setText(infos.get(index).getfName() + "—" + infos.get(index).getAuthor());



        }
    }
    public void playDanqu(){
        musicPlayer.start();
        mLrcView.setLrcRows(getLrcRows(infos.get(index).getfPath()));
    }
    public void playSuiji(){

            index = getRandomIndex(infos.size() - 1);
            mService.play(infos.get(index).getfPath());
            mLrcView.setLrcRows(getLrcRows(infos.get(index).getfPath()));
            zongji.setText(getTimeFromInt(musicPlayer.getDuration()));
            discreteSeekBar.setMax(musicPlayer.getDuration());
            gqming.setText(infos.get(index).getfName() + "—" + infos.get(index).getAuthor());


    }
    public void playShunxu(){
              index++;
             if (index <= infos.size() - 1) {

                mService.play(infos.get(index).getfPath());
                 mLrcView.setLrcRows(getLrcRows(infos.get(index).getfPath()));
                zongji.setText(getTimeFromInt(musicPlayer.getDuration()));
                discreteSeekBar.setMax(musicPlayer.getDuration());
                gqming.setText(infos.get(index).getfName() + "—" + infos.get(index).getAuthor());
             }

        else
       {
           index = -1;
           mService.pause();
           zongji.setText(getTimeFromInt(0));
           discreteSeekBar.setMax(0);
           guocheng.setText(getTimeFromInt(0));
           gqming.setText("现在没有任何正在播放的歌曲");
           bofanghuozanting.setBackgroundResource(R.drawable.btn_zantinglv);
           mHandler.removeCallbacks(mRunnable);
       }

    }
    public int getRandomIndex(int end){
        int ppt = (int)(Math.random()*end);
        return ppt;
    }
    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.btn_bofanghuozanting:

                if (shouci==true)
                {
                        mHandler.post(mRunnable);
                        shouci = false;
                        playMusic(0);
                        mLrcView.setLrcRows(getLrcRows(infos.get(0).getfPath()));
                        bofanghuozanting.setBackgroundResource(R.drawable.btn_bofanglv);

                }
                else
                {
                    shouci = false;
                }
                if(mHandler.post(mRunnable)==false) {
                    mHandler.post(mRunnable);
                }
                  if (musicPlayer.isPlaying()) {
                        mService.pause();
                        bofanghuozanting.setBackgroundResource(R.drawable.btn_zantinglv);
                    }
                   else{
                                 if (index == -1) {
                                  index++;
                                  mService.play(infos.get(index).getfPath());
                                  mLrcView.setLrcRows(getLrcRows(infos.get(index).getfPath()));
                                  zongji.setText(getTimeFromInt(musicPlayer.getDuration()));
                                  discreteSeekBar.setMax(musicPlayer.getDuration());
                                  gqming.setText(infos.get(index).getfName() + "—" + infos.get(index).getAuthor());
                                }

                      mService.continu();
                      bofanghuozanting.setBackgroundResource(R.drawable.btn_bofanglv);
                      }

                break;
            case R.id.btn_shangyiqu:
               playMusic(1);
                break;
            case R.id.btn_xiayiqu:
               playMusic(2);
                break;
            case R.id.btn_qiehuan:
                if (cishu%4==0)
                {
                    qiehuan.setBackgroundResource(R.drawable.btn_zhengchanglv);
                    state = 1;
                    if (cishu!=0) {
                        Toast.makeText(this, "顺序播放", Toast.LENGTH_SHORT).show();
                    }
                    if (cishu==0)
                    {
                        state = 2;
                        qiehuan.setBackgroundResource(R.drawable.btn_suijilv);
                        Toast.makeText(this,"随机播放",Toast.LENGTH_SHORT).show();
                    }
                }
                if (cishu%4==1)
                {
                    qiehuan.setBackgroundResource(R.drawable.btn_suijilv);
                    state = 2;
                    Toast.makeText(this,"随机播放",Toast.LENGTH_SHORT).show();
                }
                if (cishu%4==2)
                {
                    qiehuan.setBackgroundResource(R.drawable.btn_xunhuanlv);
                    state = 3;
                    Toast.makeText(this,"循环播放",Toast.LENGTH_SHORT).show();
                }
                if (cishu%4==3)
                {
                    qiehuan.setBackgroundResource(R.drawable.btn_danqulv);
                    state = 4;
                    Toast.makeText(this,"单曲循环",Toast.LENGTH_SHORT).show();
                }
                cishu++;
                break;
            case R.id.btn_liebiao:
                MyUser.logOut(YinyueActivity.this);
                break;
        }

    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        mLrcView.reset();
        if (state==1){
          playShunxu();
        }
        if (state==2)
        {
            playSuiji();
        }
        if (state==3) {
            playMusic(2);
        }
        if (state==4)
        {
            playDanqu();
        }
    }

    class MyServiceConnection implements ServiceConnection{

       @Override
       public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MyBinder binder = (MusicService.MyBinder) service;
            mService = binder.getService();
            musicPlayer = mService.player;
            musicPlayer.setOnCompletionListener(YinyueActivity.this);
           bofanghuozanting.setBackgroundResource(R.drawable.btn_zantinglv);
       }

       @Override
       public void onServiceDisconnected(ComponentName name) {

       }

   }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (conn!=null)
        {
            unbindService(conn);
        }
        mHandler.removeCallbacks(mRunnable);
        mLrcView.reset();

    }
    private List<LrcRow> getLrcRows(String path){
        List<LrcRow> rows = null;
        File f = new File(path.replace(".mp3",".lrc"));
        String line ;
        StringBuffer sb = new StringBuffer();
        try {
            FileInputStream fis = new FileInputStream(f);
            InputStreamReader isr = new InputStreamReader(fis,"utf-8");
            BufferedReader br = new BufferedReader(isr);
            while((line = br.readLine()) != null){
                sb.append(line+"\n");
            }
            rows = DefaultLrcParser.getIstance().getLrcRows(sb.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rows;
    }

}
