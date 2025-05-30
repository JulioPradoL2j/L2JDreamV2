package com.dream.game.network.clientpackets;

import org.apache.log4j.Logger;

import com.dream.game.ai.CtrlIntention;
import com.dream.game.datatables.xml.MapRegionTable;
import com.dream.game.datatables.xml.MapRegionTable.TeleportWhereType;
import com.dream.game.manager.CastleManager;
import com.dream.game.manager.ClanHallManager;
import com.dream.game.manager.FortManager;
import com.dream.game.manager.FortSiegeManager;
import com.dream.game.manager.SiegeManager;
import com.dream.game.manager.clanhallsiege.BanditStrongholdSiege;
import com.dream.game.manager.clanhallsiege.WildBeastFarmSiege;
import com.dream.game.model.L2SiegeClan;
import com.dream.game.model.actor.L2Summon;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.entity.ClanHall;
import com.dream.game.model.entity.siege.Castle;
import com.dream.game.model.entity.siege.Fort;
import com.dream.game.model.entity.siege.FortSiege;
import com.dream.game.model.entity.siege.Siege;
import com.dream.game.model.world.Location;
import com.dream.game.model.zone.L2Zone;
import com.dream.game.network.ThreadPoolManager;

public class RequestRestartPoint extends L2GameClientPacket
{
	private class DeathTask implements Runnable
	{
		private final L2PcInstance activeChar;

		public DeathTask(L2PcInstance _activeChar)
		{
			activeChar = _activeChar;
		}

