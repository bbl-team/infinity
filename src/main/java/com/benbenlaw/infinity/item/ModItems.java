package com.benbenlaw.infinity.item;

import com.benbenlaw.infinity.Infinity;
import net.minecraft.core.Direction;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.HangingSignItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SignItem;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, Infinity.MOD_ID);

    //New Items


    public static final RegistryObject<Item> DETECTOR = ITEMS.register("detector", DetectorItem::new);

    public static final RegistryObject<Item> HEAT_COIL = ITEMS.register("heat_coil",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> WOODEN_TURBINE = ITEMS.register("wooden_turbine",
            () -> new Item(new Item.Properties().durability(64)));

    public static final RegistryObject<Item> IRON_TURBINE = ITEMS.register("iron_turbine",
            () -> new Item(new Item.Properties().durability(128)));

    public static final RegistryObject<Item> DIAMOND_TURBINE = ITEMS.register("diamond_turbine",
            () -> new Item(new Item.Properties().durability(256)));








    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }

}
