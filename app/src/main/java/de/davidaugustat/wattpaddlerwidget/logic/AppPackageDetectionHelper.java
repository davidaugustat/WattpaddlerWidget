package de.davidaugustat.wattpaddlerwidget.logic;

import android.content.Context;
import android.content.pm.PackageManager;

import de.davidaugustat.wattpaddlerwidget.Constants;

public class AppPackageDetectionHelper {
    /**
     * Checks if the Wattpaddler app (com.embarcadero.Wattpaddler) is installed on the device.
     */
    public static boolean isWattpaddlerAppInstalled(Context context){
        try {
            context.getPackageManager().getPackageInfo(Constants.WATTPADDLER_APP_PACKAGE_NAME, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
}
