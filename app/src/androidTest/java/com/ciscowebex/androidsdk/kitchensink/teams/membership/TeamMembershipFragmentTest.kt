package com.ciscowebex.androidsdk.kitchensink.teams.membership

import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.ciscowebex.androidsdk.kitchensink.KitchenSinkTest
import com.ciscowebex.androidsdk.kitchensink.R
import com.ciscowebex.androidsdk.kitchensink.messaging.MessagingActivity
import com.ciscowebex.androidsdk.kitchensink.messaging.teams.membership.TeamMembershipActivity
import com.ciscowebex.androidsdk.kitchensink.messaging.teams.membership.TeamMembershipFragment
import com.ciscowebex.androidsdk.kitchensink.utils.Constants
import com.ciscowebex.androidsdk.kitchensink.utils.WaitUtils
import org.junit.Before
import org.junit.Test


class TeamMembershipFragmentTest : KitchenSinkTest() {
    var teamId = "1d3a4ec0-15b0-11eb-b0f2-7bf0c59106a7"

    @Before
    override fun initTests() {
        super.initTests()
        setUpLogin()
    }

    @Test
    fun testTeamMembershipList_homeActivity() {
        clickOnView(R.id.iv_messaging)

        intended(hasComponent(MessagingActivity::class.java.name))
        longClickOnListItem(R.id.teamsRecyclerView, 0)
        WaitUtils.sleep(1000)

        assertViewDisplayed(R.id.teamOptionsLabel)
        clickOnView(R.id.getMembers)

        intended(hasComponent(TeamMembershipActivity::class.java.name))
        assertViewDisplayed(R.id.membershipsRecyclerView)
    }

    @Test
    fun testTeamMembershipDetails() {
        clickOnView(R.id.iv_messaging)

        intended(hasComponent(MessagingActivity::class.java.name))
        longClickOnListItem(R.id.teamsRecyclerView, 0)
        WaitUtils.sleep(1000)

        assertViewDisplayed(R.id.teamOptionsLabel)
        clickOnView(R.id.getMembers)

        intended(hasComponent(TeamMembershipActivity::class.java.name))
        assertViewDisplayed(R.id.membershipsRecyclerView)

        longClickOnListItem(R.id.membershipsRecyclerView, 0)
        WaitUtils.sleep(1000)


        assertViewDisplayed(R.id.teamMemberActionOptionsLabel)
        clickOnView(R.id.getMembershipDetails)

        assertViewDisplayed(R.id.rootMemberDetailsDialog)

    }

    @Test
    fun testDeleteTeamMembership() {
        val bundle = Bundle().apply {
            putString(Constants.Bundle.TEAM_ID, teamId)
        }
        launchFragmentInContainer<TeamMembershipFragment>(bundle, R.style.Theme_AppCompat)
        WaitUtils.sleep(1000)
        longClickOnListItem(R.id.membershipsRecyclerView, 0)
        assertViewDisplayed(R.id.deleteMembership)
        WaitUtils.sleep(1000)
        clickOnView(R.id.deleteMembership)
        onView(withText(R.string.confirm_delete_membership_action))
                .inRoot(isDialog())
                .check(matches(isDisplayed()))
    }

}