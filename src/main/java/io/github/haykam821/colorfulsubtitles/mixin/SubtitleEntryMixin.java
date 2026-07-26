package io.github.haykam821.colorfulsubtitles.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import io.github.haykam821.colorfulsubtitles.ColorHolder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.SubtitleOverlay;

@Mixin(SubtitleOverlay.Subtitle.class)
@Environment(EnvType.CLIENT)
public class SubtitleEntryMixin implements ColorHolder {
	@Unique
	private int colorfulsubtitles$textColor = 0;

	@Unique
	private int colorfulsubtitles$backgroundColor = 0;

	@Unique
	private boolean colorfulsubtitles$hasBackgroundColor = false;

	@Override
	public int getTextColor() {
		return this.colorfulsubtitles$textColor;
	}

	@Override
	public void setTextColor(int color) {
		this.colorfulsubtitles$textColor = color;
	}

	@Override
	public int getBackgroundColor() {
		return this.colorfulsubtitles$backgroundColor;
	}

	@Override
	public void setBackgroundColor(int color) {
		this.colorfulsubtitles$backgroundColor = color;
	}

	@Override
	public boolean hasBackgroundColor() {
		return this.colorfulsubtitles$hasBackgroundColor;
	}

	@Override
	public void setHasBackgroundColor(boolean has) {
		this.colorfulsubtitles$hasBackgroundColor = has;
	}
}
