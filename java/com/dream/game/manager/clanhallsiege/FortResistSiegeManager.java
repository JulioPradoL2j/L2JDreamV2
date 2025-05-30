package com.dream.game.manager.clanhallsiege;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.dream.game.datatables.sql.NpcTable;
import com.dream.game.manager.ClanHallManager;
import com.dream.game.manager.ClanHallSiege;
import com.dream.game.model.L2Clan;
import com.dream.game.model.L2Spawn;
import com.dream.game.model.entity.ClanHall;
import com.dream.game.taskmanager.tasks.ExclusiveTask;
import com.dream.game.templates.chars.L2NpcTemplate;

public class FortResistSiegeManager extends ClanHallSiege
{
	public class DamageInfo
	{
		public L2Clan _clan;
		public long _damage;
	}

	protected static Logger _log = Logger.getLogger(FortResistSiegeManager.class.getName());

	private static FortResistSiegeManager _instance;

	public static final FortResistSiegeManager getInstance()
	{
		if (_instance == null)
		{
			_instance = new FortResistSiegeManager();
		}
		return _instance;
	}

	public static final FortResistSiegeManager load()
	{
		_log.info("Clan Hall Siege: Fortress Of Resistence Initialized.");
		if (_instance == null)
		{
			_instance = new FortResistSiegeManager();
		}
		return _instance;
	}

	private final List<L2Spawn> _questMobs = new ArrayList<>();

	private int _npcSpawnCount = 0;

	private final Map<Integer, DamageInfo> _clansDamageInfo = new HashMap<>();

	private final ExclusiveTask _endSiegeTask = new ExclusiveTask()
	{
		@Override
		protected void onElapsed()
		{
			if (!getIsInProgress())
			{
				cancel();
				return;
			}
			final long timeRemaining = _siegeEndDate.getTimeInMillis() - System.currentTimeMillis();
			if (timeRemaining <= 0)
			{
				endSiege(false);
				cancel();
				return;
			}
			schedule(timeRemaining);
		}
	};
	private final ExclusiveTask _startSiegeTask = new ExclusiveTask()
	{
		@Override
		protected void onElapsed()
		{
			if (getIsInProgress())
			{
				cancel();
				return;
			}

			final long timeRemaining = getSiegeDate().getTimeInMillis() - System.currentTimeMillis();
			if (timeRemaining <= 0)
			{
				startSiege();
				cancel();
				return;
			}
			schedule(timeRemaining);
		}
	};

	private FortResistSiegeManager()
	{
		long siegeDate = restoreSiegeDate(21);
		Calendar tmpDate = Calendar.getInstance();
		tmpDate.setTimeInMillis(siegeDate);
		setSiegeDate(tmpDate);
		setNewSiegeDate(siegeDate, 21, 22);
		_startSiegeTask.schedule(1000);
	}

	public void addSiegeDamage(L2Clan clan, long damage)
	{
		DamageInfo clanDamage = _clansDamageInfo.get(clan.getClanId());
		if (clanDamage != null)
		{
			clanDamage._damage += damage;
		}
		else
		{
			clanDamage = new DamageInfo();
			clanDamage._clan = clan;
			clanDamage._damage += damage;

			_clansDamageInfo.put(clan.getClanId(), clanDamage);
		}
	}

	public void addSiegeMob(int npcTemplate, int locx, int locy, int locz, int resp)
	{
		L2Spawn spawn1;
		L2NpcTemplate template1;

		template1 = NpcTable.getInstance().getTemplate(npcTemplate);
		if (template1 != null)
		{
			_npcSpawnCount++;
			spawn1 = new L2Spawn(template1);
			spawn1.setId(_npcSpawnCount);
			spawn1.setAmount(1);
			spawn1.setLocx(locx);
			spawn1.setLocy(locy);
			spawn1.setLocz(locz);
			spawn1.setHeading(0);
			spawn1.setRespawnDelay(resp);
			spawn1.setLocation(0);
			_questMobs.add(spawn1);
		}
	}

	public void endSiege(boolean type)
	{
		setIsInProgress(false);
		for (L2Spawn spawn : _questMobs)
		{
			if (spawn == null)
			{
				continue;
			}

			spawn.stopRespawn();
			if (spawn.getLastSpawn() != null)
			{
				spawn.getLastSpawn().doDie(spawn.getLastSpawn());
			}
		}
		if (type == true)
		{
			L2Clan clanIdMaxDamage = null;
			long tempMaxDamage = 0;
			for (DamageInfo damageInfo : _clansDamageInfo.values())
				if (damageInfo != null)
					if (damageInfo._damage > tempMaxDamage)
					{
						tempMaxDamage = damageInfo._damage;
						clanIdMaxDamage = damageInfo._clan;
					}
			if (clanIdMaxDamage != null)
			{
				ClanHall clanhall = null;
				clanhall = ClanHallManager.getInstance().getClanHallById(21);
				ClanHallManager.getInstance().setOwner(clanhall.getId(), clanIdMaxDamage);
			}
		}
		setNewSiegeDate(getSiegeDate().getTimeInMillis(), 21, 22);
		_startSiegeTask.schedule(1000);
	}

	public void startSiege()
	{
		setIsInProgress(true);
		_clansDamageInfo.clear();
		for (L2Spawn spawn : _questMobs)
			if (spawn != null)
			{
				spawn.init();
			}
		_siegeEndDate = Calendar.getInstance();
		_siegeEndDate.add(Calendar.MINUTE, 30);
		_endSiegeTask.schedule(1000);
	}

}