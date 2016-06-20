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
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
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
    private FetchNewsAsyncTask asyncTask;

    public NewsListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null && savedInstanceState.containsKey("newslist")) {
            mNewsList = savedInstanceState.getParcelableArrayList("newslist");
        } else {
            fetchNewsList();
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
        asyncTask = new FetchNewsAsyncTask();
        asyncTask.execute();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_news_list, container, false);

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

        emptyView = (TextView) rootView.findViewById(R.id.empty_view);

        Toolbar toolbar = (Toolbar) rootView.findViewById(R.id.toolbar_main);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.setSupportActionBar(toolbar);
        ActionBar actionBar = activity.getSupportActionBar();
        actionBar.setTitle(getResources().getString(R.string.app_name));

        setOnClickListener();
        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (asyncTask != null) {
            asyncTask.cancel(true);
        }
    }

    private void setOnClickListener() {
        adapter.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(View view, int clickedItemPosition) {
        if (mNewsList != null) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(mNewsList.get(clickedItemPosition).getLink()));
            if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                startActivity(intent);
            }
        }
    }

    private static class FetchNewsAsyncTask extends AsyncTask<Void, Void, ArrayList<News>> {

        @Override
        protected ArrayList<News> doInBackground(Void... params) {
            JSONArray jsonArray = null;
            ArrayList<News> newsArrayList = null;
            HttpURLConnection connection = null;
            try {
                URL url = new URL("https://ajax.googleapis.com/ajax/services/feed/find?" +
                        "v=1.0&q=animal");
                connection = (HttpURLConnection) url.openConnection();

                String line;
                StringBuilder builder = new StringBuilder();
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                while((line = reader.readLine()) != null) {
                    builder.append(line);
                }

                JSONObject json = new JSONObject(builder.toString());

                json = json.getJSONObject("responseData");
                jsonArray = json.getJSONArray("entries");
                json = jsonArray.getJSONObject(6);
                String feedUrl = json.getString("url");

                Log.i("NewsListFrag", "feed url: " + feedUrl);

                URL urlSecond = new URL(feedUrl);
                connection = (HttpURLConnection) urlSecond.openConnection();
                InputStream in = new BufferedInputStream(connection.getInputStream());
                String response = "";

                //start listening to the stream
                Scanner inStream = new Scanner(in);

                //process the stream and store it in StringBuilder
                while (inStream.hasNextLine())
                    response += (inStream.nextLine());

                Log.i("NewsListFrag", "response: " + response);

                newsArrayList = feedXMLToNewsArrayList(response);
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }

            return newsArrayList;
        }

        private ArrayList<News> feedXMLToNewsArrayList(String response) {
            XmlPullParserFactory factory;
            ArrayList<News> arrayList = null;
            try {
                factory = XmlPullParserFactory.newInstance();
                factory.setNamespaceAware(true);
                XmlPullParser xpp = factory.newPullParser();

                xpp.setInput(new StringReader(response));
                int eventType = xpp.getEventType();
                News news = null;
                arrayList = new ArrayList<>();
                String text = null;
                int counter = 0;
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if(eventType == XmlPullParser.START_DOCUMENT) {
                        Log.i("NewsListFrag", "Start document");
                    } else if(eventType == XmlPullParser.START_TAG) {
                        Log.i("NewsListFrag", "Start tag: " + xpp.getName());
                        if (xpp.getName().equals("item")) {
                            counter++;
                            news = new News();
                        }
                    } else if(eventType == XmlPullParser.END_TAG) {
                        Log.i("NewsListFrag", "End tag: " + xpp.getName());
                        if (counter != 0) {
                            if (xpp.getName().equals("title")) {
                                news.setTitle(text);
                            }
                            if (xpp.getName().equals("pubDate")) {
                                news.setPubDate(text);
                            }
                            if (xpp.getName().equals("link")) {
                                news.setLink(text);
                            }
                            if (xpp.getName().equals("item")) {
                                arrayList.add(news);
                            }
                        }

                    } else if(eventType == XmlPullParser.TEXT) {
                        Log.i("NewsListFrag", "Text: " + xpp.getText());
                        text = xpp.getText();
                    }
                    eventType = xpp.next();
                }
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return arrayList;
        }

        @Override
        protected void onPostExecute(ArrayList<News> newsArrayList) {
            mNewsList = newsArrayList;
            //Log.i("NewsListFrag", "news list size: " + newsArrayList.size());
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
