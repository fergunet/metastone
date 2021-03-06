package net.demilich.metastone.tests;

import org.testng.Assert;
import org.testng.annotations.Test;

import net.demilich.metastone.game.Attribute;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.MinionCard;
import net.demilich.metastone.game.cards.SpellCard;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.entities.minions.Race;
import net.demilich.metastone.game.spells.SilenceSpell;
import net.demilich.metastone.game.spells.SwapAttackAndHpSpell;
import net.demilich.metastone.game.spells.TemporaryAttackSpell;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.targeting.TargetSelection;

public class CardInteractionTests extends TestBase {

	@Test
	public void testAttackBuffStacking() {
		GameContext context = createContext(HeroClass.HUNTER, HeroClass.WARRIOR);
		Player hunter = context.getPlayer1();

		// summon Ghaz'rilla
		MinionCard gahzrillaCard = (MinionCard) CardCatalogue.getCardById("minion_gahzrilla");
		Minion gahzrilla = playMinionCard(context, hunter, gahzrillaCard);
		Assert.assertEquals(gahzrilla.getAttack(), 6);
		Assert.assertEquals(gahzrilla.getHp(), 9);

		// buff it with 'Charge' spell
		Card chargeCard = CardCatalogue.getCardById("spell_charge");
		context.getLogic().receiveCard(hunter.getId(), chargeCard);
		GameAction action = chargeCard.play();
		action.setTarget(gahzrilla);
		context.getLogic().performGameAction(hunter.getId(), action);
		Assert.assertEquals(gahzrilla.getAttack(), 8);
		Assert.assertEquals(gahzrilla.getHp(), 9);

		// buff it with 'Cruel Taskmaster' spell
		Card cruelTaskmasterCard = CardCatalogue.getCardById("minion_cruel_taskmaster");
		context.getLogic().receiveCard(hunter.getId(), cruelTaskmasterCard);
		action = cruelTaskmasterCard.play();
		action.setTarget(gahzrilla);
		context.getLogic().performGameAction(hunter.getId(), action);
		Assert.assertEquals(gahzrilla.getAttack(), 20);
		Assert.assertEquals(gahzrilla.getHp(), 8);

		context.getLogic().destroy((Actor) find(context, "minion_cruel_taskmaster"));

		// buff it with 'Abusive Sergeant' spell
		Card abusiveSergeant = CardCatalogue.getCardById("minion_abusive_sergeant");
		context.getLogic().receiveCard(hunter.getId(), abusiveSergeant);
		action = abusiveSergeant.play();
		action.setTarget(gahzrilla);
		context.getLogic().performGameAction(hunter.getId(), action);
		Assert.assertEquals(gahzrilla.getAttack(), 22);
		Assert.assertEquals(gahzrilla.getHp(), 8);

		context.endTurn();
		context.endTurn();
		Assert.assertEquals(gahzrilla.getAttack(), 20);
		Assert.assertEquals(gahzrilla.getHp(), 8);
	}

	@Test
	public void testKnifeJugglerPlusStealth() {
		GameContext context = createContext(HeroClass.ROGUE, HeroClass.WARRIOR);
		Player player = context.getPlayer1();

		Minion knifeJuggler = playMinionCard(context, player, (MinionCard) CardCatalogue.getCardById("minion_knife_juggler"));
		playCard(context, player, CardCatalogue.getCardById("spell_conceal"));
		// knife juggler should be stealthed
		Assert.assertTrue(knifeJuggler.hasAttribute(Attribute.STEALTH));
		// knife juggler should be unstealthed as soon as another minion is
		// played and his trigger fires
		playCard(context, player, new TestMinionCard(1, 1));
		Assert.assertFalse(knifeJuggler.hasAttribute(Attribute.STEALTH));
	}

	@Test
	public void testSilenceWithBuffs() {
		GameContext context = createContext(HeroClass.WARLOCK, HeroClass.WARRIOR);
		Player player = context.getPlayer1();

		// summon attack target
		context.endTurn();
		Player opponent = context.getPlayer2();
		playCard(context, opponent, new TestMinionCard(4, 4, 0));
		context.endTurn();

		// summon test minion
		player.setMana(10);
		TestMinionCard minionCard = new TestMinionCard(6, 6, 0);
		playCard(context, player, minionCard);

		Actor minion = getSingleMinion(player.getMinions());

		// buff test minion
		SpellCard buffSpellCard = (SpellCard) CardCatalogue.getCardById("spell_bananas");
		context.getLogic().receiveCard(player.getId(), buffSpellCard);
		GameAction action = buffSpellCard.play();
		action.setTarget(minion);
		context.getLogic().performGameAction(player.getId(), action);

		Assert.assertEquals(minion.getAttack(), 7);
		Assert.assertEquals(minion.getHp(), 7);

		// attack target to get test minion wounded
		attack(context, player, minion, getSingleMinion(opponent.getMinions()));
		Assert.assertEquals(minion.getAttack(), 7);
		Assert.assertEquals(minion.getHp(), 3);

		// swap hp and attack of wounded test minion
		SpellDesc swapHpAttackSpell = SwapAttackAndHpSpell.create(EntityReference.FRIENDLY_MINIONS);
		SpellCard swapSpellCard = new TestSpellCard(swapHpAttackSpell);
		buffSpellCard.setTargetRequirement(TargetSelection.NONE);
		playCard(context, player, swapSpellCard);
		Assert.assertEquals(minion.getAttack(), 3);
		Assert.assertEquals(minion.getHp(), 7);

		// silence minion and check if it regains original stats
		SpellDesc silenceSpell = SilenceSpell.create(EntityReference.FRIENDLY_MINIONS);
		SpellCard silenceSpellCard = new TestSpellCard(silenceSpell);
		silenceSpellCard.setTargetRequirement(TargetSelection.NONE);
		playCard(context, player, silenceSpellCard);
		Assert.assertEquals(minion.getAttack(), 6);
		Assert.assertEquals(minion.getHp(), 6);
	}

