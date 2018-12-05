package com.ivangr.tennispartner.adapters;

import android.content.Context;

import androidx.databinding.DataBindingUtil;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.ivangr.tennispartner.R;
import com.ivangr.tennispartner.databinding.GameListItemBinding;
import com.ivangr.tennispartner.models.Game;
import com.ivangr.tennispartner.models.User;

import org.threeten.bp.Instant;
import org.threeten.bp.ZoneId;
import org.threeten.bp.format.DateTimeFormatter;

import java.util.List;
import java.util.Locale;

public class GameAdapter extends RecyclerView.Adapter<GameAdapter.ViewHolder> {

    private List<Game> games;
    private Context context;

    public GameAdapter(List<Game> games, Context context) {
        this.games = games;
        this.context = context;
    }

    @NonNull
    @Override
    public GameAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        GameListItemBinding binding = DataBindingUtil
                .inflate(LayoutInflater.from(viewGroup.getContext()), R.layout.game_list_item, viewGroup, false);
        return new ViewHolder(binding);

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        Game game = games.get(i);
        DateTimeFormatter formatter_date = DateTimeFormatter.ofPattern("dd.MM.yyyy")
                .withLocale(Locale.getDefault())
                .withZone(ZoneId.systemDefault());

        final Instant start_time = Instant.ofEpochSecond(game.getTime());

        String date = formatter_date.format(start_time);

        viewHolder.gameListItemBinding.setGame(game);
        viewHolder.gameListItemBinding.gameDate.setText(date);
        LinearLayout image_holder = viewHolder.gameListItemBinding.gameUsers;


        List<User> users = null;
        if (users != null) {
            for (User user : users) {
                ImageView avatar = new ImageView(context);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT);

                params.height = (int) context.getResources().getDimension(R.dimen.avatar_game_list_size);
                params.width = (int) context.getResources().getDimension(R.dimen.avatar_game_list_size);

                if (users.size() == 2) {
                    int single_margin = (int) context.getResources().getDimension(R.dimen.avatar_game_list_single_margin);
                    params.setMargins(single_margin, 0, single_margin, 0);
                }
                avatar.setLayoutParams(params);

            }
        }
    }

    @Override
    public int getItemCount() {
        if (games == null)
            return 0;
        return games.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public GameListItemBinding gameListItemBinding;

        public ViewHolder(GameListItemBinding gameLayoutBinding) {
            super(gameLayoutBinding.getRoot());
            gameListItemBinding = gameLayoutBinding;
        }
    }
}

