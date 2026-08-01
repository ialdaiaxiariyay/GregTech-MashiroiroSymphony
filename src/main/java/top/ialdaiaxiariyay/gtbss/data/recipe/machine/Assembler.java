package top.ialdaiaxiariyay.gtbss.data.recipe.machine;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.Item;

import top.ialdaiaxiariyay.gtbss.GTBSS;

import java.util.function.Consumer;

import static top.ialdaiaxiariyay.gtbss.common.data.GTBSSMachines.*;

public class Assembler {

    private static final GTRecipeType TYPE = GTRecipeTypes.ASSEMBLER_RECIPES;

    private static final Item[] EMITTER = {
            GTItems.EMITTER_LV.asItem(), GTItems.EMITTER_MV.asItem(), GTItems.EMITTER_HV.asItem(),
            GTItems.EMITTER_EV.asItem(), GTItems.EMITTER_IV.asItem(), GTItems.EMITTER_LuV.asItem(),
            GTItems.EMITTER_ZPM.asItem(), GTItems.EMITTER_UV.asItem(), GTItems.EMITTER_UHV.asItem(),
            GTItems.EMITTER_UEV.asItem(), GTItems.EMITTER_UIV.asItem(), GTItems.EMITTER_UXV.asItem(),
            GTItems.EMITTER_OpV.asItem()
    };

    private static final Item[] SENSOR = {
            GTItems.SENSOR_LV.asItem(), GTItems.SENSOR_MV.asItem(), GTItems.SENSOR_HV.asItem(),
            GTItems.SENSOR_EV.asItem(), GTItems.SENSOR_IV.asItem(), GTItems.SENSOR_LuV.asItem(),
            GTItems.SENSOR_ZPM.asItem(), GTItems.SENSOR_UV.asItem(), GTItems.SENSOR_UHV.asItem(),
            GTItems.SENSOR_UEV.asItem(), GTItems.SENSOR_UIV.asItem(), GTItems.SENSOR_UXV.asItem(),
            GTItems.SENSOR_OpV.asItem()
    };

    private static final Material[] DOUBLE_PLATE = {
            GTMaterials.Steel,
            GTMaterials.Aluminium,
            GTMaterials.StainlessSteel,
            GTMaterials.Titanium,
            GTMaterials.TungstenSteel,
            GTMaterials.RhodiumPlatedPalladium,
            GTMaterials.NaquadahAlloy,
            GTMaterials.Darmstadtium,
            GTMaterials.Neutronium
    };

    public static void init(Consumer<FinishedRecipe> consumer) {
        generate1ARecipes(consumer);
        generateHighAmpRecipes(consumer);
    }

    private static void generate1ARecipes(Consumer<FinishedRecipe> consumer) {
        int maxTierIndex = Math.min(DOUBLE_PLATE.length - 1, GTValues.ALL_TIERS.length - 1);

        for (int i = 0; i <= maxTierIndex; i++) {
            int[] tier = GTValues.tiersBetween(GTValues.LV, GTValues.MAX);


            TYPE.recipeBuilder(GTBSS.id("wireless_energy_input_hatch_" + i))
                    .inputItems(TagPrefix.plate, GTMaterials.EnderEye)
                    .inputItems(TagPrefix.plateDouble, DOUBLE_PLATE[i])
                    .inputItems(SENSOR[i])
                    .inputItems(EMITTER[i])
                    .inputItems(GTMachines.ENERGY_INPUT_HATCH[tier[i]])
                    .inputFluids(GTMaterials.SolderingAlloy.getFluid(144))
                    .outputItems(WIRELESS_ENERGY_INPUT_HATCH[tier[i]])
                    .duration(20 * 5)
                    .EUt(GTValues.VA[tier[i]])
                    .save(consumer);


            TYPE.recipeBuilder(GTBSS.id("wireless_energy_output_hatch_" + i))
                    .inputItems(TagPrefix.plate, GTMaterials.EnderEye)
                    .inputItems(TagPrefix.plateDouble, DOUBLE_PLATE[i])
                    .inputItems(SENSOR[i])
                    .inputItems(EMITTER[i])
                    .inputItems(GTMachines.ENERGY_OUTPUT_HATCH[tier[i]])
                    .inputFluids(GTMaterials.SolderingAlloy.getFluid(144))
                    .outputItems(WIRELESS_ENERGY_OUTPUT_HATCH[tier[i]])
                    .duration(20 * 5)
                    .EUt(GTValues.VA[tier[i]])
                    .save(consumer);
        }
    }

