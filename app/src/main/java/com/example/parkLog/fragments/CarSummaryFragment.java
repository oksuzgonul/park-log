package com.example.parkLog.fragments;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;

import com.example.parkLog.R;
import com.example.parkLog.data.Car;
import com.example.parkLog.data.Park;
import com.example.parkLog.viewModels.CarDetailsViewModel;

public class CarSummaryFragment extends Fragment {
    private Park park;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View carSummaryView = inflater.inflate(R.layout.park_summary_fragment, container, false);
        bindParkInFragment(park, carSummaryView);
        return carSummaryView;
    }

    public void bindParkInFragment(Park park, View view) {
        if (park != null && view != null) {
            TextView currentParking = view.findViewById(R.id.current_parking_title);
            currentParking.setText(park.getParkLocationName());
            TextView parkingAddress = view.findViewById(R.id.parking_address);
            parkingAddress.setText(park.getParkSnippet());
            Button getCarNowButton = view.findViewById(R.id.get_car_button);
            getCarNowButton.setOnClickListener(view1 -> {
                String stringToParse = "https://www.google.com/maps/search/?api=1&query=" + park.getParkLocationName() + "&query_place_id=" + park.getParkPlaceId();
                Uri mapsIntentUri = Uri.parse(stringToParse);
                Intent intent = new Intent(Intent.ACTION_VIEW, mapsIntentUri);
                intent.setPackage("com.google.android.apps.maps");
                startActivity(intent);
            });
        }
    }

    public void setCurrentPark(Park park) { this.park = park; }
}
