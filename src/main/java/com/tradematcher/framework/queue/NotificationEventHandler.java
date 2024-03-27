package com.tradematcher.framework.queue;

import com.tradematcher.entity.Event;
import com.tradematcher.entity.Market;
import com.tradematcher.entity.MatchResult;
import com.tradematcher.framework.command.*;
import com.tradematcher.framework.command.vo.ResultInfo;
import com.tradematcher.usecase.MatchRequest;
import com.tradematcher.util.Constants;
import com.lmax.disruptor.EventHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * 撮合请求事后事件通知器
 * @program: new-match-engine
 * @author: TradeMatcher
 * @create: 2023-04-24 10:10
 **/
@Slf4j
public class NotificationEventHandler implements EventHandler<MatchRequest> {
	private final IEventsHandler eventsHandler;

	public NotificationEventHandler(IEventsHandler eventsHandler) {
		this.eventsHandler = eventsHandler;
	}

	@Override
	public void onEvent(MatchRequest request, long sequence, boolean endOfBatch) throws Exception {
		try {

			sendCommandResult(request);

			MatchResult matchResult = request.getMatchResult();
			if (matchResult != null) {
				sendTradeEvents(request);
				sendMarketData(request);
			}
		} catch (Exception e) {
			log.error("NotificationEventHandler error", e);
		}
	}

	private void sendCommandResult(MatchRequest matchRequest) {
		String instruction = matchRequest.getInstructionType();
		CommandResult commandResult = switch (instruction) {
			case Constants.Instruction.PLACE_FOK_ORDER, Constants.Instruction.PLACE_GTC_ORDER, Constants.Instruction.PLACE_MTL_ORDER, Constants.Instruction.PLACE_FAK_ORDER, Constants.Instruction.PLACE_LIMIT_FOK_ORDER ->
					new PlaceOrderCommandResult(ResultInfo.getResultInfo(matchRequest.getCode()));
			case Constants.Instruction.CANCEL_ORDER ->
					new CancelOrderCommandResult(ResultInfo.getResultInfo(matchRequest.getCode()));
			case Constants.Instruction.LIST_SYMBOL ->
					new ListSymbolCommandResult(ResultInfo.getResultInfo(matchRequest.getCode()));
			case Constants.Instruction.QUERY_ORDER ->
					new QueryOrderCommandResult(ResultInfo.getResultInfo(matchRequest.getCode()));
			default -> null;
		};

		if (commandResult != null) {
			commandResult.convertToCommand(matchRequest);
			eventsHandler.commandResult(commandResult);
		}
	}

	private void sendTradeEvents(MatchRequest request) {
		log.info("trade event: " + request);

		MatchResult matchResult = request.getMatchResult();
		Event event = matchResult.getEvent();

		while (event != null) {
			byte type = event.getType();
			if (type == Constants.EventType.MAKER) {
				IEventsHandler.Maker maker = IEventsHandler.convertToMaker(event, request);

				eventsHandler.makerEvent(maker);
			} else if (type == Constants.EventType.REJECT) {
				IEventsHandler.Reject reject = IEventsHandler.convertToReject(event, request);

				eventsHandler.rejectEvent(reject);
			}

			event = event.getNext();
		}

		if (matchResult.isTakerCompleted()) {
			IEventsHandler.Taker taker = IEventsHandler.convertToTaker(matchResult, request);

			eventsHandler.takerEvent(taker);
		}
	}

	private void sendMarketData(MatchRequest request) {
		Market market = request.getMatchResult().getMarket();
		if (market != null) {
			int askDepth = market.getAskDepth();
			final List<IEventsHandler.RecordInfo> asks = new ArrayList<>(askDepth);
			for (int i = 0; i < askDepth; i++) {
				asks.add(new IEventsHandler.RecordInfo(market.getAskUnitPrices()[i].toString(),
						market.getAskSizes()[i].toString(), market.getAskOrderNumbers()[i]));
			}
			int bidDepth = market.getBidDepth();
			final List<IEventsHandler.RecordInfo> bids = new ArrayList<>(bidDepth);
			for (int i = 0; i < bidDepth; i++) {
				bids.add(new IEventsHandler.RecordInfo(market.getBidUnitPrices()[i].toString(),
						market.getBidSizes()[i].toString(), market.getBidOrderNumbers()[i]));
			}
			eventsHandler.marketEvent(new IEventsHandler.OrderBookInfo(request.getSymbolID(), market.getSymbolDecimal(),
					market.getVolumeDigits(), asks, bids, System.currentTimeMillis()));
		}
	}
}
