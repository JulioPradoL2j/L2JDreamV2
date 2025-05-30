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

import com.dream.Config;
import com.dream.game.ai.CtrlIntention;
import com.dream.game.manager.FortSiegeManager;
import com.dream.game.manager.SiegeManager;
import com.dream.game.model.L2Clan;
import com.dream.game.model.L2SiegeClan;
import com.dream.game.model.L2Skill;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.L2Npc;
import com.dream.game.model.entity.siege.FortSiege;
import com.dream.game.model.entity.siege.Siege;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.ThreadPoolManager;
import com.dream.game.network.serverpackets.ActionFailed;
import com.dream.game.network.serverpackets.MyTargetSelected;
import com.dream.game.network.serverpackets.StatusUpdate;
import com.dream.game.network.serverpackets.SystemMessage;
import com.dream.game.network.serverpackets.ValidateLocation;
import com.dream.game.templates.chars.L2NpcTemplate;

public class L2SiegeFlagInstance extends L2Npc
{
	public class ScheduleTalkTask implements Runnable
	{
		@Override
		public void run()
		{
			setCanTalk(true);
		}
	}

	private L2Clan _clan;
	private final L2PcInstance _player;
	private Siege _siege;
	private FortSiege _fortSiege;
	private final boolean _isAdvanced;
	private boolean _canTalk;

	private final boolean _autoSet;

	public L2SiegeFlagInstance(L2PcInstance player, int objectId, L2NpcTemplate template, boolean advanced, boolean autoSet, L2Clan clan)
	{
		super(objectId, template);

		_isAdvanced = advanced;
		_player = player;
		if (autoSet == true)
		{
			_siege = null;
			_fortSiege = null;
			_clan = clan;
		}
		else
		{
			_siege = SiegeManager.getSiege(_player);
			_fortSiege = FortSiegeManager.getSiege(_player);
			_clan = player.getClan();
		}
		_canTalk = true;
		_autoSet = autoSet;

		if (autoSet == false)
			if (_clan == null || _siege == null && _fortSiege == null)
			{
				deleteMe();
			}
			else if (_siege != null && _fortSiege == null)
			{
				L2SiegeClan sc = _siege.getAttackerClan(_player.getClan());
				if (sc == null)
				{
					deleteMe();
				}
				else
				{
					sc.addFlag(this);
				}
			}
			else if (_siege == null && _fortSiege != null)
			{
				L2SiegeClan sc = _fortSiege.getAttackerClan(_player.getClan());
				if (sc == null)
				{
					deleteMe();
				}
				else
				{
					sc.addFlag(this);
				}
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

		if (_siege != null)
		{
			L2SiegeClan sc = _siege.getAttackerClan(_player.getClan());
			if (sc != null)
			{
				sc.removeFlag(this);
			}
		}
		else if (_fortSiege != null)
		{
			L2SiegeClan sc = _fortSiege.getAttackerClan(_player.getClan());
			if (sc != null)
			{
				sc.removeFlag(this);
			}
		}

		return true;
	}

	@Override
	public boolean isAttackable()
	{
		if (_autoSet == true)
			return false;
		return true;
	}

	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		if (attacker instanceof L2PcInstance)
		{
			L2PcInstance pc = (L2PcInstance) attacker;
			return _clan != pc.getClan();
		}
		return true;
	}

	@Override
	public void onAction(L2PcInstance player)
	{
		if (_autoSet == true)
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		if (!_player.canBeTargetedByAtSiege(player) && Config.SIEGE_ONLY_REGISTERED)
			return;

		if (player == null || !canTarget(player))
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
		else if (isAutoAttackable(player) && Math.abs(player.getZ() - getZ()) < 100)
		{
			player.getAI().setIntention(CtrlIntention.ATTACK, this);
		}
		else
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
		}
	}

	@Override
	public void onForcedAttack(L2PcInstance player)
	{
		onAction(player);
	}

	@Override
	public void reduceCurrentHp(double damage, L2Character attacker, boolean awake, boolean isDOT, L2Skill skill)
	{
		if (_isAdvanced)
		{
			damage /= 2;
		}

		super.reduceCurrentHp(damage, attacker, awake, isDOT, skill);

		if (canTalk())
			if (getCastle() != null && getCastle().getSiege().getIsInProgress())
			{
				if (_clan != null)
				{
					_clan.broadcastToOnlineMembers(SystemMessage.getSystemMessage(SystemMessageId.BASE_UNDER_ATTACK));
					setCanTalk(false);
					ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleTalkTask(), 20000);
				}
			}
			else if (getFort() != null && getFort().getSiege().getIsInProgress())
				if (_clan != null)
				{
					_clan.broadcastToOnlineMembers(SystemMessage.getSystemMessage(SystemMessageId.BASE_UNDER_ATTACK));
					setCanTalk(false);
					ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleTalkTask(), 20000);
				}
	}

	void setCanTalk(boolean val)
	{
		_canTalk = val;
	}
}