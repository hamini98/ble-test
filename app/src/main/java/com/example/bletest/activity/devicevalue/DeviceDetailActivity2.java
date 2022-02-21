package com.example.bletest.activity.devicevalue;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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

import java.text.SimpleDateFormat;
import java.util.Date;

public class DeviceDetailActivity2 extends AppCompatActivity {

    TextView ShowValue_TestView, ShowValue_TestView2;
    EditText Value_EditText, Date_EditText;
    Button Show_Button, Reset_Button;
    Button Today_Button, Week_Button, Mount_Button;
    DeviceValueDomainHelper deviceValueDomainHelper;
    SQLiteDatabase valueDb;

    //화면 전환
    private Button btn_chart;


    private final static String TAG = DeviceDetailActivity2.class.getSimpleName();

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    private String deviceName;
    private String deviceAddress;
 //   private DeviceValueDomainHelper deviceValueDomainHelper;
 //   private SQLiteDatabase valueDb;


    private ListView deviceValueList;
    private ListView deviceValueList2; // 1 이거 추가
    private DeviceValueListAdapter deviceValueListAdpter;
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

    private void displayData(String value) {
        final String deviceValue = value;

        Date dt = new Date();
        //변수타입 변수명 = new(클래스객체생성) Date();
        // 현재 날짜 받아옴
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        final String recDate = simpleDateFormat.format(dt);

        //DB 에 값 insert하는거
        ContentValues values = new ContentValues();
        values.put(DeviceValueContract.DeviceValueEntry.COLUMN_NAME_VALUE, deviceValue);
        values.put(DeviceValueContract.DeviceValueEntry.COLUMN_NAME_REC_DATE, recDate);
        //recDate = 현재 시간

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
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_value_view);

        deviceValueDomainHelper = new DeviceValueDomainHelper(this);
        valueDb = deviceValueDomainHelper.getReadableDatabase();


        Value_EditText = (EditText)findViewById(R.id.editText);
        Date_EditText = (EditText)findViewById(R.id.editText2);
        btn_chart = (Button)findViewById(R.id.btn_chart);

        init(); //호출

        getDbValue();

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        //Intent 변수 = new 객체(this = 현재 액티비티랑 Service 연결해주는거 생성)
        bindService(gattServiceIntent, serviceConnection, BIND_AUTO_CREATE);
        //백그라운드로 받아온 서비스 돌려 (서비스 연결됬을때 실행할 작업들, 자동생성)

        // 조회 버튼
        Show_Button = (Button)findViewById(R.id.btn_search);
        Show_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                valueDb = deviceValueDomainHelper.getReadableDatabase();
                Cursor cursor;
                cursor = valueDb.rawQuery("SELECT * FROM value Where device_id ='" + deviceAddress + "'", null);

                //String text = "문자";
                //String testText = "SELECT * FROM value Where device_id ='" + text + "'";

                // SELECT * FROM value Where device_id ='문자'

                //"SELECT * FROM value;" + " '' "
                //"SELECT * FROM value;" + '"  "'
                //SELECT * FROM value;''
                String Value2 = "Value" + "\r\n";
                /**
                 * Value
                 *
                 */

                DeviceValueListAdapter deviceValueListAdapter = new DeviceValueListAdapter();
                deviceValueList.setAdapter(deviceValueListAdapter);


                while (cursor.moveToNext())
                {
                    deviceValueListAdapter.addValue(cursor.getString(1), cursor.getString(2));
                    //Value2 += cursor.getString(2) + "         " + cursor.getString(1) + "\r\n";
                }


