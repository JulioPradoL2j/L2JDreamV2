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

import com.dream.game.ai.L2CharacterAI;
import com.dream.game.ai.L2NpcWalkerAI;
import com.dream.game.model.L2Skill;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.L2Npc;
import com.dream.game.network.SystemChatChannelId;
import com.dream.game.network.serverpackets.CreatureSay;
import com.dream.game.templates.chars.L2NpcTemplate;

public class L2NpcWalkerInstance extends L2Npc
{
	protected class L2NpcWalkerAIAccessor extends L2Character.AIAccessor
	{
		@Override
		public void detachAI()
		{
		}
	}

	public L2NpcWalkerInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		setAI(new L2NpcWalkerAI(new L2NpcWalkerAIAccessor()));
	}

	public void broadcastChat(String chat)
	{
		if (!getKnownList().getKnownPlayers().isEmpty())
		{
			broadcastPacket(new CreatureSay(getObjectId(), SystemChatChannelId.Chat_Normal, getName(), chat));
		}
	}

	@Override
	public boolean doDie(L2Character killer)
	{
		return false;
	}

	@Override
	public L2NpcWalkerAI getAI()
	{
		return (L2NpcWalkerAI) _ai;
	}

	@Override
	public void reduceCurrentHp(double i, L2Character attacker, boolean awake, boolean isDOT, L2Skill skill)
	{

	}

	@Override
	public void setAI(L2CharacterAI newAI)
	{
		if (!(_ai instanceof L2NpcWalkerAI))
		{
			_ai = newAI;
		}
	}
}