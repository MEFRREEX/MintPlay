package com.bestaford.mintplay;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;

import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;

public class Database {

    public MintPlay plugin;
    public Connection connection;

    public Database(MintPlay plugin, String name) {
        this.plugin = plugin;
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + plugin.getDataFolder().getAbsolutePath() + "\\" + name + ".db");
            createTable(name);
        } catch (Exception exception) {
            plugin.getLogger().error(exception.getMessage());
        }
    }

    public void createTable(String name) {
        try {
            InputStreamReader reader = new InputStreamReader(plugin.getResource(name + ".sql"), Charsets.UTF_8);
            connection.createStatement().execute(CharStreams.toString(reader));
        } catch (Exception exception) {
            plugin.getLogger().error(exception.getMessage());
        }
    }
}
