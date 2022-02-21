package com.example.bletest.activity.chart;
// 얘 지금 안쓰는중
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.util.EventLogTags;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bletest.R;
import com.example.bletest.activity.devicevalue.DeviceDetailActivity;
import com.example.bletest.domain.DeviceValueContract;
import com.example.bletest.domain.DeviceValueDomainHelper;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.formatter.XAxisValueFormatter;
import com.github.mikephil.charting.formatter.YAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class ChartActivity extends AppCompatActivity {

    DeviceValueDomainHelper deviceValueDomainHelper;
    SQLiteDatabase valueDb;
    Button DayButton,WeekButton,MonthButton;
    private String Final_UserID, Final_UserName, Final_UserAge, Final_UserGender, Final_UserWeight;

    LineDataSet setComp1;

    List<Entry> entries = new ArrayList<>();
    ArrayList<Entry> valsComp1 = new ArrayList<Entry>();
    ArrayList<String> xVals = new ArrayList<String>();

    private String[] projection = {
            BaseColumns._ID, //0번째
            DeviceValueContract.DeviceValueEntry.COLUMN_NAME_VALUE, //1번째
            DeviceValueContract.DeviceValueEntry.COLUMN_NAME_REC_DATE //2번째
    };

    String sortOrder = DeviceValueContract.DeviceValueEntry.COLUMN_NAME_REC_DATE + " DESC";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chart_main);

        deviceValueDomainHelper = new DeviceValueDomainHelper(this);
        valueDb = deviceValueDomainHelper.getReadableDatabase();

        DayButton = (Button) findViewById(R.id.btn_today);
        DayButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {


                int i=0; // 실시간이라서 계속 쌓아야되니까 전역변수인데 주간, 월간 이런식으로 조회할땐 초기화해줘야되서 지역변수야

                /*Cursor cursor;
                cursor = valueDb.query(
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
                );*/
                xVals.clear();
                Cursor cursor;
                cursor = valueDb.rawQuery("SELECT " + DeviceValueContract.DeviceValueEntry.COLUMN_NAME_VALUE +
                        ", " + DeviceValueContract.DeviceValueEntry.COLUMN_NAME_REC_DATE + " FROM value", null); // 조회 부분

                while (cursor.moveToNext()) { // 값 넣어주는 부분
                    entries.add(new Entry(Float.parseFloat(cursor.getString(0)), i));
                    xVals.add("" + i++);
                }

              /*//  entries.add(new Entry(valueDb.rawQuery("SELECT"), i++));
                //entries.add(new Entry(val, xlndex));
                entries.add(new Entry(2, 2));
                entries.add(new Entry(5, 3));
                entries.add(new Entry(4, 4));

               // Cursor cursor;
              //  cursor = valueDb.rawQuery("SELECT ")
                xVals.clear();
                xVals.add("" + 0);
                xVals.add("" + 1);
                xVals.add("" + 2);
                xVals.add("" + 3);
                xVals.add("" + 4);
                xVals.add("" + 5);*/

                LineDataSet lineDataSet = new LineDataSet(entries, "속성명1"); // 여기 쓴거 다 확실하지않음
                lineDataSet.setLineWidth(2); // 라인 두께
                lineDataSet.setCircleRadius(6); // 점 크기
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
                chart.animateY(2000, Easing.EasingOption.EaseInCubic);
                chart.invalidate();
            }
        });
    }
}
