package de.davidaugustat.wattpaddlerwidget;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Helper class that handles the storing of location objects in the shared preferences.
 */
public class SharedPreferencesHelper {

    /**
     * Saves a location object to the shared preferences together with an app widget ID.
     * <p>
     * The app widget ID is used as a key so that it can be used to retrieve the location later.
     * The Constants.WIDGET_LOCATION_PREFERENCES shared preferences file gets used to store the
     * preferences.
     *
     * @param location    Location to save
     * @param appWidgetId ID of the widget this location should be associated with
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
     *
     * @param appWidgetId App widget ID that the location should be deleted for.
     */
    public static void deleteLocation(int appWidgetId, Context context) {
        SharedPreferences sharedPrefs = context.getSharedPreferences(
                Constants.WIDGET_LOCATION_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.remove(getLocationNameKey(appWidgetId));
        editor.remove(getLocationIdKey(appWidgetId));
        editor.apply();
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
}
