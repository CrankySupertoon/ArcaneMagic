package com.raphydaphy.arcanemagic.common.capabilities;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.raphydaphy.arcanemagic.api.notebook.NotebookCategory;
import com.raphydaphy.arcanemagic.common.notebook.NotebookCategories;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class NotebookInfo implements INBTSerializable<NBTTagCompound>, ICapabilityProvider
{
	// Current category selected
	public static final String tagCategory = "notebookCategory";

	// Page within the selected category
	public static final String tagPage = "notebookPage";

	// Page on the list of pages
	public static final String tagIndexPage = "notebookIndexPage";

	// If the player has ever used a notebook
	public static final String tagUsedNotebook = "usedNotebook";

	// The current search text typed into the notebook
	public static final String tagNotebookSearchKey = "notebookSearchKey";

	@CapabilityInject(NotebookInfo.class)
	public static Capability<NotebookInfo> CAP = null;

	private Map<String, Boolean> unlockedCategories = new HashMap<>();
	private int curCategory;
	private int curPage;
	private int curIndexPage;
	private boolean usedNotebook;
	private String notebookSearchKey;

	public NotebookInfo()
	{
		curCategory = 0;
		curPage = 0;
		curIndexPage = 0;
		notebookSearchKey = "";
		usedNotebook = false;
	}

	public static class DefaultInfo implements Capability.IStorage<NotebookInfo>
	{

		@Nullable
		@Override
		public NBTBase writeNBT(Capability<NotebookInfo> capability, NotebookInfo instance, EnumFacing side)
		{
			return instance.serializeNBT();
		}

		@Override
		public void readNBT(Capability<NotebookInfo> capability, NotebookInfo instance, EnumFacing side, NBTBase nbt)
		{
			instance.deserializeNBT((NBTTagCompound) nbt);
		}
	}

	public void setUsed(boolean used)
	{
		this.usedNotebook = true;
	}

	public void setPage(int page)
	{
		this.curPage = page;
	}

	public void setIndexPage(int indexPage)
	{
		this.curIndexPage = indexPage;
	}

	public void setCategory(int category)
	{
		this.curCategory = category;
	}

	public void setUnlocked(String tag)
	{
		if (unlockedCategories.containsKey(tag))
		{
			if (!unlockedCategories.get(tag))
			{
				unlockedCategories.remove(tag);
			}
		}
		unlockedCategories.put(tag, true);
	}

	public void setSearchKey(String notebookSearchKey)
	{
		if (notebookSearchKey.length() >= 1024)
		{
			notebookSearchKey = notebookSearchKey.substring(0, 1024);
		}
		this.notebookSearchKey = notebookSearchKey;
	}

	public boolean getUsed()
	{
		return this.usedNotebook;
	}

	public int getPage()
	{
		return this.curPage;
	}

	public int getIndexPage()
	{
		return this.curIndexPage;
	}

	public int getCategory()
	{
		return this.curCategory;
	}

	public String getSearchKey()
	{
		return notebookSearchKey == null ? "" : notebookSearchKey;
	}

	public boolean isUnlocked(String tag)
	{
		if (tag != null)
		{
			if (tag.equals(tagUsedNotebook))
			{
				return true;
			}
			for (String unlocked : unlockedCategories.keySet())
			{
				if (tag.equals(unlocked) && unlockedCategories.get(unlocked))
				{
					return unlockedCategories.get(unlocked);
				}
			}

		}
		return false;
	}

	public boolean isVisible(NotebookCategory cat)
	{
		if (!isUnlocked(NotebookCategories.ANCIENT_RELICS.getRequiredTag())
				&& cat.equals(NotebookCategories.UNKNOWN_REALMS))
		{
			return true;
		}
		return (isUnlocked(cat.getRequiredTag()) && isUnlocked(cat.getPrerequisiteTag()));
	}
	
	@SideOnly(Side.CLIENT)
	public boolean matchesSearchKey(NotebookCategory cat)
	{
		if (isVisible(cat))
		{
			if (getSearchKey() == null || getSearchKey().isEmpty())
			{
				return true;
			}
			else if (cat.matchesSearchKey(this))
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public NBTTagCompound serializeNBT()
	{
		NBTTagCompound tag = new NBTTagCompound();
		for (String cat : unlockedCategories.keySet())
		{
			tag.setBoolean("notebook_info_" + cat, unlockedCategories.get(cat));
		}

		tag.setBoolean(tagUsedNotebook, usedNotebook);
		tag.setInteger(tagCategory, curCategory);
		tag.setInteger(tagPage, curPage);
		tag.setInteger(tagIndexPage, curIndexPage);
		tag.setString(tagNotebookSearchKey, notebookSearchKey);

		return tag;
	}

	@Override
	public void deserializeNBT(NBTTagCompound nbt)
	{

		for (String key : nbt.getKeySet())
		{
			if (key.length() > 14 && key.substring(0, 14).equals("notebook_info_"))
			{
				unlockedCategories.put(key.substring(14), nbt.getBoolean(key));
			}
		}

		usedNotebook = nbt.getBoolean(tagUsedNotebook);
		curCategory = nbt.getInteger(tagCategory);
		curPage = nbt.getInteger(tagPage);
		curIndexPage = nbt.getInteger(tagIndexPage);
		notebookSearchKey = nbt.getString(tagNotebookSearchKey);
	}

	@Override
	public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing)
	{
		return capability == NotebookInfo.CAP;
	}

	@Nullable
	@Override
	public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing)
	{
		return capability == NotebookInfo.CAP ? NotebookInfo.CAP.cast(this) : null;
	}
}
