package app.tennispartner.tenispartner.helper;

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

import androidx.core.app.ShareCompat;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import app.tennispartner.tenispartner.R;

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

    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;

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

                // No explanation needed, we can request the permission.
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
                        .setCancelable(false)
                        .create()
                        .show();
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

    public static void inviteFriends(Activity activity) {
        ShareCompat.IntentBuilder.from(activity)
                .setChooserTitle(R.string.invitation_title)
                .setText(activity.getString(R.string.invitation_message))
                .setType("text/plain")
                .startChooser();
    }

    public static String calculateDifference(long time, Context context) {
            if (time < 1000000000000L) {
                // if timestamp given in seconds, convert to millis
                time *= 1000;
            }

            long now = System.currentTimeMillis();
            if (time > now || time <= 0) {
                return null;
            }

            final long diff = now - time;
            if (diff < MINUTE_MILLIS) {
                return context.getString(R.string.just_now);
            } else if (diff < 2 * MINUTE_MILLIS) {
                return context.getString(R.string.minute_ago);
            } else if (diff < 50 * MINUTE_MILLIS) {
                return diff / MINUTE_MILLIS + " " + context.getString(R.string.minutes_ago);
            } else if (diff < 90 * MINUTE_MILLIS) {
                return context.getString(R.string.hour_ago);
            } else if (diff < 24 * HOUR_MILLIS) {
                return diff / HOUR_MILLIS + " " + context.getString(R.string.hours_ago);
            } else if (diff < 48 * HOUR_MILLIS) {
                return context.getString(R.string.yesterday);
            } else {
                return diff / DAY_MILLIS + " " + context.getString(R.string.days_ago);
            }
    }
}
