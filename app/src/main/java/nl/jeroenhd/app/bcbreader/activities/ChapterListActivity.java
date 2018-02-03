package nl.jeroenhd.app.bcbreader.activities;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
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
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;
import com.raizlabs.android.dbflow.sql.language.Select;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import nl.jeroenhd.app.bcbreader.R;
import nl.jeroenhd.app.bcbreader.adapters.ChapterListAdapter;
import nl.jeroenhd.app.bcbreader.adapters.PageThumbAdapter;
import nl.jeroenhd.app.bcbreader.data.API;
import nl.jeroenhd.app.bcbreader.data.App;
import nl.jeroenhd.app.bcbreader.data.Chapter;
import nl.jeroenhd.app.bcbreader.data.ChapterListRequest;
import nl.jeroenhd.app.bcbreader.data.Chapter_Table;
import nl.jeroenhd.app.bcbreader.data.SuperSingleton;
import nl.jeroenhd.app.bcbreader.data.check.Check;
import nl.jeroenhd.app.bcbreader.data.check.DataPreferences;
import nl.jeroenhd.app.bcbreader.data.check.UpdateTimes;
import nl.jeroenhd.app.bcbreader.data.databases.ChapterDatabase;
import nl.jeroenhd.app.bcbreader.notifications.NotificationService;
import nl.jeroenhd.app.bcbreader.tools.AppCrashStorage;

public class ChapterListActivity extends AppCompatActivity implements ChapterListAdapter.OnChapterClickListener, Toolbar.OnMenuItemClickListener, SwipeRefreshLayout.OnRefreshListener, PopupMenu.OnMenuItemClickListener, PageThumbAdapter.OnThumbClickListener {
    private final Activity thisActivity = this;

