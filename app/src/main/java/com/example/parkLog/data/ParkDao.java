package com.example.parkLog.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ParkDao {
    @Query("SELECT * FROM park ORDER BY park_location_name")
    LiveData<List<Park>> loadAllParks();

    @Insert
    void insertPark(Park park);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updatePark(Park park);

    @Delete
    void deletePark(Park park);

    @Query("SELECT * FROM park WHERE id = :id")
    LiveData<Park> getParkById(int id);

    @Query("SELECT * FROM park WHERE parked_car_id = :parked_car_id ORDER BY park_time DESC")
    LiveData<List<Park>> getParkByCarId(int parked_car_id);

    //Getting the most recent non-finalized parking
    @Query("SELECT * FROM park WHERE parked_car_id = :parked_car_id AND park_end_time is NULL ORDER BY park_time DESC LIMIT 1")
    LiveData<Park> getCurrentParkByCarId(int parked_car_id);
}
