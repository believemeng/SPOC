package com.dspread.demoui.beans;

import com.dspread.xpos.QPOSService;

import java.io.Serializable;

/**
 * Time:2020/4/2
 * Description:
 * @author Qianmeng Chen
 */
public class ConstantsBean{
    public static QPOSService pos = null;

    public static QPOSService getQpos(){
        return pos;
    }

    public static void setQpos(QPOSService mpos){
        pos = mpos;
    }
}
