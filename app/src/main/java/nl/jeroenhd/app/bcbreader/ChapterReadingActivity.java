package nl.jeroenhd.app.bcbreader;

import android.animation.Animator;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
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

import java.util.ArrayList;

import nl.jeroenhd.app.bcbreader.data.API;
import nl.jeroenhd.app.bcbreader.data.Chapter;
import nl.jeroenhd.app.bcbreader.data.Page;
import nl.jeroenhd.app.bcbreader.data.SuperSingleton;
import nl.jeroenhd.app.bcbreader.tools.ColorHelper;
import nl.jeroenhd.app.bcbreader.views.CallbackNetworkImageView;

public class ChapterReadingActivity extends AppCompatActivity implements Toolbar.OnMenuItemClickListener {
    public static final String CHAPTER = "nl.jeroenhd.app.bcbreader.ChapterReadingActivity.CHAPTER";
    private final ChapterReadingActivity thisActivity = this;
    private RecyclerView mRecycler;
    private RecyclerView.LayoutManager mLayout;
    private ChapterReadingAdapter mAdapter;
    private ArrayList<Page> mPages;
    private Chapter mChapter;
    private CoordinatorLayout mCoordinatorLayout;
    private CallbackNetworkImageView headerBackgroundImage;
    private Toolbar toolbar;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chapter_reading);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mChapter = this.getIntent().getParcelableExtra(ChapterReadingActivity.CHAPTER);
        if (mChapter == null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Snackbar.make(mCoordinatorLayout, "Did not receive chapter from extras???", Snackbar.LENGTH_LONG).show();
                }
            });
        } else {
            this.setTitle(mChapter.getTitle());
        }

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        assert fab != null;

        fab.setImageResource(mChapter.isFavourite() ? R.drawable.ic_favorite_white_48dp : R.drawable.ic_favorite_border_white);

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

        SetupAnimation();

        SetupHeader();

        SetupData(mChapter);
        SetupRecyclerView();

        toolbar.setOnMenuItemClickListener(this);
    }

    private void SetupHeader() {
        headerBackgroundImage.setCallback(new CallbackNetworkImageView.ImageEventListener() {
            @Override
            public void onLoadSuccess(Bitmap bm) {
                if (bm == null)
                    return;
                Palette.PaletteAsyncListener paletteAsyncListener = new Palette.PaletteAsyncListener() {
                    @Override
                    public void onGenerated(Palette palette) {
                        int backgroundColor = palette.getLightVibrantColor(0xffffffff);
                        int titleColor = ColorHelper.foregroundColor(backgroundColor);
                        toolbar.setTitleTextColor(titleColor);
                        toolbar.setSubtitleTextColor(titleColor);
                    }
                };
                Palette.from(bm).generate(paletteAsyncListener);
            }

            @Override
            public void onLoadError() {
                // Default colors are alright
            }
        });
        headerBackgroundImage.setErrorImageResId(R.color.colorPrimary);
        headerBackgroundImage.setImageUrl(
                API.FormatChapterThumbURL(mChapter.getNumber()),
                SuperSingleton.getInstance(this).getImageLoader()
        );
    }

    private void SetupData(Chapter chapter) {
        mPages = new ArrayList<>();
        mPages.addAll(chapter.getPageDescriptions());
        Log.d("SetupData", "Loaded " + chapter.getDescription().length() + " pages");
    }

    private void SetupAnimation() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Transition transition = TransitionInflater.from(this).inflateTransition(R.transition.changebounds_with_arcmotion);
            getWindow().setSharedElementEnterTransition(transition);
        } else {
            // Not supported!
            Log.d("Animation", "View animation is not supported by this platform!");
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
            Log.d("animateRevealShow", "Not supported by platform");
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
                    Log.d("UpdateTheme", "<Lollipop, not implemented yet");
                }
            }
        });
    }

    private void SetupRecyclerView() {
        mRecycler = (RecyclerView) findViewById(R.id.pages);

        mLayout = new LinearLayoutManager(this);
        mRecycler.setLayoutManager(mLayout);

        mAdapter = new ChapterReadingAdapter(this, mPages);
        mRecycler.setAdapter(mAdapter);
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
        switch(id)
        {
            case R.id.action_settings:
                Intent settingsIntent = new Intent(thisActivity, SettingsActivity.class);
                startActivity(settingsIntent);
                break;
            case R.id.action_fullscreen:
                Intent fullScreenIntent = new Intent(thisActivity, FullscreenReaderActivity.class);

                //TODO: Make a nice transition here
                fullScreenIntent.putExtra(FullscreenReaderActivity.EXTRA_CHAPTER, mChapter);

                //Get the central page and pass it to the reader
                LinearLayoutManager linearLayoutManager = (LinearLayoutManager)mLayout;
                int page = linearLayoutManager.findFirstCompletelyVisibleItemPosition();

                // If no page was found (the screen doesn't show a complete page),
                // just get the first one
                if (page < 1)
                {
                    page = linearLayoutManager.findFirstVisibleItemPosition();
                }
                fullScreenIntent.putExtra(FullscreenReaderActivity.EXTRA_PAGE, page);

                startActivity(fullScreenIntent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}