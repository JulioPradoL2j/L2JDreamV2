package com.dream.game.model;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;

import com.dream.Config;
import com.dream.game.Announcements;
import com.dream.game.datatables.sql.NpcTable;
import com.dream.game.datatables.xml.ZoneTable;
import com.dream.game.geodata.GeoData;
import com.dream.game.idfactory.IdFactory;
import com.dream.game.manager.TownManager;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.L2Npc;
import com.dream.game.model.actor.instance.L2MonsterInstance;
import com.dream.game.model.entity.Town;
import com.dream.game.model.world.Location;
import com.dream.game.model.zone.L2Zone.ZoneType;
import com.dream.game.network.ThreadPoolManager;
import com.dream.game.templates.chars.L2NpcTemplate;
import com.dream.game.util.RndCoord;
import com.dream.tools.random.Rnd;

public class L2Spawn
{
	class SpawnTask implements Runnable
	{
		private final L2Npc _oldNpc;
		public Future<?> _spawnTask;

		public SpawnTask(L2Npc pOldNpc)
		{
			_oldNpc = pOldNpc;
		}

		@Override
		public void run()
		{
			try
			{
				if (_doRespawn)
				{
					respawnNpc(_oldNpc);
				}
			}
			catch (Exception e)
			{
				_log.warn("", e);
			}
			_tasks.remove(_spawnTask);
			_scheduledCount--;
		}
	}

	protected static Logger _log = Logger.getLogger(L2Spawn.class.getName());

	private final static List<SpawnListener> _spawnListeners = new ArrayList<>();

	public static void addSpawnListener(SpawnListener listener)
	{
		synchronized (_spawnListeners)
		{
			_spawnListeners.add(listener);
		}
	}

	public static Location findPointToStay(int x, int y, int z, int j, int k)
	{
		Location pos;
		if (j <= 0)
		{
			j = 10;
		}
		if (k <= 0)
		{
			k = 200;
		}
		try
		{
			for (int i = 0; i < 5; i++)
			{
				pos = RndCoord.coordsRandomize(x, y, z, 0, j, k);

				if (GeoData.getInstance().canMoveFromToTarget(x, y, z, pos.getX(), pos.getY(), pos.getZ()) && GeoData.getInstance().canMoveFromToTarget(pos.getX(), pos.getY(), pos.getZ(), x, y, z) && Math.abs(pos.getZ() - z) < 12)
					return pos;
			}
		}
		catch (Exception e)
		{

		}
		return new Location(x, y, z);
	}

	public static void notifyNpcSpawned(L2Npc npc)
	{
		synchronized (_spawnListeners)
		{
			for (SpawnListener listener : _spawnListeners)
			{
				listener.npcSpawned(npc);
			}
		}
	}

	public static void removeSpawnListener(SpawnListener listener)
	{
		synchronized (_spawnListeners)
		{
			_spawnListeners.remove(listener);
		}
	}

	private boolean _jailSpawn;
	private L2NpcTemplate _template;
	private int _id;
	private int _dbid;
	private int _location;
	private int _maximumCount = 1;
	private int _currentCount;
	protected int _scheduledCount;
	private int _locX;
	private int _locY;
	private int _locYO;
	private int _locXO;
	private boolean _originalLocxSet = false;
	private boolean _originalLocySet = false;
	private int _locZ;
	private boolean _useRandomPosRespwn = false;
	private int _randomeRespawnRange;
	private int _spawnZoneSize = 0;
	private int _heading;
	private int _respawnDelay;
	private int _respawnMinDelay;
	private int _respawnMaxDelay;
	private Constructor<?> _constructor;

	protected boolean _doRespawn;

	protected boolean _isChamp;

	private boolean _customSpawn;

	private boolean _announceSpawn;

	private L2Npc _lastSpawn;

	public final List<Future<?>> _tasks = new ArrayList<>();

	public L2Spawn(L2NpcTemplate mobTemplate) throws SecurityException
	{
		init(mobTemplate);
	}

