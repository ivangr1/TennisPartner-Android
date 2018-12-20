package app.tennispartner.tennispartner;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.Gravity;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.vansuita.materialabout.builder.AboutBuilder;
import com.vansuita.materialabout.views.AboutView;

public class InfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        AboutView view = AboutBuilder.with(this)
                .setPhoto(R.mipmap.profile_picture)
                .setCover(R.mipmap.profile_cover)
                .setName("Ivan Grubišić")
                .setSubTitle(R.string.app_developer)
                .setPhoto(R.drawable.profile_image)
                .setAppIcon(R.drawable.ic_launcher_round)
                .setAppName(R.string.app_name)
                .setVersionNameAsAppSubTitle()
                .setBrief(R.string.about_developer)
                .addWebsiteLink("https://about.me/ivan.grubisic")
                .addGitHubLink("ivangr1")
                .addLinkedInLink("in/ivangr1")
                .addInstagramLink("grubi.ivan")
                .addLink(R.drawable.whatsapp, "WhatsApp", "https://wa.me/385989689470")
                .setShowAsCard(false)
                .setLinksAnimated(true)
                .build();

        linearLayout.addView(view);

        TextView attributions = new TextView(this);
        attributions.setText(R.string.icon_attribution);
        attributions.setGravity(Gravity.CENTER_HORIZONTAL);
        attributions.setMovementMethod(LinkMovementMethod.getInstance());

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(30, 10, 30, 10);
        attributions.setLayoutParams(params);

        linearLayout.addView(attributions);

        setContentView(linearLayout);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setTitle(R.string.about_app);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
