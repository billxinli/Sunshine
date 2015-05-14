package li.bill.sunshine;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.widget.ImageView;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import li.bill.sunshine.data.WeatherContract;


/**
 * Implementation of App Widget functionality.
 */
public class SunshineWidget extends AppWidgetProvider {
    private final String LOG_TAG = SunshineWidget.class.getSimpleName();
    private static final String ACTION_CLICK = "ACTION_CLICK";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        final int N = appWidgetIds.length;
        for (int i = 0; i < N; i++) {
            RemoteViews views = updateView(context);
            appWidgetManager.updateAppWidget(appWidgetIds[i], views);
        }
    }

    @Override
    public void onDisabled(Context context) {
        Intent intent = new Intent(context, SunshineWidgetBroadcastReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
        super.onDisabled(context);
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, SunshineWidgetBroadcastReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, 0);
        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 1000 * 1, 1000, pi);
    }

    public static RemoteViews updateView(Context context) {
        final String[] NOTIFY_WEATHER_PROJECTION = new String[]{
                WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
                WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
                WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
                WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
                WeatherContract.WeatherEntry.COLUMN_DATE
        };
        // these indices must match the projection
        final int INDEX_WEATHER_ID = 0;
        final int INDEX_MAX_TEMP = 1;
        final int INDEX_MIN_TEMP = 2;
        final int INDEX_SHORT_DESC = 3;
        final int INDEX_DATE = 4;

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.sunshine_widget);
        String locationQuery = Utility.getPreferredLocation(context);

        Uri weatherUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(locationQuery, System.currentTimeMillis());

        // we'll query our contentProvider, as always
        Cursor cursor = context.getContentResolver().query(weatherUri, NOTIFY_WEATHER_PROJECTION, null, null, null);

        if (cursor.moveToFirst()) {

            int weatherId = cursor.getInt(INDEX_WEATHER_ID);
            int iconId = Utility.getArtResourceForWeatherCondition(weatherId);

            // Read date from cursor
            long dateInMillis = cursor.getLong(INDEX_DATE);
            // Find TextView and set formatted date on it
            views.setTextViewText(R.id.list_item_date_textview, Utility.getFriendlyDayString(context, dateInMillis));

            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            Date date = new Date();
            views.setTextViewText(R.id.list_item_date_textview, dateFormat.format(date));

            // Read weather forecast from cursor
            String description = cursor.getString(INDEX_SHORT_DESC);
            // Find TextView and set weather forecast on it
            views.setTextViewText(R.id.list_item_forecast_textview, description);

            // Read user preference for metric or imperial temperature units
            boolean isMetric = Utility.isMetric(context);

            // Read high temperature from cursor
            double high = cursor.getDouble(INDEX_MAX_TEMP);
            views.setTextViewText(R.id.list_item_high_textview, Utility.formatTemperature(context, high));

            // Read low temperature from cursor
            double low = cursor.getDouble(INDEX_MIN_TEMP);
            views.setTextViewText(R.id.list_item_low_textview, Utility.formatTemperature(context, low));

            views.setImageViewResource(R.id.list_item_icon, iconId);
        }
        return views;
    }
}

