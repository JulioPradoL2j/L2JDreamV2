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

import com.dream.Config;
import com.dream.game.model.L2Skill;
import com.dream.game.model.actor.instance.L2PcInstance;

import java.util.List;

public final class SkillList extends L2GameServerPacket
{
	private final List<L2Skill> _skills;
	
	public SkillList(List<L2Skill> list)
	{
		_skills = list;
	}
	
	@Override
	protected void writeImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		
		writeC(0x58);
		writeD(_skills.size());
		
		for (L2Skill s : _skills)
		{
			if (!s.isBalance())
			{
				writeD(s.isPassive() || s.isChance() ? 1 : 0);
				writeD(s.getLevel());
				
				writeD(s.getDisplayId());
				
				int grayed = 0;
				if (Config.DISABLE_SKILLS_ON_LEVEL_LOST)
				{
					if (s.getMagicLevel() - activeChar.getLevel() >= 5)
					{
						grayed = 1;
					}
				}
				writeC(grayed);
			}
		}
	}
	
}