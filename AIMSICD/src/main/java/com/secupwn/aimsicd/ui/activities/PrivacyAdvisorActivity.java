/* Android IMSI-Catcher Detector | (c) AIMSICD Privacy Project
 * -----------------------------------------------------------
 * LICENSE:  http://git.io/vki47 | TERMS:  http://git.io/vki4o
 * -----------------------------------------------------------
 */
package com.secupwn.aimsicd.ui.activities;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.secupwn.aimsicd.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

public class PrivacyAdvisorActivity extends BaseActivity {

	private ListView listView;
	private ProgressBar progressBar;
	private TextView emptyView;

	private ArrayAdapter<AppPerms> adapter;
	private final List<AppPerms> items = new ArrayList<>();

	private static final Set<String> TELEPHONY_PERMS = new HashSet<>(Arrays.asList(
			android.Manifest.permission.READ_PHONE_STATE,
			android.Manifest.permission.READ_PHONE_NUMBERS,
			android.Manifest.permission.READ_SMS,
			android.Manifest.permission.READ_CALL_LOG
	));

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_privacy_advisor);

		listView = (ListView) findViewById(R.id.privacy_list);
		progressBar = (ProgressBar) findViewById(R.id.privacy_progress);
		emptyView = (TextView) findViewById(R.id.privacy_empty);

		adapter = new ArrayAdapter<AppPerms>(this, android.R.layout.simple_list_item_2, android.R.id.text1, items) {
			@Override
			public View getView(int position, View convertView, android.view.ViewGroup parent) {
				View v = super.getView(position, convertView, parent);
				TextView t1 = (TextView) v.findViewById(android.R.id.text1);
				TextView t2 = (TextView) v.findViewById(android.R.id.text2);
				AppPerms ap = getItem(position);
				if (ap != null) {
					t1.setText(ap.label + " (" + ap.packageName + ")");
					t2.setText(getString(R.string.privacy_perms_count, ap.relevantGrantedCount, ap.relevantRequestedCount));
				}
				return v;
			}
		};
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				AppPerms ap = adapter.getItem(position);
				if (ap != null) {
					showDetailsDialog(ap);
				}
			}
		});

		loadData();
	}

	private void loadData() {
		progressBar.setVisibility(View.VISIBLE);
		emptyView.setVisibility(View.GONE);
		listView.setVisibility(View.GONE);

		new Thread(new Runnable() {
			@Override
			public void run() {
				final List<AppPerms> result = scanInstalled();
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						items.clear();
						items.addAll(result);
						adapter.notifyDataSetChanged();

						progressBar.setVisibility(View.GONE);
						if (items.isEmpty()) {
							emptyView.setVisibility(View.VISIBLE);
							listView.setVisibility(View.GONE);
						} else {
							listView.setVisibility(View.VISIBLE);
							emptyView.setVisibility(View.GONE);
						}
					}
				});
			}
		}).start();
	}

	private List<AppPerms> scanInstalled() {
		PackageManager pm = getPackageManager();
		List<PackageInfo> pkgs = pm.getInstalledPackages(PackageManager.GET_PERMISSIONS);
		List<AppPerms> result = new ArrayList<>();

		for (PackageInfo pi : pkgs) {
			if (pi.requestedPermissions == null || pi.requestedPermissions.length == 0) {
				continue;
			}
			List<String> requested = new ArrayList<>();
			List<String> granted = new ArrayList<>();

			for (int i = 0; i < pi.requestedPermissions.length; i++) {
				String perm = pi.requestedPermissions[i];
				if (!TELEPHONY_PERMS.contains(perm)) continue;

				requested.add(perm);
				boolean isGranted = false;
				if (pi.requestedPermissionsFlags != null && pi.requestedPermissionsFlags.length > i) {
					isGranted = (pi.requestedPermissionsFlags[i] & PackageInfo.REQUESTED_PERMISSION_GRANTED) != 0;
				} else {
					isGranted = pm.checkPermission(perm, pi.packageName) == PackageManager.PERMISSION_GRANTED;
				}
				if (isGranted) {
					granted.add(perm);
				}
			}

			if (!requested.isEmpty()) {
				CharSequence label;
				try {
					label = pm.getApplicationLabel(pm.getApplicationInfo(pi.packageName, 0));
				} catch (PackageManager.NameNotFoundException e) {
					label = pi.packageName;
				}
				result.add(new AppPerms(pi.packageName, label.toString(), requested, granted));
			}
		}

		Collections.sort(result, new Comparator<AppPerms>() {
			@Override
			public int compare(AppPerms o1, AppPerms o2) {
				// Sort by granted count desc, then label
				int g = Integer.valueOf(o2.relevantGrantedCount).compareTo(o1.relevantGrantedCount);
				if (g != 0) return g;
				return o1.label.compareToIgnoreCase(o2.label);
			}
		});

		return result;
	}

	private void showDetailsDialog(final AppPerms ap) {
		StringBuilder sb = new StringBuilder();
		sb.append(getString(R.string.privacy_dialog_pkg)).append(" ").append(ap.packageName).append("\n\n");
		if (!ap.relevantRequested.isEmpty()) {
			sb.append(getString(R.string.privacy_dialog_requested)).append("\n");
			for (String rp : ap.relevantRequested) {
				sb.append("• ").append(humanizePermission(rp));
				if (ap.relevantGranted.contains(rp)) {
					sb.append(" ").append(getString(R.string.privacy_dialog_granted));
				} else {
					sb.append(" ").append(getString(R.string.privacy_dialog_denied));
				}
				sb.append("\n");
			}
		}

		final String adb = buildAdbCommands(ap.packageName, ap.relevantRequested);

		new AlertDialog.Builder(this)
				.setTitle(getString(R.string.privacy_dialog_title, ap.label))
				.setMessage(sb.toString())
				.setPositiveButton(R.string.privacy_copy_cmds, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						copyToClipboard(adb);
					}
				})
				.setNegativeButton(android.R.string.cancel, null)
				.show();
	}

	private String humanizePermission(String perm) {
		if (android.Manifest.permission.READ_PHONE_STATE.equals(perm)) return getString(R.string.perm_read_phone_state);
		if (android.Manifest.permission.READ_PHONE_NUMBERS.equals(perm)) return getString(R.string.perm_read_phone_numbers);
		if (android.Manifest.permission.READ_SMS.equals(perm)) return getString(R.string.perm_read_sms);
		if (android.Manifest.permission.READ_CALL_LOG.equals(perm)) return getString(R.string.perm_read_call_log);
		return perm;
	}

	private void copyToClipboard(String text) {
		ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
		if (cm != null) {
			cm.setPrimaryClip(ClipData.newPlainText("adb", text));
		}
	}

	private String buildAdbCommands(String pkg, List<String> perms) {
		StringBuilder sb = new StringBuilder();
		sb.append("# Replace PKG if needed\n");
		sb.append("PKG=").append(pkg).append("\n\n");
		for (String p : perms) {
			sb.append("adb shell pm revoke $PKG ").append(p).append("\n");
		}
		// App-ops clamps
		sb.append("\n# App-ops clamps (Android 10+)\n");
		sb.append("adb shell cmd appops set $PKG READ_DEVICE_IDENTIFIERS ignore\n");
		sb.append("adb shell cmd appops set $PKG READ_PHONE_STATE ignore\n");
		sb.append("adb shell cmd appops set $PKG READ_PHONE_NUMBERS ignore\n");
		sb.append("adb shell cmd appops set $PKG READ_SMS ignore\n");
		sb.append("adb shell cmd appops set $PKG READ_CALL_LOG ignore\n");
		sb.append("\n# Reset app\n");
		sb.append("adb shell am force-stop $PKG\n");
		sb.append("adb shell pm clear $PKG\n");
		return sb.toString();
	}

	@AllArgsConstructor
	@ToString
	private static class AppPerms {
		@Getter
		final String packageName;
		@Getter
		final String label;
		@Getter
		final List<String> relevantRequested;
		@Getter
		final List<String> relevantGranted;

		final int relevantRequestedCount() { return relevantRequested == null ? 0 : relevantRequested.size(); }
		final int relevantGrantedCount() { return relevantGranted == null ? 0 : relevantGranted.size(); }
	}
}


