package com.dream.game.handler.admin;

import com.dream.Config;
import com.dream.game.access.gmHandler;
import com.dream.game.model.L2Object;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.instance.L2ControllableMobInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.actor.instance.L2PetInstance;
import com.dream.game.model.world.L2World;
import com.dream.game.network.Disconnection;
import com.dream.game.network.SystemChatChannelId;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.SetSummonRemainTime;
import com.dream.game.network.serverpackets.SocialAction;
import com.dream.game.network.serverpackets.StatusUpdate;
import com.dream.game.util.PcAction;

public class AdminControl extends gmHandler
{
	private static final String[] commands =
	{
		"control",
		"kick",
		"gmcancel",
		"kill",
		"invul",
		"setinvul",
		"heal",
		"start_regen",
		"stop_regen",
		"nokarma",
		"setkarma",
		"fullfood",
		"sethero",
		"setnoble",
		"remclanwait",
		"setcp",
		"sethp",
		"setmp"
	};

	private static void handleHeal(L2Character target)
	{
		target.getStatus().setCurrentHpMp(target.getMaxHp(), target.getMaxMp());
		if (target instanceof L2PcInstance)
		{
			target.getStatus().setCurrentCp(target.getMaxCp());
		}
	}

	private static void handleInvul(L2PcInstance activeChar)
	{
		if (activeChar == null)
			return;

		String text;
		if (activeChar.isInvul())
		{
			activeChar.setIsInvul(false);
			if (activeChar.getPet() != null)
			{
				activeChar.getPet().setIsInvul(false);
			}

			text = activeChar.getName() + " is vulnerable";
		}
		else
		{
			activeChar.setIsInvul(true);
			if (activeChar.getPet() != null)
			{
				activeChar.getPet().setIsInvul(true);
			}

			text = activeChar.getName() + " becomes invulnerable";
		}
		activeChar.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", text);
	}

	private static void kill(L2PcInstance activeChar, L2Character target)
	{
		if (target instanceof L2PcInstance)
		{
			if (!((L2PcInstance) target).isGM())
			{
				target.stopAllEffects();
			}
			target.reduceCurrentHp(target.getMaxHp() + target.getMaxCp() + 1, activeChar);
		}
		else
		{
			if (target.isInvul())
			{
				target.setIsInvul(false);
			}
			if (target.isChampion())
			{
				target.reduceCurrentHp(target.getMaxHp() * Config.CHAMPION_HP + 1, activeChar);
			}
			else
			{
				target.reduceCurrentHp(target.getMaxHp() + 1, activeChar);
			}
		}
	}

