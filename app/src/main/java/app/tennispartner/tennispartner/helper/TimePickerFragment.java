package app.tennispartner.tenispartner.helper;


import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.text.format.DateFormat;
import android.widget.TimePicker;

import app.tennispartner.tenispartner.CourtFragment;
import app.tennispartner.tenispartner.R;

import java.util.Calendar;

import androidx.fragment.app.DialogFragment;

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
        Intent intent = new Intent(getActivity(), CourtFragment.class);
        intent.putExtra("dateTime", date);
        getActivity().startActivityForResult(intent, RC_COURT_PICK);
    }


}
