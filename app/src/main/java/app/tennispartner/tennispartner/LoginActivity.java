package app.tennispartner.tennispartner;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;

import com.esafirm.imagepicker.features.ImagePicker;
import com.esafirm.imagepicker.features.ReturnMode;
import com.esafirm.imagepicker.model.Image;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.jakewharton.threetenabp.AndroidThreeTen;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeFormatter;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private EditText mFullNameView;
    private EditText mBirthdayView;
    private View mProgressView;
    private View mLoginFormView;
    private LocalDate mBirthday;
    private FirebaseFirestore firestore;
    private RadioGroup mGenderView;
    private RadioButton lastRadioBtn;
    private ImageView mLoginAvatar;
    private String avatarPath;
    private FirebaseUser currentUser;
    private TextInputLayout mBirthdayInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        AndroidThreeTen.init(this);
        firestore = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build();
        firestore.setFirestoreSettings(settings);

        Toolbar toolbar = findViewById(R.id.toolbar_login);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.title_activity_login);
        }

        // Set up the login form.
        mLoginAvatar = (ImageView) findViewById(R.id.login_avatar);
        avatarPath = null;

        mFullNameView = (EditText) findViewById(R.id.login_full_name);

        mBirthdayView = (EditText) findViewById(R.id.login_birthday);
        // Date picker
        DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                mBirthday = LocalDate.of(year, monthOfYear + 1, dayOfMonth);
                DateTimeFormatter sdf = DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.getDefault());
                mBirthdayView.setText(sdf.format(mBirthday));
            }

        };
        mBirthdayView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBirthdayView.setRawInputType(InputType.TYPE_CLASS_TEXT);
                mBirthdayView.setTextIsSelectable(true);
                mBirthday = LocalDate.now();
                DatePickerDialog picker = new DatePickerDialog(LoginActivity.this, date, mBirthday.getYear(), mBirthday.getMonthValue(),
                        mBirthday.getDayOfMonth());
                picker.getDatePicker().setMaxDate(System.currentTimeMillis());
                picker.show();
            }
        });

        mGenderView = (RadioGroup) findViewById(R.id.login_gender);
        lastRadioBtn = findViewById(R.id.gender_female);

        Button mPhoneSignInButton = (Button) findViewById(R.id.sign_in_button);
        mPhoneSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (ImagePicker.shouldHandle(requestCode, resultCode, data)) {
            Image image = ImagePicker.getFirstImageOrNull(data);
            mLoginAvatar.setImageURI(Uri.parse(image.getPath()));
            avatarPath = image.getPath();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mFullNameView.setError(null);
        mBirthdayView.setError(null);

        // Store values at the time of the login attempt.
        String fullName = mFullNameView.getText().toString();
        String birthday = mBirthdayView.getText().toString();
        int selectedId = mGenderView.getCheckedRadioButtonId();

        boolean cancel = false;
        View focusView = null;
        // Check for a valid fields.
        if (avatarPath == null) {
            Snackbar.make(mLoginFormView, R.string.error_field_required, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            getImage(mLoginAvatar);
                        }
                    });
            focusView = mLoginAvatar;
            cancel = true;
        }
        if (TextUtils.isEmpty(fullName)) {
            mFullNameView.setError(getString(R.string.error_field_required));
            focusView = mFullNameView;
            cancel = true;
        } else if (!isFullName(fullName)) {
            mFullNameView.setError(getString(R.string.error_not_full_name));
            focusView = mFullNameView;
            cancel = true;
        }
        if (TextUtils.isEmpty(birthday)) {
            mBirthdayView.setError(getString(R.string.error_field_required));
            focusView = mBirthdayView;
            cancel = true;
        }
        if (selectedId <= 0) {
            lastRadioBtn.setError(getString(R.string.error_field_required));
            focusView = mGenderView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);

            //Prepare the data
            String[] name = fullName.split(" ");
            String gender = findViewById(selectedId).getTag().toString();
            DateTimeFormatter sdf = DateTimeFormatter.ofPattern("MM/dd/yyyy", Locale.getDefault());
            String bday = sdf.format(mBirthday);

            mAuthTask = new UserLoginTask(avatarPath, name, bday, gender);
            mAuthTask.execute((Void) null);
        }
    }

    /**
     * Check is full name is entered
     */
    private boolean isFullName(String fullName_text) {
        String[] fullName = fullName_text.split(" ");
        for (int i = 0; i < fullName.length; i++) {
            if (fullName[i].trim().equals("")) {
                fullName[i] = null;
            }
        }
        int elementsWithText = 0;
        for (String aFullName : fullName) {
            if (aFullName != null) {
                elementsWithText++;
            }
        }

        return elementsWithText >= 2;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    private void showProgress(final boolean show) {
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        mProgressView.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }

    /**
     * Gets the currentUser image from the camera or from the gallery
     */
    public void getImage(View view) {
        ImagePicker.create(this)
                .returnMode(ReturnMode.ALL) // set whether pick and / or camera action should return immediate result or not.
                .toolbarImageTitle(getString(R.string.image_select_title)) // image selection title
                .includeVideo(false) // Show video on image picker
                .single() // single mode
                .start(); // start image picker activity with request code
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mAvatar;
        private final String[] mFullName;
        private final String mBirthday;
        private final String mGender;

        UserLoginTask(String avatar, String[] fullName, String birthday, String gender) {
            mAvatar = avatar;
            mFullName = fullName;
            mBirthday = birthday;
            mGender = gender;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            currentUser = FirebaseAuth.getInstance().getCurrentUser();

            Map<String, Object> user = new HashMap<>();
            user.put("firstName", mFullName[0]);
            user.put("lastName", mFullName[1]);
            user.put("gender", mGender);
            user.put("birthday", mBirthday);
            user.put("phoneNumber", currentUser.getPhoneNumber());

            Uri file = Uri.fromFile(new File(mAvatar));
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference avatarsRef = storage.getReference()
                    .child("images/avatars/" + currentUser.getUid() + "_" + file.getLastPathSegment());
            UploadTask uploadTask = avatarsRef.putFile(file);

            // Register observers to listen for when the download is done or if it fails
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    // Continue with the task to get the download URL
                    return avatarsRef.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        user.put("avatarUrl", downloadUri.toString());
                        firestore.collection("users").document(currentUser.getUid()).set(user)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                                .setDisplayName(mFullName[0] + " " + mFullName[1])
                                                .setPhotoUri(downloadUri)
                                                .build();
                                        currentUser.updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                SendBird.updateCurrentUserInfo(mFullName[0], downloadUri.toString(), new SendBird.UserInfoUpdateHandler() {
                                                    @Override
                                                    public void onUpdated(SendBirdException e) {
                                                        if (e != null) {    // Error.
                                                            return;
                                                        }
                                                        showProgress(false);
                                                        setResult(RESULT_OK, new Intent());
                                                        finish();
                                                    }
                                                });
                                            }
                                        });
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.w(getApplicationContext().getClass().getSimpleName(), "Error writing document", e);
                                    }
                                });
                    }
                }
            });
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
           /* mAuthTask = null;
            showProgress(false);

            if (success) {
                finish();
            } else {
                mBirthdayView.setError(getString(R.string.error_incorrect_password));
                mBirthdayView.requestFocus();
            }*/
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}

