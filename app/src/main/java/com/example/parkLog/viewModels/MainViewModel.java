package com.example.parkLog.viewModels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.parkLog.data.AppDataBase;
import com.example.parkLog.data.Car;

import java.util.List;

public class MainViewModel extends AndroidViewModel {
    private LiveData<List<Car>> cars;

    public MainViewModel(@NonNull Application application) {
        super(application);
        AppDataBase dataBase = AppDataBase.getInstance(this.getApplication());
        cars = dataBase.carDao().loadAllCars();
    }

    public LiveData<List<Car>> getCars() {
        return cars;
    }
}
