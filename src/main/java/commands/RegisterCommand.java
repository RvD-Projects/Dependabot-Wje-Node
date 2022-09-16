package commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.bukkit.Bukkit;
import org.json.JSONArray;

import dao.UsersDao;
import helpers.Helper;
import main.WhitelistJe;
import models.User;

import java.awt.*;
import java.util.logging.Logger;

public class RegisterCommand extends ListenerAdapter {
    private JDA jda;
    private WhitelistJe main;

    public RegisterCommand(WhitelistJe main) {
        this.main = main;

    }

    private boolean validatePseudo(String pseudo, SlashCommandEvent event) {
        if (!Helper.isMcPseudo(pseudo)) {
            final String errMsg = "Votre pseudo devrait comporter entre 3 et 16 caractères" +
                    "\n\n et ne doit pas comporter de caractères spéciaux à part des underscores `_` ou tirets `-`";
            event.reply(requestMsg(errMsg)).setEphemeral(true).queue();
            return false;
        }
        return true;
    }

    @Override
    public void onSlashCommand(SlashCommandEvent event) {
        if (!event.getName().equals("register"))
            return;

        final String pseudo = event.getOption("pseudo").getAsString();
        boolean valid = this.validatePseudo(pseudo, event);

        if (valid) {
            try {
                JDA jda = main.getDiscordManager().jda;

                boolean isAllowed = false;
                boolean isConfirmed = false;
                final String discordTag = event.getMember().getUser().getAsTag();
                Logger.getLogger("WJE").info(discordTag);
                User existingUser = new UsersDao().findByDisccordTag(discordTag);
                User existingPseudo = new UsersDao().findByMcName(pseudo);

                if (!existingUser.getMcName().equals(pseudo)) {
                    event.reply(requestMsg("*Vous êtes déjà enregistrer sous un autre pseudo*")).setEphemeral(true).queue();
                    return;
                }

                if (!existingPseudo.getDiscordTag().equals(discordTag)) {
                    event.reply(requestMsg("*Ce pseudo est déjà enregistrer par un autre joueur*")).setEphemeral(true).queue();
                    return;
                }

                if (existingUser.getId() > 0) {
                    isAllowed = existingUser.isAllowed();
                    isConfirmed = existingUser.setAsconfirmed(event.getId());

                    if (isAllowed && isConfirmed) {
                        event.reply(requestMsg("*Vous êtes déjà accepté sur le serveur*")).setEphemeral(true).queue();
                        return;
                    }

                    if (isAllowed && !isConfirmed) {
                        event.reply(requestMsg("*Une nouvelle demande de confirmation ?*")).setEphemeral(true).queue();
                        return;
                    }
                }

            } catch (Exception e) {
                event.reply(requestMsg("*Une erreur est survenu contactez un admin!*")).setEphemeral(true).queue();
                e.printStackTrace();
                return;
            }
        }

        // // TODO: use DAO
        // final String discord = resultset.getString("users.discord");
        // final String name = resultset.getString("users.name");

        // EmbedBuilder builder = new EmbedBuilder().setTitle("Une demande a été
        // transmise")
        // .addField("Pseudo", pseudo, true).addField("Discord", "<@" +
        // event.getMember().getId() + ">", true)
        // .setThumbnail(event.getMember().getUser().getAvatarUrl()).setFooter("ID " +
        // event.getMember().getId())
        // .setColor(new Color(0x9b7676));
        // event.reply(requestMsg("*Votre demande d'accès pour `" + pseudo
        // + "` a été envoyé aux modérateurs.*\n*Merci de patienter jusqu'à une prise de
        // décision de leur part.*"))
        // .setEphemeral(true).queue();
        // jda.getTextChannelById("1013374066540941362").sendMessage(builder.build())
        // .setActionRows(
        // ActionRow.of(net.dv8tion.jda.api.interactions.components.Button.primary("yes",
        // "Accepter"),
        // net.dv8tion.jda.api.interactions.components.Button.secondary("no",
        // "Refuser")))
        // .queue(message -> {

        // final PreparedStatement preparedstatement2;
        // preparedstatement2 = connection
        // .prepareStatement("INSERT INTO users (name, discord, messageid)" + "VALUES
        // (?, ?, ?)");
        // preparedstatement2.setString(1, pseudo);
        // preparedstatement2.setString(2, event.getMember().getId());
        // preparedstatement2.setString(3, message.getId());
        // preparedstatement2.executeUpdate();
        // event.getGuild().getMemberById(event.getMember().getId()).modifyNickname(pseudo).queue();

        // });
    }

