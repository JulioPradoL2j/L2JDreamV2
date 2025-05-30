package com.dream.game.network.clientpackets;

import org.apache.log4j.Logger;

import com.dream.Config;
import com.dream.game.datatables.xml.SkillSpellbookTable;
import com.dream.game.datatables.xml.SkillTable;
import com.dream.game.datatables.xml.SkillTreeTable;
import com.dream.game.model.L2PledgeSkillLearn;
import com.dream.game.model.L2ShortCut;
import com.dream.game.model.L2Skill;
import com.dream.game.model.L2SkillLearn;
import com.dream.game.model.actor.L2Npc;
import com.dream.game.model.actor.instance.L2FishermanInstance;
import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.model.actor.instance.L2NpcInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.actor.instance.L2VillageMasterInstance;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.ExStorageMaxCount;
import com.dream.game.network.serverpackets.PledgeSkillList;
import com.dream.game.network.serverpackets.ShortCutRegister;
import com.dream.game.network.serverpackets.StatusUpdate;
import com.dream.game.network.serverpackets.SystemMessage;
import com.dream.game.util.IllegalPlayerAction;
import com.dream.game.util.Util;

public class RequestAquireSkill extends L2GameClientPacket
{
	public static Logger _log = Logger.getLogger(RequestAquireSkill.class.getName());

	private int _id, _level, _skillType;

