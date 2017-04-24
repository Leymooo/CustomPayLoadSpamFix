package ru.leymooo.spamfix;

import java.util.HashMap;

import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.Connection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import com.google.common.base.Charsets;

public class Main extends Plugin implements Listener {
    private HashMap<Connection, Long> cancel = new HashMap<Connection, Long>();
    private HashMap<Connection, Integer> channels = new HashMap<Connection, Integer>();
    @Override
    public void onEnable() {
        getProxy().getPluginManager().registerListener(this, this);
    }
    @Override
    public void onDisable() {
        channels.clear();
        cancel.clear();
        channels = null;
        cancel = null;
    }
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMessage(PluginMessageEvent e) {
        Connection sender = e.getSender();
        if (!(sender instanceof ProxiedPlayer) ) return;
        String tag = e.getTag();
        if ("MC|BEdit".equals(tag) || "MC|BSign".equals(tag)) {
            if (needCancel(sender)) {
                e.setCancelled(true);
                return;
            } 
            cancel.put(sender, System.currentTimeMillis());
        }
        if ("REGISTER".equals(tag)) {
            String allChannels = new String( e.getData(), Charsets.UTF_8 );
            String[] channels = allChannels.split( "\0" );
            for (int i = 0; i<channels.length; i++) {
                if (checkPlayerChannels(sender)) {
                    sender.disconnect(TextComponent.fromLegacyText("Too many channels registered (max: 124)"));
                    e.setCancelled(true);
                    break;
                }
            }
        }
    }
    @EventHandler
    public void onDisconnect(PlayerDisconnectEvent e) {
        cancel.remove(e.getPlayer());
        channels.remove(e.getPlayer());
    }
    private boolean checkPlayerChannels(Connection connection) {
        if (!connection.isConnected()) return false;
        if (!channels.containsKey(connection)) {
            channels.put(connection, 1);
            return false;
        }
        return channels.replace(connection, channels.get(connection)+1)>124;
    }
    private boolean needCancel(Connection p) {
        return this.cancel.containsKey(p) && (1200 - (System.currentTimeMillis() - this.cancel.get(p))) > 0;
    }
}
