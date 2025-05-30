package com.dream.game.model.actor.instance;

import com.dream.Config;
import com.dream.game.ai.CtrlIntention;
import com.dream.game.ai.L2CharacterAI;
import com.dream.game.ai.L2SiegeGuardAI;
import com.dream.game.datatables.sql.ClanTable;
import com.dream.game.manager.FortSiegeManager;
import com.dream.game.manager.SiegeManager;
import com.dream.game.manager.clanhallsiege.DevastatedCastleSiege;
import com.dream.game.manager.clanhallsiege.FortressOfDeadSiege;
import com.dream.game.model.L2Clan;
import com.dream.game.model.L2SiegeClan;
import com.dream.game.model.L2SiegeGuard;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.knownlist.SiegeGuardKnownList;
import com.dream.game.model.actor.position.L2CharPosition;
import com.dream.game.model.entity.siege.FortSiege;
import com.dream.game.model.entity.siege.Siege;
import com.dream.game.network.serverpackets.ActionFailed;
import com.dream.game.network.serverpackets.MyTargetSelected;
import com.dream.game.network.serverpackets.StatusUpdate;
import com.dream.game.network.serverpackets.ValidateLocation;
import com.dream.game.templates.chars.L2NpcTemplate;

public class L2SiegeGuardInstance extends L2SiegeGuard
{
	public L2SiegeGuardInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		getKnownList();
	}

	@Override
	public void addDamageHate(L2Character attacker, int damage, int aggro)
	{
		if (attacker == null)
			return;

		if (!(attacker instanceof L2SiegeGuardInstance))
		{
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
					_ai = new L2SiegeGuardAI(new AIAccessor());
				}
				return _ai;
			}
		}
		return ai;
	}

	@Override
	public SiegeGuardKnownList getKnownList()
	{
		if (_knownList == null)
		{
			_knownList = new SiegeGuardKnownList(this);
		}
		return (SiegeGuardKnownList) _knownList;
	}

	@Override
	public boolean hasRandomAnimation()
	{
		return false;
	}

	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		L2PcInstance player = attacker.getActingPlayer();
		if (player == null)
			return false;
		if (player.getClan() == null)
			return true;
		if (DevastatedCastleSiege.getInstance().checkIsRegistered(player.getClan()) && DevastatedCastleSiege.getInstance().getIsInProgress())
			return true;
		if (FortressOfDeadSiege.getInstance().checkIsRegistered(player.getClan()) && FortressOfDeadSiege.getInstance().getIsInProgress())
			return true;
		boolean isCastle = getCastle() != null && getCastle().getSiege().getIsInProgress() && !getCastle().getSiege().checkIsDefender(player.getClan());
		return isCastle;
	}

	@Override
	public void onAction(L2PcInstance player)
	{
		if (!canTarget(player))
			return;

		if (Config.SIEGE_ONLY_REGISTERED)
		{
			boolean opp = false;
			Siege siege = SiegeManager.getSiege(player);
			FortSiege fortSiege = FortSiegeManager.getSiege(player);
			L2Clan oppClan = player.getClan();
			if (siege != null && siege.getIsInProgress() && oppClan != null)
			{
				for (L2SiegeClan clan : siege.getAttackerClans())
				{
					L2Clan cl = ClanTable.getInstance().getClan(clan.getClanId());

					if (cl == oppClan || cl.getAllyId() == player.getAllyId())
					{
						opp = true;
						break;
					}
				}
				for (L2SiegeClan clan : siege.getDefenderClans())
				{
					L2Clan cl = ClanTable.getInstance().getClan(clan.getClanId());

					if (cl == oppClan || cl.getAllyId() == player.getAllyId())
					{
						opp = true;
						break;
					}
				}
			}
			else if (fortSiege != null && fortSiege.getIsInProgress() && oppClan != null)
			{
				for (L2SiegeClan clan : fortSiege.getAttackerClans())
				{
					L2Clan cl = ClanTable.getInstance().getClan(clan.getClanId());

					if (cl == oppClan || cl.getAllyId() == player.getAllyId())
					{
						opp = true;
						break;
					}
				}
			}
			else if (oppClan != null && DevastatedCastleSiege.getInstance().checkIsRegistered(oppClan) && DevastatedCastleSiege.getInstance().getIsInProgress())
			{
				opp = true;
			}
			else if (oppClan != null && FortressOfDeadSiege.getInstance().checkIsRegistered(oppClan) && FortressOfDeadSiege.getInstance().getIsInProgress())
			{
				opp = true;
			}

			if (!opp)
				return;
		}

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
			boolean AutoAT = isAutoAttackable(player);
			if (AutoAT && !isAlikeDead())
				if (Math.abs(player.getZ() - getZ()) < 600)
				{
					player.getAI().setIntention(CtrlIntention.ATTACK, this);
				}
				else
				{
					player.sendPacket(ActionFailed.STATIC_PACKET);
				}
			if (!AutoAT)
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