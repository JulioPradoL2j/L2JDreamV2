package com.dream.game.model.zone;

import com.dream.game.datatables.xml.SkillTable;
import com.dream.game.model.L2Object;
import com.dream.game.model.L2Skill;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.L2Npc;
import com.dream.game.model.actor.L2Playable;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.world.Location;
import com.dream.game.model.zone.form.Shape;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.L2GameServerPacket;
import com.dream.game.network.serverpackets.SystemMessage;
import com.dream.game.skills.Env;
import com.dream.game.skills.funcs.Func;
import com.dream.game.skills.funcs.FuncOwner;
import com.dream.game.skills.funcs.FuncTemplate;
import com.dream.tools.random.Rnd;
import com.dream.util.L2Collections;
import com.dream.util.StatsSet;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import javolution.util.FastMap;

public abstract class L2Zone implements FuncOwner
{
	public static enum Affected
	{
		PLAYABLE,
		PC,
		NPC,
		ALL
	}

	public static enum Boss
	{
		ANAKIM,
		ANTHARAS,
		BAIUM,
		FOURSEPULCHERS,
		FRINTEZZA,
		LASTIMPERIALTOMB,
		LILITH,
		SAILREN,
		VALAKAS,
		VANHALTER,
		ZAKEN,
		QUEENANT,
		NONE
	}

	public static enum PvpSettings
	{
		GENERAL,
		ARENA,
		PEACE
	}

	public static enum RestartType
	{
		CHAOTIC,
		OWNER
	}

	public static enum ZoneType
	{
		Arena,
		Boss,
		Castle,
		CastleTeleport,
		Clanhall,
		Damage,
		Default,
		Water,
		Dynamic,
		DefenderSpawn,
		Fishing,
		Fort,
		HeadQuarters,
		Jail,
		Mothertree,
		Pagan,
		Siege,
		Stadium,
		Town,
		Chaotic,
		NoRestart,
		AllowAio,
		NoStrider
	}

	protected static Logger _log = Logger.getLogger(L2Zone.class.getName());

	protected static final Func[] EMPTY_FUNC_SET = new Func[0];

	public static final byte FLAG_PVP = 0;
	public static final byte FLAG_PEACE = 1;

	public static final byte FLAG_SIEGE = 2;
	public static final byte FLAG_MOTHERTREE = 3;
	public static final byte FLAG_CLANHALL = 4;
	public static final byte FLAG_NOESCAPE = 5;
	public static final byte FLAG_NOLANDING = 6;
	public static final byte FLAG_NOSTORE = 7;
	public static final byte FLAG_WATER = 8;
	public static final byte FLAG_FISHING = 9;
	public static final byte FLAG_JAIL = 10;
	public static final byte FLAG_STADIUM = 11;
	public static final byte FLAG_CHAOTIC = 12;
	public static final byte FLAG_NORESTART = 13;
	public static final byte FLAG_DANGER = 14;
	public static final byte FLAG_CASTLE = 15;
	public static final byte FLAG_NOSUMMON = 16;
	public static final byte FLAG_FORT = 17;
	public static final byte FLAG_TRADE = 18;
	public static final byte FLAG_NOCHAT = 19;
	public static final byte FLAG_QUEEN = 20;
	public static final byte FLAG_BAIUM = 21;
	public static final byte FLAG_ZAKEN = 22;
	public static final byte FLAG_NOMAP = 23;
	public static final byte FLAG_ARTEFACTCAST = 24;
	public static final byte FLAG_CASTLETRAP = 25;
	public static final byte FLAG_ALLOWAIO = 26;
	public static final byte FLAG_NOSTRIDER = 27;

	private static void parseApplySkill(List<L2Skill> _applyExit2, String set)
	{
		StringTokenizer st = new StringTokenizer(set, ";");
		while (st.hasMoreTokens())
		{
			StringTokenizer st2 = new StringTokenizer(st.nextToken(), ",");
			int skillId = Integer.parseInt(st2.nextToken());
			int level = Integer.parseInt(st2.nextToken());

			L2Skill skill = SkillTable.getInstance().getInfo(skillId, level);

			if (skill != null)
			{
				_applyExit2.add(skill);
			}
		}
	}

