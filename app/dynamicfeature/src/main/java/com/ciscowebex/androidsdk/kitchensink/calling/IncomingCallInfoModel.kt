package com.ciscowebex.androidsdk.kitchensink.calling

import com.ciscowebex.androidsdk.phone.Call

abstract class IncomingCallInfoModel(var call: Call?) {
    var isEnabled: Boolean = true
}