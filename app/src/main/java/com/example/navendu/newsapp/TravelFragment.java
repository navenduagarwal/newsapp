package com.example.navendu.newsapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

/**
 * Created by navendu on 7/2/2016.
 */
public class TravelFragment extends Fragment {
    private String category;
    private NewsAdapter mNewsListAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Add this line in order for this fragment to handle main events.
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            updateCategoryResults();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void updateCategoryResults() {
        FetchNewsTask newsTask = new FetchNewsTask(mNewsListAdapter);
        category = "travel";
        newsTask.execute(category);
    }

    @Override
    public void onStart() {
        super.onStart();
        updateCategoryResults();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.news_list, container, false);

        //The ArrayAdapter will take the data from a source and
        // use it to populate the ListView it's attached
        mNewsListAdapter = new NewsAdapter(getContext(), new ArrayList<News>());
        ListView listView = (ListView) rootView.findViewById(R.id.listview_news);
        listView.setAdapter(mNewsListAdapter);
        //Creating intent to open web browser for url on click
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                String url = mNewsListAdapter.getItem(position).getNewsURL();
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                startActivity(intent);
            }
        });

        return rootView;
    }
}
