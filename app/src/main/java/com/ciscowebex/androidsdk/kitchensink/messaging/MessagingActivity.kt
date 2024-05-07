package com.ciscowebex.androidsdk.kitchensink.messaging

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.ciscowebex.androidsdk.kitchensink.BaseActivity
import com.ciscowebex.androidsdk.kitchensink.R
import com.ciscowebex.androidsdk.kitchensink.databinding.ActivityMessagingBinding
import com.ciscowebex.androidsdk.kitchensink.messaging.spaces.SpacesFragment
import com.ciscowebex.androidsdk.kitchensink.messaging.spaces.members.MembershipFragment
import com.ciscowebex.androidsdk.kitchensink.messaging.teams.TeamsFragment
import com.ciscowebex.androidsdk.kitchensink.person.PeopleFragment
import com.google.android.material.tabs.TabLayoutMediator

class MessagingActivity : BaseActivity() {

    private lateinit var binding: ActivityMessagingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DataBindingUtil.setContentView<ActivityMessagingBinding>(this, R.layout.activity_messaging)
                .also { binding = it }
                .apply {
                    val tabs = listOf(getString(R.string.teams), getString(R.string.spaces), getString(R.string.people), getString(R.string.memberships))
                    viewPager.adapter = MessagingPagerAdapter(this@MessagingActivity, tabs.size)
                    viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                        override fun onPageSelected(position: Int) {
                            when(position) {
                                0 -> messagingMenu.visibility = View.INVISIBLE
                                1 -> messagingMenu.visibility = View.VISIBLE
                                2 -> messagingMenu.visibility = View.INVISIBLE
                                3 -> messagingMenu.visibility = View.INVISIBLE
                            }
                            super.onPageSelected(position)
                        }
                    })

                    TabLayoutMediator(binding.tabs, binding.viewPager, TabLayoutMediator.TabConfigurationStrategy { tab, position ->
                        tab.text = tabs[position]
                    }).attach()

                    setSupportActionBar(messagingMenu)
                }
    }

}

class MessagingPagerAdapter(fragmentActivity: FragmentActivity, private val numTabs: Int) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int {
        return numTabs
    }

    override fun createFragment(position: Int): Fragment {
        when (position) {
            0 -> return TeamsFragment()
            1 -> return SpacesFragment()
            2 -> return PeopleFragment()
            3 -> return MembershipFragment()
        }
        return Fragment()
    }
}