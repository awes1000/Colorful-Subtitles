package io.github.haykam821.colorfulsubtitles;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;

import io.github.haykam821.colorfulsubtitles.config.ColorfulSubtitlesConfig;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.sounds.SoundSource;

public final class ColorfulSubtitles {
	public static final String MOD_ID = "colorfulsubtitles";
	public static final Logger LOGGER = LoggerFactory.getLogger("Colorful Subtitles");

	private static ColorfulSubtitlesConfig config;

	private ColorfulSubtitles() {
		return;
	}

	public static ColorfulSubtitlesConfig getConfig() {
		if (config == null) {
			config = ColorfulSubtitles.loadConfig();
		}

		return config;
	}

	public static void setConfig(ColorfulSubtitlesConfig newConfig) {
		config = newConfig;
		saveConfig(config);
	}

	public static Path getConfigPath() {
		return FabricLoader.getInstance().getConfigDir().resolve(MOD_ID + ".json");
	}

	private static ColorfulSubtitlesConfig loadConfig() {
		File file = getConfigPath().toFile();

		if (!file.exists() || file.length() == 0) {
			saveConfig(ColorfulSubtitlesConfig.DEFAULT);
			return ColorfulSubtitlesConfig.DEFAULT;
		}

		try (Reader reader = new BufferedReader(Files.newBufferedReader(file.toPath()))) {
			JsonElement json = JsonParser.parseReader(reader);
			DataResult<Pair<ColorfulSubtitlesConfig, JsonElement>> result = ColorfulSubtitlesConfig.CODEC.decode(JsonOps.INSTANCE, json);
			ColorfulSubtitlesConfig loadedConfig = ColorfulSubtitlesConfig.withDefaults(result.getOrThrow().getFirst());

			if (!isCompleteConfig(json)) {
				saveConfig(loadedConfig);
			}

			return loadedConfig;
		} catch (Exception exception) {
			LOGGER.warn("Failed to read Colorful Subtitles config; replacing it with defaults", exception);
			saveConfig(ColorfulSubtitlesConfig.DEFAULT);
			return ColorfulSubtitlesConfig.DEFAULT;
		}
	}

	private static boolean isCompleteConfig(JsonElement json) {
		if (!json.isJsonObject()) {
			return false;
		}

		JsonObject object = json.getAsJsonObject();
		if (!object.has("default_color") || !object.has("colors") || !object.get("colors").isJsonObject()) {
			return false;
		}

		JsonObject colors = object.getAsJsonObject("colors");
		for (SoundSource source : SoundSource.values()) {
			if (!colors.has(source.getName())) {
				return false;
			}
		}

		return true;
	}

	public static void saveConfig(ColorfulSubtitlesConfig configToSave) {
		Path path = getConfigPath();
		Path parent = path.getParent();

		try {
			if (parent != null) {
				Files.createDirectories(parent);
			}
		} catch (Exception exception) {
			LOGGER.warn("Failed to create Colorful Subtitles config directory", exception);
			return;
		}

		try (Writer writer = new BufferedWriter(Files.newBufferedWriter(path))) {
			DataResult<JsonElement> result = ColorfulSubtitlesConfig.FULL_CODEC.encodeStart(JsonOps.INSTANCE, configToSave);
			new GsonBuilder().setPrettyPrinting().create().toJson(result.getOrThrow(), writer);
		} catch (Exception exception) {
			LOGGER.warn("Failed to write Colorful Subtitles config", exception);
		}
	}
}
