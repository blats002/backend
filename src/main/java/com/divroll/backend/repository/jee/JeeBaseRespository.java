package com.divroll.backend.repository.jee;

import com.divroll.backend.model.filter.TransactionFilter;
import jetbrains.exodus.entitystore.Entity;
import jetbrains.exodus.entitystore.EntityIterable;
import jetbrains.exodus.entitystore.StoreTransaction;

import java.util.List;

public abstract class JeeBaseRespository {

    protected abstract String getRoleStoreName();

    protected EntityIterable filter(final String entityType, final EntityIterable entityIterable, List<TransactionFilter> filters, final StoreTransaction txn) {
        final EntityIterable[] entityIterableResult = {entityIterable};
        // TODO: Validate filter
        filters.forEach(filter -> {
            if(filter.getOperator() == null) { // first filter in chair
                if(filter.getEqualityOp().equals(TransactionFilter.EQUALITY_OP.EQUAL)) {
                    if(filter.getPropertyValue() != null) { // equal
                        if(filter.getPropertyName().equals("aclRead")) {
                            Entity targetEntity = txn.getEntity(txn.toEntityId((String) filter.getPropertyValue()));
                            entityIterableResult[0] = txn.findLinks(entityType, targetEntity, "aclRead");
                        } else if(filter.getPropertyName().equals("aclWrite")) {
                            Entity targetEntity = txn.getEntity(txn.toEntityId((String) filter.getPropertyValue()));
                            entityIterableResult[0] = txn.findLinks(entityType, targetEntity, "aclWrite");
                        } else if(filter.getPropertyName().equals("roles")) {
                            Entity targetEntity = txn.getEntity(txn.toEntityId((String) filter.getPropertyValue()));
                            entityIterableResult[0] = txn.findLinks(entityType, targetEntity, "roles");
                        } else {
                            entityIterableResult[0] = txn.find(entityType, filter.getPropertyName(), filter.getPropertyValue());
                        }
                    } else { // min, max
                        entityIterableResult[0] = txn.find(entityType, filter.getPropertyName(), filter.getMinValue(), filter.getMaxValue());
                    }
                } else if(filter.getEqualityOp().equals(TransactionFilter.EQUALITY_OP.STARTS_WITH)) {
                    if(filter.getPropertyValue() != null) { // equal
                        if(filter.getPropertyValue() instanceof String) {
                            String compareValue = (String) filter.getPropertyValue();
                            entityIterableResult[0] = txn.findStartingWith(entityType, filter.getPropertyName(), compareValue);
                        } else {
                            throw new IllegalArgumentException("Invalid compare value " + filter.getPropertyValue());
                        }
                    } else {
                        throw new IllegalArgumentException("Value for property " + filter.getPropertyName() + " cannot be null for $findStartsWith query");
                    }
                }

            } else if(filter.getOperator().equals(TransactionFilter.BINARY_OP.INTERSECT)) {
                if(filter.getEqualityOp().equals(TransactionFilter.EQUALITY_OP.EQUAL)) {
                    if(filter.getPropertyValue() != null) {
                        if(filter.getPropertyName().equals("aclRead")) {
                            Entity targetEntity = txn.getEntity(txn.toEntityId((String) filter.getPropertyValue()));
                            entityIterableResult[0] = entityIterableResult[0].intersect(txn.findLinks(entityType, targetEntity, "aclRead"));
                        } else if(filter.getPropertyName().equals("aclWrite")) {
                            Entity targetEntity = txn.getEntity(txn.toEntityId((String) filter.getPropertyValue()));
                            entityIterableResult[0] = entityIterableResult[0].intersect(txn.findLinks(entityType, targetEntity, "aclWrite"));
                        } else if(filter.getPropertyName().equals("roles")) {
                            Entity targetEntity = txn.getEntity(txn.toEntityId((String) filter.getPropertyValue()));
                            entityIterableResult[0] = entityIterableResult[0].intersect(txn.findLinks(entityType, targetEntity, "roles"));
                        } else {
                            entityIterableResult[0] = entityIterableResult[0].intersect(txn.find(entityType, filter.getPropertyName(), filter.getPropertyValue()));
                        }
                    } else {
                        entityIterableResult[0] = entityIterableResult[0].intersect(txn.find(entityType, filter.getPropertyName(), filter.getMinValue(), filter.getMaxValue()));
                    }
                } else if(filter.getEqualityOp().equals(TransactionFilter.EQUALITY_OP.STARTS_WITH)) {
                    if(filter.getPropertyValue() != null) { // equal
                        if(filter.getPropertyValue() instanceof String) {
                            String compareValue = (String) filter.getPropertyValue();
                            entityIterableResult[0] = txn.findStartingWith(entityType, filter.getPropertyName(), compareValue);
                        } else {
                            throw new IllegalArgumentException("Invalid compare value " + filter.getPropertyValue());
                        }
                    } else {
                        throw new IllegalArgumentException("Value for property " + filter.getPropertyName() + " cannot be null for $findStartsWith query");
                    }
                }

            } else if(filter.getOperator().equals(TransactionFilter.BINARY_OP.UNION)) {
                if(filter.getEqualityOp().equals(TransactionFilter.EQUALITY_OP.EQUAL)) {
                    if(filter.getPropertyValue() != null) {
                        if(filter.getPropertyName().equals("aclRead")) {
                            Entity targetEntity = txn.getEntity(txn.toEntityId((String) filter.getPropertyValue()));
                            entityIterableResult[0] = entityIterableResult[0].union(txn.findLinks(entityType, targetEntity, "aclRead"));
                        } else if(filter.getPropertyName().equals("aclWrite")) {
                            Entity targetEntity = txn.getEntity(txn.toEntityId((String) filter.getPropertyValue()));
                            entityIterableResult[0] = entityIterableResult[0].union(txn.findLinks(entityType, targetEntity, "aclWrite"));
                        } else if(filter.getPropertyName().equals("roles")) {
                            Entity targetEntity = txn.getEntity(txn.toEntityId((String) filter.getPropertyValue()));
                            entityIterableResult[0] = entityIterableResult[0].union(txn.findLinks(entityType, targetEntity, "roles"));
                        } else {
                            entityIterableResult[0] = entityIterableResult[0].union(txn.find(entityType, filter.getPropertyName(), filter.getPropertyValue()));
                        }
//                        entityIterableResult[0].forEach(entity -> {
//                            System.out.println("entityId->" + entity.getId().toString());
//                        });
                    } else {
                        entityIterableResult[0] = entityIterableResult[0].union(txn.find(entityType, filter.getPropertyName(), filter.getMinValue(), filter.getMaxValue()));
                    }
                } else if(filter.getEqualityOp().equals(TransactionFilter.EQUALITY_OP.STARTS_WITH)) {
                    if(filter.getPropertyValue() != null) { // equal
                        if(filter.getPropertyValue() instanceof String) {
                            String compareValue = (String) filter.getPropertyValue();
                            entityIterableResult[0] = txn.findStartingWith(entityType, filter.getPropertyName(), compareValue);
                        } else {
                            throw new IllegalArgumentException("Invalid compare value " + filter.getPropertyValue());
                        }
                    } else {
                        throw new IllegalArgumentException("Value for property " + filter.getPropertyName() + " cannot be null for $findStartsWith query");
                    }
                }

            } else if(filter.getOperator().equals(TransactionFilter.BINARY_OP.MINUS)) {
                if(filter.getEqualityOp().equals(TransactionFilter.EQUALITY_OP.EQUAL)) {
                    if(filter.getPropertyValue() != null) {
                        if(filter.getPropertyName().equals("aclRead")) {
                            Entity targetEntity = txn.getEntity(txn.toEntityId((String) filter.getPropertyValue()));
                            entityIterableResult[0] = entityIterableResult[0].minus(txn.findLinks(entityType, targetEntity, "aclRead"));
                        } else if(filter.getPropertyName().equals("aclWrite")) {
                            Entity targetEntity = txn.getEntity(txn.toEntityId((String) filter.getPropertyValue()));
                            entityIterableResult[0] = entityIterableResult[0].minus(txn.findLinks(entityType, targetEntity, "aclWrite"));
                        } else if(filter.getPropertyName().equals("roles")) {
                            Entity targetEntity = txn.getEntity(txn.toEntityId((String) filter.getPropertyValue()));
                            entityIterableResult[0] = entityIterableResult[0].minus(txn.findLinks(entityType, targetEntity, "roles"));
                        } else {
                            entityIterableResult[0] = entityIterableResult[0].minus(txn.find(entityType, filter.getPropertyName(), filter.getPropertyValue()));
                        }
                    } else {
                        entityIterableResult[0] = entityIterableResult[0].minus(txn.find(entityType, filter.getPropertyName(), filter.getMinValue(), filter.getMaxValue()));
                    }
                } else if(filter.getEqualityOp().equals(TransactionFilter.EQUALITY_OP.STARTS_WITH)) {
                    if(filter.getPropertyValue() != null) { // equal
                        if(filter.getPropertyValue() instanceof String) {
                            String compareValue = (String) filter.getPropertyValue();
                            entityIterableResult[0] = txn.findStartingWith(entityType, filter.getPropertyName(), compareValue);
                        } else {
                            throw new IllegalArgumentException("Invalid compare value " + filter.getPropertyValue());
                        }
                    } else {
                        throw new IllegalArgumentException("Value for property " + filter.getPropertyName() + " cannot be null for $findStartsWith query");
                    }
                }

            } else if(filter.getOperator().equals(TransactionFilter.BINARY_OP.CONCAT)) {
                if(filter.getEqualityOp().equals(TransactionFilter.EQUALITY_OP.EQUAL)) {
                    if(filter.getPropertyValue() != null) {
                        if(filter.getPropertyName().equals("aclRead")) {
                            Entity targetEntity = txn.getEntity(txn.toEntityId((String) filter.getPropertyValue()));
                            entityIterableResult[0] = entityIterableResult[0].concat(txn.findLinks(entityType, targetEntity, "aclRead"));
                        } else if(filter.getPropertyName().equals("aclWrite")) {
                            Entity targetEntity = txn.getEntity(txn.toEntityId((String) filter.getPropertyValue()));
                            entityIterableResult[0] = entityIterableResult[0].concat(txn.findLinks(entityType, targetEntity, "aclWrite"));
                        } else if(filter.getPropertyName().equals("roles")) {
                            Entity targetEntity = txn.getEntity(txn.toEntityId((String) filter.getPropertyValue()));
                            entityIterableResult[0] = entityIterableResult[0].concat(txn.findLinks(entityType, targetEntity, "roles"));
                        } else {
                            entityIterableResult[0] = entityIterableResult[0].concat(txn.find(entityType, filter.getPropertyName(), filter.getPropertyValue()));
                        }
                    } else {
                        entityIterableResult[0] = entityIterableResult[0].concat(txn.find(entityType, filter.getPropertyName(), filter.getMinValue(), filter.getMaxValue()));
                    }
                } else if(filter.getEqualityOp().equals(TransactionFilter.EQUALITY_OP.STARTS_WITH)) {
                    if(filter.getPropertyValue() != null) { // equal
                        if(filter.getPropertyValue() instanceof String) {
                            String compareValue = (String) filter.getPropertyValue();
                            entityIterableResult[0] = txn.findStartingWith(entityType, filter.getPropertyName(), compareValue);
                        } else {
                            throw new IllegalArgumentException("Invalid compare value " + filter.getPropertyValue());
                        }
                    } else {
                        throw new IllegalArgumentException("Value for property " + filter.getPropertyName() + " cannot be null for $findStartsWith query");
                    }
                }

            }
        });
        return entityIterableResult[0];
    }
}
