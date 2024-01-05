package tech.mania.core.util;

import com.mojang.authlib.GameProfile;
import net.minecraft.block.BlockState;
import net.minecraft.client.input.Input;
import net.minecraft.client.network.ClientConnectionState;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.recipebook.ClientRecipeBook;
import net.minecraft.entity.Entity;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.packet.Packet;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.stat.StatHandler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import tech.mania.MCHook;

import java.util.ArrayList;
import java.util.List;

public class PlayerUtil implements MCHook {

    public static List<Vec3d> predictPositions(Entity entity, int tick) {
        List<Vec3d> positions = new ArrayList<>();

        Input input = mc.player.input;
        Vec3d playerVelocity = AlgebraUtil.clone(mc.player.getVelocity());

        ClientPlayerEntity player = new ClientPlayerEntity(
                mc,
                mc.world,
                new ClientPlayNetworkHandler(
                        mc,
                        new ClientConnection(NetworkSide.CLIENTBOUND),
                        new ClientConnectionState(
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null
                        )
                ) {
                    @Override
                    public void sendPacket(Packet<?> packet) {
                        //super.sendPacket(packet);
                    }

                    @Override
                    public GameProfile getProfile() {
                        return mc.getNetworkHandler().getProfile();
                    }

                    @Override
                    public FeatureSet getEnabledFeatures() {
                        return mc.getNetworkHandler().getEnabledFeatures();
                    }
                },
                new StatHandler(),
                new ClientRecipeBook(),
                entity.isSneaking(),
                entity.isSprinting()
        ) {
            @Override
            public float getHealth() {
                return getMaxHealth();
            }

            @Override
            public void tickMovement() {
                fallDistance = 0;
                super.tickMovement();
            }

            @Override
            public void tick() {
                tickMovement();
            }

            @Override
            protected void fall(double heightDifference, boolean onGround, BlockState state, BlockPos landedPosition) {
                //super.fall(heightDifference, onGround, state, landedPosition);
            }

            @Override
            protected boolean isCamera() {
                return true;
            }

            @Override
            public void playSound(SoundEvent sound, float volume, float pitch) {
                //super.playSound(sound, volume, pitch);
            }

            @Override
            public void playSound(SoundEvent event, SoundCategory category, float volume, float pitch) {
                //super.playSound(event, category, volume, pitch);
            }
        };
        player.input = new Input() {
            @Override
            public void tick(boolean slowDown, float slowDownFactor) {
                movementForward = input.movementForward;
                movementSideways = input.movementSideways;

                pressingForward = input.pressingForward;
                pressingBack = input.pressingBack;

                pressingLeft = input.pressingLeft;
                pressingRight = input.pressingRight;

                jumping = input.jumping;
                sneaking = input.sneaking;

                if (slowDown) {
                    movementSideways *= slowDownFactor;
                    movementForward *= slowDownFactor;
                }
            }
        };

        player.init();
        player.copyPositionAndRotation(entity);
        player.copyFrom(entity);

        player.setOnGround(entity.isOnGround());

        for (int i = 0; i < tick; i++) {
            player.resetPosition();
            player.age++;
            player.tick();
            positions.add(player.getPos());
        }

        mc.player.setVelocity(playerVelocity);
        return positions;
    }
}
