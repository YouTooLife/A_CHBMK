package net.youtoolife.chbmk;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.util.Base64;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

/**
 * Created by youtoolife on 4/7/18.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {


    public static final int NOTIFY_ID = 101998;
    public static final String NOTIFICATION_CHANNEL_ID = "101998";

    public static NotificationManager notificationManager;

    public static final  String TOKEN_BROADCAST = "fcmTB";

    public static int N = 0;

    public MyFirebaseMessagingService() {
        super();

        Log.d("!MyFCMService", "Catch");


    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        //super.onMessageReceived(remoteMessage);


        Log.d("__notification: ", "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        String preBody = "Новое уведомление!";
        String preTitle = "SMS-INFO";

        if (remoteMessage.getData().size() > 0) {

            Log.d("__not!ficationData: ", "Message data payload: " + remoteMessage.getData());
            try {
                JSONObject obj0 = new JSONObject(remoteMessage.getData());
                preBody = obj0.getString("body");
                preTitle = obj0.getString("title");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        String response = preBody;

        if (remoteMessage.getNotification() != null) {
            response = remoteMessage.getNotification().getBody();
            preTitle = remoteMessage.getNotification().getTitle();
        }
            Context context = getApplicationContext();

            String body = "Новое уведомление!";
            String title = preTitle;

            Log.d("__notificationDataBody:", "Message Notification Body: " + response);

            String login = SharedPrefManager.getInstance(context).getLogin();
            if (login == null)
                return;
            try {
                byte[] dec = Base64.decode(response.getBytes("UTF-8"), Base64.DEFAULT);
                String sdec = new String(dec, "UTF-8");
                Log.d("SDEC",  sdec);

                JSONObject obj = new JSONObject(sdec);
                JSONArray arr = obj.getJSONArray(login);
                if (arr.length() > 0) {
                    body = arr.getString(arr.length() - 1);
                    N += arr.length();
                }

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            Log.d("__notificationDataBody:", "Message Notification Body: " + body);


            /*
            boolean myActivityActive = false;
            ActivityManager am = (ActivityManager) getApplicationContext() .getSystemService(Context.ACTIVITY_SERVICE);

            List<ActivityManager.RunningTaskInfo> alltasks = am.getRunningTasks(1);
            for (ActivityManager.RunningTaskInfo task : alltasks) {
                Log.d("NotTaskU", task.topActivity.getClassName());
                if(task.topActivity.getClassName().equals(ContActivity.class.getClass().getSimpleName())) {
                        Log.d("task", "true");
                    myActivityActive = true;
                    break;
                }

            }*/

                notificationManager =
                        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

                Intent notificationIntent = new Intent(context, ContActivity.class);
                notificationIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                PendingIntent contentIntent = PendingIntent.getActivity(context,
                        0, notificationIntent,
                        PendingIntent.FLAG_CANCEL_CURRENT);

                // до версии Android 8.0 API 26
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    //String CHANNEL_ID = "my_channel_01";
                    CharSequence name = "CHBMK";
                    //String Description = "This is my channel";
                    int importance = NotificationManager.IMPORTANCE_HIGH;
                    NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance);
                    //notificationChannel.enableLights(true);
                    //notificationChannel.setLightColor(Color.RED);
                    notificationChannel.enableVibration(true);
                    notificationChannel.setVibrationPattern(new long[]{1000, 100, 500});
                    notificationManager.createNotificationChannel(notificationChannel);
                }

                    NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID);
                    long[] vibrate = new long[]{1000, 100, 500};
                    builder.setContentIntent(contentIntent)
                            .setPriority(NotificationCompat.PRIORITY_HIGH)
                            // обязательные настройки
                            //.setFullScreenIntent(contentIntent, true)
                            .setChannelId(NOTIFICATION_CHANNEL_ID)
                            .setSmallIcon(R.mipmap.ic_launcher_round)
                            .setContentTitle(title)
                            .setContentText(body)
                            ///.setStyle(new NotificationCompat.BigTextStyle().bigText("itsBIG:\n"+remoteMessage.getNotification().getBody()))
                            .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher_round))
                            .setTicker(title)
                            .setWhen(System.currentTimeMillis())
                            .setVibrate(vibrate)
                            .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE)
                            .setVisibility(Notification.VISIBILITY_PUBLIC)
                            .addAction(R.mipmap.ic_launcher, "Открыть", contentIntent)
                            .setCategory(NotificationCompat.CATEGORY_STATUS)
                            .setAutoCancel(true);

                    NotificationCompat.InboxStyle style = new NotificationCompat.InboxStyle();

                    for (String s : body.split("\\n")) {
                        style.addLine(s);
                    }
                    style.setSummaryText("сообщений: " + String.valueOf(N));
                    builder.setStyle(style);

                    // NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
                    notificationManager.notify(NOTIFY_ID, builder.build());

            getApplicationContext().sendBroadcast(new Intent(TOKEN_BROADCAST));
        }
}


/*NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(context)
                            .setSmallIcon(R.mipmap.ic_launcher_round)
                            .setContentTitle(remoteMessage.getNotification().getTitle())
                            .setContentText(remoteMessage.getNotification().getBody());

            Intent resultIntent = new Intent(context, ResultActivity.class);

            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            stackBuilder.addParentStack(ResultActivity.class);
            stackBuilder.addNextIntent(resultIntent);

            PendingIntent resultPendingIntent =
                    stackBuilder.getPendingIntent(
                            0,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );
            mBuilder.setContentIntent(resultPendingIntent);

            NotificationManager mNotificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            mNotificationManager.notify(0,mBuilder.build());*/
