package io.intelehealth.client.activities.setup_activity;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.webkit.URLUtil;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.Driver;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.Trigger;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.parse.Parse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import io.intelehealth.client.R;
import io.intelehealth.client.activities.home_activity.HomeActivity;
import io.intelehealth.client.activities.login_activity.AdminPassword;
import io.intelehealth.client.activities.login_activity.OfflineLogin;
import io.intelehealth.client.activities.setting_activity.SettingsActivity;
import io.intelehealth.client.api.retrofit.RestApi;
import io.intelehealth.client.api.retrofit.ServiceGenerator;
import io.intelehealth.client.application.IntelehealthApplication;
import io.intelehealth.client.models.Location;
import io.intelehealth.client.models.Results;
import io.intelehealth.client.objects.WebResponse;
import io.intelehealth.client.services.DownloadProtocolsTask;
import io.intelehealth.client.services.sync.JobDispatchService;
import io.intelehealth.client.utilities.HelperMethods;
import io.intelehealth.client.utilities.SessionManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * One time setup which requires OpenMRS server URL and user permissions
 */
public class SetupActivity extends AppCompatActivity {

    private static final String TAG = SetupActivity.class.getSimpleName();

    private TestSetup mAuthTask = null;

    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private EditText mAdminPasswordView;
    protected AccountManager manager;
    private EditText mUrlField;

    private Button mLoginButton;

    private Spinner mDropdownLocation;

    private TextView mAndroidIdTextView;


    private boolean isLocationFetched = false;

    private RadioButton r1;
    private RadioButton r2;
    private List<Location> mLocations = new ArrayList<>();


    private static final int PERMISSION_ALL = 1;
    public File base_dir;
    public String[] FILES;

    AlertDialog.Builder dialog;
    String key = null;
    SessionManager sessionManager;

    final Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    sessionManager=new SessionManager(getApplicationContext());
        mDropdownLocation = (Spinner) findViewById(R.id.spinner_location);

        // Persistent login information
        manager = AccountManager.get(SetupActivity.this);

        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        // populateAutoComplete(); TODO: create our own autocomplete code

