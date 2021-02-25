package com.dspread.demoui.beans;

import java.io.Serializable;

public class PosInfos implements Serializable {
    private String terminalId;
    private String bootLoaderVersion;
    private String firmwareVersion;
    private String hardwareVersion;
    private String pciFirmwareVresion;
    private String pciHardwareVersion;

    public String getTerminalId() {
        return terminalId;
    }

    public void setTerminalId(String terminalId) {
        this.terminalId = terminalId;
    }

    public String getBootLoaderVersion() {
        return bootLoaderVersion;
    }

    public void setBootLoaderVersion(String bootLoaderVersion) {
        this.bootLoaderVersion = bootLoaderVersion;
    }

    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    public void setFirmwareVersion(String firmwareVersion) {
        this.firmwareVersion = firmwareVersion;
    }

    public String getHardwareVersion() {
        return hardwareVersion;
    }

    public void setHardwareVersion(String hardwareVersion) {
        this.hardwareVersion = hardwareVersion;
    }

    public String getPciFirmwareVresion() {
        return pciFirmwareVresion;
    }

    public void setPciFirmwareVresion(String pciFirmwareVresion) {
        this.pciFirmwareVresion = pciFirmwareVresion;
    }

    public String getPciHardwareVersion() {
        return pciHardwareVersion;
    }

    public void setPciHardwareVersion(String pciHardwareVersion) {
        this.pciHardwareVersion = pciHardwareVersion;
    }
}
