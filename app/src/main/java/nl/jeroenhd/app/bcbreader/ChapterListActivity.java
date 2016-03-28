package nl.jeroenhd.app.bcbreader;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.android.volley.Response;
import com.android.volley.VolleyError;

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
    //private FloatingActionButton mFab;
    private ArrayList<Chapter> mChapterData;
    private ChapterListAdapter mAdapter;
    private final Response.Listener<List<Chapter>> successListener = new Response.Listener<List<Chapter>>() {
        @Override
        public void onResponse(List<Chapter> response) {
            int currentCount = mChapterData.size();
            // Houston, we've got data!
            mChapterData.clear();
            mAdapter.notifyItemRangeRemoved(0, currentCount);

            mChapterData.addAll(response);
            mAdapter.notifyItemRangeInserted(0, mChapterData.size());

            Snackbar.make(mRecycler, "Loaded chapters!", Snackbar.LENGTH_LONG).show();
        }
    };
    private SuperSingleton singleton;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chapter_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        CoordinatorLayout mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator);

        singleton = SuperSingleton.getInstance(this);

        SetupData();
        SetupRecycler();
    }

    private void SetupData() {
        mChapterData = new ArrayList<>();
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
}
