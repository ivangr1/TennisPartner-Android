package app.tennispartner.tenispartner.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import com.bumptech.glide.Glide;
import app.tennispartner.tenispartner.R;
import app.tennispartner.tenispartner.UserDetailActivity;
import com.sendbird.android.User;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

/**
 * A simple adapter that displays a list of Users.
 */
public class UserListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private List<User> mUsers;
    private String mChannelUrl;
    private boolean mIsGroupChannel;


    public UserListAdapter(Context context, String channelUrl, boolean isGroupChannel) {
        mContext = context;
        mUsers = new ArrayList<>();
        mChannelUrl = channelUrl;
        mIsGroupChannel = isGroupChannel;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_user, parent, false);
        return new UserHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((UserHolder) holder).bind(mContext, (UserHolder) holder, mUsers.get(position));
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public void setUserList(List<? extends User> users) {
        mUsers.clear();
        mUsers.addAll(users);
        notifyDataSetChanged();
    }

    public void addLast(User user) {
        mUsers.add(user);
        notifyDataSetChanged();
    }

    private class UserHolder extends RecyclerView.ViewHolder {
        private CardView materialCard;
        private View view;
        private TextView nameText;
        private ImageView profileImage;
        private ImageView blockedImage;
        private RelativeLayout relativeLayoutBlock;
        private TextView textViewBlocked;

        UserHolder(View itemView) {
            super(itemView);

            view = itemView;
            nameText = itemView.findViewById(R.id.text_user_list_nickname);
            profileImage = itemView.findViewById(R.id.image_user_list_profile);
            materialCard = itemView.findViewById(R.id.material_card_user_chat);
        }

        private void bind(final Context context, final UserHolder holder, final User user) {
            nameText.setText(user.getNickname());
            Glide.with(context)
                    .load(user.getProfileUrl())
                    .into(profileImage);

            final View.OnClickListener mOnClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, UserDetailActivity.class);
                    intent.putExtra("userId", user.getUserId());

                    context.startActivity(intent);
                }
            };
            materialCard.setOnClickListener(mOnClickListener);

            /*if (mIsGroupChannel) {
                if (SendBird.getCurrentUser().getUserId().equals(user.getUserId())) {
                    relativeLayoutBlock.setVisibility(View.GONE);
                    textViewBlocked.setVisibility(View.GONE);
                } else {
                    relativeLayoutBlock.setVisibility(View.VISIBLE);

                    holder.view.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                        }
                    });
                }

                final boolean isBlockedByMe = ((Member) user).isBlockedByMe();
                if (isBlockedByMe) {
                    blockedImage.setVisibility(View.VISIBLE);
                    textViewBlocked.setVisibility(View.VISIBLE);
                } else {
                    blockedImage.setVisibility(View.GONE);
                    textViewBlocked.setVisibility(View.GONE);
                }
            } else {
                blockedImage.setVisibility(View.GONE);
                relativeLayoutBlock.setVisibility(View.GONE);
            }*/
        }
    }
}

