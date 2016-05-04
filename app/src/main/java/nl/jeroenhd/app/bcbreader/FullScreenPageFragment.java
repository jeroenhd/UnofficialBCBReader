package nl.jeroenhd.app.bcbreader;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import nl.jeroenhd.app.bcbreader.data.Chapter;
import nl.jeroenhd.app.bcbreader.views.PageImageView;

/**
 * A fragment to show a single page
 */
public class FullScreenPageFragment extends Fragment {
    PageImageView imageView;
    private Chapter chapter;
    private int page;

    public static FullScreenPageFragment newInstance(Chapter mChapter, int page)
    {
        FullScreenPageFragment fullScreenPageFragment = new FullScreenPageFragment();
        Bundle args = new Bundle();
        args.putParcelable(FullscreenReaderActivity.EXTRA_CHAPTER, mChapter);
        args.putInt(FullscreenReaderActivity.EXTRA_PAGE, page);
        fullScreenPageFragment.setArguments(args);

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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_page_fullscreen, container, false);

        assert v!=null;
        imageView = (PageImageView) v.findViewById(R.id.page);

        imageView.setPage(chapter.getNumber(), page);

        return v;
    }
}
