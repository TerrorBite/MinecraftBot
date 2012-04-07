package com.avisenera.minecraftbot;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

// From the config file:
// Will satisfy the plugin developer's curiosity of how many servers are using this plugin.
public class MetricsSender extends Thread {
    public static void send(String cb_version, String mcb_version) {
        // Running in a thread so it doesn't interrupt whatever called this.
        MetricsSender ms = new MetricsSender(cb_version, mcb_version);
        ms.start();
    }
    
    private String cb_version; // craftbukkit
    private String mcb_version; // minecraftbot
    
    protected MetricsSender(String cb_version, String mcb_version) {
        this.cb_version = cb_version;
        this.mcb_version = mcb_version;
    }
    
    @Override
    public void run() {
        // metrics.php: name parameter in the URL is what is being tracked, then
        // the extra data is sent with a POST. in this case, 'server' and 'plugin'
        try {
            URL url = new URL("http://random.avisenera.com/metrics.php?name=minecraftbot");
            URLConnection connection = url.openConnection();
            connection.setDoOutput(true);
            
            String post;
            post = URLEncoder.encode("server", "UTF-8") + "=" + URLEncoder.encode(cb_version, "UTF-8") + "&";
            post += URLEncoder.encode("plugin", "UTF-8") + "=" + URLEncoder.encode(mcb_version, "UTF-8");
            
            OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
            out.write(post);
            out.flush();
            
            // Nothing is done with the response data.
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            while (in.readLine() != null) {}
            
            in.close();
            out.close();
        } catch (Exception e) {
            // ignore all errors
        }
    }
}
