package nl.jeroenhd.app.bcbreader;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;
import com.raizlabs.android.dbflow.sql.language.Select;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import nl.jeroenhd.app.bcbreader.data.API;
import nl.jeroenhd.app.bcbreader.data.Chapter;
import nl.jeroenhd.app.bcbreader.data.ChapterListRequest;
import nl.jeroenhd.app.bcbreader.data.SuperSingleton;
import nl.jeroenhd.app.bcbreader.data.check.Check;
import nl.jeroenhd.app.bcbreader.data.check.UpdateTimes;
import nl.jeroenhd.app.bcbreader.data.databases.ChapterDatabase;

public class ChapterListActivity extends AppCompatActivity implements ChapterListAdapter.OnChapterClickListener, Toolbar.OnMenuItemClickListener, SwipeRefreshLayout.OnRefreshListener, PopupMenu.OnMenuItemClickListener {
    private final Activity thisActivity = this;

    private RecyclerView mRecycler;
    private final Response.ErrorListener errorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Snackbar.make(mRecycler, error.getMessage(), Snackbar.LENGTH_LONG).show();
        }
    };
    private ProgressBar mLoadingProgressbar;
    private ArrayList<Chapter> mChapterData;
    private ChapterListAdapter mAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;

    private SuperSingleton singleton;
    /**
     * Called when downloading the check fails
     */
    private final Response.ErrorListener checkErrorListener = new Response.ErrorListener() {
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

            if (swipeRefreshLayout != null) {
                swipeRefreshLayout.setRefreshing(false);
            }
        }
    };
    /**
     * Called when downloading the chapter list fails
     */
    private final Response.ErrorListener chapterListDownloadErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            error.printStackTrace();
            Snackbar.make(mRecycler, R.string.chapter_list_download_failed, Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.retry, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startChapterListUpdateCheck();
                        }
                    })
                    .show();


            if (swipeRefreshLayout != null) {
                swipeRefreshLayout.setRefreshing(false);
            }
        }
    };
    /**
     * The latest update times data.
     * Is initialised to null!
     */
    private UpdateTimes latestUpdateTimes = null;
    /**
     * The response listener for a successful download of the chapter list
     */
    private final Response.Listener<List<Chapter>> chapterDownloadSuccessListener = new Response.Listener<List<Chapter>>() {
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

            if (swipeRefreshLayout != null) {
                swipeRefreshLayout.setRefreshing(false);
            }

            final Date lastUpdate = latestUpdateTimes.lastUpdate(new Date());

            long diff;
            if (lastUpdate != null) {
                diff = new Date().getTime() - lastUpdate.getTime();
            } else {
                Log.e("JustUpdatedPopup", "Unvalid parameters! lastUpdate is null!");
                diff = -1;
            }

            // If the last update was less than 3 hours ago...
            if ((diff > 0 && diff <= 1000 * 60 * 60 * 3)) {
                Snackbar.make(swipeRefreshLayout, R.string.go_to_latest_update, Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.go, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Chapter latestChapter = ChapterDatabase.getLastChapter();

                                View chapterView = mRecycler.getChildAt(mRecycler.getChildCount() - 1);
                                onChapterSelect(chapterView, latestChapter, latestChapter.getPageCount());
                            }

                        })
                        .show();
            }
        }
    };
    /**
     * Called when the check API file has been downloaded successfully
     */
    private final Response.Listener<String> checkSuccessListener = new Response.Listener<String>() {
        @Override
        public void onResponse(String response) {
            Gson gson = SuperSingleton.getInstance(thisActivity).getGsonBuilder().create();
            Check check = gson.fromJson(response, Check.class);

            // Store the latest update times
            latestUpdateTimes = check.getUpdateTimes();

            double latestChapterNumber = check.getAddress().getLatestChapter();
            double latestPageNumber = check.getAddress().getLatestPage();
            Chapter latestChapterInBuffer = mChapterData.size() > 0 ? mChapterData.get(mChapterData.size() - 1) : null;

            if (latestChapterInBuffer == null ||
                    latestChapterNumber > latestChapterInBuffer.getNumber() || (
                    latestChapterInBuffer.getNumber().equals(latestChapterNumber) &&
                            latestChapterInBuffer.getPageCount() < latestPageNumber
            )
                    ) {
                // List needs an update
                ChapterListRequest downloadRequest = new ChapterListRequest(API.ChaptersDB, API.RequestHeaders(), chapterDownloadSuccessListener, chapterListDownloadErrorListener);
                singleton.getVolleyRequestQueue().add(downloadRequest);
            }


            if (swipeRefreshLayout != null) {
                swipeRefreshLayout.setRefreshing(false);
            }
        }
    };
    private LinearLayoutManager mLayoutManager;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chapter_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        assert toolbar != null;
        toolbar.setOnMenuItemClickListener(this);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        assert swipeRefreshLayout != null;
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(
                R.color.mike,
                R.color.lucy,
                R.color.david,
                R.color.daisy,
                R.color.paulo
        );

        CoordinatorLayout mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator);

        singleton = SuperSingleton.getInstance(this);
        mLoadingProgressbar = (ProgressBar) findViewById(R.id.emptyListSpinner);

        assert mLoadingProgressbar != null;
        assert  mCoordinatorLayout != null;

        SetupData();
        SetupRecycler();
    }

    /**
     * Load and set up the data for the list view
     */
    private void SetupData() {
        mChapterData = new ArrayList<>();

        List<Chapter> chapters = new Select().from(Chapter.class).queryList();
        if (chapters.size() > 0) {
            mLoadingProgressbar.setVisibility(View.GONE);
            mChapterData.addAll(chapters);
        }

        startChapterListUpdateCheck();
    }

    /**
     * Start updating the chapter list
     */
    private void startChapterListUpdateCheck() {
        StringRequest stringRequest = new StringRequest(API.CheckURI, this.checkSuccessListener, this.checkErrorListener);
        singleton.getVolleyRequestQueue().add(stringRequest);

        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(true);
        }
    }

    /**
     * Prepare the RecyclerView for showing chapters
     */
    private void SetupRecycler() {
        mRecycler = (RecyclerView) findViewById(R.id.chapterList);

        mLayoutManager = new LinearLayoutManager(this);
        mRecycler.setLayoutManager(mLayoutManager);

        boolean isSortDescending = getSortDescending();
        setSortDescending(isSortDescending);

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

    /**
     * Called when a chapter is selected in the list
     *
     * @param view    The view the user has interacted with
     * @param chapter The chapter the user has selected
     * @param page The page to scroll to (use 1 to start from the beginning)
     */
    @Override
    public void onChapterSelect(final View view, final Chapter chapter, final int page) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Intent iNewActivity = new Intent(thisActivity, ChapterReadingActivity.class);

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    // Nice animations (lollipop+)

                    View thumbView = view.findViewById(R.id.thumb);

                    Pair<View, String> thumbPair = Pair.create(thumbView, thumbView.getTransitionName());

                    ActivityOptionsCompat options;
                    //noinspection unchecked
                    options = ActivityOptionsCompat.makeSceneTransitionAnimation(thisActivity/*, fabPair*/, thumbPair);

                    // Add own extras
                    Bundle bundle = options.toBundle();
                    bundle.setClassLoader(Chapter.class.getClassLoader());
                    iNewActivity.putExtra(ChapterReadingActivity.CHAPTER, chapter);
                    iNewActivity.putExtra(ChapterReadingActivity.SCROLL_TO, page);

                    thisActivity.startActivity(iNewActivity, bundle);
                } else {
                    // Ugly animations
                    //TODO: Try to improve these animations!

                    iNewActivity.putExtra(ChapterReadingActivity.CHAPTER, chapter);
                    startActivity(iNewActivity);
                }
            }
        });
    }

    /**
     * Called when a chapter has been added to the user's favourites
     * @param view The view the user has interacted with
     * @param chapter The chapter the user has selected
     */
    @Override
    public void onChapterFavourite(AppCompatImageView view, Chapter chapter) {
        // Switch between favourite/not favourite
        chapter.setFavourite(!chapter.isFavourite());

        // Save the fav state
        chapter.save();

        // Update the list
        int index = mChapterData.indexOf(chapter);
        mAdapter.notifyItemChanged(index);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.menu_settings:
                Intent settingsIntent = new Intent(thisActivity, SettingsActivity.class);
                startActivity(settingsIntent);
                break;
            case R.id.menu_sort:
                PopupMenu popupMenu = new PopupMenu(thisActivity, findViewById(R.id.menu_sort));
                popupMenu.inflate(R.menu.popup_menu_sort);
                popupMenu.setOnMenuItemClickListener(this);
                popupMenu.show();
                break;
            case R.id.popup_menu_ascending:
            case R.id.popup_menu_descending:
                setSortDescending(id == R.id.popup_menu_descending);
                break;
        }
        return false;
    }

    /**
     * Get the sort order for the chapter list
     *
     * @return true if descending, false if ascending
     */
    private boolean getSortDescending() {
        return PreferenceManager
                .getDefaultSharedPreferences(this)
                .getBoolean("chapter_sort_descending", false);
    }

    /**
     * Set the sort order for the chapter list
     *
     * @param descending True to sort descending, false to sort ascending
     */
    private void setSortDescending(boolean descending) {
        PreferenceManager
                .getDefaultSharedPreferences(thisActivity)
                .edit()
                .putBoolean("chapter_sort_descending", descending)
                .apply();

        mLayoutManager.setReverseLayout(descending);
        mLayoutManager.setStackFromEnd(descending);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setSortDescending(getSortDescending());
    }

    /**
     * Called when the chapter list should be refreshed
     */
    @Override
    public void onRefresh() {
        startChapterListUpdateCheck();
    }
}
