/*
 * Copyright 2016-2017 Cisco Systems Inc
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 */

package com.ciscowebex.androidsdk.kitchensink.launcher.fragments;


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.Image;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.TextView;

import com.ciscowebex.androidsdk.kitchensink.R;
import com.ciscowebex.androidsdk.kitchensink.actions.WebexAgent;
import com.ciscowebex.androidsdk.kitchensink.actions.commands.AddCallHistoryAction;
import com.ciscowebex.androidsdk.kitchensink.actions.commands.RequirePermissionAction;
import com.ciscowebex.androidsdk.kitchensink.actions.commands.toggleSpeakerAction;
import com.ciscowebex.androidsdk.kitchensink.actions.events.AnswerEvent;
import com.ciscowebex.androidsdk.kitchensink.actions.events.DialEvent;
import com.ciscowebex.androidsdk.kitchensink.actions.events.HangupEvent;
import com.ciscowebex.androidsdk.kitchensink.actions.events.OnCallMembershipEvent;
import com.ciscowebex.androidsdk.kitchensink.actions.events.OnConnectEvent;
import com.ciscowebex.androidsdk.kitchensink.actions.events.OnDisconnectEvent;
import com.ciscowebex.androidsdk.kitchensink.actions.events.OnMediaChangeEvent;
import com.ciscowebex.androidsdk.kitchensink.actions.events.OnRemoteAuxVideoEvent;
import com.ciscowebex.androidsdk.kitchensink.actions.events.OnRingingEvent;
import com.ciscowebex.androidsdk.kitchensink.actions.events.PermissionAcquiredEvent;
import com.ciscowebex.androidsdk.kitchensink.launcher.LauncherActivity;
import com.ciscowebex.androidsdk.kitchensink.ui.BaseFragment;
import com.ciscowebex.androidsdk.kitchensink.ui.FullScreenSwitcher;
import com.ciscowebex.androidsdk.kitchensink.ui.ParticipantsAdapter;
import com.ciscowebex.androidsdk.people.Person;
import com.ciscowebex.androidsdk.phone.CallMembership;
import com.ciscowebex.androidsdk.phone.CallObserver;
import com.ciscowebex.androidsdk.phone.MediaRenderView;
import com.ciscowebex.androidsdk.phone.RemoteAuxVideo;
import com.github.benoitdion.ln.Ln;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;

import static com.ciscowebex.androidsdk.phone.CallObserver.RemoteSendingSharingEvent;
import static com.ciscowebex.androidsdk.phone.CallObserver.SendingSharingEvent;
/**
 * A simple {@link BaseFragment} subclass.
 */
public class CallFragment extends BaseFragment {
    protected static final int MEDIA_PROJECTION_REQUEST = 2;
    private static final String CALLEE = "callee";
    private static final String INCOMING_CALL = "incoming";
    private WebexAgent agent;
    private FullScreenSwitcher screenSwitcher;
    private boolean isConnected = false;
    private List<RemoteAuxVideo> _remoteAuxVideoList = new ArrayList<>();
    private HashMap<Long, RemoteAuxVideoViewHolder> _remoteAuxVideoViewMap = new HashMap<>();
    private HashMap<String, String> _remoteAuxAvatarMap = new HashMap<>();

    @BindView(R.id.localView)
    View localView;

    @BindView(R.id.remoteView)
    View remoteView;

    @BindView(R.id.viewRemoteAvatar)
    ImageView remoteAvatar;

    @BindView(R.id.screenShare)
    View screenShare;

    @BindView(R.id.view_call_control)
    View viewCallControl;

    @BindView(R.id.view_aux_videos_container)
    GridLayout viewRemoteAuxVideosContainer;

    @BindView(R.id.view_aux_videos)
    GridLayout viewRemoteAuxVideos;

    @BindView(R.id.view_participants)
    RecyclerView viewParticipants;

    @BindView(R.id.buttonHangup)
    Button buttonHangup;

    @BindView(R.id.buttonDTMF)
    Button buttonDTMF;

    @BindView(R.id.switchLoudSpeaker)
    Switch switchLoudSpeaker;

    @BindView(R.id.switchSendVideo)
    Switch switchSendingVideo;

    @BindView(R.id.switchSendAudio)
    Switch switchSendingAudio;

    @BindView(R.id.switchReceiveVideo)
    Switch switchReceiveVideo;

    @BindView(R.id.switchReceiveAudio)
    Switch switchReceiveAudio;

