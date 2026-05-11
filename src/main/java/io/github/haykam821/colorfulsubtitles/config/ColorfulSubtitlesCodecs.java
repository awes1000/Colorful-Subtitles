package io.github.haykam821.colorfulsubtitles.config;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Stream;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Keyable;

import net.minecraft.sounds.SoundSource;

public final class ColorfulSubtitlesCodecs {
	private static final SoundSource[] SOUND_SOURCES = SoundSource.values();

	private static final Codec<SoundSource> SOUND_SOURCE = Codec.STRING.comapFlatMap(ColorfulSubtitlesCodecs::getSoundSource, SoundSource::getName);

	private static final Keyable SOUND_SOURCE_KEYS = new Keyable() {
		@Override
		public <T> Stream<T> keys(DynamicOps<T> ops) {
			return Arrays.stream(SOUND_SOURCES)
				.map(SoundSource::getName)
				.map(ops::createString);
		}
	};

	protected static final Codec<Map<SoundSource, SubtitleColor>> SOUND_SOURCE_TO_SUBTITLE_COLOR = Codec.simpleMap(SOUND_SOURCE, SubtitleColor.CODEC, SOUND_SOURCE_KEYS).codec();

	private ColorfulSubtitlesCodecs() {
		return;
	}

	private static DataResult<SoundSource> getSoundSource(String name) {
		for (SoundSource source : SOUND_SOURCES) {
			if (source.getName().equals(name)) {
				return DataResult.success(source);
			}
		}

		return DataResult.error(() -> "Unknown sound source '" + name + "'");
	}
}
