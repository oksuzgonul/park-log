package com.example.parkLog.viewModels;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.parkLog.data.AppDataBase;

public class ParkDetailsViewModelFactory  extends ViewModelProvider.NewInstanceFactory {
    private final AppDataBase dataBase;
    private final int parkId;

    public ParkDetailsViewModelFactory(AppDataBase dataBase, int parkId) {
        this.dataBase = dataBase;
        this.parkId = parkId;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new ParkDetailsViewModel(dataBase, parkId);
    }
}
