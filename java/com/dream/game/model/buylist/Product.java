/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.dream.game.model.buylist;

import com.dream.L2DatabaseFactory;
import com.dream.game.network.ThreadPoolManager;
import com.dream.game.templates.item.L2Item;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author KenM
 */
public class Product
{
	protected final class RestockTask implements Runnable
	{
		@Override
		public void run()
		{
			restock();
		}
	}

	private static final Logger _log = Logger.getLogger(Product.class.getName());
	private final int _buyListId;
	private final L2Item _item;
	private final int _price;
	private final long _restockDelay;
	private final int _maxCount;
	private AtomicInteger _count = null;

	private ScheduledFuture<?> _restockTask = null;

	public Product(int buyListId, L2Item item, int price, long restockDelay, int maxCount)
	{
		_buyListId = buyListId;
		_item = item;
		_price = price;
		_restockDelay = restockDelay * 60000;
		_maxCount = maxCount;

		if (hasLimitedStock())
		{
			_count = new AtomicInteger(maxCount);
		}
	}

	public boolean decreaseCount(int val)
	{
		if (_count == null)
			return false;

		if (_restockTask == null || _restockTask.isDone())
		{
			_restockTask = ThreadPoolManager.getInstance().scheduleGeneral(new RestockTask(), getRestockDelay());
		}

		boolean result = _count.addAndGet(-val) >= 0;
		save();
		return result;
	}

	public int getBuyListId()
	{
		return _buyListId;
	}

	public int getCount()
	{
		if (_count == null)
			return 0;

		final int count = _count.get();
		return count > 0 ? count : 0;
	}

	public L2Item getItem()
	{
		return _item;
	}

	public int getItemId()
	{
		return getItem().getItemId();
	}

	public int getMaxCount()
	{
		return _maxCount;
	}

	public int getPrice()
	{
		return _price;
	}

	public long getRestockDelay()
	{
		return _restockDelay;
	}

	public boolean hasLimitedStock()
	{
		return getMaxCount() > -1;
	}

	public void restartRestockTask(long nextRestockTime)
	{
		final long remainingTime = nextRestockTime - System.currentTimeMillis();
		if (remainingTime > 0)
		{
			_restockTask = ThreadPoolManager.getInstance().scheduleGeneral(new RestockTask(), remainingTime);
		}
		else
		{
			restock();
		}
	}

	public void restock()
	{
		setCount(getMaxCount());
		save();
	}

	
	private void save()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("INSERT INTO `buylists`(`buylist_id`, `item_id`, `count`, `next_restock_time`) VALUES(?, ?, ?, ?) ON DUPLICATE KEY UPDATE `count` = ?, `next_restock_time` = ?");
			statement.setInt(1, getBuyListId());
			statement.setInt(2, getItemId());
			statement.setInt(3, getCount());
			statement.setInt(5, getCount());

			if (_restockTask != null && _restockTask.getDelay(TimeUnit.MILLISECONDS) > 0)
			{
				long nextRestockTime = System.currentTimeMillis() + _restockTask.getDelay(TimeUnit.MILLISECONDS);
				statement.setLong(4, nextRestockTime);
				statement.setLong(6, nextRestockTime);
			}
			else
			{
				statement.setLong(4, 0);
				statement.setLong(6, 0);
			}
			statement.executeUpdate();
			statement.close();
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Failed to save Product buylist_id:" + getBuyListId() + " item_id:" + getItemId(), e);
		}
	}

	public void setCount(int currentCount)
	{
		if (_count == null)
		{
			_count = new AtomicInteger();
		}

		_count.set(currentCount);
	}
}