package com.example.navendu.newsapp;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by navendu on 7/2/2016.
 */
public class ArtsFragment extends Fragment {
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
        FetchNewsTask newsTask = new FetchNewsTask();
        category = "artanddesign";
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

    public class FetchNewsTask extends AsyncTask<String, Void, ArrayList<News>> {
        private final String LOG_TAG = FetchNewsTask.class.getSimpleName();

        private ArrayList<News> getNewsDataFromJson(String newsJsonStr)
                throws JSONException {
            final String OWM_RESPONSE = "response";
            final String OWM_RESULTS = "results";
            final String OWM_TITLE = "webTitle";
            final String OWM_TAGS = "tags";
            final String OWM_TYPE = "type";
            final String OWM_FIELDS = "fields";
            final String OWM_THUMBNAIL = "thumbnail";
            final String OWM_ARTICLE_URL = "webUrl";

            JSONObject newsResultJson = new JSONObject(newsJsonStr);
            JSONObject newsResponse = newsResultJson.getJSONObject(OWM_RESPONSE);
            JSONArray newsArray = newsResponse.getJSONArray(OWM_RESULTS);

            ArrayList<News> resultStrs = new ArrayList<>();

            for (int i = 0; i < newsArray.length(); i++) {
                String title;
                String author;
                String articleThumbnail;
                String articleURL;
                String type = "contributor";

                //Get JSON Object representing the News Article
                JSONObject newsArticle = newsArray.getJSONObject(i);

                //Get title
                title = newsArticle.getString(OWM_TITLE);

                //Get Web URL
                articleURL = newsArticle.getString(OWM_ARTICLE_URL);

                //Get tags object as child
                if (newsArticle.has(OWM_TAGS)) {
                    JSONArray tagsArray = newsArticle.getJSONArray(OWM_TAGS);
                    JSONObject tagsObjectFirst = tagsArray.getJSONObject(0);
                    //Get type and webTitle aka Author string from fields object
                    if (tagsObjectFirst.getString(OWM_TYPE).equals(type)) {
                        author = tagsObjectFirst.getString(OWM_TITLE);
                    } else {
                        author = null;
                    }
                    for (int j = 1; j < tagsArray.length(); j++) {
                        JSONObject tagsObject = tagsArray.getJSONObject(j);
                        //Get type and webTitle aka Author string from fields object
                        if (tagsObject.getString(OWM_TYPE).equals(type)) {
                            if (author.equals(null)) {
                                author = tagsObject.getString(OWM_TITLE);
                            } else {
                                author = author + " , " + tagsObject.getString(OWM_TITLE);
                            }
                        }
                    }
                } else {
                    author = null;
                }

                //Get fields object as child
                if (newsArticle.has(OWM_FIELDS)) {
                    JSONObject fieldsObject = newsArticle.getJSONObject(OWM_FIELDS);

                    //Get thumbnail string from fields object
                    if (fieldsObject.has(OWM_THUMBNAIL)) {
                        articleThumbnail = fieldsObject.getString(OWM_THUMBNAIL);
                    } else {
                        articleThumbnail = null;
                    }
                } else articleThumbnail = null;


                News newNews = new News(title, author, articleThumbnail, articleURL);
                resultStrs.add(newNews);
            }
            return resultStrs;
        }

        @Override
        protected ArrayList<News> doInBackground(String... params) {

            //if no query keyword
            if (params.length == 0) {
                return null;
            }

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String newsJsonStr = null;

            try {

                final String SECTION_PARAM = "section";
                final String TAGS_PARAM = "show-tags";
                final String FIELDS_PARAM = "show-fields";
                final String ORDER_PARAM = "order-by";
                final String API_KEY_PARAM = "api-key";

                Uri.Builder builtUri = new Uri.Builder();
                builtUri.scheme("http")
                        .authority("content.guardianapis.com")
                        .appendPath("search")
                        .appendQueryParameter(SECTION_PARAM, params[0])
                        .appendQueryParameter(TAGS_PARAM, "contributor")
                        .appendQueryParameter(FIELDS_PARAM, "thumbnail,headline,short-url")
                        .appendQueryParameter(ORDER_PARAM, "newest")
                        .appendQueryParameter(API_KEY_PARAM, BuildConfig.NEWSDB_API_KEY)
                        .build();

                URL url = new URL(builtUri.toString());

                Log.v(LOG_TAG, "Built URL : " + builtUri.toString());

                // Create the request to Guardian API, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                newsJsonStr = buffer.toString();
                Log.i(LOG_TAG, "Downloaded Data " + newsJsonStr);

            } catch (IOException e) {
                e.printStackTrace();
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the book data, there's no point in attemping
                // to parse it.
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }

            }
            try {
                return getNewsDataFromJson(newsJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }
            // This will only happen if there was an error getting or parsing the forecast.
            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<News> result) {
            if (result != null) {
                mNewsListAdapter.clear();
                for (News newsJsonStr : result) {
                    mNewsListAdapter.add(newsJsonStr);
                }
                // New data is back from the server.  Hooray!
            }
        }
    }

}
