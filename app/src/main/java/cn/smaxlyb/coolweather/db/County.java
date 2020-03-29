package cn.smaxlyb.coolweather.db;

import org.jetbrains.annotations.NotNull;
import org.litepal.crud.LitePalSupport;

import java.util.Objects;

/**
 * @author smaxlyb
 * @date 2020/3/29 9:09
 * website: https://smaxlyb.cn
 */
public class County extends LitePalSupport {
    private int id;
    private String countyName;
    private String weatherId;
    private int cityId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCountyName() {
        return countyName;
    }

    public void setCountyName(String countryName) {
        this.countyName = countryName;
    }

    public String getWeatherId() {
        return weatherId;
    }

    public void setWeatherId(String weatherId) {
        this.weatherId = weatherId;
    }

    public int getCityId() {
        return cityId;
    }

    public void setCityId(int cityId) {
        this.cityId = cityId;
    }

    @NotNull
    @Override
    public String toString() {
        return "County{" +
                "id=" + id +
                ", countryName='" + countyName + '\'' +
                ", weatherId='" + weatherId + '\'' +
                ", cityId=" + cityId +
                '}';
    }
}
