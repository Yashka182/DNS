package dns.server;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Client {
    private String server;
    private int port;
    private int selfPort;

    public Client(String server, int port, int selfPort){
        this.server = server;
        this.port = port;
        this.selfPort = selfPort;
    }

    public void run(){
        EventLoopGroup group = new NioEventLoopGroup();

        try{
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .handler(new ChannelInitializer<SocketChannel>(){
                        @Override
                        public void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline pipe = socketChannel.pipeline();

                            pipe.addLast(new LineBasedFrameDecoder(80));
                            pipe.addLast(new StringDecoder());
                            pipe.addLast(new StringEncoder());
                            pipe.addLast(new ClientHandler());
                        }
                    });
            Channel channel = b.connect(server, port).sync().channel();
            System.out.println("Connected to " + channel.remoteAddress());

            System.out.println("Client started\n" +
                    "Enter create/read/update/delete ip number and name divided by spaces");

            BufferedReader read = new BufferedReader(new InputStreamReader(System.in));
            while(true){
                String input = read.readLine();
                if(input.equalsIgnoreCase("the end")){
                    break;
                }

                channel.write(input + "\n");
                channel.flush();
            }
            channel.disconnect();
            System.out.println("Disconnected");
        }catch(Exception e){
            e.printStackTrace();
        }finally {
            group.shutdownGracefully();
        }
    }

    public static void main(String[] args){
        String server = "localhost";
        int port = 8080;
        int selfPort = 1111;
        new Client(server, port, selfPort).run();
    }
}
