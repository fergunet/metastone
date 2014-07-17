package net.pferdimanzug.hearthstone.analyzer.game.cards.concrete.paladin;

import net.pferdimanzug.hearthstone.analyzer.game.cards.Rarity;
import net.pferdimanzug.hearthstone.analyzer.game.cards.SpellCard;
import net.pferdimanzug.hearthstone.analyzer.game.entities.heroes.HeroClass;
import net.pferdimanzug.hearthstone.analyzer.game.spells.HealingSpell;
import net.pferdimanzug.hearthstone.analyzer.game.targeting.TargetSelection;

public class HolyLight extends SpellCard {

	public HolyLight() {
		super("Holy Light", Rarity.FREE, HeroClass.PALADIN, 2);
		setDescription("Restore #6 Health.");
		setTargetRequirement(TargetSelection.ANY);
		setSpell(new HealingSpell(6));
	}



	@Override
	public int getTypeId() {
		return 248;
	}
}
