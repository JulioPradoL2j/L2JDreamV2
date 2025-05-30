package com.dream.game.model.entity.events.DM;

import java.util.Map;

import com.dream.Message;
import com.dream.Message.MessageId;
import com.dream.annotations.L2Properties;
import com.dream.game.Announcements;
import com.dream.game.ai.CtrlIntention;
import com.dream.game.datatables.sql.ItemTable;
import com.dream.game.datatables.xml.SkillTable;
import com.dream.game.handler.VoicedCommandHandler;
import com.dream.game.manager.TaskManager;
import com.dream.game.model.L2Skill;
import com.dream.game.model.L2Skill.SkillTargetType;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.L2Npc;
import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.entity.events.GameEvent;
import com.dream.game.model.entity.events.TvT.TvT.Team;
import com.dream.game.model.world.L2World;
import com.dream.game.model.world.Location;
import com.dream.game.network.SystemChatChannelId;
import com.dream.game.network.ThreadPoolManager;
import com.dream.game.network.serverpackets.CreatureSay;
import com.dream.game.network.serverpackets.ExShowScreenMessage;
import com.dream.game.taskmanager.tasks.ExclusiveTask;
import com.dream.game.templates.item.L2EtcItemType;
import com.dream.game.templates.skills.L2SkillType;
import com.dream.tools.random.Rnd;
import com.dream.util.ArrayUtils;

import javolution.util.FastMap;

public class DeathMatch extends GameEvent
{
	public final Map<Integer, Team> _players = new FastMap<>();
    private final FastMap<Integer, Location> _playerLoc = new FastMap<>();
    private int _state = GameEvent.STATE_INACTIVE;
    private static DeathMatch _instance = null;
    public long _eventDate = 0;
    private int _minLvl = 0;
    private int _maxLvl = 0;
    private int _maxPlayers = 60;
    private int _minPlayers = 0;
    public int _instanceId = 0;
    private int _regTime = 0;
    public int _eventTime = 0;
    private int[] _rewardId = null;
    private int[] _rewardAmount = null;
    private int _reviveDelay = 0;
    public int _remaining;

    public boolean ON_START_REMOVE_ALL_EFFECTS;
    public boolean ON_START_UNSUMMON_PET;
    public Location EVENT_LOCATION;
    private boolean RESORE_HP_MP_CP;
    private boolean ALLOW_POTIONS;
    private boolean ALLOW_SUMMON;
    private boolean JOIN_CURSED;
    private boolean ALLOW_INTERFERENCE;
    public boolean RESET_SKILL_REUSE;
    private boolean DM_RETURNORIGINAL;

    public static final DeathMatch getInstance()
    {
        if (_instance == null)
        {
            new DeathMatch();
        }
        return _instance;
    }

    public String getStatus()
    {
        int free = (_maxPlayers - _players.size());
        if (free < 0)
        {
            free = 0;
        }

        return free + " from " + _maxPlayers;
    }

    public DeathMatch()
    {
        _instance = this;
    }

    @Override
    public boolean finish()
    {
        _eventTask.cancel();
        _registrationTask.cancel();
        L2PcInstance player;
		for (Integer playerId : _players.keySet())
        {
            player = L2World.getInstance().findPlayer(playerId);

            if (player != null)
            {
                remove(player);
            }
        }
        _players.clear();
        _state = GameEvent.STATE_INACTIVE;
        return true;
    }

    @Override
    public String getName()
    {
		return "DeathMatch";
    }

    @Override
    public int getState()
    {
        return _state;
    }

    @Override
    public boolean isParticipant(L2PcInstance player)
    {
		if (_state == GameEvent.STATE_INACTIVE)
			return false;
		return _players.containsKey(player.getObjectId());
    }

