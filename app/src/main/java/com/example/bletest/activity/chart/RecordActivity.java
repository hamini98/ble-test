package com.example.bletest.activity.chart;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
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
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bletest.R;
import com.example.bletest.activity.devicevalue.DeviceDetailActivity;
import com.example.bletest.activity.devicevalue.DeviceValueListAdapter;
import com.example.bletest.domain.DeviceValueContract;
import com.example.bletest.domain.DeviceValueDomainHelper;
import com.example.bletest.service.BluetoothLeService;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.XAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.lang.reflect.Array;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RecordActivity extends AppCompatActivity implements View.OnClickListener, DatePickerDialog.OnDateSetListener {


    List<Entry> entries = new ArrayList<>(); // 값 - 인덱스 넣어주면 순차적으로 그려줘, y축이름(데이터값)
    ArrayList<String> xVals = new ArrayList<String>(); // X 축 이름 값

    DeviceValueDomainHelper deviceValueDomainHelper;
    SQLiteDatabase valueDb;

    private static String TAG = RecordActivity.class.getSimpleName();
    private static final long ONE_DAY_MILLISECOND = 86400000;

    private TextView tv_since;
    private TextView tv_till;
    private Button date_search;
    private boolean mIsClickSince;


    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    private String deviceName;
    private String deviceAddress;

    private ListView deviceValueList2; // 1 이거 추가
    private DeviceValueListAdapter deviceValueListAdpter;
    private BluetoothLeService bluetoothLeService;

    private boolean mConnected = false;

    private String[] projection = {
            BaseColumns._ID, // 0번째
            DeviceValueContract.DeviceValueEntry.COLUMN_NAME_VALUE, // 1번째
            DeviceValueContract.DeviceValueEntry.COLUMN_NAME_REC_DATE // 2번째
    };

    String sortOrder = DeviceValueContract.DeviceValueEntry.COLUMN_NAME_REC_DATE + " DESC";
    // 정렬기준 ASC : 내림차순(옛날거부터 보여줘), DESC : 오름차순 (최신거부터)
    // 타입 변수명 =

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        // 타입(클래스) 변수명 = new 객체 만듬

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

/*    private void displayData(String value) { // 수신했을때 작동
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


        values.put(DeviceValueContract.DeviceValueEntry.DEVICE_ID, deviceAddress);
        //deviceAddress = 디바이스 ID

        // 쿼리 만든거
        valueDb.insert(DeviceValueContract.DeviceValueEntry.TABLE_NAME, null, values);
        // 만든 쿼리 insert 실행

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                deviceValueListAdpter.addValue(deviceValue, recDate);
                deviceValueListAdpter.notifyDataSetChanged();
            } // 데이터 새로 생긴애 있으니까 보여줘 getView
        });
    }*/


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        initValue();
        initLayout();

        deviceValueDomainHelper = new DeviceValueDomainHelper(this); // 이 클래스에 db 객체 새로 생성
        valueDb = deviceValueDomainHelper.getReadableDatabase(); // 어댑터 (연결부)

        init(); //호출

        getDbValue();

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        //Intent 변수 = new 객체(this = 현재 액티비티랑 Service 연결해주는거 생성)
        bindService(gattServiceIntent, serviceConnection, BIND_AUTO_CREATE);
        //백그라운드로 받아온 서비스 돌려 (서비스 연결됬을때 실행할 작업들, 자동생성)

        date_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                valueDb = deviceValueDomainHelper.getReadableDatabase();

                String tvsince = tv_since.getText().toString(); // trim() 공백을 없애겠다
                String tvtill = tv_till.getText().toString(); // trim() 없어도돼서 지움

                Cursor cursor;
                cursor = valueDb.rawQuery("SELECT * FROM value Where device_id = '" + deviceAddress + "' AND date(rec_date) >= date('" + tvsince + "') AND date(rec_date) <= date('" + tv_till.getText().toString() + "'); ", null);
                // 위에 String 형식이여도 문장에 변수를 넣고싶으면 무조건 문자열 형태 " + 변수명 + " 이거 해 (자바열은 문자열 무조건 ")
                String Value2 = "SELECT * FROM value Where device_id = '" + deviceAddress + "' AND date(rec_date) >= date('" + tv_since + "') AND date(rec_date) <= date('" + tv_till + "'); "; // 로그보기용 이거 틀린식인데 이거덕에 틀린거앎

                DeviceValueListAdapter deviceValueListAdapter = new DeviceValueListAdapter();
                deviceValueList2.setAdapter(deviceValueListAdapter);

                Date dt = new Date();
                entries.clear();
                xVals.clear();
                int i = 0;

                // [0, 2, 3]
                // [0, 2, 3, 0, 2, 3]
                while (cursor.moveToNext()) {
                    deviceValueListAdapter.addValue(cursor.getString(1), cursor.getString(2));
                    entries.add(new Entry(Float.parseFloat(cursor.getString(1)), i++)); // 문자형을 실수로 변환 y축
                    xVals.add("" + cursor.getString(2)); // 하나씩 받아와서 넣어줌 (X축 시간으로 나온게 이거 때문)
                }

                // = >=  <=
                deviceValueListAdapter.notifyDataSetChanged();
                cursor.close();
                valueDb.close();

                makeChart(); // 이 함수가 뿌려주는애 / 실시간은 db 데이터 가져올 필요가없다 그래서 select문 필요없음
            }
        });
    }


    public void init() { //선언
        final Intent intent = getIntent();
        deviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        deviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS); // 값을 받는 부분

        getSupportActionBar().setTitle(deviceName);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        deviceValueDomainHelper = new DeviceValueDomainHelper(getBaseContext());
        valueDb = deviceValueDomainHelper.getWritableDatabase(); // insert 할때 필요한 권한

        deviceValueListAdpter = new DeviceValueListAdapter();

        deviceValueList2 = (ListView) findViewById(R.id.device_value_list2);


        deviceValueList2.setAdapter(deviceValueListAdpter);
        //어댑터랑 화면 연결
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
        switch (item.getItemId()) {
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


    private void initValue() { // 초기 값 설정
        mIsClickSince = false;
    }

    private void initLayout() { // 초기 레이아웃 설정
        tv_since = (TextView) findViewById(R.id.datesearch_sample_tv_since);
        tv_till = (TextView) findViewById(R.id.datesearch_sample_tv_till);
        date_search = (Button) findViewById(R.id.datesearch_sample_btn_search);

        tv_since.setText(new SimpleDateFormat("yyyy-MM-dd").format(System.currentTimeMillis() - (ONE_DAY_MILLISECOND * 3)));
        tv_till.setText(new SimpleDateFormat("yyyy-MM-dd").format(System.currentTimeMillis()));
/*        String tvsince = tv_since.getText().toString(); // trim() 공백을 없애겠다
        String tvtill = tv_till.getText().toString(); // trim() 없어도돼서 지움 얘네 전역변수여야되서 밖에 뺌*/

        tv_since.setOnClickListener(this);
        tv_till.setOnClickListener(this);
        date_search.setOnClickListener(this);


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.datesearch_sample_tv_since: // 언제부터

                mIsClickSince = true;

                int since_year = Integer.parseInt(tv_since.getText().toString().trim().substring(0, 4));
                int since_month = Integer.parseInt(tv_since.getText().toString().trim().substring(5, 7));
                int since_day = Integer.parseInt(tv_since.getText().toString().trim().substring(8, 10));

                new DatePickerDialog(this, this, since_year, (since_month - 1), since_day).show();
                break;

            case R.id.datesearch_sample_tv_till: // 언제까지
                mIsClickSince = false;

                int till_year = Integer.parseInt(tv_till.getText().toString().trim().substring(0, 4));
                int till_month = Integer.parseInt(tv_till.getText().toString().trim().substring(5, 7));
                int till_day = Integer.parseInt(tv_till.getText().toString().trim().substring(8, 10));

                new DatePickerDialog(this, this, till_year, (till_month - 1), till_day).show();


                break;
            case R.id.datesearch_sample_btn_search: // 검색

                if (isSetDateOk()) {


                    int search_since_year = Integer.parseInt(tv_since.getText().toString().trim().substring(0, 4));
                    int search_since_month = Integer.parseInt(tv_since.getText().toString().trim().substring(5, 7));
                    int search_since_day = Integer.parseInt(tv_since.getText().toString().trim().substring(8, 10));

                    int search_till_year = Integer.parseInt(tv_till.getText().toString().trim().substring(0, 4));
                    int search_till_month = Integer.parseInt(tv_till.getText().toString().trim().substring(5, 7));
                    int search_till_day = Integer.parseInt(tv_till.getText().toString().trim().substring(8, 10));

                } else {
                    Toast.makeText(RecordActivity.this, "선택한 날짜의 범위가 잘못되었습니다", Toast.LENGTH_SHORT).show();
                }

                break;


        }


    }


    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

        String pick_date = Integer.toString(year) + "-" + String.format("%02d-%02d", (monthOfYear + 1), dayOfMonth);
        // 빈자리를 무조건 0으로 채우겠다. %0(몇번째 자리까지 나타낼지)d
        if (mIsClickSince) {
            tv_since.setText(pick_date);
        } else {
            tv_till.setText(pick_date);
        }

    }


    /**
     * 검색 날짜 범위를 제대로 설정했는지 여부 반환.
     *
     * @return <strong>true</strong> 범위 제대로 설정 함 <strong>false</strong> 범위 제대로 설정 안함
     */
    private boolean isSetDateOk() {

        int since_year = Integer.parseInt(tv_since.getText().toString().trim().substring(0, 4));
        int since_month = Integer.parseInt(tv_since.getText().toString().trim().substring(5, 7));
        int since_day = Integer.parseInt(tv_since.getText().toString().trim().substring(8, 10));

        int till_year = Integer.parseInt(tv_till.getText().toString().trim().substring(0, 4));
        int till_month = Integer.parseInt(tv_till.getText().toString().trim().substring(5, 7));
        int till_day = Integer.parseInt(tv_till.getText().toString().trim().substring(8, 10));

        if (since_year > till_year) {
            return false;
        } else {

            if (since_month > till_month) {
                return false;
            } else if (since_month == till_month) {

                if (since_day > till_day) {
                    return false;
                }

            }

            return true;
        }
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
    }


    private void makeChart() {
        LineDataSet lineDataSet = new LineDataSet(entries, "속성명1"); // 여기 쓴거 다 확실하지않음
        lineDataSet.setLineWidth(2); // 라인 두께
        lineDataSet.setCircleRadius(1); // 점 크기
        lineDataSet.setCircleColor(Color.parseColor("#FFA1B4DC")); // 점 색깔
        lineDataSet.setCircleColorHole(Color.BLUE); // 점 겉 색깔
        // lineDataSet.setCircleColorHole(Color.parseColor("#FFA1B4DC"));
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

    private class MyXAxisValueFormatter implements XAxisValueFormatter {

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