package com.example.bletest.activity.main;

public class DeviceListViewItem {

    private String deviceName;
    private String deviceAddress;

    public DeviceListViewItem() {}

    public String getDeviceName() {
        //getDeviceName 함수 사용한데에서 deviceName 값 반환
        return this.deviceName;
    }

    public String getDeviceAddress() { return this.deviceAddress; }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public void setDeviceAddress(String deviceAddress) {
        this.deviceAddress = deviceAddress;
    }
}
