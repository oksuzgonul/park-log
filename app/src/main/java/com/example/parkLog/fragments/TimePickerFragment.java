package com.example.parkLog.fragments;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.parkLog.R;

import java.util.Calendar;
import java.util.Date;

public class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {
    private Date ParkValid;
    private final EditText ParkingValidText;
    private boolean parkTomorrow = false;

    public TimePickerFragment(EditText parkingValidText) {
        ParkingValidText = parkingValidText;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // Use the current time as the default values for the picker
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        // Create a new instance of TimePickerDialog and return it
        return new TimePickerDialog(getActivity(), this, hour, minute,
                DateFormat.is24HourFormat(getActivity()));
    }

    @Override
    public void onTimeSet(TimePicker timePicker, int hour, int minute) {
        Date currentTime = Calendar.getInstance().getTime();
        Calendar parkValidCalendar = Calendar.getInstance();
        parkValidCalendar.set(Calendar.HOUR_OF_DAY, hour);
        parkValidCalendar.set(Calendar.MINUTE, minute);
        if (currentTime.after(parkValidCalendar.getTime())) {
            //Adding 1 day to the date in case the parking is done
            // late night and the validation is until next morning
            parkValidCalendar.add(Calendar.DATE, 1);
            parkTomorrow = true;
        }
        setParkingValidText(hour, minute);
        ParkValid = parkValidCalendar.getTime();
    }
    public Date getParkValid() { return ParkValid; }
    private void setParkingValidText(int hour, int minute) {
        String separator;
        if (minute < 10) {
            separator = ":0";
        } else {
            separator = ":";
        }
        String timeText = hour + separator + minute;
        ParkingValidText.setText(timeText);
        if (parkTomorrow) {
            Toast.makeText(
                    getActivity().getApplicationContext(),
                    getString(R.string.next_day_parking_warning) + timeText,
                    Toast.LENGTH_LONG).show();
        }
    }
}
