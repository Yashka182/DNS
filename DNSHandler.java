package dns.server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;


public class DNSHandler extends ChannelInboundHandlerAdapter {
    public  static final String dbUrl = "jdbc:h2:/c:/DNSServer/db/DNSdb";
    public static final String dbDriver = "org.h2.Driver";

    Table table;




    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            String mess = (String) msg;
            String status = new String();
            String[] tokens = mess.split(" ");
            /*for(int i = 0; i < tokens.length; i ++){
                System.out.println(tokens[i]);
            }*/
            switch (tokens[0]) {
                case "create":
                    this.createRow(Long.getLong(tokens[1]), tokens[2]);
                    status = "Row created";
                    break;
                case "update":
                    this.updateRow(Long.getLong(tokens[1]), tokens[2]);
                    status = "Row updated";
                    break;
                case "read":
                    status = "Row fetched: " + showResult(this.getRows(Long.getLong(tokens[1])));
                    break;
                case "delete":
                    this.deleteRow(Long.getLong(tokens[1]));
                    status = "Row deleted";
                    break;
            }
            Channel channel = ctx.channel();
            channel.write("Result: " + status + "\n");
            channel.flush();
        }finally {
            ReferenceCountUtil.release(msg);
        }
}




    public DNSHandler() throws SQLException, ClassNotFoundException{
        Class.forName(dbDriver);
        table = new Table("DNS");
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(dbUrl);
    }

    public void createRow(long ip, String name) throws SQLException{
        table.executeDML("INSERT INTO " + table.tableName +
                " VALUES (" + ip + ", '" + name + "');", "Row created");
    }

    public void updateRow(long ip, String name) throws SQLException{
        table.executeDML("UPDATE " + table.tableName +
                " SET NAME = '" + name  +
                "' WHERE IP = " + ip, "Row updated");
    }

    public void deleteRow(long ip) throws SQLException{
        table.executeDML("DELETE FROM " + table.tableName +
                " WHERE IP = " + ip, "Row deleted");
    }

    public ResultSet getRows(long ip) throws SQLException{
        return table.executeSQL("SELECT * FROM " + table.tableName +
                " WHERE IP = " + ip);

    }

    public String showResult(ResultSet result) throws SQLException{
        int columns = result.getMetaData().getColumnCount();
        StringBuilder output = new StringBuilder("IP      NAME\n");

        while(result.next()){
            StringBuilder row = new StringBuilder();
            for(int i = 1; i <= columns; i++){
                row.append(result.getString(i) + "   ");
            }
            output.append(row.toString() + "\n");
        }
        return output.toString();
    }

    /*public static void main(String[] args) {
        try{
            DNSHandler server = new DNSHandler();
            //server.table.createTable();
            //server.createRow(12345, "Google");
            //server.createRow(113456, "Yandex");
            server.deleteRow(12345);
            ResultSet res = server.getRows(12345);
            server.showResult(res);

        }catch(SQLException e){
            e.printStackTrace();
            System.out.println("SQL error");
        }catch (ClassNotFoundException e){
            e.printStackTrace();
            System.out.println("JDBC driver error");
        }
    }*/
}
