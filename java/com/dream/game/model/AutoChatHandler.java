package com.dream.game.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.dream.Config;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.L2Npc;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.entity.sevensigns.SevenSigns;
import com.dream.game.network.SystemChatChannelId;
import com.dream.game.network.ThreadPoolManager;
import com.dream.game.network.serverpackets.CreatureSay;
import com.dream.tools.random.Rnd;

public class AutoChatHandler implements SpawnListener
{
	public class AutoChatInstance
	{
		private class AutoChatDefinition
		{
			protected int _chatIndex = 0;
			protected L2Npc _npcInstance;

			protected AutoChatInstance _chatInstance;

			private long _chatDelay = 0;
			private String[] _chatTexts = null;
			private boolean _isActiveDefinition;
			private boolean _randomChat;

			protected AutoChatDefinition(AutoChatInstance chatInst, L2Npc npcInst, String[] chatTexts, long chatDelay)
			{
				_npcInstance = npcInst;

				_chatInstance = chatInst;
				_randomChat = chatInst.isDefaultRandom();

				_chatDelay = chatDelay;
				_chatTexts = chatTexts;

				if (!chatInst.isGlobal())
				{
					setActive(true);
				}
			}

			private long getChatDelay()
			{
				if (_chatDelay > 0)
					return _chatDelay;
				return _chatInstance.getDefaultDelay();
			}

			protected String[] getChatTexts()
			{
				if (_chatTexts != null)
					return _chatTexts;
				return _chatInstance.getDefaultTexts();
			}

			private boolean isActive()
			{
				return _isActiveDefinition;
			}

			boolean isRandomChat()
			{
				return _randomChat;
			}

			void setActive(boolean activeValue)
			{
				if (isActive() == activeValue)
					return;

				if (activeValue)
				{
					AutoChatRunner acr = new AutoChatRunner(_npcId, _npcInstance.getObjectId());
					_chatTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(acr, getChatDelay(), getChatDelay());
				}
				else
				{
					_chatTask.cancel(false);
				}

				_isActiveDefinition = activeValue;
			}

			void setChatDelay(long delayValue)
			{
				_chatDelay = delayValue;
			}

			void setChatTexts(String[] textsValue)
			{
				_chatTexts = textsValue;
			}

			void setRandomChat(boolean randValue)
			{
				_randomChat = randValue;
			}
		}

		private class AutoChatRunner implements Runnable
		{
			private final int _runnerNpcId;
			private final int _objectId;

			protected AutoChatRunner(int pNpcId, int pObjectId)
			{
				_runnerNpcId = pNpcId;
				_objectId = pObjectId;
			}

