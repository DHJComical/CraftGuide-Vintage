package com.dhjcomical.craftguide.recipes;

import com.dhjcomical.craftguide.api.slotTypes.ChanceSlot;
import com.dhjcomical.craftguide.api.slotTypes.EUSlot;
import com.dhjcomical.craftguide.api.slotTypes.ExtraSlot;
import com.dhjcomical.craftguide.api.slotTypes.ItemSlot;
import com.dhjcomical.craftguide.api.slotTypes.Slot;
import ic2.api.item.IC2Items;
import ic2.api.recipe.*;
import net.minecraft.item.ItemStack;
import com.dhjcomical.craftguide.api.*;
import com.dhjcomical.craftguide.api.slotTypes.*;

import java.util.*;

public class IC2ExperimentalRecipes implements RecipeProvider {

    public IC2ExperimentalRecipes() {
        StackInfo.addSource(new IC2GeneratorFuel());
        StackInfo.addSource(new IC2Power());
        StackInfo.addSource(new IC2ExperimentalAmplifiers());
    }

    @Override
    public void generateRecipes(RecipeGenerator generator) {
        if (IC2Items.getItem("te", "macerator") == null) {
            return;
        }

        addMachineRecipes(generator, IC2Items.getItem("te", "macerator"), Recipes.macerator, 2, 800);
        addMachineRecipes(generator, IC2Items.getItem("te", "extractor"), Recipes.extractor, 2, 400);
        addMachineRecipes(generator, IC2Items.getItem("te", "compressor"), Recipes.compressor, 2, 400);
        addMachineRecipes(generator, IC2Items.getItem("te", "centrifuge"), Recipes.centrifuge, 5, 2500);
        addMachineRecipes(generator, IC2Items.getItem("te", "block_cutter"), Recipes.blockcutter, 8, 1000);
        addMachineRecipes(generator, IC2Items.getItem("te", "blast_furnace"), Recipes.blastfurnace, 128, 30000);
        addMachineRecipes(generator, IC2Items.getItem("te", "metal_former"), Recipes.metalformerExtruding, 10, 400);
        addMachineRecipes(generator, IC2Items.getItem("te", "metal_former"), Recipes.metalformerCutting, 10, 400);
        addMachineRecipes(generator, IC2Items.getItem("te", "metal_former"), Recipes.metalformerRolling, 10, 400);
        addMachineRecipes(generator, IC2Items.getItem("te", "ore_washing_plant"), Recipes.oreWashing, 16, 2000);

        addScrapboxOutput(generator, IC2Items.getItem("crafting", "scrap_box"), Recipes.scrapboxDrops);
    }

    private void addMachineRecipes(RecipeGenerator generator, ItemStack machineStack, IMachineRecipeManager<IRecipeInput, Collection<ItemStack>, ItemStack> recipeManager, int eut, int totalEU) {
        if (recipeManager == null || !recipeManager.isIterable()) return;

        Set<String> addedRecipeSignatures = new HashSet<>();
        int maxOutput = 1;
        for (MachineRecipe<IRecipeInput, Collection<ItemStack>> recipe : recipeManager.getRecipes()) {
            if (recipe.getOutput() != null) {
                maxOutput = Math.max(maxOutput, recipe.getOutput().size());
            }
        }

        List<Slot> recipeSlots = new ArrayList<>();
        recipeSlots.add(new ItemSlot(3, 21, 16, 16, true).drawOwnBackground());
        recipeSlots.add(new ExtraSlot(23, 30, 16, 16, machineStack).clickable().showName().setSlotType(SlotType.MACHINE_SLOT));
        recipeSlots.add(new EUSlot(23, 12).setConstantPacketSize(eut).setConstantEUValue(-totalEU));
        for (int i = 0; i < maxOutput; i++) {
            int x = 41 + (i / 2) * 18;
            int y = 12 + (i % 2) * 18;
            recipeSlots.add(new ItemSlot(x, y, 16, 16, true).setSlotType(SlotType.OUTPUT_SLOT).drawOwnBackground());
        }

        RecipeTemplate template = generator.createRecipeTemplate(recipeSlots.toArray(new Slot[0]), machineStack);

        for (MachineRecipe<IRecipeInput, Collection<ItemStack>> recipe : recipeManager.getRecipes()) {
            IRecipeInput recipeInput = recipe.getInput();
            Collection<ItemStack> recipeOutput = recipe.getOutput();

            if (recipeInput == null || recipeOutput == null || recipeInput.getInputs().isEmpty() || recipeOutput.isEmpty()) {
                continue;
            }

            String recipeSignature = generateRecipeSignature(recipeInput, recipeOutput);

            if (addedRecipeSignatures.add(recipeSignature)) {
                Object processedInput = processRecipeInput(recipeInput);
                if (processedInput == null) continue;

                Object[] recipeContents = new Object[recipeSlots.size()];
                recipeContents[0] = processedInput;

                int outputIndex = 0;
                for (ItemStack outputStack : recipeOutput) {
                    if (outputIndex < maxOutput) {
                        recipeContents[outputIndex + 3] = outputStack;
                        outputIndex++;
                    }
                }
                generator.addRecipe(template, recipeContents);
            }
        }
    }

