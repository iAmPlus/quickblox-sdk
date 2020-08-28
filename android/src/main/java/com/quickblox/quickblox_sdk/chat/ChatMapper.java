package com.quickblox.quickblox_sdk.chat;

import android.text.TextUtils;

import com.quickblox.chat.model.QBAttachment;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBDialogCustomData;
import com.quickblox.core.request.QBRequestGetBuilder;
import com.quickblox.core.request.QueryRule;
import com.quickblox.quickblox_sdk.utils.DateUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Injoit on 2019-12-27.
 * Copyright © 2019 Quickblox. All rights reserved.
 */
class ChatMapper {

    private ChatMapper() {
        //empty
    }

    static QBChatMessage mapToQBChatMessage(Map<String, Object> map) {
        QBChatMessage message = new QBChatMessage();

        String id = "";
        if (map.containsKey("id")) {
            id = (String) map.get("id");
        }

        Integer senderId = null;
        if (map.containsKey("senderId")) {
            senderId = (Integer) map.get("senderId");
        }

        String dialogId = "";
        if (map.containsKey("dialogId")) {
            dialogId = (String) map.get("dialogId");
        }

        message.setId(id);
        message.setSenderId(senderId);
        message.setDialogId(dialogId);

        return message;
    }

    static Map qbAttachmentToMap(QBAttachment attachment) {
        Map<String, Object> map = new HashMap<>();

        if (!TextUtils.isEmpty(attachment.getType())) {
            map.put("type", attachment.getType());
        }
        if (!TextUtils.isEmpty(attachment.getId())) {
            map.put("id", attachment.getId());
        }
        if (!TextUtils.isEmpty(attachment.getUrl())) {
            map.put("url", attachment.getUrl());
        }
        if (!TextUtils.isEmpty(attachment.getName())) {
            map.put("name", attachment.getName());
        }
        if (!TextUtils.isEmpty(attachment.getContentType())) {
            map.put("contentType", attachment.getContentType());
        }
        if (!TextUtils.isEmpty(attachment.getData())) {
            map.put("data", attachment.getData());
        }
        if (attachment.getSize() > 0) {
            map.put("size", attachment.getSize());
        }
        if (attachment.getHeight() != 0) {
            map.put("height", attachment.getHeight());
        }
        if (attachment.getWidth() != 0) {
            map.put("width", attachment.getWidth());
        }
        if (attachment.getDuration() != 0) {
            map.put("duration", attachment.getDuration());
        }

        return map;
    }

    static Map qbChatMessageToMap(QBChatMessage message) {
        Map<String, Object> map = new HashMap<>();

        if (!TextUtils.isEmpty(message.getId())) {
            map.put("id", message.getId());
        }
        if (message.getAttachments() != null && message.getAttachments().size() > 0) {
            List<Map> attachmentsArray = new ArrayList<>();
            for (QBAttachment qbAttachment : message.getAttachments()) {
                Map attachment = qbAttachmentToMap(qbAttachment);
                attachmentsArray.add(attachment);
            }
            map.put("attachments", attachmentsArray);
        }
        if (message.getProperties() != null && message.getProperties().size() > 0) {
            Map<String, String> writableMap = new HashMap<>();
            for (Map.Entry<String, String> entry : message.getProperties().entrySet()) {
                String propertyName = entry.getKey();
                String propertyValue = entry.getValue();
                writableMap.put(propertyName, propertyValue);
            }
            map.put("properties", writableMap);
        }
        if (message.getDateSent() != 0) {
            map.put("dateSent", message.getDateSent() * 1000);
        }
        if (message.getSenderId() != null && message.getSenderId() > 0) {
            map.put("senderId", message.getSenderId());
        }
        if (message.getRecipientId() != null && message.getRecipientId() > 0) {
            map.put("recipientId", message.getRecipientId());
        }
        if (message.getReadIds() != null && message.getReadIds().size() > 0) {
            map.put("readIds", message.getReadIds());
        }
        if (message.getDeliveredIds() != null && message.getDeliveredIds().size() > 0) {
            map.put("deliveredIds", message.getDeliveredIds());
        }
        if (!TextUtils.isEmpty(message.getDialogId())) {
            map.put("dialogId", message.getDialogId());
        }
        map.put("markable", message.isMarkable());
        map.put("delayed", message.isDelayed());
        if (!TextUtils.isEmpty(message.getBody())) {
            map.put("body", message.getBody());
        }

        return map;
    }

