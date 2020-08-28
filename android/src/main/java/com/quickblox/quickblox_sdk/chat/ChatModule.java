package com.quickblox.quickblox_sdk.chat;

import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.quickblox.auth.session.QBSessionManager;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBMessageStatusesManager;
import com.quickblox.chat.QBRestChatService;
import com.quickblox.chat.exception.QBChatException;
import com.quickblox.chat.listeners.QBChatDialogMessageListener;
import com.quickblox.chat.listeners.QBChatDialogTypingListener;
import com.quickblox.chat.listeners.QBMessageStatusListener;
import com.quickblox.chat.listeners.QBSystemMessageListener;
import com.quickblox.chat.model.QBAttachment;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.chat.request.QBDialogRequestBuilder;
import com.quickblox.chat.request.QBMessageGetBuilder;
import com.quickblox.chat.utils.DialogUtils;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.request.QBRequestGetBuilder;
import com.quickblox.quickblox_sdk.QuickbloxSdkPlugin;
import com.quickblox.quickblox_sdk.base.BaseModule;
import com.quickblox.quickblox_sdk.base.EventHandler;
import com.quickblox.quickblox_sdk.utils.EventsUtil;
import com.quickblox.users.model.QBUser;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.muc.DiscussionHistory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;

/**
 * Created by Injoit on 2019-12-27.
 * Copyright © 2019 Quickblox. All rights reserved.
 */
public class ChatModule implements BaseModule {
    static final String CHANNEL_NAME = "FlutterQBChatChannel";

    private static final String CONNECT_METHOD = "connect";
    private static final String DISCONNECT_METHOD = "disconnect";
    private static final String IS_CONNECTED_METHOD = "isConnected";
    private static final String PING_SERVER_METHOD = "pingServer";
    private static final String PING_USER_METHOD = "pingUser";
    private static final String GET_DIALOGS_METHOD = "getDialogs";
    private static final String GET_DIALOGS_COUNT_METHOD = "getDialogsCount";
    private static final String UPDATE_DIALOG_METHOD = "updateDialog";
    private static final String CREATE_DIALOG_METHOD = "createDialog";
    private static final String DELETE_DIALOG_METHOD = "deleteDialog";
    private static final String LEAVE_DIALOG_METHOD = "leaveDialog";
    private static final String JOIN_DIALOG_METHOD = "joinDialog";
    private static final String GET_ONLINE_USERS_METHOD = "getOnlineUsers";
    private static final String SEND_MESSAGE_METHOD = "sendMessage";
    private static final String SEND_SYSTEM_MESSAGE_METHOD = "sendSystemMessage";
    private static final String MARK_MESSAGE_READ_METHOD = "markMessageRead";
    private static final String MARK_MESSAGE_DELIVERED_METHOD = "markMessageDelivered";
    private static final String SEND_IS_TYPING_METHOD = "sendIsTyping";
    private static final String SEND_STOPPED_TYPING_METHOD = "sendStoppedTyping";
    private static final String GET_DIALOG_MESSAGES_METHOD = "getDialogMessages";

    private Set<QBChatDialog> dialogsCache = new QBDialogsSet(new QBDialogSetAddListenerImpl());
    private SystemMessageListener systemMessageListener;
    private QBMessageStatusListener messageStatusListener;
    private IncomingMessageListener incomingMessageListener;
    private ConnectionListener connectionListener;

    private QuickbloxSdkPlugin.ModuleEvents moduleEvents;

    private BinaryMessenger binaryMessenger;

    public ChatModule(BinaryMessenger binaryMessenger, QuickbloxSdkPlugin.ModuleEvents moduleEvents) {
        this.binaryMessenger = binaryMessenger;
        this.moduleEvents = moduleEvents;
        initEventHandler();
    }

    @Override
    public void initEventHandler() {
        EventHandler.init(ChatConstants.getAllEvents(), binaryMessenger);
    }

    @Override
    public String getChannelName() {
        return CHANNEL_NAME;
    }

    @Override
    public MethodChannel.MethodCallHandler getMethodHandler() {
        return this::handleMethod;
    }

    @Override
    public void handleMethod(MethodCall methodCall, MethodChannel.Result result) {
        Map<String, Object> data = methodCall.arguments();
        switch (methodCall.method) {
            case CONNECT_METHOD:
                connect(data, result);
                break;
            case DISCONNECT_METHOD:
                disconnect(result);
                break;
            case IS_CONNECTED_METHOD:
                isConnected(result);
                break;
            case PING_SERVER_METHOD:
                pingServer(result);
                break;
            case PING_USER_METHOD:
                pingUser(data, result);
                break;
            case GET_DIALOGS_METHOD:
                getDialogs(data, result);
                break;
            case GET_DIALOGS_COUNT_METHOD:
                getDialogsCount(data, result);
                break;
            case UPDATE_DIALOG_METHOD:
                updateDialog(data, result);
                break;
            case CREATE_DIALOG_METHOD:
                createDialog(data, result);
                break;
            case DELETE_DIALOG_METHOD:
                deleteDialog(data, result);
                break;
            case LEAVE_DIALOG_METHOD:
                leaveDialog(data, result);
                break;
            case JOIN_DIALOG_METHOD:
                joinDialog(data, result);
                break;
            case GET_ONLINE_USERS_METHOD:
                getOnlineUsers(data, result);
                break;
            case SEND_MESSAGE_METHOD:
                sendMessage(data, result);
                break;
            case SEND_SYSTEM_MESSAGE_METHOD:
                sendSystemMessage(data, result);
                break;
            case MARK_MESSAGE_READ_METHOD:
                markMessageRead(data, result);
                break;
            case MARK_MESSAGE_DELIVERED_METHOD:
                markMessageDelivered(data, result);
                break;
            case SEND_IS_TYPING_METHOD:
                sendIsTyping(data, result);
                break;
            case SEND_STOPPED_TYPING_METHOD:
                sendStoppedTyping(data, result);
                break;
            case GET_DIALOG_MESSAGES_METHOD:
                getDialogMessages(data, result);
                break;
        }
    }

