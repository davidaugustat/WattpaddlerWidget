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

    /**
     * Updates the app widget with the provided ID.
     *
     * @param appWidgetId ID of the widget to update
     * @param isManual true iff the update was triggered by user interaction, i.e. the user clicked
     *                 the refresh button.
     */
    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId, boolean isManual) {
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.main_widget);

        views.setOnClickPendingIntent(R.id.buttonUpdate, getPendingSelfIntent(context, appWidgetId));
        setUpOpenAppOnClick(views, context);
        refreshWidget(views, context, appWidgetManager, appWidgetId, isManual);

        Log.d("Updating widget", "Updating widget with ID " + appWidgetId);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId, false);
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
     * <p>
     * Deletes the locations associated with these widget(s) from the shared preferences as they
     * are not needed anymore.
     *
     * @param appWidgetIds IDs of the deleted app widgets (in most cases only one widget)
     */
    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
        for (int appWidgetId : appWidgetIds) {
            SharedPreferencesHelper.deleteLocation(appWidgetId, context);
        }
    }

    /**
     * Loads the data from the API and displays it on the widget.
     *
     * @param views            RemoteViews representing the widget
     * @param context          Context used to get strings
     * @param appWidgetManager AppWidgetManager used to update the widget
     * @param appWidgetId      ID of the widget to update
     * @param isManual         true iff the widget was manually updated. In this case a info toast
     *                         "Network error" is displayed to inform the user about the error. If
     *                         the widget was automatically updated, no error message is displayed
     *                         but the widget remains unchanged (continues to display old data).
     */
    private static void refreshWidget(RemoteViews views, Context context, AppWidgetManager appWidgetManager, int appWidgetId,
                                      boolean isManual) {
        try {
            Location location = SharedPreferencesHelper.getLocation(appWidgetId, context);
            new DataFetcher(context).fetchTidesDataSingleDay(location, tidesInfo -> {
                        updateWidgetLayout(views, context, appWidgetManager, appWidgetId, tidesInfo);
                        Log.d("Tides Info", tidesInfo.toString());
                        if (isManual) {
                            Toast.makeText(context, context.getString(R.string.widget_updated_toast_text), Toast.LENGTH_SHORT).show();
                        }
                    },
                    errorMessage -> {
                        updateWidgetLayoutAtError(errorMessage, views, appWidgetManager, appWidgetId);
                        Log.e("Error fetching tides", errorMessage);
                        if (isManual) {
                            Toast.makeText(context, context.getString(R.string.error_updating_toast_text), Toast.LENGTH_SHORT).show();
                        }
                    });
        } catch (IllegalArgumentException exception) {
            Log.d("No location", "No location stored for widget ID" + appWidgetId);
            updateWidgetLayoutAtError(exception.toString(), views, appWidgetManager, appWidgetId);
        }
    }

    /**
     * Updates the widget but does not apply any changes. This is just to make sure that all changes
     * that were made in the {@link #updateAppWidget(Context, AppWidgetManager, int, boolean)} are
     * committed.
     *
     * <p>This is especially important to ensure that the refresh button gets set up even in case that
     * a network error occurs at the very first update of the widget. In this case the user should
     * still be able to perform a refresh.
     *
     * <p>Additionally, if {@link Constants#SHOW_DEBUG} is true, the error message gets printed to
     * the debug text view.
     *
     * @param errorString      String for the error message
     * @param views            RemoteViews representing the widget
     * @param appWidgetManager AppWidgetManager used to update the widget.
     * @param appWidgetId      ID of the widget.
     */
    private static void updateWidgetLayoutAtError(String errorString, RemoteViews views,
                                                  AppWidgetManager appWidgetManager, int appWidgetId) {
        if (Constants.SHOW_DEBUG) {
            views.setViewVisibility(R.id.textViewDebug, View.VISIBLE);
            views.setTextViewText(R.id.textViewDebug, errorString);
        }
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

        setMainLayoutVisible(views);
        views.setTextViewText(R.id.textViewLocation, tidesInfo.getLocationName());
        views.setTextViewText(R.id.textViewDate, tidesInfo.getDateFormatted());
        views.setTextViewText(R.id.textViewHighTide, getHighTidesText(context, tidesInfo));
        views.setTextViewText(R.id.textViewLowTide, getLowTidesText(context, tidesInfo));

        // Show last updated text only for debug purposes:
        if (Constants.SHOW_DEBUG) {
            String lastUpdatedText = String.format(context.getString(R.string.last_updated_text),
                    tidesInfo.getLastUpdatedTimeFormatted());
            views.setTextViewText(R.id.textViewDebug, lastUpdatedText);
            views.setViewVisibility(R.id.textViewDebug, View.VISIBLE);
        }

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    /**
     * Makes all layout elements that belong to the main layout visible and hides the status text
     * view (which is displayed by default before the first update).
     */
    private static void setMainLayoutVisible(RemoteViews views) {
        views.setViewVisibility(R.id.widgetRow1Layout, View.VISIBLE);
        views.setViewVisibility(R.id.widgetRow2Layout, View.VISIBLE);
        views.setViewVisibility(R.id.textViewLowTide, View.VISIBLE);
        views.setViewVisibility(R.id.textViewStatus, View.GONE);
    }

    /**
     * Generates string for low tides that can be displayed on the widget.
     */
    @NonNull
    private static String getLowTidesText(Context context, TidesInfo tidesInfo) {
        return String.format(
                context.getString(R.string.low_tides_text),
                tidesInfo.getLowTide1Formatted(),
                tidesInfo.getLowTide2Formatted()
        );
    }

    /**
     * Generates string for high tides that can be displayed on the widget.
     */
    @NonNull
    private static String getHighTidesText(Context context, TidesInfo tidesInfo) {
        return String.format(
                context.getString(R.string.high_tides_text),
                tidesInfo.getHighTide1Formatted(),
                tidesInfo.getHighTide2Formatted()
        );
    }

    /**
     * Called when the underlying broadcast receiver of this app widget provider receives an intent.
     * <p>
     * This method is used to receive intents that are sent when the refresh button of the widget
     * is clicked. In this case the widget gets updated.
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        if (intent.getAction().equals(Constants.WIDGET_REFRESH_BUTTON_ACTION)) {
            int appWidgetId = intent.getIntExtra(Constants.APP_WIDGET_ID_EXTRA, Constants.INVALID_APP_WIDGET_ID);
            if (appWidgetId == Constants.INVALID_APP_WIDGET_ID) {
                Log.e("OnReceive", "App widget ID was not passed with intent.");
                return;
            }
            Log.d("Widget onReceive", "Refresh button clicked for widget with ID " + appWidgetId);
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            updateAppWidget(context, appWidgetManager, appWidgetId, true);
        }
    }

    /**
     * Returns a PendingIntent that points to this AppWidgetReceiver. When the PendingIntent
     * is executed, the onReceive() method of this class gets called. This is used to do something
     * when the refresh button of the widget gets pressed.
     * <p>
     * The pending intent includes the Constants.WIDGET_REFRESH_BUTTON_ACTION as well as the app
     * widget ID of the widget on which the button was pressed.
     *
     * @param appWidgetId ID of the app widget that should be updated when the refresh button is
     *                    pressed.
     */
    private static PendingIntent getPendingSelfIntent(Context context, int appWidgetId) {
        Intent intent = new Intent(context, MainWidget.class);
        intent.setAction(Constants.WIDGET_REFRESH_BUTTON_ACTION);
        intent.putExtra(Constants.APP_WIDGET_ID_EXTRA, appWidgetId);
        return PendingIntent.getBroadcast(context, appWidgetId, intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * If the Wattpaddler app (com.embarcadero.Wattpaddler) is installed on the device,
     * the main layout of the widget gets a click listener that opens this app when clicked.
     */
    private static void setUpOpenAppOnClick(RemoteViews views, Context context) {
        if (AppPackageDetectionHelper.isWattpaddlerAppInstalled(context)) {
            Intent intent = context.getPackageManager()
                    .getLaunchIntentForPackage(Constants.WATTPADDLER_APP_PACKAGE_NAME);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            views.setOnClickPendingIntent(R.id.widgetMainLayout, pendingIntent);
        } else {
            Log.d("App not installed", "Wattpaddler app not is installed on the device. " +
                    "Not setting up a click listener for it.");
        }
    }


}