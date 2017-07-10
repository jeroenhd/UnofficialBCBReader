package nl.jeroenhd.app.bcbreader.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.raizlabs.android.dbflow.sql.language.Select;

import java.util.List;
import java.util.Locale;

import nl.jeroenhd.app.bcbreader.BCBReaderApplication;
import nl.jeroenhd.app.bcbreader.R;
import nl.jeroenhd.app.bcbreader.adapters.FullscreenPagePagerAdapter;
import nl.jeroenhd.app.bcbreader.data.API;
import nl.jeroenhd.app.bcbreader.data.App;
import nl.jeroenhd.app.bcbreader.data.Chapter;
import nl.jeroenhd.app.bcbreader.data.Chapter_Table;
import nl.jeroenhd.app.bcbreader.data.check.DataPreferences;
import nl.jeroenhd.app.bcbreader.fragments.FullscreenPageFragment;
import nl.jeroenhd.app.bcbreader.tools.CompatHelper;
import nl.jeroenhd.app.bcbreader.tools.ShareManager;

import static android.view.View.GONE;

/**
 * A full screen comic reader activity
 */
public class FullscreenReaderActivity extends AppCompatActivity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener, FullscreenPageFragment.FullscreenPageFragmentCallback, ViewPager.OnPageChangeListener {
    public static final String EXTRA_CHAPTER = "nl.jeroenhd.app.bcbreader.activities.ChapterListActivity.EXTRA_CHAPTER";
    public static final String EXTRA_PAGE = "nl.jeroenhd.app.bcbreader.activities.ChapterListActivity.EXTRA_PAGE";
    public static final String JUST_SHOW_LATEST = "nl.jeroenhd.app.bcbreader.activities.ChapterListActivity.JUST_SHOW_LATEST";
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
    private final FullscreenReaderActivity thisActivity = this;
    private final Handler mHideHandler = new Handler();
    private Button buttonPrev;
    private Button buttonNext;
    private SeekBar seekBar;
    private View mContentView;
    private Toolbar toolbar;
    private boolean mVisible;
    private Chapter currentChapter;
    private ViewPager viewPager;
    private boolean firstToolbarShow = true;
    private TextView commentaryView;
    private BottomSheetBehavior bottomSheetBehavior;
    private NestedScrollView commentaryScroller;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            commentaryScroller.setVisibility(GONE);

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
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring bottomSheetBehavior of controls going away
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fullscreen_reader);

        mVisible = true;
        //mContentView = findViewById(R.id.fullscreen_content);
        mContentView = findViewById(R.id.pager);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setVisibility(View.VISIBLE);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //NavUtils.navigateUpFromSameTask(FullscreenReaderActivity.this);
                thisActivity.onBackPressed();
            }
        });

        buttonNext = (Button) findViewById(R.id.button_right);
        buttonPrev = (Button) findViewById(R.id.button_left);
        seekBar = (SeekBar) findViewById(R.id.seekbar);
        viewPager = (ViewPager) mContentView;

        commentaryView = (TextView) findViewById(R.id.commentary);
        // Make links in commentary work
        commentaryView.setMovementMethod(LinkMovementMethod.getInstance());

        commentaryScroller = (NestedScrollView) findViewById(R.id.bottom_sheet);
        bottomSheetBehavior = BottomSheetBehavior.from(commentaryScroller);
        bottomSheetBehavior.setHideable(true);
        bottomSheetBehavior.setSkipCollapsed(true);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean showCommentary = preferences.getBoolean("show_commentary", true);
        commentaryScroller.setVisibility(
                showCommentary ? View.VISIBLE : GONE
        );

        assert buttonPrev != null;
        assert buttonNext != null;
        assert seekBar != null;
        assert viewPager != null;
        assert toolbar != null;
        assert commentaryView != null;

        this.setSupportActionBar(toolbar);

        buttonNext.setOnTouchListener(mDelayHideTouchListener);
        buttonPrev.setOnTouchListener(mDelayHideTouchListener);
        seekBar.setOnTouchListener(mDelayHideTouchListener);

        buttonPrev.setOnClickListener(this);
        buttonNext.setOnClickListener(this);
        seekBar.setOnSeekBarChangeListener(this);

        Bundle extras = this.getIntent().getExtras();
        int currentPage;

        Intent intent = getIntent();
        String action = intent.getAction();

        // Check if the application was started by visiting a URL
        if (action != null && action.equals(Intent.ACTION_VIEW)) {
            Uri data = intent.getData();
            Log.d(App.TAG, "ActivityFromUri: Data: " + data.toString());
            List<String> queryParams = data.getPathSegments();
            double chapterNumber = Double.parseDouble(queryParams.get(0).substring(1));
            Integer page = Integer.parseInt(queryParams.get(1).replaceAll("[^0-9]", ""));

            currentChapter = new Select().from(Chapter.class).where(Chapter_Table.number.eq(chapterNumber)).querySingle();

            currentPage = page;

            if (currentChapter == null || page > currentChapter.getPageCount()) {
                Log.d(App.TAG, "ActivityFromUri: Chapter " + chapterNumber + ", page + " + page + " is not in the database (yet)!");
                //TODO: Figure out if something needs to be done here
            }
        } else if (action != null && action.equals(BCBReaderApplication.ACTION_SHORTCUT)) {
            // The app was started using a custom shortcut
            currentChapter = DataPreferences.getLatestChapter(this);
            currentPage = DataPreferences.getLatestPage(this);
        } else if (extras != null) {
            if (!extras.containsKey(EXTRA_CHAPTER) || !extras.containsKey(EXTRA_PAGE)) {
                throw new IllegalArgumentException("Missing argument (CHAPTER or PAGE_NUMBER)");
            }

            currentChapter = extras.getParcelable(EXTRA_CHAPTER);
            if (currentChapter == null) {
                Log.e(App.TAG, "FullScreenReader: Activity started without a valid chapter in the extras");
                throw new IllegalArgumentException("Provide a chapter to display!");
            }
            currentPage = extras.getInt(EXTRA_PAGE);

            if (currentPage > currentChapter.getPageCount()) {
                // Page number is too high
                throw new IllegalArgumentException("The page number is higher than the page count of this chapter!");
            }
        } else {
            // No extra's?
            Log.e(App.TAG, "FullScreenReader: Activity started without action or without any extras");
            throw new IllegalArgumentException("Provide a chapter to display!");
        }

        // 0-based, so pageCount - 1
        seekBar.setMax(currentChapter.getPageCount() - 1);
        // Minus one because p1 = data[0]
        seekBar.setProgress(currentPage - 1);

        // prev + last + next
        viewPager.setOffscreenPageLimit(5);
        viewPager.setAdapter(new FullscreenPagePagerAdapter(getSupportFragmentManager(), this.currentChapter, this));
        viewPager.setCurrentItem(currentPage - 1);

        viewPager.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                new GestureDetectorCompat(thisActivity, new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onSingleTapUp(MotionEvent e) {
                        toggle();
                        return super.onSingleTapConfirmed(e);
                    }
                }).onTouchEvent(event);
                return false;
            }
        });
        viewPager.addOnPageChangeListener(this);

        updateCommentary(viewPager.getCurrentItem());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.context_menu_chapter, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_share:
                Double chapterNumber = currentChapter.getNumber();
                Long page = (long) (viewPager.getCurrentItem() + 1);

                ShareManager.ShareImageWithText(thisActivity,
                        API.FormatPageUrl(thisActivity, chapterNumber, page, API.getQualitySuffix(thisActivity)),
                        ShareManager.getStupidPhrase(thisActivity) + " " + API.FormatPageLink(chapterNumber, page),
                        getString(R.string.share),
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(final VolleyError error) {
                                thisActivity.runOnUiThread(
                                        new Runnable() {
                                            @Override
                                            public void run() {
                                                Snackbar.make(mContentView,
                                                        String.format(
                                                                thisActivity.getString(R.string.error_while_sharing),
                                                                error.getLocalizedMessage()
                                                        ),
                                                        Snackbar.LENGTH_INDEFINITE)
                                                        .show();
                                            }
                                        }
                                );
                            }
                        });

                break;
            default:
                return false;
        }
        return true;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(500);
    }

    /**
     * Toggle visibility of the pagination controls and the toolbar
     */
    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    /**
     * Go full screen, hide the controls
     */
    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        hideActionBar(actionBar);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Show the controls after coming back from full screen
     */
    private void show() {
        commentaryScroller.setVisibility(View.VISIBLE);
        // Show the system bar
        mContentView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                /*| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION*/);
        mVisible = true;

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            showActionBar(actionBar);
        }

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    /**
     * Animate hiding the action bar
     * Based on:
     * https://stackoverflow.com/questions/33667552/android-supportactionbar-not-animating-on-show-hide
     *
     * @param actionBar The ActionBar to hide
     */
    private void hideActionBar(@Nullable final ActionBar actionBar) {
        if (actionBar == null)
            return;

        // If the toolbar was not found or the API level is too low to animate the change,
        // don't animate the hiding and just do it
        if (toolbar != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            toolbar.animate()
                    .translationY(-(toolbar.getHeight() + getStatusBarHeight()))
                    .withEndAction(new Runnable() {
                        @Override
                        public void run() {
                            actionBar.hide();
                        }
                    })
                    .start();
        } else {
            actionBar.hide();
        }
    }

    /**
     * Animate showing the action bar
     * Based on:
     * https://stackoverflow.com/questions/33667552/android-supportactionbar-not-animating-on-show-hide
     *
     * @param actionBar The ActionBar to hide
     */
    private void showActionBar(@NonNull final ActionBar actionBar) {
        if (!actionBar.isShowing()) {
            actionBar.show();

            if (firstToolbarShow) {
                // Workaround for toolbar hiding behind status bar
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) toolbar.getLayoutParams();
                params.topMargin += getStatusBarHeight();
                toolbar.setLayoutParams(params);

                firstToolbarShow = false;
            }

            if (toolbar != null) {
                toolbar.animate()
                        .translationY(0)
                        .setDuration(UI_ANIMATION_DELAY)
                        .start();
            }
        }
    }

    /**
     * Get the height of the status bar
     * Taken from: https://stackoverflow.com/questions/3407256/height-of-status-bar-in-android
     *
     * @return The height of the status bar, in pixels
     */
    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
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
            this.onNext();
        if (v.equals(buttonPrev))
            this.onPrev();
    }

    /**
     * Handle the request to show the previous page
     */
    private void onNext() {
        //TODO: Maybe allow loading the next chapter?
        int currentItem = viewPager.getCurrentItem();
        if (currentItem < currentChapter.getPageDescriptions().size() - 1) {
            viewPager.setCurrentItem(currentItem + 1);
        } else {
            viewPager.setCurrentItem(0);
        }
    }

    /**
     * Handle the request to show the previous page
     */
    private void onPrev() {
        //TODO: Maybe allow loading the previous chapter?
        int currentItem = viewPager.getCurrentItem();
        int pageIndex;
        if (currentItem >= 1) {
            pageIndex = currentItem - 1;
        } else {
            pageIndex = currentChapter.getPageCount() - 1;
        }
        viewPager.setCurrentItem(pageIndex);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            viewPager.setCurrentItem(progress);
        }
        String title = String.format(Locale.getDefault(), getString(R.string.title_chapter_title_page_number), currentChapter.getTitle(), progress + 1);
        toolbar.setTitle(title);
        this.setTitle(title);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onTap(View view) {
        toggle();
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        seekBar.setProgress(position);

        updateCommentary(position);
    }

    /**
     * Update the commentary TextView to reflect the change in page
     *
     * @param pageIndex The index of the page in the current chapter (0-based!)
     */
    private void updateCommentary(int pageIndex) {
        commentaryView.setText(CompatHelper.fromHtml(currentChapter.getPageDescriptions().get(pageIndex).getDescription()));
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
}
