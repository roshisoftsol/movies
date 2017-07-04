package in.roshi.movies.database;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by roshi on 23/06/17.
 */

public class DBContract {

    public static final String AUTHORITY = "in.roshi.movies";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);
    public static final String PATH_MOVIES = "movies";
    public static final String PATH_REVIEW = "review";
    public static final String PATH_VIDEO = "videos";

    private DBContract(){

    }

    public static final class Favourite implements BaseColumns{

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIES).build();

        public static final String TABLE_NAME = "favourite";
        public static final String COLUMN_MOVIE_ID = "movieid";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_OVERVIEW = "overview";
        public static final String COLUMN_RELEASE_DATE = "releasedate";
        public static final String COLUMN_POSTER_PATH = "posterpath";
        public static final String COLUMN_POPULARITY = "popularity";
        public static final String COLUMN_RUNTIME = "runtime";
        public static final String COLUMN_TIMESTAMP = "timestamp";
    }

    public static final class REVIEW implements BaseColumns{

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_REVIEW).build();

        public static final String TABLE_NAME = "review";
        public static final String COLUMN_MOVIE_ID = "movieid";
        public static final String COLUMN_CONTENT = "content";
        public static final String COLUMN_REVIEW_ID = "reviewID";
        public static final String COLUMN_AUTHOR = "author";
        public static final String COLUMN_URL = "url";
    }

    public static final class VIDEO implements BaseColumns{

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_VIDEO).build();

        public static final String TABLE_NAME = "video";
        public static final String COLUMN_MOVIE_ID = "movieid";
        public static final String COLUMN_SITE = "site";
        public static final String COLUMN_VIDEO_ID = "id";
        public static final String COLUMN_ISO_639_1 = "iso6391";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_TYPE = "type";
        public static final String COLUMN_KEY = "key";
        public static final String COLUMN_ISO_3166_1 = "iso3166_1";
        public static final String COLUMN_SIZE = "size";

    }

}
