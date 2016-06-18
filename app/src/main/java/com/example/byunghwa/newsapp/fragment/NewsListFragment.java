package com.example.byunghwa.newsapp.fragment;


import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.byunghwa.newsapp.R;
import com.example.byunghwa.newsapp.adapter.CustomStaggeredGridLayoutManager;
import com.example.byunghwa.newsapp.adapter.NewsListRecyclerViewAdapter;
import com.example.byunghwa.newsapp.model.News;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;


/**
 * A simple {@link Fragment} subclass.
 */
public class NewsListFragment extends Fragment implements NewsListRecyclerViewAdapter.OnItemClickListener {

    private final String TAG = "NewsListFrag";

    private static SwipeRefreshLayout mSwipeRefreshLayout;
    private static RecyclerView recyclerView;
    private static TextView emptyView;
    private static NewsListRecyclerViewAdapter adapter;
    private CustomStaggeredGridLayoutManager manager;

    private static ArrayList<News> mNewsList;

    public NewsListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            fetchNewsList();
        } else {
            if (savedInstanceState.containsKey("newslist")) {
                mNewsList = savedInstanceState.getParcelableArrayList("newslist");
            } else {
                fetchNewsList();
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mNewsList != null) {
            outState.putParcelableArrayList("newslist", mNewsList);
        }
    }

    private void fetchNewsList() {
        new FetchNewsAsyncTask().execute();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_news_list, container, false);
        initRecyclerView(rootView);
        initSwipeRefreshLayout(rootView);
        initEmptyView(rootView);
        initToolbar(rootView);
        setOnClickListener();
        return rootView;
    }

    private void initToolbar(View rootView) {
        Toolbar toolbar = (Toolbar) rootView.findViewById(R.id.toolbar_main);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.setSupportActionBar(toolbar);
        ActionBar actionBar = activity.getSupportActionBar();
        actionBar.setTitle(getResources().getString(R.string.app_name));
    }

    private void setOnClickListener() {
        adapter.setOnItemClickListener(this);
    }

    private void initEmptyView(View rootView) {
        emptyView = (TextView) rootView.findViewById(R.id.empty_view);
    }

    private void initSwipeRefreshLayout(View rootView) {
        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_refresh_layout);
        mSwipeRefreshLayout.setRefreshing(true);
        /*
         note that when you use a SwipeRefreshLayout, you have to set this OnRefreshListener to it
         or the indicator wouldn't show up
          */
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchNewsList();
            }
        });
    }

    private void initRecyclerView(View rootView) {
        recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler);

        recyclerView.setHasFixedSize(true);// setting this to true will prevent the whole list from refreshing when
        // new items have been added to the list (which prevents list from flashing)

        adapter = new NewsListRecyclerViewAdapter(getActivity());
        recyclerView.setAdapter(adapter);

        int columnCount = 2;
        manager = new CustomStaggeredGridLayoutManager(columnCount, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(manager);

        if (mNewsList != null) {
            Log.i("NewsListFrag", "restoring list...");
            adapter.swapData(mNewsList);
        }
    }

    @Override
    public void onItemClick(View view, int clickedItemPosition) {
        if (mNewsList != null) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(mNewsList.get(clickedItemPosition).getUrl()));
            if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                startActivity(intent);
            }
        }
    }

    private static class FetchNewsAsyncTask extends AsyncTask<Void, Void, ArrayList<News>> {

        @Override
        protected ArrayList<News> doInBackground(Void... params) {
            HttpURLConnection urlConnection = null;
            JSONObject json = null;
            JSONArray jsonArray = null;
            ArrayList<News> newsArrayList = null;
            try {
                URL url = new URL("https://ajax.googleapis.com/ajax/services/feed/find?v=1.0&q=animal");
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                String response = "";

                //start listening to the stream
                Scanner inStream = new Scanner(in);

                //process the stream and store it in StringBuilder
                while (inStream.hasNextLine())
                    response += (inStream.nextLine());
                json = new JSONObject(response);
                json = json.getJSONObject("responseData");
                jsonArray = json.getJSONArray("entries");
                newsArrayList = jsonArrayToNewsArrayList(jsonArray);
                Log.i("NewsListFrag", "response: " + response);
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                urlConnection.disconnect();
            }

            return newsArrayList;
        }

        private ArrayList<News> jsonArrayToNewsArrayList(JSONArray jsonArray) throws JSONException {
            JSONObject jsonObject;
            ArrayList<News> newsArrayList = new ArrayList<>();
            News news;
            for (int i=jsonArray.length()-1;i>=0;i--) {
                jsonObject = jsonArray.getJSONObject(i);
                news = new News();
                news.setTitle(jsonObject.getString("title"));
                Log.i("NewsListFrag", "title: " + jsonObject.getString("title"));
                news.setContentSnippet(jsonObject.getString("contentSnippet"));
                Log.i("NewsListFrag", "content snippet: " + jsonObject.getString("contentSnippet"));
                news.setUrl(jsonObject.getString("url"));
                Log.i("NewsListFrag", "url: " + jsonObject.getString("url"));

                newsArrayList.add(news);
            }
            return newsArrayList;
        }

        @Override
        protected void onPostExecute(ArrayList<News> newsArrayList) {
            mNewsList = newsArrayList;
            adapter.swapData(newsArrayList);
            mSwipeRefreshLayout.setRefreshing(false);
            if (newsArrayList != null) {
                if (newsArrayList.size() > 0) {
                    emptyView.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                } else {
                    emptyView.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                }
            } else {
                emptyView.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            }
        }
    }

}
