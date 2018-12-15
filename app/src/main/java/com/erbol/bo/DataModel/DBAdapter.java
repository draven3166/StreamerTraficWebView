package com.erbol.bo.DataModel;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

public class DBAdapter {
	//Columnas de la tabla Radios
    private static final String TAG = "DBAdapter";
    public static final String RA_ID = "id";
    public static final String RA_NAME = "name";
    public static final String RA_LOGO = "logo_url";
    public static final String RA_STREAM = "url_stream";
    public static final String RA_WEB = "url_website";
    public static final String RA_CITY = "city";
    public static final String RA_FREQ = "radiofrequency";
    public static final String RA_FORMAT = "format";
    private static final String DATABASE_TABLE = "radios";

    //Columnas de la tabla Figures
    public static final String FI_IDC = "idc";
    public static final String FI_LAT = "latp";
    public static final String FI_LNG = "lngp";
    public static final String FI_RAD = "radc";
    public static final String FI_FILL = "fillf";
    public static final String FI_STROKE = "strokef";
    public static final String FI_TYPE = "typef";
    private static final String DATABASE_TABLEF = "figures";

    public static final String N_CONF = "numc";
    private static final String DATABASE_TABLEC = "conflicts";

    private static final String DATABASE_NAME = "erbol.db";
    private static final int DATABASE_VERSION = 1;
    
    private static final String DATABASE_CREATE =
            "create table "+DATABASE_TABLE+" (id long not null, "
            + "name text not null, "
            + "logo_url text not null, "
            + "url_stream text not null, "
            + "url_website text not null, "
            + "city text not null, "
            + "radiofrequency text not null, "
            + "format text not null);";

    private static final String DATABASE_CREATEF =
            "create table "+DATABASE_TABLEF+" (idc long not null, "
                    + "latp text not null, "
                    + "lngp text not null, "
                    + "radc long not null, "
                    + "fillf text not null, "
                    + "strokef text not null, "
                    + "typef text not null);";

    private static final String DATABASE_CREATEC =
            "create table "+DATABASE_TABLEC+" (numc int not null);";

    private Context context = null;  
    private DatabaseHelper DBHelper;
    private SQLiteDatabase db;

    public DBAdapter(Context ctx) {
        this.context = ctx;
        DBHelper = new DatabaseHelper(context);
    }
        
