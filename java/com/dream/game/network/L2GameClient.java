package com.dream.game.network;

import com.dream.Config;
import com.dream.L2DatabaseFactory;
import com.dream.game.communitybbs.Manager.RegionBBSManager;
import com.dream.game.datatables.sql.ClanTable;
import com.dream.game.model.CharSelectInfoPackage;
import com.dream.game.model.L2Clan;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.world.L2World;
import com.dream.game.network.AuthServerThread.SessionKey;
import com.dream.game.network.serverpackets.ActionFailed;
import com.dream.game.network.serverpackets.L2GameServerPacket;
import com.dream.game.network.serverpackets.LeaveWorld;
import com.dream.game.network.serverpackets.ServerClose;
import com.dream.mmocore.MMOClient;
import com.dream.mmocore.MMOConnection;
import com.dream.mmocore.ReceivablePacket;
import com.dream.tools.security.BlowFishKeygen;
import com.dream.tools.security.GameCrypt;
import com.dream.util.StatsSet;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;

public final class L2GameClient extends MMOClient<MMOConnection<L2GameClient>> implements Runnable
{
	protected class AutoSave implements Runnable
	{
		@Override
		public void run()
		{
			try
			{
				if (getActiveChar() != null && getActiveChar().isOnline() == 1)
				{
					getActiveChar().store();

					if (getActiveChar().getPet() != null)
					{
						getActiveChar().getPet().store();
					}
				}
			}
			catch (Exception e)
			{
				_log.warn("Error on AutoSaveTask.", e);
			}
		}
	}

	class CleanupTask implements Runnable
	{
		@Override
		public void run()
		{
			try
			{
				if (_autoSaveInDB != null)
				{
					_autoSaveInDB.cancel(true);
				}

				L2PcInstance player = getActiveChar();
				if (player != null)
				{
					player.setClient(null);

					if (player.isOnline() == 1)
					{
						player.deleteMe();
					}
				}

				setActiveChar(null);
			}
			catch (Exception e1)
			{
				_log.error("Error while cleanup client.", e1);
			}
			finally
			{
				AuthServerThread.getInstance().sendLogout(getAccountName());
			}
		}
	}

	public static enum GameClientState
	{
		CONNECTED,
		AUTHED,
		IN_GAME;
	}

	public static interface IExReader
	{
		public void checkChar(L2PcInstance cha);

		public int read(ByteBuffer buf);
	}

	public static final Logger _log = Logger.getLogger(L2GameClient.class.getName());

