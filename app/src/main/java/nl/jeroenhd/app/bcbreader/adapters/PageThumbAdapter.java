package nl.jeroenhd.app.bcbreader.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import nl.jeroenhd.app.bcbreader.R;
import nl.jeroenhd.app.bcbreader.data.Chapter;
import nl.jeroenhd.app.bcbreader.views.PageThumbImageView;

/**
 * A RecyclerView adapter for page thumbs
 */

public class PageThumbAdapter extends RecyclerView.Adapter<PageThumbAdapter.ViewHolder> {
    private Context mContext;
    private Chapter mChapter;
    private OnThumbClickListener onThumbClickListener;

    public PageThumbAdapter(Context context, Chapter chapter, OnThumbClickListener onThumbClickListener) {
        this.mContext = context;
        this.mChapter = chapter;
        this.onThumbClickListener = onThumbClickListener;
    }

    public void setChapter(Chapter newChapter) {
        if (mChapter != null)
            this.notifyItemRangeRemoved(0, mChapter.getPageCount());

        this.mChapter = newChapter;

        if (mChapter != null)
            this.notifyItemRangeInserted(0, mChapter.getPageCount());
    }

    @NonNull
    @Override
    public PageThumbAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View inflatedView = inflater.inflate(R.layout.content_page_thumb, parent, false);
        return new PageThumbAdapter.ViewHolder(inflatedView, this.onThumbClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull PageThumbAdapter.ViewHolder holder, int position) {
        holder.pageText.setText(String.format(mContext.getString(R.string.page_number), position + 1));
        holder.pageThumbImageView.setPage(mChapter.getNumber(), position + 1);
        holder.page = position + 1;
    }

    @Override
    public int getItemCount() {
        if (mChapter == null)
            return 0;

        return mChapter.getPageCount();
    }

    public interface OnThumbClickListener {
        /**
         * Triggered when a user clicks a thumbnail
         *
         * @param chapter     The chapter this thumb belongs to
         * @param page        The page this thumb belongs to
         * @param clickedView The view clicked, for animation purposes
         */
        void onThumbnailClick(Chapter chapter, int page, View clickedView);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        PageThumbImageView pageThumbImageView;
        TextView pageText;
        private OnThumbClickListener onClick;
        private int page;

        ViewHolder(final View itemView, OnThumbClickListener onThumbClickListener) {
            super(itemView);

            this.pageThumbImageView = itemView.findViewById(R.id.thumb);
            this.pageThumbImageView.setOnClickListener(v -> onClick.onThumbnailClick(mChapter, page, v));
            this.pageText = itemView.findViewById(R.id.text);
            this.onClick = onThumbClickListener;
        }
    }
}
