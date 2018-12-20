package app.tennispartner.tennispartner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ShareCompat;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.appinvite.AppInviteInvitation;

public class ShareActivity extends AppCompatActivity {

    private static final int REQUEST_INVITE = 91;
    private static final String TAG = ShareActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);

        View someView = findViewById(R.id.share_layout);
        View root = someView.getRootView();
        root.setBackgroundColor(getResources().getColor(R.color.colorShare));

        Toolbar toolbar = (Toolbar) findViewById(R.id.default_toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_close);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: requestCode=" + requestCode + ", resultCode=" + resultCode);

        if (requestCode == REQUEST_INVITE) {
            if (resultCode == RESULT_OK) {
                // Get the invitation IDs of all sent messages
                String[] ids = AppInviteInvitation.getInvitationIds(resultCode, data);
                for (String id : ids) {
                    Log.d(TAG, "onActivityResult: sent invitation " + id);
                }
                Toast.makeText(this, R.string.invitation_sent_toast, Toast.LENGTH_SHORT).show();
            } else {
                // Sending failed or it was canceled, show failure message to the user
                // ...
            }
        }
    }

    public void inviteSMSmail(View view) {
        Intent intent = new AppInviteInvitation.IntentBuilder(getString(R.string.invitation_title))
                .setMessage(getString(R.string.invitation_message))
                /*.setDeepLink(Uri.parse(getString(R.string.invitation_deep_link)))*/
                .setCustomImage(Uri.parse(getString(R.string.invitation_custom_image)))
                .setCallToActionText(getString(R.string.invitation_cta))
                .build();
        startActivityForResult(intent, REQUEST_INVITE);
    }

    public void InviteOther(View view) {
        ShareCompat.IntentBuilder.from(this)
                .setType("text/plain")
                .setText(getString(R.string.invitation_message))
                .startChooser();
    }

    public void InviteLink(View view) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null) {
            clipboard.addPrimaryClipChangedListener(new ClipboardManager.OnPrimaryClipChangedListener() {
                @Override
                public void onPrimaryClipChanged() {
                    Toast.makeText(ShareActivity.this, R.string.share_link_copied, Toast.LENGTH_SHORT).show();
                }
            });
            ClipData clip = ClipData.newPlainText("appLink", "https://tennispartner.app/get");
            clipboard.setPrimaryClip(clip);
        }
    }
}
