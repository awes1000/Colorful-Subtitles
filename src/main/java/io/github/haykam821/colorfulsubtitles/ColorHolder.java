package io.github.haykam821.colorfulsubtitles;

import io.github.haykam821.colorfulsubtitles.config.ColorfulSubtitlesConfig;
import io.github.haykam821.colorfulsubtitles.config.SubtitleColor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.ARGB;

@Environment(EnvType.CLIENT)
public interface ColorHolder {
	int getTextColor();
	void setTextColor(int color);

	int getBackgroundColor();
	void setBackgroundColor(int color);

	default void setColor(SoundInstance sound) {
		ColorfulSubtitlesConfig config = ColorfulSubtitles.getConfig();
		SubtitleColor color = config.getColorForCategory(sound.getSource());

		this.setTextColor(ARGB.opaque(color.getText().getValue()));

		if (color.getBackground().isPresent()) {
			this.setBackgroundColor(color.getBackground().get().getValue());
		} else {
			SubtitleColor defaultColor = config.getDefaultColor();
			this.setBackgroundColor(defaultColor.getBackground().map(TextColor::getValue).orElse(-1));
		}
	}
}