    public void onInitCredentials() {
        addConnectionListener();
    }

    private void addIncomingMessageListener() {
        if (incomingMessageListener == null) {
            incomingMessageListener = new IncomingMessageListener();
            QBChatService.getInstance().getIncomingMessagesManager().addDialogMessageListener(incomingMessageListener);
        }
    }

    private void removeIncomingMessageListener() {
        if (incomingMessageListener != null) {
            QBChatService.getInstance().getIncomingMessagesManager().removeDialogMessageListrener(incomingMessageListener);
            incomingMessageListener = null;
        }
    }

    private void addConnectionListener() {
        if (connectionListener == null) {
            connectionListener = new ConnectionListener();
            QBChatService.getInstance().addConnectionListener(connectionListener);
        }
    }

    private void removeConnectionListener() {
        if (connectionListener != null) {
            QBChatService.getInstance().removeConnectionListener(connectionListener);
            connectionListener = null;
        }
    }

    private void addSystemMessageListener() {
        if (systemMessageListener == null) {
            systemMessageListener = new SystemMessageListener();
            QBChatService.getInstance().getSystemMessagesManager().addSystemMessageListener(systemMessageListener);
        }
    }

    private void removeSystemMessageListener() {
        if (systemMessageListener != null) {
            QBChatService.getInstance().getSystemMessagesManager().removeSystemMessageListener(systemMessageListener);
            systemMessageListener = null;
        }
    }

    private synchronized void addStatusMessageListener() {
        if (messageStatusListener == null) {
            messageStatusListener = new StatusMessageListener();
            QBMessageStatusesManager manager = QBChatService.getInstance().getMessageStatusesManager();
            manager.addMessageStatusListener(messageStatusListener);
        }
    }

    private void removeStatusMessageListener() {
        if (messageStatusListener != null) {
            QBChatService.getInstance().getMessageStatusesManager().removeMessageStatusListener(messageStatusListener);
            messageStatusListener = null;
        }
    }

    private QBChatDialog getDialogFromCache(String dialogId) {
        QBChatDialog dialog = null;
        for (QBChatDialog chatDialog : dialogsCache) {
            if (chatDialog.getDialogId().equals(dialogId)) {
                dialog = chatDialog;
                break;
            }
        }
        return dialog;
    }

