// Created by Andrew Wonnacott

import java.util.List;
import java.util.*;
import java.io.*;

public class Bidder_zhang implements Bidder {
  private double budget;
  private static final double factor = 1;
  private int trials;
  private double[] avgWinBids;
  private double avg_gain;
  private static final int TRUTHFUL_UNTIL = 200;
  private static final double PROFIT_RATE_FLOOR = 0;
  private static final boolean BY_RATE = false;
  private static final double ERR = 0.2;
  private static final int DEPOSIT = 10;
  private double deposit;
  private int no_money_trial;
  private double min_pay;

  private int[] cnt;
  private int[][] win_cnt;
  private int num;

  public Bidder_zhang(double budget){
    this.budget = budget;
    this.trials = 0;
    this.avg_gain = 0;
    this.avgWinBids = new double[10];
    this.no_money_trial = 10001;
    if(DEPOSIT <= this.budget * 0.1){
      this.budget -= DEPOSIT;
      this.deposit = DEPOSIT;
    } else{
      this.budget -= this.budget * 0.1;
      this.deposit = this.budget * 0.1;
    }
    this.min_pay = this.budget / 10000;

    this.cnt = new int[4];
    this.win_cnt = new int[4][4];
  }

  public static Bidder New(double budget){
    return new Bidder_zhang(budget);
  }

  // given your value for the day, determine an action
  public double getBid(double v){
    this.trials++;
    if(this.trials >= this.no_money_trial){
      //double[] res = getOptBid(v);
      //double expected_gain = res[1];
      //double expected_bid = res[0];
      //return expected_gain < (v - this.min_pay) * 0.015 ? this.min_pay : expected_bid;
      return this.min_pay;
    }
    double bid = Math.min(v, this.budget);

    if(this.trials > TRUTHFUL_UNTIL){
      bid = getOptBid(v)[0];
    }
    return bid;
  }

  private double[] getOptBid(double v){
    double first_pay = avgWinBids[0] + ERR;
    double second_pay = avgWinBids[1] * 0.6 + avgWinBids[2] * 0.4;
    double third_pay = avgWinBids[8] * 0.8 + avgWinBids[6] * 0.2;
    double last_pay = Math.min(this.min_pay, this.budget);

    double first_gain = this.budget >= first_pay ? (v - avgWinBids[0]) * 0.05 : 0;
    double second_gain = this.budget >= second_pay ? (v - avgWinBids[1]) * 0.035 : 0;
    double third_gain = this.budget >= third_pay ? (v - avgWinBids[8]) * 0.015 : 0;

    if(!BY_RATE) {
      if (first_gain > second_gain && first_gain > third_gain && first_gain > 0) {
        this.cnt[0]++;
        this.num = 0;
        return new double[]{first_pay, first_gain};
      } else if (second_gain > first_gain && second_gain > third_gain && second_gain > 0) {
        this.cnt[1]++;
        this.num = 1;
        return new double[]{second_pay, second_gain};
      } else if (third_gain > 0) {
        this.cnt[2]++;
        this.num = 2;
        return new double[]{third_pay, third_gain};
      } else {
        this.cnt[3]++;
        this.num = 3;
        return new double[]{last_pay, 0};
      }
    } else{
      double first_gain_rate = first_gain / (first_pay * 0.05);
      double second_gain_rate = second_gain / (second_pay * 0.035);
      double third_gain_rate = third_gain / (third_pay * 0.015);

      if(first_gain_rate > second_gain_rate && first_gain_rate > third_gain_rate && first_gain_rate > PROFIT_RATE_FLOOR){
        return new double[]{first_pay, first_gain};
      } else if(second_gain_rate > first_gain_rate && second_gain_rate > third_gain_rate && second_gain_rate > PROFIT_RATE_FLOOR){
        return new double[]{second_pay, second_gain};
      } else if(third_gain_rate > PROFIT_RATE_FLOOR){
        return new double[]{third_pay, third_gain};
      }
      return new double[]{0, 0};
    }
  }

  // callback function with results
  public void addResults(List<Double> bids, int myBid, double myPayment) {
    // record my utility and budget
    if (myBid >= 0) {
      budget -= myPayment;
      int idx = myBid;
      if(myBid > 1){
        idx = 2;
      }
      if(this.trials > TRUTHFUL_UNTIL) {
        this.win_cnt[this.num][idx]++;
      }
    } else{
      if(this.trials > TRUTHFUL_UNTIL) {
        this.win_cnt[this.num][3]++;
      }
    }
    List<Double> copy = new ArrayList<>(bids);
    Collections.sort(copy);
    for(int i = 9; i >= 0; i--){
      avgWinBids[9 - i] = (avgWinBids[9 - i] * (this.trials - 1) + copy.get(i)) / this.trials;
    }
    if(copy.get(0) < 0.015 && this.no_money_trial == 10001){
      this.budget += this.deposit;
      this.deposit = 0;
      this.no_money_trial = this.trials;
      this.min_pay = this.budget / (10000 - this.trials);
    }

    if(this.trials == 1000 || this.trials == 8000){
      //System.err.println("trial = " + this.trials + "; average bids = " + Arrays.toString(this.avgWinBids));
    }
    if(this.trials == 10000){
      //System.err.println(Arrays.toString(this.cnt));
      for(int i = 0; i < this.win_cnt.length; i++){
        //System.err.println(Arrays.toString(this.win_cnt[i]));
      }
    }
  }
}