	private static void setTargetKarma(L2PcInstance activeChar, int karma)
	{
		L2Object target = activeChar.getTarget();
		L2PcInstance player = null;
		if (target != null && target instanceof L2PcInstance)
		{
			player = (L2PcInstance) target;
		}
		else
			return;

		if (karma >= 0)
		{
			StatusUpdate su = new StatusUpdate(player);
			su.addAttribute(StatusUpdate.KARMA, karma);
			player.setKarma(karma);
			player.sendPacket(su);
			if (player != activeChar)
			{
				player.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "GM changed the number of your karma. The karma amounted " + karma);
			}
			activeChar.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "The value of a player's karma " + player.getName() + " successfully changed");
		}
		else
		{
			activeChar.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "The value of karma should be above 0");
		}
	}

	@Override
	public String[] getCommandList()
	{
		return commands;
	}

	@Override
	public void runCommand(L2PcInstance admin, String... params)
	{
		if (admin == null)
			return;

		final String command = params[0];

		if (command.equals("control"))
		{
			AdminMethods.showMenuPage(admin, "control.htm");
			return;
		}
		else if (command.equals("remclanwait"))
		{
			L2Object target = admin.getTarget();
			L2PcInstance player = null;

			if (target != null && target instanceof L2PcInstance)
			{
				player = (L2PcInstance) target;
			}
			else
				return;

			if (player.getClan() == null)
			{
				player.setClanJoinExpiryTime(0);
				player.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "GM removed the time penalty for joining the clan");
				admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "The penalty was successfully deleted");
			}
			else
			{
				admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Player " + player.getName() + " is a member of a clan");
			}
		}
		else if (command.equals("setnoble"))
		{
			L2Object target = admin.getTarget();

			if (target != null && target instanceof L2PcInstance)
			{
				L2PcInstance player = (L2PcInstance) target;

				if (player.isNoble())
				{
					admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "You have lost the status of noblesse.");
				}
				else
				{
					player.broadcastPacket(new SocialAction(player, 16));
					admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "You are now noblesse.");
				}
				player.setNoble(!player.isNoble());
				player.broadcastUserInfo();
			}
		}
		else if (command.equals("sethero"))
		{
			L2Object target = admin.getTarget();

			if (target != null && target instanceof L2PcInstance)
			{
				L2PcInstance player = (L2PcInstance) target;
				if (player.isHero())
				{
					admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "You have lost a hero status.");
				}
				else
				{
					player.broadcastPacket(new SocialAction(player, 16));
					admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Congratulations, you are a hero");
				}
				PcAction.admGiveHero(player, player.isHero());
				player.setHero(!player.isHero());
				player.broadcastUserInfo();
			}
		}
		else if (command.equals("fullfood"))
		{
			L2Object target = admin.getTarget();
			if (target instanceof L2PetInstance)
			{
				L2PetInstance targetPet = (L2PetInstance) target;
				targetPet.setCurrentFed(targetPet.getMaxFed());
				targetPet.getOwner().sendPacket(new SetSummonRemainTime(targetPet.getMaxFed(), targetPet.getCurrentFed()));
				admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "You have successfully feed pet.");
			}
			else
			{
				admin.sendPacket(SystemMessageId.INCORRECT_TARGET);
			}
		}
		else if (command.equals("nokarma"))
		{
			setTargetKarma(admin, 0);
			return;
		}
		else if (command.equals("setkarma"))
		{
			try
			{
				int karma = Integer.parseInt(params[1]);
				setTargetKarma(admin, karma);
			}
			catch (Exception e)
			{
			}
			return;
		}
		else if (command.equals("start_regen"))
		{
			L2Object obj = admin.getTarget();
			if (obj == null)
			{
				obj = admin;
			}

			if (obj instanceof L2PcInstance)
			{
				((L2PcInstance) obj).getStatus().startHpMpRegeneration();
			}
			admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Started HP restore.");
		}
		else if (command.equals("stop_regen"))
		{
			L2Object obj = admin.getTarget();
			if (obj == null)
			{
				obj = admin;
			}

			if (obj instanceof L2PcInstance)
			{
				((L2PcInstance) obj).getStatus().stopHpMpRegeneration();
			}
			admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Stoped Hp restore.");
		}
		else if (command.equals("invul"))
		{
			handleInvul(admin);
			return;
		}
		else if (command.equals("heal"))
		{
			if (params.length == 1)
			{
				if (admin.getTarget() != null && admin.getTarget() instanceof L2Character)
				{
					handleHeal((L2Character) admin.getTarget());
					admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "You healed " + admin.getTarget().getName());
				}
				return;
			}
			try
			{
				try
				{
					int radius = Integer.parseInt(params[1]);
					for (L2Character cha : admin.getKnownList().getKnownCharactersInRadius(radius))
					{
						if (cha == null || cha.isAlikeDead())
						{
							continue;
						}
						handleHeal(cha);
					}
					admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Healed all within a radius of " + radius);
				}
				catch (NumberFormatException e)
				{
					L2PcInstance target = L2World.getInstance().getPlayer(params[1]);
					if (target != null)
					{
						handleHeal(target);
						admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "You healed " + target.getName());
					}
				}
			}
			catch (StringIndexOutOfBoundsException e)
			{
				admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Enter a nickname or RADIUS");
			}
			return;
		}
		else if (command.equals("setinvul"))
		{
			L2Object target = admin.getTarget();
			if (target instanceof L2PcInstance)
			{
				handleInvul((L2PcInstance) target);
			}
			return;
		}
		else if (command.equals("kill"))
		{
			if (params.length > 1)
			{
				L2PcInstance player = L2World.getInstance().getPlayer(params[1]);
				if (player != null)
				{
					if (params.length > 2)
					{
						try
						{
							int radius = Integer.parseInt(params[2]);
							for (L2Character knownChar : player.getKnownList().getKnownCharactersInRadius(radius))
							{
								if (knownChar == null || knownChar instanceof L2ControllableMobInstance || knownChar == admin)
								{
									continue;
								}
								kill(admin, knownChar);
							}
							admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Killed all within a radius of " + radius + " Player " + player.getName());
							return;
						}
						catch (NumberFormatException e)
						{
							admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Raidus is incorrect");
							return;
						}
					}
					kill(admin, player);
				}
				else
				{
					try
					{
						int radius = Integer.parseInt(params[1]);

						for (L2Character knownChar : admin.getKnownList().getKnownCharactersInRadius(radius))
						{
							if (knownChar == null || knownChar instanceof L2ControllableMobInstance || knownChar == admin)
							{
								continue;
							}
							kill(admin, knownChar);
						}

						admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Killed all within a radius of " + radius);
						return;
					}
					catch (NumberFormatException e)
					{
						admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Raidus is incorrect");
						return;
					}
				}
			}
			else
			{
				L2Object obj = admin.getTarget();
				if (obj == null || obj instanceof L2ControllableMobInstance || !(obj instanceof L2Character))
				{
					admin.sendPacket(SystemMessageId.INCORRECT_TARGET);
				}
				else
				{
					kill(admin, (L2Character) obj);
					admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "You killed " + obj.getName());
				}
			}
			return;
		}
		else if (command.equals("kick"))
		{
			L2PcInstance player = null;
			if (params.length > 1)
			{
				player = L2World.getInstance().getPlayer(params[1]);
			}

			if (player == null)
			{
				L2Object obj = admin.getTarget();
				if (obj != null && obj instanceof L2PcInstance)
				{
					player = (L2PcInstance) obj;
				}
			}

			if (player != null)
			{
				String kickName = player.getName();

				if (player.isOfflineTrade())
				{
					player.setOfflineTrade(false);
					player.standUp();
				}

				admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "You have ben kicked from the game by administrator.");
				new Disconnection(player).defaultSequence(false);
				admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Player " + kickName + " removed from the game");
			}
			else
			{
				admin.sendPacket(SystemMessageId.INCORRECT_TARGET);
			}
			return;
		}
		else if (command.equals("gmcancel"))
		{
			try
			{
				L2Object target = admin.getTarget();
				if (target != null && target instanceof L2Character)
				{
					((L2Character) target).stopAllEffects();
					admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Canceling effects " + target.getName() + " completed");
					return;
				}
				else if (params.length > 1)
				{
					int radius = 0;
					radius = Integer.parseInt(params[1]);
					if (radius > 0)
					{
						for (L2PcInstance temp : admin.getKnownList().getKnownPlayersInRadius(radius))
							if (temp != null)
							{
								temp.stopAllEffects();
							}
						admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "All players within a radius of " + radius + " effects canceled.");
						return;
					}
				}
			}
			catch (Exception e)
			{
				admin.sendPacket(SystemMessageId.INCORRECT_TARGET);
				return;
			}
			admin.sendPacket(SystemMessageId.INCORRECT_TARGET);
			return;
		}
		else if (command.startsWith("set"))
		{
			L2Object obj = admin.getTarget();
			if (obj == null)
			{
				obj = admin;
			}
			if (!(obj instanceof L2Character))
			{
				admin.sendPacket(SystemMessageId.INCORRECT_TARGET);
				return;
			}
			try
			{
				int val = Integer.parseInt(params[1]);
				if (command.equals("setcp"))
				{
					if (obj instanceof L2PcInstance)
					{
						((L2PcInstance) obj).getStatus().setCurrentCp(val);
						admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "The CP level changed");
					}
					else
					{
						admin.sendPacket(SystemMessageId.INCORRECT_TARGET);
					}
				}
				else if (command.equals("sethp"))
				{
					((L2Character) obj).getStatus().setCurrentHp(val);
					admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "The HP level changed");
				}
				else if (command.equals("setmp"))
				{
					((L2Character) obj).getStatus().setCurrentMp(val);
					admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "The MP level changed");
				}
			}
			catch (Exception e)
			{
				admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Set the argument");
			}
			return;
		}
	}
}