	private static void parseRemoveSkill(List<L2Skill> _removeEnter2, String set)
	{
		StringTokenizer st = new StringTokenizer(set, ";");
		while (st.hasMoreTokens())
		{
			int skillId = Integer.parseInt(st.nextToken());
			L2Skill skill = SkillTable.getInstance().getInfo(skillId, 1);
			if (skill != null)
			{
				_removeEnter2.add(skill);
			}
		}
	}

	public static L2Zone parseZone(Node zn)
	{
		String type = "Default";
		String name = "";
		int id = 0;
		boolean enabled = true;
		L2Zone zone = null;
		Class<?> clazz;
		Constructor<?> constructor;
		try
		{
			id = Integer.parseInt(zn.getAttributes().getNamedItem("id").getNodeValue());
			Node tn = zn.getAttributes().getNamedItem("type");
			Node nn = zn.getAttributes().getNamedItem("name");
			Node en = zn.getAttributes().getNamedItem("enabled");
			if (tn != null)
			{
				type = tn.getNodeValue();
			}
			if (en != null)
			{
				enabled = Boolean.parseBoolean(en.getNodeValue());
			}

			name = nn != null ? nn.getNodeValue() : Integer.valueOf(id).toString();

			clazz = Class.forName("com.dream.game.model.zone.L2" + type + "Zone");
			constructor = clazz.getConstructor();
			zone = (L2Zone) constructor.newInstance();
			zone._typeName = type;
		}
		catch (Exception e)
		{
			_log.error("Cannot create a L2" + type + "Zone for id " + id, e);
			return null;
		}

		zone._id = id;
		if (zn.getAttributes().getNamedItem("zoneType") != null)
		{
			zone._type = ZoneType.valueOf(zn.getAttributes().getNamedItem("zoneType").getNodeValue());
		}
		else
		{
			try
			{
				zone._type = ZoneType.valueOf(type);
			}
			catch (IllegalArgumentException e)
			{
				zone._type = ZoneType.Default;
			}
		}
		zone._name = name;
		zone._enabled = enabled;

		List<Shape> shapes = new ArrayList<>();
		List<Shape> exShapes = new ArrayList<>();
		for (Node n = zn.getFirstChild(); n != null; n = n.getNextSibling())
			if ("set".equalsIgnoreCase(n.getNodeName()))
			{
				NamedNodeMap attrs = n.getAttributes();
				zone._values.set(attrs.getNamedItem("name").getNodeValue(), attrs.getNamedItem("value").getNodeValue());
			}
			else if ("shape".equalsIgnoreCase(n.getNodeName()))
			{
				Shape sh = Shape.parseShape(n, id);
				if (sh != null)
				{
					if (sh.isExclude())
					{
						exShapes.add(sh);
					}
					else
					{
						shapes.add(sh);
					}
				}
				else
					return null;
			}
			else if ("entity".equalsIgnoreCase(n.getNodeName()))
			{
				try
				{
					zone.parseEntity(n);
				}
				catch (Exception e)
				{
					_log.error("Cannot parse entity for zone " + zone.getName() + " (" + zone.getId() + ")", e);
					return null;
				}
			}
			else if ("instance".equalsIgnoreCase(n.getNodeName()))
			{
				try
				{
					zone.parseInstance(n);
				}
				catch (Exception e)
				{
					_log.error("Cannot parse instance for zone " + zone.getName() + " (" + zone.getId() + ")", e);
					return null;
				}
			}
			else if ("settings".equalsIgnoreCase(n.getNodeName()))
			{
				try
				{
					zone.parseSettings(n);
				}
				catch (Exception e)
				{
					_log.error("Cannot parse settings for zone " + zone.getName() + " (" + zone.getId() + ")", e);
					return null;
				}
			}
			else if ("msg".equalsIgnoreCase(n.getNodeName()))
			{
				try
				{
					zone.parseMessages(n);
				}
				catch (Exception e)
				{
					_log.error("Cannot parse messages for zone " + zone.getName() + " (" + zone.getId() + ")", e);
					return null;
				}
			}
			else if ("skill".equalsIgnoreCase(n.getNodeName()))
			{
				try
				{
					zone.parseSkills(n);
				}
				catch (Exception e)
				{
					_log.error("Cannot parse skills for zone " + zone.getName() + " (" + zone.getId() + ")", e);
					return null;
				}
			}
			else if ("cond".equalsIgnoreCase(n.getNodeName()))
			{
				try
				{
					zone.parseCondition(n.getFirstChild());
				}
				catch (Exception e)
				{
					_log.error("Cannot parse skills for zone " + zone.getName() + " (" + zone.getId() + ")", e);
					return null;
				}
			}
			else if ("restart_chaotic".equalsIgnoreCase(n.getNodeName()))
			{
				try
				{
					zone.parseRestart(n, RestartType.CHAOTIC);
				}
				catch (Exception e)
				{
					_log.error("Cannot parse chaotic restart point for zone " + zone.getName() + " (" + zone.getId() + ")", e);
					return null;
				}
			}
			else if ("restart_owner".equalsIgnoreCase(n.getNodeName()))
			{
				try
				{
					zone.parseRestart(n, RestartType.OWNER);
				}
				catch (Exception e)
				{
					_log.error("Cannot parse owner restart point for zone " + zone.getName() + " (" + zone.getId() + ")", e);
					return null;
				}
			}
		zone.parseZoneDetails(zn);
		zone._shapes = shapes.toArray(new Shape[shapes.size()]);
		if (!exShapes.isEmpty())
		{
			zone._exShapes = exShapes.toArray(new Shape[exShapes.size()]);
		}

		shapes.clear();
		exShapes.clear();

		zone.register();

		return zone;
	}

