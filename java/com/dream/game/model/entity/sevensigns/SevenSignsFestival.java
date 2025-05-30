package com.dream.game.model.entity.sevensigns;

import com.dream.Config;
import com.dream.L2DatabaseFactory;
import com.dream.game.ai.CtrlIntention;
import com.dream.game.datatables.sql.ClanTable;
import com.dream.game.datatables.sql.NpcTable;
import com.dream.game.datatables.sql.SpawnTable;
import com.dream.game.datatables.xml.MapRegionTable.TeleportWhereType;
import com.dream.game.model.L2Clan;
import com.dream.game.model.L2Party;
import com.dream.game.model.L2Spawn;
import com.dream.game.model.SpawnListener;
import com.dream.game.model.actor.L2Npc;
import com.dream.game.model.actor.instance.L2FestivalMonsterInstance;
import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.actor.position.L2CharPosition;
import com.dream.game.model.base.Experience;
import com.dream.game.model.world.L2World;
import com.dream.game.network.SystemChatChannelId;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.CreatureSay;
import com.dream.game.network.serverpackets.MagicSkillUse;
import com.dream.game.network.serverpackets.SystemMessage;
import com.dream.game.templates.chars.L2NpcTemplate;
import com.dream.game.util.Util;
import com.dream.tools.random.Rnd;
import com.dream.util.L2Timer;
import com.dream.util.StatsSet;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

public class SevenSignsFestival implements SpawnListener
{
	private class FestivalManager implements Runnable
	{
		protected Map<Integer, L2DarknessFestival> _festivalInstances;

		public FestivalManager()
		{
			_festivalInstances = new HashMap<>();
			_managerInstance = this;

			_festivalCycle++;

			setNextCycleStart();
			setNextFestivalStart(Config.ALT_FESTIVAL_CYCLE_LENGTH - FESTIVAL_SIGNUP_TIME);
		}

		public final L2DarknessFestival getFestivalInstance(int oracle, int festivalId)
		{
			if (!isFestivalInitialized())
				return null;

			festivalId += oracle == SevenSigns.CABAL_DUSK ? 10 : 20;
			return _festivalInstances.get(festivalId);
		}

		@Override
		public void run()
		{
			synchronized (FestivalManager.class)
			{
				if (SevenSigns.getInstance().isSealValidationPeriod())
					return;

				if (SevenSigns.getInstance().getMilliToPeriodChange() < Config.ALT_FESTIVAL_CYCLE_LENGTH)
					return;

				if (_log.isDebugEnabled() || Config.DEBUG)
				{
					_log.debug("SevenSignsFestival: Festival manager initialized. Those wishing to participate have " + getMinsToNextFestival() + " minute(s) to sign up.");
				}

				sendMessageToAll("Festival Guide", "The main event will start in " + getMinsToNextFestival() + " minutes. You can register to participate.");

				try
				{
					Thread.sleep(FESTIVAL_SIGNUP_TIME);
				}
				catch (InterruptedException e)
				{
				}

				_dawnPreviousParticipants.clear();
				_duskPreviousParticipants.clear();

				for (L2DarknessFestival festivalInst : _festivalInstances.values())
				{
					festivalInst.unspawnMobs();
				}

				_noPartyRegister = true;

				while (_noPartyRegister)
					if (_duskFestivalParticipants.isEmpty() && _dawnFestivalParticipants.isEmpty())
					{
						setNextCycleStart();
						setNextFestivalStart(Config.ALT_FESTIVAL_CYCLE_LENGTH - FESTIVAL_SIGNUP_TIME);
						try
						{
							Thread.sleep(Config.ALT_FESTIVAL_CYCLE_LENGTH - FESTIVAL_SIGNUP_TIME);
						}
						catch (InterruptedException e)
						{
						}
						for (L2DarknessFestival festivalInst : _festivalInstances.values())
							if (!festivalInst._npcInsts.isEmpty())
							{
								festivalInst.unspawnMobs();
							}
					}
					else
					{
						_noPartyRegister = false;
					}
				if (_log.isDebugEnabled() || Config.DEBUG)
				{
					_log.debug("SevenSignsFestival: No empty ParticipantsLists! Festial starts now ");
				}
				long elapsedTime = 0;

				for (int i = 0; i < FESTIVAL_COUNT; i++)
				{
					if (_duskFestivalParticipants.get(i) != null)
					{
						_festivalInstances.put(10 + i, new L2DarknessFestival(SevenSigns.CABAL_DUSK, i));
					}

					if (_dawnFestivalParticipants.get(i) != null)
					{
						_festivalInstances.put(20 + i, new L2DarknessFestival(SevenSigns.CABAL_DAWN, i));
					}
				}

				_festivalInitialized = true;

				setNextFestivalStart(Config.ALT_FESTIVAL_CYCLE_LENGTH);
				sendMessageToAll("Festival Guide", "The main event begins.");

				if (_log.isDebugEnabled() || Config.DEBUG)
				{
					_log.debug("SevenSignsFestival: The current set of festivals will begin in " + Config.ALT_FESTIVAL_FIRST_SPAWN / 60000 + " minute(s).");
				}

				try
				{
					Thread.sleep(Config.ALT_FESTIVAL_FIRST_SPAWN);
				}
				catch (InterruptedException e)
				{
				}

				elapsedTime = Config.ALT_FESTIVAL_FIRST_SPAWN;

				_festivalInProgress = true;

				for (L2DarknessFestival festivalInst : _festivalInstances.values())
				{
					festivalInst.festivalStart();
					festivalInst.sendMessageToParticipants("Festival of darkness has begun!");
				}

				if (_log.isDebugEnabled() || Config.DEBUG)
				{
					_log.debug("SevenSignsFestival: Each of the festivals will end in " + Config.ALT_FESTIVAL_LENGTH / 60000 + " minutes. New participants can signup then.");
				}

				try
				{
					Thread.sleep(Config.ALT_FESTIVAL_FIRST_SWARM - Config.ALT_FESTIVAL_FIRST_SPAWN);
				}
				catch (InterruptedException e)
				{
				}

				elapsedTime += Config.ALT_FESTIVAL_FIRST_SWARM - Config.ALT_FESTIVAL_FIRST_SPAWN;

				for (L2DarknessFestival festivalInst : _festivalInstances.values())
				{
					festivalInst.moveMonstersToCenter();
				}

				try
				{
					Thread.sleep(Config.ALT_FESTIVAL_SECOND_SPAWN - Config.ALT_FESTIVAL_FIRST_SWARM);
				}
				catch (InterruptedException e)
				{

				}

				for (L2DarknessFestival festivalInst : _festivalInstances.values())
				{
					festivalInst.spawnFestivalMonsters(FESTIVAL_DEFAULT_RESPAWN / 2, 2);
					festivalInst.sendMessageToParticipants("The Festival will end on " + (Config.ALT_FESTIVAL_LENGTH - Config.ALT_FESTIVAL_SECOND_SPAWN) / 60000 + " minutes.");
				}

				elapsedTime += Config.ALT_FESTIVAL_SECOND_SPAWN - Config.ALT_FESTIVAL_FIRST_SWARM;

				try
				{
					Thread.sleep(Config.ALT_FESTIVAL_SECOND_SWARM - Config.ALT_FESTIVAL_SECOND_SPAWN);
				}
				catch (InterruptedException e)
				{
				}

				for (L2DarknessFestival festivalInst : _festivalInstances.values())
				{
					festivalInst.moveMonstersToCenter();
				}

				elapsedTime += Config.ALT_FESTIVAL_SECOND_SWARM - Config.ALT_FESTIVAL_SECOND_SPAWN;

				try
				{
					Thread.sleep(Config.ALT_FESTIVAL_CHEST_SPAWN - Config.ALT_FESTIVAL_SECOND_SWARM);
				}
				catch (InterruptedException e)
				{

				}

				for (L2DarknessFestival festivalInst : _festivalInstances.values())
				{
					festivalInst.spawnFestivalMonsters(FESTIVAL_DEFAULT_RESPAWN, 3);
					festivalInst.sendMessageToParticipants("There were treasure chests of the Festival! Hurry, the Festival will end soon.");
				}

				elapsedTime += Config.ALT_FESTIVAL_CHEST_SPAWN - Config.ALT_FESTIVAL_SECOND_SWARM;

				try
				{
					Thread.sleep(Config.ALT_FESTIVAL_LENGTH - elapsedTime);
				}
				catch (InterruptedException e)
				{

				}

				_festivalInProgress = false;

				for (L2DarknessFestival festivalInst : _festivalInstances.values())
				{
					festivalInst.festivalEnd();
				}

				_dawnFestivalParticipants.clear();
				_duskFestivalParticipants.clear();

				_festivalInitialized = false;

				for (L2DarknessFestival festivalInst : _festivalInstances.values())
				{
					festivalInst.unspawnMobs();
				}

				sendMessageToAll("Witch Festival", "Nothing I can't do anything about it, but I send you back!");

				if (_log.isDebugEnabled() || Config.DEBUG)
				{
					_log.debug("SevenSignsFestival: The next set of festivals begin in " + getMinsToNextFestival() + " minute(s).");
				}

			}
		}
	}

	private class FestivalSpawn
	{
		protected final int _x;
		protected final int _y;
		protected final int _z;
		protected final int _heading;
		protected final int _npcId;

		protected FestivalSpawn(int x, int y, int z, int heading)
		{
			_x = x;
			_y = y;
			_z = z;

			_heading = heading < 0 ? Rnd.nextInt(65536) : heading;

			_npcId = -1;
		}

		protected FestivalSpawn(int[] spawnData)
		{
			_x = spawnData[0];
			_y = spawnData[1];
			_z = spawnData[2];

			_heading = spawnData[3] < 0 ? Rnd.nextInt(65536) : spawnData[3];

			if (spawnData.length > 4)
			{
				_npcId = spawnData[4];
			}
			else
			{
				_npcId = -1;
			}
		}
	}

	private class L2DarknessFestival
	{
		protected final int _cabal;
		protected final int _levelRange;
		protected boolean _challengeIncreased;

		private FestivalSpawn _startLocation;
		private FestivalSpawn _witchSpawn;

		private L2Npc _witchInst;
		public final List<L2FestivalMonsterInstance> _npcInsts;

		private List<L2PcInstance> _participants;
		private final Map<L2PcInstance, FestivalSpawn> _originalLocations;

