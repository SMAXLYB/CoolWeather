package cn.smaxlyb.coolweather.gson;

import com.google.gson.annotations.SerializedName;

import javax.security.auth.callback.Callback;

/**
 * @author smaxlyb
 * @date 2020/3/30 14:46
 * website: https://smaxlyb.cn
 */
public class Basic {
    @SerializedName("city")
    public String cityName;

    @SerializedName("id")
    public String weatherId;

    public Update update;
    public class Update{
        @SerializedName("loc")
        public String updateTime;
    }
}
