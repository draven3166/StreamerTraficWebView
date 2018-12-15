package com.erbol.bo.Adapters;

import android.app.Activity;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.erbol.bo.Adapters.RadioAdapter;
import com.erbol.bo.DataModel.DBAdapter;
import com.erbol.bo.DataModel.Radios;
import com.erbol.bo.R;

import java.util.ArrayList;

public class SearchListener implements SearchView.OnQueryTextListener {
    private ArrayList<Radios> radios;
    private RadioAdapter mAdapter;
    private Activity activity;
    private TextView tvNotf;

    public SearchListener(Activity activity) {
        this.activity = activity;
    }

    public boolean onQueryTextChange(String newText) {
        DBAdapter queryRad = new DBAdapter(activity);
        queryRad.open();
        radios = new ArrayList<Radios>();
        ListView mListView = (ListView) activity.findViewById(R.id.lv_radios);
        tvNotf = (TextView)activity.findViewById(R.id.tv_notif);
        if (TextUtils.isEmpty(newText)) {
            mListView.clearTextFilter();
            radios = queryRad.getRadios("*");
            if (radios.size()==0){
                tvNotf.setVisibility(View.VISIBLE);
            }else{
                tvNotf.setVisibility(View.GONE);
            }
            mAdapter = new RadioAdapter(activity, radios);
            mListView.setAdapter(mAdapter);
        } else {
            radios = queryRad.getRadios(newText.toString());
            if (radios.size()==0){
                tvNotf.setVisibility(View.VISIBLE);
            }else{
                tvNotf.setVisibility(View.GONE);
            }
            mAdapter = new RadioAdapter(activity, radios);
            mListView.setAdapter(mAdapter);
        }
        queryRad.close();
        return true;
    }

    public boolean onQueryTextSubmit(String query) {
        return false;
    }
}