	public L2Spawn(Node node) throws SecurityException
	{
		Node v = node.getAttributes().getNamedItem("npcId");
		L2NpcTemplate tpl = NpcTable.getInstance().getTemplate(Integer.parseInt(v.getNodeValue()));
		init(tpl);
		v = node.getAttributes().getNamedItem("x");
		setLocx(Integer.parseInt(v.getNodeValue()));
		v = node.getAttributes().getNamedItem("y");
		setLocy(Integer.parseInt(v.getNodeValue()));
		v = node.getAttributes().getNamedItem("z");
		setLocz(Integer.parseInt(v.getNodeValue()));
		v = node.getAttributes().getNamedItem("respawn");
		if (v != null)
		{
			setRespawnDelay(Integer.parseInt(v.getNodeValue()));
		}
		else
		{
			stopRespawn();
		}
		v = node.getAttributes().getNamedItem("champion");
		if (v != null)
		{
			setChampion(Boolean.parseBoolean(v.getNodeValue()));
		}
	}

	public void changeTemplate(L2NpcTemplate mobTemplate)
	{
		stopRespawn();
		L2Npc npc = getLastSpawn();
		if (npc != null)
		{
			npc.deleteMe();
		}
		decreaseCount(npc);
		_template = mobTemplate;
		if (_template == null)
			return;
		String implementationName = _template.getType();

		if (mobTemplate.getNpcId() == 30995)
		{
			implementationName = "L2RaceManager";
		}
		if (mobTemplate.getNpcId() >= 31046 && mobTemplate.getNpcId() <= 31053)
		{
			implementationName = "L2SymbolMaker";
		}
		try
		{
			_constructor = Class.forName("com.dream.game.model.actor.instance." + implementationName + "Instance").getConstructors()[0];
		}
		catch (ClassNotFoundException e)
		{
			_log.error("Unable to create a Npc ! " + e);
		}
		init();
	}

	public synchronized void decreaseCount(L2Npc oldNpc)
	{

		if (_currentCount < 1)
			return;
		_currentCount--;

		if (isRespawnable() && _scheduledCount + _currentCount < _maximumCount)
		{

			_scheduledCount++;
			SpawnTask spawnTask = new SpawnTask(oldNpc);
			spawnTask._spawnTask = ThreadPoolManager.getInstance().scheduleGeneral(spawnTask, _respawnDelay);
			_tasks.add(spawnTask._spawnTask);
		}
	}

	public L2Npc doSpawn()
	{
		return doSpawn(false, false);
	}

	public L2Npc doSpawn(boolean isSummonSpawn)
	{
		return doSpawn(isSummonSpawn, false);
	}

	public L2Npc doSpawn(boolean isSummonSpawn, boolean firstspawn)
	{
		L2Npc mob = null;
		try
		{
			if (_template.getType().equalsIgnoreCase("L2Pet") || _template.getType().equalsIgnoreCase("L2Minion") || _template.getType().equalsIgnoreCase("L2Decoy") || _template.getType().equalsIgnoreCase("L2Trap") || _template.getType().equalsIgnoreCase("L2EffectPoint"))
			{
				_currentCount++;
				return mob;
			}

			Object[] parameters =
			{
				IdFactory.getInstance().getNextId(),
				_template
			};

			L2Object tmp = (L2Object) _constructor.newInstance(parameters);

			if (isSummonSpawn && tmp instanceof L2Character)
			{
				((L2Character) tmp).setShowSummonAnimation(isSummonSpawn);
			}

			if (!(tmp instanceof L2Npc))
				return mob;
			mob = (L2Npc) tmp;
			return intializeNpcInstance(mob, firstspawn);
		}
		catch (Exception e)
		{
			_currentCount++;
			_log.warn("NPC " + _template.getNpcId() + " class not found: ", e);
		}
		return mob;
	}

	public void enableRndRangeRespawn(boolean par)
	{
		_useRandomPosRespwn = par;
	}

