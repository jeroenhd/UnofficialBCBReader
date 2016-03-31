package nl.jeroenhd.app.bcbreader;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.raizlabs.android.dbflow.runtime.TransactionManager;
import com.raizlabs.android.dbflow.runtime.transaction.process.ProcessModelInfo;
import com.raizlabs.android.dbflow.runtime.transaction.process.SaveModelTransaction;
import com.raizlabs.android.dbflow.sql.language.Select;

import java.util.ArrayList;
import java.util.List;

import nl.jeroenhd.app.bcbreader.data.API;
import nl.jeroenhd.app.bcbreader.data.Chapter;
import nl.jeroenhd.app.bcbreader.data.ChapterListRequest;
import nl.jeroenhd.app.bcbreader.data.SuperSingleton;

public class ChapterListActivity extends AppCompatActivity implements ChapterListAdapter.OnChapterClickListener {
    private final Activity thisActivity = this;
    private RecyclerView mRecycler;
    private final Response.ErrorListener errorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Snackbar.make(mRecycler, error.getMessage(), Snackbar.LENGTH_LONG).show();
        }
    };
    private ProgressBar mLoadingProgressbar;
    //private FloatingActionButton mFab;
    private ArrayList<Chapter> mChapterData;
    private ChapterListAdapter mAdapter;


    private Toolbar toolbar;

    private CoordinatorLayout mCoordinatorLayout;
    private final Response.Listener<List<Chapter>> successListener = new Response.Listener<List<Chapter>>() {
        @Override
        public void onResponse(List<Chapter> response) {
            TransactionManager.getInstance().addTransaction(new SaveModelTransaction<>(ProcessModelInfo.withModels(response)));

            // Houston, we've got data!
            int startingIndex = 0, count = 0;
            // Look for each chapter
            for (int i = 0; i < response.size(); i++) {
                Chapter c = response.get(i);

                boolean numberFound = false;
                for (int j = 0; j < mChapterData.size(); j++) {
                    Chapter oldChapter = mChapterData.get(j);

                    // Don't add chapter that was already added:
                    // Check if a chapter with the same number already exists
                    if (oldChapter.getNumber().equals(c.getNumber())) {
                        numberFound = true;
                        // Check if the descriptions are the same, if so, don't add this one
                        if (oldChapter != c) {
                            c.setFavourite(oldChapter.isFavourite());
                            // Metadata is not the same, update it!
                            mChapterData.set(j, c);
                            mAdapter.notifyItemChanged(j);
                        }
                    }
                }

                // If the chapter hasn't been found
                if (!numberFound) {
                    startingIndex = (startingIndex == 0 ? i : startingIndex);
                    count++;
                    mChapterData.add(c);
                }
            }
            mLoadingProgressbar.setVisibility(View.GONE);
            mAdapter.notifyItemRangeInserted(startingIndex, count);

            Snackbar.make(mRecycler, "Loaded chapters!", Snackbar.LENGTH_LONG).show();
        }
    };
    private SuperSingleton singleton;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chapter_list);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setOnMenuItemClickListener(toolbarMenuClick);

        mCoordinatorLayout = (CoordinatorLayout)findViewById(R.id.coordinator);

        singleton = SuperSingleton.getInstance(this);
        mLoadingProgressbar = (ProgressBar) findViewById(R.id.progressBar);

        SetupData();
        SetupRecycler();
    }

    private void SetupData() {
        mChapterData = new ArrayList<>();

        List<Chapter> chapters = new Select().from(Chapter.class).queryList();
        if (chapters.size() > 0) {
            mLoadingProgressbar.setVisibility(View.GONE);
            mChapterData.addAll(chapters);
        }

        ChapterListRequest downloadRequest = new ChapterListRequest(API.ChaptersDB, API.RequestHeaders(), successListener, errorListener);
        singleton.getVolleyRequestQueue().add(downloadRequest);
    }

    private void SetupRecycler() {
        mRecycler = (RecyclerView) findViewById(R.id.chapterList);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecycler.setLayoutManager(mLayoutManager);

        mAdapter = new ChapterListAdapter(this, mChapterData, this);
        mRecycler.setAdapter(mAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_chapter_list, menu);

        return true;
    }

    Toolbar.OnMenuItemClickListener toolbarMenuClick = new Toolbar.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            int id = item.getItemId();
            switch(id)
            {
                case R.id.menu_settings:
                    Intent settingsIntent = new Intent(thisActivity, SettingsActivity.class);
                    startActivity(settingsIntent);
                    break;
            }
            return false;
        }
    };

    @Override
    public void onChapterSelect(final View v, final Chapter c) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Intent iNewActivity = new Intent(thisActivity, ChapterReadingActivity.class);

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    // Nice animations (lollipop+)

                    View thumbView = v.findViewById(R.id.thumb);

                    Pair<View, String> thumbPair = Pair.create(thumbView, thumbView.getTransitionName());

                    ActivityOptionsCompat options;
                    //noinspection unchecked
                    options = ActivityOptionsCompat.makeSceneTransitionAnimation(thisActivity/*, fabPair*/, thumbPair);

                    // Add own extras
                    Bundle bundle = options.toBundle();
                    bundle.setClassLoader(Chapter.class.getClassLoader());
                    iNewActivity.putExtra(ChapterReadingActivity.CHAPTER, c);

                    thisActivity.startActivity(iNewActivity, bundle);
                } else {
                    // Ugly animations
                    //TODO: Try to improve these animations!

                    iNewActivity.putExtra(ChapterReadingActivity.CHAPTER, c);
                    startActivity(iNewActivity);
                }
            }
        });
    }

    @Override
    public void onChapterFavourite(AppCompatImageView v, Chapter c) {
        // Switch between favourite/not favourite
        c.setFavourite(!c.isFavourite());

        // Save the fav state
        c.save();

        // Update the list
        int index = mChapterData.indexOf(c);
        mAdapter.notifyItemChanged(index);

    }
}
