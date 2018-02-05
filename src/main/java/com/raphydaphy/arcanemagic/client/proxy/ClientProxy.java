package com.raphydaphy.arcanemagic.client.proxy;

import java.awt.Color;

import com.raphydaphy.arcanemagic.api.anima.Anima;
import com.raphydaphy.arcanemagic.api.anima.AnimaStack;
import com.raphydaphy.arcanemagic.api.notebook.NotebookCategory;
import com.raphydaphy.arcanemagic.client.particle.ParticleAnima;
import com.raphydaphy.arcanemagic.client.particle.ParticlePos;
import com.raphydaphy.arcanemagic.client.particle.ParticleQueue;
import com.raphydaphy.arcanemagic.client.particle.ParticleRenderer;
import com.raphydaphy.arcanemagic.client.particle.ParticleUtil;
import com.raphydaphy.arcanemagic.client.render.AnalyzerTESR;
import com.raphydaphy.arcanemagic.client.render.AnimaConjurerTESR;
import com.raphydaphy.arcanemagic.client.render.AnimusMaterializerTESR;
import com.raphydaphy.arcanemagic.client.render.ArcaneForgeTESR;
import com.raphydaphy.arcanemagic.client.render.ArcaneTransfigurationTableTESR;
import com.raphydaphy.arcanemagic.client.render.InfernalSmelterTESR;
import com.raphydaphy.arcanemagic.client.toast.CategoryUnlockedToast;
import com.raphydaphy.arcanemagic.common.item.ItemIlluminator;
import com.raphydaphy.arcanemagic.common.proxy.CommonProxy;
import com.raphydaphy.arcanemagic.common.tileentity.TileEntityAnalyzer;
import com.raphydaphy.arcanemagic.common.tileentity.TileEntityAnimaConjurer;
import com.raphydaphy.arcanemagic.common.tileentity.TileEntityAnimusMaterializer;
import com.raphydaphy.arcanemagic.common.tileentity.TileEntityArcaneForge;
import com.raphydaphy.arcanemagic.common.tileentity.TileEntityArcaneTransfigurationTable;
import com.raphydaphy.arcanemagic.common.tileentity.TileEntityInfernalSmelter;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy extends CommonProxy
{
	public static ParticleRenderer particleRenderer = new ParticleRenderer();

	@Override
	public void sendAnimaSafe(AnimaStack anima, Vec3d from, Vec3d to, Vec3d toCosmetic, boolean spawnParticles)
	{
		Anima.sendAnima(Minecraft.getMinecraft().world, anima, from, to, toCosmetic, false, spawnParticles);
	}

	@Override
	public void addCategoryUnlockToast(NotebookCategory category, boolean expanded)
	{
		Minecraft.getMinecraft().getToastGui().add(new CategoryUnlockedToast(category, expanded));
	}

	@Override
	public void preInit(FMLPreInitializationEvent event)
	{
		MinecraftForge.EVENT_BUS.register(this);
	}

	@Override
	public void init(FMLInitializationEvent event)
	{
		registerColors();

		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityAnimaConjurer.class, new AnimaConjurerTESR());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityArcaneTransfigurationTable.class,
				new ArcaneTransfigurationTableTESR());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityAnalyzer.class, new AnalyzerTESR());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityAnimusMaterializer.class, new AnimusMaterializerTESR());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityInfernalSmelter.class, new InfernalSmelterTESR());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityArcaneForge.class, new ArcaneForgeTESR());
	}

	public static void registerColors()
	{
	}

	@Override
	public void spawnAnimaParticles(World world, Vec3d pos, Vec3d speed, Anima anima, Vec3d travelPos,
			boolean isCosmetic)
	{

		Minecraft.getMinecraft().effectRenderer.addEffect(
				new ParticleAnima(world, pos.x, pos.y, pos.z, speed.x, speed.y, speed.z, anima, travelPos, isCosmetic));
	}

	@Override
	public void addIlluminatorParticle(ItemIlluminator item, World world, BlockPos pos, EnumFacing facing, float hitX,
			float hitY, float hitZ)
	{
		if (world.isRemote)
		{
			ParticleQueue.getInstance().addParticle(world, new ParticlePos(pos, facing, hitX, hitY, hitZ));
		}
	}

	@Override
	public String translate(String key, Object... args)
	{
		return I18n.format(key, args);
	}

	@Override
	public void magicParticle(BlockPos from, BlockPos to)
	{
		Color color = Color.GREEN;
		World world = Minecraft.getMinecraft().world;

		if (world != null)
		{

			float distX = from.getX() - to.getX();
			float vx = 0.01625f * distX;

			float distZ = from.getZ() - to.getZ();
			float vz = 0.01625f * distZ;

			float alpha = Math.min(Math.max(world.rand.nextFloat(), 0.25f), 0.30f);
			ParticleUtil.spawnParticleGlowTest(world, to.getX() + .5f, to.getY() + .8f, to.getZ() + 0.5f, vx, .053f, vz,
					color.getRed() / 256f, color.getGreen() / 256f, color.getBlue() / 256f, alpha,
					Math.min(Math.max(world.rand.nextFloat() * 6, 1.5f), 2), (int) (111));
		}
	}
}
