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

import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.widget.Toast;

import com.gimbal.android.Communication;
import com.gimbal.android.CommunicationListener;
import com.gimbal.android.CommunicationManager;
import com.gimbal.android.Gimbal;
import com.gimbal.android.PlaceEventListener;
import com.gimbal.android.PlaceManager;
import com.gimbal.android.Push;
import com.gimbal.android.Visit;
import com.gimbal.android.sample.GimbalEvent.TYPE;
import com.gimbal.experience.android.Action;
import com.gimbal.experience.android.ExperienceListener;
import com.gimbal.experience.android.ExperienceManager;

public class AppService extends Service {
    public static final String APPSERVICE_STARTED_ACTION = "appservice_started";
    public static final String EXPERIENCE_ACTION = "experience_action";
    public static final String PROCESS_ACTION = "process_action";


    private static final int MAX_NUM_EVENTS = 100;
    private LinkedList<GimbalEvent> events;
    private PlaceEventListener placeEventListener;
    private CommunicationListener communicationListener;
    private ExperienceListener experienceListener;

    @Override
    public void onCreate() {
        events = new LinkedList<GimbalEvent>(GimbalDAO.getEvents(getApplicationContext()));

        Gimbal.setApiKey(this.getApplication(), "82ab4eec-7d97-4985-bf58-f9f7881f48d8");

        // Setup PlaceEventListener
        placeEventListener = new PlaceEventListener() {

            @Override
            public void onVisitStart(Visit visit) {
                addEvent(new GimbalEvent(TYPE.PLACE_ENTER, visit.getPlace().getName(), new Date(visit.getArrivalTimeInMillis())));
            }

            @Override
            public void onVisitStartWithDelay(Visit visit, int delayTimeInSeconds) {
                if (delayTimeInSeconds > 0) {
                    addEvent(new GimbalEvent(TYPE.PLACE_ENTER_DELAY, visit.getPlace().getName(), new Date(System.currentTimeMillis())));
                }
            }

            @Override
            public void onVisitEnd(Visit visit) {
                addEvent(new GimbalEvent(TYPE.PLACE_EXIT, visit.getPlace().getName(), new Date(visit.getDepartureTimeInMillis())));
            }
        };
        PlaceManager.getInstance().addListener(placeEventListener);

        // Setup CommunicationListener
        communicationListener = new CommunicationListener() {

            PendingIntent pendingIntent;

            @Override
            public Notification.Builder prepareCommunicationForDisplay(Communication communication, Visit visit, int notificationId) {
                addEvent(new GimbalEvent(TYPE.COMMUNICATION_PRESENTED, communication.getTitle() + ":  CONTENT_DELIVERED", new Date()));
                Intent intent = new Intent(AppService.this, BookingActivity.class);
//                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                pendingIntent = PendingIntent.getActivity(AppService.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                Notification.Builder builder = new Notification.Builder(AppService.this)
                        .setSmallIcon(R.drawable.app_icon)
                        .setContentTitle("Car Parking!!!")
                        .setContentText("Do you want to book a slot now??")
                        .setContentIntent(pendingIntent);
                // If you want a custom notification create and return it here
                return builder;
            }

            @Override
            public Notification.Builder prepareCommunicationForDisplay(Communication communication, Push push, int notificationId) {
                addEvent(new GimbalEvent(TYPE.COMMUNICATION_INSTANT_PUSH, communication.getTitle() + ":  CONTENT_DELIVERED", new Date()));
                Intent intent = new Intent(AppService.this, BookingActivity.class);
//                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                pendingIntent = PendingIntent.getActivity(AppService.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                Notification.Builder builder = new Notification.Builder(AppService.this)
                        .setSmallIcon(R.drawable.app_icon)
                        .setContentTitle("Car Parking!!!")
                        .setContentText("Do you want to book a slot now??")
                        .setContentIntent(pendingIntent);

                // If you want a custom notification create and return it here
                return builder;
            }

            @Override
            public void onNotificationClicked(List<Communication> communications) {
                for (Communication communication : communications) {
                    if(communication != null) {
                        addEvent(new GimbalEvent(TYPE.NOTIFICATION_CLICKED, communication.getTitle() + ": CONTENT_CLICKED", new Date()));
                    }
                }
                Intent intent = new Intent(getApplicationContext(), BookingActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                Toast.makeText(getApplicationContext(),"Hello done", Toast.LENGTH_LONG).show();
            }
        };
        CommunicationManager.getInstance().addListener(communicationListener);

        experienceListener = new ExperienceListener() {
            @Override
            public Collection<Action> filterActions(Collection<Action> actions) {
                return super.filterActions(actions);
            }

            @Override
            public void presentFragmentForAction(Fragment fragment, Action action) {
                String message = "Experience";

                if (action.getNotificationMessage() != null) {
                    message = action.getNotificationMessage();
                }
                addEvent(new GimbalEvent(TYPE.EXPERIENCE_FRAGMENT_PRESENTED, message, new Date()));
            }

            @Override
            public Notification.Builder prepareActionNotificationForDisplay(Action action, int notificationId) {
                return null;
            }

            @Override
            public Action presentNotificationForAction(Action action) {
                return super.presentNotificationForAction(action);
            }

            @Override
            public void onNotificationClicked(Action action) {
                bringAppToForeground(action);
            }
        };

        ExperienceManager.getInstance().addListener(experienceListener);
    }

    private void addEvent(GimbalEvent event) {
        while (events.size() >= MAX_NUM_EVENTS) {
            events.removeLast();
        }
        events.add(0, event);
        GimbalDAO.setEvents(getApplicationContext(), events);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        notifyServiceStarted();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        PlaceManager.getInstance().removeListener(placeEventListener);
        CommunicationManager.getInstance().removeListener(communicationListener);
        ExperienceManager.getInstance().removeListener(experienceListener);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void notifyServiceStarted() {
        Intent intent = new Intent(APPSERVICE_STARTED_ACTION);
        sendBroadcast(intent);
    }

    private void bringAppToForeground(Action action) {
        Intent intent = new Intent(this, AppActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.setAction(PROCESS_ACTION);
        intent.putExtra(EXPERIENCE_ACTION, action);
        startActivity(intent);
    }
}
