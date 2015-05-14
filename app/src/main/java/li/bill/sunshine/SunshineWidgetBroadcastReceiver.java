package li.bill.sunshine;

import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.widget.RemoteViews;

public class SunshineWidgetBroadcastReceiver extends BroadcastReceiver {
    private final String LOG_TAG = SunshineWidgetBroadcastReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, LOG_TAG);
        //Acquire the lock
        wakeLock.acquire();

        RemoteViews views = SunshineWidget.updateView(context);
        ComponentName widget = new ComponentName(context, SunshineWidget.class);
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        manager.updateAppWidget(widget, views);

        //Release the lock
        wakeLock.release();
    }
}