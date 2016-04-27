package com.finxl.finxlsample;

import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.finxl.finxlsample.model.Facts;
import com.finxl.finxlsample.util.Constants;
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
        initializeViews();
        initializeListeners();
        new GetFactsTask().execute();
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
        mRefreshLayout.setOnRefreshListener(this);
    }

    @Override
    public void onRefresh() {
        mRefreshLayout.setRefreshing(true);
        new GetFactsTask().execute();
    }

    private class GetFactsTask extends AsyncTask<String, String, Facts> {
        private String mToken;
        private ServerUtilities mServerUtil;

        public GetFactsTask() {
            mServerUtil = ServerUtilities.getInstance(getApplicationContext());
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
            if(result != null) {
                mLoadingText.setVisibility(View.GONE);
                mFactsListView.setVisibility(View.VISIBLE);
                // Set the title to the action bar title.
                getActionBar().setTitle(result.getTitle());
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
            mLoadingText.setVisibility(View.VISIBLE);
            mFactsListView.setVisibility(View.GONE);
        }
    }
}
