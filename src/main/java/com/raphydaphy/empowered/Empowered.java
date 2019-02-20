package com.raphydaphy.empowered;

import com.raphydaphy.empowered.init.ModItems;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Empowered implements ModInitializer
{
	public static final String DOMAIN = "empowered";
	private static final Logger LOGGER = LogManager.getLogger();

	public static ItemGroup GROUP;

	public static Logger getLogger()
	{
		return LOGGER;
	}

	@Override
	public void onInitialize()
	{
		GROUP = FabricItemGroupBuilder.create(new Identifier(DOMAIN, "items")).icon(() -> new ItemStack(ModItems.CHANNELING_ROD)).build();
		ModItems.init();
	}
}