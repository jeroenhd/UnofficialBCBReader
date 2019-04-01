package nl.jeroenhd.app.bcbreader.adapters;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import nl.jeroenhd.app.bcbreader.data.Chapter;
import nl.jeroenhd.app.bcbreader.fragments.FullscreenPageFragment;

/**
 * A pager adapter for the full screen comic reader
 */
public class FullscreenPagePagerAdapter extends FragmentStatePagerAdapter {
    private final Chapter mChapter;
    private final FullscreenPageFragment.FullscreenPageFragmentCallback mPageCallback;

    public FullscreenPagePagerAdapter(FragmentManager fm, Chapter chapter, FullscreenPageFragment.FullscreenPageFragmentCallback callback) {
        super(fm);

        mChapter = chapter;
        mPageCallback = callback;
    }

    @Override
    public Fragment getItem(int position) {
        // Pages are 1-based, while positions are 0-based!
        return FullscreenPageFragment.newInstance(mChapter, position + 1, mPageCallback);
    }

    @Override
    public int getCount() {
        return mChapter == null ? 0 : mChapter.getPageCount();
    }
}
