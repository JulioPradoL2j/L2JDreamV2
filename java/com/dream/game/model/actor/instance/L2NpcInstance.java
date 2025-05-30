package com.dream.game.model.actor.instance;

import java.util.List;

import com.dream.Config;
import com.dream.game.cache.HtmCache;
import com.dream.game.datatables.xml.SkillTable;
import com.dream.game.datatables.xml.SkillTreeTable;
import com.dream.game.model.L2Effect;
import com.dream.game.model.L2EnchantSkillLearn;
import com.dream.game.model.L2Skill;
import com.dream.game.model.L2SkillLearn;
import com.dream.game.model.actor.L2Npc;
import com.dream.game.model.actor.status.FolkStatus;
import com.dream.game.model.base.ClassId;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.AcquireSkillList;
import com.dream.game.network.serverpackets.ActionFailed;
import com.dream.game.network.serverpackets.ExEnchantSkillList;
import com.dream.game.network.serverpackets.NpcHtmlMessage;
import com.dream.game.network.serverpackets.SystemMessage;
import com.dream.game.skills.effects.EffectBuff;
import com.dream.game.skills.effects.EffectDebuff;
import com.dream.game.templates.chars.L2NpcTemplate;
import com.dream.game.templates.skills.L2SkillType;

public class L2NpcInstance extends L2Npc
{
	private final List<ClassId> _classesToTeach;

