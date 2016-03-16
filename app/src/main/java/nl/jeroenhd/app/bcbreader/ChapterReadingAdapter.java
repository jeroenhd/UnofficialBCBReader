package nl.jeroenhd.app.bcbreader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
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

import java.util.ArrayList;
import java.util.List;

import nl.jeroenhd.app.bcbreader.data.API;
import nl.jeroenhd.app.bcbreader.data.Page;
import nl.jeroenhd.app.bcbreader.data.SuperSingleton;

/**
 * A class implementing a RecyclerView.Adapter for reading a chapter
 */
public class ChapterReadingAdapter extends RecyclerView.Adapter<ChapterReadingAdapter.ViewHolder> {
    private Context mContext;
    private ArrayList<Page> mData;
    private SuperSingleton singleton;

    public ChapterReadingAdapter(Context context, ArrayList<Page> data)
    {
        mContext = context;
        mData = data;

        singleton = SuperSingleton.getInstance(mContext);
    }

    @Override
    public ChapterReadingAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        View view = layoutInflater.inflate(R.layout.list_item_page, parent, false);

        return new ViewHolder(view);
    }

    private void ApplyImage(ChapterReadingAdapter.ViewHolder holder, BitmapDrawable image)
    {
        int children = holder.imagesLayout.getChildCount();
        List<BitmapDrawable> segments = splitBitmap(image);
        int childrenRequired = segments.size();

        // Make the amount of children required equal to the amount of items already in the LinearLayout
        if (childrenRequired < children)
        {
            while (children < childrenRequired)
            {
                holder.imagesLayout.addView(generateImageView());
            }
        } else {
            while(children > childrenRequired)
            {
                holder.imagesLayout.removeView(holder.imagesLayout.getChildAt(0));
            }
        }

        for (int i = 0; i < childrenRequired; i++)
        {
            ((ImageView)(holder.imagesLayout.getChildAt(i))).setImageDrawable(segments.get(i));
        }
    }

    @Override
    public void onBindViewHolder(ChapterReadingAdapter.ViewHolder holder, int position) {
        Page page = mData.get(position);

        BitmapDrawable image;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            image = (BitmapDrawable) mContext.getDrawable(R.drawable.dummy_page);
        } else {
            //noinspection deprecation
            image = (BitmapDrawable)mContext.getResources().getDrawable(R.drawable.dummy_page);
        }

        ApplyImage(holder, image);
        //StartLoading(API.FormatPageUrl(page.getChapter(), page.getPage()), holder);
        holder.commentaryView.setText(Html.fromHtml(page.getDescription()));
    }

    private void StartLoading(String url, ChapterReadingAdapter.ViewHolder holder) {
        //ImageLoader img = ImageLoader.getImageListener();
        /*img.get(url, new ImageLoader.ImageListener() {
            @Override
            public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {

            }

            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });*/
    }

    List<BitmapDrawable> splitBitmap(BitmapDrawable bitmap)
    {
        ArrayList<BitmapDrawable> list = new ArrayList<>();
        list.add(bitmap);

        return list;
    }

    ImageView generateImageView()
    {
        ImageView imageView = new ImageView(mContext);
        ViewGroup.LayoutParams layoutParams = imageView.getLayoutParams();
        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        imageView.setAdjustViewBounds(true);

        return imageView;
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public LinearLayout imagesLayout;
        public TextView commentaryView;

        public ViewHolder(View itemView) {
            super(itemView);

            imagesLayout = (LinearLayout)itemView.findViewById(R.id.imageSegments);
            commentaryView = (TextView)itemView.findViewById(R.id.commentary);
        }
    }
}
