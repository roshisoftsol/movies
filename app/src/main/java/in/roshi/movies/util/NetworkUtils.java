package in.roshi.movies.util;

import android.net.Uri;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

public class NetworkUtils {

    final static String MOVIEDB_BASE_URL =
            "https://api.themoviedb.org/3/movie";

    // INSERT API KEY HERE
    final static String MOVIEDB_API_KEY = "";

    final static String MOVIEDB_LANGUAGE = "en-US";

    final static String PARAM_KEY = "api_key";
    final static String PARAM_LANGUAGE = "language";

    /*
     * The sort field. One of stars, forks, or updated.
     * Default: results are sorted by best match if no field is specified.
     */
    final static String sortBy = "stars";

    public static URL buildUrl(int sort) {

        String final_url = MOVIEDB_BASE_URL;
        if(sort == 1)
            final_url += "/popular";
        else final_url += "/top_rated";

        Uri builtUri = Uri.parse(final_url).buildUpon()
                .appendQueryParameter(PARAM_KEY, MOVIEDB_API_KEY)
                .appendQueryParameter(PARAM_LANGUAGE, MOVIEDB_LANGUAGE)
                .build();

        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return url;
    }

    public static URL buildMovieUrl(int movieID) {

        String final_url = MOVIEDB_BASE_URL + "/" + movieID;
        Uri builtUri = Uri.parse(final_url).buildUpon()
                .appendQueryParameter(PARAM_KEY, MOVIEDB_API_KEY)
                .appendQueryParameter(PARAM_LANGUAGE, MOVIEDB_LANGUAGE)
                .build();

        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return url;
    }

    public static URL buildMovieVideoUrl(int movieID) {
        String final_url = MOVIEDB_BASE_URL + "/" + movieID + "/videos";
        Uri builtUri = Uri.parse(final_url).buildUpon()
                .appendQueryParameter(PARAM_KEY, MOVIEDB_API_KEY)
                .appendQueryParameter(PARAM_LANGUAGE, MOVIEDB_LANGUAGE)
                .build();

        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return url;
    }

    public static URL buildMovieReviewUrl(int movieID) {
        String final_url = MOVIEDB_BASE_URL + "/" + movieID + "/reviews";
        Uri builtUri = Uri.parse(final_url).buildUpon()
                .appendQueryParameter(PARAM_KEY, MOVIEDB_API_KEY)
                .appendQueryParameter(PARAM_LANGUAGE, MOVIEDB_LANGUAGE)
                .build();

        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return url;
    }

    /**
     * This method returns the entire result from the HTTP response.
     *
     * @param url The URL to fetch the HTTP response from.
     * @return The contents of the HTTP response.
     * @throws IOException Related to network and stream reading
     */
    public static String getResponseFromHttpUrl(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            if (hasInput) {
                return scanner.next();
            } else {
                return null;
            }
        } finally {
            urlConnection.disconnect();
        }
    }
}
