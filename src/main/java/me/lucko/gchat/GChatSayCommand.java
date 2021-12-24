package me.lucko.gchat;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.ConsoleCommandSource;
import com.velocitypowered.api.proxy.Player;
import me.lucko.gchat.api.events.GChatConsoleMessageSendEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class GChatSayCommand implements SimpleCommand {
    private final GChatPlugin plugin;
    private String chatFormat;

    public GChatSayCommand(GChatPlugin plugin) {
        this.plugin = plugin;
        this.chatFormat = plugin.getConfig().getConsoleFormat();
    }

    @Override
    public void execute(final Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();

        if(args.length == 0) {
            source.sendMessage(Component.text("Correction usage: /say {message}.", NamedTextColor.RED));
            return;
        }

        StringBuilder message = new StringBuilder();
        for(String part : args) {
            message.append(part).append(" ");
        }

        TextComponent formatText = LegacyComponentSerializer.
                legacyAmpersand().deserialize(chatFormat.replace("{message}", message.toString()));

        GChatConsoleMessageSendEvent sendEvent = new GChatConsoleMessageSendEvent(message.toString(), chatFormat.replace("{message}", message.toString()));
        plugin.getProxy().getEventManager().fire(sendEvent).thenAcceptAsync((event -> {
            if(!event.getResult().isAllowed()) {
                return;
            }

            plugin.getProxy().getConsoleCommandSource().sendMessage(formatText);

            for (Player p : plugin.getProxy().getAllPlayers()) {
                p.sendMessage(formatText);
            }
        }));
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("gchat.staff.say") || invocation.source() instanceof ConsoleCommandSource;
    }
}
