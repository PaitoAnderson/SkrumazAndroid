package com.skrumaz.app;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;
import com.skrumaz.app.classes.AllowedValue;
import com.skrumaz.app.classes.AttributeDefinition;
import com.skrumaz.app.classes.CreateResult;
import com.skrumaz.app.data.Store.TypeDefinitions;
import com.skrumaz.app.data.WebService.GetCreateAuthorization;
import com.skrumaz.app.data.WebService.GetFormAttributes;
import com.skrumaz.app.data.WebService.PutDomainObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

/**
 * Created by Paito Anderson on 1/25/2014.
 */
public class Create extends ActionBarActivity {

    // TAG for logging
    private static final String TAG = "CREATE";

    private Context mContext;

    private LinearLayout processContainer;
    private LinearLayout inputContainer;
    private TextView progressText;
    private ProgressBar progressSpinner;
    private Boolean continueRequests = true;

    private String createName;
    private String createType;

    JSONObject createObject = new JSONObject();
    private CreateResult createResult;

    private List<AttributeDefinition> attributeDefinitions = new ArrayList<>();

    private View activeView;

    private Calendar myCalendar = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);

        Bundle extras = getIntent().getExtras();
        createName = extras.getString("CreateName");
        createType = extras.getString("CreateType");

        processContainer = (LinearLayout) findViewById(R.id.processContainer);
        inputContainer = (LinearLayout) findViewById(R.id.inputContainer);
        progressText = (TextView) findViewById(R.id.progressText);
        progressSpinner = (ProgressBar) findViewById(R.id.progressSpinner);

        // Set Context variable to self
        mContext = this;

        // Setup Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Add back button icon
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setTitle("Create " + createName);

        // Populate Form
        new GetForm().execute();
    }

    private class GetForm extends AsyncTask<String, Integer, Boolean> {

        @Override
        protected void onPreExecute() {

            startLoading("Loading Form...");

            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (!continueRequests) {
                progressSpinner.setVisibility(View.GONE);
                String breakingError = "";
                progressText.setText(breakingError);
            }

            int layoutInputID = 0;

            // Build Inputs
            for (AttributeDefinition attributeDefinition : attributeDefinitions) {

                // Logging
                Log.d(TAG, attributeDefinition.getName() + " " + attributeDefinition.getAttributeType().toString());

                //attributeDefinitions.get(layoutInputID).setLayoutId(layoutInputID);

                switch (attributeDefinition.getAttributeType()) {
                    case BOOLEAN:
                        CheckBox checkBox = new CheckBox(mContext);
                        if (attributeDefinition.getRequired()) {
                            checkBox.setTextColor(getResources().getColor(R.color.accent_color));
                        }
                        checkBox.setText(attributeDefinition.getName());
                        checkBox.setId(layoutInputID);
                        inputContainer.addView(checkBox);
                        break;
                    case INTEGER:
                        // Add Label
                        TextView textView = new TextView(mContext);
                        if (attributeDefinition.getRequired()) {
                            textView.setTextColor(getResources().getColor(R.color.accent_color));
                        }
                        textView.setText(attributeDefinition.getName());
                        inputContainer.addView(textView);
                        // Add Input
                        EditText editText = new EditText(mContext);
                        editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                        editText.setId(layoutInputID);
                        editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(attributeDefinition.getMaxLength())});
                        inputContainer.addView(editText);
                        break;
                    case QUANTITY:
                        // Add Label
                        TextView textView2 = new TextView(mContext);
                        if (attributeDefinition.getRequired()) {
                            textView2.setTextColor(getResources().getColor(R.color.accent_color));
                        }
                        textView2.setText(attributeDefinition.getName());
                        inputContainer.addView(textView2);
                        // Add Input
                        EditText editText2 = new EditText(mContext);
                        editText2.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
                        editText2.setId(layoutInputID);
                        editText2.setFilters(new InputFilter[]{new InputFilter.LengthFilter(attributeDefinition.getMaxLength())});
                        inputContainer.addView(editText2);
                        break;
                    case STRING:
                        // Add Label
                        TextView textView3 = new TextView(mContext);
                        if (attributeDefinition.getRequired()) {
                            textView3.setTextColor(getResources().getColor(R.color.accent_color));
                        }
                        textView3.setText(attributeDefinition.getName());
                        inputContainer.addView(textView3);
                        if (attributeDefinition.getConstrained()) {
                            // Add Empty Spinner
                            Button button = new Button(mContext);
                            button.setId(layoutInputID);
                            button.setOnClickListener(SpinnerOnClick);
                            button.setBackgroundResource(android.R.drawable.btn_dropdown);
                            button.setTextColor(getResources().getColor(android.R.color.black));
                            inputContainer.addView(button);
                        } else {
                            // Add Input
                            EditText editText3 = new EditText(mContext);
                            editText3.setInputType(InputType.TYPE_CLASS_TEXT);
                            editText3.setId(layoutInputID);
                            editText3.setFilters(new InputFilter[]{new InputFilter.LengthFilter(attributeDefinition.getMaxLength())});
                            inputContainer.addView(editText3);
                        }
                        break;
                    case TEXT:
                        // Add Label
                        TextView textView4 = new TextView(mContext);
                        if (attributeDefinition.getRequired()) {
                            textView4.setTextColor(getResources().getColor(R.color.accent_color));
                        }
                        textView4.setText(attributeDefinition.getName());
                        inputContainer.addView(textView4);
                        // Add Input
                        EditText editText4 = new EditText(mContext);
                        editText4.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
                        editText4.setId(layoutInputID);
                        editText4.setFilters(new InputFilter[]{new InputFilter.LengthFilter(attributeDefinition.getMaxLength())});
                        editText4.setMinLines(1);
                        editText4.setVerticalScrollBarEnabled(true);
                        inputContainer.addView(editText4);
                        break;
                    case DATE:
                        // Add Label
                        TextView textView5 = new TextView(mContext);
                        if (attributeDefinition.getRequired()) {
                            textView5.setTextColor(getResources().getColor(R.color.accent_color));
                        }
                        textView5.setText(attributeDefinition.getName());
                        inputContainer.addView(textView5);
                        // Add Input
                        EditText editText3 = new EditText(mContext);
                        editText3.setInputType(InputType.TYPE_CLASS_TEXT);
                        editText3.setHint("DD/MMM/YYYY");
                        editText3.setId(layoutInputID);
                        editText3.setOnFocusChangeListener(DateOnFocus);
                        editText3.setOnClickListener(DateOnClick);
                        inputContainer.addView(editText3);
                        break;
                    case STATE:
                    case RATING:
                        // TODO: Could be a static list...
                        //Spinner spinner = new Spinner(mContext);
                        //spinner.setId(layoutInputID);
                        //spinner.setAdapter(null);
                        //spinner.setOnItemSelectedListener(SpinnerOnClick);
                        //inputContainer.addView(spinner);
                    case OBJECT:
                        // Add Label
                        TextView textView6 = new TextView(mContext);
                        if (attributeDefinition.getRequired()) {
                            textView6.setTextColor(getResources().getColor(R.color.accent_color));
                        }
                        textView6.setText(attributeDefinition.getName());
                        inputContainer.addView(textView6);
                        // Add Empty Spinner
                        Button button = new Button(mContext);
                        button.setId(layoutInputID);
                        button.setOnClickListener(SpinnerOnClick);
                        button.setBackgroundResource(android.R.drawable.btn_dropdown);
                        button.setTextColor(getResources().getColor(android.R.color.black));
                        inputContainer.addView(button);
                        break;
                    case COLLECTION:
                        // TODO: Support this..
                        break;
                }
                layoutInputID++;
            }

            finishLoading();

            super.onPostExecute(result);
        }

        @Override
        protected Boolean doInBackground(String... params) {

            // Definition ID for User Stories
            TypeDefinitions typeDefinitions = new TypeDefinitions(mContext);
            Long definitionId = typeDefinitions.getDefinition(mContext, createType);

            // Get Form Attributes
            attributeDefinitions.addAll(new GetFormAttributes().FetchItems(mContext, definitionId));

            return null;
        }
    }

    //On click for Date Pickers
    private final View.OnClickListener DateOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            // Update Active View
            activeView = v;

            //Inform the user the date input has been clicked
            new DatePickerDialog(Create.this, date, myCalendar
                    .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                    myCalendar.get(Calendar.DAY_OF_MONTH)).show();
        }
    };

    //On focus for Date Pickers
    private final View.OnFocusChangeListener DateOnFocus = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean focus) {

            // Update Active View
            activeView = v;

            //Inform the user the date input has focus
            if (focus) {
                new DatePickerDialog(Create.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        }
    };

    DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, monthOfYear);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateDatePicker();
        }
    };

    // On click listener for Spinners (Really a button)
    private final View.OnClickListener SpinnerOnClick = new View.OnClickListener() {
        public void onClick(final View v) {

            // Update Active View
            activeView = v;

            // Create custom alert dialog select list
            new SpinnerAllowedValues(attributeDefinitions.get(v.getId()).getObjectId(), attributeDefinitions.get(v.getId()).getName()).show(getFragmentManager(), "MyDialog");
        }
    };

    private void updateDatePicker() {

        // Update Input
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        EditText activeDatePicker = (EditText) activeView;
        activeDatePicker.setText(sdf.format(myCalendar.getTime()));
        updateReturnValue(null);
    }

    public void setValueFromSpinner(AllowedValue input) {
        // Update Input
        Button activeButton = (Button) activeView;
        activeButton.setText(input.getName());
        updateReturnValue(input);
    }

    protected void updateReturnValue(AllowedValue input) {
        switch (attributeDefinitions.get(activeView.getId()).getAttributeType()) {
            case DATE:
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T00:00:00.'SSSZ", Locale.getDefault());
                attributeDefinitions.get(activeView.getId()).setReturnValue(sdf.format(myCalendar.getTime()));
                break;
            case STRING: // Only used for constrained strings
            case STATE:
            case RATING:
                attributeDefinitions.get(activeView.getId()).setReturnValue(input.getName());
                break;
            case OBJECT:
                attributeDefinitions.get(activeView.getId()).setReturnValue(input.getOid().toString());
                break;
        }
    }

    public void startLoading(String message) {
        inputContainer.setVisibility(View.INVISIBLE);
        processContainer.setVisibility(View.VISIBLE);
        progressSpinner.setVisibility(View.VISIBLE);
        progressText.setVisibility(View.VISIBLE);
        progressText.setText(message);
    }

    public void finishLoading() {
        if (continueRequests) {
            processContainer.setVisibility(View.GONE);
            inputContainer.setVisibility(View.VISIBLE);
        }
    }

    public void createItem() {

        //String securityKey = new GetSecurityToken().FetchKey(mContext);
        //Log.d(TAG, "key=" + securityKey);

        try {
            JSONObject inputObj = new JSONObject();
            int layoutInputID = 0;
            for (AttributeDefinition attributeDefinition : attributeDefinitions) {
                switch (attributeDefinition.getAttributeType()) {
                    case BOOLEAN:
                        inputObj.put(attributeDefinition.getElementName(), ((CheckBox) inputContainer.findViewById(layoutInputID)).isChecked());
                        break;
                    case STRING:
                        if (attributeDefinition.getConstrained()) {
                            if (attributeDefinition.getReturnValue() !=null &&  !attributeDefinition.getReturnValue().isEmpty()){
                                inputObj.put(attributeDefinition.getElementName(), attributeDefinition.getReturnValue());
                            }
                        }
                        else
                        {
                            if (((EditText) inputContainer.findViewById(layoutInputID)).getText().length() > 0) {
                                inputObj.put(attributeDefinition.getElementName(), ((EditText) inputContainer.findViewById(layoutInputID)).getText());
                            }
                        }
                        break;
                    case INTEGER:
                    case QUANTITY:
                    case TEXT:
                        if (((EditText) inputContainer.findViewById(layoutInputID)).getText().length() > 0) {
                            inputObj.put(attributeDefinition.getElementName(), ((EditText) inputContainer.findViewById(layoutInputID)).getText());
                        }
                        break;
                    case STATE:
                    case RATING:
                    case OBJECT:
                    case DATE:
                        if (attributeDefinition.getReturnValue() !=null &&  !attributeDefinition.getReturnValue().isEmpty()){
                            inputObj.put(attributeDefinition.getElementName(), attributeDefinition.getReturnValue());
                        }
                        break;
                    case COLLECTION:
                        // TODO: Support this..
                        break;
                }
                layoutInputID++;
            }
            createObject.put(createType, inputObj);
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
        Log.e(TAG, createObject.toString());

        new CreateItem().execute();
    }

    private class CreateItem extends AsyncTask<String, Integer, Boolean> {

        @Override
        protected void onPreExecute() {
            startLoading("Creating " + createName + "...");
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(String... params) {

            // Create Object (Includes getting Security Key)
            createResult = new PutDomainObject().PutObject(mContext, new GetCreateAuthorization().Fetch(mContext), createType, createObject);

            return null;
        }

        @Override
        protected void onPostExecute(Boolean result) {

            finishLoading();

            if (createResult.getSuccess()) {

                // TODO: Invalidate Iteration Cache
                //if (createType.equalsIgnoreCase("Iteration"))
                //{
                    //Iterations db = new Iterations(mContext);
                    //db.invalidIterations();
                //}

                // Notify user of the success
                Toast.makeText(mContext, createResult.getAllMessages().get(0), Toast.LENGTH_LONG).show();

                // Return to previous activity
                finish();
            } else {

                // Loop through the errors
                for (String errorMessage : createResult.getAllMessages()) {
                    // Show users the errors as a ActionBar notification
                    Crouton.makeText((Activity) mContext, errorMessage, Style.ALERT).show();
                }
            }

            super.onPostExecute(result);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.create, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action buttons
        switch (item.getItemId()) {
            case android.R.id.home:
                super.onBackPressed();
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.fade_out);
                finish();
                break;
            case R.id.action_save:
                createItem();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        EasyTracker.getInstance(this).activityStart(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EasyTracker.getInstance(this).activityStop(this);
    }

    @Override
    protected void onDestroy(){
        Crouton.clearCroutonsForActivity(this);
        super.onDestroy();
    }
}
