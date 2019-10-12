package com.ciscowebex.androidsdk.kitchensink.service;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.ciscowebex.androidsdk.kitchensink.R;
import com.ciscowebex.androidsdk.kitchensink.actions.WebexAgent;

import java.util.List;

public class FloatWindowService extends Service {
    private WindowManager windowManager;
    private WindowManager.LayoutParams layoutParams;
    private LayoutInflater layoutInflater;
    private View floatingView;
    private WebexAgent agent;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new MyBinder();
    }

    public class MyBinder extends Binder {
        public FloatWindowService getService() {
            return FloatWindowService.this;
        }
    }

    public void setAgent(WebexAgent agent) {
        this.agent = agent;
        initWindow();
        initFloating();
    }

    private void initWindow() {
        windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        layoutParams = new WindowManager.LayoutParams();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
                WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR |
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;
        layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.gravity = Gravity.LEFT | Gravity.TOP;
        layoutParams.x = windowManager.getDefaultDisplay().getWidth();
        layoutParams.y = 210;
        layoutInflater = LayoutInflater.from(getApplicationContext());
        floatingView = layoutInflater.inflate(R.layout.remote_video_view, null);
        View remoteView = floatingView.findViewById(R.id.view_video);
        View localView = floatingView.findViewById(R.id.view_video2);
        windowManager.addView(floatingView, layoutParams);
        new Handler().postDelayed(() -> agent.setVideoRenderViews(new Pair<>(localView, remoteView)), 500);
    }

    private void initFloating() {
        FrameLayout frameLayout = floatingView.findViewById(R.id.frameLayout);
        frameLayout.setOnClickListener(v -> setTopApp(getApplicationContext()));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (null != windowManager && null != floatingView)
            windowManager.removeView(floatingView);
    }

    public static void setTopApp(Context context) {
        if (!isRunningForeground(context)) {
            ActivityManager activityManager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
            if (null == activityManager)
                return;
            List<ActivityManager.RunningTaskInfo> taskList = activityManager.getRunningTasks(100);
            for (ActivityManager.RunningTaskInfo rti : taskList) {
                if (rti.topActivity.getPackageName().equals(context.getPackageName())) {
                    activityManager.moveTaskToFront(rti.id, 0);
                    return;
                }
            }
        }
    }

    public static boolean isRunningForeground(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (null == activityManager)
            return false;
        List<ActivityManager.RunningAppProcessInfo> appProcessInfoList = activityManager.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo appProcessInfo : appProcessInfoList) {
            if (appProcessInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                if (appProcessInfo.processName.equals(context.getApplicationInfo().processName)) {
                    return true;
                }
            }
        }
        return false;
    }
}
