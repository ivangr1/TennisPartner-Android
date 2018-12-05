package com.ivangr.tennispartner.helper;

import android.os.Bundle;

import android.view.MenuItem;

import com.ivangr.tennispartner.GroupChatActivity;
import com.ivangr.tennispartner.R;
import com.ivangr.tennispartner.adapters.UserListAdapter;
import com.sendbird.android.GroupChannel;
import com.sendbird.android.Member;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


public class MemberListActivity extends AppCompatActivity {

    private static final String CONNECTION_HANDLER_ID = "CONNECTION_HANDLER_MEMBER_LIST";
    public static final String EXTRA_CHANNEL_URL = "EXTRA_CHANNEL_URL";
    public static final String EXTRA_USER_ID = "EXTRA_USER_ID";
    public static final String EXTRA_USER_PROFILE_URL = "EXTRA_USER_PROFILE_URL";
    public static final String EXTRA_USER_NICKNAME = "EXTRA_USER_NICKNAME";
    public static final String EXTRA_USER_BLOCKED_BY_ME = "EXTRA_USER_BLOCKED_BY_ME";

    private UserListAdapter mListAdapter;
    private RecyclerView mRecyclerView;
    private GridLayoutManager mLayoutManager;
    private String mChannelUrl;
    private GroupChannel mChannel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_member_list);

        mChannelUrl = getIntent().getStringExtra(GroupChatActivity.EXTRA_CHANNEL_URL);
        mRecyclerView = findViewById(R.id.recycler_member_list);
        mListAdapter = new UserListAdapter(this, mChannelUrl, true);

        Toolbar toolbar = findViewById(R.id.toolbar_member_list);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        setUpRecyclerView();
    }

    @Override
    protected void onResume() {
        super.onResume();

        ConnectionManager.addConnectionManagementHandler(CONNECTION_HANDLER_ID, new ConnectionManager.ConnectionManagementHandler() {
            @Override
            public void onConnected(boolean reconnect) {
                getChannelFromUrl(mChannelUrl);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();

        ConnectionManager.removeConnectionManagementHandler(CONNECTION_HANDLER_ID);
    }

    private void setUpRecyclerView() {
        mLayoutManager = new GridLayoutManager(this, 3);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mListAdapter);
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

    private void getChannelFromUrl(String url) {
        GroupChannel.getChannel(url, new GroupChannel.GroupChannelGetHandler() {
            @Override
            public void onResult(GroupChannel groupChannel, SendBirdException e) {
                if (e != null) {
                    // Error!
                    return;
                }

                mChannel = groupChannel;

                refreshChannel();
            }
        });
    }

    private void refreshChannel() {
        mChannel.refresh(new GroupChannel.GroupChannelRefreshHandler() {
            @Override
            public void onResult(SendBirdException e) {
                if (e != null) {
                    // Error!
                    return;
                }

                setMemberList(mChannel.getMembers());
            }
        });
    }

    private void setMemberList(List<Member> memberList) {
        List<Member> sortedUserList = new ArrayList<>();
        for (Member me : memberList) {
            if (me.getUserId().equals(SendBird.getCurrentUser().getUserId())) {
                sortedUserList.add(me);
                break;
            }
        }
        for (Member other : memberList) {
            if (other.getUserId().equals(SendBird.getCurrentUser().getUserId())) {
                continue;
            }
            sortedUserList.add(other);
        }

        mListAdapter.setUserList(sortedUserList);
    }
}
