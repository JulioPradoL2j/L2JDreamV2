package com.dream.game.datatables.sql;

import com.dream.L2DatabaseFactory;
import com.dream.game.Shutdown;
import com.dream.util.StatsSet;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;

public class ServerData
{
	private static class SingletonHolder
	{
		protected static final ServerData _instance = new ServerData();
	}

	private static Logger _log = Logger.getLogger(ServerData.class);

	public static final ServerData getInstance()
	{
		return SingletonHolder._instance;
	}

	private final StatsSet _serverData = new StatsSet();

	
	public ServerData()
	{
		try
		{
			Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement stm = con.prepareStatement("select * from server_data");
			ResultSet rs = stm.executeQuery();
			while (rs.next())
			{
				_serverData.set(rs.getString(1), rs.getString(2));
			}
			rs.close();
			stm.close();
			con.close();
			Shutdown.getInstance().registerShutdownHandler(new Runnable()
			{
				@Override
				public void run()
				{
					save();
				}
			});
		}
		catch (SQLException e)
		{
			_log.error("ServerData: Unable to retrive server data, exiting...", e);
			System.exit(1);
		}
	}

	public StatsSet getData()
	{
		return _serverData;
	}

	
	public void save()
	{
		try
		{

			Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement update = con.prepareStatement("update server_data set valueData = ? where valueName = ?");
			PreparedStatement insert = con.prepareStatement("insert into  server_data values(?, ?)");
			for (String s : _serverData.getSet().keySet())
			{
				update.setString(2, s);
				update.setString(1, _serverData.getString(s));
				if (update.executeUpdate() == 0)
				{
					insert.setString(1, s);
					insert.setString(2, _serverData.getString(s));
					insert.execute();
				}
			}
			insert.close();
			update.close();
			con.close();
		}
		catch (SQLException e)
		{
			_log.error("ServerData: Unable to save server data!", e);
		}
	}
}