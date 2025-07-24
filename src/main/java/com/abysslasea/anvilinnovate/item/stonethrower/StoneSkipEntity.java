package com.abysslasea.anvilinnovate.item.stonethrower;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;

import java.util.Random;

public class StoneSkipEntity extends ThrowableItemProjectile {

    private static final EntityDataAccessor<ItemStack> DATA_ITEM_STACK = SynchedEntityData.defineId(StoneSkipEntity.class, EntityDataSerializers.ITEM_STACK);
    private static final float ROTATION_DECAY = 0.98f;

    private static final double MIN_BOUNCE_SPEED = 0.15;
    private static final double WATER_DENSITY = 0.3;
    private static final int MAX_LIFETIME = 1200;
    private static final double BOUNCE_HEIGHT = 0.05;

    private float rotationSpeed;
    private float horizontalRotation;
    private int bounces;
    private int maxBounces = -1;
    private ItemStack stack = ItemStack.EMPTY;
    private int cooldown = 0;
    private int age = 0;

    public StoneSkipEntity(EntityType<? extends StoneSkipEntity> type, Level level) {
        super(type, level);
    }

    public StoneSkipEntity(Level level, LivingEntity owner, ItemStack stack, float rotationSpeed) {
        super(ThrowingStones.STONE_SKIP.get(), owner, level);
        this.stack = stack.copy();
        this.rotationSpeed = rotationSpeed;
        this.horizontalRotation = owner.getYRot();
        this.setYRot(this.horizontalRotation);
        this.setXRot(owner.getXRot());
        this.maxBounces = generateMaxBounces();

        this.entityData.set(DATA_ITEM_STACK, this.stack);
    }

    private int generateMaxBounces() {
        Random rand = new Random();
        int r = rand.nextInt(100);
        if (r < 10) return 1;
        else if (r < 30) return 2;
        else if (r < 55) return 3;
        else if (r < 80) return 4;
        else return 5;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_ITEM_STACK, ItemStack.EMPTY);
    }

    @Override
    protected Item getDefaultItem() {
        return Items.STONE;
    }

    @Override
    public void tick() {
        super.tick();
        age++;

        if (age > MAX_LIFETIME) {
            dropAsItem();
            discard();
            return;
        }

        if (cooldown > 0) {
            cooldown--;
        }

        updateRotation();

        if (cooldown == 0 && getDeltaMovement().y < 0) {
            Vec3 start = position();
            Vec3 end = start.add(getDeltaMovement().scale(1.5));
            ClipContext context = new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.WATER, this);
            BlockHitResult hitResult = level().clip(context);
            if (hitResult.getType() == HitResult.Type.BLOCK) {
                if (level().getFluidState(hitResult.getBlockPos()).is(FluidTags.WATER)) {
                    handleWaterSkip(hitResult);
                }
            }
        }

        if (bounces >= maxBounces && cooldown == 0 && getDeltaMovement().lengthSqr() < 0.01) {
            sinkToBottom();
        }
    }

    @Override
    protected void updateRotation() {
        horizontalRotation += rotationSpeed;
        rotationSpeed *= ROTATION_DECAY;
        setYRot(horizontalRotation);
    }

    private void handleWaterSkip(BlockHitResult hitResult) {
        if (bounces >= maxBounces) {
            sinkToBottom();
            return;
        }

        Vec3 motion = getDeltaMovement();
        double speed = motion.length();

        if (speed < MIN_BOUNCE_SPEED) {
            sinkToBottom();
            return;
        }

        Vec3 waterNormal = new Vec3(0, 1, 0);

        double dot = motion.normalize().dot(waterNormal);
        double incidentAngle = Math.acos(Math.abs(dot));

        Vec3 reflected = calculateBounceVector(motion, waterNormal, incidentAngle);
        reflected = reflected.scale(1.0 - WATER_DENSITY);

        Vec3 rotationEffect = new Vec3(
                -Math.sin(Math.toRadians(horizontalRotation)) * 0.1,
                0,
                Math.cos(Math.toRadians(horizontalRotation)) * 0.1
        );
        reflected = reflected.add(rotationEffect);

        reflected = reflected.scale(0.85);

        setDeltaMovement(reflected);

        Vec3 hitPos = hitResult.getLocation();
        setPos(hitPos.x, hitPos.y + BOUNCE_HEIGHT, hitPos.z);

        bounces++;
        cooldown = 3;

        if (level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SPLASH,
                    getX(), getY(), getZ(),
                    8 + bounces * 2,
                    0.2, 0.1, 0.2,
                    0.15);

            serverLevel.sendParticles(ParticleTypes.BUBBLE,
                    getX(), getY(), getZ(),
                    12,
                    0.3, 0.01, 0.3,
                    0.1);
        }
    }

    private Vec3 calculateBounceVector(Vec3 velocity, Vec3 normal, double incidentAngle) {
        Vec3 reflection = velocity.subtract(normal.scale(2 * velocity.dot(normal)));
        double angleFactor = Math.sin(incidentAngle);
        return reflection.scale(0.7 + angleFactor * 0.5);
    }

    private void sinkToBottom() {
        if (!level().isClientSide) {
            setDeltaMovement(getDeltaMovement().multiply(0.2, 0.2, 0.2));

            if (level() instanceof ServerLevel serverLevel) {
                MinecraftServer server = serverLevel.getServer();
                server.tell(new TickTask(server.getTickCount() + 20, () -> {
                    if (!isRemoved()) {
                        serverLevel.sendParticles(ParticleTypes.BUBBLE_POP,
                                getX(), getY(), getZ(),
                                8,
                                0.1, 0.1, 0.1,
                                0.02);
                        dropAsItem();
                        discard();
                    }
                }));
            }
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);

        if (!level().isClientSide) {
            if (result.getType() == HitResult.Type.BLOCK) {
                BlockHitResult blockHit = (BlockHitResult) result;
                if (isWaterCollision(blockHit)) return;
                handleGroundCollision(blockHit);
            } else if (result.getType() == HitResult.Type.ENTITY) {
                dropAsItem();
                discard();
            }
        }
    }

    private boolean isWaterCollision(BlockHitResult hitResult) {
        return level().getFluidState(hitResult.getBlockPos()).is(FluidTags.WATER);
    }

    private void handleGroundCollision(BlockHitResult hitResult) {
        if (!level().isClientSide) {
            dropAsItem();
            discard();
        }
    }

    private void dropAsItem() {
        if (!stack.isEmpty() && !level().isClientSide) {
            ItemStack dropStack = stack.copy();
            dropStack.setCount(1);
            ItemEntity item = new ItemEntity(level(), getX(), getY(), getZ(), dropStack);
            item.setPickUpDelay(10);
            level().addFreshEntity(item);
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.put("Item", stack.save(new CompoundTag()));
        tag.putInt("MaxBounces", maxBounces);
        tag.putInt("Bounces", bounces);
        tag.putInt("Cooldown", cooldown);
        tag.putInt("Age", age);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.stack = ItemStack.of(tag.getCompound("Item"));
        this.maxBounces = tag.getInt("MaxBounces");
        this.bounces = tag.getInt("Bounces");
        this.cooldown = tag.getInt("Cooldown");
        this.age = tag.getInt("Age");
    }

    public ItemStack getItemStack() {
        ItemStack synced = entityData.get(DATA_ITEM_STACK);
        return synced.isEmpty() ? new ItemStack(Items.STONE) : synced;
    }
}
