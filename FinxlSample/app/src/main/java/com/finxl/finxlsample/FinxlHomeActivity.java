package com.finxl.finxlsample;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.finxl.finxlsample.model.Fact;
import com.finxl.finxlsample.model.Facts;
import com.finxl.finxlsample.util.Constants;
import com.finxl.finxlsample.util.FinxlUtils;
import com.finxl.finxlsample.util.JsonUtil;
import com.finxl.finxlsample.util.ServerUtilities;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

/**
 * Created by Mahesh Chauhan on 4/27/2016.
 *
 * This is the main activity responsible for loading all the facts and displaying all
 * in the listview.
 * The facts are downloaded as a json data from the link as mentioned below:
 * https://dl.dropboxusercontent.com/u/746330/facts.json
 *
 * After parsing, the images will be downloaded seperately as otherwise it will take
 * time to display the data. Images will be displayed in the listview as soon as it will be downloaded.
 * In addtion, there will be cache for taking care of images and if repeated the cached image will be
 * taken to display rather than again downloading the image.
 */
public class FinxlHomeActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    // Initializing variables.
    private ListView mFactsListView;
    private TextView mLoadingText;
    private FactsListAdapter mListAdapter;
    private SwipeRefreshLayout mRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finxl_home);
        // Initialize the views now.
        initializeViews();
        // Initialize the listeners.
        initializeListeners();
        // Lets load the Facts first time.
        Context context = getApplicationContext();
        if(ServerUtilities.getInstance(context).isNetworkAvailable(context)) {
            new GetFactsTask(false).execute();
        } else {
            // If no connection available, show the error.
            showConnectionError();
        }
    }

    /**
     * Initialize the views.
     */
    private void initializeViews() {
        mFactsListView = (ListView) findViewById(R.id.factsList);
        mLoadingText = (TextView) findViewById(R.id.loadingText);
        mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.refreshLayout);
        mListAdapter = new FactsListAdapter(this);
    }

    private void initializeListeners() {
        // Initialize the swipedown refresh listener.
        mRefreshLayout.setOnRefreshListener(this);
    }

    @Override
    public void onRefresh() {
        // User has swiped down, let;s get the data.
        Context context = getApplicationContext();
        if(ServerUtilities.getInstance(context).isNetworkAvailable(context)) {
            new GetFactsTask(true).execute();
        } else {
            showConnectionError();
        }
    }

    /**
     * AsyncTask to get all the facts from {@literal Constants.FACTS_DOWNLOAD_LINK}.
     * After fetching the data, we parses the data and send it to the listadapter which then displays
     * the data in the listview.
     */
    private class GetFactsTask extends AsyncTask<String, String, Facts> {
        private ServerUtilities mServerUtil;
        // Boolean to take care of whether to show the refresh progress or not
        // It going to be displayed only when there is a swipe down refresh.
        private boolean mDisplayRefresh;

        public GetFactsTask(boolean refreshIcon) {
            mServerUtil = ServerUtilities.getInstance(getApplicationContext());
            mDisplayRefresh = refreshIcon;
        }
        @Override
        protected Facts doInBackground(String... params) {
            try {
                // Send the request to ServerUtilities to fetch the Json Data.
                String response = mServerUtil.sendGetRequest(Constants.FACTS_DOWNLOAD_LINK);
                // Send it to JsonParser to parse the response.
                if(!TextUtils.isEmpty(response)) {
                    Facts facts = (Facts) JsonUtil.parseJsonToType(getApplicationContext(),
                            new JSONObject(response), new TypeToken<Facts>() {
                    }.getType());
                    return facts;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return null;
        }

        /*
         * (non-Javadoc)
         *
         * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
         */
        @Override
        protected void onPostExecute(Facts result) {
            if(mDisplayRefresh) {
                mRefreshLayout.setRefreshing(false);
            }
            if(result != null) {
                mLoadingText.setVisibility(View.GONE);
                mFactsListView.setVisibility(View.VISIBLE);
                // Set the title to the action bar title.
                getSupportActionBar().setTitle(result.getTitle());
                mListAdapter.setFacts(result.getFacts());
                mFactsListView.setAdapter(mListAdapter);
            }
        }

        /*
         * (non-Javadoc)
         *
         * @see android.os.AsyncTask#onPreExecute()
         */
        @Override
        protected void onPreExecute() {
            if(mDisplayRefresh) {
                mRefreshLayout.setRefreshing(true);
            }
            mLoadingText.setVisibility(View.VISIBLE);
            mLoadingText.setText(getString(R.string.loadingText));
            mFactsListView.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                // Action button is tapped.
                Context context = getApplicationContext();
                if(ServerUtilities.getInstance(context).isNetworkAvailable(context)) {
                    new GetFactsTask(false).execute();
                } else {
                    showConnectionError();
                }
                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.finxl_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Shows the connection error to display proper message to the user.
     */
    private void showConnectionError() {
        mLoadingText.setVisibility(View.VISIBLE);
        mLoadingText.setText(getString(R.string.connectionError));
        mFactsListView.setVisibility(View.GONE);
    }
}
