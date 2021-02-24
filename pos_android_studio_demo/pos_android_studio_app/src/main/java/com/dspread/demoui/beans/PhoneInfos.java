package com.dspread.demoui.beans;

import java.io.Serializable;

/**
 * Time:2020/9/17
 * Description:
 * @author Qianmeng Chen
 */
public class PhoneInfos implements Serializable {
    private String signedAttestation;
    private String cotsVersion;
    private String buildVersion;
    private String cotsMode;

    public String getSignedAttestation() {
        return signedAttestation;
    }

    public void setSignedAttestation(String signedAttestation) {
        this.signedAttestation = signedAttestation;
    }

    public String getCotsVersion() {
        return cotsVersion;
    }

    public void setCotsVersion(String cotsVersion) {
        this.cotsVersion = cotsVersion;
    }

    public String getBuildVersion() {
        return buildVersion;
    }

    public void setBuildVersion(String buildVersion) {
        this.buildVersion = buildVersion;
    }

    public String getCotsMode() {
        return cotsMode;
    }

    public void setCotsMode(String cotsMode) {
        this.cotsMode = cotsMode;
    }
}
