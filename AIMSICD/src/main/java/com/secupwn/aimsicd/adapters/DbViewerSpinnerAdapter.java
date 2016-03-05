/* Android IMSI-Catcher Detector | (c) AIMSICD Privacy Project
 * -----------------------------------------------------------
 * LICENSE:  http://git.io/vki47 | TERMS:  http://git.io/vki4o
 * -----------------------------------------------------------
 */
package com.secupwn.aimsicd.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.secupwn.aimsicd.R;
import com.secupwn.aimsicd.adapters.holders.ViewTableNameSpinnerHolder;
import com.secupwn.aimsicd.enums.StatesDbViewer;

import java.util.List;

public class DbViewerSpinnerAdapter extends ArrayAdapter<StatesDbViewer> {

    public DbViewerSpinnerAdapter(Context pContext, int pResource) {
        super(pContext, pResource);
        mDataList = StatesDbViewer.getStates();
    }

    List<StatesDbViewer> mDataList;


    @Override
    public View getView(int pPosition, View pConvertView, ViewGroup pParent) {

        View lView = pConvertView;
        ViewTableNameSpinnerHolder lHolder;
        if (lView == null
                || (lView.getId() != R.id.item_root_layout && !(lView.getTag() instanceof ViewTableNameSpinnerHolder))) {

            lView = ((LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(
                    R.layout.item_spinner_db_viewer, pParent, false);
            lHolder = setViewHolderToView(lView);

        } else {
            lHolder = (ViewTableNameSpinnerHolder) lView.getTag();

        }

        StatesDbViewer lEntry = mDataList.get(pPosition);
        lHolder.name.setText(lEntry.getDisplayName(getContext()));

        return lView;
    }


    @Override
    public View getDropDownView(int pPosition, View pConvertView, ViewGroup pParent) {
        return getView(pPosition, pConvertView, pParent);
    }


    @Override
    public StatesDbViewer getItem(int position) {
        return mDataList.get(position);
    }


    private ViewTableNameSpinnerHolder setViewHolderToView(View pView) {
        ViewTableNameSpinnerHolder lHolder;
        lHolder = new ViewTableNameSpinnerHolder();

        lHolder.name = (TextView) pView.findViewById(R.id.item_name);

        pView.setTag(lHolder);
        return lHolder;
    }


    @Override
    public int getCount() {
        return mDataList.size();
    }

}
