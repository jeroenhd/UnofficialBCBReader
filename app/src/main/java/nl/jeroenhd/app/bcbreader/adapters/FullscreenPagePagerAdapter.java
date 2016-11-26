package nl.jeroenhd.app.bcbreader.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import nl.jeroenhd.app.bcbreader.data.Chapter;
import nl.jeroenhd.app.bcbreader.fragments.FullscreenPageFragment;

/**
 * A pager adapter for the full screen comic reader
 * TODO: Implement
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
