package net.demilich.metastone.game.spells.trigger;

import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.events.GameEventType;
import net.demilich.metastone.game.events.TurnEndEvent;
import net.demilich.metastone.game.spells.TargetPlayer;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;

public class TurnEndTrigger extends GameEventTrigger {

	public TurnEndTrigger() {
		this(EventTriggerDesc.createEmpty(TurnEndTrigger.class));
	}

	public TurnEndTrigger(EventTriggerDesc desc) {
		super(desc);
	}

	@Override
	protected boolean fire(GameEvent event, Entity host) {
		TurnEndEvent turnEndEvent = (TurnEndEvent) event;
		
		TargetPlayer targetPlayer = desc.getTargetPlayer();
		int targetPlayerId = turnEndEvent.getPlayerId();
		if (targetPlayer != null) {
			return determineTargetPlayer(turnEndEvent, targetPlayer, host, targetPlayerId);
		}

		return true;
	}

	@Override
	public GameEventType interestedIn() {
		return GameEventType.TURN_END;
	}

}
