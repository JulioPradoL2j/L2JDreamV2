package com.dream.auth;

import com.dream.auth.manager.BanManager;
import com.dream.auth.network.serverpackets.Init;
import com.dream.mmocore.IAcceptFilter;
import com.dream.mmocore.IClientFactory;
import com.dream.mmocore.IMMOExecutor;
import com.dream.mmocore.MMOConnection;
import com.dream.mmocore.ReceivablePacket;
import com.dream.tools.security.IPv4Filter;

import java.nio.channels.SocketChannel;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class SelectorHelper implements IMMOExecutor<L2AuthClient>, IClientFactory<L2AuthClient>, IAcceptFilter
{
	private final ThreadPoolExecutor _generalPacketsThreadPool;

	private final IPv4Filter _ipv4filter;

	public SelectorHelper()
	{
		_generalPacketsThreadPool = new ThreadPoolExecutor(4, 6, 15L, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
		_ipv4filter = new IPv4Filter();
	}

	@Override
	public boolean accept(SocketChannel sc)
	{
		return _ipv4filter.accept(sc) && !BanManager.getInstance().isBannedAddress(sc.socket().getInetAddress());
	}

	@Override
	public L2AuthClient create(MMOConnection<L2AuthClient> con)
	{
		L2AuthClient client = new L2AuthClient(con);
		client.sendPacket(new Init(client));
		return client;
	}

	@Override
	public void execute(ReceivablePacket<L2AuthClient> packet)
	{
		_generalPacketsThreadPool.execute(packet);
	}
}