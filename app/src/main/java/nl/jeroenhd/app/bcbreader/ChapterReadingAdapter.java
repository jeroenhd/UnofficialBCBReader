package nl.jeroenhd.app.bcbreader;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import nl.jeroenhd.app.bcbreader.data.Page;
import nl.jeroenhd.app.bcbreader.views.PageImageView;

/**
 * A class implementing a RecyclerView.Adapter for reading a chapter
 */
public class ChapterReadingAdapter extends RecyclerView.Adapter<ChapterReadingAdapter.ViewHolder> {
    private final Context mContext;
    private final ArrayList<Page> mData;

    public ChapterReadingAdapter(Context context, ArrayList<Page> data) {
        mContext = context;
        mData = data;
    }

    @Override
    public ChapterReadingAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        View view = layoutInflater.inflate(R.layout.list_item_page, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        viewHolder.toolbar.inflateMenu(R.menu.context_menu_chapter);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ChapterReadingAdapter.ViewHolder holder, int position) {
        Page page = mData.get(position);

        Double chapterNumber = page.getChapter();
        Double pageNumber = page.getPage();
        holder.networkImageView.setPage(chapterNumber, pageNumber);

        holder.commentaryView.setText(Html.fromHtml(page.getDescription()));
        holder.commentaryView.setMovementMethod(LinkMovementMethod.getInstance());
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final PageImageView networkImageView;
        public final TextView commentaryView;
        public final Toolbar toolbar;

        public ViewHolder(View itemView) {
            super(itemView);

            networkImageView = (PageImageView) itemView.findViewById(R.id.page);
            commentaryView = (TextView) itemView.findViewById(R.id.commentary);
            toolbar = (Toolbar)itemView.findViewById(R.id.cardToolbar);
        }
    }
}
