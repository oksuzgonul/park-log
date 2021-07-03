package com.example.parkLog.utils;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.parkLog.R;
import com.example.parkLog.ui.CarDetailsActivity;

import static com.example.parkLog.ui.CarDetailsActivity.CAR_EXTRA;
import static com.example.parkLog.ui.NewCarActivity.DEFAULT_CAR_ID;
import static com.example.parkLog.ui.NewParkActivity.PARK_UPDATES_CHANNEL_ID;
import static com.example.parkLog.ui.NewParkActivity.PARK_UPDATES_NOTIFICATION_ID;

public class ParkExpireBroadcast extends BroadcastReceiver {
    public static final String CAR_EXTRA_BC_REC = "bc_car_extra";

    @Override
    public void onReceive(Context context, Intent intent) {
        String carParked = intent.getStringExtra(CAR_EXTRA_BC_REC);
        int carId = intent.getIntExtra(CAR_EXTRA, DEFAULT_CAR_ID);
        Intent carIntent = new Intent(context, CarDetailsActivity.class);
        carIntent.putExtra(CAR_EXTRA, carId);
        carIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent
                .getActivity(context, 0, carIntent, 0);

        String notificationText = context.getString(R.string.notification_text) + " " + carParked;

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, PARK_UPDATES_CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_car )
                        .setContentTitle(context.getString(R.string.notification_title))
                        .setContentText(notificationText)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(PARK_UPDATES_NOTIFICATION_ID, builder.build());
    }
}
