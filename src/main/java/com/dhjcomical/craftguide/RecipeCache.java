package com.dhjcomical.craftguide;

import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import com.dhjcomical.craftguide.api.BasicRecipeFilter;
import com.dhjcomical.craftguide.api.CraftGuideRecipe;
import com.dhjcomical.craftguide.api.CraftGuideRecipeExtra1;
import com.dhjcomical.craftguide.api.ItemFilter;
import com.dhjcomical.craftguide.api.RecipeFilter;
import com.dhjcomical.craftguide.api.RecipeGenerator;
import com.dhjcomical.craftguide.api.RecipeProvider;
import com.dhjcomical.craftguide.api.SlotType;
import com.dhjcomical.craftguide.client.ui.IRecipeCacheListener;
import com.dhjcomical.craftguide.filters.NoItemFilter;
import com.dhjcomical.craftguide.itemtype.ItemType;

public class RecipeCache
{
	private SortedSet<ItemType> craftingTypes = new TreeSet<>();
	private Map<ItemType, List<CraftGuideRecipe>> craftResults = new HashMap<>();
	private List<CraftGuideRecipe> typeResults;
	private List<CraftGuideRecipe> filteredResults = new ArrayList<>();
	private RecipeGeneratorImplementation generator = RecipeGeneratorImplementation.instance;
	private ItemFilter filterItem = null;
	private Deque<ItemFilter> filterHistory = new LinkedList<>();
	private Deque<ItemFilter> filterHistoryForwards = new LinkedList<>();
	private List<IRecipeCacheListener> listeners = new LinkedList<>();
	private Set<ItemType> currentTypes = null;
	private SortedSet<ItemType> allItems = new TreeSet<>();
	private boolean firstReset = true;

	/* Effectively Runnable, but having a separate type should make code searches
	 * more useful for identifying relevant implementations
	 */
	public static interface Task
	{
		void run();
	}

	private static Task nextTask = null;
	private static Thread taskThread = null;
	static
	{
		taskThread = new Thread(
			new Runnable()
			{
				@Override
				public void run()
				{
					while(true)
					{
						synchronized(taskThread)
						{
							if(nextTask == null)
							{
								try
								{
									taskThread.wait();
								}
								catch(InterruptedException e)
								{
								}
							}
						}

						if(nextTask != null)
						{
							Task current = nextTask;

							try
							{
								current.run();
							}
							catch(Exception e)
							{
								CraftGuideLog.log(e, "Exception encountered in task thread", true);
							}

							if(nextTask == current)
								nextTask = null;
						}
					}
				}
			},
			"CraftGuide Asynchronous Recipe Processing (CARP)"
		);
		taskThread.setDaemon(true);
		taskThread.start();
	}

	public static void runTask(Task task)
	{
		if(CraftGuide.useWorkerThread)
		{
			synchronized(taskThread)
			{
				nextTask = task;
				taskThread.notify();
			}
		}
		else
		{
			task.run();
		}
	}

	public static boolean hasActiveTask()
	{
		return nextTask != null;
	}

	public RecipeCache()
	{
		reset();
	}

    public synchronized void reset() {
        CraftGuide.needsRecipeRefresh = false;
        runTask(() -> {
            CraftGuideLog.log("(re)loading recipe list...");
            Map<ItemStack, List<CraftGuideRecipe>> rawRecipes = generateRecipes();
            Map<ItemType, List<CraftGuideRecipe>> newCraftResults = new HashMap<>();

            for (Map.Entry<ItemStack, List<CraftGuideRecipe>> entry : rawRecipes.entrySet()) {
                ItemType type = ItemType.getInstance(entry.getKey());
                if (type != null) {
                    newCraftResults.computeIfAbsent(type, k -> new ArrayList<>()).addAll(entry.getValue());
                }
            }

            SortedSet<ItemType> newAllItems = generateAllItemList(newCraftResults);
            SortedSet<ItemType> newCraftingTypes = new TreeSet<>(newCraftResults.keySet());

            if (firstReset) {
                currentTypes = new HashSet<>(newCraftingTypes);
                for (ItemStack stack : generator.disabledTypes) {
                    currentTypes.remove(ItemType.getInstance(stack));
                }
                firstReset = false;
            }

            craftResults = newCraftResults;
            allItems = newAllItems;
            craftingTypes = newCraftingTypes;

            setTypes(currentTypes);
            listeners.forEach(listener -> listener.onReset(RecipeCache.this));
        });
    }

