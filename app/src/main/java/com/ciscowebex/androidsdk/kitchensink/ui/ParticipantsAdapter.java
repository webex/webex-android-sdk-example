package com.ciscowebex.androidsdk.kitchensink.ui;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.ciscowebex.androidsdk.kitchensink.R;
import com.ciscowebex.androidsdk.phone.CallMembership;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ParticipantsAdapter extends RecyclerView.Adapter {
    private List<CallMembershipEntity> mDataset;
    private String activeSpeakerId = null;
    private OnLetInClickListener onLetInClickListener;

    public interface OnLetInClickListener {
        void onLetInClick(CallMembershipEntity entity);
    }


    public static class CallMembershipEntity {
        private String mPersonId;
        private String mName;
        private String mAvatarUrl;
        private boolean mSendingAudio;
        private boolean mSendingVideo;
        private CallMembership.State mState;
        private boolean mIsHeader = false;

        private CallMembershipEntity() {
        }

        public CallMembershipEntity(String personId, String name, String avatarUrl, boolean sendingAudio, boolean sendingVideo, CallMembership.State state) {
            this.mPersonId = personId;
            this.mName = name;
            this.mAvatarUrl = avatarUrl;
            this.mSendingAudio = sendingAudio;
            this.mSendingVideo = sendingVideo;
            this.mState = state;
        }

        public String getPersonId() {
            return mPersonId;
        }
    }

    public void setOnLetInClickListener(OnLetInClickListener onLetInClickListener) {
        this.onLetInClickListener = onLetInClickListener;
    }

    static class MyHeaderViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.txt_header)
        TextView mHeader;

        MyHeaderViewHolder(View item) {
            super(item);
            ButterKnife.bind(this, item);
        }

        void setHeader(String header) {
            mHeader.setText(header);
        }
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.txt_name)
        TextView mName;

        @BindView(R.id.txt_active_speaker)
        TextView mActiveSpeaker;

        @BindView(R.id.img_avatar)
        ImageView mAvatar;

        @BindView(R.id.img_audio)
        ImageView mAudio;

        @BindView(R.id.img_video)
        ImageView mVideo;

        @BindView(R.id.btn_let_in)
        Button mLetIn;

        MyViewHolder(View item) {
            super(item);
            ButterKnife.bind(this, item);
        }

        void setName(String name) {
            mName.setText(name);
        }

        void setActiveSpeaker(boolean activeSpeaker) {
            mActiveSpeaker.setVisibility(activeSpeaker ? View.VISIBLE : View.GONE);
        }

        void setSendingAudio(boolean sendingAudio) {
            int res = sendingAudio ? android.R.drawable.presence_audio_online : android.R.drawable.presence_audio_away;
            mAudio.setImageResource(res);
        }

        void setSendingVideo(boolean sendingVideo) {
            int res = sendingVideo ? android.R.drawable.presence_video_online : android.R.drawable.presence_video_away;
            mVideo.setImageResource(res);
        }

        void setAvatar(String avatarUrl) {
            Picasso.with(itemView.getContext()).cancelRequest(mAvatar);
            if (avatarUrl == null || avatarUrl.isEmpty()) {
                mAvatar.setImageResource(R.drawable.google_contacts_android);
            } else {
                Picasso.with(itemView.getContext()).load(avatarUrl).fit().into(mAvatar);
            }
        }
    }

    public ParticipantsAdapter(List<CallMembershipEntity> data) {
        mDataset = data;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == 1) {
            View item = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_item_participant, parent, false);
            MyViewHolder vh = new MyViewHolder(item);
            return vh;
        } else {
            View item = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_item_participant_header, parent, false);
            MyHeaderViewHolder vh = new MyHeaderViewHolder(item);
            return vh;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        CallMembershipEntity entity = mDataset.get(position);
        if (holder instanceof MyViewHolder) {
            MyViewHolder myViewHolder = (MyViewHolder) holder;
            myViewHolder.setName(entity.mName);
            myViewHolder.setActiveSpeaker(activeSpeakerId != null && entity.mPersonId.equals(activeSpeakerId));
            myViewHolder.setSendingAudio(entity.mSendingAudio);
            myViewHolder.setSendingVideo(entity.mSendingVideo);
            myViewHolder.setAvatar(entity.mAvatarUrl);
            myViewHolder.mLetIn.setVisibility(entity.mState == CallMembership.State.INLOBBY ? View.VISIBLE : View.GONE);
            if (onLetInClickListener != null) {
                myViewHolder.mLetIn.setOnClickListener(v -> onLetInClickListener.onLetInClick(entity));
            }
        } else if (holder instanceof MyHeaderViewHolder) {
            MyHeaderViewHolder myHeaderViewHolder = (MyHeaderViewHolder) holder;
            myHeaderViewHolder.setHeader(entity.mName);
        }
    }

    @Override
    public int getItemViewType(int position) {
        CallMembershipEntity entity = mDataset.get(position);
        return entity.mIsHeader ? 0 : 1;
    }


    @Override
    public int getItemCount() {
        return mDataset == null ? 0 : mDataset.size();
    }

    public boolean addOrUpdateItem(CallMembershipEntity entity) {
        if (entity == null) return false;
        if (mDataset == null) {
            mDataset = new ArrayList<>();
        }
        int pos = findItem(entity.mPersonId);
        if (pos == -1) {
            mDataset.add(entity);
        } else {
            mDataset.set(pos, entity);
        }
        Collections.sort(mDataset, new MComparator());
        makeData();
        notifyDataSetChanged();
        return true;
    }

    private void makeData() {
        CallMembership.State lastState = null;
        List<CallMembershipEntity> dataSet = new ArrayList<>();
        for (CallMembershipEntity entity:mDataset){
            if (entity.mIsHeader)
                dataSet.add(entity);
        }
        mDataset.removeAll(dataSet);
        dataSet.clear();
        for (CallMembershipEntity entity : mDataset) {
            if (null == lastState) {
                CallMembershipEntity e = new CallMembershipEntity();
                e.mName = entity.mState == CallMembership.State.JOINED ? "In Meeting" : entity.mState == CallMembership.State.INLOBBY ? "In Lobby" : "Not in Meeting";
                e.mIsHeader = true;
                dataSet.add(e);
            } else if (lastState != entity.mState) {
                CallMembershipEntity e = new CallMembershipEntity();
                e.mName = entity.mState == CallMembership.State.JOINED ? "In Meeting" : entity.mState == CallMembership.State.INLOBBY ? "In Lobby" : "Not in Meeting";
                e.mIsHeader = true;
                dataSet.add(e);
            }
            dataSet.add(entity);
            lastState = entity.mState;
        }
        mDataset.clear();
        mDataset.addAll(dataSet);
    }

    public boolean removeItem(String personId) {
        int pos = findItem(personId);
        if (pos == -1) return false;
        mDataset.remove(pos);
        makeData();
        notifyDataSetChanged();
        return true;
    }

    public boolean updateName(String personId, String name) {
        int pos = findItem(personId);
        if (pos == -1) return false;
        mDataset.get(pos).mName = name;
        notifyDataSetChanged();
        return true;
    }

    public boolean updateAvatar(String personId, String avatar) {
        int pos = findItem(personId);
        if (pos == -1) return false;
        mDataset.get(pos).mAvatarUrl = avatar;
        notifyDataSetChanged();
        return true;
    }

    public boolean updateSendingAudioStatus(String personId, boolean sendingAudio) {
        int pos = findItem(personId);
        if (pos == -1) return false;
        mDataset.get(pos).mSendingAudio = sendingAudio;
        notifyDataSetChanged();
        return true;
    }

    public boolean updateSendingVideoStatus(String personId, boolean sendingVideo) {
        int pos = findItem(personId);
        if (pos == -1) return false;
        mDataset.get(pos).mSendingVideo = sendingVideo;
        notifyDataSetChanged();
        return true;
    }

    public void updateActiveSpeaker(String personId) {
        activeSpeakerId = personId;
        notifyDataSetChanged();
    }

    public String getActiveSpeaker() {
        return activeSpeakerId;
    }

    private int findItem(String personId) {
        if (mDataset == null || personId == null) return -1;
        int pos = -1;
        for (int i = 0; i < mDataset.size(); i++) {
            if (mDataset.get(i).mPersonId == null)
                continue;
            if (mDataset.get(i).mPersonId.equals(personId)) {
                pos = i;
                break;
            }
        }
        return pos;
    }

    class MComparator implements Comparator<CallMembershipEntity> {

        @Override
        public int compare(CallMembershipEntity o1, CallMembershipEntity o2) {
            return o1.mState == CallMembership.State.INLOBBY ? -1 : o1.mState == CallMembership.State.JOINED ? 0 : 1;
        }
    }
}