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
package com.divroll.backend.service.jee;

import com.divroll.backend.Constants;
import com.divroll.backend.model.ACL;
import com.divroll.backend.model.ByteValue;
import com.divroll.backend.model.exception.ACLException;
import com.divroll.backend.service.KeyValueService;
import com.divroll.backend.xodus.XodusEnvStore;
import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import com.google.inject.Inject;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
public class JeeKeyValueService implements KeyValueService {

  private static final Logger LOG = LoggerFactory.getLogger(JeeKeyValueService.class);

  @Inject XodusEnvStore store;

  @Override
  public <T> boolean putIfNotExists(
      String instance,
      String namespace,
      String entityType,
      String key,
      Comparable value,
      String[] read,
      String[] write,
      Class<T> clazz) {
    if (value == null) {
      return false;
    }

    if (String.class.equals(clazz)) {
      ByteValue byteValue = new ByteValue();
      try {
        byteValue.setValue(((String) value).getBytes(Constants.DEFAULT_CHARSET));
        byteValue.setRead(read);
        byteValue.setWrite(write);
      } catch (Exception e) {
        e.printStackTrace();
      } finally {
        return store.putIfNotExists(instance, entityType, key, byteValue);
      }
    } else if (ByteBuffer.class.equals(clazz)) {
      ByteBuffer byteBuffer = ((ByteBuffer) value);
      byte[] arr = new byte[byteBuffer.remaining()];
      byteBuffer.get(arr);

      ByteValue byteValue = new ByteValue();
      byteValue.setValue(arr);
      byteValue.setRead(read);
      byteValue.setWrite(write);
      return store.putIfNotExists(instance, entityType, key, byteValue);
    }

    return false;
  }

  @Override
  public <T> T get(
      String instance, String namespace, String entityType, String key, String uuid, Class<T> clazz)
      throws ACLException {
    if (String.class.equals(clazz)) {
      ByteValue value = store.get(instance, entityType, key, ByteValue.class);
      if (value != null) {
        String[] read = value.getRead();
        Boolean publicRead =
            read != null ? Arrays.asList(read).contains(Constants.ACL_ASTERISK) : false;
        if (!publicRead) {
          if (Arrays.asList(read).contains(uuid)) {
            try {
              byte[] bytes = value.getValue();
              String s = new String(bytes, Constants.DEFAULT_CHARSET);
              return (T) s;
            } catch (UnsupportedEncodingException e) {
              // do nothing
            }
          } else {
            throw new ACLException();
          }
        } else {
          try {
            byte[] bytes = value.getValue();
            String s = new String(bytes, Constants.DEFAULT_CHARSET);
            return (T) s;
          } catch (UnsupportedEncodingException e) {
            // do nothing
          }
        }
      }
    } else if (ByteBuffer.class.equals(clazz)) {
      ByteValue value = store.get(instance, entityType, key, ByteValue.class);
      if (value != null) {
        String[] read = value.getRead();
        Boolean publicRead =
            read != null ? Arrays.asList(read).contains(Constants.ACL_ASTERISK) : false;
        if (!publicRead) {
          if (Arrays.asList(read).contains(uuid)) {
            return (T) ByteBuffer.wrap(value.getValue());
          } else {
            throw new ACLException();
          }
        } else {
          return (T) ByteBuffer.wrap(value.getValue());
        }
      }
    } else if (ACL.class.equals(clazz)) { // Helper case to delete a key
      ACL value = store.get(instance, entityType, key, ACL.class);
      if (value != null) {
        String[] read = value.getRead();
        Boolean publicRead =
            read != null ? Arrays.asList(read).contains(Constants.ACL_ASTERISK) : false;
        if (!publicRead) {
          if (Arrays.asList(read).contains(uuid)) {
            return (T) value;
          } else {
            throw new ACLException();
          }
        } else {
          return (T) value;
        }
      }
    }
    return null;
  }

  @Override
  public <T> boolean put(
      String instance,
      String namespace,
      String entityType,
      String key,
      Comparable value,
      String uuid,
      String[] read,
      String[] write,
      Class<T> clazz)
      throws ACLException {

    if (value == null) {
      return false;
    }

    // This will throw the ACLException
    ACL acl = get(instance, namespace, entityType, key, uuid, ACL.class);

    if (acl == null) {
      return false;
    }

    String[] aclRead = acl.getRead();
    String[] aclWrite = acl.getWrite();

    Boolean publicWrite = false;
    if (aclWrite != null) {
      publicWrite = Arrays.asList(aclWrite).contains(Constants.ACL_ASTERISK);
    }

    if (!publicWrite) {
      if (uuid == null) {
        throw new ACLException();
      } else if (aclWrite == null) {
        throw new ACLException();
      } else {
        if (!Arrays.asList(aclWrite).contains(uuid)) {
          throw new ACLException();
        }
      }
    }

    if (read != null && !Arrays.asList(read).isEmpty()) {
      if (aclRead == null) {
        aclRead = read;
      } else {
        List<String> aclReadList = Arrays.asList(aclRead);
        List<String> aclReadListAdd = Arrays.asList(read);
        for (String s : aclReadListAdd) {
          if (!aclReadList.contains(s)) {
            aclReadList.add(s);
          }
        }
        String[] newAclRead = aclReadList.toArray(new String[aclReadList.size()]);
        aclRead = newAclRead;
      }
    }

    if (write != null && !Arrays.asList(write).isEmpty()) {
      if (aclWrite == null) {
        aclWrite = write;
      } else {
        List<String> aclWriteList = Arrays.asList(aclWrite);
        List<String> aclWriteListAdd = Arrays.asList(write);
        for (String s : aclWriteListAdd) {
          if (!aclWriteList.contains(s)) {
            aclWriteList.add(s);
          }
        }
        String[] newAclWrite = aclWriteList.toArray(new String[aclWriteList.size()]);
        aclWrite = newAclWrite;
      }
    }

    if (String.class.equals(clazz)) {
      ByteValue byteValue = new ByteValue();
      try {
        byteValue.setValue(((String) value).getBytes(Constants.DEFAULT_CHARSET));
        byteValue.setRead(aclRead);
        byteValue.setWrite(aclWrite);
      } catch (Exception e) {
        e.printStackTrace();
      } finally {
        return store.put(instance, entityType, key, byteValue);
      }
    } else if (ByteBuffer.class.equals(clazz)) {
      ByteBuffer byteBuffer = ((ByteBuffer) value);
      byte[] arr = new byte[byteBuffer.remaining()];
      byteBuffer.get(arr);

      ByteValue byteValue = new ByteValue();
      byteValue.setValue(arr);
      byteValue.setRead(aclRead);
      byteValue.setWrite(aclWrite);
      return store.put(instance, entityType, key, byteValue);
    }

    return false;
  }

  @Override
  public boolean delete(
      String instance, String namespace, String entityType, String key, String uuid)
      throws ACLException {
    if (get(instance, namespace, entityType, key, uuid, ACL.class) != null) {
      return store.delete(instance, entityType, key);
    } else {
      throw new ACLException();
    }
  }
}
