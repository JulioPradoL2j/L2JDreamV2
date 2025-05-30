package com.dream.game.network.serverpackets;

import java.util.ArrayList;
import java.util.List;

public class AcquireSkillList extends L2GameServerPacket
{
	private class Skill
	{
		public int id;
		public int nextLevel;
		public int maxLevel;
		public int spCost;
		public int requirements;

		public Skill(int pId, int pNextLevel, int pMaxLevel, int pSpCost, int pRequirements)
		{
			id = pId;
			nextLevel = pNextLevel;
			maxLevel = pMaxLevel;
			spCost = pSpCost;
			requirements = pRequirements;
		}
	}

	public enum SkillType
	{
		Usual,
		Fishing,
		Clan
	}

	private final List<Skill> _skills;

	private final SkillType _fishingSkills;

	public AcquireSkillList(SkillType type)
	{
		_skills = new ArrayList<>();
		_fishingSkills = type;
	}

	public void addSkill(int id, int nextLevel, int maxLevel, int spCost, int requirements)
	{
		_skills.add(new Skill(id, nextLevel, maxLevel, spCost, requirements));
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0x8a);
		writeD(_fishingSkills.ordinal());
		writeD(_skills.size());

		for (Skill temp : _skills)
		{
			writeD(temp.id);
			writeD(temp.nextLevel);
			writeD(temp.maxLevel);
			writeD(temp.spCost);
			writeD(temp.requirements);
		}
	}

}