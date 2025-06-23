package com.douslitere.rewarded;

import android.app.Activity;
import android.util.Log;
import java.util.Arrays;
import java.util.List;
import androidx.annotation.NonNull;

import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.JSObject;

import com.ironsource.mediationsdk.IronSource;
import com.unity3d.mediation.LevelPlay;
import com.unity3d.mediation.LevelPlayInitRequest;
import com.unity3d.mediation.LevelPlayInitListener;
import com.unity3d.mediation.LevelPlayInitError;
import com.unity3d.mediation.LevelPlayConfiguration;
import com.unity3d.mediation.rewarded.LevelPlayReward;
import com.unity3d.mediation.rewarded.LevelPlayRewardedAd;
import com.unity3d.mediation.rewarded.LevelPlayRewardedAdListener;
import com.unity3d.mediation.LevelPlayAdError;
import com.unity3d.mediation.LevelPlayAdInfo;

@CapacitorPlugin(name = "RewardedVideoPlugin")
public class RewardedVideoPluginPlugin extends Plugin {

    private static final String TAG = "RewardedVideoPlugin";
    public static final String APP_KEY = "227b0b39d";
    public static final String REWARDED_VIDEO_AD_UNIT_ID = "67konrqff1j08cgg";

    private LevelPlayRewardedAd rewardedAd;
    private PluginCall currentCall;

    @PluginMethod
    public void initialize(PluginCall call) {
        Activity activity = getActivity();

        // Mode test
        IronSource.setMetaData("is_test_suite", "enable");

        List<LevelPlay.AdFormat> legacyAdFormats = Arrays.asList(LevelPlay.AdFormat.REWARDED);

        LevelPlayInitRequest initRequest = new LevelPlayInitRequest.Builder(APP_KEY)
                .withLegacyAdFormats(legacyAdFormats)
                .build();

        LevelPlayInitListener initListener = new LevelPlayInitListener() {
            @Override
            public void onInitFailed(@NonNull LevelPlayInitError error) {
                Log.e(TAG, "Init failed: " + error.getErrorMessage());
                call.reject("Initialization failed", error.getErrorMessage());
            }

            @Override
            public void onInitSuccess(LevelPlayConfiguration configuration) {
                Log.d(TAG, "Init success");
                createRewardedAd();
                call.resolve();
            }
        };

        LevelPlay.init(activity, initRequest, initListener);
    }

    private void createRewardedAd() {
        rewardedAd = new LevelPlayRewardedAd(REWARDED_VIDEO_AD_UNIT_ID);

        rewardedAd.setListener(new LevelPlayRewardedAdListener() {
            @Override
            public void onAdLoaded(@NonNull LevelPlayAdInfo adInfo) {
                Log.d(TAG, "Ad loaded");
                notifyAdEvent("adLoaded", null, null);
            }

            @Override
            public void onAdLoadFailed(@NonNull LevelPlayAdError error) {
                Log.e(TAG, "Ad load failed: " + error.getErrorMessage());
                notifyAdEvent("adLoadFailed", error.getErrorMessage(), null);
            }

            @Override
            public void onAdDisplayed(@NonNull LevelPlayAdInfo adInfo) {
                Log.d(TAG, "Ad displayed");
                notifyAdEvent("adDisplayed", null, null);
            }

            @Override
            public void onAdClosed(@NonNull LevelPlayAdInfo adInfo) {
                Log.d(TAG, "Ad closed");
                notifyAdEvent("adClosed", null, null);
                reloadNextAd();
            }

            @Override
            public void onAdClicked(@NonNull LevelPlayAdInfo adInfo) {
                Log.d(TAG, "Ad clicked");
                notifyAdEvent("adClicked", null, null);
            }

            @Override
            public void onAdDisplayFailed(@NonNull LevelPlayAdError error, @NonNull LevelPlayAdInfo adInfo) {
                Log.e(TAG, "Ad display failed: " + error.getErrorMessage());
                notifyAdEvent("adDisplayFailed", error.getErrorMessage(), null);

                if (currentCall != null) {
                    currentCall.reject("Ad display failed", error.getErrorMessage());
                    currentCall = null;
                }
            }

            @Override
            public void onAdInfoChanged(@NonNull LevelPlayAdInfo adInfo) {
                Log.d(TAG, "Ad info changed");
                notifyAdEvent("adInfoChanged", null, null);
            }

            @Override
            public void onAdRewarded(@NonNull LevelPlayReward reward, @NonNull LevelPlayAdInfo adInfo) {
                Log.d(TAG, "User rewarded: " + reward.getAmount() + " " + reward.getName());

                JSObject rewardData = new JSObject();
                rewardData.put("amount", reward.getAmount());
                rewardData.put("name", reward.getName());

                notifyAdEvent("adRewarded", null, rewardData);

                if (currentCall != null) {
                    currentCall.resolve(rewardData);
                    currentCall = null;
                }
            }
        });

        rewardedAd.loadAd();
    }

    @PluginMethod
    public void showRewarded(PluginCall call) {
        this.currentCall = call;

        if (rewardedAd != null && rewardedAd.isAdReady()) {
            Log.d(TAG, "Showing ad...");
            notifyAdEvent("adStarting", null, null);
            rewardedAd.showAd(getActivity());
        } else {
            Log.w(TAG, "Ad not ready");
            notifyAdEvent("adNotReady", "Ad not loaded or ready", null);
            call.reject("Rewarded Ad not ready", "LOAD_FAILED");
            this.currentCall = null;
        }
    }

    @PluginMethod
    public void isAdReady(PluginCall call) {
        JSObject result = new JSObject();
        boolean isReady = (rewardedAd != null && rewardedAd.isAdReady());
        result.put("ready", isReady);
        call.resolve(result);
    }

    @PluginMethod
    public void loadAd(PluginCall call) {
        if (rewardedAd != null) {
            Log.d(TAG, "Loading new ad...");
            rewardedAd.loadAd();
            call.resolve();
        } else {
            call.reject("Ad instance not created");
        }
    }

    private void notifyAdEvent(String eventType, String errorMessage, JSObject data) {
        JSObject eventData = new JSObject();
        eventData.put("eventType", eventType);
        eventData.put("timestamp", System.currentTimeMillis());

        if (errorMessage != null) {
            eventData.put("error", errorMessage);
        }

        if (data != null) {
            eventData.put("data", data);
        }

        notifyListeners("rewardedAdEvent", eventData);
        Log.d(TAG, "Event sent: " + eventType);
    }

    private void reloadNextAd() {
        if (rewardedAd != null) {
            Log.d(TAG, "Preloading next ad...");

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    new android.os.Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (rewardedAd != null) {
                                rewardedAd.loadAd();
                            }
                        }
                    }, 1000);
                }
            });
        }
    }
}