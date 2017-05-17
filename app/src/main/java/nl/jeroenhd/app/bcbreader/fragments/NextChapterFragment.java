package nl.jeroenhd.app.bcbreader.fragments;

import android.os.Bundle;
import android.util.Log;

import nl.jeroenhd.app.bcbreader.R;
import nl.jeroenhd.app.bcbreader.activities.FullscreenReaderActivity;
import nl.jeroenhd.app.bcbreader.data.App;
import nl.jeroenhd.app.bcbreader.data.Chapter;

/**
 * A fragment that loads the next chapter when the user has scrolled past the beginning of the chapter
 */
public class NextChapterFragment extends NavigationEventFragment {
    /**
     * Create a new instance of a NavigationEventFragment
     * @param mChapter The current chapter
     * @param callback The callback to use
     * @return A new NavigationEventFragment
     */
    public static NextChapterFragment newInstance(Chapter mChapter, NavigationEventCallback callback) {
        NextChapterFragment fullscreenPageFragment = new NextChapterFragment();
        Bundle args = new Bundle();
        args.putParcelable(FullscreenReaderActivity.EXTRA_CHAPTER, mChapter);
        fullscreenPageFragment.setArguments(args);
        fullscreenPageFragment.callback = callback;
        fullscreenPageFragment.chapter = mChapter;

        return fullscreenPageFragment;
    }

    /**
     * Navigate to the next chapter
     */
    @Override
    protected void navigateToDifferentChapter() {
        if (callback == null)
        {
            Log.e(App.TAG, "NextChapterFragment could not post callback: callback == null!");
        } else {
            Chapter nextChapter = chapter.getNext();

            // There might not be a previous chapter in some cases
            if (nextChapter == null)
            {
                callback.onNavigateTo(null);
            } else {
                callback.onNavigateTo(nextChapter);
            }
        }
    }

    @Override
    public String getNavigationCaption() {
        Chapter nextChapter = chapter.getNext();

        if (nextChapter == null)
            return getString(R.string.no_next_chapter);

        return getString(R.string.to_next_chapter);
    }
}
