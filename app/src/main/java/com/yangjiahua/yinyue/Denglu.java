package com.yangjiahua.yinyue;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.listener.SaveListener;

public class Denglu extends Activity implements View.OnFocusChangeListener,View.OnClickListener {
    private EditText Tianxieyhm;
    private EditText Tianxiemm;
    private Button denglu;
    private CustomImageButton customImageButton;
    private CheckBox remPassword;
    private String password;
    private String jiemi;
    private SpotsDialog spotsDialog;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_denglu);
        Tianxieyhm = (EditText) findViewById(R.id.edt_Tianxieyhm);
        Tianxiemm = (EditText) findViewById(R.id.edt_Tianxiemm);
        denglu = (Button) findViewById(R.id.btn_denglu);
        remPassword = (CheckBox) findViewById(R.id.cb_remPassword);
        customImageButton = (CustomImageButton) findViewById(R.id.customImageButton);
        customImageButton.setText("忘记密码?");
        customImageButton.setColor(Color.argb(255, 255, 96, 17));
        customImageButton.setTextSize(28f);
        customImageButton.setOnClickListener(this);
        spotsDialog = new SpotsDialog(Denglu.this);
        denglu.setOnClickListener(this);
        Tianxieyhm.setOnFocusChangeListener(this);
        Tianxiemm.setOnFocusChangeListener(this);
        sharedPreferences = this.getSharedPreferences("MYACOUNT", Context.MODE_PRIVATE);
        if (sharedPreferences.getBoolean("ISCHECK", false)) {
            remPassword.setChecked(true);
            Tianxieyhm.setText(sharedPreferences.getString("USER_ACCOUNT", ""));
            try {
                jiemi = AESEncryptor.decrypt("458841265536", sharedPreferences.getString("USER_PASSWORD", ""));
                Tianxiemm.setText(jiemi);
            } catch (Exception e) {
                Toast.makeText(Denglu.this, "尝试获取密码失败", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
        remPassword.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (remPassword.isChecked()) {
                    sharedPreferences.edit().putBoolean("ISCHECK", true).commit();
                } else {
                    sharedPreferences.edit().putBoolean("ISCHECK", false).commit();
                }
            }
        });
        MyUser userInfo = BmobUser.getCurrentUser(Denglu.this, MyUser.class);
        if (userInfo != null) {
            Intent intent = new Intent(Denglu.this, YinyueActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        EditText edt = (EditText) v;
        String hint;
        if (hasFocus) {
            hint = edt.getHint().toString();
            edt.setTag(hint);
            edt.setHint("");
        } else {
            hint = edt.getTag().toString();
            edt.setHint(hint);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_denglu:
            spotsDialog.show();
            final MyUser myUser2 = new MyUser();
            myUser2.setUsername(Tianxieyhm.getText().toString());
            password = Tianxiemm.getText().toString();
            try {
                password = AESEncryptor.encrypt("458841265536", password);
                myUser2.setPassword(password);
            } catch (Exception e) {
                Toast.makeText(Denglu.this, "密码错误", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
            myUser2.login(Denglu.this, new SaveListener() {
                @Override
                public void onSuccess() {
                    spotsDialog.dismiss();
                    Toast.makeText(Denglu.this, "登录成功", Toast.LENGTH_SHORT).show();
                    if (remPassword.isChecked()) {
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("USER_ACCOUNT", myUser2.getUsername());
                        editor.putString("USER_PASSWORD", password);
                        editor.commit();
                    }
                    Intent intent = new Intent(Denglu.this, YinyueActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.you_zuo_out, R.anim.you_zuo_in);
                }

                @Override
                public void onFailure(int i, String s) {
                    spotsDialog.dismiss();
                    Toast.makeText(Denglu.this, "登录失败:" + s, Toast.LENGTH_SHORT).show();
                }
            });
            break;
            case R.id.customImageButton:

                break;
        }
    }
}