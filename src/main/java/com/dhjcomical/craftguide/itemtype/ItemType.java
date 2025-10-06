package com.dhjcomical.craftguide.itemtype;

import com.dhjcomical.craftguide.CommonUtilities;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

public class ItemType implements Comparable<ItemType> {

    private final Object representative;
    private final ItemStack displayStack;

    private ItemType(Object object) {
        if (object instanceof ItemStack) {
            ItemStack stack = ((ItemStack) object).copy();
            stack.setCount(1);
            this.representative = stack;
            this.displayStack = stack;
        } else if (object instanceof List) {
            this.representative = object;
            this.displayStack = findFirstValidStack((List<?>) object);
        } else {
            throw new IllegalArgumentException("Cannot create ItemType from object of type: " + object.getClass().getName());
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

        return Objects.equals(this.representative, other.representative);
    }

    @Override
    public int hashCode() {
        if (representative instanceof ItemStack) {
            ItemStack stack = (ItemStack) representative;
            int result = Objects.hash(stack.getItem(), stack.getItemDamage());
            result = 31 * result + (stack.hasTagCompound() ? stack.getTagCompound().hashCode() : 0);
            return result;
        }
        return Objects.hash(representative);
    }

    @Override
    public int compareTo(ItemType other) {
        ItemStack thisStack = this.getDisplayStack();
        ItemStack otherStack = other.getDisplayStack();

        if (thisStack.isEmpty() && otherStack.isEmpty()) return 0;
        if (thisStack.isEmpty()) return 1; // Empty items sort last
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

        if (!this.equals(other)) {
            return Integer.compare(System.identityHashCode(this.representative), System.identityHashCode(other.representative));
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