    /**
     * FIX #2: Creates a robust, order-independent signature for a recipe to prevent duplicates.
     */
    private String generateRecipeSignature(IRecipeInput input, Collection<ItemStack> output) {
        StringBuilder signature = new StringBuilder();

        // Sort inputs by registry name and then metadata to handle order variations
        List<ItemStack> sortedInputs = new ArrayList<>(input.getInputs());
        sortedInputs.sort(Comparator.comparing((ItemStack s) -> s.getItem().getRegistryName().toString()).thenComparingInt(ItemStack::getMetadata));

        for (ItemStack stack : sortedInputs) {
            signature.append(stack.getItem().getRegistryName().toString()).append(":").append(stack.getMetadata()).append(",");
        }
        signature.append("@").append(input.getAmount());
        signature.append("->");

        // Sort outputs as well for consistency
        List<ItemStack> sortedOutputs = new ArrayList<>(output);
        sortedOutputs.sort(Comparator.comparing((ItemStack s) -> s.getItem().getRegistryName().toString()).thenComparingInt(ItemStack::getMetadata));

        for (ItemStack stack : sortedOutputs) {
            signature.append(stack.getItem().getRegistryName().toString()).append(":").append(stack.getMetadata()).append("x").append(stack.getCount()).append(",");
        }

        return signature.toString();
    }

    private Object processRecipeInput(IRecipeInput input) {
        List<ItemStack> inputStacks = input.getInputs();
        if (inputStacks.isEmpty()) return null;

        for (ItemStack stack : inputStacks) {
            if (!stack.isEmpty()) {
                stack.setCount(input.getAmount());
            }
        }

        if (inputStacks.size() == 1) {
            return inputStacks.get(0);
        } else {
            return inputStacks;
        }
    }

    private void addScrapboxOutput(RecipeGenerator generator, ItemStack scrapbox, IScrapboxManager scrapboxDrops) {
        if (scrapbox == null || scrapbox.isEmpty() || scrapboxDrops == null) return;
        Slot[] recipeSlots = {
                new ItemSlot(12, 21, 16, 16, true).setSlotType(SlotType.INPUT_SLOT).drawOwnBackground(),
                new ExtraSlot(31, 21, 16, 16, scrapbox).clickable().showName().setSlotType(SlotType.MACHINE_SLOT),
                new ChanceSlot(50, 21, 16, 16).setSlotType(SlotType.OUTPUT_SLOT).drawOwnBackground(),
        };
        RecipeTemplate template = generator.createRecipeTemplate(recipeSlots, scrapbox);
        for (Map.Entry<ItemStack, Float> entry : scrapboxDrops.getDrops().entrySet()) {
            ItemStack output = entry.getKey();
            if (output == null || output.isEmpty()) continue;
            Object[] chanceOutput = new Object[] { output, (int)(entry.getValue() * 100_000) };
            Object[] recipeContents = new Object[3];
            recipeContents[0] = scrapbox;
            recipeContents[2] = chanceOutput;
            generator.addRecipe(template, recipeContents);
        }
    }
}