package io.intelehealth.client.activities.identification_activity;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import io.intelehealth.client.R;
import io.intelehealth.client.activities.camera_activity.CameraActivity;
import io.intelehealth.client.activities.patient_detail_activity.PatientDetailActivity;

import io.intelehealth.client.database.LocalRecordsDatabaseHelper;
import io.intelehealth.client.node.Node;
import io.intelehealth.client.objects.Patient;
import io.intelehealth.client.utilities.EditTextUtils;
import io.intelehealth.client.utilities.HelperMethods;
import io.intelehealth.client.utilities.Logger;

import static io.intelehealth.client.utilities.HelperMethods.LOG_TAG;
import static io.intelehealth.client.utilities.HelperMethods.REQUEST_CAMERA;
import static io.intelehealth.client.utilities.HelperMethods.REQUEST_READ_EXTERNAL;


/**
 * Created by Amal Afroz Alam on 3/25/16.
 */

public class IdentificationActivity extends AppCompatActivity {
    String TAG = IdentificationActivity.class.getSimpleName();

    EditText mFirstName;
    EditText mMiddleName;
    EditText mLastName;
    EditText mDOB;
    EditText mPhoneNum;
    EditText mAge;
    AlertDialog.Builder mAgePicker;
    EditText mAddress1;
    EditText mAddress2;
    AutoCompleteTextView mCity;
    EditText mPostal;
    RadioButton mGenderM;
    RadioButton mGenderF;
    String mGender;
    EditText mRelationship;
    EditText mOccupation;
    DatePickerDialog mDOBPicker;
    EditText countryText;
    EditText stateText;
    Spinner mCountry;
    Spinner mState;
    Integer mDOBYear;
    Integer mDOBMonth;
    Integer mDOBDay;
    Integer mAgeYears = 0;
    Integer mAgeMonths = 0;
    EditText casteText;
    EditText economicText;
    EditText educationText;
    TextInputLayout casteLayout;
    TextInputLayout economicLayout;
    TextInputLayout educationLayout;

    LinearLayout countryStateLayout;

    Spinner mCaste;
    Spinner mEducation;
    Spinner mEconomicStatus;

    String mPhoto;
    Patient patient = new Patient();
    Patient patient1 = new Patient();
    Integer patientID;
    Integer patientID_edit = -1;
    Node information;
    Calendar today = Calendar.getInstance();
    Calendar dob = Calendar.getInstance();

    LocalRecordsDatabaseHelper mDbHelper;
    String visitID;
    public static String state1;
    public static String country1;
    ImageView mImageView;
    String mCurrentPhotoPath;
    SharedPreferences.Editor e;
    boolean hasLicense = false;
    String mFileName = "config.json";
    private ArrayAdapter<CharSequence> educationAdapter;
    private ArrayAdapter<CharSequence> economicStatusAdapter;

