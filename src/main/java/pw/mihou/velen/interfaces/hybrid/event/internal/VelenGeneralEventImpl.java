package pw.mihou.velen.interfaces.hybrid.event.internal;

import org.javacord.api.event.interaction.SlashCommandCreateEvent;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.interaction.callback.InteractionOriginalResponseUpdater;
import pw.mihou.velen.impl.VelenCommandImpl;
import pw.mihou.velen.interfaces.VelenCommand;
import pw.mihou.velen.interfaces.hybrid.event.VelenGeneralEvent;
import pw.mihou.velen.interfaces.hybrid.objects.VelenHybridArguments;
import pw.mihou.velen.internals.routing.VelenRoutedArgument;
import pw.mihou.velen.internals.routing.VelenUnderscoreParser;
import pw.mihou.velen.utils.VelenUtils;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

public class VelenGeneralEventImpl implements VelenGeneralEvent {

    private final InteractionOriginalResponseUpdater updater;
    private final SlashCommandCreateEvent slashEvent;
    private final MessageCreateEvent event;
    private final String command;
    private final VelenHybridArguments arguments;
    private final VelenCommand vl;

    public VelenGeneralEventImpl(String command, SlashCommandCreateEvent slashEvent, MessageCreateEvent event, String[] args, VelenCommand vl) {
        this.command = command;
        this.vl = vl;
        VelenHybridArguments a = null;

        this.slashEvent = slashEvent;
        if(slashEvent != null)
            this.updater = slashEvent.getSlashCommandInteraction().respondLater().join();
        else
            this.updater = null;

        this.event = event;

        if(args != null && event != null) {
            String[] commandIndexes = Stream.concat(Arrays.stream(
                    new String[]{event.getMessageContent().split(" ")[0]}),
                            Arrays.stream(VelenUtils.splitContent(event.getMessageContent()))
                            .filter(s -> !s.equals(event.getMessageContent().split(" ")[0])))
                    .toArray(String[]::new);
            VelenRoutedArgument[] vArgs = VelenUnderscoreParser.route(event.getMessageContent(), ((VelenCommandImpl)vl).getFormats())
                    .entrySet()
                    .stream()
                    .map(entry -> new VelenRoutedArgument(entry.getKey(), entry.getValue().getLeft(),
                            entry.getValue().getRight() == null ? commandIndexes[entry.getKey()] :  entry.getValue().getRight()))
                    .toArray(VelenRoutedArgument[]::new);

            if (args.length == 0)
                a = new VelenHybridArguments(new VelenRoutedArgument[0], event.getApi(), vl);
            else
                a = new VelenHybridArguments(vArgs, event.getApi(), vl);
        }

        if(slashEvent != null)
            a = new VelenHybridArguments(slashEvent.getSlashCommandInteraction().getOptions(), vl);

        this.arguments = Objects.requireNonNull(a);
    }


    public VelenCommand getCommand() {
        return vl;
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
