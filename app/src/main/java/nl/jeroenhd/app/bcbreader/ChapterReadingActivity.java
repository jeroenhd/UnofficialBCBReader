package nl.jeroenhd.app.bcbreader;

import android.animation.Animator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.Window;
import android.view.animation.AccelerateInterpolator;
import android.widget.ImageView;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.NetworkImageView;
import com.google.gson.GsonBuilder;

import java.io.Console;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import nl.jeroenhd.app.bcbreader.data.API;
import nl.jeroenhd.app.bcbreader.data.Chapter;
import nl.jeroenhd.app.bcbreader.data.ChapterListRequest;
import nl.jeroenhd.app.bcbreader.data.Page;
import nl.jeroenhd.app.bcbreader.data.SuperSingleton;

public class ChapterReadingActivity extends AppCompatActivity {
    public static final String CHAPTER = "nl.jeroenhd.app.bcbreader.ChapterReadingActivity.CHAPTER";
    RecyclerView mRecycler;
    RecyclerView.LayoutManager mLayout;
    ChapterReadingAdapter mAdapter;
    ArrayList<Page> mPages;
    Chapter mChapter;
    CoordinatorLayout mCoordinatorLayout;
    CollapsingToolbarLayout mCollapsingToolbarLayout;
    NetworkImageView headerBackgroundImage;

    final ChapterReadingActivity thisActivity = this;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chapter_reading);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO: SetChapterIsFavorite(chapter, true);
                Snackbar.make(view, "Added as favorite", Snackbar.LENGTH_LONG)
                        .setAction("Undo", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //TODO: SetChapterIsFavorite(chapter, false);
                            }
                        }).show();
            }
        });
        if (getSupportActionBar()!=null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        headerBackgroundImage = (NetworkImageView)findViewById(R.id.backgroundImage);
        mCoordinatorLayout = (CoordinatorLayout)findViewById(R.id.coordinator);
        mCollapsingToolbarLayout = (CollapsingToolbarLayout)findViewById(R.id.toolbar_layout);

        SetupAnimation();

        mChapter = this.getIntent().getParcelableExtra(ChapterReadingActivity.CHAPTER);
        if (mChapter==null)
        {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Snackbar.make(mCoordinatorLayout, "Did not receive chapter from extras???", Snackbar.LENGTH_LONG).show();
                }
            });
        } else {
            this.setTitle(mChapter.getTitle());
        }

        headerBackgroundImage.setErrorImageResId(R.color.colorPrimary);
        headerBackgroundImage.setImageUrl(
                API.FormatChapterThumbURL(mChapter.getNumber()),
                SuperSingleton.getInstance(this).getImageLoader()
        );

        SetupData(mChapter);
        SetupRecyclerView();
    }

    void SetupData(Chapter chapter)
    {
        mPages = new ArrayList<>();
        mPages.addAll(chapter.getPageDescriptions());
    }

    void SetupAnimation()
    {
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

    void UpdateTheme(Bitmap headerBitmap)
    {
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

    void SetupRecyclerView()
    {
        mRecycler = (RecyclerView) findViewById(R.id.pages);

        mLayout = new LinearLayoutManager(this);
        mRecycler.setLayoutManager(mLayout);

        mAdapter = new ChapterReadingAdapter(this, mPages);
        mRecycler.setAdapter(mAdapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                this.onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}