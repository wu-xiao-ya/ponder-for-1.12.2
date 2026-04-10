package net.createmod.ponder.foundation.ui;

import java.lang.reflect.Field;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;

final class PlayerInventoryResolver {

    private static final String[] INVENTORY_FIELD_NAMES = { "inventory", "field_71071_by" };
    private static EntityPlayer cachedSnapshotPlayer;
    private static InventoryPlayer cachedSnapshotInventory;

    private PlayerInventoryResolver() {
    }

    static InventoryPlayer get(EntityPlayer player) {
        if (player == null) {
            return null;
        }

        for (String fieldName : INVENTORY_FIELD_NAMES) {
            InventoryPlayer inventory = getField(player, fieldName);
            if (inventory != null) {
                return inventory;
            }
        }

        return null;
    }

    static InventoryPlayer getSnapshot(EntityPlayer player) {
        if (player == null) {
            return null;
        }

        if (cachedSnapshotInventory == null || cachedSnapshotPlayer != player) {
            cachedSnapshotPlayer = player;
            cachedSnapshotInventory = new InventoryPlayer(player);
            clearInventory(cachedSnapshotInventory);
        }

        return cachedSnapshotInventory;
    }

    private static InventoryPlayer getField(EntityPlayer player, String fieldName) {
        Class<?> current = player.getClass();
        while (current != null) {
            try {
                Field field = current.getDeclaredField(fieldName);
                field.setAccessible(true);
                Object value = field.get(player);
                return value instanceof InventoryPlayer ? (InventoryPlayer) value : null;
            } catch (NoSuchFieldException ignored) {
                current = current.getSuperclass();
            } catch (Throwable ignored) {
                return null;
            }
        }

        return null;
    }

    private static void clearInventory(InventoryPlayer inventory) {
        if (inventory == null) {
            return;
        }

        int size = Math.max(0, inventory.getSizeInventory());
        for (int slot = 0; slot < size; slot++) {
            inventory.setInventorySlotContents(slot, ItemStack.EMPTY);
        }
    }
}
