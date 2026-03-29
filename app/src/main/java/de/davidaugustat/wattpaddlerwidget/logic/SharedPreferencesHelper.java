package de.davidaugustat.wattpaddlerwidget.logic;

import android.content.Context;
import android.content.SharedPreferences;

import java.time.LocalDate;
import java.time.LocalDateTime;

import de.davidaugustat.wattpaddlerwidget.Constants;
import de.davidaugustat.wattpaddlerwidget.data.Location;
import de.davidaugustat.wattpaddlerwidget.data.NonExistentTideTime;
import de.davidaugustat.wattpaddlerwidget.data.NormalTideTime;
import de.davidaugustat.wattpaddlerwidget.data.ShiftedTideTime;
import de.davidaugustat.wattpaddlerwidget.data.TideTime;
import de.davidaugustat.wattpaddlerwidget.data.TidesInfo;

/**
 * Helper class that handles the storing of location objects and tide data in the shared
 * preferences.
 */
public class SharedPreferencesHelper {

    private static final String CACHE_PREFS = "WIDGET_CACHE";
    private static final String KEY_LOC_ID = "loc_id_";
    private static final String KEY_LOC_NAME = "loc_name_";
    private static final String KEY_DATE = "date_";
    private static final String KEY_HIGH_TIDE_1 = "high_tide_1_";
    private static final String KEY_HIGH_TIDE_2 = "high_tide_2_";
    private static final String KEY_LOW_TIDE_1 = "low_tide_1_";
    private static final String KEY_LOW_TIDE_2 = "low_tide_2_";
    private static final String KEY_LAST_UPDATED = "last_updated_";

    private static final String TIDE_TYPE_NORMAL = "NORMAL";
    private static final String TIDE_TYPE_SHIFTED = "SHIFTED";
    private static final String TIDE_TYPE_NON_EXISTENT = "NON_EXISTENT";

