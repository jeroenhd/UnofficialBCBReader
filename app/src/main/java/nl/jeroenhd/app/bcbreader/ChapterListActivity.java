package nl.jeroenhd.app.bcbreader;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import java.util.ArrayList;

import nl.jeroenhd.app.bcbreader.data.Chapter;

public class ChapterListActivity extends AppCompatActivity {
    private RecyclerView mRecycler;
    private RecyclerView.LayoutManager mLayoutManager;
    private ArrayList<Chapter> mChapterData;
    private ChapterListAdapter mAdapter;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chapter_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

                Intent chapterTestIntent = new Intent(getApplicationContext(), ChapterReadingActivity.class);
                startActivity(chapterTestIntent);
            }
        });

        SetupDummyData();
        SetupRecycler();
    }

    void SetupDummyData()
    {
        mChapterData = new ArrayList<>();
        int number;
        for (int i = 0; i < 92; i++)
        {
            number = i+1;
            mChapterData.add(new Chapter("Dummy chapter #" + number, "The description for chapter #" + number, 30, 30, "Some time ago", i));
        }
    }

    void SetupRecycler()
    {
        mRecycler = (RecyclerView) findViewById(R.id.chapterList);

        mLayoutManager = new LinearLayoutManager(this);
        mRecycler.setLayoutManager(mLayoutManager);

        mAdapter = new ChapterListAdapter(this, mChapterData);
        mRecycler.setAdapter(mAdapter);
    }

}
