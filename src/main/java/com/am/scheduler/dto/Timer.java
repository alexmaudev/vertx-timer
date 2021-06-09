package com.am.scheduler.dto;

public class Timer {

  private String id;

  private Long delay;

  private boolean isRecurring;

  private boolean isActive;

  private String callbackURI;

  public Timer() {
  }

  public Timer(String id, Long delay) {
    this.id = id;
    this.delay = delay;
  }

  public Timer(String id, Long delay, boolean isRecurring, boolean isActive, String callbackURI) {
    this.id = id;
    this.delay = delay;
    this.isRecurring = isRecurring;
    this.isActive = isActive;
    this.callbackURI = callbackURI;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Long getDelay() {
    return delay;
  }

  public void setDelay(Long delay) {
    this.delay = delay;
  }

  public boolean isRecurring() {
    return isRecurring;
  }

  public void setRecurring(boolean recurring) {
    isRecurring = recurring;
  }

  public boolean isActive() {
    return isActive;
  }

  public void setActive(boolean active) {
    isActive = active;
  }

  public String getCallbackURI() {
    return callbackURI;
  }

  public void setCallbackURI(String callbackURI) {
    this.callbackURI = callbackURI;
  }
}
