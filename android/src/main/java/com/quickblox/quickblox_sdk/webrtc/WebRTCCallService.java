package com.quickblox.quickblox_sdk.webrtc;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.quickblox.chat.QBChatService;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.quickblox_sdk.R;
import com.quickblox.quickblox_sdk.base.EventHandler;
import com.quickblox.quickblox_sdk.utils.EventsUtil;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;
import com.quickblox.videochat.webrtc.AppRTCAudioManager;
import com.quickblox.videochat.webrtc.BaseSession;
import com.quickblox.videochat.webrtc.QBRTCCameraVideoCapturer;
import com.quickblox.videochat.webrtc.QBRTCClient;
import com.quickblox.videochat.webrtc.QBRTCConfig;
import com.quickblox.videochat.webrtc.QBRTCScreenCapturer;
import com.quickblox.videochat.webrtc.QBRTCSession;
import com.quickblox.videochat.webrtc.QBRTCTypes;
import com.quickblox.videochat.webrtc.QBRTCVideoCapturer;
import com.quickblox.videochat.webrtc.callbacks.QBRTCClientSessionCallbacks;
import com.quickblox.videochat.webrtc.callbacks.QBRTCClientVideoTracksCallbacks;
import com.quickblox.videochat.webrtc.callbacks.QBRTCSessionConnectionCallbacks;
import com.quickblox.videochat.webrtc.view.QBRTCVideoTrack;

import org.jivesoftware.smack.AbstractConnectionListener;
import org.jivesoftware.smack.ConnectionListener;
import org.webrtc.CameraVideoCapturer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArraySet;

import io.flutter.plugin.common.MethodChannel;

/**
 * Created by Injoit on 2019-12-27.
 * Copyright © 2019 Quickblox. All rights reserved.
 */
public class WebRTCCallService extends Service {
    private static final String TAG = WebRTCCallService.class.getSimpleName();

    private static final int SERVICE_ID = 787;
    private static final String CHANNEL_ID = "Quickblox channel";
    private static final String CHANNEL_NAME = "Quickblox service";

    private static final int MAX_OPPONENTS_COUNT = 6;

    private static final long ANSWER_TIME_INTERVAL = 60;
    private static final int DISCONNECT_TIME_INTERVAL = 10;
    private static final long DIALING_TIME_INTERVAL = 5;

    private CallServiceBinder callServiceBinder = new CallServiceBinder();
    private AppRTCAudioManager appRTCAudioManager;

    private Context context;

    private QBRTCClient qbrtcClient;

    private static volatile Set<QBRTCSession> sessionCache = new CopyOnWriteArraySet<>();

    private ConnectionListener connectionListener;
    private VideoTrackListener videoTrackListener;
    private QBRTCClientSessionCallbacks sessionEventListener;
    private SessionConnectionListener sessionConnectionListener;

    private CallTimerTask callTimerTask = new CallTimerTask();
    private Timer callTimer = new Timer();

    public static void start(Context context) {
        Intent intent = new Intent(context, WebRTCCallService.class);
        context.startService(intent);
    }

    public static void stop(Context context) {
        Intent intent = new Intent(context, WebRTCCallService.class);
        context.stopService(intent);
    }