		protected L2DarknessFestival(int cabal, int levelRange)
		{
			_cabal = cabal;
			_levelRange = levelRange;
			_originalLocations = new HashMap<>();
			_npcInsts = new ArrayList<>();

			if (cabal == SevenSigns.CABAL_DAWN)
			{
				_participants = _dawnFestivalParticipants.get(levelRange);
				_witchSpawn = new FestivalSpawn(FESTIVAL_DAWN_WITCH_SPAWNS[levelRange]);
				_startLocation = new FestivalSpawn(FESTIVAL_DAWN_PLAYER_SPAWNS[levelRange]);
			}
			else
			{
				_participants = _duskFestivalParticipants.get(levelRange);
				_witchSpawn = new FestivalSpawn(FESTIVAL_DUSK_WITCH_SPAWNS[levelRange]);
				_startLocation = new FestivalSpawn(FESTIVAL_DUSK_PLAYER_SPAWNS[levelRange]);
			}

			if (_participants == null)
			{
				_participants = new ArrayList<>();
			}

			festivalInit();
		}

		protected void festivalEnd()
		{
			if (_log.isDebugEnabled() || Config.DEBUG)
			{
				_log.debug("SevenSignsFestival: Ending festival for " + SevenSigns.getCabalShortName(_cabal) + " (" + getFestivalName(_levelRange) + ")");
			}

			if (!_participants.isEmpty())
			{
				for (L2PcInstance participant : _participants)
					if (participant != null)
					{
						relocatePlayer(participant, false);
						participant.sendMessage("Festival of darkness is over. The leader of your group must register the result before the next event starts.");
					}

				if (_cabal == SevenSigns.CABAL_DAWN)
				{
					_dawnPreviousParticipants.put(_levelRange, _participants);
				}
				else
				{
					_duskPreviousParticipants.put(_levelRange, _participants);
				}
			}
			_participants = null;

			unspawnMobs();
		}

		protected void festivalInit()
		{
			boolean isPositive;

			if (_log.isDebugEnabled() || Config.DEBUG)
			{
				_log.debug("SevenSignsFestival: Initializing festival for " + SevenSigns.getCabalShortName(_cabal) + " (" + getFestivalName(_levelRange) + ")");
			}

			if (!_participants.isEmpty())
			{
				for (L2PcInstance participant : _participants)
				{
					if (participant == null)
					{
						continue;
					}
					_originalLocations.put(participant, new FestivalSpawn(participant.getX(), participant.getY(), participant.getZ(), participant.getHeading()));

					int x = _startLocation._x;
					int y = _startLocation._y;

					isPositive = Rnd.nextInt(2) == 1;

					if (isPositive)
					{
						x += Rnd.nextInt(FESTIVAL_MAX_OFFSET_X);
						y += Rnd.nextInt(FESTIVAL_MAX_OFFSET_Y);
					}
					else
					{
						x -= Rnd.nextInt(FESTIVAL_MAX_OFFSET_X);
						y -= Rnd.nextInt(FESTIVAL_MAX_OFFSET_Y);
					}

					participant.getAI().setIntention(CtrlIntention.IDLE);
					participant.teleToLocation(x, y, _startLocation._z, true);

					participant.stopAllEffects();

					L2ItemInstance bloodOfferings = participant.getInventory().getItemByItemId(FESTIVAL_OFFERING_ID);
					if (bloodOfferings != null)
					{
						participant.destroyItem("SevenSigns", bloodOfferings, null, true);
					}
				}
			}

			L2NpcTemplate witchTemplate = NpcTable.getInstance().getTemplate(_witchSpawn._npcId);

			try
			{
				L2Spawn npcSpawn = new L2Spawn(witchTemplate);

				npcSpawn.setLocx(_witchSpawn._x);
				npcSpawn.setLocy(_witchSpawn._y);
				npcSpawn.setLocz(_witchSpawn._z);
				npcSpawn.setHeading(_witchSpawn._heading);
				npcSpawn.setAmount(1);
				npcSpawn.setRespawnDelay(1);

				npcSpawn.startRespawn();

				SpawnTable.getInstance().addNewSpawn(npcSpawn, false);
				_witchInst = npcSpawn.doSpawn();

				if (_log.isDebugEnabled() || Config.DEBUG)
				{
					_log.debug("SevenSignsFestival: Spawned the Festival Witch " + npcSpawn.getNpcId() + " at " + _witchSpawn._x + " " + _witchSpawn._y + " " + _witchSpawn._z);
				}
			}
			catch (Exception e)
			{
				_log.warn("SevenSignsFestival: Error while spawning Festival Witch ID " + _witchSpawn._npcId + ": " + e);
			}

			_witchInst.broadcastPacket(new MagicSkillUse(_witchInst, _witchInst, 2003, 1, 1, 0, false));
			_witchInst.broadcastPacket(new MagicSkillUse(_witchInst, _witchInst, 2133, 1, 1, 0, false));

			sendMessageToParticipants("The Festival will start in 2 minutes.");
		}

		protected void festivalStart()
		{
			if (_log.isDebugEnabled() || Config.DEBUG)
			{
				_log.debug("SevenSignsFestival: Starting festival for " + SevenSigns.getCabalShortName(_cabal) + " (" + getFestivalName(_levelRange) + ")");
			}

			spawnFestivalMonsters(FESTIVAL_DEFAULT_RESPAWN, 0);
		}

		protected boolean increaseChallenge()
		{
			if (_challengeIncreased)
				return false;

			_challengeIncreased = true;

			if (_log.isDebugEnabled() || Config.DEBUG)
			{
				_log.debug("SevenSignsFestival: " + _participants.get(0).getName() + "'s team have opted to increase the festival challenge!");
			}

			spawnFestivalMonsters(FESTIVAL_DEFAULT_RESPAWN, 1);
			return true;
		}

		protected void moveMonstersToCenter()
		{
			boolean isPositive;

			if (_log.isDebugEnabled() || Config.DEBUG)
			{
				_log.debug("SevenSignsFestival: Moving spawns to arena center for festival " + SevenSigns.getCabalShortName(_cabal) + " (" + getFestivalName(_levelRange) + ")");
			}

			for (L2FestivalMonsterInstance festivalMob : _npcInsts)
			{
				if (festivalMob.isDead())
				{
					continue;
				}

				CtrlIntention currIntention = festivalMob.getAI().getIntention();

				if (currIntention != CtrlIntention.IDLE && currIntention != CtrlIntention.ACTIVE)
				{
					continue;
				}

				int x = _startLocation._x;
				int y = _startLocation._y;

				isPositive = Rnd.nextInt(2) == 1;

				if (isPositive)
				{
					x += Rnd.nextInt(FESTIVAL_MAX_OFFSET_X);
					y += Rnd.nextInt(FESTIVAL_MAX_OFFSET_Y);
				}
				else
				{
					x -= Rnd.nextInt(FESTIVAL_MAX_OFFSET_X);
					y -= Rnd.nextInt(FESTIVAL_MAX_OFFSET_Y);
				}

				L2CharPosition moveTo = new L2CharPosition(x, y, _startLocation._z, Rnd.nextInt(65536));

				festivalMob.setRunning();
				festivalMob.getAI().setIntention(CtrlIntention.MOVE_TO, moveTo);
				festivalMob.isAggressive();
				festivalMob.getAggroRange();
			}
		}

		public void relocatePlayer(L2PcInstance participant, boolean isRemoving)
		{
			try
			{
				FestivalSpawn origPosition = _originalLocations.get(participant);

				if (isRemoving)
				{
					_originalLocations.remove(participant);
				}

				participant.getAI().setIntention(CtrlIntention.IDLE);
				participant.teleToLocation(origPosition._x, origPosition._y, origPosition._z, true);
				participant.sendMessage("You have been removed from the arena Festival.");
			}
			catch (Exception e)
			{
				participant.teleToLocation(TeleportWhereType.Town);
			}
		}

		public void sendMessageToParticipants(String message)
		{
			if (!_participants.isEmpty())
			{
				for (L2PcInstance participant : _participants)
					if (participant != null)
					{
						participant.sendPacket(new CreatureSay(_witchInst.getObjectId(), SystemChatChannelId.Chat_Normal, "Witch Festival", message));
					}
			}
		}

		protected void spawnFestivalMonsters(int respawnDelay, int spawnType)
		{
			int[][] _npcSpawns = null;

			switch (spawnType)
			{
				case 0:
				case 1:
					_npcSpawns = _cabal == SevenSigns.CABAL_DAWN ? FESTIVAL_DAWN_PRIMARY_SPAWNS[_levelRange] : FESTIVAL_DUSK_PRIMARY_SPAWNS[_levelRange];
					break;
				case 2:
					_npcSpawns = _cabal == SevenSigns.CABAL_DAWN ? FESTIVAL_DAWN_SECONDARY_SPAWNS[_levelRange] : FESTIVAL_DUSK_SECONDARY_SPAWNS[_levelRange];
					break;
				default:
					_npcSpawns = _cabal == SevenSigns.CABAL_DAWN ? FESTIVAL_DAWN_CHEST_SPAWNS[_levelRange] : FESTIVAL_DUSK_CHEST_SPAWNS[_levelRange];
					break;
			}

			for (int[] element : _npcSpawns)
			{
				FestivalSpawn currSpawn = new FestivalSpawn(element);

				if (spawnType == 1 && isFestivalArcher(currSpawn._npcId))
				{
					continue;
				}

				L2NpcTemplate npcTemplate = NpcTable.getInstance().getTemplate(currSpawn._npcId);

				try
				{
					L2Spawn npcSpawn = new L2Spawn(npcTemplate);

					npcSpawn.setLocx(currSpawn._x);
					npcSpawn.setLocy(currSpawn._y);
					npcSpawn.setLocz(currSpawn._z);
					npcSpawn.setHeading(Rnd.nextInt(65536));
					npcSpawn.setAmount(1);
					npcSpawn.setRespawnDelay(respawnDelay);

					npcSpawn.startRespawn();

					SpawnTable.getInstance().addNewSpawn(npcSpawn, false);
					L2FestivalMonsterInstance festivalMob = (L2FestivalMonsterInstance) npcSpawn.doSpawn();

					if (spawnType == 1)
					{
						festivalMob.setOfferingBonus(2);
					}
					else if (spawnType == 3)
					{
						festivalMob.setOfferingBonus(5);
					}

					_npcInsts.add(festivalMob);

					if (_log.isDebugEnabled() || Config.DEBUG)
					{
						_log.debug("SevenSignsFestival: Spawned NPC ID " + currSpawn._npcId + " at " + currSpawn._x + " " + currSpawn._y + " " + currSpawn._z);
					}
				}
				catch (Exception e)
				{
					_log.warn("SevenSignsFestival: Error while spawning NPC ID " + currSpawn._npcId + ": " + e);
				}
			}
		}

