package nl.jeroenhd.app.bcbreader;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
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

import nl.jeroenhd.app.bcbreader.data.API;
import nl.jeroenhd.app.bcbreader.data.Page;
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

        final Double chapterNumber = page.getChapter();
        final Double pageNumber = page.getPage();
        holder.networkImageView.setPage(chapterNumber, pageNumber);

        holder.commentaryView.setText(Html.fromHtml(page.getDescription()));
        holder.commentaryView.setMovementMethod(LinkMovementMethod.getInstance());

        holder.toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
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
                                new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Toast.makeText(mContext, String.format(Locale.getDefault(), mContext.getString(R.string.error_while_sharing), error.getLocalizedMessage()), Toast.LENGTH_LONG).show();
                            }
                        });
                        break;
                }

                return true;
            }
        });
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
            toolbar = (Toolbar) itemView.findViewById(R.id.cardToolbar);
        }
    }
}
