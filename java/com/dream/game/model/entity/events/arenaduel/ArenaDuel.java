package com.dream.game.model.entity.events.arenaduel;

import com.dream.Config;
import com.dream.game.datatables.sql.ItemTable;
import com.dream.game.model.L2Effect;
import com.dream.game.model.actor.L2Summon;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.actor.instance.L2PetInstance;
import com.dream.game.model.itemcontainer.PcInventory;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.ThreadPoolManager;
import com.dream.game.network.serverpackets.ExShowScreenMessage;
import com.dream.game.network.serverpackets.SystemMessage;
import com.dream.tools.random.Rnd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class ArenaDuel implements Runnable
{
	protected static final Logger _log = Logger.getLogger(ArenaDuel.class.getName());

	// list of participants
	public static List<Solo> registered;
	// number of Arenas
	int free = Config.ARENA_DUEL_1X1_ARENA_COUNT;
	// Arenas
	Arena[] arenas = new Arena[Config.ARENA_DUEL_1X1_ARENA_COUNT];
	// list of fights going on
	Map<Integer, String> fights = new HashMap<>(Config.ARENA_DUEL_1X1_ARENA_COUNT);

	public ArenaDuel()
	{
		registered = new ArrayList<>();
		int[] coord;
		for (int i = 0; i < Config.ARENA_DUEL_1X1_ARENA_COUNT; i++)
		{
			coord = Config.ARENA_DUEL_1X1_ARENA_LOCS[i];
			arenas[i] = new Arena(i, coord[0], coord[1], coord[2]);
		}

		System.out.println("Initialized ArenaDuel 1x1 Event");
	}

	public static ArenaDuel getInstance()
	{
		return SingletonHolder.INSTANCE;
	}

	public boolean register(L2PcInstance player)
	{
		for (Solo p : registered)
		{
			if (p.getSide() == player)
			{
				player.sendMessage("You have already been registered in a waiting list of 1x1.");
				return false;
			}
		}
		return registered.add(new Solo(player));
	}

	public boolean isRegistered(L2PcInstance player)
	{
		for (Solo p : registered)
		{
			if (p.getSide() == player)
			{
				return true;
			}
		}
		return false;
	}

	public void addSpectator(L2PcInstance spec, int arenaId)
	{
		Arena arena = getArena(arenaId);
		if (arena != null)
			arena.addSpectator(spec);
	}

	private Arena getArena(int id)
	{
		for (Arena arena : arenas)
		{
			if (arena.id == id)
				return arena;
		}
		return null;
	}

	public Map<Integer, String> getFights()
	{
		return fights;
	}

	public boolean remove(L2PcInstance player)
	{
		for (Solo p : registered)
		{
			if (p.getSide() == player)
			{
				p.removeMessage();
				registered.remove(p);
				return true;
			}
		}
		return false;
	}

	@Override
	public synchronized void run()
	{
		// while server is running
		while (true)
		{
			// if no have participants or arenas are busy wait 1 minute
			if (registered.size() < 2 || free == 0)
			{
				try
				{
					Thread.sleep(Config.ARENA_DUEL_CALL_INTERVAL);
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
				continue;
			}
			List<Solo> opponents = selectOpponents();
			if (opponents != null && opponents.size() == 2)
			{
				Thread T = new Thread(new EvtArenaTask(opponents));
				T.setDaemon(true);
				T.start();
			}
			// wait 1 minute for not stress server
			try
			{
				Thread.sleep(Config.ARENA_DUEL_CALL_INTERVAL);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
	}

	@SuppressWarnings("null")
	private static List<Solo> selectOpponents()
	{
		List<Solo> opponents = new ArrayList<>();
		Solo sideOne = null, sideTwo = null;
		int tries = 3;
		do
		{
			int first = 0, second = 0;
			if (getRegisteredCount() < 2)
				return opponents;

			if (sideOne == null)
			{
				first = Rnd.get(getRegisteredCount());
				sideOne = registered.get(first);
				if (sideOne.check())
				{
					opponents.add(0, sideOne);
					registered.remove(first);
				}
				else
				{
					sideOne = null;
					registered.remove(first);
					return null;
				}

			}
			if (sideTwo == null)
			{
				second = Rnd.get(getRegisteredCount());
				sideTwo = registered.get(second);
				if (sideTwo.check())
				{
					opponents.add(1, sideTwo);
					registered.remove(second);
				}
				else
				{
					sideTwo = null;
					registered.remove(second);
					return null;
				}

			}
		}
		while ((sideOne == null || sideTwo == null) && --tries > 0);
		return opponents;
	}

	public static int getRegisteredCount()
	{
		return registered.size();
	}

	private class Solo
	{
		private L2PcInstance player;

		public Solo(L2PcInstance side)
		{
			this.player = side;
		}

		public L2PcInstance getSide()
		{
			return player;
		}

		public boolean check()
		{
			if (player == null || player.isOnline() != 1)
			{
				player.sendMessage("You participation in 1x1 was canceled.");
				return false;
			}
			return true;
		}

		public boolean isDead()
		{
			if (player == null || player.isDead() || player.isOnline() != 1 || !player.isArenaAttack())
				return false;

			return !(player.isDead());
		}

		public boolean isAlive()
		{
			if (player == null || player.isDead() || player.isOnline() != 1 || !player.isArenaAttack())
				return false;

			return !(player.isDead());
		}

		public void teleportTo(int x, int y, int z)
		{
			if (player != null && player.isOnline() != 0)
			{
				player.setCurrentHpMp(player.getMaxHp(), player.getMaxMp());
				player.setCurrentCp(player.getMaxCp());
				player.teleToLocation(x, y, z, false);
			}
		}

		public void rewards()
		{
			SystemMessage sm = null;

			if (player != null && player.isOnline() != 0)
			{
				for (int[] reward : Config.ARENA_DUEL_1X1_REWARD)
				{
					PcInventory inv = player.getInventory();

					// Check for stackable item, non stackabe items need to be added one by one
					if (ItemTable.getInstance().createDummyItem(reward[0]).isStackable())
					{
						inv.addItem("Tournament Reward:", reward[0], reward[1], player, null);

						if (reward[1] > 1)
						{
							sm = SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S);
							sm.addItemName(reward[0]);
							sm.addItemNumber(reward[1]);
						}
						else
						{
							sm = SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S);
							sm.addItemName(reward[0]);
						}

						player.sendPacket(sm);
					}
					else
					{
						for (int i = 0; i < reward[1]; ++i)
						{
							inv.addItem("Tournament Reward:", reward[0], 1, player, null);
							sm = SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S);
							sm.addItemName(reward[0]);
							player.sendPacket(sm);
						}
					}
				}
			}
			player.increaseArenaWins();
			sendPacket("Congratulations, " + player.getName() + " is victorious!", 5);
		}

		public void setInTournamentEvent(boolean val)
		{
			if (player != null && player.isOnline() != 0)
				player.setInArenaEvent(val);
		}

		public void removeMessage()
		{
			if (player != null && player.isOnline() != 0)
			{
				player.sendMessage("Your participation in the 1x1 event has been removed.");
				player.setArenaProtection(false);
			}
		}

		public void setArenaProtection(boolean val)
		{
			if (player != null && player.isOnline() != 0)
				player.setArenaProtection(val);
		}

		public void revive()
		{
			if (player != null && player.isOnline() != 0 && player.isDead())
				player.doRevive();
		}

		public void removeBuff()
		{
			if (player != null && player.isOnline() != 0)
			{
				for (L2Effect effect : player.getAllEffects())
				{
					if (effect.getSkill().getId() == 353 || effect.getSkill().getId() == 354 || effect.getSkill().getId() == 437 || effect.getSkill().getId() == 412 || effect.getSkill().getId() == 1099 || effect.getSkill().getId() == 1099 || effect.getSkill().getId() == 1160 || effect.getSkill().getId() == 1170 || effect.getSkill().getId() == 1208 || effect.getSkill().getId() == 1222 || effect.getSkill().getId() == 1246 || effect.getSkill().getId() == 1247 || effect.getSkill().getId() == 1248 || effect.getSkill().getId() == 1269 || effect.getSkill().getId() == 1298 || effect.getSkill().getId() == 1336 || effect.getSkill().getId() == 1337 || effect.getSkill().getId() == 1338 || effect.getSkill().getId() == 1339 || effect.getSkill().getId() == 1340 || effect.getSkill().getId() == 1341 || effect.getSkill().getId() == 1342 || effect.getSkill().getId() == 1343 || effect.getSkill().getId() == 1358 || effect.getSkill().getId() == 1359 || effect.getSkill().getId() == 1360 || effect.getSkill().getId() == 1361 || effect.getSkill().getId() == 1366 || effect.getSkill().getId() == 1367 || effect.getSkill().getId() == 4052 || effect.getSkill().getId() == 4053 || effect.getSkill().getId() == 4054 || effect.getSkill().getId() == 4055 || effect.getSkill().getId() == 4164 || effect.getSkill().getId() == 5116 || effect.getSkill().getId() == 406 || effect.getSkill().getId() == 139 || effect.getSkill().getId() == 176 || effect.getSkill().getId() == 420)
					{
						player.stopSkillEffects(effect.getSkill().getId());
						player.enableSkill(effect.getSkill().getId());
					}
				}
			}
		}

		public void removeSummon()
		{
			if (player != null && player.isOnline() != 0)
			{
				// Remove Summon's buffs
				if (player.getPet() != null)
				{
					L2Summon summon = player.getPet();
					if (summon != null)
						summon.unSummon(summon.getOwner());

					if (summon instanceof L2PetInstance)
						summon.unSummon(player);
				}

				if (player.getMountType() == 1 || player.getMountType() == 2)
					player.dismount();
			}
		}

		public void setImobilised(boolean val)
		{
			if (player != null && player.isOnline() != 0)
			{
				player.setIsInvul(val);
				player.setIsParalyzed(val);
			}
		}

		public void setArenaAttack(boolean val)
		{
			if (player != null && player.isOnline() != 0)
				player.setArenaAttack(val);
		}

		public void sendPacket(String message, int duration)
		{
			if (player != null && player.isOnline() != 0)
				player.sendPacket(new ExShowScreenMessage(message, duration * 1000));
		}

		public void initCountdown(int duration)
		{
			if (player != null && player.isOnline() != 0)
				ThreadPoolManager.getInstance().scheduleGeneral(new Countdown(player, duration), 0);
		}

		public void setFlag()
		{
			if (player != null && player.isOnline() != 0)
				player.setPvpFlag(1);
			player.updatePvPStatus();
			player.broadcastUserInfo();
		}

		public void setUnFlag()
		{
			if (player != null && player.isOnline() != 0)
			{
				player.stopPvPFlag();
				player.setPvpFlag(0);
				player.updatePvPFlag(0);
				player.broadcastUserInfo();
			}
		}

		public void increasedefeats()
		{
			if (player != null)
				player.increaseArenaDefeats();
		}
	}

	private class EvtArenaTask implements Runnable
	{
		private final Solo sideOne;
		private final Solo sideTwo;
		private final int pOneX, pOneY, pOneZ, pTwoX, pTwoY, pTwoZ;
		private Arena arena;

		public EvtArenaTask(List<Solo> opponents)
		{
			sideOne = opponents.get(0);
			sideTwo = opponents.get(1);

			L2PcInstance leader = sideOne.getSide();
			pOneX = leader.getX();
			pOneY = leader.getY();
			pOneZ = leader.getZ();

			leader = sideTwo.getSide();
			pTwoX = leader.getX();
			pTwoY = leader.getY();
			pTwoZ = leader.getZ();
		}

		@Override
		public void run()
		{
			free--;
			portPairsToArena();
			sideOne.setFlag();
			sideTwo.setFlag();
			sideOne.initCountdown(20);
			sideTwo.initCountdown(20);
			try
			{
				Thread.sleep(Config.ARENA_DUEL_WAIT_INTERVAL);
			}
			catch (InterruptedException e1)
			{
				e1.printStackTrace();
			}
			sideOne.sendPacket("Begin the match!", 5);
			sideTwo.sendPacket("Begin the match!", 5);
			sideOne.setImobilised(false);
			sideTwo.setImobilised(false);
			sideOne.setArenaAttack(true);
			sideTwo.setArenaAttack(true);
			sideOne.removeBuff();
			sideTwo.removeBuff();
			sideOne.removeSummon();
			sideTwo.removeSummon();

			while (check())
			{
				// check players status each seconds
				try
				{
					Thread.sleep(Config.ARENA_DUEL_CHECK_INTERVAL);
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
					break;
				}
			}
			finishDuel();
			free++;
		}

		private void finishDuel()
		{
			fights.remove(arena.id);
			rewardWinner();
			sideOne.revive();
			sideTwo.revive();
			sideOne.setUnFlag();
			sideTwo.setUnFlag();
			sideOne.removeBuff();
			sideTwo.removeBuff();
			sideOne.teleportTo(pOneX, pOneY, pOneZ);
			sideTwo.teleportTo(pTwoX, pTwoY, pTwoZ);
			sideOne.setInTournamentEvent(false);
			sideTwo.setInTournamentEvent(false);
			sideOne.setArenaProtection(false);
			sideTwo.setArenaProtection(false);
			sideOne.setArenaAttack(false);
			sideTwo.setArenaAttack(false);
			arena.setFree(true);
		}

		private void rewardWinner()
		{
			if (sideOne.isAlive() && !sideTwo.isAlive())
			{
				sideOne.rewards();
				sideTwo.increasedefeats();
			}
			else if (sideTwo.isAlive() && !sideOne.isAlive())
			{
				sideTwo.rewards();
				sideOne.increasedefeats();
			}
		}

		private boolean check()
		{
			return (sideOne.isDead() && sideTwo.isDead());
		}

		private void portPairsToArena()
		{
			for (Arena arena : arenas)
			{
				if (arena.isFree)
				{
					this.arena = arena;
					arena.setFree(false);
					sideOne.teleportTo(arena.x - 300, arena.y, arena.z);
					sideTwo.teleportTo(arena.x + 300, arena.y, arena.z);
					sideOne.setImobilised(true);
					sideTwo.setImobilised(true);
					sideOne.setInTournamentEvent(true);
					sideTwo.setInTournamentEvent(true);
					fights.put(this.arena.id, sideOne.getSide().getName() + " Vs " + sideTwo.getSide().getName());
					break;
				}
			}
		}
	}

	private class Arena
	{
		protected int x, y, z;
		protected boolean isFree = true;
		int id;

		public Arena(int id, int x, int y, int z)
		{
			this.id = id;
			this.x = x;
			this.y = y;
			this.z = z;
		}

		public void setFree(boolean val)
		{
			isFree = val;
		}

		public void addSpectator(L2PcInstance spec)
		{
			spec.enterObserverMode(x, y, z);
		}
	}

	protected class Countdown implements Runnable
	{
		private final L2PcInstance _player;
		private int _time;

		public Countdown(L2PcInstance player, int time)
		{
			_time = time;
			_player = player;
		}

		@Override
		public void run()
		{
			if (_player.isOnline() != 0)
			{
				switch (_time)
				{
					case 300:
					case 240:
					case 180:
					case 120:
					case 60:
						_player.sendMessage(_time + " second(s) to start the battle.");
						break;
					case 45:
						_player.sendMessage(_time + " second(s) to start the battle!");
						break;
					case 30:
						_player.sendMessage(_time + " second(s) to start the battle!");
						break;
					case 20:
						_player.sendMessage(_time + " second(s) to start the battle!");
						break;
					case 15:
						_player.sendMessage(_time + " second(s) to start the battle!");
						break;
					case 10:
						_player.sendMessage(_time + " second(s) to start the battle!");
						break;
					case 5:
						_player.sendMessage(_time + " second(s) to start the battle!");
						break;
					case 4:
						_player.sendMessage(_time + " second(s) to start the battle!");
						break;
					case 3:
						_player.sendMessage(_time + " second(s) to start the battle!");
						break;
					case 2:
						_player.sendMessage(_time + " second(s) to start the battle!");
						break;
					case 1:
						_player.sendMessage(_time + " second(s) to start the battle!");
						break;
				}
				if (_time > 1)
				{
					ThreadPoolManager.getInstance().scheduleGeneral(new Countdown(_player, _time - 1), 1000);
				}
			}
		}
	}

	/**
	 * Protection against Dual Box Feed
	 * @return
	 */
	public static Map<Integer, L2PcInstance> allParticipants()
	{
		Map<Integer, L2PcInstance> all = new HashMap<>();
		if (getRegisteredCount() > 0)
		{
			for (Solo dp : registered)
				all.put(dp.getSide().getObjectId(), dp.getSide());
			return all;
		}
		return all;
	}

	private static class SingletonHolder
	{
		protected static final ArenaDuel INSTANCE = new ArenaDuel();
	}
}