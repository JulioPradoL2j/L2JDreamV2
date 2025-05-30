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
package com.dream.game.model;

import java.util.concurrent.Future;

import com.dream.game.datatables.sql.NpcTable;
import com.dream.game.manager.FishermanManager;
import com.dream.game.manager.games.fishingChampionship;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.actor.instance.L2PenaltyMonsterInstance;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.ThreadPoolManager;
import com.dream.game.network.serverpackets.ExFishingHpRegen;
import com.dream.game.network.serverpackets.ExFishingStartCombat;
import com.dream.game.network.serverpackets.SystemMessage;
import com.dream.game.templates.chars.L2NpcTemplate;
import com.dream.tools.random.Rnd;

public class L2Fishing implements Runnable
{
	private L2PcInstance _fisher;
	private int _time;
	private int _stop = 0;
	private int _goodUse = 0;
	private int _anim = 0;
	private int _mode = 0;
	private int _deceptiveMode = 0;
	private Future<?> _fishAiTask;
	private boolean _thinking;
	private final int _fishId;
	private final int _fishMaxHp;
	private int _fishCurHp;
	private final double _regenHp;
	private final boolean _isUpperGrade;
	private int _lureType;

	public L2Fishing(L2PcInstance fisher, FishData fish, boolean isNoob, boolean isUpperGrade)
	{
		_fisher = fisher;
		_fishMaxHp = fish.getHP();
		_fishCurHp = _fishMaxHp;
		_regenHp = fish.getHpRegen();
		_fishId = fish.getId();
		_time = fish.getCombatTime() / 1000;
		_isUpperGrade = isUpperGrade;
		if (isUpperGrade)
		{
			_deceptiveMode = Rnd.get(100) >= 90 ? 1 : 0;
			_lureType = 2;
		}
		else
		{
			_deceptiveMode = 0;
			_lureType = isNoob ? 0 : 1;
		}
		_mode = Rnd.get(100) >= 80 ? 1 : 0;

		ExFishingStartCombat efsc = new ExFishingStartCombat(_fisher, _time, _fishMaxHp, _mode, _lureType, _deceptiveMode);
		_fisher.broadcastPacket(efsc);

		_fisher.sendPacket(SystemMessageId.GOT_A_BITE);

		if (_fishAiTask == null)
		{
			_fishAiTask = ThreadPoolManager.getInstance().scheduleEffectAtFixedRate(this, 1000, 1000);
		}

	}

	protected void aiTask()
	{
		if (_thinking)
			return;
		_thinking = true;
		_time--;

		try
		{
			if (_mode == 1)
			{
				if (_deceptiveMode == 0)
				{
					_fishCurHp += (int) _regenHp;
				}
			}
			else if (_deceptiveMode == 1)
			{
				_fishCurHp += (int) _regenHp;
			}
			if (_stop == 0)
			{
				_stop = 1;
				int check = Rnd.get(100);
				if (check >= 70)
				{
					_mode = _mode == 0 ? 1 : 0;
				}
				if (_isUpperGrade)
				{
					check = Rnd.get(100);
					if (check >= 90)
					{
						_deceptiveMode = _deceptiveMode == 0 ? 1 : 0;
					}
				}
			}
			else
			{
				_stop--;
			}
		}
		finally
		{
			_thinking = false;
			ExFishingHpRegen efhr = new ExFishingHpRegen(_fisher, _time, _fishCurHp, _mode, 0, _anim, 0, _deceptiveMode);
			if (_anim != 0)
			{
				_fisher.broadcastPacket(efhr);
			}
			else
			{
				_fisher.sendPacket(efhr);
			}
		}
	}

	public void changeHp(int hp, int pen)
	{
		_fishCurHp -= hp;
		if (_fishCurHp < 0)
		{
			_fishCurHp = 0;
		}

		ExFishingHpRegen efhr = new ExFishingHpRegen(_fisher, _time, _fishCurHp, _mode, _goodUse, _anim, pen, _deceptiveMode);
		_fisher.broadcastPacket(efhr);
		_anim = 0;
		if (_fishCurHp > _fishMaxHp * 2)
		{
			_fishCurHp = _fishMaxHp * 2;
			doDie(false);
		}
		else if (_fishCurHp == 0)
		{
			doDie(true);
		}
	}

	public synchronized void doDie(boolean win)
	{
		if (_fishAiTask != null)
		{
			_fishAiTask.cancel(false);
			_fishAiTask = null;
		}

		if (_fisher == null)
			return;

		if (win)
		{
			int check = Rnd.get(100);
			if (check <= 5)
			{
				spawnPenaltyMonster();
			}
			else
			{
				_fisher.sendPacket(SystemMessageId.YOU_CAUGHT_SOMETHING);
				_fisher.addItem("Fishing", _fishId, 1, null, true);
				FishermanManager.getInstance().onCatch(_fisher.getObjectId(), _fisher.getName());
				fishingChampionship.getInstance().newFish(_fisher);
			}
		}
		else
		{
			FishermanManager.getInstance().onEscape(_fisher.getObjectId(), _fisher.getName());
		}
		_fisher.endFishing(win);
		_fisher = null;
	}

