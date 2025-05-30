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
package com.dream.game.model.entity;

import com.dream.Message;
import com.dream.game.ai.CtrlIntention;
import com.dream.game.datatables.xml.SkillTable;
import com.dream.game.idfactory.IdFactory;
import com.dream.game.manager.DuelManager;
import com.dream.game.manager.SiegeManager;
import com.dream.game.model.L2Effect;
import com.dream.game.model.L2Object;
import com.dream.game.model.L2Skill;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.L2Summon;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.actor.instance.L2PetInstance;
import com.dream.game.model.actor.instance.L2SummonInstance;
import com.dream.game.model.base.Experience;
import com.dream.game.model.world.L2World;
import com.dream.game.model.zone.L2Zone;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.ThreadPoolManager;
import com.dream.game.network.serverpackets.ActionFailed;
import com.dream.game.network.serverpackets.ExDuelEnd;
import com.dream.game.network.serverpackets.ExDuelReady;
import com.dream.game.network.serverpackets.ExDuelStart;
import com.dream.game.network.serverpackets.ExDuelUpdateUserInfo;
import com.dream.game.network.serverpackets.L2GameServerPacket;
import com.dream.game.network.serverpackets.PlaySound;
import com.dream.game.network.serverpackets.SocialAction;
import com.dream.game.network.serverpackets.SystemMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

import org.apache.log4j.Logger;

import javolution.util.FastList;

public class Duel
{
	public static enum DuelResultEnum
	{
		Continue,
		Team1Win,
		Team2Win,
		Team1Surrender,
		Team2Surrender,
		Canceled,
		Timeout
	}

	public class PlayerCondition
	{
		private L2PcInstance _player;
		private L2Summon _summon;
		private double _hp;
		private double _mp;
		private double _cp;
		private double _hpS;
		private double _mpS;
		private boolean _paDuel;
		private int _x, _y, _z;
		private final Map<Integer, Integer> _summonEffects = new HashMap<>();

		public PlayerCondition(L2PcInstance player, boolean partyDuel)
		{
			if (player == null)
				return;
			_player = player;
			_hp = _player.getStatus().getCurrentHp();
			_mp = _player.getStatus().getCurrentMp();
			_cp = _player.getStatus().getCurrentCp();
			_summon = player.getPet();
			if (_summon != null)
			{
				_hpS = _summon.getStatus().getCurrentHp();
				_mpS = _summon.getStatus().getCurrentMp();
				for (L2Effect e : _summon.getAllEffects())
					if (e != null)
					{
						_summonEffects.put(e.getSkill().getId(), e.getSkill().getLevel());
					}
			}
			_player.store();
			_paDuel = partyDuel;

			if (_paDuel)
			{
				_x = _player.getX();
				_y = _player.getY();
				_z = _player.getZ();
			}
		}

		public L2PcInstance getPlayer()
		{
			return _player;
		}

