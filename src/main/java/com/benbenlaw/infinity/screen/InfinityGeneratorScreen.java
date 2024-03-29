package com.benbenlaw.infinity.screen;

import com.benbenlaw.infinity.Infinity;
import com.benbenlaw.infinity.recipe.GeneratorRecipe;
import com.benbenlaw.infinity.util.MouseUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class InfinityGeneratorScreen extends AbstractContainerScreen<InfinityGeneratorMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(Infinity.MOD_ID,"textures/gui/infinity_generator_gui.png");

    private EnergyDisplayTooltipArea energyInfoArea;
    private GeneratorDisplayTooltipArea generatorInfoArea;


    public InfinityGeneratorScreen(InfinityGeneratorMenu menu, Inventory inventory, Component component) {
        super(menu, inventory, component);
    }

    @Override
    protected void init() {
        super.init();
        assignEnergyInfoArea();
        assignGeneratorInfoArea();
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int pMouseX, int pMouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        renderEnergyAreaTooltip(guiGraphics, pMouseX, pMouseY, x, y);
        renderInformationTooltip(guiGraphics, pMouseX, pMouseY, x, y);

        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 4210752, false);
        //guiGraphics.drawString(this.font, Component.translatable("jei_pattern." + id), this.titleLabelX + 7, this.titleLabelY + 10, 4210752, false);
    }

    private void renderEnergyAreaTooltip(GuiGraphics guiGraphics, int pMouseX, int pMouseY, int x, int y) {
        if(isMouseAboveArea(pMouseX, pMouseY, x, y, 156, 11, 8, 64)) {
            guiGraphics.renderTooltip(this.font, energyInfoArea.getTooltips(),
                    Optional.empty(), pMouseX - x, pMouseY - y);
        }
    }

    private void renderInformationTooltip(GuiGraphics guiGraphics, int pMouseX, int pMouseY, int x, int y) {
        if(isMouseAboveArea(pMouseX, pMouseY, x, y, 80, 61, 16, 16)) {
            guiGraphics.renderTooltip(this.font, generatorInfoArea.getTooltips(),
                    Optional.empty(), pMouseX - x, pMouseY - y);
        }
    }


    private void assignEnergyInfoArea() {
        energyInfoArea = new EnergyDisplayTooltipArea(((width - imageWidth) / 2) + 156,
                ((height - imageHeight) / 2) + 11, menu.blockEntity.getEnergyStorage());
    }

    private void assignGeneratorInfoArea() {
        generatorInfoArea = new GeneratorDisplayTooltipArea(((width - imageWidth) / 2) + 156,
                ((height - imageHeight) / 2) + 11, menu.blockEntity);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        guiGraphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);

        energyInfoArea.render(guiGraphics);
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, delta);
        renderTooltip(guiGraphics, mouseX, mouseY);

    }

    private boolean isMouseAboveArea(int pMouseX, int pMouseY, int x, int y, int offsetX, int offsetY, int width, int height) {
        return MouseUtil.isMouseOver(pMouseX, pMouseY, x + offsetX, y + offsetY, width, height);
    }
}