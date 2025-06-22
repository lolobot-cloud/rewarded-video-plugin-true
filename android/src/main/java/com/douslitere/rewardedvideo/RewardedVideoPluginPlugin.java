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
import com.ironsource.mediationsdk.sdk.LevelPlayRewardedVideoListener;

import java.util.Arrays;
import java.util.List;

@CapacitorPlugin(name = "RewardedVideoPlugin")
public class RewardedVideoPluginPlugin extends Plugin {

    @PluginMethod
    public void initialize(PluginCall call) {
        String appKey = call.getString("appKey");
        String userId = call.getString("userId", "user123");

        Activity activity = getActivity();

        // Important : config pour Meta Audience Network + test suite (facultatif)
        IronSource.setMetaData(activity, "Facebook_IS_CacheFlag", "IMAGE");
        IronSource.setMetaData("is_test_suite", "enable");

        // Étape 1 – Créer la liste des formats legacy
        List<LevelPlay.AdFormat> legacyAdFormats = Arrays.asList(LevelPlay.AdFormat.REWARDED);

        // Étape 2 – Définir le listener AVANT init
        IronSource.setLevelPlayRewardedVideoListener(new LevelPlayRewardedVideoListener() {
            @Override
            public void onAdAvailable(Placement placement) {
                Log.d("IronSource", "Rewarded Video disponible");
            }

            @Override
            public void onAdUnavailable() {
                Log.d("IronSource", "Rewarded Video non disponible");
            }

            @Override
            public void onAdOpened() {
                Log.d("IronSource", "Rewarded Video ouvert");
            }

            @Override
            public void onAdClosed() {
                Log.d("IronSource", "Rewarded Video fermé");
            }

            @Override
            public void onAdRewarded(Placement placement) {
                Log.d("IronSource", "Utilisateur récompensé : " + placement.getRewardName());
            }

            @Override
            public void onAdShowFailed(com.ironsource.mediationsdk.logger.IronSourceError error) {
                Log.e("IronSource", "Erreur lors de l'affichage : " + error.getErrorMessage());
            }

            @Override
            public void onAdClicked(Placement placement) {
                Log.d("IronSource", "Rewarded Video cliqué");
            }
        });

        // Étape 3 – Construire la requête d’initialisation
        LevelPlayInitRequest initRequest = new LevelPlayInitRequest.Builder(appKey)
                .withLegacyAdFormats(legacyAdFormats)
                .withUserId(userId)
                .build();

        // Étape 4 – Appeler l’init avec callback
        LevelPlay.init(activity.getApplicationContext(), initRequest, new LevelPlayInitListener() {
            @Override
            public void onInitFailed(@NonNull LevelPlayInitError error) {
                call.reject("Init failed: " + error.getMessage());
            }

            @Override
            public void onInitSuccess(LevelPlayConfiguration configuration) {
                // ✅ Lance le test suite pour vérifier l'intégration
                IronSource.launchTestSuite(activity.getApplicationContext());
                call.resolve(); // SDK prêt
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
