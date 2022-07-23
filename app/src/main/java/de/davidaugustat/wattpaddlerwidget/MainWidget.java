package de.davidaugustat.wattpaddlerwidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

/**
 * Implementation of App Widget functionality.
 */
public class MainWidget extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.main_widget);

        views.setOnClickPendingIntent(R.id.buttonUpdate, getPendingSelfIntent(context, appWidgetId));
        refreshWidget(views, context, appWidgetManager, appWidgetId, isManualOrInitialRefresh());

        Log.d("Updating widget", "Updating widget with ID " + appWidgetId);
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
                            //updateWidgetLayoutAtError(R.string.no_network_text,views, context, appWidgetManager, appWidgetId);
                            updateWidgetLayoutAtError(errorMessage, views, appWidgetManager, appWidgetId);
                        }
                        Log.e("Error fetching tides", errorMessage);
                    });
        } catch (IllegalArgumentException exception){
            Log.d("No location", "No location stored for widget ID" + appWidgetId);
            updateWidgetLayoutAtError(R.string.not_set_up_text, views, context, appWidgetManager, appWidgetId);
        }
    }

    /**
     * Displays an message on the widget. This can be used to inform the user about
     * an error that ocurred during update.
     *
     * @param errorString String for the error message
     * @param views RemoteViews representing the widget
     * @param appWidgetManager AppWidgerManager used to update the widget.
     * @param appWidgetId ID of the widget.
     */
    private static void updateWidgetLayoutAtError(String errorString, RemoteViews views,
                                                  AppWidgetManager appWidgetManager, int appWidgetId) {
        views.setTextViewText(R.id.textViewLocation, errorString);
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    /**
     * Displays an message on the widget. This can be used to inform the user about
     * an error that ocurred during update.
     *
     * @param errorStringId String for the error message
     * @param views RemoteViews representing the widget
     * @param context Context used to get strings
     * @param appWidgetManager AppWidgerManager used to update the widget.
     * @param appWidgetId ID of the widget.
     */
    private static void updateWidgetLayoutAtError(@StringRes int errorStringId, RemoteViews views, Context context,
                                                  AppWidgetManager appWidgetManager, int appWidgetId) {
        updateWidgetLayoutAtError(context.getString(errorStringId), views, appWidgetManager, appWidgetId);
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
        views.setTextViewText(R.id.textViewHighTide, getHighTidesText(context, tidesInfo));
        views.setTextViewText(R.id.textViewLowTide, getLowTidesText(context, tidesInfo));

        // Show last updated text only for debug purposes:
        if(Constants.SHOW_LAST_UPDATED) {
            String lastUpdatedText = String.format(context.getString(R.string.last_updated_text),
                    tidesInfo.getLastUpdatedTimeFormatted());
            views.setTextViewText(R.id.textViewLastUpdated, lastUpdatedText);
            views.setViewVisibility(R.id.textViewLastUpdated, View.VISIBLE);
        }

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    /**
     * Generates string for low tides that can be displayed on the widget.
     */
    @NonNull
    private static String getLowTidesText(Context context, TidesInfo tidesInfo) {
        String lowTideText = "";
        switch (tidesInfo.getNumberOfLowTides()){
            case 2:
                lowTideText = String.format(context.getString(R.string.low_tides_text),
                        tidesInfo.getLowTide1Formatted(), tidesInfo.getLowTide2Formatted());
                break;
            case 1:
                lowTideText = String.format(context.getString(R.string.low_tides_text),
                        tidesInfo.getLowTide1Formatted(), "*");
                break;
            case 0:
                lowTideText = String.format(context.getString(R.string.low_tides_text), "*", "*");
                break;
        }
        return lowTideText;
    }

    /**
     * Generates string for high tides that can be displayed on the widget.
     */
    @NonNull
    private static String getHighTidesText(Context context, TidesInfo tidesInfo) {
        String highTideText = "";
        switch (tidesInfo.getNumberOfHighTides()){
            case 2:
                highTideText = String.format(context.getString(R.string.high_tides_text),
                    tidesInfo.getHighTide1Formatted(), tidesInfo.getHighTide2Formatted());
                break;
            case 1:
                highTideText = String.format(context.getString(R.string.high_tides_text),
                        tidesInfo.getHighTide1Formatted(), "*");
                break;
            case 0:
                highTideText = String.format(context.getString(R.string.high_tides_text), "*", "*");
                break;
        }
        return highTideText;
    }

    /**
     * Called when the underlying broadcast receiver of this app widget provider receives an intent.
     *
     * This method is used to receive intents that are sent when the refresh button of the widget
     * is clicked. In this case the widget gets updated.
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        if(intent.getAction().equals(Constants.WIDGET_REFRESH_BUTTON_ACTION)){
            int appWidgetId = intent.getIntExtra(Constants.APP_WIDGET_ID_EXTRA, Constants.INVALID_APP_WIDGET_ID);
            if(appWidgetId == Constants.INVALID_APP_WIDGET_ID){
                Log.e("OnReceive", "App widget ID was not passed with intent.");
                return;
            }
            Log.d("Widget onReceive", "Refresh button clicked for widget with ID " + appWidgetId);
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    /**
     * Returns a PendingIntent that points to this AppWidgetReceiver. When the PendingIntent
     * is executed, the onReceive() method of this class gets called. This is used to do something
     * when the refresh button of the widget gets pressed.
     *
     * The pending intent includes the Constants.WIDGET_REFRESH_BUTTON_ACTION as well as the app
     * widget ID of the widget on which the button was pressed.
     * @param appWidgetId ID of the app widget that should be updated when the refresh button is
     *                    pressed.
     */
    private static PendingIntent getPendingSelfIntent(Context context, int appWidgetId) {
        Intent intent = new Intent(context, MainWidget.class);
        intent.setAction(Constants.WIDGET_REFRESH_BUTTON_ACTION);
        intent.putExtra(Constants.APP_WIDGET_ID_EXTRA, appWidgetId);
        return PendingIntent.getBroadcast(context, appWidgetId, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * Returns true iff the widget is either updated for the first time or after a manual refresh.
     */
    private static boolean isManualOrInitialRefresh(){
        // TODO: Implement this properly.
        return true;
    }
}