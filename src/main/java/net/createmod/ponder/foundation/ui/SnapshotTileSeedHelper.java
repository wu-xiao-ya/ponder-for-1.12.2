package net.createmod.ponder.foundation.ui;

import java.lang.reflect.Method;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

final class SnapshotTileSeedHelper {

    private SnapshotTileSeedHelper() {
    }

    static void clearInventorySlots(TileEntity tile, int count) {
        for (int slot = 0; slot < count; slot++) {
            clearInventorySlot(tile, slot);
        }
    }

    static void clearInventorySlot(TileEntity tile, int slot) {
        try {
            Method setSlot = tile.getClass().getMethod("setInventorySlotContents", int.class, ItemStack.class);
            setSlot.invoke(tile, Integer.valueOf(slot), ItemStack.EMPTY);
            return;
        } catch (Throwable ignored) {
        }
        try {
            Method setSlot = tile.getClass().getMethod("func_70299_a", int.class, ItemStack.class);
            setSlot.invoke(tile, Integer.valueOf(slot), ItemStack.EMPTY);
        } catch (Throwable ignored) {
        }
    }

    static void seedThermalMachineFields(TileEntity tile, float currentTick) {
        int cycle = ((int) currentTick) % 160;
        int energy = 16000 - cycle * 50;
        int progress = cycle % 100;
        trySetField(tile, 0, Math.max(0, energy));
        trySetField(tile, 1, 16000);
        trySetField(tile, 2, progress);
        trySetField(tile, 3, 100);
    }

    static void trySetField(TileEntity tile, int fieldId, int value) {
        try {
            Method setField = tile.getClass().getMethod("setField", int.class, int.class);
            setField.invoke(tile, Integer.valueOf(fieldId), Integer.valueOf(value));
            return;
        } catch (Throwable ignored) {
        }
        try {
            Method setField = tile.getClass().getMethod("func_174885_b", int.class, int.class);
            setField.invoke(tile, Integer.valueOf(fieldId), Integer.valueOf(value));
        } catch (Throwable ignored) {
        }
    }
}
