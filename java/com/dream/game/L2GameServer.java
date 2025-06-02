package com.dream.game;

import com.dream.Config;
import com.dream.L2DatabaseFactory;
import com.dream.game.access.gmCache;
import com.dream.game.access.gmController;
import com.dream.game.cache.CrestCache;
import com.dream.game.cache.HtmCache;
import com.dream.game.communitybbs.Manager.ForumsBBSManager;
import com.dream.game.datatables.GmListTable;
import com.dream.game.datatables.HeroSkillTable;
import com.dream.game.datatables.NobleSkillTable;
import com.dream.game.datatables.sql.CharNameTable;
import com.dream.game.datatables.sql.ClanTable;
import com.dream.game.datatables.sql.CustomArmorSetsTable;
import com.dream.game.datatables.sql.HennaTreeTable;
import com.dream.game.datatables.sql.ItemTable;
import com.dream.game.datatables.sql.NpcTable;
import com.dream.game.datatables.sql.PetSkillsTable;
import com.dream.game.datatables.sql.ServerData;
import com.dream.game.datatables.sql.SpawnTable;
import com.dream.game.datatables.xml.ArmorSetsTable;
import com.dream.game.datatables.xml.AugmentationData;
import com.dream.game.datatables.xml.BuyListTable;
import com.dream.game.datatables.xml.CharTemplateTable;
import com.dream.game.datatables.xml.ClanLeveLUpPricesData;
import com.dream.game.datatables.xml.DoorTable;
import com.dream.game.datatables.xml.ExtractableItemsData;
import com.dream.game.datatables.xml.FishTable;
import com.dream.game.datatables.xml.HelperBuffTable;
import com.dream.game.datatables.xml.HennaTable;
import com.dream.game.datatables.xml.LevelUpData;
import com.dream.game.datatables.xml.MapRegionTable;
import com.dream.game.datatables.xml.MerchantPriceConfigTable;
import com.dream.game.datatables.xml.PartyFarmData;
import com.dream.game.datatables.xml.PetDataTable;
import com.dream.game.datatables.xml.SkillSpellbookTable;
import com.dream.game.datatables.xml.SkillTable;
import com.dream.game.datatables.xml.SkillTreeTable;
import com.dream.game.datatables.xml.StaticObjects;
import com.dream.game.datatables.xml.SummonItemsData;
import com.dream.game.datatables.xml.TeleportLocationTable;
import com.dream.game.datatables.xml.ZoneTable;
import com.dream.game.geodata.GeoData;
import com.dream.game.geodata.pathfinding.PathFinding;
import com.dream.game.handler.ChatHandler;
import com.dream.game.handler.ItemHandler;
import com.dream.game.handler.SkillHandler;
import com.dream.game.handler.TutorialHandler;
import com.dream.game.handler.UserCommandHandler;
import com.dream.game.handler.VoicedCommandHandler;
import com.dream.game.idfactory.IdFactory;
import com.dream.game.manager.AuctionManager;
import com.dream.game.manager.AutoSpawnManager;
import com.dream.game.manager.BlockListManager;
import com.dream.game.manager.BoatManager;
import com.dream.game.manager.BuffShopManager;
import com.dream.game.manager.CastleManager;
import com.dream.game.manager.CastleManorManager;
import com.dream.game.manager.ClanHallManager;
import com.dream.game.manager.CoupleManager;
import com.dream.game.manager.CrownManager;
import com.dream.game.manager.CursedWeaponsManager;
import com.dream.game.manager.DayNightSpawnManager;
import com.dream.game.manager.DimensionalRiftManager;
import com.dream.game.manager.EventsDropManager;
import com.dream.game.manager.FishermanManager;
import com.dream.game.manager.FortManager;
import com.dream.game.manager.FortSiegeManager;
import com.dream.game.manager.FourSepulchersManager;
import com.dream.game.manager.ItemsAutoDestroy;
import com.dream.game.manager.ItemsOnGroundManager;
import com.dream.game.manager.MercTicketManager;
import com.dream.game.manager.OfflineManager;
import com.dream.game.manager.PartyRoomManager;
import com.dream.game.manager.PetitionManager;
import com.dream.game.manager.QuestManager;
import com.dream.game.manager.RaidBossInfoManager;
import com.dream.game.manager.RaidBossSpawnManager;
import com.dream.game.manager.RaidPointsManager;
import com.dream.game.manager.SiegeManager;
import com.dream.game.manager.SiegeRewardManager;
import com.dream.game.manager.TaskManager;
import com.dream.game.manager.TownManager;
import com.dream.game.manager.clanhallsiege.BanditStrongholdSiege;
import com.dream.game.manager.clanhallsiege.DevastatedCastleSiege;
import com.dream.game.manager.clanhallsiege.FortResistSiegeManager;
import com.dream.game.manager.clanhallsiege.FortressOfDeadSiege;
import com.dream.game.manager.clanhallsiege.RainbowSpringSiege;
import com.dream.game.manager.clanhallsiege.WildBeastFarmSiege;
import com.dream.game.manager.games.fishingChampionship;
import com.dream.game.manager.grandbosses.AntharasManager;
import com.dream.game.manager.grandbosses.BaiumManager;
import com.dream.game.manager.grandbosses.CoreManager;
import com.dream.game.manager.grandbosses.FrintezzaManager;
import com.dream.game.manager.grandbosses.OrfenManager;
import com.dream.game.manager.grandbosses.QueenAntManager;
import com.dream.game.manager.grandbosses.SailrenManager;
import com.dream.game.manager.grandbosses.ValakasManager;
import com.dream.game.manager.grandbosses.VanHalterManager;
import com.dream.game.manager.grandbosses.ZakenManager;
import com.dream.game.manager.lastimperialtomb.LastImperialTombManager;
import com.dream.game.model.AutoChatHandler;
import com.dream.game.model.L2FarmPlayableAI;
import com.dream.game.model.L2Manor;
import com.dream.game.model.L2PcOffline;
import com.dream.game.model.L2SiegeStatus;
import com.dream.game.model.entity.Hero;
import com.dream.game.model.entity.events.BigSquash;
import com.dream.game.model.entity.events.Cristmas;
import com.dream.game.model.entity.events.EventDroplist;
import com.dream.game.model.entity.events.EventMedals;
import com.dream.game.model.entity.events.GameEventManager;
import com.dream.game.model.entity.events.L2DropDay;
import com.dream.game.model.entity.events.L2day;
import com.dream.game.model.entity.events.StarlightFestival;
import com.dream.game.model.entity.events.archievements.AchievementsManager;
import com.dream.game.model.entity.events.arenaduel.ArenaDuel;
import com.dream.game.model.entity.sevensigns.SevenSigns;
import com.dream.game.model.entity.sevensigns.SevenSignsFestival;
import com.dream.game.model.olympiad.Olympiad;
import com.dream.game.model.quest.QuestMessage;
import com.dream.game.model.quest.pack.CoreScriptsLoader;
import com.dream.game.model.restriction.ObjectRestrictions;
import com.dream.game.model.world.L2World;
import com.dream.game.network.AuthServerThread;
import com.dream.game.network.DeadlockDetector;
import com.dream.game.network.L2GameClient;
import com.dream.game.network.L2GamePacketHandler;
import com.dream.game.network.ThreadPoolManager;
import com.dream.game.scripting.L2ScriptEngineManager;
import com.dream.game.taskmanager.AttackStanceTaskManager;
import com.dream.game.taskmanager.DecayTaskManager;
import com.dream.game.taskmanager.KnownListUpdateTaskManager;
import com.dream.game.taskmanager.LeakTaskManager;
import com.dream.game.taskmanager.SQLQueue;
import com.dream.game.taskmanager.tasks.TaskPcCaffe;
import com.dream.game.util.PcAction;
import com.dream.mmocore.SelectorConfig;
import com.dream.mmocore.SelectorThread;
import com.dream.tools.security.IPv4Filter;
import com.dream.util.Console;
import com.dream.util.concurrent.RunnableStatsManager;

