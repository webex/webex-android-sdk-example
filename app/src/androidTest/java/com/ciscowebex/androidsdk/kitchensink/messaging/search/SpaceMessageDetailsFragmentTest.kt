package com.ciscowebex.androidsdk.kitchensink.messaging.search

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.swipeDown
import androidx.test.espresso.action.ViewActions.swipeLeft
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.*
import com.ciscowebex.androidsdk.kitchensink.KitchenSinkTest
import com.ciscowebex.androidsdk.kitchensink.R
import com.ciscowebex.androidsdk.kitchensink.messaging.MessagingActivity
import com.ciscowebex.androidsdk.kitchensink.utils.WaitUtils
import org.junit.Before
import org.junit.Test


class SpaceMessageDetailsFragmentTest : KitchenSinkTest() {
    @Before
    override fun initTests() {
        super.initTests()
        setUpLogin()
    }

    @Test
    fun testTeamMembershipList_spaceMessageDetailsFragment() {
        gotoSpaces()

        clickOnItemInRecyclerView(R.id.spacesRecyclerView, 1)

        WaitUtils.sleep(1000)
        onView(withId(R.id.spaceMessageRecyclerView))
                .perform(swipeDown())
        WaitUtils.sleep(1000)
        clickOnItemInRecyclerView(R.id.spaceMessageRecyclerView, 0)

        assertViewDisplayed(R.id.msgIdTextView)
    }

    private fun gotoSpaces(){
        clickOnView(R.id.iv_messaging)
        intended(hasComponent(MessagingActivity::class.java.name))

        onView(withId(R.id.view_pager))
                .perform(swipeLeft())
    }

}