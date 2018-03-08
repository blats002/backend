package com.divroll.bucket.resource.jee;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.*;
import com.google.common.io.ByteStreams;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.divroll.bucket.Configuration;
import com.divroll.bucket.GoogleJsonKey;
import com.divroll.bucket.resource.WebsiteUploadResource;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;
import org.apache.tika.Tika;
import org.restlet.Request;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.servlet.ServletUtils;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Delete;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class WebsiteUploadServerResource extends BaseServerResource
        implements WebsiteUploadResource {

//    final static Logger LOG
//            = LoggerFactory.getLogger(WebsiteUploadServerResource.class);

    private static final java.util.logging.Logger LOG
            = java.util.logging.Logger.getLogger(WebsiteUploadServerResource.class.getName());
    private static final String DIVROLL_URL = "http://10.88.17.85";
//    private static final String DIVROLL_URL = "https://divroll.com";
    private String appObjectId = null;
    private Integer MAX_SIZE = 100000000; // 100MB

    @Delete
    public Representation delete(Representation entity) {
        if(!hasUserRole()) {
            setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
            return null;
        }
        if(subdomain == null || subdomain.isEmpty()) {
            setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            return null;
        }
        try {
            ////////////////////////////////////////////////////////////////////////////////////////////////////////////
            // Check if Subdomain exists
            // and owned by client
            JSONObject whereObject = new JSONObject();
            whereObject.put("appId", subdomain);
            HttpResponse<String> getRequest = Unirest.get(
                    Configuration.DIVROLL_PARSE_URL + "/classes/Application")
                    .header(X_PARSE_APPLICATION_ID, Configuration.DIVROLL_PARSE_APP_ID)
                    .header(X_MASTER_KEY, Configuration.DIVROLL_MASTER_KEY)
                    .queryString("where", whereObject.toJSONString())
                    .asString();
            String body = getRequest.getBody();
            if(getRequest.getStatus() == 200) {
                JSONObject results = JSON.parseObject(body);
                JSONArray resultsArray = results.getJSONArray("results");
                if(!resultsArray.isEmpty()){
                    for(int i=0;i<resultsArray.size();i++){
                        JSONObject jsonObject = resultsArray.getJSONObject(i);
                        LOG.info("jsonObject: " + jsonObject.toJSONString());
                        String appId = jsonObject.getString("objectId");
                        String appSubdomain = jsonObject.getString("appId");
                        JSONObject userPointer = jsonObject.getJSONObject("userId");
                        if(userPointer == null) {
                            setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
                            return null;
                        } else {
                            String id = userPointer.getString("objectId");
                            if(userId.equals(id) && subdomain.equals(appSubdomain)) {
                                appObjectId = appId;
                                break;
                            }
                        }
                    }
                }
            }
            LOG.info("Subdomain: " + subdomain);
            LOG.info("Application ID: " + appObjectId);
            if(appObjectId == null || appObjectId.isEmpty()) {
                setStatus(Status.CLIENT_ERROR_NOT_FOUND);
                return null;
            }
            ////////////////////////////////////////////////////////////////////////////////////////////////////////////
            boolean isOk = clean(sessionToken, appObjectId);
            if(isOk) {
                return success();
            } else {
                return internalError();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Representation post(Representation entity) {
        try {
            if(!hasUserRole()) {
                setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
                return null;
            }
            if(!isQuotaAndMeterExists()) {
                return createErrorResponse(Status.CLIENT_ERROR_NOT_FOUND);
            }
            if(isQuotaTraffic()) {
                return createErrorResponse(new Status(509, "Bandwidth Limit Exceeded"));
            }
            if(isQuotaStorage()) {
                return createErrorResponse(Status.SERVER_ERROR_INSUFFICIENT_STORAGE);
            }
            if(subdomain == null || subdomain.isEmpty()) {
                setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                return null;
            }
            // Check if Subdomain exists
            // and owned by client
            JSONObject whereObject = new JSONObject();
            whereObject.put("appId", subdomain);
            HttpResponse<String> getRequest = Unirest.get(
                    Configuration.DIVROLL_PARSE_URL + "/classes/Application")
                    .header(X_PARSE_APPLICATION_ID, Configuration.DIVROLL_PARSE_APP_ID)
                    .header(X_MASTER_KEY, Configuration.DIVROLL_MASTER_KEY)
                    .queryString("where", whereObject.toJSONString())
                    .asString();
            String body = getRequest.getBody();
            if(getRequest.getStatus() == 200) {
                JSONObject results = JSON.parseObject(body);
                JSONArray resultsArray = results.getJSONArray("results");
                if(!resultsArray.isEmpty()){
                    for(int i=0;i<resultsArray.size();i++){
                        JSONObject jsonObject = resultsArray.getJSONObject(i);
                        LOG.info("jsonObject: " + jsonObject.toJSONString());
                        String appId = jsonObject.getString("objectId");
                        String appSubdomain = jsonObject.getString("appId");
                        JSONObject userPointer = jsonObject.getJSONObject("userId");
                        if(userPointer == null) {
                            setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
                            return null;
                        } else {
                            String id = userPointer.getString("objectId");
                            if(userId.equals(id) && subdomain.equals(appSubdomain)) {
                                appObjectId = appId;
                                break;
                            }
                        }
                    }
                }
            }
            LOG.info("Subdomain: " + subdomain);
            LOG.info("Application ID: " + appObjectId);
            if(appObjectId == null || appObjectId.isEmpty()) {
                setStatus(Status.CLIENT_ERROR_NOT_FOUND);
                return null;
            }
            if (entity != null) {
                if (MediaType.MULTIPART_FORM_DATA.equals(entity.getMediaType(), true)) {
                    Request restletRequest = getRequest();
                    HttpServletRequest servletRequest = ServletUtils.getRequest(restletRequest);
                    ServletFileUpload upload = new ServletFileUpload();
                    FileItemIterator fileIterator = upload.getItemIterator(servletRequest);
//                    LOG.info("Content Type: " + servletRequest.getContentType());
//                    LOG.info("Content: " + new ServletRequestContext(servletRequest).getContentLength());
//                    LOG.info("Iterator: " + fileIterator.hasNext());
//                    DiskFileItemFactory factory = new DiskFileItemFactory();
//                    factory.setSizeThreshold(MAX_SIZE);
//                    RestletFileUpload upload = new RestletFileUpload(factory);
//                    FileItemIterator fileIterator = upload.getItemIterator(entity);


                    String qqPath = null;

                    while (fileIterator.hasNext()) {
                        FileItemStream item = fileIterator.next();
                        String fieldName = item.getFieldName();
                        String name = item.getName();
                        if(item.isFormField()) {
                            //LOG.info("Got a form field: " + item.getFieldName()  + " " + Streams.asString(item.openStream()));
                            if(item.getFieldName().equals("qqpath")) {
                                qqPath = Streams.asString(item.openStream());
                            }
                        } else {
                            //LOG.info("Got uploaded file");
                            byte[] byteContent  = ByteStreams.toByteArray(item.openStream());
                            // TODO byteContent is basically the file uploaded

                            LOG.info("Field Name: " + fieldName);
                            LOG.info("Content Name: " + name);
                            LOG.info("Content Type: " + item.getContentType());
                            LOG.info("Content Length: " + byteContent.length);

                            if(qqPath == null || qqPath.isEmpty()) {
                                qqPath = name;
                            } else {
                                qqPath = qqPath + name;
                            }

                            if(byteContent != null) {
                                //String result = processFile(sessionToken, byteContent, getQueryValue("upload_type"), appObjectId);
                                String filePath = qqPath;
                                boolean isSuccess = false;
                                JSONObject jsonObject = new JSONObject();
                                //int status = writeFileToCloud(sessionToken, byteContent, name, filePath, appObjectId, userId);
                                int status = writeFileToGoogleCloud(sessionToken, byteContent, name, filePath, subdomain, userId);
                                //LOG.info("Write Status: " + status);
                                final int bLength = byteContent.length;
                                if(status == 200) {
                                    Runnable runnable = new Runnable() {
                                        @Override
                                        public void run() {
                                            updateStorage(Double.valueOf(bLength));
                                            updateTraffic(Double.valueOf(bLength));
                                        }
                                    };
                                    Thread thread = new Thread(runnable);
                                    thread.start();
                                    jsonObject.put("success", true);
                                    jsonObject.put("status", status);
                                    Representation response = new StringRepresentation(jsonObject.toJSONString());
                                    response.setMediaType(MediaType.APPLICATION_JSON);
                                    return response;
                                } else {
                                    jsonObject.put("success", false);
                                    jsonObject.put("status", status);
                                    Representation response = new StringRepresentation(jsonObject.toJSONString());
                                    response.setMediaType(MediaType.APPLICATION_JSON);
                                    return response;
                                }

                            }
                        }




                    }
                } else {
                    return badRequest();
                }
            } else {
                return badRequest();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return internalError();
        }
        return null;
    }

    @Deprecated
    private String processFile(final String sessionToken, final byte[] bytes, final String uploadType, final String appId) throws UnirestException {
        final JSONObject result = new JSONObject();
        if("assets_zip".equals(uploadType)){
            final String deploymentId = createWebsiteStartDeployment();
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        String user = getUser(sessionToken);
                        JSONObject userObject = JSONObject.parseObject(user);
                        final String userId = userObject.getString("objectId");
                        if(userId == null || userId.isEmpty()) {
                            setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
//                            result.put("success", false);
//                            return result.toJSONString();
                        }
                        ///////////////////////////////////////////
                        // Clean assets first then deploy
                        ///////////////////////////////////////////
                        if(clean(sessionToken, subdomain)) {
                            ZipInputStream zipStream = new ZipInputStream(new ByteArrayInputStream(bytes));
                            InputStreamReader isr = new InputStreamReader(zipStream);
                            ZipEntry ze;
                            Double unzippedSize = 0.00;

                            ExecutorService es = Executors.newFixedThreadPool(16);
                            Collection<Callable<Integer>> runnableList = new LinkedList<Callable<Integer>>();
                            final int[] count = {0};
                            while ((ze = zipStream.getNextEntry()) != null) {
                                final String filePath = ze.getName();
                                final long fileSize = ze.getCompressedSize();
                                //LOG.info("fileName: " + filePath);
                                //LOG.info("fileSize: " + fileSize);
                                ByteArrayOutputStream streamBuilder = new ByteArrayOutputStream();
                                int bytesRead;
                                byte[] tempBuffer = new byte[8192*2];
                                while ( (bytesRead = zipStream.read(tempBuffer)) != -1 ){
                                    streamBuilder.write(tempBuffer, 0, bytesRead);
                                }
                                //LOG.info("File path: " + filePath);
                                final String filename = filePath.replaceFirst("(^.*[/\\\\])?([^/\\\\]*)$","$2");
                                //LOG.info("File name: "+ filename);
                                final byte[] byteArray = streamBuilder.toByteArray();
                                final boolean isDirectory = ze.isDirectory();
                                if(userId != null) {
                                    Callable<Integer> callable = new Callable<Integer>() {
                                        @Override
                                        public Integer call() throws Exception {
                                            try {
//                                                int status =  writeFileToCloud(sessionToken, byteArray, filename, filePath, appId, userId);
                                                int status =  writeFileToGoogleCloud(sessionToken, byteArray, filename, filePath, subdomain, userId);
                                                LOG.info("File Name: " + filename);
                                                LOG.info("Application ID: " + appId);
                                                LOG.info("Status: " + status);
                                                if(!isDirectory) {
                                                    count[0] = count[0] + 1;
                                                }
                                                return status;
                                            } catch (Exception e) {
                                                LOG.info(e.getMessage());
                                                e.printStackTrace();
                                            }
                                            return null;
                                        }
                                    };
                                    unzippedSize = unzippedSize + byteArray.length;
                                    runnableList.add(callable);
                                    //es.execute(runnableWrite);
                                }
                            }
                            Date start = new Date();
                            LOG.info("START: " + start.getTime());
                            es.invokeAll(runnableList);
                            Date end = new Date();
                            LOG.info("END: " + end.getTime());
                            LOG.info("TOTAL: " + (end.getTime() - start.getTime()));
                            LOG.info("TOTAL COUNT: " + count[0]);

                            updateStorage(Double.valueOf(unzippedSize));
                            updateTraffic(Double.valueOf(bytes.length));

                            es.shutdown();
                            try {
                                es.awaitTermination(5L, TimeUnit.MINUTES);
                                LOG.info("Website Deployment Finished: " + appObjectId);
                                updateWebsiteDeploymentDone(deploymentId, true);
                                zipStream.close();
//                                result.put("success", true);
//                                result.put("deploymentId", deploymentId);
//                                return result.toJSONString();
                            } catch (InterruptedException e) {
                                LOG.info(e.getMessage());
                                LOG.info("Website Deployment Finished with Errors: " + appObjectId);
                                updateWebsiteDeploymentDone(deploymentId, false);
                                zipStream.close();
//                                result.put("success", false);
//                                result.put("deploymentId", deploymentId);
//                                return result.toJSONString();
                            }
                        }
                    } catch (Exception e){
                        LOG.info("Error: " + e.getMessage());
                        e.printStackTrace();
                        updateWebsiteDeploymentDone(deploymentId, false);
//                        result.put("success", false);
//                        result.put("deploymentId", deploymentId);
//                        return result.toJSONString();
                    }
                }
            };
            Thread thread = new Thread(runnable);
            thread.start();
            setStatus(Status.SUCCESS_OK);
            result.put("deploymentId", deploymentId);
            return result.toJSONString();
        } else {
            setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            result.put("success", false);
        }
        return result.toJSONString();
    }

    private String createWebsiteStartDeployment() {
        try {
            //////////////////////////////////////////////////////////
            // Save WebsiteDeployment to Parse:
            JSONObject deployment = new JSONObject();
            deployment.put("app", createPointer("Application", appObjectId));
            deployment.put("user", createPointer("_User", userId));
            deployment.put("subdomain", subdomain);
            deployment.put("isDone", false);
            JSONObject acl = new JSONObject();
            JSONObject asterisk = new JSONObject();
            asterisk.put("read", true);
            asterisk.put("write", false);
            acl.put("*", asterisk);
            deployment.put("ACL", acl);
            HttpResponse<String> post = Unirest.post(Configuration.DIVROLL_PARSE_URL + "/classes/WebsiteDeployment")
                    .header(X_PARSE_APPLICATION_ID, Configuration.DIVROLL_PARSE_APP_ID)
                    .header(X_MASTER_KEY, Configuration.DIVROLL_MASTER_KEY)
                    .header("Content-Type", "application/json")
                    .body(deployment.toJSONString())
                    .asString();
            String websiteDeployRes = post.getBody();
            return JSONObject.parseObject(websiteDeployRes).getString("objectId");
            //////////////////////////////////////////////////////////
        } catch (Exception e) {
            LOG.info("Failed to start deploy " + appObjectId + " properly");
            LOG.info(e.getMessage());
        }
        return null;
    }

    private void updateWebsiteDeploymentDone(String deploymentId, boolean success) {
        try {
            //////////////////////////////////////////////////////////
            // Save WebsiteDeployment to Parse:
            JSONObject deployment = new JSONObject();
            deployment.put("app", createPointer("Application", appObjectId));
            deployment.put("user", createPointer("_User", userId));
            deployment.put("subdomain", subdomain);
            deployment.put("isDone", true);
            deployment.put("success", success);
            JSONObject acl = new JSONObject();
            JSONObject asterisk = new JSONObject();
            asterisk.put("read", true);
            asterisk.put("write", false);
            acl.put("*", asterisk);
            deployment.put("ACL", acl);
            deployment.put("createdAt", null);
            deployment.put("updatedAt", null);
            HttpResponse<String> post = Unirest.put(Configuration.DIVROLL_PARSE_URL + "/classes/WebsiteDeployment/" + deploymentId)
                    .header(X_PARSE_APPLICATION_ID, Configuration.DIVROLL_PARSE_APP_ID)
                    .header(X_MASTER_KEY, Configuration.DIVROLL_MASTER_KEY)
                    .header("Content-Type", "application/json")
                    .body(deployment.toJSONString())
                    .asString();
            String websiteDeployRes = post.getBody();
            //////////////////////////////////////////////////////////
        } catch (Exception e) {
            LOG.info("Failed to end deploy " + appObjectId + " properly");
            LOG.info(e.getMessage());
        }
    }

    /**
     * Writes file into Parse File without checking if file exists for faster round-trip.
     * The checking is removed since before calling this function is it required to call the 'clean' function
     * that removes all File objects with the given appId
     *
     * @param sessionToken
     * @param fileBytes
     * @param fileName
     * @param path
     * @param appId
     * @param userId
     * @throws UnirestException
     * @throws IOException
     */
    private static void writeFileToCloud2(String sessionToken, byte[] fileBytes, String fileName, String path, String appId, String userId)
            throws UnirestException, IOException {

        // Pre-process

        JSONObject appPointer = new JSONObject();
        appPointer.put("__type", "Pointer");
        appPointer.put("className", "Application");
        appPointer.put("objectId", appId);

        JSONObject file = new JSONObject();
        file.put("name", fileName);
        file.put("path", path);
        file.put("size", fileBytes.length);
        file.put("appId", appPointer);

        try {
            String mimeType = new Tika().detect(fileName);
            HttpResponse<String> response = Unirest.post(Configuration.DIVROLL_PARSE_URL +
                    "/files/" + fileName)
                    .header(X_PARSE_APPLICATION_ID, Configuration.DIVROLL_PARSE_APP_ID)
                    .header(X_MASTER_KEY, Configuration.DIVROLL_MASTER_KEY)
                    .header("Content-Type", mimeType)
                    .body(fileBytes)
                    .asString();
            if(response.getStatus() == 201){
                String location = response.getHeaders().get("Location").get(0);
                String filename = location.substring(location.lastIndexOf("/")+1, location.length());
                JSONObject filePointer = new JSONObject();
                filePointer.put("__type", "File");
                filePointer.put("name", filename);
                filePointer.put("url", location);
                file.put("filePointer", filePointer);

                JSONObject acl = new JSONObject();
                JSONObject asterisk = new JSONObject();
                asterisk.put("read", true);
                asterisk.put("write", false);

                JSONObject user = new JSONObject();
                user.put("read", true);
                user.put("write", true);

                acl.put("*", asterisk);
                acl.put(userId, user);
                file.put("ACL", acl);

                file.put("createdAt", null);
                file.put("updatedAt", null);

                //LOG.info(file.toJSONString());

                // Associate to Parse Object
                HttpResponse<String> res = Unirest.post(Configuration.DIVROLL_PARSE_URL + "/classes/File")
                        .header(X_PARSE_APPLICATION_ID, Configuration.DIVROLL_PARSE_APP_ID)
                        .header(X_MASTER_KEY, Configuration.DIVROLL_MASTER_KEY)
                        .header("Content-Type", "application/json")
                        .body(file.toJSONString())
                        .asString();
                String body = res.getBody();
                //LOG.info("Post Response:" + res.getBody());
                //LOG.info("Post Status:" + res.getStatus());
            }
        } catch (UnirestException e){
            LOG.info("Failed to upload: " + path);
        } catch (Exception e) {
            LOG.info("Failed to upload: " + e.getLocalizedMessage());
        }

    }

    private static void deleteCache(String subdomain, String path) {
        try {
            String url = DIVROLL_URL + "?cachekey=" + subdomain + "/" + path;
            LOG.info("Remove cache: " + url);
            String response = Unirest.delete(url).asString().getBody();
            LOG.info("Response: " + response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static int writeFileToGoogleCloud(String sessionToken, byte[] fileBytes, String fileName, String path, String subdomain, String userId) {
        int status = 500;
        try {
            InputStream stream = new ByteArrayInputStream(GoogleJsonKey.JSON_KEY.getBytes(StandardCharsets.UTF_8));
            StorageOptions options = StorageOptions.newBuilder()
                    .setProjectId(PROJECT_ID)
                    .setCredentials(GoogleCredentials.fromStream(stream)).build();
            Storage storage = options.getService();
            String bucketName = "appsbucket";

            List<Acl> acls = new ArrayList<>();
            acls.add(Acl.of(Acl.User.ofAllUsers(), Acl.Role.READER));

            BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, subdomain + "/" + path)
                    .setAcl(acls).build();

            Blob blob = storage.create(blobInfo, fileBytes);

            String md5 = blob.getMd5();
            String mediLink = blob.getMediaLink();

            System.out.println(md5);
            System.out.println(mediLink);

            BlobId id = blob.getBlobId();
            String blobBucket = id.getBucket();
            String blobName = id.getName();
            Long blobGeneration = id.getGeneration();

            System.out.println("Bucket: " + blobBucket);
            System.out.println("Name: " + blobName);
            System.out.println("Generation: " + blobGeneration);

            JSONObject file = new JSONObject();
            file.put("bucket", id.getBucket());
            file.put("name", id.getName());
            file.put("generation", id.getGeneration());
            file.put("md5", md5);
            file.put("size", fileBytes.length);

            HttpResponse<String> res = Unirest.post(Configuration.DIVROLL_PARSE_URL + "/classes/GoogleStorageFile")
                    .header(X_PARSE_APPLICATION_ID, Configuration.DIVROLL_PARSE_APP_ID)
                    .header(X_MASTER_KEY, Configuration.DIVROLL_MASTER_KEY)
                    .header("Content-Type", "application/json")
                    .body(file.toJSONString())
                    .asString();

            status = 200;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            deleteCache(subdomain, path);
        }
        return status;
    }

    private static int writeFileToCloud(String sessionToken, byte[] fileBytes, String fileName, String path, String appId, String userId)
            throws UnirestException, IOException {

        // Pre-process

        JSONObject appPointer = new JSONObject();
        appPointer.put("__type", "Pointer");
        appPointer.put("className", "Application");
        appPointer.put("objectId", appId);

        JSONObject file = new JSONObject();
        file.put("name", fileName);
        file.put("path", path);
        file.put("size", fileBytes.length);
        file.put("appId", appPointer);

        // Check if File exists
        JSONObject whereObject = new JSONObject();
        whereObject.put("name", fileName);
        whereObject.put("path", path);
        whereObject.put("appId", appPointer);

        HttpResponse<String> getRequest = Unirest.get(
                Configuration.DIVROLL_PARSE_URL + "/classes/File")
                .header(X_PARSE_APPLICATION_ID, Configuration.DIVROLL_PARSE_APP_ID)
                .header(X_MASTER_KEY, Configuration.DIVROLL_MASTER_KEY)
                .queryString("where", whereObject.toJSONString())
                .asString();

        boolean fileExist = false;

        if(getRequest.getStatus() == 200){
            JSONArray resultsArray = JSON.parseObject(getRequest.getBody())
                    .getJSONArray("results");
            if(!resultsArray.isEmpty()){
                fileExist = true;
                String existingObjectId = JSON.parseObject(getRequest.getBody())
                        .getJSONArray("results")
                        .getJSONObject(0)
                        .getString("objectId");
                //LOG.info("File exists, updating: " + path);
                // Upload new file
                String mimeType = new Tika().detect(fileName);
                HttpResponse<String> response = Unirest.post(Configuration.DIVROLL_PARSE_URL + "/files/" + fileName)
                        .header(X_PARSE_APPLICATION_ID, Configuration.DIVROLL_PARSE_APP_ID)
                        .header(X_MASTER_KEY, Configuration.DIVROLL_MASTER_KEY)
                        .header("Content-Type", mimeType)
                        .body(fileBytes)
                        .asString();
                if(response.getStatus() == 201){
                    // Update existing File
                    String location = response.getHeaders().get("Location").get(0);
                    String filename = location.substring(location.lastIndexOf("/")+1, location.length());

                    JSONObject filePointer = new JSONObject();
                    filePointer.put("__type", "File");
                    filePointer.put("name", filename);
                    filePointer.put("url", location);

                    JSONObject updateObject = new JSONObject();
                    updateObject.put("size", fileBytes.length);
                    updateObject.put("filePointer", filePointer);
                    updateObject.put("createdAt", null);
                    updateObject.put("updatedAt", null);

                    JSONObject acl = new JSONObject();
                    JSONObject asterisk = new JSONObject();
                    asterisk.put("read", true);
                    asterisk.put("write", false);

                    JSONObject user = new JSONObject();
                    user.put("read", true);
                    user.put("write", true);

                    acl.put("*", asterisk);
                    acl.put(userId, user);
                    updateObject.put("ACL", acl);

                    HttpResponse<String> updateResponse = Unirest.put(Configuration.DIVROLL_PARSE_URL +
                            "/classes/File/" + existingObjectId)
                            .header(X_PARSE_APPLICATION_ID, Configuration.DIVROLL_PARSE_APP_ID)
                            .header(X_MASTER_KEY, Configuration.DIVROLL_MASTER_KEY)
                            .header("Content-Type", "application/json")
                            .body(updateObject.toJSONString())
                            .asString();
                    //LOG.info("Put Response:" + updateResponse.getBody());
                    //LOG.info("Put Status:" + updateResponse.getStatus());
                    return updateResponse.getStatus();
                }
            }
        }

        if(!fileExist){
            // Upload file
            //LOG.info("Uploading file: " + path);
            try {
                String mimeType = new Tika().detect(fileName);
                HttpResponse<String> response = Unirest.post(Configuration.DIVROLL_PARSE_URL +
                        "/files/" + fileName)
                        .header(X_PARSE_APPLICATION_ID, Configuration.DIVROLL_PARSE_APP_ID)
                        .header(X_MASTER_KEY, Configuration.DIVROLL_MASTER_KEY)
                        .header("Content-Type", mimeType)
                        .body(fileBytes)
                        .asString();
                if(response.getStatus() == 201){
                    String location = response.getHeaders().get("Location").get(0);
                    String filename = location.substring(location.lastIndexOf("/")+1, location.length());
                    JSONObject filePointer = new JSONObject();
                    filePointer.put("__type", "File");
                    filePointer.put("name", filename);
                    filePointer.put("url", location);
                    file.put("filePointer", filePointer);

                    JSONObject acl = new JSONObject();
                    JSONObject asterisk = new JSONObject();
                    asterisk.put("read", true);
                    asterisk.put("write", false);

                    JSONObject user = new JSONObject();
                    user.put("read", true);
                    user.put("write", true);

                    acl.put("*", asterisk);
                    acl.put(userId, user);
                    file.put("ACL", acl);

                    file.put("createdAt", null);
                    file.put("updatedAt", null);

                    //LOG.info(file.toJSONString());

                    // Associate to Parse Object
                    HttpResponse<String> res = Unirest.post(Configuration.DIVROLL_PARSE_URL + "/classes/File")
                            .header(X_PARSE_APPLICATION_ID, Configuration.DIVROLL_PARSE_APP_ID)
                            .header(X_MASTER_KEY, Configuration.DIVROLL_MASTER_KEY)
//                            .header(X_PARSE_REST_API_KEY, Configuration.DIVROLL_PARSE_REST_API_KEY)
//                            .header(X_PARSE_SESSION_TOKEN, sessionToken)
                            .header("Content-Type", "application/json")
                            .body(file.toJSONString())
                            .asString();
                    //LOG.info("Post Response:" + res.getBody());
                    //LOG.info("Post Status:" + res.getStatus());
                    return res.getStatus();
                }
            } catch (UnirestException e){
                LOG.info("Failed to upload: " + path);
            } catch (Exception e) {
                LOG.info("Failed to upload: " + e.getLocalizedMessage());
            }
        }
        return -1;
    }

    protected boolean clean(String sessionToken, String appId) {
        LOG.info("Clean start");
        LOG.info("App ID: " + appId);
        boolean isSuccess = false;
        try {
            JSONObject appPointer = new JSONObject();
            appPointer.put("__type", "Pointer");
            appPointer.put("className", "Application");
            appPointer.put("objectId", appId);

            JSONObject whereObject = new JSONObject();
            whereObject.put("appId", appPointer);

            HttpResponse<String> getRequest = Unirest.get(
                    Configuration.DIVROLL_PARSE_URL + "/classes/File")
                    .header(X_PARSE_APPLICATION_ID, Configuration.DIVROLL_PARSE_APP_ID)
                    .header(X_MASTER_KEY, Configuration.DIVROLL_MASTER_KEY)
                    //.header(X_PARSE_SESSION_TOKEN, sessionToken)
                    .queryString("where", whereObject.toJSONString())
                    .asString();
            if(getRequest.getStatus() == 200){
                JSONArray resultsArray = JSON.parseObject(getRequest.getBody())
                        .getJSONArray("results");
                if(!resultsArray.isEmpty()){
                    for(int i=0;i<resultsArray.size();i++) {
                        JSONObject result = resultsArray.getJSONObject(i);
                        if(result != null) {
                            String objectId = result.getString("objectId");
                            JSONObject filePointer = result.getJSONObject("filePointer");
                            String url = filePointer.getString("url");
                            LOG.info("Removing " + objectId);
                            deleteFile(objectId);
                            deleteParseFile(url);
                        }
                    }
                    isSuccess = true;
                }
            } else {
                LOG.info("not found");
            }
        } catch (Exception e) {
            e.printStackTrace();
            isSuccess = false;
        }
        LOG.info("Clean end: " + isSuccess);
        return isSuccess;
    }

    private void deleteFile(String objectId) throws UnirestException {
        HttpResponse<String> deleteRequest = Unirest.delete(
                Configuration.DIVROLL_PARSE_URL + "/classes/File/" + objectId)
                .header(X_PARSE_APPLICATION_ID, Configuration.DIVROLL_PARSE_APP_ID)
                .header(X_MASTER_KEY, Configuration.DIVROLL_MASTER_KEY)
                .asString();
        int status = deleteRequest.getStatus();
        String body = deleteRequest.getBody();
        LOG.info("Delete Status: " + status);
        LOG.info("Delete Body: " + body);
    }

    private void deleteParseFile(String url) throws UnirestException {
        HttpResponse<String> deleteRequest = Unirest.delete(url)
                .header(X_PARSE_APPLICATION_ID, Configuration.DIVROLL_PARSE_APP_ID)
                .header(X_MASTER_KEY, Configuration.DIVROLL_MASTER_KEY)
                .asString();
        int status = deleteRequest.getStatus();
        String body = deleteRequest.getBody();
        LOG.info("Delete Status: " + status);
        LOG.info("Delete Body: " + body);
    }


    public void updateStorage(Double value) {
        try {
            Double byteSize = Double.valueOf(value);
            JSONObject whereObject = new JSONObject();
            whereObject.put("user", createPointer("_User", userId));
            HttpResponse<String> getRequest = Unirest.get(Configuration.DIVROLL_PARSE_URL +
                    "/classes/Meter")
                    .header(X_PARSE_APPLICATION_ID, Configuration.DIVROLL_PARSE_APP_ID)
                    .header(X_MASTER_KEY, Configuration.DIVROLL_MASTER_KEY)
//                    .header(X_PARSE_REST_API_KEY, Configuration.DIVROLL_PARSE_REST_API_KEY)
//                    .header(X_PARSE_SESSION_TOKEN, sessionToken)
                    .queryString("where", whereObject.toJSONString())
                    .asString();
            String body = getRequest.getBody();
            JSONArray jsonArray = JSONObject.parseObject(body).getJSONArray("results");
            if(!jsonArray.isEmpty()) {
                JSONObject jsonObject = jsonArray.getJSONObject(0);
                String objectId = jsonObject.getString("objectId");
                Double storage = jsonObject.getDouble("storage");
                LOG.info("Old storage value=" + storage);
                storage = storage + byteSize;
                LOG.info("New storage value=" + storage);
                jsonObject.put("storage", storage);
                jsonObject.put("createdAt", null);
                jsonObject.put("updatedAt", null);
                HttpResponse<String> putRequest = Unirest.put(Configuration.DIVROLL_PARSE_URL +
                        "/classes/Meter/" + objectId)
                        .header(X_PARSE_APPLICATION_ID, Configuration.DIVROLL_PARSE_APP_ID)
                        .header(X_PARSE_REST_API_KEY, Configuration.DIVROLL_PARSE_REST_API_KEY)
                        .header(X_MASTER_KEY, Configuration.DIVROLL_MASTER_KEY)
                        .body(jsonObject.toJSONString())
                        .asString();
                String responseBody = putRequest.getBody();
                LOG.info("Update response: " + responseBody);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateTraffic(Double b) {
        try {
            Double byteSize = Double.valueOf(b);
            JSONObject whereObject = new JSONObject();
            whereObject.put("user", createPointer("_User", userId));
            HttpResponse<String> getRequest = Unirest.get(Configuration.DIVROLL_PARSE_URL +
                    "/classes/Meter")
                    .header(X_PARSE_APPLICATION_ID, Configuration.DIVROLL_PARSE_APP_ID)
                    .header(X_MASTER_KEY, Configuration.DIVROLL_MASTER_KEY)
//                    .header(X_PARSE_REST_API_KEY, Configuration.DIVROLL_PARSE_REST_API_KEY)
//                    .header(X_PARSE_SESSION_TOKEN, sessionToken)
                    .queryString("where", whereObject.toJSONString())
                    .asString();
            String body = getRequest.getBody();
            JSONArray jsonArray = JSONObject.parseObject(body).getJSONArray("results");
            if(!jsonArray.isEmpty()) {
                JSONObject jsonObject = jsonArray.getJSONObject(0);
                String objectId = jsonObject.getString("objectId");
                Double traffic = jsonObject.getDouble("traffic");
                traffic = traffic + byteSize;
                jsonObject.put("traffic", traffic);
                jsonObject.put("createdAt", null);
                jsonObject.put("updatedAt", null);
                HttpResponse<String> putRequest = Unirest.put(Configuration.DIVROLL_PARSE_URL +
                        "/classes/Meter/" + objectId)
                        .header(X_PARSE_APPLICATION_ID, Configuration.DIVROLL_PARSE_APP_ID)
                        .header(X_PARSE_REST_API_KEY, Configuration.DIVROLL_PARSE_REST_API_KEY)
                        .header(X_MASTER_KEY, Configuration.DIVROLL_MASTER_KEY)
                        .body(jsonObject.toJSONString())
                        .asString();
                String responseBody = putRequest.getBody();
                LOG.info("Update response: " + responseBody);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
/*
Parse.Cloud.useMasterKey();
    var appId = req.params.appId;
    var appQuery = new Parse.Query("Application");
    appQuery.equalTo("objectId", appId);
    appQuery.find({
        success: function (result) {
            //res.success(result);
            console.log(result);
            //res.success(result[0]);
            //var _objectId = result[0].id;
            var pointer = { __type:"Pointer", className:"Application", objectId:appId };

            var query = new Parse.Query("File");
            query.equalTo("appId", pointer);

            query.find().then(function (files) {
                Parse.Object.destroyAll(files).then(function() {
                    res.success("success");
                });
            }, function (error) {
                response.error(error);
            });
        },
        error: function () {
            res.error("unable to get Application object");
        }
    });
 */
    protected Double calculateAppUsedStorage() throws Exception {
        // Check if File exists
        Double totalSize = 0.00;
        JSONObject whereObject = new JSONObject();
        whereObject.put("appId", createPointer("Application", appObjectId));
        HttpResponse<String> getRequest = Unirest.get(Configuration.DIVROLL_PARSE_URL +
                "/classes/File")
                .header(X_PARSE_APPLICATION_ID, Configuration.DIVROLL_PARSE_APP_ID)
                .header(X_MASTER_KEY, Configuration.DIVROLL_MASTER_KEY)
//                .header(X_PARSE_REST_API_KEY, Configuration.DIVROLL_PARSE_REST_API_KEY)
//                .header(X_PARSE_SESSION_TOKEN, sessionToken)
                .queryString("where", whereObject.toJSONString())
                .asString();
        String body = getRequest.getBody();
        JSONArray jsonArray = JSONObject.parseObject(body).getJSONArray("results");
        if(!jsonArray.isEmpty()) {
            for(int i=0;i<jsonArray.size();i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                Double size = jsonObject.getDouble("size");
                if(size != null)
                    totalSize = totalSize + size;
            }
        }
        return totalSize;
    }

    public byte[] getFile(String fileName){
        // TODO: Get file and put bytes here
        return null;
    }

    private Representation createErrorResponse(Status status) {
        JSONObject result = new JSONObject();
        result.put("error", status.getCode());
        result.put("message", status.getReasonPhrase());
        StringRepresentation representation = new StringRepresentation(result.toJSONString());
        representation.setMediaType(MediaType.APPLICATION_JSON);
        setStatus(status);
        return representation;
    }

}
