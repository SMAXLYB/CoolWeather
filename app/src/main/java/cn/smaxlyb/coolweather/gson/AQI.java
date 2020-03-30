package cn.smaxlyb.coolweather.gson;

/**
 * @author smaxlyb
 * @date 2020/3/30 14:52
 * website: https://smaxlyb.cn
 */
public class AQI {

    public AQICity city;

    public class AQICity{
        public String aqi;
        public String pm25;
    }
}
