package pl.fratik.chinczyk.client.socket;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import pl.fratik.chinczyk.client.ChinczykClient;
import pl.fratik.chinczyk.socket.messages.server.ServerMessage;

import java.util.Iterator;

public class MessageHandler extends ChannelInboundHandlerAdapter {
    private final ChinczykClient client;

    public MessageHandler(ChinczykClient client) {
        this.client = client;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!(msg instanceof ServerMessage)) throw new IllegalArgumentException("nieprawidłowy typ wiadomości");
        client.handleRead((ServerMessage) msg);
        ClientSocketChannel chan = (ClientSocketChannel) ctx.channel();
        for (Iterator<Task> iterator = chan.getTasks().iterator(); iterator.hasNext();) {
            Task task = iterator.next();
            if (task.getCondition().test((ServerMessage) msg)) {
                task.getCallback().accept((ServerMessage) msg);
                iterator.remove();
            }
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        ClientSocketChannel chan = (ClientSocketChannel) ctx.channel();
        for (Iterator<Task> iterator = chan.getTasks().iterator(); iterator.hasNext();) {
            Task task = iterator.next();
            task.getDisconnect().run();
            iterator.remove();
        }
    }
}
