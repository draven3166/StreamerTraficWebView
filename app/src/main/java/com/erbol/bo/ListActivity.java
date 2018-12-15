package com.erbol.bo;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.erbol.bo.Adapters.RadioAdapter;
import com.erbol.bo.Adapters.SearchListener;
import com.erbol.bo.DataModel.DBAdapter;
import com.erbol.bo.DataModel.Radios;
import com.erbol.bo.Utils.ConstantsUtil;
import com.erbol.bo.Utils.ServiceUtil;
import com.erbol.bo.Utils.StateEthernet;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class ListActivity extends AppCompatActivity {
    private SearchView mSearchView;
    private ListView mListView;
    private TextView tvNotf;
    private RadioAdapter mAdapter;
    private ArrayList<Radios> radios;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD) {
            StrictMode.ThreadPolicy tp = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(tp);
        }
        loadDataRadio.execute();
        tvNotf = (TextView)findViewById(R.id.tv_notif);
        mListView = (ListView) findViewById(R.id.lv_radios);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_list, menu);
        mSearchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.option_search));
        mSearchView.setOnQueryTextListener(new SearchListener(this));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
//            case R.id.action_settings:
//                return true;
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void listRadios(){
        DBAdapter queryRad = new DBAdapter(ListActivity.this);
        queryRad.open();
        radios = new ArrayList<Radios>();
        radios = queryRad.getRadios("*");
        queryRad.close();
        if (radios.size()>0){
            tvNotf.setVisibility(View.GONE);
            mAdapter = new RadioAdapter(ListActivity.this, radios);
            mListView.setAdapter(mAdapter);
            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                    if (!((TextView) v.findViewById(R.id.tv_format)).getText().toString().equals("mp3")){
                        Intent irad = new Intent(ListActivity.this, PlayerActivityAAC.class);
                        irad.putExtra(ConstantsUtil.POINTRADIO, parent.getItemIdAtPosition(position));
                        startActivity(irad);
                    } else {
                        Intent irad = new Intent(ListActivity.this, PlayerActivityMP3.class);
                        irad.putExtra(ConstantsUtil.POINTRADIO, parent.getItemIdAtPosition(position));
                        startActivity(irad);
                    }
                }
            });
        } else{
            tvNotf.setVisibility(View.VISIBLE);
        }
    }

    AsyncTask<Void, Void, Boolean> loadDataRadio = new AsyncTask<Void, Void, Boolean>() {
        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(ListActivity.this);//, R.style.CustomDialog
            progressDialog.setCancelable(false);
            progressDialog.setIcon(android.R.drawable.ic_dialog_alert);
            progressDialog.setTitle(getResources().getString(R.string.dlg_title));
            progressDialog.setMessage(getResources().getString(R.string.dlg_msn));
            progressDialog.setButton(getResources().getString(R.string.txt_cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    progressDialog.cancel();
                }
            });
            progressDialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... param) {
            Boolean conexion = false;
            if (StateEthernet.verificaConexion(ListActivity.this)) {
                try {
                    DBAdapter dbrad = new DBAdapter(ListActivity.this);
                    dbrad.open();
                    dbrad.deleteRadios();
                    String timeline = ServiceUtil.getDatajson();
                    JSONObject jsonResponse = new JSONObject(timeline);

                    JSONArray jsonArray = jsonResponse.getJSONArray("red");
                    JSONObject jsonObject;
                    for (int i = 0; i < jsonArray.length(); i++) {
                        jsonObject = (JSONObject) jsonArray.get(i);
                        dbrad.addRadios(jsonObject.getInt(ConstantsUtil.TAG_IRAD),
                                jsonObject.getString(ConstantsUtil.TAG_NAME),
                                jsonObject.getString(ConstantsUtil.TAG_LOGO),
                                jsonObject.getString(ConstantsUtil.TAG_STREAM),
                                jsonObject.getString(ConstantsUtil.TAG_WEB),
                                jsonObject.getJSONArray(ConstantsUtil.TAG_CITY).getString(0),
                                jsonObject.getString(ConstantsUtil.TAG_FREQUENCY),
                                jsonObject.getString(ConstantsUtil.TAG_FORMAT));
                    }//jsonObject.getString(ConstantsUtil.TAG_CITY),
                    dbrad.close();
                    conexion = true;
                } catch (Exception e) {
                    Log.e("JSON error: ", Log.getStackTraceString(e));
                    conexion = false;
                }
            } else{
                conexion = false;
            }
            return conexion;
        }

        @Override
        protected void onPostExecute(Boolean results) {
            progressDialog.dismiss();
            if (results){
                Toast.makeText(ListActivity.this, getResources().getString(R.string.txt_load), Toast.LENGTH_LONG).show();
            }else{
                Toast.makeText(ListActivity.this, getResources().getString(R.string.txt_error), Toast.LENGTH_LONG).show();
            }
            listRadios();
        }
    };
}