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
public interface CarDao {

    @Query("SELECT * FROM car ORDER BY car_name")
    LiveData<List<Car>> loadAllCars();

    @Insert
    void insertCar(Car car);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateCar(Car car);

    @Delete
    void deleteCar(Car car);

    @Query("SELECT * FROM car WHERE id = :id")
    LiveData<Car> getCarById(int id);

    @Query("SELECT * FROM car ORDER BY parked_time DESC LIMIT 1")
    Car getLastParkedCar();
}
