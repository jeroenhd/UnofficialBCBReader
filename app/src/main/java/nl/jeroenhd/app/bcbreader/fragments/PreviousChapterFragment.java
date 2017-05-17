package nl.jeroenhd.app.bcbreader.fragments;

import android.os.Bundle;
import android.util.Log;

import nl.jeroenhd.app.bcbreader.activities.FullscreenReaderActivity;
import nl.jeroenhd.app.bcbreader.data.App;
import nl.jeroenhd.app.bcbreader.data.Chapter;

/**
 * A fragment that loads the previous chapter when the user has scrolled past the beginning of the chapter
 */

public class PreviousChapterFragment extends NavigationEventFragment {
    /**
     * Create a new instance of a NavigationEventFragment
     * @param mChapter The current chapter
     * @param callback The callback to use
     * @return A new NavigationEventFragment
     */
    public static PreviousChapterFragment newInstance(Chapter mChapter, NavigationEventCallback callback) {
        PreviousChapterFragment fullscreenPageFragment = new PreviousChapterFragment();
        Bundle args = new Bundle();
        args.putParcelable(FullscreenReaderActivity.EXTRA_CHAPTER, mChapter);
        fullscreenPageFragment.setArguments(args);
        fullscreenPageFragment.callback = callback;
        fullscreenPageFragment.chapter = mChapter;

        return fullscreenPageFragment;
    }

    /**
     * Navigate to the previous chapter
     */
    @Override
    protected void navigateToDifferentChapter() {
        if (callback == null)
        {
            Log.e(App.TAG, "PreviousChapterFragment could not post callback: callback == null!");
        } else {
            Chapter previousChapter = chapter.getPrevious();

            // There might not be a previous chapter in some cases
            if (previousChapter == null)
            {
                callback.onNavigateTo(null);
            } else {
                callback.onNavigateTo(previousChapter);
            }
        }
    }
}