    // @Override
    // public void onButtonClick(ButtonClickEvent event) {
    // if (event.getChannel().getId().equals("1013374066540941362")) {
    // if (!roleManager.hasRole(event.getMember(), "807839780309172255") &&
    // !roleManager.hasRole(event.getMember(), "809003930884505602") &&
    // !roleManager.hasRole(event.getMember(), "926270775298752512") &&
    // !roleManager.hasRole(event.getMember(), "783839953372053516")) {
    // event.reply("Dommage vous n'avez pas les accès...
    // ¯\\_(ツ)_/¯").setEphemeral(true).queue();
    // return;
    // }
    // String message = event.getMessage().getId();
    // jda = main.getDiscordManager().jda;
    // userinfo = main.getDatabaseManager().getUserinfo();
    // whitelistManager = main.getWhitelistManager();
    // if (event.getButton().getLabel().equals("Accepter")) {
    // try {
    // final Connection connection;
    // connection = userinfo.getConnection();
    // final PreparedStatement preparedstatement = connection
    // .prepareStatement("SELECT * FROM users WHERE messageid = ?");
    // preparedstatement.setString(1, message);
    // preparedstatement.executeQuery();
    // final ResultSet resultset = preparedstatement.executeQuery();
    // if (!resultset.next()) {
    // return;
    // }
    // String name = resultset.getString("users.name");
    // String discord = resultset.getString("users.discord");
    // EmbedBuilder builder = new EmbedBuilder().setTitle("Demande acceptée")
    // .addField("Pseudo", name, true).addField("Discord", "<@" + discord + ">",
    // true)
    // .setThumbnail(jda.getUserById(discord).getAvatarUrl()).setFooter("ID " +
    // discord)
    // .setColor(new Color(0x484d95));
    // jda.getTextChannelById("1013374066540941362").editMessageById(message,
    // builder.build())
    // .setActionRow(net.dv8tion.jda.api.interactions.components.Button
    // .primary("valide", "Whitelist par " +
    // event.getMember().getNickname()).asDisabled())
    // .queue();
    // jda.openPrivateChannelById(discord).queue(channel -> {
    // channel.sendMessage("**Bienvenue sur le serveur, <@" + discord + ">
    // !**").queue(null,
    // new ErrorHandler().handle(ErrorResponse.CANNOT_SEND_TO_USER, (ex) -> {
    // jda.getTextChannelById("770148932075782176")
    // .sendMessage("**Bienvenue sur le serveur, <@" + discord + "> !**").queue();
    // }));
    // });
    // final PreparedStatement preparedstatement2 = connection
    // .prepareStatement("UPDATE users SET checked = " + 1 + " WHERE discord = "
    // + resultset.getString("users.discord"));
    // preparedstatement2.executeUpdate();
    // if
    // (!whitelistManager.getPlayersAllowed().contains(resultset.getString("users.name")))
    // {
    // whitelistManager.getPlayersAllowed().add(resultset.getString("users.name"));
    // }
    // event.reply("Le joueur " + name + " a bien été
    // whitelist").setEphemeral(true).queue();
    // } catch (SQLException throwables) {
    // throwables.printStackTrace();
    // }
    // }
    // if (event.getButton().getLabel().equals("Refuser")) {
    // try {
    // final Connection connection;
    // connection = userinfo.getConnection();
    // final PreparedStatement preparedstatement = connection
    // .prepareStatement("SELECT * FROM users WHERE messageid = ?");
    // preparedstatement.setString(1, message);
    // preparedstatement.executeQuery();
    // final ResultSet resultset = preparedstatement.executeQuery();
    // if (!resultset.next()) {
    // return;
    // }
    // String name = resultset.getString("users.name");
    // String discord = resultset.getString("users.discord");
    // EmbedBuilder builder = new EmbedBuilder().setTitle("Demande
    // refusée").addField("Pseudo", name, true)
    // .addField("Discord", "<@" + discord + ">", true)
    // .setThumbnail(jda.getUserById(discord).getAvatarUrl()).setFooter("ID " +
    // discord)
    // .setColor(new Color(0x44474d));
    // jda.getTextChannelById("1013374066540941362").editMessageById(message,
    // builder.build())
    // .setActionRow(Button.secondary("unvaldie", "Refusé par " +
    // event.getMember().getNickname())
    // .asDisabled())
    // .queue();
    // if (whitelistManager.getPlayersAllowed().contains(name)) {
    // whitelistManager.getPlayersAllowed().remove(name);
    // }
    // if (Bukkit.getPlayer(name) != null && Bukkit.getPlayer(name).isOnline()) {
    // Bukkit.getScheduler().runTask(main, () -> {
    // Bukkit.getPlayer(name).kickPlayer("§cVous avez été expulsé");
    // });
    // }
    // event.getGuild().getMemberById(discord).modifyNickname(jda.getUserById(discord).getName()).queue();
    // final PreparedStatement preparedstatement2 = connection
    // .prepareStatement("DELETE FROM users WHERE discord = " + discord);
    // preparedstatement2.executeUpdate();
    // event.reply("Le joueur " + name + " a bien été
    // refusé").setEphemeral(true).queue();
    // } catch (SQLException e) {
    // e.printStackTrace();
    // }
    // }
    // }
    // }

    public String requestMsg(String message) {
        return "**Demande d'accès**\n\n" + message;
    }
}
