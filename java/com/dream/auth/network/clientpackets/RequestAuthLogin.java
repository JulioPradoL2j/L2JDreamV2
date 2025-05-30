package com.dream.auth.network.clientpackets;

import java.security.GeneralSecurityException;

import javax.crypto.Cipher;

import com.dream.AuthConfig;
import com.dream.auth.L2AuthClient;
import com.dream.auth.L2AuthClient.LoginClientState;
import com.dream.auth.manager.AuthManager;
import com.dream.auth.manager.AuthManager.AuthLoginResult;
import com.dream.auth.model.AccountKicked;
import com.dream.auth.model.AccountKicked.AccountKickedReason;
import com.dream.auth.model.GameServerInfo;
import com.dream.auth.network.serverpackets.AuthFailReason;
import com.dream.auth.network.serverpackets.AuthOk;
import com.dream.auth.network.serverpackets.ServerList;
import com.dream.auth.services.AccountBannedException;

public class RequestAuthLogin extends L2AuthClientPacket
{

	private final byte[] _raw = new byte[128];
	private String _user, _password;

	private int _ncotp;

	public int getOneTimePassword()
	{
		return _ncotp;
	}

	public String getPassword()
	{
		return _password;
	}

	public String getUser()
	{
		return _user;
	}

	@Override
	public boolean readImpl()
	{
		if (super._buf.remaining() >= 128)
		{
			readB(_raw);
			return true;
		}
		return false;
	}

	@Override
	public void run()
	{

		byte[] decrypted = null;
		final L2AuthClient client = getClient();
		try
		{
			final Cipher rsaCipher = Cipher.getInstance("RSA/ECB/nopadding");
			rsaCipher.init(Cipher.DECRYPT_MODE, getClient().getRSAPrivateKey());
			decrypted = rsaCipher.doFinal(_raw, 0x00, 0x80);
		}
		catch (GeneralSecurityException e)
		{
			return;
		}

		try
		{
			_user = new String(decrypted, 0x5E, 14).trim().toLowerCase();
			_password = new String(decrypted, 0x6C, 16).trim();
			_ncotp = decrypted[0x7c];
			_ncotp |= decrypted[0x7d] << 8;
			_ncotp |= decrypted[0x7e] << 16;
			_ncotp |= decrypted[0x7f] << 24;
		}
		catch (Exception e)
		{
			return;
		}

		AuthManager lc = AuthManager.getInstance();
		try
		{
			AuthLoginResult result = lc.tryAuthLogin(_user, _password, client);
			switch (result)
			{
				case AUTH_SUCCESS:
					client.setAccount(_user);
					client.setState(LoginClientState.AUTHED_LOGIN);
					client.setSessionKey(lc.assignSessionKeyToClient(_user, client));
					if (AuthConfig.SHOW_LICENCE)
					{
						client.sendPacket(new AuthOk(getClient().getSessionKey()));
					}
					else
					{
						getClient().sendPacket(new ServerList(getClient()));
					}

					break;
				case ALREADY_ON_LS:
					L2AuthClient oldClient;
					if ((oldClient = lc.getAuthedClient(_user)) != null)
					{
						// kick the other client
						oldClient.close(AuthFailReason.REASON_ACCOUNT_IN_USE);
						lc.removeAuthedLoginClient(_user);
					}
					// kick also current client
					client.close(AuthFailReason.REASON_ACCOUNT_IN_USE);
					break;
				case ALREADY_ON_GS:
					GameServerInfo gsi;
					if ((gsi = lc.getAccountOnGameServer(_user)) != null)
					{
						client.close(AuthFailReason.REASON_ACCOUNT_IN_USE);

						if (gsi.isAuthed())
						{
							gsi.getGameServerThread().kickPlayer(_user);
						}
					}
					gsi = null;
					break;
				case INVALID_PASSWORD:
					client.close(AuthFailReason.REASON_USER_OR_PASS_WRONG);
					break;
				case ACCOUNT_BANNED:
					client.close(new AccountKicked(AccountKickedReason.REASON_PERMANENTLY_BANNED));
					break;

			}
		}
		catch (AccountBannedException e)
		{
			client.close(AuthFailReason.REASON_ACCOUNT_BANNED);
		}
	}
}