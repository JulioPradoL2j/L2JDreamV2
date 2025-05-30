package com.dream.game.network.clientpackets;

import com.dream.Config;
import com.dream.game.datatables.xml.SkillTable;
import com.dream.game.datatables.xml.SkillTreeTable;
import com.dream.game.model.L2EnchantSkillLearn;
import com.dream.game.model.L2ShortCut;
import com.dream.game.model.L2Skill;
import com.dream.game.model.actor.L2Npc;
import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.model.actor.instance.L2NpcInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.base.Experience;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.ShortCutRegister;
import com.dream.game.network.serverpackets.SystemMessage;
import com.dream.game.network.serverpackets.UserInfo;
import com.dream.game.util.IllegalPlayerAction;
import com.dream.game.util.Util;
import com.dream.tools.random.Rnd;

public final class RequestExEnchantSkill extends L2GameClientPacket
{
	private int _skillId, _skillLvl;

	@Override
	protected void readImpl()
	{
		_skillId = readD();
		_skillLvl = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if (player == null)
			return;

		L2NpcInstance trainer = player.getLastFolkNPC();
		if (trainer == null)
			return;

		int npcid = trainer.getNpcId();

		if (!player.isInsideRadius(trainer, L2Npc.INTERACTION_DISTANCE, false, false) && !player.isGM())
			return;
		if (player.getClassId().level() < 3)
			return;
		if (player.getLevel() < 76)
			return;
		L2Skill skill = SkillTable.getInstance().getInfo(_skillId, _skillLvl);

		if (skill == null)
			return;

		if (!skill.canTeachBy(npcid) || !skill.getCanLearn(player.getClassId()))
			if (!Config.ALT_GAME_SKILL_LEARN)
			{
				Util.handleIllegalPlayerAction(player, "Client " + getClient() + " tried to learn skill that he can't!!!", IllegalPlayerAction.PUNISH_KICK);
				return;
			}

		int reqItemId = SkillTreeTable.NORMAL_ENCHANT_BOOK;
		L2EnchantSkillLearn[] skills = SkillTreeTable.getInstance().getAvailableEnchantSkills(player);

		int counts = 0;
		int requiredSp = Integer.MAX_VALUE;
		int requiredExp = Integer.MAX_VALUE;
		byte rate = 0;
		int baseLvl = 1;

		for (L2EnchantSkillLearn s : skills)
		{
			L2Skill sk = SkillTable.getInstance().getInfo(s.getId(), s.getLevel());

			if (sk == null || sk != skill || !sk.getCanLearn(player.getClassId()) || !sk.canTeachBy(npcid))
			{
				continue;
			}

			counts++;
			requiredSp = s.getSpCost();
			requiredExp = s.getExp();
			rate = s.getRate(player);
			baseLvl = s.getBaseLevel();
		}

		if (counts == 0)
			return;

		if (player.getSp() >= requiredSp)
		{
			long expAfter = player.getExp() - requiredExp;
			if (player.getExp() >= requiredExp && expAfter >= Experience.LEVEL[player.getLevel()])
			{
				boolean usesBook = _skillLvl == 101 || _skillLvl == 141;
				L2ItemInstance spb = player.getInventory().getItemByItemId(reqItemId);
				if (Config.ES_SP_BOOK_NEEDED && usesBook)
					if (spb == null)
					{
						player.sendPacket(SystemMessageId.YOU_DONT_HAVE_ALL_OF_THE_ITEMS_NEEDED_TO_ENCHANT_THAT_SKILL);
						return;
					}

				boolean check;
				check = player.getStat().removeExpAndSp(requiredExp, requiredSp);
				if (Config.ES_SP_BOOK_NEEDED && usesBook)
				{
					check &= player.destroyItem("Consume", spb.getObjectId(), 1, trainer, true);
				}

				if (!check)
				{
					player.sendPacket(SystemMessageId.YOU_DONT_HAVE_ALL_OF_THE_ITEMS_NEEDED_TO_ENCHANT_THAT_SKILL);
					return;
				}

				if (Rnd.get(100) <= rate)
				{
					player.addSkill(skill, true);
					player.sendPacket(new UserInfo(player));
					player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_SUCCEEDED_IN_ENCHANTING_THE_SKILL_S1).addSkillName(_skillId));
					updateSkillShortcuts(player, _skillLvl);
				}
				else
				{
					player.addSkill(SkillTable.getInstance().getInfo(_skillId, baseLvl), true);
					player.sendSkillList();
					player.sendPacket(SystemMessageId.YOU_HAVE_FAILED_TO_ENCHANT_THE_SKILL);
					updateSkillShortcuts(player, baseLvl);
				}
				trainer.showEnchantSkillList(player);
			}
			else
			{
				player.sendPacket(SystemMessageId.YOU_DONT_HAVE_ENOUGH_EXP_TO_ENCHANT_THAT_SKILL);
			}
		}
		else
		{
			player.sendPacket(SystemMessageId.YOU_DONT_HAVE_ENOUGH_SP_TO_ENCHANT_THAT_SKILL);
		}
	}

	private void updateSkillShortcuts(L2PcInstance player, int _newlevel)
	{
		L2ShortCut[] allShortCuts = player.getAllShortCuts();

		for (L2ShortCut sc : allShortCuts)
			if (sc.getId() == _skillId && sc.getType() == L2ShortCut.TYPE_SKILL)
			{
				L2ShortCut newsc = new L2ShortCut(sc.getSlot(), sc.getPage(), sc.getType(), sc.getId(), _newlevel, 1);
				player.sendPacket(new ShortCutRegister(newsc));
				player.registerShortCut(newsc);
			}
	}

}