package com.victor.midas.calculator.follow;

import com.victor.midas.calculator.common.IndexCalcBase;
import com.victor.midas.calculator.indicator.IndexChangePct;
import com.victor.midas.calculator.indicator.kline.IndexKLine;
import com.victor.midas.calculator.macd.IndexMACD;
import com.victor.midas.calculator.macd.model.MacdSection;
import com.victor.midas.calculator.macd.model.MacdSectionUtil;
import com.victor.midas.calculator.util.LineBreakoutUtil;
import com.victor.midas.calculator.util.MathStockUtil;
import com.victor.midas.calculator.util.MaxMinUtil;
import com.victor.midas.calculator.util.PriceLimitUtil;
import com.victor.midas.calculator.util.model.LineCrossSection;
import com.victor.midas.model.common.StockState;
import com.victor.midas.model.vo.CalcParameter;
import com.victor.midas.train.common.MidasTrainOptions;
import com.victor.midas.util.MidasException;
import com.victor.utilities.math.stats.ma.MaBase;
import com.victor.utilities.math.stats.ma.SMA;
import com.victor.utilities.utils.MathHelper;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.regression.SimpleRegression;

import java.util.List;

/**
 * calculate TrendFollowSignal
 */
public class TrendFollowSignal extends IndexCalcBase {

    public static final String INDEX_NAME = "score_tfs";
    private MaBase maMethod = new SMA();

    private double[] score;
    private double[] dif, dea, macdBar;
    private double[] pMa5, pMa60, vMa5;
    private double[] middleShadowPct, upShadowPct, downShadowPct;
    private MaxMinUtil mmPriceUtil5;
    private int sellIndex, buyIndex;
    private PriceLimitUtil priceLimitUtil = new PriceLimitUtil(8);
    private LineBreakoutUtil lineBreakoutUtil = new LineBreakoutUtil();
    private MacdSectionUtil macdSectionUtil = new MacdSectionUtil();
    private MacdSection lastSection;
    private List<MacdSection> greenSections;

    public TrendFollowSignal(CalcParameter parameter) {
        super(parameter);
    }

    @Override
    public void setRequiredCalculators() {
        requiredCalculators.add(IndexChangePct.INDEX_NAME);
        requiredCalculators.add(IndexKLine.INDEX_NAME);
        requiredCalculators.add(IndexMACD.INDEX_NAME);
    }

    @Override
    public String getIndexName() {
        return INDEX_NAME;
    }

