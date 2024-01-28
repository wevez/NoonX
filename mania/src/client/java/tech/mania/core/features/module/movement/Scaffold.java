package tech.mania.core.features.module.movement;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.option.GameOptions;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;
import tech.mania.core.features.event.*;
import tech.mania.core.features.setting.BooleanSetting;
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

    private final BooleanSetting sideRotation = BooleanSetting.build()
            .name("Side rotation")
            .value(false)
            .end();

    private final BooleanSetting sameY = BooleanSetting.build()
            .name("Same Y")
            .value(false)
            .end();

    private double placeRangeSq = 9;

    private BlockData data, dataForStrafing;
    private int sneakTick, placeCount;
    private long lastClicked;
    boolean diagonal;
    private Vec3d bestPosition;
    private int airTick, startPosY;

    public Scaffold() {
        super("Scaffold", "Place block at your feet", ModuleCategory.Movement);
        getSettings().addAll(Arrays.asList(
                        this.mode,
                        this.placeRange,
                        this.sideRotation,
                        this.sameY
                )
        );
        keyCode = GLFW.GLFW_KEY_C;
    }

    @Override
    protected void onEnable() {
        dataForStrafing = null;
        sneakTick = 0;
        data = null;
        bestPosition = null;
        airTick = 0;
        super.onEnable();
    }

    @Override
    public void onPreUpdate(PreUpdateEvent event) {
        if (mc.world.getBlockState(mc.player.getBlockPos().add(0, -1, 0)).getBlock() == Blocks.AIR) airTick++;
        else airTick = 0;
        if (mc.player.isOnGround()) {
            boolean z = airTick > 2 && !isGood(mc.crosshairTarget, data);
            if (!diagonal) {
                if (z) {
                    placeCount = 0;
                    //mc.player.jump();
                    //shouldSneak = true;
                }
            } else {
                if (z) {
                    placeCount = 0;
                    mc.player.jump();
                    //shouldSneak = true;
                }
            }
        }
    }

    @Override
    public void onRotation(RotationEvent event) {
        data = null;
        //if (mc.player.isisOnGround()()) return;
        data = getBlockData();
        if (data != null) {
            dataForStrafing = data;
        }
        float[] std = stdRotation();
        std[0] = RotationUtil.smoothRot(mc.player.getYaw(), std[0], RandomUtil.nextFloat(0, 10));
        std[1] = RotationUtil.smoothRot(mc.player.getPitch(),  std[1], RandomUtil.nextFloat(5, 15));
        std[1] += (float) (Math.sin(MathHelper.wrapDegrees(mc.player.getYaw() - std[0]) / 5) * 5);
//        if (!mc.player.isOnGround()) {
//            sneakTick = 0;
//            mc.options.sneakKey.setPressed(false);
//            if (data != null) {
//                std = unqRotation();
//            } else {
//                std = new float[]{
//                        mc.player.getYaw(),
//                        mc.player.getPitch()
//                };
//            }
//        }
        event.yaw = std[0];
        event.pitch = std[1];

        if (Math.abs(event.yaw - mc.player.getYaw()) > 1) {
            sneakTick = 10;
            mc.options.sneakKey.setPressed(true);
        } else {
            sneakTick--;
            if (sneakTick < 0) {
                mc.options.sneakKey.setPressed(false);
            }
        }
        super.onRotation(event);
    }

    private float[] unqRotation() {
        // get best yaw
        if (false) {
            Vec3d eye = mc.player.getEyePos();
            var bb = data.toBox();
            float bestYaw = 0f;
            float bestDist = Float.MAX_VALUE;
            for (double x = bb.minX; x <= bb.maxX; x += 0.1) {
                for (double z = bb.minZ; z <= bb.maxZ; z += 0.1) {
                    float currentYaw = RotationUtil.rotation(new Vec3d(x, eye.y, z), eye)[0];
                    float currentDist = Math.abs(MathHelper.wrapDegrees(currentYaw - mc.player.getYaw()));
                    if (currentDist > bestDist) continue;
                    bestYaw = currentYaw;
                    bestDist = currentDist;
                }
            }
            float[] best = {bestYaw, 0f}, temp = {bestYaw, 0f};
            bestDist = Float.MAX_VALUE;
            for (float p = 65; p <= 85; p += 0.1f) {
                temp[1] = p;
                float currentDist = Math.abs(mc.player.getPitch() - best[1]);
                if (currentDist > bestDist) continue;
                if (!isGood(RayCastUtil.rayCast(temp, 3, 0), data)) continue;
                best[1] = p;
            }
            if (best[1] == 0f) best[1] = mc.player.getPitch();
            return best;
        }
        var box = data.toBox();
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

    private long lastStrafeSwitch, strafeSwitchDelay;

    @Override
    public void onInput(InputEvent event) {
//        event.moveFix = true;
//
//        if (!this.mode.getValue().equalsIgnoreCase("God bridge")) return;
//        if (dataForStrafing == null || mc.player.isSneaking()) return;
//        boolean deltaX = dataForStrafing == null ? Math.abs(mc.player.getX()) % 1 > 0.5 : dataForStrafing.getPos().getX() + 0.5 - mc.player.getX() > 0;
//        boolean deltaZ = dataForStrafing == null ? Math.abs(mc.player.getZ()) % 1 > 0.5 : dataForStrafing.getPos().getZ() + 0.5 - mc.player.getZ() > 0;;
//        boolean z = Math.abs(dataForStrafing.getPos().getX() + 0.5 - mc.player.getX()) > 1;
//        boolean x = Math.abs(dataForStrafing.getPos().getZ() + 0.5 - mc.player.getZ()) > 1;
//        if (mc.player.isOnGround()) {
//            if (System.currentTimeMillis() - lastStrafeSwitch > strafeSwitchDelay || z || x) {
////                switch (Direction.fromRotation(mc.player.getYaw()).toString()) {
////                    case "south":
////                        lastSideways = deltaX ? -1 : 1f;
////                        break;
////                    case "north":
////                        lastSideways = deltaX ? 1 : -1f;
////                        break;
////                    case "east":
////                        lastSideways = deltaZ ? 1 : -1f;
////                        break;
////                    case "west":
////                        lastSideways = deltaZ ? -1 : 1f;
////                        break;
////                }
//                strafeSwitchDelay = 500;
//                lastStrafeSwitch = System.currentTimeMillis();
//            }
//        }
//        event.getInput().movementSideways = lastSideways;
//        if (mc.player.isSneaking() || mc.player.isUsingItem()) {
//            event.getInput().movementSideways *= 0.2;
//            event.getInput().movementForward *= 0.2;
//        }
//        super.onInput(event);
    }

    @Override
    public void onClickTick(ClickTickEvent event) {
        if (sneakTick > 0) return;
        if (System.currentTimeMillis() - lastClicked < 25) return;
        boolean sneakPacket = false;
        if (isGood(mc.crosshairTarget, data)) {
            //if (sneakPacket) mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY));
            //mc.inGameHud.getChatHud().addMessage(Text.literal("Clicked"));
            //if (sneakPacket) mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));
            ((MinecraftClientAccessor)mc).accessDoUseItem();
            System.out.println("Z");
            lastClicked = System.currentTimeMillis();
            return;
        }
        if (true) {
            return;
        }

        for (float delta = 0; delta <= 1f; delta += 0.01f) {
            HitResult result = mc.player.raycast(3, delta, false);
            if (isGood(result, data)) {
                //System.out.println("Found " + delta);
//                if (sneakPacket) mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY));
//                mc.interactionManager.interactBlock(
//                        mc.player,
//                        Hand.MAIN_HAND,
//                        (BlockHitResult) result
//                );
//                mc.player.swingHand(Hand.MAIN_HAND);
//                mc.inGameHud.getChatHud().addMessage(Text.literal("Clicked"));
//                if (sneakPacket) mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));
//                lastClicked = System.currentTimeMillis();
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
        var blockResult = (BlockHitResult) result;
        //return mc.world.getBlockState(block.getBlockPos().offset(block.getSide())).isReplaceable();
        return blockResult.getBlockPos().offset(blockResult.getSide()).toString().equalsIgnoreCase(data.getPos().offset(data.getDirection()).toString());
    }

    private float lastSideways = 0f, lastForward = 0f, lastVirtualYaw;

    private float[] stdRotation() {
        final float na = 79.9f;
        if (data == null) {
            return new float[] {
                    mc.player.getYaw(),
                    na
                    //mc.player.fallDistance <= 0 || mc.player.isisOnGround()() ? 75.95f : 82f
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
        float stdPitch = na;
        boolean deltaX = data == null ? Math.abs(mc.player.getX()) % 1 > 0.5 : data.getPos().getX() + 0.5 - mc.player.getX() > 0;
        boolean deltaZ = data == null ? Math.abs(mc.player.getZ()) % 1 > 0.5 : data.getPos().getZ() + 0.5 - mc.player.getZ() > 0;;
        if (false) {
            if (Math.abs(stdYaw % 90) < 1) {
                float add = 0f;
                switch (Direction.fromRotation(stdYaw).toString()) {
                    case "south":
                        add += deltaX ? -45 : 45;
                        break;
                    case "north":
                        add += deltaX ? 45 : -45;
                        break;
                    case "east":
                        add += deltaZ ? 45 : -45;
                        break;
                    case "west":
                        add += deltaZ ? -45 : 45;
                        break;
                }
                stdYaw += add;
                //stdPitch = 75.95f;
                stdPitch = 80;
                diagonal = false;
            } else {
                bestPosition = null;
                diagonal = true;
                stdPitch = 78.5f;
            }
        }
        if (this.mode.getValue().equalsIgnoreCase("Ray cast")) {
            float[] stdRot = new float[] {
                    stdYaw,
                    0
            };
            float bestP = stdPitch, bestDist = Float.MAX_VALUE;
            for (float p = 72; p < 90; p += 0.1f) {
                stdRot[1] = p;
                float currentDist = Math.abs(p - stdPitch);
                //if (bestDist < currentDist) continue;
                HitResult current = RayCastUtil.rayCast(stdRot, 3, 1f);
                if (!isGood(current, data)) continue;
                bestP = p;
                bestDist = currentDist;
            }
            stdPitch = bestP;
        }
        //stdYaw += (float) Math.toDegrees(Math.atan2(lastSideways, lastForward));
        float[] za = new float[] {
                stdYaw,
                //80.2f
                stdPitch
        };
        return za;
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
                        if (mc.world.getBlockState(offsetPos.offset(facing)).getBlock() == Blocks.AIR) continue;
                        dataEntry.add(new BlockData(offsetPos.offset(facing), invert[facing.ordinal()]));
                    }
                }
            }
        }
        return dataEntry.stream()
                .filter(d -> mc.player.squaredDistanceTo(d.getPos().offset(d.getDirection()).toCenterPos()) < placeRangeSq)
                .min(Comparator.comparingDouble(d -> eye.squaredDistanceTo(d.getPos().offset(d.getDirection()).toCenterPos())))
                .orElse(null);
    }

}