    // Boolean isDateChanged = false; //prajw
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_identification);
        setTitle(R.string.title_activity_identification);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        //Initialize the local database to store patient information
        mDbHelper = new LocalRecordsDatabaseHelper(this);

        Intent intent = this.getIntent(); // The intent was passed to the activity
        if (intent != null) {
            if (intent.hasExtra("pid")) {
                this.setTitle("Update Patient");
                patientID_edit = intent.getIntExtra("pid", -1);
                patient1.setId(patientID_edit);
                setscreen(String.valueOf(patientID_edit));
            }
        }

        mFirstName = (EditText) findViewById(R.id.identification_first_name);
        mMiddleName = (EditText) findViewById(R.id.identification_middle_name);
        mLastName = (EditText) findViewById(R.id.identification_last_name);
        mDOB = (EditText) findViewById(R.id.identification_birth_date_text_view);
        mPhoneNum = (EditText) findViewById(R.id.identification_phone_number);
        mAge = (EditText) findViewById(R.id.identification_age);
        mAddress1 = (EditText) findViewById(R.id.identification_address1);
        mAddress2 = (EditText) findViewById(R.id.identification_address2);
        mCity = (AutoCompleteTextView) findViewById(R.id.identification_city);
        stateText = (EditText) findViewById(R.id.identification_state);
        mState = (Spinner) findViewById(R.id.spinner_state);
        mPostal = (EditText) findViewById(R.id.identification_postal_code);
        countryText = (EditText) findViewById(R.id.identification_country);
        mCountry = (Spinner) findViewById(R.id.spinner_country);
        mGenderM = (RadioButton) findViewById(R.id.identification_gender_male);
        mGenderF = (RadioButton) findViewById(R.id.identification_gender_female);
        mRelationship = (EditText) findViewById(R.id.identification_relationship);
        mOccupation = (EditText) findViewById(R.id.identification_occupation);
        mCaste = (Spinner) findViewById(R.id.spinner_caste);
        mEducation = (Spinner) findViewById(R.id.spinner_education);
        mEconomicStatus = (Spinner) findViewById(R.id.spinner_economic_status);
        casteText = (EditText) findViewById(R.id.identification_caste);
        educationText = (EditText) findViewById(R.id.identification_education);
        economicText = (EditText) findViewById(R.id.identification_econiomic_status);

        casteLayout = (TextInputLayout) findViewById(R.id.identification_txtlcaste);
        economicLayout = (TextInputLayout) findViewById(R.id.identification_txtleconomic);
        educationLayout = (TextInputLayout) findViewById(R.id.identification_txtleducation);
        countryStateLayout = (LinearLayout) findViewById(R.id.identification_llcountry_state);


        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if (sharedPreferences.contains("licensekey"))
            hasLicense = true;


        //Check for license key and load the correct config file
        //Issue #683
        try {
            JSONObject obj = null;
            if (hasLicense) {
                obj = new JSONObject(HelperMethods.readFileRoot(mFileName, this)); //Load the config file

            } else {
                obj = new JSONObject(String.valueOf(HelperMethods.encodeJSON(this, mFileName)));

            }

            //Display the fields on the Add Patient screen as per the config file
            if (obj.getBoolean("mFirstName")) {
                mFirstName.setVisibility(View.VISIBLE);
            } else {
                mFirstName.setVisibility(View.GONE);
            }

            if (obj.getBoolean("mMiddleName")) {
                mMiddleName.setVisibility(View.VISIBLE);
            } else {
                mMiddleName.setVisibility(View.GONE);
            }

            if (obj.getBoolean("mLastName")) {
                mLastName.setVisibility(View.VISIBLE);
            } else {
                mLastName.setVisibility(View.GONE);
            }

            if (obj.getBoolean("mDOB")) {
                mDOB.setVisibility(View.VISIBLE);
            } else {
                mDOB.setVisibility(View.GONE);
            }
            if (obj.getBoolean("mPhoneNum")) {
                mPhoneNum.setVisibility(View.VISIBLE);
            } else {
                mPhoneNum.setVisibility(View.GONE);
            }
            if (obj.getBoolean("mAge")) {
                mAge.setVisibility(View.VISIBLE);
            } else {
                mAge.setVisibility(View.GONE);
            }
            if (obj.getBoolean("mAddress1")) {
                mAddress1.setVisibility(View.VISIBLE);
            } else {
                mAddress1.setVisibility(View.GONE);
            }
            if (obj.getBoolean("mAddress2")) {
                mAddress2.setVisibility(View.VISIBLE);
            } else {
                mAddress2.setVisibility(View.GONE);
            }
            if (obj.getBoolean("mCity")) {
                mCity.setVisibility(View.VISIBLE);
            } else {
                mCity.setVisibility(View.GONE);
            }

            if (obj.getBoolean("countryStateLayout")) {
                countryStateLayout.setVisibility(View.VISIBLE);
            } else {
                countryStateLayout.setVisibility(View.GONE);
            }
            if (obj.getBoolean("mPostal")) {
                mPostal.setVisibility(View.VISIBLE);
            } else {
                mPostal.setVisibility(View.GONE);
            }

            if (obj.getBoolean("mGenderM")) {
                mGenderM.setVisibility(View.VISIBLE);
            } else {
                mGenderM.setVisibility(View.GONE);
            }
            if (obj.getBoolean("mGenderF")) {
                mGenderF.setVisibility(View.VISIBLE);
            } else {
                mGenderF.setVisibility(View.GONE);
            }
            if (obj.getBoolean("mRelationship")) {
                mRelationship.setVisibility(View.VISIBLE);
            } else {
                mRelationship.setVisibility(View.GONE);
            }
            if (obj.getBoolean("mOccupation")) {
                mOccupation.setVisibility(View.VISIBLE);
            } else {
                mOccupation.setVisibility(View.GONE);
            }
            if (obj.getBoolean("casteLayout")) {
                casteLayout.setVisibility(View.VISIBLE);
            } else {
                casteLayout.setVisibility(View.GONE);
            }
            if (obj.getBoolean("educationLayout")) {
                educationLayout.setVisibility(View.VISIBLE);
            } else {
                educationLayout.setVisibility(View.GONE);
            }
            if (obj.getBoolean("economicLayout")) {
                economicLayout.setVisibility(View.VISIBLE);
            } else {
                economicLayout.setVisibility(View.GONE);
            }
            country1 = obj.getString("mCountry");

            if (country1.equalsIgnoreCase("India")) {
                EditTextUtils.setEditTextMaxLength(10, mPhoneNum);
            } else if (country1.equalsIgnoreCase("Philippines")) {
                EditTextUtils.setEditTextMaxLength(11, mPhoneNum);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }


         /*
        The patient's picture will be taken here and then stored using the method below.
        This picture will then be displayed right after, allowing the user to verify the picture was well taken.
        */
        mImageView = (ImageView) findViewById(R.id.imageview_id_picture);

        //setting the fields when user clicks edit details
        mFirstName.setText(patient1.getFirstName());
        mMiddleName.setText(patient1.getMiddleName());
        mLastName.setText(patient1.getLastName());
        mDOB.setText(patient1.getDateOfBirth());
        mPhoneNum.setText(patient1.getPhoneNumber());
        mAddress1.setText(patient1.getAddress1());
        mAddress2.setText(patient1.getAddress2());
        mCity.setText(patient1.getCityVillage());
        mPostal.setText(patient1.getPostalCode());
        mRelationship.setText(patient1.getSdw());
        mOccupation.setText(patient1.getOccupation());


        if (patient1.getPatientPhoto() != null && !patient1.getPatientPhoto().trim().isEmpty())
            mImageView.setImageBitmap(BitmapFactory.decodeFile(patient1.getPatientPhoto()));


        Resources res = getResources();
        ArrayAdapter<CharSequence> countryAdapter = ArrayAdapter.createFromResource(this,
                R.array.countries, android.R.layout.simple_spinner_item);
        countryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mCountry.setAdapter(countryAdapter);

        ArrayAdapter<CharSequence> casteAdapter = ArrayAdapter.createFromResource(this,
                R.array.caste, android.R.layout.simple_spinner_item);
        countryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mCaste.setAdapter(casteAdapter);
try {
    String economicLanguage = "economic_" + Locale.getDefault().getLanguage();
    int economics = res.getIdentifier(economicLanguage, "array", getApplicationContext().getPackageName());
    if(economics!=0) {
        economicStatusAdapter = ArrayAdapter.createFromResource(this,
                economics, android.R.layout.simple_spinner_item);
    }
    countryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    mEconomicStatus.setAdapter(economicStatusAdapter);
}catch(Exception e){
    Logger.logE("Identification","#648",e);
}
try {
    String educationLanguage = "education_" + Locale.getDefault().getLanguage();
    int educations = res.getIdentifier(educationLanguage, "array", getApplicationContext().getPackageName());
   if(educations!=0) {
       educationAdapter = ArrayAdapter.createFromResource(this,
               educations, android.R.layout.simple_spinner_item);

   }countryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    mEducation.setAdapter(educationAdapter);
}catch(Exception e){
    Logger.logE("Identification","#648",e);
}
        // generate patientid only if there is no intent for Identification activity

        if (patientID_edit == -1) {
            generateID();
        }

        // setting radio button automatically according to the databse when user clicks edit details
        if (patientID_edit != -1) {
            if (patient1.getGender().equals("M")) {
                mGenderM.setChecked(true);
                if (mGenderF.isChecked())
                    mGenderF.setChecked(false);
                Log.v(TAG, "yes");
            } else {
                mGenderF.setChecked(true);
                if (mGenderM.isChecked())
                    mGenderM.setChecked(false);
                Log.v(TAG, "yes");
            }
        }
        if (mGenderM.isChecked()) {
            mGender = "M";
        } else {
            mGender = "F";
        }


        if (patientID_edit != -1) {
            // setting country according database
            mCountry.setSelection(countryAdapter.getPosition(String.valueOf(patient1.getCountry())));
            if (patient1.getEducation_level().equals(getString(R.string.not_provided)))
                mEducation.setSelection(0);
            else
                mEducation.setSelection(educationAdapter.getPosition(String.valueOf(patient1.getEducation_level())));
            if (patient1.getEconomic_status().equals(getString(R.string.not_provided)))
                mEconomicStatus.setSelection(0);
            else
                mEconomicStatus.setSelection(economicStatusAdapter.getPosition(String.valueOf(patient1.getEconomic_status())));
            if (patient1.getCaste().equals(getString(R.string.not_provided)))
                mCaste.setSelection(0);
            else
                mCaste.setSelection(casteAdapter.getPosition(String.valueOf(patient1.getCaste())));
        } else {
            mCountry.setSelection(countryAdapter.getPosition(country1));
        }

        ArrayAdapter<CharSequence> stateAdapter = ArrayAdapter.createFromResource(this, R.array.state_error, android.R.layout.simple_spinner_item);
        stateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mState.setAdapter(stateAdapter);

        mState.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String state = parent.getItemAtPosition(position).toString();
                if (state.matches("Odisha")) {
                    //Creating the instance of ArrayAdapter containing list of fruit names
                    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(IdentificationActivity.this,
                            R.array.odisha_villages, android.R.layout.simple_spinner_item);
                    mCity.setThreshold(1);//will start working from first character
                    mCity.setAdapter(adapter);//setting the adapter data into the AutoCompleteTextView
                } else if (state.matches("Bukidnon")) {
                    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(IdentificationActivity.this,
                            R.array.bukidnon_villages, android.R.layout.simple_spinner_item);
                    mCity.setThreshold(1);//will start working from first character
                    mCity.setAdapter(adapter);//setting the adapter data into the AutoCompleteTextView
                } else {
                    mCity.setAdapter(null);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mCountry.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i != 0) {
                    String country = adapterView.getItemAtPosition(i).toString();

                    if (country.matches("India")) {
                        ArrayAdapter<CharSequence> stateAdapter = ArrayAdapter.createFromResource(IdentificationActivity.this,
                                R.array.states_india, android.R.layout.simple_spinner_item);
                        stateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        mState.setAdapter(stateAdapter);
                        // setting state according database when user clicks edit details

                        if (patientID_edit != -1) {
                            mState.setSelection(stateAdapter.getPosition(String.valueOf(patient1.getStateProvince())));
                        } else {
                            mState.setSelection(stateAdapter.getPosition("Odisha"));
                        }

                    } else if (country.matches("United States")) {
                        ArrayAdapter<CharSequence> stateAdapter = ArrayAdapter.createFromResource(IdentificationActivity.this,
                                R.array.states_us, android.R.layout.simple_spinner_item);
                        stateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        mState.setAdapter(stateAdapter);

                        if (patientID_edit != -1) {

                            mState.setSelection(stateAdapter.getPosition(String.valueOf(patient1.getStateProvince())));
                        }
                    } else if (country.matches("Philippines")) {
                        ArrayAdapter<CharSequence> stateAdapter = ArrayAdapter.createFromResource(IdentificationActivity.this,
                                R.array.states_philippines, android.R.layout.simple_spinner_item);
                        stateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        mState.setAdapter(stateAdapter);

                        if (patientID_edit != -1) {
                            mState.setSelection(stateAdapter.getPosition(String.valueOf(patient1.getStateProvince())));
                        } else {
                            mState.setSelection(stateAdapter.getPosition("Bukidnon"));
                        }
                    }
                } else {
                    ArrayAdapter<CharSequence> stateAdapter = ArrayAdapter.createFromResource(IdentificationActivity.this,
                            R.array.state_error, android.R.layout.simple_spinner_item);
                    stateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    mState.setAdapter(stateAdapter);
                }


            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        //Check to see if the permission was given to take pictures.
        /*if (ContextCompat.checkSelfPermission(IdentificationActivity.this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(IdentificationActivity.this,
                    new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA);
        }*/

        //When either button is clicked, that information needs to be stored.
        mGenderF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRadioButtonClicked(v);
            }
        });

        mGenderM.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRadioButtonClicked(v);
            }
        });


        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //String[] results = HelperMethods.startImageCapture(IdentificationActivity.this,
                //        IdentificationActivity.this);
                //if (results != null) {
                //    mPhoto = results[0];
                //    mCurrentPhotoPath = results[1];
                //}
                File filePath = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES) + File.separator +
                        "Patient_Images" + File.separator + patientID);
                if (!filePath.exists()) {
                    filePath.mkdir();
                }
                Intent cameraIntent = new Intent(IdentificationActivity.this, CameraActivity.class);

                // cameraIntent.putExtra(CameraActivity.SHOW_DIALOG_MESSAGE, getString(R.string.camera_dialog_default));
                cameraIntent.putExtra(CameraActivity.SET_IMAGE_NAME, patientID);
                cameraIntent.putExtra(CameraActivity.SET_IMAGE_PATH, filePath);
                startActivityForResult(cameraIntent, CameraActivity.TAKE_IMAGE);
            }
        });
        mDOBYear = today.get(Calendar.YEAR);
        mDOBMonth = today.get(Calendar.MONTH);
        mDOBDay = today.get(Calendar.DAY_OF_MONTH);
        //DOB is set using an AlertDialog
        mDOBPicker = new DatePickerDialog(this, android.R.style.Theme_Holo_Light_Dialog_NoActionBar, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                //Set the DOB calendar to the date selected by the user
                dob.set(year, monthOfYear, dayOfMonth);
                mDOB.setError(null);
                mAge.setError(null);
                //Set Maximum date to current date because even after bday is less than current date it goes to check date is set after today
                mDOBPicker.getDatePicker().setMaxDate(System.currentTimeMillis() - 1000);

                //Formatted so that it can be read the way the user sets
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                dob.set(year, monthOfYear, dayOfMonth);
                String dobString = simpleDateFormat.format(dob.getTime());
                mDOB.setText(dobString);

                mAgeYears = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);
                mAgeMonths = today.get(Calendar.MONTH) - dob.get(Calendar.MONTH);


                if (mAgeMonths < 0) {
                    mAgeMonths = mAgeMonths + 12;
                    mAgeYears = mAgeYears - 1;
                }

                if (mAgeMonths < 0 || mAgeYears < 0 || dob.after(today)) {
                    mDOB.setError(getString(R.string.identification_screen_error_dob));
                    mAge.setError(getString(R.string.identification_screen_error_age));
                    return;
                }

                mDOBYear = year;
                mDOBMonth = monthOfYear;
                mDOBDay = dayOfMonth;

                String ageString = String.valueOf(mAgeYears) + getString(R.string.identification_screen_text_years)
                        +" , "+ String.valueOf(mAgeMonths) + getString(R.string.identification_screen_text_months);
                mAge.setText(ageString);
            }
        }, mDOBYear, mDOBMonth, mDOBDay);

        //DOB Picker is shown when clicked
        mDOB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDOBPicker.show();
            }
        });

        //if patient update then age will be set
        if (patientID_edit != -1) {
            int age = HelperMethods.getAge(patient1.getDateOfBirth());
            mDOB.setText(patient1.getDateOfBirth());
            int month = HelperMethods.getMonth(patient1.getDateOfBirth());
            mAge.setText(String.valueOf(age) + getString(R.string.identification_screen_text_years) + String.valueOf(month) + getString(R.string.identification_screen_text_months));
        }
        mAge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAgePicker = new AlertDialog.Builder(IdentificationActivity.this, R.style.AlertDialogStyle);
                mAgePicker.setTitle(R.string.identification_screen_prompt_age);
                final LayoutInflater inflater = getLayoutInflater();
                View convertView = inflater.inflate(R.layout.dialog_2_numbers_picker, null);
                mAgePicker.setView(convertView);
                final NumberPicker yearPicker = (NumberPicker) convertView.findViewById(R.id.dialog_2_numbers_quantity);
                final NumberPicker monthPicker = (NumberPicker) convertView.findViewById(R.id.dialog_2_numbers_unit);
                final TextView middleText = (TextView) convertView.findViewById(R.id.dialog_2_numbers_text);
                final TextView endText = convertView.findViewById(R.id.dialog_2_numbers_text_2);
                middleText.setText(getString(R.string.identification_screen_picker_years));
                endText.setText(getString(R.string.identification_screen_picker_months));
                yearPicker.setMinValue(0);
                yearPicker.setMaxValue(100);
                monthPicker.setMinValue(0);
                monthPicker.setMaxValue(12);
                if (mAgeYears > 0) {
                    yearPicker.setValue(mAgeYears);
                }
                if (mAgeMonths > 0) {
                    monthPicker.setValue(mAgeMonths);
                }

                mAgePicker.setPositiveButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        yearPicker.setValue(yearPicker.getValue());
                        monthPicker.setValue(monthPicker.getValue());
                        String ageString = String.valueOf(yearPicker.getValue()) + getString(R.string.identification_screen_text_years) + monthPicker.getValue() + getString(R.string.identification_screen_text_months);
                        mAge.setText(ageString);


                        Calendar calendar = Calendar.getInstance();
                        int curYear = calendar.get(Calendar.YEAR);
                        int birthYear = curYear - yearPicker.getValue();
                        int curMonth = calendar.get(Calendar.MONTH);
                        int birthMonth = curMonth - monthPicker.getValue();
                        mDOBYear = birthYear;
                        mDOBMonth = birthMonth;
                        mDOBDay = 1;

                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                        dob.set(mDOBYear, mDOBMonth, mDOBDay);
                        String dobString = simpleDateFormat.format(dob.getTime());
                        mDOB.setText(dobString);
                        mDOBPicker.updateDate(mDOBYear, mDOBMonth, mDOBDay);
                        dialog.dismiss();
                    }
                });
                mAgePicker.setNegativeButton(R.string.generic_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                mAgePicker.show();
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        assert fab != null;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createNewPatient();
            }
        });


    }

    // This method is for setting the screen with existing values in database whenn user clicks edit details
    private void setscreen(String str) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        String patientSelection = "_id=?";
        String[] patientArgs = {str};
        String[] patientColumns = {"first_name", "middle_name", "last_name",
                "date_of_birth", "address1", "address2", "city_village", "state_province",
                "postal_code", "country", "phone_number", "gender", "sdw", "occupation", "patient_photo",
                "economic_status", "education_status", "caste"};
        Cursor idCursor = db.query("patient", patientColumns, patientSelection, patientArgs, null, null, null);
        if (idCursor.moveToFirst()) {
            do {
                patient1.setFirstName(idCursor.getString(idCursor.getColumnIndexOrThrow("first_name")));
                patient1.setMiddleName(idCursor.getString(idCursor.getColumnIndexOrThrow("middle_name")));
                patient1.setLastName(idCursor.getString(idCursor.getColumnIndexOrThrow("last_name")));
                patient1.setDateOfBirth(idCursor.getString(idCursor.getColumnIndexOrThrow("date_of_birth")));
                patient1.setAddress1(idCursor.getString(idCursor.getColumnIndexOrThrow("address1")));
                patient1.setAddress2(idCursor.getString(idCursor.getColumnIndexOrThrow("address2")));
                patient1.setCityVillage(idCursor.getString(idCursor.getColumnIndexOrThrow("city_village")));
                patient1.setStateProvince(idCursor.getString(idCursor.getColumnIndexOrThrow("state_province")));
                patient1.setPostalCode(idCursor.getString(idCursor.getColumnIndexOrThrow("postal_code")));
                patient1.setCountry(idCursor.getString(idCursor.getColumnIndexOrThrow("country")));
                patient1.setPhoneNumber(idCursor.getString(idCursor.getColumnIndexOrThrow("phone_number")));
                patient1.setGender(idCursor.getString(idCursor.getColumnIndexOrThrow("gender")));
                patient1.setSdw(idCursor.getString(idCursor.getColumnIndexOrThrow("sdw")));
                patient1.setOccupation(idCursor.getString(idCursor.getColumnIndexOrThrow("occupation")));
                patient1.setPatientPhoto(idCursor.getString(idCursor.getColumnIndexOrThrow("patient_photo")));
                patient1.setEconomic_status(idCursor.getString(idCursor.getColumnIndexOrThrow("economic_status")));
                patient1.setEducation_level(idCursor.getString(idCursor.getColumnIndexOrThrow("education_status")));
                patient1.setCaste(idCursor.getString(idCursor.getColumnIndexOrThrow("caste")));

            } while (idCursor.moveToNext());
            idCursor.close();
        }

    }


    public void onRadioButtonClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();
        switch (view.getId()) {
            case R.id.identification_gender_male:
                if (checked)
                    mGender = "M";
                Log.v(TAG, "gender:" + mGender);
                break;
            case R.id.identification_gender_female:
                if (checked)
                    mGender = "F";
                Log.v(TAG, "gender:" + mGender);
                break;
        }
    }

    /**
     * This method is primarily for data validation.
     * First, the screen is checked to see if a gender was selected for the patient.
     * If no gender selected, you get a dialog box telling you to do so.
     * <p/>
     * Next, if the DOB is after today's date, then you are asked to go back and correct it.
     * <p/>
     * Finally, all the text boxes are checked. With the text boxes, each EditText has a tag written in the XML.
     * If an EditText box does not have a tag, then it is assumed to be required.
     * Only optional fields have an "optional" tag on them in the XML.
     * If any of the EditText validations fail, that box is brought to focus.
     * <p/>
     * Finally, after everything is checked and made sure to be correct, a Patient object is created.
     * The object is filled with all the data that was provided, and then an Async Task is created to insert the information into the database.
     */
    public void createNewPatient() {

        boolean cancel = false;
        View focusView = null;


        if (dob.after(today)) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(IdentificationActivity.this);
            alertDialogBuilder.setMessage(R.string.identification_screen_dialog_error_dob);
            //alertDialogBuilder.setMessage(getString(R.string.identification_dialog_date_error));
            alertDialogBuilder.setNeutralButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            AlertDialog alertDialog = alertDialogBuilder.create();

            mDOBPicker.show();
            alertDialog.show();
            return;
        }


