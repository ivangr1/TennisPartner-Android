package app.tennispartner.tenispartner.helper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.people.v1.PeopleService;
import com.google.api.services.people.v1.model.Birthday;
import com.google.api.services.people.v1.model.Date;
import com.google.api.services.people.v1.model.Gender;
import com.google.api.services.people.v1.model.Person;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import app.tennispartner.tenispartner.BuildConfig;
import app.tennispartner.tenispartner.LoginActivity;
import app.tennispartner.tenispartner.R;
import app.tennispartner.tenispartner.models.User;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;

import static android.app.Activity.RESULT_OK;

public class Login {
    public static final int RC_SIGN_IN = 97;
    public static final int RC_SIGN_IN_CONTINUE = 90;

    private static FirebaseUser currentUser;
    private static List<String> googleScope;
    private static final String TAG = "LoginHelper";

    private static List<AuthUI.IdpConfig> loginProviderList() {
        List<String> facebookScope = Arrays.asList("user_birthday", "user_gender");
        googleScope = Arrays.asList(
                "profile",
                "https://www.googleapis.com/auth/user.birthday.read"
        );
        return Arrays.asList(
                new AuthUI.IdpConfig.FacebookBuilder().setPermissions(facebookScope).build(),
                new AuthUI.IdpConfig.GoogleBuilder().setScopes(googleScope).build(),
                new AuthUI.IdpConfig.PhoneBuilder().build());
    }

    public static void login(Context context) {
        if (Helper.isGooglePlayServicesAvailable((Activity) context))
            ((Activity) context).startActivityForResult(AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setTosAndPrivacyPolicyUrls(
                            "https://tennispartner.app/tos.html",
                            "https://tennispartner.app/privacy-policy.html")
                    .setLogo(R.drawable.ic_user)
                    .setAvailableProviders(loginProviderList())
                    .setIsSmartLockEnabled(!BuildConfig.DEBUG /* credentials */, true /* hints */)
                    .build(), RC_SIGN_IN);
    }

    public static void loginResult(Context context, Class<?> redirectActivity, int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully signed in
                FirebaseFirestore firestore = FirebaseFirestore.getInstance();
                currentUser = FirebaseAuth.getInstance().getCurrentUser();
                if (currentUser != null) {

                    SendBird.connect(currentUser.getUid(), new SendBird.ConnectHandler() {
                        @Override
                        public void onConnected(com.sendbird.android.User user, SendBirdException e) {
                            if (e != null) {    // Error.
                                return;
                            }

                            FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                                @Override
                                public void onComplete(@NonNull Task<InstanceIdResult> task) {
                                    String token = task.getResult().getToken();
                                    if (token == null) return;

                                    SendBird.registerPushTokenForCurrentUser(FirebaseInstanceId.getInstance().getToken(),
                                            new SendBird.RegisterPushTokenWithStatusHandler() {
                                                @Override
                                                public void onRegistered(SendBird.PushTokenRegistrationStatus status, SendBirdException e) {
                                                }
                                            });
                                }
                            });
                        }
                    });

