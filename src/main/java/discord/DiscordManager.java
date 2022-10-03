package discord;

import java.util.EnumSet;
import java.util.logging.Logger;
import javax.security.auth.login.LoginException;
import org.bukkit.Bukkit;

import commands.discords.RegisterCommand;
import commands.discords.ServerCommand;
import configs.ConfigManager;
import events.discords.OnUserConfirm;
import main.WhitelistJe;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.InviteAction;

public class DiscordManager {
    public WhitelistJe main;
    public JDA jda;

    private Logger logger;
    private String servername = "Discord® Server";
    private boolean isPrivateBot = true;
    private String guildId;
    private String inviteUrl;
	private Guild guild;
    static private ConfigManager Configs = new ConfigManager();

    public DiscordManager(WhitelistJe main) {
        this.logger = Logger.getLogger("WJE:" + this.getClass().getSimpleName());
        this.main = main;
        this.connect();
        if(this.isPrivateBot) {this.checkGuild();}
        this.setupCommands();
        this.setupListener();
    }

    public void connect() {
        try {
            jda = JDABuilder.create(Configs.get("discordBotToken", null),
                    EnumSet.allOf(GatewayIntent.class))
                    .build()
                    .awaitReady();

            if (jda == null) {
                throw new LoginException("Cannot initialize JDA");
            }
        } catch (LoginException | InterruptedException e) {
            e.printStackTrace();
            Bukkit.shutdown();
        }
    }

    private String setInvite() {
        InviteAction invite = jda.getGuildById(this.guildId).getTextChannels().get(0).createInvite();
        return invite.setMaxAge(0)
        .complete().getUrl();
    }

    public String getServerName() {
        return this.servername;
    }

    private void checkGuild() {
        String guildId;
        try {
            if(jda.getGuilds().size() > 1) {
                this.logger.warning("Discord bot's tokken already in use on another DS !!!");
            }

            guildId = Configs.get("discordServerId", null);
            this.guild = jda.getGuildById(guildId);
            this.servername = this.guild.getName();
            this.guildId = this.guild.getId();
            this.inviteUrl = this.setInvite();
        } catch (Exception e) {
            this.logger.warning("Discord bot's not authorized into this guild. (Check: " + Configs.getClass().getSimpleName() +")");
        }
    }

    private void setupListener() {
        jda.addEventListener(new OnUserConfirm(main));
    }

    private void setupCommands() {
        try {
            // Serveer
            final String srvCmd = this.main.getConfigManager().get("serverCmdName", "server");
            jda.addEventListener(new ServerCommand(main));
            jda.upsertCommand(srvCmd, "Afficher les informations du serveur `Minecraft®`")
            .queue();

            // Register
            final String rgstrCmd = this.main.getConfigManager().get("registerCmdName", "register");
            jda.addEventListener(new RegisterCommand(main));
            jda.upsertCommand(rgstrCmd, "S'enregister sur le serveur")
            .addOption(OptionType.STRING, "pseudo", "Votre pseudo `Minecraft®`", true)
            .queue();
    
            // // Whitelist
            // jda.addEventListener(new WhitelistCommand(main));
            // jda.upsertCommand("whitelist", "Commande whitelist du
            // serveur").addOption(OptionType.STRING,
            // "action", "add/remove/on/off",true).addOption(OptionType.STRING, "pseudo", "Pseudo du joueur", false)
            // .queue();
    
            // // Deny
            // jda.addEventListener(new DenyCommand(main));
            // jda.upsertCommand("deny", "Refuser l'accès au serveur (réservé au
            // staff)").addOption(OptionType.MENTIONABLE,
            // "mention", "Mentionner un membre", true).queue();
            
        } catch (Exception e) {
            this.logger.warning("Failed to initialize DS commands correctly");
        }
    }

    public void disconnect() {
        if (jda != null)
            jda.shutdownNow();
    }

    public String getInviteUrl() {
        return this.inviteUrl;
    }

	public Guild getGuild() {
		return this.guild;
	}
}
