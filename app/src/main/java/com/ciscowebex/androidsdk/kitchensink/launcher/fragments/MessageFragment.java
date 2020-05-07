package com.ciscowebex.androidsdk.kitchensink.launcher.fragments;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ciscowebex.androidsdk.kitchensink.R;
import com.ciscowebex.androidsdk.kitchensink.actions.WebexAgent;
import com.ciscowebex.androidsdk.kitchensink.actions.commands.RequirePermissionAction;
import com.ciscowebex.androidsdk.kitchensink.actions.events.PermissionAcquiredEvent;
import com.ciscowebex.androidsdk.kitchensink.ui.BaseFragment;
import com.ciscowebex.androidsdk.membership.Membership;
import com.ciscowebex.androidsdk.membership.MembershipClient;
import com.ciscowebex.androidsdk.message.LocalFile;
import com.ciscowebex.androidsdk.message.Mention;
import com.ciscowebex.androidsdk.message.Message;
import com.ciscowebex.androidsdk.message.MessageClient;
import com.ciscowebex.androidsdk.message.MessageObserver;
import com.ciscowebex.androidsdk.message.RemoteFile;
import com.ciscowebex.androidsdk.space.SpaceClient;
import com.ciscowebex.androidsdk.utils.MimeUtils;
import com.github.benoitdion.ln.Ln;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.app.Activity.RESULT_OK;


public class MessageFragment extends BaseFragment {
    private static final String TARGET_ID = "target_id";
    private static final int FILE_SELECT_REQUEST = 1;

    @BindView(R.id.message_text)
    EditText textMessage;

    @BindView(R.id.message_view)
    RecyclerView recyclerMessage;

    @BindView(R.id.message_mention)
    ImageButton btnMention;

    @BindView(R.id.message_status)
    TextView textStatus;

    @BindView(R.id.membership_recyclerview)
    RecyclerView recyclerMembership;

    MessageAdapter adapterMessage;

    MembershipAdapter adapterMembership;

    WebexAgent agent = WebexAgent.getInstance();

    MessageClient messageClient = agent.getMessageClient();

    MembershipClient membershipClient = agent.getMembershipClient();

    SpaceClient spaceClient = agent.getSpaceClient();

    ArrayList<File> selectedFile;

    ArrayList<Object> mentionedMembershipList;

    String targetId;

    public static MessageFragment newInstance(String id) {
        MessageFragment fragment = new MessageFragment();
        Bundle args = new Bundle();
        args.putInt(LAYOUT, R.layout.fragment_message);
        args.putString(TARGET_ID, id);
        fragment.setArguments(args);
        return fragment;
    }

    private String getTargetId() {
        Bundle bundle = getArguments();
        return bundle != null ? bundle.getString(TARGET_ID) : null;
    }

    public MessageFragment() {
        // Required empty public constructor
        selectedFile = new ArrayList<>();
        mentionedMembershipList = new ArrayList<>();
    }

    @Override
    public void onActivityCreated(Bundle saved) {
        super.onActivityCreated(saved);
        recyclerMessage.setLayoutManager(new LinearLayoutManager(this.getActivity()));
        adapterMessage = new MessageAdapter(this.getActivity());
        recyclerMessage.setAdapter(adapterMessage);

        messageClient.setMessageObserver(evt -> {
            if (evt instanceof MessageObserver.MessageReceived) {
                MessageObserver.MessageReceived event = (MessageObserver.MessageReceived) evt;
                Ln.i("message: " + event.getMessage());
                adapterMessage.mData.add(event.getMessage());
                adapterMessage.notifyDataSetChanged();
                //if (event.getMessage().getPersonEmail().equals("sparksdktestuser16@tropo.com")) {
                textStatus.setText("");
                //}
            } else if (evt instanceof MessageObserver.MessageDeleted) {
                MessageObserver.MessageDeleted event = (MessageObserver.MessageDeleted) evt;
                Ln.i("message deleted " + event.getMessageId());
            }
        });

        recyclerMembership.setLayoutManager(new LinearLayoutManager(this.getActivity()));
        adapterMembership = new MembershipAdapter(this.getActivity());
        adapterMembership.mData.add("ALL");
        recyclerMembership.setAdapter(adapterMembership);
    }

