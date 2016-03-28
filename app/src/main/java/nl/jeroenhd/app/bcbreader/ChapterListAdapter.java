package nl.jeroenhd.app.bcbreader;

import android.content.Context;
import android.graphics.PorterDuff;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v7.widget.AppCompatImageView;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;

import java.util.ArrayList;

import nl.jeroenhd.app.bcbreader.data.API;
import nl.jeroenhd.app.bcbreader.data.Chapter;
import nl.jeroenhd.app.bcbreader.data.SuperSingleton;

/**
 * A list adapter for the RecyclerView of ChapterListActivity
 */
public class ChapterListAdapter extends RecyclerView.Adapter<ChapterListAdapter.ViewHolder> {
    private final Context mContext;
    private final ArrayList<Chapter> mData;
    private final OnChapterClickListener mOnItemClickListener;
    private final SuperSingleton singleton;

    public ChapterListAdapter(Context context, ArrayList<Chapter> data, OnChapterClickListener onItemClickListener)
    {
        this.mContext = context;
        this.mData = data;
        this.mOnItemClickListener = onItemClickListener;

        this.singleton = SuperSingleton.getInstance(context);
    }

    @Override
    public ChapterListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View inflatedView = inflater.inflate(R.layout.list_item_chapter, parent, false);

        return new ViewHolder(inflatedView, this.mOnItemClickListener);
    }

    private void DownloadImageToImageView(final String URL, final ViewHolder holder)
    {
        ImageLoader imageLoader = singleton.getImageLoader();
        imageLoader.get(URL, ImageLoader.getImageListener(
                holder.ChapterThumbView,
                R.color.colorAccent,
                R.drawable.chapter_error
        ));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        Chapter chapter = mData.get(position);
        DownloadImageToImageView(API.FormatChapterThumbURL(chapter.getNumber()),holder);

        holder.ChapterTitleView.setText(chapter.getTitle());
        holder.ChapterDescriptionView.setText(chapter.getDescription());
        holder.FavouriteImageView.setImageResource(
                position % 2 == 0 ?
                        R.drawable.ic_favorite_border_white_48dp : R.drawable.ic_favorite_white_48dp
        );
        holder.Chapter = chapter;
        int color;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            color = mContext.getColor(R.color.colorAccent);
        } else {
            //noinspection deprecation
            color = mContext.getResources().getColor(R.color.colorAccent);
        }
        holder.FavouriteImageView.getDrawable().setColorFilter(color, PorterDuff.Mode.SRC_IN);
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final ImageView ChapterThumbView;
        public final TextView ChapterTitleView;
        public final TextView ChapterDescriptionView;
        public final AppCompatImageView FavouriteImageView;
        public Chapter Chapter;
        private final OnChapterClickListener ClickHandler;

        public ViewHolder(View itemView, OnChapterClickListener onClick) {
            super(itemView);

            itemView.setOnClickListener(this);
            this.ClickHandler = onClick;

            this.ChapterThumbView = (ImageView) itemView.findViewById(R.id.thumb);
            this.ChapterTitleView = (TextView) itemView.findViewById(R.id.title);
            this.ChapterDescriptionView = (TextView)itemView.findViewById(R.id.description);
            this.FavouriteImageView = (AppCompatImageView) itemView.findViewById(R.id.favourite);
        }

        @Override
        public void onClick(View v) {
            this.ClickHandler.onChapterSelect(v, this.Chapter);
        }
    }

    public interface OnChapterClickListener
    {
        void onChapterSelect(View v, Chapter c);
    }
}
