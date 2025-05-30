import sys
from com.dream.game.model.actor.instance import L2PcInstance
from java.util import Iterator
from com.dream.game.datatables.xml import SkillTable
from com.dream import L2DatabaseFactory
from com.dream.game.model.quest import State
from com.dream.game.model.quest import QuestState
from com.dream.game.model.quest.jython import QuestJython as JQuest

qn = "5555_ClanManager"

NPC=[50033]

PriceIDClanLevel1= 57
PriceCountClanLevel1= 1000000

PriceIDClanLevel2= 57
PriceCountClanLevel2= 2000000

PriceIDClanLevel3= 57
PriceCountClanLevel3= 5000000

PriceIDClanLevel4= 57
PriceCountClanLevel4= 10000000

PriceIDClanLevel5= 57
PriceCountClanLevel5= 20000000

PriceIDClanLevel6= 9215
PriceCountClanLevel6= 5

PriceIDClanLevel7= 9215
PriceCountClanLevel7= 10

PriceIDClanLevel8= 9215
PriceCountClanLevel8= 20

PriceIDClanSkill= 9215
PriceCountClanSkill= 50

PriceIDClanFull= 9215
ReputationScoreCount1= 30000000
PriceCountClanFull= 100

PriceIDReputationScore= 9215
PriceCountReputationScore= 5
ReputationScoreCount= 10000

QuestId     = 5555
QuestName   = "ClanManager"
QuestDesc   = "custom"
InitialHtml = "1.htm"

print "Clan Manager Enabled..."

