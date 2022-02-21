package com.example.bletest.activity.devicevalue;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.BaseColumns;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bletest.R;
import com.example.bletest.activity.chart.ChartActivity;
import com.example.bletest.domain.DeviceValueContract;
import com.example.bletest.domain.DeviceValueDomainHelper;
import com.example.bletest.service.BluetoothLeService;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.XAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DeviceDetailActivity extends AppCompatActivity {

    int i = 0;

    List<Entry> entries = new ArrayList<>(); // 값-인덱스 넣어주면 순차적으로 그려줌 , Y축이름(데이터값)
    ArrayList<String> xVals = new ArrayList<String>(); // X 축 이름 값

    DeviceValueDomainHelper deviceValueDomainHelper;
    SQLiteDatabase valueDb;


    private final static String TAG = DeviceDetailActivity.class.getSimpleName();

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    private String deviceName;
    private String deviceAddress;
 //   private DeviceValueDomainHelper deviceValueDomainHelper;
 //   private SQLiteDatabase valueDb;

    private DeviceValueListAdapter deviceValueListAdpter;
    private ListView deviceValueList;
    private ListView deviceValueList2; // 1 이거 추가
    private BluetoothLeService bluetoothLeService;
    private boolean mConnected = false;

    private String[] projection = {
            BaseColumns._ID, //0번째
            DeviceValueContract.DeviceValueEntry.COLUMN_NAME_VALUE, //1번째
            DeviceValueContract.DeviceValueEntry.COLUMN_NAME_REC_DATE //2번째
    };

    String sortOrder = DeviceValueContract.DeviceValueEntry.COLUMN_NAME_REC_DATE + " DESC";
    // ASC : 내림차순(가장 옛날거부터 보여줘) , DESC : 오름차순 (정렬기준)(최신거부터)
    // 타입 변수명 =
    private final ServiceConnection serviceConnection = new ServiceConnection() {
        // 타입(클래스) 변수명 = new 객체만듬

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            bluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            bluetoothLeService.init();

            bluetoothLeService.connect(deviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            bluetoothLeService = null;
        }
    };

    private final BroadcastReceiver gattUpdateReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            // 어떤 행동을 했는지  받아와서 앞에 있는 action에 넣은거
            if (bluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                invalidateOptionsMenu();
            } else if (bluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                invalidateOptionsMenu();
            } else if (bluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                Log.i(TAG, "서비스 받아옴");
                //displayGattServices(bluetoothLeService.getSupportedGattServices());
            } else if (bluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                Log.i(TAG, "데이터 받아옴");
                displayData(intent.getStringExtra(bluetoothLeService.EXTRA_DATA));
            }
        }

    };

    private void getDbValue() {

        deviceValueListAdpter.clear();
        // SQLite 연결해서 결과값 받는 타입
        Cursor cursor = valueDb.query(
                //타입 변수명 = valueDb 클래스에 있는 query 함수쓴거
                DeviceValueContract.DeviceValueEntry.TABLE_NAME,   // The table to query
                //클래스안에.클래스 안에.TABLE_NAME 갖고와
                projection,             // SELECT * FROM 에서 * = TABLE 안에 있는 모든 컬럼 다 받겠다
                null,              // 어떤 컬럼 사용할건지
                //value = ? AND rec_date = ?
                null,          // 그
                //2, date('now', 'location')
                null,                   // don't group the rows
                //group by 그룹화
                null,                   // don't filter by row groups
                sortOrder               // The sort order
        );

        /*while(cursor.moveToNext()) {
            deviceValueListAdpter.addValue(cursor.getString(1), cursor.getString(2));
            // ID는 필요없어서 안넣음 1번째(값), 두번째(시간) 넣어줘
        }*/

        cursor.close();

    }

    private void displayData(String value) { // 수신했을때 작동
        final String deviceValue = value;

        Date dt = new Date();
        //변수타입 변수명 = new(클래스객체생성) Date();
        // 현재 날짜 받아옴
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        final String recDate = simpleDateFormat.format(dt);

        //DB 에 값 insert하는거
        ContentValues values = new ContentValues();
        values.put(DeviceValueContract.DeviceValueEntry.COLUMN_NAME_VALUE, deviceValue); // 앞의 (이름의) 변수에 뒤에 (이름의) 값 넣어
        values.put(DeviceValueContract.DeviceValueEntry.COLUMN_NAME_REC_DATE, recDate);
        //recDate = 현재 시간


        entries.add(new Entry(Float.parseFloat(deviceValue), i++)); // 문자형을 실수로 변환
        xVals.add("" + dt.getTime()); // 하나씩 받아와서 넣어줌 (X축 시간으로 나온게 이거 때문)

        makeChart(); // 이 함수가 뿌려주는애 / 실시간은 db 데이터 가져올 필요가없다 그래서 select문 필요없음


        values.put(DeviceValueContract.DeviceValueEntry.DEVICE_ID, deviceAddress); // db insert
        //deviceAddress = 디바이스 ID //DEVICE_ID에 deviceAddress 넣고 객체화

        // 쿼리 만든거
        valueDb.insert(DeviceValueContract.DeviceValueEntry.TABLE_NAME, null, values); // 명령어
        // 만든 쿼리 insert 실행

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                deviceValueListAdpter.addValue(deviceValue, recDate);
                deviceValueListAdpter.notifyDataSetChanged();
            } // 데이터 새로 생긴애 있으니까 보여줘 getView
        });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_value_view);

        deviceValueDomainHelper = new DeviceValueDomainHelper(this); // 이 클래스에 db 객체 새로 생성
        valueDb = deviceValueDomainHelper.getReadableDatabase(); // 어댑터 (연결부)

        init(); //호출

        getDbValue();

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        //Intent 변수 = new 객체(this = 현재 액티비티랑 Service 연결해주는거 생성)
        bindService(gattServiceIntent, serviceConnection, BIND_AUTO_CREATE);
        //백그라운드로 받아온 서비스 돌려 (서비스 연결됬을때 실행할 작업들, 자동생성)
    }

    public void init() { //선언
        final Intent intent = getIntent();
        deviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        deviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS); // 값을 받는 부분

        getSupportActionBar().setTitle(deviceName);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        deviceValueDomainHelper = new DeviceValueDomainHelper(getBaseContext());
        valueDb = deviceValueDomainHelper.getWritableDatabase();

        deviceValueListAdpter = new DeviceValueListAdapter();

        deviceValueList = (ListView) findViewById(R.id.device_value_list);

        //리스트 목록만
        deviceValueList.setAdapter(deviceValueListAdpter);
        //어댑터랑 화면 연결
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(gattUpdateReceiver, makeGattUpdateIntentFilter()); //호출
        if (bluetoothLeService != null) {
            final boolean result = bluetoothLeService.connect(deviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(gattUpdateReceiver);
        //행동이 있었으면 실행시켜
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(serviceConnection);
        bluetoothLeService = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gatt_services, menu);
        if (mConnected) {
            menu.findItem(R.id.menu_connect).setVisible(false);
            menu.findItem(R.id.menu_disconnect).setVisible(true);
        } else {
            menu.findItem(R.id.menu_connect).setVisible(true);
            menu.findItem(R.id.menu_disconnect).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_connect:
                bluetoothLeService.connect(deviceAddress);
                return true;
            case R.id.menu_disconnect:
                bluetoothLeService.disconnect();
                return true;
            case android.R.id.home:
                //뒤로 돌아가기 버튼 (메뉴 왼쪽 상단에 있는 <-)
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
        // super = 상속받았을 때 상속한 부모의 함수를 쓰겠다. 위에 변경한거 썼지만 여기에 원본도 쓸거임
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter; // 이 action들을 추가
    }



    /**
     * =============== 객체 생성 ==================
     * class 클래스명 () {
     *
     * }
     * =============== 객체 초기화(인스턴스) ==================
     * 메모리에 할당
     *
     * new 객체명();
     *
     * =============== 함수 호출 ==================
     *
     * 함수명 (파라미터);
     *
     * =============== 함수 선언 ==================
     *
     * private 반환타입 함수명 (파라미터 ) {
     *     중괄호 있으면 함수 선언한거
     * }
     */


    private void momo(Cursor cursor) {
        String Value2 = "Value" + "\r\n";

        DeviceValueListAdapter deviceValueListAdapter = new DeviceValueListAdapter();
        deviceValueList2.setAdapter(deviceValueListAdapter);

        while (cursor.moveToNext())
        {
            deviceValueListAdapter.addValue(cursor.getString(1), cursor.getString(2));
        }

        deviceValueListAdapter.notifyDataSetChanged(); // 바뀐게 있으면 화면 바꿔
        cursor.close();
        valueDb.close();
    }

    private void makeChart() {
        LineDataSet lineDataSet = new LineDataSet(entries, "속성명1"); // 여기 쓴거 다 확실하지않음
        lineDataSet.setLineWidth(2); // 라인 두께
        lineDataSet.setCircleRadius(3); // 점 크기
        lineDataSet.setCircleColor(Color.parseColor("#FFA1B4DC")); // 점 색깔
        lineDataSet.setCircleColorHole(Color.BLUE); // 점 겉 색깔
        lineDataSet.setColor(Color.parseColor("#FFA1B4DC")); // 라인 색깔
        lineDataSet.setDrawCircleHole(true); // 원의 겉 부분 칠할거?
        lineDataSet.setDrawCircles(true); // 원 찍을거?
        lineDataSet.setDrawHorizontalHighlightIndicator(false);
        lineDataSet.setDrawHighlightIndicators(false);
        lineDataSet.setDrawValues(false);

        LineChart chart = (LineChart) findViewById(R.id.chart);

        ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
        dataSets.add(lineDataSet);
        LineData lineData = new LineData(xVals, dataSets);
        chart.setData(lineData);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(Color.BLACK);
        xAxis.enableGridDashedLine(8, 24, 0);

        YAxis yLAxis = chart.getAxisLeft();
        yLAxis.setTextColor(Color.BLACK);

        YAxis yRAxis = chart.getAxisRight();
        yRAxis.setDrawLabels(false);
        yRAxis.setDrawAxisLine(false);
        yRAxis.setDrawGridLines(false);

        chart.setDoubleTapToZoomEnabled(false);
        chart.setDrawGridBackground(false);

        xAxis.setValueFormatter(new MyXAxisValueFormatter());
        //chart.animateY(2000, Easing.EasingOption.EaseInCubic);
        chart.invalidate();
    }

    private class MyXAxisValueFormatter implements XAxisValueFormatter { // 원래 public이였는데 바꾼거 안돼면 다시 바꿔 recordActivity에서도 똑같이 바꿈

        @Override
        public String getXValue(String dateInMillisecons, int index, ViewPortHandler viewPortHandler) {
            try {

                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                return sdf.format(new Date(Long.parseLong(dateInMillisecons)));

            } catch (Exception e) {

                return dateInMillisecons;
            }
        }
    }
}