    private void connect(Map<String, Object> data, final MethodChannel.Result result) {
        Integer userId = data != null && data.containsKey("userId") ? (Integer) data.get("userId") : null;
        String password = data != null && data.containsKey("password") ? (String) data.get("password") : null;

        if (userId == null || TextUtils.isEmpty(password)) {
            result.error("The userId, password are required parameters", null, null);
            return;
        }

        QBUser user = new QBUser();
        if (!TextUtils.isEmpty(password)) {
            user.setPassword(password);
        }
        user.setId(userId);
        QBChatService.getInstance().setUseStreamManagement(true);
        QBChatService.getInstance().login(user, new QBEntityCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid, Bundle bundle) {
                addSystemMessageListener();
                addStatusMessageListener();
                addIncomingMessageListener();
                moduleEvents.onChatConnected();
                result.success(aVoid);
            }

            @Override
            public void onError(QBResponseException e) {
                result.error(e.getMessage(), null, null);
            }
        });
    }

    private void disconnect(final MethodChannel.Result result) {
        removeSystemMessageListener();
        QBChatService.getInstance().logout(new QBEntityCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid, Bundle bundle) {
                moduleEvents.onChatDisconnected();
                dialogsCache.clear();
                result.success(aVoid);
            }

            @Override
            public void onError(QBResponseException e) {
                result.error(e.getMessage(), null, null);
            }
        });
    }

    private void isConnected(final MethodChannel.Result result) {
        boolean connected = QBChatService.getInstance().isLoggedIn();
        result.success(connected);
    }

    private void pingServer(final MethodChannel.Result result) {
        QBChatService.getInstance().getPingManager().pingServer(new QBEntityCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid, Bundle bundle) {
                result.success(aVoid);
            }

            @Override
            public void onError(QBResponseException e) {
                result.error(e.getMessage(), null, null);
            }
        });
    }

    private void pingUser(Map<String, Object> data, final MethodChannel.Result result) {
        Integer userId = data != null && data.containsKey("userId") ? (Integer) data.get("userId") : null;

        if (userId == null || userId <= 0) {
            result.error("The userId is required parameter", null, null);
            return;
        }

        QBChatService.getInstance().getPingManager().pingUser(userId, new QBEntityCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid, Bundle bundle) {
                result.success(aVoid);
            }

            @Override
            public void onError(QBResponseException e) {
                result.error(e.getMessage(), null, null);
            }
        });
    }

    private void getDialogs(Map<String, Object> data, final MethodChannel.Result result) {
        Map sortMap = data != null && data.containsKey("sort") ? (Map) data.get("sort") : null;
        Map filterMap = data != null && data.containsKey("filter") ? (Map) data.get("filter") : null;
        int limit = data != null && data.containsKey("limit") ? (int) data.get("limit") : 100;
        int skip = data != null && data.containsKey("skip") ? (int) data.get("skip") : 0;

        QBRequestGetBuilder qbRequestGetBuilder = new QBRequestGetBuilder();

        ChatMapper.addDialogFilterToRequestBuilder(qbRequestGetBuilder, filterMap);
        ChatMapper.addDialogSortToRequestBuilder(qbRequestGetBuilder, sortMap);

        qbRequestGetBuilder.setLimit(limit);
        qbRequestGetBuilder.setSkip(skip);

        QBRestChatService.getChatDialogs(null, qbRequestGetBuilder).performAsync(new QBEntityCallback<ArrayList<QBChatDialog>>() {
            @Override
            public void onSuccess(ArrayList<QBChatDialog> qbChatDialogs, Bundle bundle) {
                Map<String, Object> payload = new HashMap<>();

                if (dialogsCache.containsAll(qbChatDialogs)) {
                    dialogsCache.removeAll(qbChatDialogs);
                }

                dialogsCache.addAll(qbChatDialogs);
                List<Map> dialogsList = new ArrayList<>();
                for (QBChatDialog qbDialog : qbChatDialogs) {
                    Map dialog = ChatMapper.qbChatDialogToMap(qbDialog);
                    dialogsList.add(dialog);
                }

                payload.put("dialogs", dialogsList);
                payload.put("skip", bundle.containsKey("skip") ? bundle.getInt("skip") : skip);
                payload.put("limit", bundle.containsKey("limit") ? bundle.getInt("limit") : limit);
                payload.put("total", bundle.containsKey("total_entries") ? bundle.getInt("total_entries") : -1);

                result.success(payload);
            }

            @Override
            public void onError(QBResponseException e) {
                result.error(e.getMessage(), null, null);
            }
        });
    }

    private void getDialogsCount(Map<String, Object> data, final MethodChannel.Result result) {
        Map filterMap = data != null && data.containsKey("filter") ? (Map) data.get("filter") : null;
        int limit = data != null && data.containsKey("limit") ? (int) data.get("limit") : 100;
        int skip = data != null && data.containsKey("skip") ? (int) data.get("skip") : 0;

        QBRequestGetBuilder qbRequestGetBuilder = new QBRequestGetBuilder();

        ChatMapper.addDialogFilterToRequestBuilder(qbRequestGetBuilder, filterMap);

        qbRequestGetBuilder.setLimit(limit);
        qbRequestGetBuilder.setSkip(skip);

        QBRestChatService.getChatDialogsCount(qbRequestGetBuilder, new Bundle()).performAsync(new QBEntityCallback<Integer>() {
            @Override
            public void onSuccess(Integer countOfDialogs, Bundle bundle) {
                result.success(countOfDialogs);
            }

            @Override
            public void onError(QBResponseException e) {
                result.error(e.getMessage(), null, null);
            }
        });
    }

    private void updateDialog(Map<String, Object> data, final MethodChannel.Result result) {
        final String dialogId = data != null && data.containsKey("dialogId") ? (String) data.get("dialogId") : null;
        List<Integer> addUsers = data != null && data.containsKey("addUsers") ? (List<Integer>) data.get("addUsers") : null;
        List<Integer> removeUsers = data != null && data.containsKey("  removeUsers") ? (List<Integer>) data.get("  removeUsers") : null;
        String dialogName = data != null && data.containsKey("name") ? (String) data.get("name") : null;

        if (TextUtils.isEmpty(dialogId)) {
            result.error("Parameter dialogId is required", null, null);
            return;
        }

        QBChatDialog chatDialog = getDialogFromCache(dialogId);
        if (chatDialog != null) {
            updateDialog(dialogId, chatDialog, dialogName, removeUsers, addUsers, result);
        } else {
            QBRestChatService.getChatDialogById(dialogId).performAsync(new QBEntityCallback<QBChatDialog>() {
                @Override
                public void onSuccess(QBChatDialog chatDialog, Bundle bundle) {
                    updateDialog(dialogId, chatDialog, dialogName, removeUsers, addUsers, result);
                }

                @Override
                public void onError(QBResponseException e) {
                    e.printStackTrace();
                    result.error(e.getMessage(), null, null);
                }
            });
        }
    }

    private void updateDialog(String dialogId, QBChatDialog chatDialog, String dialogName, List<Integer> removeUsers, List<Integer> addUsers, MethodChannel.Result result) {
        if (!TextUtils.isEmpty(dialogName)) {
            chatDialog.setName(dialogName);
        }

        if (removeUsers != null && removeUsers.size() > 0) {
            removeUsersFromDialogs(removeUsers, chatDialog, result);
        }

        if (addUsers != null && addUsers.size() > 0) {
            addUsersToDialog(addUsers, chatDialog, result);
        }

        Map dialog = ChatMapper.qbChatDialogToMap(chatDialog);
        result.success(dialog);

        dialogsCache.remove(new QBChatDialog(dialogId));
        dialogsCache.add(chatDialog);
    }

    private void removeUsersFromDialogs(List<Integer> users, QBChatDialog qbChatDialog, MethodChannel.Result result) {
        QBDialogRequestBuilder requestBuilder = new QBDialogRequestBuilder();
        for (int userId : users) {
            requestBuilder.removeUsers(userId);
        }

        QBRestChatService.updateChatDialog(qbChatDialog, requestBuilder).performAsync(new QBEntityCallback<QBChatDialog>() {
            @Override
            public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {

            }

            @Override
            public void onError(QBResponseException e) {
                e.printStackTrace();
                result.error(e.getMessage(), null, null);
            }
        });
    }

    private void addUsersToDialog(List<Integer> users, QBChatDialog qbChatDialog, MethodChannel.Result result) {
        QBDialogRequestBuilder requestBuilder = new QBDialogRequestBuilder();
        for (int userId : users) {
            requestBuilder.addUsers(userId);
        }

        QBRestChatService.updateChatDialog(qbChatDialog, requestBuilder).performAsync(new QBEntityCallback<QBChatDialog>() {
            @Override
            public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {

            }

            @Override
            public void onError(QBResponseException e) {
                result.error(e.getMessage(), null, null);
            }
        });
    }

    private void createDialog(Map<String, Object> data, final MethodChannel.Result result) {
        List<Integer> occupantsIds = data != null && data.containsKey("occupantsIds") ? (List<Integer>) data.get("occupantsIds") : null;
        String chatName = data != null && data.containsKey("name") ? (String) data.get("name") : null;
        Integer type = data != null && data.containsKey("type") ? (Integer) data.get("type") : null;

        if (occupantsIds == null || occupantsIds.size() == 0 || TextUtils.isEmpty(chatName)) {
            result.error("Parameters occupantsIds and name are required", null, null);
            return;
        }

        //add current user id
        Integer currentUserId = QBSessionManager.getInstance().getSessionParameters().getUserId();
        if (!occupantsIds.isEmpty() && !occupantsIds.contains(currentUserId)) {
            occupantsIds.add(currentUserId);
        }

        QBDialogType dialogType;

        if (type != null) {
            dialogType = QBDialogType.parseByCode(type);
        } else if (occupantsIds.size() > 2) {
            dialogType = (QBDialogType.GROUP);
        } else if (occupantsIds.size() == 2) {
            dialogType = (QBDialogType.PRIVATE);
        } else {
            dialogType = (QBDialogType.PUBLIC_GROUP);
        }

        QBChatDialog qbChatDialog = DialogUtils.buildDialog(chatName, dialogType, occupantsIds);

        QBRestChatService.createChatDialog(qbChatDialog).performAsync(new QBEntityCallback<QBChatDialog>() {
            @Override
            public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {
                dialogsCache.add(qbChatDialog);
                Map dialog = ChatMapper.qbChatDialogToMap(qbChatDialog);
                result.success(dialog);
            }

            @Override
            public void onError(QBResponseException e) {
                result.error(e.getMessage(), null, null);
            }
        });
    }

    private void deleteDialog(Map<String, Object> data, final MethodChannel.Result result) {
        final String dialogId = data != null && data.containsKey("dialogId") ? (String) data.get("dialogId") : null;

        if (TextUtils.isEmpty(dialogId)) {
            result.error("Parameter dialogId is required", null, null);
            return;
        }

        QBRestChatService.deleteDialog(dialogId, false).performAsync(new QBEntityCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid, Bundle bundle) {
                QBChatDialog dialog = getDialogFromCache(dialogId);

                if (dialog == null) {
                    dialog = new QBChatDialog(dialogId);
                    dialog.setType(QBDialogType.GROUP);
                }

                dialogsCache.remove(dialog);
                result.success(aVoid);
            }

            @Override
            public void onError(QBResponseException e) {
                result.error(e.getMessage(), null, null);
            }
        });
    }

    private void leaveDialog(Map<String, Object> data, final MethodChannel.Result result) {
        String dialogId = data != null && data.containsKey("dialogId") ? (String) data.get("dialogId") : null;

        if (TextUtils.isEmpty(dialogId)) {
            result.error("Parameter dialogId is required", null, null);
            return;
        }

        QBChatDialog dialog = getDialogFromCache(dialogId);

        if (dialog != null) {
            leaveDialog(dialog, result);
        } else {
            QBRestChatService.getChatDialogById(dialogId).performAsync(new QBEntityCallback<QBChatDialog>() {
                @Override
                public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {
                    leaveDialog(qbChatDialog, result);
                }

                @Override
                public void onError(QBResponseException e) {
                    result.error(e.getMessage(), e.getMessage(), null);
                }
            });
        }
    }

    private void leaveDialog(QBChatDialog dialog, MethodChannel.Result result) {
        try {
            dialog.leave();
            dialogsCache.remove(dialog);
            result.success(null);
        } catch (Exception e) {
            e.printStackTrace();
            result.error(e.getMessage(), null, null);
        }
    }

    private void joinDialog(Map<String, Object> data, final MethodChannel.Result result) {
        String dialogId = data != null && data.containsKey("dialogId") ? (String) data.get("dialogId") : null;

        if (TextUtils.isEmpty(dialogId)) {
            result.error("Parameter dialogId is required", null, null);
            return;
        }

        QBChatDialog dialog = getDialogFromCache(dialogId);

        if (dialog != null) {
            joinDialog(dialog, result);
        } else {
            QBRestChatService.getChatDialogById(dialogId).performAsync(new QBEntityCallback<QBChatDialog>() {
                @Override
                public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {
                    joinDialog(qbChatDialog, result);
                }

                @Override
                public void onError(QBResponseException e) {
                    result.error(e.getMessage(), e.getMessage(), null);
                }
            });
        }
    }

    private void joinDialog(QBChatDialog dialog, MethodChannel.Result result) {
        if (dialog.isPrivate()) {
            result.error("The private dialog should not be joined", null, null);
            return;
        }

        DiscussionHistory history = new DiscussionHistory();
        history.setMaxStanzas(0);
        dialog.join(history, new QBEntityCallback() {
            @Override
            public void onSuccess(Object o, Bundle bundle) {
                result.success(null);
            }

            @Override
            public void onError(QBResponseException e) {
                result.error(e.getMessage(), null, null);
            }
        });
    }

    private void getOnlineUsers(Map<String, Object> data, final MethodChannel.Result result) {
        String dialogId = data != null && data.containsKey("dialogId") ? (String) data.get("dialogId") : null;

        if (TextUtils.isEmpty(dialogId)) {
            result.error("Parameter dialogId is required", null, null);
            return;
        }

        QBChatDialog dialog = getDialogFromCache(dialogId);

        if (dialog != null) {
            getOnlineUsers(dialog, result);
        } else {
            QBRestChatService.getChatDialogById(dialogId).performAsync(new QBEntityCallback<QBChatDialog>() {
                @Override
                public void onSuccess(QBChatDialog dialog, Bundle bundle) {
                    getOnlineUsers(dialog, result);
                }

                @Override
                public void onError(QBResponseException e) {
                    result.error(e.getMessage(), e.getMessage(), null);
                }
            });
        }
    }

    private void getOnlineUsers(QBChatDialog dialog, MethodChannel.Result result) {
        try {
            List<Integer> users = (List<Integer>) dialog.requestOnlineUsers();
            result.success(users);
        } catch (Exception e) {
            e.printStackTrace();
            result.error(e.getMessage(), null, null);
        }
    }

    private void sendMessage(Map<String, Object> data, final MethodChannel.Result result) {
        final String dialogId = data != null && data.containsKey("dialogId") ? (String) data.get("dialogId") : null;
        final String body = data != null && data.containsKey("body") ? (String) data.get("body") : null;
        final List<Map> attachments = data != null && data.containsKey("attachments") ? (List<Map>) data.get("attachments") : null;
        final Map<String, Object> properties = data != null && data.containsKey("properties") ? (Map) data.get("properties") : null;
        final boolean markable = data != null && data.containsKey("markable") && (boolean) data.get("markable");
        final long dateSent = data != null && (data.containsKey("dateSent") && data.get("dateSent") != null) ? (long) data.get("dateSent") : System.currentTimeMillis() / 1000;
        final boolean saveToHistory = data != null && (data.containsKey("saveToHistory") && data.get("saveToHistory") != null) && (boolean) data.get("saveToHistory");

        if (TextUtils.isEmpty(dialogId)) {
            result.error("Parameter dialogId is required", null, null);
            return;
        }

        final QBChatMessage qbChatMessage = new QBChatMessage();

        List<QBAttachment> qbAttachmentList = new ArrayList<>();

        if (properties != null && properties.size() > 0) {
            for (Map.Entry<String, Object> entry : properties.entrySet()) {
                String propertyName = entry.getKey();
                Object propertyValue = entry.getValue();
                qbChatMessage.setComplexProperty(propertyName, propertyValue);
            }
        }

        if (attachments != null && attachments.size() > 0) {
            for (Map attachmentMap : attachments) {
                if (attachmentMap == null) {
                    continue;
                }

                String attachmentType = attachmentMap.containsKey("type") ? (String) attachmentMap.get("type") : null;
                String attachmentId = attachmentMap.containsKey("id") ? (String) attachmentMap.get("id") : null;
                String attachmentUrl = attachmentMap.containsKey("url") ? (String) attachmentMap.get("url") : null;
                String attachmentName = attachmentMap.containsKey("name") ? (String) attachmentMap.get("name") : null;
                String contentType = attachmentMap.containsKey("contentType") ? (String) attachmentMap.get("contentType") : null;
                String attachmentData = attachmentMap.containsKey("data") ? (String) attachmentMap.get("data") : null;
                Integer attachmentSize = attachmentMap.containsKey("size") ? (Integer) attachmentMap.get("size") : null;
                Integer attachmentHeight = attachmentMap.containsKey("height") ? (Integer) attachmentMap.get("height") : null;
                Integer attachmentWidth = attachmentMap.containsKey("width") ? (Integer) attachmentMap.get("width") : null;
                Integer attachmentDuration = attachmentMap.containsKey("duration") ? (Integer) attachmentMap.get("duration") : null;

                if (!TextUtils.isEmpty(attachmentType)) {
                    QBAttachment attachment = new QBAttachment(attachmentType);

                    if (!TextUtils.isEmpty(attachmentId)) {
                        attachment.setId(attachmentId);
                    }
                    if (!TextUtils.isEmpty(attachmentUrl)) {
                        attachment.setUrl(attachmentUrl);
                    }
                    if (!TextUtils.isEmpty(attachmentName)) {
                        attachment.setName(attachmentName);
                    }
                    if (!TextUtils.isEmpty(contentType)) {
                        attachment.setContentType(contentType);
                    }
                    if (!TextUtils.isEmpty(attachmentData)) {
                        attachment.setData(attachmentData);
                    }
                    if (attachmentSize != null && attachmentSize > 0) {
                        attachment.setSize(attachmentSize);
                    }
                    if (attachmentHeight != null && attachmentHeight > 0) {
                        attachment.setHeight(attachmentHeight);
                    }
                    if (attachmentWidth != null && attachmentWidth > 0) {
                        attachment.setWidth(attachmentWidth);
                    }
                    if (attachmentDuration != null && attachmentDuration > 0) {
                        attachment.setDuration(attachmentDuration);
                    }

                    qbAttachmentList.add(attachment);
                }
            }
        }

        qbChatMessage.setBody(body);
        qbChatMessage.setMarkable(markable);
        qbChatMessage.setSaveToHistory(saveToHistory);
        qbChatMessage.setDateSent(dateSent);
        qbChatMessage.setAttachments(qbAttachmentList);

        final QBChatDialog dialog = getDialogFromCache(dialogId);

        if (dialog != null) {
            sendMessage(dialogId, dialog, qbChatMessage, result);
        } else {
            QBRestChatService.getChatDialogById(dialogId).performAsync(new QBEntityCallback<QBChatDialog>() {
                @Override
                public void onSuccess(QBChatDialog dialog, Bundle bundle) {
                    dialogsCache.add(dialog);
                    sendMessage(dialogId, dialog, qbChatMessage, result);
                }

                @Override
                public void onError(QBResponseException e) {
                    result.error(e.getMessage(), e.getMessage(), null);
                }
            });
        }
    }

    private void sendMessage(String dialogId, QBChatDialog dialog, QBChatMessage qbChatMessage, MethodChannel.Result result) {
        if (!dialog.isJoined() && !dialog.getType().equals(QBDialogType.PRIVATE)) {
            DiscussionHistory history = new DiscussionHistory();
            history.setMaxStanzas(0);

            dialog.initForChat(QBChatService.getInstance());

            try {
                dialog.join(history);
            } catch (XMPPException | SmackException e) {
                e.printStackTrace();
                result.error(e.getMessage(), null, null);
                return;
            }
        }

        if (dialog.getType().equals(QBDialogType.PRIVATE)) {
            qbChatMessage.setRecipientId(dialog.getRecipientId());
        }

        if (!dialog.getType().equals(QBDialogType.PUBLIC_GROUP)) {
            //current user id
            Integer userId = QBSessionManager.getInstance().getSessionParameters().getUserId();

            Collection<Integer> idsList = new ArrayList<>();
            idsList.add(userId);

            qbChatMessage.setDeliveredIds(idsList);
            qbChatMessage.setReadIds(idsList);
        }

        dialog.sendMessage(qbChatMessage, new QBEntityCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid, Bundle bundle) {
                result.success(aVoid);

                qbChatMessage.setDialogId(dialogId);

                //timestamp
                long timeStamp = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
                qbChatMessage.setDateSent(timeStamp);

                //current user id
                Integer userId = QBSessionManager.getInstance().getSessionParameters().getUserId();
                qbChatMessage.setSenderId(userId);

                Map message = ChatMapper.qbChatMessageToMap(qbChatMessage);
                String eventName = ChatConstants.Events.RECEIVED_NEW_MESSAGE;
                Map data = EventsUtil.buildResult(eventName, message);
                EventHandler.sendEvent(eventName, data);
            }

            @Override
            public void onError(QBResponseException e) {
                result.error(e.getMessage(), null, null);
            }
        });
    }

    private void sendSystemMessage(Map<String, Object> data, final MethodChannel.Result result) {
        Integer recipientId = data != null && data.containsKey("recipientId") ? (Integer) data.get("recipientId") : null;
        Map<String, Object> properties = data != null && data.containsKey("properties") ? (Map) data.get("properties") : null;

        if (recipientId == null || recipientId <= 0) {
            result.error("Parameter recipientId is required", null, null);
            return;
        }

        QBChatMessage qbChatMessage = new QBChatMessage();
        qbChatMessage.setRecipientId(recipientId);

        if (properties != null) {
            for (Map.Entry<String, Object> entry : properties.entrySet()) {
                String propertyName = entry.getKey();
                Object propertyValue = entry.getValue();
                qbChatMessage.setProperty(propertyName, String.valueOf(propertyValue));
            }
        }

        QBChatService.getInstance().getSystemMessagesManager().sendSystemMessage(qbChatMessage, new QBEntityCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid, Bundle bundle) {
                result.success(aVoid);
            }

            @Override
            public void onError(QBResponseException e) {
                result.error(e.getMessage(), null, null);
            }
        });
    }

    private void markMessageRead(Map<String, Object> data, final MethodChannel.Result result) {
        Map<String, Object> messageMap = data != null && data.containsKey("message") ? (Map) data.get("message") : null;

        if (messageMap == null) {
            result.error("The parameter message is required", null, null);
            return;
        }

        QBChatMessage chatMessage = ChatMapper.mapToQBChatMessage(messageMap);

        String dialogId = chatMessage.getDialogId();

        QBChatDialog dialog = getDialogFromCache(dialogId);
        if (dialog != null) {
            readMessage(dialog, chatMessage, result);
        } else {
            QBRestChatService.getChatDialogById(dialogId).performAsync(new QBEntityCallback<QBChatDialog>() {
                @Override
                public void onSuccess(QBChatDialog dialog, Bundle bundle) {
                    dialogsCache.remove(dialog);
                    dialogsCache.add(dialog);
                    readMessage(dialog, chatMessage, result);
                }

                @Override
                public void onError(QBResponseException e) {
                    e.printStackTrace();
                    result.error(e.getMessage(), null, null);
                }
            });
        }
    }

    private void readMessage(QBChatDialog dialog, QBChatMessage chatMessage, MethodChannel.Result result) {
        try {
            dialog.readMessage(chatMessage);
            result.success(null);
        } catch (Exception e) {
            e.printStackTrace();
            result.error(e.getMessage(), null, null);
        }
    }

    private void markMessageDelivered(Map<String, Object> data, final MethodChannel.Result result) {
        Map<String, Object> messageMap = data != null && data.containsKey("message") ? (Map) data.get("message") : null;

        if (messageMap == null) {
            result.error("The parameter message is required", null, null);
            return;
        }

        QBChatMessage chatMessage = ChatMapper.mapToQBChatMessage(messageMap);

        String dialogId = chatMessage.getDialogId();

        QBChatDialog dialog = getDialogFromCache(dialogId);
        if (dialog != null) {
            deliverMessage(dialog, chatMessage, result);
        } else {
            QBRestChatService.getChatDialogById(dialogId).performAsync(new QBEntityCallback<QBChatDialog>() {
                @Override
                public void onSuccess(QBChatDialog dialog, Bundle bundle) {
                    deliverMessage(dialog, chatMessage, result);
                }

                @Override
                public void onError(QBResponseException e) {
                    e.printStackTrace();
                    result.error(e.getMessage(), null, null);
                }
            });
        }

    }

    private void deliverMessage(QBChatDialog dialog, QBChatMessage chatMessage, MethodChannel.Result result) {
        try {
            dialog.deliverMessage(chatMessage);
            result.success(null);
        } catch (Exception e) {
            e.printStackTrace();
            result.error(e.getMessage(), null, null);
        }
    }

    private void sendIsTyping(Map<String, Object> data, final MethodChannel.Result result) {
        String dialogId = data != null && data.containsKey("dialogId") ? (String) data.get("dialogId") : null;

        if (TextUtils.isEmpty(dialogId)) {
            result.error("Parameter dialogId is required", null, null);
            return;
        }

        QBChatDialog dialog = getDialogFromCache(dialogId);
        if (dialog != null) {
            sendIsTyping(dialog, result);
        } else {
            QBRestChatService.getChatDialogById(dialogId).performAsync(new QBEntityCallback<QBChatDialog>() {
                @Override
                public void onSuccess(QBChatDialog dialog, Bundle bundle) {
                    dialogsCache.add(dialog);
                    sendIsTyping(dialog, result);
                }

                @Override
                public void onError(QBResponseException e) {
                    result.error(e.getMessage(), null, null);
                }
            });
        }
    }

    private void sendIsTyping(QBChatDialog dialog, MethodChannel.Result result) {
        dialog.sendIsTypingNotification(new QBEntityCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid, Bundle bundle) {
                result.success(aVoid);
            }

            @Override
            public void onError(QBResponseException e) {
                result.error(e.getMessage(), null, null);
            }
        });
    }

    private void sendStoppedTyping(Map<String, Object> data, final MethodChannel.Result result) {
        String dialogId = data != null && data.containsKey("dialogId") ? (String) data.get("dialogId") : null;

        if (TextUtils.isEmpty(dialogId)) {
            result.error("Parameter dialogId is required", null, null);
            return;
        }

        QBChatDialog dialog = getDialogFromCache(dialogId);
        if (dialog != null) {
            sendStoppedTyping(dialog, result);
        } else {
            QBRestChatService.getChatDialogById(dialogId).performAsync(new QBEntityCallback<QBChatDialog>() {
                @Override
                public void onSuccess(QBChatDialog dialog, Bundle bundle) {
                    dialogsCache.add(dialog);
                    sendStoppedTyping(dialog, result);
                }

                @Override
                public void onError(QBResponseException e) {
                    result.error(e.getMessage(), null, null);
                }
            });
        }
    }

    private void sendStoppedTyping(QBChatDialog dialog, MethodChannel.Result result) {
        dialog.sendStopTypingNotification(new QBEntityCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid, Bundle bundle) {
                result.success(aVoid);
            }

            @Override
            public void onError(QBResponseException e) {
                result.error(e.getMessage(), null, null);
            }
        });
    }

    private void getDialogMessages(Map<String, Object> data, final MethodChannel.Result result) {
        String dialogId = data != null && data.containsKey("dialogId") ? (String) data.get("dialogId") : null;
        Map sortMap = data != null && data.containsKey("sort") ? (Map) data.get("sort") : null;
        Map filterMap = data != null && data.containsKey("filter") ? (Map) data.get("filter") : null;
        int limit = data != null && data.containsKey("limit") ? (int) data.get("limit") : 100;
        int skip = data != null && data.containsKey("skip") ? (int) data.get("skip") : 0;
        boolean markAsRead = data != null && data.containsKey("markAsRead") && (boolean) data.get("markAsRead");

        if (TextUtils.isEmpty(dialogId)) {
            result.error("Parameter dialogId is required", null, null);
            return;
        }

        QBChatDialog dialog = new QBChatDialog(dialogId);
        dialog.initForChat(dialogId, QBDialogType.GROUP, QBChatService.getInstance());

        QBMessageGetBuilder qbRequestGetBuilder = new QBMessageGetBuilder();

        ChatMapper.addMessageFilterToRequestBuilder(qbRequestGetBuilder, filterMap);
        ChatMapper.addMessageSortToRequestBuilder(qbRequestGetBuilder, sortMap);

        qbRequestGetBuilder.setSkip(skip);
        qbRequestGetBuilder.setLimit(limit);
        qbRequestGetBuilder.markAsRead(markAsRead);

        QBRestChatService.getDialogMessages(dialog, qbRequestGetBuilder).performAsync(new QBEntityCallback<ArrayList<QBChatMessage>>() {
            @Override
            public void onSuccess(ArrayList<QBChatMessage> qbChatMessages, Bundle bundle) {
                Map<String, Object> payload = new HashMap<>();

                List<Map> messages = new ArrayList<>();
                for (QBChatMessage qbChatMessage : qbChatMessages) {
                    Map message = ChatMapper.qbChatMessageToMap(qbChatMessage);
                    messages.add(message);
                }
                payload.put("messages", messages);
                payload.put("skip", bundle.containsKey("skip") ? bundle.getInt("skip") : skip);
                payload.put("limit", bundle.containsKey("limit") ? bundle.getInt("limit") : limit);
                payload.put("total", bundle.containsKey("total_entries") ? bundle.getInt("total_entries") : -1);

                result.success(payload);
            }

            @Override
            public void onError(QBResponseException e) {
                result.error(e.getMessage(), null, null);
            }
        });
    }

    ///////////////////////////////////////////////////////////////////////////
    // INCOMING MESSAGE LISTENER
    ///////////////////////////////////////////////////////////////////////////
    private class IncomingMessageListener implements QBChatDialogMessageListener {

        @Override
        public void processMessage(String dialogId, QBChatMessage qbChatMessage, Integer integer) {
            Integer userId = QBSessionManager.getInstance().getSessionParameters().getUserId();

            if (!qbChatMessage.getSenderId().equals(userId)) {
                Map message = ChatMapper.qbChatMessageToMap(qbChatMessage);
                String eventName = ChatConstants.Events.RECEIVED_NEW_MESSAGE;
                Map data = EventsUtil.buildResult(eventName, message);
                EventHandler.sendEvent(eventName, data);
            }
        }

        @Override
        public void processError(String s, QBChatException e, QBChatMessage qbChatMessage, Integer integer) {

        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // CONNECTION LISTENER
    ///////////////////////////////////////////////////////////////////////////
    private class ConnectionListener implements org.jivesoftware.smack.ConnectionListener {

        @Override
        public void connected(XMPPConnection xmppConnection) {
            String eventName = ChatConstants.Events.CONNECTED;
            Map eventData = EventsUtil.buildResult(eventName, null);
            EventHandler.sendEvent(eventName, eventData);
        }

        @Override
        public void authenticated(XMPPConnection xmppConnection, boolean b) {
            //ignore
        }

        @Override
        public void connectionClosed() {
            String eventName = ChatConstants.Events.CONNECTION_CLOSED;
            Map eventData = EventsUtil.buildResult(eventName, null);
            EventHandler.sendEvent(eventName, eventData);
        }

        @Override
        public void connectionClosedOnError(Exception e) {
            String eventName = ChatConstants.Events.RECONNECTION_FAILED;
            Map eventData = EventsUtil.buildResult(eventName, null);
            EventHandler.sendEvent(eventName, eventData);
        }

        @Override
        public void reconnectionSuccessful() {
            new JoinPublicChatJob(new JoinPublicChatJob.PublicJoinChatJobListener() {
                @Override
                public void onSuccess() {
                    String eventName = ChatConstants.Events.RECONNECTION_SUCCESSFUL;
                    Map eventData = EventsUtil.buildResult(eventName, null);
                    EventHandler.sendEvent(eventName, eventData);
                }

                @Override
                public void onError() {
                    String eventName = ChatConstants.Events.RECONNECTION_FAILED;
                    Map eventData = EventsUtil.buildResult(eventName, null);
                    EventHandler.sendEvent(eventName, eventData);
                }
            }).execute(dialogsCache);
        }

        @Override
        public void reconnectingIn(int i) {
            //ignore
        }

        @Override
        public void reconnectionFailed(Exception e) {
            String eventName = ChatConstants.Events.RECONNECTION_FAILED;
            Map eventData = EventsUtil.buildResult(eventName, null);
            EventHandler.sendEvent(eventName, eventData);
        }
    }

    private static class JoinPublicChatJob extends AsyncTask<Set<QBChatDialog>, Void, Void> {
        private PublicJoinChatJobListener jobListener;

        JoinPublicChatJob(PublicJoinChatJobListener jobListener) {
            this.jobListener = jobListener;
        }

        @Override
        protected Void doInBackground(Set<QBChatDialog>... dialogsCache) {
            boolean successReconnected = true;

            if (dialogsCache.length <= 0) {
                return null;
            }

            Set<QBChatDialog> dialogs = dialogsCache[0];

            for (QBChatDialog dialog : dialogs) {
                if (!dialog.isPrivate()) {
                    DiscussionHistory history = new DiscussionHistory();
                    history.setMaxStanzas(0);

                    dialog.initForChat(QBChatService.getInstance());

                    if (!dialog.isJoined()) {
                        try {
                            dialog.join(history);
                        } catch (XMPPException | SmackException e) {
                            e.printStackTrace();
                            successReconnected = false;
                        }
                    }
                }
            }

            if (successReconnected) {
                jobListener.onSuccess();
            } else {
                jobListener.onError();
            }

            return null;
        }

        private interface PublicJoinChatJobListener {

            void onSuccess();

            void onError();
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // SYSTEM MESSAGE LISTENER
    ///////////////////////////////////////////////////////////////////////////
    private class SystemMessageListener implements QBSystemMessageListener {
        @Override
        public void processMessage(QBChatMessage qbChatMessage) {
            String eventName = ChatConstants.Events.RECEIVED_SYSTEM_MESSAGE;
            Map message = ChatMapper.qbChatMessageToMap(qbChatMessage);
            Map data = EventsUtil.buildResult(eventName, message);
            EventHandler.sendEvent(eventName, data);
        }

        @Override
        public void processError(QBChatException e, QBChatMessage qbChatMessage) {
            //ignore
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // STATUS MESSAGE LISTENER
    ///////////////////////////////////////////////////////////////////////////
    private class StatusMessageListener implements QBMessageStatusListener {
        private static final String MESSAGE_ID = "messageId";
        private static final String DIALOG_ID = "dialogId";
        private static final String USER_ID = "userId";

        @Override
        public void processMessageDelivered(String messageId, String dialogId, Integer userId) {
            Map<String, Object> payload = new HashMap<>();
            payload.put(MESSAGE_ID, messageId);
            payload.put(DIALOG_ID, dialogId);
            payload.put(USER_ID, userId);

            String eventName = ChatConstants.Events.MESSAGE_DELIVERED;
            Map data = EventsUtil.buildResult(eventName, payload);
            EventHandler.sendEvent(eventName, data);
        }

        @Override
        public void processMessageRead(String messageId, String dialogId, Integer userId) {
            Map<String, Object> payload = new HashMap<>();
            payload.put(MESSAGE_ID, messageId);
            payload.put(DIALOG_ID, dialogId);
            payload.put(USER_ID, userId);

            String eventName = ChatConstants.Events.MESSAGE_READ;
            Map data = EventsUtil.buildResult(eventName, payload);
            EventHandler.sendEvent(eventName, data);
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // TYPING LISTENER
    ///////////////////////////////////////////////////////////////////////////
    private class TypingListener implements QBChatDialogTypingListener {
        private static final String DIALOG_ID = "dialogId";
        private static final String USER_ID = "userId";

        private String dialogId;

        TypingListener(String dialogId) {
            this.dialogId = dialogId;
        }

        @Override
        public boolean equals(@Nullable Object obj) {
            boolean equals;
            if (obj instanceof TypingListener) {
                equals = this.dialogId.equals(((TypingListener) obj).dialogId);
            } else {
                equals = super.equals(obj);
            }
            return equals;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 53 * hash + dialogId.hashCode();
            return hash;
        }

        @Override
        public void processUserIsTyping(String dialogId, Integer userId) {
            Map<String, Object> payload = new HashMap<>();
            payload.put(USER_ID, userId);
            payload.put(DIALOG_ID, dialogId);

            String eventName = ChatConstants.Events.USER_IS_TYPING;
            Map data = EventsUtil.buildResult(eventName, payload);
            EventHandler.sendEvent(eventName, data);
        }

        @Override
        public void processUserStopTyping(String dialogId, Integer userId) {
            Map<String, Object> payload = new HashMap<>();
            payload.put(USER_ID, userId);
            payload.put(DIALOG_ID, dialogId);

            String eventName = ChatConstants.Events.USER_STOPPED_TYPING;
            Map data = EventsUtil.buildResult(eventName, payload);
            EventHandler.sendEvent(eventName, data);
        }
    }

    private class QBDialogSetAddListenerImpl implements QBDialogsSet.QBDialogSetAddListener {

        @Override
        public void onAdded(QBChatDialog chatDialog) {
            chatDialog.addIsTypingListener(new TypingListener(chatDialog.getDialogId()));
        }
    }
}