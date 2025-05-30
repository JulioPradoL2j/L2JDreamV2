package com.dream.game.handler.skill;

import com.dream.Config;
import com.dream.game.handler.ISkillHandler;
import com.dream.game.manager.CastleManager;
import com.dream.game.manager.FortManager;
import com.dream.game.manager.FortSiegeManager;
import com.dream.game.manager.SiegeManager;
import com.dream.game.model.L2Skill;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.instance.L2ArtefactInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.entity.siege.Castle;
import com.dream.game.model.entity.siege.Fort;
import com.dream.game.model.entity.siege.FortSiege;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.SystemMessage;
import com.dream.game.templates.skills.L2SkillType;

public class TakeCastle implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS =
	{
		L2SkillType.TAKECASTLE
	};

	public static boolean checkIfOkToCastSealOfRule(L2Character activeChar, boolean isCheckOnly)
	{

		if (activeChar.getActiveWeaponItem() != null && activeChar.getActiveWeaponItem().getItemId() == Config.FORTSIEGE_COMBAT_FLAG_ID)
		{
			FortSiege siege = FortSiegeManager.getSiege(activeChar);
			if (siege != null && siege.getAttackerClan(((L2PcInstance) activeChar).getClan()) != null)
				if (activeChar.getTarget() instanceof L2ArtefactInstance)
				{
					L2ArtefactInstance flag = (L2ArtefactInstance) activeChar.getTarget();
					if (flag.getFortId() == siege.getFort().getFortId())
						return true;
				}
		}
		return SiegeManager.getInstance().checkIfOkToCastSealOfRule(activeChar, CastleManager.getInstance().getCastle(activeChar), isCheckOnly);
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

		L2PcInstance player = (L2PcInstance) activeChar;

		if (player.getClan() == null)
			return;
		Fort fort = FortManager.getInstance().getFort(player);
		if (fort != null && fort.getFortId() > 0)
			if (fort.getSiege().getIsInProgress())
				if (fort.getSiege().getAttackerClan(player.getClan()) != null)
					if (player.getActiveWeaponItem() != null && player.getActiveWeaponItem().getItemId() == Config.FORTSIEGE_COMBAT_FLAG_ID)
					{
						player.getInventory().destroyItemByItemId("endSiege", Config.FORTSIEGE_COMBAT_FLAG_ID, 1, player, null);
						fort.getSiege().announceToPlayer(SystemMessage.getSystemMessage(SystemMessageId.S1).addString("An attempt to capture"), player.getClan().getName());
						fort.endOfSiege(player.getClan());
					}

		if (player.getClan() == null || player.getClan().getLeaderId() != player.getObjectId())
			return;

		Castle castle = CastleManager.getInstance().getCastle(player);
		if (castle == null || !SiegeManager.getInstance().checkIfOkToCastSealOfRule(player, castle, true))
			return;

		if (targets.length > 0 && targets[0] instanceof L2ArtefactInstance)
		{
			castle.engrave(player.getClan(), targets[0].getObjectId());
		}
	}
}