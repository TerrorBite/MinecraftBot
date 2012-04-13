/* 
Copyright Paul James Mutton, 2001-2009, http://www.jibble.org/

This file is part of PircBot.

This software is dual-licensed, allowing you to choose between the GNU
General Public License (GPL) and the www.jibble.org Commercial License.
Since the GPL may be too restrictive for use in a proprietary application,
a commercial license is also provided. Full license information can be
found at http://www.jibble.org/licenses/

*/

package org.jibble.pircbot;

import java.util.EnumMap;

/**
 * This class is used to represent a user on an IRC server.
 * Instances of this class are returned by the getUsers method
 * in the PircBot class.
 *  <p>
 * Note that this class no longer implements the Comparable interface
 * for Java 1.1 compatibility reasons.
 *
 * @since   1.0.0
 * @author  Paul James Mutton,
 *          <a href="http://www.jibble.org/">http://www.jibble.org/</a>
 * @version    1.5.0 (Build time: Mon Dec 14 20:07:17 2009)
 */
public class User {
    
    // Changes made for MinecraftBot:
    // Since a nick can appear to have multiple prefixes, using a Map instead of a String to track them.
    public enum Prefix {
        OWNER ("~", "q"),
        PROTECTED ("&", "a"),
        OP ("@", "o"),
        HALFOP ("%", "h"),
        VOICE ("+", "v");
        
        public final String symbol;
        public final String mode;
        private Prefix(String symbol, String mode) {
            this.symbol = symbol;
            this.mode = mode;
        }
    }
    private EnumMap<Prefix, Boolean> _prefix;
    
    
    /**
     * Constructs a User object with a known prefix and nick.
     *
     * @param prefix The status of the user, for example, "@".
     * @param nick The nick of the user.
     */
    User(String prefix, String nick) {
        _prefix = new EnumMap<Prefix, Boolean>(Prefix.class);
        for (Prefix p : Prefix.values())
            _prefix.put(p, Boolean.FALSE);
        
        addPrefix(prefix);
        
        _nick = nick;
        _lowerNick = nick.toLowerCase();
    }
    
    /**
     * Adds a prefix to the user.
     * @param prefix The prefix symbol (or equivalent mode) to add
     */
    public void addPrefix(String prefix) {
        if (prefix == null || prefix.length() != 1) return;
        for (Prefix p : Prefix.values()) {
            if (prefix.equals(p.mode)) _prefix.put(p, Boolean.TRUE);
            if (prefix.equals(p.symbol)) _prefix.put(p, Boolean.TRUE);
        }
    }
    
    /**
     * Removes a prefix from the user.
     * @param prefix The prefix symbol to remove
     */
    public void delPrefix(String prefix) {
        if (prefix == null || prefix.length() != 1) return;
        for (Prefix p : Prefix.values()) {
            if (prefix.equals(p.mode)) _prefix.put(p, Boolean.FALSE);
            if (prefix.equals(p.symbol)) _prefix.put(p, Boolean.FALSE);
        }
    }
    
    /**
     * Returns the highest-level prefix of the user. If the User object has
     * been obtained from a list of users in a channel, then this will reflect the user's
     * status in that channel.
     *
     * @return The prefix of the user. If there is no prefix, then an empty
     *         String is returned.
     */
    public String getPrefix() {
        for (Prefix p : Prefix.values()) {
            if (_prefix.get(p).booleanValue()) return p.symbol;
        }
        
        return "";
    }
    
    /**
     * Returns the nick of the user.
     * 
     * @return The user's nick.
     */
    public String getNick() {
        return _nick;
    }
    
    
    /**
     * Returns the nick of the user complete with their prefix if they
     * have one, e.g. "@Dave".
     * 
     * @return The user's prefix and nick.
     */
    public String toString() {
        return this.getPrefix() + this.getNick();
    }
    
    
    /**
     * Returns true if the nick represented by this User object is the same
     * as the argument. A case insensitive comparison is made.
     * 
     * @return true if the nicks are identical (case insensitive).
     */
    public boolean equals(String nick) {
        return nick.toLowerCase().equals(_lowerNick);
    }
    
    
    /**
     * Returns true if the nick represented by this User object is the same
     * as the nick of the User object given as an argument.
     * A case insensitive comparison is made.
     * 
     * @return true if o is a User object with a matching lowercase nick.
     */
    public boolean equals(Object o) {
        if (o instanceof User) {
            User other = (User) o;
            return other._lowerNick.equals(_lowerNick);
        }
        return false;
    }
    
    
    /**
     * Returns the hash code of this User object.
     * 
     * @return the hash code of the User object.
     */
    public int hashCode() {
        return _lowerNick.hashCode();
    }
    
    
    /**
     * Returns the result of calling the compareTo method on lowercased
     * nicks. This is useful for sorting lists of User objects.
     * 
     * @return the result of calling compareTo on lowercased nicks.
     */
    public int compareTo(Object o) {
        if (o instanceof User) {
            User other = (User) o;
            return other._lowerNick.compareTo(_lowerNick);
        }
        return -1;
    }
    
    
    private String _nick;
    private String _lowerNick;
    
}
