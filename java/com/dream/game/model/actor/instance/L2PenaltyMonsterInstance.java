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
package com.dream.game.model.actor.instance;

import com.dream.game.ai.CtrlEvent;
import com.dream.game.datatables.sql.SpawnTable;
import com.dream.game.model.L2Spawn;
import com.dream.game.model.actor.L2Character;
import com.dream.game.network.SystemChatChannelId;
import com.dream.game.network.serverpackets.CreatureSay;
import com.dream.game.templates.chars.L2NpcTemplate;
import com.dream.tools.random.Rnd;

public class L2PenaltyMonsterInstance extends L2MonsterInstance
{
	private L2PcInstance _ptk;

	public L2PenaltyMonsterInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public boolean doDie(L2Character killer)
	{
		if (!super.doDie(killer))
			return false;

		if (Rnd.nextInt(100) <= 75)
		{
			broadcastPacket(new CreatureSay(getObjectId(), SystemChatChannelId.Chat_Normal, getName(), "I say fish don't take your bait!"));
		}
		return true;
	}

	@Override
	public L2Character getMostHated()
	{
		return _ptk;
	}

	@Deprecated
	public void notifyPlayerDead()
	{
		deleteMe();

		L2Spawn spawn = getSpawn();
		if (spawn != null)
		{
			spawn.stopRespawn();
			SpawnTable.getInstance().deleteSpawn(spawn, false);
		}
	}

	public void setPlayerToKill(L2PcInstance ptk)
	{
		if (Rnd.nextInt(100) <= 80)
		{
			broadcastPacket(new CreatureSay(getObjectId(), SystemChatChannelId.Chat_Normal, getName(), "Mmm, your bait was delicious!"));
		}
		_ptk = ptk;
		addDamageHate(ptk, 10, 10);
		getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, ptk);
		addAttackerToAttackByList(ptk);
	}
}