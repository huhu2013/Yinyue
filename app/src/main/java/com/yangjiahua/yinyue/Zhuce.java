package com.yangjiahua.yinyue;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.bmob.sms.BmobSMS;
import cn.bmob.sms.exception.BmobException;
import cn.bmob.sms.listener.RequestSMSCodeListener;
import cn.bmob.sms.listener.VerifySMSCodeListener;


public class Zhuce extends Activity implements View.OnClickListener,View.OnFocusChangeListener{
    private EditText zhuce;
    private EditText yanzhengma;
    private Button yanzheng;
    private Button xiayibu;
    private ImageButton fanhui;
    private ImageView YzmImageView;
    private EditText Tuxingedt;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_zhuce);
        zhuce = (EditText) findViewById(R.id.edt_zhuce);
        yanzhengma = (EditText) findViewById(R.id.edt_yanzhengma);
        yanzheng = (Button) findViewById(R.id.btn_yanzhengma);
        xiayibu = (Button) findViewById(R.id.btn_xiayibu);
        fanhui = (ImageButton) findViewById(R.id.btn_backto_zd);
        YzmImageView = (ImageView) findViewById(R.id.YzmimageView);
        Tuxingedt = (EditText) findViewById(R.id.edt_tuxing);
        yanzheng.setOnClickListener(this);
        xiayibu.setOnClickListener(this);
        zhuce.setOnClickListener(this);
        yanzhengma.setOnClickListener(this);
        fanhui.setOnClickListener(this);
        Tuxingedt.setOnClickListener(this);
        YzmImageView.setImageBitmap(BPUtil.getInstance().createBitmap());
        YzmImageView.setOnClickListener(this);
        zhuce.setSelection(0);
        Tuxingedt.setSelection(0);
        yanzhengma.setSelection(0);
        zhuce.setOnFocusChangeListener(this);
        Tuxingedt.setOnFocusChangeListener(this);
        yanzhengma.setOnFocusChangeListener(this);
        BmobSMS.initialize(Zhuce.this,"5ff17b39552723beb03fe6a7de8e8ff2");

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_yanzhengma:
                if (TextUtils.isEmpty(zhuce.getText())) {
                    YzmImageView.setImageBitmap(BPUtil.getInstance().createBitmap());
                    Toast.makeText(Zhuce.this, "手机号为空", Toast.LENGTH_SHORT).show();
                } else {
                    if (isMobileNO(zhuce.getText().toString()) == false) {
                        YzmImageView.setImageBitmap(BPUtil.getInstance().createBitmap());
                        Toast.makeText(Zhuce.this, "请输入正确的手机号", Toast.LENGTH_SHORT).show();
                    } else {
                        if (TextUtils.isEmpty(Tuxingedt.getText())) {
                            YzmImageView.setImageBitmap(BPUtil.getInstance().createBitmap());
                            Toast.makeText(Zhuce.this, "验证码为空", Toast.LENGTH_SHORT).show();
                        } else {

                            if (!Tuxingedt.getText().toString().equals(exChangeToLower(BPUtil.getInstance().getCode())) && !Tuxingedt.getText().toString().equals(BPUtil.getInstance().getCode())
                                    && !Tuxingedt.getText().toString().equals(exChangeToUpper(BPUtil.getInstance().getCode()))) {
                                YzmImageView.setImageBitmap(BPUtil.getInstance().createBitmap());
                                Toast.makeText(Zhuce.this, "验证码不正确", Toast.LENGTH_SHORT).show();
                            } else {
                                BmobSMS.requestSMSCode(Zhuce.this, zhuce.getText().toString(), "注册模板", new RequestSMSCodeListener() {
                                    @Override
                                    public void done(Integer integer, BmobException e) {
                                        if (e == null) {
                                            YzmImageView.setImageBitmap(BPUtil.getInstance().createBitmap());
                                            Toast.makeText(Zhuce.this, "短信验证码发送成功", Toast.LENGTH_SHORT).show();
                                        } else {
                                            YzmImageView.setImageBitmap(BPUtil.getInstance().createBitmap());
                                            if (e.getErrorCode() == 9016)
                                                Toast.makeText(Zhuce.this, "网络无法连接，请检查您的网络", Toast.LENGTH_LONG).show();
                                            if (e.getErrorCode() == 10010)
                                                Toast.makeText(Zhuce.this, "由于您今天操作次数过多,今天将无法继续进行短信验证", Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });
                            }
                        }
                    }

                }
                break;
            case R.id.btn_xiayibu:
                BmobSMS.verifySmsCode(Zhuce.this, zhuce.getText().toString(), yanzhengma.getText().toString(), new VerifySMSCodeListener() {
                    @Override
                    public void done(BmobException e) {
                        if (e == null) {
                            Toast.makeText(Zhuce.this, "短信验证成功", Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(Zhuce.this, Shengfenzhuce.class);
                            startActivity(intent);
                            overridePendingTransition(R.anim.you_zuo_out, R.anim.you_zuo_in);
                        } else
                            Toast.makeText(Zhuce.this, "短信验证失败:" + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();

                    }
                });
                break;

            case R.id.btn_backto_zd:
                Intent intent = new Intent(Zhuce.this, MainActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.zuo_you_out, R.anim.zuo_you_in);
                break;
            case R.id.YzmimageView:
                YzmImageView.setImageBitmap(BPUtil.getInstance().createBitmap());
                break;


        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_BACK&&event.getAction()==KeyEvent.ACTION_DOWN){
           finish();
            overridePendingTransition(R.anim.zuo_you_out,R.anim.zuo_you_in);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    public boolean isMobileNO(String mobiles) {
        Pattern p = Pattern
                .compile("^((13[0-9])|(15[^4,\\D])|(18[0,5-9]))\\d{8}$");
        Matcher m = p.matcher(mobiles);

        return m.matches();
    }
    public static String exChangeToLower(String str){
        StringBuffer sb = new StringBuffer();
        if(str!=null){
            for(int i=0;i<str.length();i++){
                char c = str.charAt(i);
                if(Character.isUpperCase(c)){
                    sb.append(Character.toLowerCase(c));
                }
                else
                {
                    sb.append(c);
                }
            }
        }
        return sb.toString();
    }

    public static String exChangeToUpper(String str){
        StringBuffer sb = new StringBuffer();
        if(str!=null){
            for(int i=0;i<str.length();i++){
                char c = str.charAt(i);
                if(Character.isLowerCase(c)){
                    sb.append(Character.toUpperCase(c));
                }
                else
                {
                    sb.append(c);
                }
            }
        }
        return sb.toString();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
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
}
