package com.hackathon.nova.database;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExecutorThread {
  private static final ExecutorService executorService = Executors.newFixedThreadPool(1);

  public static ExecutorService getExecutor() {
    return executorService;
  }
}
