package io.github.haykam821.colorfulsubtitles;

import net.fabricmc.api.ClientModInitializer;

public class ColorfulSubtitlesClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ColorfulSubtitles.getConfig();
	}
}
