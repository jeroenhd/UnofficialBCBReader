package nl.jeroenhd.app.bcbreader;

import android.content.Context;
import android.graphics.PorterDuff;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v7.widget.AppCompatImageView;
import android.widget.TextView;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import nl.jeroenhd.app.bcbreader.data.Chapter;

/**
 * A list adapter for the RecyclerView of ChapterListActivity
 */
public class ChapterListAdapter extends RecyclerView.Adapter<ChapterListAdapter.ViewHolder> {
    private Context mContext;
    private ArrayList<Chapter> mData;
    private OnChapterClickListener mOnItemClickListener;

    public ChapterListAdapter(Context context, ArrayList<Chapter> data, OnChapterClickListener onItemClickListener)
    {
        this.mContext = context;
        this.mData = data;
        this.mOnItemClickListener = onItemClickListener;
    }

    @Override
    public ChapterListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View inflatedView = inflater.inflate(R.layout.list_item_chapter, parent, false);

        return new ViewHolder(inflatedView, this.mOnItemClickListener);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Chapter chapter = mData.get(position);
        holder.ChapterThumbView.setImageResource(R.drawable.dummy_chapter_thumb);
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
        public CircleImageView ChapterThumbView;
        public TextView ChapterTitleView;
        public TextView ChapterDescriptionView;
        public AppCompatImageView FavouriteImageView;
        public Chapter Chapter;
        private OnChapterClickListener ClickHandler;

        public ViewHolder(View itemView, OnChapterClickListener onClick) {
            super(itemView);

            itemView.setOnClickListener(this);
            this.ClickHandler = onClick;

            this.ChapterThumbView = (CircleImageView) itemView.findViewById(R.id.thumb);
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