    static Map qbChatDialogToMap(QBChatDialog dialog) {
        Map<String, Object> map = new HashMap<>();

        map.put("isJoined", dialog.isJoined());

        if (dialog.getCreatedAt() != null) {
            Date date = dialog.getCreatedAt();
            String createdAt = DateUtil.convertDateToISO(date);
            map.put("createdAt", createdAt);
        }
        if (!TextUtils.isEmpty(dialog.getLastMessage())) {
            map.put("lastMessage", dialog.getLastMessage());
        }
        if (dialog.getLastMessageDateSent() > 0) {
            map.put("lastMessageDateSent", dialog.getLastMessageDateSent() * 1000);
        }
        if (dialog.getLastMessageUserId() != null && dialog.getLastMessageUserId() > 0) {
            map.put("lastMessageUserId", dialog.getLastMessageUserId());
        }
        if (!TextUtils.isEmpty(dialog.getName())) {
            map.put("name", dialog.getName());
        }
        if (!TextUtils.isEmpty(dialog.getPhoto())) {
            map.put("photo", dialog.getPhoto());
        }
        if (dialog.getType().getCode() > 0) {
            map.put("type", dialog.getType().getCode());
        }
        if (dialog.getUnreadMessageCount() != null && dialog.getUnreadMessageCount() > 0) {
            map.put("unreadMessagesCount", dialog.getUnreadMessageCount());
        }
        if (dialog.getUpdatedAt() != null) {
            Date date = dialog.getUpdatedAt();
            String updatedAt = DateUtil.convertDateToISO(date);
            map.put("updatedAt", updatedAt);
        }
        if (dialog.getUserId() != null && dialog.getUserId() > 0) {
            map.put("userId", dialog.getUserId());
        }
        if (!TextUtils.isEmpty(dialog.getRoomJid())) {
            map.put("roomJid", dialog.getRoomJid());
        }
        if (!TextUtils.isEmpty(dialog.getDialogId())) {
            map.put("id", dialog.getDialogId());
        }
        if (dialog.getOccupants() != null && dialog.getOccupants().size() > 0) {
            map.put("occupantsIds", dialog.getOccupants());
        }

        // TODO: 2019-10-21 need to check this logic
        if (dialog.getCustomData() != null && !TextUtils.isEmpty(dialog.getCustomData().getClassName())) {
            Map customDataMap = qbDialogCustomDataToMap(dialog.getCustomData());
            String className = dialog.getCustomData().getClassName();
            Map<String, Object> data = new HashMap<>();
            data.put(className, customDataMap);
            map.put("customData", data);
        }

        return map;
    }

    static Map qbDialogCustomDataToMap(QBDialogCustomData customData) {
        Map<String, Object> map = new HashMap<>();

        HashMap<String, Object> objectHashMap = customData.getFields();
        for (Map.Entry<String, Object> entry : objectHashMap.entrySet()) {
            String propertyKey = entry.getKey();
            if (customData.getInteger(propertyKey) != null) {
                map.put(propertyKey, customData.getInteger(propertyKey));
            }
            if (customData.getFloat(propertyKey) != null) {
                Double data = Double.valueOf(customData.getFloat(propertyKey));
                map.put(propertyKey, data);
            }
            if (customData.getBoolean(propertyKey) != null) {
                map.put(propertyKey, customData.getBoolean(propertyKey));
            }
            if (!TextUtils.isEmpty((String) customData.get(propertyKey))) {
                map.put(propertyKey, customData.get(propertyKey));
            }
            if (customData.getArray(propertyKey) != null && customData.getArray(propertyKey).size() > 0) {
                List<Object> propertyList = customData.getArray(propertyKey);
                map.put(propertyKey, propertyList);
            }
        }

        return map;
    }

    static QBRequestGetBuilder addDialogSortToRequestBuilder(QBRequestGetBuilder requestGetBuilder, Map sortMap) {
        if (sortMap != null) {
            String sortField = sortMap.containsKey("field") ? (String) sortMap.get("field") : null;

            boolean ascendingValue = sortMap.containsKey("ascending") && (boolean) sortMap.get("ascending");
            if (ascendingValue) {
                requestGetBuilder.sortAsc(sortField);
            } else {
                requestGetBuilder.sortDesc(sortField);
            }
        }

        return requestGetBuilder;
    }

