package pw.mihou.velen.interfaces.hybrid.responder;

import org.javacord.api.entity.Icon;
import org.javacord.api.entity.Mentionable;
import org.javacord.api.entity.message.MessageDecoration;
import org.javacord.api.entity.message.MessageFlag;
import org.javacord.api.entity.message.component.HighLevelComponent;
import org.javacord.api.entity.message.component.LowLevelComponent;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.message.mention.AllowedMentions;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.EnumSet;

/**
 * Copied from Javacord's MessageBuilderBase but with
 * a twist of interface.
 *
 * @param <T> The type to return.
 */
public interface VelenResponderBase<T> {


    /**
     * Add multiple high level components to the message.
     *
     * @param components The high level components.
     * @return The current instance in order to chain call methods.
     */
    T addComponents(HighLevelComponent... components);

    /**
     * Add multiple low level components, wrapped in an ActionRow, to the message.
     *
     * @param components The low level components.
     * @return The current instance in order to chain call methods.
     */
    T addActionRow(LowLevelComponent... components);

    /**
     * Appends code to the message.
     *
     * @param language The language, e.g. "java".
     * @param code     The code.
     * @return The current instance in order to chain call methods.
     */
    T appendCode(String language, String code);

    /**
     * Appends a sting with or without decoration to the message.
     *
     * @param message The string to append.
     * @param decorations The decorations of the string.
     * @return The current instance in order to chain call methods.
     */
    T append(String message, MessageDecoration... decorations);

    /**
     * Appends a mentionable entity (usually a user or channel) to the message.
     *
     * @param entity The entity to mention.
     * @return The current instance in order to chain call methods.
     */
    T append(Mentionable entity);

    /**
     * Appends the string representation of the object (calling {@link String#valueOf(Object)} method) to the message.
     *
     * @param object The object to append.
     * @return The current instance in order to chain call methods.
     * @see StringBuilder#append(Object)
     */
    T append(Object object);

    /**
     * Appends a new line to the message.
     *
     * @return The current instance in order to chain call methods.
     */
    T appendNewLine();

    /**
     * Sets the content of the message.
     * This method overwrites all previous content changes
     * (using {@link #append(String, MessageDecoration...)} for example).
     *
     * @param content The new content of the message.
     * @return The current instance in order to chain call methods.
     */
     T setContent(String content);

    /**
     * Removes the content of the message.
     * This method overwrites all previous content changes
     * (using {@link #append(String, MessageDecoration...)} for example).
     *
     * @return The current instance in order to chain call methods.
     */
    T removeContent();


    /**
     * Sets the embed of the message (overrides all existing embeds).
     *
     * @param embed The embed to set.
     * @return The current instance in order to chain call methods.
     */
    T setEmbed(EmbedBuilder embed);

    /**
     * Sets multiple embeds of the message (overrides all existing embeds).
     *
     * @param embeds The embed to set.
     * @return The current instance in order to chain call methods.
     */
    T setEmbeds(EmbedBuilder... embeds);

    /**
     * Adds an embed to the message.
     *
     * @param embed The embed to add.
     * @return The current isntance in order to chain call methods.
     */
     T addEmbed(EmbedBuilder embed);

    /**
     * Adds a file to the message.
     *
     * @param image The image to add as an attachment.
     * @param fileName The file name of the image.
     * @return The current instance in order to chain call methods.
     * @see #addAttachment(BufferedImage, String)
     */
    T addFile(BufferedImage image, String fileName);

    /**
     * Adds a file to the message.
     *
     * @param file The file to add as an attachment.
     * @return The current instance in order to chain call methods.
     * @see #addAttachment(File)
     */
    T addFile(File file);