		protected void unspawnMobs()
		{
			if (_witchInst != null)
			{
				_witchInst.getSpawn().stopRespawn();
				_witchInst.deleteMe();
			}

			if (_npcInsts != null)
			{
				for (L2FestivalMonsterInstance monsterInst : _npcInsts)
					if (monsterInst != null)
					{
						monsterInst.getSpawn().stopRespawn();
						monsterInst.deleteMe();
					}
			}
		}
	}

	protected static Logger _log = Logger.getLogger(SevenSignsFestival.class.getName());

	private static SevenSignsFestival _instance;

	private static final String GET_CLAN_NAME = "SELECT clan_name FROM clan_data WHERE clan_id = (SELECT clanid FROM characters WHERE char_name = ?)";

	public static final long FESTIVAL_SIGNUP_TIME = Config.ALT_FESTIVAL_CYCLE_LENGTH - Config.ALT_FESTIVAL_LENGTH - 60000;

	private static final int FESTIVAL_MAX_OFFSET_X = 230;

	private static final int FESTIVAL_MAX_OFFSET_Y = 230;

	private static final int FESTIVAL_DEFAULT_RESPAWN = 60; // Specify

	public static final int FESTIVAL_COUNT = 5;
	public static final int FESTIVAL_LEVEL_MAX_31 = 0;
	public static final int FESTIVAL_LEVEL_MAX_42 = 1;
	public static final int FESTIVAL_LEVEL_MAX_53 = 2;

	public static final int FESTIVAL_LEVEL_MAX_64 = 3;
	public static final int FESTIVAL_LEVEL_MAX_NONE = 4;

	public static final int[] FESTIVAL_LEVEL_SCORES =
	{
		60,
		70,
		100,
		120,
		150
	};

	public static final int FESTIVAL_OFFERING_ID = 5901;

	public static final int FESTIVAL_OFFERING_VALUE = 5;

	public static final int[][] FESTIVAL_DAWN_PLAYER_SPAWNS =
	{
		{
			-79187,
			113186,
			-4895,
			0
		},
		{
			-75918,
			110137,
			-4895,
			0
		},
		{
			-73835,
			111969,
			-4895,
			0
		},
		{
			-76170,
			113804,
			-4895,
			0
		},
		{
			-78927,
			109528,
			-4895,
			0
		}
	};

	public static final int[][] FESTIVAL_DUSK_PLAYER_SPAWNS =
	{
		{
			-77200,
			88966,
			-5151,
			0
		},
		{
			-76941,
			85307,
			-5151,
			0
		},
		{
			-74855,
			87135,
			-5151,
			0
		},
		{
			-80208,
			88222,
			-5151,
			0
		},
		{
			-79954,
			84697,
			-5151,
			0
		}
	};

	protected static final int[][] FESTIVAL_DAWN_WITCH_SPAWNS =
	{
		{
			-79183,
			113052,
			-4891,
			0,
			31132
		},
		{
			-75916,
			110270,
			-4891,
			0,
			31133
		},
		{
			-73979,
			111970,
			-4891,
			0,
			31134
		},
		{
			-76174,
			113663,
			-4891,
			0,
			31135
		},
		{
			-78930,
			109664,
			-4891,
			0,
			31136
		}
	};

	protected static final int[][] FESTIVAL_DUSK_WITCH_SPAWNS =
	{
		{
			-77199,
			88830,
			-5147,
			0,
			31142
		},
		{
			-76942,
			85438,
			-5147,
			0,
			31143
		},
		{
			-74990,
			87135,
			-5147,
			0,
			31144
		},
		{
			-80207,
			88222,
			-5147,
			0,
			31145
		},
		{
			-79952,
			84833,
			-5147,
			0,
			31146
		}
	};

	protected static final int[][][] FESTIVAL_DAWN_PRIMARY_SPAWNS =
	{
		{
			{
				-78537,
				113839,
				-4895,
				-1,
				18009
		},
			{
				-78466,
				113852,
				-4895,
				-1,
				18010
		},
			{
				-78509,
				113899,
				-4895,
				-1,
				18010
		},

			{
				-78481,
				112557,
				-4895,
				-1,
				18009
		},
			{
				-78559,
				112504,
				-4895,
				-1,
				18010
		},
			{
				-78489,
				112494,
				-4895,
				-1,
				18010
		},

			{
				-79803,
				112543,
				-4895,
				-1,
				18012
		},
			{
				-79854,
				112492,
				-4895,
				-1,
				18013
		},
			{
				-79886,
				112557,
				-4895,
				-1,
				18014
		},

			{
				-79821,
				113811,
				-4895,
				-1,
				18015
		},
			{
				-79857,
				113896,
				-4895,
				-1,
				18017
		},
			{
				-79878,
				113816,
				-4895,
				-1,
				18018
		},

			{
				-79190,
				113660,
				-4895,
				-1,
				18011
		},
			{
				-78710,
				113188,
				-4895,
				-1,
				18011
		},
			{
				-79190,
				112730,
				-4895,
				-1,
				18016
		},
			{
				-79656,
				113188,
				-4895,
				-1,
				18016
		}
		},
		{
			{
				-76558,
				110784,
				-4895,
				-1,
				18019
		},
			{
				-76607,
				110815,
				-4895,
				-1,
				18020
		},
			{
				-76559,
				110820,
				-4895,
				-1,
				18020
		},

			{
				-75277,
				110792,
				-4895,
				-1,
				18019
		},
			{
				-75225,
				110801,
				-4895,
				-1,
				18020
		},
			{
				-75262,
				110832,
				-4895,
				-1,
				18020
		},

			{
				-75249,
				109441,
				-4895,
				-1,
				18022
		},
			{
				-75278,
				109495,
				-4895,
				-1,
				18023
		},
			{
				-75223,
				109489,
				-4895,
				-1,
				18024
		},

			{
				-76556,
				109490,
				-4895,
				-1,
				18025
		},
			{
				-76607,
				109469,
				-4895,
				-1,
				18027
		},
			{
				-76561,
				109450,
				-4895,
				-1,
				18028
		},

			{
				-76399,
				110144,
				-4895,
				-1,
				18021
		},
			{
				-75912,
				110606,
				-4895,
				-1,
				18021
		},
			{
				-75444,
				110144,
				-4895,
				-1,
				18026
		},
			{
				-75930,
				109665,
				-4895,
				-1,
				18026
		}
		},
		{
			{
				-73184,
				111319,
				-4895,
				-1,
				18029
		},
			{
				-73135,
				111294,
				-4895,
				-1,
				18030
		},
			{
				-73185,
				111281,
				-4895,
				-1,
				18030
		},

			{
				-74477,
				111321,
				-4895,
				-1,
				18029
		},
			{
				-74523,
				111293,
				-4895,
				-1,
				18030
		},
			{
				-74481,
				111280,
				-4895,
				-1,
				18030
		},

			{
				-74489,
				112604,
				-4895,
				-1,
				18032
		},
			{
				-74491,
				112660,
				-4895,
				-1,
				18033
		},
			{
				-74527,
				112629,
				-4895,
				-1,
				18034
		},

			{
				-73197,
				112621,
				-4895,
				-1,
				18035
		},
			{
				-73142,
				112631,
				-4895,
				-1,
				18037
		},
			{
				-73182,
				112656,
				-4895,
				-1,
				18038
		},

			{
				-73834,
				112430,
				-4895,
				-1,
				18031
		},
			{
				-74299,
				111959,
				-4895,
				-1,
				18031
		},
			{
				-73841,
				111491,
				-4895,
				-1,
				18036
		},
			{
				-73363,
				111959,
				-4895,
				-1,
				18036
		}
		},
		{
			{
				-75543,
				114461,
				-4895,
				-1,
				18039
		},
			{
				-75514,
				114493,
				-4895,
				-1,
				18040
		},
			{
				-75488,
				114456,
				-4895,
				-1,
				18040
		},

			{
				-75521,
				113158,
				-4895,
				-1,
				18039
		},
			{
				-75504,
				113110,
				-4895,
				-1,
				18040
		},
			{
				-75489,
				113142,
				-4895,
				-1,
				18040
		},

			{
				-76809,
				113143,
				-4895,
				-1,
				18042
		},
			{
				-76860,
				113138,
				-4895,
				-1,
				18043
		},
			{
				-76831,
				113112,
				-4895,
				-1,
				18044
		},

			{
				-76831,
				114441,
				-4895,
				-1,
				18045
		},
			{
				-76840,
				114490,
				-4895,
				-1,
				18047
		},
			{
				-76864,
				114455,
				-4895,
				-1,
				18048
		},

			{
				-75703,
				113797,
				-4895,
				-1,
				18041
		},
			{
				-76180,
				114263,
				-4895,
				-1,
				18041
		},
			{
				-76639,
				113797,
				-4895,
				-1,
				18046
		},
			{
				-76180,
				113337,
				-4895,
				-1,
				18046
		}
		},
		{
			{
				-79576,
				108881,
				-4895,
				-1,
				18049
		},
			{
				-79592,
				108835,
				-4895,
				-1,
				18050
		},
			{
				-79614,
				108871,
				-4895,
				-1,
				18050
		},

			{
				-79586,
				110171,
				-4895,
				-1,
				18049
		},
			{
				-79589,
				110216,
				-4895,
				-1,
				18050
		},
			{
				-79620,
				110177,
				-4895,
				-1,
				18050
		},

			{
				-78825,
				110182,
				-4895,
				-1,
				18052
		},
			{
				-78238,
				110182,
				-4895,
				-1,
				18053
		},
			{
				-78266,
				110218,
				-4895,
				-1,
				18054
		},

			{
				-78275,
				108883,
				-4895,
				-1,
				18055
		},
			{
				-78267,
				108839,
				-4895,
				-1,
				18057
		},
			{
				-78241,
				108871,
				-4895,
				-1,
				18058
		},

			{
				-79394,
				109538,
				-4895,
				-1,
				18051
		},
			{
				-78929,
				109992,
				-4895,
				-1,
				18051
		},
			{
				-78454,
				109538,
				-4895,
				-1,
				18056
		},
			{
				-78929,
				109053,
				-4895,
				-1,
				18056
		}
		}
	};

