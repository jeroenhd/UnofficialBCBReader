package nl.jeroenhd.app.bcbreader.activities;


import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.webkit.WebView;

import java.util.List;

import nl.jeroenhd.app.bcbreader.R;
import nl.jeroenhd.app.bcbreader.data.App;
import nl.jeroenhd.app.bcbreader.data.Telemetry;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p/>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends AppCompatPreferenceActivity {
    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static final Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);

            } else if (preference instanceof RingtonePreference) {
                // For ringtone preferences, look up the correct display value
                // using RingtoneManager.
                if (TextUtils.isEmpty(stringValue)) {
                    // Empty values correspond to 'silent' (no ringtone).
                    preference.setSummary(R.string.pref_ringtone_silent);

                } else {
                    Ringtone ringtone = RingtoneManager.getRingtone(
                            preference.getContext(), Uri.parse(stringValue));

                    if (ringtone == null) {
                        // Clear the summary if there was a lookup error.
                        preference.setSummary(null);
                    } else {
                        // Set the summary to reflect the new ringtone display
                        // name.
                        String name = ringtone.getTitle(preference.getContext());
                        preference.setSummary(name);
                    }
                }

            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary(stringValue);
            }
            return true;
        }
    };

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.pref_headers, target);
    }

    /**
     * This method stops fragment injection in malicious applications.
     * Make sure to deny any unknown fragments here.
     */
    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName)
                || NotificationPreferenceFragment.class.getName().equals(fragmentName)
                || TelemetryPreferenceFragment.class.getName().equals(fragmentName)
                || AboutPreferenceFragment.class.getName().equals(fragmentName)
                || ReadingPreferenceFragment.class.getName().equals(fragmentName);
    }

    public static class ReadingPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_reading);
            setHasOptionsMenu(true);

            bindPreferenceSummaryToValue(findPreference("reading_quality"));
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                Activity activity = getActivity();
                if (activity != null) {
                    activity.onBackPressed();
                } else {
                    startActivity(new Intent(getActivity(), SettingsActivity.class));
                }
            }
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * This fragment shows notification preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class NotificationPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_notification);
            setHasOptionsMenu(true);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(findPreference("notifications_ringtone"));
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class AboutPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_about);
            setHasOptionsMenu(true);

            Preference volleyLicense = findPreference("pref_license_volley");
            Preference gsonLicense = findPreference("pref_license_gson");
            Preference dbflowLicense = findPreference("pref_license_dbflow");
            Preference jobLicense = findPreference("pref_license_android-job");
            Preference versionName = findPreference("app_version");

            volleyLicense.setOnPreferenceClickListener(preference -> {
                showLicense("volley_license.html");
                return false;
            });
            gsonLicense.setOnPreferenceClickListener(preference -> {
                showLicense("gson_license.txt");
                return false;
            });
            dbflowLicense.setOnPreferenceClickListener(preference -> {
                showLicense("dbflow_license.txt");
                return false;
            });
            jobLicense.setOnPreferenceClickListener((preference) -> {
                showLicense("Android-Job_license.html");
                return false;
            });

            assert versionName != null;
            versionName.setSummary(App.Version());
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                Activity activity = getActivity();
                if (activity != null) {
                    activity.onBackPressed();
                } else {
                    startActivity(new Intent(getActivity(), SettingsActivity.class));
                }
                return true;
            }
            return super.onOptionsItemSelected(item);
        }

        private void showLicense(String licenseAssetFile) {
            Context context = getActivity();
            WebView view = (WebView) LayoutInflater.from(context).inflate(R.layout.activity_license, (ViewGroup) getView(), false);
            view.loadUrl("file:///android_asset/" + licenseAssetFile);
            new AlertDialog.Builder(context, R.style.AppTheme)
                    .setTitle(getString(R.string.title_license))
                    .setView(view)
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
        }
    }

    /**
     * This fragment shows data and sync preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class TelemetryPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_telemetry);
            setHasOptionsMenu(true);

            Preference model, ram, androidVersion, storage, sdcard, sdcardEmulated, uniqueID, appVersion;
            Preference sendNow;

            model = findPreference("telemetry_model");
            ram = findPreference("telemetry_ram");
            androidVersion = findPreference("telemetry_android_version");
            storage = findPreference("telemetry_storage");
            sdcard = findPreference("telemetry_sdcard");
            sdcardEmulated = findPreference("telemetry_sdcard_emulated");
            uniqueID = findPreference("telemetry_id");
            appVersion = findPreference("app_version");

            final Telemetry telemetry = Telemetry.getInstance(getActivity());
            model.setSummary(telemetry.getModel());
            ram.setSummary(telemetry.getRAMSizeMB() + "MB");
            androidVersion.setSummary(telemetry.getAndroidVersion());
            storage.setSummary(telemetry.getInternalFreeMB() + "MB free out of " + telemetry.getInternalSizeMB() + "MB total");
            sdcard.setSummary(telemetry.getSDCardFreeMB() + "MB free out of " + telemetry.getSDCardSizeMB() + "MB total");
            sdcardEmulated.setSummary(telemetry.isSDCardEmulated() ? "Yes" : "No");
            uniqueID.setSummary(telemetry.getUniqueID());
            appVersion.setSummary(App.Version());

            sendNow = findPreference("telemetry_send_manually");
            sendNow.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    telemetry.send(true, getActivity());
                    return false;
                }
            });
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                Activity activity = getActivity();
                if (activity != null) {
                    activity.onBackPressed();
                } else {
                    startActivity(new Intent(getActivity(), SettingsActivity.class));
                }
            }
            return super.onOptionsItemSelected(item);
        }
    }
}
