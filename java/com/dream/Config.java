package com.dream;

import com.dream.annotations.L2Properties;
import com.dream.game.geodata.GeoData.PathFindingMode;
import com.dream.game.handler.IReloadHandler;
import com.dream.game.handler.ReloadHandler;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.util.ClassMasterSettings;
import com.dream.util.StringUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import javolution.text.TypeFormat;

public class Config extends L2Config
{
	public static enum ChatMode
	{
		GLOBAL,
		REGION,
		GM,
		OFF
	}
	
	public static enum CorrectSpawnsZ
	{
		TOWN,
		MONSTER,
		ALL,
		NONE
	}
	
	public static enum IdFactoryType
	{
		Compaction,
		BitSet,
		Stack,
		Increment,
		Rebuild
	}
	
	public static enum ObjectMapType
	{
		L2ObjectHashMap,
		WorldObjectMap
	}
	
	public static enum ObjectSetType
	{
		L2ObjectHashSet,
		WorldObjectSet
	}
	
	public static boolean EFIREX_GAMESHARK = false;
	
	// Tournament Arena Interval
	public static int ARENA_DUEL_CALL_INTERVAL;
	public static int ARENA_DUEL_CHECK_INTERVAL;
	public static int ARENA_DUEL_WAIT_INTERVAL;
	// Tournament Items Restriction
	public static String ARENA_DUEL_RESTRICT;
	public static List<Integer> ARENA_DUEL_LIST_ITEMS_RESTRICT;
	public static boolean ARENA_DUEL_ALLOW_S;
	
	// Tournament hour 4x4 and 9x9
	public static int START_HOUR_A;
	public static int END_HOUR_A;
	
	public static int START_HOUR_B;
	public static int END_HOUR_B;
	
	// Tournament 1x1
	public static boolean ARENA_DUEL_1X1_ENABLED;
	public static int ARENA_DUEL_1X1_ARENA_COUNT;
	public static int[][] ARENA_DUEL_1X1_ARENA_LOCS;
	public static List<int[]> ARENA_DUEL_1X1_REWARD;
	
	// ANTIBOT CAPTCHA
	public static boolean BOTS_PREVENTION;
	public static int KILLS_COUNTER;
	public static int KILLS_COUNTER_RANDOMIZATION;
	public static int VALIDATION_TIME;
	public static int PUNISHMENT;
	public static int PUNISHMENT_TIME;
	public static boolean GAMEGUARD_ENFORCE = false;
	
	public static boolean ENABLE_EVENT_MANAGER;
	
	public static int EVENT_MANAGER_ID;
	
	public static boolean SPAWN_EVENT_MANAGER;
	
	public static int EVENT_PARTICIPATION_FEE_ID;
	
	public static int EVENT_PARTICIPATION_FEE_QNT;
	
	public static String EVENT_BLOCKED_CLASS;
	
	public static List<Integer> LIST_EVENT_BLOCKED_CLASSES = new ArrayList<>();
	
	public static String EVENT_BLOCKED_CLASS_NAMES;
	
	protected static Logger _log = Logger.getLogger(Config.class.getName());
	
	// Npc Properties
	public static boolean SHOW_NPC_LVL;
	
	public static boolean FREE_TELEPORT;
	public static int FREE_TELEPORT_MINLVL;
	public static int FREE_TELEPORT_MAXLVL;
	
	public static boolean NOBLE_FREE_TELEPORT;
	public static int NOBLE_FREE_TELEPORT_MINLVL;
	public static int NOBLE_FREE_TELEPORT_MAXLVL;
	
	public static int MAX_DRIFT_RANGE;
	
	public static int MIN_NPC_ANIMATION;
	
	public static int MAX_NPC_ANIMATION;
	
	public static int MIN_MONSTER_ANIMATION;
	
	public static int MAX_MONSTER_ANIMATION;
	
	public static int NPC_MIN_WALK_ANIMATION;
	
	public static int NPC_MAX_WALK_ANIMATION;
	
	public static boolean ALT_MOB_AGGRO_IN_PEACEZONE;
	
	public static boolean ALT_ATTACKABLE_NPCS;
	public static boolean ALLOW_PET_WALKERS;
	public static int ALT_URN_TEMP_FAIL;
	
	public static boolean ALLOW_RENTPET;
	
	public static boolean ALLOW_WYVERN_UPGRADER;
	
	public static int MANAGER_CRYSTAL_COUNT;
	
	public static int WYVERN_SPEED;
	
	public static int STRIDER_SPEED;
	
	// ClassMaster
	public static boolean ALT_CLASS_MASTER_STRIDER_UPDATE;
	
	public static String ALT_CLASS_MASTER_SETTINGS_LINE;
	
	public static ClassMasterSettings ALT_CLASS_MASTER_SETTINGS;
	
	public static boolean ALT_L2J_CLASS_MASTER;
	
	public static boolean ALT_CLASS_MASTER_ENTIRE_TREE;
	
	public static boolean ALT_CLASS_MASTER_TUTORIAL;
	
	public static boolean ALT_DISABLE_RAIDBOSS_PETRIFICATION;
	public static int MAX_LEVEL_RAID_CURSE;
	public static boolean FORCE_UPDATE_RAIDBOSS_ON_DB;
	public static long KICKTIMERESTART;
	// Player Properties
	public static boolean ALT_GAME_VIEWNPC;
	public static boolean ALT_GAME_SHOWPC_DROP;
	public static boolean ALLOW_EXCHANGE;
	public static int CRUMA_TOWER_LEVEL_RESTRICT;
	public static boolean ALT_STRICT_HERO_SYSTEM;
	public static boolean ENCHANT_HERO_WEAPONS;
	public static boolean HERO_LOG_NOCLAN;
	public static boolean ALT_GAME_DELEVEL;
	public static boolean ALT_GAME_CREATION;
	public static boolean IS_CRAFTING_ENABLED;
	public static boolean AUTO_LEARN_SKILLS;
	public static boolean ALT_GAME_SKILL_LEARN;
	public static boolean AUTO_LEARN_DIVINE_INSPIRATION;
	public static boolean ES_SP_BOOK_NEEDED;
	public static boolean ES_XP_NEEDED;
	public static boolean ES_SP_NEEDED;
	public static boolean SP_BOOK_NEEDED;
	public static boolean LIFE_CRYSTAL_NEEDED;
	public static boolean DIVINE_SP_BOOK_NEEDED;
	public static boolean ALT_ITEM_SKILLS_NOT_INFLUENCED;
	public static boolean CHECK_SKILLS_ON_ENTER;
	public static boolean CHECK_ADDITIONAL_SKILLS;
	public static boolean ACUMULATIVE_SUBCLASS_SKILLS;
	public static boolean ALT_GAME_SUBCLASS_WITHOUT_QUESTS;
	public static boolean ALT_GAME_SUBCLASS_EVERYWHERE;
	public static boolean SUBCLASS_WITH_ITEM_AND_NO_QUEST;
	public static boolean SUBCLASS_WITH_CUSTOM_ITEM;
	public static boolean LEVEL_ADD_LOAD;
	public static boolean ALT_GAME_MAGICFAILURES;
	public static boolean SHOW_SKILL_SUCCESS_CHANCE;
	public static boolean ALLOW_USE_EXP_SET;
	public static boolean ALLOW_MENU;
	public static boolean SHOW_DEBUFF_ONLY;
	public static boolean ALLOW_AUTO_LOOT;
	public static boolean AUTO_LOOT;
	public static boolean AUTO_LOOT_RAID;
	public static boolean AUTO_LOOT_HERBS;
	public static boolean AUTO_LOOT_ADENA;
	public static boolean DESTROY_PLAYER_INVENTORY_DROP;
	public static boolean ALLOW_DISCARDITEM;
	public static boolean ALLOW_KEYBOARD_MOVEMENT;
	public static boolean CHECK_PLAYER_MACRO;
	public static boolean CONSUME_SPIRIT_SOUL_SHOTS;
	public static boolean CONSUME_ARROWS;
	public static boolean KARMA_DROP_GM;
	public static boolean KARMA_AWARD_PK_KILL;
	public static boolean ALT_GAME_KARMA_PLAYER_CAN_BE_KILLED_IN_PEACEZONE;
	public static boolean ALT_GAME_KARMA_PLAYER_CAN_SHOP;
	public static boolean ALT_GAME_KARMA_PLAYER_CAN_TELEPORT;
	
	public static boolean ALT_GAME_KARMA_PLAYER_CAN_TRADE;
	public static boolean ALT_GAME_KARMA_PLAYER_CAN_USE_WAREHOUSE;
	public static boolean CURSED_WEAPON_NPC_INTERACT;
	public static boolean BLOCK_PARTY_INVITE_ON_COMBAT;
	public static boolean BLOCK_CHANGE_WEAPON_ON_ATTACK;
	public static boolean ALT_GAME_KARMA_PLAYER_CAN_USE_GK;
	public static int MAX_RUN_SPEED;
	public static int MAX_PATK_SPEED;
	public static int MAX_MATK_SPEED;
	public static int MAX_EVASION;
	public static int HIT_TIME_LIMITER;
	public static int COMMON_RECIPE_LIMIT;
	public static int DWARF_RECIPE_LIMIT;
	public static int AUTO_LEARN_MAX_LEVEL;
	public static int SEND_NOTDONE_SKILLS;
	public static int ALT_PCRITICAL_CAP;
	public static int ALT_MCRITICAL_CAP;
	public static int SUBCLASS_WITH_CUSTOM_ITEM_ID;
	public static int SUBCLASS_WITH_CUSTOM_ITEM_COUNT;
	public static int SUBCLASS_MAX_LEVEL;
	public static int PLAYER_MAX_LEVEL;
	public static int MAX_SUBCLASS;
	public static int PLAYER_SPAWN_PROTECTION;
	public static int PLAYER_FAKEDEATH_UP_PROTECTION;
	public static int DEATH_PENALTY_CHANCE;
	public static int INVENTORY_MAXIMUM_NO_DWARF;
	public static int INVENTORY_MAXIMUM_DWARF;
	public static int INVENTORY_MAXIMUM_GM;
	public static int ALT_INVENTORY_MAXIMUM_PET;
	public static int PVP_NORMAL_TIME;
	public static int PVP_PVP_TIME;
	public static int PVP_TIME;
	public static int KARMA_MIN_KARMA;
	
	public static int KARMA_MAX_KARMA;
	public static int KARMA_XP_DIVIDER;
	public static int KARMA_PK_LIMIT;
	public static int KARMA_LOST_BASE;
	public static float KARMA_RATE;
	public static float ALT_GAME_EXPONENT_XP;
	public static float ALT_GAME_EXPONENT_SP;
	
	public static float ALT_GAME_SUMMON_PENALTY_RATE;
	public static float ALT_LETHAL_RATE_DAGGER;
	public static float ALT_LETHAL_RATE_ARCHERY;
	public static float ALT_LETHAL_RATE_OTHER;
	
	public static double ALT_GAME_CREATION_SPEED;
	public static double ALT_GAME_CREATION_XP_RATE;
	public static double ALT_GAME_CREATION_SP_RATE;
	public static double ALT_WEIGHT_LIMIT;
	public static String PET_RENT_NPC;
	public static String KARMA_NON_DROPPABLE_PET_ITEMS;
	public static String KARMA_NON_DROPPABLE_ITEMS;
	public static List<Integer> LIST_PET_RENT_NPC = new ArrayList<>();
	public static List<String> LIST_MACRO_RESTRICTED_WORDS = new ArrayList<>();
	public static List<Integer> KARMA_LIST_NONDROPPABLE_PET_ITEMS = new ArrayList<>();
	public static List<Integer> KARMA_LIST_NONDROPPABLE_ITEMS = new ArrayList<>();
	
	public static byte BLOW_FRONT;
	public static byte BLOW_SIDE;
	public static byte BLOW_BEHIND;
	public static byte SUBCLASS_INIT_LEVEL;
	public static byte SUBCLASS_MAX_LEVEL_BYTE;
	public static byte PLAYER_MAX_LEVEL_BYTE;
	
	public static int CHANCE_LEVEL;
	
	public static int ALT_MOB_NOAGRO;
	public static int AUTODESTROY_ITEM_AFTER;
	public static int HERB_AUTO_DESTROY_TIME;
	public static int GRID_NEIGHBOR_TURNON_TIME;
	public static int GRID_NEIGHBOR_TURNOFF_TIME;
	public static int GRID_AUTO_DESTROY_ITEM_AFTER;
	public static int GRID_AUTO_DESTROY_HERB_TIME;
	public static int SAVE_DROPPED_ITEM_INTERVAL;
	public static boolean DESTROY_DROPPED_PLAYER_ITEM;
	public static boolean DESTROY_EQUIPABLE_PLAYER_ITEM;
	public static boolean SAVE_DROPPED_ITEM;
	public static boolean EMPTY_DROPPED_ITEM_TABLE_AFTER_LOAD;
	public static boolean CLEAR_DROPPED_ITEM_TABLE;
	
	public static boolean GRIDS_ALWAYS_ON;
	public static String PROTECTED_ITEMS;
	public static List<Integer> LIST_PROTECTED_ITEMS = new ArrayList<>();
	public static int ALT_PERFECT_SHLD_BLOCK;
	public static int ALT_BUFFER_HATE;
	public static int ALT_BUFFER_TIME;
	public static int ALT_DANCE_TIME;
	public static int ALT_SONG_TIME;
	public static int ALT_HERO_TIME;
	public static int ALT_5MIN_TIME;
	public static int ALT_CH_TIME;
	public static int BUFFS_MAX_AMOUNT;
	public static int ALT_MINIMUM_FALL_HEIGHT;
	public static float ALT_ATTACK_DELAY;
	public static boolean ALT_GAME_TIREDNESS;
	
	public static boolean ALT_GAME_SHIELD_BLOCKS;
	public static boolean ALT_GAME_CANCEL_BOW;
	public static boolean ALT_GAME_CANCEL_CAST;
	public static boolean FAIL_FAKEDEATH;
	public static boolean STORE_SKILL_COOLTIME;
	public static boolean EFFECT_CANCELING;
	public static boolean ALT_DANCE_MP_CONSUME;
	public static boolean GRADE_PENALTY;
	public static String UNAFFECTED_SKILLS;
	public static List<Integer> UNAFFECTED_SKILL_LIST = new ArrayList<>();
	public static boolean ENABLE_MODIFY_SKILL_DURATION;
	
	public static Map<Integer, Integer> SKILL_DURATION_LIST;
	public static String ALLOWED_SKILLS;
	public static List<Integer> ALLOWED_SKILLS_LIST = new ArrayList<>();
	public static boolean CANCEL_AUGUMENTATION_EFFECT;
	public static boolean CONSUME_ON_SUCCESS;
	public static boolean USE_STATIC_REUSE;
	public static boolean USE_OLY_STATIC_REUSE;
	public static int SKILL_DELAY;
	public static float MCRIT_RATE;
	public static boolean USE_LEVEL_PENALTY;
	public static boolean USE_CHAR_LEVEL_MOD;
	
	public static boolean DISABLE_SKILLS_ON_LEVEL_LOST;
	
	public static boolean OLD_CANCEL_MODE;
	public static String ANNOUNCE_MODE;
	public static boolean GM_ANNOUNCER_NAME;
	
	public static boolean JAIL_IS_PVP;
	public static String[] FORBIDDEN_NAMES;
	public static int KARMA_DROP_LIMIT;
	public static int KARMA_RATE_DROP;
	public static int KARMA_RATE_DROP_ITEM;
	public static int KARMA_RATE_DROP_EQUIP;
	public static int KARMA_RATE_DROP_EQUIP_WEAPON;
	public static float RATE_DROP_MANOR;
	public static float RATE_XP;
	public static float RATE_SP;
	public static float RATE_PARTY_XP;
	public static float RATE_PARTY_SP;
	
	public static boolean ALLOW_VIP_XPSP;
	public static float VIP_XP;
	public static float VIP_SP;
	public static float VIP_DROP_RATE;
	public static float VIP_SPOIL_RATE;
	
	public static float RATE_QUESTS_REWARD_EXPSP;
	public static float RATE_QUESTS_REWARD_ADENA;
	public static float RATE_QUESTS_REWARD_ITEMS;
	public static float RATE_RUN_SPEED;
	public static float RATE_DROP_ADENA;
	public static float RATE_CONSUMABLE_COST;
	public static float RATE_CRAFT_COST;
	public static float RATE_DROP_ITEMS;
	public static float RATE_DROP_SEAL_STONES;
	public static float RATE_DROP_SPOIL;
	public static float RATE_DROP_QUEST;
	public static int RATE_EXTR_FISH;
	public static float RATE_KARMA_EXP_LOST;
	public static float RATE_SIEGE_GUARDS_PRICE;
	public static float RATE_DROP_COMMON_HERBS;
	public static float RATE_DROP_MP_HP_HERBS;
	public static float RATE_DROP_GREATER_HERBS;
	public static float RATE_DROP_SUPERIOR_HERBS;
	public static float RATE_DROP_SPECIAL_HERBS;
	public static float PET_XP_RATE;
	public static float PET_FOOD_RATE;
	public static float SINEATER_XP_RATE;
	public static float RATE_DROP_ITEMS_BY_RAID;
	public static Map<Integer, Integer> NORMAL_WEAPON_ENCHANT_LEVEL = new HashMap<>();
	
	public static Map<Integer, Integer> BLESS_WEAPON_ENCHANT_LEVEL = new HashMap<>();
	public static Map<Integer, Integer> CRYTAL_WEAPON_ENCHANT_LEVEL = new HashMap<>();
	public static Map<Integer, Integer> NORMAL_ARMOR_ENCHANT_LEVEL = new HashMap<>();
	
	public static Map<Integer, Integer> BLESS_ARMOR_ENCHANT_LEVEL = new HashMap<>();
	public static Map<Integer, Integer> CRYSTAL_ARMOR_ENCHANT_LEVEL = new HashMap<>();
	public static Map<Integer, Integer> NORMAL_JEWELRY_ENCHANT_LEVEL = new HashMap<>();
	
	public static Map<Integer, Integer> BLESS_JEWELRY_ENCHANT_LEVEL = new HashMap<>();
	public static Map<Integer, Integer> CRYSTAL_JEWELRY_ENCHANT_LEVEL = new HashMap<>();
	public static boolean ALLOW_CRYSTAL_SCROLL;
	public static int ENCHANT_MAX_WEAPON_NORMAL;
	public static int ENCHANT_MAX_WEAPON_BLESSED;
	public static int ENCHANT_MAX_WEAPON_CRYSTAL;
	public static int ENCHANT_MAX_ARMOR_NORMAL;
	public static int ENCHANT_MAX_ARMOR_BLESSED;
	public static int ENCHANT_MAX_ARMOR_CRYSTAL;
	public static int ENCHANT_MAX_JEWELRY_NORMAL;
	public static int ENCHANT_MAX_JEWELRY_BLESSED;
	public static int ENCHANT_MAX_JEWELRY_CRYSTAL;
	public static int ENCHANT_OVER_CHANT_CHECK;
	public static int ENCHANT_SAFE_MAX;
	public static int ENCHANT_SAFE_MAX_FULL;
	public static int ENCHANT_DWARF_1_ENCHANTLEVEL;
	public static int ENCHANT_DWARF_2_ENCHANTLEVEL;
	public static int ENCHANT_DWARF_3_ENCHANTLEVEL;
	public static int ENCHANT_DWARF_1_CHANCE;
	public static int ENCHANT_DWARF_2_CHANCE;
	public static int ENCHANT_DWARF_3_CHANCE;
	public static boolean ENCHANT_DWARF_SYSTEM;
	public static boolean CHECK_ENCHANT_LEVEL_EQUIP;
	public static boolean AUGMENT_EXCLUDE_NOTDONE;
	public static int AUGMENTATION_NG_SKILL_CHANCE;
	public static int AUGMENTATION_NG_GLOW_CHANCE;
	public static int AUGMENTATION_MID_SKILL_CHANCE;
	public static int AUGMENTATION_MID_GLOW_CHANCE;
	public static int AUGMENTATION_HIGH_SKILL_CHANCE;
	public static int AUGMENTATION_HIGH_GLOW_CHANCE;
	public static int AUGMENTATION_TOP_SKILL_CHANCE;
	public static int AUGMENTATION_TOP_GLOW_CHANCE;
	public static int AUGMENTATION_BASESTAT_CHANCE;
	public static boolean ALT_FAILED_ENC_LEVEL;
	public static int ALT_MANOR_REFRESH_TIME;
	public static int ALT_MANOR_REFRESH_MIN;
	public static int ALT_MANOR_APPROVE_TIME;
	public static int ALT_MANOR_APPROVE_MIN;
	public static int ALT_MANOR_MAINTENANCE_PERIOD;
	public static int ALT_MANOR_SAVE_PERIOD_RATE;
	
	public static boolean ALLOW_MANOR;
	public static boolean ALT_MANOR_SAVE_ALL_ACTIONS;
	public static int MAX_PVTSTORESELL_SLOTS_DWARF;
	public static int MAX_PVTSTORESELL_SLOTS_OTHER;
	public static int MAX_PVTSTOREBUY_SLOTS_DWARF;
	
	public static int MAX_PVTSTOREBUY_SLOTS_OTHER;
	public static boolean CHECK_ZONE_ON_PVT;
	public static int WAREHOUSE_SLOTS_NO_DWARF;
	public static int WAREHOUSE_SLOTS_DWARF;
	public static int WAREHOUSE_SLOTS_CLAN;
	public static int FREIGHT_SLOTS;
	public static int ALT_GAME_FREIGHT_PRICE;
	public static boolean ALT_GAME_FREIGHTS;
	public static boolean ENABLE_WAREHOUSESORTING_CLAN;
	public static boolean ENABLE_WAREHOUSESORTING_PRIVATE;
	public static boolean ENABLE_WAREHOUSESORTING_FREIGHT;
	public static boolean ALLOW_WAREHOUSE;
	public static boolean ALLOW_FREIGHT;
	
	public static boolean WAREHOUSE_CACHE;
	public static int WAREHOUSE_CACHE_TIME;
	public static double NPC_HP_REGEN_MULTIPLIER;
	public static double NPC_MP_REGEN_MULTIPLIER;
	public static double PLAYER_CP_REGEN_MULTIPLIER;
	public static double PLAYER_HP_REGEN_MULTIPLIER;
	public static double PLAYER_MP_REGEN_MULTIPLIER;
	public static double RAID_HP_REGEN_MULTIPLIER;
	public static double RAID_MP_REGEN_MULTIPLIER;
	public static double RAID_PDEFENCE_MULTIPLIER;
	public static double RAID_MDEFENCE_MULTIPLIER;
	
	public static double PET_HP_REGEN_MULTIPLIER;
	
	public static double PET_MP_REGEN_MULTIPLIER;
	public static int REQUEST_ID;
	public static int MAX_CHARACTERS_NUMBER_PER_ACCOUNT;
	public static int MAXIMUM_ONLINE_USERS;
	public static int MIN_PROTOCOL_REVISION;
	public static int MAX_PROTOCOL_REVISION;
	public static boolean ACCEPT_ALTERNATE_ID;
	public static boolean SERVER_LIST_BRACKET;
	public static boolean SERVER_LIST_CLOCK;
	public static boolean SERVER_GMONLY;
	
	public static boolean ENCHANTROLLBACK;
	public static int ENCHANTROLLBACK_VALUE;
	
	public static File DATAPACK_ROOT;
	public static boolean COMPILE_SCRIPTS = true;
	public static int GENERAL_THREAD_POOL_SIZE = 5;
	public static int PACKET_THREAD_POOL_SIZE = 4;
	public static int EFFECT_THREAD_POOL_SIZE = 6;
	public static int AI_THREAD_POOL_SIZE = 6;
	public static int THREAD_POOL_SIZE;
	
	public static String SERVER_NAME;
	public static int MMO_SELECTOR_SLEEP_TIME = 26;
	public static int MMO_MAX_SEND_PER_PASS = 18;
	public static int MMO_MAX_READ_PER_PASS = 18;
	public static int MMO_HELPER_BUFFER_COUNT = 26;
	
	public static boolean MMO_TCP_NODELAY = false;
	
	public static boolean ENABLE_DDOS_PROTECTION = true;
	public static boolean BAN_CHAT_LOG;
	public static boolean BAN_ACCOUNT_LOG;
	public static boolean JAIL_LOG;
	public static boolean BAN_CHAR_LOG;
	public static boolean CLASSIC_ANNOUNCE_MODE;
	public static boolean ANNOUNCE_BAN_CHAT;
	public static boolean ANNOUNCE_UNBAN_CHAT;
	public static boolean ANNOUNCE_BAN_ACCOUNT;
	public static boolean ANNOUNCE_UNBAN_ACCOUNT;
	public static boolean ANNOUNCE_JAIL;
	public static boolean ANNOUNCE_UNJAIL;
	public static boolean ANNOUNCE_BAN_CHAR;
	public static boolean ANNOUNCE_UNBAN_CHAR;
	
	public static int GLOBAL_BAN_CHAT_TIME;
	public static boolean DATETIME_SAVECAL = true;
	public static int DATETIME_SUNRISE = 6;
	public static int DATETIME_SUNSET = 0;
	public static int DATETIME_MULTI = 10;
	public static int DATETIME_MOVE_DELAY = 180;
	public static String TIME_ZONE;
	
	public static boolean ADVANCED_DIAGONAL_STRATEGY = true;
	
	public static boolean ONLY_CLANLEADER_CAN_SIT_ON_THRONE;
	
	public static IdFactoryType IDFACTORY_TYPE;
	
	public static boolean BAD_ID_CHECKING;
	
	public static boolean ID_FACTORY_CLEANUP;
	public static int DEFAULT_PUNISH = 2;
	public static int SAFE_REBOOT_TIME = 10;
	public static boolean SAFE_REBOOT;
	public static boolean SAFE_REBOOT_DISABLE_ENCHANT;
	public static boolean SAFE_REBOOT_DISABLE_TELEPORT;
	public static boolean SAFE_REBOOT_DISABLE_CREATEITEM;
	public static boolean SAFE_REBOOT_DISABLE_TRANSACTION;
	public static boolean SAFE_REBOOT_DISABLE_PC_ITERACTION;
	public static boolean SAFE_REBOOT_DISABLE_NPC_ITERACTION;
	public static int DISCONNECTED_UNKNOWN_PACKET = 10;
	public static boolean Allow_Same_IP_On_Events = true;
	public static int ENCHAT_TIME = 1;
	public static int LIT_REGISTRATION_MODE;
	public static int LIT_REGISTRATION_TIME;
	
	public static int LIT_MIN_PARTY_CNT;
	public static int LIT_MAX_PARTY_CNT;
	public static int LIT_MIN_PLAYER_CNT;
	public static int LIT_MAX_PLAYER_CNT;
	public static int LIT_TIME_LIMIT;
	public static int FS_PARTY_MEMBER_COUNT;
	public static int RIFT_MIN_PARTY_SIZE;
	public static int RIFT_MAX_JUMPS;
	public static int RIFT_AUTO_JUMPS_TIME_MIN;
	public static int RIFT_AUTO_JUMPS_TIME_MAX;
	public static int RIFT_ENTER_COST_RECRUIT;
	public static int RIFT_ENTER_COST_SOLDIER;
	public static int RIFT_ENTER_COST_OFFICER;
	public static int RIFT_ENTER_COST_CAPTAIN;
	public static int RIFT_ENTER_COST_COMMANDER;
	public static int RIFT_ENTER_COST_HERO;
	public static int RIFT_SPAWN_DELAY;
	public static int HS_DEBUFF_CHANCE;
	public static float RIFT_BOSS_ROOM_TIME_MUTIPLY;
	public static int FOG_MOBS_CLONE_CHANCE;
	public static double RAID_MINION_RESPAWN_TIMER;
	public static double RESPAWN_RESTORE_CP;
	public static double RESPAWN_RESTORE_HP;
	public static double RESPAWN_RESTORE_MP;
	public static int RESPAWN_RANDOM_MAX_OFFSET;
	public static float RAID_MIN_RESPAWN_MULTIPLIER;
	public static float RAID_MAX_RESPAWN_MULTIPLIER;
	public static boolean RESPAWN_RANDOM_ENABLED;
	public static int ALT_DEFAULT_RESTARTTOWN;
	public static boolean MON_RESPAWN_RANDOM_ENABLED;
	public static int MON_RESPAWN_RANDOM_ZONE;
	public static long CS_TELE_FEE_RATIO;
	public static int CS_TELE1_FEE;
	public static int CS_TELE2_FEE;
	public static long CS_MPREG_FEE_RATIO;
	public static int CS_MPREG1_FEE;
	public static int CS_MPREG2_FEE;
	public static int CS_MPREG3_FEE;
	public static int CS_MPREG4_FEE;
	public static long CS_HPREG_FEE_RATIO;
	public static int CS_HPREG1_FEE;
	public static int CS_HPREG2_FEE;
	public static int CS_HPREG3_FEE;
	public static int CS_HPREG4_FEE;
	public static int CS_HPREG5_FEE;
	public static long CS_EXPREG_FEE_RATIO;
	public static int CS_EXPREG1_FEE;
	public static int CS_EXPREG2_FEE;
	public static int CS_EXPREG3_FEE;
	public static int CS_EXPREG4_FEE;
	public static long CS_SUPPORT_FEE_RATIO;
	public static int CS_SUPPORT1_FEE;
	public static int CS_SUPPORT2_FEE;
	public static int CS_SUPPORT3_FEE;
	public static int CS_SUPPORT4_FEE;
	public static long CH_TELE_FEE_RATIO;
	public static int CH_TELE1_FEE;
	
	public static int CH_TELE2_FEE;
	public static long CH_ITEM_FEE_RATIO;
	public static int CH_ITEM1_FEE;
	public static int CH_ITEM2_FEE;
	public static int CH_ITEM3_FEE;
	public static long CH_MPREG_FEE_RATIO;
	public static int CH_MPREG1_FEE;
	public static int CH_MPREG2_FEE;
	public static int CH_MPREG3_FEE;
	public static int CH_MPREG4_FEE;
	public static int CH_MPREG5_FEE;
	public static long CH_HPREG_FEE_RATIO;
	public static int CH_HPREG1_FEE;
	public static int CH_HPREG2_FEE;
	public static int CH_HPREG3_FEE;
	public static int CH_HPREG4_FEE;
	public static int CH_HPREG5_FEE;
	public static int CH_HPREG6_FEE;
	public static int CH_HPREG7_FEE;
	public static int CH_HPREG8_FEE;
	public static int CH_HPREG9_FEE;
	public static int CH_HPREG10_FEE;
	public static int CH_HPREG11_FEE;
	public static int CH_HPREG12_FEE;
	public static int CH_HPREG13_FEE;
	public static long CH_EXPREG_FEE_RATIO;
	public static int CH_EXPREG1_FEE;
	public static int CH_EXPREG2_FEE;
	public static int CH_EXPREG3_FEE;
	public static int CH_EXPREG4_FEE;
	public static int CH_EXPREG5_FEE;
	public static int CH_EXPREG6_FEE;
	public static int CH_EXPREG7_FEE;
	public static long CH_SUPPORT_FEE_RATIO;
	public static int CH_SUPPORT1_FEE;
	public static int CH_SUPPORT2_FEE;
	public static int CH_SUPPORT3_FEE;
	public static int CH_SUPPORT4_FEE;
	public static int CH_SUPPORT5_FEE;
	public static int CH_SUPPORT6_FEE;
	public static int CH_SUPPORT7_FEE;
	public static int CH_SUPPORT8_FEE;
	public static long CH_CURTAIN_FEE_RATIO;
	public static int CH_CURTAIN1_FEE;
	public static int CH_CURTAIN2_FEE;
	public static long CH_FRONT_FEE_RATIO;
	public static int CH_FRONT1_FEE;
	public static int CH_FRONT2_FEE;
	public static long FORT_TELE_FEE_RATIO;
	public static int FORT_TELE1_FEE;
	
