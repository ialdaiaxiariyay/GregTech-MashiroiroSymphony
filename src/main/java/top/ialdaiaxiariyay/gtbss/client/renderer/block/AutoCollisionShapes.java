package top.ialdaiaxiariyay.gtbss.client.renderer.block;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class AutoCollisionShapes {

    private static final Map<ResourceLocation, VoxelShape> SHAPE_CACHE = new HashMap<>();
    private static final Gson GSON = new Gson();

    public static void precompute(ResourceLocation modelLocation) {
        SHAPE_CACHE.computeIfAbsent(modelLocation, AutoCollisionShapes::computeShape);
    }

    public static VoxelShape getShape(ResourceLocation modelLocation) {
        return SHAPE_CACHE.getOrDefault(modelLocation, Shapes.block());
    }

    private static VoxelShape computeShape(ResourceLocation modelLocation) {
        String path = "/assets/" + modelLocation.getNamespace() + "/models/block/" + modelLocation.getPath() + ".json";
        try (InputStream in = AutoCollisionShapes.class.getResourceAsStream(path)) {
            if (in == null) return Shapes.block();

            JsonObject root = GSON.fromJson(new InputStreamReader(in), JsonObject.class);
            JsonArray elements = root.getAsJsonArray("elements");
            if (elements == null || elements.isEmpty()) return Shapes.block();

            VoxelShape result = Shapes.empty();
            for (JsonElement elem : elements) {
                VoxelShape elementShape = createElementShape(elem.getAsJsonObject());
                if (elementShape != null) {
                    result = Shapes.or(result, elementShape);
                }
            }
            return result.isEmpty() ? Shapes.block() : result.optimize();
        } catch (Exception e) {
            return Shapes.block();
        }
    }

    private static VoxelShape createElementShape(JsonObject element) {
        JsonArray fromArr = element.getAsJsonArray("from");
        JsonArray toArr = element.getAsJsonArray("to");
        double fromX = fromArr.get(0).getAsDouble();
        double fromY = fromArr.get(1).getAsDouble();
        double fromZ = fromArr.get(2).getAsDouble();
        double toX = toArr.get(0).getAsDouble();
        double toY = toArr.get(1).getAsDouble();
        double toZ = toArr.get(2).getAsDouble();

        Vec3[] vertices = new Vec3[] {
                new Vec3(fromX, fromY, fromZ), new Vec3(fromX, fromY, toZ),
                new Vec3(fromX, toY, fromZ), new Vec3(fromX, toY, toZ),
                new Vec3(toX, fromY, fromZ), new Vec3(toX, fromY, toZ),
                new Vec3(toX, toY, fromZ), new Vec3(toX, toY, toZ)
        };

        if (element.has("rotation")) {
            JsonObject rot = element.getAsJsonObject("rotation");
            Vec3 origin = parseVec3(rot.get("origin"));
            String axis = rot.get("axis").getAsString();
            double angle = rot.get("angle").getAsDouble();
            boolean rescale = rot.has("rescale") && rot.get("rescale").getAsBoolean();

            for (int i = 0; i < vertices.length; i++) {
                vertices[i] = rotateVertex(vertices[i], origin, axis, angle, rescale);
            }
        }

        boolean hasNegative = false;
        for (Vec3 v : vertices) {
            if (v.x < 0 || v.y < 0 || v.z < 0) {
                hasNegative = true;
                break;
            }
        }
        double offset = hasNegative ? 8.0 : 0.0;

        double minX = Double.POSITIVE_INFINITY, minY = Double.POSITIVE_INFINITY, minZ = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY, maxY = Double.NEGATIVE_INFINITY, maxZ = Double.NEGATIVE_INFINITY;
        for (Vec3 v : vertices) {
            double vx = v.x + offset, vy = v.y + offset, vz = v.z + offset;
            vx = Math.max(0.0, Math.min(16.0, vx));
            vy = Math.max(0.0, Math.min(16.0, vy));
            vz = Math.max(0.0, Math.min(16.0, vz));
            minX = Math.min(minX, vx);
            maxX = Math.max(maxX, vx);
            minY = Math.min(minY, vy);
            maxY = Math.max(maxY, vy);
            minZ = Math.min(minZ, vz);
            maxZ = Math.max(maxZ, vz);
        }

        if (minX < maxX && minY < maxY && minZ < maxZ) {
            return Shapes.box(minX / 16.0, minY / 16.0, minZ / 16.0,
                    maxX / 16.0, maxY / 16.0, maxZ / 16.0);
        }
        return null;
    }

    private static Vec3 rotateVertex(Vec3 v, Vec3 origin, String axis, double angle, boolean rescale) {
        Vec3 rel = v.subtract(origin);
        double rad = Math.toRadians(rescale ? angle : -angle);
        Vec3 rotated = switch (axis) {
            case "x" -> new Vec3(rel.x,
                    rel.y * Math.cos(rad) - rel.z * Math.sin(rad),
                    rel.y * Math.sin(rad) + rel.z * Math.cos(rad));
            case "y" -> new Vec3(rel.x * Math.cos(rad) - rel.z * Math.sin(rad),
                    rel.y,
                    rel.x * Math.sin(rad) + rel.z * Math.cos(rad));
            case "z" -> new Vec3(rel.x * Math.cos(rad) - rel.y * Math.sin(rad),
                    rel.x * Math.sin(rad) + rel.y * Math.cos(rad),
                    rel.z);
            default -> rel;
        };
        return rotated.add(origin);
    }

    private static Vec3 parseVec3(JsonElement e) {
        JsonArray arr = e.getAsJsonArray();
        return new Vec3(arr.get(0).getAsDouble(), arr.get(1).getAsDouble(), arr.get(2).getAsDouble());
    }
}
