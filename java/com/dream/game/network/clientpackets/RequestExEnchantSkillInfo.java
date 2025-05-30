package com.dream.game.network.clientpackets;

import com.dream.Config;
import com.dream.game.datatables.xml.SkillTable;
import com.dream.game.datatables.xml.SkillTreeTable;
import com.dream.game.model.L2EnchantSkillLearn;
import com.dream.game.model.L2Skill;
import com.dream.game.model.actor.L2Npc;
import com.dream.game.model.actor.instance.L2NpcInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.serverpackets.ExEnchantSkillInfo;

public final class RequestExEnchantSkillInfo extends L2GameClientPacket
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
		L2PcInstance activeChar = getClient().getActiveChar();

		if (activeChar == null || _skillId == 0)
			return;

		if (activeChar.getLevel() < 76)
			return;

		L2NpcInstance trainer = activeChar.getLastFolkNPC();

		if ((trainer == null || !activeChar.isInsideRadius(trainer, L2Npc.INTERACTION_DISTANCE, false, false)) && !activeChar.isGM())
			return;

		L2Skill skill = SkillTable.getInstance().getInfo(_skillId, _skillLvl);

		if (skill == null || skill.getId() != _skillId)
			return;

		if (trainer != null && !trainer.getTemplate().canTeach(activeChar.getClassId()) && !activeChar.isGM())
			return;

		showEnchantInfo(activeChar, skill);
	}

	public void showEnchantInfo(L2PcInstance activeChar, L2Skill skill)
	{
		boolean canteach = false;

		L2EnchantSkillLearn[] skills = SkillTreeTable.getInstance().getAvailableEnchantSkills(activeChar);

		for (L2EnchantSkillLearn s : skills)
			if (s.getId() == _skillId && s.getLevel() == _skillLvl)
			{
				canteach = true;
				break;
			}

		if (!canteach)
			return;

		int requiredSp = SkillTreeTable.getInstance().getEnchantSkillSpCost(activeChar, skill);
		int requiredExp = SkillTreeTable.getInstance().getEnchantSkillExpCost(activeChar, skill);
		byte rate = SkillTreeTable.getInstance().getEnchantSkillRate(activeChar, skill);
		ExEnchantSkillInfo asi = new ExEnchantSkillInfo(skill.getId(), skill.getLevel(), requiredSp, requiredExp, rate);

		if (Config.ES_SP_BOOK_NEEDED && (skill.getLevel() == 101 || skill.getLevel() == 141))
		{
			int spbId = 6622;
			asi.addRequirement(4, spbId, 1, 0);
		}
		sendPacket(asi);
	}

}