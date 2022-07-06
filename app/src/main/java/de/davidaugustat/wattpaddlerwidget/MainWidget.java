package de.davidaugustat.wattpaddlerwidget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.widget.RemoteViews;

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

        updateWidgetContents(views, context);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
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

    private static void updateWidgetContents(RemoteViews views, Context context){
        TidesInfo tidesInfo = getDummyTidesInfo();
        views.setTextViewText(R.id.textViewLocation, tidesInfo.getLocationName());
        views.setTextViewText(R.id.textViewDate, tidesInfo.getDateFormatted());

        String highTideText = String.format(context.getString(R.string.high_tide_text),
                tidesInfo.getHighTide1(), tidesInfo.getHighTide2());
        views.setTextViewText(R.id.textViewHighTide, highTideText);

        String lowTideText = String.format(context.getString(R.string.low_tide_text),
                tidesInfo.getLowTide1(), tidesInfo.getLowTide2());
        views.setTextViewText(R.id.textViewLowTide, lowTideText);

        String lastUpdatedText = String.format(context.getString(R.string.last_updated_text),
                tidesInfo.getLastUpdatedTimeFormatted());
        views.setTextViewText(R.id.textViewLastUpdated, lastUpdatedText);
    }

    private static TidesInfo getDummyTidesInfo(){
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