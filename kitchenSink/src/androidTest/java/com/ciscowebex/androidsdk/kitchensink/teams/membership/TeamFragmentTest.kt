package com.ciscowebex.androidsdk.kitchensink.teams.membership

import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withHint
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.ciscowebex.androidsdk.kitchensink.KitchenSinkTest
import com.ciscowebex.androidsdk.kitchensink.R
import com.ciscowebex.androidsdk.kitchensink.messaging.MessagingActivity
import com.ciscowebex.androidsdk.kitchensink.messaging.search.MessagingSearchActivity
import com.ciscowebex.androidsdk.kitchensink.messaging.teams.TeamsClientViewHolder
import com.ciscowebex.androidsdk.kitchensink.messaging.teams.membership.TeamMembershipActivity
import com.ciscowebex.androidsdk.kitchensink.utils.WaitUtils
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.random.Random


@RunWith(AndroidJUnit4ClassRunner::class)
class TeamFragmentTest : KitchenSinkTest() {

    val testTeamName = "Test Team"

    @Before
    override fun initTests() {
        super.initTests()
        setUpLogin()
    }

    @Test
    fun testTeamMembershipList_teamFragment() {
        goToMessagingActivity()
        longClickOnListItem(R.id.teamsRecyclerView, 0)
        WaitUtils.sleep(TIME_1_SEC)
        assertViewDisplayed(R.id.teamOptionsLabel)
        clickOnView(R.id.getMembers)

        intended(hasComponent(TeamMembershipActivity::class.java.name))
        assertViewDisplayed(R.id.membershipsRecyclerView)
    }

    private fun goToMessagingActivity() {
        clickOnView(R.id.iv_messaging)
        intended(hasComponent(MessagingActivity::class.java.name))
    }

    @Test
    fun addTeamMember_teamFragment() {
        goToMessagingActivity()
        clickOnView(R.id.addTeamsFAB)
        onView(withText(R.string.add_team))
                .inRoot(isDialog())
                .check(matches(isDisplayed()))
        val testTeam = testTeamName + Random.nextInt(0, 1000)
        onView(withHint(R.string.team_name_hint)).check(matches(isDisplayed()))
                .perform(ViewActions.typeText(testTeam))
        closeSoftKeyboard()
        clickOnViewWithText(android.R.string.ok)

        onView(withId(R.id.teamsRecyclerView))
                .perform(ViewActions.swipeDown())
        WaitUtils.sleep(TIME_1_SEC)
        onView(withId(R.id.teamsRecyclerView)).check(matches(hasDescendant(withText(testTeam))))
    }

    @Test
    fun addPersonToTeam_teamFragment(){
        goToMessagingActivity()
        onView(withId(R.id.teamsRecyclerView)).perform(RecyclerViewActions.actionOnItemAtPosition<TeamsClientViewHolder>(0, clickChildViewWithId(R.id.iv_add_to_team)))
        WaitUtils.sleep(TIME_1_SEC)
        intended(hasComponent(MessagingSearchActivity::class.java.name))
    }

    @Test
    fun testBottomSheetOptions_teamsFragment(){
        goToMessagingActivity()
        longClickOnListItem(R.id.teamsRecyclerView, 0)
        assertViewDisplayed(R.id.getMembers)
        assertViewDisplayed(R.id.editTeamName)
        assertViewDisplayed(R.id.addSpaceFromTeam)
        assertViewDisplayed(R.id.deleteTeam)
        assertViewDisplayed(R.id.cancel)
    }
}
