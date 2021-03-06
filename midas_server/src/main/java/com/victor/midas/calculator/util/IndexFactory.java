package com.victor.midas.calculator.util;

import com.victor.midas.calculator.common.ICalculator;
import com.victor.midas.calculator.common.MarketIndexAggregationCalcBase;
import com.victor.midas.model.vo.CalcParameter;
import com.victor.midas.train.perf.PerfCollector;
import com.victor.midas.util.MidasException;
import com.victor.utilities.datastructures.graph.*;
import com.victor.utilities.visual.VisualAssist;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * Create index calculators
 */
public class IndexFactory {

    private static final HashMap<String, ICalculator> calcName2calculator = new HashMap<>();

    private static final HashSet<String> marketCalculators = new HashSet<>();

    public static CalcParameter parameter = new CalcParameter();

    /**
     * use dependency to get all calculators
     */
    public static List<ICalculator> getAllNeededCalculators(String targetCalculatorName) throws MidasException {
        Queue<String> toProcessCalcNames = new LinkedList<>();
        Set<String> visited = new HashSet<>();
        toProcessCalcNames.add(targetCalculatorName);

        Graph<String> dependency = new Graph<>(GraphType.DIRECTED);

        while(!toProcessCalcNames.isEmpty()){
            String calcName = toProcessCalcNames.remove();
            if(!visited.contains(calcName)){
                visited.add(calcName);
                ICalculator current = calcName2calculator.get(calcName);
                Set<String> needed = current.getRequiredCalculators();
                for(String need : needed){
                    dependency.addEdge(calcName, need);
                    if(!visited.contains(need)){
                        toProcessCalcNames.add(need);
                    }
                }

                // if targetCalculatorName is normal calculator, then add all MarketIndexAggregation calculators
                if(calcName.equals(targetCalculatorName) && !(current instanceof MarketIndexAggregationCalcBase)){
                    for(String need : marketCalculators){
                        dependency.addEdge(targetCalculatorName, need);
                        if(!visited.contains(need)){
                            toProcessCalcNames.add(need);
                        }
                    }
                }
            }
        }

        List<String> names = TopologicalSort.sortThenGetRawData(dependency);
        VisualAssist.print("all calculators needed: ", names);
        if(names.size() == 0) names.add(targetCalculatorName);
        return getCalculatorCopy(names);
    }

    /**
     * instead of use Factory calculator instances
     * this function will reflect the constructor to create a new version of calculators to avoid training parameter pollution
     * set one unique PerfCollector for all calculators
     */
    private static List<ICalculator> getCalculatorCopy(List<String> names) throws MidasException {
        List<ICalculator> calculators = new ArrayList<>();
        CalcParameter parameterCopy = new CalcParameter();
        PerfCollector perfCollector = new PerfCollector();
        for(String name : names){
            try {
                Constructor constructor = calcName2calculator.get(name).getClass().getConstructor(CalcParameter.class);
                ICalculator calculator = (ICalculator)constructor.newInstance(parameterCopy);
                calculator.setPerfCollector(perfCollector);
                calculators.add(calculator);
            } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
                throw new MidasException("can not init calculator " + name, e);
            }
        }
        return calculators;
    }

    public static void addCalculator(String name, ICalculator calculator){
        calcName2calculator.put(name, calculator);
    }

    public static void applyNewParameter(CalcParameter param, List<ICalculator> calcList){
        for(ICalculator calc : calcList){
            calc.applyParameter(param);
        }
    }

    public static CalcParameter getParameter() {
        return parameter;
    }

    public static void setParameter(CalcParameter parameter) {
        IndexFactory.parameter = parameter;
    }

    public static HashMap<String, ICalculator> getCalcName2calculator() {
        return calcName2calculator;
    }

    public static void addMarketCalculator(String name){
        marketCalculators.add(name);
    }
}
