
package com.quickblox.quickblox_sdk.file;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.quickblox.content.QBContent;
import com.quickblox.content.model.QBFile;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.QBProgressCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.quickblox_sdk.base.BaseModule;
import com.quickblox.quickblox_sdk.base.EventHandler;
import com.quickblox.quickblox_sdk.utils.EventsUtil;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;

/**
 * Created by Injoit on 2019-12-27.
 * Copyright © 2019 Quickblox. All rights reserved.
 */
public class FileModule implements BaseModule {
    static final String CHANNEL_NAME = "FlutterQBFileChannel";

    private static final String SUBSCRIBE_UPLOAD_PROGRESS_METHOD = "subscribeUploadProgress";
    private static final String UNSUBSCRIBE_UPLOAD_PROGRESS_METHOD = "unsubscribeUploadProgress";
    private static final String UPLOAD_METHOD = "upload";
    private static final String GET_INFO_METHOD = "getInfo";
    private static final String GET_PUBLIC_URL_METHOD = "getPublicURL";
    private static final String GET_PRIVATE_URL_METHOD = "getPrivateURL";

    private Set<UploadProgressItem> uploadProgressItemSet = new CopyOnWriteArraySet<>();
    private Context context;
    private BinaryMessenger binaryMessenger;

    public FileModule(Context context, BinaryMessenger binaryMessenger) {
        this.context = context;
        this.binaryMessenger = binaryMessenger;
    }

