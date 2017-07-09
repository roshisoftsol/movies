package in.roshi.movies.home.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.gson.Gson;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import in.roshi.movies.R;
import in.roshi.movies.database.DBContract;
import in.roshi.movies.home.adaptor.MovieAdaptor;
import in.roshi.movies.home.model.MovieResults;
import in.roshi.movies.home.model.Results;
import in.roshi.movies.singlemovie.activity.SingleMovie;
import in.roshi.movies.util.NetworkUtils;

public class Home extends AppCompatActivity
        implements
        LoaderManager.LoaderCallbacks<MovieResults>,
        MovieAdaptor.ListItemClickListener  {

    private final String LOG_KEY = this.getClass().getCanonicalName();

    private static final int FETCH_MOVIES_TASK_ID = 1;
    private static final int FETCH_FAVORITES_TASK_ID = 2;
    private static final String FETCH_MOVIES_URL_IDENTIFIER = "url";

    private static final int SEARCH_BY_POPULARITY = 1;
    private static final int SEARCH_BY_RATING = 2;
    private static final int SEARCH_FAVOURITE = 3;

    // all screen elements
    @BindView(R.id.pb_loading_indicator) ProgressBar mLoadingIndicator;
    @BindView(R.id.tv_error_message_display) TextView mError;
    @BindView(R.id.rv_movies) RecyclerView mMovies;

    private Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);

        GridLayoutManager layoutManager =
                new GridLayoutManager(this, calculateNoOfColumns(getApplicationContext()));

        mMovies.setLayoutManager(layoutManager);

        getMovies(SEARCH_BY_POPULARITY);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mainmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemThatWasClickedId = item.getItemId();
        if (itemThatWasClickedId == R.id.action_search_popularity) {
            getMovies(SEARCH_BY_POPULARITY);
            return true;
        }
        else if (itemThatWasClickedId == R.id.action_search_rating) {
            getMovies(SEARCH_BY_RATING);
            return true;
        }
        else if (itemThatWasClickedId == R.id.action_search_favorite) {
            getMovies(SEARCH_FAVOURITE);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<MovieResults> onCreateLoader(final int id, final Bundle args) {

        return new AsyncTaskLoader<MovieResults>(this) {

            @Override
            protected void onStartLoading() {
                if (id < 2 && args == null) {
                    return;
                }
                forceLoad();
            }

            @Override
            public MovieResults loadInBackground() {

                if(id == FETCH_FAVORITES_TASK_ID){

                    Cursor cursor = getContentResolver().query(
                            DBContract.BASE_CONTENT_URI.buildUpon()
                                    .appendPath(DBContract.PATH_MOVIES)
                                    .build(),
                            null,
                            null,
                            null,
                            null);
                    if(cursor !=null && cursor.moveToFirst()){
                        MovieResults movieResults = new MovieResults();
                        ArrayList<Results> resultList = new ArrayList<>();
                        do{
                            Results obj = new Results();
                            obj.setId(cursor.getString(cursor.getColumnIndex(DBContract.Favourite.COLUMN_MOVIE_ID)));
                            obj.setTitle(cursor.getString(cursor.getColumnIndex(DBContract.Favourite.COLUMN_TITLE)));
                            obj.setPoster_path(cursor.getString(cursor.getColumnIndex(DBContract.Favourite.COLUMN_POSTER_PATH)));
                            resultList.add(obj);
                        }while(cursor.moveToNext());
                        Results[] results = new Results[resultList.size()];
                        resultList.toArray(results);
                        movieResults.setResults(results);
                        return movieResults;
                    }
                }
                else {
                    String movieDBURLString = args.getString(FETCH_MOVIES_URL_IDENTIFIER);

                    if (movieDBURLString == null || TextUtils.isEmpty(movieDBURLString)) {
                        return null;
                    }

                    try {
                        URL searchUrl = new URL(movieDBURLString);
                        String movieDbSearchResults = NetworkUtils.getResponseFromHttpUrl(searchUrl);
                        MovieResults movieResults = gson.fromJson(movieDbSearchResults, MovieResults.class);
                        return movieResults;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                return null;
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<MovieResults> loader, MovieResults data) {
        mLoadingIndicator.setVisibility(View.INVISIBLE);
        if (data != null)
            showMovies(data);
        else showError(loader.getId());
    }

    @Override
    public void onLoaderReset(Loader<MovieResults> loader) {

    }

    private void showMovies(MovieResults movieResults) {
        mError.setVisibility(View.INVISIBLE);
        MovieAdaptor mAdapter = new MovieAdaptor(movieResults, this, getApplicationContext());
        mMovies.setAdapter(mAdapter);
        mMovies.setVisibility(View.VISIBLE);
    }

    private void showError(int id) {
        mMovies.setVisibility(View.INVISIBLE);

        if (id == FETCH_FAVORITES_TASK_ID)
            mError.setText("No movies in favourite");
        else mError.setText("Unable to fetch movies");

        mError.setVisibility(View.VISIBLE);
    }

    private void getMovies(int searchBy) {

        LoaderManager loaderManager = getSupportLoaderManager();
        Loader<String> searchLoader;

        mLoadingIndicator.setVisibility(View.VISIBLE);

        switch (searchBy){
            case SEARCH_BY_POPULARITY:{
                // get most popular
                URL mostPopularUrl = NetworkUtils.buildUrl(this.getResources().getInteger(R.integer.most_popular));
                Bundle mostPopularBundle = new Bundle();
                mostPopularBundle.putString(FETCH_MOVIES_URL_IDENTIFIER, mostPopularUrl.toString());
                searchLoader = loaderManager.getLoader(FETCH_MOVIES_TASK_ID);
                if (searchLoader == null) {
                    loaderManager.initLoader(FETCH_MOVIES_TASK_ID, mostPopularBundle, this);
                } else {
                    loaderManager.restartLoader(FETCH_MOVIES_TASK_ID, mostPopularBundle, this);
                }
                break;
            }
            case SEARCH_BY_RATING:{
                // get top rated
                URL topRatedUrl = NetworkUtils.buildUrl(this.getResources().getInteger(R.integer.top_rated));
                Bundle topRatedBundle = new Bundle();
                topRatedBundle.putString(FETCH_MOVIES_URL_IDENTIFIER, topRatedUrl.toString());
                searchLoader = loaderManager.getLoader(FETCH_MOVIES_TASK_ID);
                if (searchLoader == null) {
                    loaderManager.initLoader(FETCH_MOVIES_TASK_ID, topRatedBundle, this);
                } else {
                    loaderManager.restartLoader(FETCH_MOVIES_TASK_ID, topRatedBundle, this);
                }
                break;
            }
            case SEARCH_FAVOURITE:{
                // get favourite from db
                searchLoader = loaderManager.getLoader(FETCH_FAVORITES_TASK_ID);
                if (searchLoader == null) {
                    loaderManager.initLoader(FETCH_FAVORITES_TASK_ID, null, this);
                } else {
                    loaderManager.restartLoader(FETCH_FAVORITES_TASK_ID, null, this);
                }
                break;
            }
            default:{ }
        }
    }

    @Override
    public void onListItemClick(int clickedItemIndex) {
        Class destinationActivity = SingleMovie.class;
        Intent startChildActivityIntent = new Intent(this, destinationActivity);
        startChildActivityIntent.putExtra(Intent.EXTRA_TEXT, String.valueOf(clickedItemIndex));
        startActivity(startChildActivityIntent);
    }

    public static int calculateNoOfColumns(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        int scalingFactor = 180;
        int noOfColumns = (int) (dpWidth / scalingFactor);
        return noOfColumns;
    }


}