    @BindView(R.id.radioFrontCam)
    RadioButton radioFrontCam;

    @BindView(R.id.radioBackCam)
    RadioButton radioBackCam;

    @BindView(R.id.call_layout)
    ConstraintLayout layout;

    @BindView(R.id.switchShareContent)
    Switch switchShareContent;

    private ParticipantsAdapter participantsAdapter;

    // Required empty public constructor

    class RemoteAuxVideoViewHolder{
        View item;
        MediaRenderView mediaRenderView;
        ImageView viewAvatar;
        TextView textView;

        RemoteAuxVideoViewHolder(View item){
            this.item = item;
            this.mediaRenderView = item.findViewById(R.id.view_video);
            this.viewAvatar = item.findViewById(R.id.view_avatar);
            this.textView = item.findViewById(R.id.name);
        }
    }

    public CallFragment() {
    }

    public static CallFragment newAnswerCallInstance() {
        return CallFragment.newInstance(INCOMING_CALL);
    }

    public static CallFragment newInstance(String id) {
        CallFragment fragment = new CallFragment();
        Bundle args = new Bundle();
        args.putInt(LAYOUT, R.layout.fragment_call);
        args.putString(CALLEE, id);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onStart() {
        super.onStart();
        agent = WebexAgent.getInstance();
        screenSwitcher = new FullScreenSwitcher(getActivity(), layout, remoteView);
        updateScreenShareView();
        participantsAdapter = new ParticipantsAdapter(null);
        viewParticipants.setAdapter(participantsAdapter);
        if (!isConnected) {
            setViewAndChildrenEnabled(layout, false);
            ((SurfaceView)localView).setZOrderMediaOverlay(true);
            ((SurfaceView)screenShare).setZOrderMediaOverlay(true);
            //requirePermission();
            makeCall();
        }
    }

    private static void setViewAndChildrenEnabled(View view, boolean enabled) {
        view.setEnabled(enabled);
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                View child = viewGroup.getChildAt(i);
                setViewAndChildrenEnabled(child, enabled);
            }
        }
    }

    private void setupWidgetStates() {
        switch (agent.getDefaultCamera()) {
            case FRONT:
                radioFrontCam.setChecked(true);
                break;
            case BACK:
                radioBackCam.setChecked(true);
                break;
            case CLOSE:
                localView.setVisibility(View.GONE);
                break;
        }
        switchLoudSpeaker.setChecked(agent.getSpeakerPhoneOn());
        switchSendingVideo.setChecked(agent.isSendingVideo());
        switchSendingAudio.setChecked(agent.isSendingAudio());
        switchReceiveVideo.setChecked(agent.isReceivingVideo());
        switchReceiveAudio.setChecked(agent.isReceivingAudio());

        updateScreenShareView();
    }

    private void updateParticipants(){
        if (agent == null || agent.getActiveCall() == null) return;
        List<CallMembership> callMemberships = agent.getActiveCall().getMemberships();
        if (callMemberships == null) return;
        for (CallMembership callMembership : callMemberships){
            String personId = callMembership.getPersonId();
            if (callMembership.getState() != CallMembership.State.JOINED || personId == null || personId.isEmpty()) continue;
            agent.getWebex().people().get(personId, r -> {
                if (r == null || !r.isSuccessful() || r.getData() == null) return;
                Person person = r.getData();
                _remoteAuxAvatarMap.put(personId, person.getAvatar());
                participantsAdapter.addItem(new ParticipantsAdapter.CallMembershipEntity(personId, person.getDisplayName(), person.getAvatar(), callMembership.isSendingAudio(), callMembership.isSendingVideo()));
            });
        }
    }

    private void updateScreenShareView() {
        screenShare.setVisibility(agent.isScreenSharing() ? View.VISIBLE : View.INVISIBLE);
    }

    private void requirePermission() {
        new RequirePermissionAction(getActivity()).execute();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    private void setButtonsEnable(boolean enable) {
        buttonHangup.setEnabled(enable);
        buttonDTMF.setEnabled(false);
    }

    @OnClick(R.id.buttonHangup)
    public void onHangup() {
        if (isConnected) {
            agent.hangup();
        } else {
            ((LauncherActivity) getActivity()).goBackStack();
        }
    }

    @OnClick(R.id.buttonDTMF)
    public void sendDTMF() {
    }

    @OnClick(R.id.remoteView)
    public void onRemoteViewClicked() {
        screenSwitcher.toggleFullScreen();
        updateFullScreenLayout();
    }

    private void updateFullScreenLayout(){
        updateScreenShareView();
        ((SurfaceView)remoteView).setZOrderMediaOverlay(screenSwitcher.isFullScreen());
        localView.setVisibility(screenSwitcher.isFullScreen() ? View.GONE : View.VISIBLE);
    }

    @OnCheckedChanged({R.id.switchSendVideo, R.id.switchSendAudio,
            R.id.switchReceiveVideo, R.id.switchReceiveAudio, R.id.switchShareContent})
    public void onSwitchCallAbility(Switch s) {
        switch (s.getId()) {
            case R.id.switchSendVideo:
                if (radioBackCam.isChecked())
                    agent.setBackCamera();
                else {
                    radioFrontCam.setChecked(true);
                    agent.setFrontCamera();
                }
                agent.sendVideo(s.isChecked());
                break;
            case R.id.switchSendAudio:
                agent.sendAudio(s.isChecked());
                break;
            case R.id.switchReceiveVideo:
                agent.receiveVideo(s.isChecked());
                break;
            case R.id.switchReceiveAudio:
                agent.receiveAudio(s.isChecked());
                break;
            case R.id.switchShareContent:
                if (s.isChecked())
                    agent.getActiveCall().startSharing(r -> {Ln.d("startSharing result: " + r);});
                else
                    agent.getActiveCall().stopSharing(r -> {Ln.d("stopSharing result: " + r);});
                break;

        }
    }

    @OnCheckedChanged(R.id.switchLoudSpeaker)
    public void onSwitchLoudSpeakerChanged(Switch s) {
        new toggleSpeakerAction(getActivity(), s.isChecked()).execute();
    }

    @OnClick(R.id.radioBackCam)
    public void onBackCamRadioClicked() {
        agent.setBackCamera();
    }

    @OnClick(R.id.radioFrontCam)
    public void onFrontCamRadioClicked() {
        agent.setFrontCamera();
    }

    @OnClick({R.id.tab_callcontrol, R.id.tab_aux_video, R.id.tab_participants})
    public void onTabClick(View view){
        switch (view.getId()){
            case R.id.tab_callcontrol:
                viewCallControl.setVisibility(View.VISIBLE);
                viewRemoteAuxVideosContainer.setVisibility(View.GONE);
                viewParticipants.setVisibility(View.GONE);
                break;
            case R.id.tab_aux_video:
                viewCallControl.setVisibility(View.GONE);
                viewRemoteAuxVideosContainer.setVisibility(View.VISIBLE);
                viewParticipants.setVisibility(View.GONE);
                break;
            case R.id.tab_participants:
                viewCallControl.setVisibility(View.GONE);
                viewRemoteAuxVideosContainer.setVisibility(View.GONE);
                viewParticipants.setVisibility(View.VISIBLE);
                break;
            default:
                break;
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        screenSwitcher.updateOnRotation();
        updateScreenShareView();
    }

    @Override
    public void onBackPressed() {
        if (isConnected)
            agent.hangup();
    }

    private void makeCall() {
        String callee = getCallee();
        if (callee.isEmpty())
            return;

        if (callee.equals(INCOMING_CALL)) {
            setButtonsEnable(false);
            agent.answer(localView, remoteView, screenShare);
            return;
        }

        agent.dial(callee, localView, remoteView, screenShare);
        new AddCallHistoryAction(callee, "out").execute();
        setButtonsEnable(true);
    }

    private String getCallee() {
        Bundle bundle = getArguments();
        return bundle != null ? bundle.getString(CALLEE) : "";
    }

    private void feedback() {
        BaseFragment fm = new CallFeedbackFragment();
        ((LauncherActivity) getActivity()).replace(fm);
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(DialEvent event) {
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(AnswerEvent event) {
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(HangupEvent event) {
        setButtonsEnable(false);
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(OnRingingEvent event) {
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(OnConnectEvent event) {
        isConnected = true;
        setViewAndChildrenEnabled(layout, true);
        if (agent.getDefaultCamera().equals(WebexAgent.CameraCap.CLOSE))
            agent.sendVideo(false);
        setupWidgetStates();
        updateParticipants();
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(OnDisconnectEvent event) {
        if (agent.getActiveCall() == null || event.getCall().equals(agent.getActiveCall())) {
            _remoteAuxVideoList.clear();
            _remoteAuxVideoViewMap.clear();
            _remoteAuxAvatarMap.clear();
            feedback();
        }
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(OnMediaChangeEvent event) {
        if (event.callEvent instanceof RemoteSendingSharingEvent) {
            Ln.d("RemoteSendingSharingEvent: " + ((RemoteSendingSharingEvent)event.callEvent).isSending());
            updateScreenShareView();
        } else if (event.callEvent instanceof SendingSharingEvent) {
            Ln.d("SendingSharingEvent: " + ((SendingSharingEvent)event.callEvent).isSending());
            if (((SendingSharingEvent)event.callEvent).isSending()){
                sendNotification();
                backToHome();
            }
        } else if (event.callEvent instanceof CallObserver.ActiveSpeakerChangedEvent){
            CallMembership membership = ((CallObserver.ActiveSpeakerChangedEvent) event.callEvent).to();
            Ln.d("ActiveSpeakerChangedEvent: ");
            String personId = membership.getPersonId();
            if (personId == null || personId.isEmpty()) return;
            participantsAdapter.updateActiveSpeaker(personId);
            if (membership.isSendingVideo()){
                remoteAvatar.setVisibility(View.GONE);
            }else {
                remoteAvatar.setVisibility(View.VISIBLE);
                String avatar = _remoteAuxAvatarMap.get(membership.getPersonId());
                if (avatar == null || avatar.isEmpty()){
                    remoteAvatar.setImageResource(R.drawable.google_contacts_android);
                }else {
                    Picasso.with(getActivity()).cancelRequest(remoteAvatar);
                    Picasso.with(getActivity()).load(_remoteAuxAvatarMap.get(membership.getPersonId())).fit().into(remoteAvatar);
                }
            }
        } else if (event.callEvent instanceof CallObserver.RemoteAuxVideosCountChanged) {
            int newCount = ((CallObserver.RemoteAuxVideosCountChanged) event.callEvent).getCount();
            Ln.d("RemoteAuxVideosCount: " + newCount);
            int currCount = _remoteAuxVideoList.size();
            if (newCount > currCount) {
                for (int i = currCount; i < newCount; i++) {
                    View remoteAuxVideoView = LayoutInflater.from(getActivity()).inflate(R.layout.remote_video_view, null);
                    RemoteAuxVideoViewHolder remoteAuxVideoViewHolder = new RemoteAuxVideoViewHolder(remoteAuxVideoView);
                    agent.getActiveCall().subscribeRemoteAuxVideo(remoteAuxVideoViewHolder.mediaRenderView, r -> {
                        Ln.d("subscribeVideoTrack: " + r.isSuccessful());
                        if (!r.isSuccessful() || r.getData() == null) return;
                        RemoteAuxVideo remoteAuxVideo = r.getData();
                        remoteAuxVideoViewHolder.viewAvatar.setVisibility(View.GONE);
                        _remoteAuxVideoList.add(remoteAuxVideo);
                        _remoteAuxVideoViewMap.put(remoteAuxVideo.getVid(), remoteAuxVideoViewHolder);
                        viewRemoteAuxVideos.addView(remoteAuxVideoView);
                    });
                }
            } else {
                for (int i = _remoteAuxVideoList.size() - 1; i >= newCount; i--) {
                    RemoteAuxVideo remoteAuxVideo = _remoteAuxVideoList.get(i);
                    agent.getActiveCall().unsubscribeRemoteAuxVideo(remoteAuxVideo, r -> {
                        Ln.d("unsubscribeRemoteAuxVideo: " + r.isSuccessful());
                        if (!r.isSuccessful()) return;
                        RemoteAuxVideoViewHolder remoteAuxVideoViewHolder = _remoteAuxVideoViewMap.get(remoteAuxVideo.getVid());
                        _remoteAuxVideoList.remove(remoteAuxVideo);
                        _remoteAuxVideoViewMap.remove(remoteAuxVideo.getVid());
                        viewRemoteAuxVideos.removeView(remoteAuxVideoViewHolder.item);
                    });
                }
            }
        }
    }


    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(OnRemoteAuxVideoEvent event) {
        Ln.d("OnRemoteAuxVideoEvent: " + event.callEvent.getRemoteAuxVideo().getVid());
        RemoteAuxVideo remoteAuxVideo = event.callEvent.getRemoteAuxVideo();
        RemoteAuxVideoViewHolder remoteAuxVideoViewHolder = _remoteAuxVideoViewMap.get(remoteAuxVideo.getVid());
        if (event.callEvent instanceof CallObserver.RemoteAuxSendingVideoEvent) {
            Ln.d("remoteAuxSendingVideoEvent: " + remoteAuxVideo.isSendingVideo());
            if (remoteAuxVideo.isSendingVideo()) {
                remoteAuxVideoViewHolder.viewAvatar.setVisibility(View.GONE);
            } else {
                remoteAuxVideoViewHolder.viewAvatar.setVisibility(View.VISIBLE);
                String avatar = _remoteAuxAvatarMap.get(remoteAuxVideo.getPerson().getPersonId());
                if (avatar == null){
                    remoteAuxVideoViewHolder.viewAvatar.setImageResource(android.R.color.darker_gray);
                }else{
                    Picasso.with(getActivity()).cancelRequest(remoteAuxVideoViewHolder.viewAvatar);
                    Picasso.with(getActivity()).load(avatar).fit().into(remoteAuxVideoViewHolder.viewAvatar);
                }
            }
        } else if (event.callEvent instanceof CallObserver.RemoteAuxVideoPersonChangedEvent) {
            Ln.d("remoteAuxVideoPersonChangedEvent: " + remoteAuxVideo.getPerson());
            if (remoteAuxVideo.getPerson() == null){
                _remoteAuxVideoList.remove(remoteAuxVideo);
                _remoteAuxVideoViewMap.remove(remoteAuxVideo.getVid());
                viewRemoteAuxVideos.removeView(remoteAuxVideoViewHolder.item);
            } else {
                String personId = remoteAuxVideo.getPerson().getPersonId();
                agent.getWebex().people().get(personId, r -> {
                    if (!r.isSuccessful() || r.getData() == null) return;
                    Person person = r.getData();
                    remoteAuxVideoViewHolder.textView.setText(r.getData().getDisplayName());
                    if (_remoteAuxAvatarMap.get(personId) != null) return;
                    _remoteAuxAvatarMap.put(personId, person.getAvatar());
                });
            }
        }
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(OnCallMembershipEvent event) {
        CallMembership callMembership = event.callEvent.getCallMembership();
        String personId = callMembership.getPersonId();
        if (personId == null || personId.isEmpty()) return;
        if (event.callEvent instanceof CallObserver.MembershipJoinedEvent){
            agent.getWebex().people().get(personId, r -> {
                if (r == null || !r.isSuccessful() || r.getData() == null) return;
                Person person = r.getData();
                participantsAdapter.addItem(new ParticipantsAdapter.CallMembershipEntity(personId, person.getDisplayName(), person.getAvatar(), callMembership.isSendingAudio(), callMembership.isSendingVideo()));
            });

        } else if (event.callEvent instanceof CallObserver.MembershipLeftEvent){
            participantsAdapter.removeItem(personId);
        } else if (event.callEvent instanceof CallObserver.MembershipSendingAudioEvent){
            participantsAdapter.updateSendingAudioStatus(personId, callMembership.isSendingAudio());
        } else if (event.callEvent instanceof CallObserver.MembershipSendingVideoEvent){
            participantsAdapter.updateSendingVideoStatus(personId, callMembership.isSendingVideo());
            if (participantsAdapter.getActiveSpeaker() != null && participantsAdapter.getActiveSpeaker().equals(callMembership.getPersonId())){
                if (callMembership.isSendingVideo()){
                    remoteAvatar.setVisibility(View.GONE);
                }else {
                    remoteAvatar.setVisibility(View.VISIBLE);
                    String avatar = _remoteAuxAvatarMap.get(callMembership.getPersonId());
                    if (avatar == null || avatar.isEmpty()){
                        remoteAvatar.setImageResource(R.drawable.google_contacts_android);
                    }else {
                        Picasso.with(getActivity()).cancelRequest(remoteAvatar);
                        Picasso.with(getActivity()).load(_remoteAuxAvatarMap.get(callMembership.getPersonId())).fit().into(remoteAvatar);
                    }
                }
            }
        }
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(PermissionAcquiredEvent event) {
        makeCall();
    }

    private void backToHome() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        this.startActivity(intent);
    }

    private void sendNotification(){
        Intent appIntent = new Intent(getActivity(), LauncherActivity.class);
        appIntent.setAction(Intent.ACTION_MAIN);
        appIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        appIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        PendingIntent contentIntent = PendingIntent.getActivity(getActivity(), 0,appIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationManager notifyManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getActivity())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Cisco Kichensink")
                .setContentText("I'm sharing content")
                .setContentIntent(contentIntent);
        notifyManager.notify(1, builder.build());
    }

}
