package com.example.parkLog.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentContainerView;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.parkLog.R;
import com.example.parkLog.data.AppDataBase;
import com.example.parkLog.data.Car;
import com.example.parkLog.data.Park;
import com.example.parkLog.fragments.CarSummaryFragment;
import com.example.parkLog.utils.AppExecutors;
import com.example.parkLog.viewModels.CarDetailsViewModel;
import com.example.parkLog.viewModels.CarDetailsViewModelFactory;
import com.example.parkLog.utils.ParkAdapter;

import java.util.ArrayList;
import java.util.List;

import static com.example.parkLog.ui.NewCarActivity.DEFAULT_CAR_ID;
import static com.example.parkLog.ui.NewCarActivity.EDIT_CAR_EXTRA;
import static com.example.parkLog.ui.ParkDetailsActivity.CAR_ID_EXTRA;
import static com.example.parkLog.ui.ParkDetailsActivity.PARK_EXTRA;

public class CarDetailsActivity extends AppCompatActivity implements ParkAdapter.ItemClickListener {
    public static final String CAR_EXTRA = "car_extra";
    private AppDataBase dataBase;
    private Car currentCar;
    private int currentCarId;
    private final List<Park> currentCarParkList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.car_details_activity);
        dataBase = AppDataBase.getInstance(getApplicationContext());
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        RecyclerView recyclerView = findViewById(R.id.parking_history_list);

        LinearLayoutManager linearLayoutManager =
                new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);

        ParkAdapter parkAdapter = new ParkAdapter(this, this);
        parkAdapter.setDataBase(dataBase);
        recyclerView.setAdapter(parkAdapter);

        Intent intent = getIntent();
        if (intent != null) {
            currentCarId = intent.getIntExtra(CAR_EXTRA, DEFAULT_CAR_ID);
            if (currentCarId != DEFAULT_CAR_ID) {
                CarDetailsViewModelFactory carDetailsViewModelFactory =
                        new CarDetailsViewModelFactory(dataBase, currentCarId);
                CarDetailsViewModel viewModel =
                        ViewModelProviders.of(this, carDetailsViewModelFactory)
                                .get(CarDetailsViewModel.class);
                viewModel.getCar().observe(this, carEntry -> {
                    if (carEntry != null) {
                        setTitle(carEntry.getCarName());
                        bindValues(carEntry);
                    }
                    currentCar = carEntry;
                });
                viewModel.getParks().observe(this, parks -> {
                    parkAdapter.setParks(parks);
                    currentCarParkList.addAll(parks);
                });

                viewModel.getCurrentPark().observe(this, park -> {
                    CarSummaryFragment carSummaryFragment = new CarSummaryFragment();
                    if (park != null) {
                        carSummaryFragment.setCurrentPark(park);
                        getSupportFragmentManager().beginTransaction()
                                .setReorderingAllowed(true)
                                .replace(R.id.fragment_container_view, carSummaryFragment)
                                .commit();
                    } else if (getSupportFragmentManager().findFragmentById(R.id.fragment_container_view) != null) {
                        getSupportFragmentManager().beginTransaction()
                                .remove(getSupportFragmentManager().findFragmentById(R.id.fragment_container_view))
                                .commit();
                    }
                    if (currentCar != null) {
                        currentCar.setParkedState(park != null);
                        AppExecutors.getInstance().diskIO().execute(() -> dataBase.carDao().updateCar(currentCar));
                    }
                });
            }
        }

    }

    private void bindValues(Car car) {
        ImageView trackedLogo = findViewById(R.id.tracked_logo);
        TextView trackingState = findViewById(R.id.tracking_state);
        if (car.getParkedState()) {
            trackedLogo.setImageResource(R.drawable.ic_map_tracked);
            trackingState.setText(R.string.currently_tracked);
        } else {
            trackedLogo.setImageResource(R.drawable.ic_map_not_tracked);
            trackingState.setText(R.string.currently_not_tracked);
        }

        String ownerText;
        if (!car.getCarOwner().isEmpty()) {
            ownerText = getString(R.string.belongs_to) + " " + car.getCarOwner();
        } else {
            ownerText = getString(R.string.owner_not_specified);
        }
        TextView carOwner = findViewById(R.id.car_owner);
        carOwner.setText(ownerText);

        if (!car.getLicensePlate().isEmpty()) {
            TextView licensePlate = findViewById(R.id.license_plate_text_view);
            licensePlate.setText(car.getLicensePlate());
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_delete_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.edit_item) {
            editCar();
        } else if (id == R.id.delete_item) {
            deleteCar();
        }
        return super.onOptionsItemSelected(item);
    }

    private void deleteCar() {

        DialogInterface.OnClickListener dialogConfirmDelete  = (dialogInterface, i) -> {
            switch (i) {
                case DialogInterface.BUTTON_NEGATIVE:
                    break;
                case DialogInterface.BUTTON_POSITIVE:
                    AppExecutors.getInstance().diskIO().execute(() -> {
                        deleteRelatedParks();
                        dataBase.carDao().deleteCar(currentCar);
                        finish();
                    });
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.delete_warning_title))
                .setMessage(R.string.delete_car_warning_message)
                .setPositiveButton(R.string.delete_warning_yes, dialogConfirmDelete)
                .setNegativeButton(R.string.delete_warning_no, dialogConfirmDelete)
                .show();

    }

    private void deleteRelatedParks() {
        for (Park park : currentCarParkList) {
            AppExecutors.getInstance().diskIO().execute(() -> dataBase.parkDao().deletePark(park));
        }
    }

    private void editCar() {
        Intent intent = new Intent(this, NewCarActivity.class);
        intent.putExtra(EDIT_CAR_EXTRA, currentCar.getId());
        startActivity(intent);
    }

    @Override
    public void onItemClickListener(int itemId) {
        Intent intent = new Intent(this, ParkDetailsActivity.class);
        intent.putExtra(PARK_EXTRA, itemId);
        intent.putExtra(CAR_ID_EXTRA, currentCarId);
        startActivity(intent);
    }
}
