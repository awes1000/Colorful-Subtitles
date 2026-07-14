package io.github.haykam821.colorfulsubtitles.modmenu;

import java.util.Optional;
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

   public PaletteColorEntry(
      Component fieldName, int value, Component resetButtonKey, Supplier<Integer> defaultValue, Consumer<Integer> saveConsumer, boolean alphaMode
   ) {
      super(fieldName, value, resetButtonKey, defaultValue, saveConsumer, () -> Optional.empty(), false);
      this.alphaMode = alphaMode;
      if (alphaMode) {
         this.withAlpha();
      } else {
         this.withoutAlpha();
      }
   }

   public int getItemHeight() {
      return !this.expanded ? 24 : 58 + (this.alphaMode ? 11 : 0) + 6;
   }

   public void extractRenderState(
      GuiGraphicsExtractor graphics, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta
   ) {
      super.extractRenderState(graphics, index, y, x, entryWidth, entryHeight, mouseX, mouseY, hovered, tickDelta);
      this.entryX = x;
      this.entryY = y;
      this.entryWidth = entryWidth;
      this.paletteX = x + 4;
      this.paletteY = y + 24 + 3;
      this.paletteWidth = Math.max(32, entryWidth - 8);
      if (this.textFieldWidget.isFocused()) {
         this.expanded = true;
      }

      if (this.expanded) {
         this.drawPalette(graphics, this.paletteX, this.paletteY, this.paletteWidth, 34);
         this.drawPaletteMarker(graphics, this.paletteX, this.paletteY, this.paletteWidth, 34);
         if (this.alphaMode) {
            int alphaY = this.paletteY + 34 + 3;
            this.drawAlpha(graphics, this.paletteX, alphaY, this.paletteWidth, 8);
            this.drawAlphaMarker(graphics, this.paletteX, alphaY, this.paletteWidth, 8);
         }
      }
   }

   public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
      double mouseX = event.x();
      double mouseY = event.y();
      if (this.isInBaseEntry(mouseX, mouseY)) {
         this.expanded = true;
      }

      if (this.isInPalette(mouseX, mouseY)) {
         this.expanded = true;
         this.draggingPalette = true;
         this.updatePalette(mouseX, mouseY);
         return true;
      }

      if (this.alphaMode && this.isInAlpha(mouseX, mouseY)) {
         this.expanded = true;
         this.draggingAlpha = true;
         this.updateAlpha(mouseX);
         return true;
      }

      boolean handled = super.mouseClicked(event, doubleClick);
      if (handled) {
         this.expanded = true;
      } else if (!this.isInExpandedEntry(mouseX, mouseY)) {
         this.expanded = false;
      }

      return handled;
   }

   public boolean mouseDragged(MouseButtonEvent event, double deltaX, double deltaY) {
      if (this.draggingPalette) {
         this.updatePalette(event.x(), event.y());
         return true;
      } else if (this.draggingAlpha) {
         this.updateAlpha(event.x());
         return true;
      } else {
         return super.mouseDragged(event, deltaX, deltaY);
      }
   }

   public boolean mouseReleased(MouseButtonEvent event) {
      this.draggingPalette = false;
      this.draggingAlpha = false;
      return super.mouseReleased(event);
   }

   public void updateSelected(boolean selected) {
      super.updateSelected(selected);
      if (!selected && !this.draggingPalette && !this.draggingAlpha && !this.textFieldWidget.isFocused()) {
         this.expanded = false;
      }
   }

   private void drawPalette(GuiGraphicsExtractor graphics, int x, int y, int width, int height) {
      graphics.outline(x - 1, y - 1, width + 2, height + 2, -11184811);

      for (int offset = 0; offset < width; offset += 2) {
         float hue = (float)offset / Math.max(1, width - 1);
         int bright = hsvToRgb(hue, 1.0F, 1.0F);
         int next = Math.min(width, offset + 2);
         graphics.fillGradient(x + offset, y, x + next, y + height, 0xFF000000 | bright, -16777216);
      }
   }

   private void drawPaletteMarker(GuiGraphicsExtractor graphics, int x, int y, int width, int height) {
      float[] hsv = rgbToHsv(this.currentColor() & 16777215);
      int markerX = x + Math.round(hsv[0] * (width - 1));
      int markerY = y + Math.round((1.0F - hsv[2]) * (height - 1));
      graphics.outline(markerX - 2, markerY - 2, 5, 5, -1);
      graphics.outline(markerX - 1, markerY - 1, 3, 3, -16777216);
   }

   private void drawAlpha(GuiGraphicsExtractor graphics, int x, int y, int width, int height) {
      graphics.outline(x - 1, y - 1, width + 2, height + 2, -11184811);
      int rgb = this.currentColor() & 16777215;

      for (int offset = 0; offset < width; offset += 2) {
         int alpha = Math.round((float)(255 * offset) / Math.max(1, width - 1));
         int next = Math.min(width, offset + 2);
         graphics.fill(x + offset, y, x + next, y + height, alpha << 24 | rgb);
      }
   }

   private void drawAlphaMarker(GuiGraphicsExtractor graphics, int x, int y, int width, int height) {
      int alpha = this.currentColor() >>> 24 & 0xFF;
      int markerX = x + Math.round(alpha * (width - 1) / 255.0F);
      graphics.outline(markerX - 1, y - 2, 3, height + 4, -1);
      graphics.outline(markerX, y - 1, 1, height + 2, -16777216);
   }

   private boolean isInPalette(double mouseX, double mouseY) {
      return !this.expanded
         ? false
         : mouseX >= this.paletteX && mouseX <= this.paletteX + this.paletteWidth && mouseY >= this.paletteY && mouseY <= this.paletteY + 34;
   }

   private boolean isInAlpha(double mouseX, double mouseY) {
      if (!this.expanded) {
         return false;
      }

      int y = this.paletteY + 34 + 3;
      return mouseX >= this.paletteX && mouseX <= this.paletteX + this.paletteWidth && mouseY >= y && mouseY <= y + 8;
   }

   private boolean isInBaseEntry(double mouseX, double mouseY) {
      return mouseX >= this.entryX && mouseX <= this.entryX + this.entryWidth && mouseY >= this.entryY && mouseY <= this.entryY + 24;
   }

   private boolean isInExpandedEntry(double mouseX, double mouseY) {
      return mouseX >= this.entryX && mouseX <= this.entryX + this.entryWidth && mouseY >= this.entryY && mouseY <= this.entryY + this.getItemHeight();
   }

   private void updatePalette(double mouseX, double mouseY) {
      float hue = clamp((float)((mouseX - this.paletteX) / Math.max(1, this.paletteWidth - 1)));
      float value = 1.0F - clamp((float)((mouseY - this.paletteY) / Math.max(1, 33)));
      int rgb = hsvToRgb(hue, 1.0F, value);
      if (this.alphaMode) {
         int alpha = this.currentColor() >>> 24 & 0xFF;
         this.setValue(alpha << 24 | rgb);
      } else {
         this.setValue(rgb);
      }
   }

   private void updateAlpha(double mouseX) {
      int alpha = Math.round(255.0F * clamp((float)((mouseX - this.paletteX) / Math.max(1, this.paletteWidth - 1))));
      this.setValue(alpha << 24 | this.currentColor() & 16777215);
   }

   private int currentColor() {
      try {
         int color = this.getValue();
         return this.alphaMode ? color : 0xFF000000 | color & 16777215;
      } catch (Exception exception) {
         return this.alphaMode ? -1 : 0xFF000000 | (Integer)this.original;
      }
   }

   private static float clamp(float value) {
      return Math.max(0.0F, Math.min(1.0F, value));
   }

   private static int hsvToRgb(float hue, float saturation, float value) {
      int sector = (int)Math.floor(hue * 6.0F);
      float fraction = hue * 6.0F - sector;
      float p = value * (1.0F - saturation);
      float q = value * (1.0F - fraction * saturation);
      float t = value * (1.0F - (1.0F - fraction) * saturation);
      float red;
      float green;
      float blue;
      switch (sector % 6) {
         case 0:
            red = value;
            green = t;
            blue = p;
            break;
         case 1:
            red = q;
            green = value;
            blue = p;
            break;
         case 2:
            red = p;
            green = value;
            blue = t;
            break;
         case 3:
            red = p;
            green = q;
            blue = value;
            break;
         case 4:
            red = t;
            green = p;
            blue = value;
            break;
         default:
            red = value;
            green = p;
            blue = q;
      }

      return Math.round(red * 255.0F) << 16 | Math.round(green * 255.0F) << 8 | Math.round(blue * 255.0F);
   }

   private static float[] rgbToHsv(int rgb) {
      float red = (rgb >> 16 & 0xFF) / 255.0F;
      float green = (rgb >> 8 & 0xFF) / 255.0F;
      float blue = (rgb & 0xFF) / 255.0F;
      float max = Math.max(red, Math.max(green, blue));
      float min = Math.min(red, Math.min(green, blue));
      float delta = max - min;
      float hue;
      if (delta == 0.0F) {
         hue = 0.0F;
      } else if (max == red) {
         hue = (green - blue) / delta % 6.0F;
      } else if (max == green) {
         hue = (blue - red) / delta + 2.0F;
      } else {
         hue = (red - green) / delta + 4.0F;
      }

      hue /= 6.0F;
      if (hue < 0.0F) {
         hue++;
      }

      float saturation = max == 0.0F ? 0.0F : delta / max;
      return new float[]{hue, saturation, max};
   }
}
