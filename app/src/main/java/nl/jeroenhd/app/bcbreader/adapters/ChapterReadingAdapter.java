package nl.jeroenhd.app.bcbreader.adapters;

import android.content.Context;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import java.util.ArrayList;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;
import nl.jeroenhd.app.bcbreader.R;
import nl.jeroenhd.app.bcbreader.data.API;
import nl.jeroenhd.app.bcbreader.data.Page;
import nl.jeroenhd.app.bcbreader.tools.CompatHelper;
import nl.jeroenhd.app.bcbreader.tools.ShareManager;
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

    @NonNull
    @Override
    public ChapterReadingAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        View view = layoutInflater.inflate(R.layout.list_item_page, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        viewHolder.toolbar.inflateMenu(R.menu.context_menu_chapter);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ChapterReadingAdapter.ViewHolder holder, int position) {
        Page page = mData.get(position);

        final Double chapterNumber = page.getChapter();
        final Double pageNumber = page.getPage();
        holder.networkImageView.setPage(chapterNumber, pageNumber);

        holder.commentaryView.setText(CompatHelper.fromHtml(page.getDescription()));
        holder.commentaryView.setMovementMethod(LinkMovementMethod.getInstance());

        holder.toolbar.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.action_share:
                    // The full page URL is required for saving the bitmap and sharing it
                    String pageUrl = API.FormatPageUrl(mContext, chapterNumber, pageNumber, API.getQualitySuffix(mContext));
                    // The short one is to add to the share message
                    String shortPageUrl = API.FormatPageLink(chapterNumber, pageNumber.longValue());

                    ShareManager.ShareImageWithText(mContext,
                            pageUrl,
                            ShareManager.getStupidPhrase(mContext) + " " + shortPageUrl,
                            mContext.getString(R.string.share),
                            error -> Toast.makeText(mContext,
                                                    String.format(Locale.getDefault(),
                                                    mContext.getString(R.string.error_while_sharing),
                                                    error.getLocalizedMessage()),
                                                    Toast.LENGTH_LONG)
                                    .show());
                    break;
            }

            return true;
        });
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        public final Toolbar toolbar;
        final PageImageView networkImageView;
        final TextView commentaryView;

        ViewHolder(View itemView) {
            super(itemView);

            networkImageView = itemView.findViewById(R.id.page);
            commentaryView = itemView.findViewById(R.id.commentary);
            toolbar = itemView.findViewById(R.id.cardToolbar);
        }
    }
}
