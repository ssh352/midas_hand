package com.victor.midas.model.vo.score;


import com.victor.midas.train.common.MidasTrainOptions;
import com.victor.midas.train.common.TrainOptionApply;
import com.victor.midas.util.ModelConvertor;

/**
 * represent a score
 */
public class StockScore implements Comparable<StockScore>, TrainOptionApply {

    private String stockCode;

    private String conceptName;

    private double score, perf;

    private int cob;        // prediction day cob
    public int holdingPeriod = -1;      // if negative means no override, let PerfCollector decide when to sell
    public int holdHalfPeriod = -1;

    public int buyCob, sellCob;
    public int buyIndex, sellIndex;
    public int buyTiming, sellTiming;
    public double marketPerf;
    public double dailyExcessReturn;

    public StockScore() {
    }

    public StockScore(String stockCode, double score, int cob) {
        this.stockCode = stockCode;
        this.score = score;
        this.cob = cob;
    }

    public StockScore(String stockCode, String conceptName, double score, int cob) {
        this.stockCode = stockCode;
        this.conceptName = conceptName;
        this.score = score;
        this.cob = cob;
    }

    public void calculateDailyExcessReturn(){
        if(holdingPeriod > 0){
            dailyExcessReturn = (perf - marketPerf) / holdingPeriod;
        }
    }

    public String getConceptName() {
        return conceptName;
    }

    public void setConceptName(String conceptName) {
        this.conceptName = conceptName;
    }

    public String getStockCode() {
        return stockCode;
    }

    public void setStockCode(String stockCode) {
        this.stockCode = stockCode;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public double getPerf() {
        return perf;
    }

    public void setPerf(double perf) {
        this.perf = perf;
    }

    public int getCob() {
        return cob;
    }

    public void setCob(int cob) {
        this.cob = cob;
    }

    public int getBuyCob() {
        return buyCob;
    }

    public void setBuyCob(int buyCob) {
        this.buyCob = buyCob;
    }

    public int getSellCob() {
        return sellCob;
    }

    public void setSellCob(int sellCob) {
        this.sellCob = sellCob;
    }

    public int getBuyTiming() {
        return buyTiming;
    }

    public void setBuyTiming(int buyTiming) {
        this.buyTiming = buyTiming;
    }

    public int getSellTiming() {
        return sellTiming;
    }

    public void setSellTiming(int sellTiming) {
        this.sellTiming = sellTiming;
    }

    public double getMarketPerf() {
        return marketPerf;
    }

    public void setMarketPerf(double marketPerf) {
        this.marketPerf = marketPerf;
    }

    public int getHoldingPeriod() {
        return holdingPeriod;
    }

    public void setHoldingPeriod(int holdingPeriod) {
        this.holdingPeriod = holdingPeriod;
    }

    @Override
    public int compareTo(StockScore o) {
        return (Double.valueOf(score)).compareTo(o.getScore());
    }

    @Override
    public String toString() {
        return "StockScore{" +
                "stockCode='" + stockCode + '\'' +
                ", score=" + score +
                ", perf=" + perf +
                ", cob=" + cob +
                ", holdingPeriod=" + holdingPeriod +
                ", buyCob=" + buyCob +
                ", buyTiming=" + ModelConvertor.getTimingString(buyTiming) +
                ", sellCob=" + sellCob +
                ", sellTiming=" + ModelConvertor.getTimingString(sellTiming) +
                ", marketPerf=" + marketPerf +
                ", dailyExcessReturn=" + dailyExcessReturn +
                ", conceptName='" + conceptName + '\'' +
                '}';
    }

    @Override
    public void applyOptions(MidasTrainOptions options) {
        buyTiming = options.buyTiming;
        sellTiming = options.sellTiming;
    }
}
