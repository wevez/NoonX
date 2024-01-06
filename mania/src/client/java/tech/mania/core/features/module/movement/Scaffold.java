package tech.mania.core.features.module.movement;

import io.netty.channel.group.ChannelMatchers;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.option.GameOptions;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import org.lwjgl.glfw.GLFW;
import tech.mania.core.features.event.ClickTickEvent;
import tech.mania.core.features.event.InputEvent;
import tech.mania.core.features.event.RotationEvent;
import tech.mania.core.features.event.SendPacketEvent;
import tech.mania.core.features.setting.DoubleSetting;
import tech.mania.core.features.setting.ModeSetting;
import tech.mania.core.types.module.Module;
import tech.mania.core.types.module.ModuleCategory;
import tech.mania.core.util.*;
import tech.mania.mixin.client.MinecraftClientAccessor;

import java.util.*;

public class Scaffold extends Module {
    
    private static final Direction[] invert = {
            Direction.UP,
            Direction.DOWN,
            Direction.SOUTH,
            Direction.NORTH,
            Direction.EAST,
            Direction.WEST
    };

    private static final BlockPos[][][] addons;

    static {
        addons = new BlockPos[7][2][7];
        for (int x = -3; x <= 3; x++) {
            for (int y = -1; y <= 0; y++) {
                for (int z = -3; z <= 3; z++) {
                    addons[x + 3][y + 1][z + 3] = new BlockPos(x, y, z);
                }
            }
        }
    }

    private final DoubleSetting placeRange = DoubleSetting.build()
            .range(0, 6)
            .value(3)
            .name("Place Range")
            .increment(0.1)
            .onSetting(v -> placeRangeSq = v * v)
            .unit("Blocks")
            .end();

    private final ModeSetting mode = ModeSetting.build()
            .name("Mode")
            .option(
                    "No AC",
                    "Ray cast",
                    "God bridge"
            )
            .value("God bridge")
            .end();

    private double placeRangeSq = 9;

    private BlockData data;
    private int sneakTick, placeCount;
    private long lastClicked;
    boolean diagonal;
    private Vec3d bestPosition;
    private int airTick;

    public Scaffold() {
        super("Scaffold", "Place block at your feet", ModuleCategory.Movement);
        getSettings().addAll(Arrays.asList(
                this.mode,
                this.placeRange
                )
        );
        keyCode = GLFW.GLFW_KEY_C;
    }

    @Override
    protected void onEnable() {
        sneakTick = 0;
        data = null;
        bestPosition = null;
        airTick = 0;
        super.onEnable();
    }

    @Override
    public void onRotation(RotationEvent event) {
        data = null;
        Vec3d eye = mc.player.getEyePos();
        data = getBlockData();
        float[] std = stdRotation();
        // 下のブロックかどうか
        boolean onAir = mc.world.getBlockState(mc.player.getBlockPos().add(0, -1, 0)).getBlock() == Blocks.AIR;
        if (onAir) {
            airTick++;
        } else {
            airTick = 0;
        }
        std[0] = RotationUtil.smoothRot(mc.player.getYaw(), std[0], RandomUtil.nextFloat(5, 15));
        std[1] = RotationUtil.smoothRot(mc.player.getPitch(),  std[1], RandomUtil.nextFloat(5, 15));
        std[1] += (float) (Math.sin(MathHelper.wrapDegrees(mc.player.getYaw() - std[0]) / 5) * 5);
        if (!mc.player.isOnGround()) {
            mc.options.sneakKey.setPressed(false);
            mc.options.sneakKey.setPressed(false);
            if (data != null) {
                std = unqRotation();
            } else {
                std = new float[]{
                        mc.player.getYaw(),
                        mc.player.getPitch()
                };
            }
        }
        //mc.options.sneakKey.setPressed(false);
        event.yaw = std[0];
        event.pitch = std[1];

        boolean shouldSneak = Math.abs(event.yaw - mc.player.getYaw()) > 1;
        //shouldSneak = false;
        if (mc.player.isOnGround()) {
            if (!diagonal) {
                if (mc.player.isOnGround() && placeCount % 12 == 4) {
                    placeCount = 0;
                    //mc.player.jump();
                    shouldSneak = true;
                }
            } else {
                if (mc.player.isOnGround() && placeCount % 12 == 8) {
                    placeCount = 0;
                    //mc.player.jump();
                    shouldSneak = true;
                }
            }
            if (shouldSneak) {
                sneakTick = 40;
                mc.options.sneakKey.setPressed(true);
            } else {
                sneakTick--;
                if (sneakTick < 0) {
                    mc.options.sneakKey.setPressed(false);
                }
            }
        }

        if (data == null) {
            return;
        }
        super.onRotation(event);
    }

