package com.raphydaphy.arcanemagic.intergration;

import com.raphydaphy.arcanemagic.ArcaneMagic;
import com.raphydaphy.arcanemagic.init.ModRegistry;
import com.raphydaphy.arcanemagic.recipe.TransfigurationRecipe;
import me.shedaniel.rei.api.IItemRegisterer;
import me.shedaniel.rei.api.IRecipeHelper;
import me.shedaniel.rei.api.IRecipePlugin;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;

public class REIPlugin implements IRecipePlugin
{
	static final Identifier INFUSION = new Identifier(ArcaneMagic.DOMAIN, "plugins/infusion");
	static final Identifier TRANSFIGURATION = new Identifier(ArcaneMagic.DOMAIN, "plugins/transfiguration");

	@Override
	public void registerItems(IItemRegisterer itemRegisterer)
	{

	}

	@Override
	public void registerPluginCategories(IRecipeHelper recipeHelper)
	{
		recipeHelper.registerCategory(new InfusionCategory());
		recipeHelper.registerCategory(new TransfigurationCategory());
	}

	@Override
	public void registerRecipeDisplays(IRecipeHelper recipeHelper)
	{
		recipeHelper.registerDisplay(INFUSION, new InfusionDisplay(new ItemStack(Blocks.BOOKSHELF), new ItemStack(ModRegistry.TRANSFIGURATION_TABLE), 15));
		recipeHelper.registerDisplay(INFUSION, new InfusionDisplay(new ItemStack(Items.EMERALD), new ItemStack(ModRegistry.EMERALD_CRYSTAL), 8));
		recipeHelper.registerDisplay(INFUSION, new InfusionDisplay(new ItemStack(Items.DIAMOND), new ItemStack(ModRegistry.DIAMOND_CRYSTAL), 8));
		recipeHelper.registerDisplay(INFUSION, new InfusionDisplay(new ItemStack(Items.GOLD_INGOT), new ItemStack(ModRegistry.GOLD_CRYSTAL), 7));
		recipeHelper.registerDisplay(INFUSION, new InfusionDisplay(new ItemStack(Items.REDSTONE), new ItemStack(ModRegistry.REDSTONE_CRYSTAL), 7));
		recipeHelper.registerDisplay(INFUSION, new InfusionDisplay(new ItemStack(Items.LAPIS_LAZULI), new ItemStack(ModRegistry.LAPIS_CRYSTAL), 6));
		recipeHelper.registerDisplay(INFUSION, new InfusionDisplay(new ItemStack(Items.COAL), new ItemStack(ModRegistry.COAL_CRYSTAL), 6));

		for (TransfigurationRecipe recipe : ModRegistry.TRANSFIGURATION_RECIPES)
		{
			recipeHelper.registerDisplay(TRANSFIGURATION, new TransfigurationDisplay(recipe));
		}
	}

	@Override
	public void registerSpeedCraft(IRecipeHelper recipeHelper)
	{
		recipeHelper.registerSpeedCraftButtonArea(INFUSION, null);
		recipeHelper.registerSpeedCraftButtonArea(TRANSFIGURATION, null);
	}
}