	protected static final int[][][] FESTIVAL_DUSK_PRIMARY_SPAWNS =
	{
		{
			{
				-76542,
				89653,
				-5151,
				-1,
				18009
		},
			{
				-76509,
				89637,
				-5151,
				-1,
				18010
		},
			{
				-76548,
				89614,
				-5151,
				-1,
				18010
		},

			{
				-76539,
				88326,
				-5151,
				-1,
				18009
		},
			{
				-76512,
				88289,
				-5151,
				-1,
				18010
		},
			{
				-76546,
				88287,
				-5151,
				-1,
				18010
		},

			{
				-77879,
				88308,
				-5151,
				-1,
				18012
		},
			{
				-77886,
				88310,
				-5151,
				-1,
				18013
		},
			{
				-77879,
				88278,
				-5151,
				-1,
				18014
		},

			{
				-77857,
				89605,
				-5151,
				-1,
				18015
		},
			{
				-77858,
				89658,
				-5151,
				-1,
				18017
		},
			{
				-77891,
				89633,
				-5151,
				-1,
				18018
		},

			{
				-76728,
				88962,
				-5151,
				-1,
				18011
		},
			{
				-77194,
				88494,
				-5151,
				-1,
				18011
		},
			{
				-77660,
				88896,
				-5151,
				-1,
				18016
		},
			{
				-77195,
				89438,
				-5151,
				-1,
				18016
		}
		},
		{
			{
				-77585,
				84650,
				-5151,
				-1,
				18019
		},
			{
				-77628,
				84643,
				-5151,
				-1,
				18020
		},
			{
				-77607,
				84613,
				-5151,
				-1,
				18020
		},

			{
				-76603,
				85946,
				-5151,
				-1,
				18019
		},
			{
				-77606,
				85994,
				-5151,
				-1,
				18020
		},
			{
				-77638,
				85959,
				-5151,
				-1,
				18020
		},

			{
				-76301,
				85960,
				-5151,
				-1,
				18022
		},
			{
				-76257,
				85972,
				-5151,
				-1,
				18023
		},
			{
				-76286,
				85992,
				-5151,
				-1,
				18024
		},

			{
				-76281,
				84667,
				-5151,
				-1,
				18025
		},
			{
				-76291,
				84611,
				-5151,
				-1,
				18027
		},
			{
				-76257,
				84616,
				-5151,
				-1,
				18028
		},

			{
				-77419,
				85307,
				-5151,
				-1,
				18021
		},
			{
				-76952,
				85768,
				-5151,
				-1,
				18021
		},
			{
				-76477,
				85312,
				-5151,
				-1,
				18026
		},
			{
				-76942,
				84832,
				-5151,
				-1,
				18026
		}
		},
		{
			{
				-74211,
				86494,
				-5151,
				-1,
				18029
		},
			{
				-74200,
				86449,
				-5151,
				-1,
				18030
		},
			{
				-74167,
				86464,
				-5151,
				-1,
				18030
		},

			{
				-75495,
				86482,
				-5151,
				-1,
				18029
		},
			{
				-75540,
				86473,
				-5151,
				-1,
				18030
		},
			{
				-75509,
				86445,
				-5151,
				-1,
				18030
		},

			{
				-75509,
				87775,
				-5151,
				-1,
				18032
		},
			{
				-75518,
				87826,
				-5151,
				-1,
				18033
		},
			{
				-75542,
				87780,
				-5151,
				-1,
				18034
		},

			{
				-74214,
				87789,
				-5151,
				-1,
				18035
		},
			{
				-74169,
				87801,
				-5151,
				-1,
				18037
		},
			{
				-74198,
				87827,
				-5151,
				-1,
				18038
		},

			{
				-75324,
				87135,
				-5151,
				-1,
				18031
		},
			{
				-74852,
				87606,
				-5151,
				-1,
				18031
		},
			{
				-74388,
				87146,
				-5151,
				-1,
				18036
		},
			{
				-74856,
				86663,
				-5151,
				-1,
				18036
		}
		},
		{
			{
				-79560,
				89007,
				-5151,
				-1,
				18039
		},
			{
				-79521,
				89016,
				-5151,
				-1,
				18040
		},
			{
				-79544,
				89047,
				-5151,
				-1,
				18040
		},

			{
				-79552,
				87717,
				-5151,
				-1,
				18039
		},
			{
				-79552,
				87673,
				-5151,
				-1,
				18040
		},
			{
				-79510,
				87702,
				-5151,
				-1,
				18040
		},

			{
				-80866,
				87719,
				-5151,
				-1,
				18042
		},
			{
				-80897,
				87689,
				-5151,
				-1,
				18043
		},
			{
				-80850,
				87685,
				-5151,
				-1,
				18044
		},

			{
				-80848,
				89013,
				-5151,
				-1,
				18045
		},
			{
				-80887,
				89051,
				-5151,
				-1,
				18047
		},
			{
				-80891,
				89004,
				-5151,
				-1,
				18048
		},

			{
				-80205,
				87895,
				-5151,
				-1,
				18041
		},
			{
				-80674,
				88350,
				-5151,
				-1,
				18041
		},
			{
				-80209,
				88833,
				-5151,
				-1,
				18046
		},
			{
				-79743,
				88364,
				-5151,
				-1,
				18046
		}
		},
		{
			{
				-80624,
				84060,
				-5151,
				-1,
				18049
		},
			{
				-80621,
				84007,
				-5151,
				-1,
				18050
		},
			{
				-80590,
				84039,
				-5151,
				-1,
				18050
		},

			{
				-80605,
				85349,
				-5151,
				-1,
				18049
		},
			{
				-80639,
				85363,
				-5151,
				-1,
				18050
		},
			{
				-80611,
				85385,
				-5151,
				-1,
				18050
		},

			{
				-79311,
				85353,
				-5151,
				-1,
				18052
		},
			{
				-79277,
				85384,
				-5151,
				-1,
				18053
		},
			{
				-79273,
				85539,
				-5151,
				-1,
				18054
		},

			{
				-79297,
				84054,
				-5151,
				-1,
				18055
		},
			{
				-79285,
				84006,
				-5151,
				-1,
				18057
		},
			{
				-79260,
				84040,
				-5151,
				-1,
				18058
		},

			{
				-79945,
				85171,
				-5151,
				-1,
				18051
		},
			{
				-79489,
				84707,
				-5151,
				-1,
				18051
		},
			{
				-79952,
				84222,
				-5151,
				-1,
				18056
		},
			{
				-80423,
				84703,
				-5151,
				-1,
				18056
		}
		}
	};

	protected static final int[][][] FESTIVAL_DAWN_SECONDARY_SPAWNS =
	{
		{
			{
				-78757,
				112834,
				-4895,
				-1,
				18016
		},
			{
				-78581,
				112834,
				-4895,
				-1,
				18016
		},

			{
				-78822,
				112526,
				-4895,
				-1,
				18011
		},
			{
				-78822,
				113702,
				-4895,
				-1,
				18011
		},
			{
				-78822,
				113874,
				-4895,
				-1,
				18011
		},

			{
				-79524,
				113546,
				-4895,
				-1,
				18011
		},
			{
				-79693,
				113546,
				-4895,
				-1,
				18011
		},
			{
				-79858,
				113546,
				-4895,
				-1,
				18011
		},

			{
				-79545,
				112757,
				-4895,
				-1,
				18016
		},
			{
				-79545,
				112586,
				-4895,
				-1,
				18016
		},
		},
		{
			{
				-75565,
				110580,
				-4895,
				-1,
				18026
		},
			{
				-75565,
				110740,
				-4895,
				-1,
				18026
		},

			{
				-75577,
				109776,
				-4895,
				-1,
				18021
		},
			{
				-75413,
				109776,
				-4895,
				-1,
				18021
		},
			{
				-75237,
				109776,
				-4895,
				-1,
				18021
		},

			{
				-76274,
				109468,
				-4895,
				-1,
				18021
		},
			{
				-76274,
				109635,
				-4895,
				-1,
				18021
		},
			{
				-76274,
				109795,
				-4895,
				-1,
				18021
		},

			{
				-76351,
				110500,
				-4895,
				-1,
				18056
		},
			{
				-76528,
				110500,
				-4895,
				-1,
				18056
		},
		},
		{
			{
				-74191,
				111527,
				-4895,
				-1,
				18036
		},
			{
				-74191,
				111362,
				-4895,
				-1,
				18036
		},

			{
				-73495,
				111611,
				-4895,
				-1,
				18031
		},
			{
				-73327,
				111611,
				-4895,
				-1,
				18031
		},
			{
				-73154,
				111611,
				-4895,
				-1,
				18031
		},

			{
				-73473,
				112301,
				-4895,
				-1,
				18031
		},
			{
				-73473,
				112475,
				-4895,
				-1,
				18031
		},
			{
				-73473,
				112649,
				-4895,
				-1,
				18031
		},

			{
				-74270,
				112326,
				-4895,
				-1,
				18036
		},
			{
				-74443,
				112326,
				-4895,
				-1,
				18036
		},
		},
		{
			{
				-75738,
				113439,
				-4895,
				-1,
				18046
		},
			{
				-75571,
				113439,
				-4895,
				-1,
				18046
		},

			{
				-75824,
				114141,
				-4895,
				-1,
				18041
		},
			{
				-75824,
				114309,
				-4895,
				-1,
				18041
		},
			{
				-75824,
				114477,
				-4895,
				-1,
				18041
		},

			{
				-76513,
				114158,
				-4895,
				-1,
				18041
		},
			{
				-76683,
				114158,
				-4895,
				-1,
				18041
		},
			{
				-76857,
				114158,
				-4895,
				-1,
				18041
		},

			{
				-76535,
				113357,
				-4895,
				-1,
				18056
		},
			{
				-76535,
				113190,
				-4895,
				-1,
				18056
		},
		},
		{
			{
				-79350,
				109894,
				-4895,
				-1,
				18056
		},
			{
				-79534,
				109894,
				-4895,
				-1,
				18056
		},

			{
				-79285,
				109187,
				-4895,
				-1,
				18051
		},
			{
				-79285,
				109019,
				-4895,
				-1,
				18051
		},
			{
				-79285,
				108860,
				-4895,
				-1,
				18051
		},

			{
				-78587,
				109172,
				-4895,
				-1,
				18051
		},
			{
				-78415,
				109172,
				-4895,
				-1,
				18051
		},
			{
				-78249,
				109172,
				-4895,
				-1,
				18051
		},

			{
				-78575,
				109961,
				-4895,
				-1,
				18056
		},
			{
				-78575,
				110130,
				-4895,
				-1,
				18056
		},
		}
	};

