package com.example.parkLog.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.lifecycle.ViewModelProviders;

import com.example.parkLog.R;
import com.example.parkLog.data.AppDataBase;
import com.example.parkLog.data.Car;
import com.example.parkLog.data.Park;
import com.example.parkLog.fragments.CarSummaryFragment;
import com.example.parkLog.utils.AppExecutors;
import com.example.parkLog.viewModels.CarDetailsViewModel;
import com.example.parkLog.viewModels.CarDetailsViewModelFactory;
import com.example.parkLog.viewModels.ParkDetailsViewModel;
import com.example.parkLog.viewModels.ParkDetailsViewModelFactory;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static com.example.parkLog.ui.CarDetailsActivity.CAR_EXTRA;
import static com.example.parkLog.ui.NewCarActivity.DEFAULT_CAR_ID;
import static com.example.parkLog.ui.NewParkActivity.DEFAULT_PARK_ID;

public class ParkDetailsActivity extends AppCompatActivity {
    private ImageView enlarged_picture;
    private Button endParkingButton;
    private int currentCarId;

    public static final String PARK_EXTRA = "park_extra";
    public static final String CAR_ID_EXTRA = "car_id_extra";
    private AppDataBase dataBase;
    private Park currentPark;
    private Car currentCar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.park_details_activity);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        enlarged_picture = findViewById(R.id.enlarged_picture);
        enlarged_picture.setOnClickListener(view -> {
            enlarged_picture.setVisibility(View.GONE);
            enlarged_picture.setImageDrawable(null);
        });

        dataBase = AppDataBase.getInstance(getApplicationContext());
        Intent intent = getIntent();
        if (intent != null) {
            int currentParkId = intent.getIntExtra(PARK_EXTRA, DEFAULT_PARK_ID);
            currentCarId = intent.getIntExtra(CAR_ID_EXTRA, DEFAULT_CAR_ID);
            if (currentParkId != DEFAULT_PARK_ID) {
                ParkDetailsViewModelFactory parkDetailsViewModelFactory =
                        new ParkDetailsViewModelFactory(dataBase, currentParkId);
                ParkDetailsViewModel viewModel =
                        ViewModelProviders.of(this, parkDetailsViewModelFactory)
                                .get(ParkDetailsViewModel.class);
                viewModel.getPark().observe(this, park -> {
                    if (park != null) {
                        setTitle(park.getParkLocationName());
                        currentPark = park;
                        bindDataToParkDetails(park);
                        if (park.getParkEndTime() == null) {
                            endParkingButton.setVisibility(View.VISIBLE);
                        }
                        addTrackingFragment(park);
                    }
                });
                CarDetailsViewModelFactory carDetailsViewModelFactory =
                        new CarDetailsViewModelFactory(dataBase, currentCarId);
                CarDetailsViewModel carDetailsViewModel =
                        ViewModelProviders.of(this, carDetailsViewModelFactory)
                                .get(CarDetailsViewModel.class);
                carDetailsViewModel.getCar().observe(this, car -> currentCar = car);
                carDetailsViewModel.getCurrentPark().observe(this, park -> {
                    if (currentCar != null) {
                        //Car is parked if the getCurrentParkByCarId() Dao method does not result null
                        currentCar.setParkedState(park != null);
                    }
                });
            }
        }
    }

    private void addTrackingFragment(Park park) {
        if (park != null) {
            CarSummaryFragment carSummaryFragment = new CarSummaryFragment();
            carSummaryFragment.setCurrentPark(park);
            getSupportFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .replace(R.id.fragment_container_view, carSummaryFragment)
                    .commit();
        }
    }

    private void bindDataToParkDetails(Park park) {
        if (park.getPictureLocation() != null) {
            setPicture(park.getPictureLocation());
        }

        TextView floorTextView = findViewById(R.id.floor_number);
        if (!park.getFloor().isEmpty()) {
            String floorText = getString(R.string.parked_on_floor_text) + " " + park.getFloor();
            floorTextView.setText(floorText);
        } else {
            floorTextView.setVisibility(View.GONE);
        }

        TextView lotTextView = findViewById(R.id.lot_number);
        if (!park.getLot().isEmpty()) {
            String lotNumberText = getString(R.string.lot_number_text) + " " + park.getLot();
            lotTextView.setText(lotNumberText);
        } else {
            lotTextView.setVisibility(View.GONE);
        }

        TextView ticketValidTextView = findViewById(R.id.ticket_expiration);
        SimpleDateFormat parkTimeFormat =
                new SimpleDateFormat("MMM dd yyyy, HH:mm", Locale.getDefault());
        String parkValidTime = parkTimeFormat.format(park.getValidTime());
        String ticketValidText = getString(R.string.ticket_expires_on) + " " + parkValidTime;
        ticketValidTextView.setText(ticketValidText);

        CardView cardView = findViewById(R.id.details_card_view);
        cardView.setVisibility(View.VISIBLE);

        endParkingButton = findViewById(R.id.finish_parking_btn);
        endParkingButton.setOnClickListener(view -> {
            currentPark.setParkEndTime(Calendar.getInstance().getTime());
            AppExecutors.getInstance().diskIO().execute(() -> dataBase.parkDao().updatePark(currentPark));
            AppExecutors.getInstance().diskIO().execute(() -> dataBase.carDao().updateCar(currentCar));
            endParkingButton.setVisibility(View.GONE);
        });
    }
    private void setPicture(String pictureLocation) {
        Bitmap bitmap = BitmapFactory.decodeFile(pictureLocation);
        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        ImageView picture = findViewById(R.id.ticket_picture);
        CardView cardView = findViewById(R.id.photo_card_view);
        cardView.setVisibility(View.VISIBLE);
        picture.setVisibility(View.VISIBLE);
        picture.setImageBitmap(rotatedBitmap);
        picture.setOnClickListener(view -> enlargePicture(rotatedBitmap));
    }

    private void enlargePicture(Bitmap bitmap) {
        enlarged_picture.setVisibility(View.VISIBLE);
        enlarged_picture.setImageBitmap(bitmap);
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
            editPark();
        } else if (id == R.id.delete_item) {
            deletePark();
        } else if (id == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    private void deletePark() {
        DialogInterface.OnClickListener dialogConfirmDelete  = (dialogInterface, i) -> {
            switch (i) {
                case DialogInterface.BUTTON_NEGATIVE:
                    break;
                case DialogInterface.BUTTON_POSITIVE:
                    AppExecutors.getInstance().diskIO().execute(() -> {
                        dataBase.parkDao().deletePark(currentPark);
                        finish();
                    });
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.delete_warning_title))
                .setMessage(R.string.delete_park_warning_message)
                .setPositiveButton(R.string.delete_warning_yes, dialogConfirmDelete)
                .setNegativeButton(R.string.delete_warning_no, dialogConfirmDelete)
                .show();
    }

    private void editPark() {
        Intent intent = new Intent(this, NewParkActivity.class);
        intent.putExtra(NewParkActivity.EDIT_PARK_EXTRA, currentPark.getId());
        startActivity(intent);
    }
}
