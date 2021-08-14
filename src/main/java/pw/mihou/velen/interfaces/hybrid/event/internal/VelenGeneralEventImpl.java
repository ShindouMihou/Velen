package pw.mihou.velen.interfaces.hybrid.event.internal;

import org.javacord.api.event.interaction.SlashCommandCreateEvent;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.interaction.callback.InteractionOriginalResponseUpdater;
import pw.mihou.velen.interfaces.hybrid.event.VelenGeneralEvent;
import pw.mihou.velen.interfaces.hybrid.objects.VelenHybridArguments;

import java.util.Objects;
import java.util.Optional;

public class VelenGeneralEventImpl implements VelenGeneralEvent {

    private final InteractionOriginalResponseUpdater updater;
    private final SlashCommandCreateEvent slashEvent;
    private final MessageCreateEvent event;
    private final String command;
    private final VelenHybridArguments arguments;

    public VelenGeneralEventImpl(String command, SlashCommandCreateEvent slashEvent, MessageCreateEvent event, String[] args) {
        this.command = command;
        VelenHybridArguments a = null;

        this.slashEvent = slashEvent;
        if(slashEvent != null)
            this.updater = slashEvent.getSlashCommandInteraction().respondLater().join();
        else
            this.updater = null;

        this.event = event;

        if(args != null && event != null)
            if(args.length == 0)
                a = new VelenHybridArguments(new String[0], event.getApi());
            else
                a = new VelenHybridArguments(args, event.getApi());

        if(slashEvent != null)
            a = new VelenHybridArguments(slashEvent.getSlashCommandInteraction().getOptions());

        this.arguments = Objects.requireNonNull(a);
    }

    @Override
    public String getCommandName() {
        return command;
    }

    @Override
    public Optional<SlashCommandCreateEvent> asSlashEvent() {
        return Optional.ofNullable(slashEvent);
    }

    @Override
    public Optional<InteractionOriginalResponseUpdater> getUpdater() {
        return Optional.ofNullable(updater);
    }

    @Override
    public Optional<MessageCreateEvent> asMessageEvent() {
        return Optional.ofNullable(event);
    }

    @Override
    public VelenHybridArguments getArguments() {
        return arguments;
    }
}
