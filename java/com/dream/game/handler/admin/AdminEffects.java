package com.dream.game.handler.admin;

import com.dream.game.access.gmHandler;
import com.dream.game.ai.CtrlIntention;
import com.dream.game.datatables.xml.SkillTable;
import com.dream.game.model.L2Object;
import com.dream.game.model.L2Skill;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.L2Npc;
import com.dream.game.model.actor.instance.L2ChestInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.world.L2World;
import com.dream.game.network.SystemChatChannelId;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.Earthquake;
import com.dream.game.network.serverpackets.ExRedSky;
import com.dream.game.network.serverpackets.L2GameServerPacket;
import com.dream.game.network.serverpackets.NpcHtmlMessage;
import com.dream.game.network.serverpackets.PlaySound;
import com.dream.game.network.serverpackets.SSQInfo;
import com.dream.game.network.serverpackets.SocialAction;
import com.dream.game.network.serverpackets.StopMove;
import com.dream.game.network.serverpackets.SunRise;
import com.dream.game.network.serverpackets.SunSet;
import com.dream.game.network.serverpackets.SystemMessage;
import com.dream.game.skills.AbnormalEffect;

public class AdminEffects extends gmHandler
{
	private static final String[] commands =
	{
		"vis",
		"invis",
		"visible",
		"invisible",
		"earthquake",
		"atmosphere",
		"sounds",
		"play_sounds",
		"play_sound",
		"para",
		"unpara",
		"unpara_all",
		"para_all",
		"bighead",
		"shrinkhead",
		"gmspeed",
		"social",
		"abnormal",
		"abnormal_menu",
		"social_menu"
	};

	private static void adminAtmosphere(String type, String state, L2PcInstance admin)
	{
		L2GameServerPacket packet = null;

		if (type.equals("signsky"))
		{
			if (state.equals("dawn"))
			{
				packet = new SSQInfo(2);
			}
			else if (state.equals("dusk"))
			{
				packet = new SSQInfo(1);
			}
		}
		else if (type.equals("sky"))
		{
			if (state.equals("night"))
			{
				packet = SunSet.STATIC_PACKET;
			}
			else if (state.equals("day"))
			{
				packet = SunRise.STATIC_PACKET;
			}
			else if (state.equals("red"))
			{
				packet = new ExRedSky(10);
			}
		}
		else
		{
			admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Usage: //atmosphere <signsky dawn|dusk>|<sky day|night|red>");
		}
		if (packet != null)
		{
			for (L2PcInstance player : L2World.getInstance().getAllPlayers())
			{
				player.sendPacket(packet);
			}
		}
	}

	private static boolean performAbnormal(int action, L2Object target)
	{
		if (target instanceof L2Character)
		{
			L2Character character = (L2Character) target;
			if ((character.getAbnormalEffect() & action) == action)
			{
				character.stopAbnormalEffect(action);
			}
			else
			{
				character.startAbnormalEffect(action);
			}
			return true;
		}
		return false;
	}

	private static void performSocial(int action, L2Object target, L2PcInstance admin)
	{
		if (target == null)
			return;

		try
		{
			if (target instanceof L2Character)
			{
				if (target instanceof L2ChestInstance)
				{
					admin.sendPacket(SystemMessageId.NOTHING_HAPPENED);
					return;
				}
				if (target instanceof L2Npc && (action < 1 || action > 6))
				{
					admin.sendPacket(SystemMessageId.NOTHING_HAPPENED);
					return;
				}
				if (target instanceof L2PcInstance && (action < 2 || action > 16))
				{
					admin.sendPacket(SystemMessageId.NOTHING_HAPPENED);
					return;
				}
				L2Character character = (L2Character) target;
				character.broadcastPacket(new SocialAction(character, action));
			}
			else
				return;
		}
		catch (Exception e)
		{
		}
		return;
	}

