package pl.fratik.chinczyk.server.socket.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import pl.fratik.chinczyk.server.database.Client;
import pl.fratik.chinczyk.server.database.DatabaseManager;
import pl.fratik.chinczyk.socket.OpCode;
import pl.fratik.chinczyk.socket.messages.client.ClientMessage;
import pl.fratik.chinczyk.socket.messages.client.IdentifyMessage;
import pl.fratik.chinczyk.socket.messages.server.IdentifyResponseMessage;

import static pl.fratik.chinczyk.socket.messages.server.IdentifyResponseMessage.Status.*;

public class IdentifyHandler extends ChannelInboundHandlerAdapter {
    private final DatabaseManager dbm;
    private final MessageHandlerFactory factory;
    private boolean identified = false;

    public IdentifyHandler(DatabaseManager dbm, MessageHandlerFactory factory) {
        this.dbm = dbm;
        this.factory = factory;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!(msg instanceof ClientMessage)) throw new IllegalArgumentException("nieoczekiwany typ wiadomości " + msg.getClass().getName());
        if (((ClientMessage) msg).getOp() == OpCode.IDENTIFY) {
            IdentifyMessage message = (IdentifyMessage) msg;
            if (identified) {
                ctx.writeAndFlush(new IdentifyResponseMessage(ALREADY_IDENTIFIED));
                return;
            }
            if (!factory.isVersionSupported(message.getVersion())) {
                ctx.writeAndFlush(new IdentifyResponseMessage(INVALID_VERSION));
                return;
            }
            Client client = dbm.getClientDataByToken(message.getToken());
            if (client == null) {
                ctx.writeAndFlush(new IdentifyResponseMessage(INVALID_TOKEN));
                return;
            }
            identified = true;
            ctx.pipeline().addLast(factory.getHandler(message.getVersion()));
            ctx.writeAndFlush(new IdentifyResponseMessage(SUCCESS, client.getClientName(), client.getConnections()));
        } else ctx.fireChannelRead(msg);
    }
}
