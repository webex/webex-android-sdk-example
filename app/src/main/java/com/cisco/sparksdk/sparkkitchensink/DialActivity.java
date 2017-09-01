package com.cisco.sparksdk.sparkkitchensink;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import static com.ciscospark.Spark.LogLevel.RELEASE;

public class DialActivity extends AppCompatActivity {

    private static final String TAG = "DialActivity";

    ViewPager simpleViewPager;
    TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        Log.i(TAG, "onCreate: ->start");
        setContentView(R.layout.activity_dial);


        // get the reference of ViewPager and TabLayout
        simpleViewPager = (ViewPager) findViewById(R.id.simpleViewPager);
        tabLayout = (TabLayout) findViewById(R.id.simpleTabLayout);

        // Create a new Tab named "First"
        TabLayout.Tab firstTab = tabLayout.newTab();
        firstTab.setText("History"); // set the Text for the first Tab
        //firstTab.setIcon(R.drawable.ic_launcher); // set an icon for the
        // first tab
        tabLayout.addTab(firstTab); // add  the tab at in the TabLayout

        // Create a new Tab named "Second"
        TabLayout.Tab secondTab = tabLayout.newTab();
        secondTab.setText("People"); // set the Text for the second Tab
        //secondTab.setIcon(R.drawable.ic_launcher); // set an icon for the second tab
        tabLayout.addTab(secondTab); // add  the tab  in the TabLayout

        // Create a new Tab named "Third"
        TabLayout.Tab thirdTab = tabLayout.newTab();
        thirdTab.setText("Dialer"); // set the Text for the first Tab
        //thirdTab.setIcon(R.drawable.ic_launcher); // set an icon for the first tab
        tabLayout.addTab(thirdTab); // add  the tab at in the TabLayout

        com.cisco.sparksdk.sparkkitchensink.PagerAdapter adapter = new com.cisco.sparksdk
                .sparkkitchensink.PagerAdapter(getSupportFragmentManager(), tabLayout.getTabCount());
        simpleViewPager.setAdapter(adapter);

        // addOnPageChangeListener event change the tab on slide
        simpleViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
    }
}