    /**
     * Saves a location object to the shared preferences together with an app widget ID.
     * <p>
     * The app widget ID is used as a key so that it can be used to retrieve the location later.
     * The Constants.WIDGET_LOCATION_PREFERENCES shared preferences file gets used to store the
     * preferences.
     *
     * @param location    Location to save
     * @param appWidgetId ID of the widget this location should be associated with
     * @param context     Context to access SharedPreferences
     */
    public static void saveLocation(Location location, int appWidgetId, Context context) {
        SharedPreferences sharedPrefs = context.getSharedPreferences(
                Constants.WIDGET_LOCATION_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString(getLocationNameKey(appWidgetId), location.getName());
        editor.putString(getLocationIdKey(appWidgetId), location.getId());
        editor.apply();
    }

    /**
     * Retrieves the location object that was stored together with the provided app widget ID from
     * the shared preferences.
     *
     * @param appWidgetId ID of the app widget that the location should be retrieved for.
     * @param context     Context to access SharedPreferences
     * @return Location object retrieved from shared preferences
     * @throws IllegalArgumentException When there is no location stored for this app widget ID.
     */
    public static Location getLocation(int appWidgetId, Context context) {
        SharedPreferences sharedPrefs = context.getSharedPreferences(
                Constants.WIDGET_LOCATION_PREFERENCES, Context.MODE_PRIVATE);

        String locationName = sharedPrefs.getString(getLocationNameKey(appWidgetId), null);
        String locationId = sharedPrefs.getString(getLocationIdKey(appWidgetId), null);

        if (locationName == null || locationId == null) {
            throw new IllegalArgumentException("No settings stored for appWidgetId " + appWidgetId);
        }

        return new Location(locationId, locationName);
    }

    /**
     * Deletes the location associated with the given app widget ID from the shared preferences.
     * Also clears the tides cache for this widget.
     *
     * @param appWidgetId App widget ID that the location should be deleted for.
     * @param context     Context to access SharedPreferences
     */
    public static void deleteLocation(int appWidgetId, Context context) {
        SharedPreferences sharedPrefs = context.getSharedPreferences(
                Constants.WIDGET_LOCATION_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.remove(getLocationNameKey(appWidgetId));
        editor.remove(getLocationIdKey(appWidgetId));
        editor.apply();

        clearTidesCache(appWidgetId, context);
    }

    /**
     * Returns the key for the location name of the given app widget ID that is used for the shared
     * preferences.
     */
    private static String getLocationNameKey(int appWidgetId) {
        return Constants.LOCATION_NAME_KEY_PREFIX + appWidgetId;
    }

    /**
     * Returns the key for the location ID of the given app widget ID that is used for the shared
     * preferences.
     */
    private static String getLocationIdKey(int appWidgetId) {
        return Constants.LOCATION_ID_KEY_PREFIX + appWidgetId;
    }

    /**
     * Saves the provided TidesInfo object to the cache for a specific app widget.
     *
     * @param appWidgetId ID of the widget
     * @param context     Context to access SharedPreferences
     * @param tidesInfo   The TidesInfo object to cache
     */
    public static void saveTidesCache(int appWidgetId, Context context, TidesInfo tidesInfo) {
        SharedPreferences prefs = context.getSharedPreferences(CACHE_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putString(KEY_LOC_ID + appWidgetId, tidesInfo.getLocationId());
        editor.putString(KEY_LOC_NAME + appWidgetId, tidesInfo.getLocationName());
        editor.putString(KEY_DATE + appWidgetId, tidesInfo.getDate().toString());
        editor.putString(KEY_LAST_UPDATED + appWidgetId, tidesInfo.getUpdatedTime().toString());

        saveTideTime(editor, KEY_HIGH_TIDE_1 + appWidgetId, tidesInfo.getHighTide1());
        saveTideTime(editor, KEY_HIGH_TIDE_2 + appWidgetId, tidesInfo.getHighTide2());
        saveTideTime(editor, KEY_LOW_TIDE_1 + appWidgetId, tidesInfo.getLowTide1());
        saveTideTime(editor, KEY_LOW_TIDE_2 + appWidgetId, tidesInfo.getLowTide2());

        editor.apply();
    }

    /**
     * Retrieves the cached TidesInfo for a specific app widget.
     *
     * @param appWidgetId ID of the widget
     * @param context     Context to access SharedPreferences
     * @return The cached TidesInfo object, or null if no cache exists.
     */
    public static TidesInfo getTidesCache(int appWidgetId, Context context) {
        SharedPreferences prefs = context.getSharedPreferences(CACHE_PREFS, Context.MODE_PRIVATE);
        String locId = prefs.getString(KEY_LOC_ID + appWidgetId, null);
        if (locId == null){
            return null;
        }

        String locName = prefs.getString(KEY_LOC_NAME + appWidgetId, "");
        LocalDate date = LocalDate.parse(prefs.getString(KEY_DATE + appWidgetId, ""));
        LocalDateTime updatedTime = LocalDateTime.parse(
                prefs.getString(KEY_LAST_UPDATED + appWidgetId, ""));

        TideTime highTide1 = loadTideTime(prefs, KEY_HIGH_TIDE_1 + appWidgetId);
        TideTime highTide2 = loadTideTime(prefs, KEY_HIGH_TIDE_2 + appWidgetId);
        TideTime lowTide1 = loadTideTime(prefs, KEY_LOW_TIDE_1 + appWidgetId);
        TideTime lowTide2 = loadTideTime(prefs, KEY_LOW_TIDE_2 + appWidgetId);

        return new TidesInfo(locId, locName, date, lowTide1, lowTide2, highTide1, highTide2, updatedTime);
    }

    /**
     * Clears the cached tide data for a specific app widget.
     */
    private static void clearTidesCache(int appWidgetId, Context context) {
        SharedPreferences prefs = context.getSharedPreferences(CACHE_PREFS, Context.MODE_PRIVATE);
        prefs.edit()
                .remove(KEY_LOC_ID + appWidgetId)
                .remove(KEY_LOC_NAME + appWidgetId)
                .remove(KEY_DATE + appWidgetId)
                .remove(KEY_HIGH_TIDE_1 + appWidgetId)
                .remove(KEY_HIGH_TIDE_1 + appWidgetId + "_type")
                .remove(KEY_HIGH_TIDE_2 + appWidgetId)
                .remove(KEY_HIGH_TIDE_2 + appWidgetId + "_type")
                .remove(KEY_LOW_TIDE_1 + appWidgetId)
                .remove(KEY_LOW_TIDE_1 + appWidgetId + "_type")
                .remove(KEY_LOW_TIDE_2 + appWidgetId)
                .remove(KEY_LOW_TIDE_2 + appWidgetId + "_type")
                .remove(KEY_LAST_UPDATED + appWidgetId)
                .apply();
    }

    private static void saveTideTime(SharedPreferences.Editor editor, String key, TideTime tideTime) {
        if (tideTime instanceof NormalTideTime) {
            editor.putString(key + "_type", TIDE_TYPE_NORMAL);
            editor.putString(key, ((NormalTideTime) tideTime).getDateTime().toString());
        } else if (tideTime instanceof ShiftedTideTime) {
            editor.putString(key + "_type", TIDE_TYPE_SHIFTED);
        } else if (tideTime instanceof NonExistentTideTime) {
            editor.putString(key + "_type", TIDE_TYPE_NON_EXISTENT);
        }
    }

    private static TideTime loadTideTime(SharedPreferences prefs, String key) {
        String type = prefs.getString(key + "_type", "");
        switch (type) {
            case TIDE_TYPE_NORMAL:
                return new NormalTideTime(LocalDateTime.parse(prefs.getString(key, "")));
            case TIDE_TYPE_SHIFTED:
                return new ShiftedTideTime();
            case TIDE_TYPE_NON_EXISTENT:
            default:
                return new NonExistentTideTime();
        }
    }
}
