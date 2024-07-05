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
        const val PUSH_ID = "pushid"
        const val MESSAGE_ID = "MESSAGE_ID"
        const val CALENDAR_MEETING_ID = "CALENDAR_MEETING_ID"
        const val KEY_UC_LOGIN_PAGE_ACTION = "KEY_UC_LOGIN_PAGE_ACTION"
        const val KEY_SSO_URL = "KEY_SSO_URL"
        const val ACCEPT_REQUEST_CODE = 1001
        const val REJECT_REQUEST_CODE = 1002
        const val FULLSCREEN_REQUEST_CODE = 1001
        const val CALL_TYPE = "SwitchCallType"
        const val MOVE_MEETING = "MoveMeeting"
        const val CLOSED_CAPTION_DATA = "closed_captions_data"
        const val CLOSED_CAPTION_LANGUAGES = "closed_captions_languages"
        const val CLOSED_CAPTION_LANGUAGE_ITEM = "closed_captions_language_item"
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
        const val WEBEX_CUCM_CALL_ACTION = "WEBEX_CUCM_CALL_ACTION"
        const val WEBEX_CALL_ACCEPT_ACTION = "WEBEX_CALL_ACCEPT_ACTION"
        const val WEBEX_CALL_REJECT_ACTION = "WEBEX_CALL_REJECT_ACTION"
    }
    object Keys {
        const val PushRestEncryptionKey = "PeShVmYq3s6v9yaBwE1H3McQfTjWnZr4"  //256 bit AES key, use base64 encoded key to send to cucm endpoint
        const val KitchenSinkSharedPref = "KSSharedPref"
        const val LoginType = "LoginType"
        const val Email = "Email"
        const val Fedramp = "fedramp_enabled"
        const val IsVirtualBgAdded = "IsVirtualBgAdded"
        const val IsBackgroundRunningEnabled = "IsBackgroundRunningEnabled"
        const val appId = "c1ce50069c8b4dfba5d2f916"
    }
    object DefaultMax {
        const val SPACE_MAX = 100
        const val TEAM_MAX = 100
    }
    object Notification{
        const val WEBEX_CALL = 123456
    }

    // Constants indicating callback events
    object Callbacks {
        const val RE_LOGIN_REQUIRED = "RE_LOGIN_REQUIRED"
    }
}