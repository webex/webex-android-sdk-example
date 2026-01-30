package com.ciscowebex.androidsdk.kitchensink.calling

import com.ciscowebex.androidsdk.phone.Call

data class OneToOneIncomingCallModel(val _call: Call?): IncomingCallInfoModel(_call)