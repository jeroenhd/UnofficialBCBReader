package nl.jeroenhd.app.bcbreader;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;

import nl.jeroenhd.app.bcbreader.data.API;
import nl.jeroenhd.app.bcbreader.data.Chapter;
import nl.jeroenhd.app.bcbreader.data.SuperSingleton;
import nl.jeroenhd.app.bcbreader.views.PageImageView;

/**
 * A fragment to show a single page
 */
public class FullScreenPageFragment extends Fragment {
    PageImageView imageView;
    private Chapter chapter;
    private int page;
    private FullscreenPageFragmentCallback callback;
    private Context mContext;

    public static FullScreenPageFragment newInstance(Chapter mChapter, int page, FullscreenPageFragmentCallback callback)
    {
        FullScreenPageFragment fullScreenPageFragment = new FullScreenPageFragment();
        Bundle args = new Bundle();
        args.putParcelable(FullscreenReaderActivity.EXTRA_CHAPTER, mChapter);
        args.putInt(FullscreenReaderActivity.EXTRA_PAGE, page);
        fullScreenPageFragment.setArguments(args);
        fullScreenPageFragment.callback = callback;

        return fullScreenPageFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null)
        {
            chapter = getArguments().getParcelable(FullscreenReaderActivity.EXTRA_CHAPTER);
            page = getArguments().getInt(FullscreenReaderActivity.EXTRA_PAGE);
        } else {
            Log.e("FullScreenPage", "Pass me some arguments!!!");
            throw new IllegalArgumentException("FullScreenPageFragment requires arguments!");
        }
    }

    /**
     * Called when a fragment is first attached to its context.
     * {@link #onCreate(Bundle)} will be called after this.
     *
     * @param context
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_page_fullscreen, container, false);

        assert v!=null;
        imageView = (PageImageView) v.findViewById(R.id.page);
        imageView.setPage(chapter.getNumber(), page);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.onTap(v);
            }
        });

        PreloadNext();

        return v;
    }

    /**
     * Preload up to 5 pages forward and back
     */
    private void PreloadNext() {

        // The default value for the preferences is true
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        if (!prefs.getBoolean("reading_enable_preloading", true)) {
            return;
        }
        if (chapter.getPageCount() > page) {
            ImageLoader.ImageListener dummyListener = new ImageLoader.ImageListener() {
                @Override
                public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                    //Empty
                }

                @Override
                public void onErrorResponse(VolleyError error) {
                    //Empty
                }
            };

            int minPage = page - 5;
            int maxPage = page + 5;
            if (minPage < 0)
                minPage = 0;
            if (maxPage > chapter.getPageCount())
                maxPage = chapter.getPageCount();


            // Forward, then back
            for (int i = page; i <= maxPage; i++) {
                String nextPageURL = API.FormatPageUrl(chapter.getNumber(), page + i, API.getQualitySuffix(mContext));
                SuperSingleton.getInstance(mContext).getImageLoader().get(nextPageURL, dummyListener);
            }

            for (int i = page; i >= minPage; i--) {
                String nextPageURL = API.FormatPageUrl(chapter.getNumber(), page + i, API.getQualitySuffix(mContext));
                SuperSingleton.getInstance(mContext).getImageLoader().get(nextPageURL, dummyListener);
            }
        }
    }

    public interface FullscreenPageFragmentCallback
    {
        void onTap(View view);
    }
}
