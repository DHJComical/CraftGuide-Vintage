package com.dhjcomical.craftguide.recipes;

import ic2.api.item.IC2Items;
import ic2.api.recipe.*;

import java.util.*;

import net.minecraft.item.ItemStack;

import com.dhjcomical.craftguide.api.CraftGuideAPIObject;
import com.dhjcomical.craftguide.api.slotTypes.EUSlot;
import com.dhjcomical.craftguide.api.slotTypes.ExtraSlot;
import com.dhjcomical.craftguide.api.slotTypes.ItemSlot;
import com.dhjcomical.craftguide.api.RecipeGenerator;
import com.dhjcomical.craftguide.api.RecipeProvider;
import com.dhjcomical.craftguide.api.RecipeTemplate;
import com.dhjcomical.craftguide.api.slotTypes.Slot;
import com.dhjcomical.craftguide.api.SlotType;
import com.dhjcomical.craftguide.api.StackInfo;

public class IC2ExperimentalRecipes extends CraftGuideAPIObject implements RecipeProvider {

    public interface AdditionalMachines {
        Object[] extraMacerators();
        Object[] extraExtractors();
        Object[] extraCompressors();
    }

    public static List<AdditionalMachines> additionalMachines = new ArrayList<>();

    public IC2ExperimentalRecipes() {
        // 构造函数中的依赖注入保持不变
        StackInfo.addSource(new IC2GeneratorFuel());
        StackInfo.addSource(new IC2Power());
        StackInfo.addSource(new IC2ExperimentalAmplifiers());
    }

    @Override
    public void generateRecipes(RecipeGenerator generator) {
        if (IC2Items.getItem("te", "macerator") == null) {
            return;
        }

        addMachineRecipes(generator, IC2Items.getItem("te", "macerator"), getMacerator(), Recipes.macerator);
        addMachineRecipes(generator, IC2Items.getItem("te", "extractor"), getExtractor(), Recipes.extractor);
        addMachineRecipes(generator, IC2Items.getItem("te", "compressor"), getCompressor(), Recipes.compressor);
        addMachineRecipes(generator, IC2Items.getItem("te", "centrifuge"), Recipes.centrifuge);
        addMachineRecipes(generator, IC2Items.getItem("te", "block_cutter"), Recipes.blockcutter);
        addMachineRecipes(generator, IC2Items.getItem("te", "blast_furnace"), Recipes.blastfurnace);
        addMachineRecipes(generator, IC2Items.getItem("te", "metal_former"), Recipes.metalformerExtruding);
        addMachineRecipes(generator, IC2Items.getItem("te", "metal_former"), Recipes.metalformerCutting);
        addMachineRecipes(generator, IC2Items.getItem("te", "metal_former"), Recipes.metalformerRolling);
        addMachineRecipes(generator, IC2Items.getItem("te", "ore_washing_plant"), Recipes.oreWashing);

        addScrapboxOutput(generator, IC2Items.getItem("crafting", "scrap_box"), Recipes.scrapboxDrops);
    }

    private Object getMacerator() {
        ArrayList<Object> macerator = new ArrayList<>();
        macerator.add(IC2Items.getItem("te", "macerator"));
        for(AdditionalMachines additional: additionalMachines) {
            Object[] machines = additional.extraMacerators();
            if(machines != null) {
                macerator.addAll(Arrays.asList(machines));
            }
        }
        return macerator;
    }

    private Object getExtractor() {
        ArrayList<Object> extractor = new ArrayList<>();
        extractor.add(IC2Items.getItem("te", "extractor"));

        for(AdditionalMachines additional: additionalMachines) {
            Object[] machines = additional.extraExtractors();
            if(machines != null) {
                extractor.addAll(Arrays.asList(machines));
            }
        }
        return extractor;
    }

    private Object getCompressor() {
        ArrayList<Object> compressor = new ArrayList<>();
        compressor.add(IC2Items.getItem("te", "compressor"));

        for(AdditionalMachines additional: additionalMachines) {
            Object[] machines = additional.extraCompressors();
            if(machines != null) {
                compressor.addAll(Arrays.asList(machines));
            }
        }
        return compressor;
    }

    private void addMachineRecipes(RecipeGenerator generator, ItemStack type, IMachineRecipeManager recipeManager) {
        addMachineRecipes(generator, type, type, recipeManager);
    }

