package in.roshi.movies.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by roshi on 23/06/17.
 */

public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "mymovie.db";
    private static final int    DATABASE_VER  = 1;

    public DBHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VER);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String createFavourite = "CREATE TABLE " +
                DBContract.Favourite.TABLE_NAME + " (" +
                DBContract.Favourite._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                DBContract.Favourite.COLUMN_MOVIE_ID + " INTEGER NOT NULL," +
                DBContract.Favourite.COLUMN_TITLE + " TEXT NOT NULL," +
                DBContract.Favourite.COLUMN_OVERVIEW + " TEXT NOT NULL," +
                DBContract.Favourite.COLUMN_RELEASE_DATE + " TEXT NOT NULL," +
                DBContract.Favourite.COLUMN_POSTER_PATH + " TEXT NOT NULL," +
                DBContract.Favourite.COLUMN_POPULARITY + " TEXT NOT NULL," +
                DBContract.Favourite.COLUMN_RUNTIME + " TEXT NOT NULL," +
                DBContract.Favourite.COLUMN_TIMESTAMP + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ");";
        db.execSQL(createFavourite);

        final String createReview = "CREATE TABLE " +
                DBContract.REVIEW.TABLE_NAME + " (" +
                DBContract.REVIEW._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                DBContract.REVIEW.COLUMN_MOVIE_ID + " INTEGER NOT NULL," +
                DBContract.REVIEW.COLUMN_CONTENT + " TEXT NOT NULL," +
                DBContract.REVIEW.COLUMN_REVIEW_ID + " TEXT NOT NULL," +
                DBContract.REVIEW.COLUMN_AUTHOR + " TEXT NOT NULL," +
                DBContract.REVIEW.COLUMN_URL + " TEXT NOT NULL" +
                ");";
        db.execSQL(createReview);

        final String createVideo = "CREATE TABLE " +
                DBContract.VIDEO.TABLE_NAME + " (" +
                DBContract.VIDEO._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                DBContract.VIDEO.COLUMN_MOVIE_ID + " INTEGER NOT NULL," +
                DBContract.VIDEO.COLUMN_SITE + " TEXT NOT NULL," +
                DBContract.VIDEO.COLUMN_VIDEO_ID + " TEXT NOT NULL," +
                DBContract.VIDEO.COLUMN_ISO_639_1 + " TEXT NOT NULL," +
                DBContract.VIDEO.COLUMN_NAME + " TEXT NOT NULL," +
                DBContract.VIDEO.COLUMN_TYPE + " INTEGER NOT NULL," +
                DBContract.VIDEO.COLUMN_KEY + " TEXT NOT NULL," +
                DBContract.VIDEO.COLUMN_ISO_3166_1 + " TEXT NOT NULL," +
                DBContract.VIDEO.COLUMN_SIZE + " TEXT NOT NULL" +
                ");";
        db.execSQL(createVideo);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + DBContract.Favourite.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DBContract.VIDEO.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DBContract.REVIEW.TABLE_NAME);
        onCreate(db);
    }
}
