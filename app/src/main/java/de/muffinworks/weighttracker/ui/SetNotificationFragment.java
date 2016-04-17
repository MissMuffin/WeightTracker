package de.muffinworks.weighttracker.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TimePicker;

import de.muffinworks.weighttracker.R;


public class SetNotificationFragment extends DialogFragment {

    private NotificationFragmentListener listener;

    private TimePicker timePicker;

    public SetNotificationFragment() {
        // Required empty public constructor

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return super.onCreateView(inflater, container, savedInstanceState);
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_set_notification, null);

        timePicker = (TimePicker) view.findViewById(R.id.timepicker);
        timePicker.setCurrentHour(hour);
        timePicker.setCurrentMinute(minute);
        timePicker.setIs24HourView(true);

        builder.setTitle("Set a reminder")
                .setView(view)
                .setPositiveButton("Set", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        listener.SetReminder(timePicker.getCurrentHour(), timePicker.getCurrentMinute());
                    }
                })
                .setNegativeButton("Disable", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        listener.DisableReminder();
                    }
                })
                .setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });
        return builder.create();
    }

    private int hour;
    private int minute;

    public void setTime(int hour, int minute) {
        this.hour = hour;
        this.minute = minute;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Verify that the host activity implements the callback interface
        try {
            listener = (NotificationFragmentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement NotificationFragmentListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

    }

    public interface NotificationFragmentListener {
        void SetReminder(int hour, int minute);
        void DisableReminder();
    }

}
