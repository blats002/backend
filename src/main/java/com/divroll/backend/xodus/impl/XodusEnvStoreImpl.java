/*
 * Divroll, Platform for Hosting Static Sites
 * Copyright (C) 2019  Kerby Martino
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Other licenses:
 * -----------------------------------------------------------------------------
 * Commercial licenses for this work are available. These replace the above
 * AGPL 3.0 and offer limited warranties, support, maintenance, and commercial
 * deployments.
 *
 * For more information, please email: support@divroll.com
 *
 */
package com.divroll.backend.xodus.impl;

import com.divroll.backend.model.ACL;
import com.divroll.backend.model.ByteValue;
import com.divroll.backend.model.ByteValueIterable;
import com.divroll.backend.xodus.XodusEnvStore;
import com.divroll.backend.xodus.XodusManager;
import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import jetbrains.exodus.ByteIterable;
import jetbrains.exodus.bindings.*;
import jetbrains.exodus.env.*;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
public class XodusEnvStoreImpl implements XodusEnvStore {

  private static final Logger LOG = LoggerFactory.getLogger(XodusEnvStoreImpl.class);

  @Inject
  @Named("xodusRoot")
  String xodusRoot;

  @Inject XodusManager manager;

  public static byte[] serialize(Object obj) throws IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ObjectOutputStream os = new ObjectOutputStream(out);
    os.writeObject(obj);
    return out.toByteArray();
  }

  public static <T> T deserialize(byte[] data, Class<T> clazz) {
    try {
      ByteArrayInputStream in = new ByteArrayInputStream(data);
      ObjectInputStream is = new ObjectInputStream(in);
      return (T) is.readObject();
    } catch (IOException e) {
      e.printStackTrace();
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
    return null;
  }

  @Override
  public void put(String instance, final String entityType, final String key, final String value) {
    final Environment env = manager.getEnvironment(xodusRoot, instance);
    try {
      env.executeInTransaction(
          new TransactionalExecutable() {
            @Override
            public void execute(@NotNull final Transaction txn) {
              final Store store = env.openStore(entityType, StoreConfig.WITHOUT_DUPLICATES, txn);
              store.put(txn, StringBinding.stringToEntry(key), StringBinding.stringToEntry(value));
            }
          });
    } finally {
      // env.close();
    }
  }

  @Override
  public void put(String instance, final String entityType, final String key, final Boolean value) {
    final Environment env = manager.getEnvironment(xodusRoot, instance);
    try {
      env.executeInTransaction(
          new TransactionalExecutable() {
            @Override
            public void execute(@NotNull final Transaction txn) {
              final Store store = env.openStore(entityType, StoreConfig.WITHOUT_DUPLICATES, txn);
              store.put(
                  txn, StringBinding.stringToEntry(key), BooleanBinding.booleanToEntry(value));
            }
          });
    } finally {
      // env.close();
    }
  }

  @Override
  public void put(String instance, final String entityType, final String key, final Double value) {
    final Environment env = manager.getEnvironment(xodusRoot, instance);
    try {
      env.executeInTransaction(
          new TransactionalExecutable() {
            @Override
            public void execute(@NotNull final Transaction txn) {
              final Store store = env.openStore(entityType, StoreConfig.WITHOUT_DUPLICATES, txn);
              store.put(txn, StringBinding.stringToEntry(key), DoubleBinding.doubleToEntry(value));
            }
          });
    } finally {
      // env.close();
    }
  }

  @Override
  public void put(String instance, final String entityType, final String key, final Float value) {
    final Environment env = manager.getEnvironment(xodusRoot, instance);
    try {
      env.executeInTransaction(
          new TransactionalExecutable() {
            @Override
            public void execute(@NotNull final Transaction txn) {
              final Store store = env.openStore(entityType, StoreConfig.WITHOUT_DUPLICATES, txn);
              store.put(txn, StringBinding.stringToEntry(key), FloatBinding.floatToEntry(value));
            }
          });
    } finally {
      // env.close();
    }
  }

  @Override
  public void put(String instance, final String entityType, final String key, final Integer value) {
    final Environment env = manager.getEnvironment(xodusRoot, instance);
    try {
      env.executeInTransaction(
          new TransactionalExecutable() {
            @Override
            public void execute(@NotNull final Transaction txn) {
              final Store store = env.openStore(entityType, StoreConfig.WITHOUT_DUPLICATES, txn);
              store.put(txn, StringBinding.stringToEntry(key), IntegerBinding.intToEntry(value));
            }
          });
    } finally {
      // env.close();
    }
  }

  @Override
  public void put(String instance, final String entityType, final String key, final Long value) {
    final Environment env = manager.getEnvironment(xodusRoot, instance);
    env.executeInTransaction(
        new TransactionalExecutable() {
          @Override
          public void execute(@NotNull final Transaction txn) {
            final Store store = env.openStore(entityType, StoreConfig.WITHOUT_DUPLICATES, txn);
            store.put(txn, StringBinding.stringToEntry(key), LongBinding.longToEntry(value));
          }
        });
    env.close();
  }

  @Override
  public void put(String instance, final String entityType, final String key, final Short value) {
    final Environment env = manager.getEnvironment(xodusRoot, instance);
    try {
      env.executeInTransaction(
          new TransactionalExecutable() {
            @Override
            public void execute(@NotNull final Transaction txn) {
              final Store store = env.openStore(entityType, StoreConfig.WITHOUT_DUPLICATES, txn);
              store.put(txn, StringBinding.stringToEntry(key), ShortBinding.shortToEntry(value));
            }
          });
    } finally {
      // env.close();
    }
  }

  @Override
  public boolean put(
      String instance, final String entityType, final String key, final ByteValue value) {
    final Boolean[] isSuccess = {false};
    final Environment env = manager.getEnvironment(xodusRoot, instance);
    try {
      env.executeInTransaction(
          new TransactionalExecutable() {
            @Override
            public void execute(@NotNull final Transaction txn) {
              final Store store = env.openStore(entityType, StoreConfig.WITHOUT_DUPLICATES, txn);
              isSuccess[0] =
                  store.put(txn, StringBinding.stringToEntry(key), new ByteValueIterable(value));
            }
          });
    } finally {
      // env.close();
    }

    return isSuccess[0];
  }

  @Override
  public boolean putIfNotExists(
      String instance, final String entityType, final String key, final ByteValue value) {
    final Boolean[] isSuccess = {false};
    final Environment env = manager.getEnvironment(xodusRoot, instance);
    try {
      env.executeInTransaction(
          new TransactionalExecutable() {
            @Override
            public void execute(@NotNull final Transaction txn) {
              final Store store = env.openStore(entityType, StoreConfig.WITHOUT_DUPLICATES, txn);
              isSuccess[0] =
                  store.add(txn, StringBinding.stringToEntry(key), new ByteValueIterable(value));
            }
          });
    } finally {
      env.close();
    }
    return isSuccess[0];
  }

  @Override
  public boolean batchPut(
      String instance, final String entityType, final Map<String, String> properties) {
    final Boolean[] isSuccess = {false};
    final Environment env = manager.getEnvironment(xodusRoot, instance);
    try {
      env.executeInTransaction(
          new TransactionalExecutable() {
            @Override
            public void execute(@NotNull final Transaction txn) {
              final Store store = env.openStore(entityType, StoreConfig.WITHOUT_DUPLICATES, txn);
              Iterator<String> it = properties.keySet().iterator();
              while (it.hasNext()) {
                String key = it.next();
                String string = properties.get(key);
                isSuccess[0] =
                    store.put(
                        txn, StringBinding.stringToEntry(key), StringBinding.stringToEntry(string));
              }
            }
          });
    } finally {
      // env.close();
    }

    return isSuccess[0];
  }

  @Override
  public <T> T get(
      String instance, final String entityType, final String key, final Class<T> clazz) {
    final Object[] result = new Object[] {null};
    final Environment env = manager.getEnvironment(xodusRoot, instance);
    try {
      env.executeInTransaction(
          new TransactionalExecutable() {
            @Override
            public void execute(@NotNull final Transaction txn) {
              final Store store = env.openStore(entityType, StoreConfig.WITHOUT_DUPLICATES, txn);
              final ByteIterable value = store.get(txn, StringBinding.stringToEntry(key));
              if (value != null) {
                if (ByteValue.class.equals(clazz)) {
                  ByteValue byteValue = deserialize(value.getBytesUnsafe(), ByteValue.class);
                  result[0] = byteValue;
                } else if (String.class.equals(clazz)) {
                  result[0] = StringBinding.entryToString(value);
                } else if (Boolean.class.equals(clazz)) {
                  result[0] = BooleanBinding.entryToBoolean(value);
                } else if (Double.class.equals(clazz)) {
                  result[0] = DoubleBinding.entryToDouble(value);
                } else if (Float.class.equals(clazz)) {
                  result[0] = FloatBinding.entryToFloat(value);
                } else if (Integer.class.equals(clazz)) {
                  result[0] = StringBinding.entryToString(value);
                } else if (Long.class.equals(clazz)) {
                  result[0] = LongBinding.entryToLong(value);
                } else if (Short.class.equals(clazz)) {
                  result[0] = ShortBinding.entryToShort(value);
                } else if (ACL.class.equals(clazz)) {
                  // for delete operation
                  ACL valueACL = null;
                  try {
                    ByteValue byteValue = deserialize(value.getBytesUnsafe(), ByteValue.class);
                    valueACL = byteValue;
                  } catch (ClassCastException e) {

                  } catch (Exception e) {

                  }
                  result[0] = valueACL;
                }
              }
            }
          });
    } finally {
      // env.close();
    }
    return (T) result[0];
  }

  @Override
  public boolean delete(String instance, final String entityType, final String key) {
    final Boolean[] isSuccess = {false};
    final Environment env = manager.getEnvironment(xodusRoot, instance);
    try {
      env.executeInTransaction(
          new TransactionalExecutable() {
            @Override
            public void execute(@NotNull final Transaction txn) {
              final Store store = env.openStore(entityType, StoreConfig.WITHOUT_DUPLICATES, txn);
              isSuccess[0] = store.delete(txn, StringBinding.stringToEntry(key));
            }
          });
    } finally {
      // env.close();
    }

    return isSuccess[0];
  }

  //    @Override
  //    public void putIfNotExists(String instance, final String entityType, final String key, final
  // byte[] value) {
  //        final Environment env = Environments.newInstance(xodusRoot + instance);
  //        env.executeInTransaction(new TransactionalExecutable() {
  //            @Override
  //            public void execute(@NotNull final Transaction txn) {
  //                final Store store = env.openStore(entityType, StoreConfig.WITHOUT_DUPLICATES,
  // txn);
  //                store.putIfNotExists(txn, StringBinding.stringValueToEntry(key),
  // ByteBinding.byteToEntry(value));
  //            }
  //        });
  //        env.close();
  //    }

  @Override
  public boolean delete(String instance, final String entityType, String... keys) {
    final Boolean[] isSuccess = {false};
    final List<String> keyList = Arrays.asList(keys);
    final Environment env = manager.getEnvironment(xodusRoot, instance);
    try {
      env.executeInTransaction(
          new TransactionalExecutable() {
            @Override
            public void execute(@NotNull final Transaction txn) {
              final Store store = env.openStore(entityType, StoreConfig.WITHOUT_DUPLICATES, txn);
              for (String key : keyList) {
                isSuccess[0] = store.delete(txn, StringBinding.stringToEntry(key));
              }
            }
          });
    } finally {
      // env.close();
    }

    return isSuccess[0];
  }

  /**
   * Batch Put properties and delete keys
   *
   * @param instance
   * @param entityType
   * @param properties
   * @param keys
   * @return
   */
  @Override
  public boolean batchPutDelete(
      String instance,
      final String entityType,
      final Map<String, String> properties,
      final String... keys) {
    final Boolean[] isSuccess = {false};
    final List<String> keyList = Arrays.asList(keys);
    final Environment env = manager.getEnvironment(xodusRoot, instance);
    try {
      env.executeInTransaction(
          new TransactionalExecutable() {
            @Override
            public void execute(@NotNull final Transaction txn) {
              final Store store = env.openStore(entityType, StoreConfig.WITHOUT_DUPLICATES, txn);
              for (String key : keyList) {
                isSuccess[0] = store.delete(txn, StringBinding.stringToEntry(key));
              }
              Iterator<String> it = properties.keySet().iterator();
              while (it.hasNext()) {
                String key = it.next();
                String string = properties.get(key);
                isSuccess[0] =
                    store.put(
                        txn, StringBinding.stringToEntry(key), StringBinding.stringToEntry(string));
              }
            }
          });
    } finally {
      // env.close();
    }
    return isSuccess[0];
  }
}
