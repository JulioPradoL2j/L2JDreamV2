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
package com.dream.game.network.clientpackets;

import com.dream.Config;
import com.dream.game.network.serverpackets.KeyPacket;
import com.dream.game.network.serverpackets.L2GameServerPacket;

public final class ProtocolVersion extends L2GameClientPacket
{
	private int _version;
	
	@Override
	protected void readImpl()
	{
		_version = readD();
		
		if (_buf.remaining() > 260)
		{
			byte[] _data = new byte[260];
			readB(_data);
			
		}
		
	}
	
	@Override
	protected void runImpl()
	{
		if (_version == -2)
		{
			getClient().close((L2GameServerPacket) null);
		}
		
		if (_version < Config.MIN_PROTOCOL_REVISION || _version > Config.MAX_PROTOCOL_REVISION)
		{
			_log.info("Client: " + getClient().toString() + " -> Protocol Revision: " + _version + " is invalid.");
			getClient().sendPacket(new KeyPacket(getClient().enableCrypt(), 0));
			getClient().setProtocolOk(false);
			getClient().close((L2GameServerPacket) null);
		}
		else
		{
			
			getClient().setProtocolOk(true);
			getClient().setProtocolVer(_version);
			getClient().sendPacket(new KeyPacket(getClient().enableCrypt(), 1));
		}
		
	}
}