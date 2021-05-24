package com.ciscowebex.androidsdk.kitchensink

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers
import com.ciscowebex.androidsdk.kitchensink.search.SearchCommonFragment
import org.junit.Before
import org.junit.Test


class SearchActivityTest : KitchenSinkTest() {

    companion object{
        const val CALL_HISTORY_TAB_INDEX = 2
        const val SPACES_TAB_INDEX = 3
    }

    @Before
    override fun initTests() {
        super.initTests()
        setUpLogin()
    }

    @Test
    fun testCallHistory_searchActivity() {
        intended(IntentMatchers.hasComponent(HomeActivity::class.java.name))
        clickOnView(R.id.iv_startCall)
        selectTab(CALL_HISTORY_TAB_INDEX)
        launchFragmentInContainer<SearchCommonFragment>()
        assertViewDisplayed(R.id.recycler_view)
    }

    @Test
    fun testSpaces_searchActivity(){
        intended(IntentMatchers.hasComponent(HomeActivity::class.java.name))
        clickOnView(R.id.iv_startCall)
        selectTab(SPACES_TAB_INDEX)
        launchFragmentInContainer<SearchCommonFragment>()
        assertViewDisplayed(R.id.recycler_view)
    }


}