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
import com.dream.game.datatables.xml.ZoneTable;
import com.dream.game.geodata.GeoData;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.L2Npc;
import com.dream.game.model.zone.L2TrapZone;
import com.dream.game.model.zone.L2Zone;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.ActionFailed;
import com.dream.game.network.serverpackets.MoveToPawn;
import com.dream.game.network.serverpackets.SystemMessage;
import com.dream.game.templates.chars.L2NpcTemplate;

public class L2FlameTowerInstance extends L2Npc
{
	private int _upgradeLevel;
	private List<Integer> _zoneList;

	public L2FlameTowerInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void deleteMe()
	{
		enableZones(false);
		super.deleteMe();
	}

	@Override
	public boolean doDie(L2Character killer)
	{
		enableZones(false);

		if (getCastle() != null)
			// Message occurs only if the trap was triggered first.
			if (_zoneList != null && _upgradeLevel != 0)
			{
				getCastle().getSiege().announceToPlayer(SystemMessage.getSystemMessage(SystemMessageId.A_TRAP_DEVICE_HAS_BEEN_STOPPED), false);
			}

		return super.doDie(killer);
	}

	public final void enableZones(boolean state)
	{
		if (_zoneList != null && _upgradeLevel != 0)
		{
			final int maxIndex = _upgradeLevel * 2;
			for (int i = 0; i < maxIndex; i++)
			{
				final L2Zone zone = ZoneTable.getInstance().getZoneById(_zoneList.get(i));
				if (zone != null && zone instanceof L2TrapZone)
				{
					((L2TrapZone) zone).setEnabled(state);
				}
			}
		}
	}

	@Override
	public boolean isAttackable()
	{
		// Attackable during siege by attacker only
		return getCastle() != null && getCastle().getSiege().getIsInProgress();
	}

	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		// Attackable during siege by attacker only
		return attacker != null && attacker instanceof L2PcInstance && getCastle() != null && getCastle().getSiege().getIsInProgress() && getCastle().getSiege().checkIsAttacker(((L2PcInstance) attacker).getClan());
	}

	@Override
	public void onAction(L2PcInstance player)
	{
		// Set the target of the L2PcInstance player
		if (player.getTarget() != this)
		{
			player.setTarget(this);
		}
		else if (isAutoAttackable(player) && Math.abs(player.getZ() - getZ()) < 100 && GeoData.getInstance().canSeeTarget(player, this))
		{
			// Notify the L2PcInstance AI with INTERACT
			player.getAI().setIntention(CtrlIntention.ATTACK, this);
		}
		else
		{
			// Rotate the player to face the instance
			player.sendPacket(new MoveToPawn(player, this, L2Npc.INTERACTION_DISTANCE));

			// Send ActionFailed to the player in order to avoid he stucks
			player.sendPacket(ActionFailed.STATIC_PACKET);
		}
	}

	@Override
	public void onForcedAttack(L2PcInstance player)
	{
		onAction(player);
	}

	public final void setUpgradeLevel(int level)
	{
		_upgradeLevel = level;
	}

	public final void setZoneList(List<Integer> list)
	{
		_zoneList = list;
		enableZones(true);
	}
}