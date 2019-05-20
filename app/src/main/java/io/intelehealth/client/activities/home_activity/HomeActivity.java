package io.intelehealth.client.activities.home_activity;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;

import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.io.File;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import io.intelehealth.client.R;
import io.intelehealth.client.activities.login_activity.LoginActivity;
import io.intelehealth.client.activities.login_activity.OfflineLogin;
import io.intelehealth.client.activities.setting_activity.SettingsActivity;
import io.intelehealth.client.services.DownloadProtocolsTask;
import io.intelehealth.client.utilities.NetworkConnection;


/**
 * Home Screen
 */

public class HomeActivity extends AppCompatActivity {

    String value = "";
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor e;
    String backupdate, backuptime;
    Calendar calendar;

    Handler handler;
    Runnable runnable;

    private static final String TAG = HomeActivity.class.getSimpleName();

    @Override
    public void onBackPressed(){
        new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to EXIT ?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                         moveTaskToBack(true);

                    }

                })
                .setNegativeButton("No", null)
                .show();    //prajwal_changes
        //do nothing
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
               // manageBackup(true,false);
                logout();
            }
        };

        startHandler(); //will start the timer.

        SharedPreferences prefs = getSharedPreferences("CommonPrefs",
                Activity.MODE_PRIVATE);
        String ln = prefs.getString("Language","");

        if(!ln.equalsIgnoreCase("")){
            Locale locale = new Locale(ln);
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
        }
        setTitle(R.string.title_activity_login);
        Log.i(TAG, "onCreate: " + getFilesDir().toString());

        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerview_home);
        recyclerView.setHasFixedSize(true);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(HomeActivity.this, 1);
        recyclerView.setLayoutManager(gridLayoutManager);

        recyclerView.setAdapter(new HomeAdapter());

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        e = sharedPreferences.edit();
        backupdate = sharedPreferences.getString("date", "");
        backuptime = sharedPreferences.getString("time", "");


        final Calendar startDate = Calendar.getInstance();
        startDate.set(Calendar.HOUR, 10);
        startDate.set(Calendar.MINUTE, 00);
        startDate.set(Calendar.AM_PM, Calendar.PM);


        final Calendar endDate = Calendar.getInstance();
        endDate.set(Calendar.HOUR, 10);
        endDate.set(Calendar.MINUTE, 15);
        endDate.set(Calendar.AM_PM, Calendar.PM);

        if (getIntent().hasExtra("setup") && getIntent().
                getBooleanExtra("setup", false) == true) {

            //parseLoginValidation();

            String dbfilepath = Environment.getExternalStorageDirectory() + File.separator + "InteleHealth_DB" +
                    File.separator + "Intelehealth.db"; // directory: Intelehealth_DB   ,  filename: Intelehealth.db
            Log.d("newfilepath", dbfilepath);
            final File db_file = new File(dbfilepath);

            if (db_file.exists()) {
                new AlertDialog.Builder(this)
                        .setIcon(R.drawable.ic_file_download_black_48px)
                        .setTitle(R.string.local_restore_alert_title)
                        .setMessage(R.string.local_restore_alert_message)
                        .setCancelable(false)
                        .setPositiveButton(R.string.generic_yes,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        //Restore from local backup
                                        manageBackup(false, false); // to restore app data if db is empty
                                    }
                                }
                        )
                        .setNegativeButton(R.string.generic_no,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        //Do Nothing!
                                        // db_file.delete();
                                    }
                                }
                        )
                        .create().show();
            }
        }

        /*
        if (getIntent().hasExtra("login") && getIntent().
                getBooleanExtra("login", false) == true) {
            parseLoginValidation();
        }*/

        /*handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                long start = startDate.getTimeInMillis();
                long end = endDate.getTimeInMillis();
                calendar = Calendar.getInstance();

                if (start < calendar.getTimeInMillis() &&
                        calendar.getTimeInMillis() < end) {
                    // Toast.makeText(HomeActivity.this,"backup started",Toast.LENGTH_SHORT).show();
                    manageBackup(true, false);
                }
                handler.postDelayed(this, 1000 * 60);
            }
        }, 1000 * 60);*/

        //handler = new Handler();

    }

    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
        stopHandler();  //first stop the timer and then again start it
        startHandler();
    }

    public void stopHandler() {
        handler.removeCallbacks(runnable);
    }
    public void startHandler() {

        handler.postDelayed(runnable, 30*60*1000); //for 30 minutes
    }

   /* @Override
    protected void onPause() {

        stopHandler();
        Log.d("onPause", "onPauseActivity change");
        super.onPause();

    }

    @Override
    protected void onResume() {
        super.onResume();
        startHandler();

        Log.d("onResume", "onResume_restartActivity");

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopHandler();
        Log.d("onDestroy", "onDestroyActivity change");

    }*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_home, menu);
        return super.onCreateOptionsMenu(menu);

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
//            case R.id.syncOption:
//                refreshDatabases();
//                return true;
            case R.id.settingsOption:
                settings();
                return true;
            case R.id.updateProtocolsOption: {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                if (sharedPreferences.contains("licensekey")) {
                    String license = sharedPreferences.getString("licensekey", null);
                    if (license != null) {
                        DownloadProtocolsTask downloadProtocolsTask = new DownloadProtocolsTask(this);
                        downloadProtocolsTask.execute(license);
                    } else {
                        Toast.makeText(this, "License invalid", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(this);
                    LayoutInflater li = LayoutInflater.from(this);
                    View promptsView = li.inflate(R.layout.dialog_mindmap_cred, null);
                    dialog.setTitle(getString(R.string.enter_license_key))
                            .setView(promptsView)
                            .setPositiveButton(getString(R.string.button_ok), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Dialog d = (Dialog) dialog;

                                    EditText text = (EditText) d.findViewById(R.id.licensekey);
                                    String key = text.getText().toString();
                                    if (key != null && !key.trim().isEmpty()) {
                                        DownloadProtocolsTask downloadProtocolsTask = new DownloadProtocolsTask(HomeActivity.this);
                                        downloadProtocolsTask.execute(key);
                                    }
                                }
                            })
                            .setNegativeButton(getString(R.string.button_cancel), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    dialog.create().show();

                }
                return true;
            }
            case R.id.backupOption:
                manageBackup(true, false);  // to backup app data at any time of the day
                return true;

            case R.id.restoreOption:
                manageBackup(false, false); // to restore app data if db is empty
                return true;

            case R.id.logoutOption:
                manageBackup(true, false);
                logout();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * This method starts intent to another activity to change settings
     *
     * @return void
     */
    public void settings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    /**
     * This method sync all the data recorded to server and sync back locally.
     *
     * @return void
     */

    public void endOfDay() {
        // TODO: sync all the data recorded to EHR, and sync back locally
        // Information to sync includes credentials and patient info
        // Bandwidth heavy task
        try {
            throw new NullPointerException();
        } catch (NullPointerException caughtException) {

        }
    }

    /**
     * Logs out the user. It removes user account using AccountManager.
     *
     * @return void
     */
    public void logout() {

        OfflineLogin.getOfflineLogin().setOfflineLoginStatus(false);

        parseLogOut();

        AccountManager manager = AccountManager.get(HomeActivity.this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Account[] accountList = manager.getAccountsByType("io.intelehealth.openmrs");
        if (accountList.length > 0) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                manager.removeAccount(accountList[0], HomeActivity.this, null, null);
            } else {
                manager.removeAccount(accountList[0], null, null); // Legacy implementation
            }
        }

        Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    public void manageBackup(boolean isBackup, boolean isForced) {
        BackupCloud b = new BackupCloud(this);
        if (isBackup)
            b.startCloudBackup(null,false);
        if (!isBackup) {
            if (isForced) b.cloudRestoreForced();
            if (!isForced) b.startCloudRestore();
        }
    }

    //Archiving 477 -Login Validation by Parse
    /*
    private void parseLoginValidation() {
        if (NetworkConnection.isOnline(this)) {
            ParseObject register_login = new ParseObject("Login");
            register_login.put("userId", sharedPreferences.getString("creatorid", null));
            register_login.put("location", sharedPreferences.getString(SettingsActivity.KEY_PREF_LOCATION_NAME, null));
            String aid = IntelehealthApplication.getAndroidId();
            register_login.put("deviceId",aid);
            try {
                register_login.save();
                Toast.makeText(this, getString(R.string.user_login_check_success), Toast.LENGTH_SHORT).show();
            } catch (ParseException e1) {
                switch (e1.getCode()) {
                    case 401:{
                        Toast.makeText(this, getString(R.string.user_logged_in_other_device), Toast.LENGTH_SHORT).show();
                        logout();
                        break;
                    }
                    case 208: {
                        Toast.makeText(this, getString(R.string.user_logged_in), Toast.LENGTH_SHORT).show();
                        break;
                    }
                    case 403: {
                        Toast.makeText(this, getString(R.string.user_login_error), Toast.LENGTH_LONG).show();
                        logout();
                        break;
                    }
                    case 400: {
                        Toast.makeText(this, e1.getMessage(), Toast.LENGTH_SHORT).show();
                        logout();
                        break;
                    }
                    default: {
                        Toast.makeText(this, e1.getMessage(), Toast.LENGTH_SHORT).show();
                        break;
                    }
                }
            }
        } else {
            Toast.makeText(this, getString(R.string.user_server_unreachable), Toast.LENGTH_SHORT).show();
        }
    }
    */

    private void parseLogOut() {
        if (NetworkConnection.isOnline(this)) {
            ParseQuery<ParseObject> getLogin = ParseQuery.getQuery("Login");
            getLogin.whereEqualTo("userId", sharedPreferences.getString("creatorid", null));
            try {
                List<ParseObject> loginList = getLogin.find();
                if (loginList != null && !loginList.isEmpty()) {
                    for (ParseObject login : loginList)
                        login.delete();
                }
            } catch (ParseException e1) {
                Log.e(TAG, "parseLogOut: ", e1);
            }
        }
    }

}
