package com.dream.game.manager;

import com.dream.L2DatabaseFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Calendar;

import org.apache.log4j.Logger;

public abstract class ClanHallSiege
{
	protected static Logger _log = Logger.getLogger(ClanHallSiege.class.getName());
	private Calendar _siegeDate;
	public Calendar _siegeEndDate;
	private boolean _isInProgress = false;

	public final boolean getIsInProgress()
	{
		return _isInProgress;
	}

	public final Calendar getSiegeDate()
	{
		return _siegeDate;
	}

	
	public long restoreSiegeDate(int ClanHallId)
	{
		long res = 0;
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("SELECT siege_data FROM clanhall_siege WHERE id=?");
			statement.setInt(1, ClanHallId);
			ResultSet rs = statement.executeQuery();

			if (rs.next())
			{
				res = rs.getLong("siege_data");
			}

			rs.close();
			statement.close();
		}
		catch (Exception e)
		{
			_log.error("Exception: can't get clanhall siege date: " + e.getMessage(), e);
		}

		return res;
	}

	public final void setIsInProgress(boolean par)
	{
		_isInProgress = par;
	}

	
	public void setNewSiegeDate(long siegeDate, int ClanHallId, int hour)
	{
		Calendar tmpDate = Calendar.getInstance();
		if (siegeDate <= System.currentTimeMillis())
		{
			tmpDate.setTimeInMillis(System.currentTimeMillis());
			tmpDate.add(Calendar.DAY_OF_MONTH, 3);
			tmpDate.set(Calendar.DAY_OF_WEEK, 6);
			tmpDate.set(Calendar.HOUR_OF_DAY, hour);
			tmpDate.set(Calendar.MINUTE, 0);
			tmpDate.set(Calendar.SECOND, 0);

			setSiegeDate(tmpDate);
			try (Connection con = L2DatabaseFactory.getInstance().getConnection())
			{
				PreparedStatement statement = con.prepareStatement("UPDATE clanhall_siege SET siege_data=? WHERE id = ?");
				statement.setLong(1, getSiegeDate().getTimeInMillis());
				statement.setInt(2, ClanHallId);
				statement.execute();
				statement.close();
			}
			catch (Exception e)
			{
				_log.error("Exception: can't save clanhall siege date: " + e.getMessage(), e);
			}
		}
	}

	public final void setSiegeDate(Calendar par)
	{
		_siegeDate = par;
	}
}