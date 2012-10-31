package com.avisenera.minecraftbot;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Representation of a formatting code
 */
public enum Formatting {
    // IRC values based on mIRC's documentation
    // http://www.mirc.com/colors.html
    // I used the closest color possible for Minecraft
    WHITE       ("\u000300", "f"),
    BLACK       ("\u000301", "0"),
    DARK_BLUE   ("\u000302", "1"),
    GREEN       ("\u000303", "2"),
    RED         ("\u000304", "c"),
    BROWN       ("\u000305", "4"), // using MC dark red
    PURPLE      ("\u000306", "5"),
    ORANGE      ("\u000307", "6"), // using MC gold
    YELLOW      ("\u000308", "e"),
    LIGHT_GREEN ("\u000309", "a"),
    TEAL        ("\u000310", "3"), // Using MC dark aqua
    LIGHT_CYAN  ("\u000311", "b"), // using MC aqua
    LIGHT_BLUE  ("\u000312", "9"),
    PINK        ("\u000313", "d"),
    GRAY        ("\u000314", "7"),
    LIGHT_GRAY  ("\u000315", "8"),
    
    // Control codes
    BOLD        ("\u0002",   "l"),
    RANDOM      ("",         "k"), // No corresponding code in IRC
    MAGIC       ("",         "k"), // duplicate
    STRIKE      ("",         "m"), // No corresponding code in IRC either
    UNDERLINE   ("\u001f",   "n"),
    ITALIC      ("\u0016",   "o"),
    REVERSE     ("\u0016",   "o"), // duplicate, reverses in mIRC
    NORMAL      ("\u000f",   "r"),
    RESET       ("\u000f",   "r"), // duplicate
    
    // Extra
    C_RESET     ("\u0003",   "r"); // color code on its own usually means the color ends here  
    
    public final String irc; // IRC control code and possibly color value
    public final String mc; // Minecraft two-character color code

    private Formatting(String irc, String mc) {
        this.irc = irc;
        this.mc = '\u00A7' + mc;
    }
    
    public String toString() {
        return mc;
    }
    
    /**
     * Translates Minecraft color codes to IRC color codes.
     * @param line The line from Minecraft
     * @return A line with IRC color codes, if there were codes in the original line.
     * Adds a \x0F (normal) character at the end of the line to prevent colors from
     * "leaking" into the rest of the string. Keep this in mind if the entire string is
     * supposed to have a color.
     */
    public static String toIRC(final String line) {
        String msg = line;
        for (Formatting c : Formatting.values()) {
            msg = msg.replaceAll(c.mc, c.irc);
        }
        return msg; // Colors shouldn't "leak" into the rest of the string
    }
    
    /**
     * Translates IRC color codes to IRC color codes.
     * Background color codes are removed, as well as codes Minecraft doesn't
     * understand (bold, underline, etc).
     * @param line The line from IRC
     * @return A line with Minecraft color codes, if there were codes in the original line.
     */
    public static String toMC(String line) {
        String msg = fix(line);
        for (Formatting c : Formatting.values()) {
            if (c.irc.equals("")) continue;
            msg = msg.replaceAll(c.irc, c.mc);
        }
        return msg; // and finally get rid of everything else
    }
    
    private static String fix(String line) {
        // Catching background colors
        Pattern pattern = Pattern.compile("\u0003[0-9]{1,2}(,[0-9]{1,2})?");
        Matcher matcher = pattern.matcher(line);
        while (matcher.find()) {
            CharSequence bg = matcher.group(1); // This returns null if group 1 doesn't exist
            if (bg != null) line = line.replace(bg, "");
        }
        
        // This picks apart the string character by character as it looks to see if
        // there was a one digit color code entered instead of a two-digit one, then fixes it.
        // I spent three hours looking for an alternative. Regular expressions don't seen to work.
        // Code from other IRC clients that deal with this are almost unreadable or too complicated
        // to just copy over.
        // If someone knows a nicer way to do this, let me know.
        boolean found = false;
        int i = -1;
        do {
            i++;
            char cl = line.charAt(i);
            if (cl == '\u0003') {
                found = true; // found color code
                continue;
            }
            if (found && Character.isDigit(cl)) { // digit character found after code
                if (cl == '1' || cl == '0') { // it's 0 or 1 - must check if there's another number after this
                    i++; if (i>line.length()-1) break; cl = line.charAt(i); // get next character
                    if (!Character.isDigit(cl)) { // this next one is not a number
                        //insert
                        StringBuilder sb1 = new StringBuilder(line);
                        sb1.insert(i-1, '0'); //adding -before-, not -at- current position
                        line = sb1.toString();
                        i++;
                    }
                }
                else { // not 0 or 1 - definitely adding a 0 in front
                    //insert
                    StringBuilder sb1 = new StringBuilder(line);
                    sb1.insert(i, '0'); // adding 0 at the current position
                    line = sb1.toString();
                    i++;
                }
                
                found = false;
            }
        } while (i<line.length()-1);
        
        return line;
    }
}