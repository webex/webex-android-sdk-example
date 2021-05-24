package com.ciscowebex.androidsdk.kitchensink.utils

import android.content.res.Resources
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.ViewAssertion
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import org.hamcrest.core.Is.`is`
import org.junit.Assert.assertThat

object RecyclerViewMatchers {

    fun withRecyclerView(recyclerViewId: Int): RecyclerViewMatcher {
        return RecyclerViewMatcher(recyclerViewId)
    }

    fun havingItemCount(itemCount: Int): RecyclerViewItemCountAssertion {
        return RecyclerViewItemCountAssertion(itemCount)
    }

    class RecyclerViewMatcher(private val recyclerViewId: Int) {

        fun atPosition(position: Int): Matcher<View> {
            return atPositionOnView(position, View.NO_ID)
        }

        fun atPositionOnView(position: Int, targetViewId: Int): Matcher<View> {

            return object : TypeSafeMatcher<View>() {
                var resources: Resources? = null
                var childView: View? = null

                override fun describeTo(description: Description) {
                    var idDescription = Integer.toString(recyclerViewId)
                    if (this.resources != null) {
                        idDescription = try {
                            this.resources!!.getResourceName(recyclerViewId)
                        } catch (var4: Resources.NotFoundException) {
                            String.format("%s (resource name not found)", recyclerViewId)
                        }
                    }
                    description.appendText("RecyclerView with id: $idDescription at position: $position on child view with id $targetViewId")
                }

                public override fun matchesSafely(view: View): Boolean {

                    this.resources = view.resources

                    if (childView == null) {
                        val recyclerView: RecyclerView? = view.rootView.findViewById<View>(recyclerViewId) as RecyclerView
                        if (recyclerView != null && recyclerView.id == recyclerViewId) {
                            val viewHolder = recyclerView.findViewHolderForAdapterPosition(position)
                            if (viewHolder != null) {
                                childView = viewHolder.itemView
                            }
                        } else {
                            return false
                        }
                    }

                    return if (targetViewId == View.NO_ID) {
                        view === childView
                    } else {
                        val targetView = childView?.findViewById<View>(targetViewId)
                        view === targetView
                    }
                }
            }
        }
    }

    class RecyclerViewItemCountAssertion(private val expectedCount: Int) : ViewAssertion {

        override fun check(view: View, noViewFoundException: NoMatchingViewException?) {
            if (noViewFoundException != null) {
                throw noViewFoundException
            }

            val recyclerView = view as RecyclerView
            val adapter = recyclerView.adapter
            assertThat(adapter!!.itemCount, `is`(expectedCount))
        }
    }
}
