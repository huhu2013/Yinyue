package com.yangjiahua.yinyue;


import cn.bmob.v3.BmobUser;

public class MyUser extends BmobUser{
    private Boolean sex;
    private Integer age;
    private String nick;

    public Boolean getSex() {
        return sex;
    }

    public void setSex(Boolean sex) {
        this.sex = sex;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }
}
