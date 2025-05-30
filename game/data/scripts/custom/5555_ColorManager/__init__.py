import sys
from com.dream.game.model.actor.instance import L2PcInstance
from com.dream.game.model.quest import State
from com.dream.game.model.quest import QuestState
from com.dream.game.model.quest.jython import QuestJython as JQuest

qn = "5555_ColorManager"

NPC=[50033]

ItemId		= 9215
ItemQty		= 1
MinLevel	= 1
MaxLevel	= 80

QuestId     = 5555
QuestName   = "ColorManager"
QuestDesc   = "custom"
InitialHtml = "1.htm"

print "Color Manager Enabled..."

class Quest (JQuest) :
    def __init__(self,id,name,descr): JQuest.__init__(self,id,name,descr)
    
    def onAdvEvent(Self,event,npc,player):
	htmltext = "1.htm"
	st = player.getQuestState(qn)
	if not st : return text
	if (player.getLevel() < MinLevel) or (player.getLevel() > MaxLevel) : return "Lvl.htm"
	if st.getQuestItemsCount(ItemId) < ItemQty : return "NoItem.htm"
	nameColor = hex(player.getAppearance().getNameColor())
	titleColor = hex(player.getAppearance().getTitleColor())
	st.set("nameColor",str(nameColor))
	st.set("titleColor",str(titleColor))
	if event == "N_Blue" :
	    nameColor = "FF0000"
	elif event == "N_Red" :
	    nameColor = "0088FF"
	elif event == "N_Cyan" :
	    nameColor = "00FF00"
	elif event == "N_Magenta" :
	    nameColor = "7280FA"
	elif event == "N_Yellow" :
	    nameColor = "00FFFF"
	elif event == "N_Gray" :
	    nameColor = "808080"
	elif event == "N_White" :
	    nameColor = "FFFFFF"
	elif event == "T_Blue" :
	    titleColor = "FF0000"
	elif event == "T_Red" :
	    titleColor = "0000FF"
	elif event == "T_Cyan" :
	    titleColor = "FFFF00"
	elif event == "T_Green" :
	    titleColor = "00FF00"
	elif event == "T_Magenta" :
	    titleColor = "FF00FF"
	elif event == "T_Yellow" :
	    titleColor = "00FFFF"
	elif event == "T_Gray" :
	    titleColor = "808080"
	elif event == "T_White" :
	    titleColor = "FFFFFF"
	nameColor = int(nameColor,16)
	titleColor = int(titleColor,16)
	player.getAppearance().setNameColor(nameColor)
	player.getAppearance().setTitleColor(titleColor)
	st.takeItems(ItemId,ItemQty)
	player.broadcastUserInfo()
	player.store()
	text = "Thanks.htm"
	return text
	
    def onTalk (Self,npc,player):
	st = player.getQuestState(qn)
	if not st : return htmltext
	npcId = npc.getNpcId()
	if st.player.getPvpFlag() > 0 :#Player Flag
		htmltext = "Flag.htm"
		st.exitQuest(1)
	elif st.player.getKarma() > 0 :#Player com Karma
		htmltext = "Karma.htm"
		st.exitQuest(1)
	elif st.player.isInCombat() :#Player modo de Combate
		htmltext = "Combat.htm"
		st.exitQuest(1)	
	return "1.htm"

QUEST       = Quest(QuestId,str(QuestId) + "_" + QuestName,QuestDesc)

for npcId in NPC:
 QUEST.addStartNpc(npcId)
 QUEST.addTalkId(npcId)	