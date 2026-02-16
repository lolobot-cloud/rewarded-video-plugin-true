// package com.douslitere.rewarded;

// import android.app.Activity;
// import android.util.Log;
// import java.util.Arrays;
// import java.util.List;
// import androidx.annotation.NonNull;

// import com.getcapacitor.Plugin;
// import com.getcapacitor.PluginCall;
// import com.getcapacitor.PluginMethod;
// import com.getcapacitor.annotation.CapacitorPlugin;
// import com.getcapacitor.JSObject;

// import com.ironsource.mediationsdk.IronSource;
// import com.unity3d.mediation.LevelPlay;
// import com.unity3d.mediation.LevelPlayInitRequest;
// import com.unity3d.mediation.LevelPlayInitListener;
// import com.unity3d.mediation.LevelPlayInitError;
// import com.unity3d.mediation.LevelPlayConfiguration;
// import com.unity3d.mediation.rewarded.LevelPlayReward;
// import com.unity3d.mediation.rewarded.LevelPlayRewardedAd;
// import com.unity3d.mediation.rewarded.LevelPlayRewardedAdListener;
// import com.unity3d.mediation.LevelPlayAdError;
// import com.unity3d.mediation.LevelPlayAdInfo;

// @CapacitorPlugin(name = "RewardedVideoPlugin")
// public class RewardedVideoPluginPlugin extends Plugin {

//     private static final String TAG = "RewardedVideoPlugin";
//     public static final String APP_KEY = "227b0b39d";
//     public static final String REWARDED_VIDEO_AD_UNIT_ID = "67konrqff1j08cgg";

//     private LevelPlayRewardedAd rewardedAd;
//     private LevelPlayRewardedAd rewardedAdGift1;
//     private LevelPlayRewardedAd rewardedAdGift2;

//     private PluginCall currentCall;

//     @PluginMethod
//     public void initialize(PluginCall call) {
//         Activity activity = getActivity();

//         // Mode test
//         IronSource.setMetaData("is_test_suite", "enable");

//         List<LevelPlay.AdFormat> legacyAdFormats = Arrays.asList(LevelPlay.AdFormat.REWARDED);

//         LevelPlayInitRequest initRequest = new LevelPlayInitRequest.Builder(APP_KEY)
//                 .withLegacyAdFormats(legacyAdFormats)
//                 .build();

//         LevelPlayInitListener initListener = new LevelPlayInitListener() {
//             @Override
//             public void onInitFailed(@NonNull LevelPlayInitError error) {
//                 Log.e(TAG, "Init failed: " + error.getErrorMessage());
//                 call.reject("Initialization failed", error.getErrorMessage());
//             }

//             @Override
//             public void onInitSuccess(LevelPlayConfiguration configuration) {
//                 Log.d(TAG, "Init success");

//                 // ✅ Lance la test suite ici (à FAIRE seulement en mode test !)
//                 // IronSource.Agent.launchTestSuite();

//                 createRewardedAd();
//                 call.resolve();
//             }
//         };

//         LevelPlay.init(activity, initRequest, initListener);
//     }

//     private void createRewardedAd() {
//         rewardedAd = new LevelPlayRewardedAd(REWARDED_VIDEO_AD_UNIT_ID);

//         rewardedAd.setListener(new LevelPlayRewardedAdListener() {
//             @Override
//             public void onAdLoaded(@NonNull LevelPlayAdInfo adInfo) {
//                 Log.d(TAG, "Ad loaded");
//                 notifyAdEvent("adLoaded", null, null);
//             }

//             @Override
//             public void onAdLoadFailed(@NonNull LevelPlayAdError error) {
//                 Log.e(TAG, "Ad load failed: " + error.getErrorMessage());
//                 notifyAdEvent("adLoadFailed", error.getErrorMessage(), null);
//             }

//             @Override
//             public void onAdDisplayed(@NonNull LevelPlayAdInfo adInfo) {
//                 Log.d(TAG, "Ad displayed");
//                 notifyAdEvent("adDisplayed", null, null);
//             }

//             @Override
//             public void onAdClosed(@NonNull LevelPlayAdInfo adInfo) {
//                 Log.d(TAG, "Ad closed");
//                 notifyAdEvent("adClosed", null, null);
//                 reloadNextAd();
//             }

//             @Override
//             public void onAdClicked(@NonNull LevelPlayAdInfo adInfo) {
//                 Log.d(TAG, "Ad clicked");
//                 notifyAdEvent("adClicked", null, null);
//             }

//             @Override
//             public void onAdDisplayFailed(@NonNull LevelPlayAdError error, @NonNull LevelPlayAdInfo adInfo) {
//                 Log.e(TAG, "Ad display failed: " + error.getErrorMessage());
//                 notifyAdEvent("adDisplayFailed", error.getErrorMessage(), null);

//                 if (currentCall != null) {
//                     currentCall.reject("Ad display failed", error.getErrorMessage());
//                     currentCall = null;
//                 }
//             }

//             @Override
//             public void onAdInfoChanged(@NonNull LevelPlayAdInfo adInfo) {
//                 Log.d(TAG, "Ad info changed");
//                 notifyAdEvent("adInfoChanged", null, null);
//             }