    @Override
    public boolean load()
    {
        try
        {
			L2Properties Setting = new L2Properties("./config/events/dmevent.properties");

            if (!Boolean.parseBoolean(Setting.getProperty("DMEnabled", "true")))
            {
                return false;
            }

            ON_START_REMOVE_ALL_EFFECTS = Boolean.parseBoolean(Setting.getProperty("OnStartRemoveAllEffects", "true"));
            ON_START_UNSUMMON_PET = Boolean.parseBoolean(Setting.getProperty("OnStartUnsummonPet", "true"));
            DM_RETURNORIGINAL = Boolean.parseBoolean(Setting.getProperty("OriginalPosition", "false"));
            RESORE_HP_MP_CP = Boolean.parseBoolean(Setting.getProperty("OnStartRestoreHpMpCp", "false"));
            ALLOW_POTIONS = Boolean.parseBoolean(Setting.getProperty("AllowPotion", "false"));
            ALLOW_SUMMON = Boolean.parseBoolean(Setting.getProperty("AllowSummon", "false"));
            JOIN_CURSED = Boolean.parseBoolean(Setting.getProperty("CursedWeapon", "false"));
            ALLOW_INTERFERENCE = Boolean.parseBoolean(Setting.getProperty("AllowInterference", "false"));
            RESET_SKILL_REUSE = Boolean.parseBoolean(Setting.getProperty("ResetAllSkill", "false"));
            EVENT_LOCATION = new Location(Setting.getProperty("EventLocation", "149800 46800 -3412"));

            _reviveDelay = Integer.parseInt(Setting.getProperty("ReviveDelay", "10"));
            _regTime = Integer.parseInt(Setting.getProperty("RegTime", "10"));
            _eventTime = Integer.parseInt(Setting.getProperty("EventTime", "10"));
            _rewardId = null;
            _rewardAmount = null;

            for (String s : Setting.getProperty("RewardItem", "57").split(","))
            {
                _rewardId = ArrayUtils.add(_rewardId, Integer.parseInt(s));
            }
            for (String s : Setting.getProperty("RewardItemCount", "50000").split(","))
            {
                _rewardAmount = ArrayUtils.add(_rewardAmount, Integer.parseInt(s));
            }
            _minPlayers = Integer.parseInt(Setting.getProperty("MinPlayers", "2"));
            _maxPlayers = Integer.parseInt(Setting.getProperty("MaxPlayers", "60"));
            _minLvl = Integer.parseInt(Setting.getProperty("MinLevel", "1"));
            _maxLvl = Integer.parseInt(Setting.getProperty("MaxLevel", "90"));
        }
        catch (Exception e)
        {
            _log.warn("DeathMatch: Error reading config ", e);
            return false;
        }

        TaskManager.getInstance().registerTask(new TaskStartDM());
        VoicedCommandHandler.getInstance().registerVoicedCommandHandler(new VoiceDeathMatch());
        return true;
    }

    @Override
    public void onCommand(L2PcInstance actor, String command, String params)
    {
        if (_state == GameEvent.STATE_ACTIVE)
        {
            if (command.equals("join"))
            {
                if (!register(actor))
                {
                    actor.sendMessage(Message.getMessage(actor, Message.MessageId.MSG_EVENT_CANT_REGISTERED));
                }
            }
            else if (command.equals("leave"))
            {
                remove(actor);
            }
        }
    }

    @Override
    public void onKill(L2Character killer, L2Character victim)
    {
        if ((killer == null) || (victim == null))
        {
            return;
        }

        if ((killer instanceof L2PcInstance) && (victim instanceof L2PcInstance))
        {
            plk = (L2PcInstance) killer;
            pld = (L2PcInstance) victim;

            if ((plk != null) && (plk._event == this) && (pld != null) && (pld._event == this))
            {
                plk.setDmKills(plk.getDmKills() + 1);
                plk.setTitle(String.format(Message.getMessage(null, MessageId.MSG_EVT_12), plk.getDmKills()));
				plk.broadcastFullInfo();
                pld.sendMessage(Message.getMessage(pld, Message.MessageId.MSG_EVENT_WAIT_FOR_RES));
                ThreadPoolManager.getInstance().scheduleGeneral(new revivePlayer(victim), _reviveDelay * 1000);
            }
        }

    }

    @Override
    public boolean onNPCTalk(L2Npc npc, L2PcInstance talker)
    {
        return false;
    }

    @Override
    public boolean register(L2PcInstance player)
    {
        if (!canRegister(player))
        {
            return false;
        }

		_players.put(player.getObjectId(), null);
        player._event = this;
        return true;
    }

    @Override
    public void remove(L2PcInstance player)
    {
        if (isParticipant(player))
        {
            if (_state == GameEvent.STATE_RUNNING)
            {
                if (player.isDead())
                {
                    player.doRevive();
                }
                player.setDmKills(0);
                if (!DM_RETURNORIGINAL)
                {
                    randomTeleport(player);
                }
                else
                {
                    player.teleToLocation(_playerLoc.get(player.getObjectId()), false);
                }
            }
            player._event = null;
            _players.remove(player.getObjectId());
        }
    }