	public L2NpcInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		_classesToTeach = template.getTeachInfo();
	}

	@Override
	public void addEffect(L2Effect newEffect)
	{
		if (newEffect instanceof EffectDebuff || newEffect instanceof EffectBuff)
		{
			super.addEffect(newEffect);
		}
		else if (newEffect != null)
		{
			newEffect.stopEffectTask();
		}
	}

	@Override
	public FolkStatus getStatus()
	{
		if (_status == null)
		{
			_status = new FolkStatus(this);
		}
		return (FolkStatus) _status;
	}

	@Override
	public void onAction(L2PcInstance player)
	{
		player.setLastFolkNPC(this);
		super.onAction(player);
	}

	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		if (player.inObserverMode())
			return;

		if (command.startsWith("SkillList"))
		{
			if (Config.ALT_GAME_SKILL_LEARN)
			{
				String id = command.substring(9).trim();

				if (id.length() != 0)
				{
					player.setSkillLearningClassId(ClassId.values()[Integer.parseInt(id)]);
					showSkillList(player, ClassId.values()[Integer.parseInt(id)]);
				}
				else
				{
					boolean own_class = false;

					if (_classesToTeach != null)
					{
						for (ClassId cid : _classesToTeach)
							if (cid.equalsOrChildOf(player.getClassId()))
							{
								own_class = true;
								break;
							}
					}

					String text = "<html><body><center>Skill learning:</center><br>";

					if (!own_class)
					{
						String charType = player.getClassId().isMage() ? "fighter" : "mage";
						text += "Skills of your class are the easiest to learn.<br>\n" + "Skills of another class of your race are a little harder.<br>" + "Skills for classes of another race are extremely difficult.<br>" + "But the hardest of all to learn are the " + charType + " skills!<br>";
					}

					if (_classesToTeach != null)
					{
						int count = 0;
						ClassId classCheck = player.getClassId();

						while (count == 0 && classCheck != null)
						{
							for (ClassId cid : _classesToTeach)
							{
								if (cid.level() != classCheck.level())
								{
									continue;
								}

								if (SkillTreeTable.getInstance().getAvailableSkills(player, cid).length == 0)
								{
									continue;
								}

								text += "<a action=\"bypass -h npc_%objectId%_SkillList " + cid.getId() + "\">Learn " + cid + "'s class Skills</a><br>\n";
								count++;
							}
							classCheck = classCheck.getParent();
						}
						classCheck = null;
					}
					else
					{
						text += "No Skills.<br>\n";
					}

					text += "</body></html>";

					insertObjectIdAndShowChatWindow(player, text);
					player.sendPacket(ActionFailed.STATIC_PACKET);
				}
			}
			else
			{
				player.setSkillLearningClassId(player.getClassId());
				showSkillList(player, player.getClassId());
			}
		}
		else if (command.startsWith("EnchantSkillList"))
		{
			showEnchantSkillList(player);
		}
		else
		{
			super.onBypassFeedback(player, command);
		}
	}

	public void showEnchantSkillList(L2PcInstance player)
	{
		int npcId = getTemplate().getNpcId();

		if (_classesToTeach == null)
		{
			NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			StringBuilder sb = new StringBuilder();
			sb.append("<html><body>");
			sb.append("I cannot teach you. My class list is empty.<br>Ask admin to fix it. Need add my npcid and classes to skill_learn.sql.<br>NpcId:" + npcId + ", Your classId:" + player.getClassId().getId() + "<br>");
			sb.append("</body></html>");
			html.setHtml(sb.toString());
			player.sendPacket(html);

			return;
		}

		if (!getTemplate().canTeach(player.getClassId()))
		{
			showNoTeachHtml(player);
			return;
		}

		if (player.getClassId().level() < 3)
		{
			NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			StringBuilder sb = new StringBuilder();
			sb.append("<html><body>Enchant A Skill:<br>");
			sb.append("Only characters who have changed their occupation three times are allowed to enchant a skill.");
			sb.append("</body></html>");
			html.setHtml(sb.toString());
			player.sendPacket(html);
			return;
		}

		int playerLevel = player.getLevel();

		if (playerLevel >= 76)
		{
			L2EnchantSkillLearn[] skills = SkillTreeTable.getInstance().getAvailableEnchantSkills(player);
			ExEnchantSkillList esl = new ExEnchantSkillList();
			int counts = 0;
			for (L2EnchantSkillLearn s : skills)
			{
				L2Skill sk = SkillTable.getInstance().getInfo(s.getId(), s.getLevel());
				if (sk == null)
				{
					continue;
				}
				counts++;
				esl.addSkill(s.getId(), s.getLevel(), s.getSpCost(), s.getExp());
			}

			if (counts == 0)
			{
				player.sendPacket(SystemMessageId.THERE_IS_NO_SKILL_THAT_ENABLES_ENCHANT);
			}
			else
			{
				player.sendPacket(esl);
			}
		}
		else
		{
			player.sendPacket(SystemMessageId.THERE_IS_NO_SKILL_THAT_ENABLES_ENCHANT);
		}

		player.sendPacket(ActionFailed.STATIC_PACKET);
	}

	private void showNoTeachHtml(L2PcInstance player)
	{
		int npcId = getNpcId();
		String html = "";

		if (this instanceof L2WarehouseInstance)
		{
			html = HtmCache.getInstance().getHtm("data/html/warehouse/" + npcId + "-noteach.htm");
		}
		else if (this instanceof L2TrainerInstance)
		{
			html = HtmCache.getInstance().getHtm("data/html/trainer/" + npcId + "-noteach.htm");
		}
		if (html == null)
		{
			_log.warn("Npc " + npcId + " missing noTeach html!");
			NpcHtmlMessage htm = new NpcHtmlMessage(getObjectId());
			StringBuilder sb = new StringBuilder();
			sb.append("<html><body>");
			sb.append("I cannot teach you any skills.<br> You must find your current class teachers.");
			sb.append("</body></html>");
			htm.setHtml(sb.toString());
			player.sendPacket(htm);
		}
		else
		{
			NpcHtmlMessage noTeachMsg = new NpcHtmlMessage(getObjectId());
			noTeachMsg.setHtml(html.replaceAll("%objectId%", String.valueOf(getObjectId())));
			player.sendPacket(noTeachMsg);
		}
	}

	public void showSkillList(L2PcInstance player, ClassId classId)
	{
		if (_log.isDebugEnabled() || Config.DEBUG)
		{
			_log.debug("SkillList activated on: " + getObjectId());
		}

		int npcId = getTemplate().getNpcId();
		if (_classesToTeach == null)
		{
			NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			StringBuilder sb = new StringBuilder();
			sb.append("<html><body>");
			sb.append("I cannot teach you. My class list is empty.<br>Ask admin to fix it. Need add my npcid and classes to skill_learn.sql.<br>NpcId:" + npcId + ", Your classId:" + player.getClassId().getId() + "<br>");
			sb.append("</body></html>");
			html.setHtml(sb.toString());
			player.sendPacket(html);

			return;
		}

		if (!getTemplate().canTeach(classId))
		{
			showNoTeachHtml(player);
			return;
		}

		L2SkillLearn[] skills = SkillTreeTable.getInstance().getAvailableSkills(player, classId);
		AcquireSkillList asl = new AcquireSkillList(AcquireSkillList.SkillType.Usual);
		int counts = 0;

		for (L2SkillLearn s : skills)
		{
			L2Skill sk = SkillTable.getInstance().getInfo(s.getId(), s.getLevel());

			if (sk == null || !sk.getCanLearn(player.getClassId()) || !sk.canTeachBy(npcId))
			{
				continue;
			}

			if (sk.getSkillType() == L2SkillType.NOTDONE)
			{
				switch (Config.SEND_NOTDONE_SKILLS)
				{
					case 3:
						break;
					case 2:
						if (player.isGM())
						{
							break;
						}
					default:
						continue;
				}
			}

			int cost = SkillTreeTable.getInstance().getSkillCost(player, sk);
			counts++;

			asl.addSkill(s.getId(), s.getLevel(), s.getLevel(), cost, 0);
		}

		if (counts == 0)
		{
			int minlevel = SkillTreeTable.getInstance().getMinLevelForNewSkill(player, classId);

			if (minlevel > 0)
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.DO_NOT_HAVE_FURTHER_SKILLS_TO_LEARN_COME_BACK_WHEN_REACHED_S1).addNumber(minlevel));
			}
			else
			{
				player.sendPacket(SystemMessageId.NO_MORE_SKILLS_TO_LEARN);
			}
		}
		else
		{
			player.sendPacket(asl);
		}

		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
}
