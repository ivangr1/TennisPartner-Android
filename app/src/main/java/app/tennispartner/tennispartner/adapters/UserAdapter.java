package app.tennispartner.tenispartner.adapters;

import android.content.Context;
import android.content.Intent;

import androidx.databinding.DataBindingUtil;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import app.tennispartner.tenispartner.UserDetailActivity;
import app.tennispartner.tenispartner.helper.Helper;
import app.tennispartner.tenispartner.models.User;
import app.tennispartner.tenispartner.R;
import app.tennispartner.tenispartner.databinding.UserListItemBinding;
import com.jakewharton.threetenabp.AndroidThreeTen;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private List<User> users;
    private Context context;

    private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            User user = (User) view.getTag();
            Intent intent = new Intent(context, UserDetailActivity.class);
            intent.putExtra("userId", user.getId());
            intent.putExtra("chatButton", true);

            context.startActivity(intent);
        }
    };

    public UserAdapter(List<User> users, Context context) {
        setHasStableIds(true);
        this.users = users;
        this.context = context;
    }

    @NonNull
    @Override
    public UserAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        AndroidThreeTen.init(context);

        UserListItemBinding binding = DataBindingUtil
                .inflate(LayoutInflater.from(viewGroup.getContext()), R.layout.user_list_item, viewGroup, false);
        return new ViewHolder(binding);

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        User user = users.get(i);
        viewHolder.userListItemBinding.setUser(user);
        viewHolder.userListItemBinding.age.setText(String.valueOf(Helper.calculateAge(user.getBirthday())));
        viewHolder.userListItemBinding.lastLogin.setText(String.format(context.getString(R.string.active), Helper.calculateDifference(user.getLastLogin(), context)));

        Glide.with(context)
                .load(user.getAvatarUrl())
                .apply(RequestOptions.circleCropTransform())
                .apply(RequestOptions.placeholderOf(R.drawable.com_facebook_profile_picture_blank_square))
                .into(viewHolder.userListItemBinding.avatar);

        viewHolder.itemView.setTag(user);
        viewHolder.userListItemBinding.materialCardUser.setOnClickListener(mOnClickListener);
    }

    @Override
    public int getItemCount() {
        if (users == null)
            return 0;
        return users.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public UserListItemBinding userListItemBinding;

        public ViewHolder(UserListItemBinding userLayoutBinding) {
            super(userLayoutBinding.getRoot());
            userListItemBinding = userLayoutBinding;
        }
    }
}