    private float[] unqRotation() {
        Box box = data.toBox();
        float[] best = null;
        Vec3d eye = mc.player.getEyePos();
        double bestDist = Double.MAX_VALUE;
        for (double x = box.minX; x <= box.maxX; x += 0.1) {
            for (double y = box.minY; y <= box.maxY; y += 0.1) {
                for (double z = box.minZ; z <= box.maxZ; z += 0.1) {
                    float[] currentRot = RotationUtil.rotation(new Vec3d(x, y, z), eye);
                    if (!isGood(RayCastUtil.rayCast(currentRot, placeRange.getValue(), 1f), data)) {
                        continue;
                    }
                    double currentDist = Math.hypot(
                            MathHelper.wrapDegrees(mc.player.getYaw() - currentRot[0]),
                            MathHelper.wrapDegrees(mc.player.getPitch() - currentRot[1])
                    );
                    if (bestDist > currentDist) {
                        bestDist = currentDist;
                        best = currentRot;
                    }
                }
            }
        }
        return best == null ? RotationUtil.rotation(box.getCenter(), eye)  : best;
    }

    @Override
    public void onInput(InputEvent event) {
        event.moveFix = true;
        if (!diagonal && bestPosition != null) {
        }
        if ( Math.abs(mc.player.getYaw() - mc.player.getYaw()) > 1) {
            event.getInput().movementForward = 0;
            event.getInput().movementSideways = 0;
        }
        super.onInput(event);
    }

