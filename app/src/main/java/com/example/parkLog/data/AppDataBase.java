package com.example.parkLog.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverter;
import androidx.room.TypeConverters;

@Database(entities = {Car.class, Park.class}, version = 1, exportSchema = false)
@TypeConverters({DoubleConverter.class, DateConverter.class})
public abstract class AppDataBase extends RoomDatabase {
    private static final Object LOCK = new Object();
    private static final String DB_NAME = "parkLogDb";
    private static AppDataBase instance;

    public static AppDataBase getInstance(Context context) {
        if (instance == null) {
            synchronized (LOCK) {
                instance = Room.databaseBuilder(context.getApplicationContext(),
                        AppDataBase.class, AppDataBase.DB_NAME)
                        .build();
            }
        }
        return instance;
    }
    public abstract CarDao carDao();
    public abstract ParkDao parkDao();
}
