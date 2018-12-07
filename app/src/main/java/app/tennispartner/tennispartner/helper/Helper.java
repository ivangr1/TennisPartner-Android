package app.tennispartner.tennispartner.helper;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import app.tennispartner.tennispartner.R;

import org.threeten.bp.LocalDate;
import org.threeten.bp.Period;
import org.threeten.bp.format.DateTimeFormatter;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class Helper {

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    public static int calculateAge(String birthDate) {
        if (birthDate != null) {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("M/d/yyyy");
            LocalDate dt = LocalDate.parse(birthDate, dtf);
            LocalDate currentDate = LocalDate.now();
            return Period.between(dt, currentDate).getYears();
        } else {
            return 0;
        }
    }

    public static String getCity(double lat, double lon, Context context) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocation(lat, lon, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (addresses != null && addresses.size() != 0) {
            Address address = addresses.get(0);
            return address.getLocality() + ", " + address.getCountryName();
        }
        return context.getString(R.string.location_not_available);
    }

    public static boolean checkLocationPermission(Context context) {
        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) context,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the currentUser *asynchronously* -- don't block
                // this thread waiting for the currentUser's response! After the currentUser
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(context)
                        .setTitle(R.string.location_dialog_title)
                        .setMessage(R.string.location_dialog_description)
                        .setPositiveButton(R.string.location_dialog_allow, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the currentUser once explanation has been shown
                                ActivityCompat.requestPermissions((Activity) context,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions((Activity) context,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    public static void showProgress(boolean show, View hideView, View showView, Context context) {
        int shortAnimTime = context.getResources().getInteger(android.R.integer.config_shortAnimTime);

        hideView.setVisibility(show ? View.GONE : View.VISIBLE);
        hideView.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                hideView.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        showView.setVisibility(show ? View.VISIBLE : View.GONE);
        showView.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                showView.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }

    public static boolean isGooglePlayServicesAvailable(Activity activity) {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int status = googleApiAvailability.isGooglePlayServicesAvailable(activity);
        if (status != ConnectionResult.SUCCESS) {
            if (googleApiAvailability.isUserResolvableError(status)) {
                googleApiAvailability.getErrorDialog(activity, status, 2404).show();
            }
            return false;
        }
        return true;
    }

}
