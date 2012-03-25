package me.rafa652.minecraftbot;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jibble.pircbot.Colors;

/**
 * Representation of a formatting code
 */
public enum Formatting {
    // IRC values based on mIRC's documentation
    // http://www.mirc.com/colors.html
    // I used the closest color possible for Minecraft
    WHITE       ("00", "f"),
    BLACK       ("01", "0"),
    DARK_BLUE   ("02", "1"),
    BLUE        ("02", "1"), // duplicate
    GREEN       ("03", "2"),
    RED         ("04", "c"),
    BROWN       ("05", "4"), // using MC dark red
    DARK_RED    ("05", "4"), // duplicate
    PURPLE      ("06", "5"),
    ORANGE      ("07", "6"), // using MC gold
    YELLOW      ("08", "e"),
    LIGHT_GREEN ("09", "a"),
    TEAL        ("10", "b"), // Using MC aqua
    AQUA        ("10", "b"), // duplicate
    LIGHT_CYAN  ("11", "b"), // using MC aqua
    CYAN        ("11", "b"), // duplicate
    LIGHT_BLUE  ("12", "9"),
    PINK        ("13", "d"),
    GRAY        ("14", "7"),
    GREY        ("14", "7"), // duplicate
    LIGHT_GRAY  ("15", "8"),
    LIGHT_GREY  ("15", "8"), // duplicate
    
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
    C_RESET       ("\u0003",   "f"); // color code on its own usually means the color ends here  
    
    public final String irc; // IRC control code and color value
    public final String mc; // Minecraft two-character color code

    private Formatting(String irc, String mc) {
        if (irc.length() > 0 && Character.isDigit(irc.charAt(0)))
            this.irc = '\u0003' + irc; // this is a color code
        else this.irc = irc; // this is blank or a different control code
        this.mc = '\u00A7' + mc; // section symbol added before value
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
        return msg + "\u000f"; // Colors shouldn't "leak" into the rest of the string
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
        return Colors.removeFormattingAndColors(msg); // and finally get rid of everything else
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
        int i=-1;
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
    
    @Override
    public final String toString() {
        // Color codes are translated before being sent to IRC anyway.
        return mc;
    }
}