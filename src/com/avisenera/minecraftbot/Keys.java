package com.avisenera.minecraftbot;

/**
 * Collection of key values as for getting (and storing) configuration values.
 * Based on the values available in the actual config file.
 */
public class Keys {
    private Keys() {}
    
    public enum connection {
        server, server_password, server_port, retries, channel, channel_key, nick, nick_password
    }
    
    public enum settings {
        send_log_to_ops, show_players_command, show_nick_prefixes, quit_message
    }
    
    public enum line_to_irc {
        server, chat, action, join, leave, kick, death
    }
    
    public enum line_to_minecraft {
        chat, action, join, part, quit, kick,
        nick_change, mode_change, topic_change
    }
}
