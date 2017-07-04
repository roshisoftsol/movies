package in.roshi.movies.singlemovie.adaptor;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import in.roshi.movies.R;
import in.roshi.movies.singlemovie.model.videos.Results;
import in.roshi.movies.singlemovie.model.videos.Videos;

/**
 * Created by roshi on 21/06/17.
 */

public class VideoAdaptor extends RecyclerView.Adapter<VideoAdaptor.VideoViewHolder> {

    private final String LOG_KEY = this.getClass().getCanonicalName();
    private Videos videos;
    final private ListItemClickListener mOnClickListener;

    public interface ListItemClickListener {
        void onListItemClick(String clickedItemIndex);
    }

    public VideoAdaptor(Videos videos, ListItemClickListener listener) {
        mOnClickListener = listener;
        this.videos = videos;
    }

    @Override
    public VideoViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();
        int layoutIdForListItem = R.layout.videoclip;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, viewGroup, shouldAttachToParentImmediately);
        return new VideoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(VideoViewHolder holder, int position) {
        try{
            holder.bind(videos.getResults()[position]);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        if (videos != null)
            return videos.getResults().length;
        else return 0;
    }


    class VideoViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        TextView videoTitle;
        TextView videoID;
        ImageButton imageButton;

        public VideoViewHolder(View itemView) {
            super(itemView);
            videoTitle = (TextView) itemView.findViewById(R.id.tv_video_title);
            videoID = (TextView) itemView.findViewById(R.id.tv_video_id);
            imageButton = (ImageButton) itemView.findViewById(R.id.imageButton);
            imageButton.setOnClickListener(this);
        }

        void bind(Results results) {
            try{
                videoTitle.setText(results.getName());
                videoID.setText(results.getKey());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onClick(View v) {
            mOnClickListener.onListItemClick(videoID.getText().toString());
        }
    }
}