	private static void playAdminSound(L2PcInstance admin, String sound)
	{
		admin.sendPacket(new PlaySound(sound));
		admin.broadcastPacket(new PlaySound(sound));
		admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Playing " + sound + ".");
	}

	private static void sendHtml(L2PcInstance admin, String patch)
	{
		String name = patch + ".htm";
		NpcHtmlMessage html = new NpcHtmlMessage(admin.getObjectId());
		html.setFile("data/html/admin/menus/" + name);
		admin.sendPacket(html);
	}

	private L2Object target;

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

		if (command.equals("vis") || command.equals("visible"))
		{
			admin.getAppearance().setVisible();
			admin.broadcastUserInfo();
			return;
		}
		else if (command.equals("invis") || command.equals("invisible"))
		{
			admin.getAI().setIntention(CtrlIntention.IDLE);
			admin.setTarget(admin);
			admin.disableAllSkills();
			admin.getAppearance().setInvisible();
			admin.updateInvisibilityStatus();
			admin.enableAllSkills();
			return;
		}
		else if (command.equals("earthquake"))
		{
			try
			{
				int intensity = Integer.parseInt(params[1]);
				int duration = Integer.parseInt(params[2]);
				admin.broadcastPacket(new Earthquake(admin.getX(), admin.getY(), admin.getZ(), intensity, duration));
			}
			catch (Exception e)
			{
				admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Usage: //earthquake <intensity> <duration>");
			}
			sendHtml(admin, "effects");
			return;
		}
		else if (command.equals("atmosphere"))
		{
			try
			{
				String type = params[1];
				String state = params[2];
				adminAtmosphere(type, state, admin);
			}
			catch (Exception ex)
			{
			}
			sendHtml(admin, "effects");
			return;
		}
		else if (command.equals("sounds"))
		{
			AdminMethods.showHelpPage(admin, "songs/songs.htm");
			return;
		}
		else if (command.equals("play_sounds"))
		{
			try
			{
				String cmd = "";
				for (int x = 1; x < params.length; x++)
				{
					cmd += " " + params[x];
				}
				AdminMethods.showHelpPage(admin, "songs/songs" + cmd + ".htm");
			}
			catch (StringIndexOutOfBoundsException e)
			{
			}
			return;
		}
		else if (command.startsWith("play_sound"))
		{
			try
			{
				String cmd = "";
				for (int x = 1; x < params.length; x++)
				{
					cmd += " " + params[x];
				}
				playAdminSound(admin, cmd);
			}
			catch (StringIndexOutOfBoundsException e)
			{
			}
			return;
		}
		else if (command.equals("para"))
		{
			String type = "1";
			if (params.length > 1)
			{
				type = params[1];
			}
			try
			{
				L2Object target = admin.getTarget();
				L2Character player = null;
				if (target instanceof L2Character)
				{
					player = (L2Character) target;
					if (type.equals("1"))
					{
						player.startAbnormalEffect(AbnormalEffect.HOLD_1);
					}
					else
					{
						player.startAbnormalEffect(AbnormalEffect.HOLD_2);
					}
					player.setIsParalyzed(true);
					player.sendPacket(new StopMove(player));
					player.broadcastPacket(new StopMove(player));
				}
			}
			catch (Exception e)
			{
			}
		}
		else if (command.equals("unpara"))
		{
			try
			{
				L2Object target = admin.getTarget();
				L2Character player = null;
				if (target instanceof L2Character)
				{
					player = (L2Character) target;
					player.stopAbnormalEffect(AbnormalEffect.HOLD_1);
					player.stopAbnormalEffect(AbnormalEffect.HOLD_2);
					player.setIsParalyzed(false);
				}
			}
			catch (Exception e)
			{
			}
		}
		else if (command.equals("para_all") || command.equals("unpara_all"))
		{
			boolean para = command.equals("para_all");
			try
			{
				for (L2PcInstance player : admin.getKnownList().getKnownPlayers().values())
					if (!player.isGM())
						if (para)
						{
							player.startAbnormalEffect(AbnormalEffect.HOLD_1);
							player.setIsParalyzed(true);
							player.broadcastPacket(new StopMove(player));
						}
						else
						{
							player.stopAbnormalEffect(AbnormalEffect.HOLD_1);
							player.setIsParalyzed(false);
						}
			}
			catch (Exception e)
			{
			}
			sendHtml(admin, "effects");
			return;
		}
		else if (command.equals("bighead") || command.equals("shrinkhead"))
		{
			try
			{
				target = admin.getTarget();
				if (target == null)
				{
					target = admin;
				}
				if (target != null && target instanceof L2Character)
					if (command.equals("shrinkhead"))
					{
						((L2Character) target).stopAbnormalEffect(AbnormalEffect.BIG_HEAD);
					}
					else
					{
						((L2Character) target).startAbnormalEffect(AbnormalEffect.BIG_HEAD);
					}
			}
			catch (Exception e)
			{
			}
			sendHtml(admin, "effects");
			return;
		}
		else if (command.equals("gmspeed"))
		{
			try
			{
				int val = Integer.parseInt(params[1]);
				admin.stopSkillEffects(7029);
				if (val == 0 && admin.getFirstEffect(7029) != null)
				{
					admin.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EFFECT_S1_DISAPPEARED).addSkillName(7029));
				}
				else if (val >= 1 && val <= 4)
				{
					L2Skill gmSpeedSkill = SkillTable.getInstance().getInfo(7029, val);
					admin.doSimultaneousCast(gmSpeedSkill);
				}
			}
			catch (Exception e)
			{
				admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Usage: //gmspeed [0-4]");
			}
			finally
			{
				admin.updateEffectIcons();
			}
			return;
		}
		else if (command.equals("social") || command.equals("social_menu"))
		{
			if (command.equals("social_menu"))
			{
				sendHtml(admin, "submenus/social_menu");
				return;
			}

			try
			{
				if (params.length == 3)
				{
					int social = Integer.parseInt(params[1]);
					int radius = Integer.parseInt(params[2]);

					for (L2PcInstance pl : admin.getKnownList().getKnownPlayersInRadius(radius))
					{
						if (pl == null || pl.isAlikeDead() || pl.isOfflineTrade() || pl.isTeleporting() || pl.inPrivateMode())
						{
							continue;
						}
						performSocial(social, pl, admin);
					}
					admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Social have sent all within a radius of " + radius);
				}
				else if (params.length == 2)
				{
					int social = Integer.parseInt(params[1]);
					L2Object obj = admin.getTarget();
					if (obj == null)
					{
						obj = admin;
					}

					performSocial(social, obj, admin);
				}
			}
			catch (Exception e)
			{
				admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Usage: //social [id]");
			}
			sendHtml(admin, "submenus/social_menu");
			return;
		}
		else if (command.equals("abnormal") || command.equals("abnormal_menu"))
		{
			if (command.equals("abnormal_menu"))
			{
				sendHtml(admin, "submenus/abnormal_menu");
				return;
			}

			try
			{
				if (params.length == 3)
				{
					int abnormal = Integer.decode("0x" + params[1]);
					int radius = Integer.parseInt(params[2]);

					for (L2PcInstance pl : admin.getKnownList().getKnownPlayersInRadius(radius))
					{
						if (pl == null || pl.isAlikeDead() || pl.isOfflineTrade() || pl.isTeleporting() || pl.inPrivateMode())
						{
							continue;
						}
						performAbnormal(abnormal, pl);
					}
					admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Effect of shipped all within a radius of " + radius);
				}
				else if (params.length == 2)
				{
					int abnormal = Integer.decode("0x" + params[1]);
					L2Object obj = admin.getTarget();
					if (obj == null)
					{
						obj = admin;
					}

					performAbnormal(abnormal, obj);
				}
			}
			catch (Exception e)
			{
				admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Usage: //social [id]");
			}
			sendHtml(admin, "submenus/abnormal_menu");
			return;
		}
	}
}