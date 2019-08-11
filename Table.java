package dns.server;

import java.sql.*;

public class Table {
    Connection connection;
    String tableName;

    Table(String tableName) throws SQLException{
        this.tableName = tableName;
        //this.createTable();
        connection = DNSHandler.getConnection();
        System.out.println("Connection to DB established");
    }
    void executeDML(String sql, String description) throws SQLException{
        Statement statement = connection.createStatement();
        statement.execute(sql);
        statement.close();
        if(description != null){
            System.out.println(description);
        }
    }

    ResultSet executeSQL(String sql) throws SQLException{
        Statement statement = connection.createStatement();
        return statement.executeQuery(sql);
    }

    public void createTable() throws SQLException{
        executeDML("CREATE TABLE " + tableName +" (" +
                "ip BIGINT PRIMARY KEY," +
                "name VARCHAR(255) NOT NULL)", "table created");
    }
}