                deviceValueListAdapter.notifyDataSetChanged(); // 바뀐게 있으면 화면 바꿔
                cursor.close();
                valueDb.close();
            }
        });

        // 초기화 버튼
        Reset_Button = (Button)findViewById(R.id.button3);
        Reset_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                valueDb = deviceValueDomainHelper.getWritableDatabase();
                deviceValueDomainHelper.onUpgrade(valueDb, 1, 2);
                valueDb.close();
            }
        });

        // 오늘꺼 조회
        Today_Button = (Button)findViewById(R.id.button6);
        Today_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //ShowValue_TestView2.setText("Value");
                valueDb = deviceValueDomainHelper.getReadableDatabase();

                Cursor cursor;
                cursor = valueDb.rawQuery("SELECT * FROM value Where device_id = '" + deviceAddress + "' AND date(rec_date) = date('now', 'localtime');", null);
                // SELECT * FROM value Where device_id = '
                String Value2 = "Value" + "\r\n";

                DeviceValueListAdapter deviceValueListAdapter = new DeviceValueListAdapter();
                deviceValueList2.setAdapter(deviceValueListAdapter);

                while (cursor.moveToNext())
                {
                    deviceValueListAdapter.addValue(cursor.getString(1), cursor.getString(2));
                 //   Value2 += cursor.getString(2) + "         " + cursor.getString(1) + "\r\n";
                }

                deviceValueListAdapter.notifyDataSetChanged(); // 바뀐게 있으면 화면 바꿔
                cursor.close();
                valueDb.close();
                Log.v("일일 버튼","누름");
            }
        });

        // 주간 조회 버튼
        Week_Button = (Button)findViewById(R.id.button7);
        Week_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //ShowValue_TestView2.setText("");
                valueDb = deviceValueDomainHelper.getReadableDatabase();
                Cursor cursor;
                cursor = valueDb.rawQuery("SELECT * FROM value Where device_id = '" + deviceAddress + "' AND date(rec_date) >= date('now','weekday 0', '-7 days', 'localtime') AND date(rec_date) <= date('now','weekday 0', '-1 days', 'localtime');", null);

                String Value2 = "Value" + "\r\n";

                DeviceValueListAdapter deviceValueListAdapter = new DeviceValueListAdapter();
                deviceValueList2.setAdapter(deviceValueListAdapter);


                while (cursor.moveToNext())
                {
                    deviceValueListAdapter.addValue(cursor.getString(1), cursor.getString(2));
                   // Value2 += cursor.getString(2) + "         " + cursor.getString(1) + "\r\n";
                }
              //  ShowValue_TestView2.setText(Value2);
                deviceValueListAdapter.notifyDataSetChanged(); // 바뀐게 있으면 화면 바꿔
                cursor.close();
                valueDb.close();
                Log.v("주간 버튼","누름");
            }
        });

        // 월간 조회 버튼
        Mount_Button = (Button)findViewById(R.id.button8);
        Mount_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                valueDb = deviceValueDomainHelper.getReadableDatabase(); // SELECT 할때 필요한 권한
                Cursor cursor;
                cursor = valueDb.rawQuery("SELECT * FROM value WHERE device_id = '" + deviceAddress + "' AND date(rec_date) >= date('now','start of month','localtime') AND date(rec_date) <= date('now','start of month','+1 month','-1 day','localtime');", null);
               // String king = "킹";


                // 이강민 킹 '개발자' == 자바
                // SQL 구문 == 문자열 인식을 '' 이걸로 한다.
                // 이강민 개발자
                //"''"

                // SELECT * FROM device_id = asd;
                // SELECT * FROM device_id = 'asd';

                momo(cursor);
                Log.v("월간 버튼","누름");
            }
        });

        btn_chart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent chartintent = new Intent(DeviceDetailActivity2.this, ChartActivity.class);
                startActivity(chartintent);
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

        deviceValueList = (ListView) findViewById(R.id.device_value_list);
        deviceValueList2 = (ListView) findViewById(R.id.device_value_list2); // 2얘도추가

        //리스트 목록만
        deviceValueList.setAdapter(deviceValueListAdpter);
        deviceValueList2.setAdapter(deviceValueListAdpter); // 3 이렇게 3개하니까 똑같이 나옴
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


}
