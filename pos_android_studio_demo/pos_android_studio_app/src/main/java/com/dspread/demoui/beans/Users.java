package com.dspread.demoui.beans;

import java.io.Serializable;

/**
 * Time:2020/9/14
 * Author:Qianmeng Chen
 * Description:
 */
public class Users implements Serializable {
    private String userName;
    private String passWord;
    private String company;
    private String phone;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassWord() {
        return passWord;
    }

    public void setPassWord(String passWord) {
        this.passWord = passWord;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
