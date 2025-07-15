package com.abysslasea.anvilinnovate.item.stonethrower;

import com.abysslasea.anvilinnovate.Anvilinnovate;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ThrowingStones {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, Anvilinnovate.MODID);

    public static final RegistryObject<EntityType<StoneSkipEntity>> STONE_SKIP = ENTITY_TYPES.register("stone_skip",
            () -> EntityType.Builder.<StoneSkipEntity>of(StoneSkipEntity::new, MobCategory.MISC)
                    .sized(0.25f, 0.25f)
                    .clientTrackingRange(4)
                    .updateInterval(10)
                    .build("stone_skip"));
}
