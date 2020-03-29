package cn.smaxlyb.coolweather.util;

import java.io.IOException;

import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * @author smaxlyb
 * @date 2020/3/29 9:28
 * website: https://smaxlyb.cn
 */
public class HttpUtil {
    /**
     * @param address   请求地址
     * @param callback  请求失败或者成功的回调接口
     */
    public static void sendRequestWithOkHttp(String address, Callback callback){
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(address).build();
            client.newCall(request).enqueue(callback);
    }
}
