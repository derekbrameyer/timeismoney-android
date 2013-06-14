package com.doomonafireball.timeismoney.android;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import android.content.SharedPreferences;
import android.text.TextUtils;

import java.math.BigDecimal;

@Singleton
public class Datastore {

    private static final String DEVICE_VERSION = "DeviceVersion";
    private static final String HOURLY_RATE_JSON = "HourlyRateJson";
    private static final String PEOPLE_COUNT_JSON = "PeopleCountJson";

    private Gson mGson;

    @Inject EncryptedSharedPreferences encryptedSharedPreferences;

    private Gson getGson() {
        if (mGson == null) {
            mGson = new Gson();
        }
        return mGson;
    }

    private SharedPreferences.Editor getEditor() {
        return encryptedSharedPreferences.edit();
    }

    private SharedPreferences getPrefs() {
        return encryptedSharedPreferences;
    }

    public int getVersion() {
        return getPrefs().getInt(DEVICE_VERSION, 0);
    }

    public void persistVersion(int version) {
        getEditor().putInt(DEVICE_VERSION, version).commit();
    }

    public BigDecimal getHourlyRate() {
        String hourlyRateJson = getPrefs().getString(HOURLY_RATE_JSON, "");
        if (TextUtils.isEmpty(hourlyRateJson)) {
            return new BigDecimal(20);
        } else {
            return getGson().fromJson(hourlyRateJson, BigDecimal.class);
        }
    }

    public void persistHourlyRate(BigDecimal hourlyRate) {
        getEditor().putString(HOURLY_RATE_JSON, getGson().toJson(hourlyRate)).commit();
    }

    public BigDecimal getPeopleCount() {
        String peopleCountJson = getPrefs().getString(PEOPLE_COUNT_JSON, "");
        if (TextUtils.isEmpty(peopleCountJson)) {
            return new BigDecimal(1);
        } else {
            return getGson().fromJson(peopleCountJson, BigDecimal.class);
        }
    }

    public void persistPeopleCount(BigDecimal peopleCount) {
        getEditor().putString(PEOPLE_COUNT_JSON, getGson().toJson(peopleCount)).commit();
    }
}