        mLoginButton = (Button) findViewById(R.id.setup_submit_button);
        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptLogin();
            }
        });

        r1 = (RadioButton) findViewById(R.id.demoMindmap);
        r2 = (RadioButton) findViewById(R.id.downloadMindmap);

        mPasswordView = (EditText) findViewById(R.id.password);

        mAdminPasswordView = (EditText) findViewById(R.id.admin_password);
        mAdminPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        mAndroidIdTextView = (TextView) findViewById(R.id.textView_Aid);
        mAndroidIdTextView.setText("Android Id: "+ IntelehealthApplication.getAndroidId());

        Button mEmailSignInButton = (Button) findViewById(R.id.setup_submit_button);
        mEmailSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });


        mUrlField = (EditText) findViewById(R.id.editText_URL);

        Button submitButton = (Button) findViewById(R.id.setup_submit_button);


        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
                //progressBar.setVisibility(View.VISIBLE);
                //progressBar.setProgress(0);

            }
        });

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(R.string.generic_warning);
        alertDialogBuilder.setMessage(R.string.setup_internet);
        alertDialogBuilder.setNeutralButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();

        String[] PERMISSIONS = {Manifest.permission.GET_ACCOUNTS,
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA,
                Manifest.permission.INTERNET,
                Manifest.permission.ACCOUNT_MANAGER
        };

        if (!hasPermissions(this, PERMISSIONS))

        {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }

        mUrlField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                isLocationFetched = false;
                LocationArrayAdapter adapter = new LocationArrayAdapter(SetupActivity.this, new ArrayList<String>());
                mDropdownLocation.setAdapter(adapter);
            }

            @Override
            public void afterTextChanged(Editable s) {

                mHandler.removeCallbacksAndMessages(null);
                mHandler.postDelayed(userStoppedTyping, 1500); // 1.5 second


            }

            Runnable userStoppedTyping = new Runnable() {

                @Override
                public void run() {
                    // user didn't typed for 1.5 seconds, do whatever you want
                    if (!mUrlField.getText().toString().trim().isEmpty() && mUrlField.getText().toString().length() >= 12 ) {
                        if (Patterns.WEB_URL.matcher(mUrlField.getText().toString()).matches()) {
                            String BASE_URL = "http://" + mUrlField.getText().toString() + ":8080/openmrs/ws/rest/v1/";
                            if (URLUtil.isValidUrl(BASE_URL) && !isLocationFetched)
                                getLocationFromServer(BASE_URL);
                            else
                                Toast.makeText(SetupActivity.this, getString(R.string.url_invalid), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            };

        });



       /* mDropdownLocation.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.i(TAG,"position :"+position);
                if(mLocations!=null)
                Log.i(TAG,mLocations.get(position).getName());
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });*/


    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_ALL:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.e("value", "Permission Granted, Now you can use local drive .");
                } else {
                    Log.e("value", "Permission Denied, You cannot use local drive .");
                }
                break;
        }
    }

    public boolean hasPermissions(Context context, String... permissions) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Check username and password validations.
     * Get user selected location.
     */
    private void attemptLogin() {


        if (mAuthTask != null) {
            return;
        }


        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);
        mAdminPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();
        String admin_password = mAdminPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        if (!TextUtils.isEmpty(admin_password) && !isPasswordValid(admin_password)) {
            mAdminPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mAdminPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;

        }
        Location location = null;
        if (mDropdownLocation.getSelectedItemPosition() <= 0) {
            cancel = true;
            Toast.makeText(SetupActivity.this, getString(R.string.error_location_not_selected), Toast.LENGTH_LONG);
        } else {
            location = mLocations.get(mDropdownLocation.getSelectedItemPosition() - 1);
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            if (focusView != null)
                focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            if (location != null) {
                Log.i(TAG, location.getDisplay());
                String urlString = mUrlField.getText().toString();
                mAuthTask = new TestSetup(urlString, email, password, admin_password, location);
                mAuthTask.execute();
                Log.d(TAG, "attempting setup");
            }
        }
    }


    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return true;
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    /**
     * Attempts login to the OpenMRS server.
     * If successful cretes a new {@link Account}
     * If unsuccessful details are saved in SharedPreferences.
     */
    private class TestSetup extends AsyncTask<Void, Void, Integer> {

        private final String USERNAME;
        private final String PASSWORD;
        private final String CLEAN_URL;
        private final String ADMIN_PASSWORD;
        private String BASE_URL;
        private Location LOCATION;

        ProgressDialog progress;


        TestSetup(String url, String username, String password, String adminPassword, Location location) {
            CLEAN_URL = url;
            USERNAME = username;
            PASSWORD = password;
            LOCATION = location;
            ADMIN_PASSWORD = adminPassword;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progress = new ProgressDialog(SetupActivity.this);
            progress.setTitle(getString(R.string.please_wait_progress));
            progress.setMessage(getString(R.string.logging_in));
            progress.show();
        }


        @Override
        protected Integer doInBackground(Void... params) {
            BufferedReader reader;
            String JSONString;

            WebResponse loginAttempt = new WebResponse();

            try {
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

                Log.d(TAG, "UN: " + USERNAME);
                Log.d(TAG, "PW: " + PASSWORD);

                String urlModifier = "session";


                BASE_URL = "http://" + CLEAN_URL + ":8080/openmrs/ws/rest/v1/";
                String urlString = BASE_URL + urlModifier;

                URL url = new URL(urlString);

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                String encoded = Base64.encodeToString((USERNAME + ":" + PASSWORD).getBytes("UTF-8"), Base64.NO_WRAP);
                sessionManager.setEncoded(encoded);
                connection.setRequestProperty("Authorization", "Basic " + encoded);
                connection.setRequestMethod("GET");
                connection.setRequestProperty("USER-AGENT", "Mozilla/5.0");
                connection.setRequestProperty("ACCEPT-LANGUAGE", "en-US,en;0.5");

                int responseCode = connection.getResponseCode();
                loginAttempt.setResponseCode(responseCode);

                // Read the input stream into a String
                InputStream inputStream = connection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Do Nothing.
                    return 201;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return 201;
                }

                JSONString = buffer.toString();

                Log.d(TAG, "JSON Response: " + JSONString);
                loginAttempt.setResponseString(JSONString);
                if (loginAttempt != null && loginAttempt.getResponseCode() != 200) {
                    Log.d(TAG, "Login request was unsuccessful");
                    return loginAttempt.getResponseCode();
                } else if (loginAttempt == null) {
                    return 201;
                } else {
                    JsonObject responseObject = new JsonParser().parse(loginAttempt.getResponseString()).getAsJsonObject();
                    if (responseObject.get("authenticated").getAsBoolean()) {

                        JsonObject userObject = responseObject.get("user").getAsJsonObject();
                        JsonObject personObject = userObject.get("person").getAsJsonObject();

                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString("sessionid", responseObject.get("sessionId").getAsString());
                        editor.putString("creatorid", userObject.get("uuid").getAsString());
                        editor.putString("providerid", personObject.get("uuid").getAsString());
                        editor.putString("chwname", personObject.get("display").getAsString());
                        editor.commit();

                        String queryString = "?user=" + userObject.get("uuid").getAsString();
                        WebResponse responseProvider;

                        responseProvider = HelperMethods.getCommand(BASE_URL + "provider", queryString, SetupActivity.this, USERNAME, PASSWORD);

                        if (responseProvider != null && responseProvider.getResponseCode() == 200) {
                            String provider_uuid = "";

                            JSONArray resultsArray = null;

                            try {
                                JSONObject JSONResponse = new JSONObject(responseProvider.getResponseString());
                                resultsArray = JSONResponse.getJSONArray("results");

                                Log.i(TAG, "doInBackground: " + JSONResponse.toString());

                                if (resultsArray.length() != 0) {
                                    for (int i = 0; i < resultsArray.length(); i++) {
                                        JSONObject checking = resultsArray.getJSONObject(i);
                                        Log.i(TAG, "doInBackground: " + checking.getString("uuid"));
                                        provider_uuid = checking.getString("uuid");
                                        editor.putString("providerid", provider_uuid);
                                        editor.commit();
                                    }
                                    return 1;
                                }
                                return 201;
                            } catch (JSONException e) {
                                e.printStackTrace();
                                return 201;
                            }

                        }

                        return 201;


                    }
                }
            } catch (UnknownHostException e) {
                e.printStackTrace();
                return 201;
            } catch (IOException e) {
                e.printStackTrace();
                return 201;
            }
            return 201;
        }

        @Override
        protected void onPostExecute(Integer success) {
            mAuthTask = null;
//            showProgress(false);

            if (success == 1) {
                final Account account = new Account(USERNAME, "io.intelehealth.openmrs");
                manager.addAccountExplicitly(account, PASSWORD, null);

                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor editor = sharedPref.edit();

                editor.putString(SettingsActivity.KEY_PREF_LOCATION_NAME, LOCATION.getDisplay());
                editor.putString(SettingsActivity.KEY_PREF_LOCATION_UUID, LOCATION.getUuid());
                editor.putString(SettingsActivity.KEY_PREF_LOCATION_DESCRIPTION, LOCATION.getDescription());

                editor.putString(SettingsActivity.KEY_PREF_SERVER_URL_REST, BASE_URL);
                editor.putString(SettingsActivity.KEY_PREF_SERVER_URL_BASE, "http://" + CLEAN_URL + ":8080/openmrs");
                sessionManager.setBaseUrl(BASE_URL);
                editor.putString(SettingsActivity.KEY_PREF_SERVER_URL, CLEAN_URL);
                Log.d(TAG, BASE_URL);
                editor.apply();

                editor.putBoolean(SettingsActivity.KEY_PREF_SETUP_COMPLETE, true);
                editor.apply();

                OfflineLogin.getOfflineLogin().setUpOfflineLogin(USERNAME, PASSWORD);
                AdminPassword.getAdminPassword().setUp(ADMIN_PASSWORD);

                Parse.initialize(new Parse.Configuration.Builder(getApplicationContext())
                        .applicationId(HelperMethods.IMAGE_APP_ID)
                        .server("http://" + CLEAN_URL + ":1337/parse/")
                        .build()
                );
                Log.i(TAG, "onPostExecute: Parse init");
                Intent intent = new Intent(SetupActivity.this, HomeActivity.class);
                intent.putExtra("setup", true);
                if (r2.isChecked()) {
                    if (sharedPref.contains("licensekey")) {
                        startActivity(intent);
                        startJobDispatcherService(SetupActivity.this);
                        finish();
                    } else {
                        Toast.makeText(SetupActivity.this, "Please enter a valid license key", Toast.LENGTH_LONG).show();
                    }
                } else {
                    startActivity(intent);
                    finish();
                }


            } else if (success == 201) {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            } else if (success == 3) {
                mUrlField.setError(getString(R.string.url_invalid));
                mUrlField.requestFocus();
            }

            progress.dismiss();
        }
    }

    /**
     * Parse locations fetched through api and provide the appropriate dropdown.
     *
     * @param url string of url.
     */
    private void getLocationFromServer(String url) {
        try {
            ServiceGenerator.changeApiBaseUrl(url);
            RestApi apiService =
                    ServiceGenerator.createService(RestApi.class);
            Call<Results<Location>> call = apiService.getLocations(null);
            call.enqueue(new Callback<Results<Location>>() {
                @Override
                public void onResponse(Call<Results<Location>> call, Response<Results<Location>> response) {
                    if (response.code() == 200) {
                        Results<Location> locationList = response.body();
                        mLocations = locationList.getResults();
                        List<String> items = getLocationStringList(locationList.getResults());
                        LocationArrayAdapter adapter = new LocationArrayAdapter(SetupActivity.this, items);
                        mDropdownLocation.setAdapter(adapter);
                        isLocationFetched = true;
                    } else {
                        isLocationFetched = false;
                        Toast.makeText(SetupActivity.this, getString(R.string.error_location_not_fetched), Toast.LENGTH_SHORT).show();
                    }

                }

                @Override
                public void onFailure(Call<Results<Location>> call, Throwable t) {
                    isLocationFetched = false;
                    Toast.makeText(SetupActivity.this, getString(R.string.error_location_not_fetched), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "changeApiBaseUrl: " + e.getMessage());
            Log.e(TAG, "changeApiBaseUrl: " + e.getStackTrace());
            mUrlField.setError(getString(R.string.url_invalid));
        }
    }

    /**
     * Returns list of locations.
     *
     * @param locationList a list of type {@link Location}.
     * @return list of type string.
     * @see Location
     */
    private List<String> getLocationStringList(List<Location> locationList) {
        List<String> list = new ArrayList<String>();
        list.add(getString(R.string.login_location_select));
        for (int i = 0; i < locationList.size(); i++) {
            list.add(locationList.get(i).getDisplay());
        }
        return list;
    }

    public void onRadioClick(View v) {

        boolean checked = ((RadioButton) v).isChecked();
        switch (v.getId()) {
            case R.id.demoMindmap:
                if (checked) {
                    r2.setChecked(false);
                }
                break;

            case R.id.downloadMindmap:
                if (checked) {
                    r1.setChecked(false);

                    dialog = new AlertDialog.Builder(this);
                    LayoutInflater li = LayoutInflater.from(this);
                    View promptsView = li.inflate(R.layout.dialog_mindmap_cred, null);
                    dialog.setTitle(getString(R.string.enter_license_key))
                            .setView(promptsView)

                            .setPositiveButton(getString(R.string.button_ok), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Dialog d = (Dialog) dialog;

                                    EditText text = (EditText) d.findViewById(R.id.licensekey);
                                    key = text.getText().toString();
                                    //Toast.makeText(SetupActivity.this, "" + key, Toast.LENGTH_SHORT).show();
                                    if (keyVerified(key)) {
                                        // create a shared pref to store the key

                                        // SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("pref",MODE_PRIVATE);

                                        //DOWNLOAD MIND MAP FILE LIST
                                        //upnew getJSONFile().execute(null, "AllFiles", "TRUE");

                                        // UpdateProtocolsTask updateProtocolsTask = new UpdateProtocolsTask(SetupActivity.this);
                                        // updateProtocolsTask.execute(null, "AllFiles", "TRUE");
                                        DownloadProtocolsTask downloadProtocolsTask = new DownloadProtocolsTask(SetupActivity.this);
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
                break;
        }
    }

    private boolean keyVerified(String key) {
        //TODO: Verify License Key
        return true;
    }

    /**
     * A method of FirebaseJobDispatcher Library.
     * It schedules background jobs for android app.
     *
     * @param context Current context
     * @return returns void
     */
    private void startJobDispatcherService(Context context) {
        Driver driver = new GooglePlayDriver(context);
        FirebaseJobDispatcher firebaseJobDispatcher = new FirebaseJobDispatcher(driver);

        Job uploadCronJob = firebaseJobDispatcher.newJobBuilder()
                .setService(JobDispatchService.class)
                .setTag("Delayed Job Queue")
                .setLifetime(Lifetime.FOREVER)
                .setRecurring(true)
                .setTrigger(Trigger.executionWindow(
                        1770, 1830
                ))

                .setReplaceCurrent(true)
                .setConstraints(
                        // only run on any network
                        Constraint.ON_ANY_NETWORK)
                .build();

        firebaseJobDispatcher.mustSchedule(uploadCronJob);
    }

}