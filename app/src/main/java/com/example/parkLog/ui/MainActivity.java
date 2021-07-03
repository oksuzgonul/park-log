package com.example.parkLog.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.example.parkLog.BuildConfig;
import com.example.parkLog.data.Car;
import com.example.parkLog.utils.CarAdapter;
import com.example.parkLog.R;
import com.example.parkLog.viewModels.MainViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

import timber.log.Timber;

import static com.example.parkLog.ui.CarDetailsActivity.CAR_EXTRA;

public class MainActivity extends AppCompatActivity implements CarAdapter.ItemClickListener {
    private FloatingActionButton floatingActionButton;
    private CarAdapter carAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }

        //Setting the tag for the Timber
        Timber.tag("MainActivity");
        //Recording debug message with the above tag
        Timber.d("Activity Created via Timber");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        RecyclerView recyclerView = findViewById(R.id.car_recycler_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this,
                RecyclerView.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
        carAdapter = new CarAdapter(this, this);
        recyclerView.setAdapter(carAdapter);

        MainViewModel viewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        viewModel.getCars().observe(this, carListEntry -> {
            checkCarListEmpty(carListEntry);
            carAdapter.setCars(carListEntry);
        });

        floatingActionButton = findViewById(R.id.floatingActionButton);
        floatingActionButton.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), NewParkActivity.class);
            startActivity(intent);
        });
    }

    private void checkCarListEmpty(List<Car> carListEntry) {

        TextView noCarsText = findViewById(R.id.no_cars_text);
        if (carListEntry.size() == 0) {
            //Logging if the carList size can be detected correctly via Timber
            Timber.i("carList is empty");
            floatingActionButton.setAlpha(.1f);
            floatingActionButton.setClickable(false);
            noCarsText.setVisibility(View.VISIBLE);
        } else {
            floatingActionButton.setAlpha(1f);
            floatingActionButton.setClickable(true);
            noCarsText.setVisibility(View.GONE);
        }
    }

    @Override
    public void onItemClickListener(int itemId) {
        Intent intent = new Intent(this, CarDetailsActivity.class);
        intent.putExtra(CAR_EXTRA, itemId);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_car_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.add_car) {
            Intent intent = new Intent(getApplicationContext(), NewCarActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
}