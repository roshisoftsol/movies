package in.roshi.movies.singlemovie.adaptor;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import in.roshi.movies.R;
import in.roshi.movies.singlemovie.model.review.Results;
import in.roshi.movies.singlemovie.model.review.Review;

/**
 * Created by roshi on 21/06/17.
 */

public class ReviewAdaptor extends RecyclerView.Adapter<ReviewAdaptor.ReviewViewHolder> {

    private final String LOG_KEY = this.getClass().getCanonicalName();
    private Review review;

    public ReviewAdaptor(Review review) {
        this.review = review;
    }

    @Override
    public ReviewViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();
        int layoutIdForListItem = R.layout.reviewclip;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, viewGroup, shouldAttachToParentImmediately);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ReviewViewHolder holder, int position) {
        try{
            holder.bind(review.getResults()[position]);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        if(review != null)
            return review.getResults().length;
        else return 0;
    }


    class ReviewViewHolder extends RecyclerView.ViewHolder {

        TextView reviewText;

        public ReviewViewHolder(View itemView) {
            super(itemView);
            reviewText = (TextView) itemView.findViewById(R.id.tv_review);
        }

        void bind(Results results) {
            try{
                reviewText.setText(results.getContent());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
