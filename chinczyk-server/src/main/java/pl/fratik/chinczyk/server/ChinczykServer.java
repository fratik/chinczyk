package pl.fratik.chinczyk.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.fratik.chinczyk.game.Chinczyk;
import pl.fratik.chinczyk.server.database.DatabaseManager;
import pl.fratik.chinczyk.server.socket.MessageDecoder;
import pl.fratik.chinczyk.server.socket.MessageEncoder;
import pl.fratik.chinczyk.server.socket.handlers.IdentifyHandler;
import pl.fratik.chinczyk.server.socket.handlers.MessageHandlerFactory;

import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class ChinczykServer {
    public static final Random RANDOM = new Random();
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final Map<Integer, Chinczyk> games;

    public ChinczykServer(String ip, int port) throws SQLException {
        DatabaseManager dbm = new DatabaseManager();
        games = Collections.synchronizedMap(new HashMap<>());
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new MessageDecoder(games),
                                    new MessageEncoder(),
                                    new IdentifyHandler(dbm, new MessageHandlerFactory(games, dbm)));
                        }
                    }).option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            ChannelFuture f = b.bind(ip, port).sync();
            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("Wątek serwera został przerwany");
            Thread.currentThread().interrupt();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}
