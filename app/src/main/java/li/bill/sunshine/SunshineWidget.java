package li.bill.sunshine;

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

import li.bill.sunshine.data.WeatherContract;


/**
 * Implementation of App Widget functionality.
 */
public class SunshineWidget extends AppWidgetProvider {

    private static final String ACTION_CLICK = "ACTION_CLICK";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        final int N = appWidgetIds.length;
        for (int i = 0; i < N; i++) {
            updateAppWidget(context, appWidgetManager, appWidgetIds[i]);
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

    private static final String[] NOTIFY_WEATHER_PROJECTION = new String[]{
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_DATE
    };
    // these indices must match the projection
    private static final int INDEX_WEATHER_ID = 0;
    private static final int INDEX_MAX_TEMP = 1;
    private static final int INDEX_MIN_TEMP = 2;
    private static final int INDEX_SHORT_DESC = 3;
    private static final int INDEX_DATE = 4;

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.sunshine_widget);
        
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        views.setOnClickPendingIntent(R.id.sunshine_widget, pendingIntent);

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
        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }
}

