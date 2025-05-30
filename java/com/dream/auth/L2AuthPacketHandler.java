package com.dream.auth;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.dream.AuthConfig;
import com.dream.auth.L2AuthClient.LoginClientState;
import com.dream.auth.network.clientpackets.AuthGameGuard;
import com.dream.auth.network.clientpackets.RequestAuthLogin;
import com.dream.auth.network.clientpackets.RequestServerList;
import com.dream.auth.network.clientpackets.RequestServerLogin;
import com.dream.auth.network.serverpackets.AuthFailReason;
import com.dream.mmocore.IPacketHandler;
import com.dream.mmocore.ReceivablePacket;

public final class L2AuthPacketHandler implements IPacketHandler<L2AuthClient>
{
	public class ConnectionChecker extends Thread
	{
		@Override
		public void run()
		{
			for (;;)
			{
				long curTime = System.currentTimeMillis();
				synchronized (_connections)
				{
					for (L2AuthClient cl : _connections.keySet())
						if (!cl.checkOK && _connections.get(cl) + AuthConfig.INACTIVE_TIMEOUT * 1000 > curTime)
						{
							cl.close(AuthFailReason.REASON_IGNORE);
							_connections.remove(cl);
						}
				}
				try
				{
					Thread.sleep(2000);
				}
				catch (Exception e)
				{

				}
			}
		}
	}

	private static L2AuthPacketHandler _instance;

	private static void debugOpcode(int opcode, LoginClientState state)
	{
		System.out.println("Unknown Opcode: " + opcode + " for state: " + state.name());
	}

	public static L2AuthPacketHandler getInstance()
	{
		return _instance;
	}

	public final Map<L2AuthClient, Long> _connections = new ConcurrentHashMap<>();

	public L2AuthPacketHandler()
	{
		super();
		_instance = this;
		new ConnectionChecker().start();
	}

	public void addClient(L2AuthClient cl)
	{
		synchronized (_connections)
		{
			_connections.put(cl, System.currentTimeMillis());
		}
	}

	@Override
	public ReceivablePacket<L2AuthClient> handlePacket(ByteBuffer buf, L2AuthClient client)
	{
		int opcode = buf.get() & 0xFF;
		ReceivablePacket<L2AuthClient> packet = null;
		LoginClientState state = client.getState();
		switch (state)
		{
			case CONNECTED:
				if (opcode == 0x07)
				{
					packet = new AuthGameGuard();
				}
				else
				{
					debugOpcode(opcode, state);
					client.close(AuthFailReason.REASON_ACCESS_FAILED);
				}
				break;
			case AUTHED_GG:
				if (opcode == 0x00)
				{
					packet = new RequestAuthLogin();
				}
				else
				{
					if (client.getAccount() != null)
					{
						debugOpcode(opcode, state);
					}
					client.close(AuthFailReason.REASON_ACCESS_FAILED);
				}
				break;
			case AUTHED_LOGIN:
				if (opcode == 0x06)
				{
					client.setState(LoginClientState.AUTHED_CARD);
					packet = new RequestServerList();
				}

				else if (opcode == 0x05)
				{
					packet = new RequestServerList();
				}
				else if (opcode == 0x02)
				{
					packet = new RequestServerLogin();
				}
				else
				{
					debugOpcode(opcode, state);
				}
				break;
		}
		if (packet != null && !client.checkOK)
		{
			client.checkOK = true;
			synchronized (_connections)
			{
				_connections.remove(client);
			}
			ClientManager.getInstance().addClient(client);
		}
		return packet;
	}
}