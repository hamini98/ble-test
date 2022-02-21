package com.example.bletest.service;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Handler;

import com.example.bletest.activity.main.DeviceListViewAdapter;
import com.example.bletest.activity.main.MainActivity;

public class BluetoothService {

    private final String TAG = BluetoothService.class.getSimpleName();

    private final long SCAN_PERIOD = 10000;

    private boolean isScanning = false;
    private Handler handler;
    private BluetoothAdapter bluetoothAdapter;
    private DeviceListViewAdapter deviceListViewAdapter;
    private Context mainContext;

    public BluetoothService(Context mainContext, DeviceListViewAdapter deviceListViewAdapter, final BluetoothAdapter bluetoothAdapter, final Handler handler) {
        this.mainContext = mainContext;
        this.deviceListViewAdapter = deviceListViewAdapter;
        this.bluetoothAdapter = bluetoothAdapter;
        this.handler = handler;
    }

    public boolean isScanning() {
        return this.isScanning;
    }

    public void scanLeDevice(final boolean enable) {
        if (enable) {

            // Stops scanning after a pre-defined scan period.
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    isScanning = false;
                    bluetoothAdapter.stopLeScan(mLeScanCallback);
                }
            }, SCAN_PERIOD);

            this.isScanning = true;
            bluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            this.isScanning = false;
            bluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
            ((MainActivity)mainContext).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    deviceListViewAdapter.addItem(device);
                    deviceListViewAdapter.notifyDataSetChanged();
                }
            });
        }
    };
}
