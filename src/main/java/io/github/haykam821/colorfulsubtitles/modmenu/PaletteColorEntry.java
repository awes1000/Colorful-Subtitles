package io.github.haykam821.colorfulsubtitles.modmenu;

import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;

import me.shedaniel.clothconfig2.gui.entries.ColorEntry;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class PaletteColorEntry extends ColorEntry {
	private static final int BASE_HEIGHT = 24;
	private static final int PALETTE_HEIGHT = 34;
	private static final int ALPHA_HEIGHT = 8;
	private static final int GAP = 3;

	private final boolean alphaMode;
	private boolean expanded;
	private boolean draggingPalette;
	private boolean draggingAlpha;
	private int entryX;
	private int entryY;
	private int entryWidth = 32;
	private int paletteX;
	private int paletteY;
	private int paletteWidth = 32;
	private BooleanSupplier displayCondition;

	@SuppressWarnings("deprecation")
	public PaletteColorEntry(Component fieldName, int value, Component resetButtonKey, Supplier<Integer> defaultValue, Consumer<Integer> saveConsumer, boolean alphaMode) {
		super(fieldName, value, resetButtonKey, defaultValue, saveConsumer, () -> Optional.empty(), false);
		this.alphaMode = alphaMode;

		if (alphaMode) {
			this.withAlpha();
		} else {
			this.withoutAlpha();
		}
	}

	public void setDisplayCondition(BooleanSupplier condition) {
		this.displayCondition = condition;
	}

	private boolean shouldDisplay() {
		return this.displayCondition == null || this.displayCondition.getAsBoolean();
	}

	@Override
	public int getItemHeight() {
		if (!this.shouldDisplay()) {
			return 0;
		}

		if (!this.expanded) {
			return BASE_HEIGHT;
		}

		return BASE_HEIGHT + PALETTE_HEIGHT + (this.alphaMode ? ALPHA_HEIGHT + GAP : 0) + GAP * 2;
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
		if (!this.shouldDisplay()) {
			return;
		}

		super.extractRenderState(graphics, index, y, x, entryWidth, entryHeight, mouseX, mouseY, hovered, tickDelta);

		this.entryX = x;
		this.entryY = y;
		this.entryWidth = entryWidth;
		this.paletteX = x + 4;
		this.paletteY = y + BASE_HEIGHT + GAP;
		this.paletteWidth = Math.max(32, entryWidth - 8);

		if (this.textFieldWidget.isFocused()) {
			this.expanded = true;
		}

		if (!this.expanded) {
			return;
		}

		drawPalette(graphics, this.paletteX, this.paletteY, this.paletteWidth, PALETTE_HEIGHT);
		drawPaletteMarker(graphics, this.paletteX, this.paletteY, this.paletteWidth, PALETTE_HEIGHT);

		if (this.alphaMode) {
			int alphaY = this.paletteY + PALETTE_HEIGHT + GAP;
			drawAlpha(graphics, this.paletteX, alphaY, this.paletteWidth, ALPHA_HEIGHT);
			drawAlphaMarker(graphics, this.paletteX, alphaY, this.paletteWidth, ALPHA_HEIGHT);
		}
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		double mouseX = event.x();
		double mouseY = event.y();

		if (isInBaseEntry(mouseX, mouseY)) {
			this.expanded = true;
		}

		if (isInPalette(mouseX, mouseY)) {
			this.expanded = true;
			this.draggingPalette = true;
			this.updatePalette(mouseX, mouseY);
			return true;
		}

		if (this.alphaMode && isInAlpha(mouseX, mouseY)) {
			this.expanded = true;
			this.draggingAlpha = true;
			this.updateAlpha(mouseX);
			return true;
		}

		boolean handled = super.mouseClicked(event, doubleClick);
		if (handled) {
			this.expanded = true;
		} else if (!isInExpandedEntry(mouseX, mouseY)) {
			this.expanded = false;
		}

		return handled;
	}

	@Override
	public boolean mouseDragged(MouseButtonEvent event, double deltaX, double deltaY) {
		if (this.draggingPalette) {
			this.updatePalette(event.x(), event.y());
			return true;
		}

		if (this.draggingAlpha) {
			this.updateAlpha(event.x());
			return true;
		}

		return super.mouseDragged(event, deltaX, deltaY);
	}

	@Override
	public boolean mouseReleased(MouseButtonEvent event) {
		this.draggingPalette = false;
		this.draggingAlpha = false;
		return super.mouseReleased(event);
	}

	@Override
	public void updateSelected(boolean selected) {
		super.updateSelected(selected);
		if (!selected && !this.draggingPalette && !this.draggingAlpha && !this.textFieldWidget.isFocused()) {
			this.expanded = false;
		}
	}

	private void drawPalette(GuiGraphicsExtractor graphics, int x, int y, int width, int height) {
		graphics.outline(x - 1, y - 1, width + 2, height + 2, 0xFF555555);

		for (int offset = 0; offset < width; offset += 2) {
			float hue = offset / (float) Math.max(1, width - 1);
			int bright = hsvToRgb(hue, 1, 1);
			int next = Math.min(width, offset + 2);
			graphics.fillGradient(x + offset, y, x + next, y + height, 0xFF000000 | bright, 0xFF000000);
		}
	}

	private void drawPaletteMarker(GuiGraphicsExtractor graphics, int x, int y, int width, int height) {
		float[] hsv = rgbToHsv(currentColor() & 0xFFFFFF);
		int markerX = x + Math.round(hsv[0] * (width - 1));
		int markerY = y + Math.round((1 - hsv[2]) * (height - 1));
		graphics.outline(markerX - 2, markerY - 2, 5, 5, 0xFFFFFFFF);
		graphics.outline(markerX - 1, markerY - 1, 3, 3, 0xFF000000);
	}

	private void drawAlpha(GuiGraphicsExtractor graphics, int x, int y, int width, int height) {
		graphics.outline(x - 1, y - 1, width + 2, height + 2, 0xFF555555);

		int rgb = currentColor() & 0xFFFFFF;
		for (int offset = 0; offset < width; offset += 2) {
			int alpha = Math.round(255 * offset / (float) Math.max(1, width - 1));
			int next = Math.min(width, offset + 2);
			graphics.fill(x + offset, y, x + next, y + height, (alpha << 24) | rgb);
		}
	}

	private void drawAlphaMarker(GuiGraphicsExtractor graphics, int x, int y, int width, int height) {
		int alpha = (currentColor() >>> 24) & 0xFF;
		int markerX = x + Math.round(alpha * (width - 1) / 255f);
		graphics.outline(markerX - 1, y - 2, 3, height + 4, 0xFFFFFFFF);
		graphics.outline(markerX, y - 1, 1, height + 2, 0xFF000000);
	}

	private boolean isInPalette(double mouseX, double mouseY) {
		if (!this.expanded) {
			return false;
		}

		return mouseX >= this.paletteX && mouseX <= this.paletteX + this.paletteWidth && mouseY >= this.paletteY && mouseY <= this.paletteY + PALETTE_HEIGHT;
	}

	private boolean isInAlpha(double mouseX, double mouseY) {
		if (!this.expanded) {
			return false;
		}

		int y = this.paletteY + PALETTE_HEIGHT + GAP;
		return mouseX >= this.paletteX && mouseX <= this.paletteX + this.paletteWidth && mouseY >= y && mouseY <= y + ALPHA_HEIGHT;
	}

	private boolean isInBaseEntry(double mouseX, double mouseY) {
		return mouseX >= this.entryX && mouseX <= this.entryX + this.entryWidth && mouseY >= this.entryY && mouseY <= this.entryY + BASE_HEIGHT;
	}

	private boolean isInExpandedEntry(double mouseX, double mouseY) {
		return mouseX >= this.entryX && mouseX <= this.entryX + this.entryWidth && mouseY >= this.entryY && mouseY <= this.entryY + this.getItemHeight();
	}

	private void updatePalette(double mouseX, double mouseY) {
		float hue = clamp((float) ((mouseX - this.paletteX) / Math.max(1, this.paletteWidth - 1)));
		float value = 1 - clamp((float) ((mouseY - this.paletteY) / Math.max(1, PALETTE_HEIGHT - 1)));

		int rgb = hsvToRgb(hue, 1, value);
		if (this.alphaMode) {
			int alpha = (currentColor() >>> 24) & 0xFF;
			this.textFieldWidget.setValue(this.getHexColorString((alpha << 24) | rgb));
		} else {
			this.textFieldWidget.setValue(this.getHexColorString(rgb));
		}
	}

	private void updateAlpha(double mouseX) {
		int alpha = Math.round(255 * clamp((float) ((mouseX - this.paletteX) / Math.max(1, this.paletteWidth - 1))));
		this.textFieldWidget.setValue(this.getHexColorString((alpha << 24) | (currentColor() & 0xFFFFFF)));
	}

	private int currentColor() {
		try {
			int color = this.getValue();
			return this.alphaMode ? color : 0xFF000000 | (color & 0xFFFFFF);
		} catch (Exception exception) {
			return this.alphaMode ? 0xFFFFFFFF : 0xFF000000 | this.original;
		}
	}

	private static float clamp(float value) {
		return Math.max(0, Math.min(1, value));
	}

	private static int hsvToRgb(float hue, float saturation, float value) {
		int sector = (int) Math.floor(hue * 6);
		float fraction = hue * 6 - sector;
		float p = value * (1 - saturation);
		float q = value * (1 - fraction * saturation);
		float t = value * (1 - (1 - fraction) * saturation);

		float red;
		float green;
		float blue;
		switch (sector % 6) {
			case 0 -> {
				red = value;
				green = t;
				blue = p;
			}
			case 1 -> {
				red = q;
				green = value;
				blue = p;
			}
			case 2 -> {
				red = p;
				green = value;
				blue = t;
			}
			case 3 -> {
				red = p;
				green = q;
				blue = value;
			}
			case 4 -> {
				red = t;
				green = p;
				blue = value;
			}
			default -> {
				red = value;
				green = p;
				blue = q;
			}
		}

		return Math.round(red * 255) << 16 | Math.round(green * 255) << 8 | Math.round(blue * 255);
	}

	private static float[] rgbToHsv(int rgb) {
		float red = ((rgb >> 16) & 0xFF) / 255f;
		float green = ((rgb >> 8) & 0xFF) / 255f;
		float blue = (rgb & 0xFF) / 255f;
		float max = Math.max(red, Math.max(green, blue));
		float min = Math.min(red, Math.min(green, blue));
		float delta = max - min;

		float hue;
		if (delta == 0) {
			hue = 0;
		} else if (max == red) {
			hue = ((green - blue) / delta) % 6;
		} else if (max == green) {
			hue = (blue - red) / delta + 2;
		} else {
			hue = (red - green) / delta + 4;
		}

		hue /= 6;
		if (hue < 0) {
			hue += 1;
		}

		float saturation = max == 0 ? 0 : delta / max;
		return new float[] { hue, saturation, max };
	}
}