    private static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DATABASE_CREATE);
            db.execSQL(DATABASE_CREATEF);
            db.execSQL(DATABASE_CREATEC);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion
                    + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS radios");
            db.execSQL("DROP TABLE IF EXISTS figures");
            db.execSQL("DROP TABLE IF EXISTS conflicts");
            onCreate(db);
        }
    }
    
    public void open() throws SQLException {
        db = DBHelper.getWritableDatabase();
    }

    public void close() {
        DBHelper.close();
    }
    
    public long addRadios(int rid, String rname, String rlogo, String rstream, String rweb, String rcity, String rfrequency, String rformat){
        ContentValues initialValues = new ContentValues();
        initialValues.put(RA_ID, rid);
        initialValues.put(RA_NAME, rname);
        initialValues.put(RA_LOGO, rlogo);
        initialValues.put(RA_STREAM, rstream);
        initialValues.put(RA_WEB, rweb);
        initialValues.put(RA_CITY, rcity);
        initialValues.put(RA_FREQ, rfrequency);
        initialValues.put(RA_FORMAT, rformat);
        return db.insert(DATABASE_TABLE, null, initialValues);
    }

    public long addFigures(long ridc, String rlat, String rlng, long radf, String rfill, String rstroke, String rtype){
        ContentValues initialValues = new ContentValues();
        initialValues.put(FI_IDC, ridc);
        initialValues.put(FI_LAT, rlat);
        initialValues.put(FI_LNG, rlng);
        initialValues.put(FI_RAD, radf);
        initialValues.put(FI_FILL, rfill);
        initialValues.put(FI_STROKE, rstroke);
        initialValues.put(FI_TYPE, rtype);
        return db.insert(DATABASE_TABLEF, null, initialValues);
    }

    public ArrayList<Radios> getRadios(String rsearch)throws SQLException{
        ArrayList<Radios> mRadios=new ArrayList<Radios>();
        String query = "";
        if (rsearch!=null && rsearch.equals("*")){
            query = "SELECT id, name, logo_url, radiofrequency, format FROM '" +DATABASE_TABLE+ "'";
        }else {
            query = "SELECT id, name, logo_url, radiofrequency, format FROM " + DATABASE_TABLE + " WHERE name LIKE '%" + rsearch + "%'";
        }
        Cursor mCursor = db.rawQuery(query, null);
        if (mCursor != null) {
            while(mCursor.moveToNext()) {
                Radios rad = new Radios();
                rad.setIdr(mCursor.getLong(0));
                rad.setName(mCursor.getString(1));
                rad.setLogo(mCursor.getString(2));
                rad.setFrequency(mCursor.getString(3));
                rad.setFormat(mCursor.getString(4));
                mRadios.add(rad);
            }
        }
        return mRadios;
    }

    public Radios getRadio(long irad)throws SQLException{
        Radios mRadio = new Radios();
        String query = "SELECT name, logo_url, url_stream, url_website, city, radiofrequency FROM " + DATABASE_TABLE + " WHERE id='" + irad + "'";
        Cursor mCursor = db.rawQuery(query, null);
        if (mCursor != null) {
            if(mCursor.moveToNext()) {
                mRadio.setName(mCursor.getString(0));
                mRadio.setLogo(mCursor.getString(1));
                mRadio.setStream(mCursor.getString(2));
                mRadio.setWeb(mCursor.getString(3));
                mRadio.setCity(mCursor.getString(4));
                mRadio.setFrequency(mCursor.getString(5));
            }
        }
        return mRadio;
    }

    public ArrayList<Figures> getFigures(long ridc)throws SQLException{
        ArrayList<Figures> mFigures=new ArrayList<Figures>();
        String query = "SELECT latp, lngp, radc, fillf, strokef, typef FROM " + DATABASE_TABLEF + " WHERE idc='" +ridc+ "'";
        Cursor mCursor = db.rawQuery(query, null);
        if (mCursor != null) {
            while(mCursor.moveToNext()) {
                Figures fig = new Figures();
                fig.setLatp(mCursor.getString(0));
                fig.setLngp(mCursor.getString(1));
                fig.setRadc(mCursor.getLong(2));
                fig.setFillc(mCursor.getString(3));
                fig.setStroke(mCursor.getString(4));
                fig.setTypef(mCursor.getString(5));
                mFigures.add(fig);
            }
        }
        return mFigures;
    }

    public long addConflicts(int cants){
        ContentValues initialValues = new ContentValues();
        initialValues.put(N_CONF, cants);
        return db.insert(DATABASE_TABLEC, null, initialValues);
    }

    public int cantConflicts()throws SQLException{
        int cantconf = 0;
        String query = "SELECT numc FROM " + DATABASE_TABLEC;
        Cursor mCursor = db.rawQuery(query, null);
        if (mCursor != null) {
            while(mCursor.moveToNext()) {
                cantconf = mCursor.getInt(0);
            }
        }
        return cantconf;
    }

    public boolean checkRadios()throws SQLException{
        boolean checkC=false;
        String query = "SELECT * FROM " +DATABASE_TABLE;
        Cursor mCursor = db.rawQuery(query, null);
        if (mCursor != null && mCursor.getCount()>0) {
            checkC=true;
        }
        return checkC;
    }

    public void deleteRadios() {
    	db.execSQL("DELETE FROM " + DATABASE_TABLE);
	}
    public void deleteFigures() {
        db.execSQL("DELETE FROM " + DATABASE_TABLEF);
    }
    public void deleteconflicts() {
        db.execSQL("DELETE FROM " + DATABASE_TABLEC);
    }
}