    @Override
    public void onStart() {
        super.onStart();
        recyclerMembership.setVisibility(View.GONE);
        targetId = getTargetId();
    }

    private void requirePermission() {
        new RequirePermissionAction(getActivity()).execute();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private LocalFile[] generateLocalFiles() {
        ArrayList<LocalFile> arrayList = new ArrayList<>();
        if (selectedFile != null) {
            for (File f : selectedFile) {
                if (f.exists()) {
                    Ln.i("select file: " + f);
                    LocalFile.Thumbnail thumbnail = null;
                    if (MimeUtils.getContentTypeByFilename(f.getName()) == MimeUtils.ContentType.IMAGE) {
                        Bitmap bitmap = BitmapFactory.decodeFile(f.getAbsolutePath());
                        thumbnail = new LocalFile.Thumbnail(f, null, bitmap.getWidth(), bitmap.getHeight());
                        bitmap.recycle();
                    }
                    LocalFile localFile = new LocalFile(f, null, thumbnail, v -> textStatus.setText(String.format("sending %s...  %s bytes", f.getName(), v)));
                    arrayList.add(localFile);
                }
            }
        }
        LocalFile[] localFile = new LocalFile[arrayList.size()];
        arrayList.toArray(localFile);
        return localFile;
    }

    private Mention[] generateMentions() {
        Mention.All mentionAll = new Mention.All();
        ArrayList<Mention> mentionList = new ArrayList<>();
        for (Object o : this.mentionedMembershipList) {
            if (o instanceof String) {
                mentionList.add(mentionAll);
            } else {
                mentionList.add(new Mention.Person(((Membership) o).getPersonId()));
            }
        }
        Mention[] mentionArray = new Mention[mentionList.size()];
        mentionList.toArray(mentionArray);
        return mentionArray;
    }

    private void hideSoftKeyboard() {
        InputMethodManager inputMethodManager =
                (InputMethodManager) getActivity().getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null && getActivity().getCurrentFocus() != null) {
            inputMethodManager.hideSoftInputFromWindow(
                    getActivity().getCurrentFocus().getWindowToken(), 0);
        }
    }

    @OnClick(R.id.send_button)
    public void sendMessage(View btn) {
        if (!TextUtils.isEmpty(textMessage.getText()) || (selectedFile != null && !selectedFile.isEmpty())) {
            btn.setEnabled(false);
            messageClient.post(targetId, Message.draft(Message.Text.plain(textMessage.getText().toString()))
                    .addMentions(generateMentions())
                    .addAttachments(generateLocalFiles()), rst -> {
                Ln.e("posted:" + rst);
                selectedFile.clear();
                btn.setEnabled(true);
                textStatus.setText("sent");
                if (rst.isSuccessful()) {
                    adapterMessage.mData.add(rst.getData());
                    adapterMessage.notifyDataSetChanged();
                }
            });
            textStatus.setText("sending ...");
            textMessage.setText("");
        }
        //text_mention.setVisibility(View.GONE);
        textMessage.clearFocus();
        hideSoftKeyboard();
    }