    /**
     * Adds a file to the message.
     *
     * @param icon The icon to add as an attachment.
     * @return The current instance in order to chain call methods.
     * @see #addAttachment(Icon)
     */
    T addFile(Icon icon);
    /**
     * Adds a file to the message and marks it as a spoiler.
     *
     * @param url The url of the attachment.
     * @return The current instance in order to chain call methods.
     * @see #addAttachment(URL)
     */
    T addFile(URL url);
    /**
     * Adds a file to the message.
     *
     * @param bytes The bytes of the file.
     * @param fileName The name of the file.
     * @return The current instance in order to chain call methods.
     * @see #addAttachment(byte[], String)
     */
    T addFile(byte[] bytes, String fileName);
    /**
     * Adds a file to the message.
     *
     * @param stream The stream of the file.
     * @param fileName The name of the file.
     * @return The current instance in order to chain call methods.
     * @see #addAttachment(InputStream, String)
     */
    T addFile(InputStream stream, String fileName);
    /**
     * Adds a file to the message and marks it as spoiler.
     *
     * @param image The image to add as an attachment.
     * @param fileName The file name of the image.
     * @return The current instance in order to chain call methods.
     * @see #addAttachmentAsSpoiler(BufferedImage, String)
     */
    T addFileAsSpoiler(BufferedImage image, String fileName);
    /**
     * Adds a file to the message and marks it as spoiler.
     *
     * @param file The file to add as an attachment.
     * @return The current instance in order to chain call methods.
     * @see #addAttachmentAsSpoiler(File)
     */
    T addFileAsSpoiler(File file);
    /**
     * Adds a file to the message and marks it as spoiler.
     *
     * @param icon The icon to add as an attachment.
     * @return The current instance in order to chain call methods.
     * @see #addAttachmentAsSpoiler(Icon)
     */
    T addFileAsSpoiler(Icon icon);
    /**
     * Adds a file to the message and marks it as a spoiler.
     *
     * @param url The url of the attachment.
     * @return The current instance in order to chain call methods.
     * @see #addAttachment(URL)
     */
    T addFileAsSpoiler(URL url);
    /**
     * Adds a file to the message and marks it as spoiler.
     *
     * @param bytes The bytes of the file.
     * @param fileName The name of the file.
     * @return The current instance in order to chain call methods.
     * @see #addAttachmentAsSpoiler(byte[], String)
     */
    T addFileAsSpoiler(byte[] bytes, String fileName);
    /**
     * Adds a file to the message and marks it as spoiler.
     *
     * @param stream The stream of the file.
     * @param fileName The name of the file.
     * @return The current instance in order to chain call methods.
     * @see #addAttachment(InputStream, String)
     */
    T addFileAsSpoiler(InputStream stream, String fileName);
    /**
     * Adds an attachment to the message.
     *
     * @param image The image to add as an attachment.
     * @param fileName The file name of the image.
     * @return The current instance in order to chain call methods.
     */
    T addAttachment(BufferedImage image, String fileName);
    /**
     * Adds an attachment to the message.
     *
     * @param file The file to add as an attachment.
     * @return The current instance in order to chain call methods.
     */
    T addAttachment(File file);
    /**
     * Adds an attachment to the message.
     *
     * @param icon The icon to add as an attachment.
     * @return The current instance in order to chain call methods.
     */
    T addAttachment(Icon icon);
    /**
     * Adds an attachment to the message.
     *
     * @param url The url of the attachment.
     * @return The current instance in order to chain call methods.
     */
    T addAttachment(URL url);
    /**
     * Adds an attachment to the message.
     *
     * @param bytes The bytes of the file.
     * @param fileName The name of the file.
     * @return The current instance in order to chain call methods.
     */
    T addAttachment(byte[] bytes, String fileName);
    /**
     * Adds an attachment to the message.
     *
     * @param stream The stream of the file.
     * @param fileName The name of the file.
     * @return The current instance in order to chain call methods.
     */
    T addAttachment(InputStream stream, String fileName);
    /**
     * Adds an attachment to the message and marks it as spoiler.
     *
     * @param image The image to add as an attachment.
     * @param fileName The file name of the image.
     * @return The current instance in order to chain call methods.
     */
    T addAttachmentAsSpoiler(BufferedImage image, String fileName);
    /**
     * Adds an attachment to the message and marks it as spoiler.
     *
     * @param file The file to add as an attachment.
     * @return The current instance in order to chain call methods.
     */
    T addAttachmentAsSpoiler(File file);
    /**
     * Adds an attachment to the message and marks it as spoiler.
     *
     * @param icon The icon to add as an attachment.
     * @return The current instance in order to chain call methods.
     */
    T addAttachmentAsSpoiler(Icon icon);
    /**
     * Adds an attachment to the message and marks it as spoiler.
     *
     * @param url The url of the attachment.
     * @return The current instance in order to chain call methods.
     */
    T addAttachmentAsSpoiler(URL url);
    /**
     * Adds an attachment to the message and marks it as spoiler.
     *
     * @param bytes The bytes of the file.
     * @param fileName The name of the file.
     * @return The current instance in order to chain call methods.
     */
    T addAttachmentAsSpoiler(byte[] bytes, String fileName);
    /**
     * Adds an attachment to the message and marks it as spoiler.
     *
     * @param stream The stream of the file.
     * @param fileName The name of the file.
     * @return The current instance in order to chain call methods.
     */
    T addAttachmentAsSpoiler(InputStream stream, String fileName);
    /**
     * Controls who will be mentioned if mentions exist in the message.
     *
     * @param allowedMentions The mention object.
     * @return The current instance in order to chain call methods.
     */
    T setAllowedMentions(AllowedMentions allowedMentions);
    /**
     * Remove all high-level components from the message.
     *
     * @return The current instance in order to chain call methods.
     */
    T removeAllComponents();
    /**
     * Adds the embeds to the message.
     *
     * @param embeds The embeds to add.
     * @return The current instance in order to chain call methods.
     */
    T addEmbeds(EmbedBuilder... embeds);
    /**
     * Removes the embed from the message.
     *
     * @param embed The embed to remove.
     * @return The current instance in order to chain call methods.
     */
    T removeEmbed(EmbedBuilder embed);
    /**
     * Removes the embeds from the message.
     *
     * @param embeds The embeds to remove.
     * @return The current instance in order to chain call methods.
     */
    T removeEmbeds(EmbedBuilder... embeds);
    /**
     * Removes all embeds from the message.
     *
     * @return The current instance in order to chain call methods.
     */
    T removeAllEmbeds();
    /**
     * Sets the nonce of the message.
     *
     * @param nonce The nonce to set.
     * @return The current instance in order to chain call methods.
     */
    T setNonce(String nonce);

    /**
     * Sets the message flags of the message, this only works if the event was
     * from a Slash Command.
     *
     * @param messageFlags The message flag of the message.
     */
    T setFlags(EnumSet<MessageFlag> messageFlags);

    /**
     * Sets the message flags of the message, this only works if the event was
     * from a Slash Command.
     *
     * @param messageFlags The message flag of the message.
     */
    T setFlags(MessageFlag... messageFlags);
}
