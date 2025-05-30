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
package com.dream.game.model;

import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.ThreadPoolManager;
import com.dream.game.network.clientpackets.L2GameClientPacket;
import com.dream.game.network.serverpackets.SystemMessage;

public class L2Request
{
	private static final int REQUEST_TIMEOUT = 15;

	protected L2PcInstance _player;
	protected L2PcInstance _partner;
	protected boolean _isRequestor;
	protected boolean _isAnswerer;
	protected L2GameClientPacket _requestPacket;

	public L2Request(L2PcInstance player)
	{
		_player = player;
	}

	protected void clear()
	{
		_partner = null;
		_requestPacket = null;
		_isRequestor = false;
		_isAnswerer = false;
	}

	public L2PcInstance getPartner()
	{
		return _partner;
	}

	public L2GameClientPacket getRequestPacket()
	{
		return _requestPacket;
	}

	public boolean isProcessingRequest()
	{
		return _partner != null;
	}

	public void onRequestResponse()
	{
		if (_partner != null)
		{
			_partner.getRequest().clear();
		}
		clear();
	}

	private void setOnRequestTimer(boolean isRequestor)
	{
		_isRequestor = isRequestor;
		_isAnswerer = !isRequestor;
		ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
		{
			@Override
			public void run()
			{
				clear();
			}
		}, REQUEST_TIMEOUT * 1000);

	}

	private synchronized void setPartner(L2PcInstance partner)
	{
		_partner = partner;
	}

	public synchronized boolean setRequest(L2PcInstance partner, L2GameClientPacket packet)
	{
		if (partner == null)
		{
			_player.sendPacket(SystemMessageId.YOU_HAVE_INVITED_THE_WRONG_TARGET);
			return false;
		}
		if (partner.getRequest().isProcessingRequest())
		{
			_player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_IS_BUSY_TRY_LATER).addString(partner.getName()));
			return false;
		}
		if (isProcessingRequest())
		{
			_player.sendPacket(SystemMessageId.WAITING_FOR_ANOTHER_REPLY);
			return false;
		}

		_partner = partner;
		_requestPacket = packet;
		setOnRequestTimer(true);
		_partner.getRequest().setPartner(_player);
		_partner.getRequest().setRequestPacket(packet);
		_partner.getRequest().setOnRequestTimer(false);
		return true;
	}

	private synchronized void setRequestPacket(L2GameClientPacket packet)
	{
		_requestPacket = packet;
	}
}