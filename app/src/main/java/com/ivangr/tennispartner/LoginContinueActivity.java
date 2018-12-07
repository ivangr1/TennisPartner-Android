package com.ivangr.tennispartner;

import android.app.DatePickerDialog;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.ivangr.tennispartner.helper.Helper;
import com.jakewharton.threetenabp.AndroidThreeTen;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;
import com.vansuita.pickimage.bean.PickResult;
import com.vansuita.pickimage.bundle.PickSetup;
import com.vansuita.pickimage.dialog.PickImageDialog;
import com.vansuita.pickimage.listeners.IPickResult;

import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeFormatter;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * A login screen that offers login via email/password.
 */
public class LoginContinueActivity extends AppCompatActivity implements IPickResult {

    private final String TAG = LoginContinueActivity.class.getSimpleName();

    // UI references.
    private View mProgressView;
    private View mLoginFormView;
    private ImageView imageView;
    private AutoCompleteTextView mFullName;
    private AutoCompleteTextView mBirthdayText;
    private RadioGroup mRadioGroup;
    private RadioButton lastRadioBtn;
    private String avatarPath = null;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseUser currentUser;
    private FirebaseFirestore firestore;
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private LocalDate mBirthday;
    private ProgressBar mUploadProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_login);

        AndroidThreeTen.init(this);
        mBirthday = LocalDate.now();

        firestore = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build();
        firestore.setFirestoreSettings(settings);

      /*  // Set up the login form.
        mUploadProgress = findViewById(R.id.upload_progress);
        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.loading_view);
        mFullName = findViewById(R.id.textInputName);
        mBirthdayText = findViewById(R.id.textInputDate);
        mRadioGroup = findViewById(R.id.gender);
        lastRadioBtn = findViewById(R.id.gender_female);
        imageView = findViewById(R.id.avatar_login);

        currentUser = mAuth.getCurrentUser();

        // Show loading bar instead of circular
        findViewById(R.id.progress_loading).setVisibility(View.GONE);
        mUploadProgress.setVisibility(View.VISIBLE);

        // Regular login button
        Button loginButton = findViewById(R.id.login_button);
        loginButton.setOnClickListener((view) -> {
            login();
        });*/

        // Date picker
        DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                mBirthday = LocalDate.of(year, monthOfYear + 1, dayOfMonth);
                DateTimeFormatter sdf = DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.getDefault());
                mBirthdayText.setText(sdf.format(mBirthday));
            }

        };

        mBirthdayText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog picker = new DatePickerDialog(LoginContinueActivity.this, date, mBirthday.getYear(), mBirthday.getMonthValue(),
                        mBirthday.getDayOfMonth());
                picker.getDatePicker().setMaxDate(System.currentTimeMillis());
                picker.show();
            }
        });
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void login() {

        // Reset errors.
        mFullName.setError(null);
        mBirthdayText.setError(null);
        lastRadioBtn.setError(null);

        // Store values at the time of the login attempt.
        String fullName_text = mFullName.getText().toString();
        String date_text = mBirthdayText.getText().toString();
        int selectedId = mRadioGroup.getCheckedRadioButtonId();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid email address.
        if (TextUtils.isEmpty(fullName_text) || !isFullName(fullName_text)) {
            mFullName.setError(getString(R.string.error_wrong_data));
            focusView = mFullName;
            cancel = true;
        } else if (TextUtils.isEmpty(date_text)) {
            mBirthdayText.setError(getString(R.string.error_field_required));
            focusView = mBirthdayText;
            cancel = true;
        } else if (selectedId <= 0) {
            lastRadioBtn.setError(getString(R.string.error_field_required));
            focusView = lastRadioBtn;
            cancel = true;
        } else if (avatarPath == null) {
            Toast.makeText(this, "Image not selected", Toast.LENGTH_SHORT).show();
        } else {
            showProgress(true);

            String[] fullName = fullName_text.split(" ");
            RadioButton mRadioButton = findViewById(selectedId);
            String gender = mRadioButton.getTag().toString();
            DateTimeFormatter sdf = DateTimeFormatter.ofPattern("MM/dd/yyyy", Locale.getDefault());
            String birthday = sdf.format(mBirthday);

            Map<String, Object> user = new HashMap<>();

            user.put("firstName", fullName[0]);
            user.put("lastName", fullName[1]);
            user.put("gender", gender);
            user.put("birthday", birthday);
            user.put("phoneNumber", currentUser.getPhoneNumber());

            // If the currentUser uploaded an avatar, upload it to Amazon S3
            if (avatarPath != null) {
                Uri file = Uri.fromFile(new File(avatarPath));
                StorageReference storageRef = storage.getReference();
                StorageReference avatarsRef = storageRef.child("images/avatars/" + currentUser.getUid() + "_" + file.getLastPathSegment());
                UploadTask uploadTask = avatarsRef.putFile(file);

                uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            mUploadProgress.setProgress((int) progress, true);
                        } else {
                            mUploadProgress.setProgress((int) progress);
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        showProgress(false);
                        Toast.makeText(LoginContinueActivity.this, "Error uploading image!", Toast.LENGTH_SHORT).show();
                    }
                });

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
                                                    .setDisplayName(fullName_text)
                                                    .setPhotoUri(downloadUri)
                                                    .build();
                                            currentUser.updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    SendBird.updateCurrentUserInfo(fullName[0], downloadUri.toString(), new SendBird.UserInfoUpdateHandler() {
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
            }

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
        for (int i = 0; i < fullName.length; i++) {
            if (fullName[i] != null) {
                elementsWithText++;
            }
        }

        return elementsWithText >= 2;
    }

    /**
     * Gets the currentUser image from the camera or from the gallery
     */
    public void getImage(View view) {
        PickSetup setup = new PickSetup()
                .setTitle(getResources().getString(R.string.camera_dialog_title))
                .setCancelText(getResources().getString(R.string.camera_dialog_cancel))
                .setCameraButtonText(getResources().getString(R.string.camera_dialog_camera))
                .setGalleryButtonText(getResources().getString(R.string.camera_dialog_gallery));
        //
        //PickImageDialog.build(setup).show(this);
    }

    @Override
    public void onPickResult(PickResult r) {
        if (r.getError() == null) {

            //Setting the real returned image.
            imageView.setImageURI(r.getUri());

            //If you want the Bitmap.
            //imageView.setImageBitmap(r.getBitmap());

            //Image path
            avatarPath = r.getPath();
        } else {
            //Handle possible errors
            Toast.makeText(this, r.getError().getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void showProgress(boolean b) {
        Helper.showProgress(b, mLoginFormView, mProgressView, this);
    }
}

