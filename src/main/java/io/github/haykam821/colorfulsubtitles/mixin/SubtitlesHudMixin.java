package io.github.haykam821.colorfulsubtitles.mixin;

import java.util.Iterator;
import java.util.List;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import io.github.haykam821.colorfulsubtitles.ColorHolder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.SubtitleOverlay;
import net.minecraft.client.gui.components.SubtitleOverlay.Subtitle;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;

@Mixin(SubtitleOverlay.class)
@Environment(EnvType.CLIENT)
public class SubtitlesHudMixin {
	@Unique
	private ColorHolder colorfulsubtitles$iterationEntry;

	@Unique
	private static Boolean colorfulsubtitles$modernUiLoaded;

	@Redirect(method = "extractRenderState", at = @At(value = "INVOKE", target = "Ljava/util/Iterator;next()Ljava/lang/Object;", ordinal = 2))
	private Object updateIterationEntry(Iterator<Object> iterator) {
		return this.colorfulsubtitles$iterationEntry = (ColorHolder) iterator.next();
	}

	@ModifyArg(method = "extractRenderState", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;text(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;III)V"), index = 4)
	private int modifyTextDrawColor(int color) {
		if (colorfulsubtitles$isModernUiLoaded()) {
			return color;
		}

		return colorfulsubtitles$applySubtitleTextColor(color);
	}

	@ModifyExpressionValue(method = "extractRenderState", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/ARGB;color(IIII)I"))
	private int modifyModernUiTextDrawColor(int color) {
		if (!colorfulsubtitles$isModernUiLoaded()) {
			return color;
		}

		return colorfulsubtitles$applySubtitleTextColor(color);
	}

	@ModifyArg(method = "extractRenderState", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;fill(IIIII)V"), index = 4)
	private int modifyBackgroundDrawColor(int color) {
		if (this.colorfulsubtitles$iterationEntry == null || !this.colorfulsubtitles$iterationEntry.hasBackgroundColor()) {
			return color;
		}

		return this.colorfulsubtitles$iterationEntry.getBackgroundColor();
	}

	@Inject(method = "onPlaySound", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/SubtitleOverlay$Subtitle;refresh(Lnet/minecraft/world/phys/Vec3;)V"), locals = LocalCapture.CAPTURE_FAILHARD)
	private void resetColor(SoundInstance sound, WeighedSoundEvents soundSet, float range, CallbackInfo ci, Component text, Iterator<Subtitle> iterator, Subtitle entry) {
		((ColorHolder) entry).setColor(sound);
	}

	@Redirect(method = "onPlaySound", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z"))
	private boolean setColor(List<Object> entries, Object entry, SoundInstance sound, WeighedSoundEvents soundSet) {
		((ColorHolder) entry).setColor(sound);
		return entries.add(entry);
	}

	@Unique
	private int colorfulsubtitles$applySubtitleTextColor(int color) {
		if (this.colorfulsubtitles$iterationEntry == null) {
			return color;
		}

		return ARGB.scaleRGB(this.colorfulsubtitles$iterationEntry.getTextColor(), ARGB.red(color) / 255f);
	}

	@Unique
	private static boolean colorfulsubtitles$isModernUiLoaded() {
		if (colorfulsubtitles$modernUiLoaded == null) {
			colorfulsubtitles$modernUiLoaded = FabricLoader.getInstance().isModLoaded("modernui");
		}

		return colorfulsubtitles$modernUiLoaded;
	}
}
