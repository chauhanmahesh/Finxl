package com.finxl.finxlsample;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.finxl.finxlsample.model.Fact;

import java.util.List;

/**
 * Created by Mahesh Chauhan on 4/27/2016.
 *
 * A custom adapter which takes care of rendering the data over the listview.
 * It takes the list of all facts to be displayed and renders it on the porper UI components.
 */
public class FactsListAdapter extends BaseAdapter {
    private Activity mActivity;
    private static LayoutInflater mInflator = null;
    // INstance of Image loader to load the images.
    private FactsImageLoader mImageLoader = null;
    // List of fact to be displayed.
    private List<Fact> mFacts;

    public FactsListAdapter(Activity activity) {
        mActivity = activity;
        mInflator = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mImageLoader = new FactsImageLoader(mActivity.getApplicationContext());
    }

    public void setFacts(List<Fact> facts) {
        mFacts = facts;
    }

    @Override
    public int getCount() {
        return mFacts.size();
    }

    public static class ViewHolder {
        public TextView title;
        public TextView desc;
        public ImageView factImage;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        ViewHolder holder;

        if(convertView == null) {
            // Let's inflate the view.
            view = mInflator.inflate(R.layout.activity_finxl_list_row, null);
            holder = new ViewHolder();
            holder.title = (TextView) view.findViewById(R.id.factTitle);
            holder.desc = (TextView) view.findViewById(R.id.factDesc);
            holder.factImage = (ImageView) view.findViewById(R.id.factImage);
            // Tag the holder for recycling.
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        // Let's set the fact data now.
        Fact fact = mFacts.get(position);
        holder.title.setText(fact.getTitle());
        holder.desc.setText(fact.getDescription());
        ImageView image = holder.factImage;
        // Tell the image loader to load and display the image.
        mImageLoader.loadAndDisplayImage(fact.getImageUrl(), image);
        return view;
    }

}