class Quest (JQuest) :

 def __init__(self,id,name,descr): JQuest.__init__(self,id,name,descr)
 
 def onAdvEvent (self,event,npc,player) :
    htmltext = event
    st = player.getQuestState(qn)
    if not st: return
	
    if event == "Clan1" :
      if st.getPlayer().getClan():	
       if st.getPlayer().isClanLeader() and st.getPlayer().getClan().getLevel()<1:	 
        if st.getQuestItemsCount(PriceIDClanLevel1) >= PriceCountClanLevel1:
                  st.getPlayer().getClan().changeLevel(1)			  
                  st.playSound("ItemSound.quest_finish")			  
                  st.player.sendMessage("Congratulations, you have improved your clan to Lvl 1!")			  
                  st.takeItems(PriceIDClanLevel1,PriceCountClanLevel1)			  
                  return "Thanks.htm"			  
        else:
         return "NoItem.htm"
       else:
         return "ClanLvl1.htm"
      else:
         return "NoClan.htm"	

    if event == "Clan2" :
      if st.getPlayer().getClan():	
       if st.getPlayer().isClanLeader() and st.getPlayer().getClan().getLevel()==1:	 
        if st.getQuestItemsCount(PriceIDClanLevel2) >= PriceCountClanLevel2:
                  st.getPlayer().getClan().changeLevel(2)			  
                  st.playSound("ItemSound.quest_finish")			  
                  st.player.sendMessage("Congratulations, you have improved your clan to Lvl 2!")			  
                  st.takeItems(PriceIDClanLevel2,PriceCountClanLevel2)			  
                  return "Thanks.htm"			  
        else:
         return "NoItem.htm"
       else:
         return "ClanLvl2.htm"
      else:
         return "NoClan.htm"	

    if event == "Clan3" :
      if st.getPlayer().getClan():	
       if st.getPlayer().isClanLeader() and st.getPlayer().getClan().getLevel()==2:	 
        if st.getQuestItemsCount(PriceIDClanLevel3) >= PriceCountClanLevel3:
                  st.getPlayer().getClan().changeLevel(3)			  
                  st.playSound("ItemSound.quest_finish")			  
                  st.player.sendMessage("Congratulations, you have improved your clan to Lvl 3!")			  
                  st.takeItems(PriceIDClanLevel3,PriceCountClanLevel3)			  
                  return "Thanks.htm"			  
        else:
         return "NoItem.htm"
       else:
         return "ClanLvl3.htm"
      else:
         return "NoClan.htm"	

    if event == "Clan4" :
      if st.getPlayer().getClan():	
       if st.getPlayer().isClanLeader() and st.getPlayer().getClan().getLevel()==3:	 
        if st.getQuestItemsCount(PriceIDClanLevel4) >= PriceCountClanLevel4:
                  st.getPlayer().getClan().changeLevel(4)			  
                  st.playSound("ItemSound.quest_finish")			  
                  st.player.sendMessage("Congratulations, you have improved your clan to Lvl 4!")			  
                  st.takeItems(PriceIDClanLevel4,PriceCountClanLevel4)			  
                  return "Thanks.htm"			  
        else:
         return "NoItem.htm"
       else:
         return "ClanLvl4.htm"
      else:
         return "NoClan.htm"	

    if event == "Clan5" :
      if st.getPlayer().getClan():	
       if st.getPlayer().isClanLeader() and st.getPlayer().getClan().getLevel()==4:	 
        if st.getQuestItemsCount(PriceIDClanLevel5) >= PriceCountClanLevel5:
                  st.getPlayer().getClan().changeLevel(5)			  
                  st.playSound("ItemSound.quest_finish")			  
                  st.player.sendMessage("Congratulations, you have improved your clan to Lvl 5!")			  
                  st.takeItems(PriceIDClanLevel5,PriceCountClanLevel5)			  
                  return "Thanks.htm"			  
        else:
         return "NoItem.htm"
       else:
         return "ClanLvl5.htm"
      else:
         return "NoClan.htm"	

    if event == "Clan6" :
      if st.getPlayer().getClan():	
       if st.getPlayer().isClanLeader() and st.getPlayer().getClan().getLevel()==5:	 
        if st.getQuestItemsCount(PriceIDClanLevel6) >= PriceCountClanLevel6:
                  st.getPlayer().getClan().changeLevel(6)			  
                  st.playSound("ItemSound.quest_finish")			  
                  st.player.sendMessage("Congratulations, you have improved your clan to Lvl 6!")			  
                  st.takeItems(PriceIDClanLevel6,PriceCountClanLevel6)			  
                  return "Thanks.htm"			  
        else:
         return "NoItem.htm"
       else:
         return "ClanLvl6.htm"
      else:
         return "NoClan.htm"	

    if event == "Clan7" :
      if st.getPlayer().getClan():	
       if st.getPlayer().isClanLeader() and st.getPlayer().getClan().getLevel()==6:	 
        if st.getQuestItemsCount(PriceIDClanLevel7) >= PriceCountClanLevel7:
                  st.getPlayer().getClan().changeLevel(7)			  
                  st.playSound("ItemSound.quest_finish")			  
                  st.player.sendMessage("Congratulations, you have improved your clan to Lvl 7!")			  
                  st.takeItems(PriceIDClanLevel7,PriceCountClanLevel7)			  
                  return "Thanks.htm"			  
        else:
         return "NoItem.htm"
       else:
         return "ClanLvl7.htm"
      else:
         return "NoClan.htm"	

    if event == "Clan8" :
      if st.getPlayer().getClan():	
       if st.getPlayer().isClanLeader() and st.getPlayer().getClan().getLevel()==7:	 
        if st.getQuestItemsCount(PriceIDClanLevel8) >= PriceCountClanLevel8:
                  st.getPlayer().getClan().changeLevel(8)			  
                  st.playSound("ItemSound.quest_finish")			  
                  st.player.sendMessage("Congratulations, you have improved your clan to Lvl 8!")			  
                  st.takeItems(PriceIDClanLevel8,PriceCountClanLevel8)			  
                  return "Thanks.htm"			  
        else:
         return "NoItem.htm"
       else:
         return "ClanLvl8.htm"
      else:
         return "NoClan.htm"	
			
    if event == "Skills" :
      if st.getPlayer().getClan():	
       if st.getPlayer().isClanLeader() and st.getPlayer().getClan().getLevel()==8:	 
        if st.getQuestItemsCount(PriceIDClanSkill) >= PriceCountClanSkill:
                  st.getPlayer().getClan().addNewSkill(SkillTable.getInstance().getInfo(370, 3));			  
                  st.getPlayer().getClan().addNewSkill(SkillTable.getInstance().getInfo(371, 3));			  
                  st.getPlayer().getClan().addNewSkill(SkillTable.getInstance().getInfo(372, 3));			  
                  st.getPlayer().getClan().addNewSkill(SkillTable.getInstance().getInfo(373, 3));			  
                  st.getPlayer().getClan().addNewSkill(SkillTable.getInstance().getInfo(374, 3));			  
                  st.getPlayer().getClan().addNewSkill(SkillTable.getInstance().getInfo(375, 3));			  
                  st.getPlayer().getClan().addNewSkill(SkillTable.getInstance().getInfo(376, 3));			  
                  st.getPlayer().getClan().addNewSkill(SkillTable.getInstance().getInfo(377, 3));			  
                  st.getPlayer().getClan().addNewSkill(SkillTable.getInstance().getInfo(378, 3));			  
                  st.getPlayer().getClan().addNewSkill(SkillTable.getInstance().getInfo(379, 3));			  
                  st.getPlayer().getClan().addNewSkill(SkillTable.getInstance().getInfo(380, 3));			  
                  st.getPlayer().getClan().addNewSkill(SkillTable.getInstance().getInfo(381, 3));			  
                  st.getPlayer().getClan().addNewSkill(SkillTable.getInstance().getInfo(382, 3));			  
                  st.getPlayer().getClan().addNewSkill(SkillTable.getInstance().getInfo(383, 3));			  
                  st.getPlayer().getClan().addNewSkill(SkillTable.getInstance().getInfo(384, 3));			  
                  st.getPlayer().getClan().addNewSkill(SkillTable.getInstance().getInfo(385, 3));			  
                  st.getPlayer().getClan().addNewSkill(SkillTable.getInstance().getInfo(386, 3));			  
                  st.getPlayer().getClan().addNewSkill(SkillTable.getInstance().getInfo(387, 3));			  
                  st.getPlayer().getClan().addNewSkill(SkillTable.getInstance().getInfo(388, 3));			  
                  st.getPlayer().getClan().addNewSkill(SkillTable.getInstance().getInfo(389, 3));			  
                  st.getPlayer().getClan().addNewSkill(SkillTable.getInstance().getInfo(390, 3));			  
                  st.getPlayer().getClan().addNewSkill(SkillTable.getInstance().getInfo(391, 1));			  
                  st.playSound("ItemSound.quest_finish")			  
                  st.player.sendMessage("Congratulations, you have improved your clan in Full Skills!")			  
                  st.takeItems(PriceIDClanSkill,PriceCountClanSkill)			  
                  return "Thanks.htm"			  
        else:
         return "NoItem.htm"
       else:
         return "ClanLvl8.htm"
      else:
         return "NoClan.htm"	

    if event == "ClanFull" :
      if st.getPlayer().getClan():	
       if st.getPlayer().isClanLeader() and st.getPlayer().getClan().getLevel()>=5 and st.getPlayer().getClan().getLevel()<=7 :	 
        if st.getQuestItemsCount(PriceIDClanFull) >= PriceCountClanFull:
                  st.getPlayer().getClan().addNewSkill(SkillTable.getInstance().getInfo(370, 3));			  
                  st.getPlayer().getClan().addNewSkill(SkillTable.getInstance().getInfo(371, 3));			  
                  st.getPlayer().getClan().addNewSkill(SkillTable.getInstance().getInfo(372, 3));			  
                  st.getPlayer().getClan().addNewSkill(SkillTable.getInstance().getInfo(373, 3));			  
                  st.getPlayer().getClan().addNewSkill(SkillTable.getInstance().getInfo(374, 3));			  
                  st.getPlayer().getClan().addNewSkill(SkillTable.getInstance().getInfo(375, 3));			  
                  st.getPlayer().getClan().addNewSkill(SkillTable.getInstance().getInfo(376, 3));			  
                  st.getPlayer().getClan().addNewSkill(SkillTable.getInstance().getInfo(377, 3));			  
                  st.getPlayer().getClan().addNewSkill(SkillTable.getInstance().getInfo(378, 3));			  
                  st.getPlayer().getClan().addNewSkill(SkillTable.getInstance().getInfo(379, 3));			  
                  st.getPlayer().getClan().addNewSkill(SkillTable.getInstance().getInfo(380, 3));			  
                  st.getPlayer().getClan().addNewSkill(SkillTable.getInstance().getInfo(381, 3));			  
                  st.getPlayer().getClan().addNewSkill(SkillTable.getInstance().getInfo(382, 3));			  
                  st.getPlayer().getClan().addNewSkill(SkillTable.getInstance().getInfo(383, 3));			  
                  st.getPlayer().getClan().addNewSkill(SkillTable.getInstance().getInfo(384, 3));			  
                  st.getPlayer().getClan().addNewSkill(SkillTable.getInstance().getInfo(385, 3));			  
                  st.getPlayer().getClan().addNewSkill(SkillTable.getInstance().getInfo(386, 3));			  
                  st.getPlayer().getClan().addNewSkill(SkillTable.getInstance().getInfo(387, 3));			  
                  st.getPlayer().getClan().addNewSkill(SkillTable.getInstance().getInfo(388, 3));			  
                  st.getPlayer().getClan().addNewSkill(SkillTable.getInstance().getInfo(389, 3));			  
                  st.getPlayer().getClan().addNewSkill(SkillTable.getInstance().getInfo(390, 3));			  
                  st.getPlayer().getClan().addNewSkill(SkillTable.getInstance().getInfo(391, 1));			  
                  st.getPlayer().getClan().changeLevel(8)			  
                  st.getPlayer().getClan().setReputationScore(ReputationScoreCount1, 1);			  
                  st.playSound("ItemSound.quest_finish")			  
                  st.player.sendMessage("Congratulations, you have improved your clan on Maximum!")			  
                  st.takeItems(PriceIDClanFull,PriceCountClanFull)			  
                  return "Thanks.htm"			  
        else:
         return "NoItem.htm"
       else:
         return "ClanLvl8.htm"
      else:
         return "NoClan.htm"	

    if event == "Reputation" :
      if st.getPlayer().getClan():	
       if st.getPlayer().isClanLeader() and st.getPlayer().getClan().getLevel()>=5:	 
        if st.getQuestItemsCount(PriceIDReputationScore) >= PriceCountReputationScore:		  
                  rep = st.getPlayer().getClan().getReputationScore();			  
                  st.getPlayer().getClan().setReputationScore(rep+ReputationScoreCount, 1);			  
                  st.getPlayer().getClan().changeLevel(8)			  		  
                  st.playSound("ItemSound.quest_finish")			  
                  st.player.sendMessage("Congratulations, you have improved your clan Reputation!")			  
                  st.takeItems(PriceIDReputationScore,PriceCountReputationScore)			  
                  return "Thanks.htm"			  
        else:
         return "NoItem.htm"
       else:
         return "ClanLvl5.htm"
      else:
         return "NoClan.htm"	
    return htmltext		


 def onTalk (self,npc,player):
		htmltext = "<html><head><body>You can't buy while you are</body></html>"
		st = player.getQuestState(qn)
		if not st : return htmltext
		npcId = npc.getNpcId()		
		if st.player.getPvpFlag() > 0 :#Player Flag
			htmltext = "Flag.htm"
			st.exitQuest(1)
		elif st.player.getKarma() > 0 :#Player com Karma
			st.exitQuest(1)
			htmltext = "Karma.htm"
		elif st.player.isInCombat() :#Player modo de Combate
			st.exitQuest(1)
			htmltext = "Combat.htm"
		else:
			
			return InitialHtml
		return htmltext

QUEST       = Quest(QuestId,str(QuestId) + "_" + QuestName,QuestDesc)

for npcId in NPC:
 QUEST.addStartNpc(npcId)
 QUEST.addTalkId(npcId)	