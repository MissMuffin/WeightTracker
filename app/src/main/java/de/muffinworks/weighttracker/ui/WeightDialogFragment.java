package de.muffinworks.weighttracker.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //open soft keyboard automatically
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        //set existing weight
        editTextWeight.setText(oldInput);

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
                        if (input.matches("(^[.][0-9]+|^[0-9]+[.]?([0-9]+)?)") && !input.equals(oldInput)) {
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

        return builder.create();
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

    @Override
    public void onDismiss(DialogInterface dialog) {
        mListener.onDialogDismiss(WeightDialogFragment.this);
        super.onDismiss(dialog);
    }
}
