package com.SecUpwN.AIMSICD.adapters;

import com.SecUpwN.AIMSICD.R;
import com.SecUpwN.AIMSICD.drawer.NavDrawerItem;
import com.SecUpwN.AIMSICD.drawer.DrawerMenuItem;
import com.SecUpwN.AIMSICD.drawer.DrawerMenuSection;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class DrawerMenuAdapter extends ArrayAdapter<NavDrawerItem> {

    private final List<NavDrawerItem> drawerItemList;
    private final LayoutInflater inflater;


    public DrawerMenuAdapter(Context context, int textViewResourceId, List<NavDrawerItem> objects ) {
        super(context, textViewResourceId, objects);
        drawerItemList = objects;
        inflater = LayoutInflater.from(context);
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

            navMenuItemHolder = new NavMenuItemHolder();
            navMenuItemHolder.itemName = labelView ;
            navMenuItemHolder.itemIcon = iconView ;

            convertView.setTag(navMenuItemHolder);
        }

        if ( navMenuItemHolder == null ) {
            navMenuItemHolder = (NavMenuItemHolder) convertView.getTag();
        }

        navMenuItemHolder.itemName.setText(menuItem.getLabel());
        navMenuItemHolder.itemIcon.setImageResource(menuItem.getIcon());

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
        return 2;
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
    }

    private class NavMenuSectionHolder {
        private TextView itemName;
    }
}
