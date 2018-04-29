package com.gncompass.serverfront.util;

import java.util.concurrent.TimeUnit;
import java.util.Date;

public class PaymentHelper {
  public static final int DAYS_PER_YEAR = 365;
  public static final int MONTHS_PER_YEAR = 12;

  /**
   * Returns the number of days in the given period between two dates
   * @param lastPayment the date of the last payment
   * @param currentPayment the date of the current payment
   * @return the number of days
   */
  public static long daysInPeriod(Date lastPayment, Date currentPayment) {
    return TimeUnit.MILLISECONDS.toDays(currentPayment.getTime() - lastPayment.getTime());
  }

  /**
   * Calculates the payment per period for a given core fixed term loan
   * @param principal the principal amount of the loan
   * @param ratePerPeriod the interest rate per period (as decimal)
   * @param totalPeriods the total number of periods in the loan
   */
  public static Currency paymentPerPeriod(Currency principal, double ratePerPeriod,
                                          int totalPeriods) {
    return new Currency(principal.doubleValue() * ratePerPeriod
                        / (1 - Math.pow(1 + ratePerPeriod, -totalPeriods)));
  }
}
