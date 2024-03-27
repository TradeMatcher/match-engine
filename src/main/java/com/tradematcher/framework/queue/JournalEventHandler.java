package com.tradematcher.framework.queue;

import com.tradematcher.usecase.MatchRequest;
import com.tradematcher.util.Constants;
import com.lmax.disruptor.EventHandler;
import journal.io.api.Journal;
import journal.io.api.JournalBuilder;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * 撮合请求事前日志记录器
 * @program: new-match-engine
 * @author: TradeMatcher
 * @create: 2023-04-24 10:10
 **/
@Slf4j
public class JournalEventHandler implements EventHandler<MatchRequest> {
	private static final List<String> REDO_INSTRUCTION = List.of(Constants.Instruction.CANCEL_ORDER,
			Constants.Instruction.PLACE_FOK_ORDER, Constants.Instruction.PLACE_FAK_ORDER,
			Constants.Instruction.PLACE_MTL_ORDER, Constants.Instruction.PLACE_GTC_ORDER);

	private final String directory;
	private final int snapshotCount;
	private Journal journal;
	private long count;

	public JournalEventHandler(String directory, int snapshotCount) {
		this.directory = directory;
		this.snapshotCount = snapshotCount;
	}

	@Override
	public void onEvent(MatchRequest request, long sequence, boolean endOfBatch) throws Exception {
		if (request.getCode() == Constants.Code.MATCHING_RESTORE) {
			return;
		}

		if (!REDO_INSTRUCTION.contains(request.getInstructionType())) {
			return;
		}

		if (journal != null && count % snapshotCount == 0) {
			long timestamp = System.currentTimeMillis();
			journal = getJournal(timestamp);
			request.setSnapshot(timestamp);
		}

		if (journal == null) {
			long timestamp = System.currentTimeMillis();
			journal = getJournal(timestamp);
			request.setSnapshot(timestamp);
		}

		journal.write(request.serialize().getBytes(StandardCharsets.UTF_8), Journal.WriteType.SYNC);

		count++;
	}

	private Journal getJournal(long timestamp) throws IOException {
		String journalDirectory = directory + timestamp;
		Path dir = Paths.get(journalDirectory);
		Files.createDirectories(dir);
		return JournalBuilder.of(dir.toFile()).setFileSuffix(".journal").setPhysicalSync(true).open();
	}
}