	public int getAmount()
	{
		return _maximumCount;
	}

	public int getDbId()
	{
		return _dbid;
	}

	public int getHeading()
	{
		return _heading;
	}

	public int getId()
	{
		return _id;
	}

	public L2Npc getLastSpawn()
	{
		return _lastSpawn;
	}

	public int getLocation()
	{
		return _location;
	}

	public int getLocx()
	{
		return _locX;
	}

	public int getLocy()
	{
		return _locY;
	}

	public int getLocz()
	{
		return _locZ;
	}

	public int getNpcid()
	{
		return _template.getNpcId();
	}

	public int getNpcId()
	{
		return _template.getNpcId();
	}

	public int getOriginalLocx()
	{
		return _locXO;
	}

	public int getOriginalLocy()
	{
		return _locYO;
	}

	public int getRespawnDelay()
	{
		return _respawnDelay;
	}

	public int getRespawnMaxDelay()
	{
		return _respawnMaxDelay;
	}

	public int getRespawnMinDelay()
	{
		return _respawnMinDelay;
	}

	public int getRndRespawnRange()
	{
		return _randomeRespawnRange;
	}

	public int getSpawnZoneSize()
	{
		return _spawnZoneSize;
	}

	public L2NpcTemplate getTemplate()
	{
		return _template;
	}

	public int init()
	{
		return init(false);
	}

	public int init(boolean firstspawn)
	{
		while (_currentCount < _maximumCount)
		{
			doSpawn(false, firstspawn);
		}
		_doRespawn = true;

		return _currentCount;
	}

	private void init(L2NpcTemplate mobTemplate) throws SecurityException
	{
		_template = mobTemplate;
		if (_template == null)
			return;

		String implementationName = _template.getType();

		if (mobTemplate.getNpcId() == 30995)
		{
			implementationName = "L2RaceManager";
		}

		if (mobTemplate.getNpcId() >= 31046 && mobTemplate.getNpcId() <= 31053)
		{
			implementationName = "L2SymbolMaker";
		}

		try
		{
			_constructor = Class.forName("com.dream.game.model.actor.instance." + implementationName + "Instance").getConstructors()[0];
		}
		catch (ClassNotFoundException e)
		{
			_log.error("Unable to create a Npc ! " + e);
		}
	}

	private L2Npc intializeNpcInstance(L2Npc mob, boolean firstspawn)
	{
		int newlocx = 0, newlocy = 0, newlocz = 0;

		if (isRndRangeRespawn())
		{
			Location loc = findPointToStay(getLocx(), getLocy(), getLocz(), mob.getTemplate().getCollisionRadius() * 3, mob.getTemplate().getCollisionRadius() * 5);
			newlocx = loc.getX();
			newlocy = loc.getY();
			newlocz = loc.getZ();
		}
		else
		{
			newlocx = getLocx();
			newlocy = getLocy();
			newlocz = getLocz();
		}

		newlocz = GeoData.getInstance().getSpawnHeight(newlocx, newlocy, newlocz, newlocz, _id);
		for (L2Effect f : mob.getAllEffects())
			if (f != null)
			{
				mob.removeEffect(f);
			}

		if (Config.CHAMPION_ENABLE)
			if (ZoneTable.isReady && ZoneTable.getInstance().isInsideZone(ZoneType.Boss, getLocx(), getLocy()) == null)
				if ((mob instanceof L2MonsterInstance && !(mob instanceof L2Boss) || mob instanceof L2Boss && Config.CHAMPION_BOSS) && Config.CHAMPION_FREQUENCY > 0 && !mob.getTemplate().isQuestMonster() && mob.getLevel() >= Config.CHAMPION_MIN_LEVEL && mob.getLevel() <= Config.CHAMPION_MAX_LEVEL)
				{
					int mobId = mob.getTemplate().getIdTemplate();
					if (!(mobId >= 22452 && mobId <= 22484 || mobId >= 18579 && mobId <= 18602 || mobId >= 18009 && mobId <= 18058 || mobId >= 18109 && mobId <= 18118) || (mobId >= 29002 && mobId <= 29005 || mobId == 29016 || mobId == 29018) && !Config.CHAMPION_MINIONS)
						if (Rnd.get(100000) <= Config.CHAMPION_FREQUENCY)
						{
							mob.setChampion(true);
						}
				}
				else
				{
					mob.setChampion(false);
				}
		if (_isChamp)
		{
			mob.setChampion(true);
		}
		mob.setIsDead(false);
		mob.setDecayed(false);
		mob.getStatus().setCurrentHpMp(mob.getMaxHp(), mob.getMaxMp());

		if (getHeading() == -1)
		{
			mob.setHeading(Rnd.nextInt(61794));
		}
		else
		{
			mob.setHeading(getHeading());
		}

		mob.setSpawn(this);
		mob.spawnMe(newlocx, newlocy, newlocz, firstspawn);
		L2Spawn.notifyNpcSpawned(mob);
		if (_announceSpawn)
		{
			Town town = TownManager.getInstance().getClosestTown(newlocx, newlocy, newlocz);
			Announcements.getInstance().announceToPlayers(mob.getName() + " spawned " + (town != null ? " near " + town.getName() : ""));
		}
		_lastSpawn = mob;
		_currentCount++;
		return mob;
	}

