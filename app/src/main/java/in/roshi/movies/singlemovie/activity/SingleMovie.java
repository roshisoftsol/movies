package in.roshi.movies.singlemovie.activity;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import in.roshi.movies.R;
import in.roshi.movies.database.DBContract;
import in.roshi.movies.home.model.MovieResults;
import in.roshi.movies.singlemovie.adaptor.ReviewAdaptor;
import in.roshi.movies.singlemovie.adaptor.VideoAdaptor;
import in.roshi.movies.singlemovie.model.movie.Movie;
import in.roshi.movies.singlemovie.model.review.Results;
import in.roshi.movies.singlemovie.model.review.Review;
import in.roshi.movies.singlemovie.model.videos.Videos;
import in.roshi.movies.util.NetworkUtils;

public class SingleMovie extends AppCompatActivity
    implements
        VideoAdaptor.ListItemClickListener,
        LoaderManager.LoaderCallbacks {

    private final String LOG_KEY = this.getClass().getCanonicalName();

    private final int API_MOVIE_DATA_TASK_ID = 1;
    private final int API_REVIEWS_TASK_ID = 2;
    private final int API_VIDEOS_TASK_ID = 3;
    private final int DATA_FROM_DB_TASK_ID = 4;
    private final int GET_IMAGE_TASK_ID = 5;

    private ConstraintLayout constraintLayout;
    private TextView  mError;
    private ImageView mImageView;
    private TextView  mMovieTitle;
    private TextView  mMovieYear;
    private TextView  mMovieDuration;
    private TextView  mMovieRating;
    private TextView  mMovieDescription;
    private Button    mFavourite;
    private Boolean   mMovieIsFavorite;
    private Movie     mMovieData;
    private Videos    mVideos;
    private Review    mReview;
    private Bitmap    mMoviePosterBitmap;
    private RecyclerView mVideosView;
    private RecyclerView mReviewView;

    private Gson gson = new Gson();

    private int movieID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_movie);

        // Set back button
        ActionBar actionBar = this.getSupportActionBar();
        Intent intent = getIntent();
        if(actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);

        // initialize all screen elements
        constraintLayout = (ConstraintLayout) findViewById(R.id.movie_container);
        mImageView = (ImageView) findViewById(R.id.iv_movie_poster);
        mMovieTitle = (TextView) findViewById(R.id.tv_movie_title);
        mMovieYear = (TextView) findViewById(R.id.tv_movie_year);
        mMovieDuration = (TextView) findViewById(R.id.tv_movie_duration);
        mMovieRating = (TextView) findViewById(R.id.tv_movie_rating);
        mMovieDescription = (TextView) findViewById(R.id.tv_movie_description);
        mFavourite = (Button) findViewById(R.id.bt_movie_favourite);
        mVideosView = (RecyclerView) findViewById(R.id.rv_videos);
        mReviewView = (RecyclerView) findViewById(R.id.rv_review);

        LinearLayoutManager layoutManagerVideo = new LinearLayoutManager(this);
        mVideosView.setLayoutManager(layoutManagerVideo);
        LinearLayoutManager layoutManagerReview = new LinearLayoutManager(this);
        mReviewView.setLayoutManager(layoutManagerReview);

        // get id of movie on which user clicked
        if(intent.hasExtra(Intent.EXTRA_TEXT))
            movieID = Integer.parseInt(intent.getStringExtra(Intent.EXTRA_TEXT));
        else movieID = 0;

        if(movieID == 0) {
            return;
        }

        getMovieData();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == android.R.id.home)
            NavUtils.navigateUpFromSameTask(this);
        return super.onOptionsItemSelected(item);
    }

    private void getMovieData(){

        LoaderManager loaderManager = getSupportLoaderManager();
        Loader<String> searchLoader;

        // check if movie is set as favourite
        Cursor cursor = getContentResolver().query(
                DBContract.BASE_CONTENT_URI.buildUpon()
                        .appendPath(DBContract.PATH_MOVIES)
                        .appendPath(String.valueOf(movieID))
                        .build(),
                null,
                null,
                null,
                null);
        if(cursor !=null && cursor.getCount() > 0)
            mMovieIsFavorite = true;
        else mMovieIsFavorite = false;

        // Get movie data, if favourite get from db, else get from movieDBAPI
        if(mMovieIsFavorite){
            mFavourite.setText(R.string.remove_favorite);

            cursor.moveToFirst();
            mMovieData = new Movie();

            // get movie data from db
            mMovieData.setTitle(cursor.getString(cursor.getColumnIndex(DBContract.Favourite.COLUMN_TITLE)));
            mMovieData.setPoster_path(cursor.getString(cursor.getColumnIndex(DBContract.Favourite.COLUMN_POSTER_PATH)));
            mMovieData.setRuntime(cursor.getString(cursor.getColumnIndex(DBContract.Favourite.COLUMN_RUNTIME)));
            mMovieData.setPopularity(cursor.getString(cursor.getColumnIndex(DBContract.Favourite.COLUMN_POPULARITY)));
            mMovieData.setOverview(cursor.getString(cursor.getColumnIndex(DBContract.Favourite.COLUMN_OVERVIEW)));
            mMovieData.setRelease_date(cursor.getString(cursor.getColumnIndex(DBContract.Favourite.COLUMN_RELEASE_DATE)));

            // get reviews from db
            Cursor cursorReview = getContentResolver().query(
                    DBContract.BASE_CONTENT_URI.buildUpon()
                            .appendPath(DBContract.PATH_REVIEW)
                            .appendPath(String.valueOf(cursor.getInt(cursor.getColumnIndex(DBContract.Favourite._ID))))
                            .build(),
                    null,
                    null,
                    null,
                    null);
            if(cursorReview !=null && cursorReview.moveToFirst() && cursorReview.getCount() > 0){
                mReview = new Review();
                ArrayList<Results> resultList = new ArrayList<>();
                do{
                    Results obj = new Results();
                    obj.setId(cursorReview.getString(cursorReview.getColumnIndex(DBContract.REVIEW.COLUMN_REVIEW_ID)));
                    obj.setContent(cursorReview.getString(cursorReview.getColumnIndex(DBContract.REVIEW.COLUMN_CONTENT)));
                    obj.setAuthor(cursorReview.getString(cursorReview.getColumnIndex(DBContract.REVIEW.COLUMN_AUTHOR)));
                    obj.setUrl(cursorReview.getString(cursorReview.getColumnIndex(DBContract.REVIEW.COLUMN_URL)));
                    resultList.add(obj);
                }while(cursorReview.moveToNext());
                Results[] results = new Results[resultList.size()];
                resultList.toArray(results);
                mReview.setResults(results);
                cursorReview.close();
            }

            // get videos from db
            Cursor cursorVideo = getContentResolver().query(
                    DBContract.BASE_CONTENT_URI.buildUpon()
                            .appendPath(DBContract.PATH_VIDEO)
                            .appendPath(String.valueOf(cursor.getInt(cursor.getColumnIndex(DBContract.Favourite._ID))))
                            .build(),
                    null,
                    null,
                    null,
                    null);
            if(cursorVideo !=null && cursorVideo.moveToFirst() && cursorVideo.getCount() > 0){
                mVideos = new Videos();
                ArrayList<in.roshi.movies.singlemovie.model.videos.Results> videoList = new ArrayList<>();
                do{
                    in.roshi.movies.singlemovie.model.videos.Results obj =
                            new in.roshi.movies.singlemovie.model.videos.Results();
                    obj.setId(cursorVideo.getString(cursorVideo.getColumnIndex(DBContract.VIDEO.COLUMN_VIDEO_ID)));
                    obj.setSite(cursorVideo.getString(cursorVideo.getColumnIndex(DBContract.VIDEO.COLUMN_SITE)));
                    obj.setIso_639_1(cursorVideo.getString(cursorVideo.getColumnIndex(DBContract.VIDEO.COLUMN_ISO_639_1)));
                    obj.setName(cursorVideo.getString(cursorVideo.getColumnIndex(DBContract.VIDEO.COLUMN_NAME)));
                    obj.setType(cursorVideo.getString(cursorVideo.getColumnIndex(DBContract.VIDEO.COLUMN_TYPE)));
                    obj.setKey(cursorVideo.getString(cursorVideo.getColumnIndex(DBContract.VIDEO.COLUMN_KEY)));
                    obj.setIso_3166_1(cursorVideo.getString(cursorVideo.getColumnIndex(DBContract.VIDEO.COLUMN_ISO_3166_1)));
                    obj.setSize(cursorVideo.getString(cursorVideo.getColumnIndex(DBContract.VIDEO.COLUMN_SIZE)));
                    videoList.add(obj);
                }while(cursorVideo.moveToNext());
                in.roshi.movies.singlemovie.model.videos.Results[] results =
                        new in.roshi.movies.singlemovie.model.videos.Results[videoList.size()];
                videoList.toArray(results);
                mVideos.setResults(results);
                cursorVideo.close();
            }

            // get poster image
            searchLoader = loaderManager.getLoader(GET_IMAGE_TASK_ID);
            if (searchLoader == null) {
                loaderManager.initLoader(GET_IMAGE_TASK_ID, null, this);
            } else {
                loaderManager.restartLoader(GET_IMAGE_TASK_ID, null, this);
            }

            showMovieData();
            showReviews();
            showVideos();

            cursor.close();
        }
        else {
            mFavourite.setText(R.string.add_to_favorite);

            // get movie data
            searchLoader = loaderManager.getLoader(API_MOVIE_DATA_TASK_ID);
            if (searchLoader == null) {
                loaderManager.initLoader(API_MOVIE_DATA_TASK_ID, null, this);
            } else {
                loaderManager.restartLoader(API_MOVIE_DATA_TASK_ID, null, this);
            }

            // get reviews
            searchLoader = loaderManager.getLoader(API_REVIEWS_TASK_ID);
            if (searchLoader == null) {
                loaderManager.initLoader(API_REVIEWS_TASK_ID, null, this);
            } else {
                loaderManager.restartLoader(API_REVIEWS_TASK_ID, null, this);
            }

            // get videos
            searchLoader = loaderManager.getLoader(API_VIDEOS_TASK_ID);
            if (searchLoader == null) {
                loaderManager.initLoader(API_VIDEOS_TASK_ID, null, this);
            } else {
                loaderManager.restartLoader(API_VIDEOS_TASK_ID, null, this);
            }

        }
    }

    public void setFavorite(View view){
        if(mMovieIsFavorite){
            String mSelection = DBContract.Favourite.COLUMN_MOVIE_ID + "=?";
            String[] mSelectionArgs = new String[]{movieID + ""};
            int count = getContentResolver().delete(DBContract.Favourite.CONTENT_URI, mSelection, mSelectionArgs);
            if(count > 0) {
                mFavourite.setText(R.string.add_to_favorite);
                mMovieIsFavorite = false;
            } else {
                Toast.makeText(this, "Unable to remove", Toast.LENGTH_LONG);
            }
        }
        else {

            // add movie data
            ContentValues contentValues = new ContentValues();
            contentValues.put(DBContract.Favourite.COLUMN_MOVIE_ID, mMovieData.getId());
            contentValues.put(DBContract.Favourite.COLUMN_TITLE, mMovieData.getTitle());
            contentValues.put(DBContract.Favourite.COLUMN_OVERVIEW, mMovieData.getOverview());
            contentValues.put(DBContract.Favourite.COLUMN_RELEASE_DATE, mMovieData.getRelease_date());
            contentValues.put(DBContract.Favourite.COLUMN_POSTER_PATH, mMovieData.getPoster_path());
            contentValues.put(DBContract.Favourite.COLUMN_POPULARITY, mMovieData.getPopularity());
            contentValues.put(DBContract.Favourite.COLUMN_RUNTIME, mMovieData.getRuntime());
            Uri uri = getContentResolver().insert(DBContract.Favourite.CONTENT_URI, contentValues);

            if(uri != null){
                int id = Integer.valueOf(uri.getPathSegments().get(1));
                if(id > 0) {

                    // add review
                    if(mReview != null) {
                        for(Results results: mReview.getResults()){
                            contentValues = new ContentValues();
                            contentValues.put(DBContract.REVIEW.COLUMN_MOVIE_ID, id);
                            contentValues.put(DBContract.REVIEW.COLUMN_CONTENT, results.getContent());
                            contentValues.put(DBContract.REVIEW.COLUMN_REVIEW_ID, results.getId());
                            contentValues.put(DBContract.REVIEW.COLUMN_AUTHOR, results.getAuthor());
                            contentValues.put(DBContract.REVIEW.COLUMN_URL, results.getUrl());
                            Uri uriR = getContentResolver().insert(DBContract.REVIEW.CONTENT_URI, contentValues);
                        }
                    }

                    // add video
                    if(mVideos != null){
                        for (in.roshi.movies.singlemovie.model.videos.Results results: mVideos.getResults()){
                            contentValues = new ContentValues();
                            contentValues.put(DBContract.VIDEO.COLUMN_MOVIE_ID, id);
                            contentValues.put(DBContract.VIDEO.COLUMN_SITE, results.getSite());
                            contentValues.put(DBContract.VIDEO.COLUMN_VIDEO_ID, results.getId());
                            contentValues.put(DBContract.VIDEO.COLUMN_ISO_639_1, results.getIso_639_1());
                            contentValues.put(DBContract.VIDEO.COLUMN_NAME, results.getName());
                            contentValues.put(DBContract.VIDEO.COLUMN_TYPE, results.getType());
                            contentValues.put(DBContract.VIDEO.COLUMN_KEY, results.getKey());
                            contentValues.put(DBContract.VIDEO.COLUMN_ISO_3166_1, results.getIso_3166_1());
                            contentValues.put(DBContract.VIDEO.COLUMN_SIZE, results.getSize());
                            Uri uriV = getContentResolver().insert(DBContract.VIDEO.CONTENT_URI, contentValues);
                        }
                    }

                    mFavourite.setText(R.string.remove_favorite);
                    mMovieIsFavorite = true;
                } else {
                    Toast.makeText(this, "Unable to add to favorite", Toast.LENGTH_LONG);
                }
            } else {
                Toast.makeText(this, "Unable to add to favorite", Toast.LENGTH_LONG);
            }
        }
    }

    private void showMovieData(){
        if(mMovieData != null) {
            mMovieTitle.setText(mMovieData.getTitle());
            mMovieYear.setText(mMovieData.getRelease_date());
            mMovieDuration.setText(mMovieData.getRuntime() + " mins");
            mMovieRating.setText(mMovieData.getPopularity());
            mMovieDescription.setText(mMovieData.getOverview());
            showPoster();
        }
    }

    private void showReviews(){
        if(mReview != null){
            ReviewAdaptor mAdapterReview = new ReviewAdaptor(mReview);
            mReviewView.setAdapter(mAdapterReview);
        }
    }

    private void showVideos(){
        if(mVideos != null){
            VideoAdaptor mAdapterVideo = new VideoAdaptor(mVideos, this);
            mVideosView.setAdapter(mAdapterVideo);
        }
    }

    private void showPoster(){
        if(mMoviePosterBitmap != null)
            mImageView.setImageBitmap(mMoviePosterBitmap);
    }

    @Override
    public void onListItemClick(String videoID) {
        Intent intent = new Intent(
                Intent.ACTION_VIEW ,
                Uri.parse("https://www.youtube.com/watch?v=" + videoID));
        startActivity(intent);
    }

    @Override
    public Loader onCreateLoader(final int id, Bundle args) {
        return new AsyncTaskLoader<String>(this) {

            @Override
            protected void onStartLoading() {
                forceLoad();
            }

            @Override
            public String loadInBackground() {

                URL searchUrl;
                String movieDbSearchResults = null;

                if(id == API_MOVIE_DATA_TASK_ID){
                    try {
                        searchUrl = NetworkUtils.buildMovieUrl(movieID);
                        movieDbSearchResults = NetworkUtils.getResponseFromHttpUrl(searchUrl);
                        if (movieDbSearchResults != null) {
                            mMovieData = gson.fromJson(movieDbSearchResults, Movie.class);
                            String poster_url = "http://image.tmdb.org/t/p/w185" + mMovieData.getPoster_path();
                            InputStream in = new java.net.URL(poster_url).openStream();
                            mMoviePosterBitmap = BitmapFactory.decodeStream(in);
                        }
                        else mMovieData = null;
                    } catch (IOException e){
                        mMovieData = null;
                    }
                }
                else if(id == API_REVIEWS_TASK_ID){
                    try {
                        searchUrl = NetworkUtils.buildMovieReviewUrl(movieID);
                        movieDbSearchResults = NetworkUtils.getResponseFromHttpUrl(searchUrl);
                        if (movieDbSearchResults != null ) {
                            mReview = gson.fromJson(movieDbSearchResults, Review.class);
                        }
                        else mReview = null;
                    } catch (IOException e){
                        mReview = null;
                    }
                }
                else if(id == API_VIDEOS_TASK_ID){
                    try {
                        searchUrl = NetworkUtils.buildMovieVideoUrl(movieID);
                        movieDbSearchResults = NetworkUtils.getResponseFromHttpUrl(searchUrl);
                        if (movieDbSearchResults != null ) {
                            mVideos = gson.fromJson(movieDbSearchResults, Videos.class);
                        }
                        else mVideos = null;
                    } catch (IOException e){
                        mReview = null;
                    }
                }
                else if(id == GET_IMAGE_TASK_ID){
                    try {
                        String poster_url = "http://image.tmdb.org/t/p/w185" + mMovieData.getPoster_path();
                        InputStream in = new java.net.URL(poster_url).openStream();
                        mMoviePosterBitmap = BitmapFactory.decodeStream(in);
                    }
                    catch (MalformedURLException e){}
                    catch (IOException e){}
                }

                return null;
            }
        };
    }

    @Override
    public void onLoadFinished(Loader loader, Object data) {
        if(loader.getId() == API_MOVIE_DATA_TASK_ID) {
            showMovieData();
        }
        else if(loader.getId() == API_REVIEWS_TASK_ID) {
            showReviews();
        }
        else if(loader.getId() == API_VIDEOS_TASK_ID) {
            showVideos();
        }
        else if(loader.getId() == GET_IMAGE_TASK_ID) {
            showPoster();
        }
    }

    @Override
    public void onLoaderReset(Loader loader) {

    }

}