	protected static final int[][][] FESTIVAL_DUSK_SECONDARY_SPAWNS =
	{
		{
			{
				-76844,
				89304,
				-5151,
				-1,
				18011
		},
			{
				-76844,
				89479,
				-5151,
				-1,
				18011
		},
			{
				-76844,
				89649,
				-5151,
				-1,
				18011
		},

			{
				-77544,
				89326,
				-5151,
				-1,
				18011
		},
			{
				-77716,
				89326,
				-5151,
				-1,
				18011
		},
			{
				-77881,
				89326,
				-5151,
				-1,
				18011
		},

			{
				-77561,
				88530,
				-5151,
				-1,
				18016
		},
			{
				-77561,
				88364,
				-5151,
				-1,
				18016
		},

			{
				-76762,
				88615,
				-5151,
				-1,
				18016
		},
			{
				-76594,
				88615,
				-5151,
				-1,
				18016
		},
		},
		{
			{
				-77307,
				84969,
				-5151,
				-1,
				18021
		},
			{
				-77307,
				84795,
				-5151,
				-1,
				18021
		},
			{
				-77307,
				84623,
				-5151,
				-1,
				18021
		},

			{
				-76614,
				84944,
				-5151,
				-1,
				18021
		},
			{
				-76433,
				84944,
				-5151,
				-1,
				18021
		},
			{
				-7626 - 1,
				84944,
				-5151,
				-1,
				18021
		},

			{
				-76594,
				85745,
				-5151,
				-1,
				18026
		},
			{
				-76594,
				85910,
				-5151,
				-1,
				18026
		},

			{
				-77384,
				85660,
				-5151,
				-1,
				18026
		},
			{
				-77555,
				85660,
				-5151,
				-1,
				18026
		},
		},
		{
			{
				-74517,
				86782,
				-5151,
				-1,
				18031
		},
			{
				-74344,
				86782,
				-5151,
				-1,
				18031
		},
			{
				-74185,
				86782,
				-5151,
				-1,
				18031
		},

			{
				-74496,
				87464,
				-5151,
				-1,
				18031
		},
			{
				-74496,
				87636,
				-5151,
				-1,
				18031
		},
			{
				-74496,
				87815,
				-5151,
				-1,
				18031
		},

			{
				-75298,
				87497,
				-5151,
				-1,
				18036
		},
			{
				-75460,
				87497,
				-5151,
				-1,
				18036
		},

			{
				-75219,
				86712,
				-5151,
				-1,
				18036
		},
			{
				-75219,
				86531,
				-5151,
				-1,
				18036
		},
		},
		{
			{
				-79851,
				88703,
				-5151,
				-1,
				18041
		},
			{
				-79851,
				88868,
				-5151,
				-1,
				18041
		},
			{
				-79851,
				89040,
				-5151,
				-1,
				18041
		},

			{
				-80548,
				88722,
				-5151,
				-1,
				18041
		},
			{
				-80711,
				88722,
				-5151,
				-1,
				18041
		},
			{
				-80883,
				88722,
				-5151,
				-1,
				18041
		},

			{
				-80565,
				87916,
				-5151,
				-1,
				18046
		},
			{
				-80565,
				87752,
				-5151,
				-1,
				18046
		},

			{
				-79779,
				87996,
				-5151,
				-1,
				18046
		},
			{
				-79613,
				87996,
				-5151,
				-1,
				18046
		},
		},
		{
			{
				-79271,
				84330,
				-5151,
				-1,
				18051
		},
			{
				-79448,
				84330,
				-5151,
				-1,
				18051
		},
			{
				-79601,
				84330,
				-5151,
				-1,
				18051
		},

			{
				-80311,
				84367,
				-5151,
				-1,
				18051
		},
			{
				-80311,
				84196,
				-5151,
				-1,
				18051
		},
			{
				-80311,
				84015,
				-5151,
				-1,
				18051
		},

			{
				-80556,
				85049,
				-5151,
				-1,
				18056
		},
			{
				-80384,
				85049,
				-5151,
				-1,
				18056
		},

			{
				-79598,
				85127,
				-5151,
				-1,
				18056
		},
			{
				-79598,
				85303,
				-5151,
				-1,
				18056
		},
		}
	};
	protected static final int[][][] FESTIVAL_DAWN_CHEST_SPAWNS =
	{
		{
			{
				-78999,
				112957,
				-4927,
				-1,
				18109
		},
			{
				-79153,
				112873,
				-4927,
				-1,
				18109
		},
			{
				-79256,
				112873,
				-4927,
				-1,
				18109
		},
			{
				-79368,
				112957,
				-4927,
				-1,
				18109
		},

			{
				-79481,
				113124,
				-4927,
				-1,
				18109
		},
			{
				-79481,
				113275,
				-4927,
				-1,
				18109
		},

			{
				-79364,
				113398,
				-4927,
				-1,
				18109
		},
			{
				-79213,
				113500,
				-4927,
				-1,
				18109
		},
			{
				-79099,
				113500,
				-4927,
				-1,
				18109
		},
			{
				-78960,
				113398,
				-4927,
				-1,
				18109
		},

			{
				-78882,
				113235,
				-4927,
				-1,
				18109
		},
			{
				-78882,
				113099,
				-4927,
				-1,
				18109
		},
		},
		{
			{
				-76119,
				110383,
				-4927,
				-1,
				18110
		},
			{
				-75980,
				110442,
				-4927,
				-1,
				18110
		},
			{
				-75848,
				110442,
				-4927,
				-1,
				18110
		},
			{
				-75720,
				110383,
				-4927,
				-1,
				18110
		},

			{
				-75625,
				110195,
				-4927,
				-1,
				18110
		},
			{
				-75625,
				110063,
				-4927,
				-1,
				18110
		},

			{
				-75722,
				109908,
				-4927,
				-1,
				18110
		},
			{
				-75863,
				109832,
				-4927,
				-1,
				18110
		},
			{
				-75989,
				109832,
				-4927,
				-1,
				18110
		},
			{
				-76130,
				109908,
				-4927,
				-1,
				18110
		},

			{
				-76230,
				110079,
				-4927,
				-1,
				18110
		},
			{
				-76230,
				110215,
				-4927,
				-1,
				18110
		},
		},
		{
			{
				-74055,
				111781,
				-4927,
				-1,
				18111
		},
			{
				-74144,
				111938,
				-4927,
				-1,
				18111
		},
			{
				-74144,
				112075,
				-4927,
				-1,
				18111
		},
			{
				-74055,
				112173,
				-4927,
				-1,
				18111
		},

			{
				-73885,
				112289,
				-4927,
				-1,
				18111
		},
			{
				-73756,
				112289,
				-4927,
				-1,
				18111
		},

			{
				-73574,
				112141,
				-4927,
				-1,
				18111
		},
			{
				-73511,
				112040,
				-4927,
				-1,
				18111
		},
			{
				-73511,
				111912,
				-4927,
				-1,
				18111
		},
			{
				-73574,
				111772,
				-4927,
				-1,
				18111
		},

			{
				-73767,
				111669,
				-4927,
				-1,
				18111
		},
			{
				-73899,
				111669,
				-4927,
				-1,
				18111
		},
		},
		{
			{
				-76008,
				113566,
				-4927,
				-1,
				18112
		},
			{
				-76159,
				113485,
				-4927,
				-1,
				18112
		},
			{
				-76267,
				113485,
				-4927,
				-1,
				18112
		},
			{
				-76386,
				113566,
				-4927,
				-1,
				18112
		},

			{
				-76482,
				113748,
				-4927,
				-1,
				18112
		},
			{
				-76482,
				113885,
				-4927,
				-1,
				18112
		},

			{
				-76371,
				114029,
				-4927,
				-1,
				18112
		},
			{
				-76220,
				114118,
				-4927,
				-1,
				18112
		},
			{
				-76092,
				114118,
				-4927,
				-1,
				18112
		},
			{
				-75975,
				114029,
				-4927,
				-1,
				18112
		},

			{
				-75861,
				11385 - 1,
				-4927,
				-1,
				18112
		},
			{
				-75861,
				113713,
				-4927,
				-1,
				18112
		},
		},
		{
			{
				-79100,
				109782,
				-4927,
				-1,
				18113
		},
			{
				-78962,
				109853,
				-4927,
				-1,
				18113
		},
			{
				-78851,
				109853,
				-4927,
				-1,
				18113
		},
			{
				-78721,
				109782,
				-4927,
				-1,
				18113
		},

			{
				-78615,
				109596,
				-4927,
				-1,
				18113
		},
			{
				-78615,
				109453,
				-4927,
				-1,
				18113
		},

			{
				-78746,
				109300,
				-4927,
				-1,
				18113
		},
			{
				-78881,
				109203,
				-4927,
				-1,
				18113
		},
			{
				-79027,
				109203,
				-4927,
				-1,
				18113
		},
			{
				-79159,
				109300,
				-4927,
				-1,
				18113
		},

			{
				-79240,
				109480,
				-4927,
				-1,
				18113
		},
			{
				-79240,
				109615,
				-4927,
				-1,
				18113
		},
		}
	};

