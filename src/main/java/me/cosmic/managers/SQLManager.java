package me.cosmic.managers;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

public class SQLManager {

    private String host = "localhost";
    private String port = "3306";
    private String db = "test";
    private String username = "root";
    private String password = "";

    private Connection connection;

    public boolean isConnected() {
        return (connection != null);
    }

    public void connect() throws ClassNotFoundException, SQLException {
        if (!isConnected()) {
            connection = DriverManager.getConnection("jdbc:mysql://" +
                            host + ":" + port + "/" + db + "?useSSL=false",
                    username, password);
        }
    }

    public void disconnect() {
        if (isConnected()) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public void registerDeathChest(Location location, Player player) {
        String name = player.getDisplayName();
        String uuid = player.getUniqueId().toString();
        double x = location.getX();
        double y = location.getY();
        double z = location.getZ();
        String world = location.getWorld().toString();

        PreparedStatement preparedStatement;
        try {
            preparedStatement = getConnection().prepareStatement("INSERT INTO deathchestslocation" + "(playername, uuid, locationx, locationy, locationz, world) VALUES (?, ?, ?, ?, ?, ?)");
            preparedStatement.setString(1, name);
            preparedStatement.setString(2, uuid);
            preparedStatement.setDouble(3, x);
            preparedStatement.setDouble(4, y);
            preparedStatement.setDouble(5, z);
            preparedStatement.setString(6, world);
            preparedStatement.executeUpdate();
            return;
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void removeDeathChest(Player player) {
        String uuid = player.getUniqueId().toString();

        PreparedStatement preparedStatement;
        try {
            preparedStatement = getConnection().prepareStatement("DELETE FROM deathchestslocation WHERE uuid=?");
            preparedStatement.setString(1, uuid);
            preparedStatement.executeUpdate();
            return;
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
