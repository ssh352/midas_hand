# -*- coding: utf-8 -*-
import urllib2
import json
import os.path
import pandas as pd
from pymongo import MongoClient


def json_object_to_convert(obj):
    if obj:
        return obj.encode("utf-8")
    else:
        return obj


def json_data_get(url):
    response = urllib2.urlopen(url)
    resp = response.read()
    return json.loads(resp)


def get_dataframe_from_file(file_path):
    if os.path.isfile(file_path):
        return pd.read_csv(file_path)
    else:
        return None


def get_all_stock_codes():
    client = MongoClient("mongodb://localhost:27017")
    db = client.prod
    names = db.StockMisc.find_one({"_id": "AllStockNames"})
    return names['stockNames']

if __name__ == '__main__':
    print get_all_stock_codes()
    print 'test finished'

