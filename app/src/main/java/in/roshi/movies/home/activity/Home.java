package in.roshi.movies.home.activity;

import android.content.Intent;
import android.database.Cursor;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.gson.Gson;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

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
        MovieAdaptor.ListItemClickListener {

    private final String LOG_KEY = this.getClass().getCanonicalName();

    private static final int TOP_RATED_TASK_ID = 1;
    private static final int MOST_POPULAR_TASK_ID = 2;
    private static final int FAVORITE_TASK_ID = 3;
    private static final String URL_IDENTIFIER = "url";

    // all screen elements
    private ProgressBar mLoadingIndicator;
    private ScrollView mScrollHome;
    private TextView mErrorTopRated;
    private TextView mErrorMostPopular;
    private TextView mErrorFavorite;
    private RecyclerView mMostPopular;
    private RecyclerView mTopRated;
    private RecyclerView mFavorite;

    private Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // initialize all screen elements
        mLoadingIndicator = (ProgressBar) findViewById(R.id.pb_loading_indicator);
        mScrollHome = (ScrollView) findViewById(R.id.sv_home);
        mErrorTopRated = (TextView) findViewById(R.id.tv_error_top_rated);
        mErrorMostPopular = (TextView) findViewById(R.id.tv_error_most_popular);
        mErrorFavorite = (TextView) findViewById(R.id.tv_error_favorite);
        mMostPopular = (RecyclerView) findViewById(R.id.rv_most_popular);
        mTopRated = (RecyclerView) findViewById(R.id.rv_top_rated);
        mFavorite = (RecyclerView) findViewById(R.id.rv_favorite);

        GridLayoutManager lmMostPopular = new GridLayoutManager(this, 1, GridLayoutManager.HORIZONTAL, false);
        GridLayoutManager lmTopRated = new GridLayoutManager(this, 1, GridLayoutManager.HORIZONTAL, false);
        GridLayoutManager lmFavorite = new GridLayoutManager(this, 1, GridLayoutManager.HORIZONTAL, false);

        mMostPopular.setLayoutManager(lmMostPopular);
        mTopRated.setLayoutManager(lmTopRated);
        mFavorite.setLayoutManager(lmFavorite);

        getMovies();

    }

    @Override
    public Loader<MovieResults> onCreateLoader(final int id, final Bundle args) {

        return new AsyncTaskLoader<MovieResults>(this) {

            @Override
            protected void onStartLoading() {
                if (id < 3 && args == null) {
                    return;
                }
                forceLoad();
            }

            @Override
            public MovieResults loadInBackground() {

                if(id == FAVORITE_TASK_ID){
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
                    String movieDBURLString = args.getString(URL_IDENTIFIER);

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

        if(loader.getId() == MOST_POPULAR_TASK_ID) {
            if (data != null)
                showMostPopular(data);
            else showErrorMostPopular();
        }
        else if(loader.getId() == TOP_RATED_TASK_ID) {
            if (data != null)
                showTopRated(data);
            else showErrorTopRated();
        }
        else if(loader.getId() == FAVORITE_TASK_ID) {
            if (data != null)
                showFavourite(data);
            else showErrorFavorite();
        }
    }

    @Override
    public void onLoaderReset(Loader<MovieResults> loader) {

    }

    private void showMostPopular(MovieResults movieResults) {
        mErrorMostPopular.setVisibility(View.INVISIBLE);
        MovieAdaptor mAdapter = new MovieAdaptor(movieResults, this);
        mMostPopular.setAdapter(mAdapter);
        mMostPopular.setVisibility(View.VISIBLE);
    }

    private void showTopRated(MovieResults movieResults) {
        mErrorTopRated.setVisibility(View.INVISIBLE);
        MovieAdaptor mAdapter = new MovieAdaptor(movieResults, this);
        mTopRated.setAdapter(mAdapter);
        mTopRated.setVisibility(View.VISIBLE);
    }

    private void showFavourite(MovieResults movieResults) {
        mErrorFavorite.setVisibility(View.INVISIBLE);
        MovieAdaptor mAdapter = new MovieAdaptor(movieResults, this);
        mFavorite.setAdapter(mAdapter);
        mFavorite.setVisibility(View.VISIBLE);
    }

    private void showErrorMostPopular() {
        mMostPopular.setVisibility(View.INVISIBLE);
        mErrorMostPopular.setVisibility(View.VISIBLE);
    }

    private void showErrorTopRated() {
        mTopRated.setVisibility(View.INVISIBLE);
        mErrorTopRated.setVisibility(View.VISIBLE);
    }

    private void showErrorFavorite() {
        mFavorite.setVisibility(View.INVISIBLE);
        mErrorFavorite.setVisibility(View.VISIBLE);
    }

    private void getMovies() {

        LoaderManager loaderManager = getSupportLoaderManager();
        Loader<String> searchLoader;

        mLoadingIndicator.setVisibility(View.VISIBLE);

        // get most popular
        URL mostPopularUrl = NetworkUtils.buildUrl(this.getResources().getInteger(R.integer.most_popular));
        Bundle mostPopularBundle = new Bundle();
        mostPopularBundle.putString(URL_IDENTIFIER, mostPopularUrl.toString());
        searchLoader = loaderManager.getLoader(MOST_POPULAR_TASK_ID);
        if (searchLoader == null) {
            loaderManager.initLoader(MOST_POPULAR_TASK_ID, mostPopularBundle, this);
        } else {
            loaderManager.restartLoader(MOST_POPULAR_TASK_ID, mostPopularBundle, this);
        }

        // get top rated
        URL topRatedUrl = NetworkUtils.buildUrl(this.getResources().getInteger(R.integer.top_rated));
        Bundle topRatedBundle = new Bundle();
        topRatedBundle.putString(URL_IDENTIFIER, topRatedUrl.toString());
        searchLoader = loaderManager.getLoader(TOP_RATED_TASK_ID);
        if (searchLoader == null) {
            loaderManager.initLoader(TOP_RATED_TASK_ID, topRatedBundle, this);
        } else {
            loaderManager.restartLoader(TOP_RATED_TASK_ID, topRatedBundle, this);
        }

        // get favourite from db
        searchLoader = loaderManager.getLoader(FAVORITE_TASK_ID);
        if (searchLoader == null) {
            loaderManager.initLoader(FAVORITE_TASK_ID, null, this);
        } else {
            loaderManager.restartLoader(FAVORITE_TASK_ID, null, this);
        }

    }

    @Override
    public void onListItemClick(int clickedItemIndex) {
        Class destinationActivity = SingleMovie.class;
        Intent startChildActivityIntent = new Intent(this, destinationActivity);
        startChildActivityIntent.putExtra(Intent.EXTRA_TEXT, String.valueOf(clickedItemIndex));
        startActivity(startChildActivityIntent);
    }
}
