package com.ciscowebex.androidsdk.kitchensink.auth

import androidx.test.filters.LargeTest
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.ciscowebex.androidsdk.kitchensink.KitchenSinkTest
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4ClassRunner::class)
@LargeTest
open class LoginActivityTest : KitchenSinkTest() {

    @Test
    fun testWebExLogin_LoginActivity() {
        setUpLogin()
    }
}