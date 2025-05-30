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

import com.dream.game.ai.CtrlIntention;
import com.dream.game.ai.L2CharacterAI;
import com.dream.game.ai.L2FortSiegeGuardAI;
import com.dream.game.model.L2SiegeGuard;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.L2Playable;
import com.dream.game.model.actor.L2Summon;
import com.dream.game.model.actor.knownlist.FortSiegeGuardKnownList;
import com.dream.game.model.actor.position.L2CharPosition;
import com.dream.game.network.serverpackets.ActionFailed;
import com.dream.game.network.serverpackets.MyTargetSelected;
import com.dream.game.network.serverpackets.StatusUpdate;
import com.dream.game.network.serverpackets.ValidateLocation;
import com.dream.game.templates.chars.L2NpcTemplate;

public class L2FortSiegeGuardInstance extends L2SiegeGuard
{
	public L2FortSiegeGuardInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		getKnownList();
	}

	@Override
	public void addDamageHate(L2Character attacker, int damage, int aggro)
	{
		if (attacker == null)
			return;

		if (!(attacker instanceof L2FortSiegeGuardInstance))
		{
			if (attacker instanceof L2Playable)
			{
				L2PcInstance player = null;
				if (attacker instanceof L2PcInstance)
				{
					player = (L2PcInstance) attacker;
				}
				else if (attacker instanceof L2Summon)
				{
					player = ((L2Summon) attacker).getOwner();
				}
				if (player != null && player.getClan() != null && player.getClan().getHasFort() == getFort().getFortId())
					return;
			}
			super.addDamageHate(attacker, damage, aggro);
		}
	}

	@Override
	public L2CharacterAI getAI()
	{
		L2CharacterAI ai = _ai;
		if (ai == null)
		{
			synchronized (this)
			{
				if (_ai == null)
				{
					_ai = new L2FortSiegeGuardAI(new AIAccessor());
				}
				return _ai;
			}
		}
		return ai;
	}

	@Override
	public FortSiegeGuardKnownList getKnownList()
	{
		if (_knownList == null)
		{
			_knownList = new FortSiegeGuardKnownList(this);
		}

		return (FortSiegeGuardKnownList) _knownList;
	}

	@Override
	public boolean hasRandomAnimation()
	{
		return false;
	}

	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		if (attacker == null)
			return false;

		L2PcInstance player = attacker.getActingPlayer();
		if (player == null)
			return false;
		if (player.getClan() == null)
			return true;

		boolean isFort = getFort() != null && getFort().getSiege().getIsInProgress() && !getFort().getSiege().checkIsDefender(player.getClan());

		return isFort;
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
		else
		{
			if (isAutoAttackable(player) && !isAlikeDead())
				if (Math.abs(player.getZ() - getZ()) < 600)
				{
					player.getAI().setIntention(CtrlIntention.ATTACK, this);
				}
				else
				{
					player.sendPacket(ActionFailed.STATIC_PACKET);
				}
			if (!isAutoAttackable(player))
			{
				if (!canInteract(player))
				{
					player.getAI().setIntention(CtrlIntention.INTERACT, this);
				}
				else
				{
					showChatWindow(player, 0);
				}
				player.sendPacket(ActionFailed.STATIC_PACKET);
			}
		}
	}

	@Override
	public void returnHome()
	{
		if (getStat().getWalkSpeed() <= 0)
			return;

		if (getSpawn() != null && !isInsideRadius(getSpawn().getLocx(), getSpawn().getLocy(), 40, false))
		{
			setisReturningToSpawnPoint(true);
			clearAggroList();

			if (hasAI())
			{
				getAI().setIntention(CtrlIntention.MOVE_TO, new L2CharPosition(getSpawn().getLocx(), getSpawn().getLocy(), getSpawn().getLocz(), 0));
			}
		}
	}
}