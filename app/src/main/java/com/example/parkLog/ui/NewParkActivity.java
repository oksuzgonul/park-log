package com.example.parkLog.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.FileProvider;
import androidx.lifecycle.ViewModelProviders;

import com.example.parkLog.R;
import com.example.parkLog.data.AppDataBase;
import com.example.parkLog.data.Car;
import com.example.parkLog.data.Park;
import com.example.parkLog.fragments.TimePickerFragment;
import com.example.parkLog.utils.AppExecutors;
import com.example.parkLog.utils.ParkExpireBroadcast;
import com.example.parkLog.utils.ParkWidgetService;
import com.example.parkLog.viewModels.MainViewModel;
import com.example.parkLog.viewModels.ParkDetailsViewModel;
import com.example.parkLog.viewModels.ParkDetailsViewModelFactory;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import timber.log.Timber;

import static com.example.parkLog.ui.CarDetailsActivity.CAR_EXTRA;
import static com.example.parkLog.utils.ParkExpireBroadcast.CAR_EXTRA_BC_REC;

public class NewParkActivity extends AppCompatActivity implements OnMapReadyCallback,
        AdapterView.OnItemSelectedListener {

    public static final String PARK_UPDATES_CHANNEL_ID = "1234";
    public static final int PARK_UPDATES_NOTIFICATION_ID = 1;

    private MapView mapView;
    private ImageView picture;
    private ImageView enlarged_picture;
    private GoogleMap map;
    private int currentParkId;
    private Park parkToSave;
    private PlacesClient placesClient;
    private final ArrayList<Place> placeList = new ArrayList<>();

    private TimePickerFragment timePickerFragment;


    private Location lastDeviceLocation;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private boolean locationPermissionGranted;

    public static final String EDIT_PARK_EXTRA = "edit_park_extra";
    public static final int DEFAULT_PARK_ID = -1;

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final String LOCATION_KEY = "location";
    private static final LatLng defaultLocation = new LatLng(0, 0);
    private static final int DEFAULT_ZOOM = 15;
    private static final int REQUEST_CODE_LOCATION = 1;
    private static final String TAG  = NewParkActivity.class.getSimpleName();
    private static final int PLACE_LIST_MAX_SIZE = 5;

    private EditText parkingValidButton;
    private Button takePicButton;

    private String parkName;
    private LatLng parkLocation;
    private String parkSnippet;
    private String parkPicture;
    private int parkedCarId;
    private String parkPlaceId;
    private Date parkValid;

    private Car currentCar;

    private AppDataBase dataBase;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null && savedInstanceState.containsKey(EDIT_PARK_EXTRA)) {
            lastDeviceLocation = savedInstanceState.getParcelable(LOCATION_KEY);
            currentParkId = savedInstanceState.getInt(EDIT_PARK_EXTRA, DEFAULT_PARK_ID);
        }

        setContentView(R.layout.new_park_activity);

        createNotificationChannel();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Places.initialize(getApplicationContext(), getString(R.string.google_maps_key));
        placesClient = Places.createClient(this);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        Button pickParkingButton =  findViewById(R.id.current_location_button);
        pickParkingButton.setOnClickListener(view -> showNearbyPlaces());

        Button parkSaveButton = findViewById(R.id.park_save_button);
        parkSaveButton.setOnClickListener(view -> savePark());

        parkingValidButton = findViewById(R.id.parking_valid_button);
        parkingValidButton.setOnClickListener(view -> showTimePicker());

        takePicButton = findViewById(R.id.take_pic_button);
        takePicButton.setOnClickListener(view -> dispatchTakePictureIntent());

        enlarged_picture = findViewById(R.id.enlarged_picture);
        enlarged_picture.setOnClickListener(view -> {
            enlarged_picture.setVisibility(View.GONE);
            enlarged_picture.setImageDrawable(null);
        });


        List<Car> tempCarList = new ArrayList<>();
        Spinner spinner = findViewById(R.id.car_spinner);
        ArrayAdapter<Car> adapter =
                new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, tempCarList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setOnItemSelectedListener(this);
        spinner.setAdapter(adapter);

        dataBase = AppDataBase.getInstance(getApplicationContext());

        MainViewModel viewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        viewModel.getCars().observe(this, carListEntry -> {
            tempCarList.addAll(carListEntry);
            adapter.notifyDataSetChanged();
        });

        picture = findViewById(R.id.picture_image);

        mapView = findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        //If the activity is called with an existing park, it is edit
        // mode instead of a new park activity, so ViewModels are used
        // to capture the park data and the car data
        Intent intent = getIntent();
        if (intent != null) {
            currentParkId = intent.getIntExtra(EDIT_PARK_EXTRA, DEFAULT_PARK_ID);
            if (currentParkId != DEFAULT_PARK_ID) {
                parkSaveButton.setText(R.string.update_park_button_title);
                ParkDetailsViewModelFactory parkDetailsViewModelFactory =
                        new ParkDetailsViewModelFactory(dataBase, currentParkId);
                ParkDetailsViewModel parkDetailsViewModel =
                        ViewModelProviders.of(this, parkDetailsViewModelFactory)
                                .get(ParkDetailsViewModel.class);
                parkDetailsViewModel.getPark().observe(this, park -> {
                    bindParkItemToOldValues(park);
                    repopulateEditTexts(park);
                    putMarker(park);
                    if (park != null && park.getPictureLocation() != null) {
                        setPicture(park.getPictureLocation());
                    }
                });
            }
        }
    }

    private void bindParkItemToOldValues(Park park) {
        parkToSave = park;
        parkLocation = new LatLng(park.getParkLatitude(), park.getParkLongitude());
        parkName = park.getParkLocationName();
        parkValid = park.getValidTime();
        parkPicture = park.getPictureLocation();
        parkedCarId = park.getParkedCarId();
        parkSnippet = park.getParkSnippet();
        parkPlaceId = park.getParkPlaceId();
    }

    private void repopulateEditTexts(Park park) {
        EditText addressLocationText = findViewById(R.id.address_location_text);
        String parkingAddress =
                park.getParkLocationName() + "\n" + park.getParkSnippet();
        addressLocationText.setText(parkingAddress);
        EditText floorText = findViewById(R.id.floor_number);
        floorText.setText(park.getFloor());
        EditText lotText = findViewById(R.id.lot_number);
        lotText.setText(park.getLot());
        EditText parkValidText = findViewById(R.id.parking_valid_button);
        parkValidText.setText(park.getValidTime().toString());
    }

    private void putMarker(Park park) {
        LatLng savedLatLng =
                new LatLng(park.getParkLatitude(), park.getParkLongitude());
        map.addMarker(new MarkerOptions()
                .position(savedLatLng)
                .snippet(park.getParkSnippet())
                .title(park.getParkLocationName()));
    }

    private void setPicture(String pictureLocation) {
        Bitmap bitmap = BitmapFactory.decodeFile(pictureLocation);
        Log.i(TAG, pictureLocation);
        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        Bitmap rotatedBitmap = Bitmap
                .createBitmap(bitmap, 0, 0,
                        bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        picture.setVisibility(View.VISIBLE);
        takePicButton.setText(R.string.change_picture);
        picture.setImageBitmap(rotatedBitmap);
        picture.setOnClickListener(view -> enlargePicture(rotatedBitmap));
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        if (map != null) {
            outState.putParcelable(LOCATION_KEY, lastDeviceLocation);
        }
        outState.putInt(EDIT_PARK_EXTRA, currentParkId);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        map.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                View customInfoWindow = getLayoutInflater().inflate(R.layout.custom_marker,
                        findViewById(R.id.map), false);

                TextView infoTitle = customInfoWindow.findViewById(R.id.location_title);
                infoTitle.setText(marker.getTitle());

                TextView infoSnippet = customInfoWindow.findViewById(R.id.location_address);
                infoSnippet.setText(marker.getSnippet());

                return customInfoWindow;
            }
        });
        checkLocationPermission();
    }

    @Override
    public void onResume() {
        mapView.onResume();
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    private void checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED ) {
            locationPermissionGranted = true;
            updateMapView();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_CODE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        locationPermissionGranted = false;
        if (requestCode == REQUEST_CODE_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationPermissionGranted = true;
            }
        }
        updateMapView();
    }

    //if the location permission is given, activates the location button on map
    //and runs getDeviceLocation(), if not, checks for location permission again.
    private void updateMapView() {
        if (map == null) {
            return;
        }
        try {
            if (locationPermissionGranted) {
                map.setMyLocationEnabled(true);
                getDeviceLocation();
            } else {
                map.setMyLocationEnabled(false);
                Toast.makeText(getApplicationContext(),
                        getString(R.string.location_permission_toast),
                        Toast.LENGTH_LONG).show();
            }
        } catch (SecurityException e) {
            Timber.e(e);
        }
    }

    //Centers the camera on the last known device location
    private void getDeviceLocation() {
        try {
            if (locationPermissionGranted) {
                Task<Location> locationTask = fusedLocationProviderClient.getLastLocation();
                locationTask.addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        lastDeviceLocation = task.getResult();
                        if (lastDeviceLocation != null) {
                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(lastDeviceLocation.getLatitude(),
                                            lastDeviceLocation.getLongitude()),
                                            DEFAULT_ZOOM));
                        }
                    }else {
                        Timber.e(task.getException().toString());
                        map.moveCamera(CameraUpdateFactory
                                .newLatLngZoom(defaultLocation, DEFAULT_ZOOM));
                    }
                });
            }
        } catch (SecurityException e) {
            Timber.e(e);
        }
    }

    private void showNearbyPlaces() {
        if (map == null) {
            return;
        }

        if (locationPermissionGranted) {
            List<Place.Field> placeFields = Arrays.asList(Place.Field.NAME, Place.Field.ADDRESS,
                    Place.Field.LAT_LNG, Place.Field.ID);

            FindCurrentPlaceRequest request = FindCurrentPlaceRequest.newInstance(placeFields);

            @SuppressLint("MissingPermission") final
            Task<FindCurrentPlaceResponse> placeResponseTask =
                    placesClient.findCurrentPlace(request);

            placeResponseTask.addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    FindCurrentPlaceResponse likelyPlaces = task.getResult();
                    int listMaxSize = Math.min(likelyPlaces.getPlaceLikelihoods().size(),
                            PLACE_LIST_MAX_SIZE);
                    int listSize = 0;
                    for (PlaceLikelihood placeLikelihood : likelyPlaces.getPlaceLikelihoods()) {
                        placeList.add(placeLikelihood.getPlace());
                        listSize++;
                        if (listSize > listMaxSize -1) {
                            break;
                        }
                    }
                    NewParkActivity.this.openSelector();
                } else {
                    Timber.e(task.getException().toString());
                }
            });
        } else {
            Timber.i("Location permission not granted.");
            checkLocationPermission();
        }
    }

    private void openSelector() {
        @SuppressLint("MissingPermission")
        DialogInterface.OnClickListener listener = (dialogInterface, i) -> {
            LatLng markerLatLng;
            String markerSnippet;
            String markerTitle;
            if (i == 0) {
                markerLatLng = new LatLng(lastDeviceLocation.getLatitude(),
                        lastDeviceLocation.getLongitude());
                markerTitle = getString(R.string.marker_title_no_place);
                markerSnippet = getString(R.string.marker_snippet_no_place);
            } else {
                Place placeClicked = placeList.get(i-1);
                markerLatLng = placeClicked.getLatLng();
                markerSnippet = placeClicked.getAddress();
                markerTitle = placeClicked.getName();
                parkPlaceId = placeClicked.getId();
                if (placeClicked.getAttributions() != null) {
                    markerSnippet += "\n" + placeClicked.getAttributions();
                }
            }

            map.clear();
            map.setMyLocationEnabled(true);
            map.addMarker(new MarkerOptions()
                    .title(markerTitle)
                    .position(markerLatLng)
                    .snippet(markerSnippet));
            map.moveCamera(CameraUpdateFactory
                    .newLatLngZoom(markerLatLng, DEFAULT_ZOOM));
            EditText addressLocationText = findViewById(R.id.address_location_text);
            String textToDisplay = markerTitle + ",\n" + markerSnippet;
            addressLocationText.setText(textToDisplay);
            parkName = markerTitle;
            parkLocation = markerLatLng;
            parkSnippet = markerSnippet;
            placeList.clear();
        };

        String[] placeNames = new String[placeList.size() + 1];
        placeNames[0] = getString(R.string.use_current_coordinates);
        for (int k = 0; k < placeList.size(); k++) {
            placeNames[k + 1] = placeList.get(k).getName();
        }

        new AlertDialog.Builder(this)
                .setTitle(R.string.pick_place)
                .setItems(placeNames, listener)
                .show();
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        currentCar = (Car) adapterView.getItemAtPosition(i);
        parkedCarId = currentCar.getId();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        currentCar = (Car) adapterView.getItemAtPosition(0);
        //Use the first car on the list as the default selection.
        parkedCarId = currentCar.getId();
    }

    private void savePark() {
        if (parkName == null) {
            Toast.makeText(getApplicationContext(),
                    R.string.pick_location_warning_toast,
                    Toast.LENGTH_LONG)
                    .show();
        } else {
            Date parkTime = Calendar.getInstance().getTime();
            if (timePickerFragment != null) {
                parkValid = timePickerFragment.getParkValid();
            } else {
                parkValid = Calendar.getInstance().getTime();
                long oneHourToMillis = 1000 * 60 * 60;
                parkValid.setTime(parkValid.getTime() + oneHourToMillis);
                Toast.makeText(getApplicationContext(),
                        R.string.parking_valid_default_set,
                        Toast.LENGTH_LONG)
                        .show();
            }
            EditText parkLotView = findViewById(R.id.lot_number);
            String parkLot = parkLotView.getText().toString();
            EditText parkFloorView = findViewById(R.id.floor_number);
            String parkFloor = parkFloorView.getText().toString();
            parkToSave = new Park(parkName, parkFloor, parkLot, parkValid, parkTime,
                    parkPicture, parkedCarId, parkLocation.latitude,
                    parkLocation.longitude, parkSnippet, parkPlaceId);

            AppExecutors.getInstance().diskIO().execute(() -> {
                if (currentParkId == DEFAULT_PARK_ID) {
                    dataBase.parkDao().insertPark(parkToSave);
                } else {
                    parkToSave.setId(currentParkId);
                    dataBase.parkDao().updatePark(parkToSave);
                }

            });

            setParkExpireBroadcast();

            AppExecutors.getInstance().diskIO().execute(() -> {
                currentCar.setParkedState(true);
                currentCar.setParkedTime(parkTime);
                dataBase.carDao().updateCar(currentCar);
                ParkWidgetService.startActionUpdateWidget(this);
                finish();
            });

        }
    }

    private void setParkExpireBroadcast() {
        Toast.makeText(getApplicationContext(),
                R.string.notification_toast, Toast.LENGTH_LONG).show();

        Intent intent = new Intent(NewParkActivity.this, ParkExpireBroadcast.class);
        intent.putExtra(CAR_EXTRA, currentCar.getId());
        intent.putExtra(CAR_EXTRA_BC_REC, currentCar.getCarName());
        PendingIntent pendingIntent = PendingIntent
                .getBroadcast(NewParkActivity.this, 0, intent, 0);

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        long parkValidTime = parkToSave.getValidTime().getTime();
        long fifteenMinutes = 1000 * 60 * 15; //fifteen minutes in milliseconds

        alarmManager
                .set(AlarmManager.RTC_WAKEUP, parkValidTime - fifteenMinutes, pendingIntent);
    }

    private void showTimePicker() {
        timePickerFragment = new TimePickerFragment(parkingValidButton);
        timePickerFragment.show(getSupportFragmentManager(), "timePicker");
    }


    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photoFile;
        try {
            photoFile = createImagePath();
            if (photoFile != null) {
                Uri photoURI = FileProvider
                        .getUriForFile(this,
                                "com.example.android.parkLogProvider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        } catch (IOException | ActivityNotFoundException e) {
            Timber.e(e);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            setPicture(parkPicture);
        }
    }

    private File createImagePath() throws IOException {
        String timeStamp =
                new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss",
                        Locale.getDefault()).format(new Date());
        String uniqueFileName = "JPEG_parkLog" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File tempImageFile = File.createTempFile(uniqueFileName, ".jpg", storageDir);
        parkPicture = tempImageFile.getAbsolutePath();
        return tempImageFile;
    }

    private void enlargePicture(Bitmap bitmap) {
        enlarged_picture.setVisibility(View.VISIBLE);
        enlarged_picture.setImageBitmap(bitmap);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence channel_name = getString(R.string.CHANNEL_NAME_PARK_UPDATES);
            String channel_description = getString(R.string.CHANNEL_DESC_PARK_UPDATES);
            int channel_importance = NotificationManager.IMPORTANCE_DEFAULT;

            NotificationChannel notificationChannel =
                    new NotificationChannel(PARK_UPDATES_CHANNEL_ID,
                            channel_name, channel_importance);
            notificationChannel.setDescription(channel_description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }
}
