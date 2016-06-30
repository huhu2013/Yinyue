package com.yangjiahua.yinyue;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import cn.bmob.sms.BmobSMS;
import cn.bmob.v3.listener.SaveListener;

public class Shengfenzhuce extends Activity implements View.OnClickListener,View.OnFocusChangeListener{
    private EditText yonghuming;
    private EditText mima1;
    private EditText mima2;
    private EditText mail;
    private Button queren;
    private String account;
    private String password;
    private static final String MY_PRIVATE = "MY_PRIVATE";
    private static final String PRIVATE_ACCOUNT = "PRIVATE_ACCOUNT";
    private static final String PRIVATE_PASSWORD = "PRIVATE_PASSWORD";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_shengfenzhuce);
        yonghuming = (EditText) findViewById(R.id.edt_yonghuming);
        mima1 = (EditText) findViewById(R.id.edt_mima1);
        mima2 = (EditText) findViewById(R.id.edt_mima2);
        mail = (EditText) findViewById(R.id.edt_mail);
        queren = (Button) findViewById(R.id.btn_queren);
        yonghuming.setOnFocusChangeListener(this);
        mima1.setOnFocusChangeListener(this);
        mima2.setOnFocusChangeListener(this);
        mail.setOnFocusChangeListener(this);
        queren.setOnClickListener(this);
        BmobSMS.initialize(Shengfenzhuce.this, "5ff17b39552723beb03fe6a7de8e8ff2");
    }

    @Override
    public void onClick(View v) {
        MyUser myUser = new MyUser();
        if (yonghuming.getText().length()==0&&mima1.getText().length()==0&&mima2.getText().length()==0) {
            Toast.makeText(Shengfenzhuce.this, "请输入用户名和密码", Toast.LENGTH_SHORT).show();
        }
        if (yonghuming.getText().length()==0&&mima1.getText().length()==0&&mima2.getText().length()>0)
        {
            Toast.makeText(Shengfenzhuce.this,"请输入用户名和密码",Toast.LENGTH_SHORT).show();
        }
        if (yonghuming.getText().length()==0&&mima1.getText().length()>0&&mima2.getText().length()==0)
        {
            Toast.makeText(Shengfenzhuce.this,"请输入用户名和密码",Toast.LENGTH_SHORT).show();
        }
        if (yonghuming.getText().length()>0&&mima1.getText().length()==0&&mima2.getText().length()==0)
        {
            Toast.makeText(Shengfenzhuce.this,"请输入密码",Toast.LENGTH_SHORT).show();
        }
        if (yonghuming.getText().length()==0&&mima1.getText().length()>0&&mima2.getText().length()>0)
        {
            Toast.makeText(Shengfenzhuce.this,"请输入用户名",Toast.LENGTH_SHORT).show();
        }
        if (yonghuming.getText().length()>0&&!(mima1.getText().toString().equals(mima2.getText().toString())))
        {
            Toast.makeText(Shengfenzhuce.this,"两次输入的密码不一致，请重新输入",Toast.LENGTH_SHORT).show();
        }
        if (yonghuming.getText().length()>0&&(mima1.getText().toString().equals(mima1.getText().toString())&&!(mima1.getText().length()>=6&&
                                                       mima1.getText().length()<=12)));
        {
            if (mima1.getText().length()<6)
            {
                Toast.makeText(Shengfenzhuce.this,"密码的长度不够",Toast.LENGTH_SHORT).show();
            }
            if (mima1.getText().length()>12)
            {
                Toast.makeText(Shengfenzhuce.this,"密码过于冗长",Toast.LENGTH_SHORT).show();
            }
        }
        if (yonghuming.getText().length()>0&&(mima1.getText().toString().equals(mima1.getText().toString())&&(mima1.getText().length()>=6&&

                                                            mima1.getText().length()<=12))) {
            account = yonghuming.getText().toString();
            myUser.setUsername(account);
            if (mima2.getText().toString().equals(mima1.getText().toString()))
            {
                password  = mima2.getText().toString();
                try {
                    password = AESEncryptor.encrypt("458841265536",password);
                } catch (Exception e) {
                    Toast.makeText(Shengfenzhuce.this,"内部出错，请重新输入密码",Toast.LENGTH_SHORT).show();
                    password = "";
                }
                myUser.setPassword(password);
            }
            if (mail.getText().length()>0) {
                myUser.setEmail(mail.getText().toString());
            }
        }
         myUser.signUp(Shengfenzhuce.this, new SaveListener() {
             @Override
             public void onSuccess() {
                 Toast.makeText(Shengfenzhuce.this,"注册成功",Toast.LENGTH_SHORT).show();
                 Intent intent = new Intent(Shengfenzhuce.this, YinyueActivity.class);
                 startActivity(intent);
                 overridePendingTransition(R.anim.you_zuo_out, R.anim.you_zuo_in);
             }

             @Override
             public void onFailure(int i, String s) {
                Toast.makeText(Shengfenzhuce.this,"注册失败:"+s,Toast.LENGTH_SHORT).show();
             }
         });

    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        EditText edt = (EditText) v;
        String hint;
        if (hasFocus)
        {
            hint = edt.getHint().toString();
            edt.setTag(hint);
            edt.setHint("");
        }
        else
        {
            hint = edt.getTag().toString();
            edt.setHint(hint);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        SharedPreferences sharedPreferences = this.getSharedPreferences(MY_PRIVATE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PRIVATE_ACCOUNT,account);
        editor.putString(PRIVATE_PASSWORD,password);
        editor.commit();
    }
}
