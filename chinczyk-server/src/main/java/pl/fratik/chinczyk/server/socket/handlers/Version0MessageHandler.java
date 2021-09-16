package pl.fratik.chinczyk.server.socket.handlers;

import io.netty.channel.ChannelHandlerContext;
import pl.fratik.chinczyk.game.Chinczyk;
import pl.fratik.chinczyk.game.Place;
import pl.fratik.chinczyk.game.Player;
import pl.fratik.chinczyk.server.database.DatabaseManager;
import pl.fratik.chinczyk.server.database.PlayerData;
import pl.fratik.chinczyk.server.game.ServerChinczyk;
import pl.fratik.chinczyk.socket.messages.client.*;
import pl.fratik.chinczyk.socket.messages.client.game.ClientGameMessage;
import pl.fratik.chinczyk.socket.messages.server.ConnectResponseMessage;
import pl.fratik.chinczyk.socket.messages.server.DisconnectResponseMessage;
import pl.fratik.chinczyk.socket.messages.server.HostResponseMessage;
import pl.fratik.chinczyk.socket.messages.server.InfoResponseMessage;
import pl.fratik.chinczyk.socket.messages.server.game.HeaderGameMessage;
import pl.fratik.chinczyk.socket.messages.server.game.SyncEndGameMessage;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

class Version0MessageHandler extends MessageHandler {
    Version0MessageHandler(Map<Integer, Chinczyk> games, DatabaseManager dbm) {
        super(games, dbm);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!(msg instanceof ClientMessage)) throw new IllegalArgumentException("nieoczekiwany typ wiadomości " + msg.getClass().getName());
        switch (((ClientMessage) msg).getOp()) {
            case HOST: {
                HostMessage message = (HostMessage) msg;
                PlayerData pData = message.getId() == null ? null : dbm.getPlayerData(message.getId());
                if (pData == null) {
                    ctx.writeAndFlush(new HostResponseMessage(HostResponseMessage.Status.HOST_NULL));
                    return;
                }
                ServerChinczyk chinczyk;
                try {
                    chinczyk = ServerChinczyk.createNewGame(dbm, pData, message.isPrivateMode(), message.getLanguage(), games);
                    chinczyk.addConnection(ctx.channel(), false);
                } catch (Exception e) {
                    ctx.writeAndFlush(new HostResponseMessage(HostResponseMessage.Status.INTERNAL_ERROR));
                    return;
                }
                ctx.write(new HostResponseMessage(HostResponseMessage.Status.SUCCESS, chinczyk.getGameCode()));
                ctx.write(new HeaderGameMessage(chinczyk.getGameCode(), chinczyk.generateHeader()));
                ctx.writeAndFlush(new SyncEndGameMessage(chinczyk.getGameCode()));
                break;
            }
            case CONNECT: {
                ConnectMessage message = (ConnectMessage) msg;
                ServerChinczyk chinczyk = (ServerChinczyk) games.get(message.getGameCode());
                if (chinczyk == null) {
                    ctx.writeAndFlush(new ConnectResponseMessage(message.getGameCode(), ConnectResponseMessage.Status.INVALID_GAME, false));
                    return;
                }
                if (chinczyk.isConnected(ctx.channel())) {
                    ctx.writeAndFlush(new ConnectResponseMessage(message.getGameCode(), ConnectResponseMessage.Status.ALREADY_CONNECTED, false));
                    return;
                }
                chinczyk.addConnection(ctx.channel(), true);
                break;
            }
            case INFO: {
                InfoMessage message = (InfoMessage) msg;
                ServerChinczyk chinczyk = (ServerChinczyk) games.get(message.getGameCode());
                if (chinczyk == null) {
                    ctx.writeAndFlush(new InfoResponseMessage(InfoResponseMessage.Status.INVALID_GAME));
                    return;
                }
                Set<Place> availablePlaces = EnumSet.allOf(Place.class);
                availablePlaces.removeAll(chinczyk.getPlayersMap().keySet());
                ctx.writeAndFlush(new InfoResponseMessage(InfoResponseMessage.Status.SUCCESS, chinczyk.getGameCode(),
                        chinczyk.isPrivate(), availablePlaces,
                        (int) chinczyk.getPlayers().stream().filter(Player::isReady).count(),
                        chinczyk.getStatus(), chinczyk.getLanguage(), chinczyk.getRules()));
                break;
            }
            case DISCONNECT: {
                DisconnectMessage message = (DisconnectMessage) msg;
                ServerChinczyk chinczyk = (ServerChinczyk) games.get(message.getGameCode());
                if (chinczyk == null || !chinczyk.isConnected(ctx.channel())) {
                    ctx.writeAndFlush(new DisconnectResponseMessage(message.getGameCode(), DisconnectResponseMessage.Status.NOT_CONNECTED));
                    return;
                }
                chinczyk.removeConnection(ctx.channel(), true);
                break;
            }
            case GAME: {
                if (!(msg instanceof ClientGameMessage)) throw new IllegalArgumentException("nieoczekiwany typ wiadomości");
                ClientGameMessage message = (ClientGameMessage) msg;
                ServerChinczyk chinczyk = (ServerChinczyk) games.get(message.getGameCode());
                if (chinczyk == null || !chinczyk.isConnected(ctx.channel())) {
                    ctx.writeAndFlush(message.getInvalidGameMessage());
                    return;
                }
                chinczyk.handleEvent(ctx, message);
                break;
            }
            case IDENTIFY:
                throw new IllegalStateException("OP IDENTIFY nie powinno tu trafić");
            default:
                throw new IllegalArgumentException("otrzymano serwerowy OP");
        }
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) {
        for (Chinczyk chinczyk : games.values()) {
            if (((ServerChinczyk) chinczyk).isConnected(ctx.channel()))
                ((ServerChinczyk) chinczyk).removeConnection(ctx.channel(), false);
        }
    }
}