	public static int FORT_TELE2_FEE;
	public static long FORT_MPREG_FEE_RATIO;
	public static int FORT_MPREG1_FEE;
	
	public static int FORT_MPREG2_FEE;
	public static long FORT_HPREG_FEE_RATIO;
	public static int FORT_HPREG1_FEE;
	
	public static int FORT_HPREG2_FEE;
	public static long FORT_EXPREG_FEE_RATIO;
	public static int FORT_EXPREG1_FEE;
	
	public static int FORT_EXPREG2_FEE;
	public static long FORT_SUPPORT_FEE_RATIO;
	public static int FORT_SUPPORT1_FEE;
	
	public static int FORT_SUPPORT2_FEE;
	/** Class Master */
	public static boolean SPAWN_CLASS_MASTER;
	public static String CLASS_MASTER_SETTINGS_LINE;
	
	public static ClassMasterSettings CLASS_MASTER_SETTINGS;
	public static boolean ALLOW_SAY_MSG_CLASS_MASSTER;
	public static boolean CLASS_MASTER_POPUP;
	public static boolean AUTODELETE_INVALID_QUEST_DATA;
	public static boolean LOG_ITEMS;
	
	public static String IGNORE_LOG;
	
	public static boolean FALLDOWNONDEATH;
	public static int COORD_SYNCHRONIZE;
	public static int DELETE_DAYS;
	public static int ZONE_TOWN;
	public static int CHAR_STORE_INTERVAL;
	public static boolean UPDATE_ITEMS_ON_CHAR_STORE;
	public static boolean LAZY_ITEMS_UPDATE;
	public static boolean RESTORE_PLAYER_INSTANCE;
	public static boolean L2OFF_ADENA_PROTECTION;
	public static boolean SET_ETCITEM_MAX_SELL;
	public static int SET_ETCITEM_MAX_SELL_VALUE;
	
	public static boolean ALLOW_SUMMON_TO_INSTANCE;
	public static boolean USE_BOW_CROSSBOW_DISTANCE_PENALTY;
	public static double BOW_CROSSBOW_DISTANCE_PENALTY;
	public static boolean GEODATA;
	public static boolean PATHFINDING;
	public static boolean FORCE_GEODATA = true;
	public static File GEODATA_ROOT;
	public static int PATH_LENGTH;
	public static int Z_DENSITY;
	public static PathFindingMode PATHFIND_MODE;
	public static String GEOENGINE;
	public static CorrectSpawnsZ GEO_CORRECT_Z;
	public static boolean ALLOW_CURSED_WEAPONS;
	public static boolean ALLOW_WEAR;
	
	public static boolean ALLOW_LOTTERY;
	
	public static boolean ALLOW_WATER;
	
	public static boolean ALLOW_BOAT;
	public static boolean ALLOW_GUARDS;
	public static boolean ALLOW_FISHING;
	public static boolean ALLOW_NPC_WALKERS;
	public static boolean RESET_TO_BASE;
	public static boolean ENABLE_RESTART;
	public static String RESTART_TIME;
	public static String RESTART_WARN_TIME;
	
	public static int MAX_ITEM_IN_PACKET;
	
	public static boolean MULTIPLE_ITEM_DROP;
	public static boolean PRECISE_DROP_CALCULATION;
	public static boolean DEEPBLUE_DROP_RULES;
	
	/** Community board */
	public static String COMMUNITY_TYPE;
	
	public static String BBS_DEFAULT;
	public static boolean SHOW_LEVEL_COMMUNITYBOARD;
	public static boolean SHOW_STATUS_COMMUNITYBOARD;
	
	public static int NAME_PAGE_SIZE_COMMUNITYBOARD;
	public static int NAME_PER_ROW_COMMUNITYBOARD;
	public static boolean ALLOW_CUSTOM_COMMUNITY;
	public static List<String> COMMUNITY_BUFFER_EXCLUDE_ON = new ArrayList<>();
	public static List<String> COMMUNITY_GATEKEEPER_EXCLUDE_ON = new ArrayList<>();
	public static boolean ONLINE_COMMUNITY_BOARD;
	public static boolean COLOR_COMMUNITY_BOARD;
	public static String BBS_RESTRICTIONS;
	public static List<String> BBS_DISABLED_PAGES = new ArrayList<>();
	public static boolean SHOW_LEGEND;
	public static boolean SHOW_KARMA_PLAYERS;
	public static boolean SHOW_JAILED_PLAYERS;
	public static boolean SHOW_CLAN_LEADER;
	public static int SHOW_CLAN_LEADER_CLAN_LEVEL;
	public static boolean MAIL_STORE_DELETED_LETTERS;
	public static int MIN_CLAN_LEVEL_FOR_USE_AUCTION;
	public static int ALT_CLAN_MEMBERS_FOR_WAR;
	public static int ALT_CLAN_WAR_PENALTY_WHEN_ENDED;
	public static int ALT_CLAN_JOIN_DAYS;
	
	public static int ALT_CLAN_CREATE_DAYS;
	public static int ALT_CLAN_DISSOLVE_DAYS;
	public static int ALT_ALLY_JOIN_DAYS_WHEN_LEAVED;
	public static int ALT_ALLY_JOIN_DAYS_WHEN_DISMISSED;
	public static int ALT_ACCEPT_CLAN_DAYS_WHEN_DISMISSED;
	public static int ALT_CREATE_ALLY_DAYS_WHEN_DISSOLVED;
	public static int ALT_MAX_NUM_OF_CLANS_IN_ALLY;
	public static int MINIMUN_LEVEL_FOR_PLEDGE_CREATION;
	public static boolean ALT_MEMBERS_CAN_WITHDRAW_FROM_CLANWH;
	public static boolean REMOVE_CASTLE_CIRCLETS;
	public static int ALT_REPUTATION_SCORE_PER_KILL;
	public static String ALT_REPUTATION_SCORE_PER_KILL_SM;
	
	public static int ALT_CLAN_LEADER_DATE_CHANGE;
	public static String ALT_CLAN_LEADER_HOUR_CHANGE;
	public static boolean ALT_CLAN_LEADER_INSTANT_ACTIVATION;
	
	public static int MAX_CLAN_MEMBERS_LVL0;
	public static int MAX_CLAN_MEMBERS_LVL1;
	public static int MAX_CLAN_MEMBERS_LVL2;
	public static int MAX_CLAN_MEMBERS_LVL3;
	public static int MAX_CLAN_MEMBERS_LVL4;
	public static int MAX_CLAN_MEMBERS_LVL5;
	public static int MAX_CLAN_MEMBERS_LVL6;
	public static int MAX_CLAN_MEMBERS_LVL7;
	public static int MAX_CLAN_MEMBERS_LVL8;
	public static int MAX_CLAN_MEMBERS_ROYALS;
	public static int MAX_CLAN_MEMBERS_KNIGHTS;
	
	public static double PARTY_XP_CUTOFF_PERCENT;
	public static int ALT_PARTY_RANGE;
	public static int ALT_PARTY_RANGE2;
	public static int MAX_PARTY_LEVEL_DIFFERENCE;
	
	public static int PARTY_XP_CUTOFF_LEVEL;
	public static boolean NO_PARTY_LEVEL_LIMIT;
	public static String PARTY_XP_CUTOFF_METHOD;
	public static int MAX_PETITIONS_PER_PLAYER;
	public static int MAX_PETITIONS_PENDING;
	public static boolean PETITIONING_ALLOWED;
	public static boolean PETITION_NEED_GM_ONLINE;
	
	public static boolean SEND_PAGE_ON_PETTITON;
	public static List<Pattern> FILTER_LIST = new ArrayList<>();
	public static ChatMode DEFAULT_GLOBAL_CHAT;
	public static ChatMode DEFAULT_TRADE_CHAT;
	public static boolean REGION_CHAT_ALSO_BLOCKED;
	
	public static int CHAT_LENGTH;
	
	public static boolean USE_SAY_FILTER;
	public static String CHAT_FILTER_CHARS;
	public static int KARMA_ON_OFFENSIVE;
	public static boolean LOG_CHAT;
	public static int GLOBAL_CHAT_TIME;
	public static int TRADE_CHAT_TIME;
	public static int HERO_CHAT_TIME;
	public static int SHOUT_CHAT_LEVEL;
	public static int TRADE_CHAT_LEVEL;
	public static boolean ALT_GAME_CASTLE_DAWN;
	public static boolean ALT_GAME_CASTLE_DUSK;
	public static boolean ALT_GAME_REQUIRE_CLAN_CASTLE;
	public static int ALT_FESTIVAL_MIN_PLAYER;
	public static int ALT_MAXIMUM_PLAYER_CONTRIB;
	
	public static long ALT_FESTIVAL_MANAGER_START;
	
	public static long ALT_FESTIVAL_LENGTH;
	public static long ALT_FESTIVAL_CYCLE_LENGTH;
	public static long ALT_FESTIVAL_FIRST_SPAWN;
	public static long ALT_FESTIVAL_FIRST_SWARM;
	public static long ALT_FESTIVAL_SECOND_SPAWN;
	public static long ALT_FESTIVAL_SECOND_SWARM;
	public static long ALT_FESTIVAL_CHEST_SPAWN;
	public static int ALT_FESTIVAL_ARCHER_AGGRO;
	public static int ALT_FESTIVAL_CHEST_AGGRO;
	public static int ALT_FESTIVAL_MONSTER_AGGRO;
	public static boolean ANNOUNCE_MAMMON_SPAWN;
	public static boolean ANNOUNCE_7S_AT_START_UP;
	public static boolean ALT_STRICT_SEVENSIGNS;
	public static int ALT_DAWN_JOIN_COST;
	public static double ALT_SIEGE_DAWN_GATES_PDEF_MULT;
	public static double ALT_SIEGE_DUSK_GATES_PDEF_MULT;
	public static double ALT_SIEGE_DAWN_GATES_MDEF_MULT;
	public static double ALT_SIEGE_DUSK_GATES_MDEF_MULT;
	public static boolean PC_CAFFE_ENABLED;
	public static int PC_CAFFE_MIN_LEVEL;
	public static int PC_CAFFE_MAX_LEVEL;
	public static int PC_CAFFE_MIN_SCORE;
	public static int PC_CAFFE_MAX_SCORE;
	public static int PC_CAFFE_INTERVAL;
	
	public static int ALT_LOTTERY_PRIZE;
	public static int ALT_LOTTERY_TICKET_PRICE;
	public static int ALT_LOTTERY_2_AND_1_NUMBER_PRIZE;
	public static float ALT_LOTTERY_5_NUMBER_RATE;
	public static float ALT_LOTTERY_4_NUMBER_RATE;
	public static float ALT_LOTTERY_3_NUMBER_RATE;
	
	public static int CHAMPION_FREQUENCY;
	public static int CHAMPION_HP;
	public static boolean CHAMPION_PASSIVE;
	public static String CHAMPION_TITLE;
	public static int CHAMPION_ADENA;
	public static int CHAMPION_REWARDS;
	
	public static int CHAMPION_EXP_SP;
	
	public static int CHAMPION_MIN_LEVEL;
	public static int CHAMPION_MAX_LEVEL;
	public static int CHAMPION_SPCL_CHANCE;
	public static int CHAMPION_SPCL_ITEM;
	public static int CHAMPION_SPCL_QTY;
	public static int CHAMPION_SPCL_LVL_DIFF;
	public static float CHAMPION_HP_REGEN;
	public static float CHAMPION_ATK;
	public static float CHAMPION_SPD_ATK;
	public static boolean CHAMPION_BOSS;
	public static boolean CHAMPION_MINIONS;
	public static boolean CHAMPION_ENABLE;
	public static boolean EVENT_MESSAGE;
	public static boolean STAR_DROP;
	public static boolean STAR_SPAWN;
	public static int STAR_CHANCE1;
	public static int STAR_CHANCE2;
	public static int STAR_CHANCE3;
	
	public static boolean MEDAL_DROP;
	public static boolean MEDAL_SPAWN;
	public static int MEDAL_CHANCE1;
	public static int MEDAL_CHANCE2;
	public static boolean CRISTMAS_DROP;
	public static boolean CRISTMAS_SPAWN;
	public static int CRISTMAS_CHANCE;
	public static int CRISTMAS_TREE_TIME;
	
	public static boolean L2DAY_DROP;
	public static boolean L2DAY_SPAWN;
	public static int L2DAY_CHANCE;
	public static int L2DAY_SCROLLCHANCE;
	public static int L2DAY_ENCHSCROLLCHANCE;
	public static int L2DAY_ACCCHANCE;
	public static String L2DAY_REWARD;
	public static String L2DAY_ACCESSORIE;
	public static String L2DAY_SCROLL;
	
	public static boolean L2DROPDAY_ENABLE;
	public static int L2DROPDAY_ITEM1;
	public static int L2DROPDAY_CHANCE1;
	public static int L2DROPDAY_ITEM2;
	public static int L2DROPDAY_CHANCE2;
	public static int L2DROPDAY_ITEM3;
	public static int L2DROPDAY_CHANCE3;
	public static int L2DROPDAY_ITEM4;
	public static int L2DROPDAY_CHANCE4;
	public static int L2DROPDAY_ITEM5;
	public static int L2DROPDAY_CHANCE5;
	public static int L2DROPDAY_ITEM6;
	public static int L2DROPDAY_CHANCE6;
	public static int L2DROPDAY_ITEM7;
	public static int L2DROPDAY_CHANCE7;
	public static int L2DROPDAY_ITEM8;
	public static int L2DROPDAY_CHANCE8;
	public static int L2DROPDAY_ITEM9;
	public static int L2DROPDAY_CHANCE9;
	public static int L2DROPDAY_ITEM10;
	public static int L2DROPDAY_CHANCE10;
	public static int L2DROPDAY_ITEM11;
	public static int L2DROPDAY_CHANCE11;
	public static int L2DROPDAY_ITEM12;
	public static int L2DROPDAY_CHANCE12;
	public static int L2DROPDAY_ITEM13;
	public static int L2DROPDAY_CHANCE13;
	public static int L2DROPDAY_ITEM14;
	public static int L2DROPDAY_CHANCE14;
	public static int L2DROPDAY_ITEM15;
	public static int L2DROPDAY_CHANCE15;
	public static int L2DROPDAY_ITEM16;
	public static int L2DROPDAY_CHANCE16;
	public static int L2DROPDAY_ITEM17;
	public static int L2DROPDAY_CHANCE17;
	public static int L2DROPDAY_ITEM18;
	public static int L2DROPDAY_CHANCE18;
	public static int L2DROPDAY_ITEM19;
	public static int L2DROPDAY_CHANCE19;
	public static int L2DROPDAY_ITEM20;
	public static int L2DROPDAY_CHANCE20;
	public static int L2DROPDAY_ITEM21;
	public static int L2DROPDAY_CHANCE21;
	public static int L2DROPDAY_ITEM22;
	public static int L2DROPDAY_CHANCE22;
	public static int L2DROPDAY_ITEM23;
	public static int L2DROPDAY_CHANCE23;
	public static int L2DROPDAY_ITEM24;
	public static int L2DROPDAY_CHANCE24;
	public static int L2DROPDAY_ITEM25;
	public static int L2DROPDAY_CHANCE25;
	public static int L2DROPDAY_ITEM26;
	public static int L2DROPDAY_CHANCE26;
	
	public static int BIGSQUASH_CHANCE;
	public static boolean BIGSQUASH_SPAWN;
	public static boolean BIGSQUASH_DROP;
	public static boolean BIGSQUASH_USE_SEEDS;
	public static boolean FISHERMAN_ENABLED;
	public static int FISHERMAN_INTERVAL;
	
	public static int FISHERMAN_REWARD_ID;
	public static int FISHERMAN_REWARD_COUNT;
	public static boolean ALLOW_WEDDING;
	public static boolean SPAWN_WEDDING_NPC;
	public static int WEDDING_PRICE;
	public static int WEDDING_TELEPORT_PRICE;
	public static int WEDDING_TELEPORT_INTERVAL;
	public static int WEDDING_DIVORCE_COSTS;
	public static boolean WEDDING_PUNISH_INFIDELITY;
	public static boolean WEDDING_TELEPORT;
	public static boolean WEDDING_SAMESEX;
	public static boolean WEDDING_FORMALWEAR;
	public static boolean WEDDING_GIVE_CUPID_BOW;
	
	public static boolean WEDDING_HONEYMOON_PORT;
	public static int WEDDING_PORT_X;
	public static int WEDDING_PORT_Y;
	public static int WEDDING_PORT_Z;
	public static boolean WEDDING_USE_COLOR;
	public static int WEDDING_NORMAL;
	public static int WEDDING_GAY;
	public static int WEDDING_LESBI;
	public static int SIEGE_MAX_ATTACKER;
	public static int SIEGE_MAX_DEFENDER;
	public static int SIEGE_BLOODALIANCE_REWARD_CNT;
	public static int SIEGE_RESPAWN_DELAY_ATTACKER;
	public static int SIEGE_FLAG_MAX_COUNT;
	public static int SIEGE_CLAN_MIN_LEVEL;
	
	public static int SIEGE_LENGTH_MINUTES;
	
	public static int SIEGE_CLAN_MIN_MEMBERCOUNT;
	public static boolean SPAWN_SIEGE_GUARD;
	public static boolean SIEGE_ONLY_REGISTERED;
	public static boolean ALT_FLYING_WYVERN_IN_SIEGE;
	public static boolean SIEGE_GATE_CONTROL;
	public static boolean CHANGE_SIEGE_TIME_IS_DISABLES;
	public static boolean CORECT_SIEGE_DATE_BY_7S;
	public static List<String> CL_SET_SIEGE_TIME_LIST;
	public static List<Integer> SIEGE_HOUR_LIST_MORNING;
	public static List<Integer> SIEGE_HOUR_LIST_AFTERNOON;
	public static int MAX_GUARD_COUNT_FOR_CASTLE;
	public static int CASTLE_REWARD_ID;
	public static int CASTLE_REWARD_COUNT;
	public static int FORTSIEGE_MAX_ATTACKER;
	public static int FORTSIEGE_FLAG_MAX_COUNT;
	public static int FORTSIEGE_CLAN_MIN_LEVEL;
	public static int FORTSIEGE_LENGTH_MINUTES;
	public static int FORTSIEGE_COUNTDOWN_LENGTH;
	public static int FORTSIEGE_MERCHANT_DELAY;
	public static int FORTSIEGE_COMBAT_FLAG_ID;
	
	public static int FORTSIEGE_REWARD_ID;
	public static int FORTSIEGE_REWARD_COUNT;
	public static boolean LOAD_AUTOANNOUNCE_AT_STARTUP = true;
	public static int WEAR_DELAY;
	public static int WEAR_PRICE;
	public static boolean ALT_PLAYER_CAN_DROP_ADENA;
	public static boolean SHOW_HTML_WELCOME;
	public static boolean ALT_RECOMMEND;
	public static boolean ALT_BLACKSMITH_USE_RECIPES;
	
	public static boolean LOAD_CUSTOM_TELEPORTS;
	
	public static int PET_TICKET_ID;
	
	public static int SPECIAL_PET_TICKET_ID;
	public static int PLAYER_RATE_DROP_ADENA;
	
	public static boolean ALT_MANA_POTIONS;
	public static int MANAHEAL_POWER;
	
	public static boolean ALLOW_NEW_CHAR_CUSTOM_POSITION;
	public static int NEW_CHAR_POSITION_X;
	
	public static int NEW_CHAR_POSITION_Y;
	public static int NEW_CHAR_POSITION_Z;
	public static boolean ENABLE_STARTUP_LVL;
	public static int ADD_LVL_NEWBIE;
	
	public static int STARTING_AA;
	public static int STARTING_ADENA;
	public static boolean ALLOW_CUSTOM_ITEM_TABLE;
	public static boolean ALLOW_CUSTOM_NPC_TABLE;
	
	public static boolean ALLOW_CUSTOM_ARMORSET_TABLE;
	public static boolean ALLOW_CUSTOM_DROPLIST_TABLE;
	public static boolean ALLOW_CUSTOM_SPAWNLIST_TABLE;
	public static int AUCTION_ITEM_ID;
	
	public static int RAID_MIN_MP_TO_CAST;
	public static boolean EPIC_REQUIRE_QUEST;
	public static boolean BANKING_ENABLED;
	public static int BANKING_GOLDBAR_PRICE;
	
	public static int BANKING_GOLDBAR_ID;
	public static byte[] HEX_ID;
	public static int SERVER_ID;
	
	public static int ALT_OLY_START_TIME;
	public static int ALT_OLY_MIN;
	public static int ALT_OLY_CPERIOD;
	
	public static int ALT_OLY_BATTLE;
	public static int ALT_OLY_WPERIOD;
	
	public static int ALT_OLY_VPERIOD;
	
	public static boolean ALT_OLY_ALLOW_BSS;
	public static boolean ALT_OLY_ENABLED;
	public static boolean ALT_OLY_SAME_IP;
	public static int ALT_OLY_CLASSED;
	public static int ALT_OLY_BATTLE_REWARD_ITEM;
	public static int ALT_OLY_CLASSED_RITEM_C;
	public static int ALT_OLY_NONCLASSED_RITEM_C;
	public static int ALT_OLY_GP_PER_POINT;
	public static int ALT_OLY_MIN_POINT_FOR_EXCH;
	public static int ALT_OLY_HERO_POINTS;
	public static List<Integer> LIST_OLY_RESTRICTED_ITEMS = new ArrayList<>();
	public static int ALT_OLY_NONCLASSED;
	public static boolean ALT_OLY_MATCH_HEAL_COUNTS;
	public static boolean ALT_OLY_REMOVE_CUBICS;
	public static boolean ALT_OLY_LOG_FIGHTS;
	public static boolean ALT_OLY_SHOW_MONTHLY_WINNERS;
	public static int ALT_OLY_ENCHANT_LIMIT;
	public static boolean ALT_OLY_RESTORE_PLAYER_HP_CP_MP_ON_TELEPORT;
	public static boolean ALT_OLY_RESTORE_PLAYER_HP_CP_MP_ON_FIGHT_START;
	public static List<Integer> LIST_OLY_RESTRICTED_SKILLS = new ArrayList<>();
	public static boolean ALT_OLY_RESET_SKILL_TIME;
	public static boolean ALT_OLY_REMOVE_POINTS_ON_TIE;
	public static int ALT_OLY_START_PCOUNT;
	public static int ALT_OLY_WEEKLY_PCOUNT;
	public static String ALT_OLY_DURATION_TYPES;
	public static int ALT_OLY_DURATION;
	public static boolean ALT_OLY_INCLUDE_SUMMON_DAMAGE;
	public static int GAME_SERVER_LOGIN_PORT;
	public static int PORT_GAME;
	public static String GAMESERVER_HOSTNAME;
	public static String GAME_SERVER_LOGIN_HOST;
	public static String INTERNAL_HOSTNAME;
	public static String EXTERNAL_HOSTNAME;
	public static int DATABASE_MAX_CONNECTIONS;
	public static String DATABASE_DRIVER;
	public static String DATABASE_URL;
	public static String DATABASE_LOGIN;
	public static boolean USE_DATABASE_LAYER = true;
	public static int DATABASE_MAX_IDLE_TIMEOUT = 600;
	public static int DATABASE_IDLE_TEST_PERIOD = 60;
	
	public static String DATABASE_PASSWORD;
	
	public static boolean ASSERT = false;
	public static boolean DEVELOPER = false;
	public static boolean DEBUG = false;
	public static boolean SEND_PACKET_LOG = false;
	public static boolean RECIVE_PACKET_LOG = false;
	
	public static boolean ALT_DEV_NO_QUESTS = false;
	public static boolean ALT_DEV_NO_SPAWNS = false;
	
	public static boolean SHOW_NOT_REG_QUEST = true;
	public static boolean SERVER_LIST_TESTSERVER = false;
	public static int DEADLOCKCHECK_INTERVAL = 10000;
	public static boolean GM_STARTUP_INVISIBLE;
	
	public static boolean GM_STARTUP_INVULNERABLE;
	
	public static boolean GM_STARTUP_SILENCE;
	public static boolean GM_STARTUP_AUTO_LIST;
	public static boolean SHOW_GM_LOGIN;
	public static boolean GM_ITEM_RESTRICTION;
	public static int GM_MAX_ENCHANT;
	public static int STANDARD_RESPAWN_DELAY;
	public static boolean GM_AUDIT;
	public static boolean SHOW_HTML_CHAT;
	public static int GM_NAME_COLOR;
	public static int GM_TITLE_COLOR;
	
	public static boolean ALLOW_OFFLINE_TRADE;
	public static boolean ALLOW_OFFLINE_TRADE_CRAFT;
	public static boolean ALLOW_OFFLINE_TRADE_COLOR_NAME;
	public static int OFFLINE_TRADE_COLOR_NAME;
	public static boolean ALLOW_OFFLINE_TRADE_PROTECTION;
	public static int OFFLINE_TRADE_PRICE_ITEM_ID;
	public static int OFFLINE_TRADE_PRICE_ITEM_ID_TIME;
	public static int OFFLINE_TRADE_PRICE_COUNT;
	public static int OFFLINE_CRAFT_PRICE_ITEM_ID;
	public static int OFFLINE_CRAFT_PRICE_ITEM_ID_TIME;
	public static int OFFLINE_CRAFT_PRICE_COUNT;
	public static boolean RESTORE_OFFLINE_TRADERS;
	
	public static int ALLOW_OFFLINE_HOUR;
	
	public static boolean SHOW_NPC_CREST;
	public static boolean CUSTOM_CHAR_TITLE;
	public static String CUSTOM_CHAR_TITLE_TEXT;
	public static int TITLE_COLOR;
	public static int COLOR_FOR_AMMOUNT1;
	public static int COLOR_FOR_AMMOUNT2;
	public static int COLOR_FOR_AMMOUNT3;
	public static int COLOR_FOR_AMMOUNT4;
	public static int COLOR_FOR_AMMOUNT5;
	public static int PVP_AMMOUNT1;
	public static int PVP_AMMOUNT2;
	public static int PVP_AMMOUNT3;
	
	public static int PVP_AMMOUNT4;
	public static int PVP_AMMOUNT5;
	public static boolean PVP_COLOR_SYSTEM;
	public static int PVP_COLOR_MODE;
	public static int TITLE_COLOR_FOR_AMMOUNT1;
	
	public static int TITLE_COLOR_FOR_AMMOUNT2;
	public static int TITLE_COLOR_FOR_AMMOUNT3;
	public static int TITLE_COLOR_FOR_AMMOUNT4;
	public static int TITLE_COLOR_FOR_AMMOUNT5;
	public static final int PVP_MODE_TITLE = 1;
	public static final int PVP_MODE_NAME = 2;
	
	public static final int PVP_MODE_BOTH = 3;
	public static boolean VOICED_HELP;
	public static boolean VOICED_OFFLINE;
	public static boolean VOICED_WEDDING;
	public static boolean VOICED_BANK;
	public static boolean VOICED_CONFIGURATOR;
	public static boolean VOICED_CLASS_MASTER;
	public static boolean VOICED_AIOX_COMMAND;
	public static boolean VOICED_AUTOFARM_COMMAND;
	/** Server version */
	public static String SERVER_VERSION;
	/** Date of server build */
	public static String SERVER_BUILD_DATE;
	
	public static boolean ENABLE_REWARDSKILL_TIME;
	public static int REWARDSKILL_TIME_TASK;
	public static int REWARDSKILL_TIME_SKILL_ID;
	public static int REWARDSKILL_TIME_SKILL_MAXLVL;
	public static boolean ENABLE_FIRSTLOGIN_REWARD;
	public static boolean ENABLE_REWARDITEM_BYTIME;
	public static int REWARDITEM_BYTIME_MIN;
	public static int[][] REWARDITEM_BYTIME_ITEM;
	public static int[][] FIRSTLOGIN_REWARD_ITEM;
	
	public static boolean FIRSTLOGIN_BUFFS;
	public static String FIRSTLOGIN_FIGHTER_BUFF;
	public static ArrayList<Integer> FIRSTLOGIN_FIGHTER_BUFF_LIST = new ArrayList<>();
	public static String FIRSTLOGIN_MAGE_BUFF;
	public static ArrayList<Integer> FIRSTLOGIN_MAGE_BUFF_LIST = new ArrayList<>();
	
	// Startup
	public static boolean STARTUP_SYSTEM_ENABLED;
	public static boolean DISABLE_TUTORIAL;
	public static boolean STARTUP_SYSTEM_SELECTCLASS;
	public static boolean STARTUP_SYSTEM_SELECTARMOR;
	public static boolean STARTUP_SYSTEM_SELECTWEAP;
	public static boolean STARTUP_SYSTEM_BUFF_MAGE;
	public static boolean STARTUP_SYSTEM_BUFF_FIGHT;
	public static String FIGHTER_BUFF;
	public static ArrayList<Integer> FIGHTER_BUFF_LIST = new ArrayList<>();
	public static String MAGE_BUFF;
	public static ArrayList<Integer> MAGE_BUFF_LIST = new ArrayList<>();
	
	public static String BYBASS_HEAVY_ITEMS;
	public static String BYBASS_LIGHT_ITEMS;
	public static String BYBASS_ROBE_ITEMS;
	
	public static List<int[]> SET_HEAVY_ITEMS = new ArrayList<>();
	public static int[] SET_HEAVY_ITEMS_LIST;
	
	public static List<int[]> SET_LIGHT_ITEMS = new ArrayList<>();
	public static int[] SET_LIGHT_ITEMS_LIST;
	
	public static List<int[]> SET_ROBE_ITEMS = new ArrayList<>();
	public static int[] SET_ROBE_ITEMS_LIST;
	
	public static String BYBASS_WP_01_ITEM;
	public static String BYBASS_WP_02_ITEM;
	public static String BYBASS_WP_03_ITEM;
	public static String BYBASS_WP_04_ITEM;
	public static String BYBASS_WP_05_ITEM;
	public static String BYBASS_WP_06_ITEM;
	public static String BYBASS_WP_07_ITEM;
	public static String BYBASS_WP_08_ITEM;
	public static String BYBASS_WP_09_ITEM;
	public static String BYBASS_WP_10_ITEM;
	public static String BYBASS_WP_11_ITEM;
	public static String BYBASS_WP_12_ITEM;
	public static String BYBASS_WP_13_ITEM;
	public static String BYBASS_WP_14_ITEM;
	public static String BYBASS_WP_15_ITEM;
	public static String BYBASS_WP_16_ITEM;
	public static String BYBASS_WP_17_ITEM;
	public static String BYBASS_WP_18_ITEM;
	public static String BYBASS_WP_19_ITEM;
	public static String BYBASS_WP_20_ITEM;
	public static String BYBASS_WP_21_ITEM;
	public static String BYBASS_WP_22_ITEM;
	public static String BYBASS_WP_23_ITEM;
	public static String BYBASS_WP_24_ITEM;
	public static String BYBASS_WP_25_ITEM;
	public static String BYBASS_WP_26_ITEM;
	public static String BYBASS_WP_27_ITEM;
	public static String BYBASS_WP_28_ITEM;
	public static String BYBASS_WP_29_ITEM;
	public static String BYBASS_WP_30_ITEM;
	public static String BYBASS_WP_31_ITEM;
	public static String BYBASS_WP_SHIELD;
	public static String BYBASS_ARROW;
	public static int WP_01_ID;
	public static int WP_02_ID;
	public static int WP_03_ID;
	public static int WP_04_ID;
	public static int WP_05_ID;
	public static int WP_06_ID;
	public static int WP_07_ID;
	public static int WP_08_ID;
	public static int WP_09_ID;
	public static int WP_10_ID;
	public static int WP_11_ID;
	public static int WP_12_ID;
	public static int WP_13_ID;
	public static int WP_14_ID;
	public static int WP_15_ID;
	public static int WP_16_ID;
	public static int WP_17_ID;
	public static int WP_18_ID;
	public static int WP_19_ID;
	public static int WP_20_ID;
	public static int WP_21_ID;
	public static int WP_22_ID;
	public static int WP_23_ID;
	public static int WP_24_ID;
	public static int WP_25_ID;
	public static int WP_26_ID;
	public static int WP_27_ID;
	public static int WP_28_ID;
	public static int WP_29_ID;
	public static int WP_30_ID;
	public static int WP_31_ID;
	public static int WP_ARROW;
	public static int WP_SHIELD;
	