	protected static final int[][][] FESTIVAL_DUSK_CHEST_SPAWNS =
	{
		{
			{
				-77016,
				88726,
				-5183,
				-1,
				18114
		},
			{
				-77136,
				88646,
				-5183,
				-1,
				18114
		},
			{
				-77247,
				88646,
				-5183,
				-1,
				18114
		},
			{
				-77380,
				88726,
				-5183,
				-1,
				18114
		},

			{
				-77512,
				88883,
				-5183,
				-1,
				18114
		},
			{
				-77512,
				89053,
				-5183,
				-1,
				18114
		},

			{
				-77378,
				89287,
				-5183,
				-1,
				18114
		},
			{
				-77254,
				89238,
				-5183,
				-1,
				18114
		},
			{
				-77095,
				89238,
				-5183,
				-1,
				18114
		},
			{
				-76996,
				89287,
				-5183,
				-1,
				18114
		},

			{
				-76901,
				89025,
				-5183,
				-1,
				18114
		},
			{
				-76901,
				88891,
				-5183,
				-1,
				18114
		},
		},
		{
			{
				-77128,
				85553,
				-5183,
				-1,
				18115
		},
			{
				-77036,
				85594,
				-5183,
				-1,
				18115
		},
			{
				-76919,
				85594,
				-5183,
				-1,
				18115
		},
			{
				-76755,
				85553,
				-5183,
				-1,
				18115
		},

			{
				-76635,
				85392,
				-5183,
				-1,
				18115
		},
			{
				-76635,
				85216,
				-5183,
				-1,
				18115
		},

			{
				-76761,
				85025,
				-5183,
				-1,
				18115
		},
			{
				-76908,
				85004,
				-5183,
				-1,
				18115
		},
			{
				-77041,
				85004,
				-5183,
				-1,
				18115
		},
			{
				-77138,
				85025,
				-5183,
				-1,
				18115
		},

			{
				-77268,
				85219,
				-5183,
				-1,
				18115
		},
			{
				-77268,
				85410,
				-5183,
				-1,
				18115
		},
		},
		{
			{
				-75150,
				87303,
				-5183,
				-1,
				18116
		},
			{
				-75150,
				87175,
				-5183,
				-1,
				18116
		},
			{
				-75150,
				87175,
				-5183,
				-1,
				18116
		},
			{
				-75150,
				87303,
				-5183,
				-1,
				18116
		},

			{
				-74943,
				87433,
				-5183,
				-1,
				18116
		},
			{
				-74767,
				87433,
				-5183,
				-1,
				18116
		},

			{
				-74556,
				87306,
				-5183,
				-1,
				18116
		},
			{
				-74556,
				87184,
				-5183,
				-1,
				18116
		},
			{
				-74556,
				87184,
				-5183,
				-1,
				18116
		},
			{
				-74556,
				87306,
				-5183,
				-1,
				18116
		},

			{
				-74757,
				86830,
				-5183,
				-1,
				18116
		},
			{
				-74927,
				86830,
				-5183,
				-1,
				18116
		},
		},
		{
			{
				-80010,
				88128,
				-5183,
				-1,
				18117
		},
			{
				-80113,
				88066,
				-5183,
				-1,
				18117
		},
			{
				-80220,
				88066,
				-5183,
				-1,
				18117
		},
			{
				-80359,
				88128,
				-5183,
				-1,
				18117
		},

			{
				-80467,
				88267,
				-5183,
				-1,
				18117
		},
			{
				-80467,
				88436,
				-5183,
				-1,
				18117
		},

			{
				-80381,
				88639,
				-5183,
				-1,
				18117
		},
			{
				-80278,
				88577,
				-5183,
				-1,
				18117
		},
			{
				-80142,
				88577,
				-5183,
				-1,
				18117
		},
			{
				-80028,
				88639,
				-5183,
				-1,
				18117
		},

			{
				-79915,
				88466,
				-5183,
				-1,
				18117
		},
			{
				-79915,
				88322,
				-5183,
				-1,
				18117
		},
		},
		{
			{
				-80153,
				84947,
				-5183,
				-1,
				18118
		},
			{
				-80003,
				84962,
				-5183,
				-1,
				18118
		},
			{
				-79848,
				84962,
				-5183,
				-1,
				18118
		},
			{
				-79742,
				84947,
				-5183,
				-1,
				18118
		},

			{
				-79668,
				84772,
				-5183,
				-1,
				18118
		},
			{
				-79668,
				84619,
				-5183,
				-1,
				18118
		},

			{
				-79772,
				84471,
				-5183,
				-1,
				18118
		},
			{
				-79888,
				84414,
				-5183,
				-1,
				18118
		},
			{
				-80023,
				84414,
				-5183,
				-1,
				18118
		},
			{
				-80166,
				84471,
				-5183,
				-1,
				18118
		},

			{
				-80253,
				84600,
				-5183,
				-1,
				18118
		},
			{
				-80253,
				84780,
				-5183,
				-1,
				18118
		},
		}
	};

	
	private static void addReputationPointsForPartyMemberClan(String partyMemberName)
	{
		L2PcInstance player = L2World.getInstance().getPlayer(partyMemberName);
		if (player != null)
		{
			if (player.getClan() != null)
			{
				player.getClan().setReputationScore(player.getClan().getReputationScore() + 100, true);
				player.getClan().broadcastClanStatus();
				player.getClan().broadcastToOnlineMembers(SystemMessage.getSystemMessage(SystemMessageId.CLAN_MEMBER_S1_WAS_IN_HIGHEST_RANKED_PARTY_IN_FESTIVAL_OF_DARKNESS_AND_GAINED_S2_REPUTATION).addString(partyMemberName).addNumber(100));
			}
		}
		else
		{
			Connection con = null;

			try
			{
				con = L2DatabaseFactory.getInstance().getConnection(con);
				PreparedStatement statement = con.prepareStatement(GET_CLAN_NAME);
				statement.setString(1, partyMemberName);
				ResultSet rset = statement.executeQuery();
				if (rset.next())
				{
					String clanName = rset.getString("clan_name");
					if (clanName != null)
					{
						L2Clan clan = ClanTable.getInstance().getClanByName(clanName);
						if (clan != null)
						{
							clan.setReputationScore(clan.getReputationScore() + 100, true);
							clan.broadcastClanStatus();
							clan.broadcastToOnlineMembers(SystemMessage.getSystemMessage(SystemMessageId.CLAN_MEMBER_S1_WAS_IN_HIGHEST_RANKED_PARTY_IN_FESTIVAL_OF_DARKNESS_AND_GAINED_S2_REPUTATION).addString(partyMemberName).addNumber(100));
						}
					}
				}

				rset.close();
				statement.close();
			}
			catch (Exception e)
			{
				_log.warn("could not get clan name of " + partyMemberName + ": " + e);
			}
			finally
			{
				L2DatabaseFactory.close(con);
			}
		}
	}

	public static final String getFestivalName(int festivalID)
	{
		String festivalName;

		switch (festivalID)
		{
			case FESTIVAL_LEVEL_MAX_31:
				festivalName = "Level 31 or lower";
				break;
			case FESTIVAL_LEVEL_MAX_42:
				festivalName = "Level 42 or lower";
				break;
			case FESTIVAL_LEVEL_MAX_53:
				festivalName = "Level 53 or lower";
				break;
			case FESTIVAL_LEVEL_MAX_64:
				festivalName = "Level 64 or lower";
				break;
			default:
				festivalName = "No Level Limit";
				break;
		}

		return festivalName;
	}

	public static final String getFestivalNameText(int festivalID)
	{
		String festivalName;

		switch (festivalID)
		{
			case FESTIVAL_LEVEL_MAX_31:
				festivalName = "Level 31 or lower";
				break;
			case FESTIVAL_LEVEL_MAX_42:
				festivalName = "Level 42 or lower";
				break;
			case FESTIVAL_LEVEL_MAX_53:
				festivalName = "Level 53 or lower";
				break;
			case FESTIVAL_LEVEL_MAX_64:
				festivalName = "Level 64 or lower";
				break;
			default:
				festivalName = "No Level Limit";
				break;
		}

		return festivalName;
	}

	public static SevenSignsFestival getInstance()
	{
		if (_instance == null)
		{
			_instance = new SevenSignsFestival();
		}

		return _instance;
	}

	public static final int getMaxLevelForFestival(int festivalId)
	{
		int maxLevel = Experience.MAX_LEVEL - 1;

		switch (festivalId)
		{
			case SevenSignsFestival.FESTIVAL_LEVEL_MAX_31:
				maxLevel = 31;
				break;
			case SevenSignsFestival.FESTIVAL_LEVEL_MAX_42:
				maxLevel = 42;
				break;
			case SevenSignsFestival.FESTIVAL_LEVEL_MAX_53:
				maxLevel = 53;
				break;
			case SevenSignsFestival.FESTIVAL_LEVEL_MAX_64:
				maxLevel = 64;
				break;
		}

		return maxLevel;
	}

	public static final boolean isFestivalArcher(int npcId)
	{
		if (npcId < 18009 || npcId > 18108)
			return false;

		int identifier = npcId % 10;
		return identifier == 4 || identifier == 9;
	}

	public static final boolean isFestivalChest(int npcId)
	{
		return !(npcId < 18109 || npcId > 18118);
	}

	protected FestivalManager _managerInstance;
	protected L2Timer _managerScheduledTask;

	protected int _signsCycle = SevenSigns.getInstance().getCurrentCycle();
	protected int _festivalCycle;

	protected long _nextFestivalCycleStart;
	protected long _nextFestivalStart;

	protected boolean _festivalInitialized;
	protected boolean _festivalInProgress;

	protected List<Integer> _accumulatedBonuses;

	public boolean _noPartyRegister;

	private L2Npc _dawnChatGuide;

	private L2Npc _duskChatGuide;

	protected Map<Integer, List<L2PcInstance>> _dawnFestivalParticipants;

	protected Map<Integer, List<L2PcInstance>> _duskFestivalParticipants;

	protected Map<Integer, List<L2PcInstance>> _dawnPreviousParticipants;

	protected Map<Integer, List<L2PcInstance>> _duskPreviousParticipants;

	private Map<Integer, Integer> _dawnFestivalScores;

	private Map<Integer, Integer> _duskFestivalScores;

	private Map<Integer, Map<Integer, StatsSet>> _festivalData;

	public SevenSignsFestival()
	{
		_accumulatedBonuses = new ArrayList<>();

		_dawnFestivalParticipants = new HashMap<>();
		_dawnPreviousParticipants = new HashMap<>();
		_dawnFestivalScores = new HashMap<>();

		_duskFestivalParticipants = new HashMap<>();
		_duskPreviousParticipants = new HashMap<>();
		_duskFestivalScores = new HashMap<>();

		_festivalData = new HashMap<>();

		restoreFestivalData();

		if (SevenSigns.getInstance().isSealValidationPeriod())
		{
			_log.info("SevenSignsFestival: Initialization bypassed due to Seal Validation in effect.");
			return;
		}

		L2Spawn.addSpawnListener(this);
		startFestivalManager();
	}

	public void addAccumulatedBonus(int festivalId, int stoneType, int stoneAmount)
	{
		int eachStoneBonus = 0;

		switch (stoneType)
		{
			case SevenSigns.SEAL_STONE_BLUE_ID:
				eachStoneBonus = SevenSigns.SEAL_STONE_BLUE_VALUE;
				break;
			case SevenSigns.SEAL_STONE_GREEN_ID:
				eachStoneBonus = SevenSigns.SEAL_STONE_GREEN_VALUE;
				break;
			case SevenSigns.SEAL_STONE_RED_ID:
				eachStoneBonus = SevenSigns.SEAL_STONE_RED_VALUE;
				break;
		}

		int newTotalBonus = _accumulatedBonuses.get(festivalId) + stoneAmount * eachStoneBonus;
		_accumulatedBonuses.set(festivalId, newTotalBonus);
	}

	public final int distribAccumulatedBonus(L2PcInstance player)
	{
		int playerBonus = 0;
		String playerName = player.getName();
		int playerCabal = SevenSigns.getInstance().getPlayerCabal(player);

		if (playerCabal != SevenSigns.getInstance().getCabalHighestScore())
			return 0;

		if (_festivalData.get(_signsCycle) != null)
		{
			for (StatsSet festivalData : _festivalData.get(_signsCycle).values())
				if (festivalData.getString("members").indexOf(playerName) > -1)
				{
					int festivalId = festivalData.getInteger("festivalId");
					int numPartyMembers = festivalData.getString("members").split(",").length;
					int totalAccumBonus = _accumulatedBonuses.get(festivalId);

					playerBonus = totalAccumBonus / numPartyMembers;
					_accumulatedBonuses.set(festivalId, totalAccumBonus - playerBonus);
					break;
				}
		}

		return playerBonus;
	}

