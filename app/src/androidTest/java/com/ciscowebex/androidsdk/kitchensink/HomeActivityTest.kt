package com.ciscowebex.androidsdk.kitchensink

import android.content.Intent
import android.net.Uri
import android.view.View
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.*
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.rule.GrantPermissionRule
import com.ciscowebex.androidsdk.kitchensink.calling.CallActivity
import com.ciscowebex.androidsdk.kitchensink.messaging.MessagingActivity
import com.ciscowebex.androidsdk.kitchensink.search.SearchActivity
import com.ciscowebex.androidsdk.kitchensink.utils.WaitUtils
import org.hamcrest.Matchers.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.TimeUnit


class HomeActivityTest : KitchenSinkTest() {

    @Rule @JvmField
    val grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.RECORD_AUDIO,
            android.Manifest.permission.READ_PHONE_STATE
    )

    @Before
    override fun initTests() {
        super.initTests()
        setUpLogin()
    }

    @Test
    fun testInitiateCallButton_homeActivity() {
        clickOnView(R.id.iv_startCall)
        intended(hasComponent(SearchActivity::class.java.name))
    }

    @Test
    fun testWaitingCallButton_homeActivity() {
        //clickOnView(R.id.iv_waitingCall)
        intended(hasComponent(CallActivity::class.java.name))
    }

    @Test
    fun testFeedbackButton_homeActivity() {
        clickOnView(R.id.iv_feedback)
        val subject = targetContext.getString(R.string.feedbackLogsSubject)

        val expectedIntent = allOf(
                hasAction(Intent.ACTION_CHOOSER),
                hasExtra(
                        equalTo(Intent.EXTRA_INTENT),
                        allOf(
                                hasAction(Intent.ACTION_SENDTO),
                                hasData(Uri.parse("mailto:")),
                                hasExtra(
                                        `is`(Intent.EXTRA_SUBJECT),
                                        `is`(subject)
                                )
                        )
                )
        )

        intended(expectedIntent)

    }

    @Test
    fun testLogoutButton_homeActivity() {
        clickOnView(R.id.iv_logout)
        assertViewDisplayed(R.id.progressLayout)
        WaitUtils.waitForCondition(
                60, TimeUnit.SECONDS,
                {
                    val rootLoginActivity = getActivity()?.findViewById<View>(R.id.rootLoginActivity)
                    rootLoginActivity != null
                },
                {
                    "$TAG:: Not able to find rootLoginActivity"
                }
        )
    }

    @Test
    fun testMessageButton_homeActivity() {
        clickOnView(R.id.iv_messaging)
        intended(hasComponent(MessagingActivity::class.java.name))
    }

    @Test
    fun testGetMeButton_homeActivity() {
        clickOnView(R.id.iv_getMe)
        onView(withId(R.id.dialogOk))
                .inRoot(isDialog())
                .check(matches(isDisplayed()))
    }
}