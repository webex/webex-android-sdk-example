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


import android.content.Intent;
import android.net.Uri;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.ciscowebex.androidsdk.kitchensink.R;
import com.ciscowebex.androidsdk.kitchensink.ui.BaseFragment;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnItemSelected;
import butterknife.OnTouch;

/**
 * A simple {@link BaseFragment} subclass.
 */
public class FeedbackFragment extends BaseFragment {
    public static final int PICK_IMAGE = 1;

    @BindView(R.id.spinnerTopic)
    Spinner topic;

    @BindView(R.id.imageName)
    TextView imageUri;

    @BindView(R.id.comment)
    EditText comment;

    Uri attachment;

    public FeedbackFragment() {
        // Required empty public constructor
        setLayout(R.layout.fragment_feedback);
    }

    @OnItemSelected(R.id.spinnerTopic)
    public void pickUpTopic(int position) {
    }

    @OnTouch(R.id.pickImage)
    public boolean pickUpImage(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
        }
        return true;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_IMAGE) {
            if (data == null) {
                return;
            }
            attachment = data.getData();
            imageUri.setText(attachment.getLastPathSegment());
        }
    }

    @OnClick(R.id.sendButton)
    public void sendEmail() {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:"));
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"devsupport@ciscospark.com"});
        intent.putExtra(Intent.EXTRA_SUBJECT, topic.getSelectedItem().toString());
        intent.putExtra(Intent.EXTRA_TEXT, comment.getText());
        if (attachment != null)
            intent.putExtra(Intent.EXTRA_STREAM, attachment);
        startActivity(Intent.createChooser(intent, "Pick an Email provider"));
    }

}