    private void addMachineRecipes(RecipeGenerator generator, ItemStack type, Object machine, IMachineRecipeManager recipeManager) {
        addMachineRecipes(generator, type, machine, recipeManager, 2, 800);
    }


    private void addMachineRecipes(RecipeGenerator generator, ItemStack type, Object machine, IMachineRecipeManager<IRecipeInput, Collection<ItemStack>, ItemStack> recipeManager, int eut, int totalEU) {

        if (recipeManager == null || !recipeManager.isIterable()) return;

        int maxOutput = 1;
        for (MachineRecipe<IRecipeInput, Collection<ItemStack>> recipe : recipeManager.getRecipes()) {
            Collection<ItemStack> output = recipe.getOutput();
            if (recipe.getOutput() != null) {
                int currentSize = 0;
                for (ItemStack stack : recipe.getOutput()) {
                    currentSize++;
                }
                maxOutput = Math.max(maxOutput, currentSize);
            }
        }

        int columns = (maxOutput + 1) / 2;
        Slot[] recipeSlots = new Slot[maxOutput + 3];

        recipeSlots[0] = new ItemSlot(columns > 1 ? 3 : 12, 21, 16, 16, true).drawOwnBackground();
        recipeSlots[1] = new ExtraSlot(columns > 1 ? 23 : 31, 30, 16, 16, machine).clickable().showName().setSlotType(SlotType.MACHINE_SLOT);
        recipeSlots[2] = new EUSlot(columns > 1 ? 23 : 31, 12).setConstantPacketSize(eut).setConstantEUValue(-totalEU);

        for (int i = 0; i < maxOutput / 2; i++) {
            recipeSlots[i * 2 + 3] = new ItemSlot((columns > 1 ? 41 : 50) + i * 18, 12, 16, 16, true).setSlotType(SlotType.OUTPUT_SLOT).drawOwnBackground();
            recipeSlots[i * 2 + 4] = new ItemSlot((columns > 1 ? 41 : 50) + i * 18, 30, 16, 16, true).setSlotType(SlotType.OUTPUT_SLOT).drawOwnBackground();
        }

        if ((maxOutput & 1) == 1) {
            recipeSlots[columns * 2 + 1] = new ItemSlot((columns > 1 ? 23 : 32) + columns * 18, 21, 16, 16, true).setSlotType(SlotType.OUTPUT_SLOT).drawOwnBackground();
        }

        RecipeTemplate template = generator.createRecipeTemplate(recipeSlots, type);

        for (MachineRecipe<IRecipeInput, Collection<ItemStack>> recipe : recipeManager.getRecipes()) {
            IRecipeInput recipeInput = recipe.getInput();
            Collection<ItemStack> recipeOutput = recipe.getOutput();

            if (recipeInput == null || recipeOutput == null || recipeInput.getInputs().isEmpty()) {
                continue;
            }

            List<ItemStack> inputs = new ArrayList<>();
            for (ItemStack s : recipeInput.getInputs()) {
                ItemStack stack = s.copy();
                stack.setCount(recipeInput.getAmount());
                inputs.add(stack);
            }

            Object[] recipeContents = new Object[maxOutput + 3];
            recipeContents[0] = inputs;
            recipeContents[1] = machine;
            recipeContents[2] = null;
            List<ItemStack> outputList = new ArrayList<>(recipeOutput);

            for (int i = 0; i < Math.min(maxOutput, outputList.size()); i++) {
                recipeContents[i + 3] = outputList.get(i);
            }
            generator.addRecipe(template, recipeContents);
        }
    }

    private void addScrapboxOutput(RecipeGenerator generator, ItemStack scrapbox, IScrapboxManager scrapboxDrops) {
        Slot[] recipeSlots = { /* ... */ };
        RecipeTemplate template = generator.createRecipeTemplate(recipeSlots, scrapbox);

        for (Map.Entry<ItemStack, Float> entry : scrapboxDrops.getDrops().entrySet()) {
            Object[] recipeContents = new Object[]{
                    scrapbox,
                    new Object[]{
                            entry.getKey(),
                            (int) (entry.getValue() * 100000),
                    },
            };
            generator.addRecipe(template, recipeContents);
        }
    }

}