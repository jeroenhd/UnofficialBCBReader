package nl.jeroenhd.app.bcbreader;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import nl.jeroenhd.app.bcbreader.data.API;
import nl.jeroenhd.app.bcbreader.data.Chapter;
import nl.jeroenhd.app.bcbreader.data.ChapterListRequest;
import nl.jeroenhd.app.bcbreader.data.Page;

public class ChapterReadingActivity extends AppCompatActivity {
    public static final String CHAPTER = "nl.jeroenhd.app.bcbreader.ChapterReadingActivity.CHAPTER";
    RecyclerView mRecycler;
    RecyclerView.LayoutManager mLayout;
    ChapterReadingAdapter mAdapter;
    ArrayList<Page> mPages;
    Chapter mChapter;
    CoordinatorLayout mCoordinatorLayout;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chapter_reading);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        if (getSupportActionBar()!=null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mCoordinatorLayout = (CoordinatorLayout)findViewById(R.id.coordinator);

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

        SetupData(mChapter);
        SetupRecyclerView();
    }

    void SetupData(Chapter chapter)
    {
        mPages = new ArrayList<>();
        mPages.addAll(chapter.getPageDescriptions());
        /*for (Double i = 0.0; i < 20; i++)
        {
            mPages.add(new Page("Example commentary", i+1, 0.0));
        }*/
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
