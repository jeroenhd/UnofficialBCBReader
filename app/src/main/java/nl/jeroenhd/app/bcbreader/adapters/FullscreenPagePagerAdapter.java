package nl.jeroenhd.app.bcbreader.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;

import nl.jeroenhd.app.bcbreader.data.App;
import nl.jeroenhd.app.bcbreader.data.Chapter;
import nl.jeroenhd.app.bcbreader.fragments.FullscreenPageFragment;
import nl.jeroenhd.app.bcbreader.fragments.NavigationEventFragment;
import nl.jeroenhd.app.bcbreader.fragments.NextChapterFragment;
import nl.jeroenhd.app.bcbreader.fragments.PreviousChapterFragment;

/**
 * A pager adapter for the full screen comic reader
 */
public class FullscreenPagePagerAdapter extends FragmentStatePagerAdapter {
    private final FullscreenPageFragment.FullscreenPageFragmentCallback mPageCallback;
    private final NextChapterFragment.NavigationEventCallback mNavigationCallback;
    private Chapter mChapter;

    public FullscreenPagePagerAdapter(FragmentManager fm, Chapter chapter, FullscreenPageFragment.FullscreenPageFragmentCallback pageFragmentCallback, NavigationEventFragment.NavigationEventCallback navigationEventCallback) {
        super(fm);

        mChapter = chapter;
        mPageCallback = pageFragmentCallback;
        mNavigationCallback = navigationEventCallback;
    }


    @Override
    public Fragment getItem(int position) {
        // Check if this is the first or last page
        boolean hasPrevious = mChapter.getPrevious() != null;
        boolean hasNext = mChapter.getNext() != null;

        if (position == 0 && hasPrevious) {
            return PreviousChapterFragment.newInstance(mChapter, mNavigationCallback);
        } else if (position > mChapter.getPageCount() + (hasPrevious ? 1 : 0) && hasNext) {
            Log.d(App.TAG, "[Chapter " + mChapter.getNumber() + ", position " + position + "]: spawning next chapter fragment");
            return NextChapterFragment.newInstance(mChapter, mNavigationCallback);
        }

        int pageNumber = position;
        if (mChapter.getPrevious() == null)
            pageNumber++;
        return FullscreenPageFragment.newInstance(mChapter, pageNumber, mPageCallback);
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

    public void setChapter(Chapter currentChapter) {
        this.mChapter = currentChapter;
    }
}