	private static final String LOAD_ACC_DATA = "SELECT valueName, valueData from account_data where account_name = ?";
	private static final String STORE_ACC_DATA = "REPLACE account_data (account_name, valueName, valueData) VALUES(?, ?, ?)";
	public static final String PLAYER_LANG = "lang";

	
	public static void deleteCharByObjId(int objid)
	{
		if (objid < 0)
			return;

		Connection con = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			PreparedStatement statement;

			statement = con.prepareStatement("DELETE FROM character_friends WHERE charId=? OR friendId=?");
			statement.setInt(1, objid);
			statement.setInt(2, objid);
			statement.execute();
			statement.close();

			statement = con.prepareStatement("DELETE FROM character_hennas WHERE charId=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();

			statement = con.prepareStatement("DELETE FROM character_macroses WHERE charId=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();

			statement = con.prepareStatement("DELETE FROM character_quests WHERE charId=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();

			statement = con.prepareStatement("DELETE FROM character_recipebook WHERE charId=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();

			statement = con.prepareStatement("DELETE FROM character_shortcuts WHERE charId=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();

			statement = con.prepareStatement("DELETE FROM character_skills WHERE charId=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();

			statement = con.prepareStatement("DELETE FROM character_skills_save WHERE charId=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();

			statement = con.prepareStatement("DELETE FROM character_subclasses WHERE charId=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();

			statement = con.prepareStatement("DELETE FROM character_raid_points WHERE charId=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();

			statement = con.prepareStatement("DELETE FROM character_recommends WHERE charId=? OR target_id=?");
			statement.setInt(1, objid);
			statement.setInt(2, objid);
			statement.execute();
			statement.close();

			statement = con.prepareStatement("DELETE FROM heroes WHERE charId=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();

			statement = con.prepareStatement("DELETE FROM olympiad_nobles WHERE charId=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();

			statement = con.prepareStatement("DELETE FROM seven_signs WHERE charId=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();

			statement = con.prepareStatement("DELETE FROM pets WHERE item_obj_id IN (SELECT object_id FROM items WHERE items.owner_id=?)");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();

			statement = con.prepareStatement("DELETE FROM item_attributes WHERE itemId IN (SELECT object_id FROM items WHERE items.owner_id=?)");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();

			statement = con.prepareStatement("DELETE FROM items WHERE owner_id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();

			statement = con.prepareStatement("DELETE FROM characters WHERE charId=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			_log.error("Error deleting character.", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	public GameClientState _state;
	final InetAddress address = getConnection().getInetAddress();
	private int _unknownPackets = 0;
	private String _accountName = null;
	public SessionKey _sessionId;
	private L2PcInstance _activeChar;
	private int[] _charSlotMapping;
	private final ClientStats _stats;
	private GameCrypt _crypt;
	public L2PcInstance activeChar;
	protected ScheduledFuture<?> _autoSaveInDB;
	private final long _connectionStartTime;

	private boolean _disconnected;
	private boolean _protocol;
	private long _protocolVer;

	public IExReader _reader;
	private boolean _isDetached = false;
	private final StatsSet _accountData = new StatsSet();
	private boolean _isAuthedGG;

	private ScheduledFuture<?> _guardCheckTask = null;
	private final ArrayBlockingQueue<ReceivablePacket<L2GameClient>> _packetQueue;

	private final ReentrantLock _queueLock = new ReentrantLock();
	private final ReentrantLock _activeCharLock = new ReentrantLock();

	protected ScheduledFuture<?> _cleanupTask = null;

	private int _bufferError;

	private int _nWindowCount;

	public L2GameClient(MMOConnection<L2GameClient> con)
	{
		super(con);
		_state = GameClientState.CONNECTED;
		_connectionStartTime = System.currentTimeMillis();
		_crypt = new GameCrypt();
		_stats = new ClientStats();
		_packetQueue = new ArrayBlockingQueue<>(Config.CLIENT_PACKET_QUEUE_SIZE);
	
		_autoSaveInDB = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new AutoSave(), Config.AUTOSAVE_INITIAL_TIME, Config.AUTOSAVE_DELAY_TIME);
	}

	public void cleanMe(boolean fast)
	{
		try
		{
			synchronized (this)
			{
				if (_cleanupTask == null)
				{
					_cleanupTask = ThreadPoolManager.getInstance().scheduleGeneral(new CleanupTask(), fast ? 5 : 15000L);
				}
			}
		}
		catch (Exception e1)
		{
			_log.warn("Error during cleanup.", e1);
		}
	}

	public void clearBufferErrors()
	{
		_bufferError = 0;
	}

	synchronized void close(boolean toLoginScreen)
	{
		getConnection().close(toLoginScreen ? ServerClose.STATIC_PACKET : LeaveWorld.STATIC_PACKET);
		setDisconnected();
	}

	public synchronized void close(L2GameServerPacket gsp)
	{
		getConnection().close(gsp);
		setDisconnected();
	}

	public synchronized void closeNow()
	{
		new Disconnection(this).defaultSequence(false);
	}

	@Override
	public boolean decrypt(ByteBuffer buf, int size)
	{
		getCrypt().decrypt(buf.array(), buf.position(), size);
		return true;
	}

	public byte[] enableCrypt()
	{
		byte[] key = BlowFishKeygen.getRandomKey();
		getCrypt().setKey(key);
	
		return key;
	}

	@Override
	public boolean encrypt(final ByteBuffer buf, final int size)
	{
		getCrypt().encrypt(buf.array(), buf.position(), size);
		buf.position(buf.position() + size);
		return true;
	}

	public void execute(ReceivablePacket<L2GameClient> packet)
	{
		if (getStats().countFloods())
		{
			_log.error("Client " + toString() + " - Disconnected, too many floods:" + getStats().longFloods + " long and " + getStats().shortFloods + " short.");
			closeNow();
			return;
		}

		if (!_packetQueue.offer(packet))
		{
			if (getStats().countQueueOverflow())
			{
				_log.error("Client " + toString() + " - Disconnected, too many queue overflows.");
				closeNow();
			}
			else
			{
				sendPacket(ActionFailed.STATIC_PACKET);
			}

			return;
		}

		if (_queueLock.isLocked())
			return;

		try
		{
			if (_state == GameClientState.CONNECTED)
			{
				if (getStats().processedPackets > 3)
				{
					closeNow();
					return;
				}

				ThreadPoolManager.getInstance().executeIOPacket(this);
			}
			else
			{
				ThreadPoolManager.getInstance().executePacket(this);
			}
		}
		catch (RejectedExecutionException e)
		{
			if (!ThreadPoolManager.getInstance().isShutdown())
			{
				_log.error("Failed executing: " + packet.getClass().getSimpleName() + " for Client: " + toString());
			}
		}
	}

	public StatsSet getAccountData()
	{
		return _accountData;
	}

	public String getAccountName()
	{
		return _accountName;
	}

	public L2PcInstance getActiveChar()
	{
		return _activeChar;
	}

	public int getBufferErrors()
	{
		return _bufferError;
	}

	public long getConnectionStartTime()
	{
		return _connectionStartTime;
	}

	private GameCrypt getCrypt()
	{
		if (_crypt == null)
		{
			_crypt = new GameCrypt();
		}
		return _crypt;
	}

	public InetAddress getInetAddress()
	{
		return address;
	}

	public int getnWindowCount()
	{
		return _nWindowCount;
	}

	private int getObjectIdForSlot(int charslot)
	{
		if (_charSlotMapping == null || charslot < 0 || charslot >= _charSlotMapping.length)
		{
			_log.warn(toString() + " tried to delete Character in slot " + charslot + " but no characters exits at that slot.");
			return -1;
		}
		return _charSlotMapping[charslot];
	}

	public long getProtocolVer()
	{
		return _protocolVer;
	}

	public SessionKey getSessionId()
	{
		return _sessionId;
	}

	public GameClientState getState()
	{
		return _state;
	}

	public ClientStats getStats()
	{
		return _stats;
	}

	public void incBufferErrors()
	{
		_bufferError++;
	}

	public int incUnknownPackets()
	{
		if (Config.DEBUG || _log.isDebugEnabled())
		{
			_log.info("Server received unknown packets from IP: " + getConnection().getInetAddress() + " [Account: " + getAccountName() + "]. UPC: " + _unknownPackets + ".");
		}

		return _unknownPackets++;
	}

	public boolean isDetached()
	{
		return _isDetached;
	}

	public boolean isDisconnected()
	{
		return _disconnected;
	}

	public boolean isProtocolOk()
	{
		return _protocol;
	}

	public L2PcInstance loadCharFromDisk(int charslot)
	{
		L2PcInstance character = L2PcInstance.load(getObjectIdForSlot(charslot));

		if (character != null)
		{
			character.setRunning();
			character.standUp();
			character.refreshOverloaded();
			character.refreshExpertisePenalty();
			character.setOnlineStatus(true);
		}
		else
		{
			_log.fatal("could not restore in slot: " + charslot);
		}

		return character;
	}

	
	public void markRestoredChar(int charslot)
	{
		int objid = getObjectIdForSlot(charslot);
		if (objid < 0)
			return;

		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			PreparedStatement statement = con.prepareStatement("UPDATE characters SET deletetime=0 WHERE charId=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			_log.error("Error restoring character.", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	public byte markToDeleteChar(int charslot)
	{
		int objid = getObjectIdForSlot(charslot);

		if (objid < 0)
			return -1;

		byte result = -1;

		Connection con = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			PreparedStatement statement = con.prepareStatement("SELECT clanId from characters WHERE charId=?");
			statement.setInt(1, objid);
			ResultSet rs = statement.executeQuery();
			byte answer = -1;
			if (rs.next())
			{
				int clanId = rs.getInt(1);
				if (clanId != 0)
				{
					L2Clan clan = ClanTable.getInstance().getClan(clanId);

					if (clan == null)
					{
						answer = 0;
					}
					else if (clan.getLeaderId() == objid)
					{
						answer = 2;
					}
					else
					{
						answer = 1;
					}
				}
				else
				{
					answer = 0;
				}

				if (answer == 0)
					if (Config.DELETE_DAYS < 1)
					{
						deleteCharByObjId(objid);
					}
					else
					{
						statement = con.prepareStatement("UPDATE characters SET deletetime=? WHERE charId=?");
						statement.setLong(1, System.currentTimeMillis() + Config.DELETE_DAYS * 86400000L);
						statement.setInt(2, objid);
						statement.execute();
						statement.close();
					}
			}
			result = answer;
		}
		catch (Exception e)
		{
			_log.error("Error updating delete time of character.", e);
			result = -1;
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}

		return result;
	}

	public void onBufferUnderflow()
	{
		if (getStats().countUnderflowException())
		{
			_log.error("Client " + toString() + " - Disconnected: Too many buffer underflow exceptions.");
			closeNow();
			return;
		}

		if (_state == GameClientState.CONNECTED)
		{
			closeNow();
		}
	}

	@Override
	protected synchronized void onDisconnection()
	{
		storeData();
		ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
		{
			@Override
			public void run()
			{
				AuthServerThread.getInstance().sendLogout(getAccountName());
				try
				{
					RegionBBSManager.getInstance().changeCommunityBoard();
				}
				catch (Exception e)
				{
					_log.error("", e);
				}
			}
		}, 0);

		new Disconnection(this).onDisconnection();

		setDisconnected();
	}

	@Override
	protected synchronized void onForcedDisconnection()
	{
		if (Config.DEBUG || _log.isDebugEnabled())
		{
			_log.info("Client " + toString() + " disconnected abnormally.");
		}
	}

	@Override
	public void run()
	{
		if (!_queueLock.tryLock())
			return;

		try
		{
			int count = 0;
			while (true)
			{
				final ReceivablePacket<L2GameClient> packet = _packetQueue.poll();
				if (packet == null)
					return;

				if (_isDetached)
				{
					_packetQueue.clear();
					return;
				}

				try
				{
					packet.run();
				}
				catch (Exception e)
				{
					_log.error("Exception during execution " + packet.getClass().getSimpleName() + ", client: " + toString() + "," + e.getMessage());
				}

				count++;
				if (getStats().countBurst(count))
					return;
			}
		}
		finally
		{
			_queueLock.unlock();
		}
	}

	public void sendPacket(L2GameServerPacket gsp)
	{
		if (gsp == null)
			return;

		if (isDisconnected())
			return;

		if (_isDetached)
			return;

		if (getConnection() != null)
		{
			getConnection().sendPacket(gsp);
		}

		if (Config.SEND_PACKET_LOG)
		{
			_log.info("[S] -> " + gsp.getClass().getSimpleName());
		}

		gsp.runImpl();
	}

	
	public void setAccountName(String pAccountName)
	{
		_accountName = pAccountName;
		_accountData.clear();
		try
		{
			Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement stm = con.prepareStatement(LOAD_ACC_DATA);
			if (getAccountName() != null && getAccountName().length() > 0)
			{
				stm.setString(1, getAccountName());
				ResultSet rs = stm.executeQuery();
				while (rs.next())
				{
					_accountData.set(rs.getString(1), rs.getString(2));
				}
				rs.close();
				stm.close();
			}
			con.close();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}

	}

	public void setActiveChar(L2PcInstance pActiveChar)
	{
		_activeChar = pActiveChar;
		if (_activeChar != null)
			if (_reader != null && _activeChar != null)
			{
				_reader.checkChar(_activeChar);
			}
		L2World.getInstance().storeObject(_activeChar);
	}

	public void setCharSelection(CharSelectInfoPackage[] chars)
	{
		_charSlotMapping = new int[chars.length];

		int i = 0;
		for (CharSelectInfoPackage element : chars)
		{
			_charSlotMapping[i++] = element.getObjectId();
		}
	}

	public void setDetached(boolean b)
	{
		_isDetached = b;
	}

	public void setDisconnected()
	{
		_disconnected = true;
	}

	public void setProtocolOk(boolean b)
	{
		_protocol = b;

	}

	public void setProtocolVer(long ver)
	{
		_protocolVer = ver;
	}

	public void setSessionId(SessionKey sk)
	{
		_sessionId = sk;
	}

	public void setState(GameClientState pState)
	{
		_state = pState;

	}

	public void setWindowCount(int _nCount)
	{
		_nWindowCount = _nCount;
	}

	
	public void storeData()
	{
		try
		{
			if (_accountName == null)
				return;
			Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(STORE_ACC_DATA);
			statement.setString(1, _accountName);
			for (String s : _accountData.getSet().keySet())
			{
				statement.setString(2, s);
				statement.setString(3, _accountData.getString(s));
				statement.execute();
			}
			statement.close();
			con.close();
		}
		catch (SQLException e)
		{
			_log.error("Error while saving account data", e);
		}

	}

	@Override
	public String toString()
	{
		try
		{
			final InetAddress address = getConnection().getInetAddress();
			switch (getState())
			{
				case CONNECTED:
					return "[IP: " + (address == null ? "disconnected" : address.getHostAddress()) + "]";
				case AUTHED:
					return "[Account: " + getAccountName() + " - IP: " + (address == null ? "disconnected" : address.getHostAddress()) + "]";
				case IN_GAME:
					return "[Character: " + (getActiveChar() == null ? "disconnected" : getActiveChar().getName()) + " - Account: " + getAccountName() + " - IP: " + (address == null ? "disconnected" : address.getHostAddress()) + "]";
				default:
					throw new IllegalStateException("Missing state on switch");
			}
		}
		catch (NullPointerException e)
		{
			return "[Character read failed due to disconnect]";
		}
	}

	public ReentrantLock getActiveCharLock()
	{
		return _activeCharLock;
	}

	
	public boolean isAuthedGG()
	{
		return _isAuthedGG;
	}

	public void setGameGuardOk(boolean val)
	{
		_isAuthedGG = val;
	}

	public void stopGuardTask()
	{
		if (_guardCheckTask != null)
		{
			_guardCheckTask.cancel(true);
			_guardCheckTask = null;
		}

	}

	public void close(final int delay)
	{

		close(ServerClose.STATIC_PACKET);
		synchronized (this)
		{
			if (_cleanupTask != null)
			{
				cancelCleanup();
			}
			_cleanupTask = ThreadPoolManager.getInstance().scheduleGeneral(new CleanupTask(), delay); // delayed
		}
		stopGuardTask();
	
	}

	private boolean cancelCleanup()
	{
		final Future<?> task = _cleanupTask;
		if (task != null)
		{
			_cleanupTask = null;
			return task.cancel(true);
		}
		return false;
	}
}