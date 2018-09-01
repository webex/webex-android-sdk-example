package com.ciscowebex.androidsdk.kitchensink.ui;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ciscowebex.androidsdk.kitchensink.R;
import com.ciscowebex.androidsdk.phone.CallMembership;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ParticipantsAdapter extends RecyclerView.Adapter<ParticipantsAdapter.MyViewHolder> {
    private List<CallMembershipEntity>  mDataset;
    private String activeSpeakerId = null;

    public static class CallMembershipEntity{
        private String mPersonId;
        private String mName;
        private String mAvatarUrl;
        private boolean mSendingAudio;
        private boolean mSendingVideo;

        public CallMembershipEntity(String personId, String name, String avatarUrl, boolean sendingAudio, boolean sendingVideo) {
            this.mPersonId = personId;
            this.mName = name;
            this.mAvatarUrl = avatarUrl;
            this.mSendingAudio = sendingAudio;
            this.mSendingVideo = sendingVideo;
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

        MyViewHolder(View item) {
            super(item);
            ButterKnife.bind(this, item);
        }

        void setName(String name){ mName.setText(name); }

        void setActiveSpeaker(boolean activeSpeaker){ mActiveSpeaker.setVisibility(activeSpeaker ? View.VISIBLE : View.GONE); }

        void setSendingAudio(boolean sendingAudio){
            int res = sendingAudio ? android.R.drawable.presence_audio_online : android.R.drawable.presence_audio_away;
            mAudio.setImageResource(res);
        }

        void setSendingVideo(boolean sendingVideo){
            int res = sendingVideo ? android.R.drawable.presence_video_online : android.R.drawable.presence_video_away;
            mVideo.setImageResource(res);
        }

        void setAvatar(String avatarUrl){
            Picasso.with(itemView.getContext()).cancelRequest(mAvatar);
            if (avatarUrl == null || avatarUrl.isEmpty()){
                mAvatar.setImageResource(R.drawable.google_contacts_android);
            }else {
                Picasso.with(itemView.getContext()).load(avatarUrl).fit().into(mAvatar);
            }
        }
    }

    public ParticipantsAdapter(List<CallMembershipEntity> data) {
        mDataset = data;
    }

    @Override
    public ParticipantsAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View item =  LayoutInflater.from(parent.getContext()).inflate(R.layout.view_item_participant, parent, false);
        MyViewHolder vh = new MyViewHolder(item);
        return vh;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        CallMembershipEntity entity = mDataset.get(position);
        holder.setName(entity.mName);
        holder.setActiveSpeaker(activeSpeakerId != null && entity.mPersonId.equals(activeSpeakerId));
        holder.setSendingAudio(entity.mSendingAudio);
        holder.setSendingVideo(entity.mSendingVideo);
        holder.setAvatar(entity.mAvatarUrl);
    }

    @Override
    public int getItemCount() {
        return mDataset == null ? 0 : mDataset.size();
    }

    public boolean addItem(CallMembershipEntity entity){
        if (entity == null) return false;
        int pos = findItem(entity.mPersonId);
        if (pos != -1) return false;
        if (mDataset == null){
            mDataset = new ArrayList<>();
        }
        mDataset.add(entity);
        notifyDataSetChanged();
        return true;
    }

    public boolean removeItem(String personId){
        int pos = findItem(personId);
        if (pos == -1) return false;
        mDataset.remove(pos);
        notifyDataSetChanged();
        return true;
    }

    public boolean updateName(String personId, String name){
        int pos = findItem(personId);
        if (pos == -1) return false;
        mDataset.get(pos).mName = name;
        notifyDataSetChanged();
        return true;
    }

    public boolean updateAvatar(String personId, String avatar){
        int pos = findItem(personId);
        if (pos == -1) return false;
        mDataset.get(pos).mAvatarUrl = avatar;
        notifyDataSetChanged();
        return true;
    }
    public boolean updateSendingAudioStatus(String personId, boolean sendingAudio){
        int pos = findItem(personId);
        if (pos == -1) return false;
        mDataset.get(pos).mSendingAudio = sendingAudio;
        notifyDataSetChanged();
        return true;
    }

    public boolean updateSendingVideoStatus(String personId, boolean sendingVideo){
        int pos = findItem(personId);
        if (pos == -1) return false;
        mDataset.get(pos).mSendingVideo = sendingVideo;
        notifyDataSetChanged();
        return true;
    }

    public void updateActiveSpeaker(String personId){
        activeSpeakerId = personId;
        notifyDataSetChanged();
    }

    public String getActiveSpeaker(){
        return activeSpeakerId;
    }

    private int findItem(String personId){
        if (mDataset == null || personId == null) return -1;
        int pos = -1;
        for (int i=0; i<mDataset.size(); i++){
            if (mDataset.get(i).mPersonId.equals(personId)){
                pos = i;
                break;
            }
        }
        return pos;
    }

}