import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;
import java.util.TimeZone;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

public class L2GameServer
{
	public interface StartupHook
	{
		public void onStartup();
	}
	
	private static final Logger _log = Logger.getLogger(L2GameServer.class);
	
	public static final Calendar _serverStarted = Calendar.getInstance();
	
	public static long _upTime = 0;
	
	public static double _intialTime = 0;
	
	private static SelectorThread<L2GameClient> _selectorThread;
	
	private static AuthServerThread _loginThread;
	
	private static L2GamePacketHandler _gamePacketHandler;
	
	private static Set<StartupHook> _startupHooks = new HashSet<>();
	
	public synchronized static void addStartupHook(StartupHook hook)
	{
		if (_startupHooks != null)
		{
			_startupHooks.add(hook);
		}
		else
		{
			hook.onStartup();
		}
	}
	
	private static void createBootDirs()
	{
		File logFolder = new File(Config.DATAPACK_ROOT, "log");
		logFolder.mkdir();
		
		new File(Config.DATAPACK_ROOT, "data/crests").mkdirs();
	}
	
	public static long getFreeMemory()
	{
		return (Runtime.getRuntime().maxMemory() - Runtime.getRuntime().totalMemory() + Runtime.getRuntime().freeMemory()) / 1048576;
	}
	
	public static SelectorThread<L2GameClient> getSelectorThread()
	{
		return _selectorThread;
	}
	
