package com.abysslasea.anvilinnovate.mixin;

import net.minecraft.world.level.block.AnvilBlock;

@org.spongepowered.asm.mixin.Mixin(AnvilBlock.class)
public class KMixin {
    private void someMethod() {} // 虚拟方法
}