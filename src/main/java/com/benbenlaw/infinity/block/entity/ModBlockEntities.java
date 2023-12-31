package com.benbenlaw.infinity.block.entity;

import com.benbenlaw.infinity.Infinity;
import com.benbenlaw.infinity.block.ModBlocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Infinity.MOD_ID);

    public static final RegistryObject<BlockEntityType<InfinityGeneratorBlockEntity>> INFINITY_GENERATOR_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("infinity_generator_block_entity", () ->
                    BlockEntityType.Builder.of(InfinityGeneratorBlockEntity::new,
                            ModBlocks.INFINITY_GENERATOR.get()).build(null));






    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }

}