	public static Calendar getStartedTime()
	{
		return _serverStarted;
	}
	
	public static long getTotalMemory()
	{
		return Runtime.getRuntime().maxMemory() / 1048576;
	}
	
	public static long getUsedMemory()
	{
		return (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576;
	}
	
	private static void initLogging()
	{
		DOMConfigurator.configure("./config/admin/log4j.xml");
	}
	
	public static void main(String[] args) throws Throwable
	{
		prepare();
		
	
		
		if (Config.SERVER_VERSION != null)
		{
			_log.info("	Revision: " + Config.SERVER_VERSION + " : " + Config.SERVER_BUILD_DATE);
		}
		
		Console.printSection("Word's Filter");
		Config.loadFilter();
		
		Console.printSection("Database Engine");
		L2DatabaseFactory.getInstance();
		PcAction.clearRestartTask();
		
		Console.printSection("Script Engine");
		L2ScriptEngineManager.getInstance();
		
		Console.printSection("ThreadPool Manager");
		ThreadPool();
		
		Console.printSection("Lineage 2 World");
		ServerData.getInstance();
		L2World.getInstance();
		
		Console.printSection("DeadLock Detector");
		if (Config.DEADLOCKCHECK_INTERVAL > 0)
		{
			DeadlockDetector.getInstance();
		}
		else
		{
			_log.info("DeadlockDetector: Manager is disabled");
		}
		
		Console.printSection("Map Region");
		MapRegionTable.getInstance();
		
		Console.printSection("Announcement");
		Announcements.getInstance();
		
		Console.printSection("ID Factory");
		if (!IdFactory.getInstance().isInitialized())
		{
			_log.fatal("IdFactory: Could not read object IDs from DB");
		}
		_log.info("IdFactory: Free ObjectID's remaining: " + IdFactory.getInstance().size());
		RunnableStatsManager.getInstance();
		
		Console.printSection("Geodata & PathFinding");
		GeoData.getInstance();
		PathFinding.getInstance();
		
		Console.printSection("Server Manager");
		GameTimeController.getInstance();
		BoatManager.getInstance();
		StaticObjects.getInstance();
		
		Console.printSection("Task Manager");
		AttackStanceTaskManager.getInstance();
		DecayTaskManager.getInstance();
		KnownListUpdateTaskManager.getInstance();
		LeakTaskManager.getInstance();
		SQLQueue.getInstance();
		
		Console.printSection("Teleport's");
		TeleportLocationTable.getInstance();
		
		Console.printSection("Skills");
		SkillTreeTable.getInstance();
		SkillTable.getInstance();
		PetSkillsTable.getInstance();
		NobleSkillTable.getInstance();
		HeroSkillTable.getInstance();
		
		Console.printSection("Items");
		ItemTable.getInstance();
		ArmorSetsTable.getInstance();
		
		if (Config.BUFFSHOP_ENABLE)
		{
			BuffShopManager.getInstance();
		}
		
		if (Config.ALLOW_CUSTOM_ARMORSET_TABLE)
		{
			CustomArmorSetsTable.getInstance();
		}
		AugmentationData.getInstance();
		if (Config.SP_BOOK_NEEDED)
		{
			SkillSpellbookTable.getInstance();
		}
		SummonItemsData.getInstance();
		ExtractableItemsData.getInstance();
		if (Config.ALLOW_FISHING)
		{
			FishTable.getInstance();
		}
		ItemsOnGroundManager.getInstance();
		if (Config.AUTODESTROY_ITEM_AFTER > 0 || Config.HERB_AUTO_DESTROY_TIME > 0)
		{
			ItemsAutoDestroy.getInstance();
		}
		
		HtmCache.getInstance();
		
		Console.printSection("Characters");
		CharNameTable.getInstance();
		CharTemplateTable.getInstance();
		LevelUpData.getInstance();
		HelperBuffTable.getInstance();
		HennaTable.getInstance();
		HennaTreeTable.getInstance();
		if (Config.ALLOW_WEDDING)
		{
			CoupleManager.getInstance();
		}
		ClanTable.getInstance();
		ClanLeveLUpPricesData.getInstance();
		CrestCache.getInstance();
		Hero.getInstance();
		BlockListManager.getInstance();
		
		Console.printSection("NPC Stats");
		NpcTable.getInstance();
		PetDataTable.getInstance().loadPetsData();
		RaidBossInfoManager.getInstance();
		
		Console.printSection("Auto Handlers");
		AutoChatHandler.getInstance();
		AutoSpawnManager.getInstance();
		
		Console.printSection("Seven Signs");
		SevenSigns.getInstance();
		SevenSignsFestival.getInstance();
		
		Console.printSection("Entities and Zones");
		CrownManager.getInstance();
		TownManager.getInstance();
		MercTicketManager.getInstance();
		MerchantPriceConfigTable.getInstance();
		ClanHallManager.getInstance();
		DoorTable.getInstance();
		CastleManager.getInstance();
		SiegeManager.getInstance().load();
		
		if (Config.ACTIVATED_SYSTEM)
		{
			SiegeRewardManager.getInstance();
		}
		
		FortManager.getInstance();
		FortSiegeManager.getInstance().load();
		ZoneTable.getInstance();
		DoorTable.getInstance().registerToClanHalls();
		DoorTable.getInstance().setCommanderDoors();
		
		Console.printSection("Clan Hall Siege");
		FortResistSiegeManager.load();
		BanditStrongholdSiege.load();
		DevastatedCastleSiege.load();
		FortressOfDeadSiege.load();
		WildBeastFarmSiege.load();
		RainbowSpringSiege.load();
		
		Console.printSection("Script Data");
		QuestManager.getInstance();
		CoreScriptsLoader.Register();
		try
		{
			L2ScriptEngineManager.getInstance().loadScripts();
			
		}
		catch (IOException ioe)
		{
			_log.fatal("Failed loading scripts, no script going to be loaded");
		}
		QuestManager.getInstance().report();
		EventsDropManager.getInstance();
		EventDroplist.getInstance();
		
		if (Config.FISHERMAN_ENABLED)
		{
			FishermanManager.getInstance().engineInit();
		}
		if (Config.SHOW_NOT_REG_QUEST)
		{
			QuestMessage.showNotRegQuest();
		}
		
		Console.printSection("Spawn Data");
		SpawnTable.getInstance();
		if (Config.SPAWN_WEDDING_NPC)
		{
			PcAction.spawnManager();
		}
		DayNightSpawnManager.getInstance().notifyChangeMode();
		RaidBossSpawnManager.getInstance();
		RaidPointsManager.init();
		
		Console.printSection("Economy");
		CursedWeaponsManager.getInstance();
		BuyListTable.getInstance();
		CastleManorManager.getInstance();
		L2Manor.getInstance();
		AuctionManager.getInstance();
		PartyRoomManager.getInstance();
		
		Console.printSection("Olympiad");
		Olympiad.getInstance();
		
		Console.printSection("Arena Duel");
		if (Config.ARENA_DUEL_1X1_ENABLED)
		{
			ThreadPoolManager.getInstance().scheduleGeneral(ArenaDuel.getInstance(), 5000);
			_log.info("ArenaDuel: 1x1 Enabled");
		}
		
		Console.printSection("DimensionalRift");
		DimensionalRiftManager.getInstance();
		
		Console.printSection("FourSepulchers");
		FourSepulchersManager.getInstance().init();
		
		Console.printSection("Bosses");
		QueenAntManager.getInstance().init();
		ZakenManager.getInstance().init();
		CoreManager.getInstance().init();
		OrfenManager.getInstance().init();
		SailrenManager.getInstance().init();
		VanHalterManager.getInstance().init();
		
		Console.printSection("Grand Bosses");
		AntharasManager.getInstance().init();
		BaiumManager.getInstance().init();
		ValakasManager.getInstance().init();
		LastImperialTombManager.getInstance().init();
		FrintezzaManager.getInstance().init();
		
		Console.printSection("Handlers");
		ItemHandler.getInstance();
		SkillHandler.getInstance();
		UserCommandHandler.getInstance();
		VoicedCommandHandler.getInstance();
		ChatHandler.getInstance();
		TutorialHandler.getInstance();
		
		Console.printSection("Misc");
		ObjectRestrictions.getInstance();
		L2SiegeStatus.getInstance();
		TaskManager.getInstance();
		GmListTable.getInstance();
		PetitionManager.getInstance();
		AchievementsManager.getInstance();
		
		if (Config.COMMUNITY_TYPE.equals("full"))
		{
			ForumsBBSManager.getInstance().initRoot();
		}
		
		fishingChampionship.getInstance();
		
		Console.printSection("Offline Trade");
		if (Config.ALLOW_OFFLINE_TRADE)
		{
			OfflineManager.getInstance();
		}
		if (Config.RESTORE_OFFLINE_TRADERS)
		{
			L2PcOffline.loadOffliners();
		}
		else
		{
			L2PcOffline.clearOffliner();
		}
		
		Runtime.getRuntime().addShutdownHook(Shutdown.getInstance());
		System.gc();
		
		Console.printSection("ServerThreads");
		_loginThread = AuthServerThread.getInstance();
		_loginThread.start();
		
		final SelectorConfig sc = new SelectorConfig();
		sc.MAX_READ_PER_PASS = Config.MMO_MAX_READ_PER_PASS;
		sc.MAX_SEND_PER_PASS = Config.MMO_MAX_SEND_PER_PASS;
		sc.SLEEP_TIME = Config.MMO_SELECTOR_SLEEP_TIME;
		sc.HELPER_BUFFER_COUNT = Config.MMO_HELPER_BUFFER_COUNT;
		sc.TCP_NODELAY = Config.MMO_TCP_NODELAY;
		
		_gamePacketHandler = new L2GamePacketHandler();
		
		_selectorThread = new SelectorThread<>(sc, _gamePacketHandler, _gamePacketHandler, _gamePacketHandler, new IPv4Filter());
		
		InetAddress bindAddress = null;
		if (!Config.GAMESERVER_HOSTNAME.equals("*"))
		{
			try
			{
				bindAddress = InetAddress.getByName(Config.GAMESERVER_HOSTNAME);
			}
			catch (UnknownHostException e1)
			{
				_log.warn("WARNING: The GameServer bind address is invalid, using all available IPs. Reason: " + e1.getMessage(), e1);
			}
		}
		
		try
		{
			_selectorThread.openServerSocket(bindAddress, Config.PORT_GAME);
		}
		catch (IOException e)
		{
			_log.fatal("FATAL: Failed to open server socket. Reason: " + e.getMessage(), e);
			System.exit(1);
		}
		_selectorThread.start();
		
		Console.printSection("Pc Caffe");
		if (Config.PC_CAFFE_ENABLED)
		{
			new TaskPcCaffe().schedule(Config.PC_CAFFE_INTERVAL * 60000);
		}
		else
		{
			_log.info("Pc Caffe: Manager is disabled");
		}
		
		Console.printSection("Event Engine");
		GameEventManager.getInstance();
		
		Console.printSection("Event Party Farm");
		PartyFarmData.getInstance();
		
		Console.printSection("Auto Farm Task");
		L2FarmPlayableAI.getInstance().start();
		
		Console.printSection("Oficial Event's");
		Cristmas.startEvent();
		EventMedals.startEvent();
		StarlightFestival.startEvent();
		L2day.startEvent();
		L2DropDay.startEvent();
		BigSquash.startEvent();
		
		Console.printSection("Administration");
		gmController.getInstance();
		gmCache.getInstance();
		
		Console.printSection("Tasks Manager");
		TaskManager.getInstance().startAllTasks();
		onStartup();
		System.gc();
		printInfo();
	}
	
	private synchronized static void onStartup()
	{
		final Set<StartupHook> startupHooks = _startupHooks;
		
		_startupHooks = null;
		
		for (StartupHook hook : startupHooks)
		{
			hook.onStartup();
		}
	}
	
	private static void prepare()
	{
		if (!GraphicsEnvironment.isHeadless())
		{
			new L2InterfaceGS();
		}
		
		System.setProperty("line.separator", "\r\n");
		System.setProperty("file.encoding", "UTF-8");
		System.setProperty("python.home", ".");
		
		_intialTime = System.currentTimeMillis();
		
		initLogging();
		createBootDirs();
		Config.loadAll();
		TimeZone.setDefault(TimeZone.getTimeZone(Config.TIME_ZONE));
	}
	
	private static void printInfo()
	{
		Console.printSection("Server Info");
		double finalTime = System.currentTimeMillis();
		if (Config.SERVER_VERSION != null)
		{
			_log.info("Build version: " + Config.SERVER_VERSION + " : " + Config.SERVER_BUILD_DATE);
		}
		_log.info("Memory: Free " + getFreeMemory() + " MB of " + getTotalMemory() + " MB. Used " + getUsedMemory() + " MB.");
		_log.info("Ready on IP: " + Config.EXTERNAL_HOSTNAME + ":" + Config.PORT_GAME + ".");
		_log.info("Maximum Online: " + Config.MAXIMUM_ONLINE_USERS);
		_log.info("Load time: " + (int) ((finalTime - _intialTime) / 1000) + " Seconds.");
		Console.printSection("");
		_upTime = System.currentTimeMillis();
	}
	
	private static void ThreadPool()
	{
		_log.info("ThreadPoolManager: Initializing.");
		ThreadPoolManager.getInstance();
		_log.info("General Thread :" + Config.GENERAL_THREAD_POOL_SIZE + ".");
		_log.info("Effect Thread : " + Config.EFFECT_THREAD_POOL_SIZE + ".");
		_log.info("AI Thread :     " + Config.AI_THREAD_POOL_SIZE + ".");
		_log.info("Packet Thread : " + Config.PACKET_THREAD_POOL_SIZE + ".");
		_log.info("Total Thread :  " + Config.THREAD_POOL_SIZE + ".");
	}
}