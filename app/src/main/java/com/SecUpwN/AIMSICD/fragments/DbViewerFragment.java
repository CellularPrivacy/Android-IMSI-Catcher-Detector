package com.SecUpwN.AIMSICD.fragments;

import com.SecUpwN.AIMSICD.adapters.AIMSICDDbAdapter;
import com.SecUpwN.AIMSICD.utils.Helpers;
import com.SecUpwN.AIMSICD.R;
import com.SecUpwN.AIMSICD.adapters.BaseInflaterAdapter;
import com.SecUpwN.AIMSICD.adapters.CardItemData;
import com.SecUpwN.AIMSICD.adapters.CellCardInflater;
import com.SecUpwN.AIMSICD.adapters.DefaultLocationCardInflater;
import com.SecUpwN.AIMSICD.adapters.OpenCellIdCardInflater;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;

public class DbViewerFragment extends Fragment {

    private AIMSICDDbAdapter mDb;
    private Spinner tblSpinner;
    private String mTableSelected;
    private boolean mMadeSelection;
    private ListView lv;
    private View mView;
    private Context mContext;

    public DbViewerFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity().getBaseContext();
        mDb  = new AIMSICDDbAdapter(mContext);
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

    @Override
    public void onResume() {
        super.onResume();
        lv = (ListView) mView.findViewById(R.id.list_view);
        tblSpinner = (Spinner) mView.findViewById(R.id.table_spinner);
        tblSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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

        });

        Button loadTable = (Button) mView.findViewById(R.id.load_table_data);

        loadTable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMadeSelection) {
                    new MyAsync().execute();
                }
            }
        });
    }

    private void BuildTable(Cursor tableData) {
        if (tableData != null && tableData.getCount() > 0) {
            switch (mTableSelected) {
                case "OpenCellID Data": {
                    BaseInflaterAdapter<CardItemData> adapter
                            = new BaseInflaterAdapter<>(
                            new OpenCellIdCardInflater());
                    while (tableData.moveToNext()) {
                        CardItemData data = new CardItemData("CellID: " + tableData.getString(0),
                                "LAC: " + tableData.getString(1), "MCC: " + tableData.getString(2),
                                "MNC: " + tableData.getString(3),
                                "Latitude: " + tableData.getString(4),
                                "Longitude: " + tableData.getString(5),
                                "Average Signal Strength: " + tableData.getString(6),
                                "Samples: " + tableData.getString(7));
                        adapter.addItem(data, false);
                    }
                    lv.setAdapter(adapter);
                    break;
                }
                case "Default MCC Locations": {
                    BaseInflaterAdapter<CardItemData> adapter
                            = new BaseInflaterAdapter<>(
                            new DefaultLocationCardInflater());
                    while (tableData.moveToNext()) {
                        CardItemData data = new CardItemData("Country: " + tableData.getString(0),
                                "MCC: " + tableData.getString(1),
                                "Latitude: " + tableData.getString(2),
                                "Longitude: " + tableData.getString(3));
                        adapter.addItem(data, false);
                    }
                    lv.setAdapter(adapter);
                    break;
                }
                default: {
                    BaseInflaterAdapter<CardItemData> adapter
                            = new BaseInflaterAdapter<>(
                            new CellCardInflater());
                    while (tableData.moveToNext()) {
                        CardItemData data = new CardItemData("CellID: " + tableData.getString(0),
                                "LAC: " + tableData.getString(1),
                                "Network Type: " + tableData.getString(2),
                                "Latitude: " + tableData.getString(3),
                                "Longitude: " + tableData.getString(4),
                                "Signal Strength: " + tableData.getString(5));
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