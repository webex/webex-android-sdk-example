package com.ciscowebex.androidsdk.kitchensink

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.annotation.NonNull
import androidx.annotation.StringRes
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.PerformException
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.espresso.web.sugar.Web
import androidx.test.espresso.web.webdriver.DriverAtoms
import androidx.test.espresso.web.webdriver.Locator
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.ciscowebex.androidsdk.kitchensink.auth.LoginActivity
import com.ciscowebex.androidsdk.kitchensink.utils.RecyclerViewMatchers.withRecyclerView
import com.ciscowebex.androidsdk.kitchensink.utils.WaitUtils
import com.ciscowebex.androidsdk.kitchensink.utils.WaitUtils.waitForCondition
import com.google.android.material.tabs.TabLayout
import org.hamcrest.Matcher
import org.hamcrest.Matchers.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.rules.Timeout
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
abstract class KitchenSinkTest {

    val TAG = KitchenSinkTest::class.java.simpleName
    val TIME_1_SEC: Long = 1000

    val targetContext: Context by lazy {
        InstrumentationRegistry.getInstrumentation().targetContext as Context
    }

    @get: Rule
    val activityRule = ActivityScenarioRule(LoginActivity::class.java)

    @Rule
    @JvmField
    var teamsTestTimeout: Timeout = Timeout(5, TimeUnit.MINUTES)

    @Before
    open fun initTests() {
        Intents.init()
    }

    @After
    fun releaseIntents(){
        Intents.release()
    }

    fun setUpLogin() {
        val testEmail = "xeiotulvlhijlxtrmw@awdrt.com"
        val testPassword = "Test1234"

        activityRule.scenario.moveToState(Lifecycle.State.RESUMED)

        assertViewDisplayed(R.id.btn_oauth_login)
        clickOnView(R.id.btn_oauth_login)

        WaitUtils.sleep(2000)

        if (getActivity() is LoginActivity) {
            assertViewDisplayed(R.id.loginWebview)

            Web.onWebView().forceJavascriptEnabled()

            WaitUtils.waitElementToAppear("IDToken1")
            Web.onWebView(withId(R.id.loginWebview)).withElement(DriverAtoms.findElement(Locator.ID, "IDToken1"))

                    // Clear previous input
                    .perform(DriverAtoms.clearElement())
                    // Enter text into the input element
                    .perform(DriverAtoms.webKeys(testEmail))
            WaitUtils.waitElementToAppear("IDButton2")
            Web.onWebView(withId(R.id.loginWebview)).withElement(DriverAtoms.findElement(Locator.ID, "IDButton2")).perform(DriverAtoms.webClick())
            WaitUtils.waitElementToAppear("IDToken2")
            Web.onWebView(withId(R.id.loginWebview)).withElement(DriverAtoms.findElement(Locator.ID, "IDToken2"))
                    .perform(DriverAtoms.clearElement())
                    .perform(DriverAtoms.webKeys(testPassword))

            WaitUtils.waitElementToAppear("Button1")
            Web.onWebView(withId(R.id.loginWebview)).withElement(DriverAtoms.findElement(Locator.ID, "Button1")).perform(DriverAtoms.webClick())
        }

        waitForCondition(
                60, TimeUnit.SECONDS,
                {
                    val homeActivityRootView = getActivity()?.findViewById<View>(R.id.rootHomeActivity)
                    homeActivityRootView != null
                },
                {
                    "$TAG:: Not able to find homeActivityRootView"
                }
        )

        activityRule.scenario.close()
    }

    protected fun typeTextAction(@IdRes viewId: Int, textToType: String = "Summer is good") {
        onView(withId(viewId))
                .check(matches(isDisplayed()))
                .perform(typeText(textToType))
        closeSoftKeyboard()
    }

    protected fun assertView(@StringRes textOnView: Int) {
        onView(withText(textOnView)).check(matches(isDisplayed()))
    }

    protected fun assertView(@IdRes viewId: Int, @StringRes textOnView: Int) {
        onView(allOf(withId(viewId), withText(textOnView))).check(matches(isDisplayed()))
    }

    protected fun assertViewDisplayed(@IdRes viewId: Int) {
        onView(withId(viewId)).check(matches(isDisplayed()))
    }

    protected fun assertViewNotDisplayed(@IdRes viewId: Int) {
        onView(withId(viewId)).check(matches(not(isDisplayed())))
    }

    protected fun assertViewWithContentDescription(@IdRes viewId: Int, @StringRes stringId: Int) {
        onView(allOf(withId(viewId), withContentDescription(stringId)))
    }

    protected fun clickOnView(@IdRes viewId: Int) {
        onView(withId(viewId)).perform(click())
    }

