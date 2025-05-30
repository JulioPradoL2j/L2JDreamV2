import sys
from com.dream.game.model.actor.instance import L2PcInstance
from java.util import Iterator
from com.dream.game.datatables.xml import SkillTable
from com.dream import L2DatabaseFactory
from com.dream.game.model.actor.appearance import PcAppearance
from com.dream.game.model.quest import State
from com.dream.game.model.quest import QuestState
from com.dream.game.model.quest.jython import QuestJython as JQuest

qn = "5555_CleanPk"

NPC=[50033]
PriceID= 9215
PriceCount= 5


QuestId     = 5555
QuestName   = "CleanPk"
QuestDesc   = "custom"
InitialHtml = "1.htm"

print "Clean Pk Enabled..."

class Quest (JQuest) :

 def __init__(self,id,name,descr): JQuest.__init__(self,id,name,descr)
 
 def onAdvEvent (self,event,npc,player) :
    htmltext = event
    st = player.getQuestState(qn)
    if not st: return
	
    if event == "Reduce" :
      if st.getPlayer().getPkKills() >= 4 :	
       if st.getQuestItemsCount(PriceID) >= PriceCount :	 
                 st.player.setPkKills(0)			  
                 st.getPlayer().broadcastUserInfo();			  
                 st.playSound("ItemSound.quest_finish")			  
                 st.player.sendMessage("Congratulations, you reduced Pk Points to 0!")			  
                 st.takeItems(PriceID,PriceCount)			  
                 return "Thanks.htm"			  
       else:
         return "NoItem.htm"
      else:
         return "NoPk.htm"	
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