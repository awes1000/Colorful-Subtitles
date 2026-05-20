package io.github.haykam821.colorfulsubtitles.config;

import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.ChatFormatting;
import net.minecraft.sounds.SoundSource;

public class ColorfulSubtitlesConfig {
	private static final Map<SoundSource, SubtitleColor> DEFAULT_COLORS = ImmutableMap.<SoundSource, SubtitleColor>builder()
		.put(SoundSource.MUSIC, SubtitleColor.ofText(ChatFormatting.DARK_PURPLE))
		.put(SoundSource.RECORDS, SubtitleColor.ofText(ChatFormatting.DARK_RED))
		.put(SoundSource.WEATHER, SubtitleColor.ofText(ChatFormatting.AQUA))
		.put(SoundSource.BLOCKS, SubtitleColor.ofText(ChatFormatting.GREEN))
		.put(SoundSource.HOSTILE, SubtitleColor.ofText(ChatFormatting.RED))
		.put(SoundSource.NEUTRAL, SubtitleColor.ofText(ChatFormatting.YELLOW))
		.put(SoundSource.PLAYERS, SubtitleColor.ofText(ChatFormatting.GOLD))
		.put(SoundSource.AMBIENT, SubtitleColor.ofText(ChatFormatting.GRAY))
		.put(SoundSource.VOICE, SubtitleColor.ofText(ChatFormatting.LIGHT_PURPLE))
		.build();

	public static final ColorfulSubtitlesConfig DEFAULT = new ColorfulSubtitlesConfig(DEFAULT_COLORS, SubtitleColor.DEFAULT);

	public static final Codec<ColorfulSubtitlesConfig> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
			ColorfulSubtitlesCodecs.SOUND_SOURCE_TO_SUBTITLE_COLOR.optionalFieldOf("colors", DEFAULT_COLORS).forGetter(config -> config.colors),
			SubtitleColor.CODEC.optionalFieldOf("default_color", SubtitleColor.DEFAULT).forGetter(config -> config.defaultColor)
		).apply(instance, ColorfulSubtitlesConfig::new);
	});

	private final Map<SoundSource, SubtitleColor> colors;
	private final SubtitleColor defaultColor;

	private ColorfulSubtitlesConfig(Map<SoundSource, SubtitleColor> colors, SubtitleColor defaultColor) {
		this.colors = colors;
		this.defaultColor = defaultColor;
	}

	public SubtitleColor getColorForCategory(SoundSource source) {
		SubtitleColor color = this.colors.get(source);
		return color == null ? this.defaultColor : color;
	}

	public SubtitleColor getDefaultColor() {
		return this.defaultColor;
	}

	public Map<SoundSource, SubtitleColor> getColors() {
		return this.colors;
	}

	public static Map<SoundSource, SubtitleColor> getDefaults() {
		return DEFAULT_COLORS;
	}

	public static ColorfulSubtitlesConfig create(Map<SoundSource, SubtitleColor> colors, SubtitleColor defaultColor) {
		return new ColorfulSubtitlesConfig(colors, defaultColor);
	}
}
