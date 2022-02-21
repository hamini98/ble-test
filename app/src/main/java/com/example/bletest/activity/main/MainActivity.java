package com.example.bletest.activity.main;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.bletest.activity.chart.ChartMainActivity;
import com.example.bletest.service.BluetoothService;
import com.example.bletest.R;
import com.example.bletest.activity.devicevalue.DeviceDetailActivity;

public class MainActivity extends AppCompatActivity {

    private final static String TAG = MainActivity.class.getSimpleName();

    // 지역적으로 정의된 정수(0보다 커야 함)
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int PERMISSION_REQUEST_CODE = 1;

    // 블루투스 관리 모듈
    private BluetoothAdapter bluetoothAdapter;

    private ListView deviceListView; // 타입이 클래스일수도있는거 (선언해주면)
    private DeviceListViewAdapter deviceListViewAdapter;
    private BluetoothService bluetoothService;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        Log.i(TAG, String.valueOf(bluetoothService.isScanning()));
        if (!bluetoothService.isScanning()) {
            menu.findItem(R.id.menu_stop).setVisible(false);
            menu.findItem(R.id.menu_scan).setVisible(true);
            menu.findItem(R.id.menu_refresh).setActionView(null);
        } else {
            menu.findItem(R.id.menu_stop).setVisible(true);
            menu.findItem(R.id.menu_scan).setVisible(false);
            menu.findItem(R.id.menu_refresh).setActionView(
                    R.layout.actionbar_indeterminate_progress);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_scan:
                deviceListViewAdapter.clear();
                bluetoothService.scanLeDevice(true);
                break;
            case R.id.menu_stop:
                bluetoothService.scanLeDevice(false);
                break;
        }

        invalidateOptionsMenu(); // 위에 버퍼링 작업으로 돌아가

        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();

        while (!checkCompatibility()) { // 이 조건이 틀릴때까지 반복
            Toast.makeText(this, R.string.ble_not_enabled, Toast.LENGTH_SHORT).show();
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                Log.i(TAG, e.getMessage());
            }
        }
        // deviceListView에 클릭 이벤트 리스너 등록
        deviceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                final BluetoothDevice device = deviceListViewAdapter.getItem(i);
                if (device == null) return;

                final Intent intent = new Intent(MainActivity.this, ChartMainActivity.class);
                intent.putExtra(DeviceDetailActivity.EXTRAS_DEVICE_NAME, device.getName());
                // 딕셔너리. device.getName 값 넣어준거 EXTRAS_DEVICE_NAME에
                intent.putExtra(DeviceDetailActivity.EXTRAS_DEVICE_ADDRESS, device.getAddress());

                bluetoothService.scanLeDevice(false);

                startActivity(intent); // 다음창 실행
            }
        });
    }

    // 호환성 체크
    private Boolean checkCompatibility() {
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        // 블루투스가 켜져있지 않으면 켜라고 요청
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Toast.makeText(this, R.string.ble_not_enabled, Toast.LENGTH_SHORT).show();
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);

            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            return false;
        }
        return true;
    }

    // 초기 설정
    private void init() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_CODE);
        }
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        deviceListViewAdapter = new DeviceListViewAdapter();

        deviceListView = (ListView) findViewById(R.id.device_list);
        deviceListView.setAdapter(deviceListViewAdapter);

        DeviceListViewItem deviceListViewItem = new DeviceListViewItem();

        bluetoothService = new BluetoothService(this, deviceListViewAdapter, bluetoothAdapter, new Handler());

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if(requestCode == PERMISSION_REQUEST_CODE)
        {
            if(grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, R.string.denied_location_permission, Toast.LENGTH_SHORT).show();
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    finish();
                }
            }
        }
    }
}
