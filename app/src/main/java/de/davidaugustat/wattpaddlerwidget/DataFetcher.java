package de.davidaugustat.wattpaddlerwidget;

import android.content.Context;

import androidx.core.util.Consumer;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class fetches data from the HTTP API and converts it to object oriented datasets.
 */
public class DataFetcher {

    private final Context context;

    public DataFetcher(Context context) {
        this.context = context;
    }

    /**
     * Fetches a list of all available locations and provides them as a list of Location objects.
     *
     * @param dataFetchedAction Called when the data has arrived.
     * @param errorAction       Called in case of a network error.
     */
    public void fetchLocations(Consumer<List<Location>> dataFetchedAction,
                               Consumer<String> errorAction) {
        String url = context.getString(R.string.locations_api_url);
        getTextFromUrl(url, response -> {
            List<Location> locations = locationsCsvToList(response);
            dataFetchedAction.accept(locations);
        }, error -> errorAction.accept(error.getMessage()));
    }

    /**
     * Fetches the dies data for a single day from the widget API. The API returns all high and
     * low tides of the specified day.
     *
     * This method converts the received data into a TidesInfo object which is then provided to
     * a callback.
     *
     * @param location Location of which the tides data should be fetched.
     * @param date Day for which the data should be fetched. Format: yyyy-mm-dd
     * @param dataFetchedAction Gets called as soon as data is available
     * @param errorAction Gets called in case of a network error.
     */
    public void fetchTidesDataSingleDay(Location location, String date, Consumer<TidesInfo> dataFetchedAction,
                                        Consumer<String> errorAction) {
        String url = String.format(context.getString(R.string.tides_widget_api_url), location.getId(), date);
        getTextFromUrl(url, response -> {
            TidesInfo tidesInfo = tidesInfoStringToObject(location, response);
            dataFetchedAction.accept(tidesInfo);
        }, error -> {
            errorAction.accept(error.getMessage());
        });
    }

    /**
     * Converts raw response from the widget API to a TidesInfo objects.
     *
     * The response string must have the following structure (example):
     *
     * STARTDATA+
     * 2022-07-07; 0:38;N
     * 2022-07-07; 6:55;H
     * 2022-07-07;12:39;N
     * 2022-07-07;19:04;H
     * ENDDATA+
     * Pegel/Date 510P at 2022-07-07
     *
     * It is allowed that only one high tide (H) or low tide (N) occurs instead of two.
     *
     * @param location Location for which the data was queried.
     * @param response Raw response text from the API
     * @return TidesInfo object containing the data from the API
     */
    private TidesInfo tidesInfoStringToObject(Location location, String response) {
        String[] lines = response.split("\n");
        if (lines.length < 4) {
            throw new IllegalArgumentException("Malformatted response data. Not enough lines.");
        }
        String[] dayInfos = Arrays.copyOfRange(lines, 1, lines.length - 2);

        String date = null;
        String highTide1 = null;
        String highTide2 = null;
        String lowTide1 = null;
        String lowTide2 = null;

        for (String dayInfo : dayInfos) {
            String[] components = dayInfo.split(";");
            if (components.length < 3) {
                throw new IllegalArgumentException("Malformatted response data. Not columns.");
            }
            String dateString = components[0].trim();
            String timeString = components[1].trim();
            String tideCategoryString = components[2].trim();

            if (date == null) {
                date = dateString;
            }

            switch (tideCategoryString) {
                case "H":
                    if (highTide1 == null) {
                        highTide1 = timeString;
                    } else {
                        highTide2 = timeString;
                    }
                    break;
                case "N":
                    if (lowTide1 == null) {
                        lowTide1 = timeString;
                    } else {
                        lowTide2 = timeString;
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Invalid tide category. Must be 'H' or 'N'");
            }
        }
        return new TidesInfo(location, date, highTide1, highTide2, lowTide1, lowTide2);
    }

    /**
     * Fetches a string from an URL via HTTP.
     *
     * @param url           URL which should be used to access the data.
     * @param successAction Called after data has been retrieved.
     * @param errorAction   Called in case of a network error.
     */
    private void getTextFromUrl(String url, Response.Listener<String> successAction,
                                Response.ErrorListener errorAction) {
        RequestQueue queue = Volley.newRequestQueue(context);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, successAction, errorAction);
        queue.add(stringRequest);
    }

    /**
     * Parses the CSV string containing locations and their IDs and converts it to a list of
     * Location objects.
     *
     * @param locationsCsv String containing a CSV-formatted list of locations
     * @return Locations contained in the CSV file as objects
     */
    private List<Location> locationsCsvToList(String locationsCsv) {
        String[] lines = locationsCsv.split("\n");
        List<Location> locations = new ArrayList<>(lines.length);
        for (String line : lines) {
            String[] columns = line.split(";");
            if (columns.length < 2) {
                throw new IllegalArgumentException("Invalid CSV line: " + line);
            }
            String locationName = columns[0];
            String locationId = columns[1];
            Location location = new Location(locationId, locationName);
            locations.add(location);
        }
        return locations;
    }
}
