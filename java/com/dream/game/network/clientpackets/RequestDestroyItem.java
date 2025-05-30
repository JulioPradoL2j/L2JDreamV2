package com.dream.game.network.clientpackets;

import com.dream.Config;
import com.dream.L2DatabaseFactory;
import com.dream.game.datatables.xml.PetDataTable;
import com.dream.game.manager.CursedWeaponsManager;
import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.world.L2World;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.ActionFailed;
import com.dream.game.network.serverpackets.InventoryUpdate;
import com.dream.game.network.serverpackets.ItemList;
import com.dream.game.network.serverpackets.StatusUpdate;
import com.dream.game.util.Util;

import java.sql.Connection;
import java.sql.PreparedStatement;

import org.apache.log4j.Logger;

public class RequestDestroyItem extends L2GameClientPacket
{
	public static Logger _log = Logger.getLogger(RequestDestroyItem.class.getName());

	private int _objectId, _count;

	@Override
	protected void readImpl()
	{
		_objectId = readD();
		_count = readD();
	}

	
	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;

		if (_count < 0)
		{
			Util.handleIllegalPlayerAction(activeChar, "Player " + activeChar.getName() + " trying to delete things, amount < 0", Config.DEFAULT_PUNISH);
			return;
		}

		int count = _count;

		if (activeChar.getPrivateStoreType() != 0)
		{
			activeChar.sendPacket(SystemMessageId.CANNOT_TRADE_DISCARD_DROP_ITEM_WHILE_IN_SHOPMODE);
			return;
		}

		L2ItemInstance itemToRemove = activeChar.getInventory().getItemByObjectId(_objectId);

		if (itemToRemove == null)
		{
			activeChar.sendPacket(SystemMessageId.CANNOT_DISCARD_THIS_ITEM);
			return;
		}

		if (activeChar.isCastingNow())
			if (activeChar.getCurrentSkill() != null && activeChar.getCurrentSkill().getSkill().getItemConsumeId() == itemToRemove.getItemId())
			{
				activeChar.sendPacket(SystemMessageId.CANNOT_DISCARD_THIS_ITEM);
				return;
			}

		if (activeChar.isCastingSimultaneouslyNow())
			if (activeChar.getLastSimultaneousSkillCast() != null && activeChar.getLastSimultaneousSkillCast().getItemConsumeId() == itemToRemove.getItemId())
			{
				activeChar.sendPacket(SystemMessageId.CANNOT_DISCARD_THIS_ITEM);
				return;
			}

		if (activeChar.getSecondRefusal())
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		int itemId = itemToRemove.getItemId();
		if (itemToRemove.isWear() || !itemToRemove.isDestroyable() && !activeChar.isGM() || CursedWeaponsManager.getInstance().isCursed(itemId) && !activeChar.isGM())
		{
			if (itemToRemove.isHeroItem())
			{
				activeChar.sendPacket(SystemMessageId.HERO_WEAPONS_CANT_DESTROYED);
			}
			else
			{
				activeChar.sendPacket(SystemMessageId.CANNOT_DISCARD_THIS_ITEM);
			}
			return;
		}

		if (Config.ALT_STRICT_HERO_SYSTEM)
			if (itemToRemove.isHeroItem() && !activeChar.isGM())
			{
				activeChar.sendPacket(SystemMessageId.HERO_WEAPONS_CANT_DESTROYED);
				return;
			}

		if (!itemToRemove.isStackable() && count > 1)
		{
			Util.handleIllegalPlayerAction(activeChar, "Remove one things, amount, name of player 1 > " + activeChar.getName() + ".", Config.DEFAULT_PUNISH);
			return;
		}

		if (_count > itemToRemove.getCount())
		{
			count = itemToRemove.getCount();
		}

		if (itemToRemove.isEquipped())
		{
			L2ItemInstance[] unequiped = activeChar.getInventory().unEquipItemInSlotAndRecord(itemToRemove.getLocationSlot());
			InventoryUpdate iu = new InventoryUpdate();
			for (L2ItemInstance element : unequiped)
			{
				activeChar.checkSSMatch(null, element);

				iu.addModifiedItem(element);
			}
			activeChar.sendPacket(iu);
			activeChar.broadcastUserInfo();
		}

		if (PetDataTable.isPetItem(itemId))
		{
			Connection con = null;
			try
			{
				if (activeChar.getPet() != null && activeChar.getPet().getControlItemId() == _objectId)
				{
					activeChar.getPet().unSummon(activeChar);
				}

				con = L2DatabaseFactory.getInstance().getConnection(con);
				PreparedStatement statement = con.prepareStatement("DELETE FROM pets WHERE item_obj_id=?");
				statement.setInt(1, _objectId);
				statement.execute();
				statement.close();
			}
			catch (Exception e)
			{
				_log.warn("could not delete pet objectid: ", e);
			}
			finally
			{
				L2DatabaseFactory.close(con);
			}
		}

		L2ItemInstance removedItem = activeChar.getInventory().destroyItem("Destroy", _objectId, count, activeChar, null);

		if (removedItem == null)
			return;

		activeChar.getInventory().updateInventory(removedItem);
		sendPacket(new ItemList(activeChar, true));

		StatusUpdate su = new StatusUpdate(activeChar);
		su.addAttribute(StatusUpdate.CUR_LOAD, activeChar.getCurrentLoad());
		activeChar.sendPacket(su);

		L2World world = L2World.getInstance();
		world.removeObject(removedItem);
	}

}