package com.dream.game.network.clientpackets;

import org.apache.log4j.Logger;

import com.dream.Config;
import com.dream.game.Shutdown;
import com.dream.game.manager.CursedWeaponsManager;
import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.ActionFailed;
import com.dream.game.network.serverpackets.InventoryUpdate;
import com.dream.game.network.serverpackets.ItemList;
import com.dream.game.templates.item.L2Item;
import com.dream.game.util.FloodProtector;
import com.dream.game.util.FloodProtector.Protected;
import com.dream.game.util.IllegalPlayerAction;
import com.dream.game.util.Util;

public class RequestDropItem extends L2GameClientPacket
{
	public static Logger _log = Logger.getLogger(RequestDropItem.class.getName());

	private int _objectId, _count, _x, _y, _z;

	@Override
	protected void readImpl()
	{
		_objectId = readD();
		_count = readD();
		_x = readD();
		_y = readD();
		_z = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();

		if (activeChar == null || activeChar.isDead())
			return;

		if (!FloodProtector.tryPerformAction(activeChar, Protected.DROPITEM))
			return;

		if (Config.SAFE_REBOOT && Config.SAFE_REBOOT_DISABLE_TRANSACTION && Shutdown.getCounterInstance() != null && Shutdown.getCounterInstance().getCountdown() <= Config.SAFE_REBOOT_TIME)
		{
			activeChar.sendPacket(SystemMessageId.FUNCTION_INACCESSIBLE_NOW);
			return;
		}

		L2ItemInstance item = activeChar.checkItemManipulation(_objectId, _count, "Drop");

		if (item == null)
		{
			_log.info("Error while droping item for char " + activeChar.getName() + " (validity check).");
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		if (item.getItemId() != 9693)
			if (!Config.ALLOW_DISCARDITEM && !activeChar.isGM() || !item.isDropable() && !activeChar.isGM())
			{
				activeChar.sendPacket(SystemMessageId.CANNOT_DISCARD_THIS_ITEM);
				return;
			}
		if (item.getItemId() == Config.FORTSIEGE_COMBAT_FLAG_ID)
			return;
		if (item.isAugmented())
		{
			activeChar.sendPacket(SystemMessageId.AUGMENTED_ITEM_CANNOT_BE_DISCARDED);
			return;
		}
		if (activeChar.getSecondRefusal())
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		if (activeChar.isAio())
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		if (Config.ALT_STRICT_HERO_SYSTEM)
			if (item.isHeroItem())
			{
				activeChar.sendPacket(SystemMessageId.CANNOT_DISCARD_THIS_ITEM);
				return;
			}
		if (CursedWeaponsManager.getInstance().isCursed(item.getItemId()))
		{
			activeChar.sendPacket(SystemMessageId.CANNOT_DISCARD_THIS_ITEM);
			return;
		}
		if (_count > item.getCount())
		{
			activeChar.sendPacket(SystemMessageId.NOTHING_HAPPENED);
			return;
		}
		if (Config.PLAYER_SPAWN_PROTECTION > 0 && activeChar.isInvul() && !activeChar.isGM())
		{
			activeChar.sendPacket(SystemMessageId.CANNOT_DISCARD_THIS_ITEM);
			return;
		}
		if (_count <= 0)
		{
			Util.handleIllegalPlayerAction(activeChar, "[RequestDropItem] count <= 0! ban! oid: " + _objectId + " owner: " + activeChar.getName(), IllegalPlayerAction.PUNISH_KICK);
			return;
		}
		if (!item.isStackable() && _count > 1)
		{
			Util.handleIllegalPlayerAction(activeChar, "[RequestDropItem] count > 1 but item is not stackable! ban! oid: " + _objectId + " owner: " + activeChar.getName(), IllegalPlayerAction.PUNISH_KICK);
			return;
		}
		if (activeChar.isProcessingTransaction() || activeChar.getPrivateStoreType() != 0)
		{
			activeChar.sendPacket(SystemMessageId.CANNOT_TRADE_DISCARD_DROP_ITEM_WHILE_IN_SHOPMODE);
			return;
		}
		if (activeChar.isFishing())
		{
			activeChar.sendPacket(SystemMessageId.CANNOT_DO_WHILE_FISHING_2);
			return;
		}
		if (activeChar.isCastingNow())
			if (activeChar.getCurrentSkill() != null && activeChar.getCurrentSkill().getSkill().getItemConsumeId() == item.getItemId())
			{
				activeChar.sendPacket(SystemMessageId.CANNOT_DISCARD_THIS_ITEM);
				return;
			}
		if (activeChar.isSummoning())
		{
			activeChar.sendPacket(SystemMessageId.CANNOT_DISCARD_THIS_ITEM);
			return;
		}
		if (activeChar.getActiveEnchantItem() != null)
		{
			activeChar.sendPacket(SystemMessageId.CANNOT_DISCARD_THIS_ITEM);
			return;
		}
		if (activeChar.isCastingSimultaneouslyNow())
			if (activeChar.getLastSimultaneousSkillCast() != null && activeChar.getLastSimultaneousSkillCast().getItemConsumeId() == item.getItemId())
			{
				activeChar.sendPacket(SystemMessageId.CANNOT_DISCARD_THIS_ITEM);
				return;
			}
		if (L2Item.TYPE2_QUEST == item.getItem().getType2() && !activeChar.isGM())
		{
			activeChar.sendPacket(SystemMessageId.CANNOT_DISCARD_EXCHANGE_ITEM);
			return;
		}
		if (!activeChar.isInsideRadius(_x, _y, 150, false) || Math.abs(_z - activeChar.getZ()) > 50)
		{
			activeChar.sendPacket(SystemMessageId.CANNOT_DISCARD_DISTANCE_TOO_FAR);
			return;
		}
		if (item.isEquipped())
		{
			if (item.isAugmented())
			{
				item.getAugmentation().removeBonus(activeChar);
			}
			L2ItemInstance[] unequiped = activeChar.getInventory().unEquipItemInBodySlotAndRecord(item.getItem().getBodyPart());
			InventoryUpdate iu = new InventoryUpdate();
			for (L2ItemInstance element : unequiped)
			{
				activeChar.checkSSMatch(null, element);
				iu.addModifiedItem(element);
			}
			activeChar.sendPacket(iu);
			activeChar.broadcastUserInfo();

			activeChar.sendPacket(new ItemList(activeChar, true));
		}

		activeChar.dropItem("Drop", _objectId, _count, _x, _y, _z, null, false, false);
	}

}