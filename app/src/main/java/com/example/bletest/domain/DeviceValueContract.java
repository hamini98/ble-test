package com.example.bletest.domain;

import android.provider.BaseColumns;

public final class DeviceValueContract {
    private DeviceValueContract() {}

    public static class DeviceValueEntry implements BaseColumns {
        public static final String TABLE_NAME = "value"; // value(값)을 TABLE_NAME(변수명)에다 넣은거
        public static final String COLUMN_NAME_VALUE = "value";
        public static final String COLUMN_NAME_REC_DATE = "rec_date"; // 이름 설정
        public static final String DEVICE_ID = "device_id";
    }

    public static final String SQL_CREATE_ENTRIES = "CREATE TABLE " + DeviceValueEntry.TABLE_NAME + " (" +
            DeviceValueEntry._ID + " INTEGER PRIMARY KEY," + //0
            DeviceValueEntry.COLUMN_NAME_VALUE + " VARCHAR(100)," + // 1
            DeviceValueEntry.COLUMN_NAME_REC_DATE + " VARCHAR(100)," + // 타입 설정 2
            DeviceValueEntry.DEVICE_ID + " VARCHAR(100))";




    public static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + DeviceValueEntry.TABLE_NAME;


}
