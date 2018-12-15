package com.erbol.bo.Adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.erbol.bo.DataModel.Conflicts;
import com.erbol.bo.R;

import java.util.ArrayList;

public class ConflictAdapter extends BaseAdapter {
    protected Activity activity;
    protected ArrayList<Conflicts> items;
    public ConflictAdapter(Activity activity, ArrayList<Conflicts> items) {
        this.activity = activity;
        this.items = items;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return items.get(position).getIdc();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View vi=convertView;
        if(convertView == null) {
            LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            vi = inflater.inflate(R.layout.layout_listc, null);
        }
        Conflicts itemR = items.get(position);
        TextView tvTitulo = (TextView) vi.findViewById(R.id.tv_sector);
        tvTitulo.setText(itemR.getSector());
        TextView tvSubtitulo = (TextView) vi.findViewById(R.id.tv_cause);
        tvSubtitulo.setText(itemR.getCause());
        return vi;
    }
}