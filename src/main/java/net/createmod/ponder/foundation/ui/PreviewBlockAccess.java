package net.createmod.ponder.foundation.ui;

import java.util.function.Function;

import net.createmod.ponder.foundation.ui.PonderSceneRuntimeTypes.RuntimeBlockState;
import net.createmod.ponder.foundation.ui.PonderSceneRuntimeTypes.RuntimeState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraft.init.Biomes;

class PreviewBlockAccess implements IBlockAccess {

    private final RuntimeState runtimeState;
    private final Function<RuntimeBlockState, TileEntity> tileEntityFactory;

    PreviewBlockAccess(RuntimeState runtimeState, Function<RuntimeBlockState, TileEntity> tileEntityFactory) {
        this.runtimeState = runtimeState;
        this.tileEntityFactory = tileEntityFactory;
    }

    @Override
    public TileEntity getTileEntity(BlockPos pos) {
        RuntimeBlockState block = runtimeState.blocksByPosition().get(pos);
        if (block == null || block.currentState == null) {
            return null;
        }
        if (!block.currentState.getBlock().hasTileEntity(block.currentState)
            && (block.tileNbt == null || block.tileNbt.isEmpty())) {
            return null;
        }
        return tileEntityFactory.apply(block);
    }

    @Override
    public int getCombinedLight(BlockPos pos, int lightValue) {
        return 0x00F000F0;
    }

    @Override
    public IBlockState getBlockState(BlockPos pos) {
        RuntimeBlockState block = runtimeState.blocksByPosition().get(pos);
        if (block == null || !block.visible || block.currentState == null) {
            return Blocks.AIR.getDefaultState();
        }
        return block.currentState;
    }

    @Override
    public boolean isAirBlock(BlockPos pos) {
        return getBlockState(pos).getBlock() == Blocks.AIR;
    }

    @Override
    public Biome getBiome(BlockPos pos) {
        return Biomes.PLAINS;
    }

    @Override
    public int getStrongPower(BlockPos pos, EnumFacing direction) {
        return getBlockState(pos).getStrongPower(this, pos, direction);
    }

    @Override
    public WorldType getWorldType() {
        return WorldType.FLAT;
    }

    @Override
    public boolean isSideSolid(BlockPos pos, EnumFacing side, boolean _default) {
        IBlockState state = getBlockState(pos);
        if (state == null || state.getBlock() == Blocks.AIR) {
            return _default;
        }
        return state.isSideSolid(this, pos, side);
    }
}
