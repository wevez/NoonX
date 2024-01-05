package tech.mania.core.util;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;

public class BlockData {

    private final BlockPos pos;
    private final Direction direction;

    public BlockPos getPos() {
        return pos;
    }

    public Direction getDirection() {
        return direction;
    }

    public Box toBox() {
        final BlockPos pos = this.pos;
        return switch (this.direction) {
            case DOWN -> new Box(pos, new BlockPos(pos.getX() + 1, pos.getY(), pos.getZ() + 1));
            case NORTH ->
                    new Box(new BlockPos(pos.getX(), pos.getY(), pos.getZ()), new BlockPos(pos.getX() + 1, pos.getY() + 1, pos.getZ()));
            case EAST ->
                    new Box(new BlockPos(pos.getX() + 1, pos.getY(), pos.getZ()), new BlockPos(pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1));
            case SOUTH ->
                    new Box(new BlockPos(pos.getX(), pos.getY(), pos.getZ() + 1), new BlockPos(pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1));
            case UP ->
                    new Box(new BlockPos(pos.getX(), pos.getY() + 1, pos.getZ()), new BlockPos(pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1));
            case WEST ->
                    new Box(new BlockPos(pos.getX(), pos.getY(), pos.getZ()), new BlockPos(pos.getX(), pos.getY() + 1, pos.getZ() + 1));
        };
    }

    public BlockData(BlockPos pos, Direction direction) {
        this.pos = pos;
        this.direction = direction;
    }
}
