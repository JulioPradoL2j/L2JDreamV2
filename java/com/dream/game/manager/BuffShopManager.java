package com.dream.game.manager;

import com.dream.Config;
import com.dream.L2DatabaseFactory;
import com.dream.game.datatables.sql.ItemTable;
import com.dream.game.datatables.xml.SkillTable;
import com.dream.game.model.L2Object;
import com.dream.game.model.L2Skill;
import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.world.L2World;
import com.dream.game.network.AuthServerThread;
import com.dream.game.network.L2GameClient;
import com.dream.game.network.SystemChatChannelId;
import com.dream.game.network.ThreadPoolManager;
import com.dream.game.network.serverpackets.CreatureSay;
import com.dream.game.network.serverpackets.MagicSkillUse;
import com.dream.game.util.Broadcast;
import com.dream.util.CloseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BuffShopManager
{
	protected static final Logger _log = Logger.getLogger(BuffShopManager.class.getName());
	
	private static final String LOAD_OFFLINE_SHOPS = "SELECT * FROM character_offline_buffshop";
	private static final String LOAD_OFFLINE_SKILLS = "SELECT * FROM character_offline_buffshop_skills WHERE charId = ?";
	private static final String CLEAR_OFFLINE_SHOPS = "DELETE FROM character_offline_buffshop";
	private static final String CLEAR_OFFLINE_SKILLS = "DELETE FROM character_offline_buffshop_skills";
	
	DelayManager _delayManager;
	
	private L2PcInstance player;
	
	public static BuffShopManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	
	protected BuffShopManager()
	{
		if (Config.BUFFSHOP_RESTORE)
		{
			_log.info("BuffShop: Attempting to restore buffshops...");
			int shopCount = 0;
			try (Connection con = L2DatabaseFactory.getInstance().getConnection())
			{
				PreparedStatement stm = con.prepareStatement(LOAD_OFFLINE_SHOPS);
				ResultSet rs = stm.executeQuery();
				while (rs.next())
				{
					long time = rs.getLong("time");
					if (Config.BUFFSHOP_MAX_DAYS > 0)
					{
						Calendar cal = Calendar.getInstance();
						cal.setTimeInMillis(time);
						cal.add(6, Config.BUFFSHOP_MAX_DAYS);
						if (cal.getTimeInMillis() <= System.currentTimeMillis())
							continue;
					}
					player = null;
					try
					{
						L2GameClient client = new L2GameClient(null);
						client.setDetached(true);
						// player = L2PcInstance.restore(rs.getInt("charId"));
						client.setActiveChar(player);
						player.setOnlineStatus(false);
						client.setAccountName(player.getAccountName());
						client.setState(L2GameClient.GameClientState.IN_GAME);
						player.setClient(client);
						player.setOfflineStartTime(time);
						player.setIsBuffShop(true);
						player.spawnMe(player.getX(), player.getY(), player.getZ());
						AuthServerThread.getInstance().addGameServerLogin(player.getAccountName(), client);
						PreparedStatement stm_items = con.prepareStatement(LOAD_OFFLINE_SKILLS);
						stm_items.setInt(1, player.getObjectId());
						ResultSet items = stm_items.executeQuery();
						player.getSellList().setTitle(rs.getString("title"));
						player.clearBuffShopSellList();
						while (items.next())
						{
							player.addItemToBuffShopSellList(items.getInt("item"), items.getInt("price"));
						}
						items.close();
						stm_items.close();
						player.sitDown();
						
						player.setPrivateStoreType(1);
						player.setOnlineStatus(true);
						player.restoreEffects();
						player.broadcastUserInfo();
						shopCount++;
					}
					catch (Exception e)
					{
						_log.log(Level.WARNING, "BuffShop: Error loading shop " + player, e);
						if (player != null)
						{
							player.deleteMe();
						}
					}
				}
				rs.close();
				stm.close();
				_log.info("BuffShop: Loaded: " + shopCount + " offline shop(s).");
				stm = con.prepareStatement(CLEAR_OFFLINE_SHOPS);
				stm.execute();
				stm.close();
				stm = con.prepareStatement(CLEAR_OFFLINE_SKILLS);
				stm.execute();
				stm.close();
			}
			catch (Exception e)
			{
				_log.log(Level.WARNING, "BuffShop: Error while loading offline shops: ", e);
			}
		}
	}
	
	public void processBuffs(L2PcInstance player, L2PcInstance target, List<int[]> buffs)
	{
		List<L2Skill> _skills = new ArrayList<>();
		Map<Integer, Integer> _items = new HashMap<>();
		int totalCost = 0;
		int totalMp = 0;
		for (int i = 0; i < buffs.size(); i++)
		{
			int[] buff = buffs.get(i);
			int entry = 0;
			L2Object object = L2World.getInstance().findObject(buff[0]);
			if (object == null)
			{
				_log.info("BuffShop: Invalid object \"" + buff[0] + "\".");
			}
			else
			{
				if ((object instanceof L2ItemInstance))
				{
					entry = ((L2ItemInstance) object).getItemId();
				}
				else
				{
					_log.info("BuffShop: Invalid object type \"" + buff[1] + "\".");
					continue;
				}
				
				if (!Config.isValidBuffShopItem(entry))
				{
					_log.info("BuffShop: Invalid entry \"" + entry + "\".");
				}
				else
				{
					int[] info = Config.getBuffShopSkill(entry);
					if (info.length != 2)
					{
						_log.info("BuffShop: Invalid entry data \"" + entry + "\".");
						return;
					}
					L2Skill skill = SkillTable.getInstance().getInfo(info[0], info[1]);
					if (skill == null)
					{
						_log.info("BuffShop: Invalid skill \"" + info[0] + "-" + info[1] + "\".");
						return;
					}
					if ((skill.getItemConsumeId() > 0) && (skill.getItemConsume() > 0))
					{
						int count = skill.getItemConsume();
						if (_items.containsKey(Integer.valueOf(skill.getItemConsumeId())))
							count += _items.get(Integer.valueOf(skill.getItemConsumeId())).intValue();
						_items.put(Integer.valueOf(skill.getItemConsumeId()), Integer.valueOf(count));
					}
					_skills.add(skill);
					totalCost += buff[1];
					totalMp += skill.getMpConsume();
				}
			}
		}
		if (target.getAdena() < totalCost)
		{
			target.sendMessage("Not enough adena.");
			return;
		}
		if (_items.size() > 0)
		{
			DecimalFormat formatter = new DecimalFormat("#,###,###,###");
			try
			{
				for (Map.Entry<Integer, Integer> item : _items.entrySet())
					if (target.getInventory().getItemByItemId(item.getKey().intValue()).getCount() < item.getValue().intValue())
					{
						target.sendMessage("You need " + formatter.format(item.getValue()) + " " + ItemTable.getInstance().getTemplate(item.getKey().intValue()).getName() + ".");
						return;
					}
			}
			catch (NullPointerException e)
			{
			}
		}
		if (player.getCurrentMp() < totalMp)
		{
			target.sendMessage("Not enough mp.");
			return;
		}
		player.setCurrentMp(player.getCurrentMp() - totalMp);
		if (_items.size() > 0)
		{
			for (Map.Entry<Integer, Integer> item : _items.entrySet())
				target.destroyItemByItemId("BuffShop[" + target.getName() + "]", item.getKey().intValue(), item.getValue().intValue(), null, true);
		}
		player.setIsBizy(true);
		player.standUp();
		_delayManager = new DelayManager(_skills, player, target, 3000);
		
	}
	
	public static void updateShopTitle(L2PcInstance player)
	{
		player.startAbnormalEffect(Integer.decode("0x" + Config.BUFFSHOP_EFFECT).intValue());
	}
	
	public static void restoreEffects(L2PcInstance player)
	{
		player.stopAbnormalEffect(Integer.decode("0x" + Config.BUFFSHOP_EFFECT).intValue());
	}
	
	public void broadcastBizy(L2PcInstance player, L2PcInstance target)
	{
		Broadcast.toSelfAndKnownPlayersInRadius(player, new CreatureSay(player.getObjectId(), SystemChatChannelId.Chat_None, player.getName(), target.getName() + ", please wait one moment."), 600);
	}
	
	
	public static void onShutDown()
	{
		if (Config.BUFFSHOP_RESTORE)
		{
			_log.info("BuffShop: Attempting to save buffshops...");
			Connection con = null;
			try
			{
				con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement stm = con.prepareStatement("DELETE FROM character_offline_buffshop");
				stm.execute();
				stm.close();
				stm = con.prepareStatement("DELETE FROM character_offline_buffshop");
				stm.execute();
				stm.close();
				con.setAutoCommit(false);
				stm = con.prepareStatement("INSERT INTO character_offline_buffshop (charId,time,title) VALUES (?,?,?)");
				PreparedStatement stm_items = con.prepareStatement("INSERT INTO character_offline_buffshop_skills (charId,item,price) VALUES (?,?,?)");
				for (L2PcInstance pc : L2World.getInstance().getAllPlayers())
				{
					try
					{
						if ((pc.isBuffShop()) && ((pc.getClient() == null) || (pc.getClient().isDetached())))
						{
							stm.setInt(1, pc.getObjectId());
							stm.setLong(2, pc.getOfflineStartTime());
							stm.setString(3, pc.getSellList().getTitle());
							for (Map.Entry<Integer, int[]> i : pc.getBuffShopSellList().entrySet())
							{
								int[] vals = i.getValue();
								stm_items.setInt(1, pc.getObjectId());
								stm_items.setInt(2, i.getKey().intValue());
								stm_items.setInt(3, vals[1]);
								stm_items.executeUpdate();
								stm_items.clearParameters();
							}
							stm.executeUpdate();
							stm.clearParameters();
							con.commit();
						}
					}
					catch (Exception e)
					{
						_log.log(Level.WARNING, "BuffShop: Error while saving buffshop: [" + pc.getObjectId() + "]" + pc.getName() + " " + e, e);
					}
				}
				stm.close();
				stm_items.close();
				_log.info("BuffShop: Offline shops stored.");
			}
			catch (Exception e)
			{
				_log.log(Level.WARNING, "BuffShop: Error while saving offline shops: " + e, e);
			}
			finally
			{
				CloseUtil.close(con);
				con = null;
			}
		}
	}
	
	public void initialize(L2PcInstance player)
	{
		if (!Config.BUFFSHOP_ENABLE)
			player.sendMessage("Buffshop is currently disabled.");
		else if (player.getPrivateBuffShopLimit() < 1)
			player.sendMessage("Buffshop is currently disabled for you.");
		else
			player.tryOpenPrivateBuffStore();
	}
	
	public void debug(String message)
	{
		_log.info("BuffShopDebug: " + message);
	}
	
	private class DelayManager implements Runnable
	{
		private ScheduledFuture<?> _task = null;
		private L2PcInstance _player;
		private L2PcInstance _target;
		private List<L2Skill> _buffs;
		private int _type;
		
		public DelayManager(List<L2Skill> buffs, L2PcInstance player, L2PcInstance target, int delay)
		{
			_task = null;
			_player = player;
			_target = target;
			_buffs = buffs;
			_type = 1;
			_task = ThreadPoolManager.getInstance().scheduleGeneral(this, delay);
		}
		
		public DelayManager(L2PcInstance player, int delay)
		{
			_task = null;
			_player = player;
			_type = 2;
			_task = ThreadPoolManager.getInstance().scheduleGeneral(this, delay);
		}
		
		@Override
		public synchronized void run()
		{
			switch (_type)
			{
				case 1:
					if (_buffs.size() > 0)
					{
						Random rnd = new Random();
						L2Skill sk = _buffs.get(rnd.nextInt(_buffs.size()));
						
						_player.broadcastPacket(new MagicSkillUse(_player, _target, sk.getId(), sk.getLevel(), 1000, 0, false));
						sk.getEffects(_player, _target);
						_target.sendMessage("The effects of " + sk.getName() + " flow through you.");
						_buffs.remove(sk);
					}
					if (_buffs.size() > 0)
					{
						_delayManager = new DelayManager(_buffs, _player, _target, 1000);
						return;
					}
					_delayManager = new DelayManager(_player, 1500);
					break;
				case 2:
					_player.sitDown();
					_player.setIsBizy(false);
			}
			
			_task = null;
		}
		
		@SuppressWarnings("unused")
		public synchronized void cancel()
		{
			if (_task != null)
				_task.cancel(false);
			_task = null;
		}
	}
	
	private static class SingletonHolder
	{
		protected static final BuffShopManager _instance = new BuffShopManager();
	}
}