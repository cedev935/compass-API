package com.gncompass.serverfront.util;

public class Currency {
  private static final long CENTS_FACTOR = 100L;

  // Internals
  private long cents = 0L;

  public Currency() {
  }

  public Currency(double value) {
    parse(value);
  }

  public Currency(float value) {
    parse(value);
  }

  private Currency(long cents) {
    this.cents = cents;
  }

  /*=============================================================
   * PRIVATE FUNCTIONS
   *============================================================*/

  private void parse(double value) {
    if(value > Integer.MIN_VALUE && value < Integer.MAX_VALUE) {
      cents = Math.round(value * CENTS_FACTOR);
    } else {
      throw new RuntimeException("Parsed currency value is out of range");
    }
  }

  private void parse(float value) {
    parse((double) value);
  }

  /*=============================================================
   * PUBLIC FUNCTIONS
   *============================================================*/

  public Currency add(Currency currency) {
    return new Currency(cents + currency.cents);
  }

  public double doubleValue() {
    return new Long(cents).doubleValue() / CENTS_FACTOR;
  }

  public float floatValue() {
    return new Long(cents).floatValue() / CENTS_FACTOR;
  }

  public boolean lessThanZero() {
    return (cents < 0);
  }

  public void setToZero() {
    cents = 0;
  }

  public Currency subtract(Currency currency) {
    return new Currency(cents - currency.cents);
  }
}
