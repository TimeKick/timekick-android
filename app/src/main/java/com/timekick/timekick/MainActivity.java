package com.timekick.timekick;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.preference.PreferenceManager;

public class MainActivity extends ActionBarActivity implements ActionBar.TabListener {

    /**
     * The {@link PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private int totalCount;

    public static String ACTIVITY_TYPE = "Time";
    private SmartFragmentStatePagerAdapter adapterViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PreferenceManager.setDefaultValues(this, R.xml.pref_general, false);

        setContentView(R.layout.activity_time);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        //mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        //mViewPager.setAdapter(mSectionsPagerAdapter);
        adapterViewPager = new MyPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(adapterViewPager);

        // Set up the action bar.
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.setStackedBackgroundDrawable(new ColorDrawable(Color.parseColor("#F1E2F1")));

        actionBar.setDisplayUseLogoEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setIcon(R.drawable.logo);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < adapterViewPager.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(adapterViewPager.getPageTitle(i))
                                    //.setIcon(mSectionsPagerAdapter.getIcon(i))
                            .setTabListener(this));

        }

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        editor = prefs.edit();
        totalCount = prefs.getInt("counter", 0);
        totalCount++;
        editor.putInt("counter", totalCount);
        editor.commit();

        //Feedback Prompt
        Boolean show_feedback = prefs.getBoolean(SettingsActivity.KEY_FEEDBACK, Boolean.parseBoolean(""));
        if (totalCount%5==0 && show_feedback) {
            new AlertDialog.Builder(this)
                    .setTitle("Feedback")
                    .setMessage("Thank you for using TimeKick.  Would you please let us know what we could do better?")
                    .setCancelable(true)
                    .setPositiveButton("Sure", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.timekickap.com/support"));
                            startActivity(browserIntent);
                        }
                    })
                    .setNegativeButton("No Thanks", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    }).create().show();
        }
    }

    public void switchToTab(Integer tabIndex) {
        if (mViewPager != null) {
            mViewPager.setCurrentItem(tabIndex);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_time, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            //Settings
            Intent intent = new Intent(this, SettingsActivity.class);
            this.startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    public void loadFavorite() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        if (sharedPref != null) {
            String favMode = sharedPref.getString(getString(R.string.favorite_mode_key),null);
            if (favMode != null) {
                if (favMode.equals(getString(R.string.activity_time))) {
                    switchToTab(0);
                    ((TimeFragment)adapterViewPager.getRegisteredFragment(0)).loadFavorite();
                } else if (favMode.equals(getString(R.string.activity_countdown))) {
                    switchToTab(1);
                    ((CountdownFragment)adapterViewPager.getRegisteredFragment(1)).loadFavorite();
                } else if (favMode.equals(getString(R.string.activity_target))) {
                    switchToTab(2);
                    ((TargetFragment)adapterViewPager.getRegisteredFragment(2)).loadFavorite();
                }
            }
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return TimeFragment.newInstance(position);
                case 1:
                    return CountdownFragment.newInstance(position);
                case 2:
                    return TargetFragment.newInstance(position);
            }
            return null;
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Time";
                case 1:
                    return "Countdown";
                case 2:
                    return "Target";
            }
            return null;
        }
    }

    public static class MyPagerAdapter extends SmartFragmentStatePagerAdapter {
        private static int NUM_ITEMS = 3;

        public MyPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        // Returns total number of pages
        @Override
        public int getCount() {
            return NUM_ITEMS;
        }

        // Returns the fragment to display for that page
        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0: // Fragment # 0 - This will show FirstFragment
                    return TimeFragment.newInstance(0);
                case 1: // Fragment # 0 - This will show FirstFragment different title
                    return CountdownFragment.newInstance(1);
                case 2: // Fragment # 1 - This will show SecondFragment
                    return TargetFragment.newInstance(2);
                default:
                    return null;
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Time";
                case 1:
                    return "Countdown";
                case 2:
                    return "Target";
            }
            return null;
        }
    }
}