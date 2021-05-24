package com.ciscowebex.androidsdk.kitchensink.utils

import android.os.SystemClock
import android.util.Log
import androidx.test.espresso.web.model.Atom
import androidx.test.espresso.web.model.ElementReference
import androidx.test.espresso.web.webdriver.DriverAtoms
import androidx.test.espresso.web.webdriver.Locator
import junit.framework.Assert.fail
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

object WaitUtils {
    private const val TAG = "WaitUtils"
    internal fun sleep(millis: Long) {
        try {
            Thread.sleep(millis)
        } catch (ignored: InterruptedException) {
        }
    }

    fun waitElementToAppear(id: String) {
        var maxTries = 0
        while (maxTries != 60) {
            Thread.sleep(1000)
            try {
                if(DriverAtoms.findElement(Locator.ID, id) != null){
                    break
                }
            }catch (e: TimeoutException){ }

            maxTries++
        }
    }

    fun waitForCondition(time: Long, unit: TimeUnit, condition: () -> Boolean, failureMessage: () -> String, warnAfter: Long = 0): Long {
        return waitForCondition(unit.toMillis(time), condition, failureMessage, warnAfter)
    }

    fun waitForCondition(timeout: Long, condition: () -> Boolean, failureMessage: () -> String, warnAfter: Long = 0): Long {
        val startTime = SystemClock.uptimeMillis()
        val endTime = startTime + timeout

        while (true) {
            val timedOut = SystemClock.uptimeMillis() > endTime

            if (timedOut) {
                val format = "Condition not satisified after $timeout ms"

                Log.i(TAG, "waitForCondition timedOut, $format - ${failureMessage()}")

                fail(failureMessage())
            }

            sleep(50)

            if (condition()) {
                val duration = SystemClock.uptimeMillis() - startTime

                if ((warnAfter > 0) && (duration > warnAfter)) {
                    Log.i(TAG, "Condition took $duration ms, longer than $warnAfter ms")
                } else {
                    Log.i(TAG, "Condition took $duration ms")
                }

                return duration
            }
        }
    }

}