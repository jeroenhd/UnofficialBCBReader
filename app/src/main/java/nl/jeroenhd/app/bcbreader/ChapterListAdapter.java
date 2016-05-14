package nl.jeroenhd.app.bcbreader;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
    private final Drawable clonedFavDrawable;
    private final Drawable clonedFavDrawableBorder;

    public ChapterListAdapter(Context context, ArrayList<Chapter> data, OnChapterClickListener onItemClickListener) {
        this.mContext = context;
        this.mData = data;
        this.mOnItemClickListener = onItemClickListener;

        this.singleton = SuperSingleton.getInstance(context);

        // Clone drawables and cache them
        // Without cloning, setting the tint on them causes the drawable to have the tint
        //  in other parts of the app as well
        clonedFavDrawable = cloneDrawable(R.drawable.ic_favorite_white_48dp);
        clonedFavDrawableBorder = cloneDrawable(R.drawable.ic_favorite_border_white);
    }

    @Override
    public ChapterListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View inflatedView = inflater.inflate(R.layout.list_item_chapter, parent, false);

        return new ViewHolder(inflatedView, this.mOnItemClickListener);
    }

    private void DownloadImageToImageView(final String URL, final ViewHolder holder) {
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
        DownloadImageToImageView(API.FormatChapterThumbURL(chapter.getNumber()), holder);

        holder.ChapterTitleView.setText(chapter.getTitle());
        holder.ChapterDescriptionView.setText(chapter.getDescription());

        holder.FavouriteImageView.setImageDrawable(chapter.isFavourite() ? clonedFavDrawable : clonedFavDrawableBorder);

        holder.CurrentChapter = chapter;
        int color;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            color = mContext.getColor(R.color.colorAccent);
        } else {
            //noinspection deprecation
            color = mContext.getResources().getColor(R.color.colorAccent);
        }
        holder.FavouriteImageView.getDrawable().setColorFilter(color, PorterDuff.Mode.SRC_IN);
    }

    /**
     * Clone a drawable. Useful for tinting drawables
     *
     * @param resId The resource to clone
     * @return A cloned drawable that can be edited without causing side effects in other classes
     */
    private Drawable cloneDrawable(int resId) {
        Drawable drawable;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            drawable = mContext.getDrawable(resId);
        } else {
            //noinspection deprecation
            drawable = mContext.getResources().getDrawable(resId);
        }

        assert drawable != null;

        return drawable.mutate();
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    public interface OnChapterClickListener {
        void onChapterSelect(View v, Chapter c);

        void onChapterFavourite(AppCompatImageView v, Chapter c);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final ImageView ChapterThumbView;
        public final TextView ChapterTitleView;
        public final TextView ChapterDescriptionView;
        public final AppCompatImageView FavouriteImageView;
        private final OnChapterClickListener ClickHandler;
        public Chapter CurrentChapter;

        public ViewHolder(View itemView, OnChapterClickListener onClick) {
            super(itemView);

            itemView.setOnClickListener(this);
            this.ClickHandler = onClick;

            this.ChapterThumbView = (ImageView) itemView.findViewById(R.id.thumb);
            this.ChapterTitleView = (TextView) itemView.findViewById(R.id.title);
            this.ChapterDescriptionView = (TextView) itemView.findViewById(R.id.description);
            this.FavouriteImageView = (AppCompatImageView) itemView.findViewById(R.id.favourite);
            this.FavouriteImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ClickHandler.onChapterFavourite(FavouriteImageView, CurrentChapter);
                }
            });
        }

        @Override
        public void onClick(View v) {
            this.ClickHandler.onChapterSelect(v, this.CurrentChapter);
        }
    }
}
