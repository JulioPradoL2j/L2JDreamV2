package com.dream.game.handler.item;

import com.dream.Config;
import com.dream.game.datatables.xml.SkillTable;
import com.dream.game.handler.IItemHandler;
import com.dream.game.model.L2Clan;
import com.dream.game.model.L2Skill;
import com.dream.game.model.actor.L2Playable;
import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.serverpackets.ActionFailed;

public class ClanSkill implements IItemHandler
{
	private static final int ITEM_IDS[] =
	{
		Config.CLANSKILL_ITEM
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

		if (!player.isClanLeader())
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			player.sendMessage("Dear " + player.getName() + " You're not a Clan leader, only clan leaders can use this item!");
			return;
		}

		if (player.isClanLeader())
		{
			L2Clan clan = player.getClan();
			if (clan == null)
			{
				player.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}

			for (int[] skills : Config.CLANSKILL_ITEM_SKILL)
				{
				L2Skill clanSkill = SkillTable.getInstance().getInfo(skills[0], skills[1]);
					clan.addNewSkill(clanSkill);
				player.sendMessage("Dear " + player.getName() + " The Clan " + clan.getName() + " now Received Skill " + SkillTable.getInstance().getSkillName(skills[0]) + "!");
				}
			player.destroyItem("Consume", item.getObjectId(), 1, null, false);
				clan.broadcastClanStatus();
				player.broadcastUserInfo();
				}
		}


	@Override
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
}