package com.dream.game.model.zone;

import com.dream.game.datatables.xml.MapRegionTable.TeleportWhereType;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.InventoryUpdate;
import com.dream.game.network.serverpackets.SystemMessage;

public class L2PaganZone extends L2DefaultZone
{
	@Override
	protected void onEnter(L2Character character)
	{
		if (character instanceof L2PcInstance)
			if (character.getLevel() >= 73)
			{
				L2PcInstance player = (L2PcInstance) character;
				L2ItemInstance item = player.getInventory().getItemByItemId(8064);
				if (item != null)
				{
					player.destroyItemByItemId("Mark", 8064, 1, player, true);
					L2ItemInstance fadedMark = player.getInventory().addItem("Faded Mark", 8065, 1, player, player);

					player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EARNED_S1).addItemName(fadedMark));

					InventoryUpdate u = new InventoryUpdate();
					u.addNewItem(fadedMark);
					player.sendPacket(u);
				}
			}
			else
			{
				character.teleToLocation(TeleportWhereType.Town);
			}
		super.onEnter(character);
	}

	@Override
	protected void onExit(L2Character character)
	{
		super.onExit(character);
	}
}