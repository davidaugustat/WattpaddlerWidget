package de.davidaugustat.wattpaddlerwidget.logic;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.core.util.Consumer;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import de.davidaugustat.wattpaddlerwidget.BuildConfig;
import de.davidaugustat.wattpaddlerwidget.data.Location;
import de.davidaugustat.wattpaddlerwidget.R;
import de.davidaugustat.wattpaddlerwidget.data.TidesInfo;
import de.davidaugustat.wattpaddlerwidget.data.TidesInfoBuilder;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * This class fetches data from the HTTP API and converts it to object oriented datasets.
 */
public class DataFetcher {

    private final Context context;
    private static final int REQUEST_TIMEOUT_MILLIS = 5000;
    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(REQUEST_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)
            .readTimeout(REQUEST_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)
            .build();

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
        String url = BuildConfig.LOCATIONS_API_URL + context.getString(R.string.locations_api_path);
        getTextFromUrl(url, response -> {
            List<Location> locations = locationsCsvToList(response);
            dataFetchedAction.accept(locations);
        }, errorAction);
    }

    /**
     * Fetches the tides data for a single day from the widget API. The API returns all high and
     * low tides of the specified day.
     * <p>
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
        String url = BuildConfig.TIDES_WIDGET_API_URL
                + String.format(context.getString(R.string.tides_widget_api_path), location.getId(), date);
        getTextFromUrl(url, response -> {
            try {
                TidesInfo tidesInfo = tidesInfoStringToObject(location, date, response);
                dataFetchedAction.accept(tidesInfo);
            } catch (IllegalArgumentException e){
                errorAction.accept("Error: Malformed response from API");
            }
        }, errorAction);
    }

    /**
     * Fetches the tides data for the current day from the widget API. The API returns all high and
     * low tides of the current day.
     * <p>
     * This method converts the received data into a TidesInfo object which is then provided to
     * a callback.
     *
     * @param location Location of which the tides data should be fetched.
     * @param dataFetchedAction Gets called as soon as data is available
     * @param errorAction Gets called in case of a network error.
     */
    public void fetchTidesDataSingleDay(Location location, Consumer<TidesInfo> dataFetchedAction,
                                                    Consumer<String> errorAction){
        String currentDateString = DateTimeHelper.getCurrentDateInQueryNotation();
        fetchTidesDataSingleDay(location, currentDateString, dataFetchedAction, errorAction);
    }

    /**
     * Converts raw response from the widget API to a TidesInfo objects.
     * <p>
     * The response string must have the following structure (example):
     * <p>
     * STARTDATA+
     * 2022-07-07; 0:38;N
     * 2022-07-07; 6:55;H
     * 2022-07-07;12:39;N
     * 2022-07-07;19:04;H
     * ENDDATA+
     * Pegel/Date 510P at 2022-07-07
     * <p>
     * It is allowed that only one high tide (H) or low tide (N) occurs instead of two. It is also
     * allowed that no low tides (N) are contained at all.
     *
     * @param location Location for which the data was queried.
     * @param response Raw response text from the API
     * @return TidesInfo object containing the data from the API
     */
    private TidesInfo tidesInfoStringToObject(Location location, String targetDate, String response) {
        String[] lines = response.split("\n");
        if (lines.length < 4) {
            throw new IllegalArgumentException("Malformed response data. Not enough lines.");
        }
        String[] dayInfos = Arrays.copyOfRange(lines, 1, lines.length - 2);
        TidesInfoBuilder tidesInfoFactory = new TidesInfoBuilder(location, targetDate);

        for (String dayInfo : dayInfos) {
            String[] components = dayInfo.split(";");
            if (components.length < 3) {
                throw new IllegalArgumentException("Malformed response data. Not enough columns.");
            }
            String dateString = components[0].trim();
            String timeString = components[1].trim();
            String tideCategoryString = components[2].trim();
            tidesInfoFactory.addTideTime(dateString, timeString, tideCategoryString);
        }
        return tidesInfoFactory.build();
    }

    /**
     * Fetches a string from an URL via HTTP using OkHttp.
     *
     * @param url           URL which should be used to access the data.
     * @param successAction Called after data has been retrieved.
     * @param errorAction   Called in case of a network error.
     */
    private void getTextFromUrl(String url, Consumer<String> successAction,
                                Consumer<String> errorAction) {
        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                new Handler(Looper.getMainLooper()).post(() -> errorAction.accept(e.toString()));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    new Handler(Looper.getMainLooper()).post(() -> errorAction.accept("Unexpected code " + response));
                    return;
                }

                try (ResponseBody responseBody = response.body()) {
                    // The API seems to use ISO-8859-1 (Latin-1) encoding, which is common for
                    // older German web services. OkHttp defaults to UTF-8 if no charset is
                    // specified in the Content-Type header. We explicitly handle this here
                    // to support German umlauts and "ß".
                    MediaType contentType = responseBody.contentType();
                    Charset charset = (contentType != null) ? contentType.charset(StandardCharsets.ISO_8859_1) : StandardCharsets.ISO_8859_1;
                    if (charset == null) {
                        charset = StandardCharsets.ISO_8859_1;
                    }

                    String bodyString = new String(responseBody.bytes(), charset);
                    new Handler(Looper.getMainLooper()).post(() -> successAction.accept(bodyString));
                }
            }
        });
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
