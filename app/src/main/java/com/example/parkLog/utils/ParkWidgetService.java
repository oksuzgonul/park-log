package com.example.parkLog.utils;

import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import androidx.annotation.Nullable;

import com.example.parkLog.data.AppDataBase;
import com.example.parkLog.data.Car;

public class ParkWidgetService extends IntentService {

    public static final String PARK_UPDATE_WIDGET = "park_update_widget";

    public ParkWidgetService() {
        super("ParkService");
    }

    public static void startActionUpdateWidget(Context context) {
        Intent intent = new Intent(context, ParkWidgetService.class);
        intent.setAction(PARK_UPDATE_WIDGET);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (PARK_UPDATE_WIDGET.equals(action)) {
                handleActionUpdateWidget();
            }
        }
    }
    private void handleActionUpdateWidget() {
        new AsyncTask<Context, Void, Car>() {
            @Override
            protected Car doInBackground(Context... contexts) {
                AppDataBase dataBase = AppDataBase.getInstance(contexts[0]);
                return dataBase.carDao().getLastParkedCar();
            }

            @Override
            protected void onPostExecute(Car car) {
                super.onPostExecute(car);
                if (car != null) {
                    AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getBaseContext());
                    int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(getBaseContext(), LastParkedCarWidget.class));
                    LastParkedCarWidget.updateAppWidgets(getBaseContext(), appWidgetManager, appWidgetIds, car);
                }
            }
        }.execute(this);


    }
}
