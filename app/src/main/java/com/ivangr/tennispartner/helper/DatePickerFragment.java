package com.ivangr.tennispartner.helper;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.widget.DatePicker;

import com.ivangr.tennispartner.R;

import java.util.Calendar;

import androidx.fragment.app.DialogFragment;

public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current date as the default date in the picker
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        // Create a new instance of DatePickerDialog and return it
        DatePickerDialog picker = new DatePickerDialog(getActivity(), this, year, month, day);
        picker.getDatePicker().setMinDate(System.currentTimeMillis());
        picker.setTitle(getString(R.string.suggest_match_date));
        return picker;
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        Bundle date = new Bundle();
        int[] dateInt = {year, month, day, 0, 0};
        date.putIntArray("date", dateInt);
        DialogFragment newFragment = new TimePickerFragment();
        newFragment.setArguments(date);
        newFragment.show(getFragmentManager(), "timePicker");
    }
}