	protected Affected _affected = Affected.ALL;
	protected boolean _removeAll = false;
	protected int _id;
	protected String _name;
	protected boolean _enabled;
	protected Shape[] _shapes;
	protected Shape[] _exShapes;
	protected FastMap<Integer, L2Character> _characterList;
	protected List<L2PcInstance> _playersInZone;
	protected FastMap<RestartType, List<Location>> _restarts;
	protected int _castleId;
	protected int _clanhallId;
	protected int _townId;
	protected int _fortId;
	protected ZoneType _type;
	protected PvpSettings _pvp;
	protected Boss _boss;
	protected boolean _noEscape;
	protected boolean _noLanding;
	protected boolean _noStrider;
	protected boolean _noPrivateStore;
	protected boolean _noSummon;
	public boolean _noMiniMap;
	protected boolean _trade;
	protected boolean _chaotic;
	protected boolean _noRestart;
	protected boolean _allowaio;
	protected boolean _noChat;
	protected boolean _Queen;
	protected boolean _Baium;
	protected boolean _Zaken;
	protected SystemMessage _onEnterMsg;
	protected SystemMessage _onExitMsg;
	protected int _abnormal;
	protected int _hpDamage;
	protected int _mpDamage;
	protected boolean _exitOnDeath;
	protected boolean _buffRepeat;
	protected List<L2Skill> _applyEnter;
	protected List<L2Skill> _applyExit;
	protected List<L2Skill> _removeEnter;
	protected List<L2Skill> _removeExit;
	protected FuncTemplate[] _funcTemplates;
	protected boolean _artefactCast;

	protected boolean _trapCast;
	protected String _typeName;
	protected StatsSet _values;
	protected String _instanceName;

	protected String _instanceGroup;

	protected int _minPlayers;

	protected int _maxPlayers;

	public L2Zone()
	{
		_characterList = new FastMap<Integer, L2Character>().shared();
		_playersInZone = new ArrayList<>();
		_values = new StatsSet();
	}

	public void broadcast(L2GameServerPacket pkt)
	{
		for (L2PcInstance pc : getPlayersInZone())
		{
			pc.sendPacket(pkt);
		}
	}

	public int getCastleId()
	{
		return _castleId;
	}

	public FastMap<Integer, L2Character> getCharactersInside()
	{
		return _characterList;
	}

	public int getClanhallId()
	{
		return _clanhallId;
	}

	public String getClassName()
	{
		return getClass().getSimpleName();
	}