    @Override
    public void onClickTick(ClickTickEvent event) {
        if (sneakTick > 0) return;
        //if (System.currentTimeMillis() - lastClicked < 25) return;
        boolean sneakPacket = false;
        if (airTick > 0) {
            if (RandomUtil.percent(10)) {
               // ((MinecraftClientAccessor) mc).accessDoUseItem();
               // mc.inGameHud.getChatHud().addMessage(Text.literal("Clicked"));
            }
        }
        if (isGood(mc.crosshairTarget, data)) {
            if (sneakPacket) mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY));
            mc.interactionManager.interactBlock(
                    mc.player,
                    Hand.MAIN_HAND,
                    (BlockHitResult) mc.crosshairTarget
            );
            mc.player.swingHand(Hand.MAIN_HAND);
            mc.inGameHud.getChatHud().addMessage(Text.literal("Clicked"));
            if (sneakPacket) mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));
            //((MinecraftClientAccessor) mc).accessDoUseItem();
            lastClicked = System.currentTimeMillis();
            return;
        }
        if (mc.player.isOnGround()) {
            return;
        }

        for (float delta = 0; delta <= 1f; delta += 0.01f) {
            HitResult result = mc.player.raycast(3, delta, false);
            if (isGood(result, data)) {
                //System.out.println("Found " + delta);
                if (sneakPacket) mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY));
                mc.interactionManager.interactBlock(
                        mc.player,
                        Hand.MAIN_HAND,
                        (BlockHitResult) result
                );
                mc.player.swingHand(Hand.MAIN_HAND);
                mc.inGameHud.getChatHud().addMessage(Text.literal("Clicked"));
                if (sneakPacket) mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));
                lastClicked = System.currentTimeMillis();
                break;
            }
        }
        super.onClickTick(event);
    }

    @Override
    public void onSendPacket(SendPacketEvent event) {
        if (event.getPacket() instanceof HandSwingC2SPacket) {
            placeCount++;
        }
        super.onSendPacket(event);
    }

    private boolean isGood(HitResult result, BlockData data) {
        if (result == null || data == null) return false;
        if (!(result instanceof BlockHitResult)) {
            return false;
        }
        BlockHitResult block = (BlockHitResult) result;
        //return mc.world.getBlockState(block.getBlockPos().offset(block.getSide())).isReplaceable();
        return block.getBlockPos().offset(block.getSide()).toString().equalsIgnoreCase(data.getPos().offset(data.getDirection()).toString());
    }

    private float lastSideways = 0f, lastForward = 0f, lastVirtualYaw;

    private float[] stdRotation() {
        if (data == null) {
            return new float[] {
                    mc.player.getYaw(),
                    mc.player.getPitch()
            };
        }
        GameOptions options = mc.options;
        //if (options.forwardKey.isPressed() || options.backKey.isPressed() || options.rightKey.isPressed() || options.leftKey.isPressed()) {
            lastSideways = options.forwardKey.isPressed() == options.backKey.isPressed() ? 0.0F : (options.forwardKey.isPressed() ? 1.0F : -1.0F);
            lastForward = options.leftKey.isPressed() == options.rightKey.isPressed() ? 0.0F : (options.leftKey.isPressed() ? 1.0F : -1.0F);
            lastVirtualYaw = RotationUtil.virtualYaw + 180;
        //}
        float stdYaw = Math.round(lastVirtualYaw / 45) * 45;
        //stdYaw += (float) Math.toDegrees(Math.atan2(lastSideways, lastForward));
        float stdPitch;
        boolean deltaX = data == null ? Math.abs(mc.player.getX()) % 1 > 0.5 : data.getPos().getX() + 0.5 - mc.player.getX() > 0;
        boolean deltaZ = data == null ? Math.abs(mc.player.getZ()) % 1 > 0.5 : data.getPos().getZ() + 0.5 - mc.player.getZ() > 0;;
        if (Math.abs(stdYaw % 90) < 1) {
            switch (Direction.fromRotation(stdYaw).toString()) {
                case "south":
                    stdYaw += deltaX ? -45 : 45;
                    break;
                case "north":
                    stdYaw += deltaX ? 45 : -45;
                    break;
                case "east":
                    stdYaw += deltaZ ? 45 : -45;
                    break;
                case "west":
                    stdYaw += deltaZ ? -45 : 45;
                    break;
            }
            stdPitch = 75.95f;
            diagonal = false;
        } else {
            bestPosition = null;
            diagonal = true;
            stdPitch = 78.5f;
        }
        //stdYaw += (float) Math.toDegrees(Math.atan2(lastSideways, lastForward));
        float[] z = new float[] {
                stdYaw,
                //80.2f
                stdPitch
        };
        return z;
    }

    private BlockData getBlockData() {
        BlockPos blockPos = mc.player.getBlockPos().add(0, -1, 0);
        if (mc.world.getBlockState(blockPos).getBlock() != Blocks.AIR) {
            return null;
        }
        Vec3d eye = mc.player.getEyePos();
        List<BlockData> dataEntry = new ArrayList<>();
        for (BlockPos[][] xBP : addons) {
            for (BlockPos[] yBP : xBP) {
                for (BlockPos zBP : yBP) {
                    BlockPos offsetPos = blockPos.add(zBP);
                    if (mc.world.getBlockState(offsetPos).getBlock() != Blocks.AIR) continue;;
                    for (Direction facing : Direction.values()) {
                        if (mc.world.getBlockState(offsetPos.offset(facing)).isReplaceable()) continue;
                        dataEntry.add(new BlockData(offsetPos.offset(facing), invert[facing.ordinal()]));
                    }
                }
            }
        }
        return dataEntry.stream()
                .filter(d -> mc.player.squaredDistanceTo(d.getPos().offset(d.getDirection()).toCenterPos()) <    placeRangeSq)
                .min(Comparator.comparingDouble(d -> eye.squaredDistanceTo(d.getPos().offset(d.getDirection()).toCenterPos())))
                .orElse(null);
    }

}