		public void restoreCondition()
		{
			if (_player == null)
				return;
			_player.getStatus().setCurrentHpMp(_hp, _mp);
			_player.getStatus().setCurrentCp(_cp);

			L2Summon summon = _player.getPet();
			if (_summon != summon)
			{
				if (_summon != null)
				{
					if (summon == null)
						if (!(_summon instanceof L2PetInstance))
						{
							L2SummonInstance newSummon = new L2SummonInstance(IdFactory.getInstance().getNextId(), _summon.getTemplate(), _player, null);
							newSummon.setName(_summon.getTemplate().getName());
							newSummon.setTitle(_player.getName());
							newSummon.setExpPenalty(((L2SummonInstance) _summon).getExpPenalty());
							if (newSummon.getLevel() >= Experience.LEVEL.length)
							{
								newSummon.getStat().setExp(Experience.LEVEL[Experience.LEVEL.length - 1]);
							}
							else
							{
								newSummon.getStat().setExp(Experience.LEVEL[newSummon.getLevel() % Experience.LEVEL.length]);
							}
							newSummon.getStatus().setCurrentHp(_hpS);
							newSummon.getStatus().setCurrentMp(_mpS);
							newSummon.setHeading(_player.getHeading());
							newSummon.setRunning();
							_player.setPet(newSummon);
							newSummon.stopAllEffects();
							if (_summonEffects != null)
							{
								for (int temp : _summonEffects.keySet())
								{
									L2Skill skill = SkillTable.getInstance().getInfo(temp, _summonEffects.get(temp));
									if (skill != null)
									{
										skill.getEffects(newSummon, newSummon);
									}
								}
							}
							L2World.getInstance().storeObject(newSummon);
							newSummon.spawnMe(_player.getX() + 50, _player.getY() + 100, _player.getZ());
						}
				}
				else if (summon instanceof L2PetInstance)
				{
					if (summon.isDead())
					{
						summon.doRevive();
					}
					summon.unSummon(_player);
				}

			}
			else if (_summon != null)
			{
				if (_summon.isDead() && _summon instanceof L2PetInstance)
				{
					_summon.doRevive();
					_summon.getStatus().setCurrentHpMp(_hpS, _mpS);
				}
				else
				{
					_summon.getStatus().setCurrentHpMp(_hpS, _mpS);
				}
				_summon.stopAllEffects();
				if (_summonEffects != null)
				{
					for (int temp : _summonEffects.keySet())
					{
						L2Skill skill = SkillTable.getInstance().getInfo(temp, _summonEffects.get(temp));
						if (skill != null)
						{
							skill.getEffects(_summon, _summon);
						}
					}
				}
			}

			if (_paDuel)
			{
				teleportBack();
				// _player.stopAllEffects();
				// _player.restoreEffects();
			}

		}

		public void teleportBack()
		{
			_player.teleToLocation(_x, _y, _z);
		}
	}

	public class ScheduleDuelTask implements Runnable
	{
		private final Duel _duel;

		public ScheduleDuelTask(Duel duel)
		{
			_duel = duel;
		}