//             @Override
//             public void onAdRewarded(@NonNull LevelPlayReward reward, @NonNull LevelPlayAdInfo adInfo) {
//                 Log.d(TAG, "User rewarded: " + reward.getAmount() + " " + reward.getName());

//                 JSObject rewardData = new JSObject();
//                 rewardData.put("amount", reward.getAmount());
//                 rewardData.put("name", reward.getName());

//                 notifyAdEvent("adRewarded", null, rewardData);

//                 if (currentCall != null) {
//                     currentCall.resolve(rewardData);
//                     currentCall = null;
//                 }
//             }
//         });

//         rewardedAd.loadAd();
//     }

//     @PluginMethod
//     public void showRewarded(PluginCall call) {
//         this.currentCall = call;

//         if (rewardedAd != null && rewardedAd.isAdReady()) {
//             Log.d(TAG, "Showing ad...");
//             notifyAdEvent("adStarting", null, null);
//             rewardedAd.showAd(getActivity());
//         } else {
//             Log.w(TAG, "Ad not ready");
//             notifyAdEvent("adNotReady", "Ad not loaded or ready", null);
//             call.reject("Rewarded Ad not ready", "LOAD_FAILED");
//             this.currentCall = null;
//         }
//     }

//     @PluginMethod
//     public void isAdReady(PluginCall call) {
//         JSObject result = new JSObject();
//         boolean isReady = (rewardedAd != null && rewardedAd.isAdReady());
//         result.put("ready", isReady);
//         call.resolve(result);
//     }

//     @PluginMethod
//     public void loadAd(PluginCall call) {
//         if (rewardedAd != null) {
//             Log.d(TAG, "Loading new ad...");
//             rewardedAd.loadAd();
//             call.resolve();
//         } else {
//             call.reject("Ad instance not created");
//         }
//     }

//     private void notifyAdEvent(String eventType, String errorMessage, JSObject data) {
//         JSObject eventData = new JSObject();
//         eventData.put("eventType", eventType);
//         eventData.put("timestamp", System.currentTimeMillis());

//         if (errorMessage != null) {
//             eventData.put("error", errorMessage);
//         }

//         if (data != null) {
//             eventData.put("data", data);
//         }

//         notifyListeners("rewardedAdEvent", eventData);
//         Log.d(TAG, "Event sent: " + eventType);
//     }

//     private void reloadNextAd() {
//         if (rewardedAd != null) {
//             Log.d(TAG, "Preloading next ad...");

