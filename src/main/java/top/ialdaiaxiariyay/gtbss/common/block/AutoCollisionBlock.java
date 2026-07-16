package top.ialdaiaxiariyay.gtbss.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.jetbrains.annotations.NotNull;
import top.ialdaiaxiariyay.gtbss.GTBSS;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class AutoCollisionBlock extends Block {

    private final ResourceLocation modelLocation;
    private final AtomicReference<VoxelShape> cachedShape = new AtomicReference<>();

    public AutoCollisionBlock(Properties properties, ResourceLocation modelLocation) {
        super(properties);
        this.modelLocation = modelLocation;
    }

    @Override
    public @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos,
                                        CollisionContext context) {
        VoxelShape shape = cachedShape.get();
        if (shape == null) {
            shape = computeShapeFromModel();
            cachedShape.set(shape);
        }
        return shape;
    }

    private VoxelShape computeShapeFromModel() {
        String path = String.format("/assets/%s/models/block/%s.json",
                modelLocation.getNamespace(),
                modelLocation.getPath());

        try (InputStream inputStream = getClass().getResourceAsStream(path)) {
            if (inputStream == null) {
                GTBSS.LOGGER.error("Model file not found: {} - using full block collision.", path);
                return Shapes.block();
            }

            JsonObject root = JsonParser.parseReader(new InputStreamReader(inputStream)).getAsJsonObject();
            JsonArray elements = root.getAsJsonArray("elements");
            if (elements == null || elements.isEmpty()) {
                return Shapes.block();
            }

            double minX = Double.POSITIVE_INFINITY;
            double minY = Double.POSITIVE_INFINITY;
            double minZ = Double.POSITIVE_INFINITY;
            double maxX = Double.NEGATIVE_INFINITY;
            double maxY = Double.NEGATIVE_INFINITY;
            double maxZ = Double.NEGATIVE_INFINITY;
            boolean hasNegative = false;

            for (JsonElement elementElem : elements) {
                JsonObject element = elementElem.getAsJsonObject();
                JsonArray from = element.getAsJsonArray("from");
                JsonArray to = element.getAsJsonArray("to");

                double fromX = from.get(0).getAsDouble();
                double fromY = from.get(1).getAsDouble();
                double fromZ = from.get(2).getAsDouble();
                double toX = to.get(0).getAsDouble();
                double toY = to.get(1).getAsDouble();
                double toZ = to.get(2).getAsDouble();

                if (fromX < 0 || fromY < 0 || fromZ < 0 || toX < 0 || toY < 0 || toZ < 0) {
                    hasNegative = true;
                }

                minX = Math.min(minX, fromX);
                minY = Math.min(minY, fromY);
                minZ = Math.min(minZ, fromZ);
                maxX = Math.max(maxX, toX);
                maxY = Math.max(maxY, toY);
                maxZ = Math.max(maxZ, toZ);
            }

            double worldMinX, worldMinY, worldMinZ, worldMaxX, worldMaxY, worldMaxZ;
            if (hasNegative) {
                worldMinX = minX + 8.0;
                worldMinY = minY + 8.0;
                worldMinZ = minZ + 8.0;
                worldMaxX = maxX + 8.0;
                worldMaxY = maxY + 8.0;
                worldMaxZ = maxZ + 8.0;
            } else {
                worldMinX = minX;
                worldMinY = minY;
                worldMinZ = minZ;
                worldMaxX = maxX;
                worldMaxY = maxY;
                worldMaxZ = maxZ;
            }

            worldMinX = Math.max(0.0, Math.min(16.0, worldMinX));
            worldMinY = Math.max(0.0, Math.min(16.0, worldMinY));
            worldMinZ = Math.max(0.0, Math.min(16.0, worldMinZ));
            worldMaxX = Math.max(0.0, Math.min(16.0, worldMaxX));
            worldMaxY = Math.max(0.0, Math.min(16.0, worldMaxY));
            worldMaxZ = Math.max(0.0, Math.min(16.0, worldMaxZ));

            if (worldMinX < worldMaxX && worldMinY < worldMaxY && worldMinZ < worldMaxZ) {
                double widthX = worldMaxX - worldMinX;
                double widthY = worldMaxY - worldMinY;
                double widthZ = worldMaxZ - worldMinZ;
                double originX = worldMinX / 16.0;
                double originY = worldMinY / 16.0;
                double originZ = worldMinZ / 16.0;
                double sizeX = widthX / 16.0;
                double sizeY = widthY / 16.0;
                double sizeZ = widthZ / 16.0;
                return Shapes.box(originX, originY, originZ, originX + sizeX, originY + sizeY, originZ + sizeZ);
            } else {
                return Shapes.block();
            }
        } catch (Exception e) {
            GTBSS.LOGGER.error("Failed to parse model file for collision: {}", path);
            GTBSS.LOGGER.error(e.getMessage());
            return Shapes.block();
        }
    }

    @Override
    public @NotNull VoxelShape getCollisionShape(@NotNull BlockState state, @NotNull BlockGetter level,
                                                 @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return getShape(state, level, pos, context);
    }

    @Override
    public @NotNull VoxelShape getInteractionShape(@NotNull BlockState state, @NotNull BlockGetter level,
                                                   @NotNull BlockPos pos) {
        return getShape(state, level, pos, CollisionContext.empty());
    }
}
