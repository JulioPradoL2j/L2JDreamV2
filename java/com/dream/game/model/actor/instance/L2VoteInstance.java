package com.dream.game.model.actor.instance;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.EnumMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.dream.Config;
import com.dream.L2DatabaseFactory;
import com.dream.game.ai.CtrlIntention;
import com.dream.game.network.serverpackets.ActionFailed;
import com.dream.game.network.serverpackets.MyTargetSelected;
import com.dream.game.network.serverpackets.NpcHtmlMessage;
import com.dream.game.network.serverpackets.SocialAction;
import com.dream.game.network.serverpackets.ValidateLocation;
import com.dream.game.templates.chars.L2NpcTemplate;
import com.dream.tools.random.Rnd;

/**
 * @author Reborn12
 */
public class L2VoteInstance extends L2NpcInstance
{
	private static final Logger _log = Logger.getLogger(L2VoteInstance.class.getName());
	// Queries
	private static final String DELETE_QUERY = "DELETE FROM mods_voting_reward WHERE time < ?";
	private static final String SELECT_QUERY = "SELECT * FROM mods_voting_reward";
	private static final String INSERT_QUERY = "INSERT INTO mods_voting_reward (data, scope, time) VALUES (?, ?, ?)";
	// Constants
	private static final long VOTING_INTERVAL = TimeUnit.HOURS.toMillis(12);
	// api LINKS
	private static final String TOPZONE_API_URL = "http://l2topzone.com/api.php?API_KEY=%s&SERVER_ID=%s&IP=%s";
	private static final String NETWORK_API_URL = "https://l2network.eu/index.php?a=in&u=%s&ipc=%s";
	private static final String HOPZONE_API_URL = "https://api.hopzone.net/lineage2/vote?token=%s&ip_address=%s";
	// Cache
	private static final Map<UserScope, ScopeContainer> VOTTERS_CACHE = new EnumMap<>(UserScope.class);

