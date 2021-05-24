package com.ciscowebex.androidsdk.kitchensink.webhooks

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.swipeDown
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isDisplayingAtLeast
import androidx.test.espresso.matcher.ViewMatchers.hasMinimumChildCount
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.ciscowebex.androidsdk.kitchensink.KitchenSinkTest
import com.ciscowebex.androidsdk.kitchensink.R
import com.ciscowebex.androidsdk.kitchensink.utils.WaitUtils
import org.hamcrest.CoreMatchers.allOf
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Random


@RunWith(AndroidJUnit4ClassRunner::class)
class WebhookTest : KitchenSinkTest() {
    var min = 0
    var max = 100

    var random = Random()
    private var webhookName = "TestingWebhook "
    private var webhookUpdateName = "TestingUpdateWebhook "
    private var webhookURL = "https://webhook.site/6d71f999-00a9-43d9-ac9d-7ea29d1538f9"
    private var resource = "memberships"
    private var event = "created"
    private var secret = "secret100"

    @Before
    override fun initTests() {
        super.initTests()
        setUpLogin()
        val number = random.nextInt(max - min + 1) + min
        webhookName += number
        webhookUpdateName += number
        clickOnView(R.id.iv_webhook)
        Intents.intended(IntentMatchers.hasComponent(WebhooksActivity::class.java.name))
        WaitUtils.sleep(2000)
    }

    @Test
    fun webhook0_Create() {
        assertViewDisplayed(R.id.addWebhookButton)
        clickOnView(R.id.addWebhookButton)
        WaitUtils.sleep(1000)
        assertViewDisplayed(R.id.rootWebhookCreateDialog)
        onView(withId(R.id.nameEditText)).check(matches(isDisplayed())).perform(typeText(webhookName), closeSoftKeyboard())
        onView(withId(R.id.targetUrlEditText)).check(matches(isDisplayed())).perform(typeText(webhookURL), closeSoftKeyboard())
        onView(withId(R.id.resourceEditText)).check(matches(isDisplayed())).perform(typeText(resource), closeSoftKeyboard())
        onView(withId(R.id.eventEditText)).check(matches(isDisplayed())).perform(typeText(event), closeSoftKeyboard())
        onView(withId(R.id.secretEditText)).check(matches(isDisplayed())).perform(typeText(secret), closeSoftKeyboard())
        onView(withText("CREATE")).inRoot(isDialog())
                .check(matches(isDisplayed()))
                .perform(click())
        WaitUtils.sleep(5000)
        onView(withId(R.id.webhook_recycler_view)).check(matches(hasDescendant(withText(webhookName))))
    }

    @Test
    fun webhook1_Get() {
        onView(withId(R.id.webhook_recycler_view)).perform(withCustomConstraints(swipeDown(), isDisplayingAtLeast(85)))
        WaitUtils.sleep(5000)
        onView(withId(R.id.webhook_recycler_view)).check(matches(hasMinimumChildCount(1)))
        onView(allOf(withId(R.id.webhook_recycler_view))).perform(RecyclerViewActions.actionOnItemAtPosition<WebhooksActivity.WebhookListAdapter.webhookViewHolder>(0, longclickChildViewWithId(R.id.rootListItemLayout)))
        WaitUtils.sleep(1000)
        assertViewDisplayed(R.id.webhookOptionsBottomSheet)
        assertViewDisplayed(R.id.webhookGetDetails)
        clickOnView(R.id.webhookGetDetails)
        WaitUtils.sleep(5000)
        assertViewDisplayed(R.id.rootWebHookDetailDialog)
    }

    @Test
    fun webhook2_Delete() {
        onView(withId(R.id.webhook_recycler_view)).perform(withCustomConstraints(swipeDown(), isDisplayingAtLeast(85)))
        WaitUtils.sleep(5000)
        onView(withId(R.id.webhook_recycler_view)).check(matches(hasMinimumChildCount(1)))
        onView(allOf(withId(R.id.webhook_recycler_view))).perform(RecyclerViewActions.actionOnItemAtPosition<WebhooksActivity.WebhookListAdapter.webhookViewHolder>(0, longclickChildViewWithId(R.id.rootListItemLayout)))
        WaitUtils.sleep(1000)
        assertViewDisplayed(R.id.webhookOptionsBottomSheet)
        assertViewDisplayed(R.id.webhookDelete)
        clickOnView(R.id.webhookDelete)
    }

    @Test
    fun webhook3_Update() {
        onView(withId(R.id.webhook_recycler_view)).perform(withCustomConstraints(swipeDown(), isDisplayingAtLeast(85)))
        WaitUtils.sleep(5000)
        onView(withId(R.id.webhook_recycler_view)).check(matches(hasMinimumChildCount(1)))
        onView(allOf(withId(R.id.webhook_recycler_view))).perform(RecyclerViewActions.actionOnItemAtPosition<WebhooksActivity.WebhookListAdapter.webhookViewHolder>(0, longclickChildViewWithId(R.id.rootListItemLayout)))
        WaitUtils.sleep(1000)
        assertViewDisplayed(R.id.webhookOptionsBottomSheet)
        assertViewDisplayed(R.id.webhookUpdate)
        clickOnView(R.id.webhookUpdate)
        assertViewDisplayed(R.id.rootWebhookUpdateDialog)
        onView(withId(R.id.nameEditText)).check(matches(isDisplayed())).perform(replaceText(webhookUpdateName), closeSoftKeyboard())
        onView(withText("UPDATE")).inRoot(isDialog())
                .check(matches(isDisplayed()))
                .perform(click())
        WaitUtils.sleep(5000)
        assertViewDisplayed(R.id.rootWebHookDetailDialog)
    }
}