    @Override
    public boolean canRegister(L2PcInstance player)
    {
        if (getState() != STATE_ACTIVE)
        {
            player.sendMessage(Message.getMessage(player, Message.MessageId.MSG_EVENT_NOT_ALLOWED));
            return false;
        }
        if (isParticipant(player))
        {
            player.sendMessage(Message.getMessage(player, Message.MessageId.MSG_EVENT_ALREADY_REGISTERED));
            return false;
        }

        if (_players.size() >= _maxPlayers)
        {
            player.sendMessage(Message.getMessage(player, Message.MessageId.MSG_EVENT_FULL));
            return false;
        }
        if (player.isCursedWeaponEquipped() && !JOIN_CURSED)
        {
            player.sendMessage(Message.getMessage(player, Message.MessageId.MSG_CURSED_WEAPON_NOT_ALLOW));
            return false;
        }
        if ((player.getLevel() > _maxLvl) || (player.getLevel() < _minLvl))
        {
            player.sendMessage(Message.getMessage(player, Message.MessageId.MSG_EVENT_WRONG_LEVEL));
            return false;
        }
        if (!player.canRegisterToEvents())
        {
            return false;
        }
        return true;
    }

    @Override
    public boolean start()
    {
        _players.clear();

        AnnounceToPlayers(true, String.format(Message.getMessage(null, MessageId.MSG_EVT_1), getName()));
		AnnounceToPlayers(true, String.format(Message.getMessage(null, MessageId.MSG_EVT_15), getName()));
        AnnounceToPlayers(true, String.format(Message.getMessage(null, MessageId.MSG_EVT_2), getName(), _minLvl, _maxLvl));
        for (int i = 0; i < _rewardId.length; i++)
        {
			AnnounceToPlayers(true, "DeathMatch: Reward - " + _rewardAmount[i] + " " + ItemTable.getInstance().getTemplate(_rewardId[i]).getName());
        }
        AnnounceToPlayers(true, String.format(Message.getMessage(null, MessageId.MSG_EVT_4), getName(), _regTime));

        _state = GameEvent.STATE_ACTIVE;
        _remaining = (_regTime * 60000) / 2;
        _registrationTask.schedule(_remaining);
        return true;
    }

    @Override
    public boolean canInteract(L2Character actor, L2Character target)
    {
        if (_state == GameEvent.STATE_RUNNING)
        {
            return ((actor._event == target._event) && (actor._event == this)) || ALLOW_INTERFERENCE;
        }
        return true;
    }

    @Override
    public boolean canAttack(L2Character attacker, L2Character target)
    {
        if (_state == GameEvent.STATE_RUNNING)
        {
            return (attacker._event == target._event) && (attacker._event == this);
        }

        return false;
    }

    @Override
    public boolean canBeSkillTarget(L2Character caster, L2Character target, L2Skill skill)
    {
        if (_state == GameEvent.STATE_RUNNING)
        {
            return false;
        }

        return true;
    }

    @Override
    public boolean canUseItem(L2Character actor, L2ItemInstance item)
    {
        if (_state == GameEvent.STATE_RUNNING)
        {
            if (item.getItem().getItemType() == L2EtcItemType.POTION)
            {
                return ALLOW_POTIONS;
            }
            int itemId = item.getItemId();
            return !(((itemId == 3936) || (itemId == 3959) || (itemId == 737) || (itemId == 9157) || (itemId == 10150) || (itemId == 13259)));

        }
        return true;
    }

    @Override
    public boolean canUseSkill(L2Character caster, L2Skill skill)
    {
        if (_state == GameEvent.STATE_RUNNING)
        {
            if ((skill.getTargetType() == SkillTargetType.TARGET_PET) || (skill.getTargetType() == SkillTargetType.TARGET_SELF))
            {
                return true;
            }
            else if (skill.getSkillType() == L2SkillType.SUMMON)
            {
                return ALLOW_SUMMON;
            }
            else if ((skill.getSkillType() == L2SkillType.HEAL) || (skill.getSkillType() == L2SkillType.BUFF) || (skill.getSkillType() == L2SkillType.MANAHEAL))
            {
                return caster.getTarget() == caster;
            }
        }
        return true;
    }

