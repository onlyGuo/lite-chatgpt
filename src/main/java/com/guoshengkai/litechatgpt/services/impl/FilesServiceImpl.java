package com.guoshengkai.litechatgpt.services.impl;

import com.aliyun.oss.HttpMethod;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.internal.OSSHeaders;
import com.aliyun.oss.model.CannedAccessControlList;
import com.aliyun.oss.model.GeneratePresignedUrlRequest;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.ObjectMetadata;
import com.guoshengkai.litechatgpt.core.beans.Method;
import com.guoshengkai.litechatgpt.core.sql.where.C;
import com.guoshengkai.litechatgpt.core.util.DateUtil;
import com.guoshengkai.litechatgpt.core.util.ThreadUtil;
import com.guoshengkai.litechatgpt.dao.FilesDao;
import com.guoshengkai.litechatgpt.entity.Files;
import com.guoshengkai.litechatgpt.services.FilesService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.net.URL;
import java.util.*;

@Service
public class FilesServiceImpl implements FilesService {

    @Resource
    private FilesDao filesDao;

    private OSS client;

    @Value("${oss.endpoint}")
    private String endpoint;
    @Value("${oss.access-key-id}")
    private String accessKeyId;
    @Value("${oss.access-key-secret}")
    private String accessKeySecret;
    @Value("${oss.bucket}")
    private String bucket;

    @PostConstruct
    private void init(){
        client = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
    }

    @Override
    public String getSignUrl(String fileKey) {
        URL url = client.generatePresignedUrl(bucket, fileKey, new Date(new Date().getTime() + 3600 * 1000));
        return url.toString();
    }

    @Override
    public String getSignUrl(String fileKey, int size, int zip) {
        GeneratePresignedUrlRequest generatePresignedUrlRequest = new GeneratePresignedUrlRequest(bucket, fileKey, HttpMethod.GET);
        generatePresignedUrlRequest.setExpiration(new Date(new Date().getTime() + 3600 * 1000));
        generatePresignedUrlRequest.setProcess("image/resize,m_lfit,h_" + size + ",w_" + size + "/format,jpg");
        URL url = client.generatePresignedUrl(generatePresignedUrlRequest);
        return url.toString();
    }

    @Override
    public void use(String fileKey) {
        filesDao.execute("UPDATE " + filesDao.tableName() + " SET USE_COUNT = USE_COUNT + 1 WHERE FILE_KEY = ?",
                fileKey);
    }

    @Override
    public void unUse(String fileKey) {
        Files files = filesDao.get(Method.where(Files::getFileKey, C.EQ, fileKey));
        if (files == null){
            return;
        }
        files.setUseCount(files.getUseCount() - 1);
        if (files.getUseCount() < 1){
            delete(fileKey);
            return;
        }
        filesDao.update(files);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String getUploadUrl() {
        Date expiration = new Date(new Date().getTime() + 3600 * 1000);
        String objectName = ThreadUtil.getUserId() + "/upload/" + DateUtil.formatPramm("yyyy/MM/dd/")
                + UUID.randomUUID().toString().replace("-", "") + ".file";
        // 入库
        Files files = new Files();
        files.setFileKey(objectName);
        files.setCreateTime(new Date());
        files.setUserId(ThreadUtil.getUserId());
        filesDao.add(files);

        Map<String, String> headers = new HashMap<>();
        headers.put(OSSHeaders.CONTENT_TYPE, "application/octet-stream");

        // 生成签名URL。
        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucket, objectName, HttpMethod.PUT);
        // 设置过期时间。
        request.setExpiration(expiration);
        request.setHeaders(headers);
        return client.generatePresignedUrl(request).toString();
    }

    @Override
    public void delete(String fileKey) {
        client.deleteObject(bucket, fileKey);
        filesDao.del(Method.where(Files::getFileKey, C.EQ, fileKey));
    }

    @Override
    public List<Files> listUnUse(Date date) {
        return filesDao.list(Method.where(Files::getUseCount, C.XIAO, 1));
    }

