package com.yejy.springredis.log;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * @author yejunyang2012@163.com
 * @date 2021/9/21 18:51
 **/
@Slf4j
public class MyReaderHttpServletRequestWrapper extends HttpServletRequestWrapper {

    private final byte[] body;
    private HttpServletRequest request;
    private String bodyStr;


    public MyReaderHttpServletRequestWrapper(HttpServletRequest request) {
        super(request);
        this.bodyStr = getBodyParam(request);
        this.body = bodyStr.getBytes(StandardCharsets.UTF_8);
        this.request = request;
        printLog();
    }

    private void  printLog(){
        log.info("\n==========================================" +
                "\n path : [{}] " +
                "\n method : {}" +
                "\n origin : {}" +
                "\n queryParam : {}" +
                "\n requesBody : {}" +
                "\n===========================================",request.getServletPath(),request.getMethod(),
                request.getHeader("Origin"),getQueryParam(request),bodyStr);
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(getInputStream()));
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        final ByteArrayInputStream bais = new ByteArrayInputStream(body);
        return new ServletInputStream() {
            @Override
            public boolean isFinished() {
                return false;
            }

            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setReadListener(ReadListener readListener) {

            }

            @Override
            public int read() throws IOException {
                return bais.read();
            }
        };
    }

    private String getBodyParam(HttpServletRequest request){
        StringBuilder sb = new StringBuilder();
        InputStream inputStream = null;
        BufferedReader reader = null;
        try {
            inputStream = request.getInputStream();
            reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            String line = "";
            while ((line = reader.readLine()) != null){
                sb.append(line);
            }
        }catch (Exception e){
            log.error("log error :",e);
        }finally {
            if (reader != null){
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return sb.toString();
    }

    private String getQueryParam(HttpServletRequest httpServletRequest){
        Map<String,String> params = new HashMap<>();
        for (String key : httpServletRequest.getParameterMap().keySet()){
            params.put(key,httpServletRequest.getParameter(key));
        }
        if (CollectionUtils.isEmpty(params)) {
            return "";
        }
        return JSON.toJSONString(params);
    }

}
