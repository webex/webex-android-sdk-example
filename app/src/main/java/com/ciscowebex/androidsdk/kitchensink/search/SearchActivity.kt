package com.ciscowebex.androidsdk.kitchensink.search

import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.ciscowebex.androidsdk.kitchensink.BaseActivity
import com.ciscowebex.androidsdk.kitchensink.R
import com.ciscowebex.androidsdk.kitchensink.calling.DialFragment
import com.ciscowebex.androidsdk.kitchensink.calling.calendarMeeting.CalendarMeetingListFragment
import com.ciscowebex.androidsdk.kitchensink.databinding.ActivitySearchBinding
import com.ciscowebex.androidsdk.kitchensink.utils.Constants
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.tabs.TabLayoutMediator.TabConfigurationStrategy
import org.koin.androidx.viewmodel.ext.android.viewModel

class SearchActivity : BaseActivity() {
    lateinit var binding: ActivitySearchBinding

    private val searchViewModel: SearchViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DataBindingUtil.setContentView<ActivitySearchBinding>(this, R.layout.activity_search)
                .also { binding = it }
                .apply {
                    viewPager.adapter = ViewPagerFragmentAdapter(this@SearchActivity, searchViewModel.titles)
                    TabLayoutMediator(tabLayout, viewPager,
                            TabConfigurationStrategy { tab: TabLayout.Tab, position: Int ->
                                tab.text = searchViewModel.titles[position]
                            }
                    ).attach()
                }
    }

    private class ViewPagerFragmentAdapter(fragmentActivity: FragmentActivity, val titles: List<String>) :
            FragmentStateAdapter(fragmentActivity) {
        override fun createFragment(position: Int): Fragment {
            when (position) {
                0 -> return DialFragment()
                1 -> {
                    val bundle = Bundle()
                    bundle.putString(Constants.Bundle.KEY_TASK_TYPE, SearchCommonFragment.Companion.TaskType.TaskSearchSpace)
                    val searchFragment = SearchCommonFragment()
                    searchFragment.arguments = bundle
                    return searchFragment
                }
                2 -> {
                    val bundle = Bundle()
                    bundle.putString(Constants.Bundle.KEY_TASK_TYPE, SearchCommonFragment.Companion.TaskType.TaskCallHistory)
                    val callHistoryFragment = SearchCommonFragment()
                    callHistoryFragment.arguments = bundle
                    return callHistoryFragment
                }
                3 -> {
                    val bundle = Bundle()
                    bundle.putString(Constants.Bundle.KEY_TASK_TYPE, SearchCommonFragment.Companion.TaskType.TaskListSpaces)
                    val spaceListFragment = SearchCommonFragment()
                    spaceListFragment.arguments = bundle
                    return spaceListFragment
                }
                4 -> return CalendarMeetingListFragment()
            }
            return SearchCommonFragment()
        }

        override fun getItemCount(): Int {
            return titles.size
        }
    }
}