    private static SortedSet<ItemType> generateAllItemList(Map<ItemType, List<CraftGuideRecipe>> craftResults) {
        SortedSet<ItemType> allItems = new TreeSet<>();

        Map<String, ItemType> oreDictCache = new HashMap<>();

        for (List<CraftGuideRecipe> recipeList : craftResults.values()) {
            for (CraftGuideRecipe recipe : recipeList) {
                for (Object item : recipe.getItems()) {
                    if (item == null) continue;

                    if (item instanceof List) {
                        List<?> list = (List<?>) item;
                        String oreDictName = ForgeExtensions.getOreDictionaryName(list);

                        if (oreDictName != null) {
                            if (!oreDictCache.containsKey(oreDictName)) {
                                ItemType oreType = ItemType.getInstance(item);
                                if (oreType != null) {
                                    allItems.add(oreType);
                                    oreDictCache.put(oreDictName, oreType);
                                }
                            }
                        } else {
                            ItemType itemType = ItemType.getInstance(item);
                            if (itemType != null) {
                                allItems.add(itemType);
                            }
                        }
                    } else {
                        ItemType itemType = ItemType.getInstance(item);
                        if (itemType != null) {
                            allItems.add(itemType);
                        }
                    }
                }
            }
        }

        return allItems;
    }

	private static void removeUselessDuplicates(SortedSet<ItemType> allItems)
	{
		HashMap<Item, Set<ItemType>> items = new HashMap<>();

		for(ItemType type: allItems)
		{
			if(type.getStack() instanceof ItemStack)
			{
				Item item = ((ItemStack)type.getStack()).getItem();
				Set<ItemType> set = items.get(item);

				if(set == null)
				{
					set = new HashSet<>();
					items.put(item, set);
				}

				set.add(type);
			}
			else if(type.getStack() instanceof List)
			{
				for(Object o: (List<?>)type.getStack())
				{
					if(o instanceof ItemStack)
					{
						Item item = ((ItemStack)o).getItem();
						Set<ItemType> set = items.get(item);

						if(set == null)
						{
							set = new HashSet<>();
							items.put(item, set);
						}

						set.add(type);
					}
				}
			}
		}

		List<ItemType> toAdd = new ArrayList<>();

		for(Iterator<ItemType> i = allItems.iterator(); i.hasNext();)
		{
			ItemType type = i.next();

			if(type.getStack() instanceof ItemStack && CommonUtilities.getItemDamage((ItemStack)type.getStack()) == CraftGuide.DAMAGE_WILDCARD)
			{
				Item item = ((ItemStack)type.getStack()).getItem();
				Set<ItemType> set = items.get(item);

				if(set.size() == 1)
				{
					i.remove();
					toAdd.add(ItemType.getInstance(new ItemStack(item, 1, 0)));
				}
				else if(set.size() == 2)
				{
					i.remove();
				}
			}
		}

		allItems.addAll(toAdd);

		for(Iterator<ItemType> i = allItems.iterator(); i.hasNext();)
		{
			ItemType type = i.next();

			if(type.getStack() instanceof List && ((List<?>)type.getStack()).size() == 1 && ((List<?>)type.getStack()).get(0) instanceof ItemStack)
			{
				ItemStack stack = (ItemStack)((List<?>)type.getStack()).get(0);
				Item item = stack.getItem();
				Set<ItemType> set = items.get(item);
				boolean found = false;

				for(ItemType other: set)
				{
					if(other == type)
					{
						continue;
					}
					else if(other.getStack() instanceof ItemStack)
					{
						ItemStack otherStack = (ItemStack)other.getStack();
						if(ItemStack.areItemStacksEqual(stack, otherStack))
						{
							found = true;
							break;
						}
					}
				}

				if(found)
					i.remove();
			}
		}
	}

    private Map<ItemStack, List<CraftGuideRecipe>> generateRecipes()
    {
        generator.clearRecipes();
        CraftGuideLog.log("  Generating recipes from registered providers...");

        for(RecipeProvider provider : CraftGuide.RECIPE_PROVIDERS)
        {
            CraftGuideLog.log("    Generating recipes from " + provider.getClass().getName());
            try
            {
                provider.generateRecipes(generator);
            }
            catch(Exception | LinkageError e)
            {
                CraftGuideLog.log(e, "Provider " + provider.getClass().getName() + " failed!", true);
            }
        }

        return generator.getRecipes();
    }

