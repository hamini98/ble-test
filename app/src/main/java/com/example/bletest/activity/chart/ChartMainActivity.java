package com.example.bletest.activity.chart;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;

import com.example.bletest.R;
import com.example.bletest.activity.devicevalue.DeviceDetailActivity;
import com.example.bletest.activity.main.DeviceListViewAdapter;
import com.example.bletest.activity.main.MainActivity;
import com.example.bletest.service.BluetoothService;
import com.github.mikephil.charting.charts.Chart;

public class ChartMainActivity extends AppCompatActivity {

    Button realtime, recordtime;
    private String deviceName;
    private String deviceAddress;
    private DeviceListViewAdapter deviceListViewAdapter;
    private BluetoothService bluetoothService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart_main);

        final Intent intent = getIntent();
        deviceName = intent.getStringExtra(DeviceDetailActivity.EXTRAS_DEVICE_NAME);
        deviceAddress = intent.getStringExtra(DeviceDetailActivity.EXTRAS_DEVICE_ADDRESS); // 값을 받는 부분

        /*// 실시간
        realtime = (Button)findViewById(R.id.realtime);
        realtime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent chartintent = new Intent(ChartMainActivity.this, RealtimeActivity.class);
                startActivity(chartintent);
            }
        });*/



        // 기록
        recordtime = (Button)findViewById(R.id.recordtime);
        recordtime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChartMainActivity.this, RecordActivity.class);
                intent.putExtra(DeviceDetailActivity.EXTRAS_DEVICE_NAME, deviceName); // 변수라서 () 안써
                intent.putExtra(DeviceDetailActivity.EXTRAS_DEVICE_ADDRESS, deviceAddress); // 위에서 받아와서 넘겨줌
                startActivity(intent);
            }
        });

/*        realtime = (Button)findViewById(R.id.realtime);
        realtime.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                final BluetoothDevice device = deviceListViewAdapter.getItem(i);
                if (device == null) return;

                final Intent intent = new Intent(ChartMainActivity.this, DeviceDetailActivity.class);
                intent.putExtra(DeviceDetailActivity.EXTRAS_DEVICE_NAME, device.getName());
                // 딕셔너리. device.getName 값 넣어준거 EXTRAS_DEVICE_NAME에
                intent.putExtra(DeviceDetailActivity.EXTRAS_DEVICE_ADDRESS, device.getAddress());

                bluetoothService.scanLeDevice(false);

                startActivity(intent); // 다음창 실행
            }
        });*/

        realtime = (Button)findViewById(R.id.realtime);
        realtime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChartMainActivity.this, DeviceDetailActivity.class);
                intent.putExtra(DeviceDetailActivity.EXTRAS_DEVICE_NAME, deviceName); // 변수라서 () 안써
                intent.putExtra(DeviceDetailActivity.EXTRAS_DEVICE_ADDRESS, deviceAddress); // 위에서 받아와서 넘겨줌


                startActivity(intent);
            }
        });
    }


}