	public final int getAccumulatedBonus(int festivalId)
	{
		return _accumulatedBonuses.get(festivalId);
	}

	public final int getCurrentFestivalCycle()
	{
		return _festivalCycle;
	}

	public final int[] getFestivalForPlayer(L2PcInstance player)
	{
		int[] playerFestivalInfo =
		{
			-1,
			-1
		};
		int festivalId = 0;

		while (festivalId < FESTIVAL_COUNT)
		{
			List<L2PcInstance> participants = _dawnFestivalParticipants.get(festivalId);

			if (participants != null && participants.contains(player))
			{
				playerFestivalInfo[0] = SevenSigns.CABAL_DAWN;
				playerFestivalInfo[1] = festivalId;

				return playerFestivalInfo;
			}

			participants = _duskFestivalParticipants.get(++festivalId);

			if (participants != null && participants.contains(player))
			{
				playerFestivalInfo[0] = SevenSigns.CABAL_DUSK;
				playerFestivalInfo[1] = festivalId;

				return playerFestivalInfo;
			}

			festivalId++;
		}

		return playerFestivalInfo;
	}

	public final int getFinalScore(int oracle, int festivalId)
	{
		if (oracle == SevenSigns.CABAL_DAWN)
			return _dawnFestivalScores.get(festivalId);

		return _duskFestivalScores.get(festivalId);
	}

	public final int getHighestScore(int oracle, int festivalId)
	{
		return getHighestScoreData(oracle, festivalId).getInteger("score");
	}

	public final StatsSet getHighestScoreData(int oracle, int festivalId)
	{
		int offsetId = festivalId;

		if (oracle == SevenSigns.CABAL_DAWN)
		{
			offsetId += 5;
		}

		StatsSet currData = null;

		try
		{
			currData = _festivalData.get(_signsCycle).get(offsetId);
		}
		catch (Exception e)
		{
			currData = new StatsSet();
			currData.set("score", 0);
			currData.set("members", "");

			if (_log.isDebugEnabled() || Config.DEBUG)
			{
				_log.debug("SevenSignsFestival: Data missing for " + SevenSigns.getCabalName(oracle) + ", FestivalID = " + festivalId + " (Current Cycle " + _signsCycle + ")");
			}
		}

		return currData;
	}

	public final int getMinsToNextCycle()
	{
		if (SevenSigns.getInstance().isSealValidationPeriod())
			return -1;

		return Math.round((_nextFestivalCycleStart - System.currentTimeMillis()) / 60000);
	}

	public final int getMinsToNextFestival()
	{
		if (SevenSigns.getInstance().isSealValidationPeriod())
			return -1;

		return Math.round((_nextFestivalStart - System.currentTimeMillis()) / 60000) + 1;
	}

	public final StatsSet getOverallHighestScoreData(int festivalId)
	{
		StatsSet result = null;
		int highestScore = 0;

		for (Map<Integer, StatsSet> currCycleData : _festivalData.values())
		{
			for (StatsSet currFestData : currCycleData.values())
			{
				int currFestID = currFestData.getInteger("festivalId");
				int festivalScore = currFestData.getInteger("score");

				if (currFestID != festivalId)
				{
					continue;
				}

				if (festivalScore > highestScore)
				{
					highestScore = festivalScore;
					result = currFestData;
				}
			}
		}

		return result;
	}

	public final List<L2PcInstance> getParticipants(int oracle, int festivalId)
	{
		if (oracle == SevenSigns.CABAL_DAWN)
			return _dawnFestivalParticipants.get(festivalId);

		return _duskFestivalParticipants.get(festivalId);
	}

	public final List<L2PcInstance> getPreviousParticipants(int oracle, int festivalId)
	{
		if (oracle == SevenSigns.CABAL_DAWN)
			return _dawnPreviousParticipants.get(festivalId);

		return _duskPreviousParticipants.get(festivalId);
	}

	public final String getTimeToNextFestivalStr()
	{
		if (SevenSigns.getInstance().isSealValidationPeriod())
			return "<font color=\"FF0000\">At this point in time is the period of ownership. Festival of darkness will continue next week.</font>";

		int minToStart = getMinsToNextFestival();
		if (minToStart < 2)
			return "<font color=\"FF0000\">Festival of darkness will begin in a " + minToStart + " minutes.</font>";
		if (minToStart < 5)
			return "<font color=\"FF0000\">Festival of darkness will begin in a " + minToStart + " minutes.</font>";
		return "<font color=\"FF0000\">Festival of darkness will begin in a " + minToStart + " minutes.</font>";
	}

	public final int getTotalAccumulatedBonus()
	{
		int totalAccumBonus = 0;

		for (int accumBonus : _accumulatedBonuses)
		{
			totalAccumBonus += accumBonus;
		}

		return totalAccumBonus;
	}

	public final boolean increaseChallenge(int oracle, int festivalId)
	{
		L2DarknessFestival festivalInst = _managerInstance.getFestivalInstance(oracle, festivalId);

		return festivalInst.increaseChallenge();
	}

	public final boolean isFestivalInitialized()
	{
		return _festivalInitialized;
	}

	public final boolean isFestivalInProgress()
	{
		return _festivalInProgress;
	}

	public final boolean isParticipant(L2PcInstance player)
	{
		if (SevenSigns.getInstance().isSealValidationPeriod())
			return false;

		if (_managerInstance == null)
			return false;

		for (List<L2PcInstance> participants : _dawnFestivalParticipants.values())
			if (participants.contains(player))
				return true;

		for (List<L2PcInstance> participants : _duskFestivalParticipants.values())
			if (participants.contains(player))
				return true;

		return false;
	}

	@Override
	public void npcSpawned(L2Npc npc)
	{
		if (npc == null)
			return;

		int npcId = npc.getNpcId();

		if (npcId == 31127)
		{
			if (_log.isDebugEnabled() || Config.DEBUG)
			{
				_log.debug("SevenSignsFestival: Instance found for NPC ID 31127 (" + npc.getObjectId() + ").");
			}

			_dawnChatGuide = npc;
		}

		if (npcId == 31137)
		{
			if (_log.isDebugEnabled() || Config.DEBUG)
			{
				_log.debug("SevenSignsFestival: Instance found for NPC ID 31137 (" + npc.getObjectId() + ").");
			}

			_duskChatGuide = npc;
		}
	}

	protected void resetFestivalData(boolean updateSettings)
	{
		_festivalCycle = 0;
		_signsCycle = SevenSigns.getInstance().getCurrentCycle();

		for (int i = 0; i < FESTIVAL_COUNT; i++)
		{
			_accumulatedBonuses.set(i, 0);
		}

		_dawnFestivalParticipants.clear();
		_dawnPreviousParticipants.clear();
		_dawnFestivalScores.clear();

		_duskFestivalParticipants.clear();
		_duskPreviousParticipants.clear();
		_duskFestivalScores.clear();

		Map<Integer, StatsSet> newData = new HashMap<>();

		for (int i = 0; i < FESTIVAL_COUNT * 2; i++)
		{
			int festivalId = i;

			if (i >= FESTIVAL_COUNT)
			{
				festivalId -= FESTIVAL_COUNT;
			}

			StatsSet tempStats = new StatsSet();
			tempStats.set("festivalId", festivalId);
			tempStats.set("cycle", _signsCycle);
			tempStats.set("date", "0");
			tempStats.set("score", 0);
			tempStats.set("members", "");

			if (i >= FESTIVAL_COUNT)
			{
				tempStats.set("cabal", SevenSigns.getCabalShortName(SevenSigns.CABAL_DAWN));
			}
			else
			{
				tempStats.set("cabal", SevenSigns.getCabalShortName(SevenSigns.CABAL_DUSK));
			}

			newData.put(i, tempStats);
		}

		_festivalData.put(_signsCycle, newData);

		saveFestivalData(updateSettings);

		for (L2PcInstance onlinePlayer : L2World.getInstance().getAllPlayers())
			if (onlinePlayer != null)
			{
				L2ItemInstance bloodOfferings = onlinePlayer.getInventory().getItemByItemId(FESTIVAL_OFFERING_ID);

				if (bloodOfferings != null)
				{
					onlinePlayer.destroyItem("SevenSigns", bloodOfferings, null, false);
				}
			}

		_log.info("SevenSignsFestival: Reinitialized engine for next competition period.");
	}

	protected void restoreFestivalData()
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;

		if (_log.isDebugEnabled() || Config.DEBUG)
		{
			_log.info("7SignsFestival: Restoring festival data. Current SS Cycle: " + _signsCycle);
		}

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			statement = con.prepareStatement("SELECT festivalId, cabal, cycle, date, score, members " + "FROM seven_signs_festival");
			rset = statement.executeQuery();

			while (rset.next())
			{
				int festivalCycle = rset.getInt("cycle");
				int festivalId = rset.getInt("festivalId");
				String cabal = rset.getString("cabal");

				StatsSet festivalDat = new StatsSet();
				festivalDat.set("festivalId", festivalId);
				festivalDat.set("cabal", cabal);
				festivalDat.set("cycle", festivalCycle);
				festivalDat.set("date", rset.getString("date"));
				festivalDat.set("score", rset.getInt("score"));
				festivalDat.set("members", rset.getString("members"));

				if (_log.isDebugEnabled() || Config.DEBUG)
				{
					_log.debug("SevenSignsFestival: Loaded data from DB for (Cycle = " + festivalCycle + ", Oracle = " + cabal + ", Festival = " + getFestivalName(festivalId));
				}

				if (cabal.equals("dawn"))
				{
					festivalId += FESTIVAL_COUNT;
				}

				Map<Integer, StatsSet> tempData = _festivalData.get(festivalCycle);

				if (tempData == null)
				{
					tempData = new HashMap<>();
				}

				tempData.put(festivalId, festivalDat);
				_festivalData.put(festivalCycle, tempData);
			}

			rset.close();
			statement.close();

			String query = "SELECT festival_cycle, ";

			for (int i = 0; i < FESTIVAL_COUNT - 1; i++)
			{
				query += "accumulated_bonus" + String.valueOf(i) + ", ";
			}

			query += "accumulated_bonus" + String.valueOf(FESTIVAL_COUNT - 1) + " ";
			query += "FROM seven_signs_status WHERE id=0";

			statement = con.prepareStatement(query);
			rset = statement.executeQuery();

