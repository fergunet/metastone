package es.ugr.gpstone;

import java.util.List;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.behaviour.DoNothingBehaviour;
import net.demilich.metastone.game.behaviour.IBehaviour;
import net.demilich.metastone.game.behaviour.PlayRandomBehaviour;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.CardProxy;
import net.demilich.metastone.game.cards.HeroCard;
import net.demilich.metastone.game.decks.Deck;
import net.demilich.metastone.game.decks.DeckFactory;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.logic.GameLogic;
import net.demilich.metastone.game.spells.trigger.GameStateChangedTrigger;
import net.demilich.metastone.game.statistics.GameStatistics;
import net.demilich.metastone.gui.deckbuilder.DeckProxy;
import net.demilich.metastone.gui.gameconfig.GameConfig;
import net.demilich.metastone.gui.gameconfig.PlayerConfig;
import net.demilich.metastone.gui.simulationmode.SimulateGamesCommand;
import net.demilich.metastone.gui.simulationmode.SimulationResult;
import net.demilich.metastone.tests.MassTest;


public class CommandLineLauncher {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		try{
			//MassTest mt = new MassTest();
			new CardProxy();
			//mt.testRandomMassPlay();	
			
			

			PlayerConfig playerConfig1 = generatePlayerConfig("Legendary GvG Ramp Druid","");
			playerConfig1.setName("Player 1");
			
	
			PlayerConfig playerConfig2 = generatePlayerConfig("Legendary GvG Ramp Druid","");
			playerConfig2.setName("Player 2");			
			
			//playerConfig1.build();
			//playerConfig2.build();
			
			Player player1 = new Player(playerConfig1);
			Player player2 = new Player(playerConfig2);
			
			GameConfig gameConfig = new GameConfig();
			gameConfig.setNumberOfGames(100);
			gameConfig.setPlayerConfig1(playerConfig1);
			gameConfig.setPlayerConfig2(playerConfig2);

			GameContext newGame = new GameContext(player1, player2, new GameLogic());
			//newGame.init();
			newGame.play();

			System.out.println("WINNER IS "+newGame.getWinningPlayerId());
			SimulationResult sresult = new SimulationResult(gameConfig);
			sresult.calculateMetaStatistics();
			GameStatistics p1stats = sresult.getPlayer1Stats();
			GameStatistics p2stats = sresult.getPlayer2Stats();

			
			System.out.println("ENDING");
			System.out.println(p1stats.toString());
			System.out.println(p2stats.toString());
			
			System.out.println(newGame.getPlayer1().getStatistics());//of the last game. Use the MERGE from SimulationResult after
			newGame.dispose();
			
		
		}catch(Exception ex){
			ex.printStackTrace();
		}
		
	}
	
	private static PlayerConfig generatePlayerConfig(String deckName, String behaviourName) throws Exception{
		DeckProxy dProxy = new DeckProxy();
		dProxy.loadDecks();
		List<Deck> ds = dProxy.getDecks();
		/*for(Deck theD:ds){
			System.out.println(theD.getName()+"-----"+theD.getFilename());
		}*/
		Deck d = dProxy.getDeckByName(deckName);//DeckFactory.getRandomDeck(HeroClass.DRUID);

		if(d == null)
			throw new Exception("Deck with name "+deckName+" does not exist;");

		HeroClass heroClass = d.getHeroClass();		
		PlayerConfig pc = new PlayerConfig(d, new PlayRandomBehaviour());
		pc.setHeroCard(getHeroCardForClass(heroClass));
		return pc;
	}
	
	protected static HeroCard getHeroCardForClass(HeroClass heroClass) {
		for (Card card : CardCatalogue.getHeroes()) {
			HeroCard heroCard = (HeroCard) card;
			if (heroCard.getHeroClass() == heroClass) {
				return heroCard;
			}
		}
		return null;
	}
	
	

}
