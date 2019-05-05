package com.songoda.kingdoms.database;

import com.songoda.kingdoms.Kingdoms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

public class DatabaseTransferTask<T> implements Runnable {
	
	private final List<TransferPair<T>> pairs = new ArrayList<>();
	private Kingdoms instance;

	public DatabaseTransferTask(Kingdoms instance, TransferPair<T> pair) {
		this.instance = instance;
		this.pairs.add(pair);
	}
	
	public DatabaseTransferTask(Kingdoms instance, Collection<TransferPair<T>> pairs) {
		this.instance = instance;
		this.pairs.addAll(pairs);
	}

	@Override
	public void run() {
		int slot = 0;
		Logger logger = instance.getLogger();
		for (TransferPair<T> pair : pairs) {
			Database<T> from = pair.from;
			Database<T> to = pair.to;
			Set<String> keys = from.getKeys();
			int i = 0, percentage = -1;
			for (String key : keys) {
				try {
					T data = from.get(key);
					to.put(key, data);
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					if (getPercentage(i, keys.size()) % 5 == 0) {
						percentage = getPercentage(i, keys.size());
						logger.info(slot + ". " + pair.toString());
						logger.info("Transfer [" + percentage + "%] done...");
					}
					i++;
				}
			}
			logger.info(slot + ". " + pair.toString());
			logger.info("Transfer [100%] finished!");
			slot++;
		}
		System.gc();
	}

	private int getPercentage(int cur, int outOf) {
		return (int) (((double) cur / outOf) * 100);
	}

	public static class TransferPair<T> {
		
		private Database<T> from;
		private Database<T> to;
		
		public TransferPair(Database<T> from, Database<T> to) {
			this.from = from;
			this.to = to;
		}

		@Override
		public String toString() {
			return from.getClass().getSimpleName() + " ==> " + to.getClass().getSimpleName();
		}

	}

}
