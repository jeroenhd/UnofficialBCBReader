package nl.jeroenhd.app.bcbreader.adapters;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.raizlabs.android.dbflow.annotation.NotNull;
import com.raizlabs.android.dbflow.sql.language.Method;
import com.raizlabs.android.dbflow.sql.language.Select;

import java.util.ArrayList;

import nl.jeroenhd.app.bcbreader.R;
import nl.jeroenhd.app.bcbreader.data.API;
import nl.jeroenhd.app.bcbreader.data.App;
import nl.jeroenhd.app.bcbreader.data.Chapter;
import nl.jeroenhd.app.bcbreader.data.Chapter_Table;
import nl.jeroenhd.app.bcbreader.data.SuperSingleton;
import nl.jeroenhd.app.bcbreader.tools.CompatHelper;
import nl.jeroenhd.app.bcbreader.views.FadingNetworkImageView;

/**
 * A list adapter for the RecyclerView of ChapterListActivity
 */
public class ChapterListAdapter extends RecyclerView.Adapter<ChapterListAdapter.ViewHolder> {
    private final Context mContext;
    private final ArrayList<Chapter> mData;
    private final OnChapterClickListener mOnItemClickListener;
    private final Drawable clonedFavDrawable;
    private final Drawable clonedFavDrawableBorder;

    public ChapterListAdapter(Context context, ArrayList<Chapter> data, OnChapterClickListener onItemClickListener) {
        this.mContext = context;
        this.mData = data;
        this.mOnItemClickListener = onItemClickListener;

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

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        Chapter chapter = mData.get(position);

        // Clear the old thumb
        holder.ChapterThumbView.setImageDrawable(new ColorDrawable(CompatHelper.getColor(mContext, android.R.color.white)));
        // Download and show the new thumb
        holder.ChapterThumbView.setImageUrl(
                API.FormatChapterThumbURL(mContext, chapter.getNumber()),
                SuperSingleton.getInstance(mContext).getImageLoader()
        );

        holder.ChapterTitleView.setText(chapter.getTitle());
        holder.ChapterDescriptionView.setText(chapter.getDescription());

        holder.CurrentChapter = chapter;
        int color = CompatHelper.getColor(mContext, R.color.colorAccent);
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
        /**
         * Called when a chapter is selected in the list
         *
         * @param view    The view the user has interacted with
         * @param chapter The chapter the user has selected
         * @param page    The page to scroll to (use 1 to start from the beginning, 0 to indicate no page in particular)
         */
        void onChapterSelect(View view, Chapter chapter, int page);
    }

    static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final FadingNetworkImageView ChapterThumbView;
        final TextView ChapterTitleView;
        final TextView ChapterDescriptionView;
        private final OnChapterClickListener ClickHandler;

        @NotNull
        Chapter CurrentChapter;

        ViewHolder(View itemView, OnChapterClickListener onClick) {
            super(itemView);

            itemView.setOnClickListener(this);
            this.ClickHandler = onClick;

            this.ChapterThumbView = (FadingNetworkImageView) itemView.findViewById(R.id.thumb);
            this.ChapterTitleView = (TextView) itemView.findViewById(R.id.title);
            this.ChapterDescriptionView = (TextView) itemView.findViewById(R.id.description);
        }

        @Override
        public void onClick(View v) {
            Chapter updateChapter = new Select(Method.ALL_PROPERTY).from(Chapter.class).where(Chapter_Table.number.eq(CurrentChapter.getNumber())).querySingle();
            if (updateChapter == null)
            {
                Log.d(App.TAG, "ChapterListAdapter::ViewHolder::onClick: refresh of Chapter object failed!");
            } else {
                this.CurrentChapter = updateChapter;
            }
            this.ClickHandler.onChapterSelect(v, this.CurrentChapter, this.CurrentChapter.getLastPageRead());
        }
    }
}