    private RecyclerView mChapterRecycler;
    private final Response.ErrorListener errorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Snackbar.make(mChapterRecycler, error.getMessage(), Snackbar.LENGTH_LONG).show();
        }
    };
    private RecyclerView mPageThumbRecycler;
    private TextView mBigChapterTitle;
    private ProgressBar mLoadingProgressbar;
    private ArrayList<Chapter> mChapterData;
    private ChapterListAdapter mChapterListAdapter;
    private PageThumbAdapter mPageThumbAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;

    private SuperSingleton singleton;
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
                            // Metadata is not the same, update it!
                            mChapterData.set(j, c);
                            mChapterListAdapter.notifyItemChanged(j);
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
            mChapterListAdapter.notifyItemRangeInserted(startingIndex, count);

            if (swipeRefreshLayout != null) {
                swipeRefreshLayout.setRefreshing(false);
            }

            final Date lastUpdate = latestUpdateTimes.lastUpdate(new Date());

            long diff;
            if (lastUpdate != null) {
                diff = new Date().getTime() - lastUpdate.getTime();
            } else {
                Log.e(App.TAG, "Invalid parameters! lastUpdate is null!");
                diff = -1;
            }

            int hours = Integer.valueOf(PreferenceManager.getDefaultSharedPreferences(thisActivity).getString("recent_page_notifier_max_time", "1"));
            // If the last update was less than 3 hours ago...
            if ((diff > 0 && diff <= 1000 * 60 * 60 * hours)) {
                Snackbar.make(swipeRefreshLayout, R.string.go_to_latest_update, Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.go, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                openLatestPage();
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
            DataPreferences.SaveCheck(thisActivity, check);

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
    /**
     * Called when downloading the check fails
     */
    private final Response.ErrorListener checkErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            int errorStringId;
            if (error != null && error.getCause() != null && error.getCause().getClass() == javax.net.ssl.SSLHandshakeException.class) {
                errorStringId = R.string.update_check_failed_hackers_on_the_loose;
            } else {
                errorStringId = R.string.update_check_failed;
            }

            Snackbar.make(mChapterRecycler, errorStringId, Snackbar.LENGTH_INDEFINITE)
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
            Snackbar.make(mChapterRecycler, R.string.chapter_list_download_failed, Snackbar.LENGTH_INDEFINITE)
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
    private LinearLayoutManager mChapterListLayoutManager;

    /**
     * Open the latest page
     */
    private void openLatestPage() {
        Chapter latestChapter = ChapterDatabase.getLastChapter();

        View chapterView = mChapterRecycler.getChildAt(mChapterRecycler.getChildCount() - 1);
        onChapterSelect(chapterView, latestChapter, DataPreferences.getLatestPage(this));
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chapter_list);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        assert toolbar != null;
        toolbar.setOnMenuItemClickListener(this);

        swipeRefreshLayout = findViewById(R.id.swiperefresh);
        assert swipeRefreshLayout != null;
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(
                R.color.mike,
                R.color.lucy,
                R.color.david,
                R.color.daisy,
                R.color.paulo
        );

        CoordinatorLayout mCoordinatorLayout = findViewById(R.id.coordinator);

        singleton = SuperSingleton.getInstance(this);
        mLoadingProgressbar = findViewById(R.id.emptyListSpinner);

        assert mLoadingProgressbar != null;
        assert  mCoordinatorLayout != null;

        SetupData();
        SetupChapterListRecycler();
        SetupPageThumbRecycler();
        CheckOrSubmitCrashLogs();
    }

    /**
     * Check for crash logs, ask permission to send
     */
    private void CheckOrSubmitCrashLogs() {
        final AppCrashStorage appCrashStorage = new AppCrashStorage(this);
        File[] crashFiles = appCrashStorage.getCrashFiles();

        if (crashFiles.length == 0)
            return;

        AlertDialog alertDialog = new AlertDialog
                .Builder(this, R.style.Theme_AppCompat_Light_Dialog_Alert)
                .setTitle(R.string.crash_dialog_title)
                .setMessage(R.string.confirm_send_crash_report)
                .setPositiveButton(R.string.send_crash_reports, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        appCrashStorage.send();
                    }
                })
                .setNegativeButton(R.string.dont_send_crash_reports, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        appCrashStorage.deleteReports();
                    }
                })
                .show();
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
    private void SetupChapterListRecycler() {
        mChapterRecycler = findViewById(R.id.chapterList);

        mChapterListLayoutManager = new LinearLayoutManager(this);
        mChapterRecycler.setLayoutManager(mChapterListLayoutManager);

        boolean isSortDescending = getSortDescending();
        setSortDescending(isSortDescending);

        mChapterListAdapter = new ChapterListAdapter(this, mChapterData, this);
        mChapterRecycler.setAdapter(mChapterListAdapter);
    }

    /**
     * Prepare the RecyclerView for showing page thumbs (in tablet mode)
     */
    private void SetupPageThumbRecycler() {
        mPageThumbRecycler = findViewById(R.id.page_thumb_recycler);
        if (mPageThumbRecycler == null)
            return;

        mBigChapterTitle = findViewById(R.id.chapter_list_title);

        GridLayoutManager mPageThumbLayoutManager = new GridLayoutManager(this, getResources().getInteger(R.integer.page_thumb_column_count));
        mPageThumbRecycler.setLayoutManager(mPageThumbLayoutManager);

        mPageThumbAdapter = new PageThumbAdapter(this, null, this);
        mPageThumbRecycler.setAdapter(mPageThumbAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_chapter_list, menu);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
            int adb = Settings.Secure.getInt(this.getContentResolver(),
                    Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0);
            menu.findItem(R.id.menu_debug).setVisible(adb != 0);
        }

        if (DataPreferences.getLastReadChapterNumber(this) < 1) {
            menu.findItem(R.id.menu_continue_reading).setVisible(false);
        }

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
    public void onChapterSelect(final View view, final Chapter chapter, int page) {
        final int adjustedPage;

        if (mPageThumbRecycler == null && page <= 0)
            adjustedPage = 1;
        else
            adjustedPage = page;

        if (mPageThumbRecycler == null || page > 0) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Intent fullScreenIntent = new Intent(thisActivity, FullscreenReaderActivity.class);
                    fullScreenIntent.putExtra(FullscreenReaderActivity.EXTRA_CHAPTER, chapter);
                    fullScreenIntent.putExtra(FullscreenReaderActivity.EXTRA_PAGE, adjustedPage);

                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP && view != null) {
                        //View titleView = view.findViewById(R.id.title);
                        View titleView = thisActivity.findViewById(R.id.toolbar);
                        Pair<View, String> titlePair = Pair.create(titleView, titleView.getTransitionName());
                        Pair<View, String> entireViewPair = Pair.create(view, view.getTransitionName());

                        @SuppressWarnings("unchecked") Bundle bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(thisActivity, entireViewPair, titlePair).toBundle();

                        startActivity(fullScreenIntent, bundle);
                    } else {
                        //TODO: Make a nice transition here
                        startActivity(fullScreenIntent);
                    }
                }
            });
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mBigChapterTitle.setText(String.format(getString(R.string.chapter_title_big), API.FormatChapterNumber(chapter.getNumber()), chapter.getTitle()));
                    mPageThumbAdapter.setChapter(chapter);
                }
            });
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.menu_settings:
                Intent settingsIntent = new Intent(thisActivity, SettingsActivity.class);
                startActivity(settingsIntent);
                break;
            case R.id.menu_debug:
                SuperSingleton.getInstance(this)
                        .getImageLoader()
                        .get(API.FormatPageUrl(this, DataPreferences.getLatestChapterNumber(this), DataPreferences.getLatestPage(this), "@m"), new ImageLoader.ImageListener() {
                            @Override
                            public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                                Bitmap bmp = response.getBitmap();
                                if (bmp == null) {
                                    Log.d(App.TAG, "Could not load bitmap for notification!");
                                } else {
                                    new NotificationService().DisplayNotification(bmp);
                                    DataPreferences.setLastNotificationDate(ChapterListActivity.this);
                                }
                            }

                            @Override
                            public void onErrorResponse(VolleyError error) {
                                new NotificationService().DisplayNotification(null);
                            }
                        });
                break;
            case R.id.menu_continue_reading: {
                double chapterNr = DataPreferences.getLastReadChapterNumber(this);
                int pageNr = DataPreferences.getLastReadPageNumber(this);
                Chapter continueChapter = new Select()
                        .from(Chapter.class)
                        .where(Chapter_Table.number.eq(chapterNr))
                        .querySingle();
                this.onChapterSelect(null, continueChapter, pageNr);
            }
            break;
            case R.id.go_to_latest_page:
                openLatestPage();
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

        mChapterListLayoutManager.setReverseLayout(descending);
        mChapterListLayoutManager.setStackFromEnd(descending);
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

    @Override
    public void onThumbnailClick(Chapter chapter, int page, View clickedView) {
        onChapterSelect(clickedView, chapter, page);
    }
}