    @Override
    public void onRevive(L2Character actor)
    {
        if (RESORE_HP_MP_CP && (_state == GameEvent.STATE_RUNNING))
        {
            actor.getStatus().setCurrentCp(actor.getMaxCp());
            actor.getStatus().setCurrentHp(actor.getMaxHp());
            actor.getStatus().setCurrentMp(actor.getMaxMp());
        }
    }

    @Override
    public void onLogin(L2PcInstance player)
    {
        if (_state == GameEvent.STATE_RUNNING)
        {
            remove(player);
        }
    }

    public void AnnounceToPlayers(Boolean toall, String announce)
    {
        if (toall)
        {
            Announcements.getInstance().criticalAnnounceToAll(announce);
        }
        else
        {
            CreatureSay cs = new CreatureSay(0, SystemChatChannelId.Chat_Critical_Announce, "", announce);
            L2PcInstance player;
            if ((_players != null) && !_players.isEmpty())
            {
				for (Integer playerid : _players.keySet())
                {
                    player = L2World.getInstance().findPlayer(playerid);
                    if ((player != null) && (player.isOnline() != 0))
                    {
                        player.sendPacket(cs);
                    }
                }
            }
        }
    }

    private final ExclusiveTask _registrationTask = new ExclusiveTask()
    {
        private boolean showed;

        @Override
        protected void onElapsed()
        {
            if (_remaining < 1000)
            {
                run();
            }
            else
            {
                if (_remaining >= 60000)
                {
                    AnnounceToPlayers(true, String.format(Message.getMessage(null, MessageId.MSG_EVT_5), getName(), _remaining / 60000));
                }
                else if (!showed)
                {
                    AnnounceToPlayers(true, String.format(Message.getMessage(null, MessageId.MSG_EVT_6), getName()));
                    showed = true;
                }
                _remaining /= 2;
                schedule(_remaining);
            }
        }
    };

