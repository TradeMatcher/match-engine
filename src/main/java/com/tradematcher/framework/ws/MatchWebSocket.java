package com.tradematcher.framework.ws;

import com.tradematcher.framework.command.CommandResult;
import com.tradematcher.framework.command.CommonCommandResult;
import com.tradematcher.framework.command.RequestCommand;
import com.tradematcher.framework.command.vo.ResultInfo;
import com.tradematcher.framework.queue.Dispatcher;
import com.tradematcher.util.Constants;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.extern.slf4j.Slf4j;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 撮合请求websocket服务
 * @program: new-match-engine
 * @author: TradeMatcher
 * @create: 2023-04-24 15:56
 **/
@Slf4j
public class MatchWebSocket extends WebSocketServer {
	/**
	 * <li>key = access key</li>
	 * <li>value = ws session</li>
	 */
	static final Map<String, WebSocket> TRADE_CHANNEL_MAP = new ConcurrentHashMap<>(1);
	static final Map<String, WebSocket> MARKET_CHANNEL_MAP = new ConcurrentHashMap<>(1);
	private static final String DEFAULT_DIRECTORY = "./.backup/";
	private static final int SNAPSHOT_COUNT = 1000;
	private static final int DEFAULT_PORT = 8888;
	private static final String DEFAULT_HOSTNAME = "0.0.0.0";
	private final Dispatcher dispatcher = new Dispatcher(DEFAULT_DIRECTORY, SNAPSHOT_COUNT);
	private final Gson gson = new Gson();

	public MatchWebSocket(InetSocketAddress address, int maxConnections) {
		super(address, maxConnections);
	}

	public static void main(String[] args) throws Exception {
		MatchWebSocket server = new MatchWebSocket(new InetSocketAddress(DEFAULT_HOSTNAME, DEFAULT_PORT), 3);
		server.setConnectionLostTimeout(10);
		server.setReuseAddr(true);
		server.run();

		Files.createDirectories(Paths.get(DEFAULT_DIRECTORY));
	}

	@Override
	public void onOpen(WebSocket ws, ClientHandshake handshake) {
		log.debug("on open");
		String connectionInfo = ws.getResourceDescriptor();
		String[] connectionInfos = connectionInfo.split("/");
		String accessKey = connectionInfos[1];
		String type = connectionInfos[2];
		log.info("websocket accessKey:{} type:{}", accessKey, type);

		if (accessKey == null || accessKey.length() != 32) {
			CommonCommandResult commonCommandResult = new CommonCommandResult(ResultInfo.ACCESS_KEY_ERROR);
			ws.send(gson.toJson(commonCommandResult));
			ws.close();

			return;
		}

		if (!Constants.Match.TRADE_KEY.equals(type) && !Constants.Match.MARKET_KEY.equals(type)) {
			CommonCommandResult commonCommandResult = new CommonCommandResult(ResultInfo.TYPE_ERROR);
			ws.send(gson.toJson(commonCommandResult));
			ws.close();

			return;
		}

		//如果出现重复的accessKey连接，会导致先前已经建立的accessKey连接断连
		if (Constants.Match.TRADE_KEY.equals(type)) {
			WebSocket tradeWebSocket = TRADE_CHANNEL_MAP.putIfAbsent(accessKey, ws);
			if (tradeWebSocket != null) {
				if (tradeWebSocket.isClosed()) {
					TRADE_CHANNEL_MAP.put(accessKey, ws);
				} else {
					log.info("trade ws {} is duplicate", accessKey);
					ws.close();
					tradeWebSocket.close();
					TRADE_CHANNEL_MAP.remove(accessKey);
					return;
				}
			}
		}

		//如果出现重复的accessKey连接，会导致先前已经建立的accessKey连接断连
		if (Constants.Match.MARKET_KEY.equals(type)) {
			WebSocket marketWebSocket = MARKET_CHANNEL_MAP.putIfAbsent(accessKey, ws);
			if (marketWebSocket != null) {
				if (marketWebSocket.isClosed()) {
					MARKET_CHANNEL_MAP.put(accessKey, ws);
				} else {
					log.info("market ws {} is duplicate", accessKey);
					ws.close();
					marketWebSocket.close();
					MARKET_CHANNEL_MAP.remove(accessKey);
					return;
				}
			}
		}

		TRADE_CHANNEL_MAP.forEach((key, value) -> log.info("TRADE_CHANNEL_MAP = " + key + "_" + value.isClosed()));
		MARKET_CHANNEL_MAP.forEach((key, value) -> log.info("MARKET_CHANNEL_MAP = " + key + "_" + value.isClosed()));
	}

	@Override
	public void onClose(WebSocket conn, int code, String reason, boolean remote) {
		log.error("on close {} {} {}", code, reason, remote);
	}

	@Override
	public void onMessage(WebSocket ws, String message) {
		String connectionInfo = ws.getResourceDescriptor();
		String[] connectionInfos = connectionInfo.split("/");
		String accessKey = connectionInfos[1];
		String type = connectionInfos[2];

		JsonObject reqJsonObj = JsonParser.parseString(message).getAsJsonObject();
		//校验请求json串中的accessKey是否存在
		String requestAccessKey = Optional.ofNullable(reqJsonObj.get(Constants.Match.ACCESS_KEY).getAsString())
				.orElse("none");
		if (!TRADE_CHANNEL_MAP.containsKey(requestAccessKey) && ws.getResourceDescriptor()
				.endsWith(Constants.Match.TRADE_KEY)) {
			CommonCommandResult commonCommandResult = new CommonCommandResult(ResultInfo.ACCESS_KEY_NO_CONNECTION);
			log.info("match engine response check accessKey parameters:{}",
					reqJsonObj + "_" + commonCommandResult.getMsg());
			ws.send(gson.toJson(commonCommandResult));
			ws.close();

			return;
		}

		if (!MARKET_CHANNEL_MAP.containsKey(requestAccessKey) && connectionInfo.endsWith(Constants.Match.MARKET_KEY)) {
			CommonCommandResult commonCommandResult = new CommonCommandResult(ResultInfo.ACCESS_KEY_NO_CONNECTION);
			log.info("match engine response check accessKey parameters:{}",
					reqJsonObj + "_" + commonCommandResult.getMsg());
			ws.send(gson.toJson(commonCommandResult));
			ws.close();

			return;
		}

		//校验请求accessKey和连接accessKey是否一致
		RequestCommand command = RequestCommand.getCommandByJson(reqJsonObj, type);
		if (!accessKey.equals(command.getAccessKey())) {
			log.info("request accessKey: {} connection accessKey:{} not match", command.getAccessKey(), accessKey);
			CommonCommandResult commonCommandResult = new CommonCommandResult(ResultInfo.ACCESS_KEY_ERROR);
			ws.send(gson.toJson(commonCommandResult));
			ws.close();

			return;
		}

		CommandResult commandResult = command.check();
		if (ResultInfo.SUCCESS.getCode() != commandResult.getCode()) {
			log.info("match engine response check json parameters:{}", reqJsonObj + "_" + commandResult.getMsg());
			ws.send(gson.toJson(commandResult));

			return;
		}
		log.info("command:{}, json:{}", command.getCommand(), reqJsonObj);
		command.execute(dispatcher);
	}

	@Override
	public void onError(WebSocket conn, Exception ex) {
		log.error("on error", ex);
	}

	@Override
	public void onStart() {
		log.info("match on start");
	}
}
