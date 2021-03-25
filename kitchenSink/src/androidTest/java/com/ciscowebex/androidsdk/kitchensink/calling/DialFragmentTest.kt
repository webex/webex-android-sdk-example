package com.ciscowebex.androidsdk.kitchensink.calling

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ciscowebex.androidsdk.kitchensink.KitchenSinkTest
import com.ciscowebex.androidsdk.kitchensink.R
import com.ciscowebex.androidsdk.kitchensink.utils.Constants
import org.hamcrest.CoreMatchers.allOf
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DialFragmentTest : KitchenSinkTest() {

    override fun initTests() {
        super.initTests()
        setUpLogin()
    }

    @Test
    fun testDialKeys() {
        launchFragmentInContainer<DialFragment>()
        // Dial "0123456789"
        clickOnView(R.id.tv_number_0)
        clickOnView(R.id.tv_number_1)
        clickOnView(R.id.tv_number_2)
        clickOnView(R.id.tv_number_3)
        clickOnView(R.id.tv_number_4)
        clickOnView(R.id.tv_number_5)
        clickOnView(R.id.tv_number_6)
        clickOnView(R.id.tv_number_7)
        clickOnView(R.id.tv_number_8)
        clickOnView(R.id.tv_number_9)
        longClickOnView(R.id.tv_number_0)
        clickOnView(R.id.tv_number_star)
        clickOnView(R.id.tv_number_hash)
        // Check the dialed value
        assertViewWithText(R.id.et_dial_input, "0123456789+*#")
    }

    @Test
    fun testBackPress() {
        launchFragmentInContainer<DialFragment>()
        for (i in 1..5) {
            clickOnView(R.id.tv_number_0)
        }
        assertViewWithText(R.id.et_dial_input, "00000")
        clickOnView(R.id.ib_backspace)
        assertViewWithText(R.id.et_dial_input, "0000")
        longClickOnView(R.id.ib_backspace)
        assertViewWithText(R.id.et_dial_input, "")
    }

    @Test
    fun testToggleKeypadAndDialpad() {
        launchFragmentInContainer<DialFragment>()
        assertViewDisplayed(R.id.ib_keypad_toggle)
        clickOnView(R.id.ib_keypad_toggle)

        assertViewNotDisplayed(R.id.ib_keypad_toggle)
        assertViewDisplayed(R.id.ib_numpad_toggle)
        assertViewNotDisplayed(R.id.dial_buttons_container)

        clickOnView(R.id.ib_numpad_toggle)

        assertViewNotDisplayed(R.id.ib_numpad_toggle)
        assertViewDisplayed(R.id.ib_keypad_toggle)
        assertViewDisplayed(R.id.dial_buttons_container)
    }

    @Test
    fun testCallButton() {
        launchFragmentInContainer<DialFragment>()
        for (i in 1..5) {
            clickOnView(R.id.tv_number_1)
        }
        clickOnView(R.id.ib_startCall)
        intended(allOf(hasComponent(CallActivity::class.java.name),
                hasExtra(Constants.Intent.OUTGOING_CALL_CALLER_ID, "11111")))

    }
}