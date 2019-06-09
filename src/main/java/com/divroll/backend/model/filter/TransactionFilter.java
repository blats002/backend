/*
 * Divroll, Platform for Hosting Static Sites
 * Copyright 2019, Divroll, and individual contributors
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
package com.divroll.backend.model.filter;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
public class TransactionFilter {

  private BINARY_OP operator;
  private EQUALITY_OP equalityOp;
  private String propertyName;
  private Comparable propertyValue;
  private Comparable minValue;
  private Comparable maxValue;

  private TransactionFilter() {}

  public TransactionFilter(
      BINARY_OP operator, String propertyName, Comparable propertyValue, EQUALITY_OP equalityOp) {
    setOperator(operator);
    setPropertyName(propertyName);
    setPropertyValue(propertyValue);
    setEqualityOp(equalityOp);
  }

  public TransactionFilter(
      BINARY_OP operator,
      String propertyName,
      Comparable minValue,
      Comparable maxValue,
      EQUALITY_OP equalityOp) {
    setOperator(operator);
    setPropertyName(propertyName);
    setMaxValue(maxValue);
    setMinValue(minValue);
    setEqualityOp(equalityOp);
  }

  public BINARY_OP getOperator() {
    return operator;
  }

  public void setOperator(BINARY_OP operator) {
    this.operator = operator;
  }

  public EQUALITY_OP getEqualityOp() {
    return equalityOp;
  }

  public void setEqualityOp(EQUALITY_OP equalityOp) {
    this.equalityOp = equalityOp;
  }

  public String getPropertyName() {
    return propertyName;
  }

  public void setPropertyName(String propertyName) {
    this.propertyName = propertyName;
  }

  public Comparable getPropertyValue() {
    return propertyValue;
  }

  public void setPropertyValue(Comparable propertyValue) {
    this.propertyValue = propertyValue;
  }

  public Comparable getMinValue() {
    return minValue;
  }

  public void setMinValue(Comparable minValue) {
    this.minValue = minValue;
  }

  public Comparable getMaxValue() {
    return maxValue;
  }

  public void setMaxValue(Comparable maxValue) {
    this.maxValue = maxValue;
  }

  @Override
  public String toString() {
    String s = "[";
    s = s + "operator=" + getOperator() + "\n";
    s = s + "equalityOp=" + getEqualityOp() + "\n";
    s = s + "propertyName=" + getPropertyName() + "\n";
    s = s + "propertyValue=" + getPropertyValue() + "\n";
    s = s + "maxValue=" + getMaxValue() + "\n";
    s = s + "minValue=" + getMinValue() + "\n";
    s = s + "]";
    return s;
  }

  public enum BINARY_OP {
    INTERSECT,
    UNION,
    MINUS,
    CONCAT
  }

  public enum EQUALITY_OP {
    EQUAL,
    STARTS_WITH
  }
}