	@Override
	public void run()
	{
		if (_fisher == null)
			return;

		if (_fishCurHp >= _fishMaxHp * 2)
		{
			_fisher.sendPacket(SystemMessageId.BAIT_STOLEN_BY_FISH);
			doDie(false);
		}
		else if (_time <= 0)
		{
			_fisher.sendPacket(SystemMessageId.FISH_SPIT_THE_HOOK);
			doDie(false);
		}
		else
		{
			aiTask();
		}
	}

	private void spawnPenaltyMonster()
	{
		int lvl = (int) Math.round(_fisher.getLevel() * 0.1);
		int npcid;

		_fisher.sendPacket(SystemMessageId.YOU_CAUGHT_SOMETHING_SMELLY_THROW_IT_BACK);
		switch (lvl)
		{
			case 0:
			case 1:
				npcid = 18319;
				break;
			case 2:
				npcid = 18320;
				break;
			case 3:
				npcid = 18321;
				break;
			case 4:
				npcid = 18322;
				break;
			case 5:
				npcid = 18323;
				break;
			case 6:
				npcid = 18324;
				break;
			case 7:
				npcid = 18325;
				break;
			case 8:
				npcid = 18326;
				break;
			default:
				npcid = 18319;
				break;
		}
		L2NpcTemplate temp;
		temp = NpcTable.getInstance().getTemplate(npcid);
		if (temp != null)
		{
			try
			{
				L2Spawn spawn = new L2Spawn(temp);
				spawn.setLocx(_fisher.getFishx());
				spawn.setLocy(_fisher.getFishy());
				spawn.setLocz(_fisher.getFishz());
				spawn.setAmount(1);
				spawn.setHeading(_fisher.getHeading());
				spawn.stopRespawn();
				((L2PenaltyMonsterInstance) spawn.doSpawn()).setPlayerToKill(_fisher);
			}
			catch (Exception e)
			{

			}
		}
	}

	public void usePomping(int dmg, int pen)
	{
		_anim = 1;
		if (Rnd.get(100) > 90)
		{
			_fisher.sendPacket(SystemMessageId.FISH_RESISTED_ATTEMPT_TO_BRING_IT_IN);
			_goodUse = 0;
			changeHp(0, pen);
			return;
		}
		if (_fisher == null)
			return;
		if (_mode == 0)
		{
			if (_deceptiveMode == 0)
			{
				_fisher.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.PUMPING_SUCCESFUL_S1_DAMAGE).addNumber(dmg));
				if (pen == 50)
				{
					_fisher.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.PUMPING_SUCCESSFUL_PENALTY_S1).addNumber(pen));
				}
				_goodUse = 1;
				changeHp(dmg, pen);
			}
			else
			{
				_fisher.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.FISH_RESISTED_PUMPING_S1_HP_REGAINED).addNumber(dmg));
				_goodUse = 2;
				changeHp(-dmg, pen);
			}
		}
		else if (_deceptiveMode == 0)
		{
			_fisher.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.FISH_RESISTED_PUMPING_S1_HP_REGAINED).addNumber(dmg));
			_goodUse = 2;
			changeHp(-dmg, pen);
		}
		else
		{
			_fisher.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.PUMPING_SUCCESFUL_S1_DAMAGE).addNumber(dmg));
			if (pen == 50)
			{
				_fisher.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.PUMPING_SUCCESSFUL_PENALTY_S1).addNumber(pen));
			}
			_goodUse = 1;
			changeHp(dmg, pen);
		}
	}

	public void useRealing(int dmg, int pen)
	{
		_anim = 2;
		if (Rnd.get(100) > 90)
		{
			_fisher.sendPacket(SystemMessageId.FISH_RESISTED_ATTEMPT_TO_BRING_IT_IN);
			_goodUse = 0;
			changeHp(0, pen);
			return;
		}
		if (_fisher == null)
			return;
		if (_mode == 1)
		{
			if (_deceptiveMode == 0)
			{
				_fisher.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.REELING_SUCCESFUL_S1_DAMAGE).addNumber(dmg));
				if (pen == 50)
				{
					_fisher.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.REELING_SUCCESSFUL_PENALTY_S1).addNumber(pen));
				}
				_goodUse = 1;
				changeHp(dmg, pen);
			}
			else
			{
				_fisher.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.FISH_RESISTED_REELING_S1_HP_REGAINED).addNumber(dmg));
				_goodUse = 2;
				changeHp(-dmg, pen);
			}
		}
		else if (_deceptiveMode == 0)
		{
			_fisher.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.FISH_RESISTED_REELING_S1_HP_REGAINED).addNumber(dmg));
			_goodUse = 2;
			changeHp(-dmg, pen);
		}
		else
		{
			_fisher.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.REELING_SUCCESFUL_S1_DAMAGE).addNumber(dmg));
			if (pen == 50)
			{
				_fisher.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.REELING_SUCCESSFUL_PENALTY_S1).addNumber(pen));
			}
			_goodUse = 1;
			changeHp(dmg, pen);
		}
	}
}