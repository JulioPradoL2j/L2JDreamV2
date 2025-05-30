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
package com.dream.game.network.serverpackets;

import java.util.Vector;

import com.dream.game.model.L2Clan;
import com.dream.game.model.L2Skill;

public class PledgeSkillList extends L2GameServerPacket
{
	class Skill
	{
		public int id;
		public int level;

		Skill(int pId, int pLevel)
		{
			id = pId;
			level = pLevel;
		}

	}

	private final L2Clan _clan;

	private final Vector<Skill> _skill;

	public PledgeSkillList(L2Clan clan)
	{
		_clan = clan;
		_skill = new Vector<>();
	}

	public void addSkill(int id, int level)
	{
		_skill.add(new Skill(id, level));
	}

	@Override
	protected void writeImpl()
	{
		L2Skill[] skills = _clan.getAllSkills();

		writeC(0xfe);
		writeH(0x39);
		writeD(skills.length);
		for (L2Skill sk : skills)
		{
			writeD(sk.getId());
			writeD(sk.getLevel());
		}
	}

}