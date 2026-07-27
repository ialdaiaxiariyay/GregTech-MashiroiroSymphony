package top.ialdaiaxiariyay.gtbss.client.renderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import org.jetbrains.annotations.NotNull;
import top.ialdaiaxiariyay.gtbss.GTBSS;
import top.ialdaiaxiariyay.gtbss.common.data.GTBSSItems;
import top.ialdaiaxiariyay.gtbss.common.entity.SpearEntity;

public class SpearRenderer extends EntityRenderer<SpearEntity> {

    public static final ResourceLocation SPEAR_LOCATION = GTBSS.id("textures/item/gungnir.png");

    public SpearRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(@NotNull SpearEntity entity, float entityYaw, float partialTicks, @NotNull PoseStack poseStack,
                       @NotNull MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        float yRot = Mth.lerp(partialTicks, entity.yRotO, entity.getYRot()) + 90.0F;
        float xRot = Mth.lerp(partialTicks, entity.xRotO, entity.getXRot()) - 90.0F;
        poseStack.mulPose(Axis.YP.rotationDegrees(yRot));
        poseStack.mulPose(Axis.ZP.rotationDegrees(xRot));

        poseStack.scale(1.5f, 1.5f, 1.5f);
        ItemStack stack = new ItemStack(GTBSSItems.GUNGNIR.get());
        Minecraft.getInstance().getItemRenderer().renderStatic(
                stack,
                ItemDisplayContext.NONE,
                packedLight,
                OverlayTexture.NO_OVERLAY,
                poseStack,
                buffer,
                entity.level(),
                0);

        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull SpearEntity entity) {
        return SPEAR_LOCATION;
    }
}
