package nl.jeroenhd.app.bcbreader;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import java.util.ArrayList;

import nl.jeroenhd.app.bcbreader.data.Page;

public class ChapterReadingActivity extends AppCompatActivity {
    private RecyclerView mRecycler;
    private RecyclerView.LayoutManager mLayout;
    private ChapterReadingAdapter mAdapter;
    private ArrayList<Page> mPages;

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

        SetupData();
        SetupRecyclerView();
    }

    void SetupData()
    {
        mPages = new ArrayList<>();
        for (int i = 0; i < 20; i++)
        {
            mPages.add(new Page("Example commentary", i+1, 0.0));
        }
    }

    void SetupRecyclerView()
    {
        mRecycler = (RecyclerView) findViewById(R.id.pages);

        mLayout = new LinearLayoutManager(this);
        mRecycler.setLayoutManager(mLayout);

        mAdapter = new ChapterReadingAdapter(this, mPages);
        mRecycler.setAdapter(mAdapter);
    }
}