			@Override
			public synchronized void run()
			{
				AutoChatInstance chatInst = _registeredChats.get(_runnerNpcId);
				AutoChatDefinition[] chatDefinitions;

				if (chatInst.isGlobal())
				{
					chatDefinitions = chatInst.getChatDefinitions();
				}
				else
				{
					AutoChatDefinition chatDef = chatInst.getChatDefinition(_objectId);

					if (chatDef == null)
					{
						_log.warn("AutoChatHandler: Auto chat definition is NULL for NPC ID " + _npcId + ".");
						return;
					}

					chatDefinitions = new AutoChatDefinition[]
					{
						chatDef
					};
				}

				for (AutoChatDefinition chatDef : chatDefinitions)
				{
					try
					{
						L2Npc chatNpc = chatDef._npcInstance;
						List<L2PcInstance> nearbyPlayers = new ArrayList<>();

						for (L2Character player : chatNpc.getKnownList().getKnownCharactersInRadius(DEFAULT_CHAT_RANGE))
							if (player instanceof L2PcInstance && !((L2PcInstance) player).isGM())
							{
								nearbyPlayers.add((L2PcInstance) player);
							}

						int maxIndex = chatDef.getChatTexts().length;
						int lastIndex = Rnd.nextInt(maxIndex);

						String creatureName = chatNpc.getName();
						String text;

						if (!chatDef.isRandomChat())
						{
							lastIndex = chatDef._chatIndex;
							if (lastIndex == maxIndex)
							{
								lastIndex = 0;
							}
							chatDef._chatIndex = lastIndex + 1;
						}

						text = chatDef.getChatTexts()[lastIndex];

						if (text == null)
							return;

						if (!nearbyPlayers.isEmpty())
						{
							final int winningCabal = SevenSigns.getInstance().getCabalHighestScore();
							int losingCabal = SevenSigns.CABAL_NULL;

							if (winningCabal == SevenSigns.CABAL_DAWN)
							{
								losingCabal = SevenSigns.CABAL_DUSK;
							}
							else if (winningCabal == SevenSigns.CABAL_DUSK)
							{
								losingCabal = SevenSigns.CABAL_DAWN;
							}

							if (text.indexOf("%player_") > -1)
							{
								List<Integer> karmaPlayers = new ArrayList<>();
								List<Integer> winningCabals = new ArrayList<>();
								List<Integer> losingCabals = new ArrayList<>();

								for (int i = 0; i < nearbyPlayers.size(); i++)
								{
									L2PcInstance nearbyPlayer = nearbyPlayers.get(i);

									// Get all nearby players with karma
									if (nearbyPlayer.getKarma() > 0)
									{
										karmaPlayers.add(i);
									}

									// Get all nearby Seven Signs winners and
									// loosers
									if (SevenSigns.getInstance().getPlayerCabal(nearbyPlayer) == winningCabal)
									{
										winningCabals.add(i);
									}
									else if (SevenSigns.getInstance().getPlayerCabal(nearbyPlayer) == losingCabal)
									{
										losingCabals.add(i);
									}
								}

								if (text.indexOf("%player_random%") > -1)
								{
									int randomPlayerIndex = Rnd.nextInt(nearbyPlayers.size());
									L2PcInstance randomPlayer = nearbyPlayers.get(randomPlayerIndex);
									text = text.replaceAll("%player_random%", randomPlayer.getName());
								}
								else if (text.indexOf("%player_killer%") > -1 && !karmaPlayers.isEmpty())
								{
									int randomPlayerIndex = karmaPlayers.get(Rnd.nextInt(karmaPlayers.size()));
									L2PcInstance randomPlayer = nearbyPlayers.get(randomPlayerIndex);
									text = text.replaceAll("%player_killer%", randomPlayer.getName());
								}
								else if (text.indexOf("%player_cabal_winner%") > -1 && !winningCabals.isEmpty())
								{
									int randomPlayerIndex = winningCabals.get(Rnd.nextInt(winningCabals.size()));
									L2PcInstance randomPlayer = nearbyPlayers.get(randomPlayerIndex);
									text = text.replaceAll("%player_cabal_winner%", randomPlayer.getName());
								}
								else if (text.indexOf("%player_cabal_loser%") > -1 && !losingCabals.isEmpty())
								{
									int randomPlayerIndex = losingCabals.get(Rnd.nextInt(losingCabals.size()));
									L2PcInstance randomPlayer = nearbyPlayers.get(randomPlayerIndex);
									text = text.replaceAll("%player_cabal_loser%", randomPlayer.getName());
								}
							}
						}

						if (text == null)
							return;

						if (text.contains("%player_"))
							return;

						chatNpc.broadcastPacket(new CreatureSay(chatNpc.getObjectId(), SystemChatChannelId.Chat_Normal, creatureName, text));
					}
					catch (Exception e)
					{
						_log.error(e.getMessage(), e);
						return;
					}
				}
			}
		}

		protected int _npcId;
		private long _defaultDelay = DEFAULT_CHAT_DELAY;

		private String[] _defaultTexts;
		private boolean _defaultRandom = false;

		private boolean _globalChat = false;
		private boolean _isActive;

		private final Map<Integer, AutoChatDefinition> _chatDefinitions = new HashMap<>();

		protected ScheduledFuture<?> _chatTask;

		protected AutoChatInstance(int npcId, String[] chatTexts, long chatDelay, boolean isGlobal)
		{
			_defaultTexts = chatTexts;
			_npcId = npcId;
			_defaultDelay = chatDelay;
			_globalChat = isGlobal;

			setActive(true);
		}

		public int addChatDefinition(L2Npc npcInst)
		{
			return addChatDefinition(npcInst, null, 0);
		}

		public int addChatDefinition(L2Npc npcInst, String[] chatTexts, long chatDelay)
		{
			int objectId = npcInst.getObjectId();
			AutoChatDefinition chatDef = new AutoChatDefinition(this, npcInst, chatTexts, chatDelay);

			_chatDefinitions.put(objectId, chatDef);
			return objectId;
		}

		protected AutoChatDefinition getChatDefinition(int objectId)
		{
			return _chatDefinitions.get(objectId);
		}

