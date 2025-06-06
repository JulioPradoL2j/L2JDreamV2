package com.dream.game.handler.skill;

import com.dream.game.datatables.xml.DressMeData;
import com.dream.game.handler.ISkillHandler;
import com.dream.game.model.L2Skill;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.holders.DressMeHolder;
import com.dream.game.templates.skills.L2SkillType;

public class applySkins implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS =
	{
		L2SkillType.DRESSME
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
		DressMeHolder dress = DressMeData.getInstance().getBySkillId(skill.getId());
		
		if (dress == null)
		{
			player.sendMessage("Visual not found.");
			return;
		}
		final long cooldown = 10000;
		
		long currentTime = System.currentTimeMillis();
		if (currentTime - player.getLastDressMeSummonTime() < cooldown)
		{
			player.sendMessage("You need to wait before summoning another DressMe.");
			return;
		}
		if (player.getArmorSkin() != null && player.getArmorSkin().getSkillId() == skill.getId())
		{
			player.sendMessage("This armor look is already active.");
			player.removeDressMeArmor();
			return;
		}
		
		if (player.getWeaponSkin() != null && player.getWeaponSkin().getSkillId() == skill.getId())
		{
			player.sendMessage("This weapon look is already active.");
			player.removeDressMeWeapon();
			return;
		}

		
		player.setLastDressMeSummonTime(currentTime);
		player.applyDressMe(dress);
		player.broadcastUserInfo();
	}
}
