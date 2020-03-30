package cn.smaxlyb.coolweather.gson;

import android.widget.TextView;

import com.google.gson.annotations.SerializedName;

import java.nio.channels.ClosedSelectorException;

/**
 * @author smaxlyb
 * @date 2020/3/30 15:17
 * website: https://smaxlyb.cn
 */
public class Forecast {
    public String date;

    @SerializedName("cond")
    public More more;

    @SerializedName("tmp")
    public Temperature temperature;

    public class More {
        @SerializedName("txt_d")
        public String info;
    }

    public class Temperature {
        public String max;
        public String min;
    }
}