    @OnClick(R.id.message_upload_file)
    public void selectFile() {
        requirePermission();
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(PermissionAcquiredEvent event) {
        Intent intent = new Intent();
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,
                "Select File"), FILE_SELECT_REQUEST);
    }


    @OnClick(R.id.message_mention)
    public void mentionPeople() {
        if (recyclerMembership.getVisibility() != View.VISIBLE) {
            recyclerMembership.setVisibility(View.VISIBLE);
            recyclerMessage.setVisibility(View.GONE);
        } else {
            recyclerMembership.setVisibility(View.GONE);
            recyclerMessage.setVisibility(View.VISIBLE);
        }
        agent.getMembership(getTargetId(), result -> {
            if (result.isSuccessful()) {
                List<Membership> list = (List<Membership>) result.getData();
                adapterMembership.mData.clear();
                adapterMembership.mData.add("ALL");
                adapterMembership.mData.addAll(list);
                adapterMembership.notifyDataSetChanged();
            }
        });
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Ln.e("onActivityResult");
        if (resultCode == RESULT_OK) {
            if (requestCode == FILE_SELECT_REQUEST) {

                Uri uri = data.getData();

                String path = getPath(getActivity(), uri);
                if (path != null) {
                    File selected = new File(path);
                    selectedFile.add(selected);
                    StringBuilder buffer = new StringBuilder();
                    for (File f : selectedFile) {
                        buffer.append(" ").append(f.getName());
                    }
                    textStatus.setText(buffer.toString());
                }
            }
        }
    }

    class FilesAdapter extends RecyclerView.Adapter<FilesAdapter.FilesViewHolder> {
        private final LayoutInflater mLayoutInflater;
        private final Context mContext;
        private ArrayList<RemoteFile> mData;

        FilesAdapter(Context context) {
            mContext = context;
            mLayoutInflater = LayoutInflater.from(mContext);
            mData = new ArrayList<>();
        }

        @Override
        public FilesAdapter.FilesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new FilesViewHolder(mLayoutInflater.inflate(R.layout.listitem_file, parent, false));
        }

        @Override
        public void onBindViewHolder(FilesAdapter.FilesViewHolder holder, int position) {
            RemoteFile file = mData.get(position);
            holder.textFilename.setText(file.getDisplayName());
            agent.downloadThumbnail(file, null, null, (uri) -> {
                holder.imageFile.setImageURI(uri.getData());
                holder.progressBarDownload.setVisibility(View.GONE);
            });
        }

        @Override
        public int getItemCount() {
            return mData.size();
        }

        class FilesViewHolder extends RecyclerView.ViewHolder {

            @BindView(R.id.message_item_file)
            ImageView imageFile;

            @BindView(R.id.message_item_file_download_progress)
            ProgressBar progressBarDownload;

            @BindView(R.id.message_item_file_download)
            ImageButton btnDownload;

            @BindView(R.id.message_item_load_process)
            TextView textLoadProcess;

            @BindView(R.id.message_item_filename)
            TextView textFilename;

            @OnClick(R.id.message_item_file_download)
            public void download() {
                RemoteFile file = mData.get(getAdapterPosition());
                agent.downloadFile(
                        file,
                        null,
                        progress -> {
                            textLoadProcess.setText(String.format("%s", Math.round(progress)));
                        },
                        uri -> {
                            textLoadProcess.setText("complete");
                        }
                );
            }

            public FilesViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }
    }

    class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
        private final LayoutInflater mLayoutInflater;
        private final Context mContext;
        private ArrayList<Message> mData;

        MessageAdapter(Context context) {
            mContext = context;
            mData = new ArrayList<>();
            mLayoutInflater = LayoutInflater.from(mContext);
        }

        @NonNull
        @Override
        public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new MessageViewHolder(mLayoutInflater.inflate(R.layout.listview_message, parent, false));
        }

        @Override
        public void onBindViewHolder(MessageViewHolder holder, int position) {
            Message message = mData.get(position);
            holder.textDate.setText(message.getCreated().toString());
            holder.textMessage.setText(message.getText());
            try {
                JSONObject json = new JSONObject(message.toString());
                holder.textPayload.setText(json.toString(4));
            } catch (JSONException e) {
                Ln.e("JSONObject parse error");
                holder.textPayload.setText(message.toString());
            }
            if (message.isSelfMentioned()) {
                holder.textMention.setVisibility(View.VISIBLE);
            } else {
                holder.textMention.setVisibility(View.GONE);
            }
            List<RemoteFile> list = message.getFiles();
            if (list != null && list.size() > 0) {
                FilesAdapter adapter = new FilesAdapter(mContext);
                holder.recyclerFiles.setLayoutManager(new LinearLayoutManager(mContext));
                holder.recyclerFiles.setAdapter(adapter);
                adapter.mData.addAll(list);
                adapter.notifyDataSetChanged();
            }
        }

        @Override
        public int getItemCount() {
            return mData == null ? 0 : mData.size();
        }

        class MessageViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.messageLayout)
            View layoutMessage;

            @BindView(R.id.message_item_text)
            TextView textMessage;

            @BindView(R.id.message_item_date)
            TextView textDate;

            @BindView(R.id.message_item_mention)
            TextView textMention;

            @BindView(R.id.message_item_list_files)
            RecyclerView recyclerFiles;

            @BindView(R.id.message_item_payload)
            TextView textPayload;

            @BindView(R.id.payloadLayout)
            View layoutMessagePayload;

            @OnClick(R.id.expand)
            public void expand() {
                if (layoutMessagePayload.getVisibility() != View.VISIBLE) {
                    layoutMessagePayload.setVisibility(View.VISIBLE);
                    layoutMessage.setVisibility(View.GONE);
                } else {
                    layoutMessagePayload.setVisibility(View.GONE);
                    layoutMessage.setVisibility(View.VISIBLE);
                }
            }

            MessageViewHolder(View view) {
                super(view);
                ButterKnife.bind(this, view);
            }
        }
    }

