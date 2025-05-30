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

import java.util.List;

import com.dream.game.ai.CtrlIntention;
import com.dream.game.manager.FortSiegeManager;
import com.dream.game.manager.FortSiegeManager.SiegeSpawn;
import com.dream.game.model.L2Skill;
import com.dream.game.model.L2Spawn;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.L2Summon;
import com.dream.game.model.actor.position.L2CharPosition;
import com.dream.game.network.ThreadPoolManager;
import com.dream.game.network.serverpackets.NpcSay;
import com.dream.game.templates.chars.L2NpcTemplate;

public class L2FortCommanderInstance extends L2FortSiegeGuardInstance
{
	public class ScheduleTalkTask implements Runnable
	{
		@Override
		public void run()
		{
			setCanTalk(true);
		}
	}

	private boolean _canTalk;

	public L2FortCommanderInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		getKnownList();
		_canTalk = true;
	}

	@Override
	public final void addDamage(L2Character attacker, int damage, L2Skill skill)
	{
		L2Spawn spawn = getSpawn();
		if (spawn != null && canTalk())
		{
			List<SiegeSpawn> commanders = FortSiegeManager.getInstance().getCommanderSpawnList(getFort().getFortId());
			for (SiegeSpawn spawn2 : commanders)
				if (spawn2.getNpcId() == spawn.getNpcid())
				{
					String text = "";
					switch (spawn2.getId())
					{
						case 1:
							text = "Attacking the enemy's reinforcements is necesary. Time to Die!";
							break;
						case 2:
							if (attacker instanceof L2Summon)
							{
								attacker = ((L2Summon) attacker).getOwner();
							}
							text = "Everyone, concentrate your attacks on " + attacker.getName() + "! Show the enemy your resolve!";
							break;
						case 3:
							text = "Spirit of Fire, unleash your power! Burn the enemy!!";
							break;
					}
					if (!text.isEmpty())
					{
						broadcastPacket(new NpcSay(getObjectId(), 1, getNpcId(), text));
						setCanTalk(false);
						ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleTalkTask(), 10000);
					}
				}
		}
		super.addDamage(attacker, damage, skill);
	}

	@Override
	public void addDamageHate(L2Character attacker, int damage, int aggro, L2Skill skill)
	{
		if (attacker == null)
			return;

		if (!(attacker instanceof L2FortCommanderInstance))
		{
			super.addDamageHate(attacker, damage, aggro, skill);
		}
	}

	private boolean canTalk()
	{
		return _canTalk;
	}

	@Override
	public boolean doDie(L2Character killer)
	{
		if (!super.doDie(killer))
			return false;

		if (getFort().getSiege().getIsInProgress())
		{
			getFort().getSiege().killedCommander(this);
		}

		return true;
	}

	@Override
	public boolean hasRandomAnimation()
	{
		return false;
	}

	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		if (attacker == null || !(attacker instanceof L2PcInstance))
			return false;

		boolean isFort = getFort() != null && getFort().getFortId() > 0 && getFort().getSiege().getIsInProgress() && !getFort().getSiege().checkIsDefender(((L2PcInstance) attacker).getClan());

		return isFort;
	}

	@Override
	public void returnHome()
	{
		if (!isInsideRadius(getSpawn().getLocx(), getSpawn().getLocy(), 200, false))
		{
			setisReturningToSpawnPoint(true);
			clearAggroList();
			if (hasAI())
			{
				getAI().setIntention(CtrlIntention.MOVE_TO, new L2CharPosition(getSpawn().getLocx(), getSpawn().getLocy(), getSpawn().getLocz(), 0));
			}
		}
	}

	void setCanTalk(boolean val)
	{
		_canTalk = val;
	}
}