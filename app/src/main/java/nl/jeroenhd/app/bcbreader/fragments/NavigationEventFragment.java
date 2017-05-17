package nl.jeroenhd.app.bcbreader.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import nl.jeroenhd.app.bcbreader.R;
import nl.jeroenhd.app.bcbreader.activities.FullscreenReaderActivity;
import nl.jeroenhd.app.bcbreader.data.App;
import nl.jeroenhd.app.bcbreader.data.Chapter;

/**
 * A class to navigate to the next or previous chapter after the user has scrolled past the last page
 * in a chapter.
 */

public class NavigationEventFragment extends Fragment {
    protected NavigationEventCallback callback;
    protected Chapter chapter;

    /**
     * Create a new instance of a NavigationEventFragment
     * @param mChapter The current chapter
     * @param callback The callback to use
     * @return A new NavigationEventFragment
     */
    public static NavigationEventFragment newInstance(Chapter mChapter, NavigationEventCallback callback) {
        NavigationEventFragment fullscreenPageFragment = new NavigationEventFragment();
        Bundle args = new Bundle();
        args.putParcelable(FullscreenReaderActivity.EXTRA_CHAPTER, mChapter);
        fullscreenPageFragment.setArguments(args);
        fullscreenPageFragment.callback = callback;
        fullscreenPageFragment.chapter = mChapter;

        return fullscreenPageFragment;
    }

    /**
     * Navigate to the next chapter
     * Do not use this method from NavigationEventFragment; use one of its subclasses
     */
    protected void navigateToDifferentChapter()
    {
        Log.e(App.TAG, "Error: NavigationEventFragment.navigateToDifferentChapter doesn't do anything! Please use the appropriate subclass.");
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (isVisibleToUser)
            navigateToDifferentChapter();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_page_navigational_fragment, container, false);

        TextView textView = (TextView) v.findViewById(R.id.text);
        textView.setText(this.getNavigationCaption());
        return v;
    }

    public String getNavigationCaption() {
        Log.e(App.TAG, "Error: NavigationEventFragment.getNavigationCaption doesn't do anything! Please use the appropriate subclass.");
        return "";
    }

    /**
     * An interface to communicate navigation events to the parent activity
     */
    public interface NavigationEventCallback
    {
        /**
         * Called when the parent activity should navigate to another chapter
         * @param chapter The chapter to navigate to
         */
        void onNavigateTo(Chapter chapter);
    }
}
