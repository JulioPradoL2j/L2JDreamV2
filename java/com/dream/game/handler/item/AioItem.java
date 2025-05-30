package com.dream.game.handler.item;

import com.dream.Config;
import com.dream.game.handler.IItemHandler;
import com.dream.game.model.actor.L2Playable;
import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.zone.L2Zone;
import com.dream.game.network.serverpackets.ActionFailed;
import com.dream.game.network.serverpackets.EtcStatusUpdate;

public class AioItem implements IItemHandler
{
	private static final int ITEM_IDS[] =
	{
		Config.AIO_ITEM
	};

	@Override
	public void useItem(L2Playable playable, L2ItemInstance item, boolean par)
	{

	}

	@Override
	public synchronized void useItem(L2Playable playable, L2ItemInstance item)
	{
		if (!(playable instanceof L2PcInstance))
			return;

		L2PcInstance player = (L2PcInstance) playable;

		if (!Config.LIST_CLASSES_AIO.contains(player.getTemplate().getClassId().getId()) && !player.isSubClassActive())
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			player.sendMessage("Dear " + player.getName() + " you must have one of this classes " + Config.AIO_CLASSES_NAME + " to use this Item.");
		}

		else
		{
			if (player.isAio())
			{
				player.sendPacket(ActionFailed.STATIC_PACKET);
				player.sendMessage("Dear " + player.getName() + " you are already an AIOx");
			} 
			
			else if (player.destroyItem("Consume", item.getObjectId(), 1, null, false))
			{

				if (!Config.ALLOW_AIO_LEAVE_TOWN)
				{
					if (!player.isInsideZone(L2Zone.FLAG_ALLOWAIO))
					{
						player.teleToLocation(83400, 147943, -3404, true); // Giran
					}
				}
				// L2Skill skill = SkillTable.getInstance().getInfo(15519, 1);
				// Broadcast.toSelfAndKnownPlayersInRadius(player, new MagicSkillUse(player, player, 15519, 1, 0, 0, false), 360000);

				// player.sendPacket(new MagicSkillUse(playable, player, 15519, 1, 1, 0, false));
				// player.broadcastPacket(new MagicSkillUse(playable, player, 15519, 1, 1, 0, false));

				player.setAio(true);
				player.setEndTime("aio", Config.AIO_DIAS);
				player.getStat().addExp(player.getStat().getExpForLevel(81));

				if (Config.ALLOW_AIO_NCOLOR && player.isAio())
					player.getAppearance().setNameColor(Config.AIO_NCOLOR);

				if (Config.ALLOW_AIO_TCOLOR && player.isAio())
					player.getAppearance().setTitleColor(Config.AIO_TCOLOR);

				if (Config.ALLOW_AIO_DUAL && player.isAio())
					player.getInventory().addItem("AIOx", Config.AIO_DUAL_ID, 1, player, null);

				player.rewardAioSkills();
				player.sendPacket(new EtcStatusUpdate(player));
				player.sendSkillList();
				player.sendMessage("Dear " + player.getName() + " now you have AIOx Status " + " you have acess to all Buff Skill's," + " remember you have " + Config.AIO_DIAS + " AIOx days.");
				player.broadcastUserInfo();
			}
		}
	}

	@Override
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
}