		protected AutoChatDefinition[] getChatDefinitions()
		{
			return _chatDefinitions.values().toArray(new AutoChatDefinition[_chatDefinitions.values().size()]);
		}

		public long getDefaultDelay()
		{
			return _defaultDelay;
		}

		public String[] getDefaultTexts()
		{
			return _defaultTexts;
		}

		public int getDefinitionCount()
		{
			return _chatDefinitions.size();
		}

		public int getNPCId()
		{
			return _npcId;
		}

		public L2Npc[] getNPCInstanceList()
		{
			List<L2Npc> npcInsts = new ArrayList<>();

			for (AutoChatDefinition chatDefinition : _chatDefinitions.values())
			{
				npcInsts.add(chatDefinition._npcInstance);
			}
			return npcInsts.toArray(new L2Npc[npcInsts.size()]);
		}

		public boolean isActive()
		{
			return _isActive;
		}

		public boolean isDefaultRandom()
		{
			return _defaultRandom;
		}

		public boolean isGlobal()
		{
			return _globalChat;
		}

		public boolean isRandomChat(int objectId)
		{
			if (!_chatDefinitions.containsKey(objectId))
				return false;
			return _chatDefinitions.get(objectId).isRandomChat();
		}

		public boolean removeChatDefinition(int objectId)
		{
			if (!_chatDefinitions.containsKey(objectId))
				return false;

			AutoChatDefinition chatDefinition = _chatDefinitions.get(objectId);
			chatDefinition.setActive(false);
			_chatDefinitions.remove(objectId);
			return true;
		}

		public void setActive(boolean activeValue)
		{
			if (_isActive == activeValue)
				return;

			_isActive = activeValue;

			if (!isGlobal())
			{
				for (AutoChatDefinition chatDefinition : _chatDefinitions.values())
				{
					chatDefinition.setActive(activeValue);
				}
				return;
			}

			if (isActive())
			{
				AutoChatRunner acr = new AutoChatRunner(_npcId, -1);
				_chatTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(acr, _defaultDelay, _defaultDelay);
			}
			else
			{
				_chatTask.cancel(false);
			}
		}

		public void setChatDelay(int objectId, long delayValue)
		{
			AutoChatDefinition chatDef = getChatDefinition(objectId);

			if (chatDef != null)
			{
				chatDef.setChatDelay(delayValue);
			}
		}

		public void setChatTexts(int objectId, String[] textsValue)
		{
			AutoChatDefinition chatDef = getChatDefinition(objectId);

			if (chatDef != null)
			{
				chatDef.setChatTexts(textsValue);
			}
		}

		public void setDefaultChatDelay(long delayValue)
		{
			_defaultDelay = delayValue;
		}

		public void setDefaultChatTexts(String[] textsValue)
		{
			_defaultTexts = textsValue;
		}

		public void setDefaultRandom(boolean randValue)
		{
			_defaultRandom = randValue;
		}

		public void setRandomChat(int objectId, boolean randValue)
		{
			AutoChatDefinition chatDef = getChatDefinition(objectId);

			if (chatDef != null)
			{
				chatDef.setRandomChat(randValue);
			}
		}
	}

	protected static Logger _log = Logger.getLogger(AutoChatHandler.class.getName());

	private static AutoChatHandler _instance;
	private static final int DEFAULT_CHAT_RANGE = 1500;
	private static final int DEFAULT_CHAT_DELAY = 30000;

	public static AutoChatHandler getInstance()
	{
		if (_instance == null)
		{
			_instance = new AutoChatHandler();
		}
		return _instance;
	}

	protected final Map<Integer, AutoChatInstance> _registeredChats;

	private AutoChatHandler()
	{
		_registeredChats = new HashMap<>();
		restoreChatData();
		L2Spawn.addSpawnListener(this);
		_log.info("Auto Chat: Loaded " + size() + " handlers in total.");
	}

	public AutoChatInstance getAutoChatInstance(int id, boolean byObjectId)
	{
		if (!byObjectId)
			return _registeredChats.get(id);

		for (AutoChatInstance chatInst : _registeredChats.values())
			if (chatInst.getChatDefinition(id) != null)
				return chatInst;
		return null;
	}

	@Override
	public void npcSpawned(L2Npc npc)
	{
		synchronized (_registeredChats)
		{
			if (npc == null)
				return;

			int npcId = npc.getNpcId();

			if (_registeredChats.containsKey(npcId))
			{
				AutoChatInstance chatInst = _registeredChats.get(npcId);

				if (chatInst != null && chatInst.isGlobal())
				{
					chatInst.addChatDefinition(npc);
				}
			}
		}
	}

