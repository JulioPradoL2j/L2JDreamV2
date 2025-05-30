package com.dream.game.model;

import com.dream.Config;
import com.dream.game.manager.BossSpawnManager;
import com.dream.game.manager.RaidPointsManager;
import com.dream.game.manager.grandbosses.BossLair;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.instance.L2MonsterInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.ThreadPoolManager;
import com.dream.game.network.serverpackets.NpcHtmlMessage;
import com.dream.game.network.serverpackets.SystemMessage;
import com.dream.game.templates.chars.L2NpcTemplate;
import com.dream.tools.random.Rnd;

public abstract class L2Boss extends L2MonsterInstance
{
	private static final int BOSS_MAINTENANCE_INTERVAL = 10000;

	public static final int BOSS_INTERACTION_DISTANCE = 500;

	private BossSpawnManager.StatusEnum _raidStatus;

	public BossLair _lair;

	public L2Boss(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public boolean canInteract(L2PcInstance player)
	{
		return isInsideRadius(player, BOSS_INTERACTION_DISTANCE, false, false);
	}

	@Override
	public boolean doDie(L2Character killer)
	{
		if (!super.doDie(killer))
			return false;

		L2PcInstance player = killer.getActingPlayer();
		if (player != null)
		{
			broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.RAID_WAS_SUCCESSFUL));
			if (player.getParty() != null)
			{
				for (L2PcInstance member : player.getParty().getPartyMembers())
				{
					rewardRaidPoints(member);
					if (Config.ALLOW_KILL_BARAKIEL_SETNOBLES)
					{
						giveNobles(member);
						giveNobles(player);
					}
				}
			}
			else
			{
				rewardRaidPoints(player);
			}
		}
		if (_lair != null)
		{
			_lair.setUnspawn();
		}
		return true;
	}

	@Override
	protected int getMaintenanceInterval()
	{
		return BOSS_MAINTENANCE_INTERVAL;
	}

	public BossSpawnManager.StatusEnum getRaidStatus()
	{
		return _raidStatus;
	}

	private void giveNobles(L2PcInstance p)
	{
		if (getNpcId() == 25325 && !p.isNoble() && p.isSubClassActive() && p.getLevel() >= 75 && p.isInsideRadius(getX(), getY(), getZ(), 1000, false, false))
		{
			p.setNoble(true);
			p.addItem("Quest", 7694, 1, p, true);
			NpcHtmlMessage html = new NpcHtmlMessage(0);
			html.setHtml("<html><body>Congratulations, you're now a noble!<br1>Open the Skills & Magic (ALT+K) to see your acquired abilities.</body>");
			p.sendPacket(html);
		}
	}

	@Override
	public boolean hasRandomAnimation()
	{
		return false;
	}

	public void healFull()
	{
		super.getStatus().setCurrentHp(super.getMaxHp());
		super.getStatus().setCurrentMp(super.getMaxMp());
	}

	@Override
	protected void manageMinions()
	{
		_minionList.spawnMinions();
		_minionMaintainTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new Runnable()
		{
			@Override
			public void run()
			{
				L2Spawn bossSpawn = getSpawn();
				if (!isInsideRadius(bossSpawn.getLocx(), bossSpawn.getLocy(), bossSpawn.getLocz(), 5000, true, false))
				{
					teleToLocation(bossSpawn.getLocx(), bossSpawn.getLocy(), bossSpawn.getLocz(), true);
					healFull();
				}
				_minionList.maintainMinions();
			}
		}, 60000, getMaintenanceInterval() + Rnd.get(5000));
	}

	@Override
	public void reduceCurrentHp(double damage, L2Character attacker, boolean awake, boolean isDOT, L2Skill skill)
	{
		super.reduceCurrentHp(damage, attacker, awake, isDOT, skill);
	}

	private void rewardRaidPoints(L2PcInstance player)
	{
		int points = getLevel() / 2 + Rnd.get(-5, 5);
		RaidPointsManager.addPoints(player, getNpcId(), points);
		player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EARNED_S1_RAID_POINTS).addNumber(points));
	}

	public void setRaidStatus(BossSpawnManager.StatusEnum status)
	{
		_raidStatus = status;
	}
}