export interface RewardedVideoPluginPlugin {
  echo(options: { value: string }): Promise<{ value: string }>;
}