    private static void generateHighAmpRecipes(Consumer<FinishedRecipe> consumer) {
        int[] actualTiers = GTValues.tiersBetween(
                GTValues.EV,
                GTCEuAPI.isHighTier() ? GTValues.MAX : GTValues.UHV
        );

        final int EV_INDEX_OFFSET = GTValues.EV - GTValues.LV;

        for (int i = 0; i < actualTiers.length; i++) {
            int tier = actualTiers[i];

            if (GTMachines.ENERGY_INPUT_HATCH_4A[tier] == null) {
                continue;
            }

            int materialIndex = i + EV_INDEX_OFFSET;
            int safeIndex = materialIndex % DOUBLE_PLATE.length;

            TYPE.recipeBuilder(GTBSS.id("wireless_energy_input_hatch_4a_" + i))
                    .inputItems(TagPrefix.plate, GTMaterials.EnderEye)
                    .inputItems(TagPrefix.plateDouble, DOUBLE_PLATE[safeIndex])
                    .inputItems(SENSOR[safeIndex % SENSOR.length])
                    .inputItems(EMITTER[safeIndex % EMITTER.length])
                    .inputItems(GTMachines.ENERGY_INPUT_HATCH_4A[tier]) // 使用 tier 作为索引
                    .inputFluids(GTMaterials.SolderingAlloy.getFluid(144))
                    .outputItems(WIRELESS_ENERGY_INPUT_HATCH_4A[tier])
                    .duration(20 * 5)
                    .EUt(GTValues.VA[tier])
                    .save(consumer);

            if (GTMachines.ENERGY_OUTPUT_HATCH_4A[tier] != null) {
                TYPE.recipeBuilder(GTBSS.id("wireless_energy_output_hatch_4a_" + i))
                        .inputItems(TagPrefix.plate, GTMaterials.EnderEye)
                        .inputItems(TagPrefix.plateDouble, DOUBLE_PLATE[safeIndex])
                        .inputItems(SENSOR[safeIndex % SENSOR.length])
                        .inputItems(EMITTER[safeIndex % EMITTER.length])
                        .inputItems(GTMachines.ENERGY_OUTPUT_HATCH_4A[tier])
                        .inputFluids(GTMaterials.SolderingAlloy.getFluid(144))
                        .outputItems(WIRELESS_ENERGY_OUTPUT_HATCH_4A[tier])
                        .duration(20 * 5)
                        .EUt(GTValues.VA[tier])
                        .save(consumer);
            }

            if (GTMachines.ENERGY_INPUT_HATCH_16A[tier] != null) {
                TYPE.recipeBuilder(GTBSS.id("wireless_energy_input_hatch_16a_" + i))
                        .inputItems(TagPrefix.plate, GTMaterials.EnderEye)
                        .inputItems(TagPrefix.plateDouble, DOUBLE_PLATE[safeIndex])
                        .inputItems(SENSOR[safeIndex % SENSOR.length])
                        .inputItems(EMITTER[safeIndex % EMITTER.length])
                        .inputItems(GTMachines.ENERGY_INPUT_HATCH_16A[tier])
                        .inputFluids(GTMaterials.SolderingAlloy.getFluid(144))
                        .outputItems(WIRELESS_ENERGY_INPUT_HATCH_16A[tier])
                        .duration(20 * 5)
                        .EUt(GTValues.VA[tier])
                        .save(consumer);
            }

            if (GTMachines.ENERGY_OUTPUT_HATCH_16A[tier] != null) {
                TYPE.recipeBuilder(GTBSS.id("wireless_energy_output_hatch_16a_" + i))
                        .inputItems(TagPrefix.plate, GTMaterials.EnderEye)
                        .inputItems(TagPrefix.plateDouble, DOUBLE_PLATE[safeIndex])
                        .inputItems(SENSOR[safeIndex % SENSOR.length])
                        .inputItems(EMITTER[safeIndex % EMITTER.length])
                        .inputItems(GTMachines.ENERGY_OUTPUT_HATCH_16A[tier])
                        .inputFluids(GTMaterials.SolderingAlloy.getFluid(144))
                        .outputItems(WIRELESS_ENERGY_OUTPUT_HATCH_16A[tier])
                        .duration(20 * 5)
                        .EUt(GTValues.VA[tier])
                        .save(consumer);
            }
        }
    }
}