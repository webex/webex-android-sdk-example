package com.ciscowebex.androidsdk.kitchensink.utils

class Constants {
    object Intent {
        const val PERSON = "PERSON"
        const val OUTGOING_CALL_CALLER_ID = "OUTGOING_CALL_CALLER_ID"
        const val CALLING_ACTIVITY_ID = "CALLING_ACTIVITY_ID"
        const val TEAM_ID = "teamId"
        const val SPACE_ID = "spaceId"
        const val COMPOSER_ID = "composerId"
        const val COMPOSER_TYPE = "composerType"
        const val COMPOSER_REPLY_PARENT_MESSAGE = "composerReplyParentMessage"
        const val CALL_ID = "callid"
    }
    object Bundle {
        const val MESSAGE_ID = "messageId"
        const val PERSON_ID = "person_id"
        const val KEY_TASK_TYPE = "task_type"
        const val SPACE_ID = "spaceId"
        const val IS_CALLING_ENABLED = "isCallingEnabled"
        const val IS_MESSAGING_ENABLED = "isMessagingEnabled"
        const val TEAM_ID = "teamId"
        const val REMOTE_FILE = "remote_file"
    }
    object Action {
        const val MESSAGE_ACTION = "MESSAGE_ACTION"
        const val WEBEX_CALL_ACTION = "WEBEX_CALL_ACTION"
    }
    object Keys {
        const val PushRestEncryptionKey = "@McQfTjWnZr4u7x!A%D*G-KaNdRgUkXp"
        const val KitchenSinkSharedPref = "KSSharedPref"
        const val LoginType = "LoginType"
    }
}