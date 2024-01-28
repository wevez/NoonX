package tech.mania.core.features.module.movement;

import net.minecraft.block.Blocks;
import net.minecraft.client.option.GameOptions;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;
import tech.mania.core.features.event.ClickTickEvent;
import tech.mania.core.features.event.InputEvent;
import tech.mania.core.features.event.PreUpdateEvent;
import tech.mania.core.features.event.RotationEvent;
import tech.mania.core.features.setting.DoubleSetting;
import tech.mania.core.features.setting.ModeSetting;
import tech.mania.core.types.module.Module;
import tech.mania.core.types.module.ModuleCategory;
import tech.mania.core.util.BlockData;
import tech.mania.core.util.RandomUtil;
import tech.mania.core.util.RayCastUtil;
import tech.mania.core.util.RotationUtil;
import tech.mania.mixin.client.MinecraftClientAccessor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class ReScaffold extends Module {

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
                    "Fruitberry",
                    "Telly",
                    "Moonwalk"
            )
            .value("God bridge")
            .end();

    private double placeRangeSq;
    private BlockData data;
    private BlockPos posForStrafing;
    private int airTick, sneakTick, placeCount;
    private boolean diagonal;
    private int sameYPos;
    private boolean jumping;
    private long lastClicked;

    public ReScaffold() {
        super("Scaffold", "Place block at your feet", ModuleCategory.Movement);
        getSettings().addAll(Arrays.asList(
                        this.mode,
                        this.placeRange
                )
        );
        keyCode = GLFW.GLFW_KEY_C;
    }

    @Override
    public void onRotation(RotationEvent event) {
        if (mode.getValue().equalsIgnoreCase("Moonwalk")) {
            sameYPos = (int) mc.player.getY();
        }
        if (mc.player.isOnGround()) {
            sameYPos = (int) mc.player.getY();
            if (mode.getValue().equalsIgnoreCase("Telly")) {
               // event.yaw = Math.round(RotationUtil.virtualYaw / 45); // TODO might be flagged
                return;
            }
        }
        data = getBlockData();
        if (data != null) {
            posForStrafing = data.getPos();
        }float[] std = stdRotation(false);
        event.yaw = std[0];
        event.pitch = std[1];

        if (Math.abs(MathHelper.wrapDegrees(mc.player.getYaw() - event.yaw)) > 1) {
            sneakTick = 10;
        }
        super.onRotation(event);
    }

    @Override
    public void onPreUpdate(PreUpdateEvent event) {
        if (mode.getValue().equalsIgnoreCase("Moonwalk")) {
            if (mc.world.getBlockState(mc.player.getBlockPos().add(0, -1, 0)).getBlock() == Blocks.AIR) {
                airTick++;
            } else {
                airTick = 0;
            }
            if (mc.player.isOnGround() && airTick > 3 && !isGood(mc.crosshairTarget, data, false)) {
//                mc.player.jump();
                //sneakTick = 1;
                jumping = true;
            }
            //else {
//                jumping = false;
//            }
            if (placeCount > 7 && mc.player.isOnGround()) {
                placeCount = 0;
            }
        }
        if (sneakTick > 0) {
            sneakTick--;
           // mc.options.sneakKey.setPressed(true);
        } else {
            mc.options.sneakKey.setPressed(false);
        }
        super.onPreUpdate(event);
    }

    @Override
    public void onClickTick(ClickTickEvent event) {
        if (sneakTick > 0) return;
        if (mc.player.getVelocity().horizontalLength() == 0) return;
        if (System.currentTimeMillis() - lastSwitchTime < 50) return;
        boolean flag = airTick > 1 && mc.crosshairTarget instanceof BlockHitResult && ((BlockHitResult) mc.crosshairTarget).getSide() != Direction.UP;
        if (flag) {
            //if (sneakPacket) mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY));
            mc.inGameHud.getChatHud().addMessage(Text.literal("Clicked"));
            //if (sneakPacket) mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));
           // mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY));

            ((MinecraftClientAccessor)mc).accessDoUseItem();
            //System.out.println("Z");
           // mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));
//           var b = (BlockHitResult) mc.crosshairTarget;
//            mc.interactionManager.interactBlock(
//                    mc.player,
//                    Hand.MAIN_HAND,
//                    b
//            );
//            mc.player.swingHand(Hand.MAIN_HAND);
            lastClicked = System.currentTimeMillis();
            placeCount++;
        } else {
//            for (float delta = 0; delta <= 1f; delta += 0.01f) {
//                HitResult result = mc.player.raycast(3, delta, false);
//                if (isGood(result, data, true)) {
//                    mc.interactionManager.interactBlock(
//                        mc.player,
//                        Hand.MAIN_HAND,
//                        (BlockHitResult) result
//                    );
//                    mc.player.swingHand(Hand.MAIN_HAND);
//                    placeCount++;
//                    lastClicked = System.currentTimeMillis();
//                    break;
//                }
//            }
        }
        super.onClickTick(event);
    }

    private float lastSide = 0f;
    private long lastSwitchTime;
    private BlockPos lastSafePos;

    @Override
    protected void onEnable() {
        lastSafePos = null;
        data = null;
        lastSide = 1;

        super.onEnable();
    }

    private BlockPos getUnder() {
        return mc.player.getBlockPos().add(0, -1, 0);
    }

    @Override
    public void onInput(InputEvent event) {
        event.moveFix = true;
        if (sneakTick > 0) {
            return;
        }
        if (diagonal) {
            event.getInput().movementSideways = mc.player.age % 20 > 9 ? -1 : 1;
            return;
        }
        BlockPos under = getUnder();
        if (mc.world.getBlockState(under).getBlock() != Blocks.AIR) {
            lastSafePos = under;
        }
        if (event.getInput().movementForward != 1) return;
        if (!diagonal) {
            double modX = mc.player.getX() - Math.floor(mc.player.getX());
            double modZ = mc.player.getZ() - Math.floor(mc.player.getZ());
            double va = RandomUtil.nextDouble(0.2, 0.45);//RandomUtil.nextDouble(0.4, 0.49);
            double ma = 1 - va;
            switch (Direction.fromRotation(mc.player.getYaw()).toString()) {
                case "south":
                    if (modX > ma) lastSide = 1;
                    if (modX < va) lastSide = -1;
                    break;
                case "north":
                    if (modX > ma) lastSide = -1;
                    if (modX < va) lastSide = 1;
                    break;
                case "east":
                    if (modZ > ma) lastSide = -1;
                    if (modZ < va) lastSide = 1;
                    break;
                case "west":
                    if (modZ > ma) lastSide = 1;
                    if (modZ < va) lastSide = -1;
                    break;
            }
            event.getInput().movementSideways = lastSide;
        }
//        if (mc.player.raycast(1, 1f, false) instanceof BlockHitResult) {
//
//        } else {
//
//            t.println("Z");
//            boolean deltaX = Math.abs(mc.player.getX()) % 1 > 0.5;
//            boolean deltaZ = Math.abs(mc.player.getZ()) % 1 > 0.5;
//            switch (Direction.fromRotation(mc.player.getYaw()).toString()) {
//                case "south":
//                    lastSide = deltaX ? -1 : 1f;
//                    break;
//                case "north":
//                    lastSide = deltaX ? 1 : -1f;
//                    break;
//                case "east":
//                    lastSide = deltaZ ? 1 : -1f;
//                    break;
//                case "west":
//                    lastSide = deltaZ ? -1 : 1f;
//                    break;
//            }
//        }
//        if (mc.player.age % 3 == 0) {
//            event.getInput().movementForward = 0f;
//        }
//        if (posForStrafing == null) return;
//        if (mc.player.isSneaking() || !mc.player.isOnGround()) return;
//        float lastSideways = 0f;
//        if (false && event.getInput().movementForward == 1) {
//            boolean deltaX = posForStrafing == null ? Math.abs(mc.player.getX()) % 1 > 0.5 : posForStrafing.getX() + 0.5 - mc.player.getX() > 0;
//            boolean deltaZ = posForStrafing == null ? Math.abs(mc.player.getZ()) % 1 > 0.5 : posForStrafing.getZ() + 0.5 - mc.player.getZ() > 0;;
//
//            switch (Direction.fromRotation(mc.player.getYaw()).toString()) {
//                    case "south":
//                        lastSideways = deltaX ? -1 : 1f;
//                        break;
//                    case "north":
//                        lastSideways = deltaX ? 1 : -1f;
//                        break;
//                    case "east":
//                        lastSideways = deltaZ ? 1 : -1f;
//                        break;
//                    case "west":
//                        lastSideways = deltaZ ? -1 : 1f;
//                        break;
//                }
//            event.getInput().movementSideways = lastSideways;
      //  }
        super.onInput(event);
    }

    private float lastVirtualYaw;

    private float[] stdRotation(boolean sideRotation) {
        if (mc.player.getVelocity().horizontalLength() > 0.2) {
            if (data == null) {
                return new float[]{
                        mc.player.getYaw(),
                        mc.player.getPitch()
                };
            }
            var box = data.toBox();
            float[] best = null;
            Vec3d eye = mc.player.getEyePos();
            double bestDist = Double.MAX_VALUE;
            for (double x = box.minX; x <= box.maxX; x += 0.1) {
                for (double y = box.minY; y <= box.maxY; y += 0.1) {
                    for (double z = box.minZ; z <= box.maxZ; z += 0.1) {
                        float[] currentRot = RotationUtil.rotation(new Vec3d(x, y, z), eye);
                        if (!isGood(RayCastUtil.rayCast(currentRot, placeRange.getValue(), 1f), data, false)) {
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
        final float na = diagonal ? 77f : 80f;
        //final float na = 75.95f;
        GameOptions options = mc.options;
        lastVirtualYaw = RotationUtil.virtualYaw + 180;
        float stdYaw = Math.round(lastVirtualYaw / 45) * 45;
//        double modX = mc.player.getX() - Math.floor(mc.player.getX());
//        double modZ = mc.player.getZ() - Math.floor(mc.player.getZ());
//        double va = 0.499;//RandomUtil.nextDouble(0.4, 0.49);
//        double ma = 1 - va;
//        switch (Direction.fromRotation(mc.player.getYaw()).toString()) {
//            case "south":
//                if (modX > ma) stdYaw += 45;
//                if (modX < va) lastSide = -1;
//                break;
//            case "north":
//                if (modX > ma) lastSide = -1;
//                if (modX < va) stdYaw += 45;
//                break;
//            case "east":
//                if (modZ > ma) lastSide = -1;
//                if (modZ < va) stdYaw += 45;
//                break;
//            case "west":
//                if (modZ > ma) stdYaw += 45;
//                if (modZ < va) lastSide = -1;
//                break;
//        }
        if (data == null) {
            float[]x = new float[] {
                    stdYaw,
                    na
            };
            float smoothScale = 0.6f;
            x[0] = RotationUtil.smoothRot(mc.player.getYaw(), x[0], RandomUtil.nextFloat(10f, 15) * smoothScale);
            x[1] = RotationUtil.smoothRot(mc.player.getPitch(), x[1], RandomUtil.nextFloat(10f, 15) * smoothScale);
            //x[1] += (float) (Math.sin(MathHelper.wrapDegrees(mc.player.getYaw() - x[0]) / 5) * 5);
            return x;
        }
        //stdYaw += (float) Math.toDegrees(Math.atan2(lastSideways, lastForward));
        float stdPitch = na;
        diagonal = Math.abs(stdYaw % 90) > 1;
        float[] stdRot = new float[] {
                stdYaw,
                0
        };
        stdRot[0] = RotationUtil.getFixedSensitivityAngle(stdRot[0], mc.player.getYaw());
        if (false && mode.getValue().equalsIgnoreCase("Moonwalk") || mode.getValue().equalsIgnoreCase("Telly")) {
            float bestP = stdPitch, bestDist = Float.MAX_VALUE;
            for (float p = 72; p < 90; p += 0.1f) {
                stdRot[1] = p;
                stdRot[1] = RotationUtil.getFixedSensitivityAngle(stdRot[1], mc.player.getPitch());
                float currentDist = Math.abs(p - stdPitch);
                if (bestDist < currentDist) continue;
                HitResult current = RayCastUtil.rayCast(stdRot, 3, 1f);
                if (!isGood(current, data, false)) continue;
                bestP = p;
                bestDist = currentDist;
            }
            stdPitch = bestP;
        }
//        stdYaw += (float) Math.toDegrees(Math.atan2(lastSideways, lastForward));
        float[] x = new float[] {
                stdYaw,
                stdPitch
        };
        float smoothScale = mode.getValue().equalsIgnoreCase("Telly") ? 8 : 0.5f;
        if (!mode.getValue().equalsIgnoreCase("Telly") || Math.abs(MathHelper.wrapDegrees(mc.player.getYaw() - x[0])) > 80) {
            x[0] = RotationUtil.smoothRot(mc.player.getYaw(), x[0], RandomUtil.nextFloat(10f, 15) * smoothScale);
            x[1] = RotationUtil.smoothRot(mc.player.getPitch(), x[1], RandomUtil.nextFloat(10f, 15) * smoothScale);
            //x[1] += (float) (Math.sin(MathHelper.wrapDegrees(mc.player.getYaw() - x[0]) / 5) * 5);
        }
        return x;
    }

    private boolean isGood(HitResult result, BlockData data, boolean placeCheck) {
        if (result == null) return false;
        if (!(result instanceof BlockHitResult)) {
            return false;
        }
        var blockResult = (BlockHitResult) result;
//        if (placeCheck  && (!mc.player.isOnGround() && mc.player.getVelocity().y > 0.3)) {
//            return false;
//         //   return true;
//            //return mc.world.getBlockState(blockResult.getBlockPos().offset(blockResult.getSide())).getBlock() == Blocks.AIR;
//        }
        return blockResult.getSide() != Direction.UP;
        //return blockResult.getBlockPos().offset(blockResult.getSide()).toString().equalsIgnoreCase(data.getPos().offset(data.getDirection()).toString());
    }

    private BlockData getBlockData() {
        BlockPos blockPos = new BlockPos(
                (int) mc.player.getX(),
                sameYPos,
                (int) mc.player.getZ()
        ).add(0, -1, 0);
        if (mc.world.getBlockState(blockPos).getBlock() != Blocks.AIR) {
            return null;
        }
        {
            BlockPos offsetPos = blockPos;
                ;
                for (Direction facing : Direction.values()) {
                    if (mc.world.getBlockState(offsetPos.offset(facing)).getBlock() == Blocks.AIR) continue;
                    return new BlockData(offsetPos.offset(facing), invert[facing.ordinal()]);
                }
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
        placeRangeSq = 9;
        return dataEntry.stream()
                .filter(d -> mc.player.squaredDistanceTo(d.getPos().offset(d.getDirection()).toCenterPos()) < placeRangeSq)
                .min(Comparator.comparingDouble(d -> eye.squaredDistanceTo(d.getPos().offset(d.getDirection()).toCenterPos())))
                .orElse(null);
    }
}
