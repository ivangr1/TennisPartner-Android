package app.tennispartner.tennispartner;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.MenuItem;

import com.vansuita.materialabout.builder.AboutBuilder;
import com.vansuita.materialabout.views.AboutView;

public class InfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

        setContentView(view);
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
