package com.ciscowebex.androidsdk.kitchensink.messaging

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.ciscowebex.androidsdk.kitchensink.BaseActivity
import com.ciscowebex.androidsdk.kitchensink.R
import com.google.android.material.tabs.TabLayout
import com.ciscowebex.androidsdk.kitchensink.messaging.spaces.SpacesFragment
import com.ciscowebex.androidsdk.kitchensink.messaging.spaces.members.MembershipFragment
import com.ciscowebex.androidsdk.kitchensink.messaging.teams.TeamsFragment
import com.ciscowebex.androidsdk.kitchensink.person.PeopleFragment
import com.google.android.material.tabs.TabLayoutMediator

class MessagingActivity : BaseActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var messagingMenu: Toolbar
    private lateinit var tabs: TabLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_messaging)
        
        viewPager = findViewById(R.id.view_pager)
        messagingMenu = findViewById(R.id.messagingMenu)
        tabs = findViewById(R.id.tabs)
        
        val tabsList = listOf(getString(R.string.teams), getString(R.string.spaces), getString(R.string.people), getString(R.string.memberships))
        viewPager.adapter = MessagingPagerAdapter(this@MessagingActivity, tabsList.size)
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

        TabLayoutMediator(tabs, viewPager, TabLayoutMediator.TabConfigurationStrategy { tab, position ->
            tab.text = tabsList[position]
        }).attach()

        setSupportActionBar(messagingMenu)
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