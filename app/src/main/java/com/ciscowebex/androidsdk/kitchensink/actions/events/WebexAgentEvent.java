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

package com.ciscowebex.androidsdk.kitchensink.actions.events;


import com.ciscowebex.androidsdk.Result;
import com.ciscowebex.androidsdk.WebexError;

import org.greenrobot.eventbus.EventBus;


/**
 * Created on 15/09/2017.
 */

public class WebexAgentEvent {
    Result result;

    public WebexAgentEvent(Result result) {
        this.result = result;
    }

    public boolean isSuccessful() {
        return result.isSuccessful();
    }

    public WebexError getError() {
        return result.getError();
    }

    public Result getResult() {
        return result;
    }

    public static void postEvent(Object event) {
        EventBus.getDefault().post(event);
    }

    public void post() {
        postEvent(this);
    }
}
