package top.ialdaiaxiariyay.gtms.client.gui.screens;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import guideme.Guide;
import guideme.GuidePage;
import guideme.PageAnchor;
import guideme.PageCollection;
import guideme.color.ConstantColor;
import guideme.color.SymbolicColor;
import guideme.compiler.PageCompiler;
import guideme.compiler.ParsedGuidePage;
import guideme.document.LytRect;
import guideme.document.block.LytDocument;
import guideme.document.block.LytParagraph;
import guideme.extensions.ExtensionCollection;
import guideme.indices.PageIndex;
import guideme.internal.screen.DocumentScreen;
import guideme.navigation.NavigationTree;
import guideme.render.GuiAssets;
import guideme.render.RenderContext;
import top.ialdaiaxiariyay.gtms.GTMS;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

public class MarkdownViewScreen extends DocumentScreen {

    private final LytDocument document;
    private final ResourceLocation baseLocation;

    public MarkdownViewScreen(Component title, ResourceLocation baseLocation) {
        super(title);
        this.baseLocation = baseLocation;
        this.document = loadMarkdown();
    }

    @Override
    protected int getMaxWidth() {
        return Integer.MAX_VALUE;
    }

    @Override
    protected void init() {
        super.init();

        screenRect = new LytRect(0, 0, width, height);
        int margin = 20;
        int docRight = width - margin;
        int docBottom = height - margin;
        LytRect docRect = new LytRect(margin, margin, docRight - margin, docBottom - margin);
        setDocumentRect(docRect);
        updateDocumentLayout();
    }

    private LytDocument loadMarkdown() {
        String langCode = Minecraft.getInstance().getLanguageManager().getSelected();
        if (langCode.isEmpty()) {
            langCode = "en_us";
        }

        String basePath = baseLocation.getPath();
        int extIndex = basePath.lastIndexOf('.');
        String localizedPath;
        if (extIndex > 0) {
            localizedPath = basePath.substring(0, extIndex) + "_" + langCode + basePath.substring(extIndex);
        } else {
            localizedPath = basePath + "_" + langCode;
        }
        ResourceLocation localizedLocation = ResourceLocation.parse(
                baseLocation.getNamespace() + ":" + localizedPath);

        if (!resourceExists(localizedLocation)) {
            return createErrorDocument(
                    "Cannot find Markdown file for language (" + langCode + "): " + localizedLocation);
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(Minecraft.getInstance().getResourceManager().open(localizedLocation)))) {
            String markdownContent = reader.lines().collect(Collectors.joining("\n"));
            ParsedGuidePage parsed = PageCompiler.parse(
                    localizedLocation.getNamespace(),
                    langCode,
                    localizedLocation,
                    markdownContent);

            Guide guide = createGuide(localizedLocation);
            GuidePage guidePage = PageCompiler.compile(guide, ExtensionCollection.empty(), parsed);
            return guidePage.document();
        } catch (Exception e) {
            GTMS.LOGGER.info(e.getMessage());
            return createErrorDocument("Failed to load Markdown file: " + localizedLocation + "\n" + e.getMessage());
        }
    }

    private Guide createGuide(ResourceLocation pageId) {
        return Guide.builder(GTMS.id("markdown"))
                .register(false)
                .startPage(pageId)
                .defaultNamespace(pageId.getNamespace())
                .build();
    }

    private boolean resourceExists(ResourceLocation location) {
        return Minecraft.getInstance().getResourceManager().getResource(location).isPresent();
    }

    private LytDocument createErrorDocument(String message) {
        LytDocument errorDoc = new LytDocument();
        LytParagraph errorParagraph = new LytParagraph();
        errorParagraph.appendText("§c" + message);
        errorDoc.append(errorParagraph);
        return errorDoc;
    }

    @Override
    protected LytDocument getDocument() {
        return document;
    }

    @Override
    protected void scaledRender(GuiGraphics guiGraphics, RenderContext context, int mouseX, int mouseY,
                                float partialTick) {
        context.fillIcon(screenRect, GuiAssets.GUIDE_BACKGROUND, SymbolicColor.GUIDE_SCREEN_BACKGROUND);

        var documentRect = getDocumentRect();
        context.fillRect(documentRect, new ConstantColor(0x40333333));

        ensureDocumentLayout();
        renderDocument(context);

        super.scaledRender(guiGraphics, context, mouseX, mouseY, partialTick);
        renderDocumentTooltip(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void navigateTo(ResourceLocation pageId) {}

    @Override
    public void navigateTo(PageAnchor anchor) {}

    @Override
    public void reloadPage() {}

    @Override
    public PageCollection getGuide() {
        return new PageCollection() {

            @Override
            public <T extends PageIndex> T getIndex(Class<T> indexClass) {
                return null;
            }

            @Override
            public Collection<ParsedGuidePage> getPages() {
                return Collections.emptyList();
            }

            @Override
            public ParsedGuidePage getParsedPage(ResourceLocation id) {
                return null;
            }

            @Override
            public GuidePage getPage(ResourceLocation id) {
                return null;
            }

            @Override
            public byte[] loadAsset(ResourceLocation id) {
                return null;
            }

            @Override
            public NavigationTree getNavigationTree() {
                return new NavigationTree();
            }

            @Override
            public boolean pageExists(ResourceLocation pageId) {
                return false;
            }
        };
    }
}
