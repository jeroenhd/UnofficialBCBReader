package nl.jeroenhd.app.bcbreader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.NetworkImageView;

import java.util.ArrayList;
import java.util.List;

import nl.jeroenhd.app.bcbreader.data.API;
import nl.jeroenhd.app.bcbreader.data.Page;
import nl.jeroenhd.app.bcbreader.data.SuperSingleton;
import nl.jeroenhd.app.bcbreader.views.FadingNetworkImageView;
import nl.jeroenhd.app.bcbreader.views.PageImageView;

/**
 * A class implementing a RecyclerView.Adapter for reading a chapter
 */
public class ChapterReadingAdapter extends RecyclerView.Adapter<ChapterReadingAdapter.ViewHolder> {
    private Context mContext;
    private ArrayList<Page> mData;

    public ChapterReadingAdapter(Context context, ArrayList<Page> data)
    {
        mContext = context;
        mData = data;
    }

    @Override
    public ChapterReadingAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        View view = layoutInflater.inflate(R.layout.list_item_page, parent, false);

        return new ViewHolder(view);
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
        public PageImageView networkImageView;
        public TextView commentaryView;

        public ViewHolder(View itemView) {
            super(itemView);

            networkImageView = (PageImageView)itemView.findViewById(R.id.page);
            commentaryView = (TextView)itemView.findViewById(R.id.commentary);
        }
    }
}
