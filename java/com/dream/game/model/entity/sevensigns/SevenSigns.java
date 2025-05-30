package com.dream.game.model.entity.sevensigns;

import com.dream.Config;
import com.dream.L2DatabaseFactory;
import com.dream.Message;
import com.dream.game.datatables.sql.NpcTable;
import com.dream.game.datatables.xml.MapRegionTable.TeleportWhereType;
import com.dream.game.datatables.xml.SkillTable;
import com.dream.game.manager.AutoSpawnManager;
import com.dream.game.manager.AutoSpawnManager.AutoSpawnInstance;
import com.dream.game.manager.CastleManager;
import com.dream.game.model.AutoChatHandler;
import com.dream.game.model.L2Spawn;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.world.L2World;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.ThreadPoolManager;
import com.dream.game.network.serverpackets.SSQInfo;
import com.dream.game.network.serverpackets.SystemMessage;
import com.dream.game.templates.chars.L2NpcTemplate;
import com.dream.util.StatsSet;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

import org.apache.log4j.Logger;

public class SevenSigns
{
	protected class SevenSignsPeriodChange implements Runnable
	{
		@Override
		public void run()
		{
			final int periodEnded = getCurrentPeriod();
			_activePeriod++;

			switch (periodEnded)
			{
				case PERIOD_COMP_RECRUITING:

					SevenSignsFestival.getInstance().startFestivalManager();
					sendMessageToAll(SystemMessageId.COMPETITION_PERIOD_BEGUN);
					break;
				case PERIOD_COMPETITION:

					sendMessageToAll(SystemMessageId.RESULTS_PERIOD_BEGUN);

					SevenSignsFestival.getInstance().stopFestivalManager();

					calcNewSealOwners();

					break;
				case PERIOD_COMP_RESULTS:
					int compWinner = getCabalHighestScore();
					switch (compWinner)
					{
						case CABAL_DAWN:
							sendMessageToAll(SystemMessageId.DAWN_WON);
							break;
						case CABAL_DUSK:
							sendMessageToAll(SystemMessageId.DUSK_WON);
							break;
					}
					_previousWinner = compWinner;

					initializeSeals();

					giveCPMult(getSealOwner(SEAL_STRIFE));

					sendMessageToAll(SystemMessageId.VALIDATION_PERIOD_BEGUN);

					_log.info("SevenSigns: The " + getCabalName(_previousWinner) + " have won the competition with " + getCurrentScore(_previousWinner) + " points!");
					break;
				case PERIOD_SEAL_VALIDATION:

					SevenSignsFestival.getInstance().rewardHighestRanked();
					_activePeriod = PERIOD_COMP_RECRUITING;

					sendMessageToAll(SystemMessageId.PREPARATIONS_PERIOD_BEGUN);

					removeCPMult();

					resetPlayerData();
					resetSeals();

					SevenSignsFestival.getInstance().resetFestivalData(false);

					_dawnStoneScore = 0;
					_duskStoneScore = 0;

					_dawnFestivalScore = 0;
					_duskFestivalScore = 0;
					for (int x = 0; x < 5; x++)
					{
						_dawnFestivalIdScore[x] = 0;
						_duskFestivalIdScore[x] = 0;
					}

					_currentCycle++;
					break;
			}

			saveSevenSignsData(null, true);

			teleLosingCabalFromDungeons(getCabalShortName(getCabalHighestScore()));

			SSQInfo ss = new SSQInfo();

			for (L2PcInstance player : L2World.getInstance().getAllPlayers())
			{
				player.sendPacket(ss);
			}

			spawnSevenSignsNPC();

			_log.info("SevenSigns: The " + getCurrentPeriodName() + " period has begun!");

			setCalendarForNextPeriodChange();

			SevenSignsPeriodChange sspc = new SevenSignsPeriodChange();
			_periodTimer = ThreadPoolManager.getInstance().scheduleGeneral(sspc, getMilliToPeriodChange());
		}
	}

	protected static Logger _log = Logger.getLogger(SevenSigns.class.getName());

	private static SevenSigns _instance;

	public static final String SEVEN_SIGNS_DATA_FILE = "./config/main/events/fun_events.properties";

	public static final String SEVEN_SIGNS_HTML_PATH = "data/html/seven_signs/";

	public static final int CABAL_NULL = 0;

	public static final int CABAL_DUSK = 1;

	public static final int CABAL_DAWN = 2;

	public static final int SEAL_NULL = 0;

	public static final int SEAL_AVARICE = 1;
	public static final int SEAL_GNOSIS = 2;
	public static final int SEAL_STRIFE = 3;
	public static final int PERIOD_COMP_RECRUITING = 0;
	public static final int PERIOD_COMPETITION = 1;
	public static final int PERIOD_COMP_RESULTS = 2;
	public static final int PERIOD_SEAL_VALIDATION = 3;
	public static final int PERIOD_START_HOUR = 18;
	public static final int PERIOD_START_MINS = 00;

	public static final int PERIOD_START_DAY = Calendar.MONDAY;
	private static int[] ANGELS_ID =
	{
		21187,
		21188,
		21189,
		21190,
		21191,
		21192,
		21193,
		21194,
		21195,
		21196,
		21197,
		21198,
		21199,
		21200,
		21201,
		21202,
		21203,
		21204,
		21205,
		21206,
		21207
	};
	private static int[] DEMONS_ID =
	{
		21166,
		21167,
		21168,
		21169,
		21170,
		21171,
		21172,
		21173,
		21174,
		21175,
		21176,
		21177,
		21178,
		21179,
		21180,
		21181,
		21182,
		21183,
		21184,
		21185,
		21186
	};
	public static List<L2Spawn> ANGELS = new ArrayList<>();

	public static List<L2Spawn> DEMONS = new ArrayList<>();
	public static final int PERIOD_MINOR_LENGTH = 900000;

	public static final int PERIOD_MAJOR_LENGTH = 604800000 - PERIOD_MINOR_LENGTH;
	public static final int ANCIENT_ADENA_ID = 5575;
	public static final int RECORD_SEVEN_SIGNS_ID = 5707;
	public static final int CERTIFICATE_OF_APPROVAL_ID = 6388;

	public static final int RECORD_SEVEN_SIGNS_COST = 500;
	public static final int ORATOR_NPC_ID = 31094;
	public static final int PREACHER_NPC_ID = 31093;
	public static final int MAMMON_MERCHANT_ID = 31113;
	public static final int MAMMON_BLACKSMITH_ID = 31126;
	public static final int MAMMON_MARKETEER_ID = 31092;
	public static final int SPIRIT_IN_ID = 31111;
	public static final int SPIRIT_OUT_ID = 31112;
	public static final int LILITH_NPC_ID = 25283;
	public static final int ANAKIM_NPC_ID = 25286;
	public static final int CREST_OF_DAWN_ID = 31170;
	public static final int CREST_OF_DUSK_ID = 31171;
	public static final int SEAL_STONE_BLUE_ID = 6360;
	public static final int SEAL_STONE_GREEN_ID = 6361;

	public static final int SEAL_STONE_RED_ID = 6362;
	public static final int SEAL_STONE_BLUE_VALUE = 3;
	public static final int SEAL_STONE_GREEN_VALUE = 5;

	public static final int SEAL_STONE_RED_VALUE = 10;
	public static final int BLUE_CONTRIB_POINTS = 3;
	public static final int GREEN_CONTRIB_POINTS = 5;

