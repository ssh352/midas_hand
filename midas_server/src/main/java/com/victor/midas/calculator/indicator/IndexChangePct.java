package com.victor.midas.calculator.indicator;

import com.victor.midas.calculator.common.IndexCalcBase;
import com.victor.midas.calculator.util.IndexFactory;
import com.victor.midas.calculator.util.MathStockUtil;
import com.victor.midas.model.vo.CalcParameter;
import com.victor.midas.util.MidasConstants;
import com.victor.midas.util.MidasException;
import com.victor.utilities.utils.ArrayHelper;

import java.util.HashMap;

/**
 * calculate change percentage per day
 */
public class IndexChangePct extends IndexCalcBase {

    public final static String INDEX_NAME = MidasConstants.INDEX_NAME_CHANGEPCT;

    private double[] changePct;

    private double[] end;

    private int len;

    public IndexChangePct(CalcParameter parameter) {
        super(parameter);
    }

    @Override
    public String getIndexName() {
        return INDEX_NAME;
    }

    @Override
    public void calculate() throws MidasException {
        for (int i = 1; i < len; i++) {
            changePct[i] = MathStockUtil.calculateChangePct(end[i - 1], end[i]);
        }
        addIndexData(INDEX_NAME, changePct);
    }

    @Override
    protected void initIndex() throws MidasException {
        end = (double[])stock.queryCmpIndex(MidasConstants.INDEX_NAME_END);
        len = end.length;
        changePct = new double[len];
        cmpIndexName2Index = new HashMap<>();
    }


}
