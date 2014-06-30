package com.SecUpwN.AIMSICD.adapters;

import com.SecUpwN.AIMSICD.AIMSICD.NavDrawerItem;
import com.SecUpwN.AIMSICD.R;
import com.SecUpwN.AIMSICD.fragments.DrawerMenuItem;
import com.SecUpwN.AIMSICD.fragments.DrawerMenuSection;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class DrawerMenuAdapter extends ArrayAdapter<NavDrawerItem> {
        private LayoutInflater inflater;

        public DrawerMenuAdapter(Context context, int textViewResourceId, NavDrawerItem[] objects ) {
            super(context, textViewResourceId, objects);
            this.inflater = LayoutInflater.from(context);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = null ;
            NavDrawerItem menuItem = this.getItem(position);
            if ( menuItem.getType() == DrawerMenuItem.ITEM_TYPE ) {
                view = getItemView(convertView, parent, menuItem );
            }
            else {
                view = getSectionView(convertView, parent, menuItem);
            }
            return view ;
        }

        public View getItemView( View convertView, ViewGroup parentView, NavDrawerItem navDrawerItem ) {

            DrawerMenuItem menuItem = (DrawerMenuItem) navDrawerItem ;
            NavMenuItemHolder navMenuItemHolder = null;

            if (convertView == null) {
                convertView = inflater.inflate( R.layout.drawer_item, parentView, false);
                TextView labelView = (TextView) convertView
                        .findViewById( R.id.drawer_menu_item_label );
                ImageView iconView = (ImageView) convertView
                        .findViewById( R.id.drawer_menu_item_icon );

                navMenuItemHolder = new NavMenuItemHolder();
                navMenuItemHolder.labelView = labelView ;
                navMenuItemHolder.iconView = iconView ;

                convertView.setTag(navMenuItemHolder);
            }

            if ( navMenuItemHolder == null ) {
                navMenuItemHolder = (NavMenuItemHolder) convertView.getTag();
            }

            navMenuItemHolder.labelView.setText(menuItem.getLabel());
            navMenuItemHolder.iconView.setImageResource(menuItem.getIcon());

            return convertView ;
        }

        public View getSectionView(View convertView, ViewGroup parentView,
                NavDrawerItem navDrawerItem) {

            DrawerMenuSection menuSection = (DrawerMenuSection) navDrawerItem ;
            NavMenuSectionHolder navMenuItemHolder = null;

            if (convertView == null) {
                convertView = inflater.inflate( R.layout.drawer_section, parentView, false);
                TextView labelView = (TextView) convertView
                        .findViewById( R.id.drawer_menu_section_label );

                navMenuItemHolder = new NavMenuSectionHolder();
                navMenuItemHolder.labelView = labelView ;
                convertView.setTag(navMenuItemHolder);
            }

            if ( navMenuItemHolder == null ) {
                navMenuItemHolder = (NavMenuSectionHolder) convertView.getTag();
            }

            navMenuItemHolder.labelView.setText(menuSection.getLabel());

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
            private TextView labelView;
            private ImageView iconView;
        }

        private class NavMenuSectionHolder {
            private TextView labelView;
        }
    }
