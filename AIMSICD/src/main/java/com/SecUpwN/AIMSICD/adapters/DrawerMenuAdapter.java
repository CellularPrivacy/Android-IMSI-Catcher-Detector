/* Android IMSI-Catcher Detector | (c) AIMSICD Privacy Project
 * -----------------------------------------------------------
 * LICENSE:  http://git.io/vki47 | TERMS:  http://git.io/vki4o
 * -----------------------------------------------------------
 */
package com.SecUpwN.AIMSICD.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.SecUpwN.AIMSICD.R;
import com.SecUpwN.AIMSICD.constants.DrawerMenu;
import com.SecUpwN.AIMSICD.drawer.DrawerMenuItem;
import com.SecUpwN.AIMSICD.drawer.DrawerMenuSection;
import com.SecUpwN.AIMSICD.drawer.NavDrawerItem;
import com.SecUpwN.AIMSICD.utils.Helpers;

import java.util.List;

public class DrawerMenuAdapter extends ArrayAdapter<NavDrawerItem> {

    private final LayoutInflater inflater;
    private final View.OnClickListener mInfoButtonListener;
    private final Animation mBounceHelpButtonAnimation;

    private static Context appContext;
 
    @SuppressLint("ShowToast")
    public DrawerMenuAdapter(Context context, int textViewResourceId, List<NavDrawerItem> objects ) {
        super(context, textViewResourceId, objects);
        inflater = LayoutInflater.from(context);
        appContext = context.getApplicationContext();

        mInfoButtonListener = new View.OnClickListener() {
            @Override
            public void onClick(View pView) {
                pView.startAnimation(mBounceHelpButtonAnimation);
                if (pView.getTag() != null && pView.getTag() instanceof Integer)
                    showHelpToast((Integer) pView.getTag());
            }
        };
        mBounceHelpButtonAnimation = AnimationUtils.loadAnimation(appContext, R.anim.action_button_help);
    }

    private void showHelpToast(Integer pToastValueId) {
        Helpers.msgLong(appContext, appContext.getString(pToastValueId));
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        NavDrawerItem menuItem = this.getItem(position);
        if ( menuItem.getType() == DrawerMenuItem.ITEM_TYPE ) {
            view = getItemView(convertView, parent, menuItem );
        }
        else {
            view = getSectionView(convertView, parent, menuItem);
        }
        return view ;
    }

    View getItemView(View convertView, ViewGroup parentView, NavDrawerItem navDrawerItem) {

        DrawerMenuItem menuItem = (DrawerMenuItem) navDrawerItem ;
        NavMenuItemHolder navMenuItemHolder = null;

        if (convertView == null) {
            convertView = inflater.inflate( R.layout.drawer_item, parentView, false);
            TextView labelView = (TextView) convertView
                    .findViewById( R.id.drawer_menu_item_label );
            ImageView iconView = (ImageView) convertView
                    .findViewById( R.id.drawer_menu_item_icon );
            ImageView lInfoButton = (ImageView) convertView.findViewById(R.id.drawer_menu_item_info_button);

            navMenuItemHolder = new NavMenuItemHolder();
            navMenuItemHolder.itemName = labelView ;
            navMenuItemHolder.itemIcon = iconView ;
            navMenuItemHolder.itemInfoButton = lInfoButton;

            convertView.setTag(navMenuItemHolder);
        }

        if ( navMenuItemHolder == null ) {
            navMenuItemHolder = (NavMenuItemHolder) convertView.getTag();
        }

        navMenuItemHolder.itemName.setText(menuItem.getLabel());
        navMenuItemHolder.itemIcon.setImageResource(menuItem.getIconId());
        if (menuItem.isShowInfoButton()) {
            navMenuItemHolder.itemInfoButton.setTag(menuItem.getHelpStringId());
            navMenuItemHolder.itemInfoButton.setVisibility(View.VISIBLE);
            navMenuItemHolder.itemInfoButton.setOnClickListener(mInfoButtonListener);
        } else {
            navMenuItemHolder.itemInfoButton.setTag(menuItem.getHelpStringId());
            navMenuItemHolder.itemInfoButton.setVisibility(View.INVISIBLE);
            navMenuItemHolder.itemInfoButton.setOnClickListener(null);
        }

        return convertView ;
    }

    View getSectionView(View convertView, ViewGroup parentView,
            NavDrawerItem navDrawerItem) {

        DrawerMenuSection menuSection = (DrawerMenuSection) navDrawerItem ;
        NavMenuSectionHolder navMenuItemHolder = null;

        if (convertView == null) {
            convertView = inflater.inflate( R.layout.drawer_section, parentView, false);
            TextView labelView = (TextView) convertView
                    .findViewById( R.id.drawer_menu_section_label );

            navMenuItemHolder = new NavMenuSectionHolder();
            navMenuItemHolder.itemName = labelView ;
            convertView.setTag(navMenuItemHolder);
        }

        if ( navMenuItemHolder == null ) {
            navMenuItemHolder = (NavMenuSectionHolder) convertView.getTag();
        }

        navMenuItemHolder.itemName.setText(menuSection.getLabel());

        return convertView ;
    }

    @Override
    public int getViewTypeCount() {
        return DrawerMenu.COUNT_OF_MENU_TYPE;
    }

    @Override
    public int getItemViewType(int position) {
        return this.getItem(position).getType();
    }

    @Override
    public boolean isEnabled(int position) {
        return getItem(position).isEnabled();
    }


    private static class NavMenuItemHolder {
        TextView itemName;
        ImageView itemIcon;
        ImageView itemInfoButton;
    }

    private class NavMenuSectionHolder {
        private TextView itemName;
    }
}
