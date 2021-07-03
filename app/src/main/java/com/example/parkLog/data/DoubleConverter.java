package com.example.parkLog.data;

import androidx.room.TypeConverter;

public class DoubleConverter {
    @TypeConverter
    public static Double toDouble(String doubleString) {
        return doubleString == null ? null : Double.parseDouble(doubleString);
    }

    @TypeConverter
    public static String toStr(Double d) {
        return d == null ? null : d.toString();
    }
}
