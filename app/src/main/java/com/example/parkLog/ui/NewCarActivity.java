package com.example.parkLog.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProviders;

import com.example.parkLog.R;
import com.example.parkLog.data.AppDataBase;
import com.example.parkLog.data.Car;
import com.example.parkLog.utils.AppExecutors;
import com.example.parkLog.viewModels.CarDetailsViewModel;
import com.example.parkLog.viewModels.CarDetailsViewModelFactory;

public class NewCarActivity extends AppCompatActivity {

    private AppDataBase dataBase;
    private EditText car_make;
    private EditText license_plate;
    private EditText car_owner;

    private int currentCarId = DEFAULT_CAR_ID;
    private boolean parkedState;

    public static final int DEFAULT_CAR_ID = -1;
    public static final String EDIT_CAR_EXTRA = "edit_car_extra";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_car_activity);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        dataBase = AppDataBase.getInstance(getApplicationContext());

        car_make = findViewById(R.id.car_name);
        license_plate = findViewById(R.id.license_plate);
        car_owner = findViewById(R.id.car_owner);

        Button button = findViewById(R.id.car_save_button);
        button.setOnClickListener( view -> saveCar());

        Intent intent = getIntent();
        if (intent != null) {
            currentCarId = intent.getIntExtra(EDIT_CAR_EXTRA, DEFAULT_CAR_ID);
            if (currentCarId != DEFAULT_CAR_ID) {
                button.setText(R.string.update_car_button_title);
                CarDetailsViewModelFactory carDetailsViewModelFactory =
                        new CarDetailsViewModelFactory(dataBase, currentCarId);
                CarDetailsViewModel viewModel =
                        ViewModelProviders.of(this, carDetailsViewModelFactory)
                                .get(CarDetailsViewModel.class);
                viewModel.getCar().observe(this, car -> {
                    parkedState = car.getParkedState();
                    repopulateEditTexts(car);
                });
            }
        }
    }

    private void repopulateEditTexts(Car car) {
        car_make.setText(car.getCarName());
        car_owner.setText(car.getCarOwner());
        license_plate.setText(car.getLicensePlate());
    }

    private void saveCar() {
        if (car_make.getText().toString().isEmpty()) {
            Toast.makeText(getApplicationContext(),
                    R.string.car_name_warning_toast,
                    Toast.LENGTH_LONG)
                    .show();
        } else {
            Car car = new Car(
                    car_make.getText().toString(),
                    license_plate.getText().toString(),
                    car_owner.getText().toString(),
                    parkedState);

            AppExecutors.getInstance().diskIO().execute(() -> {
                if (currentCarId == DEFAULT_CAR_ID) {
                    dataBase.carDao().insertCar(car);
                } else  {
                    car.setId(currentCarId);
                    dataBase.carDao().updateCar(car);
                }
                finish();
            });
        }
    }
}
