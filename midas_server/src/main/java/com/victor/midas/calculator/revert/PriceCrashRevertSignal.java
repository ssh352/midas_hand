package com.victor.midas.calculator.revert;

import com.victor.midas.calculator.common.IndexCalcBase;
import com.victor.midas.calculator.indicator.IndexChangePct;
import com.victor.midas.calculator.indicator.kline.IndexKLine;
import com.victor.midas.calculator.macd.IndexMACD;
import com.victor.midas.calculator.util.MathStockUtil;
import com.victor.midas.calculator.util.MaxMinUtil;
import com.victor.midas.model.common.StockState;
import com.victor.midas.model.vo.CalcParameter;
import com.victor.midas.train.common.MidasTrainOptions;
import com.victor.midas.util.MidasException;
import com.victor.utilities.math.stats.ma.MaBase;
import com.victor.utilities.math.stats.ma.SMA;
import com.victor.utilities.utils.MathHelper;

/**
 * calculate PriceCrashRevertSignal
 */
public class PriceCrashRevertSignal extends IndexCalcBase {

    public static final String INDEX_NAME = "pcrs";
    private MaBase maMethod = new SMA();

    private double[] score;
    private double[] dif, dea, macdBar;
    private double[] pMa5, pMa10, pMa20, pMa60, vMa5;
    private double[] middleShadowPct, upShadowPct, downShadowPct;
    private MaxMinUtil mmPriceUtil5;
    private int sellIndex;
    private StockState state;

    public PriceCrashRevertSignal(CalcParameter parameter) {
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
        pMa5 = maMethod.calculate(end, 5);
        pMa10 = maMethod.calculate(end, 10);
        pMa20 = maMethod.calculate(end, 20);
        pMa60 = maMethod.calculate(end, 60);
        vMa5 = maMethod.calculate(total, 5);

        sellIndex = -1;
        state = StockState.HoldMoney;
        for (int i = 5; i < len; i++) {
//            if(dates[i] == 20160411){
//                System.out.println("wow");
//            }

            if(state == StockState.HoldMoney) {
                if (changePct[i] < 0 && changePct[i - 1] < 0 && changePct[i - 2] > 0
                        && end[i] <= start[i] && end[i - 1] <= start[i - 1] && end[i - 2] >= start[i - 2]
                        && max[i] > min[i - 1]
                        && total[i] < total[i - 1] * 1.5
                        //&& (i + 1 < len && start[i + 1] < end[i])   // this condition need to remove in real, you cannot know tomorrow's open
                        && macdBar[i] >= 0
                        && MathStockUtil.calculateChangePct(min[i], end[i]) < 0.01
                        && howManyMaUp(i) >= 3) {
                    int maxIndex = mmPriceUtil5.getMaxIndex(i);
                    int minIndex = mmPriceUtil5.getMinIndex(maxIndex);
                    double maxPrice = mmPriceUtil5.getMaxPrice(maxIndex);
                    double minPrice = mmPriceUtil5.getMinPrice(minIndex);
                    if(MathHelper.isMoreAbs(maxPrice - end[i], maxPrice - minPrice, 0.61)){
                        setBuy(4.6d, i);
                    }
                }
                if(changePct[i] < 0 && macdBar[i] >= 0 && isMaBullForm(i - 1)){
                    if((MathHelper.isLessAbs(MathStockUtil.calculateChangePct(pMa20[i], min[i]), 0.01)
                            || MathHelper.isLessAbs(MathStockUtil.calculateChangePct(pMa20[i], end[i]), 0.01))
                            && changePct[i] > -0.097d
                            && Math.abs(middleShadowPct[i]) < 0.049
                            && Math.abs(upShadowPct[i]) > Math.abs(downShadowPct[i])
                            && MathHelper.isInRange(end[i], pMa20[i], pMa10[i])
                            && MathHelper.isLessAbs(macdBar[i - 1], macdBar[i], 3.2)
                            && min[i] < min[i - 1]
                            ){
                        int maxIndex = mmPriceUtil5.getMaxIndex(i);
                        double maxPrice = mmPriceUtil5.getMaxPrice(maxIndex);
                        if(MathStockUtil.calculateChangePct(max[i], maxPrice) > 0.01d){
                            setBuy(4.3d, i);
                        }
                    }
                }
            } else if(state == StockState.HoldStock && i == sellIndex){
                score[i] = -5d;
                state = StockState.HoldMoney;
                sellIndex = -1;
            }
//            score[i] = lastSection.status1.ordinal();
        }
        addIndexData(INDEX_NAME, score);
    }

    private void setBuy(double currentScore, int idx){
        if(currentScore > score[idx]){
            score[idx] = currentScore;
            state = StockState.HoldStock;
            sellIndex = idx + 2;
        }
    }

    private boolean isMaBullForm(int i){
        return pMa60[i] < pMa20[i] && pMa20[i] < pMa10[i] && pMa10[i] < pMa5[i];
    }

    private int howManyMaUp(int i){
        int cnt = 0;
        if(pMa5[i] > pMa5[i - 1]) cnt++;
        if(pMa10[i] > pMa10[i - 1]) cnt++;
        if(pMa20[i] > pMa20[i - 1]) cnt++;
        if(pMa60[i] > pMa60[i - 1]) cnt++;
        return cnt;
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
