package nl.jeroenhd.app.bcbreader;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;

import nl.jeroenhd.app.bcbreader.data.Chapter;

/**
 * A full screen comic reader activity
 * TODO: It might be possible to just use the page sum of all chapters as a total amount of elements in the ViewPager
 */
public class FullscreenReaderActivity extends AppCompatActivity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {
    public static final String EXTRA_CHAPTER = "nl.jeroenhd.app.bcbreader.ChapterListActivity.EXTRA_CHAPTER";
    public static final String EXTRA_PAGE = "nl.jeroenhd.app.bcbreader.ChapterListActivity.EXTRA_PAGE";
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;
    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;
    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    Button buttonPrev, buttonNext;
    SeekBar seekBar;
    private View mContentView;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };
    private Chapter currentChapter;
    private ViewPager viewPager;
    private FullscreenPagePagerAdapter mPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fullscreen_reader);

        mVisible = true;
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        mContentView = findViewById(R.id.fullscreen_content);

        // Set up the user interaction to manually show or hide the system UI.
        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });

        buttonNext = (Button) findViewById(R.id.button_right);
        buttonPrev = (Button) findViewById(R.id.button_left);
        seekBar = (SeekBar) findViewById(R.id.seekbar);

        assert buttonPrev != null;
        assert buttonNext != null;
        assert seekBar != null;

        buttonNext.setOnTouchListener(mDelayHideTouchListener);
        buttonPrev.setOnTouchListener(mDelayHideTouchListener);
        seekBar.setOnTouchListener(mDelayHideTouchListener);

        buttonPrev.setOnClickListener(this);
        buttonNext.setOnClickListener(this);
        seekBar.setOnSeekBarChangeListener(this);

        Bundle extras = this.getIntent().getExtras();
        int currentPage;
        if (extras != null)
        {
            if (!extras.containsKey(EXTRA_CHAPTER) || !extras.containsKey(EXTRA_PAGE))
            {
                throw new IllegalArgumentException("Missing argument (CHAPTER or PAGE_NUMBER)");
            }

            currentChapter = extras.getParcelable(EXTRA_CHAPTER);
            if (currentChapter == null)
            {
                Log.e("FullScreenReader", "Activity started without a valid chapter in the extras");
                throw new IllegalArgumentException("Provide a chapter to display!");
            }
            currentPage = extras.getInt(EXTRA_PAGE);

            if (currentPage >= currentChapter.getPageCount())
            {
                // Page number is too high
                throw new IllegalArgumentException("The page number is higher than the page count of this chapter!");
            }
        } else {
            // No extra's?
            Log.e("FullScreenReader", "Activity started without any extras");
            throw new IllegalArgumentException("Provide a chapter to display!");
        }
        seekBar.setMax(currentChapter.getPageCount());
        seekBar.setProgress(currentPage);
        viewPager.setCurrentItem(currentPage);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mControlsView.setVisibility(View.GONE);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    @Override
    public void onClick(View v) {
        if (v.equals(buttonNext))
            this.onNext(v);
        if (v.equals(buttonPrev))
            this.onPrev(v);
    }

    /**
     * Handle the request to show the previous page
     * @param clickedView The button that was clicked to start this action
     */
    public void onNext(View clickedView)
    {
        //TODO: Maybe allow loading the next chapter?
        int currentItem = viewPager.getCurrentItem();
        if (currentItem < currentChapter.getPageDescriptions().size() - 1)
        {
            viewPager.setCurrentItem(currentItem + 1);
        } else {
            viewPager.setCurrentItem(0);
        }
    }

    /**
     * Handle the request to show the previous page
     * @param clickedView The button that was clicked to start this action
     */
    public void onPrev(View clickedView)
    {
        //TODO: Maybe allow loading the previous chapter?
        int currentItem = viewPager.getCurrentItem();
        if (currentItem >= 1){
            viewPager.setCurrentItem(currentItem - 1);
        } else {
            viewPager.setCurrentItem(currentChapter.getPageCount() - 1);
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser)
        {
            viewPager.setCurrentItem(progress);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
