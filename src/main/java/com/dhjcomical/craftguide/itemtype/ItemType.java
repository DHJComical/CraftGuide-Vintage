package com.dhjcomical.craftguide.itemtype;

import com.dhjcomical.craftguide.CommonUtilities;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nullable;
import java.util.*;

public class ItemType implements Comparable<ItemType> {

    private final Object representative;
    private final ItemStack displayStack;

    // A cached, order-independent hash code for lists
    private final int listHashCode;

    private ItemType(Object object) {
        if (object instanceof ItemStack) {
            ItemStack stack = ((ItemStack) object).copy();
            stack.setCount(1);
            this.representative = stack;
            this.displayStack = stack;
            this.listHashCode = 0; // Not used for single items
        } else if (object instanceof List) {
            this.representative = object;
            this.displayStack = findFirstValidStack((List<?>) object);
            // Pre-calculate an order-independent hash code for this list
            this.listHashCode = calculateListHashCode((List<?>)object);
        } else {
            throw new IllegalArgumentException("Cannot create ItemType from object: " + object.getClass().getName());
        }
    }

    @Nullable
    public static ItemType getInstance(Object object) {
        if (object instanceof ItemStack && !((ItemStack) object).isEmpty()) {
            return new ItemType(object);
        } else if (object instanceof List && !((List<?>) object).isEmpty()) {
            if (findFirstValidStack((List<?>)object) != ItemStack.EMPTY) {
                return new ItemType(object);
            }
        }
        return null;
    }

    private static ItemStack findFirstValidStack(List<?> list) {
        for (Object obj : list) {
            if (obj instanceof ItemStack && !((ItemStack) obj).isEmpty()) {
                return ((ItemStack) obj);
            }
        }
        return ItemStack.EMPTY;
    }

    public ItemStack getDisplayStack() {
        return this.displayStack;
    }

    public Object getStack() {
        return this.representative;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemType other = (ItemType) o;

        if (this.representative instanceof ItemStack && other.representative instanceof ItemStack) {
            return CommonUtilities.checkItemStackMatch((ItemStack) this.representative, (ItemStack) other.representative);
        }

        if (this.representative instanceof List && other.representative instanceof List) {
            List<?> listA = (List<?>) this.representative;
            List<?> listB = (List<?>) other.representative;

            if (listA.size() != listB.size()) return false;

            List<ItemStack> tempA = new ArrayList<>();
            for(Object obj : listA) if(obj instanceof ItemStack) tempA.add((ItemStack)obj);

            List<ItemStack> tempB = new ArrayList<>();
            for(Object obj : listB) if(obj instanceof ItemStack) tempB.add((ItemStack)obj);

            if (tempA.size() != tempB.size()) return false;

            for (ItemStack stackA : tempA) {
                boolean foundMatch = false;
                for (int i = 0; i < tempB.size(); i++) {
                    if (CommonUtilities.checkItemStackMatch(stackA, tempB.get(i))) {
                        tempB.remove(i);
                        foundMatch = true;
                        break;
                    }
                }
                if (!foundMatch) {
                    return false;
                }
            }
            return tempB.isEmpty();
        }

        return false; // Mismatched types
    }

    @Override
    public int hashCode() {
        if (representative instanceof ItemStack) {
            ItemStack stack = (ItemStack) representative;
            int result = Objects.hash(stack.getItem(), stack.getItemDamage());
            return 31 * result + (stack.hasTagCompound() ? stack.getTagCompound().hashCode() : 0);
        }
        return this.listHashCode;
    }

    private int calculateListHashCode(List<?> list) {
        int hashCode = 0;
        for (Object obj : list) {
            if (obj instanceof ItemStack) {
                ItemStack stack = (ItemStack) obj;
                int itemHash = Objects.hash(stack.getItem(), stack.getItemDamage());
                itemHash = 31 * itemHash + (stack.hasTagCompound() ? stack.getTagCompound().hashCode() : 0);
                hashCode ^= itemHash;
            }
        }
        return hashCode;
    }

    @Override
    public int compareTo(ItemType other) {
        ItemStack thisStack = this.getDisplayStack();
        ItemStack otherStack = other.getDisplayStack();

        if (thisStack.isEmpty() && otherStack.isEmpty()) return 0;
        if (thisStack.isEmpty()) return 1;
        if (otherStack.isEmpty()) return -1;

        int idCompare = Integer.compare(Item.getIdFromItem(thisStack.getItem()), Item.getIdFromItem(otherStack.getItem()));
        if (idCompare != 0) {
            return idCompare;
        }

        int damageCompare = Integer.compare(thisStack.getItemDamage(), otherStack.getItemDamage());
        if (damageCompare != 0) {
            return damageCompare;
        }

        NBTTagCompound thisNbt = thisStack.getTagCompound();
        NBTTagCompound otherNbt = otherStack.getTagCompound();

        if (thisNbt == null && otherNbt != null) return -1;
        if (thisNbt != null && otherNbt == null) return 1;
        if (thisNbt != null) {
            int nbtCompare = thisNbt.toString().compareTo(otherNbt.toString());
            if (nbtCompare != 0) {
                return nbtCompare;
            }
        }

        boolean thisIsList = this.representative instanceof List;
        boolean otherIsList = other.representative instanceof List;
        if (thisIsList != otherIsList) {
            return thisIsList ? 1 : -1;
        }

        return 0;
    }
    @Override
    public String toString() {
        if (representative instanceof ItemStack) {
            ItemStack stack = (ItemStack) representative;
            return "ItemType{" + stack.getItem().getRegistryName() + "@" + stack.getItemDamage() + ", nbt=" + stack.getTagCompound() + "}";
        }
        return "ItemType{List@" + System.identityHashCode(representative) + "}";
    }
}