	private void filterRawRecipes(Map<ItemStack, List<CraftGuideRecipe>> rawRecipes)
	{
		CraftGuideLog.log("  Filtering recipes...");
		for(Object object: ReflectionAPI.APIObjects)
		{
			if(object instanceof RecipeFilter)
			{
				CraftGuideLog.log("    Filtering recipes from " + object.getClass().getName());

				try
				{
					for(ItemStack type: rawRecipes.keySet())
					{
						rawRecipes.put(type, ((RecipeFilter)object).filterRecipes(rawRecipes.get(type), type));
					}
				}
				catch(Exception | LinkageError e)
				{
					CraftGuideLog.log(e);
				}
			}
			else if(object instanceof BasicRecipeFilter)
			{

				CraftGuideLog.log("    Filtering recipes from " + object.getClass().getName());

				try
				{
					for(ItemStack type: rawRecipes.keySet())
					{
						Iterator<CraftGuideRecipe> iterator = rawRecipes.get(type).iterator();

						while(iterator.hasNext())
						{
							if(!((BasicRecipeFilter)object).shouldKeepRecipe(iterator.next(), type))
							{
								iterator.remove();
							}
						}
					}
				}
				catch(Exception | LinkageError e)
				{
					CraftGuideLog.log(e);
				}
			}
		}
	}

	public void setTypes(Set<ItemType> types)
	{
		typeResults = new ArrayList<>();
		currentTypes = types;

		if(types == null)
		{
			for(ItemType type: craftingTypes)
			{
				typeResults.addAll(craftResults.get(type));
			}
		}
		else
		{
			for(ItemType type: craftingTypes)
			{
				if(types.contains(type))
				{
					typeResults.addAll(craftResults.get(type));
				}
			}
		}

		filter(filterItem);
	}

	public List<CraftGuideRecipe> getRecipes()
	{
		return filteredResults;
	}

	public Map<ItemType, List<CraftGuideRecipe>> getAllRecipes()
	{
		return craftResults;
	}

	public void filter(ItemFilter filter)
	{
		if(filter instanceof NoItemFilter)
		{
			filter = null;
		}

		if(filterItem != null)
		{
			filterHistory.push(filterItem);
		}

		filterHistoryForwards.clear();

		setFilter(filter);
	}

	public boolean hasPreviousFilter()
	{
		return filterHistory.size() > 0;
	}

	public void previousFilter()
	{
		if(hasPreviousFilter())
		{
			if(filterItem != null)
			{
				filterHistoryForwards.push(filterItem);
			}

			setFilter(filterHistory.pop());
		}
	}

	public boolean hasNextFilter()
	{
		return filterHistoryForwards.size() > 0;
	}

	public void nextFilter()
	{
		if(hasNextFilter())
		{
			if(filterItem != null)
			{
				filterHistory.push(filterItem);
			}

			setFilter(filterHistoryForwards.pop());
		}
	}

	private void setFilter(ItemFilter filter)
	{
		filterItem = filter;

		boolean input = GuiCraftGuide.filterSlotTypes.get(SlotType.INPUT_SLOT);
		boolean output = GuiCraftGuide.filterSlotTypes.get(SlotType.OUTPUT_SLOT);
		boolean machine = GuiCraftGuide.filterSlotTypes.get(SlotType.MACHINE_SLOT);

		if(filter == null)
		{
			filteredResults = typeResults;
		}
		else
		{
			filteredResults = new ArrayList<>();

			for(CraftGuideRecipe recipe: typeResults)
			{
				try
				{
					if(recipe instanceof CraftGuideRecipeExtra1)
					{
						CraftGuideRecipeExtra1 e = (CraftGuideRecipeExtra1)recipe;

						if((input && e.containsItem(filter, SlotType.INPUT_SLOT))
						|| (output && e.containsItem(filter, SlotType.OUTPUT_SLOT))
						|| (machine && e.containsItem(filter, SlotType.MACHINE_SLOT)))
						{
							filteredResults.add(recipe);
						}
					}
					else if(recipe.containsItem(filter))
					{
						filteredResults.add(recipe);
					}
				}
				catch(Exception e)
				{
					CraftGuideLog.log(e, "Exception thrown while matching against recipe " + recipe, false);
				}
			}
		}

		for(IRecipeCacheListener listener: listeners)
		{
			listener.onChange(this);
		}
	}

	public ItemFilter getFilter()
	{
		return filterItem;
	}

	public Set<ItemType> getCraftTypes()
	{
		return craftingTypes;
	}

	public void addListener(IRecipeCacheListener listener)
	{
		listeners.add(listener);
	}

	public SortedSet<ItemType> getAllItems()
	{
		return allItems;
	}

	public Set<ItemType> getFilterTypes()
	{
		return currentTypes;
	}
}