		@Override
		public void run()
		{
			try
			{
				Location loc = null;
				Siege siege = null;
				FortSiege fsiege = null;

				if (activeChar.isInJail())
				{
					_requestedPointType = 27;
				}
				else if (activeChar.isFestivalParticipant())
				{
					_requestedPointType = 5;
				}
				if ((activeChar._event != null) && activeChar._event.isRunning())
				{
					if (activeChar._event.requestRevive(activeChar, _requestedPointType))
					{
						return;
					}
				}
				switch (_requestedPointType)
				{
					case 1:
						if (activeChar.getClan() == null || activeChar.getClan().getHasHideout() == 0)
							return;
						Fort fort = FortManager.getInstance().getFortByOwner(activeChar.getClan());
						if (fort != null)
						{
							if (fort.getFunction(Fort.FUNC_RESTORE_EXP) != null)
							{
								activeChar.restoreExp(fort.getFunction(Fort.FUNC_RESTORE_EXP).getLvl());
							}
							loc = MapRegionTable.getInstance().getTeleToLocation(activeChar, TeleportWhereType.Fortress);
						}
						else
						{
							loc = MapRegionTable.getInstance().getTeleToLocation(activeChar, TeleportWhereType.ClanHall);

							if (ClanHallManager.getInstance().getClanHallByOwner(activeChar.getClan()) != null && ClanHallManager.getInstance().getClanHallByOwner(activeChar.getClan()).getFunction(ClanHall.FUNC_RESTORE_EXP) != null)
							{
								activeChar.restoreExp(ClanHallManager.getInstance().getClanHallByOwner(activeChar.getClan()).getFunction(ClanHall.FUNC_RESTORE_EXP).getLvl());
							}
						}
						break;
					case 2:
						siege = SiegeManager.getSiege(activeChar);
						if (siege != null && siege.getIsInProgress())
						{
							if (siege.checkIsDefender(activeChar.getClan()))
							{
								loc = MapRegionTable.getInstance().getTeleToLocation(activeChar, TeleportWhereType.Castle);
							}
							else if (siege.checkIsAttacker(activeChar.getClan()))
							{
								loc = MapRegionTable.getInstance().getTeleToLocation(activeChar, TeleportWhereType.Town);
							}
							else
								return;
						}
						else
						{
							if (activeChar.getClan() == null || activeChar.getClan().getHasCastle() == 0)
								return;
							loc = MapRegionTable.getInstance().getTeleToLocation(activeChar, TeleportWhereType.Castle);
						}
						Castle castle = CastleManager.getInstance().getCastleByOwner(activeChar.getClan());
						if (castle != null && castle.getFunction(Castle.FUNC_RESTORE_EXP) != null)
						{
							activeChar.restoreExp(CastleManager.getInstance().getCastleByOwner(activeChar.getClan()).getFunction(Castle.FUNC_RESTORE_EXP).getLvl());
						}
						break;
					case 3:
						L2SiegeClan siegeClan = null;
						siege = SiegeManager.getSiege(activeChar);

						fsiege = FortSiegeManager.getSiege(activeChar);

						if (fsiege == null && siege != null && siege.getIsInProgress())
						{
							siegeClan = siege.getAttackerClan(activeChar.getClan());
						}
						else if (siege == null && fsiege != null && fsiege.getIsInProgress())
						{
							siegeClan = fsiege.getAttackerClan(activeChar.getClan());
						}
						if (!BanditStrongholdSiege.getInstance().isPlayerRegister(activeChar.getClan(), activeChar.getName()) && !WildBeastFarmSiege.getInstance().isPlayerRegister(activeChar.getClan(), activeChar.getName()))
							if (siegeClan == null || siegeClan.getFlag().size() == 0)
								return;
						loc = MapRegionTable.getInstance().getTeleToLocation(activeChar, TeleportWhereType.SiegeFlag);
						break;
					case 4:
						if (!activeChar.isGM() && !activeChar.isFestivalParticipant())
							return;
						if (activeChar.isGM())
						{
							activeChar.restoreExp(100.0);
						}
						loc = new Location(activeChar.getX(), activeChar.getY(), activeChar.getZ());
						break;
					case 27:
						if (!activeChar.isInJail())
							return;
						loc = new Location(-114356, -249645, -2984);
						break;
					default:
						if (activeChar.isInsideZone(L2Zone.FLAG_JAIL) || activeChar.isInsideZone(L2Zone.FLAG_NOESCAPE))
						{
							loc = new Location(activeChar.getX(), activeChar.getY(), activeChar.getZ());
						}
						else
						{
							loc = MapRegionTable.getInstance().getTeleToLocation(activeChar, TeleportWhereType.Town);
						}
						break;
				}

				activeChar.setIsPendingRevive(true);
				activeChar.teleToLocation(loc, true);
				if (activeChar.getPet() != null)
				{
					L2Summon pet = activeChar.getPet();
					pet.abortAttack();
					pet.abortCast();
					pet.getAI().setIntention(CtrlIntention.ACTIVE);
					pet.teleToLocation(loc, false);
				}
			}
			catch (Exception e)
			{
				_log.error(e.getMessage(), e);
			}
		}
	}

	public static Logger _log = Logger.getLogger(RequestRestartPoint.class.getName());
	protected int _requestedPointType;

	protected boolean _continuation;

	@Override
	protected void readImpl()
	{
		_requestedPointType = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();

		if (activeChar == null)
			return;

		if (activeChar.isFakeDeath())
		{
			activeChar.stopFakeDeath(null);
			return;
		}
		else if (!activeChar.isDead())
			return;

		Castle castle = CastleManager.getInstance().getCastle(activeChar.getX(), activeChar.getY(), activeChar.getZ());
		if (castle != null && castle.getSiege().getIsInProgress())
			if (activeChar.getClan() != null && castle.getSiege().checkIsAttacker(activeChar.getClan()))
			{
				int restartTime = Siege.getAttackerRespawnDelay();
				ThreadPoolManager.getInstance().scheduleGeneral(new DeathTask(activeChar), restartTime);
				if (restartTime > 0)
				{
					activeChar.sendMessage("You will be teleported in " + restartTime / 1000 + " seconds.");
				}
				return;
			}
		new DeathTask(activeChar).run();
	}

}