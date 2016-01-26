package com.moxun.generator;

import com.intellij.notification.*;

/**
 * logger
 * Created by moxun on 15/11/27.
 */
public class Logger {
    public static void init() {
        NotificationsConfiguration.getNotificationsConfiguration().register("JSON Model Generator", NotificationDisplayType.NONE);
    }

    public static void info(String text) {
        Notifications.Bus.notify(
                new Notification("JSON Model Generator","JSON Model Generator [INFO]",text, NotificationType.INFORMATION));
    }

    public static void warn(String text) {
        Notifications.Bus.notify(
                new Notification("JSON Model Generator","JSON Model Generator [WARNING]",text, NotificationType.WARNING));
    }

    public static void error(String text) {
        Notifications.Bus.notify(
                new Notification("JSON Model Generator","JSON Model Generator [ERROR]",text, NotificationType.ERROR));
    }
}
