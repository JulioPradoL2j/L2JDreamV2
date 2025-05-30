/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.dream.game.datatables;

import java.util.ArrayList;
import java.util.List;

import com.dream.game.datatables.xml.SkillTable;
import com.dream.game.model.L2Skill;

public final class NobleSkillTable
{
	private static final int[] NOBLE_SKILL_IDS =
	{
		325,
		326,
		327,
		1323,
		1324,
		1325,
		1326,
		1327
	};

	private static NobleSkillTable _instance;

	public static NobleSkillTable getInstance()
	{
		if (_instance == null)
		{
			_instance = new NobleSkillTable();
		}

		return _instance;
	}

	public static boolean isNobleSkill(final int skillid)
	{
		Integer[] _HeroSkillsId = new Integer[]
		{
			325,
			326,
			327,
			1323,
			1324,
			1325,
			1326,
			1327
		};

		for (final int id : _HeroSkillsId)
			if (id == skillid)
				return true;
		_HeroSkillsId = null;

		return false;
	}

	private final List<L2Skill> _nobleSkills = new ArrayList<>();

	private NobleSkillTable()
	{
		for (int skillId : NOBLE_SKILL_IDS)
		{
			_nobleSkills.add(SkillTable.getInstance().getInfo(skillId, 1));
		}
	}

	public Iterable<L2Skill> getNobleSkills()
	{
		return _nobleSkills;
	}
}