	public L2VoteInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		load();
	}

	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		StringTokenizer st = new StringTokenizer(command, " ");
		String actualCommand = st.nextToken();
		long time = getLastVotedTime(player);

		if (actualCommand.startsWith("getreward"))
		{
			// Make sure player haven't received reward already!
			if (time > 0)
			{
				sendReEnterMessage(time, player);
				return;
			}
			// Check if player votted
			if (votedAllSites(player.getHost()))
			{
				// Give him reward
				giveReward(player);

				// Mark down this reward as given
				markAsVotted(player);

				// Say thanks ;)
				player.sendMessage("Thanks for voting here's Your reward!");
			}
			else
				player.sendMessage("You Didnt Voted");
		}
		else
		{
			super.onBypassFeedback(player, command);
		}
	}
	@Override
	public void onAction(L2PcInstance player)
	{
		if (this != player.getTarget())
		{
			player.setTarget(this);
			player.sendPacket(new MyTargetSelected(getObjectId(), player.getLevel() - getLevel()));
			player.sendPacket(new ValidateLocation(this));
		}
		else if (isInsideRadius(player, INTERACTION_DISTANCE, false, false))
		{
			broadcastPacket(new SocialAction(this, Rnd.get(8)));
			player.setLastFolkNPC(this);
			showMessageWindow(player);
			player.sendPacket(ActionFailed.STATIC_PACKET);
		}
		else
		{
			player.getAI().setIntention(CtrlIntention.INTERACT, this);
			player.sendPacket(ActionFailed.STATIC_PACKET);
		}
	}

	public void showMessageWindow(L2PcInstance player)
	{
		String filename = "data/html/mods/VoteManager/vote.htm";
		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile(filename);
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%npcname%", getName());
		html.replace("%player%", player.getName());
		player.sendPacket(html);
	}

	private static boolean votedAllSites(String ip)
	{
		return votedHop(ip) && votedNet(ip) && votedTop(ip);
	}

	private static final void load()
	{
		// Initialize the cache
		for (UserScope scope : UserScope.values())
		{
			VOTTERS_CACHE.put(scope, new ScopeContainer());
		}

		// Cleanup old entries and load the data for votters
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(DELETE_QUERY);
			Statement st = con.createStatement())
		{
			ps.setLong(1, System.currentTimeMillis());
			ps.execute();

			// Load the data
			try (ResultSet rset = st.executeQuery(SELECT_QUERY))
			{
				while (rset.next())
				{
					final String data = rset.getString("data");
					final UserScope scope = UserScope.findByName(rset.getString("scope"));
					final Long time = rset.getLong("time");
					if (scope != null)
					{
						VOTTERS_CACHE.get(scope).registerVotter(data, time);
					}
				}
			}
		}
		catch (SQLException e)
		{
			_log.log(Level.WARNING, L2VoteInstance.class.getSimpleName() + ": " + e.getMessage(), e);
		}
	}

	private static void giveReward(L2PcInstance player)
	{
		player.addItem("Vote Reward:", Config.VOTE_API_REWARD_ID, Config.VOTE_API_REWARD_COUNT, player, true);
	}

	private static void sendReEnterMessage(long time, L2PcInstance player)
	{
		if (time > System.currentTimeMillis())
		{
			final long remainingTime = (time - System.currentTimeMillis()) / 1000;
			final int hours = (int) (remainingTime / 3600);
			final int minutes = (int) ((remainingTime % 3600) / 60);
			final int seconds = (int) ((remainingTime % 3600) % 60);

			String msg = "You have received your reward already try again in: " + hours + " hours";
			if (minutes > 0)
			{
				msg += " " + minutes + " minutes";
			}
			if (seconds > 0)
			{
				msg += " " + seconds + " seconds";
			}
			player.sendMessage(msg);
		}
	}

	private static final long getLastVotedTime(L2PcInstance activeChar)
	{
		for (Entry<UserScope, ScopeContainer> entry : VOTTERS_CACHE.entrySet())
		{
			final String data = entry.getKey().getData(activeChar);
			final long reuse = entry.getValue().getReuse(data);
			if (reuse > 0)
			{
				return reuse;
			}
		}
		return 0;
	}

	private static final void markAsVotted(final L2PcInstance player)
	{
		final long reuse = System.currentTimeMillis() + VOTING_INTERVAL;
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(INSERT_QUERY))
		{
			for (UserScope scope : UserScope.values())
			{
				final String data = scope.getData(player);
				final ScopeContainer container = VOTTERS_CACHE.get(scope);
				container.registerVotter(data, reuse);

				ps.setString(1, data);
				ps.setString(2, scope.name());
				ps.setLong(3, reuse);
				ps.addBatch();
			}
			ps.executeBatch();
		}
		catch (SQLException e)
		{
			_log.log(Level.WARNING, L2VoteInstance.class.getSimpleName() + ": " + e.getMessage(), e);
		}
	}

	public static final boolean votedTop(String ip)
	{
		try
		{
			final URL obj = new URL(String.format(TOPZONE_API_URL, Config.VOTE_API_KEYTOPZONE, Config.VOTE_SERVERID_KEYTOPZONE, ip));

			final HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			con.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.106 Safari/537.36");

			final int responseCode = con.getResponseCode();
			if (responseCode == 200) // OK
			{
				try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream())))
				{
					return br.readLine().equals("TRUE");
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return false;
	}

	public static final boolean votedHop(String ip)
	{
		try
		{
			final URL obj = new URL(String.format(HOPZONE_API_URL, Config.VOTE_APIKEY_HOPZONE, ip));
			final HttpURLConnection con = (HttpURLConnection) obj.openConnection();

			con.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.106 Safari/537.36");

			final int responseCode = con.getResponseCode();
			if (responseCode == 200)
			{
				final StringBuilder sb = new StringBuilder();
				try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream())))
				{
					String inputLine;
					while ((inputLine = in.readLine()) != null)
					{
						sb.append(inputLine);
					}
				}
				return sb.toString().contains("true");
			}
		}
		catch (Exception e)
		{
		}
		return false;
	}

	public static final boolean votedNet(String ip)
	{
		try
		{
			final URL obj = new URL(String.format(NETWORK_API_URL, Config.VOTE_SERVERID_NETWORK, ip));
			final HttpURLConnection con = (HttpURLConnection) obj.openConnection();

			con.setRequestProperty("User-Agent", "L2Network");
			con.setConnectTimeout(5000);

			final int responseCode = con.getResponseCode();
			if (responseCode == 200)
			{
				final StringBuilder sb = new StringBuilder();
				try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream())))
				{
					String inputLine;
					while ((inputLine = in.readLine()) != null)
					{
						sb.append(inputLine);
					}
				}
				return sb.toString().equals("1");
			}
		}
		catch (Exception e)
		{
		}
		return false;
	}

	private enum UserScope
	{
		ACCOUNT
		{
			@Override
			public String getData(L2PcInstance player)
			{
				return player.getAccountName();
			}
		},
		IP
		{
			@Override
			public String getData(L2PcInstance player)
			{
				return player.getHost();
			}
		};
		// HWID
		// {
		// @Override
		// public String getData(L2PcInstance player)
		// {
		// return player.getClient().getHWid().getMain();
		// }
		// };

		public abstract String getData(L2PcInstance player);

		public static UserScope findByName(String name)
		{
			for (UserScope scope : values())
			{
				if (scope.name().equals(name))
				{
					return scope;
				}
			}
			return null;
		}
	}

	private static class ScopeContainer
	{
		private final Map<String, Long> _votters = new ConcurrentHashMap<>();

		public ScopeContainer()
		{
		}

		public void registerVotter(String data, long reuse)
		{
			_votters.put(data, reuse);
		}

		public long getReuse(String data)
		{
			if (_votters.containsKey(data))
			{
				long time = _votters.get(data);
				if (time > System.currentTimeMillis())
				{
					return time;
				}
			}
			return 0;
		}
	}
}
