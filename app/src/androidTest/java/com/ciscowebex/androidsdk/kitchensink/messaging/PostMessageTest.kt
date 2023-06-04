package com.ciscowebex.androidsdk.kitchensink.messaging


import android.view.View
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.hasMinimumChildCount
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.ciscowebex.androidsdk.kitchensink.KitchenSinkTest
import com.ciscowebex.androidsdk.kitchensink.R
import com.ciscowebex.androidsdk.kitchensink.messaging.composer.MessageComposerActivity
import com.ciscowebex.androidsdk.kitchensink.messaging.spaces.SpacesFragment
import com.ciscowebex.androidsdk.kitchensink.messaging.spaces.adapters.SpacesClientViewHolder
import com.ciscowebex.androidsdk.kitchensink.messaging.spaces.detail.SpaceDetailActivity
import com.ciscowebex.androidsdk.kitchensink.person.PeopleClientViewHolder
import com.ciscowebex.androidsdk.kitchensink.person.PeopleFragment
import com.ciscowebex.androidsdk.kitchensink.utils.WaitUtils
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.IOException
import java.util.Random


@RunWith(AndroidJUnit4ClassRunner::class)
class PostMessageTest : KitchenSinkTest() {
    var min = 0
    var max = 100

    var random = Random()
    private var testMessage = "Hello Test Message "

    @Before
    override fun initTests() {
        super.initTests()
        setUpLogin()
        val number = random.nextInt(max - min + 1) + min
        testMessage += number
    }

    @Test
    fun postMessageBySpaceId() {
        launchFragmentInContainer<SpacesFragment>(null, R.style.AppTheme)
        assertViewDisplayed(R.id.spacesRecyclerView)
        onView(withId(R.id.spacesRecyclerView)).check(matches(hasMinimumChildCount(1)))
        onView(allOf(withId(R.id.spacesRecyclerView))).perform(RecyclerViewActions.actionOnItemAtPosition<SpacesClientViewHolder>(0, clickChildViewWithId(R.id.spaceTitleTextView)))
        Intents.intended(IntentMatchers.hasComponent(SpaceDetailActivity::class.java.name))
        onView(withId(R.id.postMessageFAB)).check(matches(isDisplayed())).perform(clickChildViewWithId(R.id.postMessageFAB))
        WaitUtils.sleep(1000)
        Intents.intended(IntentMatchers.hasComponent(MessageComposerActivity::class.java.name))
        onView(withId(R.id.message)).check(matches(isDisplayed())).perform(typeText(testMessage), closeSoftKeyboard())
        assertViewDisplayed(R.id.sendButton)
        clickOnView(R.id.sendButton)
        WaitUtils.sleep(5000)
        assertViewDisplayed(R.id.rootPostMessageDetailDialog)
    }

    private fun peopleFragmentBottomSheet() {
        launchFragmentInContainer<PeopleFragment>(null, R.style.AppTheme)
        WaitUtils.sleep(2000)
        assertViewDisplayed(R.id.recycler_view)
        assertViewDisplayed(R.id.search_view)
        onView(withId(R.id.recycler_view)).check(matches(hasMinimumChildCount(1)))
        onView(allOf(withId(R.id.recycler_view))).perform(RecyclerViewActions.actionOnItemAtPosition<PeopleClientViewHolder>(0, longclickChildViewWithId(R.id.personClientLayout)))
        WaitUtils.sleep(1000)
        assertViewDisplayed(R.id.peopleOptionsBottomSheet)
    }

    @Test
    fun fetchPersonDetailByID() {
        peopleFragmentBottomSheet()
        assertViewDisplayed(R.id.fetchPersonByID)
        clickOnView(R.id.fetchPersonByID)
        WaitUtils.sleep(2000)
        assertViewDisplayed(R.id.rootPersonDetailDialog)
    }

    @Test
    fun postMessageByPersonID() {
        peopleFragmentBottomSheet()
        assertViewDisplayed(R.id.postMessageByID)
        clickOnView(R.id.postMessageByID)
        WaitUtils.sleep(1000)
        Intents.intended(IntentMatchers.hasComponent(MessageComposerActivity::class.java.name))
        onView(withId(R.id.message)).check(matches(isDisplayed())).perform(typeText(testMessage), closeSoftKeyboard())
        assertViewDisplayed(R.id.sendButton)
        clickOnView(R.id.sendButton)
        WaitUtils.sleep(5000)
        assertViewDisplayed(R.id.rootPostMessageDetailDialog)
    }

    @Test
    fun postMessageByPersonEmail() {
        peopleFragmentBottomSheet()
        assertViewDisplayed(R.id.postMessageByEmail)
        clickOnView(R.id.postMessageByEmail)
        WaitUtils.sleep(1000)
        Intents.intended(IntentMatchers.hasComponent(MessageComposerActivity::class.java.name))
        onView(withId(R.id.message)).check(matches(isDisplayed())).perform(typeText(testMessage), closeSoftKeyboard())
        assertViewDisplayed(R.id.sendButton)
        clickOnView(R.id.sendButton)
        WaitUtils.sleep(5000)
        assertViewDisplayed(R.id.rootPostMessageDetailDialog)
    }

    @Throws(IOException::class)
    private fun getFileFromAssets(fileName: String): File = File(targetContext.cacheDir, fileName)
            .also {
                if (!it.exists()) {
                    it.outputStream().use { cache ->
                        targetContext.resources.assets.open(fileName).use { inputStream ->
                            inputStream.copyTo(cache)
                        }
                    }
                }
            }

    private fun addItemToRecyclerView(file: File): Matcher<View> {
        return object : BoundedMatcher<View, RecyclerView>(RecyclerView::class.java) {
            override fun describeTo(description: Description?) {
            }

            override fun matchesSafely(item: RecyclerView?): Boolean {
                item?.adapter?.let {
                    val adapter = it as MessageComposerActivity.UploadAttachmentsAdapter
//                    adapter.attachedFiles.add(file)
                    adapter.notifyDataSetChanged()
                    return true
                }

                return false
            }
        }
    }

    @Test
    fun sendContentByPersonEmail() {
        peopleFragmentBottomSheet()
        assertViewDisplayed(R.id.postMessageByEmail)
        clickOnView(R.id.postMessageByEmail)
        WaitUtils.sleep(1000)
        Intents.intended(IntentMatchers.hasComponent(MessageComposerActivity::class.java.name))
        onView(withId(R.id.message)).check(matches(isDisplayed())).perform(typeText(testMessage), closeSoftKeyboard())
        val filePath =  getFileFromAssets("cisco.png").absolutePath
        val file = File(filePath)
        val exist = File(filePath).exists()
        onView(withId(R.id.attachment_recycler_view)).check(matches(addItemToRecyclerView(file)))
        WaitUtils.sleep(1000)
        assertViewDisplayed(R.id.sendButton)
        clickOnView(R.id.sendButton)
        WaitUtils.sleep(5000)
        assertViewDisplayed(R.id.rootPostMessageDetailDialog)
    }
}