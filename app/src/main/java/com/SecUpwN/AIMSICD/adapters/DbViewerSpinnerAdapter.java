/* Android IMSI Catcher Detector
 *      Copyright (C) 2015
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You may obtain a copy of the License at
 *      https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/blob/master/LICENSE
 */
package com.SecUpwN.AIMSICD.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.SecUpwN.AIMSICD.R;
import com.SecUpwN.AIMSICD.adapters.Holders.ViewTableNameSpinnerHolder;
import com.SecUpwN.AIMSICD.enums.StatesDbViewer;

import java.util.ArrayList;

public class DbViewerSpinnerAdapter extends ArrayAdapter<StatesDbViewer> {

    public DbViewerSpinnerAdapter(Context pContext, int pResource) {
        super(pContext, pResource);
        mDataList = StatesDbViewer.getStates();
    }

    ArrayList<StatesDbViewer> mDataList;


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