	public double getDistanceToZone(int x, int y)
	{
		double dist = Double.MAX_VALUE;
		for (Shape sh : _shapes)
		{
			dist = Math.min(dist, sh.getDistanceToZone(x, y));
		}
		return dist;
	}

	public double getDistanceToZone(L2Object object)
	{
		return getDistanceToZone(object.getX(), object.getY());
	}

	public int getFortId()
	{
		return _fortId;
	}

	@Override
	public final String getFuncOwnerName()
	{
		return getName();
	}

	@Override
	public final L2Skill getFuncOwnerSkill()
	{
		return null;
	}

	public int getHPDamagePerSecond()
	{
		return _hpDamage;
	}

	public int getId()
	{
		return _id;
	}

	public final int getMaxZ(int x, int y, int z)
	{
		for (Shape sh : _shapes)
			if (sh.contains(x, y))
				return sh.getMaxZ();

		return z;
	}

	public final int getMaxZ(L2Object obj)
	{
		return getMaxZ(obj.getX(), obj.getY(), obj.getZ());
	}

	public int getMiddleX()
	{
		if (_shapes.length == 0)
		{
			_log.error(this + " has no shapes defined");
			return 0;
		}

		int sum = 0;
		for (Shape sh : _shapes)
		{
			sum += sh.getMiddleX();
		}
		return sum / _shapes.length;
	}

	public int getMiddleY()
	{
		if (_shapes.length == 0)
		{
			_log.error(this + " has no shapes defined");
			return 0;
		}

		int sum = 0;
		for (Shape sh : _shapes)
		{
			sum += sh.getMiddleY();
		}

		return sum / _shapes.length;
	}

	public final int getMinZ(int x, int y, int z)
	{
		for (Shape sh : _shapes)
			if (sh.contains(x, y))
				return sh.getMinZ();

		return z;
	}

	public final int getMinZ(L2Object obj)
	{
		return getMinZ(obj.getX(), obj.getY(), obj.getZ());
	}

	public int getMPDamagePerSecond()
	{
		return _mpDamage;
	}

	public String getName()
	{
		return _name;
	}

	public List<L2PcInstance> getPlayersInZone()
	{
		return _playersInZone;
	}

	public Location getRandomLocation()
	{
		if (_shapes.length == 0)
		{
			_log.error(this + " has no shapes defined");
			return new Location(0, 0, 0);
		}
		return _shapes[Rnd.nextInt(_shapes.length)].getRandomLocation();
	}

	public FastMap<RestartType, List<Location>> getRestartMap()
	{
		if (_restarts == null)
		{
			_restarts = new FastMap<RestartType, List<Location>>().shared();
		}

		return _restarts;
	}

	public Location getRestartPoint(RestartType type)
	{
		if (_restarts.containsKey(type) && !_restarts.get(type).isEmpty())
		{
			List<Location> rts = _restarts.get(type);
			return rts.get(Rnd.nextInt(rts.size() - 1));
		}

		return null;
	}

	protected List<Func> getStatFuncs(L2Character player)
	{
		if (_funcTemplates == null)
			return L2Collections.emptyList();

		List<Func> funcs = new ArrayList<>(_funcTemplates.length);
		for (FuncTemplate t : _funcTemplates)
		{
			Env env = new Env();
			env.player = player;
			env.target = player;
			Func f = t.getFunc(env, this);
			if (f != null)
			{
				funcs.add(f);
			}
		}
		return funcs;
	}

	public int getTownId()
	{
		return _townId;
	}

	public ZoneType getType()
	{
		return _type;
	}

	public final String getTypeName()
	{
		return _typeName;
	}

	public final StatsSet getZoneValues()
	{
		return _values;
	}

	public boolean intersectsRectangle(int ax, int bx, int ay, int by)
	{
		for (Shape sh : _shapes)
			if (sh.intersectsRectangle(ax, bx, ay, by))
				return true;
		return false;
	}

	public boolean isCharacterInZone(L2Character character)
	{
		return _characterList.containsKey(character.getObjectId());
	}

