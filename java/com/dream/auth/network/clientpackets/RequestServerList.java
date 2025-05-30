/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */
package com.dream.auth.network.clientpackets;

import com.dream.auth.network.serverpackets.AuthFailReason;
import com.dream.auth.network.serverpackets.ServerList;

public class RequestServerList extends L2AuthClientPacket
{
	private int _skey1;
	private int _skey2;
	private int _data3;

	public int getData3()
	{
		return _data3;
	}

	public int getSessionKey1()
	{
		return _skey1;
	}

	public int getSessionKey2()
	{
		return _skey2;
	}

	@Override
	public boolean readImpl()
	{
		if (super._buf.remaining() >= 8)
		{
			_skey1 = readD();
			_skey2 = readD();
			return true;
		}
		return false;
	}

	@Override
	public void run()
	{
		if (getClient().getSessionKey().checkLoginPair(_skey1, _skey2))
		{
			getClient().sendPacket(new ServerList(getClient()));
		}
		else
		{
			getClient().close(AuthFailReason.REASON_ACCESS_FAILED);
		}
	}
}