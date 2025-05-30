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

import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.quest.Quest;
import com.dream.game.model.quest.QuestState;

public class QuestList extends L2GameServerPacket
{
	private Quest[] _quests;
	private final L2PcInstance _activeChar;

	public QuestList(L2PcInstance player)
	{
		_activeChar = player;
		try
		{
			_quests = player.getAllActiveQuests();
		}
		catch (NullPointerException npe)
		{
			_quests = null;
		}
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0x80);
		if (_quests == null || _quests.length == 0)
		{
			writeH(0x00);
		}
		else
		{
			writeH(_quests.length);
			for (Quest q : _quests)
			{
				writeD(q.getQuestIntId());
				QuestState qs = _activeChar.getQuestState(q.getName());
				if (qs == null)
				{
					writeD(0x00);
					continue;
				}

				int states = qs.getInt("__compltdStateFlags");
				if (states != 0)
				{
					writeD(states);
				}
				else
				{
					writeD(qs.getInt("cond"));
				}
			}
		}
	}

}