//             getActivity().runOnUiThread(new Runnable() {
//                 @Override
//                 public void run() {
//                     new android.os.Handler().postDelayed(new Runnable() {
//                         @Override
//                         public void run() {
//                             if (rewardedAd != null) {
//                                 rewardedAd.loadAd();
//                             }
//                         }
//                     }, 1000);
//                 }
//             });
//         }
//     }
// }

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

    // MAIN placement
    public static final String REWARDED_VIDEO_AD_UNIT_ID = "67konrqff1j08cgg";

    // placements gifts
    private static final String GIFT1_ID = "vrpt254zy1nhs6mg";
    private static final String GIFT2_ID = "7cp5iulhn86ddv1t";

    private LevelPlayRewardedAd rewardedAd;
    private LevelPlayRewardedAd rewardedAdGift1;
    private LevelPlayRewardedAd rewardedAdGift2;

    private PluginCall currentCall;

    // ================= INIT =================
    @PluginMethod
    public void initialize(PluginCall call) {
        Activity activity = getActivity();

        // MODE TEST (retire en production)
        IronSource.setMetaData("is_test_suite", "enable");

        List<LevelPlay.AdFormat> legacyAdFormats = Arrays.asList(LevelPlay.AdFormat.REWARDED);

        LevelPlayInitRequest initRequest = new LevelPlayInitRequest.Builder(APP_KEY)
                .withLegacyAdFormats(legacyAdFormats)
                .build();

        LevelPlayInitListener initListener = new LevelPlayInitListener() {

            @Override
            public void onInitFailed(@NonNull LevelPlayInitError error) {
                Log.e(TAG, "Init failed: " + error.getErrorMessage());
                call.reject("Init failed", error.getErrorMessage());
            }

            @Override
            public void onInitSuccess(LevelPlayConfiguration configuration) {
                Log.d(TAG, "Init success");

                createRewardedAds();
                call.resolve();
            }
        };

        LevelPlay.init(activity, initRequest, initListener);
    }

    // ================= CREATE ALL ADS =================
    private void createRewardedAds() {

        rewardedAd = createSingleAd(REWARDED_VIDEO_AD_UNIT_ID, "main");
        rewardedAdGift1 = createSingleAd(GIFT1_ID, "gift1");
        rewardedAdGift2 = createSingleAd(GIFT2_ID, "gift2");

        rewardedAd.loadAd();
        rewardedAdGift1.loadAd();
        rewardedAdGift2.loadAd();
    }

    // ================= FACTORY =================
    private LevelPlayRewardedAd createSingleAd(String placementId, String type) {

        LevelPlayRewardedAd ad = new LevelPlayRewardedAd(placementId);

        ad.setListener(new LevelPlayRewardedAdListener() {

            @Override
            public void onAdLoaded(@NonNull LevelPlayAdInfo adInfo) {
                Log.d(TAG, "Loaded: " + type);
                notifyAdEvent("adLoaded_" + type, null, null);
            }

            @Override
            public void onAdLoadFailed(@NonNull LevelPlayAdError error) {
                Log.e(TAG, "Load failed " + type + ": " + error.getErrorMessage());
                notifyAdEvent("adLoadFailed_" + type, error.getErrorMessage(), null);
            }

            @Override
            public void onAdDisplayed(@NonNull LevelPlayAdInfo adInfo) {
                Log.d(TAG, "Displayed: " + type);
                notifyAdEvent("adDisplayed_" + type, null, null);
            }

            @Override
            public void onAdClosed(@NonNull LevelPlayAdInfo adInfo) {
                Log.d(TAG, "Closed: " + type);
                notifyAdEvent("adClosed_" + type, null, null);
                reloadNextAd(type);
            }

            @Override
            public void onAdClicked(@NonNull LevelPlayAdInfo adInfo) {
                notifyAdEvent("adClicked_" + type, null, null);
            }

            @Override
            public void onAdDisplayFailed(@NonNull LevelPlayAdError error, @NonNull LevelPlayAdInfo adInfo) {
                Log.e(TAG, "Display failed " + type + ": " + error.getErrorMessage());

                if (currentCall != null) {
                    currentCall.reject("Display failed", error.getErrorMessage());
                    currentCall = null;
                }
            }

            @Override
            public void onAdInfoChanged(@NonNull LevelPlayAdInfo adInfo) {
            }

            @Override
            public void onAdRewarded(@NonNull LevelPlayReward reward, @NonNull LevelPlayAdInfo adInfo) {

                Log.d(TAG, "Reward user from " + type);

                JSObject rewardData = new JSObject();
                rewardData.put("amount", reward.getAmount());
                rewardData.put("name", reward.getName());
                rewardData.put("placement", type);

                notifyAdEvent("adRewarded_" + type, null, rewardData);

                if (currentCall != null) {
                    currentCall.resolve(rewardData);
                    currentCall = null;
                }
            }
        });

        return ad;
    }

    // ================= SHOW =================
    @PluginMethod
    public void showRewarded(PluginCall call) {

        this.currentCall = call;

        String type = call.getString("type", "main");
        LevelPlayRewardedAd selectedAd = null;

        if (type.equals("gift1")) {
            selectedAd = rewardedAdGift1;
        } else if (type.equals("gift2")) {
            selectedAd = rewardedAdGift2;
        } else {
            selectedAd = rewardedAd;
        }

        if (selectedAd != null && selectedAd.isAdReady()) {
            Log.d(TAG, "Showing ad: " + type);
            notifyAdEvent("adStarting_" + type, null, null);
            selectedAd.showAd(getActivity());
        } else {
            call.reject("Ad not ready: " + type);
            currentCall = null;
        }
    }

    // ================= READY CHECK =================
    @PluginMethod
    public void isAdReady(PluginCall call) {

        String type = call.getString("type", "main");
        boolean ready = false;

        if (type.equals("gift1") && rewardedAdGift1 != null) {
            ready = rewardedAdGift1.isAdReady();
        } else if (type.equals("gift2") && rewardedAdGift2 != null) {
            ready = rewardedAdGift2.isAdReady();
        } else if (rewardedAd != null) {
            ready = rewardedAd.isAdReady();
        }

        JSObject result = new JSObject();
        result.put("ready", ready);
        call.resolve(result);
    }

    // ================= FORCE LOAD =================
    @PluginMethod
    public void loadAd(PluginCall call) {

        if (rewardedAd != null)
            rewardedAd.loadAd();
        if (rewardedAdGift1 != null)
            rewardedAdGift1.loadAd();
        if (rewardedAdGift2 != null)
            rewardedAdGift2.loadAd();

        call.resolve();
    }

    // ================= EVENTS =================
    private void notifyAdEvent(String eventType, String errorMessage, JSObject data) {

        JSObject eventData = new JSObject();
        eventData.put("eventType", eventType);
        eventData.put("timestamp", System.currentTimeMillis());

        if (errorMessage != null)
            eventData.put("error", errorMessage);
        if (data != null)
            eventData.put("data", data);

        notifyListeners("rewardedAdEvent", eventData);
        Log.d(TAG, "Event: " + eventType);
    }

    // ================= RELOAD =================
    private void reloadNextAd(String type) {

        getActivity().runOnUiThread(() -> {
            new android.os.Handler().postDelayed(() -> {

                Log.d(TAG, "Reloading: " + type);

                if (type.equals("gift1") && rewardedAdGift1 != null) {
                    rewardedAdGift1.loadAd();
                } else if (type.equals("gift2") && rewardedAdGift2 != null) {
                    rewardedAdGift2.loadAd();
                } else if (rewardedAd != null) {
                    rewardedAd.loadAd();
                }

            }, 1500);
        });
    }
}
