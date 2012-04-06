package com.avisenera.minecraftbot;

/**
 * Collection of key values as for getting (and storing) configuration values.
 * Based on the values available in the actual config file.
 */
public class Keys {
    private Keys() {}
    
    public enum connection {
        server, server_password, server_port, channel, nick,
        nick_password, quit_message
    }
    
    public enum settings {
        ping_developer, send_log_to_ops
    }
    
    public enum line_to_irc {
        server, chat, action, join, leave, kick, death
    }
    
    public enum line_to_minecraft {
        chat, action, join, part, quit, kick,
        nick_change, mode_change, topic_change
    }
}
