package com.ciscowebex.androidsdk.kitchensink.launcher.fragments;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
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

import com.ciscowebex.androidsdk.kitchensink.R;
import com.ciscowebex.androidsdk.kitchensink.actions.WebexAgent;
import com.ciscowebex.androidsdk.kitchensink.actions.commands.RequirePermissionAction;
import com.ciscowebex.androidsdk.kitchensink.actions.events.PermissionAcquiredEvent;
import com.ciscowebex.androidsdk.kitchensink.ui.BaseFragment;
import com.ciscowebex.androidsdk.membership.Membership;
import com.ciscowebex.androidsdk.message.LocalFile;
import com.ciscowebex.androidsdk.message.Mention;
import com.ciscowebex.androidsdk.message.Message;
import com.ciscowebex.androidsdk.message.MessageClient;
import com.ciscowebex.androidsdk.message.MessageObserver;
import com.ciscowebex.androidsdk.message.RemoteFile;
import com.github.benoitdion.ln.Ln;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
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

    ArrayList<File> selectedFile;

    ArrayList<Membership> mentionedMembershipList;

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
            if (evt instanceof MessageObserver.MessageArrived) {
                MessageObserver.MessageArrived event = (MessageObserver.MessageArrived) evt;
                Ln.e("message: " + event.getMessage().toString());
                adapterMessage.mData.add(event.getMessage());
                adapterMessage.notifyDataSetChanged();
                //if (event.getMessage().getPersonEmail().equals("sparksdktestuser16@tropo.com")) {
                textStatus.setText("");
                //}
            } else {
                MessageObserver.MessageDeleted event = (MessageObserver.MessageDeleted) evt;
                Ln.e("message deleted " + event.getMessageId());
            }
        });

        recyclerMembership.setLayoutManager(new LinearLayoutManager(this.getActivity()));
        adapterMembership = new MembershipAdapter(this.getActivity());
        adapterMembership.mData.add(new MembershipAll());
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
                    LocalFile localFile = new LocalFile(f);
                    localFile.setProgressHandler(x -> {
                        textStatus.setText("sending " + localFile.getName() + "...  " + x + "%");
                    });
                    localFile.setThumbnail(new LocalFile.Thumbnail());
                    localFile.getThumbnail().setPath(f.getPath());
                    localFile.getThumbnail().setWidth(622);
                    localFile.getThumbnail().setHeight(492);
                    arrayList.add(localFile);
                }
            }
        }
        LocalFile[] localFile = new LocalFile[arrayList.size()];
        arrayList.toArray(localFile);
        return localFile;
    }

    private Mention[] generateMentions() {
        Mention.MentionAll mentionAll = new Mention.MentionAll();
        ArrayList<Mention> mentionList = new ArrayList<>();
        for (Membership m : this.mentionedMembershipList) {
            if (m instanceof MembershipAll) {
                mentionList.add(mentionAll);
            } else {
                Mention.MentionPerson person = new Mention.MentionPerson(m.getPersonId());
                mentionList.add(person);
            }
        }
        Mention[] mention_array = new Mention[mentionList.size()];
        mentionList.toArray(mention_array);
        return mention_array;
    }

    private void hideSoftKeyboard() {
        InputMethodManager inputMethodManager =
                (InputMethodManager) getActivity().getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.hideSoftInputFromWindow(
                    getActivity().getCurrentFocus().getWindowToken(), 0);
        }
    }

    @OnClick(R.id.send_button)
    public void sendMessage(View btn) {
        if (!TextUtils.isEmpty(textMessage.getText())) {
            btn.setEnabled(false);
            agent.sendMessage(targetId,
                    textMessage.getText().toString(),
                    generateMentions(),
                    generateLocalFiles(),
                    rst -> {
                        Ln.e("posted:" + rst.toString());
                        selectedFile.clear();
                        btn.setEnabled(true);
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
                adapterMembership.mData.add(new MembershipAll());
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
            List<RemoteFile> list = message.getRemoteFiles();
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

    // fake membership implements ALL
    private class MembershipAll extends Membership {
        @Override
        public String getPersonDisplayName() {
            return "ALL";
        }
    }

    class MembershipAdapter extends RecyclerView.Adapter<MembershipAdapter.MembershipViewHolder> {
        private final LayoutInflater mLayoutInflater;
        private final Context mContext;
        private final ArrayList<Membership> mData;

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
            Membership membership = mData.get(position);
            if (membership instanceof MembershipAll) {
                holder.textContent.setText("ALL");
            } else {
                holder.textContent.setText(membership.getPersonDisplayName());
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
                Membership membership = mData.get(pos);
                recyclerMembership.setVisibility(View.GONE);
                recyclerMessage.setVisibility(View.VISIBLE);
                mentionedMembershipList.add(membership);
                textMessage.getText().append("@" + membership.getPersonDisplayName()).append(" ");
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
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                try {
                    String encoded_id = URLEncoder.encode(id, "utf-8");
                    final Uri contentUri = ContentUris.withAppendedId(
                            Uri.parse("content://downloads/public_downloads"), Long.valueOf(encoded_id));
                    return getDataColumn(context, contentUri, null, null);
                } catch (Exception e) {
                    e.printStackTrace();
                    return "";
                }
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
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
        final String column = "_data";
        final String[] projection = {
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

}
