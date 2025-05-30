package com.dream.game.handler.skill;

import com.dream.game.handler.ISkillHandler;
import com.dream.game.model.L2Manor;
import com.dream.game.model.L2Skill;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.model.actor.instance.L2MonsterInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.PlaySound;
import com.dream.game.network.serverpackets.SystemMessage;
import com.dream.game.templates.skills.L2SkillType;
import com.dream.tools.random.Rnd;

public class Sow implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS =
	{
		L2SkillType.SOW
	};

	private static boolean calcSuccess(L2PcInstance activeChar, L2MonsterInstance target, int seedId)
	{
		int basicSuccess = L2Manor.getInstance().isAlternative(seedId) ? 20 : 90;
		int minlevelSeed = 0;
		int maxlevelSeed = 0;
		minlevelSeed = L2Manor.getInstance().getSeedMinLevel(seedId);
		maxlevelSeed = L2Manor.getInstance().getSeedMaxLevel(seedId);

		int levelPlayer = activeChar.getLevel();
		int levelTarget = target.getLevel();

		if (levelTarget < minlevelSeed)
		{
			basicSuccess -= 5 * (minlevelSeed - levelTarget);
		}
		if (levelTarget > maxlevelSeed)
		{
			basicSuccess -= 5 * (levelTarget - maxlevelSeed);
		}

		int diff = levelPlayer - levelTarget;
		if (diff < 0)
		{
			diff = -diff;
		}
		if (diff > 5)
		{
			basicSuccess -= 5 * (diff - 5);
		}

		if (basicSuccess < 1)
		{
			basicSuccess = 1;
		}

		int rate = Rnd.nextInt(10);

		return rate < basicSuccess;
	}

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

		L2PcInstance activePlayer = (L2PcInstance) activeChar;

		for (L2Character element : targets)
		{
			if (!(element instanceof L2MonsterInstance))
			{
				continue;
			}

			L2MonsterInstance target = (L2MonsterInstance) element;

			if (target.isSeeded())
			{
				continue;
			}

			if (target.isDead())
			{
				continue;
			}

			if (target.getSeeder() != activeChar)
			{
				continue;
			}

			int seedId = target.getSeedType();
			if (seedId == 0)
			{
				continue;
			}

			L2ItemInstance item = activePlayer.getInventory().getItemByItemId(seedId);
			if (item == null)
				return;

			activePlayer.destroyItem("Consume", item.getObjectId(), 1, null, false);

			SystemMessage sm;
			if (calcSuccess(activePlayer, target, seedId))
			{
				activePlayer.sendPacket(new PlaySound("Itemsound.quest_itemget"));
				target.setSeeded();
				sm = SystemMessage.getSystemMessage(SystemMessageId.THE_SEED_WAS_SUCCESSFULLY_SOWN);
			}
			else
			{
				sm = SystemMessage.getSystemMessage(SystemMessageId.THE_SEED_WAS_NOT_SOWN);
			}
			if (activePlayer.getParty() == null)
			{
				activePlayer.sendPacket(sm);
			}
			else
			{
				activePlayer.getParty().broadcastToPartyMembers(sm);
			}
		}
	}
}