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
package com.dream.game.idfactory;

public class IncrementIDFactory extends IdFactory
{
	private int _curId;

	protected IncrementIDFactory()
	{
		super();
		try
		{
			int ids[] = extractUsedObjectIDTable();
			_curId = ids[ids.length - 1];
			_curId++;
		}
		catch (Exception e)
		{
			_curId = FIRST_OID;
		}
		_log.info("IdFactory: Next objectId is " + _curId);
		_initialized = true;
	}

	@Override
	protected void cleanUpDB()
	{

	}

	@Override
	public int getCurrentId()
	{
		return _curId;
	}

	@Override
	public synchronized int getNextId()
	{
		return _curId++;
	}

	@Override
	public synchronized void releaseId(int id)
	{

	}

	@Override
	protected void setAllCharacterOffline()
	{

	}

	@Override
	public int size()
	{
		return LAST_OID - _curId;
	}
}