package in.roshi.movies.home.adaptor;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.InputStream;

import in.roshi.movies.R;
import in.roshi.movies.home.model.MovieResults;
import in.roshi.movies.home.model.Results;

/**
 * Created by roshi on 21/06/17.
 */

public class MovieAdaptor extends RecyclerView.Adapter<MovieAdaptor.MovieViewHolder> {

    private final String LOG_KEY = this.getClass().getCanonicalName();
    private MovieResults movieResults;
    final private ListItemClickListener mOnClickListener;

    public interface ListItemClickListener {
        void onListItemClick(int clickedItemIndex);
    }

    public MovieAdaptor(MovieResults movieResults, ListItemClickListener listener) {
        mOnClickListener = listener;
        this.movieResults = movieResults;
    }

    @Override
    public MovieViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();
        int layoutIdForListItem = R.layout.movieclip;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, viewGroup, shouldAttachToParentImmediately);
        MovieViewHolder viewHolder = new MovieViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(MovieViewHolder holder, int position) {
        try{
            holder.bind(movieResults.getResults()[position]);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public int getItemCount() {
        return movieResults.getResults().length;
    }


    class MovieViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        TextView listItemNumberView;
        TextView movieID;
        ImageView imageView;

        public MovieViewHolder(View itemView) {
            super(itemView);

            listItemNumberView = (TextView) itemView.findViewById(R.id.tv_movie_name);
            movieID = (TextView) itemView.findViewById(R.id.tv_movie_id);
            imageView = (ImageView) itemView.findViewById(R.id.iv_movie_poster);
            itemView.setOnClickListener(this);
        }

        void bind(Results results) {
            try{
                String poster_url = "http://image.tmdb.org/t/p/w185" + results.getPoster_path();
                listItemNumberView.setText(results.getTitle());
                movieID.setText(results.getId());
                new DownloadImageTask(imageView).execute(poster_url);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onClick(View v) {
            int clickedPosition = getAdapterPosition();
            TextView t = (TextView) v.findViewById(R.id.tv_movie_id);
            mOnClickListener.onListItemClick(Integer.parseInt(t.getText().toString()));
        }
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }

}
