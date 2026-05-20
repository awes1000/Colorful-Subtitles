package io.github.haykam821.colorfulsubtitles.modmenu;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public class ColorfulSubtitlesModMenu implements ModMenuApi {
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return ColorfulSubtitlesConfigScreen::create;
	}
}
