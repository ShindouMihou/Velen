package pw.mihou.velen.utils.invitebuilder;

public enum InviteScope {

    IDENTIFY("identify"),
    EMAIL("email"),
    CONENCTIONS("connections"),
    GUILDS("guilds"),
    GUILDS_JOIN("guilds.join"),
    GUILDS_MEMBERS_READ("guilds.members.read"),
    GDM_JOIN("gdm.join"),
    RPC("rpc"),
    RPC_NOTIFICATIONS_READ("rpc.notifications.read"),
    RPC_VOICE_READ("rpc.voice.read"),
    RPC_VOICE_WRITE("rpc.voice.write"),
    RPC_ACTIVITIES_WRITE("rpc.activities.write"),
    BOT("bot", false),
    WEBHOOK_INCOMING("webhook.incoming"),
    MESSAGES_READ("messages.read"),
    APPLICATIONS_BUILDS_UPLOAD("applications.builds.upload"),
    APPLICATIONS_BUILDS_READ("applications.builds.read"),
    APPLICATIONS_COMMANDS("applications.commands", false),
    APPLICATIONS_COMMANDS_PERMISSIONS_UPDATE("applications.commands.permissions.update"),
    APPLICATIONS_STORE_UPDATE("applications.store.update"),
    APPLICATIONS_ENTITLEMENTS("applications.entitlements"),
    ACTIVITIES_READ("activities.read"),
    ACTIVITES_WRITE("activities.write"),
    RELATIONSHIPS_READ("relationships.read");

    private final String scope;
    private final boolean requires_redirect;

    InviteScope(String scope) {
        this.scope = scope;
        this.requires_redirect = true;
    }

    InviteScope(String scope, boolean requires_redirect) {
        this.scope = scope;
        this.requires_redirect = requires_redirect;
    }

    /**
     * Gets the query paramater value for this scope.
     *
     * @return The parameter for the query value of this scope.
     */
    public String getScope() {
        return scope;
    }

    /**
     * Does this scope requires a redirect uri?
     *
     * @return Does this scope requires a redirect uri?
     */
    public boolean requiresRedirect() {
        return requires_redirect;
    }
}
