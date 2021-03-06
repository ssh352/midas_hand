package com.victor.midas.calculator.stats;

import com.victor.midas.calculator.common.IndexCalcBase;
import com.victor.midas.calculator.indicator.IndexChangePct;
import com.victor.midas.model.vo.CalcParameter;
import com.victor.midas.util.MidasException;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

/**
 * this is used for index level results (like IDX000001)
 * used for identify good or bad period for whole stocks
 */
public class IndexBadState extends IndexCalcBase {

    private final static String INDEX_NAME = "isBad";

    private double[] acp;
    private int[] isBad;

    private double[] upShadowPct;
    private double[] downShadowPct;
    private double[] middleShadowPct;
    private double[] pMaLong;               // 60 days price MA

    private static final int INTERVAL = 5;
    private static final double BAD_THRESHOLD = -0.03;
    private static final double RECOVER_THRESHOLD = BAD_THRESHOLD / ( INTERVAL - 1);

    public IndexBadState(CalcParameter parameter) {
        super(parameter);
    }

    @Override
    public String getIndexName() {
        return INDEX_NAME;
    }

    @Override
    public void setRequiredCalculators() {
        requiredCalculators.add(IndexChangePct.INDEX_NAME);
    }

    @Override
    public void calculate() throws MidasException {
        calculateIndex();
        //addIndexData("acp", acp);
        addIndexData("isBad", isBad);
    }

    private void calculateIndex(){
        boolean isDanger = false;
        DescriptiveStatistics descriptiveStats = new DescriptiveStatistics();
        descriptiveStats.setWindowSize(INTERVAL);
        for( int i = Math.max(0, len - INTERVAL); i < len; i++) {
            descriptiveStats.addValue(changePct[i]);
            acp[i] = descriptiveStats.getMean();
        }
        for( int i = INTERVAL; i < len; i++) {
            if(changePct[i] < BAD_THRESHOLD){
                isDanger = true;
                descriptiveStats.clear();
            }
            descriptiveStats.addValue(changePct[i]);
            acp[i] = descriptiveStats.getMean();
            if(isDanger){
                if(acp[i] < RECOVER_THRESHOLD){
                    isBad[i] = 1;
                } else {
                    isDanger = false;
                }
            }
        }

    }

    @Override
    protected void initIndex() throws MidasException {
        upShadowPct = (double[])stock.queryCmpIndex("k_u");
        downShadowPct = (double[])stock.queryCmpIndex("k_d");
        middleShadowPct = (double[])stock.queryCmpIndex("k_m");
        pMaLong = (double[])stock.queryCmpIndex("pMaLong");

        acp = new double[len];
        isBad = new int[len];
    }
}