		@Override
		public void run()
		{
			try
			{
				DuelResultEnum status = _duel.checkEndDuelCondition();

				if (status == DuelResultEnum.Canceled)
				{
					setFinished(true);
					_duel.endDuel(status);
				}
				else if (status != DuelResultEnum.Continue)
				{
					setFinished(true);
					playKneelAnimation();
					_duelEndTask = ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleEndDuelTask(_duel, status), 5000);
				}
				else
				{
					_duelTask = ThreadPoolManager.getInstance().scheduleGeneral(this, 1000);
				}
			}
			catch (Exception e)
			{
				_log.error(e.getMessage(), e);
			}
		}
	}

	public class ScheduleEndDuelTask implements Runnable
	{
		private final Duel _duel;
		private final DuelResultEnum _result;

		public ScheduleEndDuelTask(Duel duel, DuelResultEnum result)
		{
			_duel = duel;
			_result = result;
		}

		@Override
		public void run()
		{
			try
			{
				_duel.endDuel(_result);
			}
			catch (Exception e)
			{
				_log.error(e.getMessage(), e);
			}
		}
	}

	public class ScheduleStartDuelTask implements Runnable
	{
		private final Duel _duel;

		public ScheduleStartDuelTask(Duel duel)
		{
			_duel = duel;
		}

		@Override
		public void run()
		{
			try
			{
				int count = _duel.countdown();

				if (count == 4)
				{
					_duel.teleportPlayers(149485, 46718, -3413);

					ThreadPoolManager.getInstance().scheduleGeneral(this, 20000);
				}
				else if (count > 0)
				{
					ThreadPoolManager.getInstance().scheduleGeneral(this, 1000);
				}
				else
				{
					_duel.startDuel();
				}
			}
			catch (Exception e)
			{
				_log.error(e.getMessage(), e);
			}
		}
	}

	public final static Logger _log = Logger.getLogger(Duel.class.getName());

	public static final int DUELSTATE_NODUEL = 0;

	public static final int DUELSTATE_DUELLING = 1;
	public static final int DUELSTATE_DEAD = 2;
	public static final int DUELSTATE_WINNER = 3;
	public static final int DUELSTATE_INTERRUPTED = 4;
	private static L2PcInstance attacker;
	private static L2PcInstance target;

	private static void doFightStop(L2PcInstance temp)
	{
		if (temp.getPet() != null)
		{
			temp.getPet().abortCast();
			temp.getPet().getAI().setIntention(CtrlIntention.ACTIVE, null);
		}
		temp.abortCast();
		temp.getAI().setIntention(CtrlIntention.ACTIVE);
		temp.setTarget(null);
		temp.sendPacket(ActionFailed.STATIC_PACKET);
	}

	public static boolean isInvul(L2Character targetChar, L2Character attackerChar)
	{
		attacker = L2Object.getActingPlayer(attackerChar);
		target = L2Object.getActingPlayer(targetChar);

		if (attacker == null && target == null)
			return false;

		final boolean attackerIsInDuel = attacker != null && attacker.isInDuel();
		final boolean targetIsInDuel = target != null && target.isInDuel();

		if (!attackerIsInDuel && !targetIsInDuel)
			return false;

		if (attackerIsInDuel)
			if (attacker.getDuelState() == Duel.DUELSTATE_DEAD || attacker.getDuelState() == Duel.DUELSTATE_WINNER)
				return true;

		if (targetIsInDuel)
			if (target.getDuelState() == Duel.DUELSTATE_DEAD || target.getDuelState() == Duel.DUELSTATE_WINNER)
				return true;

		if (attackerIsInDuel && targetIsInDuel && attacker.getDuelId() == target.getDuelId())
			return false;

		if (attackerIsInDuel)
		{
			attacker.setDuelState(Duel.DUELSTATE_INTERRUPTED);
		}

		if (targetIsInDuel)
		{
			target.setDuelState(Duel.DUELSTATE_INTERRUPTED);
		}

		return false;
	}

	private final int _duelId;

	private L2PcInstance _playerA;

	private L2PcInstance _playerB;

	private final boolean _partyDuel;

	private long _duelEndTime;

	private int _surrenderRequest = 0;

	private int _countdown = 4;

	private boolean _finished = false;

	public ScheduledFuture<?> _duelTask = null;

	public ScheduledFuture<?> _duelEndTask = null;

	private FastList<PlayerCondition> _playerConditions;

	public Duel(L2PcInstance playerA, L2PcInstance playerB, int partyDuel, int duelId)
	{
		_duelId = duelId;
		_playerA = playerA;
		_playerB = playerB;
		_partyDuel = partyDuel == 1;

		_duelEndTime = System.currentTimeMillis();
		if (_partyDuel)
		{
			_duelEndTime += 300 * 1000;
		}
		else
		{
			_duelEndTime += 120 * 1000;
		}

		_playerConditions = new FastList<>();

		setFinished(false);

		if (_partyDuel)
		{
			_countdown++;
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.IN_A_MOMENT_YOU_WILL_BE_TRANSPORTED_TO_THE_SITE_WHERE_THE_DUEL_WILL_TAKE_PLACE);
			broadcastToTeam1(sm);
			broadcastToTeam2(sm);
		}
		ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleStartDuelTask(this), 3000);
	}

	public void broadcastToTeam1(L2GameServerPacket packet)
	{
		if (_playerA == null)
			return;

		if (_partyDuel && _playerA.getParty() != null)
		{
			for (L2PcInstance temp : _playerA.getParty().getPartyMembers())
			{
				temp.sendPacket(packet);
			}
		}
		else
		{
			_playerA.sendPacket(packet);
		}
	}

	public void broadcastToTeam2(L2GameServerPacket packet)
	{
		if (_playerB == null)
			return;

		if (_partyDuel && _playerB.getParty() != null)
		{
			for (L2PcInstance temp : _playerB.getParty().getPartyMembers())
			{
				temp.sendPacket(packet);
			}
		}
		else
		{
			_playerB.sendPacket(packet);
		}
	}

	public DuelResultEnum checkEndDuelCondition()
	{
		if (_playerA == null || _playerB == null)
			return DuelResultEnum.Canceled;

		if (_surrenderRequest != 0)
		{
			if (_surrenderRequest == 1)
				return DuelResultEnum.Team1Surrender;

			return DuelResultEnum.Team2Surrender;
		}
		else if (getRemainingTime() <= 0)
			return DuelResultEnum.Timeout;
		else if (_playerA.getDuelState() == DUELSTATE_WINNER)
		{
			stopFighting();
			return DuelResultEnum.Team1Win;
		}
		else if (_playerB.getDuelState() == DUELSTATE_WINNER)
		{
			stopFighting();
			return DuelResultEnum.Team2Win;
		}

		else if (!_partyDuel)
		{
			if (_playerA.getDuelState() == DUELSTATE_INTERRUPTED || _playerB.getDuelState() == DUELSTATE_INTERRUPTED)
				return DuelResultEnum.Canceled;

			if (!_playerA.isInsideRadius(_playerB, 1600, false, false))
				return DuelResultEnum.Canceled;

			if (isDuelistInPvp(true))
				return DuelResultEnum.Canceled;

			if (_playerA.isInsideZone(L2Zone.FLAG_PEACE) || _playerB.isInsideZone(L2Zone.FLAG_PEACE) || SiegeManager.checkIfInZone(_playerA) || SiegeManager.checkIfInZone(_playerB) || _playerA.isInsideZone(L2Zone.FLAG_PVP) || _playerB.isInsideZone(L2Zone.FLAG_PVP))
				return DuelResultEnum.Canceled;
		}

		return DuelResultEnum.Continue;
	}

	public int countdown()
	{
		_countdown--;

		if (_countdown > 3)
			return _countdown;
		SystemMessage sm = null;
		if (_countdown > 0)
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.THE_DUEL_WILL_BEGIN_IN_S1_SECONDS).addNumber(_countdown);
		}
		else
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.LET_THE_DUEL_BEGIN);
		}

		broadcastToTeam1(sm);
		broadcastToTeam2(sm);

		return _countdown;
	}

	public void doSurrender(L2PcInstance player)
	{
		if (_surrenderRequest != 0)
			return;

		stopFighting();

		if (_partyDuel)
		{
			if (_playerA.getParty().getPartyMembers().contains(player))
			{
				_surrenderRequest = 1;
				for (L2PcInstance temp : _playerA.getParty().getPartyMembers())
				{
					temp.setDuelState(DUELSTATE_DEAD);
				}
				for (L2PcInstance temp : _playerB.getParty().getPartyMembers())
				{
					temp.setDuelState(DUELSTATE_WINNER);
				}
			}
			else if (_playerB.getParty().getPartyMembers().contains(player))
			{
				_surrenderRequest = 2;
				for (L2PcInstance temp : _playerB.getParty().getPartyMembers())
				{
					temp.setDuelState(DUELSTATE_DEAD);
				}
				for (L2PcInstance temp : _playerA.getParty().getPartyMembers())
				{
					temp.setDuelState(DUELSTATE_WINNER);
				}
			}
		}
		else if (player == _playerA)
		{
			_surrenderRequest = 1;
			_playerA.setDuelState(DUELSTATE_DEAD);
			_playerB.setDuelState(DUELSTATE_WINNER);
		}
		else if (player == _playerB)
		{
			_surrenderRequest = 2;
			_playerB.setDuelState(DUELSTATE_DEAD);
			_playerA.setDuelState(DUELSTATE_WINNER);
		}
	}


	public void endDuel(DuelResultEnum result)
	{
		if (_playerA == null || _playerB == null)
		{
			_playerConditions.clear();
			_playerConditions = null;
			DuelManager.getInstance().removeDuel(this);
			if (_duelTask != null)
			{
				_duelTask.cancel(false);
			}
			if (_duelEndTask != null)
			{
				_duelEndTask.cancel(false);
			}
			return;
		}
		if (_duelTask != null)
		{
			_duelTask.cancel(false);
		}
		if (_duelEndTask != null)
		{
			_duelEndTask.cancel(false);
		}

		SystemMessage sm = null;
		switch (result)
		{
			case Team1Win:
				restorePlayerConditions(false);
				if (_partyDuel)
				{
					sm = SystemMessage.getSystemMessage(SystemMessageId.S1S_PARTY_HAS_WON_THE_DUEL).addString(_playerA.getName());
				}
				else
				{
					sm = SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_WON_THE_DUEL).addString(_playerA.getName());
				}

				broadcastToTeam1(sm);
				broadcastToTeam2(sm);
				break;
			case Team2Win:
				restorePlayerConditions(false);

				if (_partyDuel)
				{
					sm = SystemMessage.getSystemMessage(SystemMessageId.S1S_PARTY_HAS_WON_THE_DUEL).addString(_playerB.getName());
				}
				else
				{
					sm = SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_WON_THE_DUEL).addString(_playerB.getName());
				}

				broadcastToTeam1(sm);
				broadcastToTeam2(sm);
				break;
			case Team1Surrender:
				restorePlayerConditions(false);

				if (_partyDuel)
				{
					sm = SystemMessage.getSystemMessage(SystemMessageId.SINCE_S1S_PARTY_WITHDREW_FROM_THE_DUEL_S1S_PARTY_HAS_WON);
				}
				else
				{
					sm = SystemMessage.getSystemMessage(SystemMessageId.SINCE_S1_WITHDREW_FROM_THE_DUEL_S2_HAS_WON);
				}
				sm.addString(_playerA.getName());
				sm.addString(_playerB.getName());

				broadcastToTeam1(sm);
				broadcastToTeam2(sm);
				break;
			case Team2Surrender:
				restorePlayerConditions(false);

				if (_partyDuel)
				{
					sm = SystemMessage.getSystemMessage(SystemMessageId.SINCE_S1S_PARTY_WITHDREW_FROM_THE_DUEL_S1S_PARTY_HAS_WON);
				}
				else
				{
					sm = SystemMessage.getSystemMessage(SystemMessageId.SINCE_S1_WITHDREW_FROM_THE_DUEL_S2_HAS_WON);
				}
				sm.addString(_playerB.getName());
				sm.addString(_playerA.getName());

				broadcastToTeam1(sm);
				broadcastToTeam2(sm);
				break;
			case Canceled:
				stopFighting();
				restorePlayerConditions(true);
				broadcastToTeam1(SystemMessage.getSystemMessage(SystemMessageId.THE_DUEL_HAS_ENDED_IN_A_TIE));
				broadcastToTeam2(SystemMessage.getSystemMessage(SystemMessageId.THE_DUEL_HAS_ENDED_IN_A_TIE));
				break;
			case Timeout:
				stopFighting();
				restorePlayerConditions(false);
				broadcastToTeam1(SystemMessage.getSystemMessage(SystemMessageId.THE_DUEL_HAS_ENDED_IN_A_TIE));
				broadcastToTeam2(SystemMessage.getSystemMessage(SystemMessageId.THE_DUEL_HAS_ENDED_IN_A_TIE));
				break;
		}
		ExDuelEnd duelEnd = null;
		if (_partyDuel)
		{
			duelEnd = new ExDuelEnd(1);
		}
		else
		{
			duelEnd = new ExDuelEnd(0);
		}

		broadcastToTeam1(duelEnd);
		broadcastToTeam2(duelEnd);

		_playerConditions.clear();
		_playerConditions = null;
		DuelManager.getInstance().removeDuel(this);
	}

	public boolean getFinished()
	{
		return _finished;
	}

	public int getId()
	{
		return _duelId;
	}

	public L2PcInstance getLooser()
	{
		if (!getFinished() || _playerA == null || _playerB == null)
			return null;
		if (_playerA.getDuelState() == DUELSTATE_WINNER)
			return _playerB;
		else if (_playerB.getDuelState() == DUELSTATE_WINNER)
			return _playerA;
		return null;
	}

	public L2PcInstance getPlayerA()
	{
		return _playerA;
	}

	public L2PcInstance getPlayerB()
	{
		return _playerB;
	}

	public int getRemainingTime()
	{
		return (int) (_duelEndTime - System.currentTimeMillis());
	}

	public L2PcInstance getWinner()
	{
		if (!getFinished() || _playerA == null || _playerB == null)
			return null;
		if (_playerA.getDuelState() == DUELSTATE_WINNER)
			return _playerA;
		if (_playerB.getDuelState() == DUELSTATE_WINNER)
			return _playerB;
		return null;
	}

	public boolean isDuelistInPvp(boolean sendMessage)
	{
		if (_partyDuel)
			return false;
		else if (_playerA.getPvpFlag() != 0 || _playerB.getPvpFlag() != 0)
		{
			if (sendMessage)
			{
				_playerA.sendMessage(Message.getMessage(_playerA, Message.MessageId.MSG_DUEL_CANCELED_DUE_PVP));
				_playerB.sendMessage(Message.getMessage(_playerB, Message.MessageId.MSG_DUEL_CANCELED_DUE_PVP));
			}
			return true;
		}
		return false;
	}

	public boolean isPartyDuel()
	{
		return _partyDuel;
	}

	public void onPlayerDefeat(L2PcInstance player)
	{
		player.setDuelState(DUELSTATE_DEAD);

		if (_partyDuel)
		{
			boolean teamdefeated = true;
			for (L2PcInstance temp : player.getParty().getPartyMembers())
				if (temp.getDuelState() == DUELSTATE_DUELLING)
				{
					teamdefeated = false;
					break;
				}

			if (teamdefeated)
			{
				L2PcInstance winner = _playerA;
				if (_playerA.getParty().getPartyMembers().contains(player))
				{
					winner = _playerB;
				}

				for (L2PcInstance temp : winner.getParty().getPartyMembers())
				{
					temp.setDuelState(DUELSTATE_WINNER);
				}
			}
		}
		else
		{
			if (player != _playerA && player != _playerB)
			{
				_log.warn("Error in onPlayerDefeat(): player is not part of this 1vs1 duel");
			}

			if (_playerA == player)
			{
				_playerB.setDuelState(DUELSTATE_WINNER);
			}
			else
			{
				_playerA.setDuelState(DUELSTATE_WINNER);
			}
		}
	}

	public void onRemoveFromParty(L2PcInstance player)
	{
		if (!_partyDuel)
			return;

		if (player == _playerA || player == _playerB)
		{
			for (FastList.Node<PlayerCondition> e = _playerConditions.head(), end = _playerConditions.tail(); (e = e.getNext()) != end;)
			{
				restorePlayerConditions(player);
			}

			_playerA = null;
			_playerB = null;
		}
		else
		{
			for (FastList.Node<PlayerCondition> e = _playerConditions.head(), end = _playerConditions.tail(); (e = e.getNext()) != end;)
				if (e.getValue().getPlayer() == player)
				{
					restorePlayerConditions(player);
					break;
				}
		}
	}

	public void playKneelAnimation()
	{
		L2PcInstance looser = getLooser();

		if (looser == null)
			return;

		if (_partyDuel && looser.getParty() != null)
		{
			for (L2PcInstance temp : looser.getParty().getPartyMembers())
			{
				temp.broadcastPacket(new SocialAction(temp, 7));
			}
		}
		else
		{
			looser.broadcastPacket(new SocialAction(looser, 7));
		}
	}

	public void restorePlayerConditions(boolean abnormalDuelEnd)
	{
		if (_partyDuel)
		{
			for (L2PcInstance temp : _playerA.getParty().getPartyMembers())
			{
				temp.setIsInDuel(0);
				temp.setTeam(0);
				temp.broadcastUserInfo();
			}
			for (L2PcInstance temp : _playerB.getParty().getPartyMembers())
			{
				temp.setIsInDuel(0);
				temp.setTeam(0);
				temp.broadcastUserInfo();
			}
		}
		else
		{
			_playerA.setIsInDuel(0);
			_playerA.setTeam(0);
			_playerA.broadcastUserInfo();
			_playerB.setIsInDuel(0);
			_playerB.setTeam(0);
			_playerB.broadcastUserInfo();
		}

		if (abnormalDuelEnd)
			return;

		for (FastList.Node<PlayerCondition> e = _playerConditions.head(), end = _playerConditions.tail(); (e = e.getNext()) != end;)
		{
			e.getValue().restoreCondition();
		}
	}

	public void restorePlayerConditions(L2PcInstance pl)
	{
		pl.setIsInDuel(0);
		pl.setTeam(0);
		pl.broadcastUserInfo();
		for (PlayerCondition e : _playerConditions)
			if (e.getPlayer() == pl)
			{
				e.restoreCondition();
				_playerConditions.remove(e);
			}
	}

	public void savePlayerConditions()
	{
		if (_partyDuel)
		{
			for (L2PcInstance temp : _playerA.getParty().getPartyMembers())
			{
				_playerConditions.add(new PlayerCondition(temp, _partyDuel));
			}
			for (L2PcInstance temp : _playerB.getParty().getPartyMembers())
			{
				_playerConditions.add(new PlayerCondition(temp, _partyDuel));
			}
		}
		else
		{
			_playerConditions.add(new PlayerCondition(_playerA, _partyDuel));
			_playerConditions.add(new PlayerCondition(_playerB, _partyDuel));
		}
	}

	public void setFinished(boolean mode)
	{
		_finished = mode;
	}

	public void startDuel()
	{
		savePlayerConditions();

		if (_playerA.isInDuel() || _playerB.isInDuel())
		{
			_playerConditions.clear();
			_playerConditions = null;
			DuelManager.getInstance().removeDuel(this);
			return;
		}

		if (_partyDuel)
		{
			for (L2PcInstance temp : _playerA.getParty().getPartyMembers())
			{
				temp.cancelActiveTrade();
				temp.setIsInDuel(_duelId);
				temp.setTeam(1);
				temp.broadcastUserInfo();
				broadcastToTeam2(new ExDuelUpdateUserInfo(temp));
			}
			for (L2PcInstance temp : _playerB.getParty().getPartyMembers())
			{
				temp.cancelActiveTrade();
				temp.setIsInDuel(_duelId);
				temp.setTeam(2);
				temp.broadcastUserInfo();
				broadcastToTeam1(new ExDuelUpdateUserInfo(temp));
			}

			ExDuelReady ready = new ExDuelReady(1);
			ExDuelStart start = new ExDuelStart(1);

			broadcastToTeam1(ready);
			broadcastToTeam2(ready);
			broadcastToTeam1(start);
			broadcastToTeam2(start);
		}
		else
		{
			_playerA.setIsInDuel(_duelId);
			_playerA.setTeam(1);
			_playerB.setIsInDuel(_duelId);
			_playerB.setTeam(2);

			ExDuelReady ready = new ExDuelReady(0);
			ExDuelStart start = new ExDuelStart(0);

			broadcastToTeam1(ready);
			broadcastToTeam2(ready);
			broadcastToTeam1(start);
			broadcastToTeam2(start);

			broadcastToTeam1(new ExDuelUpdateUserInfo(_playerB));
			broadcastToTeam2(new ExDuelUpdateUserInfo(_playerA));
			_playerA.broadcastUserInfo();
			_playerB.broadcastUserInfo();
		}

		broadcastToTeam1(new PlaySound(1, "B04_S01", 0, 0, 0, 0, 0));
		broadcastToTeam2(new PlaySound(1, "B04_S01", 0, 0, 0, 0, 0));

		_duelTask = ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleDuelTask(this), 1000);
	}

	private void stopFighting()
	{
		if (_partyDuel)
		{
			for (L2PcInstance temp : _playerA.getParty().getPartyMembers())
			{
				doFightStop(temp);
			}
			for (L2PcInstance temp : _playerB.getParty().getPartyMembers())
			{
				doFightStop(temp);
			}
		}
		else
		{
			doFightStop(_playerA);
			doFightStop(_playerB);
		}
	}

	public void teleportPlayers(int x, int y, int z)
	{
		if (!_partyDuel)
			return;

		int offset = 0;

		for (L2PcInstance temp : _playerA.getParty().getPartyMembers())
		{
			temp.teleToLocation(x + offset - 180, y - 150, z);
			offset += 40;
		}
		offset = 0;
		for (L2PcInstance temp : _playerB.getParty().getPartyMembers())
		{
			temp.teleToLocation(x + offset - 180, y + 150, z);
			offset += 40;
		}
	}
}