package com.dream.game.handler.skill;

import com.dream.Config;
import com.dream.Message;
import com.dream.game.datatables.xml.ZoneTable;
import com.dream.game.geodata.GeoData;
import com.dream.game.handler.ISkillHandler;
import com.dream.game.model.L2Skill;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.itemcontainer.Inventory;
import com.dream.game.model.zone.L2Zone;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.InventoryUpdate;
import com.dream.game.network.serverpackets.ItemList;
import com.dream.game.templates.item.L2Weapon;
import com.dream.game.templates.item.L2WeaponType;
import com.dream.game.templates.skills.L2SkillType;
import com.dream.game.util.Util;
import com.dream.tools.random.Rnd;

public class Fishing implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS =
	{
		L2SkillType.FISHING
	};

	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}

	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Character... targets)
	{
		if (!(activeChar instanceof L2PcInstance))
			return;

		L2PcInstance player = (L2PcInstance) activeChar;

		if (!Config.ALLOW_FISHING)
		{
			player.sendMessage(Message.getMessage(player, Message.MessageId.MSG_FISHING_IS_NOT_ALLOWED));
			return;
		}
		if (player.isFishing())
		{
			if (player.getFishCombat() != null)
			{
				player.getFishCombat().doDie(false);
			}
			else
			{
				player.endFishing(false);
			}
			player.sendPacket(SystemMessageId.FISHING_ATTEMPT_CANCELLED);
			return;
		}
		if (player.isInBoat())
		{
			player.sendPacket(SystemMessageId.CANNOT_FISH_ON_BOAT);
			return;
		}
		if (!player.isInsideZone(L2Zone.FLAG_FISHING) || player.isInsideZone(L2Zone.FLAG_PEACE))
		{
			player.sendPacket(SystemMessageId.CANNOT_FISH_HERE);
			return;
		}
		if (player.isInWater())
		{
			player.sendPacket(SystemMessageId.CANNOT_FISH_UNDER_WATER);
			return;
		}

		int d = Rnd.get(50) + 150;
		double angle = Util.convertHeadingToDegree(player.getHeading());
		double radian = Math.toRadians(angle);

		int dx = (int) (d * Math.cos(radian));
		int dy = (int) (d * Math.sin(radian));

		int x = activeChar.getX() + dx;
		int y = activeChar.getY() + dy;

		L2Zone water = ZoneTable.getInstance().isInsideZone(L2Zone.ZoneType.Water, x, y);
		if (water == null)
		{
			player.sendPacket(SystemMessageId.CANNOT_FISH_HERE);
			return;
		}
		boolean isHotSpringZone = false;
		if (water.getName().equalsIgnoreCase("24_14_water"))
		{
			isHotSpringZone = true;
		}

		int z = water.getMaxZ(x, y, activeChar.getZ());

		if (Config.GEODATA && !GeoData.getInstance().canSeeTarget(activeChar.getX(), activeChar.getY(), activeChar.getZ(), x, y, z) || !Config.GEODATA && Util.calculateDistance(activeChar.getX(), activeChar.getY(), activeChar.getZ(), x, y, z, true) > d * 1.73)
		{
			player.sendPacket(SystemMessageId.CANNOT_FISH_HERE);
			return;
		}
		if (player.isInCraftMode() || player.isInStoreMode())
		{
			player.sendPacket(SystemMessageId.CANNOT_FISH_WHILE_USING_RECIPE_BOOK);
			return;
		}
		L2Weapon weaponItem = player.getActiveWeaponItem();
		if (weaponItem == null || weaponItem.getItemType() != L2WeaponType.ROD)
		{
			player.sendPacket(SystemMessageId.FISHING_POLE_NOT_EQUIPPED);
			return;
		}
		L2ItemInstance lure = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND);
		if (lure == null)
		{
			player.sendPacket(SystemMessageId.BAIT_ON_HOOK_BEFORE_FISHING);
			return;
		}
		player.setLure(lure);
		L2ItemInstance lure2 = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND);

		if (lure2 == null || lure2.getCount() < 1)
		{
			player.sendPacket(SystemMessageId.NOT_ENOUGH_BAIT);
			player.sendPacket(new ItemList(player, false));
		}
		else
		{
			lure2 = player.getInventory().destroyItem("Consume", player.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_LHAND), 1, player, null);
			InventoryUpdate iu = new InventoryUpdate();
			iu.addModifiedItem(lure2);
			player.sendPacket(iu);
		}

		player.startFishing(x, y, z + 10, isHotSpringZone);
	}
}