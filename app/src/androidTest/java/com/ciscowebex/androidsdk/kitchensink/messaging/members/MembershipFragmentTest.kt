package com.ciscowebex.androidsdk.kitchensink.messaging.members

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.swipeLeft
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.ciscowebex.androidsdk.kitchensink.KitchenSinkTest
import com.ciscowebex.androidsdk.kitchensink.R
import com.ciscowebex.androidsdk.kitchensink.messaging.MessagingActivity
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4ClassRunner::class)
class MembershipFragmentTest: KitchenSinkTest() {
    @Before
    override fun initTests() {
        super.initTests()
        setUpLogin()
    }

    @Test
    fun membershipTest_membershipFragmentTest(){
        clickOnView(R.id.iv_messaging)
        Intents.intended(IntentMatchers.hasComponent(MessagingActivity::class.java.name))

        onView(withId(R.id.view_pager)).perform(swipeLeft())
        onView(withId(R.id.view_pager)).perform(swipeLeft())
        onView(withId(R.id.view_pager)).perform(swipeLeft())

        assertViewDisplayed(R.id.membershipsRecyclerView)
        onView(withId(R.id.membershipsRecyclerView)).check(matches(hasDescendant(withId(R.id.membershipPersonDisplayNameTextView))))
    }
}