    public static boolean isRunning(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        boolean running = false;
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (WebRTCCallService.class.getName().equals(service.service.getClassName())) {
                running = true;
                break;
            }
        }
        return running;
    }

    @Override
    public void onCreate() {
        context = getApplicationContext();

        try {
            initRTCClient();
            addConnectionListener();
            addSessionEventListener();
            sessionConnectionListener = new SessionConnectionListener();
            videoTrackListener = new VideoTrackListener();
            initAudioManager();
        } catch (RuntimeException e) {
            WebRTCCallService.stop(context);
            e.printStackTrace();
            System.exit(0);
        }

        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String notificationTitle = getString(R.string.notification_title_ready);
        String notificationText = "";

        Notification notification = buildNotification(notificationTitle, notificationText);
        startForeground(SERVICE_ID, notification);

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        removeConnectionListener();
        removeSessionEventListener();
        removeVideoTrackListeners();
        removeSessionConnectionsListeners();
        releaseAudioManager();
        stopCallTimer();
        stopForeground(true);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return callServiceBinder;
    }

    private Notification buildNotification(String notificationTitle, String notificationText) {
        Class moduleClass = getActivityClass();
        Intent notifyIntent = new Intent(this, moduleClass);
        notifyIntent.setAction(Intent.ACTION_MAIN);
        notifyIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        PendingIntent notifyPendingIntent = PendingIntent.getActivity(this, 0,
                notifyIntent, 0);

        NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();
        bigTextStyle.setBigContentTitle(notificationTitle);
        bigTextStyle.bigText(notificationText);

        String channelID = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ?
                createNotificationChannel(CHANNEL_ID, CHANNEL_NAME)
                : getString(R.string.app_name);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelID);
        builder.setStyle(bigTextStyle);
        builder.setContentTitle(notificationTitle);
        builder.setContentText(notificationText);
        builder.setWhen(System.currentTimeMillis());
        builder.setSmallIcon(R.mipmap.ic_launcher);

        Bitmap bitmapIcon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
        builder.setLargeIcon(bitmapIcon);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            builder.setPriority(NotificationManager.IMPORTANCE_LOW);
        } else {
            builder.setPriority(Notification.PRIORITY_LOW);
        }
        builder.setContentIntent(notifyPendingIntent);

        return builder.build();
    }

    private void updateNotification(String notificationTitle, String notificationText) {
        Notification notification = buildNotification(notificationTitle, notificationText);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(SERVICE_ID, notification);
    }

    private Class getActivityClass() {
        String packageName = context.getPackageName();
        Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(packageName);
        String className = launchIntent.getComponent().getClassName();
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private String createNotificationChannel(String channelID, String channelName) {
        NotificationChannel channel = new NotificationChannel(channelID, channelName, NotificationManager.IMPORTANCE_LOW);
        channel.setLightColor(getColor(R.color.primary));
        channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.createNotificationChannel(channel);
        return channelID;
    }

    private void initRTCClient() throws RuntimeException {
        qbrtcClient = QBRTCClient.getInstance(this);
        qbrtcClient.setCameraErrorHandler(new CameraEventsListener());

        QBRTCConfig.setMaxOpponentsCount(MAX_OPPONENTS_COUNT);
        QBRTCConfig.setDebugEnabled(true);
        configRTCTimers(ANSWER_TIME_INTERVAL, DISCONNECT_TIME_INTERVAL, DIALING_TIME_INTERVAL);

        qbrtcClient.prepareToProcessCalls();

        if (QBChatService.getInstance() == null) {
            Toast.makeText(context, "Error connecting to chat", Toast.LENGTH_LONG).show();
            WebRTCCallService.stop(context);
            System.exit(0);
        }

        QBChatService chatService = QBChatService.getInstance();
        chatService.getVideoChatWebRTCSignalingManager().addSignalingManagerListener((qbSignaling, createdLocally) -> {
            if (!createdLocally) {
                qbrtcClient.addSignaling(qbSignaling);
            }
        });
    }

    private void removeSessionConnectionsListeners() {
        for (QBRTCSession session : sessionCache) {
            session.removeSessionCallbacksListener(sessionConnectionListener);
        }
    }

    protected void removeVideoTrackListeners() {
        for (QBRTCSession session : sessionCache) {
            session.removeVideoTrackCallbacksListener(videoTrackListener);
        }
    }

    public void initAudioManager() {
        appRTCAudioManager = AppRTCAudioManager.create(this);
        appRTCAudioManager.setManageSpeakerPhoneByProximity(false);

        appRTCAudioManager.setOnWiredHeadsetStateListener((plugged, hasMicrophone) -> {
        });

        appRTCAudioManager.setBluetoothAudioDeviceStateListener(connected -> {
        });

        appRTCAudioManager.start((audioDevice, set) -> {
        });

        appRTCAudioManager.selectAudioDevice(AppRTCAudioManager.AudioDevice.EARPIECE);
    }

    public void switchAudioOutput(final int value, final MethodChannel.Result result) {
        Handler mainHandler = new Handler(context.getMainLooper());
        Runnable mainThreadRunnable = () -> {
            AppRTCAudioManager.AudioDevice audioDevice = WebRTCConstants.appRTCAudioDeviceFromValue(value);
            if (appRTCAudioManager.getAudioDevices().contains(audioDevice)) {
                appRTCAudioManager.selectAudioDevice(audioDevice);
                if (result != null) {
                    result.success(null);
                }
            } else {
                if (result != null) {
                    String deviceName = WebRTCConstants.getDeviceNameFromValue(value);
                    if (TextUtils.isEmpty(deviceName)) {
                        deviceName = String.valueOf(value);
                    }
                    result.error("Switch Device of type " + deviceName + " is not found", null, null);
                }
            }
        };

        //Some devices like Huawei requires the delay before change audio output
        mainHandler.postDelayed(mainThreadRunnable, 500);
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public void releaseAudioManager() {
        appRTCAudioManager.stop();
    }

    ///////////////////////////////////////////////////////////////////////////
    // INIT LISTENERS
    ///////////////////////////////////////////////////////////////////////////
    private void addSessionEventListener() {
        sessionEventListener = new SessionEventListener();
        qbrtcClient.addSessionCallbacksListener(sessionEventListener);
    }

    private void removeSessionEventListener() {
        qbrtcClient.removeSessionsCallbacksListener(sessionEventListener);
    }

    private void addConnectionListener() {
        connectionListener = new ConnectionListenerImpl();
        QBChatService.getInstance().addConnectionListener(connectionListener);
    }

    private void removeConnectionListener() {
        QBChatService.getInstance().removeConnectionListener(connectionListener);
    }

    private QBRTCSession getSessionFromCache(String sessionId) {
        QBRTCSession foundSession = null;
        for (QBRTCSession session : sessionCache) {
            if (session.getSessionID().equals(sessionId)) {
                foundSession = session;
                break;
            }
        }
        return foundSession;
    }

    ///////////////////////////////////////////////////////////////////////////
    // COMMON METHODS
    ///////////////////////////////////////////////////////////////////////////
    public void acceptCall(String sessionId, Map<String, String> userInfo, ServiceCallback<QBRTCSession> serviceCallback) {
        QBRTCSession qbrtcSession = getSessionFromCache(sessionId);
        if (qbrtcSession != null) {
            qbrtcSession.acceptCall(userInfo);
            serviceCallback.onSuccess(qbrtcSession);

            startCallTimer();
        } else {
            serviceCallback.onError("The session with id: " + sessionId + " has not found");
        }
    }

    public void startCall(final List<Integer> opponentsIdsList, QBRTCTypes.QBConferenceType conferenceType,
                          Map<String, String> userInfo, ServiceCallback<QBRTCSession> serviceCallback) {
        if (qbrtcClient == null) {
            serviceCallback.onError("The RTC Client has not connected");
            return;
        }

        QBRTCSession session = qbrtcClient.createNewSessionWithOpponents(opponentsIdsList, conferenceType);
        session.getSessionDescription().setUserInfo(userInfo);
        session.addVideoTrackCallbacksListener(videoTrackListener);
        session.addSessionCallbacksListener(sessionConnectionListener);
        sessionCache.add(session);
        session.startCall(userInfo);

        serviceCallback.onSuccess(session);

        QBUsers.getUsersByIDs(opponentsIdsList, null).performAsync(new QBEntityCallback<ArrayList<QBUser>>() {
            @Override
            public void onSuccess(ArrayList<QBUser> qbUsers, Bundle bundle) {
                List<String> users = new ArrayList<>();

                for (QBUser user : qbUsers) {
                    users.add(TextUtils.isEmpty(user.getFullName()) ? user.getLogin() : user.getFullName());
                }

                String notificationTitle = getString(R.string.notification_title_outgoing);
                String userNames = Arrays.toString(users.toArray())
                        .replace("[", "")
                        .replace("]", "");
                String notificationText = getString(R.string.notification_text_outgoing, userNames);
                updateNotification(notificationTitle, notificationText);
            }

            @Override
            public void onError(QBResponseException e) {
                String notificationTitle = getString(R.string.notification_title_outgoing);
                String userNames = Arrays.toString(opponentsIdsList.toArray());
                String notificationText = getString(R.string.notification_text_outgoing, userNames);
                updateNotification(notificationTitle, notificationText);
            }
        });
    }

    public void rejectCall(String sessionId, Map<String, String> userInfo, ServiceCallback<QBRTCSession> serviceCallback) {
        QBRTCSession qbrtcSession = getSessionFromCache(sessionId);
        if (qbrtcSession != null) {
            qbrtcSession.rejectCall(userInfo);
            serviceCallback.onSuccess(qbrtcSession);
        } else {
            serviceCallback.onError("The session with id: " + sessionId + " has not found");
        }
    }

    public void hangUpCall(String sessionId, Map<String, String> userInfo, ServiceCallback<QBRTCSession> serviceCallback) {
        QBRTCSession qbrtcSession = getSessionFromCache(sessionId);
        if (qbrtcSession != null) {
            qbrtcSession.hangUp(userInfo);
            serviceCallback.onSuccess(qbrtcSession);
        } else {
            serviceCallback.onError("The session with id: " + sessionId + " has not found");
        }

        stopCallTimer();

        String notificationTitle = getString(R.string.notification_title_ready);
        String notificationText = "";

        updateNotification(notificationTitle, notificationText);
    }

    public void setAudioEnabled(boolean enabled, String sessionId, ServiceCallback<Void> serviceCallback) {
        QBRTCSession qbrtcSession = getSessionFromCache(sessionId);
        if (qbrtcSession != null) {
            qbrtcSession.getMediaStreamManager().getLocalAudioTrack().setEnabled(enabled);
            serviceCallback.onSuccess(null);
        } else {
            serviceCallback.onError("The session with id: " + sessionId + " has not found");
        }
    }

    public void startScreenSharing(Intent data, String sessionId, ServiceCallback<Void> serviceCallback) {
        QBRTCSession qbrtcSession = getSessionFromCache(sessionId);
        if (qbrtcSession != null) {
            qbrtcSession.getMediaStreamManager().setVideoCapturer(new QBRTCScreenCapturer(data, null));
            serviceCallback.onSuccess(null);
        } else {
            serviceCallback.onError("The session with id: " + sessionId + " has not found");
        }
    }

    public void stopScreenSharing(String sessionId, ServiceCallback<Void> serviceCallback) {
        QBRTCSession qbrtcSession = getSessionFromCache(sessionId);
        if (qbrtcSession != null) {
            try {
                qbrtcSession.getMediaStreamManager().setVideoCapturer(new QBRTCCameraVideoCapturer(this, null));
                serviceCallback.onSuccess(null);
            } catch (QBRTCCameraVideoCapturer.QBRTCCameraCapturerException e) {
                serviceCallback.onError(e.getMessage());
            }
        } else {
            serviceCallback.onError("The session with id: " + sessionId + " has not found");
        }
    }

    public void getCallerId(String sessionId, ServiceCallback<Integer> serviceCallback) {
        QBRTCSession qbrtcSession = getSessionFromCache(sessionId);
        if (qbrtcSession != null) {
            Integer callerId = qbrtcSession.getCallerID();
            serviceCallback.onSuccess(callerId);
        } else {
            serviceCallback.onError("The session with id: " + sessionId + " has not found");
        }
    }

    public void getOpponents(String sessionId, ServiceCallback<List<Integer>> serviceCallback) {
        QBRTCSession qbrtcSession = getSessionFromCache(sessionId);
        if (qbrtcSession != null) {
            List<Integer> opponentsList = qbrtcSession.getOpponents();
            serviceCallback.onSuccess(opponentsList);
        } else {
            serviceCallback.onError("The session with id: " + sessionId + " has not found");
        }
    }

    public void isVideoCall(String sessionId, ServiceCallback<Boolean> serviceCallback) {
        QBRTCSession qbrtcSession = getSessionFromCache(sessionId);
        if (qbrtcSession != null) {
            Boolean videoCall = QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_VIDEO == qbrtcSession.getConferenceType();
            serviceCallback.onSuccess(videoCall);
        } else {
            serviceCallback.onError("The session with id: " + sessionId + " has not found");
        }
    }

    public void setVideoEnabled(boolean videoEnabled, String sessionId, ServiceCallback<Void> serviceCallback) {
        QBRTCSession qbrtcSession = getSessionFromCache(sessionId);
        if (qbrtcSession != null) {
            qbrtcSession.getMediaStreamManager().getLocalVideoTrack().setEnabled(videoEnabled);
            serviceCallback.onSuccess(null);
        } else {
            serviceCallback.onError("The session with id: " + sessionId + " has not found");
        }
    }

    public void getCurrentSessionState(String sessionId, ServiceCallback<BaseSession.QBRTCSessionState> serviceCallback) {
        QBRTCSession qbrtcSession = getSessionFromCache(sessionId);
        if (qbrtcSession != null) {
            BaseSession.QBRTCSessionState state = qbrtcSession.getState();
            serviceCallback.onSuccess(state);
        } else {
            serviceCallback.onError("The session with id: " + sessionId + " has not found");
        }
    }

    public void isMediaStreamManagerExist(String sessionId, ServiceCallback<Boolean> serviceCallback) {
        QBRTCSession qbrtcSession = getSessionFromCache(sessionId);
        if (qbrtcSession != null) {
            Boolean exist = qbrtcSession.getMediaStreamManager() != null;
            serviceCallback.onSuccess(exist);
        } else {
            serviceCallback.onError("The session with id: " + sessionId + " has not found");
        }
    }

    public void getPeerChannel(String sessionId, Integer userId, ServiceCallback<QBRTCTypes.QBRTCConnectionState> serviceCallback) {
        QBRTCSession qbrtcSession = getSessionFromCache(sessionId);
        if (qbrtcSession != null) {
            QBRTCTypes.QBRTCConnectionState peerChannel = qbrtcSession.getPeerChannel(userId).getState();
            serviceCallback.onSuccess(peerChannel);
        } else {
            serviceCallback.onError("The session with id: " + sessionId + " has not found");
        }
    }

    public void switchCamera(String sessionId, CameraVideoCapturer.CameraSwitchHandler cameraSwitchHandler, ServiceCallback<Void> serviceCallback) {
        QBRTCSession qbrtcSession = getSessionFromCache(sessionId);
        if (qbrtcSession != null) {
            QBRTCCameraVideoCapturer videoCapturer = (QBRTCCameraVideoCapturer) qbrtcSession.getMediaStreamManager().getVideoCapturer();
            videoCapturer.switchCamera(cameraSwitchHandler);
            serviceCallback.onSuccess(null);
        } else {
            serviceCallback.onError("The session with id: " + sessionId + " has not found");
        }
    }

    public void isSharingScreenState(String sessionId, ServiceCallback<Boolean> serviceCallback) {
        QBRTCSession qbrtcSession = getSessionFromCache(sessionId);
        if (qbrtcSession != null) {
            boolean sharingScreen = false;
            QBRTCVideoCapturer videoCapturer = qbrtcSession.getMediaStreamManager().getVideoCapturer();
            if (videoCapturer instanceof QBRTCScreenCapturer) {
                sharingScreen = true;
            }
            serviceCallback.onSuccess(sharingScreen);
        } else {
            serviceCallback.onError("The session with id: " + sessionId + " has not found");
        }
    }

    public void getSession(String sessionId, ServiceCallback<QBRTCSession> serviceCallback) {
        QBRTCSession qbrtcSession = getSessionFromCache(sessionId);
        if (qbrtcSession != null) {
            serviceCallback.onSuccess(qbrtcSession);
        } else {
            serviceCallback.onError("The session with id: " + sessionId + " has not found");
        }
    }

    private void releaseVideoTrack(String sessionId, Integer userId) {
        QBRTCSession qbrtcSession = getSessionFromCache(sessionId);
        if (qbrtcSession != null) {
            qbrtcSession.getMediaStreamManager().getVideoTrack(userId).cleanUp();
            qbrtcSession.getMediaStreamManager().getVideoTrack(userId).dealloc();
        }
    }

    public void getVideoTrack(String sessionId, Integer userId, ServiceCallback<QBRTCVideoTrack> serviceCallback) {
        QBRTCSession qbrtcSession = getSessionFromCache(sessionId);
        if (qbrtcSession != null) {
            int currentUserId = QBChatService.getInstance().getUser().getId();
            QBRTCVideoTrack videoTrack;
            if (userId == currentUserId) {
                videoTrack = qbrtcSession.getMediaStreamManager().getLocalVideoTrack();
            } else {
                videoTrack = qbrtcSession.getMediaStreamManager().getVideoTrack(userId);
            }
            if (videoTrack == null) {
                serviceCallback.onError("The video track is null");
                return;
            }
            serviceCallback.onSuccess(videoTrack);
        } else {
            serviceCallback.onError("The session with id: " + sessionId + " has not found");
        }
    }

    public void startCallTimer() {
        if (callTimerTask != null) {
            callTimerTask.cancel();
        }
        if (callTimer != null) {
            callTimer = new Timer();
        }
        callTimerTask = new CallTimerTask();
        callTimer.scheduleAtFixedRate(callTimerTask, 0, 1000L);
    }

    private void stopCallTimer() {
        if (callTimerTask != null) {
            callTimerTask.cancel();
        }
        if (callTimer != null) {
            callTimer.cancel();
            callTimer.purge();
        }
    }

    private void configRTCTimers(long answerTimeInterval, int disconnectTimeInterval, long dialingTimeInterval) {
        QBRTCConfig.setAnswerTimeInterval(answerTimeInterval);
        Log.e(TAG, "answerTimeInterval = " + answerTimeInterval);

        QBRTCConfig.setDisconnectTime(disconnectTimeInterval);
        Log.e(TAG, "disconnectTimeInterval = " + disconnectTimeInterval);

        QBRTCConfig.setDialingTimeInterval(dialingTimeInterval);
        Log.e(TAG, "dialingTimeInterval = " + dialingTimeInterval);
    }

    private class CallTimerTask extends TimerTask {
        private Long callTime;

        CallTimerTask() {
            callTime = 1000L;
        }

        @Override
        public void run() {
            callTime = callTime + 1000L;

            String callTime = getCallTime();

            String notificationTitle = getString(R.string.notification_title_call);
            String notificationText = getString(R.string.notification_text_call, callTime);

            updateNotification(notificationTitle, notificationText);
        }

        private String getCallTime() {
            String time = "";
            if (callTime != null) {
                String format = String.format(context.getString(R.string.call_time_format), 2);
                Long elapsedTime = callTime / 1000;
                String seconds = String.format(format, elapsedTime % 60);
                String minutes = String.format(format, elapsedTime % 3600 / 60);
                String hours = String.format(format, elapsedTime / 3600);

                time = minutes + ":" + seconds;
                if (!TextUtils.isEmpty(hours) && !hours.equals("00")) {
                    time = hours + ":" + minutes + ":" + seconds;
                }
            }
            return time;
        }
    }

    class CallServiceBinder extends Binder {
        WebRTCCallService getService() {
            return WebRTCCallService.this;
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // CONNECTION LISTENER
    ///////////////////////////////////////////////////////////////////////////
    private class ConnectionListenerImpl extends AbstractConnectionListener {
        @Override
        public void connectionClosedOnError(Exception e) {

        }

        @Override
        public void reconnectionSuccessful() {

        }

        @Override
        public void reconnectingIn(int seconds) {

        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // CAMERA EVENTS LISTENER
    ///////////////////////////////////////////////////////////////////////////
    private class CameraEventsListener implements CameraVideoCapturer.CameraEventsHandler {
        @Override
        public void onCameraError(String s) {
        }

        @Override
        public void onCameraDisconnected() {
        }

        @Override
        public void onCameraFreezed(String s) {
        }

        @Override
        public void onCameraOpening(String s) {
        }

        @Override
        public void onFirstFrameAvailable() {
        }

        @Override
        public void onCameraClosed() {
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // SESSION CONNECTION LISTENER
    ///////////////////////////////////////////////////////////////////////////
    private class SessionConnectionListener implements QBRTCSessionConnectionCallbacks {

        @Override
        public void onStartConnectToUser(QBRTCSession session, Integer userId) {
            Map<String, Object> payload = new HashMap<>();

            Map<String, Object> sessionMap = WebRTCMapper.qBRTCSessionToMap(session);

            String eventName = WebRTCConstants.Events.PEER_CONNECTION_STATE_CHANGED;

            payload.put("session", sessionMap);
            payload.put("userId", userId);
            payload.put("state", WebRTCConstants.PeerConnectionStates.NEW);

            Map<String, Object> eventData = EventsUtil.buildResult(eventName, payload);

            EventHandler.sendEvent(eventName, eventData);
        }

        @Override
        public void onDisconnectedTimeoutFromUser(QBRTCSession session, Integer userId) {

        }

        @Override
        public void onConnectionFailedWithUser(QBRTCSession session, Integer userId) {
            Map<String, Object> payload = new HashMap<>();

            Map<String, Object> sessionMap = WebRTCMapper.qBRTCSessionToMap(session);

            String eventName = WebRTCConstants.Events.PEER_CONNECTION_STATE_CHANGED;

            payload.put("session", sessionMap);
            payload.put("userId", userId);
            payload.put("state", WebRTCConstants.PeerConnectionStates.FAILED);

            Map<String, Object> eventData = EventsUtil.buildResult(eventName, payload);

            EventHandler.sendEvent(eventName, eventData);
        }

        @Override
        public void onStateChanged(QBRTCSession qbrtcSession, BaseSession.QBRTCSessionState qbrtcSessionState) {

        }

        @Override
        public void onConnectedToUser(QBRTCSession session, Integer userId) {
            Map<String, Object> payload = new HashMap<>();

            Map<String, Object> sessionMap = WebRTCMapper.qBRTCSessionToMap(session);

            String eventName = WebRTCConstants.Events.PEER_CONNECTION_STATE_CHANGED;

            payload.put("session", sessionMap);
            payload.put("userId", userId);
            payload.put("state", WebRTCConstants.PeerConnectionStates.CONNECTED);

            Map<String, Object> eventData = EventsUtil.buildResult(eventName, payload);

            EventHandler.sendEvent(eventName, eventData);
        }

        @Override
        public void onDisconnectedFromUser(QBRTCSession session, Integer userId) {
            Map<String, Object> payload = new HashMap<>();

            Map<String, Object> sessionMap = WebRTCMapper.qBRTCSessionToMap(session);

            String eventName = WebRTCConstants.Events.PEER_CONNECTION_STATE_CHANGED;

            payload.put("session", sessionMap);
            payload.put("userId", userId);
            payload.put("state", WebRTCConstants.PeerConnectionStates.DISCONNECTED);

            Map<String, Object> eventData = EventsUtil.buildResult(eventName, payload);

            EventHandler.sendEvent(eventName, eventData);
        }

        @Override
        public void onConnectionClosedForUser(QBRTCSession session, Integer userId) {
            Map<String, Object> payload = new HashMap<>();

            Map<String, Object> sessionMap = WebRTCMapper.qBRTCSessionToMap(session);

            String eventName = WebRTCConstants.Events.PEER_CONNECTION_STATE_CHANGED;

            payload.put("session", sessionMap);
            payload.put("userId", userId);
            payload.put("state", WebRTCConstants.PeerConnectionStates.CLOSED);

            Map<String, Object> eventData = EventsUtil.buildResult(eventName, payload);

            EventHandler.sendEvent(eventName, eventData);
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // SESSION EVENT LISTENER
    ///////////////////////////////////////////////////////////////////////////
    private class SessionEventListener implements QBRTCClientSessionCallbacks {
        @Override
        public void onReceiveNewSession(QBRTCSession session) {

            final Integer callerName = session.getCallerID();

            QBUsers.getUser(callerName).performAsync(new QBEntityCallback<QBUser>() {
                @Override
                public void onSuccess(QBUser qbUser, Bundle bundle) {
                    String notificationTitle = getString(R.string.notification_title_incoming);
                    String userName = TextUtils.isEmpty(qbUser.getFullName()) ? qbUser.getLogin() : qbUser.getFullName();
                    String notificationText = getString(R.string.notification_text_incoming, userName);
                    updateNotification(notificationTitle, notificationText);
                }

                @Override
                public void onError(QBResponseException e) {
                    String notificationTitle = getString(R.string.notification_title_incoming);
                    String userName = String.valueOf(callerName);
                    String notificationText = getString(R.string.notification_text_incoming, userName);
                    updateNotification(notificationTitle, notificationText);
                }
            });

            session.addVideoTrackCallbacksListener(videoTrackListener);
            session.addSessionCallbacksListener(sessionConnectionListener);
            sessionCache.add(session);

            String eventName = WebRTCConstants.Events.CALL;

            Map<String, Object> payload = new HashMap<>();

            Map sessionMap = WebRTCMapper.qBRTCSessionToMap(session);
            Map userInfoMap = WebRTCMapper.userInfoToMap(session);

            payload.put("session", sessionMap);
            payload.put("userId", session.getCallerID());
            payload.put("userInfo", userInfoMap);

            Map eventData = EventsUtil.buildResult(eventName, payload);

            EventHandler.sendEvent(eventName, eventData);
        }

        @Override
        public void onUserNoActions(QBRTCSession session, Integer userId) {

        }

        @Override
        public void onSessionStartClose(QBRTCSession session) {

        }

        @Override
        public void onUserNotAnswer(QBRTCSession session, Integer userId) {
            String eventName = WebRTCConstants.Events.NOT_ANSWER;

            Map<String, Object> payload = new HashMap<>();
            Map sessionMap = WebRTCMapper.qBRTCSessionToMap(session);

            payload.put("session", sessionMap);
            payload.put("userId", userId);

            Map eventData = EventsUtil.buildResult(eventName, payload);

            EventHandler.sendEvent(eventName, eventData);
        }

        @Override
        public void onCallRejectByUser(QBRTCSession session, Integer userId, Map<String, String> userInfo) {
            String eventName = WebRTCConstants.Events.REJECT;

            Map<String, Object> payload = new HashMap<>();

            Map sessionMap = WebRTCMapper.qBRTCSessionToMap(session);
            Map userInfoMap = WebRTCMapper.userInfoToMap(session);

            payload.put("session", sessionMap);
            payload.put("userId", userId);
            payload.put("userInfo", userInfoMap);

            Map eventData = EventsUtil.buildResult(eventName, payload);

            EventHandler.sendEvent(eventName, eventData);
        }

        @Override
        public void onCallAcceptByUser(QBRTCSession session, Integer userId, Map<String, String> userInfo) {
            String eventName = WebRTCConstants.Events.ACCEPT;

            Map<String, Object> payload = new HashMap<>();

            Map sessionMap = WebRTCMapper.qBRTCSessionToMap(session);
            Map userInfoMap = WebRTCMapper.userInfoToMap(session);

            payload.put("session", sessionMap);
            payload.put("userId", userId);
            payload.put("userInfo", userInfoMap);

            Map eventData = EventsUtil.buildResult(eventName, payload);

            EventHandler.sendEvent(eventName, eventData);

            startCallTimer();
        }

        @Override
        public void onReceiveHangUpFromUser(QBRTCSession session, Integer userId, Map<String, String> userInfo) {

            stopCallTimer();

            String notificationTitle = getString(R.string.notification_title_ready);
            String notificationText = "";

            updateNotification(notificationTitle, notificationText);

            String eventName = WebRTCConstants.Events.HANG_UP;

            Map<String, Object> payload = new HashMap<>();

            Map sessionMap = WebRTCMapper.qBRTCSessionToMap(session);
            Map userInfoMap = WebRTCMapper.userInfoToMap(session);

            payload.put("session", sessionMap);
            payload.put("userId", userId);
            payload.put("userInfo", userInfoMap);

            Map eventData = EventsUtil.buildResult(eventName, payload);

            EventHandler.sendEvent(eventName, eventData);
        }

        @Override
        public void onSessionClosed(QBRTCSession session) {
            session.removeVideoTrackCallbacksListener(videoTrackListener);
            sessionCache.remove(session);

            String eventName = WebRTCConstants.Events.CALL_END;

            Map<String, Object> payload = new HashMap<>();
            Map sessionMap = WebRTCMapper.qBRTCSessionToMap(session);
            payload.put("session", sessionMap);

            Map eventData = EventsUtil.buildResult(eventName, payload);

            EventHandler.sendEvent(eventName, eventData);
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // VIDEO TRACK LISTENER
    ///////////////////////////////////////////////////////////////////////////
    private class VideoTrackListener implements QBRTCClientVideoTracksCallbacks<QBRTCSession> {
        @Override
        public void onLocalVideoTrackReceive(QBRTCSession session, QBRTCVideoTrack videoTrack) {
            int userId = QBChatService.getInstance().getUser().getId();
            Log.d(TAG, "onLocalVideoTrackReceive() for session: " + session.getSessionID());

            Map videoTrackMap = WebRTCMapper.qBRTCVideoTrackToMap(videoTrack, userId);

            EventHandler.sendEvent(WebRTCConstants.Events.RECEIVED_VIDEO_TRACK,
                    EventsUtil.buildResult(WebRTCConstants.Events.RECEIVED_VIDEO_TRACK, videoTrackMap));
        }

        @Override
        public void onRemoteVideoTrackReceive(QBRTCSession session, QBRTCVideoTrack videoTrack, Integer userId) {
            Log.d(TAG, "onRemoteVideoTrackReceive for session:  " + session.getSessionID() + ", for user: " + userId);

            Map videoTrackMap = WebRTCMapper.qBRTCVideoTrackToMap(videoTrack, userId);

            EventHandler.sendEvent(WebRTCConstants.Events.RECEIVED_VIDEO_TRACK,
                    EventsUtil.buildResult(WebRTCConstants.Events.RECEIVED_VIDEO_TRACK, videoTrackMap));
        }
    }

    interface ServiceCallback<T> {
        void onSuccess(T value);

        void onError(String errorMessage);
    }
}