//prajwal
     /* if (mDOBYear.toString().equals("") && mDOBMonth.toString().equals("") && mDOBDay.toString().equals("")) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(IdentificationActivity.this);
            alertDialogBuilder.setMessage("Enter year nd month prajwal");
            alertDialogBuilder.setNeutralButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            AlertDialog alertDialog = alertDialogBuilder.create();

            mDOBPicker.show();
            alertDialog.show();
            return;
        }
*/

//praj


        ArrayList<EditText> values = new ArrayList<>();
        values.add(mFirstName);
        values.add(mMiddleName);
        values.add(mLastName);
        values.add(mDOB);
        values.add(mPhoneNum);
        values.add(mAddress1);
        values.add(mAddress2);
        values.add(mCity);
        values.add(mPostal);
        values.add(mRelationship);
        values.add(mOccupation);

     /* for (int i = 0; i < values.size(); i++) {
            EditText et = values.get(i);
            if (TextUtils.isEmpty(et.getText().toString()) && et.getTag() == null) {
                et.setError("Prajwal here");
                focusView = et;
                cancel = true;
                return;
            } else {
                et.setError(null);
            }
        }*/

        if (!mGenderF.isChecked() && !mGenderM.isChecked()) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(IdentificationActivity.this);
            alertDialogBuilder.setMessage(R.string.identification_screen_dialog_error_gender);
            alertDialogBuilder.setNeutralButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
            return;
        }

        //prajw


        // prajw


        //prajwal
        if (!mFirstName.getText().toString().equals("") && !mLastName.getText().toString().equals("")
                && !mCity.getText().toString().equals("") && !countryText.getText().toString().equals("") &&
                !stateText.getText().toString().equals("") && !mDOB.getText().toString().equals("") && !mAge.getText().toString().equals("")) {

            Log.v(TAG, "Result");
            //meera
            //Toast.makeText(IdentificationActivity.this, "Patient Registered", Toast.LENGTH_SHORT).show();

        } else {
            if (mFirstName.getText().toString().equals("")) {
                mFirstName.setError(getString(R.string.error_field_required));
            }

            if (mLastName.getText().toString().equals("")) {
                mLastName.setError(getString(R.string.error_field_required));
            }

            if (mDOB.getText().toString().equals("")) {
                mDOB.setError(getString(R.string.error_field_required));
            }

            if (mAge.getText().toString().equals("")) {
                mAge.setError(getString(R.string.error_field_required));
            }

         /*   if(mOccupation.getText().toString().equals(""))
            {
                mOccupation.setError(getString(R.string.error_field_required));
            }*/

           /* if(casteText.getText().toString().equals(""))
            {
                casteText.setError(getString(R.string.error_field_required));
            }

            if(economicText.getText().toString().equals(""))
            {
                economicText.setError(getString(R.string.error_field_required));
            }

            if(educationText.getText().toString().equals(""))
            {
                educationText.setError(getString(R.string.error_field_required));
            }*/

            if (mCity.getText().toString().equals("")) {
                mCity.setError(getString(R.string.error_field_required));
            }


            Toast.makeText(IdentificationActivity.this, "Please Enter Required Fields", Toast.LENGTH_LONG).show();
            return;
        }

        //end prajwal


        if (mCountry.getSelectedItemPosition() == 0) {
            countryText.setError(getString(R.string.error_field_required));
            focusView = countryText;
            cancel = true;
            return;
        } else {
            countryText.setError(null);
        }


        if (mState.getSelectedItemPosition() == 0) {
            stateText.setError(getString(R.string.error_field_required));
            focusView = stateText;
            cancel = true;
            return;
        } else {
            stateText.setError(null);
        }

        if (cancel) {
            focusView.requestFocus();
        } else {

            try {
                patient1.setFirstName(mFirstName.getText().toString());
                patient.setFirstName(mFirstName.getText().toString());
                if (TextUtils.isEmpty(mMiddleName.getText().toString())) {
                    patient.setMiddleName("");
                    patient1.setMiddleName("");
                } else {
                    patient.setMiddleName(mMiddleName.getText().toString());
                    patient1.setMiddleName(mMiddleName.getText().toString());
                }

                if (TextUtils.isEmpty(mLastName.getText().toString())) {
                    patient.setLastName("");
                    patient1.setLastName("");
                } else {
                    patient.setLastName(mLastName.getText().toString());
                    patient1.setLastName(mLastName.getText().toString());
                }
                if (TextUtils.isEmpty(mDOB.getText().toString())) {
                    patient.setDateOfBirth("");
                    patient1.setDateOfBirth("");
                } else {
                    patient.setDateOfBirth(mDOB.getText().toString());
                    patient1.setDateOfBirth(mDOB.getText().toString());
                }
                if (TextUtils.isEmpty(mPhoneNum.getText().toString())) {
                    patient.setPhoneNumber("");
                    patient1.setPhoneNumber("");
                } else {
                    patient.setPhoneNumber(mPhoneNum.getText().toString());
                    patient1.setPhoneNumber(mPhoneNum.getText().toString());
                }
                if (TextUtils.isEmpty(mAddress1.getText().toString())) {
                    patient.setAddress1("");
                    patient1.setAddress1("");
                } else {
                    patient.setAddress1(mAddress1.getText().toString());
                    patient1.setAddress1(mAddress1.getText().toString());
                }
                if (TextUtils.isEmpty(mAddress2.getText().toString())) {
                    patient.setAddress2("");
                    patient1.setAddress2("");
                } else {
                    patient.setAddress2(mAddress2.getText().toString());
                    patient1.setAddress2(mAddress2.getText().toString());
                }
                if (TextUtils.isEmpty(mCity.getText().toString())) {
                    patient.setCityVillage("");
                    patient1.setCityVillage("");
                } else {
                    patient.setCityVillage(mCity.getText().toString());
                    patient1.setCityVillage(mCity.getText().toString());
                }
//                if (TextUtils.isEmpty(mState.getText().toString())) {
//                    currentPatient.setStateProvince("");
//                } else {
//                    currentPatient.setStateProvince(mState.getText().toString());
//                }
                if (TextUtils.isEmpty(mPostal.getText().toString())) {
                    patient.setPostalCode("");
                    patient1.setPostalCode("");
                } else {
                    patient.setPostalCode(mPostal.getText().toString());
                    patient1.setPostalCode(mPostal.getText().toString());
                }
                if (TextUtils.isEmpty(mRelationship.getText().toString())) {
                    patient.setSdw("");
                    patient1.setSdw("");
                } else {
                    patient.setSdw(mRelationship.getText().toString());
                    patient1.setSdw(mRelationship.getText().toString());
                }
                if (TextUtils.isEmpty(mOccupation.getText().toString())) {
                    patient.setOccupation("");
                    patient1.setOccupation("");
                } else {
                    patient.setOccupation(mOccupation.getText().toString());
                    patient1.setOccupation(mOccupation.getText().toString());
                }

                //currentPatient.setCountry(mCountry.getText().toString());
                patient.setGender(mGender);
                patient1.setGender(mGender);
                patient.setCountry(mCountry.getSelectedItem().toString());
                patient1.setCountry(mCountry.getSelectedItem().toString());


                if (mCaste.getSelectedItemPosition() == 0) {
                    patient.setCaste(getString(R.string.not_provided));
                    patient1.setCaste(getString(R.string.not_provided));
                } else {
                    patient.setCaste(mCaste.getSelectedItem().toString());
                    patient1.setCaste(mCaste.getSelectedItem().toString());
                }

                if (mEconomicStatus.getSelectedItemPosition() == 0) {
                    patient.setEconomic_status(getString(R.string.not_provided));
                    patient1.setEconomic_status(getString(R.string.not_provided));
                } else {
                    patient.setEconomic_status(mEconomicStatus.getSelectedItem().toString());
                    patient1.setEconomic_status(mEconomicStatus.getSelectedItem().toString());
                }

                if (mEducation.getSelectedItemPosition() == 0) {
                    patient.setEducation_level(getString(R.string.not_provided));
                    patient1.setEducation_level(getString(R.string.not_provided));
                } else {
                    patient.setEducation_level(mEducation.getSelectedItem().toString());
                    patient1.setEducation_level(mEducation.getSelectedItem().toString());
                }

                patient.setStateProvince(mState.getSelectedItem().toString());
                patient1.setStateProvince(mState.getSelectedItem().toString());
                Log.v(TAG, "" + mState.getSelectedItem());
            } catch (NullPointerException e) {
                Snackbar.make(findViewById(R.id.cl_table), R.string.identification_screen_error_data_fields, Snackbar.LENGTH_SHORT);
            }
            if (patientID_edit != -1) {
                new UpdatePatientTable(patient1).execute();
            } else {
                new InsertPatientTable(patient).execute();
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.v(TAG, "Result Received");
        if (requestCode == CameraActivity.TAKE_IMAGE) {
            Log.v(TAG, "Request Code " + CameraActivity.TAKE_IMAGE);
            if (resultCode == RESULT_OK) {
                Log.i(TAG, "Result OK");
                mCurrentPhotoPath = data.getStringExtra("RESULT");
                Log.v("IdentificationActivity", mCurrentPhotoPath);
                Glide.with(this)
                        .load(new File(mCurrentPhotoPath))
                        .thumbnail(0.25f)
                        .into(mImageView);
            }
        }
    }

    public class InsertPatientTable extends AsyncTask<Void, Void, Boolean>
            implements DialogInterface.OnCancelListener {

        SQLiteDatabase db4 = mDbHelper.getWritableDatabase();

        InsertPatientTable(Patient currentPatient) {
            patient = currentPatient;
        }

        ContentValues patientEntries = new ContentValues();
        ContentValues visitData = new ContentValues();

        public void gatherEntries() {
            patientEntries.put("first_name", patient.getFirstName());
            patientEntries.put("middle_name", patient.getMiddleName());
            patientEntries.put("last_name", patient.getLastName());
            patientEntries.put("date_of_birth", patient.getDateOfBirth());
            patientEntries.put("phone_number", patient.getPhoneNumber());
            patientEntries.put("address1", patient.getAddress1());
            patientEntries.put("address2", patient.getAddress2());
            patientEntries.put("city_village", patient.getCityVillage());
            patientEntries.put("state_province", patient.getStateProvince());
            patientEntries.put("postal_code", patient.getPostalCode());
            patientEntries.put("country", patient.getCountry());
            patientEntries.put("gender", patient.getGender());
            patientEntries.put("sdw", patient.getSdw());
            patientEntries.put("occupation", patient.getOccupation());
            patientEntries.put("patient_photo", mCurrentPhotoPath);
            patientEntries.put("economic_status", patient.getEconomic_status());
            patientEntries.put("education_status", patient.getEducation_level());
            patientEntries.put("caste", patient.getCaste());


            //TODO: move identifier1 and id2 from patient table to patient_attribute table
        }


        @Override
        protected Boolean doInBackground(Void... params) {

            gatherEntries();

            db4.insert(
                    "patient",
                    null,
                    patientEntries
            );
            db4.close();
            return null;
        }

        @Override
        public void onCancel(DialogInterface dialog) {
            cancel(true);
            dialog.dismiss();
        }

        /**
         * Starting from Identification Activity, the patientID is passed between all activities.
         * All activities will put the ID out from the intent, and then package it into the next one after.
         * patientID - the ID number used for local storage; also used when posting to OpenMRS
         * tag - used when the user edits a patient's information
         */
        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            Intent intent2 = new Intent(IdentificationActivity.this, PatientDetailActivity.class);
            String fullName = patient.getFirstName() + " " + patient.getLastName();
            intent2.putExtra("patientID", patientID);
            intent2.putExtra("name", fullName);
            intent2.putExtra("tag", "new");
            intent2.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            startActivity(intent2);
            finish();
        }
    }

    // This is a async method for updating the database if user changes any details of patient
    public class UpdatePatientTable extends AsyncTask<Void, Void, Boolean> implements DialogInterface.OnCancelListener {
        SQLiteDatabase db1 = mDbHelper.getWritableDatabase();

        UpdatePatientTable(Patient currentpatient) {

            patient1 = currentpatient;
        }

        ContentValues patientEntries1 = new ContentValues();

        public void gatherEntries1() {
            patientEntries1.put("first_name", patient1.getFirstName());
            patientEntries1.put("middle_name", patient1.getMiddleName());
            patientEntries1.put("last_name", patient1.getLastName());
            patientEntries1.put("date_of_birth", patient1.getDateOfBirth());
            patientEntries1.put("phone_number", patient1.getPhoneNumber());
            patientEntries1.put("address1", patient1.getAddress1());
            patientEntries1.put("address2", patient1.getAddress2());
            patientEntries1.put("city_village", patient1.getCityVillage());
            patientEntries1.put("state_province", patient1.getStateProvince());
            patientEntries1.put("postal_code", patient1.getPostalCode());
            patientEntries1.put("country", patient1.getCountry());
            patientEntries1.put("gender", patient1.getGender());
            patientEntries1.put("sdw", patient1.getSdw());
            patientEntries1.put("occupation", patient1.getOccupation());
            patientEntries1.put("economic_status", patient1.getEconomic_status());
            patientEntries1.put("education_status", patient1.getEducation_level());
            patientEntries1.put("caste", patient1.getCaste());
            if (mCurrentPhotoPath != null) {
                if (patient1.getPatientPhoto() != null && !patient1.getPatientPhoto().trim().isEmpty()) {
                    File file = new File(patient1.getPatientPhoto());
                    file.delete();
                }
                patientEntries1.put("patient_photo", mCurrentPhotoPath);
            }

        }

        @Override
        protected Boolean doInBackground(Void... params) {
            gatherEntries1();
            db1.update("patient", patientEntries1, "_id=?", new String[]{String.valueOf(patient1.getId())});

            db1.close();

            return null;
        }

        @Override
        public void onCancel(DialogInterface dialog) {

        }

        @Override
        protected void onPostExecute(Boolean bBoolean) {
            super.onPostExecute(bBoolean);
            Intent intent3 = new Intent(IdentificationActivity.this, PatientDetailActivity.class);
            String fullName = patient1.getFirstName() + " " + patient1.getLastName();

            intent3.putExtra("patientID", patientID_edit);
            intent3.putExtra("name", fullName);
            intent3.putExtra("tag", "edit");
            intent3.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            startActivity(intent3);
            finish();
        }
    }

    public static String getUserCountryFunction(Context context) {
        try {
            final TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            final String simCountry = tm.getSimCountryIso();
//            Log.d("Country", simCountry);
            if (simCountry != null && simCountry.length() == 2) { // SIM country code is available
                return simCountry.toUpperCase(Locale.US);
            } else if (tm.getPhoneType() != TelephonyManager.PHONE_TYPE_CDMA) { // device is not 3G (would be unreliable)
                String networkCountry = tm.getNetworkCountryIso();
//                Log.d("Country", networkCountry);
                if (networkCountry != null && networkCountry.length() == 2) { // network country code is available
                    return networkCountry.toUpperCase(Locale.US);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CAMERA) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                String[] results = HelperMethods.dispatchTakePictureIntent(REQUEST_CAMERA, IdentificationActivity.this);
                if (results != null) {
                    mPhoto = results[0];
                    mCurrentPhotoPath = results[1];
                }
            } else {
                Log.e("Camera Permissions", "Permission Denied");
            }
        } else if (requestCode == REQUEST_READ_EXTERNAL) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Log.e("Read/Write Permissions", "Permission Denied");
            }
        }
    }

    public void generateID() {
        SQLiteDatabase db1 = mDbHelper.getWritableDatabase();
        String table = "patient";
        String[] columnsToReturn = {"_id"};
        String orderBy = "_id";
        final Cursor idCursor = db1.query(table, columnsToReturn, null, null, null, null, orderBy);
        idCursor.moveToLast();

        if (idCursor.getCount() > 0) {
            Integer lastIntegerID = idCursor.getInt(idCursor.getColumnIndexOrThrow("_id"));
            lastIntegerID++;
            patientID = lastIntegerID; //This patient is assigned the new incremented number
            patient.setId(patientID);
        } else {
            patient.setId(1);
        }


        idCursor.close();
        patientID = patient.getId();
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to go back ?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                }).setNegativeButton("No", null).show();

    }


}
