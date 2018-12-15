package com.erbol.bo.Adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import com.erbol.bo.DataModel.Radios;
import com.erbol.bo.R;
import com.squareup.picasso.Picasso;

public class RadioAdapter extends BaseAdapter {
    protected Activity activity;
    protected ArrayList<Radios> items;
    public RadioAdapter(Activity activity, ArrayList<Radios> items) {
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
        return items.get(position).getIdr();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View vi=convertView;
        if(convertView == null) {
            LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            vi = inflater.inflate(R.layout.layout_list, null);
        }
        Radios itemR = items.get(position);
        ImageView ivLogo = (ImageView) vi.findViewById(R.id.image);
        Picasso.with(activity).load(itemR.getLogo()).into(ivLogo);
        TextView tvTitulo = (TextView) vi.findViewById(R.id.tv_name);
        tvTitulo.setText(itemR.getName());
        TextView tvSubtitulo = (TextView) vi.findViewById(R.id.tv_frequency);
        tvSubtitulo.setText(itemR.getFrequency());
        TextView tvFormat = (TextView) vi.findViewById(R.id.tv_format);
        tvFormat.setText(itemR.getFormat());
        return vi;
    }
}