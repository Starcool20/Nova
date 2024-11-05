package com.hackathon.nova.database;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DatabaseExecutor {
  private static final ExecutorService executorService = Executors.newFixedThreadPool(2);

  public static ExecutorService getExecutor() {
    return executorService;
  }
}
