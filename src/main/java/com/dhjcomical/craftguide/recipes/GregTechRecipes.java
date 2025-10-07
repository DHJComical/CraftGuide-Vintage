package com.dhjcomical.craftguide.recipes;

import com.dhjcomical.craftguide.CraftGuideLog;
import com.dhjcomical.craftguide.api.*;
import com.dhjcomical.craftguide.api.slotTypes.*;
import com.dhjcomical.craftguide.api.slotTypes.ExtraSlot;
import com.dhjcomical.craftguide.api.slotTypes.ItemSlot;
import com.dhjcomical.craftguide.api.slotTypes.LiquidSlot;
import com.dhjcomical.craftguide.api.slotTypes.Slot;
import net.minecraft.item.ItemStack;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class GregTechRecipes extends CraftGuideAPIObject implements RecipeProvider {

    // 反射缓存
    private Class<?> recipeMapsClass;
    private Class<?> recipeMapClass;
    private Class<?> recipeClass;
    private Class<?> gtRecipeInputClass;
    private Class<?> metaTileEntityClass;
    private Class<?> gregTechAPIClass;

    private Method getRecipeListMethod;
    private Method isHiddenMethod;
    private Method getInputsMethod;
    private Method getFluidInputsMethod;
    private Method getOutputsMethod;
    private Method getFluidOutputsMethod;
    private Method getInputStacksMethod;
    private Method getInputFluidStackMethod;
    private Method getEUtMethod;
    private Method getDurationMethod;
    private Method getRecipeMapMethod;
    private Method getStackFormMethod;

    private Field mteRegistryField;
    private Field unlocalizedNameField;

    private Object[] recipeMapArray;

    public GregTechRecipes() {
        try {
            initReflection();
        } catch (Exception e) {
            CraftGuideLog.log("Failed to initialize GregTech reflection", true);
            CraftGuideLog.log(e);
        }
    }

    private void initReflection() throws Exception {

        recipeMapsClass = Class.forName("gregtech.api.recipes.RecipeMaps");
        recipeMapClass = Class.forName("gregtech.api.recipes.RecipeMap");
        recipeClass = Class.forName("gregtech.api.recipes.Recipe");
        gtRecipeInputClass = Class.forName("gregtech.api.recipes.ingredients.GTRecipeInput");
        metaTileEntityClass = Class.forName("gregtech.api.metatileentity.MetaTileEntity");
        gregTechAPIClass = Class.forName("gregtech.api.GregTechAPI");

        getRecipeListMethod = recipeMapClass.getMethod("getRecipeList");
        isHiddenMethod = recipeClass.getMethod("isHidden");
        getInputsMethod = recipeClass.getMethod("getInputs");
        getFluidInputsMethod = recipeClass.getMethod("getFluidInputs");
        getOutputsMethod = recipeClass.getMethod("getOutputs");
        getFluidOutputsMethod = recipeClass.getMethod("getFluidOutputs");
        getInputStacksMethod = gtRecipeInputClass.getMethod("getInputStacks");
        getInputFluidStackMethod = gtRecipeInputClass.getMethod("getInputFluidStack");
        getEUtMethod = recipeClass.getMethod("getEUt");
        getDurationMethod = recipeClass.getMethod("getDuration");
        getRecipeMapMethod = metaTileEntityClass.getMethod("getRecipeMap");
        getStackFormMethod = metaTileEntityClass.getMethod("getStackForm");

        mteRegistryField = gregTechAPIClass.getField("MTE_REGISTRY");
        unlocalizedNameField = recipeMapClass.getField("unlocalizedName");

        recipeMapArray = new Object[]{
                getRecipeMapField("MACERATOR_RECIPES"),
                getRecipeMapField("EXTRACTOR_RECIPES"),
                getRecipeMapField("COMPRESSOR_RECIPES"),
                getRecipeMapField("ALLOY_SMELTER_RECIPES"),
                getRecipeMapField("ASSEMBLER_RECIPES"),
                getRecipeMapField("BENDER_RECIPES"),
                getRecipeMapField("CANNER_RECIPES"),
                getRecipeMapField("CIRCUIT_ASSEMBLER_RECIPES"),
                getRecipeMapField("ELECTROLYZER_RECIPES"),
                getRecipeMapField("CENTRIFUGE_RECIPES"),
                getRecipeMapField("WIREMILL_RECIPES"),
                getRecipeMapField("LATHE_RECIPES"),
                getRecipeMapField("FLUID_HEATER_RECIPES"),
                getRecipeMapField("DISTILLERY_RECIPES"),
                getRecipeMapField("CHEMICAL_RECIPES"),
                getRecipeMapField("CUTTER_RECIPES"),
                getRecipeMapField("MIXER_RECIPES"),
                getRecipeMapField("FORGE_HAMMER_RECIPES"),
        };
    }

    private Object getRecipeMapField(String fieldName) {
        try {
            Field field = recipeMapsClass.getField(fieldName);
            return field.get(null);
        } catch (Exception e) {
            CraftGuideLog.log("Failed to get RecipeMap field: " + fieldName);
            return null;
        }
    }

    @Override
    public void generateRecipes(RecipeGenerator generator) {
        if (recipeMapArray == null) return;

        for (Object recipeMap : recipeMapArray) {
            if (recipeMap == null) continue;

            try {
                Collection<?> recipeList = (Collection<?>) getRecipeListMethod.invoke(recipeMap);
                if (recipeList == null || recipeList.isEmpty()) continue;

                addGtMachineRecipes(generator, recipeMap, recipeList);
            } catch (Exception e) {
                try {
                    String mapName = (String) unlocalizedNameField.get(recipeMap);
                    CraftGuideLog.log("Failed to generate recipes for GregTech machine: " + mapName);
                } catch (Exception ex) {
                    CraftGuideLog.log("Failed to generate recipes for GregTech machine: UNKNOWN");
                }
                CraftGuideLog.log(e);
            }
        }
    }

    private void addGtMachineRecipes(RecipeGenerator generator, Object recipeMap, Collection<?> recipeList) throws Exception {
        ItemStack machineStack = getMachineStackFor(recipeMap);

        if (machineStack.isEmpty()) {
            // 尝试从配方输出获取
            for (Object recipe : recipeList) {
                List<?> outputs = (List<?>) getOutputsMethod.invoke(recipe);
                if (outputs != null && !outputs.isEmpty()) {
                    for (Object output : outputs) {
                        if (output instanceof ItemStack && !((ItemStack) output).isEmpty()) {
                            machineStack = (ItemStack) output;
                            break;
                        }
                    }
                    if (!machineStack.isEmpty()) break;
                }
            }

            if (machineStack.isEmpty()) {
                String mapName = (String) unlocalizedNameField.get(recipeMap);
                CraftGuideLog.log("No machine stack found for: " + mapName);
                return;
            }
        }

        for (Object recipe : recipeList) {
            try {
                boolean isHidden = (boolean) isHiddenMethod.invoke(recipe);
                if (isHidden) continue;

                List<?> itemInputs = (List<?>) getInputsMethod.invoke(recipe);
                List<?> fluidInputs = (List<?>) getFluidInputsMethod.invoke(recipe);
                List<?> itemOutputs = (List<?>) getOutputsMethod.invoke(recipe);
                List<?> fluidOutputs = (List<?>) getFluidOutputsMethod.invoke(recipe);

                if ((itemInputs == null || itemInputs.isEmpty()) &&
                        (fluidInputs == null || fluidInputs.isEmpty())) {
                    continue;
                }

                addSingleRecipe(generator, machineStack, recipe, itemInputs, fluidInputs, itemOutputs, fluidOutputs);
            } catch (Exception e) {
                String mapName = (String) unlocalizedNameField.get(recipeMap);
                CraftGuideLog.log("Failed to add recipe for machine: " + mapName);
                CraftGuideLog.log(e);
            }
        }
    }

    private void addSingleRecipe(RecipeGenerator generator, ItemStack machineStack, Object recipe,
                                 List<?> itemInputs, List<?> fluidInputs,
                                 List<?> itemOutputs, List<?> fluidOutputs) throws Exception {

        List<Slot> slots = new ArrayList<>();
        int x = 3;
        int yOffset = 3;

        if (itemInputs != null && !itemInputs.isEmpty()) {
            for (int i = 0; i < itemInputs.size(); i++) {
                slots.add(new ItemSlot(x, yOffset + i * 18, 16, 16));
            }
            x += 18;
        }

        if (fluidInputs != null && !fluidInputs.isEmpty()) {
            for (int i = 0; i < fluidInputs.size(); i++) {
                slots.add(new LiquidSlot(x, yOffset + i * 18));
            }
            x += 18;
        }

        if (x == 3) x = 12;

        ExtraSlot machineSlot = new ExtraSlot(x, yOffset + 18, 16, 16, machineStack);
        machineSlot.clickable();
        machineSlot.showName();
        slots.add(machineSlot);

        slots.add(new TextSlot(x - 8, yOffset + 36));
        slots.add(new TextSlot(x - 8, yOffset + 45));

        x += 22;

        if (itemOutputs != null && !itemOutputs.isEmpty()) {
            for (int i = 0; i < itemOutputs.size(); i++) {
                slots.add(new ItemSlot(x, yOffset + i * 18, 16, 16, true));
            }
            x += 18;
        }

        if (fluidOutputs != null && !fluidOutputs.isEmpty()) {
            for (int i = 0; i < fluidOutputs.size(); i++) {
                slots.add(new LiquidSlot(x, yOffset + i * 18));
            }
            x += 18;
        }

        int maxInputRows = Math.max(
                itemInputs != null ? itemInputs.size() : 0,
                fluidInputs != null ? fluidInputs.size() : 0
        );
        int maxOutputRows = Math.max(
                itemOutputs != null ? itemOutputs.size() : 0,
                fluidOutputs != null ? fluidOutputs.size() : 0
        );
        int height = Math.max(maxInputRows, maxOutputRows) * 18 + 6;
        height = Math.max(height, 60);

        RecipeTemplate template = generator.createRecipeTemplate(slots.toArray(new Slot[0]), machineStack);
        template.setSize(x + 3, height);

        Object[] data = new Object[slots.size()];
        int dataIndex = 0;

        if (itemInputs != null) {
            for (Object gtInput : itemInputs) {
                data[dataIndex++] = getInputStacksMethod.invoke(gtInput);
            }
        }
        if (fluidInputs != null) {
            for (Object gtInput : fluidInputs) {
                data[dataIndex++] = getInputFluidStackMethod.invoke(gtInput);
            }
        }

        data[dataIndex++] = null;

        int eut = (int) getEUtMethod.invoke(recipe);
        int duration = (int) getDurationMethod.invoke(recipe);
        data[dataIndex++] = String.format("%d EU/t", eut);
        data[dataIndex++] = String.format("%.2fs", duration / 20.0);

        if (itemOutputs != null) {
            for (Object stack : itemOutputs) {
                data[dataIndex++] = stack;
            }
        }
        if (fluidOutputs != null) {
            for (Object fluid : fluidOutputs) {
                data[dataIndex++] = fluid;
            }
        }

        generator.addRecipe(template, data);
    }

    private ItemStack getMachineStackFor(Object recipeMap) {
        try {
            Collection<?> mteRegistry = (Collection<?>) mteRegistryField.get(null);
            if (mteRegistry == null) return ItemStack.EMPTY;

            for (Object metaTileEntity : mteRegistry) {
                if (metaTileEntity == null) continue;

                Object entityRecipeMap = getRecipeMapMethod.invoke(metaTileEntity);
                if (entityRecipeMap == recipeMap) {
                    ItemStack stack = (ItemStack) getStackFormMethod.invoke(metaTileEntity);
                    if (stack != null && !stack.isEmpty()) {
                        return stack;
                    }
                }
            }
        } catch (Exception e) {
            try {
                String mapName = (String) unlocalizedNameField.get(recipeMap);
                CraftGuideLog.log("Error getting machine stack for: " + mapName);
            } catch (Exception ex) {
                CraftGuideLog.log("Error getting machine stack for: UNKNOWN");
            }
            CraftGuideLog.log(e);
        }
        return ItemStack.EMPTY;
    }
}