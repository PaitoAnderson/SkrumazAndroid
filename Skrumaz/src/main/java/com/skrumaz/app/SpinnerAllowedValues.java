package com.skrumaz.app;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.skrumaz.app.classes.AllowedValue;
import com.skrumaz.app.data.WebService.GetAllowedValues;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Paito Anderson on 2/1/2014.
 */
public class SpinnerAllowedValues extends DialogFragment {

    private Context mContext;

    private LinearLayout processContainer;
    private ListView inputContainer;
    private TextView progressText;
    private ProgressBar progressSpinner;
    private Boolean continueRequests = true;

    private Long fieldId;
    private String fieldName;

    private List<AllowedValue> allowedValues  = new ArrayList<>();

    public SpinnerAllowedValues() {
        // Empty constructor required for DialogFragment
    }

    // Constructor used
    // This screen can't rotate so it's pretty safe..
    @SuppressLint("ValidFragment")
    public SpinnerAllowedValues(Long fieldId, String fieldName) {
        this.fieldId = fieldId;
        this.fieldName = fieldName;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set Context variable to self
        mContext = getActivity();

        // Set to a style without a title, no theme
        setStyle(DialogFragment.STYLE_NORMAL, 0);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // Inflate View
        final LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        View layoutView = layoutInflater.inflate(R.layout.spinner_allowed_values, null);

        processContainer = (LinearLayout) layoutView.findViewById(R.id.processContainer);
        inputContainer = (ListView) layoutView.findViewById(R.id.inputContainer);
        progressText = (TextView) layoutView.findViewById(R.id.progressText);
        progressSpinner = (ProgressBar) layoutView.findViewById(R.id.progressSpinner);

        // Setup / Build AlertDialog
        AlertDialog alertDialog = new AlertDialog.Builder(layoutView.getContext())
                .setView(layoutView)
                .setTitle(fieldName)
                .create();

        // Populate Values
        new GetService().execute();

        return alertDialog;
    }

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
                String breakingError = "";
                progressText.setText(breakingError);
            }

            finishLoading();

            // Populate ListView
            populateListView();

            super.onPostExecute(result);
        }

        @Override
        protected Boolean doInBackground(String... params) {

            // Get Allowed Values
            allowedValues.addAll(new GetAllowedValues().FetchItems(mContext, fieldId));

            return null;
        }
    }

    public void startLoading() {
        // Reset Views / Spinner
        processContainer.setVisibility(View.VISIBLE);
        progressSpinner.setVisibility(View.VISIBLE);
        progressText.setVisibility(View.VISIBLE);
        progressText.setText("Loading Values...");
    }

    public void finishLoading() {
        if (continueRequests) {
            processContainer.setVisibility(View.GONE);
            inputContainer.setVisibility(View.VISIBLE);
        }
    }

    public void populateListView() {

        // Create adapter from allowedValues
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                getActivity(),
                android.R.layout.simple_list_item_1,
                AllowedValue.getOptionList(allowedValues));

        // Set adapter to ListView
        inputContainer.setAdapter(arrayAdapter);

        // Handle user selection
        inputContainer.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                // Update parent with selection
                Create callingActivity = (Create) getActivity();
                callingActivity.setValueFromSpinner(allowedValues.get(position));

                // Close DialogFragment
                getDialog().dismiss();
            }
        });
    }
}
