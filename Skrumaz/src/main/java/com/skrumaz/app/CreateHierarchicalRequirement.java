package com.skrumaz.app;

import android.app.ActionBar;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
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
import com.skrumaz.app.data.Preferences;
import com.skrumaz.app.data.Store.TypeDefinitions;
import com.skrumaz.app.data.WebService.GetFormAttributes;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Created by Paito Anderson on 1/25/2014.
 */
public class CreateHierarchicalRequirement extends Activity {

    // TAG for logging
    private static final String TAG = "CREATE";

    private LinearLayout processContainer;
    private LinearLayout inputContainer;
    private TextView progressText;
    private ProgressBar progressSpinner;
    private Boolean continueRequests = true;
    private String breakingError = "";

    private Context mContext;
    private List<AttributeDefinition> attributeDefinitions  = new ArrayList<AttributeDefinition>();
    private int layoutInputID = 0;

    private View activeView;

    Calendar myCalendar = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_hierarchical_requirement);

        processContainer = (LinearLayout) findViewById(R.id.processContainer);
        inputContainer = (LinearLayout) findViewById(R.id.inputContainer);
        progressText = (TextView) findViewById(R.id.progressText);
        progressSpinner = (ProgressBar) findViewById(R.id.progressSpinner);

        // Set Context variable to self
        mContext = this;

        // Add back button icon
        ActionBar actionbar = getActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setTitle("Create User Story");

        // Populate Form
        new GetService().execute();
    }

    // Type Definitions - DONE!
    //https://rally1.rallydev.com/slm/webservice/v2.0/typedefinition/?pagesize=100&pretty=true

    // Fields
    //https://rally1.rallydev.com/slm/webservice/v2.0/project/6378006083/typedefinition/3418690825/scopedattributedefinition?fetch=ObjectID,AttributeType,ChildProjectHiddenCount,ChildProjectVisibleCount,Constrained,Custom,Hidden,Name,Required,SharedAcrossWorkItems,Sortable,SystemRequired,VisibilityOnChildProjects,VisibleOnlyToAdmins&pagesize=100&pretty=true&order=ObjectID

    // Fields
    //https://rally1.rallydev.com/slm/webservice/v2.0/TypeDefinition/3418690825/Attributes?pretty=true&pagesize=100&order=ObjectID

    // Values Available (For Dropdowns)
    //https://rally1.rallydev.com/slm/webservice/v2.0/attributedefinition/ae430cd3-f67b-46d2-a899-04ef13476603/AllowedValues?pretty=true

    class GetService extends AsyncTask<String, Integer, Boolean> {

        @Override
        protected void onPreExecute() {

            startLoading();

            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (!continueRequests)
            {
                progressSpinner.setVisibility(View.GONE);
                progressText.setText(breakingError);
            }

            // Build Inputs
            for (AttributeDefinition attributeDefinition : attributeDefinitions) {

                // Logging
                Log.d(TAG, attributeDefinition.getName() + " " + attributeDefinition.getAttributeType().toString());

                //attributeDefinitions.get(layoutInputID).setLayoutId(layoutInputID);

                switch (attributeDefinition.getAttributeType()) {
                    case BOOLEAN:
                        CheckBox checkBox = new CheckBox(mContext);
                        if (attributeDefinition.getRequired())
                        {
                            checkBox.setTextColor(getResources().getColor(R.color.accent_color));
                        }
                        checkBox.setText(attributeDefinition.getName());
                        checkBox.setId(layoutInputID);
                        inputContainer.addView(checkBox);
                        break;
                    case INTEGER:
                        // Add Label
                        TextView textView = new TextView(mContext);
                        if (attributeDefinition.getRequired())
                        {
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
                        if (attributeDefinition.getRequired())
                        {
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
                        if (attributeDefinition.getRequired())
                        {
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
                        if (attributeDefinition.getRequired())
                        {
                            textView4.setTextColor(getResources().getColor(R.color.accent_color));
                        }
                        textView4.setText(attributeDefinition.getName());
                        inputContainer.addView(textView4);
                        // Add Input
                        EditText editText4 = new EditText(mContext);
                        editText4.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_MULTI_LINE);
                        editText4.setId(layoutInputID);
                        editText4.setFilters(new InputFilter[]{new InputFilter.LengthFilter(attributeDefinition.getMaxLength())});
                        editText4.setMinLines(1);
                        editText4.setVerticalScrollBarEnabled(true);
                        inputContainer.addView(editText4);
                        break;
                    case DATE:
                        // Add Label
                        TextView textView5 = new TextView(mContext);
                        if (attributeDefinition.getRequired())
                        {
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
                    case STATE:
                        // TODO: Support this..
                        break;
                    case COLLECTION:
                        // TODO: Support this..
                        break;
                    case OBJECT:
                        // Add Label
                        TextView textView6 = new TextView(mContext);
                        if (attributeDefinition.getRequired())
                        {
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

                        //Spinner spinner = new Spinner(mContext);
                        //spinner.setId(layoutInputID);
                        //spinner.setAdapter(null);
                        //spinner.setOnItemSelectedListener(SpinnerOnClick);
                        //inputContainer.addView(spinner);
                        break;
                }
                layoutInputID++;
            }

            finishLoading();

            super.onPostExecute(result);
        }

        @Override
        protected Boolean doInBackground(String... params) {

            // Project ID
            Long projectId = Preferences.getProjectId(getBaseContext(), true);
            Log.d(TAG, "projectID: " + projectId);

            // Definition ID for User Stories
            TypeDefinitions typeDefinitions = new TypeDefinitions(mContext);
            Long definitionId = typeDefinitions.getDefinition(mContext, "HierarchicalRequirement");
            Log.d(TAG, "definitionID: " + definitionId);

            // Get Form Attributes
            attributeDefinitions.addAll(new GetFormAttributes().FetchItems(mContext, projectId, definitionId));

            return null;
        }
    }

    //On click for Date Pickers
    final View.OnClickListener DateOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            // Update Active View
            activeView = v;

            //Inform the user the date input has been clicked
            new DatePickerDialog(CreateHierarchicalRequirement.this, date, myCalendar
                .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                 myCalendar.get(Calendar.DAY_OF_MONTH)).show();
        }
    };

    //On focus for Date Pickers
    final View.OnFocusChangeListener DateOnFocus = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean focus) {

            // Update Active View
            activeView = v;

            //Inform the user the date input has focus
            if (focus) {
                new DatePickerDialog(CreateHierarchicalRequirement.this, date, myCalendar
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
    final View.OnClickListener SpinnerOnClick = new View.OnClickListener() {
        public void onClick(final View v) {

            // Update Active View
            activeView = v;

            // Create custom alert dialog select list
            new SpinnerAllowedValues(attributeDefinitions.get(v.getId()).getObjectId(), attributeDefinitions.get(v.getId()).getName()).show(getFragmentManager(), "MyDialog");
        }
    };

    private void updateDatePicker() {

        // Update Input
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.US);
        EditText activeDatePicker = (EditText) activeView;
        activeDatePicker.setText(sdf.format(myCalendar.getTime()));
    }

    public void setValueFromSpinner(AllowedValue input)
    {
        // Update Input
        Button activeButton = (Button) activeView;
        activeButton.setText(input.getName());
    }

    public void startLoading() {
        // Reset Views / Spinner
        processContainer.setVisibility(View.VISIBLE);
        progressSpinner.setVisibility(View.VISIBLE);
        progressText.setVisibility(View.VISIBLE);
        progressText.setText("Loading Form..."); // Text updated using SetProgress()
    }

    public void finishLoading() {
        if (continueRequests) {
            processContainer.setVisibility(View.GONE);
            inputContainer.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
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
        switch(item.getItemId()) {
            case android.R.id.home:
                super.onBackPressed();
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.fade_out);
                finish();
                break;
            case R.id.action_save:
                Toast.makeText(mContext, "Call Save...", Toast.LENGTH_SHORT).show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();
        EasyTracker.getInstance(this).activityStart(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EasyTracker.getInstance(this).activityStop(this);
    }
}
