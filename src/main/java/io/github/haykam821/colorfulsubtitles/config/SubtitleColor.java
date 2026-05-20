package io.github.haykam821.colorfulsubtitles.config;

import java.util.Locale;
import java.util.Optional;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TextColor;

public class SubtitleColor {
	protected static final SubtitleColor DEFAULT = SubtitleColor.ofText(TextColor.fromRgb(0xFFFFFF));

	private static final Codec<Integer> ARGB_HEX_CODEC = Codec.STRING.comapFlatMap(
		SubtitleColor::parseArgbHex,
		SubtitleColor::formatArgbHex
	);

	private static final Codec<SubtitleColor> RECORD_CODEC = RecordCodecBuilder.create(instance -> instance.group(
			TextColor.CODEC.fieldOf("text").forGetter(color -> color.text),
			ARGB_HEX_CODEC.optionalFieldOf("background").forGetter(color -> color.background)
	).apply(instance, SubtitleColor::new));

	private static final Codec<SubtitleColor> SIMPLE_CODEC = TextColor.CODEC.xmap(SubtitleColor::ofText, color -> color.text);

	public static final Codec<SubtitleColor> CODEC = Codec.either(RECORD_CODEC, SIMPLE_CODEC).xmap(either -> {
		return either.map(left -> left, right -> right);
	}, color -> {
		return color.background.isEmpty() ? Either.right(color) : Either.left(color);
	});

	private final TextColor text;
	private final Optional<Integer> background;

	private SubtitleColor(TextColor text, Optional<Integer> background) {
		this.text = text;
		this.background = background;
	}

	public TextColor getText() {
		return this.text;
	}

	public Optional<Integer> getBackground() {
		return this.background;
	}

	@Override
	public String toString() {
		return "SubtitleColor{text=" + this.text + ", background=" + this.background.map(SubtitleColor::formatArgbHex).orElse("none") + "}";
	}

	public static SubtitleColor ofText(TextColor text) {
		return new SubtitleColor(text, Optional.empty());
	}

	public static SubtitleColor ofText(ChatFormatting formatting) {
		return SubtitleColor.ofText(TextColor.fromLegacyFormat(formatting));
	}

	public static SubtitleColor create(TextColor text, Optional<Integer> background) {
		return new SubtitleColor(text, background);
	}

	private static DataResult<Integer> parseArgbHex(String raw) {
		String value = raw.startsWith("#") ? raw.substring(1) : raw;
		if (value.length() != 6 && value.length() != 8) {
			return DataResult.error(() -> "Expected #RRGGBB or #AARRGGBB, got '" + raw + "'");
		}
		try {
			long parsed = Long.parseLong(value, 16);
			if (value.length() == 6) {
				return DataResult.success(0xFF000000 | (int) parsed);
			}
			return DataResult.success((int) parsed);
		} catch (NumberFormatException e) {
			return DataResult.error(() -> "Invalid hex color '" + raw + "'");
		}
	}

	public static String formatArgbHex(int argb) {
		int rgb = argb & 0xFFFFFF;
		int alpha = (argb >>> 24) & 0xFF;
		if (alpha == 0xFF) {
			return String.format(Locale.ROOT, "#%06X", rgb);
		}
		return String.format(Locale.ROOT, "#%02X%06X", alpha, rgb);
	}
}
