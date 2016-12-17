package nl.jeroenhd.app.bcbreader.activities;

import android.animation.Animator;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.Window;
import android.view.animation.AccelerateInterpolator;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.raizlabs.android.dbflow.sql.language.Select;

import java.util.ArrayList;
import java.util.List;

import nl.jeroenhd.app.bcbreader.BCBReaderApplication;
import nl.jeroenhd.app.bcbreader.R;
import nl.jeroenhd.app.bcbreader.adapters.ChapterReadingAdapter;
import nl.jeroenhd.app.bcbreader.data.API;
import nl.jeroenhd.app.bcbreader.data.App;
import nl.jeroenhd.app.bcbreader.data.Chapter;
import nl.jeroenhd.app.bcbreader.data.ChapterListRequest;
import nl.jeroenhd.app.bcbreader.data.Chapter_Table;
import nl.jeroenhd.app.bcbreader.data.Page;
import nl.jeroenhd.app.bcbreader.data.SuperSingleton;
import nl.jeroenhd.app.bcbreader.data.check.DataPreferences;
import nl.jeroenhd.app.bcbreader.data.databases.ChapterDatabase;
import nl.jeroenhd.app.bcbreader.tools.ColorHelper;
import nl.jeroenhd.app.bcbreader.views.CallbackNetworkImageView;