    @Override
    public void download(String fileKey, HttpServletResponse response) {
        OSSObject object = null;
        try {
            object = client.getObject(bucket, fileKey);
        }catch (Exception ignored){}
        if (null == object){
            return;
        }
        response.setContentType(object.getObjectMetadata().getContentType());
        try (InputStream in = object.getObjectContent(); OutputStream out = response.getOutputStream()){
            byte[] buffer = new byte[1024];
            int len;
            while ((len = in.read(buffer)) != -1){
                out.write(buffer, 0, len);
            }
        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public Files uploadByUrl(String url) {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        String objectName = ThreadUtil.getUserId() + "/upload/" + DateUtil.formatPramm("yyyy/MM/dd/")
                + UUID.randomUUID().toString().replace("-", "") + ".file";
        try (CloseableHttpResponse execute = httpClient.execute(new HttpGet(url))){
            InputStream content = execute.getEntity().getContent();
            client.putObject(bucket,  objectName, content);
            content.close();

            // 入库
            Files files = new Files();
            files.setFileKey(objectName);
            files.setCreateTime(new Date());
            files.setUserId(ThreadUtil.getUserId());
            filesDao.add(files);

            return files;
        }catch (IOException e){
            throw new RuntimeException(e);
        }

    }

    @Override
    public Files upload(byte[] bytes) {
        String objectName = ThreadUtil.getUserId() + "/upload/" + DateUtil.formatPramm("yyyy/MM/dd/")
                + UUID.randomUUID().toString().replace("-", "") + ".file";
        client.putObject(bucket, objectName, new ByteArrayInputStream(bytes));
        // 入库
        Files files = new Files();
        files.setFileKey(objectName);
        files.setCreateTime(new Date());
        files.setUserId(ThreadUtil.getUserId());
        filesDao.add(files);
        return files;
    }

    @Override
    public String getTempBytesUrl(byte[] bytes) {
        return getTempBytesUrl(bytes, null);
    }

    @Override
    public String getTempBytesUrl(byte[] bytes, String contentType) {
        String objectName = ThreadUtil.getUserId() + "/upload/" + DateUtil.formatPramm("yyyy/MM/dd/")
                + UUID.randomUUID().toString().replace("-", "");
        if (null != contentType && contentType.contains("/")){
            objectName += "." + contentType.split("/")[1];
        }else{
            objectName += ".file";
        }
        // 入库
        Files files = new Files();
        files.setFileKey(objectName);
        files.setCreateTime(new Date());
        files.setUserId(ThreadUtil.getUserId());
        filesDao.add(files);

        // 生成签名URL。
        if(null == contentType){
            client.putObject(bucket, objectName, new ByteArrayInputStream(bytes));
        }else{
            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentType(contentType);
            client.putObject(bucket, objectName, new ByteArrayInputStream(bytes), objectMetadata);
            // 公共读
            client.setObjectAcl(bucket, objectName, CannedAccessControlList.PublicRead);
            return "http://oss.icoding.ink" + "/" + objectName;
        }

        return client.generatePresignedUrl(bucket, objectName, new Date(System.currentTimeMillis() + 60000 * 10)).toString()
                .replace("https://g-chat.oss-cn-beijing.aliyuncs.com", "http://oss.icoding.ink");
    }

    @Override
    public Object getSignUrl(String fileKey, String contentType) {
        GeneratePresignedUrlRequest generatePresignedUrlRequest = new GeneratePresignedUrlRequest(bucket, fileKey, HttpMethod.GET);
        generatePresignedUrlRequest.setExpiration(new Date(new Date().getTime() + 3600 * 1000));
        generatePresignedUrlRequest.setProcess("image/format,png");
        String string = client.generatePresignedUrl(generatePresignedUrlRequest).toString();
        return string.replace("https://g-chat.oss-cn-beijing.aliyuncs.com", "http://oss.icoding.ink");
    }


    @SneakyThrows
    public static void main(String[] args) {
        CloseableHttpClient httpClient = null;
        CloseableHttpResponse response = null;
        try {
            HttpPut put = new HttpPut("https://g-chat.oss-cn-beijing.aliyuncs.com/1/upload/2023/05/09/1699df763fbb4559b7876a07fd9266db.file?Expires=1683618392&OSSAccessKeyId=UvwcGpBtW7QArPxB&Signature=awNsC4t%2BWj%2FxO%2B%2FZab9ntkt5%2BDk%3D");
            HttpEntity entity = new FileEntity(new File("/Users/gsk/Pictures/未标题-1.png"));
            put.setEntity(entity);

            httpClient = HttpClients.createDefault();

            response = httpClient.execute(put);

            System.out.println("返回上传状态码："+response.getStatusLine().getStatusCode());
            if(response.getStatusLine().getStatusCode() == 200){
                System.out.println("使用网络库上传成功");
            }
            System.out.println(EntityUtils.toString(response.getEntity()));
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            response.close();
            httpClient.close();
        }
    }
}