			while (rset.next())
			{
				_festivalCycle = rset.getInt("festival_cycle");

				for (int i = 0; i < FESTIVAL_COUNT; i++)
				{
					_accumulatedBonuses.add(i, rset.getInt("accumulated_bonus" + String.valueOf(i)));
				}
			}

			rset.close();
			statement.close();
			con.close();

			if (_log.isDebugEnabled() || Config.DEBUG)
			{
				_log.debug("SevenSignsFestival: Loaded data from database.");
			}
		}
		catch (SQLException e)
		{
			_log.fatal("SevenSignsFestival: Failed to load configuration: " + e);
		}
		finally
		{
			try
			{
				if (rset != null)
				{
					rset.close();
					rset = null;
				}
				if (statement != null)
				{
					statement.close();
					statement = null;
				}
				if (con != null)
				{
					con.close();
					con = null;
				}
			}
			catch (SQLException e)
			{
			}
		}
	}

	protected void rewardHighestRanked()
	{
		String[] partyMembers;
		StatsSet overallData = getOverallHighestScoreData(FESTIVAL_LEVEL_MAX_31);
		if (overallData != null)
		{
			partyMembers = overallData.getString("members").split(",");
			for (String partyMemberName : partyMembers)
			{
				addReputationPointsForPartyMemberClan(partyMemberName);
			}
		}

		overallData = getOverallHighestScoreData(FESTIVAL_LEVEL_MAX_42);
		if (overallData != null)
		{
			partyMembers = overallData.getString("members").split(",");
			for (String partyMemberName : partyMembers)
			{
				addReputationPointsForPartyMemberClan(partyMemberName);
			}
		}

		overallData = getOverallHighestScoreData(FESTIVAL_LEVEL_MAX_53);
		if (overallData != null)
		{
			partyMembers = overallData.getString("members").split(",");
			for (String partyMemberName : partyMembers)
			{
				addReputationPointsForPartyMemberClan(partyMemberName);
			}
		}

		overallData = getOverallHighestScoreData(FESTIVAL_LEVEL_MAX_64);
		if (overallData != null)
		{
			partyMembers = overallData.getString("members").split(",");
			for (String partyMemberName : partyMembers)
			{
				addReputationPointsForPartyMemberClan(partyMemberName);
			}
		}

		overallData = getOverallHighestScoreData(FESTIVAL_LEVEL_MAX_NONE);
		if (overallData != null)
		{
			partyMembers = overallData.getString("members").split(",");
			for (String partyMemberName : partyMembers)
			{
				addReputationPointsForPartyMemberClan(partyMemberName);
			}
		}
	}

	public void saveFestivalData(boolean updateSettings)
	{
		Connection con = null;
		PreparedStatement statement = null;
		if (!updateSettings)
		{
			try
			{
				con = L2DatabaseFactory.getInstance().getConnection(con);

				for (Map<Integer, StatsSet> currCycleData : _festivalData.values())
				{
					for (StatsSet festivalDat : currCycleData.values())
					{
						int festivalCycle = festivalDat.getInteger("cycle");
						int festivalId = festivalDat.getInteger("festivalId");
						String cabal = festivalDat.getString("cabal");

						statement = con.prepareStatement("UPDATE seven_signs_festival SET date=?, score=?, members=? WHERE cycle=? AND cabal=? AND festivalId=?");
						statement.setLong(1, Long.valueOf(festivalDat.getString("date")));
						statement.setInt(2, festivalDat.getInteger("score"));
						statement.setString(3, festivalDat.getString("members"));
						statement.setInt(4, festivalCycle);
						statement.setString(5, cabal);
						statement.setInt(6, festivalId);

						if (statement.executeUpdate() > 0)
						{
							if (_log.isDebugEnabled() || Config.DEBUG)
							{
								_log.debug("SevenSignsFestival: Updated data in DB (Cycle = " + festivalCycle + ", Cabal = " + cabal + ", FestID = " + festivalId + ")");
							}

							statement.close();
							continue;
						}

						statement.close();

						statement = con.prepareStatement("INSERT INTO seven_signs_festival (festivalId, cabal, cycle, date, score, members) VALUES (?,?,?,?,?,?)");
						statement.setInt(1, festivalId);
						statement.setString(2, cabal);
						statement.setInt(3, festivalCycle);
						statement.setLong(4, Long.valueOf(festivalDat.getString("date")));
						statement.setInt(5, festivalDat.getInteger("score"));
						statement.setString(6, festivalDat.getString("members"));
						statement.execute();
						statement.close();

						if (_log.isDebugEnabled() || Config.DEBUG)
						{
							_log.debug("SevenSignsFestival: Inserted data in DB (Cycle = " + festivalCycle + ", Cabal = " + cabal + ", FestID = " + festivalId + ")");
						}
					}
				}

				con.close();
				if (!updateSettings)
					// _log.fatal("SevenSignsFestival: Failed to save configuration: " + e);
					if (updateSettings)
					{
						SevenSigns.getInstance().saveSevenSignsData(null, true);
					}
			}
			catch (SQLException e)
			{
				_log.fatal("SevenSignsFestival: Failed to save configuration: " + e);
			}
			finally
			{
				try
				{
					if (statement != null)
					{
						statement.close();
						statement = null;
					}
					if (con != null)
					{
						con.close();
						con = null;
					}
				}
				catch (Exception e)
				{
				}
			}
		}
	}

	public void sendMessageToAll(String senderName, String message)
	{
		if (_dawnChatGuide == null || _duskChatGuide == null)
			return;

		_dawnChatGuide.broadcastPacket(new CreatureSay(_dawnChatGuide.getObjectId(), SystemChatChannelId.Chat_Shout, senderName, message));
		_duskChatGuide.broadcastPacket(new CreatureSay(_duskChatGuide.getObjectId(), SystemChatChannelId.Chat_Shout, senderName, message));
	}

	public boolean setFinalScore(L2PcInstance player, int oracle, int festivalId, int offeringScore)
	{
		List<String> partyMembers;

		int currDawnHighScore = getHighestScore(SevenSigns.CABAL_DAWN, festivalId);
		int currDuskHighScore = getHighestScore(SevenSigns.CABAL_DUSK, festivalId);

		int thisCabalHighScore = 0;
		int otherCabalHighScore = 0;

		if (oracle == SevenSigns.CABAL_DAWN)
		{
			thisCabalHighScore = currDawnHighScore;
			otherCabalHighScore = currDuskHighScore;

			_dawnFestivalScores.put(festivalId, offeringScore);
		}
		else
		{
			thisCabalHighScore = currDuskHighScore;
			otherCabalHighScore = currDawnHighScore;

			_duskFestivalScores.put(festivalId, offeringScore);
		}

		StatsSet currFestData = getHighestScoreData(oracle, festivalId);

		if (offeringScore > thisCabalHighScore)
		{
			partyMembers = new ArrayList<>();
			List<L2PcInstance> prevParticipants = getPreviousParticipants(oracle, festivalId);

			for (L2PcInstance partyMember : prevParticipants)
				if (partyMember != null)
				{
					partyMembers.add(partyMember.getName());
				}

			currFestData.set("date", String.valueOf(System.currentTimeMillis()));
			currFestData.set("score", offeringScore);
			currFestData.set("members", Util.implodeString(partyMembers, ","));

			if (offeringScore > otherCabalHighScore)
			{
				int contribPoints = FESTIVAL_LEVEL_SCORES[festivalId];

				SevenSigns.getInstance().addFestivalScore(oracle, contribPoints, festivalId);
				saveFestivalData(true);
				return true;
			}
			saveFestivalData(true);
		}
		return false;
	}

	public void setNextCycleStart()
	{
		_nextFestivalCycleStart = System.currentTimeMillis() + Config.ALT_FESTIVAL_CYCLE_LENGTH;
	}

	public void setNextFestivalStart(long milliFromNow)
	{
		_nextFestivalStart = System.currentTimeMillis() + milliFromNow;
	}

	public void setParticipants(int oracle, int festivalId, L2Party festivalParty)
	{
		List<L2PcInstance> participants = new ArrayList<>();

		if (festivalParty != null)
		{
			participants = festivalParty.getPartyMembers();

			if (_log.isDebugEnabled() || Config.DEBUG)
			{
				_log.debug("SevenSignsFestival: " + festivalParty.getPartyMembers().toString() + " have signed up to the " + SevenSigns.getCabalShortName(oracle) + " " + getFestivalName(festivalId) + " festival.");
			}
		}

		if (oracle == SevenSigns.CABAL_DAWN)
		{
			_dawnFestivalParticipants.put(festivalId, participants);
		}
		else
		{
			_duskFestivalParticipants.put(festivalId, participants);
		}
	}

	protected void startFestivalManager()
	{
		stopFestivalManager();

		if (SevenSigns.getInstance().isSealValidationPeriod())
			return;
		FestivalManager fm = new FestivalManager();
		setNextFestivalStart(Config.ALT_FESTIVAL_MANAGER_START + FESTIVAL_SIGNUP_TIME);
		_managerScheduledTask = new L2Timer("FestivalManager").scheduleAtFixedRate(fm, Config.ALT_FESTIVAL_MANAGER_START, Config.ALT_FESTIVAL_CYCLE_LENGTH);

		_log.info("7SignsFestival: 1st Festival of Darkness cycle begins in " + Config.ALT_FESTIVAL_MANAGER_START / 60000 + " minutes.");
	}

	public void stopFestivalManager()
	{
		if (_managerScheduledTask != null)
		{
			_managerScheduledTask.cancel();
			_managerScheduledTask = null;
		}
	}

	public void updateParticipants(L2PcInstance player, L2Party festivalParty)
	{
		if (!isParticipant(player))
			return;

		final int[] playerFestInfo = getFestivalForPlayer(player);
		final int oracle = playerFestInfo[0];
		final int festivalId = playerFestInfo[1];

		if (festivalId > -1)
		{
			if (_festivalInitialized)
			{
				L2DarknessFestival festivalInst = _managerInstance.getFestivalInstance(oracle, festivalId);

				if (festivalParty == null)
				{
					for (L2PcInstance partyMember : getParticipants(oracle, festivalId))
					{
						festivalInst.relocatePlayer(partyMember, true);
					}
				}
				else
				{
					festivalInst.relocatePlayer(player, true);
				}
			}
			setParticipants(oracle, festivalId, festivalParty);

			if (festivalParty != null && festivalParty.getMemberCount() < Config.ALT_FESTIVAL_MIN_PLAYER)
			{
				updateParticipants(player, null);
				festivalParty.removePartyMember(player);
			}
		}
	}
}