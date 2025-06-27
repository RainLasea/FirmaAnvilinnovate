package com.abysslasea.anvilinnovate.block;

import com.abysslasea.anvilinnovate.block.flint.CarvingFlintSlabBlockEntity;

import com.abysslasea.anvilinnovate.block.flint.CarvingFlintSlabBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, "anvilinnovate");

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, "anvilinnovate");

    public static final RegistryObject<Block> CARVING_SLAB = BLOCKS.register(
            "carving_slab",
            CarvingFlintSlabBlock::new
    );

    public static final RegistryObject<BlockEntityType<CarvingFlintSlabBlockEntity>> CARVING_SLAB_BE =
            BLOCK_ENTITIES.register("carving_slab", () ->
                    BlockEntityType.Builder.of(
                            CarvingFlintSlabBlockEntity::new,
                            CARVING_SLAB.get()
                    ).build(null)
            );
}