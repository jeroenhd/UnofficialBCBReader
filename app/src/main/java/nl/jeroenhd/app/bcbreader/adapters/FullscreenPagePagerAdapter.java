package nl.jeroenhd.app.bcbreader.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import nl.jeroenhd.app.bcbreader.data.Chapter;
import nl.jeroenhd.app.bcbreader.fragments.FullscreenPageFragment;
import nl.jeroenhd.app.bcbreader.fragments.NavigationEventFragment;
import nl.jeroenhd.app.bcbreader.fragments.NextChapterFragment;
import nl.jeroenhd.app.bcbreader.fragments.PreviousChapterFragment;

/**
 * A pager adapter for the full screen comic reader
 */
public class FullscreenPagePagerAdapter extends FragmentStatePagerAdapter {
    private final Chapter mChapter;
    private final FullscreenPageFragment.FullscreenPageFragmentCallback mPageCallback;
    private final NextChapterFragment.NavigationEventCallback mNavigationCallback;

    public FullscreenPagePagerAdapter(FragmentManager fm, Chapter chapter, FullscreenPageFragment.FullscreenPageFragmentCallback pageFragmentCallback, NavigationEventFragment.NavigationEventCallback navigationEventCallback) {
        super(fm);

        mChapter = chapter;
        mPageCallback = pageFragmentCallback;
        mNavigationCallback = navigationEventCallback;
    }


    @Override
    public Fragment getItem(int position) {
        // Check if this is the first or last page
        if (position == 0 && mChapter.getPrevious() != null) {
            return PreviousChapterFragment.newInstance(mChapter, mNavigationCallback);
        } else if (position == mChapter.getPageCount() && mChapter.getNext() != null) {
            return NextChapterFragment.newInstance(mChapter, mNavigationCallback);
        }
        return FullscreenPageFragment.newInstance(mChapter, position, mPageCallback);
    }

    @Override
    public int getCount() {
        if (mChapter == null)
            return 0;

        int count = mChapter.getPageCount();

        if (mChapter.getNext() != null)
            count++;

        if (mChapter.getPrevious() != null)
            count ++;

        return count;
    }
}
