package com.cogniance.simpledb.dao;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;

import com.cogniance.simpledb.model.SimpleDBEntity;
import com.cogniance.simpledb.util.SimpleDBObjectBuilder;
import com.xerox.amazonws.sdb.Domain;
import com.xerox.amazonws.sdb.Item;
import com.xerox.amazonws.sdb.ItemAttribute;
import com.xerox.amazonws.sdb.QueryWithAttributesResult;
import com.xerox.amazonws.sdb.SDBException;
import com.xerox.amazonws.sdb.SimpleDB;

/**
 * Extend this class to have basic support for SimpleDB DAO. Child classes should implement getters
 * for access and secret keys, and also for model class.
 * 
 * @author Andriy Gusyev
 */
public abstract class SimpleDBDAOSupport<T extends SimpleDBEntity<ID>, ID extends Serializable> {

    private static final String SELECT_COUNT_ALL = "select count(*) from %s";

    private static final String SELECT_COUNT_WHERE = "select count(*) from %s where %s";

    private static final String EMPTY_TOKEN = "";

    private static final Integer BATCH_SIZE = 250;

    private static SimpleDB sdb;

    protected abstract Class<T> getEntityClass();

    protected abstract String getAccessKey();

    protected abstract String getSecretKey();

    @SuppressWarnings("unchecked")
    protected Domain getDomain() throws SDBException {
        if (sdb == null) {
            sdb = new SimpleDB(getAccessKey(), getSecretKey());
        }
        Class clz = getEntityClass();
        String domainName;
        Entity annotation = (Entity) clz.getAnnotation(Entity.class);
        if (annotation != null) {
            domainName = annotation.name();
        } else {
            domainName = clz.getSimpleName();
        }
        return sdb.getDomain(domainName);
    }

    public List<T> getAll() {
        List<T> list = new ArrayList<T>();
        String token = EMPTY_TOKEN;
        if (token != null) {
            Result<T> result = getPortion(BATCH_SIZE, token.equals(EMPTY_TOKEN) ? null : token);
            token = result.getNextToken();
            list.addAll(result.getItems());
        }
        return list;
    }

    /**
     * Returns portion of entities from SimpleDB, if count isn't set, returns max 250 entities. If
     * count greater then 250, returns max 250 entitties.
     * 
     * @param count - sets how much entities should be returned as maximim
     * @param nextToken - token for retrieveing next portion, nextToken could be taken from
     *        {@link Result} of previous portion, should be null for first portion.
     * @return {@link Result} which contains list of items and token for next portion.
     */
    @SuppressWarnings("unchecked")
    public Result<T> getPortion(Integer count, String nextToken) {
        if (count == null || count.equals(0) || count > BATCH_SIZE) {
            count = BATCH_SIZE;
        }
        try {
            List<T> list = new ArrayList<T>();
            Domain domain = getDomain();
            QueryWithAttributesResult result = domain.listItemsWithAttributes("", null, nextToken, count);
            nextToken = result.getNextToken();
            for (List<ItemAttribute> attrs : result.getItems().values()) {
                T obj = (T) SimpleDBObjectBuilder.buildObject(getEntityClass(), attrs);
                list.add(obj);
            }
            return new Result<T>(list, nextToken);
        } catch (SDBException e) {
            throw new IllegalStateException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public T getById(ID id) {
        try {
            Domain domain = getDomain();
            Item item = domain.getItem(id.toString());
            T obj = (T) SimpleDBObjectBuilder.buildObject(getEntityClass(), item.getAttributes());
            return obj;
        } catch (SDBException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Returns number of items in domain
     * 
     * @return
     */
    public Integer countRows() {
        try {
            Domain domain = getDomain();
            String select = String.format(SELECT_COUNT_ALL, domain.getName());
            QueryWithAttributesResult result = domain.selectItems(select, null);
            for (List<ItemAttribute> list : result.getItems().values()) {
                if (list.size() > 0) {
                    ItemAttribute attr = list.get(0);
                    return Integer.parseInt(attr.getValue());
                }
            }
            return 0;
        } catch (SDBException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Returns number of items with condition
     * 
     * @param conditionQuery - part of the query after WHERE
     * @return
     */
    public Integer countRows(String conditionQuery) {
        try {
            Domain domain = getDomain();
            String select = String.format(SELECT_COUNT_WHERE, domain.getName(), conditionQuery);
            QueryWithAttributesResult result = domain.selectItems(select, null);
            for (List<ItemAttribute> list : result.getItems().values()) {
                if (list.size() > 0) {
                    ItemAttribute attr = list.get(0);
                    return Integer.parseInt(attr.getValue());
                }
            }
            return 0;
        } catch (SDBException e) {
            throw new IllegalStateException(e);
        }
    }

    public static class Result<T> implements Serializable {

        private List<T> items;

        private String nextToken;

        public Result() {
        }

        public Result(List<T> items, String nextToken) {
            this.items = items;
            this.nextToken = nextToken;
        }

        public List<T> getItems() {
            return items;
        }

        public void setItems(List<T> items) {
            this.items = items;
        }

        public String getNextToken() {
            return nextToken;
        }

        public void setNextToken(String nextToken) {
            this.nextToken = nextToken;
        }

    }
}