public class ChapterReadingActivity extends AppCompatActivity implements Toolbar.OnMenuItemClickListener {
    public static final String CHAPTER = "nl.jeroenhd.app.bcbreader.activities.ChapterReadingActivity.CHAPTER";
    public static final String SCROLL_TO = "nl.jeroenhd.app.bcbreader.activities.ChapterReadingActivity.SCROLL_TO";
    public static final String JUST_SHOW_LATEST = "nl.jeroenhd.app.bcbreader.activities.ChapterReadingActivity.JUST_SHOW_LATEST";
    private final ChapterReadingActivity thisActivity = this;
    private LinearLayoutManager mLayout;
    private ArrayList<Page> mPages;
    private int mScrollToPage = -1;
    private Chapter mChapter;
    private CoordinatorLayout mCoordinatorLayout;
    private CollapsingToolbarLayout mCollapsingToolbarLayout;
    private CallbackNetworkImageView headerBackgroundImage;
    private Toolbar toolbar;
    private FloatingActionButton fab;
    private RelativeLayout mLoadingOverlay;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chapter_reading);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        String action = intent.getAction();
        Double chapterNumber;
        boolean downloadBeforeShowing = false;

        // Check if the application was started by visiting a URL
        if (action != null && action.equals(Intent.ACTION_VIEW)) {
            Uri data = intent.getData();
            Log.d(App.TAG, "ActivityFromUri: Data: " + data.toString());
            List<String> queryParams = data.getPathSegments();
            chapterNumber = Double.parseDouble(queryParams.get(0).substring(1));
            Integer page = Integer.parseInt(queryParams.get(1).replaceAll("[^0-9]", ""));

            mChapter = new Select().from(Chapter.class).where(Chapter_Table.number.eq(chapterNumber)).querySingle();
            mScrollToPage = page;

            if (mChapter == null || page > mChapter.getPageCount()) {
                Log.d(App.TAG, "ActivityFromUri: Chapter " + chapterNumber + ", page + " + page + " is not in the database (yet)!");
                downloadBeforeShowing = true;
            }
        } else if (action != null && action.equals(BCBReaderApplication.ACTION_SHORTCUT)) {
            // This extra will be set if we don't really care and just want the latest page
            // This is useful for app shortcuts
            mChapter = DataPreferences.getLatestChapter(this);
            chapterNumber = DataPreferences.getLatestChapterNumber(this);
            mScrollToPage = DataPreferences.getLatestPage(this);
        } else {
            // Load the chapter requested
            mChapter = intent.getParcelableExtra(ChapterReadingActivity.CHAPTER);
            chapterNumber = mChapter.getNumber();
            mScrollToPage = intent.getIntExtra(ChapterReadingActivity.SCROLL_TO, -1);
        }

        mLoadingOverlay = (RelativeLayout) findViewById(R.id.loadingOverlay);
        assert mLoadingOverlay != null;
        mLoadingOverlay.setVisibility(downloadBeforeShowing ? View.VISIBLE : View.GONE);

        if (mChapter == null && !downloadBeforeShowing) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Snackbar.make(mCoordinatorLayout, "Did not receive chapter from extras???", Snackbar.LENGTH_LONG).show();
                }
            });
        }

        fab = (FloatingActionButton) findViewById(R.id.fab);

        assert fab != null;

        fab.setImageResource(mChapter != null && mChapter.isFavourite() ? R.drawable.ic_favorite_white_48dp : R.drawable.ic_favorite_border_white);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mChapter.setFavourite(!mChapter.isFavourite());
                mChapter.save();

                String msg;
                if (mChapter.isFavourite()) {
                    msg = getString(R.string.added_to_favourites);
                } else {
                    msg = getString(R.string.removed_from_favourites);
                }

                fab.setImageResource(mChapter.isFavourite() ? R.drawable.ic_favorite_white_48dp : R.drawable.ic_favorite_border_white);
                fab.invalidate();
                Snackbar.make(view, msg, Snackbar.LENGTH_LONG)
                        .setAction(getString(R.string.undo), this).show();
            }
        });
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        headerBackgroundImage = (CallbackNetworkImageView) findViewById(R.id.backgroundImage);
        mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator);
        mCollapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsingToolbarLayout);

        SetupAnimation();

        toolbar.setOnMenuItemClickListener(this);

        if (!downloadBeforeShowing) {
            SetupHeader();
            SetupData(mChapter);
            SetupRecyclerView();
        } else {
            DelayedSetupData(chapterNumber);
        }

    }

    @Override
    public void setTitle(int titleId) {
        super.setTitle(titleId);
        if (toolbar != null)
            toolbar.setTitle(titleId);
    }

    @Override
    public void setTitle(CharSequence title) {
        super.setTitle(title);
        if (toolbar != null)
            toolbar.setTitle(title);
    }

    private void SetupHeader() {
        this.setTitle(mChapter.getTitle());
        headerBackgroundImage.setOnImageEventListener(new CallbackNetworkImageView.ImageEventListener() {
            @Override
            public void onLoadSuccess(Bitmap bm) {
                if (bm == null) {
                    Log.e(App.TAG, "SetupHeader: Failed to load image (bg=null)!");
                    return;
                }
                Palette.PaletteAsyncListener paletteAsyncListener = new Palette.PaletteAsyncListener() {
                    @Override
                    public void onGenerated(Palette palette) {
                        int accentColor = palette.getVibrantColor(0);
                        int statusColor = palette.getDarkMutedColor(0);
                        int toolbarColor = palette.getMutedColor(0);

                        int backgroundColor = palette.getLightVibrantColor(0xffffffff);
                        int titleColor = ColorHelper.foregroundColor(backgroundColor);
                        toolbar.setTitleTextColor(titleColor);
                        toolbar.setSubtitleTextColor(titleColor);


                        // Set toolbar color (if available)
                        if (toolbarColor != 0 && statusColor != 0) {
                            mCollapsingToolbarLayout.setContentScrimColor(toolbarColor);

                            // Set status bar color (if available & possible)
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                // This will not work in <= Kitkat
                                getWindow().setStatusBarColor(statusColor);
                            }
                        }


                        if (accentColor != 0)
                            fab.setBackgroundColor(accentColor);


                        View textSkim = findViewById(R.id.toolbar_text_skim);
                        assert textSkim != null;
                        GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{0, toolbarColor});
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                            textSkim.setBackground(gradientDrawable);
                        } else {
                            //noinspection deprecation
                            textSkim.setBackgroundDrawable(gradientDrawable);
                        }
                    }
                };

                Palette.from(bm).generate(paletteAsyncListener);
            }

            @Override
            public void onLoadError() {
                // Default colors are alright
                Log.e(App.TAG, "SetupHeader: Error while loading!!!");
            }
        });
        headerBackgroundImage.setErrorImageResId(R.color.colorPrimary);
        headerBackgroundImage.setImageUrl(
                API.FormatChapterThumbURL(this, mChapter.getNumber()),
                SuperSingleton.getInstance(this).getImageLoader()
        );
    }

    /**
     * Prepare for viewing the pages
     *
     * @param chapter The chapter to load
     */
    private void SetupData(Chapter chapter) {
        mPages = new ArrayList<>();
        mPages.addAll(chapter.getPageDescriptions());
        Log.d(App.TAG, "SetupData: Loaded " + chapter.getPageDescriptions().size() + " pages");
    }

    /**
     * Download the new chapter list and THEN prepare and load the pages
     *
     * @param chapterNumber The chapter to load
     */
    private void DelayedSetupData(final Double chapterNumber) {
        // Indicate we're loading (should be extended)
        this.setTitle(getString(R.string.loading));
        final ProgressBar progressBar = (ProgressBar) mLoadingOverlay.findViewById(R.id.loadingProgressBar);
        progressBar.setIndeterminate(true);

        // Start downloading the chapter list
        ChapterListRequest downloadRequest = new ChapterListRequest(API.ChaptersDB,
                API.RequestHeaders(), new Response.Listener<List<Chapter>>() {
            @Override
            public void onResponse(List<Chapter> response) {
                // Save for future reuse
                ChapterDatabase.SaveUpdate(response);

                // Find the requested chapter
                for (Chapter c : response) {
                    if (c.getNumber().equals(chapterNumber)) {
                        mChapter = c;
                        mLoadingOverlay.setVisibility(View.GONE);
                        SetupHeader();
                        SetupData(c);
                        SetupRecyclerView();
                        return;
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(App.TAG, "ChapterReadingActivity: DelayedSetupData: error ocurred downloading chapter list");
                error.printStackTrace();
                // Stop the progress bar
                progressBar.setIndeterminate(false);

                // Allow the user to retry in case of an error
                Snackbar.make(mCoordinatorLayout, R.string.chapter_list_download_failed, Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.retry, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                DelayedSetupData(chapterNumber);
                            }
                        })
                        .show();
            }
        });
        SuperSingleton.getInstance(thisActivity).getVolleyRequestQueue().add(downloadRequest);
    }

    /**
     * Start the neat little transition animation
     */
    private void SetupAnimation() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Transition transition = TransitionInflater.from(this).inflateTransition(R.transition.changebounds_with_arcmotion);
            getWindow().setSharedElementEnterTransition(transition);
        } else {
            // Not supported!
            Log.d(App.TAG, "Animation: View animation is not supported by this platform!");
        }
    }

    private void animateRevealShow(View viewRoot) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            int cx = (viewRoot.getLeft() + viewRoot.getRight()) / 2;
            int cy = (viewRoot.getTop() + viewRoot.getBottom()) / 2;
            int finalRadius = Math.max(viewRoot.getWidth(), viewRoot.getHeight());

            Animator anim = ViewAnimationUtils.createCircularReveal(viewRoot, cx, cy, 0, finalRadius);
            viewRoot.setVisibility(View.VISIBLE);
            anim.setDuration(1000);
            anim.setInterpolator(new AccelerateInterpolator());
            anim.start();
        } else {
            // Not supported
            Log.d(App.TAG, "animateRevealShow: Not supported by platform");
        }
    }

    void UpdateTheme(Bitmap headerBitmap) {
        Palette.from(headerBitmap).generate(new Palette.PaletteAsyncListener() {
            @Override
            public void onGenerated(Palette palette) {
                // This only works on Lollipop and higher
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    Window window = getWindow();
                    int defaultColor = ContextCompat.getColor(thisActivity, R.color.colorPrimaryDark);

                    window.setStatusBarColor(palette.getMutedColor(defaultColor));
                } else {
                    //TODO: additional theming for <LOLLIPOP
                    Log.d(App.TAG, "UpdateTheme: <Lollipop, not implemented yet");
                }
            }
        });
    }

    private void SetupRecyclerView() {
        RecyclerView mRecycler = (RecyclerView) findViewById(R.id.pages);
        mLayout = new LinearLayoutManager(this);

        final ChapterReadingAdapter mAdapter = new ChapterReadingAdapter(this, mPages);
        mRecycler.setAdapter(mAdapter);
        mRecycler.setLayoutManager(mLayout);

        mLayout.scrollToPositionWithOffset(mScrollToPage - 1, 0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_chapter_reading, menu);

        return true;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_settings:
                Intent settingsIntent = new Intent(thisActivity, SettingsActivity.class);
                startActivity(settingsIntent);
                break;
            case R.id.action_fullscreen:
                Intent fullScreenIntent = new Intent(thisActivity, FullscreenReaderActivity.class);

                //TODO: Make a nice transition here
                fullScreenIntent.putExtra(FullscreenReaderActivity.EXTRA_CHAPTER, mChapter);

                //Get the central page and pass it to the reader
                LinearLayoutManager linearLayoutManager = mLayout;
                int page = linearLayoutManager.findFirstCompletelyVisibleItemPosition();

                // If no page was found (the screen doesn't show a complete page),
                // just get the first one
                if (page < 1) {
                    page = linearLayoutManager.findFirstVisibleItemPosition();
                }
                fullScreenIntent.putExtra(FullscreenReaderActivity.EXTRA_PAGE, page);

                startActivity(fullScreenIntent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}