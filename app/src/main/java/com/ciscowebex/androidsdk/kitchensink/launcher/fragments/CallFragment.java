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
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.NotificationCompat;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.ciscowebex.androidsdk.kitchensink.R;
import com.ciscowebex.androidsdk.kitchensink.actions.WebexAgent;
import com.ciscowebex.androidsdk.kitchensink.actions.commands.AddCallHistoryAction;
import com.ciscowebex.androidsdk.kitchensink.actions.commands.RequirePermissionAction;
import com.ciscowebex.androidsdk.kitchensink.actions.commands.toggleSpeakerAction;
import com.ciscowebex.androidsdk.kitchensink.actions.events.AnswerEvent;
import com.ciscowebex.androidsdk.kitchensink.actions.events.DialEvent;
import com.ciscowebex.androidsdk.kitchensink.actions.events.HangupEvent;
import com.ciscowebex.androidsdk.kitchensink.actions.events.OnConnectEvent;
import com.ciscowebex.androidsdk.kitchensink.actions.events.OnDisconnectEvent;
import com.ciscowebex.androidsdk.kitchensink.actions.events.OnMediaChangeEvent;
import com.ciscowebex.androidsdk.kitchensink.actions.events.OnRemoteAuxVideoEvent;
import com.ciscowebex.androidsdk.kitchensink.actions.events.OnRingingEvent;
import com.ciscowebex.androidsdk.kitchensink.actions.events.PermissionAcquiredEvent;
import com.ciscowebex.androidsdk.kitchensink.launcher.LauncherActivity;
import com.ciscowebex.androidsdk.kitchensink.ui.BaseFragment;
import com.ciscowebex.androidsdk.kitchensink.ui.FullScreenSwitcher;
import com.ciscowebex.androidsdk.phone.CallObserver;
import com.ciscowebex.androidsdk.phone.RemoteAuxVideo;
import com.github.benoitdion.ln.Ln;

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
    private List<View> _remoteAuxViewList = new ArrayList<>();
    private HashMap<String, Bitmap> _remoteAuxIconList = new HashMap<>();
    private List<RemoteAuxVideo> _remoteAuxVideoList = new ArrayList<>();

    @BindView(R.id.localView)
    View localView;

    @BindView(R.id.remoteView)
    View remoteView;

    @BindView(R.id.screenShare)
    View screenShare;

    @BindView(R.id.subviewContainer)
    TableRow subviewContainer;

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

    // Required empty public constructor
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
        updateScreenShareView();
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
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(OnDisconnectEvent event) {
        if (agent.getActiveCall() == null || event.getCall().equals(agent.getActiveCall())) {
            for (Bitmap icon : _remoteAuxIconList.values()){
                if (icon != null)
                    icon.recycle();
            }
            _remoteAuxIconList.clear();
            _remoteAuxVideoList.clear();
            _remoteAuxViewList.clear();
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
        } else if (event.callEvent instanceof CallObserver.RemoteAuxVideosCountChanged){
            int count = ((CallObserver.RemoteAuxVideosCountChanged)event.callEvent).getCount();
            Ln.d("RemoteAuxVideosCount: " + count);

            if (_remoteAuxViewList.size() < count) {
                for (int i = _remoteAuxViewList.size(); i < count - 1; i++) {
                    LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    View remoteVideoView = inflater.inflate(R.layout.remote_video_view, subviewContainer, false);
                    remoteVideoView.setTag(i);

                    ((CheckBox) remoteVideoView.findViewById(R.id.receiving)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            Ln.d("onCheckedChanged: " + isChecked);
                            int index = (int)((View) buttonView.getParent()).getTag();
                            _remoteAuxVideoList.get(index).setReceivingVideo(isChecked);
                        }
                    });

                    agent.getActiveCall().subscribeRemoteAuxVideo(remoteVideoView.findViewById(R.id.subview), r -> {
                        Ln.d("subscribeVideoTrack: " + r.isSuccessful());
                        if (r.isSuccessful()) {
                            _remoteAuxVideoList.add(r.getData());
                            _remoteAuxViewList.add(remoteVideoView);
                            subviewContainer.addView(remoteVideoView);
                        }
                    });
                }
            }else{
                for (int i = _remoteAuxViewList.size() - 1; i >= count - 1; i--){
                    int index = i;
                    agent.getActiveCall().unsubscribeRemoteAuxVideo(_remoteAuxVideoList.get(i), r -> {
                        Ln.d("unsubscribeRemoteAuxVideo: " + r.isSuccessful());
                        if (r.isSuccessful()){
                            subviewContainer.removeView(_remoteAuxViewList.get(index));
                            _remoteAuxViewList.remove(index);
                            _remoteAuxVideoList.remove(index);
                        }
                    });
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

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(OnRemoteAuxVideoEvent event) {
        Ln.d("OnRemoteAuxVideoEvent: " + event.callEvent.getRemoteAuxVideo().getVid());
        RemoteAuxVideo remoteAuxVideo = event.callEvent.getRemoteAuxVideo();
        if (event.callEvent instanceof CallObserver.RemoteAuxSendingVideoEvent) {
            Ln.d("remoteAuxSendingVideoEvent: " + remoteAuxVideo.isSendingVideo());
            if (remoteAuxVideo.getVid() > 0) {
                ViewGroup container = (ViewGroup) _remoteAuxViewList.get(new Long(remoteAuxVideo.getVid()).intValue() - 1);
                ((CheckBox)container.findViewById(R.id.sending)).setChecked(remoteAuxVideo.isSendingVideo());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (remoteAuxVideo.isSendingVideo()) {
                        container.findViewById(R.id.subview).setForeground(null);
                    }else if (container.findViewById(R.id.subview).getForeground() == null){
                        Bitmap icon = _remoteAuxIconList.get(remoteAuxVideo.getPerson().getPersonId());
                        if (icon != null){
                            container.findViewById(R.id.subview).setForeground(new BitmapDrawable(icon));
                        }else{
                            container.findViewById(R.id.subview).setForeground(new ColorDrawable(Color.BLACK));
                        }
                    }
                }
            }
        } else if (event.callEvent instanceof CallObserver.ReceivingAuxVideoEvent) {
            Ln.d("receivingAuxVideoEvent: " + remoteAuxVideo.isReceivingVideo());
            ViewGroup container = (ViewGroup) _remoteAuxViewList.get(new Long(remoteAuxVideo.getVid()).intValue() - 1);
            if (remoteAuxVideo.isReceivingVideo()) {
                container.findViewById(R.id.name).setVisibility(View.VISIBLE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    container.findViewById(R.id.subview).setForeground(null);
                }
            }else{
                container.findViewById(R.id.name).setVisibility(View.INVISIBLE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    container.findViewById(R.id.subview).setForeground(new ColorDrawable(Color.BLACK));
                }
            }
        } else if (event.callEvent instanceof CallObserver.RemoteAuxVideoPersonChangedEvent) {
            Ln.d("remoteAuxVideoPersonChangedEvent: " + remoteAuxVideo.getPerson());
            if (remoteAuxVideo.getVid() > 0) {
                String personId = remoteAuxVideo.getPerson().getPersonId();
                agent.getWebex().people().get(personId, r -> {
                    if (r.isSuccessful()){
                        ViewGroup container = (ViewGroup) _remoteAuxViewList.get(new Long(remoteAuxVideo.getVid()).intValue() - 1);
                        ((TextView)container.findViewById(R.id.name)).setText(r.getData().getDisplayName().split("@")[0]);
                        if (_remoteAuxIconList.get(personId) == null) {
                            new AsyncTask() {
                                @Override
                                protected Object doInBackground(Object[] objects) {
                                    _remoteAuxIconList.put(personId, getBitmapFromURL(r.getData().getAvatar()));
                                    return null;
                                }
                            }.execute();
                        }
                    }
                });
            }
        }
    }

    public static Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            // Log exception
            return null;
        }
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
