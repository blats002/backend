package com.divroll.webdash.server.repository.gae;

import com.divroll.webdash.shared.File;
import com.divroll.webdash.shared.Files;
import com.divroll.webdash.server.BlobFile;
import com.divroll.webdash.server.repository.FileRepository;
import com.google.appengine.api.datastore.Key;
import com.hunchee.twist.types.ListResult;

import static com.hunchee.twist.ObjectStoreService.store;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by Kerby on 1/5/2016.
 */
public class GaeFileRepository implements FileRepository {

    private static final Logger LOG
            = Logger.getLogger(GaeFileRepository.class.getName());

    @Override
    public File save(File entity) {
        return null;
    }

    @Override
    public File findOne(Key primaryKey) {
        return null;
    }

    @Override
    public Iterable<File> findAll() {
        return store().find(File.class).asIterable();
    }

    @Override
    public Long count() {
        return null;
    }

    @Override
    public void delete(Key primaryKey) {

    }

    @Override
    public void delete(Iterable<? extends File> entities) {

    }

    @Override
    public void deleteAll() {

    }

    @Override
    public boolean exists(Key primaryKey) {
        return false;
    }

    @Override
    public Files list(String cursor) {
        ListResult<BlobFile> result;
        if(cursor != null && !cursor.isEmpty()){
            result = store().find(BlobFile.class).withCursor(cursor).asList();
        } else {
            result = store().find(BlobFile.class).asList();
        }
        Files files = new Files();
        List<File> list = new ArrayList<>();
        String webSafeString = "";
        if(result.getCursor() != null){
            webSafeString = result.getCursor().getWebSafeString();
        }
        files.setCursor(webSafeString);
        for(BlobFile file : result.getList()){
            String fileName = file.getFilename();
            File f = new File();
            f.setFileName(fileName);
            list.add(f);
        }
        files.setList(list);
        return files;
    }
}
