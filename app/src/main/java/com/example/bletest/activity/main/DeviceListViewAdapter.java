package com.example.bletest.activity.main;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.example.bletest.R;

import java.util.ArrayList;

public class DeviceListViewAdapter extends BaseAdapter {

    private TextView deviceNameView;
    private TextView deviceAddressView;

    private ArrayList<BluetoothDevice> deviceList = new ArrayList<BluetoothDevice>();
    // []

    public DeviceListViewAdapter() {

    }

    @Override
    public int getCount() {
        return deviceList.size();
    }

    @Override
    public BluetoothDevice getItem(int position) {

        return deviceList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Context context = parent.getContext();

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.listitem_device, parent, false);
        }

        deviceNameView = (TextView) convertView.findViewById(R.id.device_name);
        deviceAddressView = (TextView) convertView.findViewById(R.id.device_address);

        BluetoothDevice device = deviceList.get(position);

        final String deviceName = device.getName();
        //deviceName 이라는 변수에 device명 넣은거

        if (deviceName != null && deviceName.length() > 0) {
            deviceNameView.setText(deviceName);
            //이름 있으면 보여
        } else {
            deviceNameView.setText(R.string.unknown_device_name);
        }

        deviceAddressView.setText(device.getAddress());
        //주소 보임

        return convertView;
    }

    public void addItem(final BluetoothDevice device) {
        if (!deviceList.contains(device)) {
            deviceList.add(device);
        }
    }

    public void clear() {
        deviceList.clear();
    }

}
