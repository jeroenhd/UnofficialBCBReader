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
        if (position < 1 && mChapter.getPrevious() != null) {
            Log.d(App.TAG, "[Chapter " + mChapter.getNumber() + ", position " + position + "]: spawning previous chapter fragment");
            return PreviousChapterFragment.newInstance(mChapter, mNavigationCallback);
        } else if (position > mChapter.getPageCount() && mChapter.getNext() != null) {
            Log.d(App.TAG, "[Chapter " + mChapter.getNumber() + ", position " + position + "]: spawning next chapter fragment");
            return NextChapterFragment.newInstance(mChapter, mNavigationCallback);
        }

        int pageNumber = position;
        if (mChapter.getPrevious() == null)
            pageNumber++;

        Log.d(App.TAG, "[Chapter " + mChapter.getNumber() + ", position " + position + "]: spawning page chapter for page " + pageNumber);
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
}
