package com.example.parkLog.viewModels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.parkLog.data.AppDataBase;
import com.example.parkLog.data.Car;
import com.example.parkLog.data.Park;

import java.util.List;

public class CarDetailsViewModel extends ViewModel {
    private LiveData<Car> car;
    private LiveData<List<Park>> parks;
    private LiveData<Park> currentPark;

    public CarDetailsViewModel(AppDataBase dataBase, int carId) {
        car = dataBase.carDao().getCarById(carId);
        parks = dataBase.parkDao().getParkByCarId(carId);
        currentPark = dataBase.parkDao().getCurrentParkByCarId(carId);
    }

    public LiveData<Car> getCar() {
        return car;
    }
    public LiveData<List<Park>> getParks() { return parks; }
    public LiveData<Park> getCurrentPark() { return currentPark; }
}
