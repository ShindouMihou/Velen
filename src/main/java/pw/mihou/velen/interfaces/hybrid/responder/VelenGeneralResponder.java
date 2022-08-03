package pw.mihou.velen.interfaces.hybrid.responder;

import org.javacord.api.entity.Icon;
import org.javacord.api.entity.Mentionable;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.MessageDecoration;
import org.javacord.api.entity.message.MessageFlag;
import org.javacord.api.entity.message.component.ActionRow;
import org.javacord.api.entity.message.component.HighLevelComponent;
import org.javacord.api.entity.message.component.LowLevelComponent;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.message.mention.AllowedMentions;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.interaction.callback.InteractionOriginalResponseUpdater;
import pw.mihou.velen.interfaces.hybrid.responder.internal.VelenGeneralRespond;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.EnumSet;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class VelenGeneralResponder implements VelenGeneralRespond<VelenGeneralResponder> {

    private final InteractionOriginalResponseUpdater updater;
    private final MessageCreateEvent event;
    private TextChannel channel;
    private final MessageBuilder builder;

    public VelenGeneralResponder(MessageCreateEvent event, InteractionOriginalResponseUpdater updater) {
        this.updater = updater;
        this.event = event;

        // We only want the message builder to be there if the event is there.
        this.builder = event != null ? new MessageBuilder() : null;
    }

    @Override
    public Optional<MessageBuilder> getMessageBuilder() {
        return Optional.ofNullable(builder);
    }

    @Override
    public Optional<InteractionOriginalResponseUpdater> getInteractionOriginalResponseUpdater() {
        return Optional.ofNullable(updater);
    }

    @Override
    public VelenGeneralResponder addComponents(HighLevelComponent... components) {
        if(builder == null)
            updater.addComponents(components);
        else
            builder.addComponents(components);

        return this;
    }

    @Override
    public VelenGeneralResponder addActionRow(LowLevelComponent... components) {
        if(builder == null)
            updater.addComponents(ActionRow.of(components));
        else
            builder.addActionRow(components);

        return this;
    }

    @Override
    public VelenGeneralResponder appendCode(String language, String code) {
        if(builder == null)
            updater.appendCode(language, code);
        else
            builder.appendCode(language, code);

        return this;
    }

    @Override
    public VelenGeneralResponder append(String message, MessageDecoration... decorations) {
        if(builder == null)
            updater.append(message, decorations);
        else
            builder.append(message, decorations);

        return this;
    }

    @Override
    public VelenGeneralResponder append(Mentionable entity) {
        if(builder == null)
            updater.append(entity);
        else
            builder.append(entity);

        return this;
    }

    @Override
    public VelenGeneralResponder append(Object object) {
        if(builder == null)
            updater.append(object);
        else
            builder.append(object);

        return this;
    }

    @Override
    public VelenGeneralResponder appendNewLine() {
        if(builder == null)
            updater.appendNewLine();
        else
            builder.appendNewLine();
        return this;
    }

    @Override
    public VelenGeneralResponder setContent(String content) {
        if(builder == null)
            updater.setContent(content);
        else
            builder.setContent(content);
        return this;
    }

    @Override
    public VelenGeneralResponder removeContent() {
        if(builder == null)
            updater.setContent("");
        else
            builder.removeContent();
        return this;
    }

    @Override
    public VelenGeneralResponder setEmbed(EmbedBuilder embed) {
        if(builder == null)
            updater.removeAllEmbeds().addEmbed(embed);
        else
            builder.setEmbed(embed);
        return this;
    }

    @Override
    public VelenGeneralResponder setEmbeds(EmbedBuilder... embeds) {
        if(builder == null)
            updater.removeAllEmbeds().addEmbeds(embeds);
        else
            builder.setEmbeds(embeds);
        return this;
    }

    @Override
    public VelenGeneralResponder addEmbed(EmbedBuilder embed) {
        if(builder == null)
            updater.addEmbed(embed);
        else
            builder.addEmbed(embed);
        return this;
    }

    @Override
    public VelenGeneralResponder addFile(BufferedImage image, String fileName) {
        if(builder == null)
            updater.addAttachment(image, fileName);
        else
            builder.addAttachment(image, fileName);
        return this;
    }

    @Override
    public VelenGeneralResponder addFile(File file) {
        if(builder == null)
            updater.addAttachment(file);
        else
            builder.addAttachment(file);
        return this;
    }

    @Override
    public VelenGeneralResponder addFile(Icon icon) {
        if(builder == null)
            updater.addAttachment(icon);
        else
            builder.addAttachment(icon);
        return this;
    }

    @Override
    public VelenGeneralResponder addFile(URL url) {
        if(builder == null)
            updater.addAttachment(url);
        else
            builder.addAttachment(url);
        return this;
    }

    @Override
    public VelenGeneralResponder addFile(byte[] bytes, String fileName) {
        if(builder == null)
            updater.addAttachment(bytes, fileName);
        else
            builder.addAttachment(bytes, fileName);
        return this;
    }

    @Override
    public VelenGeneralResponder addFile(InputStream stream, String fileName) {
        if(builder == null)
            updater.addAttachment(stream, fileName);
        else
            builder.addAttachment(stream, fileName);
        return this;
    }

    @Override
    public VelenGeneralResponder addFileAsSpoiler(BufferedImage image, String fileName) {
        if(builder == null)
            updater.addAttachmentAsSpoiler(image, fileName);
        else
            builder.addAttachmentAsSpoiler(image, fileName);
        return this;
    }

    @Override
    public VelenGeneralResponder addFileAsSpoiler(File file) {
        if(builder == null)
            updater.addAttachmentAsSpoiler(file);
        else
            builder.addAttachmentAsSpoiler(file);
        return this;
    }

    @Override
    public VelenGeneralResponder addFileAsSpoiler(Icon icon) {
        if(builder == null)
            updater.addAttachmentAsSpoiler(icon);
        else
            builder.addAttachmentAsSpoiler(icon);
        return this;
    }

    @Override
    public VelenGeneralResponder addFileAsSpoiler(URL url) {
        if(builder == null)
            updater.addAttachmentAsSpoiler(url);
        else
            builder.addAttachmentAsSpoiler(url);
        return this;
    }

    @Override
    public VelenGeneralResponder addFileAsSpoiler(byte[] bytes, String fileName) {
        if(builder == null)
            updater.addAttachmentAsSpoiler(bytes, fileName);
        else
            builder.addAttachmentAsSpoiler(bytes, fileName);
        return this;
    }

    @Override
    public VelenGeneralResponder addFileAsSpoiler(InputStream stream, String fileName) {
        if(builder == null)
            updater.addAttachmentAsSpoiler(stream, fileName);
        else
            builder.addAttachmentAsSpoiler(stream, fileName);
        return this;
    }

    @Override
    public VelenGeneralResponder addAttachment(BufferedImage image, String fileName) {
        if(builder == null)
            updater.addAttachment(image, fileName);
        else
            builder.addAttachment(image, fileName);
        return this;
    }

    @Override
    public VelenGeneralResponder addAttachment(File file) {
        if(builder == null)
            updater.addAttachment(file);
        else
            builder.addAttachment(file);
        return this;
    }

    @Override
    public VelenGeneralResponder addAttachment(Icon icon) {
        if(builder == null)
            updater.addAttachment(icon);
        else
            builder.addAttachment(icon);
        return this;
    }

    @Override
    public VelenGeneralResponder addAttachment(URL url) {
        if(builder == null)
            updater.addAttachment(url);
        else
            builder.addAttachment(url);
        return this;
    }

    @Override
    public VelenGeneralResponder addAttachment(byte[] bytes, String fileName) {
        if(builder == null)
            updater.addAttachment(bytes, fileName);
        else
            builder.addAttachment(bytes, fileName);
        return this;
    }

    @Override
    public VelenGeneralResponder addAttachment(InputStream stream, String fileName) {
        if(builder == null)
            updater.addAttachment(stream, fileName);
        else
            builder.addAttachment(stream, fileName);
        return this;
    }

    @Override
    public VelenGeneralResponder addAttachmentAsSpoiler(BufferedImage image, String fileName) {
        if(builder == null)
            updater.addAttachmentAsSpoiler(image, fileName);
        else
            builder.addAttachmentAsSpoiler(image, fileName);
        return this;
    }

    @Override
    public VelenGeneralResponder addAttachmentAsSpoiler(File file) {
        if(builder == null)
            updater.addAttachmentAsSpoiler(file);
        else
            builder.addAttachmentAsSpoiler(file);
        return this;
    }

    @Override
    public VelenGeneralResponder addAttachmentAsSpoiler(Icon icon) {
        if(builder == null)
            updater.addAttachmentAsSpoiler(icon);
        else
            builder.addAttachmentAsSpoiler(icon);
        return this;
    }

    @Override
    public VelenGeneralResponder addAttachmentAsSpoiler(URL url) {
        if(builder == null)
            updater.addAttachmentAsSpoiler(url);
        else
            builder.addAttachmentAsSpoiler(url);
        return this;
    }

    @Override
    public VelenGeneralResponder addAttachmentAsSpoiler(byte[] bytes, String fileName) {
        if(builder == null)
            updater.addAttachmentAsSpoiler(bytes, fileName);
        else
            builder.addAttachmentAsSpoiler(bytes, fileName);
        return this;
    }

    @Override
    public VelenGeneralResponder addAttachmentAsSpoiler(InputStream stream, String fileName) {
        if(builder == null)
            updater.addAttachmentAsSpoiler(stream, fileName);
        else
            builder.addAttachmentAsSpoiler(stream, fileName);
        return this;
    }

    @Override
    public VelenGeneralResponder setAllowedMentions(AllowedMentions allowedMentions) {
        if(builder == null)
            updater.setAllowedMentions(allowedMentions);
        else
            builder.setAllowedMentions(allowedMentions);
        return this;
    }

    @Override
    public VelenGeneralResponder removeAllComponents() {
        if(builder == null)
            updater.removeAllComponents();
        else
            builder.removeAllComponents();
        return this;
    }

    @Override
    public VelenGeneralResponder addEmbeds(EmbedBuilder... embeds) {
        if(builder == null)
            updater.addEmbeds(embeds);
        else
            builder.addEmbeds(embeds);
        return this;
    }

    @Override
    public VelenGeneralResponder removeEmbed(EmbedBuilder embed) {
        if(builder == null)
            updater.removeEmbed(embed);
        else
            builder.removeEmbed(embed);
        return this;
    }

    @Override
    public VelenGeneralResponder removeEmbeds(EmbedBuilder... embeds) {
        if(builder == null)
            updater.removeEmbeds(embeds);
        else
            builder.removeEmbeds(embeds);
        return this;
    }

    @Override
    public VelenGeneralResponder removeAllEmbeds() {
        if(builder == null)
            updater.removeAllEmbeds();
        else
            builder.removeAllEmbeds();
        return this;
    }

    /**
     * Sets the nonce of the message, this only works if the event was from a
     * Message Command.
     *
     * @param nonce The nonce to set.
     * @return The current instance in order to chain call methods.
     */
    @Override
    public VelenGeneralResponder setNonce(String nonce) {
        if(builder != null)
            builder.setNonce(nonce);
        return this;
    }

    @Override
    public VelenGeneralResponder setFlags(EnumSet<MessageFlag> messageFlags) {
        if(updater != null)
            updater.setFlags(messageFlags);

        return this;
    }

    @Override
    public VelenGeneralResponder setFlags(MessageFlag... messageFlags) {
        if(updater != null)
            updater.setFlags(messageFlags);

        return this;
    }

    /**
     * Modifies the respond channel for this responder, by default, this will send to
     * whichever channel triggered this event, this only works for Message Commands.
     *
     * @param channel The channel where this responder should respond to.
     * @return The current instance in order to chain call methods.
     */
    public VelenGeneralResponder respondTo(TextChannel channel) {
        this.channel = channel;

        return this;
    }

    @Override
    public CompletableFuture<Message> respond() {
        if(builder == null)
            return updater.update();
        else
            if(channel != null)
                return builder.send(channel);
            else
                return builder.send(event.getChannel());
    }
}