	private static IReloadHandler _reloadAll = new IReloadHandler()
	{
		@Override
		public void reload(L2PcInstance actor)
		{
			loadMainConfig();
		}
		
	};
	public static float LOW_WEIGHT = 0.5f;
	public static float MEDIUM_WEIGHT = 2.0f;
	public static float HIGH_WEIGHT = 3.0f;
	public static float DIAGONAL_WEIGHT = 0.707f;
	public static int MAX_POSTFILTER_PASSES = 3;
	public static String PATHFIND_BUFFERS = "100x6;128x6;192x6;256x4;320x4;384x4;500x2";
	public static int WORLD_X_MIN = 15;
	public static int WORLD_X_MAX = 26;
	public static byte WORLD_Y_MIN = 10;
	public static byte WORLD_Y_MAX = 26;
	public static int INTEREST_MAX_THREAD = 10;
	public static long PROTECT_COMPRESSION_WRITEDELAY = 2L;
	public static String GEOFILES_PATTERN = "(\\d{2}_\\d{2})\\.l2j";
	
	public static int CLIENT_PACKET_QUEUE_SIZE = 18;
	public static int CLIENT_PACKET_QUEUE_MAX_BURST_SIZE = 16;
	public static int CLIENT_PACKET_QUEUE_MAX_PACKETS_PER_SECOND = 120;
	public static int CLIENT_PACKET_QUEUE_MEASURE_INTERVAL = 5;
	public static int CLIENT_PACKET_QUEUE_MAX_AVERAGE_PACKETS_PER_SECOND = 50;
	public static int CLIENT_PACKET_QUEUE_MAX_FLOODS_PER_MIN = 2;
	public static int CLIENT_PACKET_QUEUE_MAX_OVERFLOWS_PER_MIN = 1;
	public static int CLIENT_PACKET_QUEUE_MAX_UNDERFLOWS_PER_MIN = 1;
	public static int CLIENT_PACKET_QUEUE_MAX_UNKNOWN_PER_MIN = 5;
	public static int THREAD_P_EFFECTS = 12;
	public static int THREAD_P_GENERAL = 15;
	public static int IO_PACKET_THREAD_CORE_SIZE = 10;
	public static int GENERAL_PACKET_THREAD_CORE_SIZE = 20;
	public static int GENERAL_THREAD_CORE_SIZE = 20;
	public static int AI_MAX_THREAD = 6;
	
	public static boolean RESERVE_HOST_ON_LOGIN = false;
	
	public static long AUTOSAVE_INITIAL_TIME = 250000;
	
	public static long AUTOSAVE_DELAY_TIME = 700000;
	
	public static void loadAdministrationConfig()
	{
		loadGmAccess();
	}
	
	public static void loadAll()
	{
		loadVersion();
		
		loadMods();
		loadOllyConfig();
		loadArenaDuelConfig();
		loadNetworkConfiguration();
		loadMainConfig();
		loadEventsConfig();
		loadAdministrationConfig();
	}
	
