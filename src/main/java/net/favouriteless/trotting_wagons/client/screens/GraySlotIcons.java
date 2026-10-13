package net.favouriteless.trotting_wagons.client.screens;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.NativeImage;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import net.favouriteless.trotting_wagons.TrottingWagons;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.system.MemoryUtil;

final class GraySlotIcons {

    private static final Map<Item, ResourceLocation> CACHE = new HashMap<>();

    private GraySlotIcons() {}

    static void draw(GuiGraphics graphics, ItemStack stack, int x, int y) {
        ResourceLocation image = CACHE.get(stack.getItem());
        if(image == null) {
            graphics.renderItem(stack, x, y);
            graphics.flush();
            image = capture(stack.getItem(), x, y);
            if(image == null)
                return;
            CACHE.put(stack.getItem(), image);
        }
        graphics.blit(image, x, y, 0, 0.0F, 0.0F, 16, 16, 16, 16);
    }

    private static ResourceLocation capture(Item item, int x, int y) {
        Minecraft minecraft = Minecraft.getInstance();
        int scale = Math.max(1, (int)Math.round(minecraft.getWindow().getGuiScale()));
        int width = 16 * scale;
        int height = 16 * scale;
        int px = x * scale;
        int py = minecraft.getWindow().getHeight() - (y + 16) * scale;
        ByteBuffer buffer = MemoryUtil.memAlloc(width * height * 4);
        try {
            GlStateManager._readPixels(px, py, width, height, 6408, 5121, buffer);
            NativeImage image = new NativeImage(16, 16, false);
            for(int gy = 0; gy < 16; gy++) {
                for(int gx = 0; gx < 16; gx++) {
                    int red = 0;
                    int green = 0;
                    int blue = 0;
                    int alpha = 0;
                    int samples = scale * scale;
                    for(int sy = 0; sy < scale; sy++) {
                        for(int sx = 0; sx < scale; sx++) {
                            int index = ((gy * scale + sy) * width + gx * scale + sx) * 4;
                            red += buffer.get(index) & 255;
                            green += buffer.get(index + 1) & 255;
                            blue += buffer.get(index + 2) & 255;
                            alpha += buffer.get(index + 3) & 255;
                        }
                    }
                    red /= samples;
                    green /= samples;
                    blue /= samples;
                    alpha /= samples;
                    int gray = 127 + ((red * 30 + green * 59 + blue * 11) / 100) / 2;
                    int packed = (alpha << 24) | (gray << 16) | (gray << 8) | gray;
                    image.setPixelRGBA(gx, 15 - gy, packed);
                }
            }
            ResourceLocation id = TrottingWagons.id("gui/gray_slot/" + item.builtInRegistryHolder().key().location().getPath());
            minecraft.getTextureManager().register(id, new DynamicTexture(image));
            return id;
        } catch(RuntimeException ignored) {
            return null;
        } finally {
            MemoryUtil.memFree(buffer);
        }
    }

}