    private final Runnable TeleportTask = new Runnable()
    {
        @Override
        public void run()
        {
            L2PcInstance player;
            int[] par =
            {
                -1,
                1
            };
            int Radius = 500;

			for (Integer playerId : _players.keySet())
            {
                player = L2World.getInstance().findPlayer(playerId);
                if (player != null)
                {
                    player.abortAttack();
                    player.abortCast();
                    player.setTarget(null);
                    if (RESET_SKILL_REUSE)
                    {
                        player.resetSkillTime(true);
                    }
					if (ON_START_REMOVE_ALL_EFFECTS)
                    {
                        player.stopAllEffects();
                    }
                    if (player.getPet() != null)
                    {
                        player.getPet().abortAttack();
                        player.getPet().abortCast();
                        player.getPet().setTarget(null);
                        if (ON_START_REMOVE_ALL_EFFECTS)
                        {
                            player.getPet().stopAllEffects();
                        }
                        if (ON_START_UNSUMMON_PET)
                        {
                            player.getPet().unSummon(player);
                        }
                    }
                    if (player.getParty() != null)
                    {
                        player.getParty().removePartyMember(player);
                    }

                    player.teleToLocation(EVENT_LOCATION.getX() + (par[Rnd.get(2)] * Rnd.get(Radius)), EVENT_LOCATION.getY() + (par[Rnd.get(2)] * Rnd.get(Radius)), EVENT_LOCATION.getZ());
                    player.setDmKills(0);
					player.setTitle(String.format(Message.getMessage(null, MessageId.MSG_EVT_12), 0));
					SkillTable.getInstance().getInfo(4515, 1).getEffects(player, player);
					player.sendPacket(new ExShowScreenMessage("1 minute until event start, wait", 10000));
                }
            }

            ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
            {
                @Override
                public void run()
                {
                    L2PcInstance player;
					for (Integer playerId : _players.keySet())
                    {
                        player = L2World.getInstance().findPlayer(playerId);
                        if (player != null)
                        {
							player.stopSkillEffects(4515);
							if (ON_START_REMOVE_ALL_EFFECTS)
							{
								player.stopAllEffects();
							}
						}
                    }
                    AnnounceToPlayers(false, String.format(Message.getMessage(null, MessageId.MSG_EVT_10), getName()));
                    
					_remaining = _eventTime * 60000;
                    _eventTask.schedule(10000);
                }
            }, 60000);
        }
    };

    public final ExclusiveTask _eventTask = new ExclusiveTask()
    {
        @Override
        protected void onElapsed()
        {
            _remaining -= 10000;
            if (_remaining <= 0)
            {
                rewardPlayers();
                return;
            }
            _eventTask.schedule(10000);
        }
    };
    private L2PcInstance plk;
    private L2PcInstance pld;

    private class revivePlayer implements Runnable
    {
        L2Character _player;

        public revivePlayer(L2Character player)
        {
            _player = player;
        }

        @Override
        public void run()
        {
            if (_player != null)
            {
                int[] par =
                {
                    -1,
                    1
                };
                int Radius = 500;
				_player.teleToLocation(EVENT_LOCATION.getX() + (par[Rnd.get(2)] * Rnd.get(Radius)), EVENT_LOCATION.getY() + (par[Rnd.get(2)] * Rnd.get(Radius)), EVENT_LOCATION.getZ());
                _player.doRevive();
            }
        }
    }

    public void rewardPlayers()
    {
        L2PcInstance player = null;
        L2PcInstance winner = null;
        int top_score = 0;

		for (Integer playerId : _players.keySet())
        {
            player = L2World.getInstance().findPlayer(playerId);
            if (player != null)
            {
                player.abortAttack();
                player.abortCast();
                player.setTarget(null);
                player.getAI().setIntention(CtrlIntention.ACTIVE);

                if ((player.getDmKills() == top_score) && (top_score > 0))
                {
                    winner = null;
                }

                if (player.getDmKills() > top_score)
                {
                    winner = player;
                    top_score = player.getDmKills();
                }
            }
        }

        if ((winner != null) && (winner.getDmKills() > 0))
        {
			AnnounceToPlayers(true, String.format(Message.getMessage(null, MessageId.MSG_EVT_11), getName(), winner.getName(), winner.getDmKills()));
            for (int i = 0; i < _rewardId.length; i++)
            {
				winner.addItem("DeathMatch: Reward", _rewardId[i], _rewardAmount[i], null, true);
            }
        }
        else
        {
            AnnounceToPlayers(true, String.format(Message.getMessage(null, MessageId.MSG_EVT_8), getName()));
        }

        ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
        {
            @Override
            public void run()
            {
                finish();
            }
        }, 10000);

        player = null;
        winner = null;
    }

    public void run()
    {
        int realPlayers = 0;
        _playerLoc.clear();
        L2PcInstance player;
		for (Integer playerId : _players.keySet())
        {
            player = L2World.getInstance().findPlayer(playerId);
            if ((player != null) && (player.getLevel() >= _minLvl) && (player.getLevel() <= _maxLvl))
            {
                if (!DM_RETURNORIGINAL)
                {
					randomTeleport(player);
                }
                else
                {
                    _playerLoc.put(playerId, new Location(player.getLoc()));
                }
                realPlayers++;
            }
            else
            {
                if (player != null)
                {
                    player._event = null;
                }
                _players.remove(playerId);
            }
        }
        if (realPlayers < _minPlayers)
        {
            AnnounceToPlayers(true, String.format(Message.getMessage(null, MessageId.MSG_EVT_9), getName()));
            finish();
            return;
        }
		ThreadPoolManager.getInstance().scheduleGeneral(TeleportTask, 10000);
        _state = GameEvent.STATE_RUNNING;
        if (_eventScript != null)
        {
            _eventScript.onStart(_instanceId);
        }

    }

    private static void randomTeleport(L2PcInstance player)
    {
        int _locX, _locY, _locZ;
        int _Rnd = Rnd.get(100);

        if (_Rnd < 20)
        {
            _locX = 81260;
            _locY = 148607;
            _locZ = -3471;
        }
        else if (_Rnd < 40)
        {
            _locX = 147709;
            _locY = -53231;
            _locZ = -2732;
        }
        else if (_Rnd < 60)
        {
            _locX = 43429;
            _locY = -50913;
            _locZ = -796;
        }
        else if (_Rnd < 80)
        {
            _locX = 80523;
            _locY = 54741;
            _locZ = -1563;
        }
        else
        {
            _locX = 110745;
            _locY = 220618;
            _locZ = -3671;
        }
        player.teleToLocation(_locX, _locY, _locZ, false);
    }

    @Override
    public int getRegistredPlayersCount()
    {
        return _players.size();
    }
}