	public boolean isCorrectType(L2Character character)
	{
		switch (_affected)
		{
			case PLAYABLE:
				return character instanceof L2Playable;
			case PC:
				return character instanceof L2PcInstance;
			case NPC:
				return character instanceof L2Npc;
			case ALL:
				return true;
		}
		return false;
	}

	public boolean isInsideZone(int x, int y)
	{
		boolean inside = false;
		for (Shape sh : _shapes)
			if (sh.contains(x, y))
			{
				inside = true;
				break;
			}

		if (_exShapes != null && inside)
		{
			for (Shape sh : _exShapes)
				if (sh.contains(x, y))
				{
					inside = false;
					break;
				}
		}
		return inside;
	}

	public boolean isInsideZone(int x, int y, int z)
	{
		boolean inside = false;
		for (Shape sh : _shapes)
			if (sh.contains(x, y, z))
			{
				inside = true;
				break;
			}

		if (_exShapes != null && inside)
		{
			for (Shape sh : _exShapes)
				if (sh.contains(x, y, z))
				{
					inside = false;
					break;
				}
		}
		return inside;
	}

	public boolean isInsideZone(L2Object object)
	{
		return isInsideZone(object.getX(), object.getY(), object.getZ());
	}

	public boolean isPeace()
	{
		return _pvp == PvpSettings.PEACE;
	}

	public void movePlayersTo(int x, int y, int z)
	{
		if (_characterList == null)
			return;
		if (_characterList.isEmpty())
			return;
		for (L2Character character : _characterList.values())
		{
			if (character == null)
			{
				continue;
			}
			if (character instanceof L2PcInstance && ((L2PcInstance) character).isOnline() == 1)
			{
				character.teleToLocation(x, y, z);
			}
		}
	}

	public abstract void onDieInside(L2Character character);

	protected abstract void onEnter(L2Character cha);

	protected abstract void onExit(L2Character character);

	public abstract void onReviveInside(L2Character character);

	protected void parseCondition(Node n) throws Exception
	{
		throw new IllegalStateException("This zone shouldn't have conditions!");
	}

	private void parseEntity(Node n) throws Exception
	{
		Node castle = n.getAttributes().getNamedItem("castleId");
		Node clanhall = n.getAttributes().getNamedItem("clanhallId");
		Node town = n.getAttributes().getNamedItem("townId");
		Node fort = n.getAttributes().getNamedItem("fortId");

		_castleId = castle != null ? Integer.parseInt(castle.getNodeValue()) : -1;
		_clanhallId = clanhall != null ? Integer.parseInt(clanhall.getNodeValue()) : -1;
		_townId = town != null ? Integer.parseInt(town.getNodeValue()) : -1;
		_fortId = fort != null ? Integer.parseInt(fort.getNodeValue()) : -1;
	}

	private void parseInstance(Node n) throws Exception
	{
		Node instanceName = n.getAttributes().getNamedItem("instanceName");
		Node instanceGroup = n.getAttributes().getNamedItem("instanceGroup");
		Node minPlayers = n.getAttributes().getNamedItem("minPlayers");
		Node maxPlayers = n.getAttributes().getNamedItem("maxPlayers");

		_instanceName = instanceName != null ? instanceName.getNodeValue() : null;
		_instanceGroup = instanceGroup != null ? instanceGroup.getNodeValue().toLowerCase() : null;
		_minPlayers = minPlayers != null ? Integer.parseInt(minPlayers.getNodeValue()) : -1;
		_maxPlayers = maxPlayers != null ? Integer.parseInt(maxPlayers.getNodeValue()) : -1;
	}

