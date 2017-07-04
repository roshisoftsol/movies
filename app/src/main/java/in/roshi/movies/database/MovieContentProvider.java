package in.roshi.movies.database;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by roshi on 26/06/17.
 */

public class MovieContentProvider extends ContentProvider {

    private DBHelper mDBHelper;

    public final String LOG_KEY = this.getClass().getCanonicalName();
    public static final int MOVIES = 100;
    public static final int MOVIE_WITH_ID = 101;
    public static final int REVIEW = 200;
    public static final int REVIEW_WITH_ID = 201;
    public static final int VIDEO = 300;
    public static final int VIDEO_WITH_ID = 301;

    private static final UriMatcher sUriMatcher = buildUriMatcher();

    public static UriMatcher buildUriMatcher() {
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        uriMatcher.addURI(DBContract.AUTHORITY, DBContract.PATH_MOVIES, MOVIES);
        uriMatcher.addURI(DBContract.AUTHORITY, DBContract.PATH_MOVIES + "/#", MOVIE_WITH_ID);

        uriMatcher.addURI(DBContract.AUTHORITY, DBContract.PATH_REVIEW, REVIEW);
        uriMatcher.addURI(DBContract.AUTHORITY, DBContract.PATH_REVIEW + "/#", REVIEW_WITH_ID);

        uriMatcher.addURI(DBContract.AUTHORITY, DBContract.PATH_VIDEO, VIDEO);
        uriMatcher.addURI(DBContract.AUTHORITY, DBContract.PATH_VIDEO + "/#", VIDEO_WITH_ID);

        return uriMatcher;
    }

    @Override
    public boolean onCreate() {
        Context context = getContext();
        mDBHelper = new DBHelper(context);
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteDatabase db = mDBHelper.getReadableDatabase();
        int match = sUriMatcher.match(uri);
        Cursor cursor = null;

        switch (match){
            case MOVIES:{
                cursor = db.query(
                            DBContract.Favourite.TABLE_NAME,
                            projection,
                            selection,
                            selectionArgs,
                            null,
                            null,
                            sortOrder);
                break;
            }
            case MOVIE_WITH_ID:{
                // content://<authority>/movies/#
                //                       get(0)/get(1)
                String id = uri.getPathSegments().get(1);

                String mSelection = DBContract.Favourite.COLUMN_MOVIE_ID + "=?";
                String[] mSelectionArgs = new String[]{id};

                cursor = db.query(
                        DBContract.Favourite.TABLE_NAME,
                        projection,
                        mSelection,
                        mSelectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            }
            case REVIEW_WITH_ID:{
                String id = uri.getPathSegments().get(1);

                String mSelection = DBContract.REVIEW.COLUMN_MOVIE_ID + "=?";
                String[] mSelectionArgs = new String[]{id};

                cursor = db.query(
                        DBContract.REVIEW.TABLE_NAME,
                        projection,
                        mSelection,
                        mSelectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            }
            case VIDEO_WITH_ID:{
                String id = uri.getPathSegments().get(1);

                String mSelection = DBContract.VIDEO.COLUMN_MOVIE_ID + "=?";
                String[] mSelectionArgs = new String[]{id};

                cursor = db.query(
                        DBContract.VIDEO.TABLE_NAME,
                        projection,
                        mSelection,
                        mSelectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown URI: " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        int match = sUriMatcher.match(uri);
        Uri returnUri;
        switch (match){
            case MOVIES:{
                long id = db.insert(DBContract.Favourite.TABLE_NAME, null, values);
                if(id > 0){
                    returnUri = ContentUris.withAppendedId(DBContract.Favourite.CONTENT_URI, id);
                } else {
                    throw new SQLException("Fail to insert row into: " + uri);
                }
                break;
            }
            case REVIEW:{
                long id = db.insert(DBContract.REVIEW.TABLE_NAME, null, values);
                if(id > 0){
                    returnUri = ContentUris.withAppendedId(DBContract.REVIEW.CONTENT_URI, id);
                } else {
                    throw new SQLException("Fail to insert row into: " + uri);
                }
                break;
            }
            case VIDEO:{
                long id = db.insert(DBContract.VIDEO.TABLE_NAME, null, values);
                if(id > 0){
                    returnUri = ContentUris.withAppendedId(DBContract.VIDEO.CONTENT_URI, id);
                } else {
                    throw new SQLException("Fail to insert row into: " + uri);
                }
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown URI: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        int match = sUriMatcher.match(uri);

        int noOfRows;
        switch (match){
            case MOVIES:{
                db.beginTransaction();
                db.delete(
                        DBContract.REVIEW.TABLE_NAME,
                        DBContract.REVIEW.COLUMN_MOVIE_ID + "=?",
                        selectionArgs);
                db.delete(
                        DBContract.VIDEO.TABLE_NAME,
                        DBContract.VIDEO.COLUMN_MOVIE_ID + "=?",
                        selectionArgs);
                noOfRows = db.delete(
                                DBContract.Favourite.TABLE_NAME,
                                DBContract.Favourite.COLUMN_MOVIE_ID + "=?",
                                selectionArgs);
                db.setTransactionSuccessful();
                db.endTransaction();
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown URI: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return noOfRows;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }
}
