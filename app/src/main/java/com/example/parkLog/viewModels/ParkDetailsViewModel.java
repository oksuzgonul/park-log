package com.example.parkLog.viewModels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.parkLog.data.AppDataBase;
import com.example.parkLog.data.Park;

public class ParkDetailsViewModel extends ViewModel {
    private LiveData<Park> park;

    public ParkDetailsViewModel(AppDataBase dataBase, int parkId) {
        park = dataBase.parkDao().getParkById(parkId);
    }

    public  LiveData<Park> getPark() { return park; }
}