    @Override
    public void calculate() throws MidasException {
        priceLimitUtil.init(end, start, max, min, changePct);
        pMa5 = maMethod.calculate(end, 5);
        pMa60 = maMethod.calculate(end, 60);
        vMa5 = maMethod.calculate(total, 5);
        lineBreakoutUtil.init(pMa5, pMa60);
        macdSectionUtil.init(min, max, end, macdBar);
        lastSection = macdSectionUtil.lastSection;
        greenSections = macdSectionUtil.greenSections;

        DescriptiveStatistics underMa5 = new DescriptiveStatistics();
        LineCrossSection currentSection, previousSection;
        boolean isBreakout = false;
        int breakoutIndex = 0, maxIndex, minIndex;
        long underMa5Cnt = 0;
        double avgChangePctUnderMa5 = 0d;

        int startIndex = -1;
        double startPrice = 0d;
        SimpleRegression regression = new SimpleRegression();
        DescriptiveStatistics changePctStats = new DescriptiveStatistics();

        sellIndex = -1;
        state = StockState.HoldMoney;
        for (itr = 5; itr < len; itr++) {
//            if(dates[i] == 20150703){
//                System.out.println("wow");
//            }
            priceLimitUtil.updateStats(itr);
            lineBreakoutUtil.update(itr);
            macdSectionUtil.update(itr);
            lastSection = macdSectionUtil.lastSection;
            currentSection = lineBreakoutUtil.currentSection;
            previousSection = lineBreakoutUtil.previousSection;

            if(start[itr - 1] + end[itr - 1] < pMa5[itr - 1] * 2 && end[itr] + start[itr] > pMa5[itr] * 2  && end[itr] > pMa5[itr]){
                isBreakout = true;
                breakoutIndex = itr;
                currentSection.breakoutIndexes.add(breakoutIndex);
                underMa5Cnt = underMa5.getN();
                avgChangePctUnderMa5 = underMa5.getMean();
            }
            if(isBreakout && itr - breakoutIndex > 4) isBreakout = false;
            if(end[itr] + start[itr] < pMa5[itr] * 2){
                underMa5.addValue(changePct[itr]);
            } else {
                underMa5.clear();
            }

            if(startIndex == mmPriceUtil5.getMinIndexRecursive(itr)){
                regression.addData(itr - startIndex, end[itr] / startPrice);
                changePctStats.addValue(Math.abs(changePct[itr]));
            } else if(end[itr] > 0.1d){
                startIndex = mmPriceUtil5.getMinIndexRecursive(itr);
                startPrice = end[itr];
                regression.clear();
                regression.addData(0d, 1d);
                changePctStats.clear();
            }

            if(previousSection == null || currentSection == null) continue;

            if(state == StockState.HoldMoney) {
                if(startIndex > 0 && regression.getN() > 3 && regression.getN() < 10
                        && regression.getSlope() > 0.016
                        && changePct[itr - 1] < -0.02d && MathStockUtil.calculateChangePct(pMa5[itr - 1], end[itr - 1]) < -0.01d
                        && changePct[itr] > 0d && end[itr] * 2 > start[itr - 1] + end[itr - 1]
                        && MathHelper.isLessAbs(total[itr - 1], total[itr - 2], 2.1d)
                        && changePctStats.getMean() < 0.086
                        && changePct[itr] < 0.092
                        //&& MathStockUtil.calculateChangePct(pMa5[i - 1], pMa5[i]) > singleDouble
                        ){
                    setBuy(4.6d, itr);
                }
//                if (isBreakout
//                        && Math.min(start[i], end[i]) > pMa5[i]
//                        && changePct[i] < 0d
//                        && avgChangePctUnderMa5 > -0.028
//                        && total[i] < total[breakoutIndex]
//                        && Math.abs(middleShadowPct[breakoutIndex - 1]) < 0.02d
//                        ) {
//                    minIndex = mmPriceUtil5.getMaxIndex(breakoutIndex);
//                    if(breakoutIndex - minIndex < 2 && currentSection.type == MacdSectionType.green && underMa5Cnt > 5){
//                        maxIndex = mmPriceUtil5.getMaxIndex(minIndex);
//                        if(Math.abs(MathStockUtil.calculateChangePct(mmPriceUtil5.getMaxPrice(maxIndex), mmPriceUtil5.getMaxPrice(i))) > 0.02){
//                            setBuy(4.6d, i);
//                        }
//                    }
//                }
            } else if(state == StockState.HoldStock){
                if(itr > buyIndex && macdBar[itr] < macdBar[itr - 1]){
                    score[itr] = -5d;
                    state = StockState.HoldMoney;
                    sellIndex = -1;
                }
            }
//            score[i] = lastSection.status1.ordinal();
        }
        addIndexData(INDEX_NAME, score);
    }

    private boolean isDecrease(double[] value, int from, int to){
        for (int i = from + 1; i <= to; i++) {
            if(value[i] > value[i - 1]) return false;
        }
        return true;
    }

    private void setBuy(double currentScore, int idx){
        if(currentScore > score[idx]){
            score[idx] = currentScore;
            state = StockState.HoldStock;
            buyIndex = idx + 1;
            sellIndex = idx + 2;
        }
    }

    @Override
    protected void initIndex() throws MidasException {
        dif = (double[])stock.queryCmpIndex("dif");
        dea = (double[])stock.queryCmpIndex("dea");
        macdBar = (double[])stock.queryCmpIndex("macdBar");
        middleShadowPct = (double[])stock.queryCmpIndex("k_m");
        upShadowPct = (double[])stock.queryCmpIndex("k_u");
        downShadowPct = (double[])stock.queryCmpIndex("k_d");
        score = new double[len];
        mmPriceUtil5 = new MaxMinUtil(stock, false);
        mmPriceUtil5.calcMaxMinIndex(5);
    }

    @Override
    public MidasTrainOptions getTrainOptions() {
        MidasTrainOptions options = new MidasTrainOptions();
        options.selectTops = false;
        options.useSignal = true;
        return options;
    }
}
