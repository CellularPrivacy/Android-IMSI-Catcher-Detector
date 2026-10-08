/* Android IMSI-Catcher Detector | (c) AIMSICD Privacy Project
 * -----------------------------------------------------------
 * LICENSE:  http://git.io/vki47 | TERMS:  http://git.io/vki4o
 * -----------------------------------------------------------
 */
package com.secupwn.aimsicd.defender.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.secupwn.aimsicd.R;
import com.secupwn.aimsicd.defender.firewall.FirewallRule;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple adapter for the firewall rule list. Delegates toggles/removals to the
 * hosting fragment via {@link Callback}.
 */
public class FirewallRulesAdapter extends BaseAdapter {

    public interface Callback {
        void onToggleRule(long id, boolean enabled);
        void onDeleteRule(long id);
    }

    private final LayoutInflater inflater;
    private final Callback callback;
    private List<FirewallRule> rules = new ArrayList<FirewallRule>();

    public FirewallRulesAdapter(Context context, Callback callback) {
        this.inflater = LayoutInflater.from(context);
        this.callback = callback;
    }

    public void setRules(List<FirewallRule> rules) {
        this.rules = rules == null ? new ArrayList<FirewallRule>() : new ArrayList<FirewallRule>(rules);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return rules.size();
    }

    @Override
    public FirewallRule getItem(int position) {
        return rules.get(position);
    }

    @Override
    public long getItemId(int position) {
        return rules.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder h;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_firewall_rule, parent, false);
            h = new ViewHolder();
            h.enabled = (CheckBox) convertView.findViewById(R.id.rule_enabled);
            h.title = (TextView) convertView.findViewById(R.id.rule_title);
            h.detail = (TextView) convertView.findViewById(R.id.rule_detail);
            h.delete = convertView.findViewById(R.id.rule_delete);
            convertView.setTag(h);
        } else {
            h = (ViewHolder) convertView.getTag();
        }
        final FirewallRule rule = getItem(position);
        h.enabled.setOnCheckedChangeListener(null);
        h.enabled.setChecked(rule.isEnabled());
        h.title.setText(rule.describe());
        String detail = rule.getReason() == null || rule.getReason().isEmpty()
                ? rule.normalizedCidr() : rule.getReason();
        if (rule.isUidRule() && (rule.getReason() == null || rule.getReason().isEmpty())) {
            detail = "uid:" + rule.getUid();
        }
        h.detail.setText(detail);
        h.enabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (callback != null) {
                    callback.onToggleRule(rule.getId(), isChecked);
                }
            }
        });
        h.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (callback != null) {
                    callback.onDeleteRule(rule.getId());
                }
            }
        });
        return convertView;
    }

    private static class ViewHolder {
        CheckBox enabled;
        TextView title;
        TextView detail;
        View delete;
    }
}
