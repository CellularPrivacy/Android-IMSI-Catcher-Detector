/* Android IMSI-Catcher Detector | (c) AIMSICD Privacy Project
 * -----------------------------------------------------------
 * LICENSE:  http://git.io/vki47 | TERMS:  http://git.io/vki4o
 * -----------------------------------------------------------
 */
package com.SecUpwN.AIMSICD.adapters;

import android.view.View;
import android.view.ViewGroup;

public interface IAdapterViewInflater<T> {
    View inflate(BaseInflaterAdapter<T> adapter, int pos, View convertView, ViewGroup parent);
}