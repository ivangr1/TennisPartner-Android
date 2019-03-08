package app.tennispartner.tenispartner.helper;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import app.tennispartner.tenispartner.MainActivity;
import app.tennispartner.tenispartner.R;
import com.vansuita.materialabout.builder.AboutBuilder;
import com.vansuita.materialabout.views.AboutView;

public class AboutDialog extends DialogFragment {
    private MainActivity mainActivity;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);

        LinearLayout linearLayout = new LinearLayout(mainActivity);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        AboutView view = AboutBuilder.with(mainActivity)
                .setAppIcon(R.drawable.ic_launcher_round)
                .setAppName(R.string.app_name)
                .setVersionNameAsAppSubTitle()
                .setShowAsCard(false)
                .setLinksAnimated(true)
                .build();

        linearLayout.addView(view);

        TextView attributions = new TextView(mainActivity);
        attributions.setText(R.string.icon_attribution);
        attributions.setGravity(Gravity.CENTER_HORIZONTAL);
        attributions.setMovementMethod(LinkMovementMethod.getInstance());

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        int margin = 70;
        params.setMargins(margin, margin, margin, margin);
        attributions.setLayoutParams(params);

        linearLayout.addView(attributions);

        builder.setTitle(R.string.about_app)
                .setView(linearLayout);

        return builder.create();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivity = (MainActivity) context;
    }
}
