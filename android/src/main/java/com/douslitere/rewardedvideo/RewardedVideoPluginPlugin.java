package com.douslitere.rewardedvideo;

import android.app.Activity;
import android.util.Log;

import androidx.annotation.NonNull;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

import com.ironsource.mediationsdk.LevelPlay;
import com.ironsource.mediationsdk.LevelPlayInitRequest;
import com.ironsource.mediationsdk.LevelPlayInitListener;
import com.ironsource.mediationsdk.LevelPlayConfiguration;
import com.ironsource.mediationsdk.LevelPlayInitError;
import com.ironsource.mediationsdk.IronSource;
import com.ironsource.mediationsdk.logger.IronLog;
import com.ironsource.mediationsdk.model.Placement;

import java.util.Arrays;

@CapacitorPlugin(name = "RewardedVideoPlugin")
public class RewardedVideoPluginPlugin extends Plugin {

    @PluginMethod
    public void initialize(PluginCall call) {
        String appKey = call.getString("appKey");
        String userId = call.getString("userId", "user123");

        Activity activity = getActivity();

        IronSource.setMetaData(activity, "Facebook_IS_CacheFlag", "IMAGE"); // Meta support
        IronSource.setMetaData("is_test_suite", "enable"); // facultatif

        LevelPlayInitRequest initRequest = new LevelPlayInitRequest.Builder(appKey)
                .withLegacyAdFormats(Arrays.asList(LevelPlay.AdFormat.REWARDED))
                .withUserId(userId)
                .build();

        LevelPlay.init(activity.getApplicationContext(), initRequest, new LevelPlayInitListener() {
            @Override
            public void onInitFailed(@NonNull LevelPlayInitError error) {
                call.reject("Init failed: " + error.getMessage());
            }

            @Override
            public void onInitSuccess(LevelPlayConfiguration configuration) {
                call.resolve();
            }
        });
    }

    @PluginMethod
    public void showRewarded(PluginCall call) {
        if (IronSource.isRewardedVideoAvailable()) {
            IronSource.showRewardedVideo();
            call.resolve();
        } else {
            call.reject("Rewarded video not available");
        }
    }
}
