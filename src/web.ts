import { WebPlugin } from '@capacitor/core';

import type { RewardedVideoPluginPlugin } from './definitions';

export class RewardedVideoPluginWeb extends WebPlugin implements RewardedVideoPluginPlugin {
  async echo(options: { value: string }): Promise<{ value: string }> {
    console.log('ECHO', options);
    return options;
  }
}
