package com.cisco.sparksdk.sparkkitchensink;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TableLayout;

import java.lang.reflect.Field;

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
        setOnclickListener(firstTab, 0);
        //firstTab.setIcon(R.drawable.ic_launcher); // set an icon for the
        // first tab
        tabLayout.addTab(firstTab); // add  the tab at in the TabLayout

        // Create a new Tab named "Second"
        TabLayout.Tab secondTab = tabLayout.newTab();
        secondTab.setText("People"); // set the Text for the second Tab
        setOnclickListener(secondTab, 1);
        //secondTab.setIcon(R.drawable.ic_launcher); // set an icon for the second tab
        tabLayout.addTab(secondTab); // add  the tab  in the TabLayout

        // Create a new Tab named "Third"
        TabLayout.Tab thirdTab = tabLayout.newTab();
        thirdTab.setText("Dialer"); // set the Text for the first Tab
        setOnclickListener(thirdTab, 2);
        //thirdTab.setIcon(R.drawable.ic_launcher); // set an icon for the first tab
        tabLayout.addTab(thirdTab); // add  the tab at in the TabLayout

        PagerAdapter adapter = new PagerAdapter(getSupportFragmentManager(), tabLayout.getTabCount());
        simpleViewPager.setAdapter(adapter);

        // addOnPageChangeListener event change the tab on slide
        simpleViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        simpleViewPager.setCurrentItem(0);
    }

    private void setOnclickListener(TabLayout.Tab tab, final int index) {
        Class c = tab.getClass();
        try {
            Field field = c.getDeclaredField("mView");
            field.setAccessible(true);
            final View view = (View) field.get(tab);
            if (view == null) return;
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    simpleViewPager.setCurrentItem(index);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void makeCall(String callee) {
        simpleViewPager.setCurrentItem(2);
        View v = simpleViewPager.getFocusedChild();
        EditText e = (EditText)v.findViewById(R.id.editCallee);
        e.setText(callee);
    }
}

