/* Android IMSI-Catcher Detector | (c) AIMSICD Privacy Project
 * -----------------------------------------------------------
 * LICENSE:  http://git.io/vki47 | TERMS:  http://git.io/vki4o
 * -----------------------------------------------------------
 */
package com.SecUpwN.AIMSICD.adapters;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;

public class BaseInflaterAdapter<T> extends BaseAdapter {

    private final List<T> m_items = new ArrayList<>();

    private IAdapterViewInflater<T> m_viewInflater;

    public BaseInflaterAdapter(IAdapterViewInflater<T> viewInflater) {
        m_viewInflater = viewInflater;
    }

    public BaseInflaterAdapter(List<T> items, IAdapterViewInflater<T> viewInflater) {
        m_items.addAll(items);
        m_viewInflater = viewInflater;
    }

    public void setViewInflater(IAdapterViewInflater<T> viewInflater, boolean notifyChange) {
        m_viewInflater = viewInflater;

        if (notifyChange) {
            notifyDataSetChanged();
        }
    }

    public void addItem(T item, boolean notifyChange) {
        m_items.add(item);

        if (notifyChange) {
            notifyDataSetChanged();
        }
    }

    public void addItems(List<T> items, boolean notifyChange) {
        m_items.addAll(items);

        if (notifyChange) {
            notifyDataSetChanged();
        }
    }

    public void clear(boolean notifyChange) {
        m_items.clear();

        if (notifyChange) {
            notifyDataSetChanged();
        }
    }

    @Override
    public int getCount() {
        return m_items.size();
    }

    @Override
    public Object getItem(int pos) {
        return getTItem(pos);
    }

    public T getTItem(int pos) {
        return m_items.get(pos);
    }

    @Override
    public long getItemId(int pos) {
        return pos;
    }

    @Override
    public View getView(int pos, View convertView, ViewGroup parent) {
        return m_viewInflater != null ? m_viewInflater.inflate(this, pos, convertView, parent)
                : null;
    }
}