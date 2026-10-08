/* Android IMSI-Catcher Detector | (c) AIMSICD Privacy Project
 * -----------------------------------------------------------
 * LICENSE:  http://git.io/vki47 | TERMS:  http://git.io/vki4o
 * -----------------------------------------------------------
 */
package com.secupwn.aimsicd.defender.ui;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.VpnService;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import com.secupwn.aimsicd.R;
import com.secupwn.aimsicd.defender.DefenderAgent;
import com.secupwn.aimsicd.defender.DefenderEvent;
import com.secupwn.aimsicd.defender.DefenderListener;
import com.secupwn.aimsicd.defender.firewall.DefenderVpnService;
import com.secupwn.aimsicd.defender.firewall.FirewallManager;
import com.secupwn.aimsicd.defender.firewall.FirewallRule;
import com.secupwn.aimsicd.defender.traffic.TrafficMonitor;
import com.secupwn.aimsicd.defender.traffic.TrafficSnapshot;
import com.secupwn.aimsicd.service.AimsicdService;
import com.secupwn.aimsicd.utils.Helpers;

import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

/**
 * Defender control panel: threat status, auto-protect / firewall / traffic
 * monitor toggles, live in/out rates + top talkers, firewall rules with manual
 * add, and recent defender events.
 */
@Slf4j
public class DefenderFragment extends Fragment implements DefenderListener,
        FirewallManager.FirewallListener, TrafficMonitor.TrafficListener,
        FirewallRulesAdapter.Callback {

    private static final int REQ_VPN_PERMISSION = 0xD3F;
    private static final long UI_TICK_MS = 2000L;

    private AimsicdService service;
    private boolean bound;
    private DefenderAgent agent;

    private final Handler ui = new Handler(Looper.getMainLooper());
    private final Runnable ticker = new Runnable() {
        @Override
        public void run() {
            try {
                if (isAdded()) {
                    refreshLiveTraffic();
                }
            } catch (Exception ignored) {
            }
            ui.postDelayed(this, UI_TICK_MS);
        }
    };

    /**
     * Post UI work guarded by attachment: defender/traffic callbacks arrive on
     * background threads and can race fragment detach otherwise.
     */
    private void postUi(final Runnable r) {
        ui.post(new Runnable() {
            @Override
            public void run() {
                if (!isAdded()) {
                    return;
                }
                r.run();
            }
        });
    }

    // Views
    private TextView threatView;
    private TextView cellView;
    private Switch swAutoProtect;
    private Switch swFirewall;
    private Switch swTraffic;
    private Switch swLteSpoof;
    private Switch swAutoRules;
    private TextView trafficView;
    private TextView talkersView;
    private TextView vpnView;
    private ListView rulesList;
    private ListView eventsList;
    private Button btnAddRule;
    private Button btnClearAuto;
    private Button btnLockdown;
    private Button btnRadioReset;
    private Button btnVpn;

    private FirewallRulesAdapter rulesAdapter;
    private ArrayAdapter<String> eventsAdapter;
    private final List<String> eventLines = new ArrayList<String>();

    private boolean suppressSwitchCallbacks;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_defender, container, false);
        threatView = (TextView) v.findViewById(R.id.defender_threat);
        cellView = (TextView) v.findViewById(R.id.defender_cell);
        swAutoProtect = (Switch) v.findViewById(R.id.defender_sw_autoprotect);
        swFirewall = (Switch) v.findViewById(R.id.defender_sw_firewall);
        swTraffic = (Switch) v.findViewById(R.id.defender_sw_traffic);
        swLteSpoof = (Switch) v.findViewById(R.id.defender_sw_lte);
        swAutoRules = (Switch) v.findViewById(R.id.defender_sw_autorules);
        trafficView = (TextView) v.findViewById(R.id.defender_traffic);
        talkersView = (TextView) v.findViewById(R.id.defender_talkers);
        vpnView = (TextView) v.findViewById(R.id.defender_vpn);
        rulesList = (ListView) v.findViewById(R.id.defender_rules);
        eventsList = (ListView) v.findViewById(R.id.defender_events);
        btnAddRule = (Button) v.findViewById(R.id.defender_btn_add_rule);
        btnClearAuto = (Button) v.findViewById(R.id.defender_btn_clear_auto);
        btnLockdown = (Button) v.findViewById(R.id.defender_btn_lockdown);
        btnRadioReset = (Button) v.findViewById(R.id.defender_btn_radio);
        btnVpn = (Button) v.findViewById(R.id.defender_btn_vpn);

        rulesAdapter = new FirewallRulesAdapter(getActivity(), this);
        rulesList.setAdapter(rulesAdapter);
        eventsAdapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_list_item_1, eventLines);
        eventsList.setAdapter(eventsAdapter);

        wireSwitches();
        btnAddRule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddRuleDialog();
            }
        });
        btnClearAuto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (agent != null) {
                    int n = agent.getFirewall().clearAutoRules();
                    Helpers.msgShort(getActivity(), getString(R.string.defender_cleared_auto, n));
                }
            }
        });
        btnLockdown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (agent == null) {
                    return;
                }
                if (agent.getFirewall().isLockdown()) {
                    agent.getAutoProtect().clearLockdown("user cleared from UI");
                } else {
                    agent.getFirewall().setLockdown(true, "manual from UI");
                }
                refreshAll();
            }
        });
        btnRadioReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (agent != null) {
                    agent.getAutoProtect().manualRadioReset();
                }
            }
        });
        btnVpn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleVpn();
            }
        });
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        Intent intent = new Intent(getActivity(), AimsicdService.class);
        getActivity().bindService(intent, connection, Context.BIND_AUTO_CREATE);
        ui.post(ticker);
    }

    @Override
    public void onPause() {
        super.onPause();
        ui.removeCallbacks(ticker);
        detachAgent();
        if (bound) {
            try {
                getActivity().unbindService(connection);
            } catch (Exception ignored) {
            }
            bound = false;
        }
    }

    // ------------------------------------------------------------------
    // Service binding
    // ------------------------------------------------------------------

    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            service = ((AimsicdService.AimscidBinder) binder).getService();
            bound = true;
            attachAgent(service.getDefenderAgent());
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            bound = false;
            detachAgent();
        }
    };

    private void attachAgent(DefenderAgent a) {
        detachAgent();
        agent = a;
        if (agent == null) {
            // Service older than defender? Fall back to the singleton.
            try {
                agent = DefenderAgent.getInstance(getActivity());
            } catch (Exception e) {
                log.warn("No DefenderAgent available: {}", e.getMessage());
                return;
            }
        }
        agent.addListener(this);
        agent.getFirewall().addListener(this);
        agent.getTrafficMonitor().addListener(this);
        refreshAll();
    }

    private void detachAgent() {
        if (agent != null) {
            try {
                agent.removeListener(this);
                agent.getFirewall().removeListener(this);
                agent.getTrafficMonitor().removeListener(this);
            } catch (Exception ignored) {
            }
            agent = null;
        }
    }

    // ------------------------------------------------------------------
    // DefenderListener
    // ------------------------------------------------------------------

    @Override
    public void onThreatLevelChanged(final DefenderAgent.ThreatLevel newLevel,
                                     final DefenderEvent cause) {
        postUi(new Runnable() {
            @Override
            public void run() {
                refreshThreat();
                refreshEvents();
            }
        });
    }

    @Override
    public void onDefenderEvent(final DefenderEvent event) {
        postUi(new Runnable() {
            @Override
            public void run() {
                refreshEvents();
                refreshThreat();
            }
        });
    }

    @Override
    public void onDefenderStateChanged() {
        postUi(new Runnable() {
            @Override
            public void run() {
                refreshAll();
            }
        });
    }

    // ------------------------------------------------------------------
    // FirewallManager.FirewallListener
    // ------------------------------------------------------------------

    @Override
    public void onRulesChanged(final List<FirewallRule> rules) {
        postUi(new Runnable() {
            @Override
            public void run() {
                rulesAdapter.setRules(rules);
                refreshVpnLine();
            }
        });
    }

    @Override
    public void onFirewallStateChanged(final boolean enabled, final boolean rooted) {
        postUi(new Runnable() {
            @Override
            public void run() {
                refreshSwitches();
                refreshVpnLine();
            }
        });
    }

    // ------------------------------------------------------------------
    // TrafficMonitor.TrafficListener
    // ------------------------------------------------------------------

    @Override
    public void onTrafficSnapshot(final TrafficSnapshot snapshot) {
        postUi(new Runnable() {
            @Override
            public void run() {
                renderSnapshot(snapshot);
            }
        });
    }

    @Override
    public void onTrafficAnomaly(TrafficMonitor.Anomaly anomaly) {
        // Event list refreshes via onDefenderEvent.
    }

    // ------------------------------------------------------------------
    // FirewallRulesAdapter.Callback
    // ------------------------------------------------------------------

    @Override
    public void onToggleRule(long id, boolean enabled) {
        if (agent != null) {
            agent.getFirewall().setRuleEnabled(id, enabled);
        }
    }

    @Override
    public void onDeleteRule(long id) {
        if (agent != null) {
            agent.getFirewall().removeRule(id);
        }
    }

    // ------------------------------------------------------------------
    // Refresh
    // ------------------------------------------------------------------

    private void refreshAll() {
        refreshSwitches();
        refreshThreat();
        refreshLiveTraffic();
        refreshVpnLine();
        if (agent != null) {
            rulesAdapter.setRules(agent.getFirewall().getRules());
            refreshEvents();
        }
        if (agent != null && btnLockdown != null) {
            btnLockdown.setText(agent.getFirewall().isLockdown()
                    ? R.string.defender_clear_lockdown : R.string.defender_lockdown_now);
        }
    }

    private void refreshSwitches() {
        if (agent == null) {
            return;
        }
        suppressSwitchCallbacks = true;
        try {
            swAutoProtect.setChecked(agent.getAutoProtect().isAutoProtectEnabled());
            swFirewall.setChecked(agent.getFirewall().isEnabled());
            swTraffic.setChecked(agent.getTrafficMonitor().isRunning()
                    || agent.isTrafficMonitorEnabled());
            swLteSpoof.setChecked(agent.isLteSpoofEnabled());
            swAutoRules.setChecked(agent.isAutoRulesEnabled());
        } finally {
            suppressSwitchCallbacks = false;
        }
    }

    private void refreshThreat() {
        if (agent == null) {
            return;
        }
        DefenderAgent.ThreatLevel level = agent.getCurrentLevel();
        threatView.setText(getString(R.string.defender_threat_level, level.name()));
        int color;
        switch (level) {
            case CRITICAL:
                color = 0xFFFF5252;
                break;
            case HIGH:
                color = 0xFFFF9800;
                break;
            case MEDIUM:
                color = 0xFFFFEB3B;
                break;
            case LOW:
                color = 0xFF8BC34A;
                break;
            default:
                color = 0xFF9E9E9E;
                break;
        }
        threatView.setTextColor(color);
        cellView.setText(agent.getLastCellFingerprint());
    }

    private void refreshLiveTraffic() {
        if (agent == null) {
            return;
        }
        TrafficSnapshot snap = agent.getTrafficMonitor().getLastSnapshot();
        if (snap == null) {
            boolean on = agent.getTrafficMonitor().isRunning();
            trafficView.setText(on ? getString(R.string.defender_traffic_waiting)
                    : getString(R.string.defender_traffic_off));
            talkersView.setText("");
            return;
        }
        renderSnapshot(snap);
    }

    private void renderSnapshot(TrafficSnapshot snap) {
        StringBuilder sb = new StringBuilder();
        sb.append("IN  ").append(TrafficSnapshot.formatRate(snap.rxBytesPerSec))
                .append("  (total ").append(TrafficSnapshot.formatTotal(snap.totalRxBytes)).append(")\n");
        sb.append("OUT ").append(TrafficSnapshot.formatRate(snap.txBytesPerSec))
                .append("  (total ").append(TrafficSnapshot.formatTotal(snap.totalTxBytes)).append(")\n");
        sb.append("Mobile IN/OUT: ").append(TrafficSnapshot.formatRate(snap.mobileRxPerSec))
                .append(" / ").append(TrafficSnapshot.formatRate(snap.mobileTxPerSec));
        trafficView.setText(sb.toString());

        StringBuilder t = new StringBuilder();
        List<TrafficSnapshot.UidRate> talkers = snap.getTopTalkers();
        if (talkers.isEmpty()) {
            t.append(getString(R.string.defender_no_talkers));
        } else {
            for (TrafficSnapshot.UidRate r : talkers) {
                t.append(r.packageName)
                        .append("  IN ").append(TrafficSnapshot.formatRate(r.rxBytesPerSec))
                        .append("  OUT ").append(TrafficSnapshot.formatRate(r.txBytesPerSec))
                        .append('\n');
            }
        }
        talkersView.setText(t.toString().trim());
    }

    private void refreshVpnLine() {
        if (agent == null) {
            return;
        }
        boolean rooted = false;
        try {
            rooted = agent.getFirewall().isRooted();
        } catch (Exception ignored) {
        }
        String backend = rooted ? "iptables (root)" : "VPN sinkhole (non-root)";
        String vpn = DefenderVpnService.isRunning()
                ? "VPN sinkhole ON (" + DefenderVpnService.getRouteCount() + " routes, "
                + DefenderVpnService.getDroppedPackets() + " pkts dropped)"
                : "VPN sinkhole OFF";
        StringBuilder line = new StringBuilder(backend).append('\n').append(vpn);
        if (!rooted && !DefenderVpnService.isRunning() && agent.getFirewall().isEnabled()
                && !agent.getFirewall().getEnabledDenyIpRules().isEmpty()) {
            // DENY rules exist but nothing enforces them yet: tell the user why.
            line.append('\n').append(getString(R.string.defender_vpn_consent_hint));
        }
        vpnView.setText(line.toString());
        btnVpn.setText(DefenderVpnService.isRunning()
                ? R.string.defender_vpn_stop : R.string.defender_vpn_start);
    }

    private void refreshEvents() {
        if (agent == null) {
            return;
        }
        eventLines.clear();
        List<DefenderEvent> events = agent.getLogStore().recent(30);
        if (events.isEmpty()) {
            eventLines.add(getString(R.string.defender_no_events));
        } else {
            for (DefenderEvent e : events) {
                eventLines.add("[" + e.getThreatLevel() + "] " + e.getTitle());
            }
        }
        eventsAdapter.notifyDataSetChanged();
    }

    // ------------------------------------------------------------------
    // Switches / dialogs / VPN
    // ------------------------------------------------------------------

    private void wireSwitches() {
        CompoundButton.OnCheckedChangeListener l = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (suppressSwitchCallbacks || agent == null) {
                    return;
                }
                int id = buttonView.getId();
                if (id == R.id.defender_sw_autoprotect) {
                    agent.setAutoProtectEnabled(isChecked);
                } else if (id == R.id.defender_sw_firewall) {
                    agent.getFirewall().setEnabled(isChecked);
                } else if (id == R.id.defender_sw_traffic) {
                    agent.setTrafficMonitorEnabled(isChecked);
                } else if (id == R.id.defender_sw_lte) {
                    agent.setLteSpoofEnabled(isChecked);
                } else if (id == R.id.defender_sw_autorules) {
                    agent.setAutoRulesEnabled(isChecked);
                }
                refreshAll();
            }
        };
        swAutoProtect.setOnCheckedChangeListener(l);
        swFirewall.setOnCheckedChangeListener(l);
        swTraffic.setOnCheckedChangeListener(l);
        swLteSpoof.setOnCheckedChangeListener(l);
        swAutoRules.setOnCheckedChangeListener(l);
    }

    private void showAddRuleDialog() {
        if (agent == null || getActivity() == null) {
            return;
        }
        final EditText input = new EditText(getActivity());
        input.setHint("203.0.113.7  or  203.0.113.0/24");
        AlertDialog.Builder b = new AlertDialog.Builder(getActivity());
        b.setTitle(R.string.defender_add_rule_title);
        b.setView(input);
        b.setPositiveButton(R.string.text_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String ip = input.getText().toString().trim();
                if (ip.isEmpty()) {
                    return;
                }
                agent.getFirewall().addRule(FirewallRule.Direction.OUT,
                        FirewallRule.Action.DENY, ip, -1, "", -1,
                        "manual block from UI", false);
                if (!agent.getFirewall().isEnabled()) {
                    agent.getFirewall().setEnabled(true);
                }
                refreshAll();
            }
        });
        b.setNegativeButton(R.string.text_cancel, null);
        b.show();
    }

    private void toggleVpn() {
        if (getActivity() == null) {
            return;
        }
        if (DefenderVpnService.isRunning()) {
            Intent stop = new Intent(getActivity(), DefenderVpnService.class);
            stop.setAction(DefenderVpnService.ACTION_STOP);
            getActivity().startService(stop);
            refreshVpnLine();
            return;
        }
        // Starting a VpnService needs user consent via system dialog.
        Intent prepare = VpnService.prepare(getActivity());
        if (prepare != null) {
            try {
                startActivityForResult(prepare, REQ_VPN_PERMISSION);
                return;
            } catch (Exception e) {
                Helpers.msgShort(getActivity(), "VPN permission failed: " + e.getMessage());
                return;
            }
        }
        startVpnService();
    }

    private void startVpnService() {
        if (getActivity() == null) {
            return;
        }
        Intent start = new Intent(getActivity(), DefenderVpnService.class);
        start.setAction(DefenderVpnService.ACTION_START);
        getActivity().startService(start);
        ui.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isAdded()) {
                    refreshVpnLine();
                }
            }
        }, 800);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_VPN_PERMISSION && resultCode == android.app.Activity.RESULT_OK) {
            startVpnService();
        }
    }
}
