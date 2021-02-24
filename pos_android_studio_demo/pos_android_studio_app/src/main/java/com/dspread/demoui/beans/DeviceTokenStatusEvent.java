package com.dspread.demoui.beans;

/**
 * Time:2020/9/16
 * Author:Qianmeng Chen
 * Description:the event bus for the token sync service
 */
public class DeviceTokenStatusEvent {
    public boolean isTokenActive;
    public DeviceTokenStatusEvent(boolean isTokenActive){
        this.isTokenActive = isTokenActive;
    }
}
