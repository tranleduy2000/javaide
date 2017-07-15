/*
 *  Copyright (c) 2017 Tran Le Duy
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.duy.frontend.notify;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import com.duy.frontend.DLog;
import com.duy.frontend.R;
import com.duy.frontend.activities.ActivitySplashScreen;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

/**
 * Created by Duy on 20-May-17.
 */

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {
    private static final String TAG = "FirebaseMessagingServic";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        //handle FCM massages here
        DLog.d(TAG, "onMessageReceived: " + remoteMessage.getFrom());

        if (remoteMessage.getData().size() > 0) {

        }

        if (remoteMessage.getNotification() != null) {
            DLog.d(TAG, "onMessageReceived: " + remoteMessage.getNotification().getBody());
            RemoteMessage.Notification contentNotification = remoteMessage.getNotification();
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
            builder.setSmallIcon(R.mipmap.ic_launcher);
            builder.setContentText(contentNotification.getTitle());
            builder.setContentText(contentNotification.getBody());

            Map<String, String> data = remoteMessage.getData();
            String className = data.get("target_class");
            if (className != null) {
                try {
                    Intent resultIntent = new Intent(this, Class.forName(className));

                    TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
                    stackBuilder.addParentStack(ActivitySplashScreen.class);
                    stackBuilder.addNextIntent(resultIntent);

                    PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(
                            0, PendingIntent.FLAG_UPDATE_CURRENT
                    );
                    builder.setContentIntent(resultPendingIntent);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
            int mId = 1;
            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.notify(mId, builder.build());

        }

    }

    @Override
    public void onDeletedMessages() {
        super.onDeletedMessages();

    }
}
