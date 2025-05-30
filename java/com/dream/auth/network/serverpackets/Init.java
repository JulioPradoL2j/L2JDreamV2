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
package com.dream.auth.network.serverpackets;

import com.dream.auth.L2AuthClient;

public final class Init extends L2AuthServerPacket
{
	private final int _sessionId;

	private final byte[] _publicKey;
	private final byte[] _blowfishKey;

	public Init(byte[] publickey, byte[] blowfishkey, int sessionId)
	{
		_sessionId = sessionId;
		_publicKey = publickey;
		_blowfishKey = blowfishkey;
	}

	public Init(L2AuthClient client)
	{
		this(client.getScrambledModulus(), client.getBlowfishKey(), client.getSessionId());
	}

	@Override
	protected void write()
	{
		writeC(0x00);

		writeD(_sessionId);
		writeD(0x0000c621);

		writeB(_publicKey);

		writeD(0x29DD954E);
		writeD(0x77C39CFC);
		writeD(0x97ADB620);
		writeD(0x07BDE0F7);

		writeB(_blowfishKey);
		writeC(0x00);
	}
}