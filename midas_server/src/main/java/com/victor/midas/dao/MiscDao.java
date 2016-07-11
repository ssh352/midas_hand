package com.victor.midas.dao;

import com.victor.midas.model.db.misc.FocusedStockNamesDb;
import com.victor.midas.model.db.misc.NationalDebtDb;
import com.victor.midas.model.db.misc.StockNamesDb;
import com.victor.midas.model.train.SingleParameterTrainResults;
import com.victor.midas.model.vo.MidasBond;
import com.victor.midas.util.MidasConstants;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * DAO for misc collection
 */

@Component
public class MiscDao {
    private final String COLLECTION_NAME = MidasConstants.MISC_COLLECTION_NAME;

    private static final Logger logger = Logger.getLogger(StockDao.class);

    private static Query allStockNamesQuery, singleTrainResultQuery, nationalDebtQuery;

    static {
        allStockNamesQuery = new Query(Criteria.where("_id").is(MidasConstants.MISC_ALL_STOCK_NAMES));
        singleTrainResultQuery = new Query(Criteria.where("_id").is(MidasConstants.MISC_SINGLE_TRAIN_RESULT));
        nationalDebtQuery = new Query(Criteria.where("_id").is(MidasConstants.MISC_NATIONAL_DEBT));
    }

    @Autowired
    private MongoTemplate mongoTemplate;

    public StockNamesDb queryStockNames(){
        StockNamesDb stockNamesDb = mongoTemplate.findOne(allStockNamesQuery, StockNamesDb.class, COLLECTION_NAME);
        return stockNamesDb;
    }

    public FocusedStockNamesDb queryFocusedStockNamesDb(){
        return mongoTemplate.findOne(new Query(Criteria.where("_id").is(FocusedStockNamesDb.MISC_NAME)), FocusedStockNamesDb.class, COLLECTION_NAME);
    }

    public SingleParameterTrainResults querySingleParameterTrainResults(){
        SingleParameterTrainResults results = mongoTemplate.findOne(singleTrainResultQuery, SingleParameterTrainResults.class, COLLECTION_NAME);
        return results;
    }

    public List<MidasBond> queryNationalDebt(){
        NationalDebtDb nationalDebtDb = mongoTemplate.findOne(nationalDebtQuery, NationalDebtDb.class, COLLECTION_NAME);
        if(nationalDebtDb != null) return nationalDebtDb.getBonds();
        return null;
    }

    /**
     * save Misc to DB
     */
    public void saveMisc(StockNamesDb stockNamesDb){
        mongoTemplate.save(stockNamesDb, COLLECTION_NAME);
    }

    public void saveMisc(FocusedStockNamesDb focusedStockNamesDb){
        mongoTemplate.save(focusedStockNamesDb, COLLECTION_NAME);
    }

    public void saveMisc(SingleParameterTrainResults results){
        mongoTemplate.save(results, COLLECTION_NAME);
    }

    public void saveMisc(List<MidasBond> bonds){
        NationalDebtDb nationalDebtDb = new NationalDebtDb(MidasConstants.MISC_NATIONAL_DEBT, bonds);
        mongoTemplate.save(nationalDebtDb, COLLECTION_NAME);
    }


    /**
     * create collection
     */
    public void createCollection(){
        if (!mongoTemplate.collectionExists(COLLECTION_NAME)) {
            logger.info("mongoTemplate create collection");
            mongoTemplate.createCollection(COLLECTION_NAME);
        }
    }

    /**
     * delete collection, means that all documents will be deleted
     */
    public void deleteCollection(){
        if (mongoTemplate.collectionExists(COLLECTION_NAME)) {
            logger.info("drop task Collection");
            mongoTemplate.dropCollection(COLLECTION_NAME);
        }
    }
}
