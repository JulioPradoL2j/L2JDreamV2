package com.dream.game.network.clientpackets;

import com.dream.Config;
import com.dream.game.datatables.xml.SkillTable;
import com.dream.game.model.L2Skill;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.restriction.AvailableRestriction;
import com.dream.game.model.restriction.ObjectRestrictions;
import com.dream.game.network.serverpackets.ActionFailed;
import com.dream.game.templates.skills.L2SkillType;
import com.dream.game.util.FloodProtector;
import com.dream.game.util.FloodProtector.Protected;

public class RequestMagicSkillUse extends L2GameClientPacket
{
	private int _magicId;
	private boolean _ctrlPressed;
	private boolean _shiftPressed;

	@Override
	protected void readImpl()
	{
		_magicId = readD();
		_ctrlPressed = readD() != 0;
		_shiftPressed = readC() != 0;
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();

		if (activeChar == null)
			return;
		if (activeChar.getSecondRefusal())
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		if (ObjectRestrictions.getInstance().checkRestriction(activeChar, AvailableRestriction.PlayerCast))
			return;

		int level = activeChar.getSkillLevel(_magicId);
		if (level <= 0)
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		L2Skill skill = SkillTable.getInstance().getInfo(_magicId, level);

		if (skill != null && skill.getSkillType() != L2SkillType.NOTDONE)
		{
			if (Config.DISABLE_SKILLS_ON_LEVEL_LOST)
				if (skill.getMagicLevel() - activeChar.getLevel() >= 5)
				{
					activeChar.sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}

			if (activeChar._lastSkill == skill.getId() && Config.SKILL_DELAY > 0)
				if (!FloodProtector.tryPerformAction(activeChar, Protected.CASTSKILL))
				{
					activeChar.sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}
			activeChar._lastSkill = skill.getId();
			if (skill.getSkillType() == L2SkillType.RECALL && !Config.ALT_GAME_KARMA_PLAYER_CAN_TELEPORT && activeChar.getKarma() > 0)
			{
				activeChar.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}

			if (skill.isToggle() && activeChar.isMounted())
			{
				activeChar.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			activeChar.useMagic(skill, _ctrlPressed, _shiftPressed);
		}
		else
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			if (skill != null && skill.getSkillType() == L2SkillType.NOTDONE)
			{

			}
		}
	}

}