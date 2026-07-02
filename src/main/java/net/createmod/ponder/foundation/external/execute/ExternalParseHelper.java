package net.createmod.ponder.foundation.external.execute;

import java.util.Locale;

import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.foundation.external.validate.ExternalJsonValidation;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

/**
 * Static parse/utility helpers for external scene operations.
 * Extracted from ExternalSceneExecutor to reduce coupling and allow reuse
 * outside the execution pipeline (e.g., validation, tooling, debug screens).
 * <p>
 * All methods are pure-static; no instance state, no UI dependency.
 */
public final class ExternalParseHelper {

    private static final int LOG_PAYLOAD_PREVIEW_CHARS = 256;

    private ExternalParseHelper() {
    }

    /**
     * Parse a block state from a block ID string and metadata value.
     *
     * @param blockId the block's registry name (e.g. "minecraft:stone")
     * @param meta    the block metadata; if negative, the default state is used
     * @return the resolved IBlockState
     * @throws IllegalArgumentException if the block ID is missing or unknown
     */
    public static IBlockState parseBlockState(String blockId, int meta) {
        if (blockId == null || blockId.trim().isEmpty()) {
            throw new IllegalArgumentException("Missing block state id");
        }
        Block block = Block.REGISTRY.getObject(new ResourceLocation(blockId));
        if (block == null) {
            throw new IllegalArgumentException("Unknown block id: " + blockId);
        }
        if (meta < 0) {
            return block.getDefaultState();
        }
        try {
            return block.getStateFromMeta(meta);
        } catch (RuntimeException ignored) {
            return block.getDefaultState();
        }
    }

    /**
     * Parse a PonderPalette from its enum name. Falls back to WHITE on
     * null/empty/invalid input.
     */
    public static PonderPalette parsePalette(String colorName) {
        if (colorName == null || colorName.trim().isEmpty()) {
            return PonderPalette.WHITE;
        }
        try {
            return PonderPalette.valueOf(colorName.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return PonderPalette.WHITE;
        }
    }

    /**
     * Parse an EnumFacing from its enum name. Falls back to UP on
     * null/empty/invalid input.
     */
    public static EnumFacing parseFacing(String direction) {
        if (direction == null || direction.trim().isEmpty()) {
            return EnumFacing.UP;
        }
        try {
            return EnumFacing.valueOf(direction.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return EnumFacing.UP;
        }
    }

    /**
     * Truncate and flatten a payload string for log/preview display.
     * Newlines and carriage returns are replaced with spaces; strings longer
     * than {@value #LOG_PAYLOAD_PREVIEW_CHARS} characters are elided with "...".
     */
    public static String previewPayload(String payload) {
        if (payload == null) {
            return "";
        }
        String compact = payload.replace('\r', ' ').replace('\n', ' ');
        if (compact.length() <= LOG_PAYLOAD_PREVIEW_CHARS) {
            return compact;
        }
        return compact.substring(0, LOG_PAYLOAD_PREVIEW_CHARS) + "...";
    }

    /**
     * Resolve a texture {@link ResourceLocation} from an input string,
     * delegating to {@link ExternalJsonValidation#parseTextureLocation}.
     * <p>
     * This forwarding method exists to keep texture-location resolution
     * consistent across both parse and validation contexts without forcing
     * callers to depend on the validation package directly.
     */
    public static ResourceLocation resolveTextureLocation(String value, String defaultNamespace) {
        return ExternalJsonValidation.parseTextureLocation(value, defaultNamespace);
    }
}
