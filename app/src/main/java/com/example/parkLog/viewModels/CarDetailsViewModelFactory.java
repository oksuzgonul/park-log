package com.example.parkLog.viewModels;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.parkLog.data.AppDataBase;

public class CarDetailsViewModelFactory extends ViewModelProvider.NewInstanceFactory {
    private final AppDataBase dataBase;
    private final int carId;

    public CarDetailsViewModelFactory(AppDataBase dataBase, int carId) {
        this.dataBase = dataBase;
        this.carId = carId;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new CarDetailsViewModel(dataBase, carId);
    }
}
