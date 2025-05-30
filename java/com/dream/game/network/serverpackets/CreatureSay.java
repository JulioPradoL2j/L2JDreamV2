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
package com.dream.game.network.serverpackets;

import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.SystemChatChannelId;

public class CreatureSay extends L2GameServerPacket
{
	private final int _objectId;
	private final SystemChatChannelId _channel;
	private final String _charName;
	private final String _text;

	public CreatureSay(int objectId, SystemChatChannelId channel, String charName, String text)
	{
		_objectId = objectId;
		_channel = channel;
		_charName = charName;
		_text = text;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0x4a);

		writeD(_objectId);
		writeD(_channel.getId());
		writeS(_charName);
		writeS(_text);
		L2PcInstance _pci = getClient().getActiveChar();
		if (_pci != null)
		{
			_pci.broadcastSnoop(_channel.getId(), _charName, _text);
		}

	}

}