	private final AutoChatInstance registerChat(int npcId, L2Npc npcInst, String[] chatTexts, int chatDelay)
	{
		AutoChatInstance chatInst = null;

		if (chatDelay < 0)
		{
			chatDelay = DEFAULT_CHAT_DELAY;
		}

		if (_registeredChats.containsKey(npcId))
		{
			chatInst = _registeredChats.get(npcId);
		}
		else
		{
			chatInst = new AutoChatInstance(npcId, chatTexts, chatDelay, npcInst == null);
		}

		if (npcInst != null)
		{
			chatInst.addChatDefinition(npcInst);
		}

		_registeredChats.put(npcId, chatInst);

		return chatInst;
	}

	public AutoChatInstance registerChat(L2Npc npcInst, String[] chatTexts, int chatDelay)
	{
		return registerChat(npcInst.getNpcId(), npcInst, chatTexts, chatDelay);
	}

	public AutoChatInstance registerGlobalChat(int npcId, String[] chatTexts, int chatDelay)
	{
		return registerChat(npcId, null, chatTexts, chatDelay);
	}

	public void reload()
	{
		// unregister all registered spawns
		for (AutoChatInstance aci : _registeredChats.values())
			if (aci != null)
			{
				// clear timer
				if (aci._chatTask != null)
				{
					aci._chatTask.cancel(true);
				}
				removeChat(aci);
			}
		_registeredChats.clear();
		restoreChatData();
	}

	public boolean removeChat(AutoChatInstance chatInst)
	{
		if (chatInst == null)
			return false;

		_registeredChats.remove(chatInst.getNPCId());
		chatInst.setActive(false);
		return true;
	}

	public boolean removeChat(int npcId)
	{
		AutoChatInstance chatInst = _registeredChats.get(npcId);

		return removeChat(chatInst);
	}

	private void restoreChatData()
	{
		int numLoaded = 0;
		int numLoaded1 = 0;
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(false);
		factory.setIgnoringComments(true);
		File f = new File(Config.DATAPACK_ROOT + "/data/xml/world/auto_chat.xml");
		if (!f.exists())
		{
			_log.warn("autochat.xml could not be loaded: file not found");
			return;
		}
		try
		{
			InputSource in = new InputSource(new InputStreamReader(new FileInputStream(f), "UTF-8"));
			in.setEncoding("UTF-8");
			Document doc = factory.newDocumentBuilder().parse(in);

			String[] messages =
			{
				"",
				"",
				"",
				"",
				"",
				"",
				"",
				""
			};
			for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
				if (n.getNodeName().equalsIgnoreCase("list"))
				{
					for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
						if (d.getNodeName().equalsIgnoreCase("autochat"))
						{
							int npcId = Integer.valueOf(d.getAttributes().getNamedItem("npcId").getNodeValue());
							int chatDelay = Integer.valueOf(d.getAttributes().getNamedItem("chatDelay").getNodeValue());
							numLoaded++;
							for (Node t = d.getFirstChild(); t != null; t = t.getNextSibling())
							{
								int i = 0;
								if (t.getNodeName().equalsIgnoreCase("chatText"))
								{
									if (t.getNodeValue() != null)
									{
										messages[i] = t.getNodeValue();
									}
									i++;
									numLoaded1++;
								}
							}
							registerGlobalChat(npcId, messages, chatDelay);
						}
				}
		}
		catch (SAXException e)
		{
			_log.error("Error while creating table", e);
		}
		catch (IOException e)
		{
			_log.error("Error while creating table", e);
		}
		catch (ParserConfigurationException e)
		{
			_log.error("Error while creating table", e);
		}

		_log.info("AutoChatHandler: Loaded " + numLoaded + " chat groups.");
		_log.info("AutoChatHandler: Loaded " + numLoaded1 + " chat texts.");
	}

	public void setAutoChatActive(boolean isActive)
	{
		for (AutoChatInstance chatInst : _registeredChats.values())
		{
			chatInst.setActive(isActive);
		}
	}

	public void setAutoChatActive(int npcId, boolean isActive)
	{
		for (AutoChatInstance chatInst : _registeredChats.values())
			if (chatInst.getNPCId() == npcId)
			{
				chatInst.setActive(isActive);
			}
	}

	public int size()
	{
		return _registeredChats.size();
	}
}