	@Override
	protected void readImpl()
	{
		_id = readD();
		_level = readD();
		_skillType = readD();
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
		if (!Config.ALT_GAME_SKILL_LEARN)
		{
			player.setSkillLearningClassId(player.getClassId());
		}
		if (player.getSkillLevel(_id) >= _level)
			return;

		L2Skill skill = SkillTable.getInstance().getInfo(_id, _level);

		int counts = 0;
		int _requiredSp = 0;

		switch (_skillType)
		{
			case 0:
			{
				L2SkillLearn[] skills = SkillTreeTable.getInstance().getAvailableSkills(player, player.getSkillLearningClassId());

				for (L2SkillLearn s : skills)
				{
					L2Skill sk = SkillTable.getInstance().getInfo(s.getId(), s.getLevel());
					if (sk == null || sk != skill || !sk.getCanLearn(player.getSkillLearningClassId()) || !sk.canTeachBy(npcid))
					{
						continue;
					}
					counts++;
					_requiredSp = SkillTreeTable.getInstance().getSkillCost(player, skill);
				}

				if (counts == 0 && !Config.ALT_GAME_SKILL_LEARN)
				{
					Util.handleIllegalPlayerAction(player, "Player " + player.getName() + " tried to learn what is not valid!!!", IllegalPlayerAction.PUNISH_KICK);
					return;
				}

				if (player.getSp() >= _requiredSp)
				{
					int spbId = -1;

					if (Config.DIVINE_SP_BOOK_NEEDED && skill.getId() == L2Skill.SKILL_DIVINE_INSPIRATION)
					{
						spbId = SkillSpellbookTable.getInstance().getBookForSkill(skill, _level);
					}
					else if (Config.SP_BOOK_NEEDED && skill.getLevel() == 1)
					{
						spbId = SkillSpellbookTable.getInstance().getBookForSkill(skill);
					}

					if (spbId > -1)
					{
						L2ItemInstance spb = player.getInventory().getItemByItemId(spbId);

						if (spb == null)
						{
							player.sendPacket(SystemMessageId.ITEM_MISSING_TO_LEARN_SKILL);
							return;
						}

						player.destroyItem("Consume", spb, trainer, true);
					}
				}
				else
				{
					player.sendPacket(SystemMessageId.NOT_ENOUGH_SP_TO_LEARN_SKILL);
					return;
				}
				break;
			}
			case 1:
			{
				int costid = 0;
				int costcount = 0;
				L2SkillLearn[] skillsc = SkillTreeTable.getInstance().getAvailableSkills(player);

				for (L2SkillLearn s : skillsc)
				{
					L2Skill sk = SkillTable.getInstance().getInfo(s.getId(), s.getLevel());

					if (sk == null || sk != skill)
					{
						continue;
					}

					counts++;
					costid = s.getIdCost();
					costcount = s.getCostCount();
					_requiredSp = s.getSpCost();
				}

				if (counts == 0)
				{
					Util.handleIllegalPlayerAction(player, "Player " + player.getName() + " tried to learn what is not valid!!!", IllegalPlayerAction.PUNISH_KICK);
					return;
				}

				if (player.getSp() >= _requiredSp)
				{
					if (!player.destroyItemByItemId("Consume", costid, costcount, trainer, false))
					{
						player.sendPacket(SystemMessageId.ITEM_MISSING_TO_LEARN_SKILL);
						return;
					}

					if (costcount > 1)
					{
						player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S2_S1_DISAPPEARED).addItemName(costid).addItemNumber(costcount));
					}
					else
					{
						player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_DISAPPEARED).addItemName(costid));
					}

				}
				else
				{
					player.sendPacket(SystemMessageId.NOT_ENOUGH_SP_TO_LEARN_SKILL);
					return;
				}
				break;
			}
			case 2:
			{
				if (!player.isClanLeader())
					return;

				int itemId = 0;
				int repCost = 100000000;
				L2PledgeSkillLearn[] skills = SkillTreeTable.getInstance().getAvailablePledgeSkills(player);

				for (L2PledgeSkillLearn s : skills)
				{
					L2Skill sk = SkillTable.getInstance().getInfo(s.getId(), s.getLevel());

					if (sk == null || sk != skill)
					{
						continue;
					}

					counts++;
					itemId = s.getItemId();
					repCost = s.getRepCost();
				}
				if (counts == 0)
				{
					Util.handleIllegalPlayerAction(player, "Player " + player.getName() + " tried to learn what is not valid!!!", IllegalPlayerAction.PUNISH_KICK);
					return;
				}
				if (player.getClan().getReputationScore() >= repCost)
				{
					if (Config.LIFE_CRYSTAL_NEEDED)
					{
						if (!player.destroyItemByItemId("Consume", itemId, 1, trainer, false))
						{
							player.sendPacket(SystemMessageId.ITEM_MISSING_TO_LEARN_SKILL);
							return;
						}
						player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S2_S1_DISAPPEARED).addItemName(itemId).addNumber(1));
					}
				}
				else
				{
					player.sendPacket(SystemMessageId.ACQUIRE_SKILL_FAILED_BAD_CLAN_REP_SCORE);
					return;
				}
				player.getClan().setReputationScore(player.getClan().getReputationScore() - repCost, true);
				player.getClan().addNewSkill(skill);

				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_DEDUCTED_FROM_CLAN_REP).addNumber(repCost));
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CLAN_SKILL_S1_ADDED).addSkillName(_id));

				player.getClan().broadcastToOnlineMembers(new PledgeSkillList(player.getClan()));

				for (L2PcInstance member : player.getClan().getOnlineMembers(0))
				{
					member.sendSkillList();
				}
				((L2VillageMasterInstance) trainer).showPledgeSkillList(player);
				return;
			}
			default:
			{
				_log.warn("Recived Wrong Packet Data in Aquired Skill - unk1:" + _skillType);
				return;
			}
		}
		player.addSkill(skill, true);
		if (_requiredSp > 0)
		{
			player.setSp(player.getSp() - _requiredSp);
			StatusUpdate su = new StatusUpdate(player);
			su.addAttribute(StatusUpdate.SP, player.getSp());
			player.sendPacket(su);
		}

		player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.LEARNED_SKILL_S1).addSkillName(_id));

		if (_level > 1)
		{
			L2ShortCut[] allShortCuts = player.getAllShortCuts();
			for (L2ShortCut sc : allShortCuts)
				if (sc.getId() == _id && sc.getType() == L2ShortCut.TYPE_SKILL)
				{
					L2ShortCut newsc = new L2ShortCut(sc.getSlot(), sc.getPage(), sc.getType(), sc.getId(), _level, 1);
					player.sendPacket(new ShortCutRegister(newsc));
					player.registerShortCut(newsc);
				}
		}

		if (trainer instanceof L2FishermanInstance)
		{
			((L2FishermanInstance) trainer).showSkillList(player);
		}
		else
		{
			trainer.showSkillList(player, player.getSkillLearningClassId());
		}

		if (_id >= 1368 && _id <= 1372)
		{
			player.sendPacket(new ExStorageMaxCount(player));
		}

		player.sendSkillList();
	}

}