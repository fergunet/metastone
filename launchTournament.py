import glob
import os
import sys
import time

java_bin = "/home/pgarcia/jdk1.8.0_45/bin/java -jar metastone.jar"
decks = ["Midrange Druid Season 18",
"Midrange Hunter Season 18",
"Aggro Paladin Season 18",
"Shadow Madness Priest Season 18",
"Oil Rogue Season 18",
"Mech Shaman Season 18",
"Warlock MalyLock Season 18",
"Control Warrior Season 18"]

ais = ["FlatMonteCarlo",
"GreedyOptimizeTurn",
"GreedyOptimizeMove",
"PlayRandomBehaviour"
]

numberOfGames = 10

for deck1 in decks:
	for deck2 in decks:
		for AI1 in ais:
			for AI2 in ais:
				params = "\""+deck1+"\" \""+deck2+"\" "+AI1+" "+AI2
				filename = params.replace(" ", "_").replace("\"","")+".txt"
				launchline = "("+java_bin+" "+params+" "+str(numberOfGames) +")>"+filename
				os.system(launchline)
				print launchline