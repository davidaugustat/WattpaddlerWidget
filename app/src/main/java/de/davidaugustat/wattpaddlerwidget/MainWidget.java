package de.davidaugustat.wattpaddlerwidget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.StringRes;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Implementation of App Widget functionality.
 */
public class MainWidget extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.main_widget);

        refreshWidget(views, context, appWidgetManager, appWidgetId, isManualOrInitialRefresh());
        Log.d("Updating widget", "Updating widget");
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
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

    /**
     * Gets called when one or more app widgets get deleted.
     *
     * Deletes the locations associated with these widget(s) from the shared preferences as they
     * are not needed anymore.
     *
     * @param appWidgetIds IDs of the deleted app widgets (in most cases only one widget)
     */
    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
        for(int appWidgetId : appWidgetIds){
            SharedPreferencesHelper.deleteLocation(appWidgetId, context);
        }
    }

    /**
     * Loads the data from the API and displays it on the widget.
     *
     * @param views RemoteViews representing the widget
     * @param context Context used to get strings
     * @param appWidgetManager AppWidgetManager used to update the widget
     * @param appWidgetId ID of the widget to update
     * @param isManualOrInitialUpdate true iff the widget was manually or initially updated. In this
     *                                case a info message "Network error" is displayed on the widget
     *                                to inform the user about the error. If the widget was
     *                                automatically updated, no error message is displayed but the
     *                                widget remains unchanged (continues to display old data).
     */
    private static void refreshWidget(RemoteViews views, Context context, AppWidgetManager appWidgetManager, int appWidgetId,
                                      boolean isManualOrInitialUpdate) {
        try {
            Location location = SharedPreferencesHelper.getLocation(appWidgetId, context);
            new DataFetcher(context).fetchTidesDataSingleDay(location, tidesInfo -> {
                        updateWidgetLayout(views, context, appWidgetManager, appWidgetId, tidesInfo);
                        Log.d("Tides Info", tidesInfo.toString());
                    },
                    errorMessage -> {
                        if (isManualOrInitialUpdate) {
                            updateWidgetLayoutAtError(R.string.no_network_text,views, context, appWidgetManager, appWidgetId);
                        }
                        Log.e("Error fetching tides", errorMessage);
                    });
        } catch (IllegalArgumentException exception){
            Log.d("No location", "No location stored for widget ID" + appWidgetId);
            updateWidgetLayoutAtError(R.string.not_set_up_text, views, context, appWidgetManager, appWidgetId);
        }
    }

    /**
     * Displays a "Network error" message on the widget. This can be used to inform the user about
     * an error that ocurred during update.
     *
     * @param views RemoteViews representing the widget
     * @param context Context used to get strings
     * @param appWidgetManager AppWidgerManager used to update the widget.
     * @param appWidgetId ID of the widget.
     */
    private static void updateWidgetLayoutAtError(@StringRes int errorStringId, RemoteViews views, Context context,
                                                  AppWidgetManager appWidgetManager, int appWidgetId) {
        views.setTextViewText(R.id.textViewLocation, context.getString(errorStringId));
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    /**
     * Displays the new tides data from the tidesInfo parameter on the widget.
     *
     * @param tidesInfo Tides info that should be displayed on the widget.
     */
    private static void updateWidgetLayout(RemoteViews views, Context context,
                                           AppWidgetManager appWidgetManager, int appWidgetId,
                                           TidesInfo tidesInfo) {
        views.setTextViewText(R.id.textViewLocation, tidesInfo.getLocationName());
        views.setTextViewText(R.id.textViewDate, tidesInfo.getDateFormatted());

        String highTideText;
        if (tidesInfo.getHighTide2() == null) {
            highTideText = String.format(context.getString(R.string.high_tide_1_value_text),
                    tidesInfo.getHighTide1());
        } else {
            highTideText = String.format(context.getString(R.string.high_tide_2_values_text),
                    tidesInfo.getHighTide1(), tidesInfo.getHighTide2());
        }
        views.setTextViewText(R.id.textViewHighTide, highTideText);

        String lowTideText;
        if (tidesInfo.getLowTide2() == null) {
            lowTideText = String.format(context.getString(R.string.low_tide_1_value_text),
                    tidesInfo.getLowTide1());
        } else {
            lowTideText = String.format(context.getString(R.string.low_tide_2_values_text),
                    tidesInfo.getLowTide1(), tidesInfo.getLowTide2());
        }
        views.setTextViewText(R.id.textViewLowTide, lowTideText);

        String lastUpdatedText = String.format(context.getString(R.string.last_updated_text),
                tidesInfo.getLastUpdatedTimeFormatted());
        views.setTextViewText(R.id.textViewLastUpdated, lastUpdatedText);

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    /**
     * Returns true iff the widget is either updated for the first time or after a manual refresh.
     */
    private static boolean isManualOrInitialRefresh(){
        // TODO: Implement this properly.
        return true;
    }

    private static TidesInfo getDummyTidesInfo() {
        return new TidesInfo(
                "123214",
                "Bremerhaven / Alter Leuchtturm",
                LocalDate.now(),
                "10:00",
                "22:30",
                "16:00",
                "4:30",
                LocalDateTime.now()
        );
    }
}