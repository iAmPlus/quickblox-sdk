package com.quickblox.quickblox_sdk.chat;

import com.quickblox.chat.listeners.QBChatDialogMessageListener;
import com.quickblox.chat.listeners.QBChatDialogMessageSentListener;
import com.quickblox.chat.listeners.QBChatDialogParticipantListener;
import com.quickblox.chat.listeners.QBChatDialogTypingListener;
import com.quickblox.chat.model.QBChatDialog;

import java.util.Collection;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Created by Injoit on 2020-01-14.
 * Copyright © 2019 Quickblox. All rights reserved.
 */
public class QBDialogsSet extends CopyOnWriteArraySet<QBChatDialog> {

    private QBDialogSetAddListener qbDialogSetAddListener;

    QBDialogsSet(QBDialogSetAddListener qbDialogSetAddListener) {
        this.qbDialogSetAddListener = qbDialogSetAddListener;
    }

    @Override
    public boolean add(QBChatDialog dialog) {

        boolean added = super.add(dialog);
        if (added && qbDialogSetAddListener != null) {
            qbDialogSetAddListener.onAdded(dialog);
        }
        return added;
    }

    @Override
    public boolean addAll(Collection<? extends QBChatDialog> collection) {
        boolean added = super.addAll(collection);
        if (added && qbDialogSetAddListener != null) {
            for (QBChatDialog dialog : collection) {
                qbDialogSetAddListener.onAdded(dialog);
            }
        }
        return added;
    }

    @Override
    public boolean removeAll(Collection<?> collection) {
        if (containsAll(collection)) {
            for (QBChatDialog dialog : (Collection<QBChatDialog>) collection) {
                removeAllListeners(dialog);
            }
        }

        return super.removeAll(collection);
    }

    @Override
    public boolean remove(Object object) {

        if (contains(object)) {
            removeAllListeners(((QBChatDialog) object));
        }

        return super.remove(object);
    }

    private void removeAllListeners(QBChatDialog dialog) {
        if (dialog.getIsTypingListeners() != null && dialog.getIsTypingListeners().size() > 0) {
            for (QBChatDialogTypingListener listener : dialog.getIsTypingListeners()) {
                dialog.removeIsTypingListener(listener);
            }
        }

        if (dialog.getMessageListeners() != null && dialog.getMessageListeners().size() > 0) {
            for (QBChatDialogMessageListener listener : dialog.getMessageListeners()) {
                dialog.removeMessageListrener(listener);
            }
        }

        if (dialog.getMessageSentListeners() != null && dialog.getMessageSentListeners().size() > 0) {
            for (QBChatDialogMessageSentListener listener : dialog.getMessageSentListeners()) {
                dialog.removeMessageSentListener(listener);
            }
        }

        if (dialog.getParticipantListeners() != null && dialog.getParticipantListeners().size() > 0) {
            for (QBChatDialogParticipantListener listener : dialog.getParticipantListeners()) {
                dialog.removeParticipantListener(listener);
            }
        }
    }

    interface QBDialogSetAddListener {
        void onAdded(QBChatDialog chatDialog);
    }
}
