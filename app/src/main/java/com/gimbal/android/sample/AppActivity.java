/**
 * Copyright (C) 2015 Gimbal, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of Gimbal, Inc.
 *
 * The following sample code illustrates various aspects of the Gimbal SDK.
 *
 * The sample code herein is provided for your convenience, and has not been
 * tested or designed to work on any particular system configuration. It is
 * provided AS IS and your use of this sample code, whether as provided or
 * with any modification, is at your own risk. Neither Gimbal, Inc.
 * nor any affiliate takes any liability nor responsibility with respect
 * to the sample code, and disclaims all warranties, express and
 * implied, including without limitation warranties on merchantability,
 * fitness for a specified purpose, and against infringement.
 */
package com.gimbal.android.sample;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListView;

import com.gimbal.experience.android.Action;
import com.gimbal.experience.android.ExperienceListener;
import com.gimbal.experience.android.ExperienceManager;

import java.util.Collection;


public class AppActivity extends AppCompatActivity{
    private static final String GIMBAL_POP_WINDOW = "gimbal_popup_window";

    private GimbalEventReceiver gimbalEventReceiver;
    private GimbalEventListAdapter adapter;

    private FrameLayout fragmentContainerLayout;
    private Button fragmentCloseButton;

    private ExperienceListener experienceListener = new ExperienceListener() {
        @Override
        public Collection<Action> filterActions(Collection<Action> actions) {
            return null;
        }

        @Override
        public void presentFragmentForAction(final Fragment fragment, Action action) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    handlePresentFragment(fragment);
                }
            });
        }

        @Override
        public Action presentNotificationForAction(Action action) {
            return null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startService(new Intent(this, AppService.class));

        if (GimbalDAO.showOptIn(getApplicationContext())) {
            startActivity(new Intent(this, OptInActivity.class));
        }
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        Button book = (Button) findViewById(R.id.book);

        book.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AppActivity.this, BookingActivity.class));
            }
        });

        adapter = new GimbalEventListAdapter(this);

        ListView listView = (ListView) findViewById(R.id.listview);
        listView.setAdapter(adapter);

        fragmentContainerLayout = (FrameLayout) findViewById(R.id.popup_window);
        fragmentCloseButton = (Button) findViewById(R.id.close_button);
        fragmentCloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleCloseFragment();
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        processActionFromIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        adapter.setEvents(GimbalDAO.getEvents(getApplicationContext()));
    }

    @Override
    protected void onStart() {
        super.onStart();
        gimbalEventReceiver = new GimbalEventReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(GimbalDAO.GIMBAL_NEW_EVENT_ACTION);
        intentFilter.addAction(AppService.APPSERVICE_STARTED_ACTION);
        registerReceiver(gimbalEventReceiver, intentFilter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(gimbalEventReceiver);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ExperienceManager.getInstance().removeListener(experienceListener);
    }

    // --------------------
    // EVENT RECEIVER
    // --------------------

    class GimbalEventReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null) {
                if (intent.getAction().compareTo(GimbalDAO.GIMBAL_NEW_EVENT_ACTION) == 0) {
                    adapter.setEvents(GimbalDAO.getEvents(getApplicationContext()));
                } else if (intent.getAction().compareTo(AppService.APPSERVICE_STARTED_ACTION) == 0) {
                    ExperienceManager.getInstance().addListener(experienceListener);
                    if (getIntent().getAction() != null && getIntent().getAction().compareTo(AppService.PROCESS_ACTION) == 0) {
                        processActionFromIntent(getIntent());
                    }
                }
            }
        }
    }

    // --------------------
    // SETTINGS MENU
    // --------------------

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void processActionFromIntent(Intent intent) {
        if (intent != null && intent.getAction() != null) {
            if (intent.getAction().compareTo(AppService.PROCESS_ACTION) == 0) {
                Action action = intent.getParcelableExtra(AppService.EXPERIENCE_ACTION);
                if (action != null) {
                    ExperienceManager.getInstance().receivedExperienceAction(action);
                }
            }
        }
    }

    private void handleCloseFragment() {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(GIMBAL_POP_WINDOW);
        if (fragment != null) {
            getSupportFragmentManager().beginTransaction().remove(fragment).commitAllowingStateLoss();
        }
        if (fragmentContainerLayout.isShown()) {
            fragmentContainerLayout.setVisibility(View.GONE);
        }
    }

    private void handlePresentFragment(Fragment fragment) {
        handleCloseFragment();
        if (fragment != null) {
            if (!fragmentContainerLayout.isShown()) {
                fragmentContainerLayout.setVisibility(View.VISIBLE);
            }

            if (getSupportFragmentManager().findFragmentByTag(GIMBAL_POP_WINDOW) != null) {
                getSupportFragmentManager().beginTransaction().remove(fragment).commitAllowingStateLoss();
            }
            getSupportFragmentManager().beginTransaction().add(R.id.popup_window, fragment, GIMBAL_POP_WINDOW).addToBackStack(GIMBAL_POP_WINDOW).commitAllowingStateLoss();
        }
    }
}
