package nl.jeroenhd.app.bcbreader;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ProgressBar;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;
import com.raizlabs.android.dbflow.sql.language.Select;

import java.util.ArrayList;
import java.util.List;

import nl.jeroenhd.app.bcbreader.data.API;
import nl.jeroenhd.app.bcbreader.data.Chapter;
import nl.jeroenhd.app.bcbreader.data.ChapterListRequest;
import nl.jeroenhd.app.bcbreader.data.SuperSingleton;
import nl.jeroenhd.app.bcbreader.data.check.Check;
import nl.jeroenhd.app.bcbreader.data.databases.ChapterDatabase;

public class ChapterListActivity extends AppCompatActivity implements ChapterListAdapter.OnChapterClickListener {
    private final Activity thisActivity = this;
    private RecyclerView mRecycler;
    private ProgressBar mLoadingProgressbar;
    //private FloatingActionButton mFab;
    private ArrayList<Chapter> mChapterData;
    private ChapterListAdapter mAdapter;
    private final Response.Listener<List<Chapter>> chapterListDownloadSuccessListener = new Response.Listener<List<Chapter>>() {
        @Override
        public void onResponse(List<Chapter> response) {
            ChapterDatabase.SaveUpdate(response);

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
    private Response.Listener<String> checkSuccessListener = new Response.Listener<String>() {
        @Override
        public void onResponse(String response) {
            Gson gson = SuperSingleton.getInstance(thisActivity).getGsonBuilder().create();
            Check check = gson.fromJson(response, Check.class);

            double latestChapterNumber = check.getAddress().getLatestChapter();
            double latestPageNumber = check.getAddress().getLatestPage();
            Chapter latestChapterInBuffer = mChapterData.size() > 0 ? mChapterData.get(mChapterData.size() - 1) : null;

            // latestChapter > bufferChapter || ( latestChapter == bufferChapter && latestPage > bufferChapter.latestPage )
            if (latestChapterInBuffer == null ||
                    latestChapterNumber > latestChapterInBuffer.getNumber() || (
                    latestChapterInBuffer.getNumber().equals(latestChapterNumber) &&
                            latestChapterInBuffer.getPageCount() < latestPageNumber
            )
                    ) {
                // List needs an update
                ChapterListRequest downloadRequest = new ChapterListRequest(API.ChaptersDB, API.RequestHeaders(), chapterListDownloadSuccessListener, chapterListDownloadErrorListener);
                singleton.getVolleyRequestQueue().add(downloadRequest);
            }
        }
    };
    private Response.ErrorListener checkErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Snackbar.make(mRecycler, R.string.update_check_failed, Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.retry, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startChapterListUpdateCheck();
                        }
                    })
                    .show();
        }
    };
    private final Response.ErrorListener chapterListDownloadErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Snackbar.make(mRecycler, R.string.chapter_list_download_failed, Snackbar.LENGTH_LONG)
                    .setAction(R.string.retry, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startChapterListUpdateCheck();
                        }
                    })
                    .show();
        }
    };

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chapter_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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

        startChapterListUpdateCheck();
    }

    private void startChapterListUpdateCheck() {
        StringRequest stringRequest = new StringRequest(API.CheckURI, this.checkSuccessListener, this.checkErrorListener);
        singleton.getVolleyRequestQueue().add(stringRequest);
    }

    private void SetupRecycler() {
        mRecycler = (RecyclerView) findViewById(R.id.chapterList);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecycler.setLayoutManager(mLayoutManager);

        mAdapter = new ChapterListAdapter(this, mChapterData, this);
        mRecycler.setAdapter(mAdapter);
    }

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
