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

import com.dream.AuthConfig;
import com.dream.auth.manager.AuthManager;
import com.dream.auth.model.SessionKey;
import com.dream.auth.network.serverpackets.AuthFailReason;
import com.dream.auth.network.serverpackets.PlayFailReason;
import com.dream.auth.network.serverpackets.PlayOk;

public class RequestServerLogin extends L2AuthClientPacket
{
	private int _skey1;
	private int _skey2;
	private int _serverId;

	public int getServerID()
	{
		return _serverId;
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
		if (super._buf.remaining() >= 9)
		{
			_skey1 = readD();
			_skey2 = readD();
			_serverId = readC();
			return true;
		}
		return false;
	}

	@Override
	public void run()
	{
		SessionKey sk = getClient().getSessionKey();

		if (AuthConfig.BRUT_PROTECTION_ENABLED && getClient().getAccount() == null)
		{
			getClient().close(AuthFailReason.REASON_ACCESS_FAILED);
			return;
		}
		if (!AuthConfig.SHOW_LICENCE || sk.checkLoginPair(_skey1, _skey2))
		{
			if (AuthManager.getInstance().isLoginPossible(getClient().getAccessLevel(), _serverId))
			{
				getClient().setLastServerId(_serverId);
				getClient()._accInfo.setLastactive(System.currentTimeMillis() / 1000);
				AuthManager.getInstance().addOrUpdateAccount(getClient()._accInfo);

				AuthManager.getInstance().setAccountLastServerId(getClient().getAccount(), _serverId);
				getClient().setJoinedGS(true);
				getClient().sendPacket(new PlayOk(sk));
			}
			else
			{
				getClient().close(PlayFailReason.REASON_TOO_MANY_PLAYERS);
			}
		}
		else
		{
			getClient().close(AuthFailReason.REASON_ACCESS_FAILED);
		}
	}
}