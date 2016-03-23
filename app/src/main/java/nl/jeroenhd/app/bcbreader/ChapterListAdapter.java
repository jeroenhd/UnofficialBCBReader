package nl.jeroenhd.app.bcbreader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v7.widget.AppCompatImageView;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageRequest;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import de.hdodenhof.circleimageview.CircleImageView;
import nl.jeroenhd.app.bcbreader.data.API;
import nl.jeroenhd.app.bcbreader.data.Chapter;
import nl.jeroenhd.app.bcbreader.data.SuperSingleton;

/**
 * A list adapter for the RecyclerView of ChapterListActivity
 */
public class ChapterListAdapter extends RecyclerView.Adapter<ChapterListAdapter.ViewHolder> {
    private Context mContext;
    private ArrayList<Chapter> mData;
    private OnChapterClickListener mOnItemClickListener;
    private SuperSingleton singleton;

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

    public int dpToPx(int dp) {
        DisplayMetrics displayMetrics = mContext.getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    void DownloadImageToImageView(final String URL, final ViewHolder holder)
    {
        /*int width = dpToPx(holder.ChapterThumbView.getWidth());
        int height = dpToPx(holder.ChapterThumbView.getHeight());*/

        ImageLoader imageLoader = singleton.getImageLoader();
        imageLoader.get(URL, ImageLoader.getImageListener(
                holder.ChapterThumbView,
                R.color.colorAccent/*R.drawable.dummy_chapter_thumb*/,
                R.drawable.chapter_error
        ));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        Chapter chapter = mData.get(position);
        //holder.ChapterThumbView.setImageResource(R.drawable.dummy_chapter_thumb);
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
