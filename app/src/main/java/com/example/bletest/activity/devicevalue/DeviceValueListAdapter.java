package com.example.bletest.activity.devicevalue;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.bletest.R;
import com.example.bletest.dto.DeviceValueDto;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class DeviceValueListAdapter extends BaseAdapter {
    private TextView recDateView;
    private TextView deviceValueView;

    private ArrayList<DeviceValueDto> deviceValueList;

    public DeviceValueListAdapter() {
        deviceValueList = new ArrayList<DeviceValueDto>();
    }

    @Override
    public int getCount() { // List의 item 개수를 반환한다. return 되는 개수에 따라 생성되는 view 개수가 결정된다
        return this.deviceValueList.size();
        //특수한 경우가 아니라면, 데이터의 개수만큼 세팅하면 되기 때문에, ArrayList의 size로 세팅
    }

    @Override
    public DeviceValueDto getItem(int i) {
        return this.deviceValueList.get(i);
    }
    //getItem(int position) List의 posion에 해당하는 데이터 반환.

    @Override
    public long getItemId(int i) {
        return i;
    }
    //List의 position에 해당하는 Item의 ID를 반환. 각각의 Item에 고유한 ID를 부여함으로써, 차후 ID를 이용한 검색, 삭제 등의 컨트롤이 가능하도록 합니다. 예제와 같이 position값을 ID로 반환하도록 작성해주시면 되겠습니다.

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Context context = parent.getContext();

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.listitem_device_value, parent, false);
        }
        //List의 position에 해당하는 View를 반환합니다. getView()는 실제로 화면에 보여질 View를 구성하기 위해, 미리 생성해두었던 레이아웃과 연결하고 데이터를 세팅하는 작업을 수행하도록 작성하시면 됩니다.

        recDateView = (TextView) convertView.findViewById(R.id.rec_date);
        deviceValueView = (TextView) convertView.findViewById(R.id.device_value);

        final DeviceValueDto value = deviceValueList.get(position);

        recDateView.setText(value.getRecDate());
        deviceValueView.setText(value.getValue());

        return convertView;
    }

    public void addValue(final String value, final String recDate) {
        this.deviceValueList.add(0, new DeviceValueDto(value, recDate));
        // 맨 위로 데이터 올라오게
    }

    public void clear() {
        deviceValueList.clear();
    }
}
