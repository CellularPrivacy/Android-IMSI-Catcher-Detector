package com.SecUpwN.AIMSICD.fragments;

import com.SecUpwN.AIMSICD.R;
import com.SecUpwN.AIMSICD.adapters.AIMSICDDbAdapter;
import com.SecUpwN.AIMSICD.adapters.BaseInflaterAdapter;
import com.SecUpwN.AIMSICD.adapters.CardItemData;
import com.SecUpwN.AIMSICD.adapters.CellCardInflater;
import com.SecUpwN.AIMSICD.adapters.DefaultLocationCardInflater;
import com.SecUpwN.AIMSICD.adapters.OpenCellIdCardInflater;
import com.SecUpwN.AIMSICD.adapters.SilentSmsCardData;
import com.SecUpwN.AIMSICD.adapters.SilentSmsCardInflater;
import com.SecUpwN.AIMSICD.utils.Helpers;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;

public class DbViewerFragment extends Fragment {

    private AIMSICDDbAdapter mDb;
    private String mTableSelected;
    private boolean mMadeSelection;
    private Context mContext;

    //Layout items
    private Spinner tblSpinner;
    private ListView lv;
    private View mView;

    public DbViewerFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = activity.getBaseContext();
        mDb = new AIMSICDDbAdapter(mContext);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        mView = inflater.inflate(R.layout.db_view,
                container, false);

        if (mView != null) {
            lv = (ListView) mView.findViewById(R.id.list_view);

            tblSpinner = (Spinner) mView.findViewById(R.id.table_spinner);
            tblSpinner.setOnItemSelectedListener(new spinnerListener());

            Button loadTable = (Button) mView.findViewById(R.id.load_table_data);

            loadTable.setOnClickListener(new btnClick());
        }

        return mView;
    }

    private class spinnerListener implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> parentView, View selectedItemView,
                int position, long id) {
            mTableSelected = String.valueOf(tblSpinner.getSelectedItem());
            mMadeSelection = true;
        }

        @Override
        public void onNothingSelected(AdapterView<?> parentView) {
            mMadeSelection = false;
        }
    }

    private class btnClick implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            if (mMadeSelection) {
                new MyAsync().execute();
            }
        }
    }

    private void BuildTable(Cursor tableData) {
        if (tableData != null && tableData.getCount() > 0) {
            switch (mTableSelected) {
                case "OpenCellID Data": {
                    BaseInflaterAdapter<CardItemData> adapter
                            = new BaseInflaterAdapter<>(
                            new OpenCellIdCardInflater());
                    int count = tableData.getCount();
                    while (tableData.moveToNext()) {
                        CardItemData data = new CardItemData("CellID: " + tableData.getString(0),
                                "LAC: " + tableData.getString(1), "MCC: " + tableData.getString(2),
                                "MNC: " + tableData.getString(3),
                                "Latitude: " + tableData.getString(4),
                                "Longitude: " + tableData.getString(5),
                                "Average Signal Strength: " + tableData.getString(6),
                                "Samples: " + tableData.getString(7),
                                "" + (tableData.getPosition() + 1) + " / " + count);
                        adapter.addItem(data, false);
                    }
                    lv.setAdapter(adapter);
                    break;
                }
                case "Default MCC Locations": {
                    BaseInflaterAdapter<CardItemData> adapter
                            = new BaseInflaterAdapter<>(
                            new DefaultLocationCardInflater());
                    int count = tableData.getCount();
                    while (tableData.moveToNext()) {
                        CardItemData data = new CardItemData("Country: " + tableData.getString(0),
                                "MCC: " + tableData.getString(1),
                                "Latitude: " + tableData.getString(2),
                                "Longitude: " + tableData.getString(3),
                                "" + (tableData.getPosition() + 1) + " / " + count);
                        adapter.addItem(data, false);
                    }
                    lv.setAdapter(adapter);
                    break;
                }
                case "Silent Sms": {
                    BaseInflaterAdapter<SilentSmsCardData> adapter
                            = new BaseInflaterAdapter<>(
                            new SilentSmsCardInflater());
                    int count = tableData.getCount();
                    while (tableData.moveToNext()) {
                        SilentSmsCardData data = new SilentSmsCardData(tableData.getString(0),
                                tableData.getString(1), tableData.getString(2),
                                tableData.getString(3),
                                tableData.getString(4), tableData.getLong(5));
                        adapter.addItem(data, false);
                    }
                    lv.setAdapter(adapter);
                    break;
                }
                default: {
                    BaseInflaterAdapter<CardItemData> adapter
                            = new BaseInflaterAdapter<>(
                            new CellCardInflater());
                    int count = tableData.getCount();
                    while (tableData.moveToNext()) {
                        CardItemData data = new CardItemData("CellID: " + tableData.getString(0),
                                "LAC: " + tableData.getString(1),
                                "Network Type: " + tableData.getString(2),
                                "Latitude: " + tableData.getString(3),
                                "Longitude: " + tableData.getString(4),
                                "Signal Strength: " + tableData.getString(5),
                                "" + (tableData.getPosition() + 1) + " / " + count);
                        adapter.addItem(data, false);
                    }
                    lv.setAdapter(adapter);
                    break;
                }
            }
            lv.setVisibility(View.VISIBLE);
        } else {
            lv.setVisibility(View.GONE);
            Helpers.sendMsg(mContext, "Table contains no data to display");
        }
    }

    private class MyAsync extends AsyncTask<Cursor, Cursor, Cursor> {

        @Override
        protected Cursor doInBackground(Cursor... params) {
            mDb.open();
            switch (mTableSelected) {
                case "Cell Data":
                    return mDb.getCellData();
                case "Location Data":
                    return mDb.getLocationData();
                case "OpenCellID Data":
                    return mDb.getOpenCellIDData();
                case "Default MCC Locations":
                    return mDb.getDefaultMccLocationData();
                case "Silent Sms":
                    return mDb.getSilentSmsData();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Cursor result) {
            super.onPostExecute(result);
            BuildTable(result);
            mDb.close();
        }
    }

}