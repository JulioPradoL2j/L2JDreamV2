package com.dream.game.manager.games;

import com.dream.L2DatabaseFactory;
import com.dream.Message;
import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.ThreadPoolManager;
import com.dream.game.network.serverpackets.ItemList;
import com.dream.game.network.serverpackets.NpcHtmlMessage;
import com.dream.game.network.serverpackets.SystemMessage;
import com.dream.tools.random.Rnd;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.log4j.Logger;

public class fishingChampionship
{
	public class finishChamp implements Runnable
	{
		@Override
		public void run()
		{
			_winPlayer.clear();
			for (Fisher fisher : _tmpPlayer)
			{
				fisher._rewarded = 1;
				_winPlayer.add(fisher);
			}
			_tmpPlayer.clear();
			refreshWinResult();
			setEndOfChamp();
			shutdown();
			_log.info("Fishing Championship: Started new event period.");
			ThreadPoolManager.getInstance().scheduleGeneral(new finishChamp(), _enddate - System.currentTimeMillis());
		}
	}

	public class Fisher
	{
		float _length = 0;
		String _name;
		int _rewarded = 0;
	}

	public class needRefresh implements Runnable
	{
		@Override
		public void run()
		{
			_needRefresh = true;
		}
	}

	private static fishingChampionship _instance;

	protected static Logger _log = Logger.getLogger(fishingChampionship.class.getName());

	public static fishingChampionship getInstance()
	{
		if (_instance == null)
		{
			_instance = new fishingChampionship();
		}
		return _instance;
	}

	public long _enddate = 0;
	private final List<String> _playersName = new ArrayList<>();
	private final List<String> _fishLength = new ArrayList<>();
	private final List<String> _winPlayersName = new ArrayList<>();
	private final List<String> _winFishLength = new ArrayList<>();

	public final List<Fisher> _tmpPlayer = new ArrayList<>();

	public final List<Fisher> _winPlayer = new ArrayList<>();

	private float _minFishLength = 0;

	public boolean _needRefresh = true;

	private fishingChampionship()
	{
		restoreData();
		refreshWinResult();
		setNewMin();
		if (_enddate <= System.currentTimeMillis())
		{
			_enddate = System.currentTimeMillis();
			new finishChamp().run();
		}
		else
		{
			ThreadPoolManager.getInstance().scheduleGeneral(new finishChamp(), _enddate - System.currentTimeMillis());
		}
	}

	public String getCurrentFishLength(int par)
	{
		if (_fishLength.size() >= par)
			return _fishLength.get(par - 1);
		return "0";
	}

	public String getCurrentName(int par)
	{
		if (_playersName.size() >= par)
			return _playersName.get(par - 1);
		return "0";
	}

	public String getFishLength(int par)
	{
		if (_winFishLength.size() >= par)
			return _winFishLength.get(par - 1);
		return "0";
	}

