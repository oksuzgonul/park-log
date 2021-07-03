package com.example.parkLog.utils;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.RemoteViews;

import com.example.parkLog.R;
import com.example.parkLog.data.Car;
import com.example.parkLog.ui.CarDetailsActivity;
import com.example.parkLog.ui.MainActivity;

import static com.example.parkLog.ui.CarDetailsActivity.CAR_EXTRA;

/**
 * Implementation of App Widget functionality.
 */
public class LastParkedCarWidget extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId, Car car) {
        if (car == null) return;

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.last_parked_car_widget);
        CharSequence widgetText = context.getString(R.string.widget_last_parked) + " " + car.getCarName();

        views.setTextViewText(R.id.appwidget_text, widgetText);
        views.setViewVisibility(R.id.widget_get_car_button, View.VISIBLE);

        Intent intent = new Intent(context, CarDetailsActivity.class);
        intent.putExtra(CAR_EXTRA, car.getId());
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        views.setOnClickPendingIntent(R.id.widget_get_car_button, pendingIntent);

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        ParkWidgetService.startActionUpdateWidget(context);
    }

    public static void updateAppWidgets(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds, Car car) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId, car);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}