	public static final int RED_CONTRIB_POINTS = 10;
	private static AutoSpawnInstance _merchantSpawn;

	private static AutoSpawnInstance _blacksmithSpawn;
	private static AutoSpawnInstance _spiritInSpawn;
	private static AutoSpawnInstance _spiritOutSpawn;
	private static AutoSpawnInstance _lilithSpawn;

	private static AutoSpawnInstance _anakimSpawn;
	private static Map<Integer, AutoSpawnInstance> _crestofdawnspawn;
	private static Map<Integer, AutoSpawnInstance> _crestofduskspawn;
	private static Map<Integer, AutoSpawnInstance> _oratorSpawns;

	private static Map<Integer, AutoSpawnInstance> _preacherSpawns;
	private static Map<Integer, AutoSpawnInstance> _marketeerSpawns;

	public static ScheduledFuture<?> _periodTimer;

	public static int calcAncientAdenaReward(int blueCount, int greenCount, int redCount)
	{
		int reward = blueCount * SEAL_STONE_BLUE_VALUE;
		reward += greenCount * SEAL_STONE_GREEN_VALUE;
		reward += redCount * SEAL_STONE_RED_VALUE;

		return reward;
	}

	public static int calcContributionScore(int blueCount, int greenCount, int redCount)
	{
		int contrib = blueCount * BLUE_CONTRIB_POINTS;
		contrib += greenCount * GREEN_CONTRIB_POINTS;
		contrib += redCount * RED_CONTRIB_POINTS;

		return contrib;
	}

	public static final String getCabalName(int cabal)
	{
		switch (cabal)
		{
			case CABAL_DAWN:
				return "Lords of Dawn";
			case CABAL_DUSK:
				return "Revolutionaries of Dusk";
		}

		return "No Cabal";
	}

	public static final String getCabalNameText(int cabal)
	{
		switch (cabal)
		{
			case CABAL_DAWN:
				return "The Lords Of Dawn";
			case CABAL_DUSK:
				return "The Revolutionaries Of The Sunset";
		}

		return "No Cabal";
	}

	public static final String getCabalShortName(int cabal)
	{
		switch (cabal)
		{
			case CABAL_DAWN:
				return "dawn";
			case CABAL_DUSK:
				return "dusk";
		}

		return "No Cabal";
	}

	public static SevenSigns getInstance()
	{
		if (_instance == null)
		{
			_instance = new SevenSigns();
		}

		return _instance;
	}

	public static final String getSealName(int seal, boolean shortName)
	{
		String sealName = !shortName ? "Seal of " : "";

		switch (seal)
		{
			case SEAL_AVARICE:
				sealName += "Avarice";
				break;
			case SEAL_GNOSIS:
				sealName += "Gnosis";
				break;
			case SEAL_STRIFE:
				sealName += "Strife";
				break;
		}

		return sealName;
	}

	public static final String getSealNameText(int seal, boolean shortName)
	{
		String sealName = !shortName ? "Print " : "";

		switch (seal)
		{
			case SEAL_AVARICE:
				sealName += "Avarice";
				break;
			case SEAL_GNOSIS:
				sealName += "Gnosis";
				break;
			case SEAL_STRIFE:
				sealName += "Strife";
				break;
		}

		return sealName;
	}

	private final Calendar _calendar = Calendar.getInstance();
	protected int _activePeriod;
	protected int _currentCycle;
	protected double _dawnStoneScore;
	protected double _duskStoneScore;

	protected int _dawnFestivalScore;

	protected int _duskFestivalScore;

	protected int[] _dawnFestivalIdScore =
	{
		0,
		0,
		0,
		0,
		0
	};

	protected int[] _duskFestivalIdScore =
	{
		0,
		0,
		0,
		0,
		0
	};

	protected int _compWinner;

	protected int _previousWinner;

	private final Map<Integer, StatsSet> _signsPlayerData;

	private final Map<Integer, Integer> _signsSealOwners;

	private final Map<Integer, Integer> _signsDuskSealTotals;

	private final Map<Integer, Integer> _signsDawnSealTotals;

	public SevenSigns()
	{
		_signsPlayerData = new HashMap<>();
		_signsSealOwners = new HashMap<>();
		_signsDuskSealTotals = new HashMap<>();
		_signsDawnSealTotals = new HashMap<>();

		try
		{
			restoreSevenSignsData();
		}
		catch (Exception e)
		{
			_log.fatal("SevenSigns: Failed to load configuration: " + e);
		}

		_log.info("SevenSigns: Currently in the " + getCurrentPeriodName() + " period!");
		initializeSeals();

		if (isSealValidationPeriod())
		{
			if (getCabalHighestScore() == CABAL_NULL)
			{
				_log.info("SevenSigns: The competition ended with a tie last week.");
			}
			else
			{
				_log.info("SevenSigns: The " + getCabalName(getCabalHighestScore()) + " were victorious last week.");
			}
		}
		else if (getCabalHighestScore() == CABAL_NULL)
		{
			_log.info("SevenSigns: If the current trend continues, will end in a tie this week.");
		}
		else
		{
			_log.info("SevenSigns: The " + getCabalName(getCabalHighestScore()) + " are in the lead this week.");
		}

		synchronized (this)
		{
			setCalendarForNextPeriodChange();
			long milliToChange = getMilliToPeriodChange();

			SevenSignsPeriodChange sspc = new SevenSignsPeriodChange();
			_periodTimer = ThreadPoolManager.getInstance().scheduleGeneral(sspc, milliToChange);

			double numSecs = milliToChange / 1000 % 60;
			double countDown = (milliToChange / 1000 - numSecs) / 60;
			int numMins = (int) Math.floor(countDown % 60);
			countDown = (countDown - numMins) / 60;
			int numHours = (int) Math.floor(countDown % 24);
			int numDays = (int) Math.floor((countDown - numHours) / 24);

			_log.info("SevenSigns: Next period begins in " + numDays + " days, " + numHours + " hours and " + numMins + " mins.");
		}
		spawnSevenSignsNPC();
	}

	public void addFestivalScore(int cabal, int amount, int festivalId)
	{
		if (cabal == CABAL_DUSK)
		{
			if (_duskFestivalIdScore[festivalId] == 0)
			{
				_duskFestivalIdScore[festivalId] = amount;
				_dawnFestivalIdScore[festivalId] = 0;
			}
		}
		else if (_dawnFestivalIdScore[festivalId] == 0)
		{
			_dawnFestivalIdScore[festivalId] = amount;
			_duskFestivalIdScore[festivalId] = 0;
		}
		_dawnFestivalScore = 0;
		_duskFestivalScore = 0;
		for (int x = 0; x < 5; x++)
		{
			_dawnFestivalScore += _dawnFestivalIdScore[x];
			_duskFestivalScore += _duskFestivalIdScore[x];
		}
	}

