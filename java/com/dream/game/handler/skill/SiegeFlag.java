package com.dream.game.handler.skill;

import com.dream.game.datatables.sql.NpcTable;
import com.dream.game.handler.ISkillHandler;
import com.dream.game.idfactory.IdFactory;
import com.dream.game.manager.FortSiegeManager;
import com.dream.game.manager.SiegeManager;
import com.dream.game.model.L2Skill;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.actor.instance.L2SiegeFlagInstance;
import com.dream.game.model.entity.siege.FortSiege;
import com.dream.game.model.entity.siege.Siege;
import com.dream.game.templates.chars.L2NpcTemplate;
import com.dream.game.templates.skills.L2SkillType;

public class SiegeFlag implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS =
	{
		L2SkillType.SIEGEFLAG
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

		Siege siege = SiegeManager.getSiege(player);
		FortSiege fsiege = FortSiegeManager.getSiege(player);
		if (siege != null && SiegeManager.checkIfOkToPlaceFlag(player, false))
		{
			L2NpcTemplate template = NpcTable.getInstance().getTemplate(35062);
			if (skill != null && template != null)
			{
				L2SiegeFlagInstance flag = new L2SiegeFlagInstance(player, IdFactory.getInstance().getNextId(), template, skill.isAdvanced(), false, null);
				flag.setTitle(player.getClan().getName());
				flag.getStatus().setCurrentHpMp(flag.getMaxHp(), flag.getMaxMp());
				flag.setHeading(player.getHeading());
				flag.spawnMe(player.getX(), player.getY(), player.getZ() + 50);
				siege.getFlag(player.getClan()).add(flag);
			}
		}
		else if (fsiege != null && FortSiegeManager.checkIfOkToPlaceFlag(activeChar, false))
		{
			L2NpcTemplate template = NpcTable.getInstance().getTemplate(35062);
			if (skill != null && template != null)
			{
				L2SiegeFlagInstance flag = new L2SiegeFlagInstance(player, IdFactory.getInstance().getNextId(), template, skill.isAdvanced(), false, null);
				flag.setTitle(player.getClan().getName());
				flag.getStatus().setCurrentHpMp(flag.getMaxHp(), flag.getMaxMp());
				flag.setHeading(player.getHeading());
				flag.spawnMe(player.getX(), player.getY(), player.getZ() + 50);
				fsiege.getFlag(player.getClan()).add(flag);
			}
		}
	}
}