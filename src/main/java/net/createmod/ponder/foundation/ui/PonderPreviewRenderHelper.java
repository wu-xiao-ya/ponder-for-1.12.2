package net.createmod.ponder.foundation.ui;

import java.lang.reflect.Method;

import net.createmod.ponder.foundation.Vec3iAccessor;
import net.createmod.ponder.foundation.ui.PonderSceneRuntimeTypes.RuntimeBlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

final class PonderPreviewRenderHelper {

    private PonderPreviewRenderHelper() {
    }

    static Method resolveBlockStateMethod() {
        try {
            Method method = IBlockState.class.getMethod("getActualState", IBlockAccess.class, BlockPos.class);
            method.setAccessible(true);
            return method;
        } catch (NoSuchMethodException ignored) {
        }

        try {
            Method method = IBlockState.class.getMethod("func_185899_b", IBlockAccess.class, BlockPos.class);
            method.setAccessible(true);
            return method;
        } catch (NoSuchMethodException ignored) {
        }

        return null;
    }

    static IBlockState getActualStateCompat(Method method, IBlockState state, IBlockAccess world, BlockPos pos) {
        if (state == null || method == null) {
            return state;
        }

        try {
            Object resolved = method.invoke(state, world, pos);
            return resolved instanceof IBlockState ? (IBlockState) resolved : state;
        } catch (Exception ignored) {
            return state;
        }
    }

    static float computeBlockBrightness(boolean showcaseMode, RuntimeBlockState block) {
        float base = showcaseMode ? 0.84F : 0.74F;
        float range = showcaseMode ? 0.22F : 0.28F;
        return net.minecraft.util.math.MathHelper.clamp(base + block.fade * range, 0.68F, 1.06F);
    }

    static void applyTileNbt(TileEntity tileEntity, String nbtRaw) {
        try {
            net.minecraft.nbt.NBTTagCompound compound = JsonToNBT.getTagFromJson(nbtRaw);
            compound.setInteger("x", Vec3iAccessor.x(tileEntity.getPos()));
            compound.setInteger("y", Vec3iAccessor.y(tileEntity.getPos()));
            compound.setInteger("z", Vec3iAccessor.z(tileEntity.getPos()));
            tileEntity.readFromNBT(compound);
        } catch (NBTException ignored) {
        } catch (RuntimeException ignored) {
        }
    }

    static void setPreviewLightmap(int previewFullBright) {
        int sky = previewFullBright & 0xFFFF;
        int block = previewFullBright >> 16 & 0xFFFF;
        net.minecraft.client.renderer.OpenGlHelper.setLightmapTextureCoords(
            net.minecraft.client.renderer.OpenGlHelper.lightmapTexUnit, sky, block);
    }
}
