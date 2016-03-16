package nl.jeroenhd.app.bcbreader;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import nl.jeroenhd.app.bcbreader.data.Page;

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

        int children = holder.imagesLayout.getChildCount();

        BitmapDrawable image;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            image = (BitmapDrawable) mContext.getDrawable(R.drawable.dummy_page);
        } else {
            //noinspection deprecation
            image = (BitmapDrawable)mContext.getResources().getDrawable(R.drawable.dummy_page);
        }


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

        holder.commentaryView.setText(Html.fromHtml(page.getDescription()));
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
