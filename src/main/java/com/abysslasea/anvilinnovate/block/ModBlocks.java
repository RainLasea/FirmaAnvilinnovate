package com.abysslasea.anvilinnovate.block;

import com.abysslasea.anvilinnovate.block.clay.ClayFormingBlock;
import com.abysslasea.anvilinnovate.block.clay.ClayFormingBlockEntity;
import com.abysslasea.anvilinnovate.block.flint.ChiseledFlintSlabBlock;
import com.abysslasea.anvilinnovate.block.flint.ChiseledFlintSlabBlockEntity;
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
            ChiseledFlintSlabBlock::new
    );

    public static final RegistryObject<BlockEntityType<ChiseledFlintSlabBlockEntity>> CARVING_SLAB_BE =
            BLOCK_ENTITIES.register("carving_slab", () ->
                    BlockEntityType.Builder.of(
                            ChiseledFlintSlabBlockEntity::new,
                            CARVING_SLAB.get()
                    ).build(null)
            );

    public static final RegistryObject<Block> CLAY_FORMING = BLOCKS.register(
            "clay_forming",
            ClayFormingBlock::new
    );

    public static final RegistryObject<BlockEntityType<ClayFormingBlockEntity>> CLAY_FORMING_BE =
            BLOCK_ENTITIES.register("clay_forming", () ->
                    BlockEntityType.Builder.of(
                            ClayFormingBlockEntity::new,
                            CLAY_FORMING.get()
                    ).build(null)
            );
}