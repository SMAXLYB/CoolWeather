package cn.smaxlyb.coolweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * @author smaxlyb
 * @date 2020/3/30 14:53
 * website: https://smaxlyb.cn
 */
public class Now {
    @SerializedName("tmp")
    public String temperature;

    @SerializedName("cond")
    public More more;

    public class More{
        @SerializedName("txt")
        public String info;
    }
}
