package net.createmod.ponder.foundation;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

public final class PonderSchematic {

    private static final PonderSchematic EMPTY =
        new PonderSchematic(new BlockPos(0, 0, 0), Collections.<BlockPos, IBlockState>emptyMap());

    private final BlockPos size;
    private final Map<BlockPos, IBlockState> blocks;

    private PonderSchematic(BlockPos size, Map<BlockPos, IBlockState> blocks) {
        this.size = size;
        this.blocks = Collections.unmodifiableMap(blocks);
    }

    public BlockPos getSize() {
        return size;
    }

    public Map<BlockPos, IBlockState> getBlocks() {
        return blocks;
    }

    public boolean isEmpty() {
        return blocks.isEmpty();
    }

    public static PonderSchematic empty() {
        return EMPTY;
    }

    public static PonderSchematic load(ResourceLocation location) {
        String resourcePath = "assets/" + location.getNamespace() + "/ponder/" + location.getPath() + ".nbt";
        InputStream stream = PonderSchematic.class.getClassLoader().getResourceAsStream(resourcePath);
        if (stream == null) {
            return empty();
        }

        try (InputStream input = stream) {
            NBTTagCompound compound = CompressedStreamTools.readCompressed(input);
            return fromNbt(compound);
        } catch (IOException e) {
            return empty();
        }
    }

    private static PonderSchematic fromNbt(NBTTagCompound compound) {
        NBTTagList sizeTag = compound.getTagList("size", 3);
        BlockPos size = sizeTag.tagCount() >= 3
            ? new BlockPos(sizeTag.getIntAt(0), sizeTag.getIntAt(1), sizeTag.getIntAt(2))
            : new BlockPos(0, 0, 0);

        Map<Integer, IBlockState> palette = new LinkedHashMap<Integer, IBlockState>();
        NBTTagList paletteTag = compound.getTagList("palette", 10);
        for (int index = 0; index < paletteTag.tagCount(); index++) {
            palette.put(Integer.valueOf(index), NBTUtil.readBlockState(paletteTag.getCompoundTagAt(index)));
        }

        Map<BlockPos, IBlockState> blocks = new LinkedHashMap<BlockPos, IBlockState>();
        NBTTagList blocksTag = compound.getTagList("blocks", 10);
        for (int index = 0; index < blocksTag.tagCount(); index++) {
            NBTTagCompound blockTag = blocksTag.getCompoundTagAt(index);
            NBTTagList posTag = blockTag.getTagList("pos", 3);
            if (posTag.tagCount() < 3) {
                continue;
            }

            IBlockState state = palette.get(Integer.valueOf(blockTag.getInteger("state")));
            if (state == null) {
                continue;
            }

            BlockPos pos = new BlockPos(posTag.getIntAt(0), posTag.getIntAt(1), posTag.getIntAt(2));
            blocks.put(pos, state);
        }

        return new PonderSchematic(size, blocks);
    }
}
