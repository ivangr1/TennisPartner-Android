package app.tennispartner.tennispartner.helper;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import app.tennispartner.tennispartner.R;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class FilterDialog extends DialogFragment {

    private int radius;
    private TextView mDistanceCounter;
    private SharedPreferences sharedPref;

    public interface NoticeDialogListener {
        void onDialogPositiveClick(int radius);
    }

    NoticeDialogListener mListener;
    private SeekBar mDistanceSeekBar;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);

        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();

        View filter_view = inflater.inflate(R.layout.filter_view, null);

        mDistanceSeekBar = filter_view.findViewById(R.id.distanceSeekBar);
        mDistanceCounter = filter_view.findViewById(R.id.distanceCounter);
        radius = sharedPref.getInt(getString(R.string.partner_radius), getResources().getInteger(R.integer.radius_default));
        mDistanceSeekBar.setProgress(radius);
        mDistanceCounter.setText(getString(R.string.distance_text_value, radius));

        builder.setTitle(R.string.dialog_filter_title)
                .setView(filter_view)
                .setPositiveButton(R.string.apply_button, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putInt(getString(R.string.partner_radius), radius);
                        editor.apply();
                        mListener.onDialogPositiveClick(radius);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });

        mDistanceSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                TextView mDistanceCounter = filter_view.findViewById(R.id.distanceCounter);
                mDistanceCounter.setText(getString(R.string.distance_text_value, progress));
                radius = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        // Create the AlertDialog object and return it
        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (NoticeDialogListener) context;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(context.toString()
                    + " must implement NoticeDialogListener");
        }
    }
}
