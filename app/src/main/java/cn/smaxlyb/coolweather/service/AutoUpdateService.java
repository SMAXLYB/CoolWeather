package cn.smaxlyb.coolweather.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;

import androidx.core.app.AlarmManagerCompat;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import cn.smaxlyb.coolweather.gson.Weather;
import cn.smaxlyb.coolweather.util.HttpUtil;
import cn.smaxlyb.coolweather.util.LogUtil;
import cn.smaxlyb.coolweather.util.Utility;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class AutoUpdateService extends Service {

    // 不需要和活动绑定
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogUtil.d("AutoUpdateService", "服务已启动");

        updateWeather();
        updateBingPic();
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        int anHour = 6 * 60 * 60 * 1000;
        long triggerAtTime = SystemClock.elapsedRealtime() + anHour;
        Intent i = new Intent(this, AutoUpdateService.class);
        PendingIntent pi = PendingIntent.getService(this, 0, i, 0);
        manager.cancel(pi);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);
        return super.onStartCommand(intent, flags, startId);
    }

    private void updateWeather() {
        // 查看本地是否有数据
        SharedPreferences prefs = getSharedPreferences("weatherData", MODE_PRIVATE);
        String weatherString = prefs.getString("weather", null);
        //如果本地有数据才进行更新
        if (weatherString != null) {
            // 利用本地的weatherId进行请求
            Weather weather = Utility.handleWeatherResponse(weatherString);
            String weatherUrl = "http://guolin.tech/api/weather?cityid=" + weather.basic.weatherId + "&key=1";
            LogUtil.d("AutoUpdateService", weatherUrl);
            HttpUtil.sendRequestWithOkHttp(weatherUrl, new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    LogUtil.e("AutoUpdateService", "后台服务更新出错：" + e.toString());
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    // 对请求结果进行解析
                    String responseText = response.body().string();
                    Weather weather = Utility.handleWeatherResponse(responseText);
                    // 对解析结果进行存储
                    if (weather != null && "ok".equals(weather.status)) {
                        prefs.edit().putString("weather", responseText).apply();
                    }
                }
            });
        }
    }

    private void updateBingPic() {
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendRequestWithOkHttp(requestBingPic, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                LogUtil.e("AutoUpdateService", "后台服务更新图片出错：" + e.toString());
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String bingPic = response.body().string();
                SharedPreferences prefs = getSharedPreferences("weatherData", MODE_PRIVATE);
                prefs.edit().putString("bing_pic", bingPic).apply();
            }
        });
    }
}
