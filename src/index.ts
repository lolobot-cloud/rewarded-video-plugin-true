import { registerPlugin } from '@capacitor/core';

import type { RewardedVideoPluginPlugin } from './definitions';

const RewardedVideoPlugin = registerPlugin<RewardedVideoPluginPlugin>('RewardedVideoPlugin', {
  web: () => import('./web').then((m) => new m.RewardedVideoPluginWeb()),
});

export * from './definitions';
export { RewardedVideoPlugin };
