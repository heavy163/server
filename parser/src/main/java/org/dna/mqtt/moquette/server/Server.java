package org.dna.mqtt.moquette.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.logging.Logger;
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.demux.DemuxingProtocolDecoder;
import org.apache.mina.filter.codec.demux.DemuxingProtocolEncoder;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.dna.mqtt.moquette.messaging.spi.impl.SimpleMessaging;
import org.dna.mqtt.moquette.proto.ConnAckEncoder;
import org.dna.mqtt.moquette.proto.ConnectDecoder;
import org.dna.mqtt.moquette.proto.DisconnectDecoder;
import org.dna.mqtt.moquette.proto.PublishDecoder;
import org.dna.mqtt.moquette.proto.SubAckEncoder;
import org.dna.mqtt.moquette.proto.SubscribeDecoder;
import org.dna.mqtt.moquette.proto.messages.ConnAckMessage;
import org.dna.mqtt.moquette.proto.messages.SubAckMessage;
/**
 * Launch a  configured version of the server.
 * @author andrea
 */
public class Server {
    
    private static final Logger LOG = Logger.getLogger(Server.class.getName());
    
    public static final int PORT = 9191;
    public static final int DEFAULT_CONNECT_TIMEOUT = 10;
    
    public static void main(String[] args) throws IOException {
        new Server().startServer();
        
    }
    
    protected void startServer() throws IOException {
        DemuxingProtocolDecoder decoder = new DemuxingProtocolDecoder();
        decoder.addMessageDecoder(new ConnectDecoder());
        decoder.addMessageDecoder(new PublishDecoder());
        decoder.addMessageDecoder(new SubscribeDecoder());
        decoder.addMessageDecoder(new DisconnectDecoder());
        
        DemuxingProtocolEncoder encoder = new DemuxingProtocolEncoder();
//        encoder.addMessageEncoder(ConnectMessage.class, new ConnectEncoder());
        encoder.addMessageEncoder(ConnAckMessage.class, new ConnAckEncoder());
        encoder.addMessageEncoder(SubAckMessage.class, new SubAckEncoder());
        
        IoAcceptor acceptor = new NioSocketAcceptor();

        acceptor.getFilterChain().addLast( "logger", new LoggingFilter("SERVER LOG") );
        acceptor.getFilterChain().addLast( "codec", new ProtocolCodecFilter(encoder, decoder));

        MQTTHandler handler = new MQTTHandler();
        SimpleMessaging messaging = new SimpleMessaging();
        //TODO fix this hugly wiring
        handler.setMessaging(messaging);
        messaging.setNotifier(handler);
        
        
        acceptor.setHandler(handler);
        ((NioSocketAcceptor)acceptor).getSessionConfig().setReuseAddress(true);
        acceptor.getSessionConfig().setReadBufferSize( 2048 );
        acceptor.getSessionConfig().setIdleTime( IdleStatus.BOTH_IDLE, DEFAULT_CONNECT_TIMEOUT );
        acceptor.bind( new InetSocketAddress(PORT) );
        LOG.info("Server binded");
    }
    
}
