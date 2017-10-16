package com.raphydaphy.arcanemagic.common.tileentity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.raphydaphy.arcanemagic.api.ArcaneMagicAPI;
import com.raphydaphy.arcanemagic.api.essence.Essence;
import com.raphydaphy.arcanemagic.api.essence.EssenceStack;
import com.raphydaphy.arcanemagic.api.notebook.NotebookCategory;
import com.raphydaphy.arcanemagic.common.capabilities.NotebookInfo;
import com.raphydaphy.arcanemagic.common.entity.EntityItemFancy;
import com.raphydaphy.arcanemagic.common.handler.ArcaneMagicPacketHandler;
import com.raphydaphy.arcanemagic.common.init.ModRegistry;
import com.raphydaphy.arcanemagic.common.item.ItemParchment;
import com.raphydaphy.arcanemagic.common.network.PacketNotebookToast;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class TileEntityAnalyzer extends TileEntityEssenceStorage implements ITickable
{
	// TODO: make this a nonnulllist
	private ItemStack[] stacks = { ItemStack.EMPTY, ItemStack.EMPTY };

	private int age = 0;
	private boolean hasValidStack = false;

	private UUID stackOwner = null;

	public TileEntityAnalyzer()
	{
		super(200);
	}

	public ItemStack[] getStacks()
	{
		return stacks;
	}

	public void setPlayer(EntityPlayer player)
	{
		if (player != null)
		{
			this.stackOwner = player.getUniqueID();
		} else
		{
			this.stackOwner = null;
		}
		markDirty();
	}

	public void setStack(int stack, ItemStack item)
	{
		this.stacks[stack] = item;

		if (stack == 0)
		{
			this.age = 0;

			if (item != null && !item.isEmpty()
					&& ArcaneMagicAPI.getFromAnalysis(getStacks()[0].copy(), new ArrayList<>()).size() > 0)
			{
				hasValidStack = true;
			} else
			{
				hasValidStack = false;
			}
		}

		markDirty();

		if (world != null)
		{
			IBlockState state = world.getBlockState(this.pos);
			world.notifyBlockUpdate(pos, state, state, 3);
		}
	}

	@Override
	public void markDirty()
	{
		super.markDirty();
		if (TileEntityAnalyzer.this.world != null && TileEntityAnalyzer.this.pos != null)
		{
			IBlockState state = TileEntityAnalyzer.this.world.getBlockState(TileEntityAnalyzer.this.pos);
			TileEntityAnalyzer.this.world.markAndNotifyBlock(TileEntityAnalyzer.this.pos,
					TileEntityAnalyzer.this.world.getChunkFromBlockCoords(TileEntityAnalyzer.this.pos), state, state,
					1 | 2);
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound compound)
	{
		super.readFromNBT(compound);
		stacks = new ItemStack[2];
		if (compound.hasKey("analyzingStack"))
		{
			stacks[0] = new ItemStack(compound.getCompoundTag("analyzingStack"));
		}
		if (compound.hasKey("parchmentStack"))
		{
			stacks[1] = new ItemStack(compound.getCompoundTag("parchmentStack"));
		}
		age = compound.getInteger("age");

		if (compound.hasKey("stackOwner"))
		{
			stackOwner = compound.getUniqueId("stackOwner");
		}

		hasValidStack = compound.getBoolean("hasValidStack");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound)
	{
		super.writeToNBT(compound);
		if (!stacks[0].isEmpty())
		{
			NBTTagCompound tagCompound = new NBTTagCompound();
			stacks[0].writeToNBT(tagCompound);
			compound.setTag("analyzingStack", tagCompound);
		}
		if (!stacks[1].isEmpty())
		{
			NBTTagCompound tagCompound = new NBTTagCompound();
			stacks[1].writeToNBT(tagCompound);
			compound.setTag("parchmentStack", tagCompound);
		}
		compound.setInteger("age", age);
		if (stackOwner != null)
		{
			compound.setUniqueId("stackOwner", stackOwner);
		}

		compound.setBoolean("hasValidStack", hasValidStack);
		return compound;
	}

	public int getAge()
	{
		return age;
	}

	public boolean canInteractWith(EntityPlayer playerIn)
	{
		// If we are too far away from this tile entity you cannot use it
		return !isInvalid() && playerIn.getDistanceSq(pos.add(0.5D, 0.5D, 0.5D)) <= 64D;
	}

	@Override
	public void update()
	{
		age++;

		if (hasValidStack)
		{
			if (getStacks()[1] != null && !getStacks()[1].isEmpty() && !world.isRemote)
			{
				if ((essenceStorage.getTotalStored() >= 200) && stackOwner != null)
				{

					// just reached 200
					if (age > 0)
					{
						age = -200;
					}
					if (age == -50)
					{
						List<NotebookCategory> unlockable = analyze(world.getPlayerEntityByUUID(stackOwner));
						ItemStack writtenParchment = new ItemStack(ModRegistry.WRITTEN_PARCHMENT, 1);
						if (!writtenParchment.hasTagCompound())
						{
							writtenParchment.setTagCompound(new NBTTagCompound());
						}
						writtenParchment.getTagCompound().setString(ItemParchment.TITLE,
								unlockable.get(0).getUnlocParchmentInfo().first());
						writtenParchment.getTagCompound().setString(ItemParchment.DESC,
								unlockable.get(0).getUnlocParchmentInfo().first());
						writtenParchment.getTagCompound().setInteger(ItemParchment.PARAGRAPHS,
								unlockable.get(0).getUnlocParchmentInfo().second());

						EntityItemFancy parchmentEntity = new EntityItemFancy(world, pos.getX() + 0.5, pos.getY() + 1.5,
								pos.getZ() + 0.5, writtenParchment);
						parchmentEntity.motionX = 0;
						parchmentEntity.motionY = 0;
						parchmentEntity.motionZ = 0;
						world.spawnEntity(parchmentEntity);
						for (EssenceStack e : essenceStorage.getStored().values())
						{
							essenceStorage.take(e, false);
						}
						setStack(1, ItemStack.EMPTY);

						age = 0;
					}
				}
				for (int x = pos.getX() - 10; x < pos.getX() + 10; x++)
				{
					for (int y = pos.getY() - 5; y < pos.getY() + 5; y++)
					{
						for (int z = pos.getZ() - 10; z < pos.getZ() + 10; z++)
						{
							if (world.rand.nextInt(2000) == 1)
							{
								BlockPos here = new BlockPos(x, y, z);
								if (world.getBlockState(here).getBlock().equals(Blocks.BEDROCK))
								{
									// Send some essence to the parchment for ink
									Essence.sendEssence(world,
											new EssenceStack(Essence.getFromBiome(world.getBiome(here)), 1),
											new Vec3d(x + 0.5, y + 0.5, z + 0.5),
											new Vec3d(pos.getX() + 0.5, pos.getY() + 0.7, pos.getZ() + 0.5),
											new Vec3d(pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5), false,
											true);

								}
							}
						}
					}
				}
			} else if (world.isRemote && world.rand.nextInt(3) == 1)
			{
				world.spawnParticle(EnumParticleTypes.PORTAL, pos.getX() + 0.4 + (world.rand.nextFloat() / 5),
						pos.getY() + 0.7, pos.getZ() + 0.4 + (world.rand.nextFloat() / 5), 0, -0.5, 0);
			}

		} else if (getStacks()[1] != null && !getStacks()[1].isEmpty() && !world.isRemote)
		{
			EntityItem blankParchment = new EntityItem(world, pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5,
					getStacks()[1].copy());
			blankParchment.motionX = 0;
			blankParchment.motionY = 0;
			blankParchment.motionZ = 0;
			world.spawnEntity(blankParchment);
			setStack(1, ItemStack.EMPTY);

			for (EssenceStack e : essenceStorage.getStored().values())
			{
				essenceStorage.take(e, false);
			}

		}
		this.markDirty();
	}

	public List<NotebookCategory> analyze(EntityPlayer player)
	{
		if (player.world.isRemote)
		{
			return new ArrayList<>();
		}
		if (getStacks()[0] != null && !getStacks()[0].isEmpty())
		{
			NotebookInfo info = player.getCapability(NotebookInfo.CAP, null);

			if (info != null && info.getUsed())
			{
				List<NotebookCategory> unlockable = ArcaneMagicAPI.getFromAnalysis(getStacks()[0].copy(),
						new ArrayList<>());
				if (essenceStorage.getTotalStored() >= 200)
				{
					System.out.println(essenceStorage.getTotalStored());
					for (NotebookCategory unlockableCat : unlockable)
					{
						if (unlockableCat != null)
						{
							if (!info.isUnlocked(unlockableCat.getRequiredTag()))
							{
								if (info.isUnlocked(unlockableCat.getPrerequisiteTag()))
								{
									info.setUnlocked(unlockableCat.getRequiredTag());

									ArcaneMagicPacketHandler.INSTANCE.sendTo(new PacketNotebookToast(unlockableCat),
											(EntityPlayerMP) player);
								}
							}
						}
					}
				}
				return unlockable;
			}
		}
		return new ArrayList<>();
	}
}