    @Override
    public void initEventHandler() {
        EventHandler.init(FileConstants.UploadProgress.FILE_UPLOAD_PROGRESS, binaryMessenger);
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
            case SUBSCRIBE_UPLOAD_PROGRESS_METHOD:
                subscribeUploadProgress(data, result);
                break;
            case UNSUBSCRIBE_UPLOAD_PROGRESS_METHOD:
                unsubscribeUploadProgress(data, result);
                break;
            case UPLOAD_METHOD:
                upload(data, result);
                break;
            case GET_INFO_METHOD:
                getInfo(data, result);
                break;
            case GET_PUBLIC_URL_METHOD:
                getPublicURL(data, result);
                break;
            case GET_PRIVATE_URL_METHOD:
                getPrivateURL(data, result);
                break;
        }
    }

    private UploadProgressItem getUploadProgressItem(String url) {
        UploadProgressItem uploadProgressItem = null;
        if (uploadProgressItemSet.contains(new UploadProgressItem(url))) {
            for (UploadProgressItem item : uploadProgressItemSet) {
                if (item.equals(new UploadProgressItem(url))) {
                    uploadProgressItem = item;
                    break;
                }
            }
        }
        return uploadProgressItem;
    }

    private void subscribeUploadProgress(Map<String, Object> data, MethodChannel.Result result) {
        String url = data != null && data.containsKey("url") ? (String) data.get("url") : null;

        if (TextUtils.isEmpty(url)) {
            result.error("The url is required parameter", null, null);
            return;
        }

        UploadProgressItem uploadProgressItem = getUploadProgressItem(url);
        if (uploadProgressItem != null) {
            uploadProgressItem.subscribe(true);
            result.success(null);
        } else {
            result.error("The file with url " + url + " has not found", null, null);
        }
    }

    private void unsubscribeUploadProgress(Map<String, Object> data, MethodChannel.Result result) {
        String url = data != null && data.containsKey("url") ? (String) data.get("url") : null;

        if (TextUtils.isEmpty(url)) {
            result.error("The url is required parameter", null, null);
            return;
        }

        UploadProgressItem uploadProgressItem = getUploadProgressItem(url);
        if (uploadProgressItem != null) {
            uploadProgressItem.subscribe(false);
            result.success(null);
        } else {
            result.error("The file with url " + url + "has not found", null, null);
        }
    }

    private void upload(Map<String, Object> data, final MethodChannel.Result result) {
        String url = data != null && data.containsKey("url") ? (String) data.get("url") : null;
        boolean isPublic = data != null && data.containsKey("public") && (boolean) data.get("public");

        if (TextUtils.isEmpty(url)) {
            result.error("Parameter uriString is required", null, null);
            return;
        }

        new LoadFileFromUrlTask(url, isPublic, result).execute();
    }

    private class LoadFileFromUrlTask extends AsyncTask<Void, Void, File> {
        private static final String SCHEME_CONTENT = "content";
        private static final String SCHEME_CONTENT_GOOGLE = "content://com.google.android";
        private static final String SCHEME_FILE = "file";

        private static final int BUFFER_SIZE_2_MB = 2048;

        private MethodChannel.Result result;
        private String uriString;
        private boolean isPublic;

        LoadFileFromUrlTask(String uriString, boolean isPublic, MethodChannel.Result result) {
            this.result = result;
            this.uriString = uriString;
            this.isPublic = isPublic;
        }

        @Override
        protected File doInBackground(Void... voids) {
            String imageFilePath = null;
            Uri uri = Uri.parse(uriString);
            String uriScheme = uri.getScheme();
            File file = null;

            boolean isFromGoogleApp = uri.toString().startsWith(SCHEME_CONTENT_GOOGLE);
            boolean isKitKatAndUpper = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

            if (SCHEME_CONTENT.equalsIgnoreCase(uriScheme) && !isFromGoogleApp && !isKitKatAndUpper) {
                String[] filePathColumn = {MediaStore.Images.Media.DATA};
                Cursor cursor = context.getContentResolver().query(uri, filePathColumn, null, null, null);
                if (cursor != null) {
                    if (cursor.getCount() > 0) {
                        cursor.moveToFirst();
                        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                        imageFilePath = cursor.getString(columnIndex);
                    }
                    cursor.close();
                }
            } else if (SCHEME_FILE.equalsIgnoreCase(uriScheme)) {
                imageFilePath = uri.getPath();
            } else {
                try {
                    imageFilePath = saveUriToFile(uri);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (!TextUtils.isEmpty(imageFilePath)) {
                file = new File(imageFilePath);
            }

            uploadProgressItemSet.add(new UploadProgressItem(uriString));

            return file;
        }

        private String saveUriToFile(Uri uri) throws Exception {
            ParcelFileDescriptor parcelFileDescriptor = context.getContentResolver().openFileDescriptor(uri, "r");
            FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();

            InputStream inputStream = new FileInputStream(fileDescriptor);
            BufferedInputStream bis = new BufferedInputStream(inputStream);

            File parentDir = getAppExternalDataDirectoryFile();
            String fileName = System.currentTimeMillis() + ".jpg";
            File resultFile = new File(parentDir, fileName);

            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(resultFile));

            byte[] buf = new byte[BUFFER_SIZE_2_MB];
            int length;

            try {
                while ((length = bis.read(buf)) > 0) {
                    bos.write(buf, 0, length);
                }
            } catch (Exception e) {
                throw new IOException("Can\'t save Storage API bitmap to a file!", e);
            } finally {
                parcelFileDescriptor.close();
                bis.close();
                bos.close();
            }

            return resultFile.getAbsolutePath();
        }

        private String getAppExternalDataDirectoryPath() {
            StringBuilder sb = new StringBuilder();
            sb.append(Environment.getExternalStorageDirectory())
                    .append(File.separator)
                    .append("Android")
                    .append(File.separator)
                    .append("data")
                    .append(File.separator)
                    .append(context.getPackageName())
                    .append(File.separator);

            return sb.toString();
        }

        private File getAppExternalDataDirectoryFile() {
            File dataDirectoryFile = new File(getAppExternalDataDirectoryPath());
            dataDirectoryFile.mkdirs();

            return dataDirectoryFile;
        }

        @Override
        protected void onPostExecute(File file) {
            super.onPostExecute(file);
            if (file != null) {
                uploadFile(file, isPublic, result, uriString);
            } else {
                result.error("Can't load file from " + uriString, null, null);
            }
        }
    }

    private void uploadFile(final File file, boolean isPublic, final MethodChannel.Result result, final String url) {
        final UploadProgressItem uploadProgressItem = getUploadProgressItem(url);
        QBContent.uploadFileTask(file, isPublic, "", new QBProgressCallback() {
            @Override
            public void onProgressUpdate(int progressValue) {
                if (uploadProgressItem != null && uploadProgressItem.subscribed()) {
                    Map<String, Object> payload = new HashMap<>();
                    payload.put("url", url);
                    payload.put("progress", progressValue);

                    String eventName = FileConstants.UploadProgress.FILE_UPLOAD_PROGRESS;
                    Map data = EventsUtil.buildResult(eventName, payload);
                    EventHandler.sendEvent(eventName, data);
                }
                if (uploadProgressItem != null && progressValue >= 100) {
                    uploadProgressItemSet.remove(uploadProgressItem);
                }
            }
        }).performAsync(new QBEntityCallback<QBFile>() {
            @Override
            public void onSuccess(QBFile qbFile, Bundle params) {
                Map file = FileMapper.qbFileToMap(qbFile);
                result.success(file);
            }

            @Override
            public void onError(QBResponseException responseException) {
                result.error(responseException.getMessage(), null, null);
            }
        });
    }

    private void getInfo(Map<String, Object> data, final MethodChannel.Result result) {
        Integer id = data != null && data.containsKey("id") ? (int) data.get("id") : null;

        if (id == null || id <= 0) {
            result.error("The id is required parameter", null, null);
            return;
        }

        QBContent.getFile(id).performAsync(new QBEntityCallback<QBFile>() {
            @Override
            public void onSuccess(QBFile qbFile, Bundle params) {
                Map file = FileMapper.qbFileToMap(qbFile);
                result.success(file);
            }

            @Override
            public void onError(QBResponseException responseException) {
                result.error(responseException.getMessage(), null, null);
            }
        });
    }

    private void getPublicURL(Map<String, Object> data, final MethodChannel.Result result) {
        String uid = data != null && data.containsKey("uid") ? (String) data.get("uid") : null;

        if (TextUtils.isEmpty(uid)) {
            result.error("The id is required parameter", null, null);
            return;
        }

        String publicUrl = QBFile.getPublicUrlForUID(uid);
        result.success(publicUrl);
    }

    private void getPrivateURL(Map<String, Object> data, final MethodChannel.Result result) {
        String uid = data != null && data.containsKey("uid") ? (String) data.get("uid") : null;

        if (TextUtils.isEmpty(uid)) {
            result.error("The id is required parameter", null, null);
            return;
        }

        String privateUrl = QBFile.getPrivateUrlForUID(uid);
        result.success(privateUrl);
    }

    private class UploadProgressItem {
        private String url;
        private boolean subscribe = false;

        UploadProgressItem(String url) {
            this.url = url;
        }

        void subscribe(boolean subscribe) {
            this.subscribe = subscribe;
        }

        boolean subscribed() {
            return subscribe;
        }

        @Override
        public boolean equals(@Nullable Object obj) {
            boolean equals;
            if (obj instanceof UploadProgressItem) {
                equals = this.url.equals(((UploadProgressItem) obj).url);
            } else {
                equals = super.equals(obj);
            }
            return equals;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 53 * hash + url.hashCode();
            return hash;
        }
    }
}