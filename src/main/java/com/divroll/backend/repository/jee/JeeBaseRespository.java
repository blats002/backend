/*
 * Divroll, Platform for Hosting Static Sites
 * Copyright 2018, Divroll, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation; either version 3.0 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.divroll.backend.repository.jee;

import com.divroll.backend.model.filter.TransactionFilter;
import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import jetbrains.exodus.entitystore.Entity;
import jetbrains.exodus.entitystore.EntityIterable;
import jetbrains.exodus.entitystore.StoreTransaction;
import util.ComparableLinkedList;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
public abstract class JeeBaseRespository {

  private static final Logger LOG = LoggerFactory.getLogger(JeeBaseRespository.class);

  public static <T> void removeDuplicates(ComparableLinkedList<T> list) {
    int size = list.size();
    int out = 0;
    {
      final Set<T> encountered = new HashSet<T>();
      for (int in = 0; in < size; in++) {
        final T t = list.get(in);
        final boolean first = encountered.add(t);
        if (first) {
          list.set(out++, t);
        }
      }
    }
    while (out < size) {
      list.remove(--size);
    }
  }

  public static <T> void removeDuplicates(ArrayList<T> list) {
    int size = list.size();
    int out = 0;
    {
      final Set<T> encountered = new HashSet<T>();
      for (int in = 0; in < size; in++) {
        final T t = list.get(in);
        final boolean first = encountered.add(t);
        if (first) {
          list.set(out++, t);
        }
      }
    }
    while (out < size) {
      list.remove(--size);
    }
  }

  protected abstract String getDefaultRoleStore();

  protected EntityIterable filter(
      final String entityType,
      final EntityIterable entityIterable,
      List<TransactionFilter> filters,
      final StoreTransaction txn) {
    final EntityIterable[] entityIterableResult = {entityIterable};
    // TODO: Validate filter
    filters.forEach(
        filter -> {
          if (filter.getOperator() == null) { // first filter in chair
            if (filter.getEqualityOp().equals(TransactionFilter.EQUALITY_OP.EQUAL)) {
              if (filter.getPropertyValue() != null) { // equal
                if (filter.getPropertyName().equals("aclRead")) {
                  Entity targetEntity =
                      txn.getEntity(txn.toEntityId((String) filter.getPropertyValue()));
                  entityIterableResult[0] = txn.findLinks(entityType, targetEntity, "aclRead");
                } else if (filter.getPropertyName().equals("aclWrite")) {
                  Entity targetEntity =
                      txn.getEntity(txn.toEntityId((String) filter.getPropertyValue()));
                  entityIterableResult[0] = txn.findLinks(entityType, targetEntity, "aclWrite");
                } else if (filter.getPropertyName().equals("roles")) {
                  Entity targetEntity =
                      txn.getEntity(txn.toEntityId((String) filter.getPropertyValue()));
                  entityIterableResult[0] = txn.findLinks(entityType, targetEntity, "roles");
                } else {
                  entityIterableResult[0] =
                      txn.find(entityType, filter.getPropertyName(), filter.getPropertyValue());
                }
              } else { // min, max
                entityIterableResult[0] =
                    txn.find(
                        entityType,
                        filter.getPropertyName(),
                        filter.getMinValue(),
                        filter.getMaxValue());
              }
            } else if (filter.getEqualityOp().equals(TransactionFilter.EQUALITY_OP.STARTS_WITH)) {
              if (filter.getPropertyValue() != null) { // equal
                if (filter.getPropertyValue() instanceof String) {
                  String compareValue = (String) filter.getPropertyValue();
                  entityIterableResult[0] =
                      txn.findStartingWith(entityType, filter.getPropertyName(), compareValue);
                } else {
                  throw new IllegalArgumentException(
                      "Invalid compare value " + filter.getPropertyValue());
                }
              } else {
                throw new IllegalArgumentException(
                    "Value for property "
                        + filter.getPropertyName()
                        + " cannot be null for $findStartsWith query");
              }
            }

          } else if (filter.getOperator().equals(TransactionFilter.BINARY_OP.INTERSECT)) {
            if (filter.getEqualityOp().equals(TransactionFilter.EQUALITY_OP.EQUAL)) {
              if (filter.getPropertyValue() != null) {
                if (filter.getPropertyName().equals("aclRead")) {
                  Entity targetEntity =
                      txn.getEntity(txn.toEntityId((String) filter.getPropertyValue()));
                  entityIterableResult[0] =
                      entityIterableResult[0].intersect(
                          txn.findLinks(entityType, targetEntity, "aclRead"));
                } else if (filter.getPropertyName().equals("aclWrite")) {
                  Entity targetEntity =
                      txn.getEntity(txn.toEntityId((String) filter.getPropertyValue()));
                  entityIterableResult[0] =
                      entityIterableResult[0].intersect(
                          txn.findLinks(entityType, targetEntity, "aclWrite"));
                } else if (filter.getPropertyName().equals("roles")) {
                  Entity targetEntity =
                      txn.getEntity(txn.toEntityId((String) filter.getPropertyValue()));
                  entityIterableResult[0] =
                      entityIterableResult[0].intersect(
                          txn.findLinks(entityType, targetEntity, "roles"));
                } else {
                  entityIterableResult[0] =
                      entityIterableResult[0].intersect(
                          txn.find(
                              entityType, filter.getPropertyName(), filter.getPropertyValue()));
                }
              } else {
                entityIterableResult[0] =
                    entityIterableResult[0].intersect(
                        txn.find(
                            entityType,
                            filter.getPropertyName(),
                            filter.getMinValue(),
                            filter.getMaxValue()));
              }
            } else if (filter.getEqualityOp().equals(TransactionFilter.EQUALITY_OP.STARTS_WITH)) {
              if (filter.getPropertyValue() != null) { // equal
                if (filter.getPropertyValue() instanceof String) {
                  String compareValue = (String) filter.getPropertyValue();
                  entityIterableResult[0] =
                      txn.findStartingWith(entityType, filter.getPropertyName(), compareValue);
                } else {
                  throw new IllegalArgumentException(
                      "Invalid compare value " + filter.getPropertyValue());
                }
              } else {
                throw new IllegalArgumentException(
                    "Value for property "
                        + filter.getPropertyName()
                        + " cannot be null for $findStartsWith query");
              }
            }

          } else if (filter.getOperator().equals(TransactionFilter.BINARY_OP.UNION)) {
            if (filter.getEqualityOp().equals(TransactionFilter.EQUALITY_OP.EQUAL)) {
              if (filter.getPropertyValue() != null) {
                if (filter.getPropertyName().equals("aclRead")) {
                  Entity targetEntity =
                      txn.getEntity(txn.toEntityId((String) filter.getPropertyValue()));
                  entityIterableResult[0] =
                      entityIterableResult[0].union(
                          txn.findLinks(entityType, targetEntity, "aclRead"));
                } else if (filter.getPropertyName().equals("aclWrite")) {
                  Entity targetEntity =
                      txn.getEntity(txn.toEntityId((String) filter.getPropertyValue()));
                  entityIterableResult[0] =
                      entityIterableResult[0].union(
                          txn.findLinks(entityType, targetEntity, "aclWrite"));
                } else if (filter.getPropertyName().equals("roles")) {
                  Entity targetEntity =
                      txn.getEntity(txn.toEntityId((String) filter.getPropertyValue()));
                  entityIterableResult[0] =
                      entityIterableResult[0].union(
                          txn.findLinks(entityType, targetEntity, "roles"));
                } else {
                  entityIterableResult[0] =
                      entityIterableResult[0].union(
                          txn.find(
                              entityType, filter.getPropertyName(), filter.getPropertyValue()));
                }
                //                        entityIterableResult[0].forEach(entity -> {
                //                            System.out.println("entityId->" +
                // entity.getId().toString());
                //                        });
              } else {
                entityIterableResult[0] =
                    entityIterableResult[0].union(
                        txn.find(
                            entityType,
                            filter.getPropertyName(),
                            filter.getMinValue(),
                            filter.getMaxValue()));
              }
            } else if (filter.getEqualityOp().equals(TransactionFilter.EQUALITY_OP.STARTS_WITH)) {
              if (filter.getPropertyValue() != null) { // equal
                if (filter.getPropertyValue() instanceof String) {
                  String compareValue = (String) filter.getPropertyValue();
                  entityIterableResult[0] =
                      txn.findStartingWith(entityType, filter.getPropertyName(), compareValue);
                } else {
                  throw new IllegalArgumentException(
                      "Invalid compare value " + filter.getPropertyValue());
                }
              } else {
                throw new IllegalArgumentException(
                    "Value for property "
                        + filter.getPropertyName()
                        + " cannot be null for $findStartsWith query");
              }
            }

          } else if (filter.getOperator().equals(TransactionFilter.BINARY_OP.MINUS)) {
            if (filter.getEqualityOp().equals(TransactionFilter.EQUALITY_OP.EQUAL)) {
              if (filter.getPropertyValue() != null) {
                if (filter.getPropertyName().equals("aclRead")) {
                  Entity targetEntity =
                      txn.getEntity(txn.toEntityId((String) filter.getPropertyValue()));
                  entityIterableResult[0] =
                      entityIterableResult[0].minus(
                          txn.findLinks(entityType, targetEntity, "aclRead"));
                } else if (filter.getPropertyName().equals("aclWrite")) {
                  Entity targetEntity =
                      txn.getEntity(txn.toEntityId((String) filter.getPropertyValue()));
                  entityIterableResult[0] =
                      entityIterableResult[0].minus(
                          txn.findLinks(entityType, targetEntity, "aclWrite"));
                } else if (filter.getPropertyName().equals("roles")) {
                  Entity targetEntity =
                      txn.getEntity(txn.toEntityId((String) filter.getPropertyValue()));
                  entityIterableResult[0] =
                      entityIterableResult[0].minus(
                          txn.findLinks(entityType, targetEntity, "roles"));
                } else {
                  entityIterableResult[0] =
                      entityIterableResult[0].minus(
                          txn.find(
                              entityType, filter.getPropertyName(), filter.getPropertyValue()));
                }
              } else {
                entityIterableResult[0] =
                    entityIterableResult[0].minus(
                        txn.find(
                            entityType,
                            filter.getPropertyName(),
                            filter.getMinValue(),
                            filter.getMaxValue()));
              }
            } else if (filter.getEqualityOp().equals(TransactionFilter.EQUALITY_OP.STARTS_WITH)) {
              if (filter.getPropertyValue() != null) { // equal
                if (filter.getPropertyValue() instanceof String) {
                  String compareValue = (String) filter.getPropertyValue();
                  entityIterableResult[0] =
                      txn.findStartingWith(entityType, filter.getPropertyName(), compareValue);
                } else {
                  throw new IllegalArgumentException(
                      "Invalid compare value " + filter.getPropertyValue());
                }
              } else {
                throw new IllegalArgumentException(
                    "Value for property "
                        + filter.getPropertyName()
                        + " cannot be null for $findStartsWith query");
              }
            }

          } else if (filter.getOperator().equals(TransactionFilter.BINARY_OP.CONCAT)) {
            if (filter.getEqualityOp().equals(TransactionFilter.EQUALITY_OP.EQUAL)) {
              if (filter.getPropertyValue() != null) {
                if (filter.getPropertyName().equals("aclRead")) {
                  Entity targetEntity =
                      txn.getEntity(txn.toEntityId((String) filter.getPropertyValue()));
                  entityIterableResult[0] =
                      entityIterableResult[0].concat(
                          txn.findLinks(entityType, targetEntity, "aclRead"));
                } else if (filter.getPropertyName().equals("aclWrite")) {
                  Entity targetEntity =
                      txn.getEntity(txn.toEntityId((String) filter.getPropertyValue()));
                  entityIterableResult[0] =
                      entityIterableResult[0].concat(
                          txn.findLinks(entityType, targetEntity, "aclWrite"));
                } else if (filter.getPropertyName().equals("roles")) {
                  Entity targetEntity =
                      txn.getEntity(txn.toEntityId((String) filter.getPropertyValue()));
                  entityIterableResult[0] =
                      entityIterableResult[0].concat(
                          txn.findLinks(entityType, targetEntity, "roles"));
                } else {
                  entityIterableResult[0] =
                      entityIterableResult[0].concat(
                          txn.find(
                              entityType, filter.getPropertyName(), filter.getPropertyValue()));
                }
              } else {
                entityIterableResult[0] =
                    entityIterableResult[0].concat(
                        txn.find(
                            entityType,
                            filter.getPropertyName(),
                            filter.getMinValue(),
                            filter.getMaxValue()));
              }
            } else if (filter.getEqualityOp().equals(TransactionFilter.EQUALITY_OP.STARTS_WITH)) {
              if (filter.getPropertyValue() != null) { // equal
                if (filter.getPropertyValue() instanceof String) {
                  String compareValue = (String) filter.getPropertyValue();
                  entityIterableResult[0] =
                      txn.findStartingWith(entityType, filter.getPropertyName(), compareValue);
                } else {
                  throw new IllegalArgumentException(
                      "Invalid compare value " + filter.getPropertyValue());
                }
              } else {
                throw new IllegalArgumentException(
                    "Value for property "
                        + filter.getPropertyName()
                        + " cannot be null for $findStartsWith query");
              }
            }
          }
        });
    return entityIterableResult[0];
  }

  protected String getISODate() {
    TimeZone tz = TimeZone.getTimeZone("UTC");
    DateFormat df =
        new SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm'Z'"); // Quoted "Z" to indicate UTC, no timezone offset
    df.setTimeZone(tz);
    return df.format(new Date());
  }

  protected boolean isReservedProperty(String propertyName) {
    return false;
  }

}
