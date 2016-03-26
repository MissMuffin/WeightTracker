package de.muffinworks.weighttracker.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import java.util.Date;

import de.muffinworks.weighttracker.R;
import de.muffinworks.weighttracker.util.DateUtil;

/**
 * Created by Bianca on 01.03.2016.
 */
public class WeightDialogFragment extends DialogFragment {

    public interface WeightDialogListener {
        void onDialogPositiveClick(DialogFragment dialog, Date date, double input);
        void onDialogNegativeClick(DialogFragment dialog);
        void onDialogDismiss(DialogFragment dialog);
    }

    private WeightDialogListener mListener;
    private EditText editTextWeight;
    private String oldInput = "";
    private Date date = DateUtil.currentDate();
    private AlertDialog alert;
    private Button posButton;


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            mListener = (WeightDialogListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement WeightDialogListener");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        //get reference to button obj
        posButton = alert.getButton(DialogInterface.BUTTON_POSITIVE);
        posButton.setEnabled(false);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //open soft keyboard automatically
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        //set existing weight
        editTextWeight.setText(oldInput);
        moveCursorToEnd();

        //add textwatcher to limit ipnut of more than 3 digits before period
        editTextWeight.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().matches("^[0-9]{4,}([.])?(.+)?")) {
                    editTextWeight.setError("Weight should be below 1000 for living humans");
                } else {
                    editTextWeight.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                //enable submit button after input has been changed
                posButton.setEnabled(true);
            }
        });

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_enter_weight, null);
        editTextWeight = (EditText)view.findViewById(R.id.edit_text_weight);

        builder.setTitle("Enter your weight")
                .setView(view)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String input = getEditTextInput();
                        double weight = -1;
                        if (input.matches("(^[.][0-9]+|^[0-9]+[.]?([0-9]+)?)")) {
                            weight = Double.parseDouble(input);
                        }
                        mListener.onDialogPositiveClick(WeightDialogFragment.this, date, weight);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onDialogNegativeClick(WeightDialogFragment.this);
                    }
                });
        alert = builder.create();
        return alert;
    }

    private String getEditTextInput() {
        return editTextWeight.getText().toString();
    }

    public void setWeight(String s) {
        this.oldInput = s;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void moveCursorToEnd() {
        editTextWeight.setSelection(editTextWeight.getText().toString().length());
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        mListener.onDialogDismiss(WeightDialogFragment.this);
        super.onDismiss(dialog);
    }
}
