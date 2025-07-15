package com.abysslasea.anvilinnovate.item.stonethrower;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.BlockPos;

import java.util.Random;

public class StoneSkipEntity extends ThrowableItemProjectile {
    private ItemStack stack = ItemStack.EMPTY;
    private int skips = 0;
    private final int maxSkips;
    private int bounceCooldown = 0;

    private static final Random RANDOM = new Random();

    public StoneSkipEntity(EntityType<? extends StoneSkipEntity> type, Level world) {
        super(type, world);
        this.maxSkips = generateMaxSkips();
    }

    public StoneSkipEntity(EntityType<? extends StoneSkipEntity> type,
                           Level world,
                           LivingEntity shooter,
                           ItemStack stack) {
        super(type, shooter, world);
        this.stack = stack.copy();
        this.maxSkips = generateMaxSkips();
    }

    private int generateMaxSkips() {
        int r = RANDOM.nextInt(100);
        if (r < 10) return 1;
        if (r < 50) return 2;
        if (r < 75) return 3;
        if (r < 95) return 4;
        return 5;
    }

    public ItemStack getItemStack() {
        return stack;
    }

    public void setStack(ItemStack stack) {
        this.stack = stack.copy();
    }

    @Override
    protected Item getDefaultItem() {
        return stack.isEmpty() ? Items.STONE : stack.getItem();
    }

    @Override
    public void tick() {
        super.tick();

        if (bounceCooldown > 0) {
            bounceCooldown--;
        }

        if (!level().isClientSide && this.isInWater()) {
            Vec3 vel = this.getDeltaMovement();

            boolean isTouchingGround = isTouchingSolidBelow();

            boolean hasBouncesLeft = skips < maxSkips;
            boolean movingDown = vel.y < 0;
            boolean canBounce = bounceCooldown == 0;

            if (hasBouncesLeft && movingDown && canBounce && !isTouchingGround) {
                double bounceFactor = 0.85;
                double newY = -vel.y * bounceFactor + 0.1;
                double newX = vel.x * 0.8;
                double newZ = vel.z * 0.8;

                this.setDeltaMovement(newX, newY, newZ);

                this.setPos(this.getX(), this.getY() + 0.1, this.getZ());

                skips++;
                bounceCooldown = 2;

                if (level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.SPLASH,
                            this.getX(), this.getY(), this.getZ(),
                            5,
                            0.1, 0.1, 0.1,
                            0.05);
                }
                return;
            }

            if (isTouchingGround || skips >= maxSkips) {
                if (!level().isClientSide) {
                    ItemStack dropStack = new ItemStack(stack.getItem(), 1);
                    level().addFreshEntity(new ItemEntity(level(), getX(), getY(), getZ(), dropStack));
                    this.discard();
                }
            }
        }
    }

    private boolean isTouchingSolidBelow() {
        double bottomY = this.getBoundingBox().minY;
        int xPos = (int) Math.floor(this.getX());
        int yPos = (int) Math.floor(bottomY - 0.01);
        int zPos = (int) Math.floor(this.getZ());

        BlockPos posBelow = new BlockPos(xPos, yPos, zPos);
        BlockState blockState = level().getBlockState(posBelow);

        return blockState.isSolid() && !blockState.getFluidState().is(FluidTags.WATER);
    }

    @Override
    protected void onHit(net.minecraft.world.phys.HitResult result) {
        super.onHit(result);
    }
}
