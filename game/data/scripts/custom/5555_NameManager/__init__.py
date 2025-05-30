import sys
from com.dream.game.model.actor.instance import L2PcInstance
from java.util import Iterator
from com.dream.game.datatables.xml import SkillTable
from com.dream import L2DatabaseFactory
from com.dream.game.model.quest import State
from com.dream.game.model.quest import QuestState
from com.dream.game.model.quest.jython import QuestJython as JQuest
from com.dream.game.datatables.sql import CharNameTable
from com.dream.game.model.world import L2World
from com.dream.game.util import Util;
from java.util.regex import Pattern;

qn = "5555_NameManager" 
 
# ID Do Npc:
NPC = [50033]
 
# ID Da Coin: 
ITEM_ID = 9215
 
# Contagem do Item
NAME_COUNT = 50
 
QuestId     = 5555
QuestName   = "NameManager"
QuestDesc   = "custom"
InitialHtml = "1.htm"

print "Name Manager Enabled..."
 
class Quest (JQuest) :

 def __init__(self,id,name,descr): JQuest.__init__(self,id,name,descr)
 
 def onAdvEvent (self,event,npc,player) :
    st = player.getQuestState(qn)
    if not st: return
	
    if event == "1" :
        return "2.htm"
    if not CharNameTable.getInstance().doesCharNameExist(event):	
     if len(event) >= 3 and len(event) <= 16 and Pattern.matches("[([a-zA-Z0-9])+([a-zA-Z0-9])+([a-zA-Z0-9])+([a-zA-Z0-9])]*", event):	 
      if st.getPlayer().isClanLeader() :
          return "Leader.htm"
      if st.getPlayer().getClan() :
          return "Clan.htm"
      if st.getQuestItemsCount(ITEM_ID) >= NAME_COUNT:
                st.takeItems(ITEM_ID,NAME_COUNT)			  
                L2World.getInstance().removeFromAllPlayers(st.getPlayer());			  
                st.getPlayer().setName(event);			  
                st.getPlayer().store();			  
                L2World.getInstance().addToAllPlayers(st.getPlayer());			  
                st.getPlayer().broadcastUserInfo();			  
                st.playSound("ItemSound.quest_finish")			  
                st.player.sendMessage("Congratulations you Change a new Nick Name!")			  
                return "Thanks.htm"			  
      else:
       return "NoItem.htm"
     else:
       return "NoChar.htm"
    else:
         return "NoName.htm"	
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