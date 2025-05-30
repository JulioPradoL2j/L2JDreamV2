import sys
from com.dream.game.model.actor.instance import L2PcInstance
from java.util import Iterator
from com.dream.game.datatables.xml import SkillTable
from com.dream import L2DatabaseFactory
from com.dream.game.model.actor.appearance import PcAppearance
from com.dream.game.model.quest import State
from com.dream.game.model.quest import QuestState
from com.dream.game.model.quest.jython import QuestJython as JQuest

qn = "5555_NoblesSeller"

NPC=[50033]
PriceIDNobles= 9215
PriceCountNobles= 50
ClassIds=88,89,90,91,92,93,94,95,96,97,98,99,100,101,102,103,104,105,106,107,108,109,110,111,112,113,114,115,116,117,118
Race=0,1,2,3,4

NOBLESS_TIARA = 7694

QuestId     = 5555
QuestName   = "NoblesSeller"
QuestDesc   = "custom"
InitialHtml = "1.htm"

print "Noblesse Seller Enabled..."

class Quest (JQuest) :

 def __init__(self,id,name,descr): JQuest.__init__(self,id,name,descr)
 
 def onAdvEvent (self,event,npc,player) :
    htmltext = event
    st = player.getQuestState(qn)
    race = player.getRace().ordinal()
    classId = player.getClassId()
    id = classId.getId()	
    Lvl = st.getPlayer().getLevel()	
    if not st: return
	
    if event == "Nobles" :
      if st.getPlayer().getLevel() >= 76:	
       if not st.getPlayer().isNoble() :	 
        if race in Race and id in ClassIds :
         if st.getQuestItemsCount(PriceIDNobles) >= PriceCountNobles :
         	st.getPlayer().setNoble(True)			  
         	st.giveItems(NOBLESS_TIARA,1)			  
         	st.playSound("ItemSound.quest_finish")			  
         	st.player.sendMessage("Congratulations you become a Nobles Status!")			  
         	st.takeItems(PriceIDNobles,PriceCountNobles)				  
         	return "Thanks.htm"			  
         else:
          return "NoItem.htm"
        else:
         return "NoClass.htm"
       else:
         return "Nobles.htm"
      else:
         return "NoLvl.htm"	
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