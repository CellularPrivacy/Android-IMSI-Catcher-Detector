package com.secupwn.aimsicd.ui.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Spinner;

import com.secupwn.aimsicd.R;
import com.secupwn.aimsicd.adapters.DbViewerSpinnerAdapter;
import com.secupwn.aimsicd.adapters.MeasuredCellStrengthAdapter;
import com.secupwn.aimsicd.data.adapter.BaseStationAdapter;
import com.secupwn.aimsicd.data.adapter.DefaultLocationAdapter;
import com.secupwn.aimsicd.data.adapter.DetectionStringAdapter;
import com.secupwn.aimsicd.data.adapter.EventAdapter;
import com.secupwn.aimsicd.data.adapter.ImportAdapter;
import com.secupwn.aimsicd.data.adapter.MeasureAdapter;
import com.secupwn.aimsicd.data.adapter.SmsDataAdapter;
import com.secupwn.aimsicd.data.model.BaseTransceiverStation;
import com.secupwn.aimsicd.data.model.DefaultLocation;
import com.secupwn.aimsicd.data.model.Event;
import com.secupwn.aimsicd.data.model.Import;
import com.secupwn.aimsicd.data.model.Measure;
import com.secupwn.aimsicd.data.model.SmsData;
import com.secupwn.aimsicd.data.model.SmsDetectionString;
import com.secupwn.aimsicd.enums.StatesDbViewer;
import com.secupwn.aimsicd.utils.RealmHelper;

import io.freefair.android.injection.annotation.InjectView;
import io.freefair.android.injection.annotation.XmlLayout;
import io.freefair.android.injection.app.InjectionFragment;
import io.realm.Realm;

/**
 * Description:    Class that handles the display of the items in the 'Database Viewer' (DBV)
 * <p/>
 * Issues:
 * <p/>
 * Notes:          See issue #234 for details on how to format the UI
 */
@XmlLayout(R.layout.fragment_db_viewer)
public final class DbViewerFragment extends InjectionFragment {

    private RealmHelper mDb;
    private StatesDbViewer mTableSelected;

    @InjectView(R.id.table_spinner)
    private Spinner tblSpinner;

    @InjectView(R.id.list_view)
    private ListView lv;

    @InjectView(R.id.db_list_empty)
    private View emptyView;

    private Realm realm;

    public DbViewerFragment() {
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mDb = new RealmHelper(activity.getBaseContext());
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        DbViewerSpinnerAdapter mSpinnerAdapter = new DbViewerSpinnerAdapter(getActivity(), R.layout.item_spinner_db_viewer);
        tblSpinner.setAdapter(mSpinnerAdapter);
        tblSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, final int position, long id) {
                Object selectedItem = tblSpinner.getSelectedItem();
                if (!(selectedItem instanceof StatesDbViewer)) {
                    return;
                }
                mTableSelected = (StatesDbViewer) selectedItem;

                switch (position) {
                    case 0:
                        setListAdapter(new BaseStationAdapter(realm.where(BaseTransceiverStation.class).findAll()));
                        break;
                    case 1:
                        setListAdapter(new MeasureAdapter(realm.where(Measure.class).findAll()));
                        break;
                    case 2:
                        setListAdapter(new ImportAdapter(realm.where(Import.class).findAll()));
                        break;
                    case 3:
                        setListAdapter(new DefaultLocationAdapter(realm.where(DefaultLocation.class).findAll()));
                        break;
                    case 4:  //Silent SMS
                        setListAdapter(new SmsDataAdapter(realm.where(SmsData.class).findAll()));
                        break;
                    case 5:
                        setListAdapter(new MeasuredCellStrengthAdapter(realm.where(Measure.class).findAll()));
                        break;
                    case 6:
                        setListAdapter(new EventAdapter(realm.where(Event.class).findAll()));
                        break;
                    case 7:
                        setListAdapter(new DetectionStringAdapter(realm.where(SmsDetectionString.class).findAll()));
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown type of table");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });
    }

    private void setListAdapter(ListAdapter adapter) {
        if (getActivity() == null) {
            return; // fragment detached
        }

        lv.setEmptyView(emptyView);
        if (adapter != null) {
            lv.setAdapter(adapter);
            lv.setVisibility(View.VISIBLE);
        } else {
            lv.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        }

        getActivity().setProgressBarIndeterminateVisibility(false);
    }

    @Override
    public void onStart() {
        super.onStart();
        realm = Realm.getDefaultInstance();
    }

    @Override
    public void onStop() {
        super.onStop();
        realm.close();
    }
}
