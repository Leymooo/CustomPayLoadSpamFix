package ru.leymooo.spamfix;

import java.util.HashMap;

import net.md_5.bungee.api.connection.Connection;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

public class Main extends Plugin implements Listener {
    HashMap<Connection, Long> cancel = new HashMap<Connection, Long>();
    @Override
    public void onEnable() {
        getProxy().getPluginManager().registerListener(this, this);
    }
    @Override
    public void onDisable() {
        cancel.clear();
        cancel = null;
    }
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMessage(PluginMessageEvent e) {
        if (e.getTag().startsWith("MC|BEdit") || e.getTag().startsWith("MC|BSign")) {
            if (needCancel(e.getSender())) {
                e.setCancelled(true);
                return;
            } 
            cancel.put(e.getSender(), System.currentTimeMillis());
        }
    }
    @EventHandler
    public void onDisconnect(PlayerDisconnectEvent e) {
        cancel.remove(e.getPlayer());
    }
    private boolean needCancel(Connection p) {
        return this.cancel.containsKey(p) && (3000 - (System.currentTimeMillis() - this.cancel.get(p))) > 0;
    }
}
