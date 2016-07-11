package com.victor.midas.services;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Set;

import com.victor.midas.calculator.common.AggregationCalcBase;
import com.victor.midas.calculator.common.ICalculator;
import com.victor.midas.calculator.common.IndexCalcBase;
import com.victor.midas.calculator.util.IndexFactory;
import com.victor.midas.dao.*;
import com.victor.midas.model.db.DayFocusDb;
import com.victor.midas.model.db.StockInfoDb;
import com.victor.midas.model.db.misc.StockNamesDb;
import com.victor.midas.model.train.SingleParameterTrainResults;
import com.victor.midas.model.vo.CalcParameter;
import com.victor.midas.model.vo.MidasBond;
import com.victor.midas.model.vo.StockVo;

import com.victor.midas.model.vo.TrainResult;
import com.victor.midas.model.vo.concept.StockCrawlData;
import com.victor.midas.util.MidasException;
import com.victor.midas.util.ModelConvertor;
import org.apache.log4j.Logger;

import org.reflections.Reflections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StocksService {

	private static final Logger logger = Logger.getLogger(StocksService.class);

    @Autowired
    private StockInfoDao stockInfoDao;
    @Autowired
    private TaskDao taskDao;
    @Autowired
    private StockDao stockDao;
    @Autowired
    private MiscDao miscDao;
    @Autowired
    private TrainDao trainDao;
    @Autowired
    private DayFocusDao dayFocusDao;
    @Autowired
    private ScoreDao scoreDao;
    @Autowired
    private ConceptScoreDao conceptScoreDao;
    @Autowired
    private StockCrawlDataDao stockCrawlDataDao;
	@Autowired
	private TypeAhead typeAhead;

    // register all calculator to factory
    static {
        Reflections reflections = new Reflections("com.victor.midas.calculator");
        Set<Class<? extends IndexCalcBase>> subTypes = reflections.getSubTypesOf(IndexCalcBase.class);
        Set<Class<? extends AggregationCalcBase>> aggregationTypes = reflections.getSubTypesOf(AggregationCalcBase.class);
        CalcParameter parameter = IndexFactory.getParameter();
        try {
            for(Class aggregation : aggregationTypes){
                Constructor constructor = aggregation.getConstructor(new Class[]{CalcParameter.class});
                ICalculator calculator = (ICalculator)constructor.newInstance(parameter);
                IndexFactory.addCalculator(calculator.getIndexName(), calculator);
            }
            for(Class indexCalcBase : subTypes){
                Constructor constructor = indexCalcBase.getConstructor(new Class[]{CalcParameter.class});
                ICalculator calculator = (ICalculator)constructor.newInstance(parameter);
                IndexFactory.addCalculator(calculator.getIndexName(), calculator);
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        logger.info("finish registering all calculators.");
        TypeAhead.init(IndexFactory.getCalcName2calculator().keySet());
    }


    public void saveStocks(List<StockVo> stocks) throws MidasException {
        StockNamesDb stockNames = ModelConvertor.convert2StockNames(stocks);
        miscDao.saveMisc(stockNames);

        List<StockInfoDb> stockInfoDbs = ModelConvertor.convert2StockInfo(stocks);
        stockInfoDao.saveStockInfo(stockInfoDbs);
        stockDao.saveStocks(stocks);
    }

    public TrainResult queryTrainResult(Long trainId){
        return trainDao.queryTrainResult(trainId);
    }

    public TrainResult queryLastTrainResult() throws MidasException {
        return trainDao.queryLastTrainResult();
    }

    public StockVo queryStock(String stockName){
        return stockDao.queryStock(stockName);
    }

    public List<StockVo> queryAllStock(){
        return stockDao.queryAllStock();
    }

    public List<String> queryAllStockNames(){
        return miscDao.queryStockNames().getStockNames();
    }

    public SingleParameterTrainResults querySingleParameterTrainResults(){
        return miscDao.querySingleParameterTrainResults();
    }

    public void saveSingleParameterTrainResults(SingleParameterTrainResults results){
        miscDao.saveMisc(results);
    }

    public List<MidasBond> queryNationalDebt(){
        return miscDao.queryNationalDebt();
    }

    public void saveNationalDebt(List<MidasBond> bonds){
        miscDao.saveMisc(bonds);
    }

    public List<StockCrawlData> queryAllStockCrawlData(){
        return stockCrawlDataDao.queryAllCrawlData();
    }

    public void saveAllStockCrawlConceptData(List<StockCrawlData> crawlDatas){
        stockCrawlDataDao.saveCrawlData(crawlDatas);
    }

    public List<StockInfoDb> getStockBasicInfo() {
        return stockInfoDao.queryAllBasicInfo();
    }

    public List<DayFocusDb> getStockDayFocus() {
        return dayFocusDao.queryAllDayFocus();
    }

    public List<DayFocusDb> getStockDayFocus(int n) {
        return dayFocusDao.queryLastDayFocus(n);
    }


    public StockInfoDao getStockInfoDao() {
        return stockInfoDao;
    }

    public TaskDao getTaskDao() {
        return taskDao;
    }

    public StockDao getStockDao() {
        return stockDao;
    }

    public MiscDao getMiscDao() {
        return miscDao;
    }

    public TypeAhead getTypeAhead() {
        return typeAhead;
    }

    public TrainDao getTrainDao() {
        return trainDao;
    }

    public void setTrainDao(TrainDao trainDao) {
        this.trainDao = trainDao;
    }

    public DayFocusDao getDayFocusDao() {
        return dayFocusDao;
    }

    public ScoreDao getScoreDao() {
        return scoreDao;
    }

    public void setScoreDao(ScoreDao scoreDao) {
        this.scoreDao = scoreDao;
    }

    public ConceptScoreDao getConceptScoreDao() {
        return conceptScoreDao;
    }

    public void setConceptScoreDao(ConceptScoreDao conceptScoreDao) {
        this.conceptScoreDao = conceptScoreDao;
    }
}
