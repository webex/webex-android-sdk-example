package com.ciscowebex.androidsdk.kitchensink.messaging.spaces

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.ciscowebex.androidsdk.kitchensink.KitchenSinkTest
import com.ciscowebex.androidsdk.kitchensink.R
import com.ciscowebex.androidsdk.kitchensink.messaging.spaces.members.membersReadStatus.MembershipReadStatusActivity
import com.ciscowebex.androidsdk.kitchensink.utils.WaitUtils
import org.junit.Before
import org.junit.Test

class SpacesFragmentTest : KitchenSinkTest() {
    @Before
    override fun initTests() {
        super.initTests()
        setUpLogin()
    }

    @Test
    fun testDeleteSpace() {
        launchFragmentInContainer<SpacesFragment>(themeResId = R.style.Theme_AppCompat)
        longClickOnListItem(R.id.spacesRecyclerView, 0)
        WaitUtils.sleep(TIME_1_SEC)
        onView(withId(R.id.deleteSpace))
                .inRoot(isDialog())
                .check(matches(isDisplayed()))
        clickOnView(R.id.deleteSpace)
        WaitUtils.sleep(TIME_1_SEC)
        onView(withText(R.string.delete_space))
                .inRoot(isDialog())
                .check(matches(isDisplayed()))
    }

    @Test
    fun testShowSpaceMembersWithReadStatus() {
        launchFragmentInContainer<SpacesFragment>(themeResId = R.style.Theme_AppCompat)
        longClickOnListItem(R.id.spacesRecyclerView, 0)
        WaitUtils.sleep(TIME_1_SEC)
        onView(withId(R.id.showSpaceMembersWithReadStatus))
                .inRoot(isDialog())
                .check(matches(isDisplayed()))
        clickOnView(R.id.showSpaceMembersWithReadStatus)
        intended(hasComponent(MembershipReadStatusActivity::class.java.name))
    }
}