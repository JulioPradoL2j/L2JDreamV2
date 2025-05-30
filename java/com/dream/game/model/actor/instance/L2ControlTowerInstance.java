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

import java.util.ArrayList;
import java.util.List;

import com.dream.game.ai.CtrlIntention;
import com.dream.game.geodata.GeoData;
import com.dream.game.model.L2Spawn;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.L2Npc;
import com.dream.game.network.serverpackets.ActionFailed;
import com.dream.game.network.serverpackets.MyTargetSelected;
import com.dream.game.network.serverpackets.StatusUpdate;
import com.dream.game.network.serverpackets.ValidateLocation;
import com.dream.game.templates.chars.L2NpcTemplate;

public class L2ControlTowerInstance extends L2Npc
{

	private List<L2Spawn> _guards;

	public L2ControlTowerInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public final void decayMe()
	{
		if (getCastle().getSiege().getIsInProgress())
		{
			getCastle().getSiege().killedCT(this);

			if (getGuards() != null && !getGuards().isEmpty())
			{
				for (L2Spawn spawn : getGuards())
				{
					if (spawn == null)
					{
						continue;
					}
					spawn.stopRespawn();
				}
			}
		}
		super.decayMe();
	}

	public final List<L2Spawn> getGuards()
	{
		if (_guards == null)
		{
			_guards = new ArrayList<>();
		}
		return _guards;
	}

	@Override
	public boolean isAttackable()
	{
		return getCastle() != null && getCastle().getCastleId() > 0 && getCastle().getSiege().getIsInProgress();
	}

	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		return attacker != null && attacker instanceof L2PcInstance && getCastle() != null && getCastle().getCastleId() > 0 && getCastle().getSiege().getIsInProgress() && getCastle().getSiege().checkIsAttacker(((L2PcInstance) attacker).getClan());
	}

	@Override
	public void onAction(L2PcInstance player)
	{
		if (!canTarget(player))
			return;

		if (this != player.getTarget())
		{
			player.setTarget(this);
			player.sendPacket(new MyTargetSelected(getObjectId(), player.getLevel() - getLevel()));

			StatusUpdate su = new StatusUpdate(this);
			su.addAttribute(StatusUpdate.CUR_HP, (int) getStatus().getCurrentHp());
			su.addAttribute(StatusUpdate.MAX_HP, getMaxHp());
			player.sendPacket(su);

			player.sendPacket(new ValidateLocation(this));
		}
		else if (isAutoAttackable(player) && Math.abs(player.getZ() - getZ()) < 100 && GeoData.getInstance().canSeeTarget(player, this))
		{
			player.getAI().setIntention(CtrlIntention.ATTACK, this);

			player.sendPacket(ActionFailed.STATIC_PACKET);
		}
	}

	@Override
	public void onForcedAttack(L2PcInstance player)
	{
		onAction(player);
	}

	public void registerGuard(L2Spawn guard)
	{
		getGuards().add(guard);
	}
}