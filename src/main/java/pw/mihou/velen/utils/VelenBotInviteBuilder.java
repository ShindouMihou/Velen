package pw.mihou.velen.utils;

import org.javacord.api.BotInviteBuilder;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.permission.Permissions;
import org.javacord.api.entity.permission.PermissionsBuilder;
import pw.mihou.velen.utils.invitebuilder.InviteScope;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class VelenBotInviteBuilder {

    private final long clientId;
    public static final String BASE_LINK = "https://discord.com/oauth2/authorize?client_id=";
    private String redirect_uri = "";
    private final List<InviteScope> inviteScopes = new ArrayList<>();
    private Permissions permissions = new PermissionsBuilder().build();
    private boolean consent = true;

    /**
     * Creates a new Velen Invite Builder that is more of an extension to
     * {@link BotInviteBuilder}, allowing bot developers to create bot invite links.
     *
     * @param clientId The client ID to use.
     */
    public VelenBotInviteBuilder(long clientId) {
        this.clientId = clientId;
    }

    /**
     * Sets the permissions for this invite builder, this will override
     * the previous permissions set if there is any.
     *
     * @param permissions The permissions to use.
     * @return {@link VelenBotInviteBuilder} for chain-calling methods.
     */
    public VelenBotInviteBuilder setPermissions(Permissions permissions) {
        this.permissions = permissions;
        return this;
    }

    /**
     * Should Velen append prompt consent to the invite links, by default, this is enabled
     * since it has no other side effects while allowing server owners to be able to reauthorize
     * scopes of the bot. For example, if the bot is missing {@link InviteScope#APPLICATIONS_COMMANDS}.
     *
     * @param promptConsent Read above.
     * @return {@link VelenBotInviteBuilder} for chain-calling methods.
     */
    public VelenBotInviteBuilder setPromptConsent(boolean promptConsent) {
        this.consent = promptConsent;
        return this;
    }

    /**
     * Sets the permissions for this invite builder, this will override
     * the previous permissions set if there is any.
     *
     * @param permissions The permissions to use.
     * @return {@link VelenBotInviteBuilder} for chain-calling methods.
     */
    public VelenBotInviteBuilder setPermissions(PermissionType... permissions) {
        this.permissions = new PermissionsBuilder()
                .setAllowed(permissions).build();
        return this;
    }

    /**
     * Sets the redirect uri for ths invite builder, this will override any
     * previous redirect uri that was set.
     *
     * A redirect uri will be needed for all except two scopes, Velen will throw
     * an {@link IllegalStateException} if the redirect uri is not set for all except
     * those two scopes.
     *
     * @param redirect_uri The redirect URI to use.
     * @return {@link VelenBotInviteBuilder} for chain-calling methods.
     */
    public VelenBotInviteBuilder setRedirectURI(String redirect_uri) {
        this.redirect_uri = redirect_uri;
        return this;
    }

    /**
     * Adds all the scopes specified on this method. If there are no scopes specified during
     * build time, Velen will automatically append both {@link InviteScope#BOT} and {@link InviteScope#APPLICATIONS_COMMANDS} scopes to ensure
     * that your bot will function even when you migrate from slash commands or so forth.
     *
     * You may use this to override the defaults and set what you want instead.
     *
     * @param scopes The scopes to set.
     * @return {@link VelenBotInviteBuilder} for chain-calling methods.
     */
    public VelenBotInviteBuilder addScopes(InviteScope... scopes) {
        this.inviteScopes.addAll(Arrays.asList(scopes));
        return this;
    }

    /**
     * This creates a new invite URI using the specifications
     * provided. <br><br>
     *
     * If there are no scopes specified, Velen will append {@link InviteScope#BOT} and {@link InviteScope#APPLICATIONS_COMMANDS}
     * to ensure that your bot will function properly even if you wish to move from slash to message or message to slash.
     *
     * <br>
     * Some scopes will require a Redirect URI and Velen will throw an exception if you place one without setting
     * {@link VelenBotInviteBuilder#setRedirectURI(String)} beforehand.
     * <br>
     * You may also have to add the Redirect URI to your Developers Portal if you haven't.
     *
     *
     * @throws IllegalStateException This is thrown when a scope requiring Redirect URI is added
     * but the invite builder doesn't have any Redirect URI specified.
     * @return The generated invite url.
     */
    public String create() {
        if (inviteScopes.stream().anyMatch(InviteScope::requiresRedirect) && (redirect_uri.isEmpty() || redirect_uri == null))
            throw new IllegalStateException("You have added a scope that requires a redirect_uri, please specify a redirect uri using setRedirectUri.");

        if (inviteScopes.isEmpty())
            inviteScopes.addAll(Arrays.asList(InviteScope.BOT, InviteScope.APPLICATIONS_COMMANDS));

        StringBuilder builder = new StringBuilder(BASE_LINK);
        builder.append(clientId)
                .append("&scopes=")
                .append(inviteScopes.stream().map(InviteScope::getScope).collect(Collectors.joining("%20")))
                .append("&permissions=")
                .append(permissions.getAllowedBitmask());

        // We will only need redirect uri if an intent requires it.
        // since Discord will just ignore it.
        if (inviteScopes.stream().anyMatch(InviteScope::requiresRedirect) && !(redirect_uri.isEmpty() || redirect_uri == null)) {
            try {
                builder.append("&redirect_uri=").append(URLEncoder.encode(redirect_uri, "UTF-8"))
                        .append("&response_type=code");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                throw new IllegalStateException("Attempt to perform URL Encoding for " + redirect_uri + " failed with: " + e.getMessage());
            }
        }

        if (consent)
            builder.append("&prompt=consent");

        return builder.toString();
    }
    

}
