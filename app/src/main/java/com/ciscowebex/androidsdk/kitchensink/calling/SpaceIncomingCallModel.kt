package com.ciscowebex.androidsdk.kitchensink.calling

import com.ciscowebex.androidsdk.phone.Call

data class SpaceIncomingCallModel(val _call: Call?): IncomingCallInfoModel(_call)