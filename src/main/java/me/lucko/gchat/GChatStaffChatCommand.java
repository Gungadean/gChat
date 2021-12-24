package me.lucko.gchat;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.ConsoleCommandSource;
import com.velocitypowered.api.proxy.Player;
import me.lucko.gchat.api.events.GChatStaffMessageSendEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class GChatStaffChatCommand implements SimpleCommand {

    private final GChatPlugin plugin;

    public GChatStaffChatCommand(GChatPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();

        if(args.length == 0) {
            if(source instanceof ConsoleCommandSource) {
                source.sendMessage(Component.text("Console cannot toggle StaffChat. Please use: /staffchat {message}", NamedTextColor.RED));
                return;
            }

            if(plugin.getStaffChatters().contains(((Player)source).getUniqueId().toString())) {
                plugin.getStaffChatters().remove(((Player)source).getUniqueId().toString());
                source.sendMessage(Component.text("Staff chat is now disabled.", NamedTextColor.GOLD));
            } else {
                plugin.getStaffChatters().add(((Player)source).getUniqueId().toString());
                source.sendMessage(Component.text("Staff chat is now enabled.", NamedTextColor.GOLD));
            }
            return;
        }

        StringBuilder fullMessage = new StringBuilder();
        for(String part : args) {
            fullMessage.append(part).append(" ");
        }

        String formattedMessage = plugin.getConfig().getStaffChatFormat().replaceAll("\\{message}", fullMessage.toString());

        GChatStaffMessageSendEvent sendEvent;

        if(source instanceof Player) {
            Player player = (Player)source;
            formattedMessage = formattedMessage.replaceAll("\\{user}", player.getUsername())
                    .replaceAll("\\{server}", player.getCurrentServer().get().getServerInfo().getName());
            sendEvent = new GChatStaffMessageSendEvent(player.getUniqueId().toString(), fullMessage.toString(), formattedMessage);
        } else {
            formattedMessage = formattedMessage.replaceAll("\\{user}", "Console")
                    .replaceAll("\\{server}", "Global");
            sendEvent = new GChatStaffMessageSendEvent("Console", fullMessage.toString(), formattedMessage);
        }

        plugin.getProxy().getEventManager().fire(sendEvent).thenAcceptAsync((event) -> {
            if(!event.getResult().isAllowed()) {
                return;
            }

            plugin.getProxy().getConsoleCommandSource().sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(event.getFormattedMessage()));

            for(Player player : plugin.getProxy().getAllPlayers()) {
                if(player.hasPermission("gchat.staff.chat")) {
                    player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(event.getFormattedMessage()));
                }
            }
        });
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("gchat.staff.chat") || invocation.source() instanceof ConsoleCommandSource;
    }
}
