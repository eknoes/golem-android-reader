package de.eknoes.inofficialgolem;

import android.app.Application;
import android.content.Context;

public class MainApplication extends Application {
    private static Context context;

    public static Context getAppContext() {
        return MainApplication.context;
    }

    public void onCreate() {
        super.onCreate();
        MainApplication.context = getApplicationContext();
    }
}