//    // fake membership implements ALL
//    private class MembershipAll extends Membership {
//
//        @Override
//        public String getPersonDisplayName() {
//            return "ALL";
//        }
//    }

    class MembershipAdapter extends RecyclerView.Adapter<MembershipAdapter.MembershipViewHolder> {
        private final LayoutInflater mLayoutInflater;
        private final Context mContext;
        private final ArrayList<Object> mData;

        MembershipAdapter(Context context) {
            mContext = context;
            mData = new ArrayList<>();
            mLayoutInflater = LayoutInflater.from(mContext);
        }


        @NonNull
        @Override
        public MembershipViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new MembershipViewHolder(mLayoutInflater.inflate(R.layout.listitem_membership, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull MembershipViewHolder holder, int position) {
            Object object = mData.get(position);
            if (object instanceof String) {
                holder.textContent.setText("ALL");
            } else if (object instanceof Membership) {
                holder.textContent.setText(((Membership) object).getPersonDisplayName());
            }
        }

        @Override
        public int getItemCount() {
            return mData != null ? mData.size() : 0;
        }

        class MembershipViewHolder extends RecyclerView.ViewHolder {

            @BindView(R.id.text_content)
            TextView textContent;

            @OnClick(R.id.text_content)
            void onMembershipClick() {
                int pos = getAdapterPosition();
                Object object = mData.get(pos);
                recyclerMembership.setVisibility(View.GONE);
                recyclerMessage.setVisibility(View.VISIBLE);
                mentionedMembershipList.add(object);
                textMessage.getText().append("@").append(object instanceof String ? ((String) object) : ((Membership) object).getPersonDisplayName()).append(" ");
            }

            MembershipViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }
    }


    /**
     * helper to retrieve the path of an image URI
     */
    public static String getPath(final Context context, final Uri uri) {

        // DocumentProvider
        if (DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                String docId = DocumentsContract.getDocumentId(uri);
                String[] split = docId.split(":");
                String fullPath = getPathFromExtSD(split);
                return TextUtils.isEmpty(fullPath) ? null : fullPath;
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    Cursor cursor = null;
                    try {
                        cursor = context.getContentResolver().query(uri, new String[]{MediaStore.MediaColumns.DISPLAY_NAME}, null, null, null);
                        if (cursor != null && cursor.moveToFirst()) {
                            String fileName = cursor.getString(0);
                            String path = Environment.getExternalStorageDirectory().toString() + "/Download/" + fileName;
                            if (!TextUtils.isEmpty(path)) {
                                return path;
                            }
                        }
                    } finally {
                        if (cursor != null)
                            cursor.close();
                    }
                }
                String id = DocumentsContract.getDocumentId(uri);
                if (!TextUtils.isEmpty(id)) {
                    if (id.startsWith("raw:")) {
                        return id.replaceFirst("raw:", "");
                    }
                    try {
                        Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
                        return getDataColumn(context, contentUri, null, null);
                    } catch (NumberFormatException e) {
                        //In Android 8 and Android P the id is not a number
                        return uri.getPath() == null ? null : uri.getPath().replaceFirst("^/document/raw:", "").replaceFirst("^raw:", "");
                    }
                }
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                String docId = DocumentsContract.getDocumentId(uri);
                String[] split = docId.split(":");
                String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                String selection = "_id=?";
                String[] selectionArgs = new String[]{
                        split[1]
                };
                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
            // GoogleDriveProvider
            else if (isGoogleDriveUri(uri)) {
                return getFilePath(uri, context, true);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {

            if (isGooglePhotosUri(uri)) {
                return uri.getLastPathSegment();
            }

            if (isGoogleDriveUri(uri)) {
                return getFilePath(uri, context, true);
            }
            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.N) {
                return getFilePath(uri, context, false);
            } else {
                return getDataColumn(context, uri, null, null);
            }
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        String column = "_data";
        String[] projection = {
                column
        };
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * Get full file path from external storage
     *
     * @param pathData The storage type and the relative path
     */
    public static String getPathFromExtSD(String[] pathData) {
        String type = pathData[0];
        String relativePath = "/" + pathData[1];
        String fullPath;

        // on my Sony devices (4.4.4 & 5.1.1), `type` is a dynamic string
        // something like "71F8-2C0A", some kind of unique id per storage
        // don't know any API that can get the root path of that storage based on its id.
        //
        // so no "primary" type, but let the check here for other devices
        if ("primary".equalsIgnoreCase(type)) {
            fullPath = Environment.getExternalStorageDirectory() + relativePath;
            if (fileExists(fullPath)) {
                return fullPath;
            }
        }

        // Environment.isExternalStorageRemovable() is `true` for external and internal storage
        // so we cannot relay on it.
        //
        // instead, for each possible path, check if file exists
        // we'll start with secondary storage as this could be our (physically) removable sd card
        fullPath = System.getenv("SECONDARY_STORAGE") + relativePath;
        if (fileExists(fullPath)) {
            return fullPath;
        }
        fullPath = System.getenv("EXTERNAL_STORAGE") + relativePath;
        if (fileExists(fullPath)) {
            return fullPath;
        }
        return fullPath;
    }

    /**
     *
     * @param uri
     * @param context
     * @param isDrive true if getting DriveFilePath, false if getting MediaFilePath on Android N
     * @return
     */
    private static String getFilePath(Uri uri, Context context, boolean isDrive) {
        Cursor cursor = null;
        File file = null;
        InputStream inputStream = null;
        FileOutputStream outputStream = null;
        try {
            cursor = context.getContentResolver().query(uri, null, null, null, null);
            if (cursor == null) {
                return null;
            }
            /*
             * Get the column indexes of the data in the Cursor,
             *     * move to the first row in the Cursor, get the data,
             *     * and display it.
             * */
            int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            cursor.moveToFirst();
            String name = (cursor.getString(nameIndex));
            if (isDrive){
                file = new File(context.getCacheDir(), name);
            } else {
                file = new File(context.getFilesDir(), name);
            }
            inputStream = context.getContentResolver().openInputStream(uri);
            if (null == inputStream) {
                return null;
            }
            outputStream = new FileOutputStream(file);
            int read;
            int maxBufferSize = 1 * 1024 * 1024;
            int bytesAvailable = inputStream.available();

            //int bufferSize = 1024;
            int bufferSize = Math.min(bytesAvailable, maxBufferSize);

            final byte[] buffers = new byte[bufferSize];
            while ((read = inputStream.read(buffers)) != -1) {
                outputStream.write(buffers, 0, read);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return file == null ? null : file.getPath();
    }

    /**
     * Check if a file exists on device
     *
     * @param filePath The absolute file path
     */
    public static boolean fileExists(String filePath) {
        File file = new File(filePath);
        return file.exists();
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Drive.
     */
    public static boolean isGoogleDriveUri(Uri uri) {
        return "com.google.android.apps.docs.storage".equals(uri.getAuthority()) || "com.google.android.apps.docs.storage.legacy".equals(uri.getAuthority());
    }

    /**
     * @param uri - The Uri to check.
     * @return - Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }
}