                    DocumentReference userRef = firestore.collection("users").document(currentUser.getUid());
                    userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> getUser) {
                            if (getUser.isSuccessful()) {
                                DocumentSnapshot userDoc = getUser.getResult();
                                // Check if the user is new
                                if (!userDoc.exists()) {
                                    // If user signed with phone, redirect him
                                    if (currentUser.getPhoneNumber() != null) {
                                        ((Activity) context).startActivityForResult(new Intent(context, LoginActivity.class), RC_SIGN_IN_CONTINUE);
                                    } else {
                                        // Create a new user with a first and last name
                                        Map<String, Object> user = new HashMap<>();

                                        String[] fullName = currentUser.getDisplayName().split(" ");
                                        user.put("firstName", fullName[0]);
                                        user.put("lastName", fullName[1]);
                                        user.put("lastLogin", currentUser.getMetadata().getLastSignInTimestamp());

                                        for (UserInfo profile : currentUser.getProviderData()) {
                                            // Id of the provider
                                            String provider = profile.getProviderId();
                                            // UID specific to the provider
                                            String providerId = profile.getUid();

                                            /*
                                              FACEBOOK LOGIN ADDITIONAL INFO
                                             */
                                            if (provider.equals("facebook.com")) {

                                                // Graph API for birthday and gender
                                                final AccessToken accessToken = AccessToken.getCurrentAccessToken();
                                                if (accessToken != null) {
                                                    GraphRequest request = GraphRequest.newMeRequest(
                                                            accessToken,
                                                            new GraphRequest.GraphJSONObjectCallback() {
                                                                @Override
                                                                public void onCompleted(JSONObject object, GraphResponse response) {
                                                                    // Application code
                                                                    try {
                                                                        user.put("birthday", object.getString("birthday"));
                                                                        user.put("gender", object.getString("gender"));
                                                                        user.put("provider", provider);
                                                                        user.put("providerId", providerId);
                                                                        // Create new user
                                                                        firestore.collection("users")
                                                                                .document(currentUser.getUid())
                                                                                .set(user)
                                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                    @Override
                                                                                    public void onSuccess(Void aVoid) {
                                                                                        final String avatarUrl = String.format(User.FB_AVATAR, providerId);
                                                                                        // Update current user object
                                                                                        SendBird.updateCurrentUserInfo(fullName[0], avatarUrl, new SendBird.UserInfoUpdateHandler() {
                                                                                            @Override
                                                                                            public void onUpdated(SendBirdException e) {
                                                                                                if (e != null) {    // Error.
                                                                                                    return;
                                                                                                }
                                                                                                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                                                                                        .setPhotoUri(Uri.parse(avatarUrl))
                                                                                                        .build();
                                                                                                currentUser.updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                    @Override
                                                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                                                        if (task.isSuccessful()) {
                                                                                                            Log.d(TAG, "User profile updated.");
                                                                                                            ((Activity) context).recreate();
                                                                                                        }
                                                                                                    }
                                                                                                });
                                                                                            }
                                                                                        });
                                                                                    }
                                                                                })
                                                                                .addOnFailureListener(new OnFailureListener() {
                                                                                    @Override
                                                                                    public void onFailure(@NonNull Exception e) {
                                                                                        Log.w(context.getClass().getSimpleName(), "Error writing document", e);
                                                                                    }
                                                                                });
                                                                    } catch (JSONException e) {
                                                                        e.printStackTrace();
                                                                    }
                                                                }
                                                            });
                                                    Bundle parameters = new Bundle();
                                                    parameters.putString("fields", "gender,birthday");
                                                    request.setParameters(parameters);
                                                    request.executeAsync();
                                                }
                                            }

                                            /*
                                              GOOGLE LOGIN ADDITIONAL INFO
                                             */

                                            else if (provider.equals("google.com")) {
                                                // Google API for birthday and gender
                                                GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(context);
                                                /** Global instance of the HTTP transport. */
                                                HttpTransport HTTP_TRANSPORT = AndroidHttp.newCompatibleTransport();
                                                /** Global instance of the JSON factory. */
                                                JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
                                                AsyncTask<Void, Void, Person> task = new AsyncTask<Void, Void, Person>() {
                                                    @Override
                                                    protected Person doInBackground(Void... params) {
                                                        Person person = null;
                                                        try {
                                                            GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(context, googleScope);
                                                            if (account != null) {
                                                                credential.setSelectedAccount(account.getAccount());
                                                            }
                                                            PeopleService service = new PeopleService.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential).build();
                                                            person = service.people().get("people/me").setPersonFields("birthdays,genders").execute();
                                                        } catch (IOException e) {
                                                            // Other non-recoverable exceptions.
                                                            e.printStackTrace();
                                                        }
                                                        return person;
                                                    }

                                                    @Override
                                                    protected void onPostExecute(Person person) {
                                                        if (person != null) {
                                                            List<Gender> genders = person.getGenders();
                                                            if (genders != null && genders.size() > 0) {
                                                                user.put("gender", genders.get(0).getValue());
                                                            } else {
                                                                user.put("gender", "-");
                                                            }
                                                            List<Birthday> birthdays = person.getBirthdays();
                                                            if (birthdays != null && birthdays.size() > 0) {
                                                                Date b = birthdays.get(0).getDate();
                                                                String birthday = b.getMonth() + "/" + b.getDay() + "/" + b.getYear();
                                                                user.put("birthday", birthday);
                                                            }
                                                            final String avatarUrl = currentUser
                                                                    .getPhotoUrl()
                                                                    .toString().replaceAll("[swh][0-9]{1,5}-[cp]", "s300-c");
                                                            user.put("provider", provider);
                                                            // UID specific to the provider
                                                            user.put("providerId", providerId);
                                                            user.put("avatarUrl", avatarUrl);
                                                            firestore.collection("users").document(currentUser.getUid()).set(user)
                                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                        @Override
                                                                        public void onSuccess(Void aVoid) {
                                                                            SendBird.updateCurrentUserInfo(fullName[0], avatarUrl, new SendBird.UserInfoUpdateHandler() {
                                                                                @Override
                                                                                public void onUpdated(SendBirdException e) {
                                                                                    if (e != null) {    // Error.
                                                                                        return;
                                                                                    }
                                                                                    ((Activity) context).recreate();
                                                                                }
                                                                            });
                                                                        }
                                                                    })
                                                                    .addOnFailureListener(new OnFailureListener() {
                                                                        @Override
                                                                        public void onFailure(@NonNull Exception e) {
                                                                            Log.w(context.getClass().getSimpleName(), "Error writing document", e);
                                                                        }
                                                                    });
                                                        }
                                                    }
                                                };
                                                task.execute();
                                            }
                                        }
                                    }
                                } else {
                                    ((Activity) context).recreate();
                                }
                            } else {
                                Log.d(TAG, "get failed with ", getUser.getException());
                            }
                        }
                    });
                }
            } else {
                // Sign in failed
                if (response == null) {
                    // User pressed back button
                    return;
                }

                if (response.getError().getErrorCode() == ErrorCodes.NO_NETWORK) {
                    return;
                }

                Log.e(context.getClass().getSimpleName(), "Sign-in error: ", response.getError());
            }
        } else if (requestCode == RC_SIGN_IN_CONTINUE) {
            if (resultCode == RESULT_OK) {
                ((Activity) context).recreate();
            }
        }
    }

    public static boolean isPhoneUserWithoutName() {
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        boolean isPhoneUser = false;
        if(currentUser != null)
        isPhoneUser = currentUser.getPhoneNumber() != null && !currentUser.getPhoneNumber().isEmpty()
                && (currentUser.getDisplayName() == null || currentUser.getDisplayName().isEmpty())
                && currentUser.getPhotoUrl() == null;
        return isPhoneUser;
    }
}