	private void parseMessages(Node n) throws Exception
	{
		Node enter = n.getAttributes().getNamedItem("onEnter");
		Node exit = n.getAttributes().getNamedItem("onExit");
		int msg = -1;

		if (enter != null)
		{
			String onEnter = enter.getNodeValue();

			try
			{
				msg = Integer.parseInt(onEnter);
			}
			catch (NumberFormatException nfe)
			{
			}

			if (msg != -1)
			{
				_onEnterMsg = SystemMessage.getSystemMessage(SystemMessageId.getSystemMessageId(msg));
			}
			else
			{
				_onEnterMsg = SystemMessage.sendString(onEnter);
			}
		}
		else
		{
			_onEnterMsg = null;
		}

		if (exit != null)
		{
			String onExit = exit.getNodeValue();
			msg = -1;
			try
			{
				msg = Integer.parseInt(onExit);
			}
			catch (NumberFormatException nfe)
			{
			}

			if (msg != -1)
			{
				_onExitMsg = SystemMessage.getSystemMessage(SystemMessageId.getSystemMessageId(msg));
			}
			else
			{
				_onExitMsg = SystemMessage.sendString(onExit);
			}
		}
		else
		{
			_onExitMsg = null;
		}
	}

	private void parseRestart(Node n, RestartType t) throws Exception
	{
		Node xn = n.getAttributes().getNamedItem("x");
		Node yn = n.getAttributes().getNamedItem("y");
		Node zn = n.getAttributes().getNamedItem("z");

		int x = Integer.parseInt(xn.getNodeValue());
		int y = Integer.parseInt(yn.getNodeValue());
		int z = Integer.parseInt(zn.getNodeValue());

		if (!getRestartMap().containsKey(t))
		{
			getRestartMap().put(t, new ArrayList<>());
		}

		getRestartMap().get(t).add(new Location(x, y, z));
	}

	private void parseSettings(Node n) throws Exception
	{
		Node pvp = n.getAttributes().getNamedItem("pvp");
		Node noLanding = n.getAttributes().getNamedItem("noLanding");
		Node noEscape = n.getAttributes().getNamedItem("noEscape");
		Node noPrivateStore = n.getAttributes().getNamedItem("noPrivateStore");
		Node trade = n.getAttributes().getNamedItem("trade");
		Node noChat = n.getAttributes().getNamedItem("noChat");
		Node noSummon = n.getAttributes().getNamedItem("noSummon");
		Node noMiniMap = n.getAttributes().getNamedItem("noMiniMap");
		Node boss = n.getAttributes().getNamedItem("boss");
		Node affected = n.getAttributes().getNamedItem("affected");
		Node buffRepeat = n.getAttributes().getNamedItem("buffRepeat");
		Node abnorm = n.getAttributes().getNamedItem("abnormal");
		Node exitOnDeath = n.getAttributes().getNamedItem("exitOnDeath");
		Node hpDamage = n.getAttributes().getNamedItem("hpDamage");
		Node mpDamage = n.getAttributes().getNamedItem("mpDamage");
		Node artefactCast = n.getAttributes().getNamedItem("actefactCast");
		Node trapCast = n.getAttributes().getNamedItem("trapCast");
		Node chaotic = n.getAttributes().getNamedItem("chaotic");
		Node noRestart = n.getAttributes().getNamedItem("noRestart");
		Node allowaio = n.getAttributes().getNamedItem("allowaio");
		Node noStrider = n.getAttributes().getNamedItem("noStrider");
		// Boss zone
		Node Queen = n.getAttributes().getNamedItem("QueenAnt");
		Node Baium = n.getAttributes().getNamedItem("Baium");
		Node Zaken = n.getAttributes().getNamedItem("Zaken");

		_pvp = pvp != null ? PvpSettings.valueOf(pvp.getNodeValue().toUpperCase()) : PvpSettings.GENERAL;
		_noLanding = noLanding != null && Boolean.parseBoolean(noLanding.getNodeValue());
		_noEscape = noEscape != null && Boolean.parseBoolean(noEscape.getNodeValue());
		_noPrivateStore = noPrivateStore != null && Boolean.parseBoolean(noPrivateStore.getNodeValue());
		_trade = trade != null && Boolean.parseBoolean(trade.getNodeValue());
		_noChat = noChat != null && Boolean.parseBoolean(noChat.getNodeValue());
		_noSummon = noSummon != null && Boolean.parseBoolean(noSummon.getNodeValue());
		_noMiniMap = noMiniMap != null && Boolean.parseBoolean(noMiniMap.getNodeValue());
		_abnormal = abnorm != null ? Integer.decode("0x" + abnorm.getNodeValue()) : 0;
		_exitOnDeath = exitOnDeath != null && Boolean.parseBoolean(exitOnDeath.getNodeValue());
		_hpDamage = hpDamage != null ? Integer.parseInt(hpDamage.getNodeValue()) : 0;
		_mpDamage = mpDamage != null ? Integer.parseInt(mpDamage.getNodeValue()) : 0;
		_buffRepeat = buffRepeat != null && Boolean.parseBoolean(buffRepeat.getNodeValue());
		_artefactCast = artefactCast != null && Boolean.parseBoolean(artefactCast.getNodeValue());
		_trapCast = trapCast != null && Boolean.parseBoolean(trapCast.getNodeValue());
		_chaotic = chaotic != null && Boolean.parseBoolean(chaotic.getNodeValue());
		_noRestart = noRestart != null && Boolean.parseBoolean(noRestart.getNodeValue());
		_allowaio = allowaio != null && Boolean.parseBoolean(allowaio.getNodeValue());
		_noStrider = noStrider != null && Boolean.parseBoolean(noStrider.getNodeValue());

		_Queen = Queen != null && Boolean.parseBoolean(Queen.getNodeValue());
		_Baium = Baium != null && Boolean.parseBoolean(Baium.getNodeValue());
		_Zaken = Zaken != null && Boolean.parseBoolean(Zaken.getNodeValue());

		if (boss != null)
		{
			_boss = Boss.valueOf(boss.getNodeValue().toUpperCase());
		}
		if (affected != null)
		{
			_affected = Affected.valueOf(affected.getNodeValue().toUpperCase());
		}
		if (_affected == null)
		{
			_affected = Affected.PLAYABLE;
		}
	}

