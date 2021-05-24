package com.ciscowebex.androidsdk.kitchensink.messaging.search

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.ciscowebex.androidsdk.kitchensink.KitchenSinkTest
import com.ciscowebex.androidsdk.kitchensink.R
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4ClassRunner::class)
class SearchPeopleFragmentTest : KitchenSinkTest() {
    @Before
    override fun initTests() {
        super.initTests()
        setUpLogin()
    }

    @Test
    fun testSearchPeopleByName() {
        launchFragmentInContainer<SearchPeopleFragment>()
        assertViewDisplayed(R.id.search_view)
        typeTextAction(R.id.search_view, "rohit sharma")
        onView(withId(R.id.recycler_view))
                .check(matches(hasDescendant(withText("Rohit Sharma"))))
    }


    @Test
    fun testSearchPeopleByEmailId() {
        launchFragmentInContainer<SearchPeopleFragment>()
        assertViewDisplayed(R.id.search_view)
        typeTextAction(R.id.search_view, "webextestac@gmail.com")
        onView(withId(R.id.recycler_view))
                .check(matches(hasDescendant(withText("Rohit Sharma"))))
    }

}