	public static void loadArenaDuelConfig()
	{
		try
		{
			Properties p = new L2Properties(ConfigFiles.ARENA_DUEL_FILE);
			
			// Tournament time check
			ARENA_DUEL_CHECK_INTERVAL = Integer.parseInt(p.getProperty("ArenaDuelBattleCheckInterval", "15")) * 1000;
			ARENA_DUEL_CALL_INTERVAL = Integer.parseInt(p.getProperty("ArenaDuelBattleCallInterval", "60")) * 1000;
			ARENA_DUEL_WAIT_INTERVAL = Integer.parseInt(p.getProperty("ArenaDuelBattleWaitInterval", "20")) * 1000;
			
			// Tournament Items Restriction
			ARENA_DUEL_ALLOW_S = Boolean.parseBoolean(p.getProperty("ArenaAllowS", "false"));
			ARENA_DUEL_RESTRICT = p.getProperty("ArenaDuelItemsRestriction");
			ARENA_DUEL_LIST_ITEMS_RESTRICT = new ArrayList<>();
			for (String id : ARENA_DUEL_RESTRICT.split(","))
				ARENA_DUEL_LIST_ITEMS_RESTRICT.add(Integer.parseInt(id));
			
			// 1x1
			ARENA_DUEL_1X1_ENABLED = Boolean.parseBoolean(p.getProperty("ArenaDuelEnable", "false"));
			if (ARENA_DUEL_1X1_ENABLED)
			{
				String[] arenaLocs = p.getProperty("ArenaDuelLoc", "").split(";");
				String[] locSplit = null;
				
				ARENA_DUEL_1X1_ARENA_COUNT = arenaLocs.length;
				ARENA_DUEL_1X1_ARENA_LOCS = new int[ARENA_DUEL_1X1_ARENA_COUNT][3];
				for (int i = 0; i < ARENA_DUEL_1X1_ARENA_COUNT; i++)
				{
					locSplit = arenaLocs[i].split(",");
					for (int j = 0; j < 3; j++)
					{
						ARENA_DUEL_1X1_ARENA_LOCS[i][j] = Integer.parseInt(locSplit[j].trim());
					}
				}
				
				ARENA_DUEL_1X1_REWARD = new ArrayList<>();
				String[] tournamentReward = p.getProperty("ArenaDuelReward", "57,100000").split(";");
				for (String reward : tournamentReward)
				{
					String[] rewardSplit = reward.split(",");
					if (rewardSplit.length != 2)
					{
						_log.error(StringUtil.concat("ArenaDuelReward: invalid config property -> ArenaDuelReward \"", reward, "\""));
					}
					else
					{
						try
						{
							ARENA_DUEL_1X1_REWARD.add(new int[]
							{
								Integer.parseInt(rewardSplit[0]),
								Integer.parseInt(rewardSplit[1])
							});
						}
						catch (NumberFormatException nfe)
						{
							if (!reward.isEmpty())
							{
								_log.error(StringUtil.concat("ArenaDuelReward: invalid config property -> ArenaDuelReward \"", reward, "\""));
							}
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			_log.error(e.getMessage(), e);
			throw new Error("Failed to Load " + ConfigFiles.ARENA_DUEL_FILE + " File.");
		}
	}
	
	public static void loadAltConfig()
	{
		try
		{
			Properties p = new L2Properties(ConfigFiles.ALT_FILE);
			
			ANNOUNCE_MODE = p.getProperty("AnnounceMode", "l2j").toLowerCase();
			GM_ANNOUNCER_NAME = Boolean.parseBoolean(p.getProperty("AnnounceGMName", "false"));
			
			ANNOUNCE_MODE = p.getProperty("AnnounceMode", "l2j").toLowerCase();
			GM_ANNOUNCER_NAME = Boolean.parseBoolean(p.getProperty("AnnounceGMName", "false"));
			
			ANNOUNCE_MODE = p.getProperty("AnnounceMode", "l2j").toLowerCase();
			GM_ANNOUNCER_NAME = Boolean.parseBoolean(p.getProperty("AnnounceGMName", "false"));
			
			ALT_GAME_TIREDNESS = Boolean.parseBoolean(p.getProperty("AltGameTiredness", "false"));
			ALT_MOB_NOAGRO = Integer.parseInt(p.getProperty("AltMobNoAttackWithLevelDifference", "0"));
			CHANCE_LEVEL = Integer.parseInt(p.getProperty("ChanceToLevel", "32"));
			GRID_AUTO_DESTROY_ITEM_AFTER = Integer.parseInt(p.getProperty("AutoDestroyDroppedItemAfter", "0"));
			AUTODESTROY_ITEM_AFTER = Integer.parseInt(p.getProperty("AutoDestroyDroppedItemAfter", "0"));
			HERB_AUTO_DESTROY_TIME = Integer.parseInt(p.getProperty("AutoDestroyHerbTime", "15")) * 1000;
			GRID_AUTO_DESTROY_HERB_TIME = Integer.parseInt(p.getProperty("AutoDestroyHerbTime", "15")) * 1000;
			PROTECTED_ITEMS = p.getProperty("ListOfProtectedItems");
			
			LIST_PROTECTED_ITEMS = new ArrayList<>();
			for (String id : PROTECTED_ITEMS.trim().split(","))
			{
				LIST_PROTECTED_ITEMS.add(Integer.parseInt(id.trim()));
			}
			
			DESTROY_DROPPED_PLAYER_ITEM = Boolean.parseBoolean(p.getProperty("DestroyPlayerDroppedItem", "false"));
			DESTROY_EQUIPABLE_PLAYER_ITEM = Boolean.parseBoolean(p.getProperty("DestroyEquipableItem", "false"));
			SAVE_DROPPED_ITEM = Boolean.parseBoolean(p.getProperty("SaveDroppedItem", "false"));
			EMPTY_DROPPED_ITEM_TABLE_AFTER_LOAD = Boolean.parseBoolean(p.getProperty("EmptyDroppedItemTableAfterLoad", "false"));
			SAVE_DROPPED_ITEM_INTERVAL = Integer.parseInt(p.getProperty("SaveDroppedItemInterval", "0")) * 60000;
			CLEAR_DROPPED_ITEM_TABLE = Boolean.parseBoolean(p.getProperty("ClearDroppedItemTable", "false"));
			GRIDS_ALWAYS_ON = Boolean.parseBoolean(p.getProperty("GridsAlwaysOn", "false"));
			GRID_NEIGHBOR_TURNON_TIME = Integer.parseInt(p.getProperty("GridNeighborTurnOnTime", "1"));
			GRID_NEIGHBOR_TURNOFF_TIME = Integer.parseInt(p.getProperty("GridNeighborTurnOffTime", "90"));
			ALLOWED_SKILLS = p.getProperty("AllowedSkills", "0");
			ALLOWED_SKILLS_LIST = new ArrayList<>();
			for (String id : ALLOWED_SKILLS.trim().split(","))
			{
				ALLOWED_SKILLS_LIST.add(Integer.parseInt(id.trim()));
			}
			
			UNAFFECTED_SKILLS = p.getProperty("UnaffectedSkills");
			UNAFFECTED_SKILL_LIST = new ArrayList<>();
			CONSUME_ON_SUCCESS = Boolean.parseBoolean(p.getProperty("ConsumeOnSuccess", "true"));
			USE_STATIC_REUSE = Boolean.parseBoolean(p.getProperty("EnableSaticReuse", "true"));
			
			for (String id : UNAFFECTED_SKILLS.trim().split(","))
			{
				UNAFFECTED_SKILL_LIST.add(Integer.parseInt(id.trim()));
			}
			
			ENABLE_MODIFY_SKILL_DURATION = Boolean.parseBoolean(p.getProperty("EnableModifySkillDuration", "false"));
			MCRIT_RATE = Float.parseFloat(p.getProperty("MCritRate", "2"));
			
			if (ENABLE_MODIFY_SKILL_DURATION)
			{
				SKILL_DURATION_LIST = new HashMap<>();
				String[] propertySplit;
				propertySplit = p.getProperty("SkillDurationList", "").split(";");
				for (String skill : propertySplit)
				{
					String[] skillSplit = skill.split(",");
					if (skillSplit.length != 2)
					{
						System.out.println("[SkillDurationList]: invalid config property -> SkillDurationList \"" + skill + "\"");
					}
					else
					{
						try
						{
							SKILL_DURATION_LIST.put(Integer.valueOf(skillSplit[0]), Integer.valueOf(skillSplit[1]));
						}
						catch (NumberFormatException nfe)
						{
							if (!skill.equals(""))
							{
								System.out.println("[SkillDurationList]: invalid config property -> SkillList \"" + skillSplit[0] + "\"" + skillSplit[1]);
							}
						}
					}
				}
			}
			
			ALT_MINIMUM_FALL_HEIGHT = Integer.parseInt(p.getProperty("AltMinimumFallHeight", "400"));
			ALT_DANCE_MP_CONSUME = Boolean.parseBoolean(p.getProperty("AltDanceMpConsume", "false"));
			ALT_BUFFER_HATE = Integer.parseInt(p.getProperty("BufferHate", "4"));
			GRADE_PENALTY = Boolean.parseBoolean(p.getProperty("GradePenalty", "true"));
			FAIL_FAKEDEATH = Boolean.parseBoolean(p.getProperty("FailFakeDeath", "true"));
			BUFFS_MAX_AMOUNT = Integer.parseInt(p.getProperty("MaxBuffAmount", "20"));
			ALT_BUFFER_TIME = Integer.parseInt(p.getProperty("AltBufferTime", "1"));
			ALT_DANCE_TIME = Integer.parseInt(p.getProperty("AltDanceTime", "1"));
			ALT_SONG_TIME = Integer.parseInt(p.getProperty("AltSongTime", "1"));
			ALT_CH_TIME = Integer.parseInt(p.getProperty("AltChTime", "1"));
			ALT_HERO_TIME = Integer.parseInt(p.getProperty("AltHeroTime", "1"));
			ALT_5MIN_TIME = Integer.parseInt(p.getProperty("Alt5MinTime", "1"));
			ALT_ATTACK_DELAY = Float.parseFloat(p.getProperty("AltAttackDelay", "1.00"));
			ALT_GAME_CANCEL_BOW = p.getProperty("AltGameCancelByHit", "Cast").trim().equalsIgnoreCase("bow") || p.getProperty("AltGameCancelByHit", "Cast").trim().equalsIgnoreCase("all");
			ALT_GAME_CANCEL_CAST = p.getProperty("AltGameCancelByHit", "Cast".trim()).equalsIgnoreCase("cast") || p.getProperty("AltGameCancelByHit", "Cast").trim().equalsIgnoreCase("all");
			ALT_GAME_SHIELD_BLOCKS = Boolean.parseBoolean(p.getProperty("AltShieldBlocks", "false"));
			ALT_PERFECT_SHLD_BLOCK = Integer.parseInt(p.getProperty("AltPerfectShieldBlockRate", "10"));
			STORE_SKILL_COOLTIME = Boolean.parseBoolean(p.getProperty("StoreSkillCooltime", "true"));
			EFFECT_CANCELING = Boolean.parseBoolean(p.getProperty("CancelLesserEffect", "true"));
			CANCEL_AUGUMENTATION_EFFECT = Boolean.parseBoolean(p.getProperty("CancelAugumentionEffect", "true"));
			SKILL_DELAY = Integer.parseInt(p.getProperty("SkillReuseDelay", "70"));
			USE_LEVEL_PENALTY = Boolean.parseBoolean(p.getProperty("UseLevelPenalty", "true"));
			USE_OLY_STATIC_REUSE = Boolean.parseBoolean(p.getProperty("OlyUseStaticReuse", "true"));
			DISABLE_SKILLS_ON_LEVEL_LOST = Boolean.parseBoolean(p.getProperty("DisableSkillsOnLevelLost", "false"));
			USE_CHAR_LEVEL_MOD = Boolean.parseBoolean(p.getProperty("UseCharLevelModifier", "true"));
			OLD_CANCEL_MODE = p.getProperty("CancelMode", "new").toLowerCase().equals("old");
			JAIL_IS_PVP = Boolean.parseBoolean(p.getProperty("JailIsPvpZone", "true"));
			FORBIDDEN_NAMES = p.getProperty("ForbiddenNames", "").split(",");
			
		}
		catch (Exception e)
		{
			_log.error(e.getMessage(), e);
			throw new Error("Failed to Load " + ConfigFiles.ALT_FILE + " File.");
		}
	}
	
	public static void loadCustomConfig()
	{
		try
		{
			Properties p = new L2Properties(ConfigFiles.CUSTOM_FILE);
			
			AUCTION_ITEM_ID = Integer.parseInt(p.getProperty("AuctionBidItemId", "57"));
			
			PET_TICKET_ID = Integer.parseInt(p.getProperty("PetTicketID", "13273"));
			SPECIAL_PET_TICKET_ID = Integer.parseInt(p.getProperty("SpecialPetTicketID", "0"));
			LOAD_CUSTOM_TELEPORTS = Boolean.parseBoolean(p.getProperty("LoadCustomTeleports", "false"));
			
			STARTING_AA = Integer.parseInt(p.getProperty("StartingAA", "100"));
			STARTING_ADENA = Integer.parseInt(p.getProperty("StartingAdena", "0"));
			SHOW_HTML_WELCOME = Boolean.parseBoolean(p.getProperty("ShowHTMLWelcome", "false"));
			ALT_BLACKSMITH_USE_RECIPES = Boolean.parseBoolean(p.getProperty("AltBlacksmithUseRecipes", "true"));
			ALT_RECOMMEND = Boolean.parseBoolean(p.getProperty("AltRecommend", "false"));
			ALT_PLAYER_CAN_DROP_ADENA = Boolean.parseBoolean(p.getProperty("PlayerCanDropAdena", "false"));
			PLAYER_RATE_DROP_ADENA = Integer.parseInt(p.getProperty("PlayerRateDropAdena", "1"));
			WEAR_DELAY = Integer.parseInt(p.getProperty("WearDelay", "5"));
			WEAR_PRICE = Integer.parseInt(p.getProperty("WearPrice", "10"));
			ALT_MANA_POTIONS = Boolean.parseBoolean(p.getProperty("AllowManaPotions", "false"));
			MANAHEAL_POWER = Integer.parseInt(p.getProperty("ManaPotionPower", "200"));
			ENABLE_STARTUP_LVL = Boolean.parseBoolean(p.getProperty("EnableStartupLvl", "false"));
			ADD_LVL_NEWBIE = Integer.parseInt(p.getProperty("StartupLvl", "1"));
			if (ADD_LVL_NEWBIE < 1)
			{
				ADD_LVL_NEWBIE = 1;
			}
			ALLOW_NEW_CHAR_CUSTOM_POSITION = Boolean.parseBoolean(p.getProperty("AltSpawnNewChar", "false"));
			NEW_CHAR_POSITION_X = Integer.parseInt(p.getProperty("AltSpawnX", "0"));
			NEW_CHAR_POSITION_Y = Integer.parseInt(p.getProperty("AltSpawnY", "0"));
			NEW_CHAR_POSITION_Z = Integer.parseInt(p.getProperty("AltSpawnZ", "0"));
			RAID_MIN_MP_TO_CAST = Integer.parseInt(p.getProperty("MinBossManaToCast", "300"));
			BANKING_ENABLED = Boolean.parseBoolean(p.getProperty("Enabled", "true"));
			BANKING_GOLDBAR_PRICE = Integer.parseInt(p.getProperty("GoldBarPrice", "250000000"));
			BANKING_GOLDBAR_ID = Integer.parseInt(p.getProperty("GoldBarId", "3470"));
			ALLOW_CUSTOM_ITEM_TABLE = Boolean.parseBoolean(p.getProperty("LoadCustomItemTables", "false"));
			ALLOW_CUSTOM_NPC_TABLE = Boolean.parseBoolean(p.getProperty("LoadCustomNpcTable", "false"));
			ALLOW_CUSTOM_DROPLIST_TABLE = Boolean.parseBoolean(p.getProperty("LoadCustomDroplistTable", "false"));
			ALLOW_CUSTOM_ARMORSET_TABLE = Boolean.parseBoolean(p.getProperty("LoadCustomArmorSetTable", "false"));
			ALLOW_CUSTOM_SPAWNLIST_TABLE = Boolean.parseBoolean(p.getProperty("LoadCustomSpawnlistTable", "false"));
			
		}
		catch (Exception e)
		{
			_log.error(e.getMessage(), e);
			throw new Error("Failed to Load " + ConfigFiles.CUSTOM_FILE + " File.");
		}
	}
	
	public static void loadEventsConfig()
	{
		loadMainEventsConfig();
		loadSiegeConfig();
	}
	

	public static void loadFilter()
	{
		if (!Config.USE_SAY_FILTER)
		{
			_log.info("Chat Filter: Filter is disabled.");
			return;
		}
		
		File file = new File(ConfigFiles.SAY_FILTER);
		try
		{
			BufferedReader fread = new BufferedReader(new FileReader(file));
			
			String line = null;
			while ((line = fread.readLine()) != null)
			{
				if (line.trim().length() == 0 || line.startsWith("#"))
				{
					continue;
				}
				FILTER_LIST.add(Pattern.compile(line, Pattern.CASE_INSENSITIVE));
			}
			fread.close();
			_log.info("Loaded " + FILTER_LIST.size() + " Filter Words.");
		}
		catch (FileNotFoundException e)
		{
			_log.info("Chat Filter: File not found.");
			return;
		}
		catch (IOException e)
		{
			_log.info("Chat Filter: Error while reading sayfilter.txt.");
			return;
		}
		
	}
	
	public static void loadFiltersConfig()
	{
		loadFilter();
	}
	
	public static void loadGmAccess()
	{
		try
		{
			Properties p = new L2Properties(ConfigFiles.GM_ACCESS_FILE);
			GM_STARTUP_INVISIBLE = Boolean.parseBoolean(p.getProperty("GMStartupInvisible", "true"));
			GM_STARTUP_INVULNERABLE = Boolean.parseBoolean(p.getProperty("GMStartupInvulnerable", "false"));
			GM_STARTUP_SILENCE = Boolean.parseBoolean(p.getProperty("GMStartupSilence", "false"));
			GM_STARTUP_AUTO_LIST = Boolean.parseBoolean(p.getProperty("GMStartupAutoList", "false"));
			SHOW_GM_LOGIN = Boolean.parseBoolean(p.getProperty("ShowGMLogin", "false"));
			GM_ITEM_RESTRICTION = Boolean.parseBoolean(p.getProperty("GmItemRestriction", "false"));
			GM_MAX_ENCHANT = Integer.parseInt(p.getProperty("GMMaxEnchant", "65535"));
			STANDARD_RESPAWN_DELAY = Integer.parseInt(p.getProperty("StandardRespawnDelay", "60"));
			GM_AUDIT = Boolean.parseBoolean(p.getProperty("GMAudit", "false"));
			SHOW_HTML_CHAT = Boolean.parseBoolean(p.getProperty("ShowHTMLChat", "true"));
			GM_NAME_COLOR = Integer.decode("0x" + p.getProperty("GmNameColor", "00FF33"));
			GM_TITLE_COLOR = Integer.decode("0x" + p.getProperty("GmTitleColor", "FF0000"));
			
		}
		catch (Exception e)
		{
			_log.error(e);
			throw new Error("Failed to Load " + ConfigFiles.GM_ACCESS_FILE + " File.");
		}
	}
	
	public static void loadGsConfig()
	{
		try
		{
			Properties p = new L2Properties(ConfigFiles.GAMESERVER_FILE);
			THREAD_POOL_SIZE = GENERAL_THREAD_POOL_SIZE + PACKET_THREAD_POOL_SIZE + EFFECT_THREAD_POOL_SIZE + AI_THREAD_POOL_SIZE;
			
			REQUEST_ID = Integer.parseInt(p.getProperty("RequestServerId", "1"));
			if (REQUEST_ID <= 0)
			{
				REQUEST_ID = 1;
			}
			
			MAXIMUM_ONLINE_USERS = Integer.parseInt(p.getProperty("MaximumOnlineUsers", "400"));
			if (MAXIMUM_ONLINE_USERS > 5000)
			{
				MAXIMUM_ONLINE_USERS = 5000;
			}
			MAX_CHARACTERS_NUMBER_PER_ACCOUNT = Integer.parseInt(p.getProperty("CharMaxNumber", "7"));
			if (MAX_CHARACTERS_NUMBER_PER_ACCOUNT > 7)
			{
				MAX_CHARACTERS_NUMBER_PER_ACCOUNT = 7;
			}
			
			ACCEPT_ALTERNATE_ID = Boolean.parseBoolean(p.getProperty("AcceptAlternateId", "true"));
			DATAPACK_ROOT = new File(p.getProperty("DatapackRoot", ".")).getCanonicalFile();
			SERVER_NAME = p.getProperty("ServerName", "L2Dream");
			
			BAN_CHAT_LOG = Boolean.parseBoolean(p.getProperty("BanChatLog", "true"));
			BAN_ACCOUNT_LOG = Boolean.parseBoolean(p.getProperty("BanAccountLog", "true"));
			JAIL_LOG = Boolean.parseBoolean(p.getProperty("JailLog", "true"));
			BAN_CHAR_LOG = Boolean.parseBoolean(p.getProperty("PlayerBanLog", "true"));
			
			CLASSIC_ANNOUNCE_MODE = Boolean.parseBoolean(p.getProperty("AnnounceBanChat", "true"));
			ANNOUNCE_BAN_CHAT = Boolean.parseBoolean(p.getProperty("AnnounceBanChat", "true"));
			ANNOUNCE_UNBAN_CHAT = Boolean.parseBoolean(p.getProperty("AnnounceUnbanChat", "true"));
			ANNOUNCE_BAN_ACCOUNT = Boolean.parseBoolean(p.getProperty("AnnounceBanAccount", "true"));
			ANNOUNCE_UNBAN_ACCOUNT = Boolean.parseBoolean(p.getProperty("AnnounceUnBanAccount", "true"));
			ANNOUNCE_JAIL = Boolean.parseBoolean(p.getProperty("AnnounceJail", "true"));
			ANNOUNCE_UNJAIL = Boolean.parseBoolean(p.getProperty("AnnounceUnJail", "true"));
			
			GLOBAL_BAN_CHAT_TIME = Integer.parseInt(p.getProperty("GlobalBanTime", "15"));
			
			TIME_ZONE = p.getProperty("TimeZone", "America/Sao_Paulo");
			ONLY_CLANLEADER_CAN_SIT_ON_THRONE = Boolean.parseBoolean(p.getProperty("OnlyClanleaderCanSitOnThrone", "false"));
			
			IDFACTORY_TYPE = IdFactoryType.valueOf(p.getProperty("IDFactory", "BitSet"));
			BAD_ID_CHECKING = Boolean.parseBoolean(p.getProperty("BadIdChecking", "true"));
			ID_FACTORY_CLEANUP = Boolean.parseBoolean(p.getProperty("CleanBadIDs", "true"));
			
			SAFE_REBOOT = Boolean.parseBoolean(p.getProperty("SafeReboot", "true"));
			SAFE_REBOOT_TIME = Integer.parseInt(p.getProperty("SafeRebootTime", "10"));
			SAFE_REBOOT_DISABLE_ENCHANT = Boolean.parseBoolean(p.getProperty("SafeRebootDisableEnchant", "false"));
			SAFE_REBOOT_DISABLE_TELEPORT = Boolean.parseBoolean(p.getProperty("SafeRebootDisableTeleport", "false"));
			SAFE_REBOOT_DISABLE_CREATEITEM = Boolean.parseBoolean(p.getProperty("SafeRebootDisableCreateItem", "false"));
			SAFE_REBOOT_DISABLE_TRANSACTION = Boolean.parseBoolean(p.getProperty("SafeRebootDisableTransaction", "false"));
			SAFE_REBOOT_DISABLE_PC_ITERACTION = Boolean.parseBoolean(p.getProperty("SafeRebootDisablePcIteraction", "false"));
			SAFE_REBOOT_DISABLE_NPC_ITERACTION = Boolean.parseBoolean(p.getProperty("SafeRebootDisableNpcIteraction", "false"));
			
			LIT_REGISTRATION_MODE = Integer.parseInt(p.getProperty("RegistrationMode", "1"));
			if (LIT_REGISTRATION_MODE != 1)
			{
				LIT_REGISTRATION_MODE = 1;
			}
			LIT_REGISTRATION_TIME = Integer.parseInt(p.getProperty("RegistrationTime", "10"));
			LIT_MIN_PARTY_CNT = Integer.parseInt(p.getProperty("MinPartyCount", "4"));
			LIT_MAX_PARTY_CNT = Integer.parseInt(p.getProperty("MaxPartyCount", "5"));
			LIT_MIN_PLAYER_CNT = Integer.parseInt(p.getProperty("MinPlayerCount", "7"));
			LIT_MAX_PLAYER_CNT = Integer.parseInt(p.getProperty("MaxPlayerCount", "45"));
			LIT_TIME_LIMIT = Integer.parseInt(p.getProperty("TimeLimit", "35"));
			RIFT_SPAWN_DELAY = Integer.parseInt(p.getProperty("RiftSpawnDelay", "10000"));
			RIFT_MIN_PARTY_SIZE = Integer.parseInt(p.getProperty("RiftMinPartySize", "5"));
			RIFT_MAX_JUMPS = Integer.parseInt(p.getProperty("MaxRiftJumps", "4"));
			RIFT_AUTO_JUMPS_TIME_MIN = Integer.parseInt(p.getProperty("AutoJumpsDelayMin", "480"));
			RIFT_AUTO_JUMPS_TIME_MAX = Integer.parseInt(p.getProperty("AutoJumpsDelayMax", "600"));
			RIFT_ENTER_COST_RECRUIT = Integer.parseInt(p.getProperty("RecruitCost", "18"));
			RIFT_ENTER_COST_SOLDIER = Integer.parseInt(p.getProperty("SoldierCost", "21"));
			RIFT_ENTER_COST_OFFICER = Integer.parseInt(p.getProperty("OfficerCost", "24"));
			RIFT_ENTER_COST_CAPTAIN = Integer.parseInt(p.getProperty("CaptainCost", "27"));
			RIFT_ENTER_COST_COMMANDER = Integer.parseInt(p.getProperty("CommanderCost", "30"));
			RIFT_ENTER_COST_HERO = Integer.parseInt(p.getProperty("HeroCost", "33"));
			RIFT_BOSS_ROOM_TIME_MUTIPLY = Float.parseFloat(p.getProperty("BossRoomTimeMultiply", "1.5"));
			HS_DEBUFF_CHANCE = Integer.parseInt(p.getProperty("HotSpringDebuffChance", "15"));
			FOG_MOBS_CLONE_CHANCE = Integer.parseInt(p.getProperty("FOGMobsCloneChance", "10"));
			
			ALT_DEFAULT_RESTARTTOWN = Integer.parseInt(p.getProperty("AltDefaultRestartTown", "0"));
			RESPAWN_RANDOM_MAX_OFFSET = Integer.parseInt(p.getProperty("RespawnRandomMaxOffset", "50"));
			RAID_MINION_RESPAWN_TIMER = Integer.parseInt(p.getProperty("RaidMinionRespawnTime", "300000"));
			RAID_MIN_RESPAWN_MULTIPLIER = Float.parseFloat(p.getProperty("RaidMinRespawnMultiplier", "1.0"));
			RAID_MAX_RESPAWN_MULTIPLIER = Float.parseFloat(p.getProperty("RaidMaxRespawnMultiplier", "1.0"));
			RESPAWN_RANDOM_ENABLED = Boolean.parseBoolean(p.getProperty("RespawnRandomInTown", "false"));
			RESPAWN_RESTORE_CP = Double.parseDouble(p.getProperty("RespawnRestoreCP", "0")) / 100;
			RESPAWN_RESTORE_HP = Double.parseDouble(p.getProperty("RespawnRestoreHP", "70")) / 100;
			RESPAWN_RESTORE_MP = Double.parseDouble(p.getProperty("RespawnRestoreMP", "70")) / 100;
			MON_RESPAWN_RANDOM_ENABLED = Boolean.parseBoolean(p.getProperty("UseMonsterRndSpawn", "true"));
			MON_RESPAWN_RANDOM_ZONE = Integer.parseInt(p.getProperty("RndSpawnZone", "300"));
			
			CS_TELE_FEE_RATIO = Long.parseLong(p.getProperty("CastleTeleportFunctionFeeRatio", "604800000"));
			CS_TELE1_FEE = Integer.parseInt(p.getProperty("CastleTeleportFunctionFeeLvl1", "7000"));
			CS_TELE2_FEE = Integer.parseInt(p.getProperty("CastleTeleportFunctionFeeLvl2", "14000"));
			CS_SUPPORT_FEE_RATIO = Long.parseLong(p.getProperty("CastleSupportFunctionFeeRatio", "86400000"));
			CS_SUPPORT1_FEE = Integer.parseInt(p.getProperty("CastleSupportFeeLvl1", "7000"));
			CS_SUPPORT2_FEE = Integer.parseInt(p.getProperty("CastleSupportFeeLvl2", "21000"));
			CS_SUPPORT3_FEE = Integer.parseInt(p.getProperty("CastleSupportFeeLvl3", "37000"));
			CS_SUPPORT4_FEE = Integer.parseInt(p.getProperty("CastleSupportFeeLvl4", "52000"));
			CS_MPREG_FEE_RATIO = Long.parseLong(p.getProperty("CastleMpRegenerationFunctionFeeRatio", "86400000"));
			CS_MPREG1_FEE = Integer.parseInt(p.getProperty("CastleMpRegenerationFeeLvl1", "2000"));
			CS_MPREG2_FEE = Integer.parseInt(p.getProperty("CastleMpRegenerationFeeLvl2", "6500"));
			CS_MPREG3_FEE = Integer.parseInt(p.getProperty("CastleMpRegenerationFeeLvl3", "13750"));
			CS_MPREG4_FEE = Integer.parseInt(p.getProperty("CastleMpRegenerationFeeLvl4", "20000"));
			CS_HPREG_FEE_RATIO = Long.parseLong(p.getProperty("CastleHpRegenerationFunctionFeeRatio", "86400000"));
			CS_HPREG1_FEE = Integer.parseInt(p.getProperty("CastleHpRegenerationFeeLvl1", "1000"));
			CS_HPREG2_FEE = Integer.parseInt(p.getProperty("CastleHpRegenerationFeeLvl2", "1500"));
			CS_HPREG3_FEE = Integer.parseInt(p.getProperty("CastleHpRegenerationFeeLvl3", "2250"));
			CS_HPREG4_FEE = Integer.parseInt(p.getProperty("CastleHpRegenerationFeeLvl4", "3270"));
			CS_HPREG5_FEE = Integer.parseInt(p.getProperty("CastleHpRegenerationFeeLvl5", "5166"));
			CS_EXPREG_FEE_RATIO = Long.parseLong(p.getProperty("CastleExpRegenerationFunctionFeeRatio", "86400000"));
			CS_EXPREG1_FEE = Integer.parseInt(p.getProperty("CastleExpRegenerationFeeLvl1", "9000"));
			CS_EXPREG2_FEE = Integer.parseInt(p.getProperty("CastleExpRegenerationFeeLvl2", "15000"));
			CS_EXPREG3_FEE = Integer.parseInt(p.getProperty("CastleExpRegenerationFeeLvl3", "21000"));
			CS_EXPREG4_FEE = Integer.parseInt(p.getProperty("CastleExpRegenerationFeeLvl4", "30000"));
			
			CH_TELE_FEE_RATIO = Long.parseLong(p.getProperty("ClanHallTeleportFunctionFeeRatio", "604800000"));
			CH_TELE1_FEE = Integer.parseInt(p.getProperty("ClanHallTeleportFunctionFeeLvl1", "7000"));
			CH_TELE2_FEE = Integer.parseInt(p.getProperty("ClanHallTeleportFunctionFeeLvl2", "14000"));
			CH_SUPPORT_FEE_RATIO = Long.parseLong(p.getProperty("ClanHallSupportFunctionFeeRatio", "86400000"));
			CH_SUPPORT1_FEE = Integer.parseInt(p.getProperty("ClanHallSupportFeeLvl1", "2500"));
			CH_SUPPORT2_FEE = Integer.parseInt(p.getProperty("ClanHallSupportFeeLvl2", "5000"));
			CH_SUPPORT3_FEE = Integer.parseInt(p.getProperty("ClanHallSupportFeeLvl3", "7000"));
			CH_SUPPORT4_FEE = Integer.parseInt(p.getProperty("ClanHallSupportFeeLvl4", "11000"));
			CH_SUPPORT5_FEE = Integer.parseInt(p.getProperty("ClanHallSupportFeeLvl5", "21000"));
			CH_SUPPORT6_FEE = Integer.parseInt(p.getProperty("ClanHallSupportFeeLvl6", "36000"));
			CH_SUPPORT7_FEE = Integer.parseInt(p.getProperty("ClanHallSupportFeeLvl7", "37000"));
			CH_SUPPORT8_FEE = Integer.parseInt(p.getProperty("ClanHallSupportFeeLvl8", "52000"));
			CH_MPREG_FEE_RATIO = Long.parseLong(p.getProperty("ClanHallMpRegenerationFunctionFeeRatio", "86400000"));
			CH_MPREG1_FEE = Integer.parseInt(p.getProperty("ClanHallMpRegenerationFeeLvl1", "2000"));
			CH_MPREG2_FEE = Integer.parseInt(p.getProperty("ClanHallMpRegenerationFeeLvl2", "3750"));
			CH_MPREG3_FEE = Integer.parseInt(p.getProperty("ClanHallMpRegenerationFeeLvl3", "6500"));
			CH_MPREG4_FEE = Integer.parseInt(p.getProperty("ClanHallMpRegenerationFeeLvl4", "13750"));
			CH_MPREG5_FEE = Integer.parseInt(p.getProperty("ClanHallMpRegenerationFeeLvl5", "20000"));
			CH_HPREG_FEE_RATIO = Long.parseLong(p.getProperty("ClanHallHpRegenerationFunctionFeeRatio", "86400000"));
			CH_HPREG1_FEE = Integer.parseInt(p.getProperty("ClanHallHpRegenerationFeeLvl1", "700"));
			CH_HPREG2_FEE = Integer.parseInt(p.getProperty("ClanHallHpRegenerationFeeLvl2", "800"));
			CH_HPREG3_FEE = Integer.parseInt(p.getProperty("ClanHallHpRegenerationFeeLvl3", "1000"));
			CH_HPREG4_FEE = Integer.parseInt(p.getProperty("ClanHallHpRegenerationFeeLvl4", "1166"));
			CH_HPREG5_FEE = Integer.parseInt(p.getProperty("ClanHallHpRegenerationFeeLvl5", "1500"));
			CH_HPREG6_FEE = Integer.parseInt(p.getProperty("ClanHallHpRegenerationFeeLvl6", "1750"));
			CH_HPREG7_FEE = Integer.parseInt(p.getProperty("ClanHallHpRegenerationFeeLvl7", "2000"));
			CH_HPREG8_FEE = Integer.parseInt(p.getProperty("ClanHallHpRegenerationFeeLvl8", "2250"));
			CH_HPREG9_FEE = Integer.parseInt(p.getProperty("ClanHallHpRegenerationFeeLvl9", "2500"));
			CH_HPREG10_FEE = Integer.parseInt(p.getProperty("ClanHallHpRegenerationFeeLvl10", "3250"));
			CH_HPREG11_FEE = Integer.parseInt(p.getProperty("ClanHallHpRegenerationFeeLvl11", "3270"));
			CH_HPREG12_FEE = Integer.parseInt(p.getProperty("ClanHallHpRegenerationFeeLvl12", "4250"));
			CH_HPREG13_FEE = Integer.parseInt(p.getProperty("ClanHallHpRegenerationFeeLvl13", "5166"));
			CH_EXPREG_FEE_RATIO = Long.parseLong(p.getProperty("ClanHallExpRegenerationFunctionFeeRatio", "86400000"));
			CH_EXPREG1_FEE = Integer.parseInt(p.getProperty("ClanHallExpRegenerationFeeLvl1", "3000"));
			CH_EXPREG2_FEE = Integer.parseInt(p.getProperty("ClanHallExpRegenerationFeeLvl2", "6000"));
			CH_EXPREG3_FEE = Integer.parseInt(p.getProperty("ClanHallExpRegenerationFeeLvl3", "9000"));
			CH_EXPREG4_FEE = Integer.parseInt(p.getProperty("ClanHallExpRegenerationFeeLvl4", "15000"));
			CH_EXPREG5_FEE = Integer.parseInt(p.getProperty("ClanHallExpRegenerationFeeLvl5", "21000"));
			CH_EXPREG6_FEE = Integer.parseInt(p.getProperty("ClanHallExpRegenerationFeeLvl6", "23330"));
			CH_EXPREG7_FEE = Integer.parseInt(p.getProperty("ClanHallExpRegenerationFeeLvl7", "30000"));
			CH_ITEM_FEE_RATIO = Long.parseLong(p.getProperty("ClanHallItemCreationFunctionFeeRatio", "86400000"));
			CH_ITEM1_FEE = Integer.parseInt(p.getProperty("ClanHallItemCreationFunctionFeeLvl1", "30000"));
			CH_ITEM2_FEE = Integer.parseInt(p.getProperty("ClanHallItemCreationFunctionFeeLvl2", "70000"));
			CH_ITEM3_FEE = Integer.parseInt(p.getProperty("ClanHallItemCreationFunctionFeeLvl3", "140000"));
			CH_CURTAIN_FEE_RATIO = Long.parseLong(p.getProperty("ClanHallCurtainFunctionFeeRatio", "86400000"));
			CH_CURTAIN1_FEE = Integer.parseInt(p.getProperty("ClanHallCurtainFunctionFeeLvl1", "2000"));
			CH_CURTAIN2_FEE = Integer.parseInt(p.getProperty("ClanHallCurtainFunctionFeeLvl2", "2500"));
			CH_FRONT_FEE_RATIO = Long.parseLong(p.getProperty("ClanHallFrontPlatformFunctionFeeRatio", "259200000"));
			CH_FRONT1_FEE = Integer.parseInt(p.getProperty("ClanHallFrontPlatformFunctionFeeLvl1", "1300"));
			CH_FRONT2_FEE = Integer.parseInt(p.getProperty("ClanHallFrontPlatformFunctionFeeLvl2", "4000"));
			
			FORT_TELE_FEE_RATIO = Long.parseLong(p.getProperty("FortTeleportFunctionFeeRatio", "604800000"));
			FORT_TELE1_FEE = Integer.parseInt(p.getProperty("FortTeleportFunctionFeeLvl1", "1000"));
			FORT_TELE2_FEE = Integer.parseInt(p.getProperty("FortTeleportFunctionFeeLvl2", "10000"));
			FORT_SUPPORT_FEE_RATIO = Long.parseLong(p.getProperty("FortSupportFunctionFeeRatio", "86400000"));
			FORT_SUPPORT1_FEE = Integer.parseInt(p.getProperty("FortSupportFeeLvl1", "7000"));
			FORT_SUPPORT2_FEE = Integer.parseInt(p.getProperty("FortSupportFeeLvl2", "1700"));
			FORT_MPREG_FEE_RATIO = Long.parseLong(p.getProperty("FortMpRegenerationFunctionFeeRatio", "86400000"));
			FORT_MPREG1_FEE = Integer.parseInt(p.getProperty("FortMpRegenerationFeeLvl1", "6500"));
			FORT_MPREG2_FEE = Integer.parseInt(p.getProperty("FortMpRegenerationFeeLvl2", "9300"));
			FORT_HPREG_FEE_RATIO = Long.parseLong(p.getProperty("FortHpRegenerationFunctionFeeRatio", "86400000"));
			FORT_HPREG1_FEE = Integer.parseInt(p.getProperty("FortHpRegenerationFeeLvl1", "2000"));
			FORT_HPREG2_FEE = Integer.parseInt(p.getProperty("FortHpRegenerationFeeLvl2", "3500"));
			FORT_EXPREG_FEE_RATIO = Long.parseLong(p.getProperty("FortExpRegenerationFunctionFeeRatio", "86400000"));
			FORT_EXPREG1_FEE = Integer.parseInt(p.getProperty("FortExpRegenerationFeeLvl1", "9000"));
			FORT_EXPREG2_FEE = Integer.parseInt(p.getProperty("FortExpRegenerationFeeLvl2", "10000"));
			
			/** Class Master */
			ALLOW_SAY_MSG_CLASS_MASSTER = Boolean.parseBoolean(p.getProperty("AllowDialogClassMater", "false"));
			SPAWN_CLASS_MASTER = Boolean.parseBoolean(p.getProperty("SpawnClassMaster", "false"));
			
			if (!p.getProperty("ConfigClassMaster").trim().equalsIgnoreCase("false"))
			{
				CLASS_MASTER_SETTINGS_LINE = p.getProperty("ConfigClassMaster");
			}
			
			CLASS_MASTER_POPUP = Boolean.parseBoolean(p.getProperty("ClassMasterPopupWindow", "true"));
			CLASS_MASTER_SETTINGS = new ClassMasterSettings(CLASS_MASTER_SETTINGS_LINE);
			
		}
		catch (Exception e)
		{
			_log.error(e.getMessage(), e);
			throw new Error("Failed to Load " + ConfigFiles.GAMESERVER_FILE + " File.");
		}
	}
	
	public static void loadHexidConfig()
	{
		try
		{
			Properties p = new L2Properties(ConfigFiles.HEXID_FILE);
			
			SERVER_ID = Integer.parseInt(p.getProperty("ServerID"));
			HEX_ID = new BigInteger(p.getProperty("HexID"), 16).toByteArray();
		}
		catch (Exception e)
		{
			_log.warn("Could Not load Hexid File (" + ConfigFiles.HEXID_FILE + "), Hopefully login will Give us one.");
			System.exit(1);
		}
	}
	
	public static void loadMainConfig()
	{
		loadGsConfig();
		loadNpcConfig();
		loadPlayerConfig();
		loadAltConfig();
		loadCustomConfig();
		loadAddOnConfig();
		loadAioxConfig();
		loadEquipmentsConfig();
		loadVoteConfig();
		loadOptionsConfig();
		loadRatesConfig();
		if (!ReloadHandler.getInstance().isRegistred("config"))
		{
			ReloadHandler.getInstance().registerHandler("config", _reloadAll);
		}
	}
	
	public static void loadMainEventsConfig()
	{
		try
		{
			Properties p = new L2Properties(ConfigFiles.MAIN_EVENTS);
			
			ALT_DAWN_JOIN_COST = Integer.parseInt(p.getProperty("AltJoinDawnCost", "50000"));
			ALT_GAME_CASTLE_DAWN = Boolean.parseBoolean(p.getProperty("AltCastleForDawn", "true"));
			ALT_GAME_CASTLE_DUSK = Boolean.parseBoolean(p.getProperty("AltCastleForDusk", "true"));
			ALT_GAME_REQUIRE_CLAN_CASTLE = Boolean.parseBoolean(p.getProperty("AltRequireClanCastle", "false"));
			ALT_FESTIVAL_MIN_PLAYER = Integer.parseInt(p.getProperty("AltFestivalMinPlayer", "5"));
			ALT_MAXIMUM_PLAYER_CONTRIB = Integer.parseInt(p.getProperty("AltMaxPlayerContrib", "1000000"));
			ALT_FESTIVAL_MANAGER_START = Long.parseLong(p.getProperty("AltFestivalManagerStart", "120000"));
			ALT_FESTIVAL_LENGTH = Long.parseLong(p.getProperty("AltFestivalLength", "1080000"));
			ALT_FESTIVAL_CYCLE_LENGTH = Long.parseLong(p.getProperty("AltFestivalCycleLength", "2280000"));
			ALT_FESTIVAL_FIRST_SPAWN = Long.parseLong(p.getProperty("AltFestivalFirstSpawn", "120000"));
			ALT_FESTIVAL_FIRST_SWARM = Long.parseLong(p.getProperty("AltFestivalFirstSwarm", "300000"));
			ALT_FESTIVAL_SECOND_SPAWN = Long.parseLong(p.getProperty("AltFestivalSecondSpawn", "540000"));
			ALT_FESTIVAL_SECOND_SWARM = Long.parseLong(p.getProperty("AltFestivalSecondSwarm", "720000"));
			ALT_FESTIVAL_CHEST_SPAWN = Long.parseLong(p.getProperty("AltFestivalChestSpawn", "900000"));
			ALT_FESTIVAL_ARCHER_AGGRO = Integer.parseInt(p.getProperty("AltFestivalArcherAggro", "200"));
			ALT_FESTIVAL_CHEST_AGGRO = Integer.parseInt(p.getProperty("AltFestivalChestAggro", "0"));
			ALT_FESTIVAL_MONSTER_AGGRO = Integer.parseInt(p.getProperty("AltFestivalMonsterAggro", "200"));
			ANNOUNCE_7S_AT_START_UP = Boolean.parseBoolean(p.getProperty("Announce7s", "true"));
			ALT_STRICT_SEVENSIGNS = Boolean.parseBoolean(p.getProperty("StrictSevenSigns", "true"));
			ANNOUNCE_MAMMON_SPAWN = Boolean.parseBoolean(p.getProperty("AnnounceMammonSpawn", "false"));
			ALT_SIEGE_DAWN_GATES_PDEF_MULT = Double.parseDouble(p.getProperty("AltDawnGatesPdefMult", "1.1"));
			ALT_SIEGE_DUSK_GATES_PDEF_MULT = Double.parseDouble(p.getProperty("AltDuskGatesPdefMult", "0.8"));
			ALT_SIEGE_DAWN_GATES_MDEF_MULT = Double.parseDouble(p.getProperty("AltDawnGatesMdefMult", "1.1"));
			ALT_SIEGE_DUSK_GATES_MDEF_MULT = Double.parseDouble(p.getProperty("AltDuskGatesMdefMult", "0.8"));
			
			PC_CAFFE_ENABLED = Boolean.parseBoolean(p.getProperty("PCCaffeEnabled", "true"));
			PC_CAFFE_INTERVAL = Integer.parseInt(p.getProperty("PCCafeInterval", "10"));
			PC_CAFFE_MIN_LEVEL = Integer.parseInt(p.getProperty("PCCafeMinLevel", "20"));
			PC_CAFFE_MAX_LEVEL = Integer.parseInt(p.getProperty("PCCafeMaxLevel", "85"));
			PC_CAFFE_MIN_SCORE = Integer.parseInt(p.getProperty("PCCafeMinScore", "0"));
			PC_CAFFE_MAX_SCORE = Integer.parseInt(p.getProperty("PCCafeMaxScore", "10"));
			
			ALT_LOTTERY_PRIZE = Integer.parseInt(p.getProperty("AltLotteryPrize", "50000"));
			ALT_LOTTERY_TICKET_PRICE = Integer.parseInt(p.getProperty("AltLotteryTicketPrice", "2000"));
			ALT_LOTTERY_5_NUMBER_RATE = Float.parseFloat(p.getProperty("AltLottery5NumberRate", "0.6"));
			ALT_LOTTERY_4_NUMBER_RATE = Float.parseFloat(p.getProperty("AltLottery4NumberRate", "0.2"));
			ALT_LOTTERY_3_NUMBER_RATE = Float.parseFloat(p.getProperty("AltLottery3NumberRate", "0.2"));
			ALT_LOTTERY_2_AND_1_NUMBER_PRIZE = Integer.parseInt(p.getProperty("AltLottery2and1NumberPrize", "200"));
			
			CHAMPION_PASSIVE = Boolean.parseBoolean(p.getProperty("ChampionPassive", "false"));
			CHAMPION_TITLE = p.getProperty("ChampionTitle", "Champion").trim();
			CHAMPION_ENABLE = Boolean.parseBoolean(p.getProperty("ChampionEnable", "false"));
			CHAMPION_FREQUENCY = Integer.parseInt(p.getProperty("ChampionFrequency", "0"));
			CHAMPION_HP = Integer.parseInt(p.getProperty("ChampionHp", "7"));
			CHAMPION_HP_REGEN = Float.parseFloat(p.getProperty("ChampionHpRegen", "1."));
			CHAMPION_REWARDS = Integer.parseInt(p.getProperty("ChampionRewards", "8"));
			CHAMPION_ADENA = Integer.parseInt(p.getProperty("ChampionAdenasRewards", "1"));
			CHAMPION_ATK = Float.parseFloat(p.getProperty("ChampionAtk", "1."));
			CHAMPION_SPD_ATK = Float.parseFloat(p.getProperty("ChampionSpdAtk", "1."));
			CHAMPION_EXP_SP = Integer.parseInt(p.getProperty("ChampionExpSp", "8"));
			CHAMPION_BOSS = Boolean.parseBoolean(p.getProperty("ChampionBoss", "false"));
			CHAMPION_MIN_LEVEL = Integer.parseInt(p.getProperty("ChampionMinLevel", "20"));
			CHAMPION_MAX_LEVEL = Integer.parseInt(p.getProperty("ChampionMaxLevel", "60"));
			CHAMPION_MINIONS = Boolean.parseBoolean(p.getProperty("ChampionMinions", "false"));
			CHAMPION_SPCL_CHANCE = Integer.parseInt(p.getProperty("ChampionSpecialItemChance", "0"));
			CHAMPION_SPCL_ITEM = Integer.parseInt(p.getProperty("ChampionSpecialItemID", "6393"));
			CHAMPION_SPCL_QTY = Integer.parseInt(p.getProperty("ChampionSpecialItemAmount", "1"));
			CHAMPION_SPCL_LVL_DIFF = Integer.parseInt(p.getProperty("ChampionSpecialItemLevelDiff", "0"));
			
			EVENT_MESSAGE = Boolean.parseBoolean(p.getProperty("EnableEventMessage", "false"));
			MEDAL_DROP = Boolean.parseBoolean(p.getProperty("MedalAddDrop", "false"));
			MEDAL_SPAWN = Boolean.parseBoolean(p.getProperty("MedalSpawnMeneger", "false"));
			MEDAL_CHANCE1 = Integer.parseInt(p.getProperty("Medal1DropChance", "10"));
			MEDAL_CHANCE2 = Integer.parseInt(p.getProperty("Medal2DropChance", "2"));
			STAR_DROP = Boolean.parseBoolean(p.getProperty("StarAddDrop", "false"));
			STAR_SPAWN = Boolean.parseBoolean(p.getProperty("StarSpawnManager", "false"));
			STAR_CHANCE1 = Integer.parseInt(p.getProperty("Star1DropChance", "10"));
			STAR_CHANCE2 = Integer.parseInt(p.getProperty("Star2DropChance", "5"));
			STAR_CHANCE3 = Integer.parseInt(p.getProperty("Star3DropChance", "2"));
			CRISTMAS_DROP = Boolean.parseBoolean(p.getProperty("CristmasAddDrop", "false"));
			CRISTMAS_SPAWN = Boolean.parseBoolean(p.getProperty("CristmasSpawnSanta", "false"));
			CRISTMAS_CHANCE = Integer.parseInt(p.getProperty("CristmasDropChance", "5"));
			CRISTMAS_TREE_TIME = Integer.parseInt(p.getProperty("CristmasTreeLifeTime", "5"));
			L2DAY_DROP = Boolean.parseBoolean(p.getProperty("L2DayAddDrop", "false"));
			L2DAY_SPAWN = Boolean.parseBoolean(p.getProperty("L2DaySpawnManager", "false"));
			L2DAY_CHANCE = Integer.parseInt(p.getProperty("L2DayDropChance", "5"));
			L2DAY_SCROLLCHANCE = Integer.parseInt(p.getProperty("L2DayScrollChs", "300"));
			L2DAY_ENCHSCROLLCHANCE = Integer.parseInt(p.getProperty("L2DayEnchScrollChs", "100"));
			L2DAY_ACCCHANCE = Integer.parseInt(p.getProperty("L2DayAccScrollChs", "10"));
			L2DAY_REWARD = p.getProperty("L2DayRewards", "3931,3927,3928,3929,3926,3930,3933,3932,3935,3934");
			L2DAY_ACCESSORIE = p.getProperty("L2DayRewardsAccessorie", "6662,6660");
			L2DAY_SCROLL = p.getProperty("L2DayRewardsScroll", "3958,3959");
			
			L2DROPDAY_ENABLE = Boolean.parseBoolean(p.getProperty("L2DropDayAddDrop", "false"));
			L2DROPDAY_ITEM1 = Integer.parseInt(p.getProperty("L2DropDayItem1", "9500"));
			L2DROPDAY_CHANCE1 = Integer.parseInt(p.getProperty("L2DropDayChance1", "1000"));
			L2DROPDAY_ITEM2 = Integer.parseInt(p.getProperty("L2DropDayItem2", "9501"));
			L2DROPDAY_CHANCE2 = Integer.parseInt(p.getProperty("L2DropDayChance2", "1000"));
			L2DROPDAY_ITEM3 = Integer.parseInt(p.getProperty("L2DropDayItem3", "9502"));
			L2DROPDAY_CHANCE3 = Integer.parseInt(p.getProperty("L2DropDayChance3", "1000"));
			L2DROPDAY_ITEM4 = Integer.parseInt(p.getProperty("L2DropDayItem4", "9503"));
			L2DROPDAY_CHANCE4 = Integer.parseInt(p.getProperty("L2DropDayChance4", "1000"));
			L2DROPDAY_ITEM5 = Integer.parseInt(p.getProperty("L2DropDayItem5", "9504"));
			L2DROPDAY_CHANCE5 = Integer.parseInt(p.getProperty("L2DropDayChance5", "1000"));
			L2DROPDAY_ITEM6 = Integer.parseInt(p.getProperty("L2DropDayItem6", "9505"));
			L2DROPDAY_CHANCE6 = Integer.parseInt(p.getProperty("L2DropDayChance6", "1000"));
			L2DROPDAY_ITEM7 = Integer.parseInt(p.getProperty("L2DropDayItem7", "9506"));
			L2DROPDAY_CHANCE7 = Integer.parseInt(p.getProperty("L2DropDayChance7", "1000"));
			L2DROPDAY_ITEM8 = Integer.parseInt(p.getProperty("L2DropDayItem8", "9507"));
			L2DROPDAY_CHANCE8 = Integer.parseInt(p.getProperty("L2DropDayChance8", "1000"));
			L2DROPDAY_ITEM9 = Integer.parseInt(p.getProperty("L2DropDayItem9", "9508"));
			L2DROPDAY_CHANCE9 = Integer.parseInt(p.getProperty("L2DropDayChance9", "1000"));
			L2DROPDAY_ITEM10 = Integer.parseInt(p.getProperty("L2DropDayItem10", "9509"));
			L2DROPDAY_CHANCE10 = Integer.parseInt(p.getProperty("L2DropDayChance10", "1000"));
			L2DROPDAY_ITEM11 = Integer.parseInt(p.getProperty("L2DropDayItem11", "9510"));
			L2DROPDAY_CHANCE11 = Integer.parseInt(p.getProperty("L2DropDayChance11", "1000"));
			L2DROPDAY_ITEM12 = Integer.parseInt(p.getProperty("L2DropDayItem12", "9511"));
			L2DROPDAY_CHANCE12 = Integer.parseInt(p.getProperty("L2DropDayChance12", "1000"));
			L2DROPDAY_ITEM13 = Integer.parseInt(p.getProperty("L2DropDayItem13", "9512"));
			L2DROPDAY_CHANCE13 = Integer.parseInt(p.getProperty("L2DropDayChance13", "1000"));
			L2DROPDAY_ITEM14 = Integer.parseInt(p.getProperty("L2DropDayItem14", "9513"));
			L2DROPDAY_CHANCE14 = Integer.parseInt(p.getProperty("L2DropDayChance14", "1000"));
			L2DROPDAY_ITEM15 = Integer.parseInt(p.getProperty("L2DropDayItem15", "9514"));
			L2DROPDAY_CHANCE15 = Integer.parseInt(p.getProperty("L2DropDayChance15", "1000"));
			L2DROPDAY_ITEM16 = Integer.parseInt(p.getProperty("L2DropDayItem16", "9515"));
			L2DROPDAY_CHANCE16 = Integer.parseInt(p.getProperty("L2DropDayChance16", "1000"));
			L2DROPDAY_ITEM17 = Integer.parseInt(p.getProperty("L2DropDayItem17", "9516"));
			L2DROPDAY_CHANCE17 = Integer.parseInt(p.getProperty("L2DropDayChance17", "1000"));
			L2DROPDAY_ITEM18 = Integer.parseInt(p.getProperty("L2DropDayItem18", "9517"));
			L2DROPDAY_CHANCE18 = Integer.parseInt(p.getProperty("L2DropDayChance18", "1000"));
			L2DROPDAY_ITEM19 = Integer.parseInt(p.getProperty("L2DropDayItem19", "9518"));
			L2DROPDAY_CHANCE19 = Integer.parseInt(p.getProperty("L2DropDayChance19", "1000"));
			L2DROPDAY_ITEM20 = Integer.parseInt(p.getProperty("L2DropDayItem20", "9519"));
			L2DROPDAY_CHANCE20 = Integer.parseInt(p.getProperty("L2DropDayChance20", "1000"));
			L2DROPDAY_ITEM21 = Integer.parseInt(p.getProperty("L2DropDayItem21", "9520"));
			L2DROPDAY_CHANCE21 = Integer.parseInt(p.getProperty("L2DropDayChance21", "1000"));
			L2DROPDAY_ITEM22 = Integer.parseInt(p.getProperty("L2DropDayItem22", "9521"));
			
			BIGSQUASH_DROP = Boolean.parseBoolean(p.getProperty("BigSquashAddDrop", "false"));
			BIGSQUASH_CHANCE = Integer.parseInt(p.getProperty("BigSquashDropChance", "5"));
			BIGSQUASH_SPAWN = Boolean.parseBoolean(p.getProperty("BigSquashSpawnManager", "false"));
			BIGSQUASH_USE_SEEDS = Boolean.parseBoolean(p.getProperty("BigSquashUseSeeds", "true"));
			FISHERMAN_ENABLED = Boolean.parseBoolean(p.getProperty("FishermanEnabled", "false"));
			FISHERMAN_INTERVAL = Integer.parseInt(p.getProperty("FishermanInterval", "60"));
			FISHERMAN_REWARD_ID = Integer.parseInt(p.getProperty("FishermanRewardId", "57"));
			FISHERMAN_REWARD_COUNT = Integer.parseInt(p.getProperty("FishermanRewardCount", "100"));
			
			ALLOW_WEDDING = Boolean.parseBoolean(p.getProperty("AllowWedding", "false"));
			SPAWN_WEDDING_NPC = Boolean.parseBoolean(p.getProperty("SpawnWeddingNpc", "false"));
			WEDDING_GIVE_CUPID_BOW = Boolean.parseBoolean(p.getProperty("WeddingGiveBow", "true"));
			WEDDING_HONEYMOON_PORT = Boolean.parseBoolean(p.getProperty("WeddingHoneyMoon", "false"));
			WEDDING_PRICE = Integer.parseInt(p.getProperty("WeddingPrice", "500000"));
			WEDDING_PUNISH_INFIDELITY = Boolean.parseBoolean(p.getProperty("WeddingPunishInfidelity", "true"));
			WEDDING_TELEPORT = Boolean.parseBoolean(p.getProperty("WeddingTeleport", "true"));
			WEDDING_TELEPORT_PRICE = Integer.parseInt(p.getProperty("WeddingTeleportPrice", "500000"));
			WEDDING_TELEPORT_INTERVAL = Integer.parseInt(p.getProperty("WeddingTeleportInterval", "120"));
			WEDDING_SAMESEX = Boolean.parseBoolean(p.getProperty("WeddingAllowSameSex", "true"));
			WEDDING_FORMALWEAR = Boolean.parseBoolean(p.getProperty("WeddingFormalWear", "true"));
			WEDDING_DIVORCE_COSTS = Integer.parseInt(p.getProperty("WeddingDivorceCosts", "20"));
			WEDDING_PORT_X = Integer.parseInt(p.getProperty("WeddingTeleporX", "0"));
			WEDDING_PORT_Y = Integer.parseInt(p.getProperty("WeddingTeleporY", "0"));
			WEDDING_PORT_Z = Integer.parseInt(p.getProperty("WeddingTeleporZ", "0"));
			WEDDING_USE_COLOR = Boolean.parseBoolean(p.getProperty("WeddingUseNickColor", "true"));
			WEDDING_NORMAL = Integer.valueOf(p.getProperty("WeddingNormalPairNickColor", "BF0000"), 16);
			WEDDING_GAY = Integer.valueOf(p.getProperty("WeddingGayPairNickColor", "0000BF"), 16);
			WEDDING_LESBI = Integer.valueOf(p.getProperty("WeddingLesbiPairNickColor", "BF00BF"), 16);
		}
		catch (Exception e)
		{
			_log.error(e.getMessage(), e);
			throw new Error("Failed to Load " + ConfigFiles.MAIN_EVENTS + " File.");
		}
	}
	
	public static void loadMods()
	{
		try
		{
			Properties p = new L2Properties(ConfigFiles.MOD_FILE);
			
			// ANTIBOT CAPTCHA
			BOTS_PREVENTION = Boolean.parseBoolean(p.getProperty("EnableCaptcha", "False"));
			KILLS_COUNTER = Integer.parseInt(p.getProperty("KillsCounter", "60"));
			KILLS_COUNTER_RANDOMIZATION = Integer.parseInt(p.getProperty("KillsCounterRandomization", "50"));
			VALIDATION_TIME = Integer.parseInt(p.getProperty("ValidationTime", "60"));
			PUNISHMENT = Integer.parseInt(p.getProperty("Punishment", "0"));
			PUNISHMENT_TIME = Integer.parseInt(p.getProperty("PunishmentTime", "60"));
			
			ALLOW_OFFLINE_TRADE = Boolean.parseBoolean(p.getProperty("AllowOfflineTrade", "false"));
			ALLOW_OFFLINE_TRADE_CRAFT = Boolean.parseBoolean(p.getProperty("AllowOfflineTradeCraft", "false"));
			ALLOW_OFFLINE_TRADE_COLOR_NAME = Boolean.parseBoolean(p.getProperty("AllowOfflineTradeColorName", "false"));
			OFFLINE_TRADE_COLOR_NAME = Integer.decode("0x" + p.getProperty("OfflineTradeColorName", "0088FF"));
			ALLOW_OFFLINE_TRADE_PROTECTION = Boolean.parseBoolean(p.getProperty("AllowOfflineTradeProtection", "false"));
			
			OFFLINE_TRADE_PRICE_ITEM_ID = Integer.parseInt(p.getProperty("OfflineTradePriceID", "57"));
			
			OFFLINE_TRADE_PRICE_ITEM_ID_TIME = Integer.parseInt(p.getProperty("OfflineTradePriceIDTime", "1"));
			OFFLINE_TRADE_PRICE_COUNT = Integer.parseInt(p.getProperty("OfflineTradePriceCount", "500000"));
			OFFLINE_CRAFT_PRICE_ITEM_ID = Integer.parseInt(p.getProperty("OfflineCraftPriceID", "57"));
			
			OFFLINE_CRAFT_PRICE_ITEM_ID_TIME = Integer.parseInt(p.getProperty("OfflineCraftPriceIDTime", "1"));
			OFFLINE_CRAFT_PRICE_COUNT = Integer.parseInt(p.getProperty("OfflineCraftPriceCount", "500000"));
			RESTORE_OFFLINE_TRADERS = Boolean.parseBoolean(p.getProperty("RestoreOfflineTraders", "true"));
			ALLOW_OFFLINE_HOUR = Integer.parseInt(p.getProperty("AllowOfflineHour", "72"));
			
			if (OFFLINE_TRADE_PRICE_ITEM_ID == 0)
			{
				OFFLINE_TRADE_PRICE_COUNT = 0;
			}
			
			if (!ALLOW_OFFLINE_TRADE)
			{
				RESTORE_OFFLINE_TRADERS = false;
			}
			
			SHOW_NPC_CREST = Boolean.parseBoolean(p.getProperty("ShowNpcCrest", "False"));
			CUSTOM_CHAR_TITLE = Boolean.parseBoolean(p.getProperty("CustomStartTitle", "False"));
			CUSTOM_CHAR_TITLE_TEXT = p.getProperty("CustomTitleText", "Dream Project");
			TITLE_COLOR = Integer.decode("0x" + p.getProperty("TitleColor", "00FF00"));
			
			PVP_COLOR_SYSTEM = Boolean.parseBoolean(p.getProperty("PvPColorSystem", "false"));
			String mode = p.getProperty("PvPColorMode", "Title");
			if (mode.equalsIgnoreCase("title"))
			{
				PVP_COLOR_MODE = PVP_MODE_TITLE;
			}
			else if (mode.equalsIgnoreCase("name"))
			{
				PVP_COLOR_MODE = PVP_MODE_NAME;
			}
			else
			{
				PVP_COLOR_MODE = PVP_MODE_TITLE | PVP_MODE_NAME;
			}
			COLOR_FOR_AMMOUNT1 = Integer.decode("0x" + p.getProperty("ColorForAmmount1", "00FF00"));
			COLOR_FOR_AMMOUNT2 = Integer.decode("0x" + p.getProperty("ColorForAmmount2", "00FF00"));
			COLOR_FOR_AMMOUNT3 = Integer.decode("0x" + p.getProperty("ColorForAmmount3", "00FF00"));
			COLOR_FOR_AMMOUNT4 = Integer.decode("0x" + p.getProperty("ColorForAmmount4", "00FF00"));
			COLOR_FOR_AMMOUNT5 = Integer.decode("0x" + p.getProperty("ColorForAmmount5", "00FF00"));
			TITLE_COLOR_FOR_AMMOUNT1 = Integer.decode("0x" + p.getProperty("TitleForAmmount1", "00FF00"));
			TITLE_COLOR_FOR_AMMOUNT2 = Integer.decode("0x" + p.getProperty("TitleForAmmount2", "00FF00"));
			TITLE_COLOR_FOR_AMMOUNT3 = Integer.decode("0x" + p.getProperty("TitleForAmmount3", "00FF00"));
			TITLE_COLOR_FOR_AMMOUNT4 = Integer.decode("0x" + p.getProperty("TitleForAmmount4", "00FF00"));
			TITLE_COLOR_FOR_AMMOUNT5 = Integer.decode("0x" + p.getProperty("TitleForAmmount5", "00FF00"));
			PVP_AMMOUNT1 = Integer.parseInt(p.getProperty("PvpAmmount1", "50"));
			PVP_AMMOUNT2 = Integer.parseInt(p.getProperty("PvpAmmount2", "100"));
			PVP_AMMOUNT3 = Integer.parseInt(p.getProperty("PvpAmmount3", "150"));
			PVP_AMMOUNT4 = Integer.parseInt(p.getProperty("PvpAmmount4", "250"));
			PVP_AMMOUNT5 = Integer.parseInt(p.getProperty("PvpAmmount5", "500"));
			
			VOICED_HELP = Boolean.parseBoolean(p.getProperty("LoadVoicedHelp", "False"));
			VOICED_OFFLINE = Boolean.parseBoolean(p.getProperty("LoadVoicedOffline", "False"));
			VOICED_WEDDING = Boolean.parseBoolean(p.getProperty("LoadVoicedWedding", "False"));
			VOICED_BANK = Boolean.parseBoolean(p.getProperty("LoadVoicedBank", "False"));
			VOICED_CONFIGURATOR = Boolean.parseBoolean(p.getProperty("LoadVoicedConfigurator", "False"));
			VOICED_CLASS_MASTER = Boolean.parseBoolean(p.getProperty("LoadVoicedClassMaster", "False"));
			VOICED_AIOX_COMMAND = Boolean.parseBoolean(p.getProperty("LoadVoicedAioxCommand", "False"));
			VOICED_AUTOFARM_COMMAND = Boolean.parseBoolean(p.getProperty("LoadVoicedAutofarmCommand", "False"));
			
			ENABLE_EVENT_MANAGER = Boolean.parseBoolean(p.getProperty("EnableEventManager", "false"));
			EVENT_MANAGER_ID = Integer.parseInt(p.getProperty("EventManagerNpcId", "50004"));
			SPAWN_EVENT_MANAGER = Boolean.parseBoolean(p.getProperty("EnableAutoSpawn", "false"));
			EVENT_PARTICIPATION_FEE_ID = Integer.parseInt(p.getProperty("EventParticipationFeeId", "57"));
			EVENT_PARTICIPATION_FEE_QNT = Integer.parseInt(p.getProperty("EventParticipationFeeQnt", "10000"));
			
			EVENT_BLOCKED_CLASS = p.getProperty("EventBlockClassId");
			LIST_EVENT_BLOCKED_CLASSES = new ArrayList<>();
			for (String id : EVENT_BLOCKED_CLASS.trim().split(","))
			{
				LIST_EVENT_BLOCKED_CLASSES.add(Integer.parseInt(id.trim()));
			}
			EVENT_BLOCKED_CLASS_NAMES = p.getProperty("EventBlockClassNames");
			
			ACTIVATED_SYSTEM = Boolean.parseBoolean(p.getProperty("ActivateSystem", "false"));
			
			STARTUP_SYSTEM_ENABLED = Boolean.parseBoolean(p.getProperty("EnableStartupSystem", "False"));
			
			STARTUP_SYSTEM_SELECTCLASS = Boolean.parseBoolean(p.getProperty("StartupSystemClass", "False"));
			STARTUP_SYSTEM_SELECTARMOR = Boolean.parseBoolean(p.getProperty("StartupSystemArmor", "False"));
			STARTUP_SYSTEM_SELECTWEAP = Boolean.parseBoolean(p.getProperty("StartupSystemWeapon", "False"));
			STARTUP_SYSTEM_BUFF_MAGE = Boolean.parseBoolean(p.getProperty("StartupSystemBuffMage", "False"));
			STARTUP_SYSTEM_BUFF_FIGHT = Boolean.parseBoolean(p.getProperty("StartupSystemBuffFight", "False"));
			
			DISABLE_TUTORIAL = Boolean.parseBoolean(p.getProperty("DisableNewbieTutorial", "False"));
			if (STARTUP_SYSTEM_ENABLED)
			{
				DISABLE_TUTORIAL = true;
			}
			FIGHTER_BUFF = p.getProperty("FighterBuffList", "0");
			FIGHTER_BUFF_LIST = new ArrayList<>();
			for (String id : FIGHTER_BUFF.trim().split(","))
			{
				FIGHTER_BUFF_LIST.add(Integer.parseInt(id.trim()));
			}
			
			MAGE_BUFF = p.getProperty("MageBuffList", "0");
			MAGE_BUFF_LIST = new ArrayList<>();
			for (String id : MAGE_BUFF.trim().split(","))
			{
				MAGE_BUFF_LIST.add(Integer.parseInt(id.trim()));
			}
			
			String[] propertySplit = p.getProperty("SetRobe", "4223,1").split(";");
			SET_ROBE_ITEMS.clear();
			for (String reward : propertySplit)
			{
				String[] rewardSplit = reward.split(",");
				if (rewardSplit.length != 2)
				{
					_log.warn("StartingCustomItemsFighter[Config.load()]: invalid config property -> StartingCustomItemsFighter \"" + reward + "\"");
				}
				else
				{
					try
					{
						SET_ROBE_ITEMS.add(new int[]
						{
							Integer.parseInt(rewardSplit[0]),
							Integer.parseInt(rewardSplit[1])
						});
					}
					catch (NumberFormatException nfe)
					{
						if (!reward.isEmpty())
						{
							_log.warn("StartingCustomItemsFighter[Config.load()]: invalid config property -> StartingCustomItemsFighter \"" + reward + "\"");
						}
					}
				}
			}
			propertySplit = p.getProperty("SetLight", "4223,1").split(";");
			SET_LIGHT_ITEMS.clear();
			for (String reward : propertySplit)
			{
				String[] rewardSplit = reward.split(",");
				if (rewardSplit.length != 2)
				{
					_log.warn("StartingCustomItemsFighter[Config.load()]: invalid config property -> StartingCustomItemsFighter \"" + reward + "\"");
				}
				else
				{
					try
					{
						SET_LIGHT_ITEMS.add(new int[]
						{
							Integer.parseInt(rewardSplit[0]),
							Integer.parseInt(rewardSplit[1])
						});
					}
					catch (NumberFormatException nfe)
					{
						if (!reward.isEmpty())
						{
							_log.warn("StartingCustomItemsFighter[Config.load()]: invalid config property -> StartingCustomItemsFighter \"" + reward + "\"");
						}
					}
				}
			}
			propertySplit = p.getProperty("SetHeavy", "4223,1").split(";");
			SET_HEAVY_ITEMS.clear();
			for (String reward : propertySplit)
			{
				String[] rewardSplit = reward.split(",");
				if (rewardSplit.length != 2)
				{
					_log.warn("StartingCustomItemsFighter[Config.load()]: invalid config property -> StartingCustomItemsFighter \"" + reward + "\"");
				}
				else
				{
					try
					{
						SET_HEAVY_ITEMS.add(new int[]
						{
							Integer.parseInt(rewardSplit[0]),
							Integer.parseInt(rewardSplit[1])
						});
					}
					catch (NumberFormatException nfe)
					{
						if (!reward.isEmpty())
						{
							_log.warn("StartingCustomItemsFighter[Config.load()]: invalid config property -> StartingCustomItemsFighter \"" + reward + "\"");
						}
					}
				}
			}
			
			BYBASS_ROBE_ITEMS = p.getProperty("htm_robe", "startup");
			BYBASS_LIGHT_ITEMS = p.getProperty("htm_light", "startup");
			BYBASS_HEAVY_ITEMS = p.getProperty("htm_heavy", "startup");
			
			BYBASS_WP_01_ITEM = p.getProperty("BpWeapon_01", "startup");
			WP_01_ID = Integer.parseInt(p.getProperty("Wp_01_ID", "5"));
			
			BYBASS_WP_02_ITEM = p.getProperty("BpWeapon_02", "startup");
			WP_02_ID = Integer.parseInt(p.getProperty("Wp_02_ID", "5"));
			
			BYBASS_WP_03_ITEM = p.getProperty("BpWeapon_03", "startup");
			WP_03_ID = Integer.parseInt(p.getProperty("Wp_03_ID", "5"));
			
			BYBASS_WP_04_ITEM = p.getProperty("BpWeapon_04", "startup");
			WP_04_ID = Integer.parseInt(p.getProperty("Wp_04_ID", "5"));
			
			BYBASS_WP_05_ITEM = p.getProperty("BpWeapon_05", "startup");
			WP_05_ID = Integer.parseInt(p.getProperty("Wp_05_ID", "5"));
			
			BYBASS_WP_06_ITEM = p.getProperty("BpWeapon_06", "startup");
			WP_06_ID = Integer.parseInt(p.getProperty("Wp_06_ID", "5"));
			
			BYBASS_WP_07_ITEM = p.getProperty("BpWeapon_07", "startup");
			WP_07_ID = Integer.parseInt(p.getProperty("Wp_07_ID", "5"));
			
			BYBASS_WP_08_ITEM = p.getProperty("BpWeapon_08", "startup");
			WP_08_ID = Integer.parseInt(p.getProperty("Wp_08_ID", "5"));
			
			BYBASS_WP_09_ITEM = p.getProperty("BpWeapon_09", "startup");
			WP_09_ID = Integer.parseInt(p.getProperty("Wp_09_ID", "5"));
			
			BYBASS_WP_10_ITEM = p.getProperty("BpWeapon_10", "startup");
			WP_10_ID = Integer.parseInt(p.getProperty("Wp_10_ID", "5"));
			
			BYBASS_WP_11_ITEM = p.getProperty("BpWeapon_11", "startup");
			WP_11_ID = Integer.parseInt(p.getProperty("Wp_11_ID", "5"));
			
			BYBASS_WP_12_ITEM = p.getProperty("BpWeapon_12", "startup");
			WP_12_ID = Integer.parseInt(p.getProperty("Wp_12_ID", "5"));
			
			BYBASS_WP_13_ITEM = p.getProperty("BpWeapon_13", "startup");
			WP_13_ID = Integer.parseInt(p.getProperty("Wp_13_ID", "5"));
			
			BYBASS_WP_14_ITEM = p.getProperty("BpWeapon_14", "startup");
			WP_14_ID = Integer.parseInt(p.getProperty("Wp_14_ID", "5"));
			
			BYBASS_WP_15_ITEM = p.getProperty("BpWeapon_15", "startup");
			WP_15_ID = Integer.parseInt(p.getProperty("Wp_15_ID", "5"));
			
			BYBASS_WP_16_ITEM = p.getProperty("BpWeapon_16", "startup");
			WP_16_ID = Integer.parseInt(p.getProperty("Wp_16_ID", "5"));
			
			BYBASS_WP_17_ITEM = p.getProperty("BpWeapon_17", "startup");
			WP_17_ID = Integer.parseInt(p.getProperty("Wp_17_ID", "5"));
			
			BYBASS_WP_18_ITEM = p.getProperty("BpWeapon_18", "startup");
			WP_18_ID = Integer.parseInt(p.getProperty("Wp_18_ID", "5"));
			
			BYBASS_WP_19_ITEM = p.getProperty("BpWeapon_19", "startup");
			WP_19_ID = Integer.parseInt(p.getProperty("Wp_19_ID", "5"));
			
			BYBASS_WP_20_ITEM = p.getProperty("BpWeapon_20", "startup");
			WP_20_ID = Integer.parseInt(p.getProperty("Wp_20_ID", "5"));
			
			BYBASS_WP_21_ITEM = p.getProperty("BpWeapon_21", "startup");
			WP_21_ID = Integer.parseInt(p.getProperty("Wp_21_ID", "5"));
			
			BYBASS_WP_22_ITEM = p.getProperty("BpWeapon_22", "startup");
			WP_22_ID = Integer.parseInt(p.getProperty("Wp_22_ID", "5"));
			
			BYBASS_WP_23_ITEM = p.getProperty("BpWeapon_23", "startup");
			WP_23_ID = Integer.parseInt(p.getProperty("Wp_23_ID", "5"));
			
			BYBASS_WP_24_ITEM = p.getProperty("BpWeapon_24", "startup");
			WP_24_ID = Integer.parseInt(p.getProperty("Wp_24_ID", "5"));
			
			BYBASS_WP_25_ITEM = p.getProperty("BpWeapon_25", "startup");
			WP_25_ID = Integer.parseInt(p.getProperty("Wp_25_ID", "5"));
			
			BYBASS_WP_26_ITEM = p.getProperty("BpWeapon_26", "startup");
			WP_26_ID = Integer.parseInt(p.getProperty("Wp_26_ID", "5"));
			
			BYBASS_WP_27_ITEM = p.getProperty("BpWeapon_27", "startup");
			WP_27_ID = Integer.parseInt(p.getProperty("Wp_27_ID", "5"));
			
			BYBASS_WP_28_ITEM = p.getProperty("BpWeapon_28", "startup");
			WP_28_ID = Integer.parseInt(p.getProperty("Wp_28_ID", "5"));
			
			BYBASS_WP_29_ITEM = p.getProperty("BpWeapon_29", "startup");
			WP_29_ID = Integer.parseInt(p.getProperty("Wp_29_ID", "5"));
			
			BYBASS_WP_30_ITEM = p.getProperty("BpWeapon_30", "startup");
			WP_30_ID = Integer.parseInt(p.getProperty("Wp_30_ID", "5"));
			
			BYBASS_WP_31_ITEM = p.getProperty("BpWeapon_31", "startup");
			WP_31_ID = Integer.parseInt(p.getProperty("Wp_31_ID", "5"));
			
			WP_ARROW = Integer.parseInt(p.getProperty("Arrow_ID", "5"));
			WP_SHIELD = Integer.parseInt(p.getProperty("Shield_ID", "5"));
			
			ENABLE_REWARDSKILL_TIME = Boolean.parseBoolean(p.getProperty("EnableSkillRewardByTime", "false"));
			REWARDSKILL_TIME_TASK = Integer.parseInt(p.getProperty("RewardSkillTime", "60")) * 60000;
			REWARDSKILL_TIME_SKILL_ID = Integer.parseInt(p.getProperty("RewardSkillID", "9999"));
			REWARDSKILL_TIME_SKILL_MAXLVL = Integer.parseInt(p.getProperty("RewardSkillMaxLvl", "20"));
			ENABLE_FIRSTLOGIN_REWARD = Boolean.parseBoolean(p.getProperty("EnableFirstLoginReward", "false"));
			FIRSTLOGIN_REWARD_ITEM = parseItemsList(p.getProperty("FirstLoginRewardTimeItem", "57,1"));
			ENABLE_REWARDITEM_BYTIME = Boolean.parseBoolean(p.getProperty("EnableRewardByTime", "false"));
			REWARDITEM_BYTIME_MIN = Integer.parseInt(p.getProperty("RewardItemByTimeMin", "5"));
			REWARDITEM_BYTIME_ITEM = parseItemsList(p.getProperty("RewardItemByTimeItem", "57,1"));
			
			FIRSTLOGIN_BUFFS = Boolean.parseBoolean(p.getProperty("FirstLoginBuffs", "False"));
			if (STARTUP_SYSTEM_ENABLED)
			{
				FIRSTLOGIN_BUFFS = false;
			}
			FIRSTLOGIN_FIGHTER_BUFF = p.getProperty("FirstLoginFighterBuffList", "0");
			FIRSTLOGIN_FIGHTER_BUFF_LIST = new ArrayList<>();
			for (String id : FIRSTLOGIN_FIGHTER_BUFF.trim().split(","))
			{
				FIRSTLOGIN_FIGHTER_BUFF_LIST.add(Integer.parseInt(id.trim()));
			}
			
			FIRSTLOGIN_MAGE_BUFF = p.getProperty("FirstLoginMageBuffList", "0");
			FIRSTLOGIN_MAGE_BUFF_LIST = new ArrayList<>();
			for (String id : FIRSTLOGIN_MAGE_BUFF.trim().split(","))
			{
				FIRSTLOGIN_MAGE_BUFF_LIST.add(Integer.parseInt(id.trim()));
			}
		}
		catch (Exception e)
		{
			_log.error(e);
			throw new Error("Failed to Load " + ConfigFiles.MOD_FILE + " File.");
		}
	}
	
	public static boolean ACTIVATED_SYSTEM;
	
	public static void loadNetworkConfig()
	{
		try
		{
			Properties p = new L2Properties(ConfigFiles.NETWORK_FILE);
			
			GAMESERVER_HOSTNAME = p.getProperty("GameServerHostName");
			PORT_GAME = Integer.parseInt(p.getProperty("GameServerPort", "7777"));
			
			EXTERNAL_HOSTNAME = p.getProperty("ExternalHostname", "*");
			INTERNAL_HOSTNAME = p.getProperty("InternalHostname", "*");
			
			GAME_SERVER_LOGIN_PORT = Integer.parseInt(p.getProperty("LoginPort", "9014"));
			GAME_SERVER_LOGIN_HOST = p.getProperty("LoginHost", "127.0.0.1");
			
			MIN_PROTOCOL_REVISION =  Integer.parseInt(p.getProperty("MinProtocolVersion", "730"));
			MAX_PROTOCOL_REVISION =  Integer.parseInt(p.getProperty("MaxProtocolVersion", "746"));
			
			
			DATABASE_DRIVER = p.getProperty("Driver", "com.mysql.jdbc.Driver");
			DATABASE_URL = p.getProperty("URL", "jdbc:mysql://localhost/");
			DATABASE_LOGIN = p.getProperty("Login", "root");
			DATABASE_PASSWORD = p.getProperty("Password", "");
			DATABASE_MAX_CONNECTIONS = Integer.parseInt(p.getProperty("MaximumDbConnections", "10"));
		}
		catch (Exception e)
		{
			_log.error(e.getMessage(), e);
			throw new Error("Failed to Load: " + ConfigFiles.NETWORK_FILE + " File.");
		}
	}
	
	public static void loadNetworkConfiguration()
	{
		loadNetworkConfig();
		loadHexidConfig();
	}
	
	public static boolean ALLOW_LETHAL_PROTECTION_MOBS;
	public static String LETHAL_PROTECTED_MOBS;
	public static ArrayList<Integer> LIST_LETHAL_PROTECTED_MOBS = new ArrayList<>();
	
	public static void loadNpcConfig()
	{
		try
		{
			Properties p = new L2Properties(ConfigFiles.NPC_FILE);
			
			SHOW_NPC_LVL = Boolean.parseBoolean(p.getProperty("ShowNpcLevel", "false"));
			MAX_DRIFT_RANGE = Integer.parseInt(p.getProperty("MaxDriftRange", "200"));
			MIN_NPC_ANIMATION = Integer.parseInt(p.getProperty("MinNPCAnimation", "10"));
			MAX_NPC_ANIMATION = Integer.parseInt(p.getProperty("MaxNPCAnimation", "20"));
			FREE_TELEPORT = Boolean.parseBoolean(p.getProperty("FreeTeleporting", "false"));
			FREE_TELEPORT_MINLVL = Integer.parseInt(p.getProperty("FreeTeleportingMinLvL", "1"));
			FREE_TELEPORT_MAXLVL = Integer.parseInt(p.getProperty("FreeTeleportingMaxLvL", "99"));
			NOBLE_FREE_TELEPORT = Boolean.parseBoolean(p.getProperty("NoblePassFreeTp", "false"));
			NOBLE_FREE_TELEPORT_MINLVL = Integer.parseInt(p.getProperty("NoblePassFreeTpMinLvL", "1"));
			NOBLE_FREE_TELEPORT_MAXLVL = Integer.parseInt(p.getProperty("NoblePassFreeTpMaxLvL", "99"));
			NPC_MIN_WALK_ANIMATION = Integer.parseInt(p.getProperty("MinNPCWalkAnimation", "10"));
			NPC_MAX_WALK_ANIMATION = Integer.parseInt(p.getProperty("MaxNPCWalkAnimation", "20"));
			MIN_MONSTER_ANIMATION = Integer.parseInt(p.getProperty("MinMonsterAnimation", "5"));
			MAX_MONSTER_ANIMATION = Integer.parseInt(p.getProperty("MaxMonsterAnimation", "20"));
			ALT_MOB_AGGRO_IN_PEACEZONE = Boolean.parseBoolean(p.getProperty("AltMobAggroInPeaceZone", "true"));
			ALT_ATTACKABLE_NPCS = Boolean.parseBoolean(p.getProperty("AltAttackableNpcs", "true"));
			ALLOW_PET_WALKERS = Boolean.parseBoolean(p.getProperty("AllowPetWalker", "false"));
			ALT_URN_TEMP_FAIL = Integer.parseInt(p.getProperty("UrnTempFail", "10"));
			ALLOW_RENTPET = Boolean.parseBoolean(p.getProperty("AllowRentPet", "false"));
			ALLOW_WYVERN_UPGRADER = Boolean.parseBoolean(p.getProperty("AllowWyvernUpgrader", "false"));
			MANAGER_CRYSTAL_COUNT = Integer.parseInt(p.getProperty("ManagerCrystalCount", "25"));
			WYVERN_SPEED = Integer.parseInt(p.getProperty("WyvernSpeed", "100"));
			STRIDER_SPEED = Integer.parseInt(p.getProperty("StriderSpeed", "80"));
			
			// ClassMaster
			ALT_CLASS_MASTER_STRIDER_UPDATE = Boolean.parseBoolean(p.getProperty("ClassMasterUpdateStrider", "False"));
			if (!p.getProperty("ConfigClassMaster").trim().equalsIgnoreCase("False"))
			{
				ALT_CLASS_MASTER_SETTINGS_LINE = p.getProperty("ConfigClassMaster");
			}
			
			ALT_CLASS_MASTER_SETTINGS = new ClassMasterSettings(ALT_CLASS_MASTER_SETTINGS_LINE);
			
			ALT_L2J_CLASS_MASTER = Boolean.parseBoolean(p.getProperty("ClassMaster", "False"));
			ALT_CLASS_MASTER_ENTIRE_TREE = Boolean.parseBoolean(p.getProperty("ClassMasterEntireTree", "False"));
			ALT_CLASS_MASTER_TUTORIAL = Boolean.parseBoolean(p.getProperty("AltClassMaster", "False"));
			
			ALT_DISABLE_RAIDBOSS_PETRIFICATION = Boolean.parseBoolean(p.getProperty("DisableRaidBossFossilization", "false"));
			MAX_LEVEL_RAID_CURSE = Integer.parseInt(p.getProperty("MaxLevelRaidBossCurse", "1"));
			FORCE_UPDATE_RAIDBOSS_ON_DB = Boolean.parseBoolean(p.getProperty("ForceUpdateRaidBossOnDB", "false"));
			KICKTIMERESTART = Long.parseLong(p.getProperty("NoRestartKickTime", "5"));
			
			ALLOW_LETHAL_PROTECTION_MOBS = Boolean.parseBoolean(p.getProperty("AllowLethalProtectionMobs", "False"));
			
			LETHAL_PROTECTED_MOBS = p.getProperty("LethalProtectedMobs", "");
			
			LIST_LETHAL_PROTECTED_MOBS = new ArrayList<>();
			for (final String id : LETHAL_PROTECTED_MOBS.split(","))
			{
				LIST_LETHAL_PROTECTED_MOBS.add(Integer.parseInt(id));
			}
			
			L2Properties boss = new L2Properties("./config/main/bosses.properties");
			EPIC_REQUIRE_QUEST = Boolean.parseBoolean(boss.getProperty("QuestRequired", "true"));
		}
		catch (Exception e)
		{
			_log.error(e.getMessage(), e);
			throw new Error("Failed to Load " + ConfigFiles.NPC_FILE + " File.");
		}
		
	}
	
	public static void loadOllyConfig()
	{
		try
		{
			Properties p = new L2Properties(ConfigFiles.OLLY_FILE);
			
			ALT_OLY_ENABLED = Boolean.parseBoolean(p.getProperty("OlympiadEnabled", "true"));
			ALT_OLY_SAME_IP = Boolean.parseBoolean(p.getProperty("AltOlySameIp", "true"));
			ALT_OLY_START_TIME = Integer.parseInt(p.getProperty("AltOlyStartTime", "18"));
			ALT_OLY_MIN = Integer.parseInt(p.getProperty("AltOlyMin", "00"));
			ALT_OLY_CPERIOD = Integer.parseInt(p.getProperty("AltOlyCPeriod", "21600000"));
			ALT_OLY_BATTLE = Integer.parseInt(p.getProperty("AltOlyBattle", "360000"));
			ALT_OLY_WPERIOD = Integer.parseInt(p.getProperty("AltOlyWperiod", "604800000"));
			ALT_OLY_VPERIOD = Integer.parseInt(p.getProperty("AltOlyVperiod", "86400000"));
			ALT_OLY_ALLOW_BSS = Boolean.parseBoolean(p.getProperty("OlympiadAllowBSS", "false"));
			ALT_OLY_CLASSED = Integer.parseInt(p.getProperty("AltOlyClassedParticipants", "5"));
			ALT_OLY_NONCLASSED = Integer.parseInt(p.getProperty("AltOlyNonClassedParticipants", "9"));
			ALT_OLY_MATCH_HEAL_COUNTS = Boolean.parseBoolean(p.getProperty("AltOlyMatchHealCounts", "false"));
			ALT_OLY_BATTLE_REWARD_ITEM = Integer.parseInt(p.getProperty("AltOlyRewardItem", "6651"));
			ALT_OLY_CLASSED_RITEM_C = Integer.parseInt(p.getProperty("AltOlyClassedRewItemCount", "50"));
			ALT_OLY_NONCLASSED_RITEM_C = Integer.parseInt(p.getProperty("AltOlyNonClassedRewItemCount", "30"));
			ALT_OLY_GP_PER_POINT = Integer.parseInt(p.getProperty("AltOlyGPPerPoint", "1000"));
			ALT_OLY_MIN_POINT_FOR_EXCH = Integer.parseInt(p.getProperty("AltOlyMinPointForExchange", "50"));
			ALT_OLY_HERO_POINTS = Integer.parseInt(p.getProperty("AltOlyHeroPoints", "300"));
			ALT_OLY_RESTORE_PLAYER_HP_CP_MP_ON_TELEPORT = Boolean.parseBoolean(p.getProperty("AltOlyHealOnTeleport", "true"));
			ALT_OLY_RESTORE_PLAYER_HP_CP_MP_ON_FIGHT_START = Boolean.parseBoolean(p.getProperty("AltOlyHealOnFightStart", "true"));
			ALT_OLY_RESET_SKILL_TIME = Boolean.parseBoolean(p.getProperty("AltOlyResetSkillTime", "true"));
			ALT_OLY_REMOVE_CUBICS = Boolean.parseBoolean(p.getProperty("AltOlyRemoveCubics", "true"));
			LIST_OLY_RESTRICTED_ITEMS = new ArrayList<>();
			for (String id : p.getProperty("AltOlyRestrictedItems", "0").split(","))
			{
				LIST_OLY_RESTRICTED_ITEMS.add(Integer.parseInt(id));
			}
			for (String id : p.getProperty("AltOlyRestrictedSkills", "0").split(","))
			{
				LIST_OLY_RESTRICTED_SKILLS.add(Integer.parseInt(id));
			}
			ALT_OLY_LOG_FIGHTS = Boolean.parseBoolean(p.getProperty("AlyOlyLogFights", "false"));
			ALT_OLY_SHOW_MONTHLY_WINNERS = Boolean.parseBoolean(p.getProperty("AltOlyShowMonthlyWinners", "true"));
			ALT_OLY_ENCHANT_LIMIT = Integer.parseInt(p.getProperty("AltOlyEnchantLimit", "-1"));
			if (ALT_OLY_ENCHANT_LIMIT > 65535)
			{
				ALT_OLY_ENCHANT_LIMIT = 65535;
			}
			ALT_OLY_START_PCOUNT = Integer.parseInt(p.getProperty("AltOlyStartPointsCount", "18"));
			ALT_OLY_WEEKLY_PCOUNT = Integer.parseInt(p.getProperty("AltOlyWeeklyPointsCount", "3"));
			ALT_OLY_DURATION_TYPES = p.getProperty("OlympiadDurationType", "Month");
			ALT_OLY_DURATION = Integer.parseInt(p.getProperty("OlympiadDuration", "1"));
			ALT_OLY_REMOVE_POINTS_ON_TIE = Boolean.parseBoolean(p.getProperty("OlympiadRemovePointsOnTie", "true"));
			ALT_OLY_INCLUDE_SUMMON_DAMAGE = Boolean.parseBoolean(p.getProperty("IncludeSummonDamage", "true"));
			
		}
		catch (Exception e)
		{
			_log.warn("Could Not load Olympiad File (" + ConfigFiles.OLLY_FILE + ").");
			System.exit(1);
		}
	}
	
	public static void loadOptionsConfig()
	{
		try
		{
			Properties p = new L2Properties(ConfigFiles.OPTIONS_FILE);
			
			RESTORE_PLAYER_INSTANCE = Boolean.parseBoolean(p.getProperty("RestorePlayerInstance", "false"));
			ALLOW_SUMMON_TO_INSTANCE = Boolean.parseBoolean(p.getProperty("AllowSummonToInstance", "true"));
			CHAR_STORE_INTERVAL = Integer.parseInt(p.getProperty("CharacterDataStoreInterval", "15"));
			UPDATE_ITEMS_ON_CHAR_STORE = Boolean.parseBoolean(p.getProperty("UpdateItemsOnCharStore", "false"));
			LAZY_ITEMS_UPDATE = Boolean.parseBoolean(p.getProperty("LazyItemsUpdate", "false"));
			COORD_SYNCHRONIZE = Integer.parseInt(p.getProperty("CoordSynchronize", "-1"));
			SERVER_GMONLY = Boolean.parseBoolean(p.getProperty("ServerGMOnly", "false"));
			
			L2OFF_ADENA_PROTECTION = Boolean.parseBoolean(p.getProperty("L2OFFAdenaProtection", "false"));
			SET_ETCITEM_MAX_SELL = Boolean.parseBoolean(p.getProperty("SetMaxEtcItemSell", "false"));
			SET_ETCITEM_MAX_SELL_VALUE = Integer.parseInt(p.getProperty("SetMaxEtcItemSellQnt", "10000"));
			
			LOG_ITEMS = Boolean.parseBoolean(p.getProperty("LogItems", "false"));
			IGNORE_LOG = p.getProperty("IgnoreLogItems", "CONSUME").toUpperCase();
			AUTODELETE_INVALID_QUEST_DATA = Boolean.parseBoolean(p.getProperty("AutoDeleteInvalidQuestData", "false"));
			SERVER_LIST_BRACKET = Boolean.parseBoolean(p.getProperty("ServerListBrackets", "false"));
			SERVER_LIST_CLOCK = Boolean.parseBoolean(p.getProperty("ServerListClock", "false"));
			ZONE_TOWN = Integer.parseInt(p.getProperty("ZoneTown", "0"));
			DELETE_DAYS = Integer.parseInt(p.getProperty("DeleteCharAfterDays", "7"));
			FALLDOWNONDEATH = Boolean.parseBoolean(p.getProperty("FallDownOnDeath", "true"));
			USE_BOW_CROSSBOW_DISTANCE_PENALTY = Boolean.parseBoolean(p.getProperty("UseBowDistancePenalty", "true"));
			BOW_CROSSBOW_DISTANCE_PENALTY = Double.parseDouble(p.getProperty("MaxBowDistancePenalty", "0.6"));
			if (BOW_CROSSBOW_DISTANCE_PENALTY > 1)
			{
				BOW_CROSSBOW_DISTANCE_PENALTY = 0.6;
			}
			GEODATA = Boolean.parseBoolean(p.getProperty("EnableGeoData", "true"));
			PATHFINDING = Boolean.parseBoolean(p.getProperty("EnablePathFinding", "true"));
			PATHFIND_MODE = PathFindingMode.valueOf(p.getProperty("PathFindingMode", "CellFinding").toUpperCase());
			String correctZ = p.getProperty("GeoCorrectZ", "All");
			GEO_CORRECT_Z = CorrectSpawnsZ.valueOf(correctZ.toUpperCase());
			GEODATA_ROOT = new File(p.getProperty("GeoDataRoot", ".")).getCanonicalFile();
			GEOENGINE = p.getProperty("GeoEngine", "dream");
			PATH_LENGTH = Integer.parseInt(p.getProperty("MaxPathLength", "3500"));
			Z_DENSITY = Integer.parseInt(p.getProperty("ZAxisDensity", "12"));
			
			ALLOW_NPC_WALKERS = Boolean.parseBoolean(p.getProperty("AllowNpcWalkers", "false"));
			ALLOW_GUARDS = Boolean.parseBoolean(p.getProperty("AllowGuards", "false"));
			ALLOW_CURSED_WEAPONS = Boolean.parseBoolean(p.getProperty("AllowCursedWeapons", "false"));
			ALLOW_WEAR = Boolean.parseBoolean(p.getProperty("AllowWear", "false"));
			ALLOW_LOTTERY = Boolean.parseBoolean(p.getProperty("AllowLottery", "false"));
			ALLOW_WATER = Boolean.parseBoolean(p.getProperty("AllowWater", "true"));
			ALLOW_FISHING = Boolean.parseBoolean(p.getProperty("AllowFishing", "true"));
			ALLOW_BOAT = Boolean.parseBoolean(p.getProperty("AllowBoat", "false"));
			
			RESET_TO_BASE = Boolean.parseBoolean(p.getProperty("ResetToBaseClassIfFail", "true"));
			
			ENABLE_RESTART = Boolean.parseBoolean(p.getProperty("EnableRestart", "false"));
			RESTART_TIME = p.getProperty("RestartTime", "06:20:00");
			RESTART_WARN_TIME = p.getProperty("RestartWarnTime", "600");
			
			MAX_ITEM_IN_PACKET = Math.max(INVENTORY_MAXIMUM_DWARF, Math.max(INVENTORY_MAXIMUM_DWARF, INVENTORY_MAXIMUM_GM));
			
			MULTIPLE_ITEM_DROP = Boolean.parseBoolean(p.getProperty("MultipleItemDrop", "true"));
			DEEPBLUE_DROP_RULES = Boolean.parseBoolean(p.getProperty("UseDeepBlueDropRules", "true"));
			PRECISE_DROP_CALCULATION = Boolean.parseBoolean(p.getProperty("PreciseDropCalculation", "true"));
			
			SHOW_CLAN_LEADER = Boolean.parseBoolean(p.getProperty("ShowClanLeader", "false"));
			SHOW_CLAN_LEADER_CLAN_LEVEL = Integer.parseInt(p.getProperty("ShowClanLeaderAtClanLevel", "3"));
			SHOW_LEGEND = Boolean.parseBoolean(p.getProperty("ShowLegend", "false"));
			SHOW_KARMA_PLAYERS = Boolean.parseBoolean(p.getProperty("ShowKarmaPlayers", "false"));
			SHOW_JAILED_PLAYERS = Boolean.parseBoolean(p.getProperty("ShowJailedPlayers", "false"));
			/** Community board */
			COMMUNITY_TYPE = p.getProperty("CommunityType", "old").toLowerCase();
			BBS_DEFAULT = p.getProperty("BBSDefault", "_bbshome");
			SHOW_LEVEL_COMMUNITYBOARD = TypeFormat.parseBoolean(p.getProperty("ShowLevelOnCommunityBoard", "False"));
			SHOW_STATUS_COMMUNITYBOARD = TypeFormat.parseBoolean(p.getProperty("ShowStatusOnCommunityBoard", "True"));
			NAME_PAGE_SIZE_COMMUNITYBOARD = TypeFormat.parseInt(p.getProperty("NamePageSizeOnCommunityBoard", "50"));
			NAME_PER_ROW_COMMUNITYBOARD = TypeFormat.parseInt(p.getProperty("NamePerRowOnCommunityBoard", "5"));
			ALLOW_CUSTOM_COMMUNITY = TypeFormat.parseBoolean(p.getProperty("CustomCommunityBoard", "False"));
			StringTokenizer buff = new StringTokenizer(p.getProperty("CommunityBufferExcludeOn", ""), " ");
			while (buff.hasMoreTokens())
			{
				COMMUNITY_BUFFER_EXCLUDE_ON.add(buff.nextToken());
			}
			StringTokenizer gk = new StringTokenizer(p.getProperty("GatekeeperExcludeOn", ""), " ");
			while (gk.hasMoreTokens())
			{
				COMMUNITY_GATEKEEPER_EXCLUDE_ON.add(gk.nextToken());
			}
			ONLINE_COMMUNITY_BOARD = TypeFormat.parseBoolean(p.getProperty("OnlineCommunityBoard", "False"));
			COLOR_COMMUNITY_BOARD = TypeFormat.parseBoolean(p.getProperty("ColorCommunityBoard", "False"));
			BBS_RESTRICTIONS = p.getProperty("RestrictCBWhen", "COMBAT OLY");
			MIN_CLAN_LEVEL_FOR_USE_AUCTION = Integer.parseInt(p.getProperty("LvlForUseAuction", "2"));
			REMOVE_CASTLE_CIRCLETS = Boolean.parseBoolean(p.getProperty("RemoveCastleCirclets", "true"));
			MINIMUN_LEVEL_FOR_PLEDGE_CREATION = Integer.parseInt(p.getProperty("MinLevelToCreatePledge", "10"));
			ALT_CLAN_MEMBERS_FOR_WAR = Integer.parseInt(p.getProperty("AltClanMembersForWar", "15"));
			ALT_CLAN_WAR_PENALTY_WHEN_ENDED = Integer.parseInt(p.getProperty("AltClanWarPenaltyWhenEnded", "5"));
			ALT_CLAN_JOIN_DAYS = Integer.parseInt(p.getProperty("DaysBeforeJoinAClan", "1"));
			ALT_CLAN_CREATE_DAYS = Integer.parseInt(p.getProperty("DaysBeforeCreateAClan", "10"));
			ALT_MEMBERS_CAN_WITHDRAW_FROM_CLANWH = Boolean.parseBoolean(p.getProperty("AltMembersCanWithdrawFromClanWH", "false"));
			ALT_MAX_NUM_OF_CLANS_IN_ALLY = Integer.parseInt(p.getProperty("AltMaxNumOfClansInAlly", "3"));
			ALT_CLAN_DISSOLVE_DAYS = Integer.parseInt(p.getProperty("DaysToPassToDissolveAClan", "7"));
			ALT_ALLY_JOIN_DAYS_WHEN_LEAVED = Integer.parseInt(p.getProperty("DaysBeforeJoinAllyWhenLeaved", "1"));
			ALT_ALLY_JOIN_DAYS_WHEN_DISMISSED = Integer.parseInt(p.getProperty("DaysBeforeJoinAllyWhenDismissed", "1"));
			ALT_ACCEPT_CLAN_DAYS_WHEN_DISMISSED = Integer.parseInt(p.getProperty("DaysBeforeAcceptNewClanWhenDismissed", "1"));
			ALT_CREATE_ALLY_DAYS_WHEN_DISSOLVED = Integer.parseInt(p.getProperty("DaysBeforeCreateNewAllyWhenDissolved", "10"));
			ALT_REPUTATION_SCORE_PER_KILL = Integer.parseInt(p.getProperty("ReputationScorePerKill", "1"));
			ALT_REPUTATION_SCORE_PER_KILL_SM = p.getProperty("ReputationScorePerKill", "1");
			ALT_CLAN_LEADER_DATE_CHANGE = Integer.parseInt(p.getProperty("AltClanLeaderDateChange", "3"));
			if (ALT_CLAN_LEADER_DATE_CHANGE < 1 || ALT_CLAN_LEADER_DATE_CHANGE > 7)
			{
				_log.warn("Wrong value specified for AltClanLeaderDateChange: " + ALT_CLAN_LEADER_DATE_CHANGE);
				ALT_CLAN_LEADER_DATE_CHANGE = 3;
			}
			ALT_CLAN_LEADER_HOUR_CHANGE = p.getProperty("AltClanLeaderHourChange", "00:00:00");
			ALT_CLAN_LEADER_INSTANT_ACTIVATION = Boolean.parseBoolean(p.getProperty("AltClanLeaderInstantActivation", "false"));
			
			MAX_CLAN_MEMBERS_LVL0 = Integer.parseInt(p.getProperty("MaxMembersClan0", "10"));
			MAX_CLAN_MEMBERS_LVL1 = Integer.parseInt(p.getProperty("MaxMembersClan1", "15"));
			MAX_CLAN_MEMBERS_LVL2 = Integer.parseInt(p.getProperty("MaxMembersClan2", "20"));
			MAX_CLAN_MEMBERS_LVL3 = Integer.parseInt(p.getProperty("MaxMembersClan3", "30"));
			MAX_CLAN_MEMBERS_LVL4 = Integer.parseInt(p.getProperty("MaxMembersClan4", "40"));
			MAX_CLAN_MEMBERS_LVL5 = Integer.parseInt(p.getProperty("MaxMembersClan5", "40"));
			MAX_CLAN_MEMBERS_LVL6 = Integer.parseInt(p.getProperty("MaxMembersClan6", "40"));
			MAX_CLAN_MEMBERS_LVL7 = Integer.parseInt(p.getProperty("MaxMembersClan7", "40"));
			MAX_CLAN_MEMBERS_LVL8 = Integer.parseInt(p.getProperty("MaxMembersClan8", "40"));
			MAX_CLAN_MEMBERS_ROYALS = Integer.parseInt(p.getProperty("MaxMembersRoyals", "20"));
			MAX_CLAN_MEMBERS_KNIGHTS = Integer.parseInt(p.getProperty("MaxMembersKnights", "10"));
			
			PARTY_XP_CUTOFF_METHOD = p.getProperty("PartyXpCutoffMethod", "percentage");
			PARTY_XP_CUTOFF_PERCENT = Double.parseDouble(p.getProperty("PartyXpCutoffPercent", "3."));
			PARTY_XP_CUTOFF_LEVEL = Integer.parseInt(p.getProperty("PartyXpCutoffLevel", "30"));
			ALT_PARTY_RANGE = Integer.parseInt(p.getProperty("AltPartyRange", "1600"));
			ALT_PARTY_RANGE2 = Integer.parseInt(p.getProperty("AltPartyRange2", "1400"));
			MAX_PARTY_LEVEL_DIFFERENCE = Integer.parseInt(p.getProperty("PartyMaxLevelDifference", "0"));
			NO_PARTY_LEVEL_LIMIT = Boolean.parseBoolean(p.getProperty("PartLevelLimit", "true"));
			
			SEND_PAGE_ON_PETTITON = Boolean.parseBoolean(p.getProperty("SendPageOnPetition", "false"));
			PETITIONING_ALLOWED = Boolean.parseBoolean(p.getProperty("PetitioningAllowed", "true"));
			MAX_PETITIONS_PER_PLAYER = Integer.parseInt(p.getProperty("MaxPetitionsPerPlayer", "5"));
			MAX_PETITIONS_PENDING = Integer.parseInt(p.getProperty("MaxPetitionsPending", "25"));
			PETITION_NEED_GM_ONLINE = Boolean.parseBoolean(p.getProperty("PetitioningNeedGmOnline", "true"));
			
			DEFAULT_GLOBAL_CHAT = ChatMode.valueOf(p.getProperty("GlobalChat", "REGION").toUpperCase());
			DEFAULT_TRADE_CHAT = ChatMode.valueOf(p.getProperty("TradeChat", "REGION").toUpperCase());
			REGION_CHAT_ALSO_BLOCKED = Boolean.parseBoolean(p.getProperty("RegionChatAlsoBlocked", "false"));
			USE_SAY_FILTER = Boolean.parseBoolean(p.getProperty("UseChatFilter", "true"));
			CHAT_LENGTH = Integer.parseInt(p.getProperty("ChatLength", "120"));
			KARMA_ON_OFFENSIVE = Integer.parseInt(p.getProperty("ChatFilterKarma", "0"));
			LOG_CHAT = Boolean.parseBoolean(p.getProperty("LogChatOnFile", "false"));
			GLOBAL_CHAT_TIME = Integer.parseInt(p.getProperty("ShoutChatReuseDelay", "1"));
			TRADE_CHAT_TIME = Integer.parseInt(p.getProperty("TradeChatReuseDelay", "1"));
			HERO_CHAT_TIME = Integer.parseInt(p.getProperty("HeroChatReuseDelay", "1"));
			CHAT_FILTER_CHARS = p.getProperty("ChatFilterChars", "[Censored]");
			if (CHAT_LENGTH > 400)
			{
				CHAT_LENGTH = 120;
			}
			SHOUT_CHAT_LEVEL = Integer.parseInt(p.getProperty("ShoutChatLevel", "1"));
			TRADE_CHAT_LEVEL = Integer.parseInt(p.getProperty("TradeChatLevel", "1"));
		}
		catch (Exception e)
		{
			_log.error(e.getMessage(), e);
			throw new Error("Failed to Load " + ConfigFiles.OPTIONS_FILE + " File.");
		}
	}
	
	public static boolean PROTECTION_SECOND_PASSWORD = false;
	
	public static boolean ALLOW_VIP_NCOLOR;
	public static int VIP_NCOLOR;
	public static boolean ALLOW_VIP_TCOLOR;
	public static int VIP_TCOLOR;
	
	public static boolean ENABLE_AIO_SYSTEM;
	public static boolean ENABLE_AIO_DELEVEL;
	public static int AIO_SET_DELEVEL;
	public static Map<Integer, Integer> AIO_SKILLS;
	public static boolean ALLOW_AIO_NCOLOR;
	public static int AIO_NCOLOR;
	public static boolean ALLOW_AIO_TCOLOR;
	public static int AIO_TCOLOR;
	public static boolean ALLOW_AIO_DUAL;
	public static int AIO_DUAL_ID = 9209;
	public static int AIO_ITEM_ID;
	public static int AIO_ITEM = 9216;
	public static int AIO_DIAS;
	public static int AIO_ITEM2 = 9217;
	public static int AIO_DIAS2;
	public static int AIO_ITEM3 = 9218;
	public static int AIO_DIAS3;
	
	public static boolean ALLOW_AIO_SPEAK_NPC;
	public static boolean ALLOW_AIO_TELEPORT;
	public static boolean ALLOW_AIO_LEAVE_TOWN;
	
	public static String AIO_COIN_TEXT;
	public static String AIO_CLASSES_NAME;
	public static String CLASSES_AIO;
	public static List<Integer> LIST_CLASSES_AIO = new ArrayList<>();
	public static int AIO_ITEM_CNT;
	public static int AIO_ITEM_CNT1;
	public static int AIO_ITEM_CNT2;
	public static boolean BUFFSHOP_ENABLE;
	public static boolean OFFLINE_BUFF_SHOP;
	public static boolean BUFFSHOP_RESTORE = false;
	public static int BUFFSHOP_MAX_DAYS;
	public static int DEFAULT_BUFFSHOP_SLOTS;
	public static Map<String, Integer> VALID_BUFFSHOP_SKILLS = new HashMap<>();
	public static String BUFFSHOP_EFFECT;
	public static int RAID_BOSS_INFO_PAGE_LIMIT;
	public static int RAID_BOSS_DROP_PAGE_LIMIT;
	public static String RAID_BOSS_DATE_FORMAT;
	public static String RAID_BOSS_IDS;
	public static List<Integer> LIST_RAID_BOSS_IDS;
	
	public static int VIP_ITEM = 9212;
	public static int VIP_DIAS;
	
	public static int VIP_ITEM2 = 9213;
	public static int VIP_DIAS2;
	
	public static int VIP_ITEM3 = 9214;
	public static int VIP_DIAS3;
	
	public static int CLANFULL_ITEM = 9220;
	public static int CLANSKILL_ITEM;
	public static int[][] CLANSKILL_ITEM_SKILL;
	
	public static int DAY_TO_SIEGE;
	public static int HOUR_TO_SIEGE;
	
	public static boolean ALLOW_PVP_REWARD;
	public static int[][] PVP_REWARD_ITEM;
	public static boolean ALLOW_PK_REWARD;
	public static int[][] PK_REWARD_ITEM;
	
	public static boolean LEAVE_BUFFS_ONDIE;
	public static boolean ALLOW_KILL_BARAKIEL_SETNOBLES;
	
	public static boolean ALLOW_LIGHT_USE_HEAVY;
	public static String NOTALLOWCLASS;
	public static List<Integer> NOTALLOWEDUSEHEAVY;
	public static boolean ALLOW_HEAVY_USE_LIGHT;
	public static String NOTALLOWCLASSE;
	public static List<Integer> NOTALLOWEDUSELIGHT;
	
	public static boolean ALT_DISABLE_BOW_CLASSES;
	public static String DISABLE_BOW_CLASSES_STRING;
	public static ArrayList<Integer> DISABLE_BOW_CLASSES = new ArrayList<>();
	public static boolean ALT_DISABLE_DAGGER_CLASSES;
	public static String DISABLE_DAGGER_CLASSES_STRING;
	public static ArrayList<Integer> DISABLE_DAGGER_CLASSES = new ArrayList<>();
	public static boolean ALT_DISABLE_SWORD_CLASSES;
	public static String DISABLE_SWORD_CLASSES_STRING;
	public static ArrayList<Integer> DISABLE_SWORD_CLASSES = new ArrayList<>();
	public static boolean ALT_DISABLE_BLUNT_CLASSES;
	public static String DISABLE_BLUNT_CLASSES_STRING;
	public static ArrayList<Integer> DISABLE_BLUNT_CLASSES = new ArrayList<>();
	public static boolean ALT_DISABLE_DUAL_CLASSES;
	public static String DISABLE_DUAL_CLASSES_STRING;
	public static ArrayList<Integer> DISABLE_DUAL_CLASSES = new ArrayList<>();
	public static boolean ALT_DISABLE_POLE_CLASSES;
	public static String DISABLE_POLE_CLASSES_STRING;
	public static ArrayList<Integer> DISABLE_POLE_CLASSES = new ArrayList<>();
	public static boolean ALT_DISABLE_BIGSWORD_CLASSES;
	public static String DISABLE_BIGSWORD_CLASSES_STRING;
	public static ArrayList<Integer> DISABLE_BIGSWORD_CLASSES = new ArrayList<>();
	
	public static String VOTE_API_KEYTOPZONE;
	public static int VOTE_SERVERID_KEYTOPZONE;
	public static String VOTE_APIKEY_HOPZONE;
	public static String VOTE_SERVERID_NETWORK;
	public static int VOTE_API_REWARD_ID;
	public static int VOTE_API_REWARD_COUNT;
	
	public static HashMap<Integer, Integer> DONATOR_WEAPON_ENCHANT_LEVEL = new HashMap<>();
	public static HashMap<Integer, Integer> DONATOR_ARMOR_ENCHANT_LEVEL = new HashMap<>();
	public static HashMap<Integer, Integer> DONATOR_JEWELRY_ENCHANT_LEVEL = new HashMap<>();
	
	public static int ENCHANT_MAX_WEAPON_DONATOR;
	public static int ENCHANT_MAX_ARMOR_DONATOR;
	public static int ENCHANT_MAX_JEWELRY_DONATOR;
	
	public static boolean ANNOUNCE_HER0;
	public static boolean ANNOUNCE_AIO;
	public static boolean ANNOUNCE_VIP;
	public static boolean ANNOUNCE_LORD;
	public static boolean ANNOUNCE_NEWBIE;
	
	public static boolean ANNOUNCE_PK_PVP;
	public static boolean ANNOUNCE_PK_PVP_NORMAL_MESSAGE;
	public static String ANNOUNCE_PK_MSG;
	public static String ANNOUNCE_PVP_MSG;
	
	public static boolean PVP_CONGRATULATIONS_MSG;
	
	public static boolean ALLOW_QUAKE_SYSTEM;
	public static boolean QUAKE_SYSTEM_RESETONDIE;
	
	public static boolean SELL_BY_ITEM;
	public static int SELL_ITEM;
	
	public static String COIN_TEXT;
	
	public static boolean WAR_LEGEND_AURA;
	public static int KILLS_TO_GET_WAR_LEGEND_AURA;
	
	public static boolean CUSTOM_STARTER_ITEMS_ENABLED;
	public static List<int[]> STARTING_CUSTOM_ITEMS_F = new ArrayList<>();
	public static List<int[]> STARTING_CUSTOM_ITEMS_M = new ArrayList<>();
	
	public static boolean ANNOUNCE_RAID_SPAWN;
	
	public static int NOBLESSE_ITEM;
	
	public static boolean ALLOW_HERO_SUBSKILL;
	public static boolean ALLOW_CUSTOM_CANCEL;
	public static int CUSTOM_CANCEL_SECONDS;
	
	public static void loadAddOnConfig()
	{
		try
		{
			Properties p = new L2Properties(ConfigFiles.ADDON_FILE);
			
			ALLOW_VIP_NCOLOR = Boolean.parseBoolean(p.getProperty("AllowVipNameColor", "True"));
			VIP_NCOLOR = Integer.decode("0x" + p.getProperty("VipNameColor", "0088FF"));
			ALLOW_VIP_TCOLOR = Boolean.parseBoolean(p.getProperty("AllowVipTitleColor", "True"));
			VIP_TCOLOR = Integer.decode("0x" + p.getProperty("VipTitleColor", "0088FF"));
			VIP_DIAS = Integer.parseInt(p.getProperty("VipDias", "30"));
			VIP_DIAS2 = Integer.parseInt(p.getProperty("VipDias2", "60"));
			VIP_DIAS3 = Integer.parseInt(p.getProperty("VipDias3", "90"));
			CLANSKILL_ITEM = Integer.parseInt(p.getProperty("ClanSkillByItem", "0"));
			CLANSKILL_ITEM_SKILL = parseItemsList(p.getProperty("ClanSkillID", "0,0"));
			
			RAID_BOSS_INFO_PAGE_LIMIT = Integer.parseInt(p.getProperty("RaidBossInfoPageLimit", "15"));
			RAID_BOSS_DROP_PAGE_LIMIT = Integer.parseInt(p.getProperty("RaidBossDropPageLimit", "15"));
			RAID_BOSS_DATE_FORMAT = p.getProperty("RaidBossDateFormat", "MMM dd, HH:mm");
			RAID_BOSS_IDS = p.getProperty("RaidBossIds", "0,0");
			LIST_RAID_BOSS_IDS = new ArrayList<>();
			for (String id : RAID_BOSS_IDS.trim().split(","))
			{
				LIST_RAID_BOSS_IDS.add(Integer.parseInt(id.trim()));
			}
			
			DAY_TO_SIEGE = Integer.parseInt(p.getProperty("DayToSiege", "14"));
			HOUR_TO_SIEGE = Integer.parseInt(p.getProperty("HourToSiege", "18"));
			
			ALLOW_PVP_REWARD = Boolean.parseBoolean(p.getProperty("AllowPvpRewardSystem", "False"));
			PVP_REWARD_ITEM = parseItemsList(p.getProperty("PvpRewardItem", "57,400"));
			ALLOW_PK_REWARD = Boolean.parseBoolean(p.getProperty("AllowPkRewardSystem", "False"));
			PK_REWARD_ITEM = parseItemsList(p.getProperty("PkRewardItem", "57,400"));
			
			LEAVE_BUFFS_ONDIE = Boolean.parseBoolean(p.getProperty("LeaveBuffsOnDie", "True"));
			ALLOW_KILL_BARAKIEL_SETNOBLES = Boolean.parseBoolean(p.getProperty("KillBarakielSetNobless", "True"));
			
			ENCHANT_MAX_WEAPON_DONATOR = Integer.parseInt(p.getProperty("EnchantMaxWeaponDonator", "9999"));
			ENCHANT_MAX_ARMOR_DONATOR = Integer.parseInt(p.getProperty("EnchantMaxArmorDonator", "9999"));
			ENCHANT_MAX_JEWELRY_DONATOR = Integer.parseInt(p.getProperty("EnchantMaxJewelryDonator", "9999"));
			
			String[] propertySplit;
			propertySplit = p.getProperty("DonatorWeaponEnchantLevel", "").split(";");
			for (String readData : propertySplit)
			{
				String[] writeData = readData.split(",");
				if (writeData.length != 2)
				{
					_log.info("invalid config property");
				}
				else
				{
					try
					{
						DONATOR_WEAPON_ENCHANT_LEVEL.put(Integer.parseInt(writeData[0]), Integer.parseInt(writeData[1]));
					}
					catch (NumberFormatException nfe)
					{
						if (!readData.equals(""))
						{
							_log.info("invalid config property");
						}
					}
				}
			}
			propertySplit = p.getProperty("DonatorArmorEnchantLevel", "").split(";");
			for (String readData : propertySplit)
			{
				String[] writeData = readData.split(",");
				if (writeData.length != 2)
				{
					_log.info("invalid config property");
				}
				else
				{
					try
					{
						DONATOR_ARMOR_ENCHANT_LEVEL.put(Integer.parseInt(writeData[0]), Integer.parseInt(writeData[1]));
					}
					catch (NumberFormatException nfe)
					{
						if (!readData.equals(""))
						{
							_log.info("invalid config property");
						}
					}
				}
			}
			propertySplit = p.getProperty("DonatorJewelryEnchantLevel", "").split(";");
			for (String readData : propertySplit)
			{
				String[] writeData = readData.split(",");
				if (writeData.length != 2)
				{
					_log.info("invalid config property");
				}
				else
				{
					try
					{
						DONATOR_JEWELRY_ENCHANT_LEVEL.put(Integer.parseInt(writeData[0]), Integer.parseInt(writeData[1]));
					}
					catch (NumberFormatException nfe)
					{
						if (!readData.equals(""))
						{
							_log.info("invalid config property");
						}
					}
				}
			}
			
			ANNOUNCE_HER0 = Boolean.parseBoolean(p.getProperty("AnnounceHeroLogin", "True"));
			ANNOUNCE_AIO = Boolean.parseBoolean(p.getProperty("AnnounceAIOxLogin", "True"));
			ANNOUNCE_VIP = Boolean.parseBoolean(p.getProperty("AnnounceVIPLogin", "True"));
			ANNOUNCE_LORD = Boolean.parseBoolean(p.getProperty("AnnounceLordLogin", "True"));
			ANNOUNCE_NEWBIE = Boolean.parseBoolean(p.getProperty("AnnounceNewbieLogin", "True"));
			
			ANNOUNCE_PK_PVP = Boolean.parseBoolean(p.getProperty("AnnouncePkPvP", "False"));
			ANNOUNCE_PK_PVP_NORMAL_MESSAGE = Boolean.parseBoolean(p.getProperty("AnnouncePkPvPNormalMessage", "True"));
			ANNOUNCE_PK_MSG = p.getProperty("AnnouncePkMsg", "Player $killer has slaughtered $target .");
			ANNOUNCE_PVP_MSG = p.getProperty("AnnouncePvpMsg", "Player $killer has defeated $target .");
			
			PVP_CONGRATULATIONS_MSG = Boolean.parseBoolean(p.getProperty("PvPCongratulationsMsg", "True"));
			
			ALLOW_QUAKE_SYSTEM = Boolean.parseBoolean(p.getProperty("AllowQuakeSystem", "True"));
			QUAKE_SYSTEM_RESETONDIE = Boolean.parseBoolean(p.getProperty("QuakeResetOnDie", "True"));
			
			SELL_BY_ITEM = Boolean.parseBoolean(p.getProperty("SellByItem", "False"));
			SELL_ITEM = Integer.parseInt(p.getProperty("SellItem", "57"));
			
			COIN_TEXT = p.getProperty("CoinText", "Festival Adena");
			
			WAR_LEGEND_AURA = Boolean.parseBoolean(p.getProperty("WarLegendAura", "False"));
			KILLS_TO_GET_WAR_LEGEND_AURA = Integer.parseInt(p.getProperty("KillsToGetWarLegendAura", "30"));
			
			CUSTOM_STARTER_ITEMS_ENABLED = Boolean.parseBoolean(p.getProperty("CustomStarterItemsEnabled", "False"));
			if (Config.CUSTOM_STARTER_ITEMS_ENABLED)
			{
				String[] propertySplit1 = p.getProperty("StartingCustomItemsMage", "57,0").split(";");
				STARTING_CUSTOM_ITEMS_M.clear();
				for (final String reward : propertySplit1)
				{
					final String[] rewardSplit = reward.split(",");
					if (rewardSplit.length != 2)
						_log.warn("StartingCustomItemsMage[Config.load()]: invalid config property -> StartingCustomItemsMage \"" + reward + "\"");
					else
					{
						try
						{
							STARTING_CUSTOM_ITEMS_M.add(new int[]
							{
								Integer.parseInt(rewardSplit[0]),
								Integer.parseInt(rewardSplit[1])
							});
						}
						catch (final NumberFormatException nfe)
						{
							if (!reward.isEmpty())
								_log.warn("StartingCustomItemsMage[Config.load()]: invalid config property -> StartingCustomItemsMage \"" + reward + "\"");
						}
					}
				}
				
				propertySplit = p.getProperty("StartingCustomItemsFighter", "57,0").split(";");
				STARTING_CUSTOM_ITEMS_F.clear();
				for (final String reward : propertySplit)
				{
					final String[] rewardSplit = reward.split(",");
					if (rewardSplit.length != 2)
						_log.warn("StartingCustomItemsFighter[Config.load()]: invalid config property -> StartingCustomItemsFighter \"" + reward + "\"");
					else
					{
						try
						{
							STARTING_CUSTOM_ITEMS_F.add(new int[]
							{
								Integer.parseInt(rewardSplit[0]),
								Integer.parseInt(rewardSplit[1])
							});
						}
						catch (final NumberFormatException nfe)
						{
							
							if (!reward.isEmpty())
								_log.warn("StartingCustomItemsFighter[Config.load()]: invalid config property -> StartingCustomItemsFighter \"" + reward + "\"");
						}
					}
				}
			}
			
			ANNOUNCE_RAID_SPAWN = Boolean.parseBoolean(p.getProperty("AnnounceRaidSpawn", "False"));
			
			NOBLESSE_ITEM = Integer.parseInt(p.getProperty("NoblesseItem", "9889"));
			
			ALLOW_HERO_SUBSKILL = Boolean.parseBoolean(p.getProperty("AllowHeroSkillOnSub", "False"));
			ALLOW_CUSTOM_CANCEL = Boolean.parseBoolean(p.getProperty("AllowCustomCancelTask", "False"));
			CUSTOM_CANCEL_SECONDS = Integer.parseInt(p.getProperty("CustomCancelSeconds", "5"));
		}
		catch (Exception e)
		{
			_log.error(e.getMessage(), e);
			throw new Error("Failed to Load " + ConfigFiles.ADDON_FILE + " File.");
		}
	}
	
	public static void loadAioxConfig()
	{
		try
		{
			Properties p = new L2Properties(ConfigFiles.AIOX_FILE);
			
			ENABLE_AIO_SYSTEM = Boolean.parseBoolean(p.getProperty("EnableAioSystem", "True"));
			ENABLE_AIO_DELEVEL = Boolean.parseBoolean(p.getProperty("EnableAioDelevel", "False"));
			AIO_SET_DELEVEL = Integer.parseInt(p.getProperty("AioSetDelevel", "1"));
			ALLOW_AIO_NCOLOR = Boolean.parseBoolean(p.getProperty("AllowAioNameColor", "True"));
			AIO_NCOLOR = Integer.decode("0x" + p.getProperty("AioNameColor", "88AA88"));
			ALLOW_AIO_TCOLOR = Boolean.parseBoolean(p.getProperty("AllowAioTitleColor", "True"));
			AIO_TCOLOR = Integer.decode("0x" + p.getProperty("AioTitleColor", "88AA88"));
			ALLOW_AIO_DUAL = Boolean.parseBoolean(p.getProperty("AllowAIODual", "False"));
			ALLOW_AIO_SPEAK_NPC = Boolean.parseBoolean(p.getProperty("AllowAioSpeakNpc", "False"));
			ALLOW_AIO_TELEPORT = Boolean.parseBoolean(p.getProperty("AllowAioTeleport", "False"));
			ALLOW_AIO_LEAVE_TOWN = Boolean.parseBoolean(p.getProperty("AllowAioLeaveTown", "False"));
			
			if (ENABLE_AIO_SYSTEM) // create map if system is enabled
			{
				String[] AioSkillsSplit = p.getProperty("AioSkills", "").split(";");
				AIO_SKILLS = new HashMap<>(AioSkillsSplit.length);
				for (String skill : AioSkillsSplit)
				{
					String[] skillSplit = skill.split(",");
					if (skillSplit.length != 2)
					{
						System.out.println("[Aio System]: invalid config property -> AioSkills \"" + skill + "\"");
					}
					else
					{
						try
						{
							AIO_SKILLS.put(Integer.parseInt(skillSplit[0]), Integer.parseInt(skillSplit[1]));
						}
						catch (NumberFormatException nfe)
						{
							if (!skill.equals(""))
							{
								System.out.println("[Aio System]: invalid config property -> AioSkills \"" + skillSplit[0] + "\"" + skillSplit[1]);
							}
						}
					}
				}
			}
			
			AIO_ITEM_ID = Integer.parseInt(p.getProperty("AioItemId", "9215"));
			
			AIO_DIAS = Integer.parseInt(p.getProperty("AioDias", "30"));
			
			AIO_DIAS2 = Integer.parseInt(p.getProperty("AioDias2", "60"));
			
			AIO_DIAS3 = Integer.parseInt(p.getProperty("AioDias3", "90"));
			
			CLASSES_AIO = p.getProperty("AllowedClassId");
			
			LIST_CLASSES_AIO = new ArrayList<>();
			for (String id : CLASSES_AIO.trim().split(","))
			{
				LIST_CLASSES_AIO.add(Integer.parseInt(id.trim()));
			}
			
			AIO_ITEM_CNT = Integer.parseInt(p.getProperty("AioItemCount", "5"));
			AIO_ITEM_CNT1 = Integer.parseInt(p.getProperty("AioItemCount2", "10"));
			AIO_ITEM_CNT2 = Integer.parseInt(p.getProperty("AioItemCount3", "15"));
			AIO_COIN_TEXT = p.getProperty("AioCoinText", "Festival Adena");
			AIO_CLASSES_NAME = p.getProperty("AioClassesName", "Prophet, Cardinal, etc");
			
			BUFFSHOP_ENABLE = Boolean.parseBoolean(p.getProperty("BuffShopEnable", "false"));
			OFFLINE_BUFF_SHOP = Boolean.parseBoolean(p.getProperty("AllowOfflineBuff", "True"));
			BUFFSHOP_MAX_DAYS = Integer.parseInt(p.getProperty("BuffShopMaxDays", "14"));
			DEFAULT_BUFFSHOP_SLOTS = Integer.parseInt(p.getProperty("DefaultBuffShopSlots", "24"));
			BUFFSHOP_EFFECT = p.getProperty("BuffShopEffect", "");
			VALID_BUFFSHOP_SKILLS.clear();
			try
			{
				for (String entry : p.getProperty("ValidBuffShopSkills", "").split(";"))
				{
					String[] vars = entry.split(",");
					if (vars.length != 2)
					{
						_log.info("Failed to load sub-value \"" + entry + "\" from \"ValidBuffShopSkills\".");
					}
					else
					{
						VALID_BUFFSHOP_SKILLS.put(vars[0].trim(), Integer.valueOf(vars[1].trim()));
					}
				}
			}
			catch (NumberFormatException e)
			{
				
			}
			
		}
		catch (Exception e)
		{
			_log.error(e.getMessage(), e);
			throw new Error("Failed to Load " + ConfigFiles.AIOX_FILE + " File.");
		}
	}
	
	public static void loadEquipmentsConfig()
	{
		try
		{
			Properties p = new L2Properties(ConfigFiles.EQUIPMENTS_FILE);
			
			ALLOW_HEAVY_USE_LIGHT = Boolean.parseBoolean(p.getProperty("AllowHeavyUseLight", "False"));
			NOTALLOWCLASSE = p.getProperty("NotAllowedUseLight", "");
			NOTALLOWEDUSELIGHT = new ArrayList<>();
			for (String classId : NOTALLOWCLASSE.split(","))
			{
				NOTALLOWEDUSELIGHT.add(Integer.parseInt(classId));
			}
			ALLOW_LIGHT_USE_HEAVY = Boolean.parseBoolean(p.getProperty("AllowLightUseHeavy", "False"));
			NOTALLOWCLASS = p.getProperty("NotAllowedUseHeavy", "");
			NOTALLOWEDUSEHEAVY = new ArrayList<>();
			for (String classId : NOTALLOWCLASS.split(","))
			{
				NOTALLOWEDUSEHEAVY.add(Integer.parseInt(classId));
			}
			
			ALT_DISABLE_BOW_CLASSES = Boolean.parseBoolean(p.getProperty("AltDisableBow", "False"));
			DISABLE_BOW_CLASSES_STRING = p.getProperty("DisableBowForClasses", "");
			DISABLE_BOW_CLASSES = new ArrayList<>();
			for (String class_id : DISABLE_BOW_CLASSES_STRING.split(","))
			{
				if (!class_id.equals(""))
					DISABLE_BOW_CLASSES.add(Integer.parseInt(class_id));
			}
			ALT_DISABLE_DAGGER_CLASSES = Boolean.parseBoolean(p.getProperty("AltDisableDagger", "False"));
			DISABLE_DAGGER_CLASSES_STRING = p.getProperty("DisableDaggerForClasses", "");
			DISABLE_DAGGER_CLASSES = new ArrayList<>();
			for (String class_id : DISABLE_DAGGER_CLASSES_STRING.split(","))
			{
				if (!class_id.equals(""))
					DISABLE_DAGGER_CLASSES.add(Integer.parseInt(class_id));
			}
			ALT_DISABLE_SWORD_CLASSES = Boolean.parseBoolean(p.getProperty("AltDisableSword", "False"));
			DISABLE_SWORD_CLASSES_STRING = p.getProperty("DisableSwordForClasses", "");
			DISABLE_SWORD_CLASSES = new ArrayList<>();
			for (String class_id : DISABLE_SWORD_CLASSES_STRING.split(","))
			{
				if (!class_id.equals(""))
					DISABLE_SWORD_CLASSES.add(Integer.parseInt(class_id));
			}
			ALT_DISABLE_BLUNT_CLASSES = Boolean.parseBoolean(p.getProperty("AltDisableBlunt", "False"));
			DISABLE_BLUNT_CLASSES_STRING = p.getProperty("DisableBluntForClasses", "");
			DISABLE_BLUNT_CLASSES = new ArrayList<>();
			for (String class_id : DISABLE_BLUNT_CLASSES_STRING.split(","))
			{
				if (!class_id.equals(""))
					DISABLE_BLUNT_CLASSES.add(Integer.parseInt(class_id));
			}
			ALT_DISABLE_DUAL_CLASSES = Boolean.parseBoolean(p.getProperty("AltDisableDual", "False"));
			DISABLE_DUAL_CLASSES_STRING = p.getProperty("DisableDualForClasses", "");
			DISABLE_DUAL_CLASSES = new ArrayList<>();
			for (String class_id : DISABLE_DUAL_CLASSES_STRING.split(","))
			{
				if (!class_id.equals(""))
					DISABLE_DUAL_CLASSES.add(Integer.parseInt(class_id));
			}
			ALT_DISABLE_POLE_CLASSES = Boolean.parseBoolean(p.getProperty("AltDisablePolle", "False"));
			DISABLE_POLE_CLASSES_STRING = p.getProperty("DisablePolleForClasses", "");
			DISABLE_POLE_CLASSES = new ArrayList<>();
			for (String class_id : DISABLE_POLE_CLASSES_STRING.split(","))
			{
				if (!class_id.equals(""))
					DISABLE_POLE_CLASSES.add(Integer.parseInt(class_id));
			}
			ALT_DISABLE_BIGSWORD_CLASSES = Boolean.parseBoolean(p.getProperty("AltDisableBigSword", "False"));
			DISABLE_BIGSWORD_CLASSES_STRING = p.getProperty("DisableBigSwordForClasses", "");
			DISABLE_BIGSWORD_CLASSES = new ArrayList<>();
			for (String class_id : DISABLE_BIGSWORD_CLASSES_STRING.split(","))
			{
				if (!class_id.equals(""))
					DISABLE_BIGSWORD_CLASSES.add(Integer.parseInt(class_id));
			}
			
		}
		catch (Exception e)
		{
			_log.error(e.getMessage(), e);
			throw new Error("Failed to Load " + ConfigFiles.EQUIPMENTS_FILE + " File.");
		}
	}
	
	public static void loadVoteConfig()
	{
		try
		{
			Properties p = new L2Properties(ConfigFiles.VOTE_FILE);
			
			VOTE_API_KEYTOPZONE = p.getProperty("ApiKeyTOPZONE", "e2ec0d41791613092ac03b6243ec6b87");
			VOTE_SERVERID_KEYTOPZONE = Integer.parseInt(p.getProperty("ServerIdKeyTOPZONE", "14093"));
			VOTE_APIKEY_HOPZONE = p.getProperty("ApiKeyHOPZONE", "0vocH6Te6bpQ89H8");
			VOTE_SERVERID_NETWORK = p.getProperty("ServerIdNETWORK", "l2nightmare");
			VOTE_API_REWARD_ID = Integer.parseInt(p.getProperty("VoteSystemRewardId", "57"));
			VOTE_API_REWARD_COUNT = Integer.parseInt(p.getProperty("VoteSystemRewardCount", "1000"));
			
		}
		catch (Exception e)
		{
			_log.error(e.getMessage(), e);
			throw new Error("Failed to Load " + ConfigFiles.VOTE_FILE + " File.");
		}
	}
	
	public static void loadPlayerConfig()
	{
		try
		{
			Properties p = new L2Properties(ConfigFiles.PLAYER_FILE);
			
			ALT_GAME_VIEWNPC = Boolean.parseBoolean(p.getProperty("AltGameViewNpc", "false"));
			ALT_GAME_SHOWPC_DROP = Boolean.parseBoolean(p.getProperty("AltGameViewNpcDrop", "false"));
			ALLOW_EXCHANGE = Boolean.parseBoolean(p.getProperty("AllowExchange", "true"));
			CRUMA_TOWER_LEVEL_RESTRICT = Integer.parseInt(p.getProperty("CrumaTowerLevelRestrict", "56"));
			MAX_RUN_SPEED = Integer.parseInt(p.getProperty("MaxRunSpeed", "250"));
			MAX_PATK_SPEED = Integer.parseInt(p.getProperty("MaxPAtkSpeed", "1500"));
			MAX_MATK_SPEED = Integer.parseInt(p.getProperty("MaxMAtkSpeed", "1999"));
			MAX_EVASION = Integer.parseInt(p.getProperty("MaxEvasion", "200"));
			HIT_TIME_LIMITER = Integer.parseInt(p.getProperty("MinimumHitTime", "330"));
			ALT_STRICT_HERO_SYSTEM = Boolean.parseBoolean(p.getProperty("StrictHeroSystem", "true"));
			ENCHANT_HERO_WEAPONS = Boolean.parseBoolean(p.getProperty("HeroWeaponsCanBeEnchanted", "false"));
			HERO_LOG_NOCLAN = Boolean.parseBoolean(p.getProperty("LogIsHeroNoClan", "false"));
			ALT_GAME_DELEVEL = Boolean.parseBoolean(p.getProperty("Delevel", "true"));
			ALT_GAME_EXPONENT_XP = Float.parseFloat(p.getProperty("AltGameExponentXp", "0."));
			ALT_GAME_EXPONENT_SP = Float.parseFloat(p.getProperty("AltGameExponentSp", "0."));
			ALT_GAME_SUMMON_PENALTY_RATE = Float.parseFloat(p.getProperty("AltSummonPenaltyRate", "1."));
			IS_CRAFTING_ENABLED = Boolean.parseBoolean(p.getProperty("CraftingEnabled", "true"));
			ALT_GAME_CREATION = Boolean.parseBoolean(p.getProperty("AltGameCreation", "false"));
			ALT_GAME_CREATION_SPEED = Double.parseDouble(p.getProperty("AltGameCreationSpeed", "1"));
			ALT_GAME_CREATION_XP_RATE = Double.parseDouble(p.getProperty("AltGameCreationRateXp", "1"));
			ALT_GAME_CREATION_SP_RATE = Double.parseDouble(p.getProperty("AltGameCreationRateSp", "1"));
			DWARF_RECIPE_LIMIT = Integer.parseInt(p.getProperty("DwarfRecipeLimit", "50"));
			COMMON_RECIPE_LIMIT = Integer.parseInt(p.getProperty("CommonRecipeLimit", "50"));
			AUTO_LEARN_SKILLS = Boolean.parseBoolean(p.getProperty("AutoLearnSkills", "false"));
			AUTO_LEARN_MAX_LEVEL = Integer.parseInt(p.getProperty("AutoLearnMaxLevel", "-1"));
			ALT_GAME_SKILL_LEARN = Boolean.parseBoolean(p.getProperty("AltGameSkillLearn", "false"));
			AUTO_LEARN_DIVINE_INSPIRATION = Boolean.parseBoolean(p.getProperty("AutoLearnDivineInspiration", "false"));
			SP_BOOK_NEEDED = Boolean.parseBoolean(p.getProperty("SpBookNeeded", "true"));
			DIVINE_SP_BOOK_NEEDED = TypeFormat.parseBoolean(p.getProperty("DivineInspirationSpBookNeeded", "True"));
			LIFE_CRYSTAL_NEEDED = Boolean.parseBoolean(p.getProperty("LifeCrystalNeeded", "true"));
			ES_SP_BOOK_NEEDED = Boolean.parseBoolean(p.getProperty("EnchantSkillSpBookNeeded", "true"));
			ES_XP_NEEDED = Boolean.parseBoolean(p.getProperty("EnchSkillXpNeeded", "true"));
			ES_SP_NEEDED = Boolean.parseBoolean(p.getProperty("EnchSkillSpNeeded", "true"));
			ALT_ITEM_SKILLS_NOT_INFLUENCED = Boolean.parseBoolean(p.getProperty("AltItemSkillsNotInfluenced", "false"));
			CHECK_SKILLS_ON_ENTER = Boolean.parseBoolean(p.getProperty("CheckSkillsOnEnter", "false"));
			CHECK_ADDITIONAL_SKILLS = Boolean.parseBoolean(p.getProperty("CheckAdditionalSkills", "false"));
			SEND_NOTDONE_SKILLS = Integer.parseInt(p.getProperty("SendNOTDONESkills", "2"));
			BLOW_FRONT = Byte.parseByte(p.getProperty("BlowFront", "50"));
			BLOW_SIDE = Byte.parseByte(p.getProperty("BlowSide", "60"));
			BLOW_BEHIND = Byte.parseByte(p.getProperty("BlowBehind", "70"));
			ALT_LETHAL_RATE_DAGGER = Float.parseFloat(p.getProperty("AltLethalRateDagger", "0.5"));
			ALT_LETHAL_RATE_OTHER = Float.parseFloat(p.getProperty("AltLethalRateOther", "1.0"));
			ALT_LETHAL_RATE_ARCHERY = Float.parseFloat(p.getProperty("AltLethalRateArchery", "0.8"));
			ALT_PCRITICAL_CAP = Integer.parseInt(p.getProperty("AltPCriticalCap", "500"));
			ALT_MCRITICAL_CAP = Integer.parseInt(p.getProperty("AltMCriticalCap", "200"));
			ALT_GAME_SUBCLASS_WITHOUT_QUESTS = Boolean.parseBoolean(p.getProperty("AltSubClassWithoutQuests", "false"));
			ALT_GAME_SUBCLASS_EVERYWHERE = Boolean.parseBoolean(p.getProperty("AltSubclassEverywhere", "false"));
			SUBCLASS_WITH_ITEM_AND_NO_QUEST = Boolean.parseBoolean(p.getProperty("SubclassWithItemAndNoQuest", "false"));
			SUBCLASS_WITH_CUSTOM_ITEM = Boolean.parseBoolean(p.getProperty("SubclassWithCustomItem", "false"));
			SUBCLASS_WITH_CUSTOM_ITEM_ID = Integer.parseInt(p.getProperty("SubclassWithCustomItemID", "4037"));
			SUBCLASS_WITH_CUSTOM_ITEM_COUNT = Integer.parseInt(p.getProperty("SubclassWithCustomItemCount", "1"));
			ACUMULATIVE_SUBCLASS_SKILLS = Boolean.parseBoolean(p.getProperty("AltSubClassSkills", "false"));
			MAX_SUBCLASS = Integer.parseInt(p.getProperty("MaxSubClass", "3"));
			SUBCLASS_MAX_LEVEL_BYTE = Byte.parseByte(p.getProperty("SubclassMaxLevel", "80"));
			SUBCLASS_MAX_LEVEL = Integer.parseInt(p.getProperty("SubclassMaxLevel", "80"));
			SUBCLASS_INIT_LEVEL = Byte.parseByte(p.getProperty("SublcassInitLevel", "40"));
			if (SUBCLASS_INIT_LEVEL > SUBCLASS_MAX_LEVEL_BYTE)
			{
				SUBCLASS_INIT_LEVEL = SUBCLASS_MAX_LEVEL_BYTE;
			}
			PLAYER_MAX_LEVEL_BYTE = Byte.parseByte(p.getProperty("PlayerMaxLevel", "81"));
			if (PLAYER_MAX_LEVEL_BYTE > 86)
			{
				PLAYER_MAX_LEVEL_BYTE = 86;
			}
			PLAYER_MAX_LEVEL = Integer.parseInt(p.getProperty("PlayerMaxLevel", "81"));
			
			ALT_WEIGHT_LIMIT = Double.parseDouble(p.getProperty("AltWeightLimit", "1."));
			LEVEL_ADD_LOAD = Boolean.parseBoolean(p.getProperty("IncreaseWeightLimitByLevel", "false"));
			ALT_GAME_MAGICFAILURES = Boolean.parseBoolean(p.getProperty("MagicFailures", "false"));
			
			SHOW_SKILL_SUCCESS_CHANCE = Boolean.parseBoolean(p.getProperty("ShowSuccessChance", "false"));
			ALLOW_USE_EXP_SET = Boolean.parseBoolean(p.getProperty("AllowUseExpSet", "false"));
			ALLOW_MENU = Boolean.parseBoolean(p.getProperty("AllowUserMenu", "true"));
			SHOW_DEBUFF_ONLY = Boolean.parseBoolean(p.getProperty("ShowDebuffOnly", "true"));
			
			ALLOW_AUTO_LOOT = p.getProperty("AlowAutoLoot").trim().equalsIgnoreCase("true");
			AUTO_LOOT = p.getProperty("AutoLootDefault").trim().equalsIgnoreCase("true");
			AUTO_LOOT_RAID = Boolean.parseBoolean(p.getProperty("AutoLootRaid", "true"));
			AUTO_LOOT_HERBS = p.getProperty("AutoLootHerbs").trim().equalsIgnoreCase("true");
			AUTO_LOOT_ADENA = p.getProperty("AutoLootAdena").trim().equalsIgnoreCase("true");
			
			PLAYER_SPAWN_PROTECTION = Integer.parseInt(p.getProperty("PlayerSpawnProtection", "5"));
			PLAYER_FAKEDEATH_UP_PROTECTION = Integer.parseInt(p.getProperty("PlayerFakeDeathUpProtection", "0"));
			DEATH_PENALTY_CHANCE = Integer.parseInt(p.getProperty("DeathPenaltyChance", "20"));
			
			PET_RENT_NPC = p.getProperty("ListPetRentNpc", "30827");
			LIST_PET_RENT_NPC = new ArrayList<>();
			
			for (String id : PET_RENT_NPC.split(","))
			{
				LIST_PET_RENT_NPC.add(Integer.parseInt(id));
			}
			
			ALLOW_KEYBOARD_MOVEMENT = Boolean.parseBoolean(p.getProperty("AllowKeyboardMovement", "true"));
			
			CHECK_PLAYER_MACRO = Boolean.parseBoolean(p.getProperty("CheckPlayerMacro", "true"));
			LIST_MACRO_RESTRICTED_WORDS = new ArrayList<>();
			for (String command : p.getProperty("MacroRestrictedCommandList", "exit").split(","))
			{
				LIST_MACRO_RESTRICTED_WORDS.add(command);
			}
			
			CONSUME_SPIRIT_SOUL_SHOTS = Boolean.parseBoolean(p.getProperty("ConsumeSoulShot", "true"));
			CONSUME_ARROWS = Boolean.parseBoolean(p.getProperty("ConsumeArrows", "true"));
			INVENTORY_MAXIMUM_NO_DWARF = Integer.parseInt(p.getProperty("MaxInventorySlotsForOther", "100"));
			INVENTORY_MAXIMUM_DWARF = Integer.parseInt(p.getProperty("MaxInventorySlotsForDwarf", "150"));
			INVENTORY_MAXIMUM_GM = Integer.parseInt(p.getProperty("MaxInventorySlotsForGameMaster", "300"));
			ALT_INVENTORY_MAXIMUM_PET = Integer.parseInt(p.getProperty("MaximumSlotsForPet", "12"));
			DESTROY_PLAYER_INVENTORY_DROP = Boolean.parseBoolean(p.getProperty("DestroyPlayerInventoryDrop", "false"));
			ALLOW_DISCARDITEM = Boolean.parseBoolean(p.getProperty("AllowDiscardItem", "true"));
			
			PVP_NORMAL_TIME = Integer.parseInt(p.getProperty("PvPVsNormalTime", "120000"));
			PVP_PVP_TIME = Integer.parseInt(p.getProperty("PvPVsPvPTime", "60000"));
			PVP_TIME = PVP_NORMAL_TIME;
			
			CURSED_WEAPON_NPC_INTERACT = Boolean.parseBoolean(p.getProperty("CursedWeaponNpcInteract", "false"));
			BLOCK_PARTY_INVITE_ON_COMBAT = Boolean.parseBoolean(p.getProperty("BlockPartyInviteOnCombat", "false"));
			BLOCK_CHANGE_WEAPON_ON_ATTACK = Boolean.parseBoolean(p.getProperty("BlockChangeWeaponWhileAttacking", "false"));
			ALT_GAME_KARMA_PLAYER_CAN_USE_GK = Boolean.parseBoolean(p.getProperty("AltKarmaPlayerCanUseGK", "false"));
			KARMA_MIN_KARMA = Integer.parseInt(p.getProperty("MinKarma", "240"));
			KARMA_MAX_KARMA = Integer.parseInt(p.getProperty("MaxKarma", "10000"));
			KARMA_RATE = Float.parseFloat(p.getProperty("KarmaRate", "1."));
			KARMA_XP_DIVIDER = Integer.parseInt(p.getProperty("XpDivider", "260"));
			KARMA_DROP_GM = Boolean.parseBoolean(p.getProperty("CanGMDropEquipment", "false"));
			KARMA_PK_LIMIT = Integer.parseInt(p.getProperty("MinimumPKRequiredToDrop", "5"));
			KARMA_LOST_BASE = Integer.parseInt(p.getProperty("BaseKarmaLost", "0"));
			KARMA_AWARD_PK_KILL = Boolean.parseBoolean(p.getProperty("AwardPKKillPVPPoint", "true"));
			KARMA_NON_DROPPABLE_PET_ITEMS = p.getProperty("ListOfPetItems", "2375,3500,3501,3502,4422,4423,4424,4425,6648,6649,6650,9882");
			KARMA_NON_DROPPABLE_ITEMS = p.getProperty("ListOfNonDroppableItems", "57,1147,425,1146,461,10,2368,7,6,2370,2369,6842,6611,6612,6613,6614,6615,6616,6617,6618,6619,6620,6621,8181,5575,7694,9388,9389,9390");
			KARMA_LIST_NONDROPPABLE_PET_ITEMS = new ArrayList<>();
			for (String id : KARMA_NON_DROPPABLE_PET_ITEMS.trim().split(","))
			{
				KARMA_LIST_NONDROPPABLE_PET_ITEMS.add(Integer.parseInt(id.trim()));
			}
			
			KARMA_LIST_NONDROPPABLE_ITEMS = new ArrayList<>();
			for (String id : KARMA_NON_DROPPABLE_ITEMS.trim().split(","))
			{
				KARMA_LIST_NONDROPPABLE_ITEMS.add(Integer.parseInt(id.trim()));
			}
			
			ALT_GAME_KARMA_PLAYER_CAN_BE_KILLED_IN_PEACEZONE = Boolean.parseBoolean(p.getProperty("AltKarmaPlayerCanBeKilledInPeaceZone", "false"));
			ALT_GAME_KARMA_PLAYER_CAN_SHOP = Boolean.parseBoolean(p.getProperty("AltKarmaPlayerCanShop", "true"));
			ALT_GAME_KARMA_PLAYER_CAN_TELEPORT = Boolean.parseBoolean(p.getProperty("AltKarmaPlayerCanTeleport", "true"));
			ALT_GAME_KARMA_PLAYER_CAN_TRADE = Boolean.parseBoolean(p.getProperty("AltKarmaPlayerCanTrade", "true"));
			ALT_GAME_KARMA_PLAYER_CAN_USE_WAREHOUSE = Boolean.parseBoolean(p.getProperty("AltKarmaPlayerCanUseWareHouse", "true"));
		}
		catch (Exception e)
		{
			_log.error(e.getMessage(), e);
			throw new Error("Failed to Load " + ConfigFiles.PLAYER_FILE + " File.");
		}
	}
	
	public static void loadRatesConfig()
	{
		try
		{
			Properties p = new L2Properties(ConfigFiles.RATES_FILE);
			RATE_XP = Float.parseFloat(p.getProperty("RateXp", "1."));
			RATE_SP = Float.parseFloat(p.getProperty("RateSp", "1."));
			RATE_PARTY_XP = Float.parseFloat(p.getProperty("RatePartyXp", "1."));
			RATE_PARTY_SP = Float.parseFloat(p.getProperty("RatePartySp", "1."));
			
			ALLOW_VIP_XPSP = Boolean.parseBoolean(p.getProperty("AllowVipMulXpSp", "True"));
			VIP_XP = Float.parseFloat(p.getProperty("VipMulXp", "2"));
			VIP_SP = Float.parseFloat(p.getProperty("VipMulSp", "2"));
			VIP_DROP_RATE = Float.parseFloat(p.getProperty("VipDropRate", "1.5"));
			VIP_SPOIL_RATE = Float.parseFloat(p.getProperty("VipSpoilRate", "1.5"));
			
			RATE_DROP_ADENA = Float.parseFloat(p.getProperty("RateDropAdena", "1."));
			RATE_CONSUMABLE_COST = Float.parseFloat(p.getProperty("RateConsumableCost", "1."));
			RATE_CRAFT_COST = Float.parseFloat(p.getProperty("RateCraftCost", "1."));
			RATE_DROP_ITEMS = Float.parseFloat(p.getProperty("RateDropItems", "1."));
			RATE_DROP_SEAL_STONES = Float.parseFloat(p.getProperty("RateDropSealStones", "1."));
			RATE_DROP_ITEMS_BY_RAID = Float.parseFloat(p.getProperty("RateRaidDropItems", "1."));
			RATE_DROP_SPOIL = Float.parseFloat(p.getProperty("RateDropSpoil", "1."));
			RATE_DROP_QUEST = Float.parseFloat(p.getProperty("RateDropQuest", "1."));
			RATE_EXTR_FISH = Integer.parseInt(p.getProperty("RateExtractFish", "1"));
			
			RATE_QUESTS_REWARD_EXPSP = Float.parseFloat(p.getProperty("RateQuestsRewardExpSp", "1."));
			RATE_QUESTS_REWARD_ADENA = Float.parseFloat(p.getProperty("RateQuestsRewardAdena", "1."));
			RATE_QUESTS_REWARD_ITEMS = Float.parseFloat(p.getProperty("RateQuestsRewardItems", "1."));
			RATE_KARMA_EXP_LOST = Float.parseFloat(p.getProperty("RateKarmaExpLost", "1."));
			RATE_SIEGE_GUARDS_PRICE = Float.parseFloat(p.getProperty("RateSiegeGuardsPrice", "1."));
			RATE_DROP_COMMON_HERBS = Float.parseFloat(p.getProperty("RateCommonHerbs", "15."));
			RATE_DROP_MP_HP_HERBS = Float.parseFloat(p.getProperty("RateHpMpHerbs", "10."));
			RATE_DROP_GREATER_HERBS = Float.parseFloat(p.getProperty("RateGreaterHerbs", "4."));
			RATE_DROP_SUPERIOR_HERBS = Float.parseFloat(p.getProperty("RateSuperiorHerbs", "0.8")) * 10;
			RATE_DROP_SPECIAL_HERBS = Float.parseFloat(p.getProperty("RateSpecialHerbs", "0.2")) * 10;
			RATE_RUN_SPEED = Float.parseFloat(p.getProperty("RateRunSpeed", "1."));
			SINEATER_XP_RATE = Float.parseFloat(p.getProperty("SinEaterXpRate", "1."));
			RATE_DROP_MANOR = Float.parseFloat(p.getProperty("RateDropManor", "1."));
			KARMA_DROP_LIMIT = Integer.parseInt(p.getProperty("KarmaDropLimit", "10"));
			KARMA_RATE_DROP = Integer.parseInt(p.getProperty("KarmaRateDrop", "70"));
			KARMA_RATE_DROP_ITEM = Integer.parseInt(p.getProperty("KarmaRateDropItem", "50"));
			KARMA_RATE_DROP_EQUIP = Integer.parseInt(p.getProperty("KarmaRateDropEquip", "40"));
			KARMA_RATE_DROP_EQUIP_WEAPON = Integer.parseInt(p.getProperty("KarmaRateDropEquipWeapon", "10"));
			
			PET_XP_RATE = Float.parseFloat(p.getProperty("PetXpRate", "1."));
			PET_FOOD_RATE = Float.parseFloat(p.getProperty("PetFoodRate", "1"));
			String[] propertySplit = p.getProperty("NormalWeaponEnchantLevel", "").split(";");
			for (String readData : propertySplit)
			{
				String[] writeData = readData.split(",");
				if (writeData.length != 2)
				{
					System.out.println("invalid config property");
				}
				else
				{
					try
					{
						NORMAL_WEAPON_ENCHANT_LEVEL.put(Integer.parseInt(writeData[0]), Integer.parseInt(writeData[1]));
					}
					catch (NumberFormatException nfe)
					{
						if (!readData.equals(""))
						{
							System.out.println("invalid config property");
						}
					}
				}
			}
			
			propertySplit = p.getProperty("BlessWeaponEnchantLevel", "").split(";");
			for (String readData : propertySplit)
			{
				String[] writeData = readData.split(",");
				if (writeData.length != 2)
				{
					System.out.println("invalid config property");
				}
				else
				{
					try
					{
						BLESS_WEAPON_ENCHANT_LEVEL.put(Integer.parseInt(writeData[0]), Integer.parseInt(writeData[1]));
					}
					catch (NumberFormatException nfe)
					{
						if (!readData.equals(""))
						{
							System.out.println("invalid config property");
						}
					}
				}
			}
			
			propertySplit = p.getProperty("CrystalWeaponEnchantLevel", "").split(";");
			for (String readData : propertySplit)
			{
				String[] writeData = readData.split(",");
				if (writeData.length != 2)
				{
					System.out.println("invalid config property");
				}
				else
				{
					try
					{
						CRYTAL_WEAPON_ENCHANT_LEVEL.put(Integer.parseInt(writeData[0]), Integer.parseInt(writeData[1]));
					}
					catch (NumberFormatException nfe)
					{
						if (!readData.equals(""))
						{
							System.out.println("invalid config property");
						}
					}
				}
			}
			
			propertySplit = p.getProperty("NormalArmorEnchantLevel", "").split(";");
			for (String readData : propertySplit)
			{
				String[] writeData = readData.split(",");
				if (writeData.length != 2)
				{
					System.out.println("invalid config property");
				}
				else
				{
					try
					{
						NORMAL_ARMOR_ENCHANT_LEVEL.put(Integer.parseInt(writeData[0]), Integer.parseInt(writeData[1]));
					}
					catch (NumberFormatException nfe)
					{
						if (!readData.equals(""))
						{
							System.out.println("invalid config property");
						}
					}
				}
			}
			
			propertySplit = p.getProperty("BlessArmorEnchantLevel", "").split(";");
			for (String readData : propertySplit)
			{
				String[] writeData = readData.split(",");
				if (writeData.length != 2)
				{
					System.out.println("invalid config property");
				}
				else
				{
					try
					{
						BLESS_ARMOR_ENCHANT_LEVEL.put(Integer.parseInt(writeData[0]), Integer.parseInt(writeData[1]));
					}
					catch (NumberFormatException nfe)
					{
						if (!readData.equals(""))
						{
							System.out.println("invalid config property");
						}
					}
				}
			}
			
			propertySplit = p.getProperty("CrystalArmorEnchantLevel", "").split(";");
			for (String readData : propertySplit)
			{
				String[] writeData = readData.split(",");
				if (writeData.length != 2)
				{
					System.out.println("invalid config property");
				}
				else
				{
					try
					{
						CRYSTAL_ARMOR_ENCHANT_LEVEL.put(Integer.parseInt(writeData[0]), Integer.parseInt(writeData[1]));
					}
					catch (NumberFormatException nfe)
					{
						if (!readData.equals(""))
						{
							System.out.println("invalid config property");
						}
					}
				}
			}
			
			propertySplit = p.getProperty("NormalJewelryEnchantLevel", "").split(";");
			for (String readData : propertySplit)
			{
				String[] writeData = readData.split(",");
				if (writeData.length != 2)
				{
					System.out.println("invalid config property");
				}
				else
				{
					try
					{
						NORMAL_JEWELRY_ENCHANT_LEVEL.put(Integer.parseInt(writeData[0]), Integer.parseInt(writeData[1]));
					}
					catch (NumberFormatException nfe)
					{
						if (!readData.equals(""))
						{
							System.out.println("invalid config property");
						}
					}
				}
			}
			
			propertySplit = p.getProperty("BlessJewelryEnchantLevel", "").split(";");
			for (String readData : propertySplit)
			{
				String[] writeData = readData.split(",");
				if (writeData.length != 2)
				{
					System.out.println("invalid config property");
				}
				else
				{
					try
					{
						BLESS_JEWELRY_ENCHANT_LEVEL.put(Integer.parseInt(writeData[0]), Integer.parseInt(writeData[1]));
					}
					catch (NumberFormatException nfe)
					{
						if (!readData.equals(""))
						{
							System.out.println("invalid config property");
						}
					}
				}
			}
			
			propertySplit = p.getProperty("CrystalJewelryEnchantLevel", "").split(";");
			for (String readData : propertySplit)
			{
				String[] writeData = readData.split(",");
				if (writeData.length != 2)
				{
					System.out.println("invalid config property");
				}
				else
				{
					try
					{
						CRYSTAL_JEWELRY_ENCHANT_LEVEL.put(Integer.parseInt(writeData[0]), Integer.parseInt(writeData[1]));
					}
					catch (NumberFormatException nfe)
					{
						if (!readData.equals(""))
						{
							System.out.println("invalid config property");
						}
					}
				}
			}
			
			ALLOW_CRYSTAL_SCROLL = Boolean.parseBoolean(p.getProperty("AllowCrystalScroll", "false"));
			ENCHANT_MAX_WEAPON_NORMAL = Integer.parseInt(p.getProperty("EnchantMaxWeaponNormal", "25"));
			ENCHANT_MAX_ARMOR_NORMAL = Integer.parseInt(p.getProperty("EnchantMaxArmorNormal", "25"));
			ENCHANT_MAX_JEWELRY_NORMAL = Integer.parseInt(p.getProperty("EnchantMaxJewelryNormal", "25"));
			ENCHANT_MAX_WEAPON_BLESSED = Integer.parseInt(p.getProperty("EnchantMaxWeaponBlessed", "25"));
			ENCHANT_MAX_ARMOR_BLESSED = Integer.parseInt(p.getProperty("EnchantMaxArmorBlessed", "25"));
			ENCHANT_MAX_JEWELRY_BLESSED = Integer.parseInt(p.getProperty("EnchantMaxJewelryBlessed", "25"));
			ENCHANT_MAX_WEAPON_CRYSTAL = Integer.parseInt(p.getProperty("EnchantMaxWeaponCrystal", "25"));
			ENCHANT_MAX_ARMOR_CRYSTAL = Integer.parseInt(p.getProperty("EnchantMaxArmorCrystal", "25"));
			ENCHANT_MAX_JEWELRY_CRYSTAL = Integer.parseInt(p.getProperty("EnchantMaxJewelryCrystal", "25"));
			ENCHANT_OVER_CHANT_CHECK = Integer.parseInt(p.getProperty("EnchantOverChantCheck", "25"));
			ENCHANT_SAFE_MAX = Integer.parseInt(p.getProperty("EnchantSafeMax", "3"));
			ENCHANT_SAFE_MAX_FULL = Integer.parseInt(p.getProperty("EnchantSafeMaxFull", "4"));
			ENCHANT_DWARF_SYSTEM = Boolean.parseBoolean(p.getProperty("EnchantDwarfSystem", "false"));
			ENCHANT_DWARF_1_ENCHANTLEVEL = Integer.parseInt(p.getProperty("EnchantDwarf1Enchantlevel", "8"));
			ENCHANT_DWARF_2_ENCHANTLEVEL = Integer.parseInt(p.getProperty("EnchantDwarf2Enchantlevel", "10"));
			ENCHANT_DWARF_3_ENCHANTLEVEL = Integer.parseInt(p.getProperty("EnchantDwarf3Enchantlevel", "12"));
			ENCHANT_DWARF_1_CHANCE = Integer.parseInt(p.getProperty("EnchantDwarf1Chance", "15"));
			ENCHANT_DWARF_2_CHANCE = Integer.parseInt(p.getProperty("EnchantDwarf2Chance", "15"));
			ENCHANT_DWARF_3_CHANCE = Integer.parseInt(p.getProperty("EnchantDwarf3Chance", "15"));
			AUGMENT_EXCLUDE_NOTDONE = Boolean.parseBoolean(p.getProperty("AugmentExcludeNotdone", "false"));
			AUGMENTATION_NG_SKILL_CHANCE = Integer.parseInt(p.getProperty("AugmentationNGSkillChance", "15"));
			AUGMENTATION_NG_GLOW_CHANCE = Integer.parseInt(p.getProperty("AugmentationNGGlowChance", "0"));
			AUGMENTATION_MID_SKILL_CHANCE = Integer.parseInt(p.getProperty("AugmentationMidSkillChance", "30"));
			AUGMENTATION_MID_GLOW_CHANCE = Integer.parseInt(p.getProperty("AugmentationMidGlowChance", "40"));
			AUGMENTATION_HIGH_SKILL_CHANCE = Integer.parseInt(p.getProperty("AugmentationHighSkillChance", "45"));
			AUGMENTATION_HIGH_GLOW_CHANCE = Integer.parseInt(p.getProperty("AugmentationHighGlowChance", "70"));
			AUGMENTATION_TOP_SKILL_CHANCE = Integer.parseInt(p.getProperty("AugmentationTopSkillChance", "60"));
			AUGMENTATION_TOP_GLOW_CHANCE = Integer.parseInt(p.getProperty("AugmentationTopGlowChance", "100"));
			AUGMENTATION_BASESTAT_CHANCE = Integer.parseInt(p.getProperty("AugmentationBaseStatChance", "1"));
			CHECK_ENCHANT_LEVEL_EQUIP = Boolean.parseBoolean(p.getProperty("CheckEnchantLevelEquip", "true"));
			ALT_FAILED_ENC_LEVEL = Boolean.parseBoolean(p.getProperty("AltEncLvlAfterFail", "false"));
			
			ALLOW_MANOR = Boolean.parseBoolean(p.getProperty("AllowManor", "false"));
			ALT_MANOR_REFRESH_TIME = Integer.parseInt(p.getProperty("AltManorRefreshTime", "20"));
			ALT_MANOR_REFRESH_MIN = Integer.parseInt(p.getProperty("AltManorRefreshMin", "00"));
			ALT_MANOR_APPROVE_TIME = Integer.parseInt(p.getProperty("AltManorApproveTime", "6"));
			ALT_MANOR_APPROVE_MIN = Integer.parseInt(p.getProperty("AltManorApproveMin", "00"));
			ALT_MANOR_MAINTENANCE_PERIOD = Integer.parseInt(p.getProperty("AltManorMaintenancePeriod", "360000"));
			ALT_MANOR_SAVE_ALL_ACTIONS = Boolean.parseBoolean(p.getProperty("AltManorSaveAllActions", "false"));
			ALT_MANOR_SAVE_PERIOD_RATE = Integer.parseInt(p.getProperty("AltManorSavePeriodRate", "2"));
			
			MAX_PVTSTORESELL_SLOTS_DWARF = Integer.parseInt(p.getProperty("MaxPvtStoreSellSlotsDwarf", "4"));
			MAX_PVTSTORESELL_SLOTS_OTHER = Integer.parseInt(p.getProperty("MaxPvtStoreSellSlotsOther", "3"));
			MAX_PVTSTOREBUY_SLOTS_DWARF = Integer.parseInt(p.getProperty("MaxPvtStoreBuySlotsDwarf", "5"));
			MAX_PVTSTOREBUY_SLOTS_OTHER = Integer.parseInt(p.getProperty("MaxPvtStoreBuySlotsOther", "4"));
			CHECK_ZONE_ON_PVT = Boolean.parseBoolean(p.getProperty("CheckZoneOnPvt", "false"));
			
			ALLOW_WAREHOUSE = Boolean.parseBoolean(p.getProperty("AllowWarehouse", "true"));
			ALLOW_FREIGHT = Boolean.parseBoolean(p.getProperty("AllowFreight", "true"));
			ENABLE_WAREHOUSESORTING_CLAN = Boolean.parseBoolean(p.getProperty("EnableWarehouseSortingClan", "false"));
			ENABLE_WAREHOUSESORTING_PRIVATE = Boolean.parseBoolean(p.getProperty("EnableWarehouseSortingPrivate", "false"));
			ENABLE_WAREHOUSESORTING_FREIGHT = Boolean.parseBoolean(p.getProperty("EnableWarehouseSortingFreight", "false"));
			ALT_GAME_FREIGHTS = Boolean.parseBoolean(p.getProperty("AltGameFreights", "false"));
			ALT_GAME_FREIGHT_PRICE = Integer.parseInt(p.getProperty("AltGameFreightPrice", "1000"));
			WAREHOUSE_SLOTS_NO_DWARF = Integer.parseInt(p.getProperty("MaxWarehouseSlotsForOther", "100"));
			WAREHOUSE_SLOTS_DWARF = Integer.parseInt(p.getProperty("MaxWarehouseSlotsForDwarf", "120"));
			WAREHOUSE_SLOTS_CLAN = Integer.parseInt(p.getProperty("MaxWarehouseSlotsForClan", "150"));
			FREIGHT_SLOTS = Integer.parseInt(p.getProperty("MaxWarehouseFreightSlots", "100"));
			WAREHOUSE_CACHE = Boolean.parseBoolean(p.getProperty("WarehouseCache", "false"));
			WAREHOUSE_CACHE_TIME = Integer.parseInt(p.getProperty("WarehouseCacheTime", "15"));
			NPC_HP_REGEN_MULTIPLIER = Double.parseDouble(p.getProperty("NPCHpRegenMultiplier", "100")) / 100;
			NPC_MP_REGEN_MULTIPLIER = Double.parseDouble(p.getProperty("NPCMpRegenMultiplier", "100")) / 100;
			PLAYER_CP_REGEN_MULTIPLIER = Double.parseDouble(p.getProperty("PlayerCpRegenMultiplier", "100")) / 100;
			PLAYER_HP_REGEN_MULTIPLIER = Double.parseDouble(p.getProperty("PlayerHpRegenMultiplier", "100")) / 100;
			PLAYER_MP_REGEN_MULTIPLIER = Double.parseDouble(p.getProperty("PlayerMpRegenMultiplier", "100")) / 100;
			RAID_HP_REGEN_MULTIPLIER = Double.parseDouble(p.getProperty("RaidHpRegenMultiplier", "100")) / 100;
			RAID_MP_REGEN_MULTIPLIER = Double.parseDouble(p.getProperty("RaidMpRegenMultiplier", "100")) / 100;
			RAID_PDEFENCE_MULTIPLIER = Double.parseDouble(p.getProperty("RaidPDefenceMultiplier", "100")) / 100;
			RAID_MDEFENCE_MULTIPLIER = Double.parseDouble(p.getProperty("RaidMDefenceMultiplier", "100")) / 100;
			PET_HP_REGEN_MULTIPLIER = Double.parseDouble(p.getProperty("PetHpRegenMultiplier", "100")) / 100;
			PET_MP_REGEN_MULTIPLIER = Double.parseDouble(p.getProperty("PetMpRegenMultiplier", "100")) / 100;
			
			ENCHANTROLLBACK = Boolean.parseBoolean(p.getProperty("EnchantRollBack", "false"));
			ENCHANTROLLBACK_VALUE = Integer.parseInt(p.getProperty("EnchantRollBackValue", "0"));
			
			/** RB Rate Control */
			ADENA_BOSS = TypeFormat.parseFloat(p.getProperty("AdenaBoss", "1.00"));
			ADENA_RAID = TypeFormat.parseFloat(p.getProperty("AdenaRaid", "1.00"));
			ADENA_MINION = TypeFormat.parseFloat(p.getProperty("AdenaMinon", "1.00"));
			JEWEL_BOSS = TypeFormat.parseFloat(p.getProperty("JewelBoss", "1.00"));
			ITEMS_BOSS = TypeFormat.parseFloat(p.getProperty("ItemsBoss", "1.00"));
			ITEMS_RAID = TypeFormat.parseFloat(p.getProperty("ItemsRaid", "1.00"));
			ITEMS_MINION = TypeFormat.parseFloat(p.getProperty("ItemsMinon", "1.00"));
			SPOIL_BOSS = TypeFormat.parseFloat(p.getProperty("SpoilBoss", "1.00"));
			SPOIL_RAID = TypeFormat.parseFloat(p.getProperty("SpoilRaid", "1.00"));
			SPOIL_MINION = TypeFormat.parseFloat(p.getProperty("SpoilMinon", "1.00"));
		}
		catch (Exception e)
		{
			_log.error(e.getMessage(), e);
			throw new Error("Failed to Load " + ConfigFiles.RATES_FILE + " File.");
		}
	}
	
	public static float ADENA_BOSS;
	public static float ADENA_RAID;
	public static float ADENA_MINION;
	public static float JEWEL_BOSS;
	public static float ITEMS_BOSS;
	public static float ITEMS_RAID;
	public static float ITEMS_MINION;
	public static float SPOIL_BOSS;
	public static float SPOIL_RAID;
	public static float SPOIL_MINION;
	
	public static void loadSiegeConfig()
	{
		try
		{
			Properties p = new L2Properties(ConfigFiles.SIEGE_CONFIGURATION_FILE);
			
			SIEGE_ONLY_REGISTERED = Boolean.parseBoolean(p.getProperty("OnlyRegistered", "true"));
			ALT_FLYING_WYVERN_IN_SIEGE = Boolean.parseBoolean(p.getProperty("AltFlyingWyvernInSiege", "false"));
			SPAWN_SIEGE_GUARD = Boolean.parseBoolean(p.getProperty("SpawnSiegeGuard", "true"));
			SIEGE_GATE_CONTROL = Boolean.parseBoolean(p.getProperty("AllowGateControl", "false"));
			SIEGE_CLAN_MIN_MEMBERCOUNT = Integer.parseInt(p.getProperty("SiegeClanMinMembersCount", "1"));
			SIEGE_MAX_ATTACKER = Integer.parseInt(p.getProperty("AttackerMaxClans", "500"));
			SIEGE_MAX_DEFENDER = Integer.parseInt(p.getProperty("DefenderMaxClans", "500"));
			SIEGE_BLOODALIANCE_REWARD_CNT = Integer.parseInt(p.getProperty("BloodAllianceReward", "1"));
			SIEGE_RESPAWN_DELAY_ATTACKER = Integer.parseInt(p.getProperty("AttackerRespawn", "30000"));
			SIEGE_FLAG_MAX_COUNT = Integer.parseInt(p.getProperty("MaxFlags", "1"));
			SIEGE_CLAN_MIN_LEVEL = Integer.parseInt(p.getProperty("SiegeClanMinLevel", "5"));
			SIEGE_LENGTH_MINUTES = Integer.parseInt(p.getProperty("SiegeLength", "120"));
			
			CHANGE_SIEGE_TIME_IS_DISABLES = Boolean.parseBoolean(p.getProperty("DisableChangeSiegeTime", "false"));
			CORECT_SIEGE_DATE_BY_7S = Boolean.parseBoolean(p.getProperty("CorrectDateBy7s", "true"));
			CL_SET_SIEGE_TIME_LIST = new ArrayList<>();
			SIEGE_HOUR_LIST_MORNING = new ArrayList<>();
			SIEGE_HOUR_LIST_AFTERNOON = new ArrayList<>();
			String[] sstl = p.getProperty("CLSetSiegeTimeList", "").split(",");
			if (sstl.length != 0)
			{
				boolean isHour = false;
				for (String st : sstl)
					if (st.equalsIgnoreCase("day") || st.equalsIgnoreCase("hour") || st.equalsIgnoreCase("minute"))
					{
						if (st.equalsIgnoreCase("hour"))
						{
							isHour = true;
						}
						CL_SET_SIEGE_TIME_LIST.add(st.toLowerCase());
					}
					else
					{
						System.out.println("[CLSetSiegeTimeList]: invalid config property -> CLSetSiegeTimeList \"" + st + "\"");
					}
				if (isHour)
				{
					String[] shl = p.getProperty("SiegeHourList", "").split(",");
					for (String st : shl)
						if (!st.equalsIgnoreCase(""))
						{
							int val = Integer.valueOf(st);
							if (val > 23 || val < 0)
							{
								System.out.println("[SiegeHourList]: invalid config property -> SiegeHourList \"" + st + "\"");
							}
							else if (val < 12)
							{
								SIEGE_HOUR_LIST_MORNING.add(val);
							}
							else
							{
								val -= 12;
								SIEGE_HOUR_LIST_AFTERNOON.add(val);
							}
						}
					if (Config.SIEGE_HOUR_LIST_AFTERNOON.isEmpty() && Config.SIEGE_HOUR_LIST_AFTERNOON.isEmpty())
					{
						System.out.println("[SiegeHourList]: invalid config property -> SiegeHourList is empty");
						CL_SET_SIEGE_TIME_LIST.remove("hour");
					}
				}
			}
			MAX_GUARD_COUNT_FOR_CASTLE = Integer.parseInt(p.getProperty("MaxGuardCount", "400"));
			CASTLE_REWARD_ID = Integer.parseInt(p.getProperty("RewardID", "0"));
			CASTLE_REWARD_COUNT = Integer.parseInt(p.getProperty("RewardCount", "0"));
			
			FORTSIEGE_MAX_ATTACKER = Integer.parseInt(p.getProperty("FortAttackerMaxClans", "500"));
			FORTSIEGE_FLAG_MAX_COUNT = Integer.parseInt(p.getProperty("FortMaxFlags", "1"));
			FORTSIEGE_CLAN_MIN_LEVEL = Integer.parseInt(p.getProperty("FortSiegeClanMinLevel", "4"));
			FORTSIEGE_LENGTH_MINUTES = Integer.parseInt(p.getProperty("FortSiegeLength", "60"));
			FORTSIEGE_COUNTDOWN_LENGTH = Integer.decode(p.getProperty("FortCountDownLength", "10"));
			FORTSIEGE_MERCHANT_DELAY = Integer.decode(p.getProperty("SuspiciousMerchantRespawnDelay", "180"));
			FORTSIEGE_COMBAT_FLAG_ID = Integer.parseInt(p.getProperty("CombatFlagID", "6718"));
			FORTSIEGE_REWARD_ID = Integer.parseInt(p.getProperty("FortRewardID", "0"));
			FORTSIEGE_REWARD_COUNT = Integer.parseInt(p.getProperty("FortRewardCount", "0"));
		}
		catch (Exception e)
		{
			_log.error(e);
			throw new Error("Failed to Load " + ConfigFiles.SIEGE_CONFIGURATION_FILE + " File.");
		}
	}
	

	public static void loadVersion()
	{
		try
		{
			Properties p = new Properties();
			InputStream is = new FileInputStream(new File(ConfigFiles.SERVER_VERSION_FILE));
			p.load(is);
			is.close();
			
			SERVER_VERSION = p.getProperty("Version", "Unsupported Custom Version.");
			SERVER_BUILD_DATE = p.getProperty("BuildDate", "Undefined Date.");
		}
		catch (Exception e)
		{
			SERVER_VERSION = "Unsupported Custom Version.";
			SERVER_BUILD_DATE = "Undefined Date.";
			throw new Error("Failed to Load " + ConfigFiles.SERVER_VERSION_FILE + " File.");
		}
	}
	

	public static void saveHexid(int serverId, String hexId)
	{
		try
		{
			Properties p = new L2Properties();
			File file = new File(ConfigFiles.HEXID_FILE);
			file.createNewFile();
			OutputStream out = new FileOutputStream(file);
			p.setProperty("ServerID", String.valueOf(serverId));
			p.setProperty("HexID", hexId);
			p.store(out, "the hexID to auth into login");
			out.close();
		}
		catch (Exception e)
		{
			_log.warn("Failed to save hex id to " + ConfigFiles.HEXID_FILE + " File.");
		}
	}
	
	public static void unallocateFilterBuffer()
	{
		_log.info("Cleaning Chat Filter..");
		FILTER_LIST.clear();
	}
	
	private static String[] keys;
	
	public static boolean isValidBuffShopItem(int value)
	{
		return VALID_BUFFSHOP_SKILLS.containsValue(Integer.valueOf(value));
	}
	
	public static int[] getBuffShopSkill(int value)
	{
		keys = null;
		for (Map.Entry<String, Integer> entry : VALID_BUFFSHOP_SKILLS.entrySet())
		{
			if (entry.getValue().intValue() == value)
			{
				keys = entry.getKey().split("-");
			}
		}
		int[] results = new int[0];
		try
		{
			results = new int[]
			{
				Integer.valueOf(keys[0].trim()).intValue(),
				Integer.valueOf(keys[1].trim()).intValue()
			};
		}
		catch (NumberFormatException e)
		{
			_log.info("CustomConfig: NumberFormatException: " + e.getMessage());
		}
		catch (NullPointerException e)
		{
			_log.info("CustomConfig: NullPointerException: " + e.getMessage());
		}
		return results;
	}
	
	public static boolean isValidBuffShopSkill(String key)
	{
		return VALID_BUFFSHOP_SKILLS.containsKey(key);
	}
	
	public static int getBuffShopItem(String key)
	{
		return VALID_BUFFSHOP_SKILLS.get(key).intValue();
	}
	
	private static int[][] parseItemsList(String line)
	{
		final String[] propertySplit = line.split(";");
		if (propertySplit.length == 0)
			return null;
		
		int i = 0;
		String[] valueSplit;
		final int[][] result = new int[propertySplit.length][];
		for (String value : propertySplit)
		{
			valueSplit = value.split(",");
			if (valueSplit.length != 2)
			{
				_log.warn(StringUtil.concat("parseItemsList[Config.load()]: invalid entry -> \"", valueSplit[0], "\", should be itemId,itemNumber"));
				return null;
			}
			
			result[i] = new int[2];
			try
			{
				result[i][0] = Integer.parseInt(valueSplit[0]);
			}
			catch (NumberFormatException e)
			{
				_log.warn(StringUtil.concat("parseItemsList[Config.load()]: invalid itemId -> \"", valueSplit[0], "\""));
				return null;
			}
			
			try
			{
				result[i][1] = Integer.parseInt(valueSplit[1]);
			}
			catch (NumberFormatException e)
			{
				_log.warn(StringUtil.concat("parseItemsList[Config.load()]: invalid item number -> \"", valueSplit[1], "\""));
				return null;
			}
			i++;
		}
		return result;
	}
}