package com.yangjiahua.yinyue;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import cn.bmob.sms.BmobSMS;


public class MainActivity extends Activity implements View.OnClickListener
        {
    private Button btnzhuce;
    private Button btndenglu;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_main);

        btnzhuce = (Button) findViewById(R.id.btn_zhuce);
        btndenglu = (Button) findViewById(R.id.btn_denglu);
        btnzhuce.setOnClickListener(this);
        btndenglu.setOnClickListener(this);
        BmobSMS.initialize(MainActivity.this, "5ff17b39552723beb03fe6a7de8e8ff2");
    }

   @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_zhuce:
                Intent intent1 = new Intent(MainActivity.this,Zhuce.class);
                startActivity(intent1);
                overridePendingTransition(R.anim.you_zuo_in,R.anim.you_zuo_out);
                break;
            case R.id.btn_denglu:
                Intent intent2 = new Intent(MainActivity.this,Denglu.class);
                startActivity(intent2);
                overridePendingTransition(R.anim.you_zuo_in,R.anim.you_zuo_out);
                break;
        }

    }

    private long exitTime = 0;
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
           if ((System.currentTimeMillis()-exitTime) > 2000)
           {
               Toast.makeText(getApplicationContext(), "再按一次退出程序", Toast.LENGTH_SHORT).show();
               exitTime = System.currentTimeMillis();
           }
            else
           {
               finish();
               overridePendingTransition(R.anim.danchu,0);
               System.exit(1);
           }
            return false;
        }

        return super.onKeyDown(keyCode, event);
    }
}
