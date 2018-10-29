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
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v4.app.NotificationCompat;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

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
import com.ciscowebex.androidsdk.kitchensink.actions.events.OnAuxStreamEvent;
import com.ciscowebex.androidsdk.kitchensink.actions.events.OnRingingEvent;
import com.ciscowebex.androidsdk.kitchensink.actions.events.PermissionAcquiredEvent;
import com.ciscowebex.androidsdk.kitchensink.launcher.LauncherActivity;
import com.ciscowebex.androidsdk.kitchensink.ui.BaseFragment;
import com.ciscowebex.androidsdk.kitchensink.ui.FullScreenSwitcher;
import com.ciscowebex.androidsdk.kitchensink.ui.ParticipantsAdapter;
import com.ciscowebex.androidsdk.people.Person;
import com.ciscowebex.androidsdk.phone.AuxStream;
import com.ciscowebex.androidsdk.phone.CallMembership;
import com.ciscowebex.androidsdk.phone.CallObserver;
import com.ciscowebex.androidsdk.phone.MediaRenderView;
import com.ciscowebex.androidsdk.phone.MultiStreamObserver;
import com.github.benoitdion.ln.Ln;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;

import static com.ciscowebex.androidsdk.kitchensink.actions.events.WebexAgentEvent.postEvent;
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
    private HashMap<View, AuxStreamViewHolder> mAuxStreamViewMap = new HashMap<>();
    private HashMap<String, Person> mIdPersonMap = new HashMap<>();

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
    View viewAuxVideosContainer;

    @BindView(R.id.view_aux_videos)
    GridLayout viewAuxVideos;

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

    class AuxStreamViewHolder {
        View item;
        MediaRenderView mediaRenderView;
        ImageView viewAvatar;
        TextView textView;

        AuxStreamViewHolder(View item){
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
        if (participantsAdapter == null){
            participantsAdapter = new ParticipantsAdapter(null);
            viewParticipants.setAdapter(participantsAdapter);
        }
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
        agent.hangup();
        ((LauncherActivity) getActivity()).goBackStack();
    }

    @OnClick(R.id.buttonDTMF)
    public void sendDTMF() {
    }

    @OnClick(R.id.remoteView)
    public void onRemoteViewClicked() {
        if (remoteAvatar.getVisibility() == View.VISIBLE) return;
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
                viewAuxVideosContainer.setVisibility(View.GONE);
                viewParticipants.setVisibility(View.GONE);
                break;
            case R.id.tab_aux_video:
                viewCallControl.setVisibility(View.GONE);
                viewAuxVideosContainer.setVisibility(View.VISIBLE);
                viewParticipants.setVisibility(View.GONE);
                break;
            case R.id.tab_participants:
                viewCallControl.setVisibility(View.GONE);
                viewAuxVideosContainer.setVisibility(View.GONE);
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
        if (!event.isSuccessful()){
            Toast.makeText(getActivity(), "Dial failed!", Toast.LENGTH_SHORT).show();
            feedback();
        }
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
        event.call.setMultiStreamObserver(new MultiStreamObserver() {
            @Override
            public void onAuxStreamChanged(AuxStreamChangedEvent event) {
                postEvent(new OnAuxStreamEvent(event));
            }

            @Override
            public View onAuxStreamAvailable() {
                Ln.d("onAuxStreamAvailable");
                View auxStreamView = LayoutInflater.from(getActivity()).inflate(R.layout.remote_video_view, null);
                AuxStreamViewHolder auxStreamViewHolder = new AuxStreamViewHolder(auxStreamView);
                mAuxStreamViewMap.put(auxStreamViewHolder.mediaRenderView, auxStreamViewHolder);
                return auxStreamViewHolder.mediaRenderView;
            }

            @Override
            public View onAuxStreamUnavailable() {
                Ln.d("onAuxStreamUnavailable");
                return null;
            }
        });
    }

    private void updateParticipants(){
        if (agent == null || agent.getActiveCall() == null) return;
        List<CallMembership> callMemberships = agent.getActiveCall().getMemberships();
        if (callMemberships == null) return;
        Ln.d("updateParticipants: " + callMemberships.size());
        for (CallMembership membership : callMemberships){
            String personId = membership.getPersonId();
            if (membership.getState() != CallMembership.State.JOINED || personId == null || personId.isEmpty() || membership.getEmail() == null || membership.getEmail().isEmpty()) continue;
            participantsAdapter.addItem(new ParticipantsAdapter.CallMembershipEntity(personId, membership.getEmail(), "", membership.isSendingAudio(), membership.isSendingVideo()));
            agent.getWebex().people().get(personId, r -> {
                if (r == null || !r.isSuccessful() || r.getData() == null) return;
                mIdPersonMap.put(personId, r.getData());
                updatePersonInfoForParticipants(personId, r.getData());
            });
        }
    }

    private void updatePersonInfoForParticipants(String personId, Person person){
        participantsAdapter.updateName(personId, person.getDisplayName());
        participantsAdapter.updateAvatar(personId, person.getAvatar());
    }

    private void updatePersonInfoForActiveSpeaker(String personId, Person person){
        if (participantsAdapter.getActiveSpeaker() == null || personId == null || person == null || !participantsAdapter.getActiveSpeaker().equals(personId)) return;
        String avatar = person.getAvatar();
        if (avatar == null || avatar.isEmpty()) {
            remoteAvatar.setImageResource(R.drawable.google_contacts_android);
        } else {
            Picasso.with(getActivity()).cancelRequest(remoteAvatar);
            Picasso.with(getActivity()).load(avatar).fit().into(remoteAvatar);
        }
    }

    private void updatePersonInfoForAuxStream(String personId, Person person, AuxStreamViewHolder auxStreamViewHolder){
        if (personId == null || personId.isEmpty() || person == null || auxStreamViewHolder == null) return;
        auxStreamViewHolder.textView.setText(person.getDisplayName());
        String avatar = person.getAvatar();
        if (avatar == null || avatar.isEmpty()) {
            auxStreamViewHolder.viewAvatar.setImageResource(R.drawable.google_contacts_android);
        } else {
            Picasso.with(getActivity()).cancelRequest(auxStreamViewHolder.viewAvatar);
            Picasso.with(getActivity()).load(avatar).fit().into(auxStreamViewHolder.viewAvatar);
        }
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(OnDisconnectEvent event) {
        if (agent.getActiveCall() == null || event.getCall().equals(agent.getActiveCall())) {
            mAuxStreamViewMap.clear();
            mIdPersonMap.clear();
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
            Ln.d("ActiveSpeakerChangedEvent: " + membership);
            if (membership != null && membership.getPersonId() != null && !membership.getPersonId().isEmpty()) {
                String personId = membership.getPersonId();
                participantsAdapter.updateActiveSpeaker(personId);
                if (membership.isSendingVideo()) {
                    remoteAvatar.setVisibility(View.GONE);
                } else {
                    remoteAvatar.setVisibility(View.VISIBLE);
                    Person person = mIdPersonMap.get(personId);
                    if (person == null){
                        remoteAvatar.setImageResource(R.drawable.google_contacts_android);
                        agent.getWebex().people().get(personId, r -> {
                            if (!r.isSuccessful() || r.getData() == null) return;
                            mIdPersonMap.put(personId, r.getData());
                            updatePersonInfoForActiveSpeaker(personId, r.getData());
                        });
                    } else {
                        String avatar = person.getAvatar();
                        if (avatar == null || avatar.isEmpty()) {
                            remoteAvatar.setImageResource(R.drawable.google_contacts_android);
                        } else {
                            Picasso.with(getActivity()).cancelRequest(remoteAvatar);
                            Picasso.with(getActivity()).load(avatar).fit().into(remoteAvatar);
                        }
                    }
                }
            } else {
                remoteAvatar.setVisibility(View.VISIBLE);
                remoteAvatar.setImageResource(android.R.color.darker_gray);
            }
        }
    }


    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(OnAuxStreamEvent event) {
        Ln.d("OnAuxStreamEvent: " + event.callEvent.getAuxStream());
        AuxStream auxStream = event.callEvent.getAuxStream();
        if (event.callEvent instanceof MultiStreamObserver.AuxStreamOpenedEvent) {
            MultiStreamObserver.AuxStreamOpenedEvent openEvent = (MultiStreamObserver.AuxStreamOpenedEvent)event.callEvent;
            if (openEvent.isSuccessful()){
                Ln.d("AuxStreamOpenedEvent successful");
                viewAuxVideos.addView(mAuxStreamViewMap.get(openEvent.getRenderView()).item);
            }else{
                Ln.d("AuxStreamOpenedEvent failed: " + openEvent.getError());
                mAuxStreamViewMap.remove(openEvent.getRenderView());
            }
        } else if (event.callEvent instanceof MultiStreamObserver.AuxStreamClosedEvent) {
            MultiStreamObserver.AuxStreamClosedEvent closeEvent = (MultiStreamObserver.AuxStreamClosedEvent)event.callEvent;
            if (closeEvent.isSuccessful()){
                Ln.d("AuxStreamClosedEvent successful");
                AuxStreamViewHolder auxStreamViewHolder = mAuxStreamViewMap.get(closeEvent.getRenderView());
                mAuxStreamViewMap.remove(closeEvent.getRenderView());
                viewAuxVideos.removeView(auxStreamViewHolder.item);
            }else{
                Ln.d("AuxStreamClosedEvent failed: " + closeEvent.getError());
            }
        } else if (event.callEvent instanceof MultiStreamObserver.AuxStreamSendingVideoEvent) {
            Ln.d("AuxStreamSendingVideoEvent: " + auxStream.isSendingVideo());
            AuxStreamViewHolder auxStreamViewHolder = mAuxStreamViewMap.get(auxStream.getRenderView());
            if (auxStreamViewHolder == null) return;
            if (auxStream.isSendingVideo()) {
                auxStreamViewHolder.viewAvatar.setVisibility(View.GONE);
            } else {
                CallMembership membership = auxStream.getPerson();
                if (membership == null || membership.getPersonId() == null || membership.getPersonId().isEmpty()) return;
                String personId = membership.getPersonId();
                auxStreamViewHolder.viewAvatar.setVisibility(View.VISIBLE);
                Person person = mIdPersonMap.get(personId);
                if (person == null){
                    auxStreamViewHolder.viewAvatar.setImageResource(R.drawable.google_contacts_android);
                    agent.getWebex().people().get(personId, r -> {
                        if (!r.isSuccessful() || r.getData() == null) return;
                        mIdPersonMap.put(personId, r.getData());
                        updatePersonInfoForAuxStream(personId, r.getData(), auxStreamViewHolder);
                    });
                } else {
                    String avatar = person.getAvatar();
                    if (avatar == null || avatar.isEmpty()) {
                        auxStreamViewHolder.viewAvatar.setImageResource(R.drawable.google_contacts_android);
                    } else {
                        Picasso.with(getActivity()).cancelRequest(auxStreamViewHolder.viewAvatar);
                        Picasso.with(getActivity()).load(avatar).fit().into(auxStreamViewHolder.viewAvatar);
                    }
                }
            }
        } else if (event.callEvent instanceof MultiStreamObserver.AuxStreamPersonChangedEvent) {
            Ln.d("AuxStreamPersonChangedEvent: " + auxStream.getPerson());
            AuxStreamViewHolder auxStreamViewHolder = mAuxStreamViewMap.get(auxStream.getRenderView());
            if (auxStream.getPerson() == null){
                mAuxStreamViewMap.remove(auxStream.getRenderView());
                viewAuxVideos.removeView(auxStreamViewHolder.item);
            } else {
                CallMembership membership = auxStream.getPerson();
                if (membership == null || membership.getPersonId() == null || membership.getPersonId().isEmpty()) return;
                String personId = membership.getPersonId();
                participantsAdapter.updateSendingAudioStatus(personId, membership.isSendingAudio());
                participantsAdapter.updateSendingVideoStatus(personId, membership.isSendingVideo());
                Person person = mIdPersonMap.get(personId);
                auxStreamViewHolder.viewAvatar.setVisibility(membership.isSendingVideo() ? View.GONE : View.VISIBLE);
                if (person == null){
                    auxStreamViewHolder.textView.setText(membership.getEmail());
                    auxStreamViewHolder.viewAvatar.setImageResource(R.drawable.google_contacts_android);
                    agent.getWebex().people().get(personId, r -> {
                        if (!r.isSuccessful() || r.getData() == null) return;
                        mIdPersonMap.put(personId, r.getData());
                        updatePersonInfoForAuxStream(personId, r.getData(), auxStreamViewHolder);
                    });
                } else {
                    auxStreamViewHolder.textView.setText(person.getDisplayName());
                    String avatar = person.getAvatar();
                    if (avatar == null || avatar.isEmpty()) {
                        auxStreamViewHolder.viewAvatar.setImageResource(R.drawable.google_contacts_android);
                    } else {
                        Picasso.with(getActivity()).cancelRequest(auxStreamViewHolder.viewAvatar);
                        Picasso.with(getActivity()).load(avatar).fit().into(auxStreamViewHolder.viewAvatar);
                    }
                }
            }
        }
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(OnCallMembershipEvent event) {
        CallMembership membership = event.callEvent.getCallMembership();
        Ln.d("OnCallMembershipEvent: " + membership);
        if (membership == null || membership.getPersonId() == null || membership.getPersonId().isEmpty()) return;
        String personId = membership.getPersonId();
        if (event.callEvent instanceof CallObserver.MembershipJoinedEvent){
            Ln.d("MembershipJoinedEvent: ");
            if (membership.getState() != CallMembership.State.JOINED || personId == null || personId.isEmpty() || membership.getEmail() == null || membership.getEmail().isEmpty()) return;
            participantsAdapter.addItem(new ParticipantsAdapter.CallMembershipEntity(personId, membership.getEmail(), "", membership.isSendingAudio(), membership.isSendingVideo()));
            agent.getWebex().people().get(personId, r -> {
                if (r == null || !r.isSuccessful() || r.getData() == null) return;
                updatePersonInfoForParticipants(personId, r.getData());
            });
        } else if (event.callEvent instanceof CallObserver.MembershipLeftEvent){
            Ln.d("MembershipLeftEvent: ");
            participantsAdapter.removeItem(personId);
        } else if (event.callEvent instanceof CallObserver.MembershipSendingAudioEvent){
            Ln.d("MembershipSendingAudioEvent: " + membership.isSendingAudio());
            participantsAdapter.updateSendingAudioStatus(personId, membership.isSendingAudio());
        } else if (event.callEvent instanceof CallObserver.MembershipSendingVideoEvent){
            Ln.d("MembershipSendingVideoEvent: " + membership.isSendingVideo());
            participantsAdapter.updateSendingVideoStatus(personId, membership.isSendingVideo());
            if (participantsAdapter.getActiveSpeaker() != null && participantsAdapter.getActiveSpeaker().equals(personId)){
                if (membership.isSendingVideo()){
                    remoteAvatar.setVisibility(View.GONE);
                }else {
                    remoteAvatar.setVisibility(View.VISIBLE);
                    Person person = mIdPersonMap.get(personId);
                    if (person == null){
                        remoteAvatar.setImageResource(R.drawable.google_contacts_android);
                        agent.getWebex().people().get(personId, r -> {
                            if (!r.isSuccessful() || r.getData() == null) return;
                            mIdPersonMap.put(personId, r.getData());
                            updatePersonInfoForActiveSpeaker(personId, r.getData());
                        });
                    } else {
                        String avatar = person.getAvatar();
                        if (avatar == null || avatar.isEmpty()) {
                            remoteAvatar.setImageResource(R.drawable.google_contacts_android);
                        } else {
                            Picasso.with(getActivity()).cancelRequest(remoteAvatar);
                            Picasso.with(getActivity()).load(avatar).fit().into(remoteAvatar);
                        }
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