	public boolean isCustom()
	{
		return _customSpawn;
	}

	public boolean isJail()
	{
		return _jailSpawn;
	}

	public boolean isRespawnable()
	{
		return _doRespawn;
	}

	public boolean isRndRangeRespawn()
	{
		return _useRandomPosRespwn;
	}

	public void resetRespawn()
	{
		stopRespawn();
		_currentCount = 0;
	}

	public void respawnNpc(L2Npc oldNpc)
	{
		intializeNpcInstance(oldNpc, false);
	}

	public void setAmount(int amount)
	{
		if (amount < 1)
		{
			amount = 1;
		}

		_maximumCount = amount;
	}

	public void setChampion(boolean val)
	{
		_isChamp = val;
	}

	public void setCustom()
	{
		_customSpawn = true;
	}

	public void setDbId(int id)
	{
		_dbid = id;
	}

	public void setHeading(int heading)
	{
		_heading = heading;
	}

	public void setId(int id)
	{
		_id = id;
	}

	public void setLocation(int location)
	{
		_location = location;
	}

	public void setLocx(int locx)
	{
		setOriginalX(locx);
		_locX = locx;
	}

	public void setLocy(int locy)
	{
		setOriginalY(locy);
		_locY = locy;
	}

	public void setLocz(int locz)
	{
		_locZ = locz;
	}

	public void setOriginalX(int par)
	{
		if (!_originalLocxSet)
		{
			_locXO = par;
			_originalLocxSet = true;
		}
	}

	public void setOriginalY(int par)
	{
		if (!_originalLocySet)
		{
			_locYO = par;
			_originalLocySet = true;
		}
	}

	public void setRespawnDelay(int i)
	{
		if (i < 10)
		{
			i = 10;
		}

		_respawnDelay = i * 1000;
	}

	public void setRespawnMaxDelay(int date)
	{
		_respawnMaxDelay = date;
	}

	public void setRespawnMinDelay(int date)
	{
		_respawnMinDelay = date;
	}

	public void setRndRespawnRange(int par)
	{
		_randomeRespawnRange = par;
	}

	public void setSpawnAnnounce(boolean val)
	{
		_announceSpawn = val;
	}

	public void setSpawnZoneSize(int par)
	{
		_spawnZoneSize = par;
	}

	public L2Npc spawnOne(boolean val)
	{
		return doSpawn(val);
	}

	public void startRespawn()
	{
		_doRespawn = true;
	}

	public void stopRespawn()
	{
		for (Future<?> f : _tasks)
		{
			f.cancel(true);
		}
		_doRespawn = false;
	}
}