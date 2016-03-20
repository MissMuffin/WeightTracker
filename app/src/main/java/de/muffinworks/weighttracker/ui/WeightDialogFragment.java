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

import de.muffinworks.weighttracker.R;

/**
 * Created by Bianca on 01.03.2016.
 */
public class WeightDialogFragment extends DialogFragment {

    public interface WeightDialogListener {
        void onDialogPositiveClick(DialogFragment dialog, double input);
        void onDialogNegativeClick(DialogFragment dialog);
        void onDialogDismiss(DialogFragment dialog);
    }

    WeightDialogListener mListener;
    EditText editTextWeight;

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
                        if (input.matches("((^[.][0-9]+|^[0-9]+[.]?([0-9]+)?))")) {
                            weight = Double.parseDouble(input);
                        }
                        mListener.onDialogPositiveClick(WeightDialogFragment.this, weight);
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

    @Override
    public void onDismiss(DialogInterface dialog) {
        mListener.onDialogDismiss(WeightDialogFragment.this);
        super.onDismiss(dialog);
    }
}
