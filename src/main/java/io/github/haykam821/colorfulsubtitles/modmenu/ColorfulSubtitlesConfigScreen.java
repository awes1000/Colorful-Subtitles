package io.github.haykam821.colorfulsubtitles.modmenu;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import io.github.haykam821.colorfulsubtitles.ColorfulSubtitles;
import io.github.haykam821.colorfulsubtitles.config.ColorfulSubtitlesConfig;
import io.github.haykam821.colorfulsubtitles.config.SubtitleColor;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.sounds.SoundSource;

public final class ColorfulSubtitlesConfigScreen {
	private static final int DEFAULT_BACKGROUND_ARGB = 0x80000000;

	private ColorfulSubtitlesConfigScreen() {
	}

	public static Screen create(Screen parent) {
		ColorfulSubtitlesConfig current = ColorfulSubtitles.getConfig();

		ConfigBuilder builder = ConfigBuilder.create()
			.setParentScreen(parent)
			.setTitle(Component.translatable("title.colorfulsubtitles.config"));

		ConfigEntryBuilder entries = builder.entryBuilder();

		Map<SoundSource, int[]> editedTextColors = new LinkedHashMap<>();
		Map<SoundSource, int[]> editedBackgroundColors = new LinkedHashMap<>();
		Map<SoundSource, boolean[]> editedHasBackground = new LinkedHashMap<>();

		ConfigCategory categoriesTab = builder.getOrCreateCategory(Component.translatable("category.colorfulsubtitles.categories"));

		SubtitleColor defaultColor = current.getDefaultColor();
		int[] defaultText = { defaultColor.getText().getValue() };
		int[] defaultBackground = { defaultColor.getBackground().orElse(DEFAULT_BACKGROUND_ARGB) };
		boolean[] defaultHasBackground = { defaultColor.getBackground().isPresent() };

		categoriesTab.addEntry(entries.startColorField(Component.translatable("option.colorfulsubtitles.default.text"), defaultText[0])
			.setDefaultValue(0xFFFFFF)
			.setSaveConsumer(value -> defaultText[0] = value)
			.build());
		categoriesTab.addEntry(entries.startBooleanToggle(Component.translatable("option.colorfulsubtitles.default.has_background"), defaultHasBackground[0])
			.setDefaultValue(false)
			.setSaveConsumer(value -> defaultHasBackground[0] = value)
			.build());
		categoriesTab.addEntry(entries.startAlphaColorField(Component.translatable("option.colorfulsubtitles.default.background"), defaultBackground[0])
			.setDefaultValue(DEFAULT_BACKGROUND_ARGB)
			.setSaveConsumer(value -> defaultBackground[0] = value)
			.build());

		for (SoundSource source : SoundSource.values()) {
			SubtitleColor color = current.getColors().getOrDefault(source, defaultColor);
			int[] text = { color.getText().getValue() };
			int[] background = { color.getBackground().orElse(DEFAULT_BACKGROUND_ARGB) };
			boolean[] hasBackground = { color.getBackground().isPresent() };

			Component name = Component.translatable("soundCategory." + source.getName());

			categoriesTab.addEntry(entries.startSubCategory(name, java.util.List.of(
				entries.startColorField(Component.translatable("option.colorfulsubtitles.text"), text[0])
					.setDefaultValue(getDefaultTextValue(source))
					.setSaveConsumer(value -> text[0] = value)
					.build(),
				entries.startBooleanToggle(Component.translatable("option.colorfulsubtitles.has_background"), hasBackground[0])
					.setDefaultValue(false)
					.setSaveConsumer(value -> hasBackground[0] = value)
					.build(),
				entries.startAlphaColorField(Component.translatable("option.colorfulsubtitles.background"), background[0])
					.setDefaultValue(DEFAULT_BACKGROUND_ARGB)
					.setSaveConsumer(value -> background[0] = value)
					.build()
			)).build());

			editedTextColors.put(source, text);
			editedBackgroundColors.put(source, background);
			editedHasBackground.put(source, hasBackground);
		}

		builder.setSavingRunnable(() -> {
			Map<SoundSource, SubtitleColor> newColors = new LinkedHashMap<>();
			for (SoundSource source : SoundSource.values()) {
				int textValue = editedTextColors.get(source)[0];
				int backgroundValue = editedBackgroundColors.get(source)[0];
				boolean hasBackground = editedHasBackground.get(source)[0];

				Optional<Integer> bg = hasBackground ? Optional.of(backgroundValue) : Optional.empty();
				newColors.put(source, SubtitleColor.create(TextColor.fromRgb(textValue), bg));
			}

			Optional<Integer> defaultBg = defaultHasBackground[0] ? Optional.of(defaultBackground[0]) : Optional.empty();
			SubtitleColor newDefault = SubtitleColor.create(TextColor.fromRgb(defaultText[0]), defaultBg);

			ColorfulSubtitles.setConfig(ColorfulSubtitlesConfig.create(newColors, newDefault));
		});

		return builder.build();
	}

	private static int getDefaultTextValue(SoundSource source) {
		SubtitleColor color = ColorfulSubtitlesConfig.getDefaults().get(source);
		return color == null ? 0xFFFFFF : color.getText().getValue();
	}
}

