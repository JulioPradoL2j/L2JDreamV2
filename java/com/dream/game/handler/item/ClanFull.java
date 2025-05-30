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

public class ClanFull implements IItemHandler
{
	private static final int ITEM_IDS[] =
	{
		Config.CLANFULL_ITEM
	};

	@Override
	public void useItem(L2Playable playable, L2ItemInstance item, boolean par)
	{

	}

	private final int reputation = 30000000;
	private final byte level = 8;

	// id skills
	private final int[] clanSkills =
	{
		370,
		371,
		372,
		373,
		374,
		375,
		376,
		377,
		378,
		379,
		380,
		381,
		382,
		383,
		384,
		385,
		386,
		387,
		388,
		389,
		390,
		391
	};

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
		}

		else
		{
			if (player.getClan().getLevel() == 8)
			{
				player.sendPacket(ActionFailed.STATIC_PACKET);
				player.sendMessage("Dear " + player.getName() + " your clan are already lvl 8");
			} 
			
			else if (player.destroyItem("Consume", item.getObjectId(), 1, null, false))
			{

				if (player.isClanLeader())
				{
				L2Clan clan = player.getClan();
				if (clan == null)
				{
					player.sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}

				for (int s : clanSkills)
				{
					L2Skill clanSkill = SkillTable.getInstance().getInfo(s, SkillTable.getInstance().getMaxLevel(s));
					clan.addNewSkill(clanSkill);
				}

				clan.setReputationScore(clan.getReputationScore() + reputation, true);
				clan.changeLevel(level);
				clan.broadcastClanStatus();
				player.sendMessage("Dear " + player.getName() + " The Clan " + clan.getName() + " now as Full Level and All Skills!");
					player.broadcastUserInfo();
				}
			}
		}
	}


	@Override
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
}