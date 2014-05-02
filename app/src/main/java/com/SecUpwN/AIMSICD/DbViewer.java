package com.SecUpwN.AIMSICD;

import com.SecUpwN.AIMSICD.adapters.BaseInflaterAdapter;
import com.SecUpwN.AIMSICD.adapters.CardItemData;
import com.SecUpwN.AIMSICD.adapters.CellCardInflater;
import com.SecUpwN.AIMSICD.adapters.DefaultLocationCardInflater;
import com.SecUpwN.AIMSICD.adapters.OpenCellIdCardInflater;

import android.app.Activity;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;

public class DbViewer extends Activity {

    private final AIMSICDDbAdapter mDb = new AIMSICDDbAdapter(this);
    private Spinner tblSpinner;
    private String mTableSelected;
    private boolean mMadeSelection;
    private ListView lv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.db_view);
        lv = (ListView) findViewById(R.id.list_view);
        lv.addHeaderView(new View(this));
        lv.addFooterView(new View(this));
        tblSpinner = (Spinner) findViewById(R.id.table_spinner);
        tblSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
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

        Button loadTable = (Button) findViewById(R.id.load_table_data);

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
            if (mTableSelected.equals("OpenCellID Data")) {
                BaseInflaterAdapter<CardItemData> adapter = new BaseInflaterAdapter<CardItemData>(
                        new OpenCellIdCardInflater());
                while (tableData.moveToNext()) {
                        CardItemData data = new CardItemData("CellID: " + tableData.getString(0),
                                "LAC: " + tableData.getString(1), "MCC: " + tableData.getString(2),
                                "MNC: " + tableData.getString(3), "Latitude: " + tableData.getString(4),
                                "Longitude: " + tableData.getString(5), "Average Signal Strength: " + tableData.getString(6),
                                "Samples: " + tableData.getString(7));
                        adapter.addItem(data, false);
                }
                lv.setAdapter(adapter);
            } else if (mTableSelected.equals("Default MCC Locations")) {
                BaseInflaterAdapter<CardItemData> adapter = new BaseInflaterAdapter<CardItemData>(
                        new DefaultLocationCardInflater());
                while (tableData.moveToNext()) {
                    CardItemData data = new CardItemData("Country: " + tableData.getString(0),
                            "MCC: " + tableData.getString(1), "Latitude: " + tableData.getString(2),
                            "Longitude: " + tableData.getString(3));
                    adapter.addItem(data, false);
                }
                lv.setAdapter(adapter);
            } else {
                BaseInflaterAdapter<CardItemData> adapter = new BaseInflaterAdapter<CardItemData>(
                        new CellCardInflater());
                while (tableData.moveToNext()){
                    CardItemData data = new CardItemData("CellID: " + tableData.getString(0),
                            "LAC: " + tableData.getString(1),
                            "Network Type: " + tableData.getString(2),
                            "Latitude: " + tableData.getString(3),
                            "Longitude: " + tableData.getString(4),
                            "Signal Strength: " + tableData.getString(5));
                    adapter.addItem(data, false);
                }
                lv.setAdapter(adapter);
            }
            lv.setVisibility(View.VISIBLE);
        } else {
            lv.setVisibility(View.GONE);
            Helpers.sendMsg(this, "Table contains no data to display");
        }
    }

    private class MyAsync extends AsyncTask<Cursor, Cursor, Cursor> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Cursor doInBackground(Cursor... params) {
            mDb.open();
            if (mTableSelected.equals("Cell Data")) {
                return mDb.getCellData();
            } else if (mTableSelected.equals("Location Data")) {
                return mDb.getLocationData();
            } else if (mTableSelected.equals("OpenCellID Data")) {
                return mDb.getOpenCellIDData();
            } else if (mTableSelected.equals("Default MCC Locations")) {
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
