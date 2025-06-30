package com.abysslasea.anvilinnovate.template;

import com.abysslasea.anvilinnovate.NetworkHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.Comparator;
import java.util.List;

public class TemplateSelectionScreen extends Screen {

    private static final int BUTTON_WIDTH = 200;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_SPACING = 24;
    private static final int TITLE_Y_OFFSET = 15;
    private static final int TOP_PADDING = 40;
    private static final int BOTTOM_PADDING = 30;
    public static final int ITEM_ICON_OFFSET = 4;

    private final BlockPos slabPos;
    private List<ResourceLocation> visibleTemplates;
    private int scrollOffset = 0;

    public TemplateSelectionScreen(BlockPos pos, Component title) {
        super(title);
        this.slabPos = pos;
    }

    @Override
    protected void init() {
        super.init();
        int centerX = this.width / 2;

        this.visibleTemplates = CarvingTemplateManager.getTemplateIds().stream()
                .sorted(Comparator.comparing(this::getTemplateSortKey))
                .toList();

        refreshTemplateButtons();
        addControlButtons(centerX);
    }

    private String getTemplateSortKey(ResourceLocation id) {
        CarvingTemplate template = CarvingTemplateManager.getTemplate(id);
        return template.getName();
    }

    private void refreshTemplateButtons() {
        clearWidgets();

        int centerX = this.width / 2;
        int maxVisible = getMaxVisibleButtons();
        int total = visibleTemplates.size();

        for (int i = 0; i < maxVisible; i++) {
            int index = i + scrollOffset;
            if (index >= total) break;

            final ResourceLocation templateId = visibleTemplates.get(index);
            final CarvingTemplate template = CarvingTemplateManager.getTemplate(templateId);
            int yPos = TOP_PADDING + (i * BUTTON_SPACING);

            Button button = new Button(
                    centerX - BUTTON_WIDTH / 2,
                    yPos,
                    BUTTON_WIDTH,
                    BUTTON_HEIGHT,
                    Component.empty(),
                    btn -> selectTemplate(templateId),
                    supplier -> Component.translatable("narration.button.template", template.getResult().getHoverName())
            ) {
                @Override
                public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
                    super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);

                    guiGraphics.renderItem(template.getResult(),
                            this.getX() + ITEM_ICON_OFFSET,
                            this.getY() + ITEM_ICON_OFFSET);

                    guiGraphics.drawString(
                            Minecraft.getInstance().font,
                            createButtonText(templateId, template.getResult()),
                            this.getX() + 24,
                            this.getY() + (this.height - 8) / 2,
                            0xFFFFFF,
                            false
                    );
                }
            };

            button.setTooltip(createTemplateTooltip(template));
            addRenderableWidget(button);
        }
    }
    private void addControlButtons(int centerX) {
        // Scroll buttons
        if (visibleTemplates.size() > getMaxVisibleButtons()) {
            addRenderableWidget(Button.builder(Component.literal("↑"), btn -> scrollUp())
                    .bounds(
                            centerX + BUTTON_WIDTH / 2 + 5,
                            TOP_PADDING,
                            20,
                            20
                    )
                    .build());

            addRenderableWidget(Button.builder(Component.literal("↓"), btn -> scrollDown())
                    .bounds(
                            centerX + BUTTON_WIDTH / 2 + 5,
                            TOP_PADDING + getMaxVisibleButtons() * BUTTON_SPACING - 20,
                            20,
                            20
                    )
                    .build());
        }

        addRenderableWidget(Button.builder(Component.translatable("gui.close"), btn -> onClose())
                .bounds(
                        centerX - 50,
                        this.height - 25,
                        100,
                        20
                )
                .build());
    }

    private void scrollUp() {
        if (scrollOffset > 0) {
            scrollOffset--;
            refreshTemplateButtons();
        }
    }

    private void scrollDown() {
        if (scrollOffset + getMaxVisibleButtons() < visibleTemplates.size()) {
            scrollOffset++;
            refreshTemplateButtons();
        }
    }

    private int getMaxVisibleButtons() {
        return (this.height - TOP_PADDING - BOTTOM_PADDING) / BUTTON_SPACING;
    }

    private void selectTemplate(ResourceLocation templateId) {
        NetworkHandler.sendToServer(new SetTemplatePacket(slabPos, templateId));
        onClose();
    }

    private MutableComponent createButtonText(ResourceLocation id, ItemStack result) {
        CarvingTemplate template = CarvingTemplateManager.getTemplate(id);
        return Component.translatable(template.getName());
    }

    private Tooltip createTemplateTooltip(CarvingTemplate template) {
        return Tooltip.create(
                Component.translatable("tooltip.anvilinnovate.output")
                        .append(": ")
                        .append(template.getResult().getHoverName())
                        .append("\n")
                        .append(Component.translatable("tooltip.anvilinnovate.pattern_size"))
                        .append(": 10x10")
        );
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);

        super.render(guiGraphics, mouseX, mouseY, partialTick);

        guiGraphics.drawCenteredString(
                this.font,
                this.title,
                this.width / 2,
                TITLE_Y_OFFSET,
                0xFFFFFF
        );

        guiGraphics.drawString(
                this.font,
                Component.translatable("gui.anvilinnovate.select_template_hint"),
                this.width / 2 - 100,
                this.height - 45,
                0xAAAAAA,
                false
        );
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
