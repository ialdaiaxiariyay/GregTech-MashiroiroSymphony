package top.ialdaiaxiariyay.gtms.api.gui;

import com.lowdragmc.lowdraglib.gui.util.ClickData;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.utils.Position;
import com.lowdragmc.lowdraglib.utils.Size;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.ialdaiaxiariyay.gtms.utils.FormatUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javax.annotation.Nonnull;

public class AlignComponentPanelWidget extends Widget {

    protected int maxWidthLimit;
    protected @Nullable Consumer<List<Component>> textProvider;
    protected BiConsumer<String, ClickData> clickAction;
    protected List<Component> lastComponents = new ArrayList<>();
    protected List<FormattedCharSequence> cachedLines = Collections.emptyList();
    protected boolean centered = false;
    protected int lineSpacing = 2;
    protected String delimiter;

    public AlignComponentPanelWidget(int x, int y, @Nonnull Consumer<List<Component>> textProvider) {
        super(x, y, 0, 0);
        this.textProvider = textProvider;
        this.textProvider.accept(this.lastComponents);
    }

    public static Component withButton(Component textComponent, String componentData) {
        Style style = textComponent.getStyle();
        style = style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "@!" + componentData));
        style = style.withColor(ChatFormatting.YELLOW);
        return textComponent.copy().withStyle(style);
    }

    public AlignComponentPanelWidget setMaxWidthLimit(int maxWidthLimit) {
        this.maxWidthLimit = maxWidthLimit;
        if (this.isRemote()) {
            this.formatDisplayText();
            this.updateComponentTextSize();
        }
        return this;
    }

    public AlignComponentPanelWidget setCenter(boolean centered) {
        this.centered = centered;
        if (this.isRemote()) {
            this.formatDisplayText();
            this.updateComponentTextSize();
        }
        return this;
    }

    public AlignComponentPanelWidget setSplitChar(String delimiter) {
        this.delimiter = delimiter;
        return this;
    }

    public void writeInitialData(FriendlyByteBuf buffer) {
        super.writeInitialData(buffer);
        buffer.writeVarInt(this.lastComponents.size());
        for (Component textComponent : this.lastComponents) {
            buffer.writeComponent(textComponent);
        }
    }

    public void readInitialData(FriendlyByteBuf buffer) {
        super.readInitialData(buffer);
        this.readUpdateInfo(1, buffer);
    }

    public void initWidget() {
        super.initWidget();
        if (this.textProvider != null) {
            this.lastComponents.clear();
            this.textProvider.accept(this.lastComponents);
        }
        if (this.isClientSideWidget && this.isRemote()) {
            this.formatDisplayText();
            this.updateComponentTextSize();
        }
    }

    public void updateScreen() {
        super.updateScreen();
        if (this.isClientSideWidget && this.textProvider != null) {
            List<Component> textBuffer = new ArrayList<>();
            this.textProvider.accept(textBuffer);
            if (!this.lastComponents.equals(textBuffer)) {
                this.lastComponents = textBuffer;
                this.formatDisplayText();
                this.updateComponentTextSize();
            }
        }
    }

    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        if (this.textProvider != null) {
            List<Component> textBuffer = new ArrayList<>();
            this.textProvider.accept(textBuffer);
            if (!this.lastComponents.equals(textBuffer)) {
                this.lastComponents = textBuffer;
                this.writeUpdateInfo(1, (buffer) -> {
                    buffer.writeVarInt(this.lastComponents.size());
                    for (Component textComponent : this.lastComponents) {
                        buffer.writeComponent(textComponent);
                    }
                });
            }
        }
    }

    public void readUpdateInfo(int id, FriendlyByteBuf buffer) {
        if (id == 1) {
            this.lastComponents.clear();
            int count = buffer.readVarInt();
            for (int i = 0; i < count; ++i) {
                this.lastComponents.add(buffer.readComponent());
            }
            this.formatDisplayText();
            this.updateComponentTextSize();
        }
    }

    public void handleClientAction(int id, FriendlyByteBuf buffer) {
        if (id == 1) {
            ClickData clickData = ClickData.readFromBuf(buffer);
            String componentData = buffer.readUtf();
            if (this.clickAction != null) {
                this.clickAction.accept(componentData, clickData);
            }
        } else {
            super.handleClientAction(id, buffer);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void updateComponentTextSize() {
        Font fontRenderer = Minecraft.getInstance().font;
        int totalHeight = this.cachedLines.size() * (9 + this.lineSpacing);
        if (totalHeight > 0) totalHeight -= this.lineSpacing;

        if (this.centered) {
            this.setSize(new Size(this.maxWidthLimit, totalHeight));
        } else {
            int maxStringWidth = 0;
            for (FormattedCharSequence line : this.cachedLines) {
                maxStringWidth = Math.max(fontRenderer.width(line), maxStringWidth);
            }
            this.setSize(
                    new Size(this.maxWidthLimit == 0 ? maxStringWidth : Math.min(this.maxWidthLimit, maxStringWidth),
                            totalHeight));
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void formatDisplayText() {
        Font fontRenderer = Minecraft.getInstance().font;
        int maxTextWidthResult = this.maxWidthLimit == 0 ? Integer.MAX_VALUE : this.maxWidthLimit;
        this.cachedLines = this.lastComponents.stream()
                .flatMap(component -> FormatUtil
                        .formatJustifyComponent(component, maxTextWidthResult, fontRenderer, this.delimiter).stream())
                .toList();
    }

    @OnlyIn(Dist.CLIENT)
    protected @Nullable Style getStyleUnderMouse(double mouseX, double mouseY) {
        Font fontRenderer = Minecraft.getInstance().font;
        Position position = this.getPosition();
        Size size = this.getSize();
        double selectedLine = (mouseY - position.y) / (double) (9 + this.lineSpacing);
        if (this.centered) {
            if (selectedLine >= 0 && selectedLine < this.cachedLines.size()) {
                FormattedCharSequence cacheLine = this.cachedLines.get((int) selectedLine);
                int lineWidth = fontRenderer.width(cacheLine);
                float offsetX = position.x + (size.width - lineWidth) / 2.0f;
                if (mouseX >= offsetX) {
                    int mouseOffset = (int) (mouseX - position.x);
                    return fontRenderer.getSplitter().componentStyleAtWidth(cacheLine, mouseOffset);
                }
            }
        } else if (mouseX >= position.x && selectedLine >= 0 && selectedLine < this.cachedLines.size()) {
            FormattedCharSequence cacheLine = this.cachedLines.get((int) selectedLine);
            int mouseOffset = (int) (mouseX - position.x);
            return fontRenderer.getSplitter().componentStyleAtWidth(cacheLine, mouseOffset);
        }
        return null;
    }

    @OnlyIn(Dist.CLIENT)
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        Style style = this.getStyleUnderMouse(mouseX, mouseY);
        if (style != null && style.getClickEvent() != null) {
            ClickEvent clickEvent = style.getClickEvent();
            String componentText = clickEvent.getValue();
            if (clickEvent.getAction() == ClickEvent.Action.OPEN_URL) {
                if (componentText.startsWith("@!")) {
                    String rawText = componentText.substring(2);
                    ClickData clickData = new ClickData();
                    if (this.clickAction != null) {
                        this.clickAction.accept(rawText, clickData);
                    }
                    this.writeClientAction(1, (buf) -> {
                        clickData.writeToBuf(buf);
                        buf.writeUtf(rawText);
                    });
                } else if (componentText.startsWith("@#")) {
                    String rawText = componentText.substring(2);
                    Util.getPlatform().openUri(rawText);
                }
                playButtonClickSound();
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @OnlyIn(Dist.CLIENT)
    public void drawInForeground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        Style style = this.getStyleUnderMouse(mouseX, mouseY);
        if (style != null && style.getHoverEvent() != null) {
            HoverEvent hoverEvent = style.getHoverEvent();
            Component hoverTips = hoverEvent.getValue(HoverEvent.Action.SHOW_TEXT);
            if (hoverTips != null) {
                this.gui.getModularUIGui().setHoverTooltip(List.of(hoverTips), ItemStack.EMPTY, null, null);
                return;
            }
        }
        super.drawInForeground(graphics, mouseX, mouseY, partialTicks);
    }

    @OnlyIn(Dist.CLIENT)
    public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
        Font fontRenderer = Minecraft.getInstance().font;
        Position position = this.getPosition();
        Size size = this.getSize();

        for (int i = 0; i < this.cachedLines.size(); ++i) {
            FormattedCharSequence cacheLine = this.cachedLines.get(i);
            int x = position.x;
            if (this.centered) {
                int lineWidth = fontRenderer.width(cacheLine);
                x = position.x + (size.width - lineWidth) / 2;
            }
            graphics.drawString(fontRenderer, cacheLine, x, position.y + i * (9 + this.lineSpacing), -1);
        }
    }

    public AlignComponentPanelWidget clickHandler(BiConsumer<String, ClickData> clickAction) {
        this.clickAction = clickAction;
        return this;
    }
}
