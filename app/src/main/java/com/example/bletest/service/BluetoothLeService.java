/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.bletest.service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.example.bletest.Util.SampleGattAttributes;

import java.util.List;
import java.util.UUID;

/**
 * Service for managing connection and data communication with a GATT server hosted on a
 * given Bluetooth LE device.
 */
public class BluetoothLeService extends Service {
    private final static String TAG = BluetoothLeService.class.getSimpleName();

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress;
    private BluetoothGatt mBluetoothGatt;
    private int mConnectionState = STATE_DISCONNECTED;

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    public final static String ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "com.example.bluetooth.le.EXTRA_DATA"; // 타입 변수명 값



    public final static UUID SERVICE_UUID =
            UUID.fromString(SampleGattAttributes.SERVICE);

    public final static UUID CHARACTERISTIC_UUID =
            UUID.fromString(SampleGattAttributes.CHARACTERISTIC);


    public final static UUID CONFIG_CHARACTERISTIC_UUID =
            UUID.fromString(SampleGattAttributes.CHARACTERISTIC_CONFIG);

    public final static UUID SETTING_SERVICE_UUID =
            UUID.fromString(SampleGattAttributes.SETTING);

    public final static UUID VOLUME_CHARACTERISTIC_UUID =
            UUID.fromString(SampleGattAttributes.VOLUME);

    // Implements callback methods for GATT events that the app cares about.  For example,
    // connection change and services discovered.
    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                intentAction = ACTION_GATT_CONNECTED;
                mConnectionState = STATE_CONNECTED;
                broadcastUpdate(intentAction);
                mBluetoothGatt.discoverServices();

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                intentAction = ACTION_GATT_DISCONNECTED;
                mConnectionState = STATE_DISCONNECTED;
                Log.i(TAG, "Disconnected from GATT server.");
                broadcastUpdate(intentAction);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {

            //gatt.readCharacteristic(gatt.getService(SETTING_SERVICE_UUID).getCharacteristic(VOLUME_CHARACTERISTIC_UUID));
            //gatt.setCharacteristicNotification(gatt.getService(SERVICE_UUID).getCharacteristic(CHARACTERISTIC_UUID), true);

            setCharacteristicNotification(gatt.getService(SERVICE_UUID).getCharacteristic(CHARACTERISTIC_UUID),true);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);

            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
        }
    };

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);
        if (!CHARACTERISTIC_UUID.equals(characteristic.getUuid())) {

            String data = new String(characteristic.getValue());
            data = data.replace("\n", "");

            //String data = new String(characteristic.getValue()).replace("\n", "");

            // final byte[] data = characteristic.getValue();

            //int testData = characteristic.getValue();
            if (data != null && data.length()> 0) {
                //final StringBuilder stringBuilder = new StringBuilder(data.length); // data 공간만큼 문자열 공간 생성

                //  1 : LED 키기
                //  2 : LED 끄기

                /*while(i < 10) {
                    i++;
                }

                for(int i = 0; i < 10; i++) {
                    10
                }
                int[] a = {0,1,2};
                for(int t : a) {
                    t = 0;
                    t = 1;
                    t = 2;
                }*/


                /*for (char byteChar : data) {// data에 있는거를 byte char에 넣어줘 / bytechar = 변수이름
                    stringBuilder.append(String.format("%d", byteChar)); // %02X 16진수
                }*/

                intent.putExtra(EXTRA_DATA, data);


                Log.d(TAG, new String(data) + "\n" + data);
            }
        } else {
            final byte[] data = characteristic.getValue();

            if (data != null && data.length > 0) { // length() 라는 함수가 string 안에있음

                final StringBuilder stringBuilder = new StringBuilder(data.length);
                // 길이만큼 스트링형의 메모리 영역 할당 영역 만듬


                for (byte byteChar : data) {
                    //stringBuilder.append(String.format("%c", byteChar)); // 문자로 바꾸고 시리얼 모니터
                    stringBuilder.append(String.format("%d", byteChar)); // 정수로 바꾸고 아두이노
                }

                stringBuilder.toString().replace("\n", "");// toString() 문자열로 바꾸는 함수

                //intent.putExtra(EXTRA_DATA, "");
                intent.putExtra("com.example.bluetooth.le.EXTRA_DATA", stringBuilder.toString().replace("\n", ""));

                //intent.putExtra(EXTRA_DATA, ""); // 받은거랑 파씽한거 다 보이게 / 마지막거의 값이 들어가서 다 지워야돼

                 Log.d(TAG, new String(data) + "\n" + stringBuilder.toString()); // 문자열로 만들어진거 띄워줘
            }
        /*} else {
            int flag = characteristic.getProperties();
            int format = -1;
            if ((flag & 0x01) != 0) {
                format = BluetoothGattCharacteristic.FORMAT_UINT16;
                Log.d(TAG, "Heart rate format UINT16.");
            } else {
                format = BluetoothGattCharacteristic.FORMAT_UINT8;
                Log.d(TAG, "Heart rate format UINT8.");
            }
            final int volume = characteristic.getIntValue(format, 1);
            intent.putExtra(EXTRA_DATA, String.valueOf(volume));*/
        }
        sendBroadcast(intent);
    }

    public class LocalBinder extends Binder {
        public BluetoothLeService getService() {
            return BluetoothLeService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the Service.
        close();
        return super.onUnbind(intent);
    }

    private final IBinder mBinder = new LocalBinder();

    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    public void init() {
        mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
    }

    public boolean connect(final String address) {
        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress)
                && mBluetoothGatt != null) {
            if (mBluetoothGatt.connect()) {
                mConnectionState = STATE_CONNECTING;
                return true;
            } else {
                return false;
            }
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            return false;
        }

        mBluetoothGatt = device.connectGatt(this, false, gattCallback);
        mBluetoothDeviceAddress = address;
        mConnectionState = STATE_CONNECTING;
        return true;
    }

    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.disconnect();
    }

    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    /**
     * 현재 벨류 받아오기
     * @param characteristic
     */
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.readCharacteristic(characteristic);
    }

    /**
     * 알람 키기
     * @param characteristic
     * @param enabled
     */
    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
                                              boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        // 바뀔때마다 오게끔
        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);

        // This is specific to Heart Rate Measurement.
        if (CHARACTERISTIC_UUID.equals(characteristic.getUuid())) {
            Log.i(TAG, "테스트");
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                    UUID.fromString(SampleGattAttributes.CHARACTERISTIC_CONFIG));
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            mBluetoothGatt.writeDescriptor(descriptor);
        }
    }
}
