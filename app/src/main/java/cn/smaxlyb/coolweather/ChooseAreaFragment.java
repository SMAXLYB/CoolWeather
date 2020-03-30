package cn.smaxlyb.coolweather;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.CellIdentityTdscdma;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import org.jetbrains.annotations.NotNull;
import org.litepal.LitePal;
import org.litepal.tablemanager.typechange.BooleanOrm;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import cn.smaxlyb.coolweather.databinding.ChooseAreaBinding;
import cn.smaxlyb.coolweather.db.City;
import cn.smaxlyb.coolweather.db.County;
import cn.smaxlyb.coolweather.db.Province;
import cn.smaxlyb.coolweather.util.DialogUtil;
import cn.smaxlyb.coolweather.util.HttpUtil;
import cn.smaxlyb.coolweather.util.LogUtil;
import cn.smaxlyb.coolweather.util.Utility;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * @author smaxlyb
 * @date 2020/3/29 12:12
 * website: https://smaxlyb.cn
 */
public class ChooseAreaFragment extends Fragment {
    // 数据级别
    private static final int LEVEL_PROVINCE = 0;
    private static final int LEVEL_CITY = 1;
    private static final int LEVEL_COUNTY = 2;
    // 视图绑定
    private ChooseAreaBinding areaBinding;
    // 数据适配器
    private ArrayAdapter<String> adapter;
    // 适配器数据
    private List<String> dataList = new ArrayList<>();
    // 省市县级数据列表
    private List<Province> provinceList;
    private List<City> cityList;
    private List<County> countyList;
    // 当前选中级别
    private int currentLevel;
    // 当前已选中的省市县
    private Province selectedProvince;
    private City selectedCity;
    private County selectedCounty;

    // 加载布局
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        areaBinding = ChooseAreaBinding.inflate(inflater, container, false);
        //创建列表，默认没有任何数据，需要查询
        adapter = new ArrayAdapter(getContext(), android.R.layout.simple_list_item_checked, dataList);
        areaBinding.listView.setAdapter(adapter);
        areaBinding.listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        return areaBinding.getRoot();
    }

    //设置监听
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // 默认显示省级数据
        queryProvinces();
        // 列表单击事件
        areaBinding.listView.setOnItemClickListener((parent, view, position, id) -> {
            if (currentLevel == LEVEL_PROVINCE) {
                // 标记选择的省份
                selectedProvince = provinceList.get(position);
                // 进行下一级查询
                queryCities();
            } else if (currentLevel == LEVEL_CITY) {
                selectedCity = cityList.get(position);
                queryCounties();
            } else if (currentLevel == LEVEL_COUNTY) {
                String weatherId = countyList.get(position).getWeatherId();
                Intent intent = new Intent(getActivity(), WeatherActivity.class);
                intent.putExtra("weather_id", weatherId);
                startActivity(intent);
                getActivity().finish();
            }
        });
        // 按钮单击事件
        areaBinding.backButton.setOnClickListener(view -> {
            //判断当前fragment处于哪个级，返回的时候就显示上一个级别
            if (currentLevel == LEVEL_COUNTY) {
                queryCities();
            } else if (currentLevel == LEVEL_CITY) {
                queryProvinces();

            }
        });
    }

    //查询省级数据
    private void queryProvinces() {
        // 设置标题
        areaBinding.titleText.setText("中国");
        // 在省级页面没有返回按钮
        areaBinding.backButton.setVisibility(View.GONE);
        // 从本地数据库查询所有数据
        provinceList = LitePal.findAll(Province.class);
        // 判断本地是否有数据
        if (provinceList.size() > 0) {
            // 如果有数据，使用本地数据
            dataList.clear();
            for (Province province : provinceList) {
                dataList.add(province.getProvinceName());
            }
            // 通知adapter数据发生改变
            adapter.notifyDataSetChanged();
            areaBinding.listView.setSelection(0);
            currentLevel = LEVEL_PROVINCE;
        } else {
            // 如果没有数据，网络查询
            String Address = "http://guolin.tech/api/china";
            queryFrom(Address, "province");
        }
    }

    //查询市级数据
    private void queryCities() {
        cityList = LitePal.where("provinceid = ?", String.valueOf(selectedProvince.getId())).find(City.class);
        // 当本地有数据时，清除现有列表
        if (cityList.size() > 0) {
            areaBinding.titleText.setText(selectedProvince.getProvinceName());
            areaBinding.backButton.setVisibility(View.VISIBLE);
            dataList.clear();
            for (City city : cityList) {
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            areaBinding.listView.setSelection(0);
            currentLevel = LEVEL_CITY;
        } else {
            String address = "http://guolin.tech/api/china/" + selectedProvince.getProvinceCode();
            queryFrom(address, "city");
        }
    }

    //查询县级数据
    private void queryCounties() {
        countyList = LitePal.where("cityid = ? ", String.valueOf(selectedCity.getId())).find(County.class);
        if (countyList.size() > 0) {
            areaBinding.titleText.setText(selectedCity.getCityName());
            dataList.clear();
            for (County county : countyList) {
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            areaBinding.listView.setSelection(0);
            currentLevel = LEVEL_COUNTY;
        } else {
            String address = "http://guolin.tech/api/china/" + selectedProvince.getProvinceCode() + "/" + selectedCity.getCityCode();
            queryFrom(address, "county");
        }
    }

    /**
     * @param address 请求地址
     * @param type    请求数据类型，省级，市级，县级
     */
    private void queryFrom(String address, final String type) {
        LogUtil.d("ChooseAreaFragment", "请求地址：" + address);
        // 弹出加载框
        DialogUtil.showDialog(getActivity());
        // 请求数据，并解析
        HttpUtil.sendRequestWithOkHttp(address, new Callback() {
            //请求成功，开始解析
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                LogUtil.d("ChooseAreaFragment", "进入解析");
                String responseText = response.body().string();
                Boolean result = false;
                if ("province".equals(type)) {
                    result = Utility.handleProvinceResponse(responseText);
                } else if ("city".equals(type)) {
                    result = Utility.handleCityResponse(responseText, selectedProvince.getId());
                } else if ("county".equals(type)) {
                    result = Utility.handleCountyResponse(responseText, selectedCity.getId());
                    LogUtil.d("ChooseAreaFragment", "解析结果：" + result);
                }
                // 如果解析成功,把子线程转到主线程
                if (result) {
                    getActivity().runOnUiThread(() -> {
                        // 取消进度框
                        DialogUtil.dismissDialog();
                        // 重新进入本地数据库查询数据
                        switch (type) {
                            case "province":
                                queryProvinces();
                                break;
                            case "city":
                                queryCities();
                                break;
                            case "county":
                                queryCounties();
                                break;
                        }
                    });
                }
            }

            // 请求失败
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                LogUtil.e("ChooseAreaFragment", "请求失败，异常原因：" + e.toString());
                // 回到主线程，取消进度框，显示警告框
                getActivity().runOnUiThread(() -> {
                    DialogUtil.dismissDialog();
                    AlertDialog dialog = new AlertDialog.Builder(getActivity())
                            .setTitle("加载失败")
                            .setMessage("请检查你的网络是否通畅！")
                            .setCancelable(false)
                            .setPositiveButton("确定", (mDialog, which) -> {
                                mDialog.dismiss();
                            })
                            .show();
                });
            }
        });
    }

}