	private void parseSkills(Node n) throws Exception
	{
		Node aen = n.getAttributes().getNamedItem("applyEnter");
		Node aex = n.getAttributes().getNamedItem("applyExit");
		Node ren = n.getAttributes().getNamedItem("removeEnter");
		Node rex = n.getAttributes().getNamedItem("removeExit");
		Node rea = n.getAttributes().getNamedItem("removeAll");

		if (aen != null)
		{
			_applyEnter = new ArrayList<>();
			parseApplySkill(_applyEnter, aen.getNodeValue());
		}
		if (aex != null)
		{
			_applyExit = new ArrayList<>();
			parseApplySkill(_applyExit, aex.getNodeValue());
		}
		if (ren != null)
		{
			_removeEnter = new ArrayList<>();
			parseRemoveSkill(_removeEnter, ren.getNodeValue());
		}
		if (rex != null)
		{
			_removeExit = new ArrayList<>();
			parseRemoveSkill(_removeExit, rex.getNodeValue());
		}
		if (rea != null)
		{
			_removeAll = true;
		}
	}

	protected void parseZoneDetails(Node zn)
	{

	}

	protected void register()
	{
	}

	public void removeCharacter(L2Character character)
	{
		if (_characterList.containsKey(character.getObjectId()))
		{
			_characterList.remove(character.getObjectId());
			if (character instanceof L2PcInstance)
			{
				_playersInZone.remove(character);
			}
			onExit(character);

		}

	}

	public void revalidateInZone(L2Character character)
	{

		if (_enabled && isCorrectType(character) && isInsideZone(character))
		{
			if (!_characterList.containsKey(character.getObjectId()))
			{
				_characterList.put(character.getObjectId(), character);
				if (character instanceof L2PcInstance)
				{
					try
					{
						_playersInZone.add((L2PcInstance) character);
					}
					catch (Exception e)
					{
					}
				}
				character.ZoneEnter(this);
				onEnter(character);
			}
		}
		else if (_characterList.containsKey(character.getObjectId()))
		{
			_characterList.remove(character.getObjectId());
			if (character instanceof L2PcInstance)
			{
				try
				{
					_playersInZone.remove(character);
				}
				catch (Exception e)
				{
				}
			}
			character.ZoneLeave(this);
			onExit(character);
		}

	}

	public void setEnabled(boolean val)
	{
		_enabled = val;
	}

	@Override
	public String toString()
	{
		return getClassName() + "[id='" + getId() + "',name='" + getName() + "']";
	}
}