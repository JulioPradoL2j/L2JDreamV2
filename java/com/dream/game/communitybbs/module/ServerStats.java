package com.dream.game.communitybbs.module;

import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.itemcontainer.Inventory;
import com.dream.game.model.world.L2World;

/**
 * @author Matim
 */
public class ServerStats
{
	public static String getAverageArmorEnchant()
	{
		int playerSize = L2World.getInstance().getAllPlayersCount();
		int enchantLevel = 0;

		for (L2PcInstance player : L2World.getInstance().getAllPlayers())
		{
			if (player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_CHEST) != null)
			{
				enchantLevel += player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_CHEST).getEnchantLevel();
			}
			if (player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LEGS) != null)
			{
				enchantLevel += player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LEGS).getEnchantLevel();
			}
			if (player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_GLOVES) != null)
			{
				enchantLevel += player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_GLOVES).getEnchantLevel();
			}

		}

		int averageEnchant = enchantLevel / (playerSize * 3);
		return String.valueOf(averageEnchant);
	}

	public static String getAverageJewelsEnchant()
	{
		int playerSize = L2World.getInstance().getAllPlayersCount();
		int enchantLevel = 0;

		for (L2PcInstance player : L2World.getInstance().getAllPlayers())
		{
			if (player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LEAR) != null)
			{
				enchantLevel += player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LEAR).getEnchantLevel();
			}
			if (player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LFINGER) != null)
			{
				enchantLevel += player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LFINGER).getEnchantLevel();
			}
			if (player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_NECK) != null)
			{
				enchantLevel += player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_NECK).getEnchantLevel();
			}

		}

		int averageEnchant = enchantLevel / (playerSize * 3);
		return String.valueOf(averageEnchant);
	}

	public static String getAverageWeaponEnchant()
	{
		int playerSize = L2World.getInstance().getAllPlayersCount();
		int enchantLevel = 0;

		for (L2PcInstance player : L2World.getInstance().getAllPlayers())
			if (player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND) != null)
			{
				enchantLevel += player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND).getEnchantLevel();
			}
			else if (player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND) != null)
			{
				enchantLevel += player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND).getEnchantLevel();
			}

		int averageEnchant = enchantLevel / playerSize;
		return String.valueOf(averageEnchant);
	}

	public static String getStat(String stat)
	{
		int playerSize = L2World.getInstance().getAllPlayersCount();
		int value = 0;
		if (stat.equals("pdef"))
		{
			for (L2PcInstance player : L2World.getInstance().getAllPlayers())
			{
				value += player.getPDef(null);
			}
		}
		else if (stat.equals("mdef"))
		{
			for (L2PcInstance player : L2World.getInstance().getAllPlayers())
			{
				value += player.getMDef(null, null);
			}
		}
		else if (stat.equals("patk"))
		{
			for (L2PcInstance player : L2World.getInstance().getAllPlayers())
				if (player.getClassId().isMage())
				{
					playerSize--;
				}
				else
				{
					value += player.getPAtk(null);
				}
		}
		else if (stat.equals("matk"))
		{
			for (L2PcInstance player : L2World.getInstance().getAllPlayers())
				if (player.getClassId().isMage())
				{
					value += player.getMAtk(null, null);
				}
				else
				{
					playerSize--;
				}
		}
		else if (stat.equals("castspeed"))
		{
			for (L2PcInstance player : L2World.getInstance().getAllPlayers())
				if (player.getClassId().isMage())
				{
					value += player.getMAtkSpd();
				}
				else
				{
					playerSize--;
				}
		}
		else if (stat.equals("attackspeed"))
		{
			for (L2PcInstance player : L2World.getInstance().getAllPlayers())
				if (player.getClassId().isMage())
				{
					playerSize--;
				}
				else
				{
					value += player.getPAtkSpd();
				}
		}
		else if (stat.equals("runspeed"))
		{
			for (L2PcInstance player : L2World.getInstance().getAllPlayers())
			{
				value += player.getRunSpeed();
			}
		}

		if (playerSize == 0)
			return "N/A";
		int average = value / playerSize;
		return String.valueOf(average);
	}

	public String getOnlineCount()
	{
		String online = "Players Online: <font color=\"9CC300\">" + L2World.getInstance().getAllPlayersCount() + "</font>";
		return online;
	}

	public String getServerStats()
	{
		String stats = "<br>Average Weapon Enchant: <font color=\"LEVEL\">" + getAverageWeaponEnchant() + "</font>" + "<br>Average Armor Enchant: <font color=\"LEVEL\">" + getAverageArmorEnchant() + "</font>" + "<br>Average Jewel Enchant: <font color=\"LEVEL\">" + getAverageJewelsEnchant() + "</font>" + "<br>" + "<br>Average pDef: <font color=\"LEVEL\">" + getStat("pdef") + "</font>" + "<br>Average mDef: <font color=\"LEVEL\">" + getStat("mdef") + "</font>" + "<br>Average pAtk: <font color=\"LEVEL\">" + getStat("patk") + "</font>" + "<br>Average mAtk: <font color=\"LEVEL\">" + getStat("mdef") + "</font>" + "<br>Average casting speed: <font color=\"LEVEL\">" + getStat("castspeed") + "</font>" + "<br>Average melee speed: <font color=\"LEVEL\">" + getStat("attackspeed") + "</font>" + "<br>Average run speed: <font color=\"LEVEL\">" + getStat("runspeed") + "</font>";
		return stats;
	}
}