    protected fun clickOnViewWithText(@StringRes stringId: Int) {
        onView(withText(stringId)).perform(click())
    }

    protected fun longClickOnView(@IdRes viewId: Int) {
        onView(withId(viewId)).perform(longClick())
    }

    protected fun assertViewExists(@IdRes viewId: Int) {
        onView(allOf(withId(viewId))).check(matches(isDisplayed()))
    }

    protected fun assertViewWithText(@IdRes viewId: Int, @NonNull textOnView: String) {
        onView(allOf(withId(viewId), withText(containsString(textOnView)))).check(matches(isDisplayed()))
    }

    protected fun assertViewWithText(@IdRes viewId: Int, @NonNull @StringRes stringId: Int) {
        onView(allOf(withId(viewId), withText(stringId))).check(matches(isDisplayed()))
    }

    protected fun getResourceName(@IdRes id: Int): String {
        return targetContext.resources.getResourceName(id)
    }

    protected fun selectTab(tabIndex: Int): ViewAction {
        return object : ViewAction {
            override fun getDescription() = "with tab at index $tabIndex"

            override fun getConstraints() = allOf(isDisplayed(), isAssignableFrom(TabLayout::class.java))

            override fun perform(uiController: UiController, view: View) {
                val tabLayout = view as TabLayout
                val tabAtIndex: TabLayout.Tab = tabLayout.getTabAt(tabIndex)
                        ?: throw PerformException.Builder()
                                .withCause(Throwable("No tab at index $tabIndex"))
                                .build()

                tabAtIndex.select()
            }
        }
    }

    fun getActivity(): Activity? {
        val activity = arrayOfNulls<Activity>(1)
        onView(isRoot()).check { view, _ ->
            var checkedView = view
            while (checkedView is ViewGroup && checkedView.childCount > 0) {
                checkedView = checkedView.getChildAt(0)
                if (checkedView.context is Activity) {
                    activity[0] = checkedView.context as Activity
                    break
                }
            }
        }
        return activity[0]
    }

    protected fun longClickOnListItem(@IdRes viewId: Int, itemPosition: Int, @IdRes targetViewId: Int? = null) {
        if (targetViewId == null) {
            onView(withRecyclerView(viewId).atPosition(itemPosition))
        } else {
            onView(withRecyclerView(viewId).atPositionOnView(itemPosition, targetViewId))
        }.check(matches(isDisplayed())).perform(longClick())
    }

    protected fun clickChildViewWithId(id: Int): ViewAction? {
        return object : ViewAction {
            override fun getConstraints(): Matcher<View> {
                return isDisplayed()
            }

            override fun getDescription(): String {
                return "Click on a child view with specified id."
            }

            override fun perform(uiController: UiController, view: View) {
                val v = view.findViewById<View>(id)
                v.performClick()
            }
        }
    }

    protected fun longclickChildViewWithId(id: Int): ViewAction? {
        return object : ViewAction {
            override fun getConstraints(): Matcher<View> {
                return isDisplayed()
            }

            override fun getDescription(): String {
                return "Click on a child view with specified id."
            }

            override fun perform(uiController: UiController, view: View) {
                val v = view.findViewById<View>(id)
                v.performLongClick()
            }
        }
    }

    protected fun withCustomConstraints(action: ViewAction, constraints: Matcher<View>): ViewAction? {
        return object : ViewAction {
            override fun getConstraints(): Matcher<View> {
                return constraints
            }

            override fun getDescription(): String {
                return action.description
            }

            override fun perform(uiController: UiController, view: View) {
                action.perform(uiController, view)
            }
        }
    }

    protected fun ScrollToBottomAction(): ViewAction? {
        return object : ViewAction {
            override fun getDescription(): String {
                return "scroll RecyclerView to bottom"
            }

            override fun getConstraints(): Matcher<View> {
                return allOf<View>(isAssignableFrom(RecyclerView::class.java), isDisplayed())
            }

            override fun perform(uiController: UiController?, view: View?) {
                val recyclerView = view as RecyclerView
                val itemCount = recyclerView.adapter?.itemCount
                val position = itemCount?.minus(1) ?: 0
                recyclerView.scrollToPosition(position)
                uiController?.loopMainThreadUntilIdle()
            }
        }
    }

    protected fun clickOnItemInRecyclerView(@IdRes viewId: Int, itemPosition: Int, @IdRes targetViewId: Int? = null) {
        if (targetViewId == null) {
            onView(withRecyclerView(viewId).atPosition(itemPosition))
        } else {
            onView(withRecyclerView(viewId).atPositionOnView(itemPosition, targetViewId))
        }.check(matches(isDisplayed())).perform(click())
    }
}
