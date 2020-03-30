package cn.smaxlyb.coolweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * @author smaxlyb
 * @date 2020/3/30 14:58
 * website: https://smaxlyb.cn
 */
public class Suggestion {
    @SerializedName("comf")
    public Comfort comfort;

    @SerializedName("cw")
    public CarWash carWash;

    @SerializedName("sport")
    public Sport sport;

    public class Comfort{
        @SerializedName("txt")
        public String info;
    }

    public class CarWash{
        @SerializedName("txt")
        public String info;
    }

    public class Sport{
        @SerializedName("txt")
        public String info;
    }
}