	@Test
	public void testSwapWithBuffs() {
		GameContext context = createContext(HeroClass.WARLOCK, HeroClass.WARRIOR);
		Player player = context.getPlayer1();

		// summon test minion
		player.setMana(10);
		TestMinionCard minionCard = new TestMinionCard(1, 3, 0);
		playCard(context, player, minionCard);

		// buff test minion with temporary buff
		SpellDesc buffSpell = TemporaryAttackSpell.create(EntityReference.FRIENDLY_MINIONS, +4);
		SpellCard buffSpellCard = new TestSpellCard(buffSpell);
		buffSpellCard.setTargetRequirement(TargetSelection.NONE);
		playCard(context, player, buffSpellCard);

		Actor minion = getSingleMinion(player.getMinions());
		Assert.assertEquals(minion.getAttack(), 5);
		Assert.assertEquals(minion.getHp(), 3);

		// swap hp and attack of wounded test minion
		SpellDesc swapHpAttackSpell = SwapAttackAndHpSpell.create(EntityReference.FRIENDLY_MINIONS);
		SpellCard swapSpellCard = new TestSpellCard(swapHpAttackSpell);
		buffSpellCard.setTargetRequirement(TargetSelection.NONE);
		playCard(context, player, swapSpellCard);
		Assert.assertEquals(minion.getAttack(), 3);
		Assert.assertEquals(minion.getHp(), 5);

		// end turn; temporary buff wears off, but stats should still be the
		// same
		context.endTurn();
		Assert.assertEquals(minion.getAttack(), 3);
		Assert.assertEquals(minion.getHp(), 5);
	}

	@Test
	public void testWarriorCards() {
		GameContext context = createContext(HeroClass.WARRIOR, HeroClass.MAGE);
		Player warrior = context.getPlayer1();
		warrior.setMana(10);

		playCard(context, warrior, CardCatalogue.getCardById("weapon_arcanite_reaper"));
		playCard(context, warrior, new TestMinionCard(2, 1, 0));

		Minion bloodsailRaider = playMinionCard(context, warrior, (MinionCard) CardCatalogue.getCardById("minion_bloodsail_raider"));
		Assert.assertEquals(bloodsailRaider.getAttack(), 7);
	}

	@Test
	public void testWildPyroPlusEquality() {
		GameContext context = createContext(HeroClass.PALADIN, HeroClass.WARRIOR);
		Player paladin = context.getPlayer1();
		playCard(context, paladin, new TestMinionCard(3, 2, 0));
		playCard(context, paladin, new TestMinionCard(4, 4, 0));
		context.getLogic().endTurn(paladin.getId());

		Player warrior = context.getPlayer2();
		playCard(context, warrior, new TestMinionCard(5, 5, 0));
		playCard(context, warrior, new TestMinionCard(1, 2, 0));
		playCard(context, warrior, new TestMinionCard(8, 8, 0));
		playCard(context, warrior, new TestMinionCard(2, 1, 0));
		context.getLogic().endTurn(warrior.getId());

		Assert.assertEquals(paladin.getMinions().size(), 2);
		Assert.assertEquals(warrior.getMinions().size(), 4);

		playCard(context, paladin, CardCatalogue.getCardById("minion_wild_pyromancer"));
		playCard(context, paladin, CardCatalogue.getCardById("spell_equality"));

		// wild pyromancer + equality should wipe the board if there no
		// deathrattles
		Assert.assertEquals(paladin.getMinions().size(), 0);
		Assert.assertEquals(warrior.getMinions().size(), 0);
	}
	
	@Test
	public void testLordJaraxxus() {
		GameContext context = createContext(HeroClass.WARLOCK, HeroClass.PALADIN);
		Player warlock = context.getPlayer1();
		Card jaraxxus = CardCatalogue.getCardById("minion_lord_jaraxxus");
		// first, just play Jaraxxus on an empty board
		playCard(context, warlock, jaraxxus);
		Assert.assertEquals(warlock.getHero().getRace(), Race.DEMON);
		Assert.assertEquals(warlock.getHero().getHp(), 15);
		Assert.assertNotNull(warlock.getHero().getWeapon());
		
		// start a new game
		context = createContext(HeroClass.WARLOCK, HeroClass.PALADIN);
		// opponent plays Repentance, which triggers on Lord Jaraxxus play
		Player paladin = context.getPlayer2();
		Card repentance = CardCatalogue.getCardById("secret_repentance");
		playCard(context, paladin, repentance);
		
		context.getLogic().endTurn(paladin.getId());
		
		warlock = context.getPlayer1();
		jaraxxus = CardCatalogue.getCardById("minion_lord_jaraxxus");
		playCard(context, warlock, jaraxxus);
		Assert.assertEquals(warlock.getHero().getRace(), Race.DEMON);
		// Jaraxxus should be affected by Repentance, bringing him down to 1 hp
		Assert.assertEquals(warlock.getHero().getHp(), 1);
		Assert.assertNotNull(warlock.getHero().getWeapon());
	}

}
