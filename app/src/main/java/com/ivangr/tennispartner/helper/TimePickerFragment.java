package com.ivangr.tennispartner.helper;


import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.TimePicker;

import com.ivangr.tennispartner.CourtActivity;
import com.ivangr.tennispartner.R;
import com.jakewharton.threetenabp.AndroidThreeTen;

import org.threeten.bp.LocalDateTime;
import org.threeten.bp.format.DateTimeFormatter;

import java.util.Calendar;

import androidx.fragment.app.DialogFragment;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 */
public class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {


    public static final int RC_COURT_PICK = 45;
    private int[] date;

    public TimePickerFragment() {
        // Required empty public constructor
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current time as the default values for the picker
        Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        date = getArguments().getIntArray("date");

        // Create a new instance of TimePickerDialog and return it
        TimePickerDialog picker = new TimePickerDialog(getActivity(), this, hour, minute,
                DateFormat.is24HourFormat(getActivity()));
        picker.setTitle(getString(R.string.suggest_match_time));
        return picker;
    }

    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        date[3] = hourOfDay;
        date[4] = minute;
        Intent intent = new Intent(getActivity(), CourtActivity.class);
        intent.putExtra("dateTime", date);
        getActivity().startActivityForResult(intent, RC_COURT_PICK);
    }


}