	public void getReward(L2PcInstance pl)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(pl.getObjectId());
		String str;
		str = "<html><head><title>Royal tournament fishing</title></head>";
		str += "Congratulations!<br>";
		str += "Here's your prize! You are a true fisherman!<br>";
		str += "Good luck next week";
		str += "</body></html>";
		html.setHtml(str);
		pl.sendPacket(html);
		for (Fisher fisher : _winPlayer)
			if (fisher._name.equalsIgnoreCase(pl.getName()))
				if (fisher._rewarded != 2)
				{
					int rewardCnt = 0;
					for (int x = 0; x < _winPlayersName.size(); x++)
						if (_winPlayersName.get(x).equalsIgnoreCase(pl.getName()))
						{
							switch (x)
							{
								case 0:
									rewardCnt = 800000;
									break;
								case 1:
									rewardCnt = 500000;
									break;
								case 2:
									rewardCnt = 300000;
									break;
								case 3:
									rewardCnt = 200000;
									break;
								case 4:
									rewardCnt = 100000;
									break;
							}
						}
					fisher._rewarded = 2;
					if (rewardCnt > 0)
					{
						L2ItemInstance item = pl.getInventory().addItem("reward", 57, rewardCnt, pl, pl);
						pl.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S).addItemName(item).addNumber(rewardCnt));
						pl.sendPacket(new ItemList(pl, false));
					}
				}
	}

	public long getTimeRemaining()
	{
		return (_enddate - System.currentTimeMillis()) / 60000;
	}

	public String getWinnerName(int par)
	{
		if (_winPlayersName.size() >= par)
			return _winPlayersName.get(par - 1);
		return "0";
	}

	public synchronized void newFish(L2PcInstance pl)
	{
		float p1 = Rnd.get(60, 90);
		float len = Rnd.get(0, 99) / 100 + p1;
		if (_tmpPlayer.size() < 5)
		{
			for (int x = 0; x < _tmpPlayer.size(); x++)
				if (_tmpPlayer.get(x)._name.equalsIgnoreCase(pl.getName()))
				{
					if (_tmpPlayer.get(x)._length < len)
					{
						_tmpPlayer.get(x)._length = len;
						pl.sendMessage(Message.getMessage(pl, Message.MessageId.MSG_FISHCHAMP_BETTER_RESULT));
						setNewMin();
					}
					return;
				}
			Fisher newFisher = new Fisher();
			newFisher._name = pl.getName();
			newFisher._length = len;
			_tmpPlayer.add(newFisher);
			pl.sendMessage(Message.getMessage(pl, Message.MessageId.MSG_FISHCHAMP_WINER_LIST));
			setNewMin();
		}
		else if (_minFishLength < len)
		{
			for (int x = 0; x < _tmpPlayer.size(); x++)
				if (_tmpPlayer.get(x)._name.equalsIgnoreCase(pl.getName()))
				{
					if (_tmpPlayer.get(x)._length < len)
					{
						_tmpPlayer.get(x)._length = len;
						pl.sendMessage(Message.getMessage(pl, Message.MessageId.MSG_FISHCHAMP_BETTER_RESULT));
						setNewMin();
					}
					return;
				}
			Fisher minFisher = null;
			float minLen = 99999;
			for (int x = 0; x < _tmpPlayer.size(); x++)
				if (_tmpPlayer.get(x)._length < minLen)
				{
					minFisher = _tmpPlayer.get(x);
					minLen = minFisher._length;
				}
			_tmpPlayer.remove(minFisher);
			Fisher newFisher = new Fisher();
			newFisher._name = pl.getName();
			newFisher._length = len;
			_tmpPlayer.add(newFisher);
			pl.sendMessage(Message.getMessage(pl, Message.MessageId.MSG_FISHCHAMP_WINER_LIST));
			setNewMin();
		}
	}

	private synchronized void refreshResult()
	{
		_needRefresh = false;
		_playersName.clear();
		_fishLength.clear();
		Fisher fisher1 = null;
		Fisher fisher2 = null;
		for (int x = 0; x <= _tmpPlayer.size() - 1; x++)
		{
			for (int y = 0; y <= _tmpPlayer.size() - 2; y++)
			{
				fisher1 = _tmpPlayer.get(y);
				fisher2 = _tmpPlayer.get(y + 1);
				if (fisher1._length < fisher2._length)
				{
					_tmpPlayer.set(y, fisher2);
					_tmpPlayer.set(y + 1, fisher1);
				}
			}
		}
		for (int x = 0; x <= _tmpPlayer.size() - 1; x++)
		{
			_playersName.add(_tmpPlayer.get(x)._name);
			_fishLength.add("" + _tmpPlayer.get(x)._length);
		}
	}

	public void refreshWinResult()
	{
		_winPlayersName.clear();
		_winFishLength.clear();
		Fisher fisher1 = null;
		Fisher fisher2 = null;
		for (int x = 0; x <= _winPlayer.size() - 1; x++)
		{
			for (int y = 0; y <= _winPlayer.size() - 2; y++)
			{
				fisher1 = _winPlayer.get(y);
				fisher2 = _winPlayer.get(y + 1);
				if (fisher1._length < fisher2._length)
				{
					_winPlayer.set(y, fisher2);
					_winPlayer.set(y + 1, fisher1);
				}
			}
		}
		for (int x = 0; x <= _winPlayer.size() - 1; x++)
		{
			_winPlayersName.add(_winPlayer.get(x)._name);
			_winFishLength.add("" + _winPlayer.get(x)._length);
		}
	}

	
	private void restoreData()
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			PreparedStatement statement = con.prepareStatement("SELECT finish_date FROM fishing_championship_date");
			ResultSet rs = statement.executeQuery();
			while (rs.next())
			{
				_enddate = rs.getLong("finish_date");
			}
			rs.close();
			statement.close();
			statement = con.prepareStatement("SELECT PlayerName,fishLength,rewarded FROM fishing_championship");
			rs = statement.executeQuery();
			while (rs.next())
			{
				int rewarded = rs.getInt("rewarded");
				if (rewarded == 0)
				{
					Fisher fisher = new Fisher();
					fisher._name = rs.getString("PlayerName");
					fisher._length = rs.getFloat("fishLength");
					_tmpPlayer.add(fisher);
				}
				if (rewarded > 0)
				{
					Fisher fisher = new Fisher();
					fisher._name = rs.getString("PlayerName");
					fisher._length = rs.getFloat("fishLength");
					fisher._rewarded = rewarded;
					_winPlayer.add(fisher);
				}
			}
			rs.close();
			statement.close();
		}
		catch (Exception e)
		{
			_log.error("Exception: can't get fishing championship info: " + e.getMessage(), e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	public void setEndOfChamp()
	{
		Calendar finishtime = Calendar.getInstance();
		finishtime.setTimeInMillis(_enddate);
		finishtime.set(Calendar.MINUTE, 0);
		finishtime.set(Calendar.SECOND, 0);
		finishtime.add(Calendar.DAY_OF_MONTH, 6);
		finishtime.set(Calendar.DAY_OF_WEEK, 3);
		finishtime.set(Calendar.HOUR_OF_DAY, 19);
		_enddate = finishtime.getTimeInMillis();
	}

	private void setNewMin()
	{
		float minLen = 99999;
		for (int x = 0; x < _tmpPlayer.size(); x++)
			if (_tmpPlayer.get(x)._length < minLen)
			{
				minLen = _tmpPlayer.get(x)._length;
			}
		_minFishLength = minLen;
	}

	public void showMidResult(L2PcInstance pl)
	{
		if (_needRefresh == true)
		{
			refreshResult();
			ThreadPoolManager.getInstance().scheduleGeneral(new needRefresh(), 60000);
		}
		NpcHtmlMessage html = new NpcHtmlMessage(pl.getObjectId());
		String str;
		str = "<html><head><title>Royal tournament fishing</title></head>";
		str += "There are fishing contest! This tournament is organized and sponsored by the Guild of fishermen. Our goal is to attract attention to fishing as many people as possible. If caught the big catch fisherman will receive valuable prize!<br><br>";
		str += "At the conclusion of the tournament, the winners will receive their award at the Guild of fishermen. Pick up the prize can only be <font color=\"LEVEL\"> in the week after the tournament</font>!<br>";
		str += "<table width=280 border=0 bgcolor=\"000000\"><tr><td width=70 align=center>Place</td><td width=110 align=center>Fisherman</td><td width=80 align=center>The Length Of The</td></tr></table><table width=280>";
		for (int x = 1; x <= 5; x++)
		{
			str += "<tr><td width=70 align=center>" + x + " Place:</td>";
			str += "<td width=110 align=center>" + getCurrentName(x) + "</td>";
			str += "<td width=80 align=center>" + getCurrentFishLength(x) + "</td></tr>";
		}
		str += "<td width=80 align=center>0</td></tr></table><br>";
		str += "List of prizes<br><table width=280 border=0 bgcolor=\"000000\"><tr><td width=70 align=center>place</td><td width=110 align=center>prize</td><td width=80 align=center>The Number Of</td></tr></table><table width=280>";
		str += "<tr><td width=70 align=center>1 place:</td><td width=110 align=center>Adena</td><td width=80 align=center>800000</td></tr><tr><td width=70 align=center>2 place:</td><td width=110 align=center>Adena</td><td width=80 align=center>500000</td></tr><tr><td width=70 align=center>3 place:</td><td width=110 align=center>Adena</td><td width=80 align=center>300000</td></tr>";
		str += "<tr><td width=70 align=center>4 place:</td><td width=110 align=center>Adena</td><td width=80 align=center>200000</td></tr><tr><td width=70 align=center>5 place:</td><td width=110 align=center>Adena</td><td width=80 align=center>100000</td></tr></table></body></html>";
		html.setHtml(str);
		pl.sendPacket(html);
	}

	public void shutdown()
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			PreparedStatement statement;
			statement = con.prepareStatement("DELETE FROM fishing_championship_date");
			statement.execute();
			statement.close();
			statement = con.prepareStatement("INSERT INTO fishing_championship_date (finish_date) VALUES (?)");
			statement.setLong(1, _enddate);
			statement.execute();
			statement.close();

			statement = con.prepareStatement("DELETE FROM fishing_championship");
			statement.execute();
			statement.close();

			for (Fisher fisher : _winPlayer)
			{
				statement = con.prepareStatement("INSERT INTO fishing_championship(PlayerName,fishLength,rewarded) VALUES (?,?,?)");
				statement.setString(1, fisher._name);
				statement.setFloat(2, fisher._length);
				statement.setInt(3, fisher._rewarded);
				statement.execute();
				statement.close();
			}
			for (Fisher fisher : _tmpPlayer)
			{
				statement = con.prepareStatement("INSERT INTO fishing_championship(PlayerName,fishLength,rewarded) VALUES (?,?,?)");
				statement.setString(1, fisher._name);
				statement.setFloat(2, fisher._length);
				statement.setInt(3, 0);
				statement.execute();
				statement.close();
			}
		}
		catch (Exception e)
		{
			_log.error("Exception: can't update player vitality: " + e.getMessage(), e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}
}