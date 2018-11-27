package com.cs407.noted;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class NotificationActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private List<String> data = new ArrayList<>();;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Notifications");

        recyclerView = (RecyclerView) findViewById(R.id.notifications_recycler_view);
        recyclerView.setHasFixedSize(true);

        // use a linear layout manager
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        // specify an adapter (see also next example)
        adapter = new NotificationAdapter(data);
        recyclerView.setAdapter(adapter);

        data.add("kyle.n.burke@gmai.com shared 'bla bla' with you");
        data.add("I'm gay");
        data.add("Another test this time, now it's really long so we'll see what heppends my due. blablablabalbaballabalbalblablablablablabalblablabbablablalba");

        adapter.notifyDataSetChanged();
    }
}
