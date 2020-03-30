package cn.smaxlyb.coolweather;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import cn.smaxlyb.coolweather.databinding.ActivityWeatherBinding;
import cn.smaxlyb.coolweather.gson.Forecast;
import cn.smaxlyb.coolweather.gson.Weather;
import cn.smaxlyb.coolweather.util.HttpUtil;
import cn.smaxlyb.coolweather.util.LogUtil;
import cn.smaxlyb.coolweather.util.Utility;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {
    private ActivityWeatherBinding weatherBinding;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        weatherBinding = ActivityWeatherBinding.inflate(LayoutInflater.from(this));
        // 活动布局显示在状态栏上面，不支持5.0以下
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        getWindow().setStatusBarColor(Color.TRANSPARENT);

        setContentView(weatherBinding.getRoot());
        // 查询本地是否有天气数据
        prefs = this.getSharedPreferences("weatherData", MODE_PRIVATE);
        String weatherString = prefs.getString("weather", null);
        String bingPic = prefs.getString("bing_pic", null);
        if (bingPic != null) {
            Glide.with(this).load(bingPic).into(weatherBinding.bingPicImg);
        } else {
            loadBingPic();
        }
        if (weatherString != null) {
            // 有缓存数据，直接展示结果
            Weather weather = Utility.handleWeatherResponse(weatherString);
            showWeatherInfo(weather);
        } else {
            // 无缓存数据，把当前页面设为不可见
            weatherBinding.weatherLayout.setVisibility(View.INVISIBLE);
            String weatherId = getIntent().getStringExtra("weather_id");
            requestWeather(weatherId);
        }
    }

    public void requestWeather(final String weatherId) {
        String weatherUrl = "http://guolin.tech/api/weather?cityid=" + weatherId + "&key=1";
        HttpUtil.sendRequestWithOkHttp(weatherUrl, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                LogUtil.e("WeatherActivity", "请求失败，错误原因：" + e.toString());
                runOnUiThread(() -> {
                    Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                // 请求响应结果
                final String responseText = response.body().string();
                // 解析响应
                final Weather weather = Utility.handleWeatherResponse(responseText);
                // 切换回主线程
                runOnUiThread(() -> {
                    if (weather != null && "ok".equals(weather.status)) {
                        // 如果解析有结果且响应成功，将响应结果保存本地,留给下次解析
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("weather", responseText);
                        editor.apply();
                        showWeatherInfo(weather);
                    } else {
                        // 如果解析没有结果或者响应失败
                        Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    // 处理并展示天气信息
    private void showWeatherInfo(Weather weather) {
        // 设置顶部标题
        weatherBinding.title.titleCity.setText(weather.basic.cityName);
        // 设置顶部更新时间，不显示年月日
        weatherBinding.title.titleUpdateTime.setText("更新于" + weather.basic.update.updateTime.split(" ")[1]);
        // 设置当前温度
        weatherBinding.now.degreeText.setText(weather.now.temperature + "℃");
        // 设置当前天气情况
        weatherBinding.now.weatherInfoText.setText(weather.now.more.info);
        // 设置最近天气情况
        // 先清除之前数据
        weatherBinding.forecast.forecastLayout.removeAllViews();
        View view = LayoutInflater.from(this).inflate(R.layout.forecast_item, weatherBinding.forecast.forecastLayout, false);
        TextView dateText = view.findViewById(R.id.date_text);
        TextView infoText = view.findViewById(R.id.info_text);
        TextView maxText = view.findViewById(R.id.max_text);
        TextView minText = view.findViewById(R.id.min_text);
        dateText.setText("日期");
        infoText.setText("天气");
        maxText.setText("最高温");
        minText.setText("最低温");
        weatherBinding.forecast.forecastLayout.addView(view);

        for (Forecast forecast : weather.forecastList) {
            // 动态添加view
            view = LayoutInflater.from(this).inflate(R.layout.forecast_item, weatherBinding.forecast.forecastLayout, false);
            dateText = view.findViewById(R.id.date_text);
            infoText = view.findViewById(R.id.info_text);
            maxText = view.findViewById(R.id.max_text);
            minText = view.findViewById(R.id.min_text);
            dateText.setText(forecast.date);
            infoText.setText(forecast.more.info);
            maxText.setText(forecast.temperature.max);
            minText.setText(forecast.temperature.min);
            weatherBinding.forecast.forecastLayout.addView(view);
        }
        // 设置生活指数
        if (weather.aqi != null) {
            weatherBinding.aqi.aqiText.setText(weather.aqi.city.aqi);
            weatherBinding.aqi.pm25Text.setText(weather.aqi.city.pm25);
        }
        // 设置建议
        weatherBinding.suggestion.comfortText.setText("舒适度：" + weather.suggestion.comfort.info);
        weatherBinding.suggestion.carWashText.setText("洗车指数：" + weather.suggestion.carWash.info);
        weatherBinding.suggestion.sportText.setText("运动建议：" + weather.suggestion.sport.info);

        weatherBinding.weatherLayout.setVisibility(View.VISIBLE);
    }

    private void loadBingPic() {
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendRequestWithOkHttp(requestBingPic, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Toast.makeText(WeatherActivity.this, "背景图片加载失败，请检查是否连接网络！", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                final String bingPic = response.body().string();
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("bing_pic", bingPic);
                editor.apply();
                runOnUiThread(() -> {
                    Glide.with(WeatherActivity.this).load(bingPic).into(weatherBinding.bingPicImg);
                });
            }
        });
    }
}