	public int addPlayerStoneContrib(L2PcInstance player, int blueCount, int greenCount, int redCount)
	{
		StatsSet currPlayer = getPlayerData(player);

		int contribScore = calcContributionScore(blueCount, greenCount, redCount);
		int totalAncientAdena = currPlayer.getInteger("ancient_adena_amount") + calcAncientAdenaReward(blueCount, greenCount, redCount);
		int totalContribScore = currPlayer.getInteger("contribution_score") + contribScore;

		if (totalContribScore > Config.ALT_MAXIMUM_PLAYER_CONTRIB)
			return -1;

		currPlayer.set("red_stones", currPlayer.getInteger("red_stones") + redCount);
		currPlayer.set("green_stones", currPlayer.getInteger("green_stones") + greenCount);
		currPlayer.set("blue_stones", currPlayer.getInteger("blue_stones") + blueCount);
		currPlayer.set("ancient_adena_amount", totalAncientAdena);
		currPlayer.set("contribution_score", totalContribScore);
		_signsPlayerData.put(player.getObjectId(), currPlayer);

		switch (getPlayerCabal(player))
		{
			case CABAL_DAWN:
				_dawnStoneScore += contribScore;
				break;
			case CABAL_DUSK:
				_duskStoneScore += contribScore;
				break;
		}

		saveSevenSignsData(player, true);
		return contribScore;
	}

	protected void calcNewSealOwners()
	{
		if (_log.isDebugEnabled() || Config.DEBUG)
		{
			_log.info("SevenSigns: (Avarice) Dawn = " + _signsDawnSealTotals.get(SEAL_AVARICE) + ", Dusk = " + _signsDuskSealTotals.get(SEAL_AVARICE));
			_log.info("SevenSigns: (Gnosis) Dawn = " + _signsDawnSealTotals.get(SEAL_GNOSIS) + ", Dusk = " + _signsDuskSealTotals.get(SEAL_GNOSIS));
			_log.info("SevenSigns: (Strife) Dawn = " + _signsDawnSealTotals.get(SEAL_STRIFE) + ", Dusk = " + _signsDuskSealTotals.get(SEAL_STRIFE));
		}

		for (Integer currSeal : _signsDawnSealTotals.keySet())
		{
			int prevSealOwner = _signsSealOwners.get(currSeal);
			int newSealOwner = CABAL_NULL;
			int dawnProportion = getSealProportion(currSeal, CABAL_DAWN);
			int totalDawnMembers = getTotalMembers(CABAL_DAWN) == 0 ? 1 : getTotalMembers(CABAL_DAWN);
			int dawnPercent = Math.round((float) dawnProportion / (float) totalDawnMembers * 100);
			int duskProportion = getSealProportion(currSeal, CABAL_DUSK);
			int totalDuskMembers = getTotalMembers(CABAL_DUSK) == 0 ? 1 : getTotalMembers(CABAL_DUSK);
			int duskPercent = Math.round((float) duskProportion / (float) totalDuskMembers * 100);

			switch (prevSealOwner)
			{
				case CABAL_NULL:
					switch (getCabalHighestScore())
					{
						case CABAL_NULL:
							newSealOwner = CABAL_NULL;
							break;
						case CABAL_DAWN:
							if (dawnPercent >= 35)
							{
								newSealOwner = CABAL_DAWN;
							}
							else
							{
								newSealOwner = CABAL_NULL;
							}
							break;
						case CABAL_DUSK:
							if (duskPercent >= 35)
							{
								newSealOwner = CABAL_DUSK;
							}
							else
							{
								newSealOwner = CABAL_NULL;
							}
							break;
					}
					break;
				case CABAL_DAWN:
					switch (getCabalHighestScore())
					{
						case CABAL_NULL:
							if (dawnPercent >= 10)
							{
								newSealOwner = CABAL_DAWN;
							}
							else
							{
								newSealOwner = CABAL_NULL;
							}
							break;
						case CABAL_DAWN:
							if (dawnPercent >= 10)
							{
								newSealOwner = CABAL_DAWN;
							}
							else
							{
								newSealOwner = CABAL_NULL;
							}
							break;
						case CABAL_DUSK:
							if (duskPercent >= 35)
							{
								newSealOwner = CABAL_DUSK;
							}
							else if (dawnPercent >= 10)
							{
								newSealOwner = CABAL_DAWN;
							}
							else
							{
								newSealOwner = CABAL_NULL;
							}
							break;
					}
					break;
				case CABAL_DUSK:
					switch (getCabalHighestScore())
					{
						case CABAL_NULL:
							if (duskPercent >= 10)
							{
								newSealOwner = CABAL_DUSK;
							}
							else
							{
								newSealOwner = CABAL_NULL;
							}
							break;
						case CABAL_DAWN:
							if (dawnPercent >= 35)
							{
								newSealOwner = CABAL_DAWN;
							}
							else if (duskPercent >= 10)
							{
								newSealOwner = CABAL_DUSK;
							}
							else
							{
								newSealOwner = CABAL_NULL;
							}
							break;
						case CABAL_DUSK:
							if (duskPercent >= 10)
							{
								newSealOwner = CABAL_DUSK;
							}
							else
							{
								newSealOwner = CABAL_NULL;
							}
							break;
					}
					break;
			}

			_signsSealOwners.put(currSeal, newSealOwner);

			if (currSeal == SEAL_STRIFE)
			{
				CastleManager.getInstance().validateTaxes(newSealOwner);
			}
		}
	}

	public void changePeriodManualy()
	{
		_periodTimer.cancel(true);
		SevenSignsPeriodChange sspc = new SevenSignsPeriodChange();
		_periodTimer = ThreadPoolManager.getInstance().scheduleGeneral(sspc, 0);
	}

	public boolean checkIsDawnPostingTicket(int itemId)
	{
		if (itemId > 6114 && itemId < 6175)
			return true;
		if (itemId > 6801 && itemId < 6812)
			return true;
		if (itemId > 7997 && itemId < 8008)
			return true;
		if (itemId > 7940 && itemId < 7951)
			return true;
		if (itemId > 6294 && itemId < 6307)
			return true;
		if (itemId > 6831 && itemId < 6834)
			return true;
		if (itemId > 8027 && itemId < 8030)
			return true;
		return itemId > 7970 && itemId < 7973;
	}

	public boolean checkIsRookiePostingTicket(int itemId)
	{
		if (itemId > 6174 && itemId < 6295)
			return true;
		if (itemId > 6811 && itemId < 6832)
			return true;
		if (itemId > 7950 && itemId < 7971)
			return true;
		return itemId > 8007 && itemId < 8028;
	}

