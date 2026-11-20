package com.rexthar.desertminingreborn;

import com.mojang.logging.LogUtils;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;

/**
 * Entry point of Desert Mining Reborn.
 *
 * <p>The mod registers its content through {@link DeferredRegister} instances owned by this class, so
 * every registry is created once and registered on the mod event bus from the constructor.</p>
 */
@Mod(DesertMiningReborn.MOD_ID)
public final class DesertMiningReborn {

    /** Mod id used as the resource namespace. It must stay lowercase and dash-free. */
    public static final String MOD_ID = "desert_mining_reborn";

    private static final Logger LOGGER = LogUtils.getLogger();

    /** Block registry. Entries are added by the content classes. */
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MOD_ID);

    /** Item registry. Entries are added by the content classes. */
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);

    /**
     * Registers the mod content on the mod event bus.
     *
     * @param context mod loading context injected by Forge
     */
    public DesertMiningReborn(FMLJavaModLoadingContext context) {
        IEventBus modBus = context.getModEventBus();
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        LOGGER.debug("{} initialized", MOD_ID);
    }
}