    static QBRequestGetBuilder addDialogFilterToRequestBuilder(QBRequestGetBuilder requestGetBuilder, Map filterMap) {
        if (filterMap != null) {
            String filterField = filterMap.containsKey("field") ? (String) filterMap.get("field") : null;

            String filterOperator = filterMap.containsKey("operator") ? (String) filterMap.get("operator") : null;

            String filterValue = filterMap.containsKey("value") ? (String) filterMap.get("value") : null;

            if (filterOperator.equals(ChatConstants.DialogFilterOperators.LT)) {
                requestGetBuilder.lt(filterField, filterValue);
            } else if (filterOperator.equals(ChatConstants.DialogFilterOperators.LTE)) {
                requestGetBuilder.lte(filterField, filterValue);
            } else if (filterOperator.equals(ChatConstants.DialogFilterOperators.GT)) {
                requestGetBuilder.gt(filterField, filterValue);
            } else if (filterOperator.equals(ChatConstants.DialogFilterOperators.GTE)) {
                requestGetBuilder.gte(filterField, filterValue);
            } else if (filterOperator.equals(ChatConstants.DialogFilterOperators.NE)) {
                requestGetBuilder.ne(filterField, filterValue);
            } else if (filterOperator.equals(ChatConstants.DialogFilterOperators.IN)) {
                requestGetBuilder.in(filterField, filterValue);
            } else if (filterOperator.equals(ChatConstants.DialogFilterOperators.NIN)) {
                requestGetBuilder.nin(filterField, filterValue);
            } else if (filterOperator.equals(ChatConstants.DialogFilterOperators.ALL)) {
                requestGetBuilder.all(filterField, filterValue);
            } else if (filterOperator.equals(ChatConstants.DialogFilterOperators.CTN)) {
                requestGetBuilder.ctn(filterField, filterValue);
            } else {
                requestGetBuilder.addRule(filterField, QueryRule.EQ, filterValue);
            }
        }

        return requestGetBuilder;
    }

    static QBRequestGetBuilder addMessageSortToRequestBuilder(QBRequestGetBuilder requestGetBuilder, Map sortMap) {
        if (sortMap != null) {
            String sortField = sortMap.containsKey("field") ? (String) sortMap.get("field") : null;
            boolean ascendingValue = sortMap.containsKey("ascending") && (boolean) sortMap.get("ascending");

            if (ascendingValue) {
                requestGetBuilder.sortAsc(sortField);
            } else {
                requestGetBuilder.sortDesc(sortField);
            }
        }

        return requestGetBuilder;
    }

    static QBRequestGetBuilder addMessageFilterToRequestBuilder(QBRequestGetBuilder requestGetBuilder, Map filterMap) {
        if (filterMap != null) {
            String filterField = filterMap.containsKey("field") ? (String) filterMap.get("field") : null;

            String filterOperator = filterMap.containsKey("operator") ? (String) filterMap.get("operator") : null;

            String filterValue = filterMap.containsKey("value") ? (String) filterMap.get("value") : null;

            if (filterOperator.equals(ChatConstants.MessageFilterOperators.LT)) {
                requestGetBuilder.lt(filterField, filterValue);
            } else if (filterOperator.equals(ChatConstants.MessageFilterOperators.LTE)) {
                requestGetBuilder.lte(filterField, filterValue);
            } else if (filterOperator.equals(ChatConstants.MessageFilterOperators.GT)) {
                requestGetBuilder.gt(filterField, filterValue);
            } else if (filterOperator.equals(ChatConstants.MessageFilterOperators.GTE)) {
                requestGetBuilder.gte(filterField, filterValue);
            } else if (filterOperator.equals(ChatConstants.MessageFilterOperators.NE)) {
                requestGetBuilder.ne(filterField, filterValue);
            } else if (filterOperator.equals(ChatConstants.MessageFilterOperators.IN)) {
                requestGetBuilder.in(filterField, filterValue);
            } else if (filterOperator.equals(ChatConstants.MessageFilterOperators.NIN)) {
                requestGetBuilder.nin(filterField, filterValue);
            } else if (filterOperator.equals(ChatConstants.MessageFilterOperators.OR)) {
                requestGetBuilder.all(filterField, filterValue);
            } else if (filterOperator.equals(ChatConstants.MessageFilterOperators.CTN)) {
                requestGetBuilder.ctn(filterField, filterValue);
            } else {
                requestGetBuilder.addRule(filterField, QueryRule.EQ, filterValue);
            }
        }

        return requestGetBuilder;
    }

    static ArrayList getFilterValueFromMap(Map filterMap) {
        ArrayList filterValue = null;
        if (filterMap != null && filterMap.containsKey("value")) {
            try {
                ArrayList array = (ArrayList) filterMap.get("value");
                filterValue = array;
                filterMap.get("value");
            } catch (ClassCastException e) {
                //ignore
            }
            if (filterValue == null) {
                try {
                    String filter = (String) filterMap.get("value");
                    String[] filterArray = TextUtils.split(filter, ",");
                    filterValue = new ArrayList(Arrays.asList(filterArray));
                } catch (ClassCastException e) {
                    //ignore
                }
            }
        }
        return filterValue;
    }
}