	public boolean checkSummonConditions(L2PcInstance activeChar)
	{
		if (activeChar == null)
			return true;

		if (isSealValidationPeriod())
			if (getSealOwner(SEAL_STRIFE) == CABAL_DAWN && getPlayerCabal(activeChar) == CABAL_DUSK)
			{
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.SEAL_OF_STRIFE_FORBIDS_SUMMONING));
				return true;
			}
		return false;
	}

	public int getAncientAdenaReward(L2PcInstance player, boolean removeReward)
	{
		StatsSet currPlayer = getPlayerData(player);
		int rewardAmount = currPlayer.getInteger("ancient_adena_amount");

		currPlayer.set("red_stones", 0);
		currPlayer.set("green_stones", 0);
		currPlayer.set("blue_stones", 0);
		currPlayer.set("ancient_adena_amount", 0);

		if (removeReward)
		{
			_signsPlayerData.put(player.getObjectId(), currPlayer);
			saveSevenSignsData(player, true);
		}

		return rewardAmount;
	}

	public final int getCabalHighestScore()
	{
		if (getCurrentScore(CABAL_DUSK) == getCurrentScore(CABAL_DAWN))
			return CABAL_NULL;
		else if (getCurrentScore(CABAL_DUSK) > getCurrentScore(CABAL_DAWN))
			return CABAL_DUSK;
		else
			return CABAL_DAWN;
	}

	public final int getCurrentCycle()
	{
		return _currentCycle;
	}

	public final int getCurrentFestivalScore(int cabal)
	{
		switch (cabal)
		{
			case CABAL_NULL:
				return 0;
			case CABAL_DAWN:
				return _dawnFestivalScore;
			case CABAL_DUSK:
				return _duskFestivalScore;
		}

		return 0;
	}

	public final int getCurrentPeriod()
	{
		return _activePeriod;
	}

	public final String getCurrentPeriodName()
	{
		String periodName = null;

		switch (_activePeriod)
		{
			case PERIOD_COMP_RECRUITING:
				periodName = "Quest Event Initialization";
				break;
			case PERIOD_COMPETITION:
				periodName = "Competition (Quest Event)";
				break;
			case PERIOD_COMP_RESULTS:
				periodName = "Quest Event Results";
				break;
			case PERIOD_SEAL_VALIDATION:
				periodName = "Seal Validation";
				break;
		}

		return periodName;
	}

	public final String getCurrentPeriodNameText()
	{
		String periodName = null;

		switch (_activePeriod)
		{
			case PERIOD_COMP_RECRUITING:
				periodName = "Quest Event Initialization";
				break;
			case PERIOD_COMPETITION:
				periodName = "Competition (Quest Event)";
				break;
			case PERIOD_COMP_RESULTS:
				periodName = "Quest Event Results";
				break;
			case PERIOD_SEAL_VALIDATION:
				periodName = "Seal Validation";
				break;
		}

		return periodName;
	}

	public final int getCurrentScore(int cabal)
	{
		double totalStoneScore = _dawnStoneScore + _duskStoneScore;

		switch (cabal)
		{
			case CABAL_NULL:
				return 0;
			case CABAL_DAWN:
				return Math.round((float) (_dawnStoneScore / ((float) totalStoneScore == 0 ? 1 : totalStoneScore)) * 500) + _dawnFestivalScore;
			case CABAL_DUSK:
				return Math.round((float) (_duskStoneScore / ((float) totalStoneScore == 0 ? 1 : totalStoneScore)) * 500) + _duskFestivalScore;
		}

		return 0;
	}

	public final double getCurrentStoneScore(int cabal)
	{
		switch (cabal)
		{
			case CABAL_NULL:
				return 0;
			case CABAL_DAWN:
				return _dawnStoneScore;
			case CABAL_DUSK:
				return _duskStoneScore;
		}

		return 0;
	}

	public final int getDaysToPeriodChange()
	{
		int numDays = _calendar.get(Calendar.DAY_OF_WEEK) - PERIOD_START_DAY;

		if (numDays < 0)
			return 0 - numDays;

		return 7 - numDays;
	}

	public final long getMilliToPeriodChange()
	{
		long currTimeMillis = System.currentTimeMillis();
		long changeTimeMillis = _calendar.getTimeInMillis();

		return changeTimeMillis - currTimeMillis;
	}

	public int getPlayerAdenaCollect(L2PcInstance player)
	{
		if (!hasRegisteredBefore(player))
			return 0;

		return _signsPlayerData.get(player.getObjectId()).getInteger("ancient_adena_amount");
	}

	public int getPlayerCabal(L2PcInstance player)
	{
		if (!hasRegisteredBefore(player))
			return CABAL_NULL;

		String playerCabal = getPlayerData(player).getString("cabal");

		if (playerCabal.equalsIgnoreCase("dawn"))
			return CABAL_DAWN;
		else if (playerCabal.equalsIgnoreCase("dusk"))
			return CABAL_DUSK;
		else
			return CABAL_NULL;
	}

	public int getPlayerContribScore(L2PcInstance player)
	{
		if (!hasRegisteredBefore(player))
			return 0;

		StatsSet currPlayer = getPlayerData(player);

		return currPlayer.getInteger("contribution_score");
	}

	public final StatsSet getPlayerData(L2PcInstance player)
	{
		if (!hasRegisteredBefore(player))
			return null;

		return _signsPlayerData.get(player.getObjectId());
	}

	public int getPlayerSeal(L2PcInstance player)
	{
		if (!hasRegisteredBefore(player))
			return SEAL_NULL;

		return getPlayerData(player).getInteger("seal");
	}

	public int getPlayerStoneContrib(L2PcInstance player)
	{
		if (!hasRegisteredBefore(player))
			return 0;

		int stoneCount = 0;

		StatsSet currPlayer = getPlayerData(player);

		stoneCount += currPlayer.getInteger("red_stones");
		stoneCount += currPlayer.getInteger("green_stones");
		stoneCount += currPlayer.getInteger("blue_stones");

		return stoneCount;
	}

	public final int getSealOwner(int seal)
	{
		return _signsSealOwners.get(seal);
	}

	public final int getSealProportion(int seal, int cabal)
	{
		if (cabal == CABAL_NULL)
			return 0;
		else if (cabal == CABAL_DUSK)
			return _signsDuskSealTotals.get(seal);
		else
			return _signsDawnSealTotals.get(seal);
	}

	public final int getTotalMembers(int cabal)
	{
		int cabalMembers = 0;
		String cabalName = getCabalShortName(cabal);

		for (StatsSet sevenDat : _signsPlayerData.values())
			if (sevenDat.getString("cabal").equals(cabalName))
			{
				cabalMembers++;
			}

		return cabalMembers;
	}

	public void giveCPMult(int StrifeOwner)
	{
		for (L2PcInstance character : L2World.getInstance().getAllPlayers())
			if (getPlayerCabal(character) != CABAL_NULL)
				if (getPlayerCabal(character) == StrifeOwner)
				{
					character.addSkill(SkillTable.getInstance().getInfo(5074, 1));
				}
				else
				{
					character.addSkill(SkillTable.getInstance().getInfo(5075, 1));
				}
	}

	private boolean hasRegisteredBefore(L2PcInstance player)
	{
		return _signsPlayerData.containsKey(player.getObjectId());
	}

	protected void initializeSeals()
	{
		for (Integer currSeal : _signsSealOwners.keySet())
		{
			int sealOwner = _signsSealOwners.get(currSeal);

			if (sealOwner != CABAL_NULL)
			{
				if (isSealValidationPeriod())
				{
					switch (currSeal)
					{
						case SEAL_AVARICE:
							if (sealOwner == CABAL_DUSK)
							{
								sendMessageToAll(SystemMessageId.DUSK_OBTAINED_AVARICE);
							}
							else
							{
								sendMessageToAll(SystemMessageId.DAWN_OBTAINED_AVARICE);
							}
							break;
						case SEAL_GNOSIS:
							if (sealOwner == CABAL_DUSK)
							{
								sendMessageToAll(SystemMessageId.DUSK_OBTAINED_GNOSIS);
							}
							else
							{
								sendMessageToAll(SystemMessageId.DAWN_OBTAINED_GNOSIS);
							}
							break;
						case SEAL_STRIFE:
							if (sealOwner == CABAL_DUSK)
							{
								sendMessageToAll(SystemMessageId.DUSK_OBTAINED_STRIFE);
							}
							else
							{
								sendMessageToAll(SystemMessageId.DAWN_OBTAINED_STRIFE);
							}
							break;
					}

					_log.info("SevenSigns: The " + getCabalName(sealOwner) + " have won the " + getSealName(currSeal, false) + ".");
				}
				else
				{
					_log.info("SevenSigns: The " + getSealName(currSeal, false) + " is currently owned by " + getCabalName(sealOwner) + ".");
				}
			}
			else
			{
				_log.info("SevenSigns: The " + getSealName(currSeal, false) + " remains unclaimed.");
			}
		}
	}

	public final boolean isCompResultsPeriod()
	{
		return _activePeriod == PERIOD_COMP_RESULTS;
	}

	public boolean isDateInSealValidPeriod(Calendar date)
	{
		long nextPeriodChange = getMilliToPeriodChange();
		long nextQuestStart = 0;
		long nextValidStart = 0;
		long tillDate = date.getTimeInMillis() - Calendar.getInstance().getTimeInMillis();
		while (2 * PERIOD_MAJOR_LENGTH + 2 * PERIOD_MINOR_LENGTH < tillDate)
		{
			tillDate -= 2 * PERIOD_MAJOR_LENGTH + 2 * PERIOD_MINOR_LENGTH;
		}
		while (tillDate < 0)
		{
			tillDate += 2 * PERIOD_MAJOR_LENGTH + 2 * PERIOD_MINOR_LENGTH;
		}

		switch (getCurrentPeriod())
		{
			case PERIOD_COMP_RECRUITING:
				nextValidStart = nextPeriodChange + PERIOD_MAJOR_LENGTH;
				nextQuestStart = nextValidStart + PERIOD_MAJOR_LENGTH + PERIOD_MINOR_LENGTH;
				break;
			case PERIOD_COMPETITION:
				nextValidStart = nextPeriodChange;
				nextQuestStart = nextPeriodChange + PERIOD_MAJOR_LENGTH + PERIOD_MINOR_LENGTH;
				break;
			case PERIOD_COMP_RESULTS:
				nextQuestStart = nextPeriodChange + PERIOD_MAJOR_LENGTH;
				nextValidStart = nextQuestStart + PERIOD_MAJOR_LENGTH + PERIOD_MINOR_LENGTH;
				break;
			case PERIOD_SEAL_VALIDATION:
				nextQuestStart = nextPeriodChange;
				nextValidStart = nextPeriodChange + PERIOD_MAJOR_LENGTH + PERIOD_MINOR_LENGTH;
				break;
		}

		return !(nextQuestStart < tillDate && tillDate < nextValidStart || nextValidStart < nextQuestStart && (tillDate < nextValidStart || nextQuestStart < tillDate));
	}

	public final boolean isSealValidationPeriod()
	{
		return _activePeriod == PERIOD_SEAL_VALIDATION;
	}

	public void removeCPMult()
	{
		for (L2PcInstance character : L2World.getInstance().getAllPlayers())
		{
			character.removeSkill(SkillTable.getInstance().getInfo(5074, 1));
			character.removeSkill(SkillTable.getInstance().getInfo(5075, 1));
		}
	}

	protected void resetPlayerData()
	{
		for (StatsSet sevenDat : _signsPlayerData.values())
		{
			int charObjId = sevenDat.getInteger("charId");

			sevenDat.set("old_cabal", sevenDat.getString("cabal"));
			sevenDat.set("cabal", "");
			sevenDat.set("seal", SEAL_NULL);
			sevenDat.set("contribution_score", 0);

			_signsPlayerData.put(charObjId, sevenDat);
		}
	}

	protected void resetSeals()
	{
		_signsDawnSealTotals.put(SEAL_AVARICE, 0);
		_signsDawnSealTotals.put(SEAL_GNOSIS, 0);
		_signsDawnSealTotals.put(SEAL_STRIFE, 0);
		_signsDuskSealTotals.put(SEAL_AVARICE, 0);
		_signsDuskSealTotals.put(SEAL_GNOSIS, 0);
		_signsDuskSealTotals.put(SEAL_STRIFE, 0);
	}

	
	protected void restoreSevenSignsData()
	{
		Connection con = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			PreparedStatement statement = con.prepareStatement("SELECT charId, cabal, old_cabal, seal, red_stones, green_stones, blue_stones, " + "ancient_adena_amount, contribution_score FROM seven_signs");
			ResultSet rset = statement.executeQuery();

			while (rset.next())
			{
				int charObjId = rset.getInt("charId");

				StatsSet sevenDat = new StatsSet();
				sevenDat.set("charId", charObjId);
				sevenDat.set("cabal", rset.getString("cabal"));
				sevenDat.set("old_cabal", rset.getString("old_cabal"));
				sevenDat.set("seal", rset.getInt("seal"));
				sevenDat.set("red_stones", rset.getInt("red_stones"));
				sevenDat.set("green_stones", rset.getInt("green_stones"));
				sevenDat.set("blue_stones", rset.getInt("blue_stones"));
				sevenDat.set("ancient_adena_amount", rset.getDouble("ancient_adena_amount"));
				sevenDat.set("contribution_score", rset.getDouble("contribution_score"));

				_signsPlayerData.put(charObjId, sevenDat);
			}

			rset.close();
			statement.close();

			statement = con.prepareStatement("SELECT * FROM seven_signs_status WHERE id=0");
			rset = statement.executeQuery();

			while (rset.next())
			{
				_currentCycle = rset.getInt("current_cycle");
				_activePeriod = rset.getInt("active_period");
				_previousWinner = rset.getInt("previous_winner");

				_dawnStoneScore = rset.getDouble("dawn_stone_score");
				_dawnFestivalScore = rset.getInt("dawn_festival_score");
				_dawnFestivalIdScore[0] = rset.getInt("dawn_festival_score1");
				_dawnFestivalIdScore[1] = rset.getInt("dawn_festival_score2");
				_dawnFestivalIdScore[2] = rset.getInt("dawn_festival_score3");
				_dawnFestivalIdScore[3] = rset.getInt("dawn_festival_score4");
				_dawnFestivalIdScore[4] = rset.getInt("dawn_festival_score5");
				_duskStoneScore = rset.getDouble("dusk_stone_score");
				_duskFestivalScore = rset.getInt("dusk_festival_score");
				_duskFestivalIdScore[0] = rset.getInt("dusk_festival_score1");
				_duskFestivalIdScore[1] = rset.getInt("dusk_festival_score2");
				_duskFestivalIdScore[2] = rset.getInt("dusk_festival_score3");
				_duskFestivalIdScore[3] = rset.getInt("dusk_festival_score4");
				_duskFestivalIdScore[4] = rset.getInt("dusk_festival_score5");

				_signsSealOwners.put(SEAL_AVARICE, rset.getInt("avarice_owner"));
				_signsSealOwners.put(SEAL_GNOSIS, rset.getInt("gnosis_owner"));
				_signsSealOwners.put(SEAL_STRIFE, rset.getInt("strife_owner"));

				_signsDawnSealTotals.put(SEAL_AVARICE, rset.getInt("avarice_dawn_score"));
				_signsDawnSealTotals.put(SEAL_GNOSIS, rset.getInt("gnosis_dawn_score"));
				_signsDawnSealTotals.put(SEAL_STRIFE, rset.getInt("strife_dawn_score"));
				_signsDuskSealTotals.put(SEAL_AVARICE, rset.getInt("avarice_dusk_score"));
				_signsDuskSealTotals.put(SEAL_GNOSIS, rset.getInt("gnosis_dusk_score"));
				_signsDuskSealTotals.put(SEAL_STRIFE, rset.getInt("strife_dusk_score"));
			}

			rset.close();
			statement.close();

			statement = con.prepareStatement("UPDATE seven_signs_status SET date=? WHERE id=0");
			statement.setInt(1, Calendar.getInstance().get(Calendar.DAY_OF_WEEK));
			statement.execute();

			statement.close();
			con.close();
		}
		catch (SQLException e)
		{
			_log.fatal("SevenSigns: Unable to load Seven Signs data from database: " + e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}

	}

	
	public void saveSevenSignsData(L2PcInstance player, boolean updateSettings)
	{
		Connection con = null;

		if (_log.isDebugEnabled() || Config.DEBUG)
		{
			_log.info("SevenSigns: Saving data to disk.");
		}

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);

			for (StatsSet sevenDat : _signsPlayerData.values())
			{
				if (player != null)
					if (sevenDat.getInteger("charId") != player.getObjectId())
					{
						continue;
					}

				PreparedStatement statement = con.prepareStatement("UPDATE seven_signs SET cabal=?, old_cabal=?, seal=?, red_stones=?, " + "green_stones=?, blue_stones=?, " + "ancient_adena_amount=?, contribution_score=? " + "WHERE charId=?");
				statement.setString(1, sevenDat.getString("cabal"));
				statement.setString(2, sevenDat.getString("old_cabal"));
				statement.setInt(3, sevenDat.getInteger("seal"));
				statement.setInt(4, sevenDat.getInteger("red_stones"));
				statement.setInt(5, sevenDat.getInteger("green_stones"));
				statement.setInt(6, sevenDat.getInteger("blue_stones"));
				statement.setDouble(7, sevenDat.getDouble("ancient_adena_amount"));
				statement.setDouble(8, sevenDat.getDouble("contribution_score"));
				statement.setInt(9, sevenDat.getInteger("charId"));
				statement.execute();

				statement.close();

			}

			if (updateSettings)
			{
				String sqlQuery = "UPDATE seven_signs_status SET current_cycle=?, active_period=?, previous_winner=?, " + "dawn_stone_score=?, dawn_festival_score=?, dawn_festival_score1=?, dawn_festival_score2=?, dawn_festival_score3=?, dawn_festival_score4=?, dawn_festival_score5=?, dusk_stone_score=?, dusk_festival_score=?, " + "dusk_festival_score1=?, dusk_festival_score2=?, dusk_festival_score3=?, dusk_festival_score4=?, dusk_festival_score5=?, avarice_owner=?, gnosis_owner=?, strife_owner=?, avarice_dawn_score=?, gnosis_dawn_score=?, " + "strife_dawn_score=?, avarice_dusk_score=?, gnosis_dusk_score=?, strife_dusk_score=?, " + "festival_cycle=?, ";

				for (int i = 0; i < SevenSignsFestival.FESTIVAL_COUNT; i++)
				{
					sqlQuery += "accumulated_bonus" + String.valueOf(i) + "=?, ";
				}

				sqlQuery += "date=? WHERE id=0";

				PreparedStatement statement = con.prepareStatement(sqlQuery);
				statement.setInt(1, _currentCycle);
				statement.setInt(2, _activePeriod);
				statement.setInt(3, _previousWinner);
				statement.setDouble(4, _dawnStoneScore);
				statement.setInt(5, _dawnFestivalScore);
				statement.setInt(6, _dawnFestivalIdScore[0]);
				statement.setInt(7, _dawnFestivalIdScore[1]);
				statement.setInt(8, _dawnFestivalIdScore[2]);
				statement.setInt(9, _dawnFestivalIdScore[3]);
				statement.setInt(10, _dawnFestivalIdScore[4]);
				statement.setDouble(11, _duskStoneScore);
				statement.setInt(12, _duskFestivalScore);
				statement.setInt(13, _duskFestivalIdScore[0]);
				statement.setInt(14, _duskFestivalIdScore[1]);
				statement.setInt(15, _duskFestivalIdScore[2]);
				statement.setInt(16, _duskFestivalIdScore[3]);
				statement.setInt(17, _duskFestivalIdScore[4]);
				statement.setInt(18, _signsSealOwners.get(SEAL_AVARICE));
				statement.setInt(19, _signsSealOwners.get(SEAL_GNOSIS));
				statement.setInt(20, _signsSealOwners.get(SEAL_STRIFE));
				statement.setInt(21, _signsDawnSealTotals.get(SEAL_AVARICE));
				statement.setInt(22, _signsDawnSealTotals.get(SEAL_GNOSIS));
				statement.setInt(23, _signsDawnSealTotals.get(SEAL_STRIFE));
				statement.setInt(24, _signsDuskSealTotals.get(SEAL_AVARICE));
				statement.setInt(25, _signsDuskSealTotals.get(SEAL_GNOSIS));
				statement.setInt(26, _signsDuskSealTotals.get(SEAL_STRIFE));
				statement.setInt(27, SevenSignsFestival.getInstance().getCurrentFestivalCycle());

				for (int i = 0; i < SevenSignsFestival.FESTIVAL_COUNT; i++)
				{
					statement.setInt(28 + i, SevenSignsFestival.getInstance().getAccumulatedBonus(i));
				}

				statement.setInt(28 + SevenSignsFestival.FESTIVAL_COUNT, Calendar.getInstance().get(Calendar.DAY_OF_WEEK));
				statement.execute();

				statement.close();
				con.close();
			}
		}
		catch (SQLException e)
		{
			_log.fatal("SevenSigns: Unable to save data to database: " + e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	public void sendCurrentPeriodMsg(L2PcInstance player)
	{
		SystemMessage sm = null;

		switch (getCurrentPeriod())
		{
			case PERIOD_COMP_RECRUITING:
				sm = SystemMessage.getSystemMessage(SystemMessageId.PREPARATIONS_PERIOD_BEGUN);
				break;
			case PERIOD_COMPETITION:
				sm = SystemMessage.getSystemMessage(SystemMessageId.COMPETITION_PERIOD_BEGUN);
				break;
			case PERIOD_COMP_RESULTS:
				sm = SystemMessage.getSystemMessage(SystemMessageId.RESULTS_PERIOD_BEGUN);
				break;
			case PERIOD_SEAL_VALIDATION:
				sm = SystemMessage.getSystemMessage(SystemMessageId.VALIDATION_PERIOD_BEGUN);
				break;
		}

		player.sendPacket(sm);
	}

	public void sendMessageToAll(SystemMessageId sysMsgId)
	{
		SystemMessage sm = SystemMessage.getSystemMessage(sysMsgId);

		for (L2PcInstance player : L2World.getInstance().getAllPlayers())
		{
			player.sendPacket(sm);
		}
	}

	protected void setCalendarForNextPeriodChange()
	{
		switch (getCurrentPeriod())
		{
			case PERIOD_SEAL_VALIDATION:
			case PERIOD_COMPETITION:
				int daysToChange = getDaysToPeriodChange();

				if (daysToChange == 7)
					if (_calendar.get(Calendar.HOUR_OF_DAY) < PERIOD_START_HOUR)
					{
						daysToChange = 0;
					}
					else if (_calendar.get(Calendar.HOUR_OF_DAY) == PERIOD_START_HOUR && _calendar.get(Calendar.MINUTE) < PERIOD_START_MINS)
					{
						daysToChange = 0;
					}

				if (daysToChange > 0)
				{
					_calendar.add(Calendar.DATE, daysToChange);
				}

				_calendar.set(Calendar.HOUR_OF_DAY, PERIOD_START_HOUR);
				_calendar.set(Calendar.MINUTE, PERIOD_START_MINS);
				break;
			case PERIOD_COMP_RECRUITING:
			case PERIOD_COMP_RESULTS:
				_calendar.add(Calendar.MILLISECOND, PERIOD_MINOR_LENGTH);
				break;
		}
	}

	
	public int setPlayerInfo(L2PcInstance player, int chosenCabal, int chosenSeal)
	{
		int charObjId = player.getObjectId();
		Connection con = null;
		StatsSet currPlayerData = getPlayerData(player);

		if (currPlayerData != null)
		{
			if (!getCabalShortName(chosenCabal).equals(currPlayerData.getString("old_cabal")))
			{
				currPlayerData.set("ancient_adena_amount", 0);
			}

			currPlayerData.set("cabal", getCabalShortName(chosenCabal));
			currPlayerData.set("seal", chosenSeal);

			_signsPlayerData.put(charObjId, currPlayerData);
		}
		else
		{
			currPlayerData = new StatsSet();
			currPlayerData.set("charId", charObjId);
			currPlayerData.set("cabal", getCabalShortName(chosenCabal));
			currPlayerData.set("old_cabal", "");
			currPlayerData.set("seal", chosenSeal);
			currPlayerData.set("red_stones", 0);
			currPlayerData.set("green_stones", 0);
			currPlayerData.set("blue_stones", 0);
			currPlayerData.set("ancient_adena_amount", 0);
			currPlayerData.set("contribution_score", 0);

			_signsPlayerData.put(charObjId, currPlayerData);

			try
			{
				con = L2DatabaseFactory.getInstance().getConnection(con);
				PreparedStatement statement = con.prepareStatement("INSERT INTO seven_signs (charId, cabal, seal) VALUES (?,?,?)");
				statement.setInt(1, charObjId);
				statement.setString(2, getCabalShortName(chosenCabal));
				statement.setInt(3, chosenSeal);
				statement.execute();

				statement.close();
				con.close();
			}
			catch (SQLException e)
			{
				_log.fatal("SevenSigns: Failed to save data: " + e);
			}
			finally
			{
				L2DatabaseFactory.close(con);
			}
		}

		if ("dawn".equals(currPlayerData.getString("cabal")))
		{
			_signsDawnSealTotals.put(chosenSeal, _signsDawnSealTotals.get(chosenSeal) + 1);
		}
		else
		{
			_signsDuskSealTotals.put(chosenSeal, _signsDuskSealTotals.get(chosenSeal) + 1);
		}

		saveSevenSignsData(player, true);
		return chosenCabal;
	}

	public void spawnSevenSignsNPC()
	{
		_merchantSpawn = AutoSpawnManager.getInstance().getAutoSpawnInstance(MAMMON_MERCHANT_ID, false);
		_blacksmithSpawn = AutoSpawnManager.getInstance().getAutoSpawnInstance(MAMMON_BLACKSMITH_ID, false);
		_marketeerSpawns = AutoSpawnManager.getInstance().getAutoSpawnInstances(MAMMON_MARKETEER_ID);
		_spiritInSpawn = AutoSpawnManager.getInstance().getAutoSpawnInstance(SPIRIT_IN_ID, false);
		_spiritOutSpawn = AutoSpawnManager.getInstance().getAutoSpawnInstance(SPIRIT_OUT_ID, false);
		_lilithSpawn = AutoSpawnManager.getInstance().getAutoSpawnInstance(LILITH_NPC_ID, false);
		_anakimSpawn = AutoSpawnManager.getInstance().getAutoSpawnInstance(ANAKIM_NPC_ID, false);
		_crestofdawnspawn = AutoSpawnManager.getInstance().getAutoSpawnInstances(CREST_OF_DAWN_ID);
		_crestofduskspawn = AutoSpawnManager.getInstance().getAutoSpawnInstances(CREST_OF_DUSK_ID);
		_oratorSpawns = AutoSpawnManager.getInstance().getAutoSpawnInstances(ORATOR_NPC_ID);
		_preacherSpawns = AutoSpawnManager.getInstance().getAutoSpawnInstances(PREACHER_NPC_ID);

		if (isSealValidationPeriod() || isCompResultsPeriod())
		{
			if (getCabalHighestScore() == CABAL_DAWN)
			{
				for (int x = 0; x <= ANGELS_ID.length - 1; x++)
				{
					for (L2Spawn spawn : ANGELS)
						if (spawn.getNpcId() == ANGELS_ID[x])
						{
							L2NpcTemplate tmpl = NpcTable.getInstance().getTemplate(DEMONS_ID[x]);
							if (tmpl != null)
							{
								spawn.changeTemplate(tmpl);
							}
						}
				}
			}
			if (getCabalHighestScore() == CABAL_DUSK)
			{
				for (int x = 0; x <= ANGELS_ID.length - 1; x++)
				{
					for (L2Spawn spawn : DEMONS)
						if (spawn.getNpcId() == DEMONS_ID[x])
						{
							L2NpcTemplate tmpl = NpcTable.getInstance().getTemplate(ANGELS_ID[x]);
							if (tmpl != null)
							{
								spawn.changeTemplate(tmpl);
							}
						}
				}
			}

			for (AutoSpawnInstance spawnInst : _marketeerSpawns.values())
			{
				AutoSpawnManager.getInstance().setSpawnActive(spawnInst, true);
			}

			if (getSealOwner(SEAL_GNOSIS) == getCabalHighestScore() && getSealOwner(SEAL_GNOSIS) != CABAL_NULL)
			{
				if (!Config.ANNOUNCE_MAMMON_SPAWN)
				{
					_blacksmithSpawn.setBroadcast(false);
				}

				if (!AutoSpawnManager.getInstance().getAutoSpawnInstance(_blacksmithSpawn.getObjectId(), true).isSpawnActive())
				{
					AutoSpawnManager.getInstance().setSpawnActive(_blacksmithSpawn, true);
				}

				for (AutoSpawnInstance spawnInst : _oratorSpawns.values())
					if (!AutoSpawnManager.getInstance().getAutoSpawnInstance(spawnInst.getObjectId(), true).isSpawnActive())
					{
						AutoSpawnManager.getInstance().setSpawnActive(spawnInst, true);
					}

				for (AutoSpawnInstance spawnInst : _preacherSpawns.values())
					if (!AutoSpawnManager.getInstance().getAutoSpawnInstance(spawnInst.getObjectId(), true).isSpawnActive())
					{
						AutoSpawnManager.getInstance().setSpawnActive(spawnInst, true);
					}

				if (!AutoChatHandler.getInstance().getAutoChatInstance(PREACHER_NPC_ID, false).isActive() && !AutoChatHandler.getInstance().getAutoChatInstance(ORATOR_NPC_ID, false).isActive())
				{
					AutoChatHandler.getInstance().setAutoChatActive(true);
				}
			}
			else
			{
				AutoSpawnManager.getInstance().setSpawnActive(_blacksmithSpawn, false);

				for (AutoSpawnInstance spawnInst : _oratorSpawns.values())
				{
					AutoSpawnManager.getInstance().setSpawnActive(spawnInst, false);
				}

				for (AutoSpawnInstance spawnInst : _preacherSpawns.values())
				{
					AutoSpawnManager.getInstance().setSpawnActive(spawnInst, false);
				}

				AutoChatHandler.getInstance().setAutoChatActive(false);
			}

			if (getSealOwner(SEAL_AVARICE) == getCabalHighestScore() && getSealOwner(SEAL_AVARICE) != CABAL_NULL)
			{
				if (!Config.ANNOUNCE_MAMMON_SPAWN)
				{
					_merchantSpawn.setBroadcast(false);
				}

				if (!AutoSpawnManager.getInstance().getAutoSpawnInstance(_merchantSpawn.getObjectId(), true).isSpawnActive())
				{
					AutoSpawnManager.getInstance().setSpawnActive(_merchantSpawn, true);
				}

				if (!AutoSpawnManager.getInstance().getAutoSpawnInstance(_spiritInSpawn.getObjectId(), true).isSpawnActive())
				{
					AutoSpawnManager.getInstance().setSpawnActive(_spiritInSpawn, true);
				}

				if (!AutoSpawnManager.getInstance().getAutoSpawnInstance(_spiritOutSpawn.getObjectId(), true).isSpawnActive())
				{
					AutoSpawnManager.getInstance().setSpawnActive(_spiritOutSpawn, true);
				}

				switch (getCabalHighestScore())
				{
					case CABAL_DAWN:
						if (!AutoSpawnManager.getInstance().getAutoSpawnInstance(_lilithSpawn.getObjectId(), true).isSpawnActive())
						{
							AutoSpawnManager.getInstance().setSpawnActive(_lilithSpawn, true);
						}

						AutoSpawnManager.getInstance().setSpawnActive(_anakimSpawn, false);
						for (AutoSpawnInstance spawnInst : _crestofdawnspawn.values())
							if (!AutoSpawnManager.getInstance().getAutoSpawnInstance(spawnInst.getObjectId(), true).isSpawnActive())
							{
								AutoSpawnManager.getInstance().setSpawnActive(spawnInst, true);
							}
						for (AutoSpawnInstance spawnInst : _crestofduskspawn.values())
						{
							AutoSpawnManager.getInstance().setSpawnActive(spawnInst, false);
						}
						break;

					case CABAL_DUSK:
						if (!AutoSpawnManager.getInstance().getAutoSpawnInstance(_anakimSpawn.getObjectId(), true).isSpawnActive())
						{
							AutoSpawnManager.getInstance().setSpawnActive(_anakimSpawn, true);
						}

						AutoSpawnManager.getInstance().setSpawnActive(_lilithSpawn, false);
						for (AutoSpawnInstance spawnInst : _crestofduskspawn.values())
							if (!AutoSpawnManager.getInstance().getAutoSpawnInstance(spawnInst.getObjectId(), true).isSpawnActive())
							{
								AutoSpawnManager.getInstance().setSpawnActive(spawnInst, true);
							}
						for (AutoSpawnInstance spawnInst : _crestofdawnspawn.values())
						{
							AutoSpawnManager.getInstance().setSpawnActive(spawnInst, false);
						}
						break;
				}
			}
			else
			{
				AutoSpawnManager.getInstance().setSpawnActive(_merchantSpawn, false);
				AutoSpawnManager.getInstance().setSpawnActive(_lilithSpawn, false);
				AutoSpawnManager.getInstance().setSpawnActive(_anakimSpawn, false);
				for (AutoSpawnInstance spawnInst : _crestofdawnspawn.values())
				{
					AutoSpawnManager.getInstance().setSpawnActive(spawnInst, false);
				}
				for (AutoSpawnInstance spawnInst : _crestofduskspawn.values())
				{
					AutoSpawnManager.getInstance().setSpawnActive(spawnInst, false);
				}
				AutoSpawnManager.getInstance().setSpawnActive(_spiritInSpawn, false);
				AutoSpawnManager.getInstance().setSpawnActive(_spiritOutSpawn, false);
			}
		}
		else
		{
			AutoSpawnManager.getInstance().setSpawnActive(_merchantSpawn, false);
			AutoSpawnManager.getInstance().setSpawnActive(_blacksmithSpawn, false);
			AutoSpawnManager.getInstance().setSpawnActive(_lilithSpawn, false);
			AutoSpawnManager.getInstance().setSpawnActive(_anakimSpawn, false);
			for (AutoSpawnInstance spawnInst : _crestofdawnspawn.values())
			{
				AutoSpawnManager.getInstance().setSpawnActive(spawnInst, false);
			}
			for (AutoSpawnInstance spawnInst : _crestofduskspawn.values())
			{
				AutoSpawnManager.getInstance().setSpawnActive(spawnInst, false);
			}
			AutoSpawnManager.getInstance().setSpawnActive(_spiritInSpawn, false);
			AutoSpawnManager.getInstance().setSpawnActive(_spiritOutSpawn, false);

			for (AutoSpawnInstance spawnInst : _oratorSpawns.values())
			{
				AutoSpawnManager.getInstance().setSpawnActive(spawnInst, false);
			}

			for (AutoSpawnInstance spawnInst : _preacherSpawns.values())
			{
				AutoSpawnManager.getInstance().setSpawnActive(spawnInst, false);
			}

			for (AutoSpawnInstance spawnInst : _marketeerSpawns.values())
			{
				AutoSpawnManager.getInstance().setSpawnActive(spawnInst, false);
			}

			AutoChatHandler.getInstance().setAutoChatActive(false);

			for (int x = 0; x <= ANGELS_ID.length - 1; x++)
			{
				for (L2Spawn spawn : ANGELS)
					if (spawn.getNpcId() == DEMONS_ID[x])
					{
						L2NpcTemplate tmpl = NpcTable.getInstance().getTemplate(ANGELS_ID[x]);
						if (tmpl != null)
						{
							spawn.changeTemplate(tmpl);
						}
					}
				for (L2Spawn spawn : DEMONS)
					if (spawn.getNpcId() == ANGELS_ID[x])
					{
						L2NpcTemplate tmpl = NpcTable.getInstance().getTemplate(DEMONS_ID[x]);
						if (tmpl != null)
						{
							spawn.changeTemplate(tmpl);
						}
					}
			}
		}
	}

	protected void teleLosingCabalFromDungeons(String compWinner)
	{
		for (L2PcInstance onlinePlayer : L2World.getInstance().getAllPlayers())
		{
			if (onlinePlayer == null)
			{
				continue;
			}

			StatsSet currPlayer = getPlayerData(onlinePlayer);

			if (currPlayer == null)
			{
				continue;
			}

			if (isSealValidationPeriod() || isCompResultsPeriod())
			{
				if (!onlinePlayer.isGM() && onlinePlayer.isIn7sDungeon() && !currPlayer.getString("cabal").isEmpty())
				{
					onlinePlayer.teleToLocation(TeleportWhereType.Town);
					onlinePlayer.setIsIn7sDungeon(false);
					onlinePlayer.sendMessage(Message.getMessage(onlinePlayer, Message.MessageId.MSG_YOU_WILL_MOVED_IN_TOWN));
				}
			}
			else if (!onlinePlayer.isGM() && onlinePlayer.isIn7sDungeon() && !currPlayer.getString("cabal").equals(""))
			{
				onlinePlayer.teleToLocation(TeleportWhereType.Town);
				onlinePlayer.setIsIn7sDungeon(false);
				onlinePlayer.sendMessage(Message.getMessage(onlinePlayer, Message.MessageId.MSG_YOU_WILL_MOVED_IN_TOWN));
			}
		}
	}
}