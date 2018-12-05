package com.ivangr.tennispartner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.ivangr.tennispartner.adapters.GroupChannelListAdapter;
import com.ivangr.tennispartner.helper.ConnectionManager;
import com.ivangr.tennispartner.helper.Helper;
import com.sendbird.android.BaseChannel;
import com.sendbird.android.BaseMessage;
import com.sendbird.android.GroupChannel;
import com.sendbird.android.GroupChannelListQuery;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;

import java.text.CollationElementIterator;
import java.util.Arrays;
import java.util.List;

public class ChatListActivity extends AppCompatActivity {

    private static final int CHANNEL_LIST_LIMIT = 15;

    private static final String CONNECTION_HANDLER_ID = "CONNECTION_HANDLER_GROUP_CHANNEL_LIST";
    private static final String CHANNEL_HANDLER_ID = "CHANNEL_HANDLER_GROUP_CHANNEL_LIST";

    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefresh;
    private GroupChannelListAdapter mChannelListAdapter;
    private LinearLayoutManager mLayoutManager;
    private GroupChannelListQuery mChannelListQuery;
    private FirebaseUser currentUser;
    private ProgressBar mProgressBar;
    private TextView mEmptyDataText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.chat_list_title);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mRecyclerView = findViewById(R.id.recycler_group_channel_list);
        mSwipeRefresh = findViewById(R.id.swipe_layout_group_channel_list);
        mProgressBar = findViewById(R.id.inbox_progress);
        mEmptyDataText = findViewById(R.id.empty_list_view);

        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mSwipeRefresh.setRefreshing(true);
                refresh();
            }
        });

        showProgress(true);
        mChannelListAdapter = new GroupChannelListAdapter(this);
        mChannelListAdapter.load();

        setUpRecyclerView();
        setUpChannelListAdapter();
    }

    @Override
    public void onResume() {
        Log.d("LIFECYCLE", "GroupChannelListFragment onResume()");

        ConnectionManager.addConnectionManagementHandler(CONNECTION_HANDLER_ID, new ConnectionManager.ConnectionManagementHandler() {
            @Override
            public void onConnected(boolean reconnect) {
                refresh();
            }
        });

        SendBird.addChannelHandler(CHANNEL_HANDLER_ID, new SendBird.ChannelHandler() {
            @Override
            public void onMessageReceived(BaseChannel baseChannel, BaseMessage baseMessage) {
            }

            @Override
            public void onChannelChanged(BaseChannel channel) {
                mChannelListAdapter.clearMap();
                mChannelListAdapter.updateOrInsert(channel);
            }

            @Override
            public void onTypingStatusUpdated(GroupChannel channel) {
                mChannelListAdapter.notifyDataSetChanged();
            }
        });

        super.onResume();
    }

    @Override
    public void onPause() {
        mChannelListAdapter.save();

        Log.d("LIFECYCLE", "GroupChannelListFragment onPause()");

        ConnectionManager.removeConnectionManagementHandler(CONNECTION_HANDLER_ID);
        SendBird.removeChannelHandler(CHANNEL_HANDLER_ID);
        super.onPause();
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

    // Sets up recycler view
    private void setUpRecyclerView() {
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mChannelListAdapter);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        // If user scrolls to bottom of the list, loads more channels.
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (mLayoutManager.findLastVisibleItemPosition() == mChannelListAdapter.getItemCount() - 1) {
                    loadNextChannelList();
                }
            }
        });
    }

    /**
     * Loads the next channels from the current query instance.
     */
    private void loadNextChannelList() {
        mChannelListQuery.setUserIdsExactFilter(Arrays.asList(currentUser.getUid()));
        mChannelListQuery.next(new GroupChannelListQuery.GroupChannelListQueryResultHandler() {
            @Override
            public void onResult(List<GroupChannel> list, SendBirdException e) {
                if (e != null) {
                    // Error!
                    e.printStackTrace();
                    return;
                }

                for (GroupChannel channel : list) {
                    mChannelListAdapter.addLast(channel);
                }
            }
        });
    }

    // Sets up channel list adapter
    private void setUpChannelListAdapter() {
        mChannelListAdapter.setOnItemClickListener(new GroupChannelListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(GroupChannel channel) {
                enterGroupChannel(channel.getUrl());
            }
        });

        mChannelListAdapter.setOnItemLongClickListener(new GroupChannelListAdapter.OnItemLongClickListener() {
            @Override
            public void onItemLongClick(final GroupChannel channel) {
                //showChannelOptionsDialog(channel);
            }
        });
    }

    /**
     * Enters a Group Channel with a URL.
     *
     * @param channelUrl The URL of the channel to enter.
     */
    void enterGroupChannel(String channelUrl) {
        Intent intent = new Intent(this, GroupChatActivity.class);
        intent.putExtra("groupChatUrl", channelUrl);
        startActivity(intent);
    }

    private void refresh() {
        refreshChannelList(CHANNEL_LIST_LIMIT);
    }

    /**
     * Creates a new query to get the list of the user's Group Channels,
     * then replaces the existing dataset.
     *
     * @param numChannels The number of channels to load.
     */
    private void refreshChannelList(int numChannels) {
        mChannelListQuery = GroupChannel.createMyGroupChannelListQuery();
        mChannelListQuery.setLimit(numChannels);

        mChannelListQuery.next(new GroupChannelListQuery.GroupChannelListQueryResultHandler() {
            @Override
            public void onResult(List<GroupChannel> list, SendBirdException e) {
                if (e != null) {
                    // Error!
                    e.printStackTrace();
                    return;
                }
                mEmptyDataText.setVisibility((list.size() != 0) ? View.GONE : View.VISIBLE);
                mChannelListAdapter.clearMap();
                mChannelListAdapter.setGroupChannelList(list);
                showProgress(false);
            }
        });

        if (mSwipeRefresh.isRefreshing()) {
            mSwipeRefresh.setRefreshing(false);
        }
    }

    private void showProgress(boolean b) {
        Helper.showProgress(b, mSwipeRefresh, mProgressBar, this);
    }


}
