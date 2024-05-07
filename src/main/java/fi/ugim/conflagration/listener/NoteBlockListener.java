package fi.ugim.conflagration.listener;

import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.NotifyNeighborBlockEvent;

public class NoteBlockListener {

    @Listener
    public void onInstrumentChange(NotifyNeighborBlockEvent event) {
        event.filterTickets(ticket -> ticket.target().state().type() == BlockTypes.NOTE_BLOCK.get());
    }

}
