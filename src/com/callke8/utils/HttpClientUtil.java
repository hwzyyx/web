package com.callke8.utils;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

/**
 * 基于 httpclient 4.3.1版本的 http工具类 <br>
 * 类详细说明.
 * <p>
 * Copyright: Copyright (c) 2018-4-18 14:33:21
 * <p>
 * Company: 北京宽连十方数字技术有限公司
 * <p>
 * 
 * @author chenwwa@c-platform.com
 * @version 1.0.0
 */
public class HttpClientUtil
{

    private static final CloseableHttpClient httpClient;

    public static final String CHARSET = "UTF-8";

    static
    {
        RequestConfig config = RequestConfig.custom().setConnectTimeout(3000).setSocketTimeout(3000).build();
        httpClient = HttpClientBuilder.create().setDefaultRequestConfig(config).build();
    }

    public static String doGet(String url, Map<String, String> params)
    {
        return doGet(url, params, CHARSET);
    }

    public static String doPost(String url, Map<String, String> params)
    {
        return doPost(url, params, CHARSET);
    }

    public static String doPost(String url, String json)
    {
        return doPost(url, json, CHARSET);
    }

    /**
     * HTTP Get 获取内容
     * 
     * @param url 请求的url地址 ?之前的地址
     * @param params 请求的参数
     * @param charset 编码格式
     * @return 页面内容
     */
    public static String doGet(String url, Map<String, String> params, String charset)
    {
        if (StringUtils.isBlank(url))
        {
            return null;
        }
        try
        {
            if (params != null && !params.isEmpty())
            {
                List<NameValuePair> pairs = new ArrayList<NameValuePair>(params.size());
                for (Map.Entry<String, String> entry : params.entrySet())
                {
                    String value = entry.getValue();
                    if (value != null)
                    {
                        pairs.add(new BasicNameValuePair(entry.getKey(), value));
                    }
                }
                url += "?" + EntityUtils.toString(new UrlEncodedFormEntity(pairs, charset));
            }
            HttpGet httpGet = new HttpGet(url);
            CloseableHttpResponse response = httpClient.execute(httpGet);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 200)
            {
                httpGet.abort();
                throw new RuntimeException("HttpClient,error status code :" + statusCode);
            }
            HttpEntity entity = response.getEntity();
            String result = null;
            if (entity != null)
            {
                result = EntityUtils.toString(entity, "utf-8");
            }
            EntityUtils.consume(entity);
            response.close();
            return result;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * HTTP Post 获取内容
     * 
     * @param url 请求的url地址 ?之前的地址
     * @param params 请求的参数
     * @param charset 编码格式
     * @return 响应
     */
    public static String doPost(String url, Map<String, String> params, String charset)
    {
        if (StringUtils.isBlank(url))
        {
            return null;
        }
        try
        {
            List<NameValuePair> pairs = null;
            if (params != null && !params.isEmpty())
            {
                pairs = new ArrayList<NameValuePair>(params.size());
                for (Map.Entry<String, String> entry : params.entrySet())
                {
                    String value = entry.getValue();
                    if (value != null)
                    {
                        pairs.add(new BasicNameValuePair(entry.getKey(), value));
                    }
                }
            }
            HttpPost httpPost = new HttpPost(url);
            if (pairs != null && pairs.size() > 0)
            {
                httpPost.setEntity(new UrlEncodedFormEntity(pairs, CHARSET));
            }
            CloseableHttpResponse response = httpClient.execute(httpPost);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 200)
            {
                httpPost.abort();
                throw new RuntimeException("HttpClient,error status code :" + statusCode);
            }
            HttpEntity entity = response.getEntity();
            String result = null;
            if (entity != null)
            {
                result = EntityUtils.toString(entity, CHARSET);
            }
            EntityUtils.consume(entity);
            response.close();
            return result;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * HTTP Post 获取内容
     * 
     * @param url 请求的url地址 ?之前的地址
     * @param json 请求的json
     * @param charset 编码格式
     * @return 响应
     */
    public static String doPost(String url, String json, String charset)
    {
        if (StringUtils.isBlank(url))
        {
            return null;
        }
        try
        {
            HttpPost httpPost = new HttpPost(url);
            httpPost.addHeader("content-type", "application/json;charset=utf-8");
            httpPost.addHeader("accept", "application/json");
            httpPost.setEntity(new StringEntity(json, Charset.forName(CHARSET)));

            CloseableHttpResponse response = httpClient.execute(httpPost);
            if (HttpStatus.SC_OK != response.getStatusLine().getStatusCode())
            {
                httpPost.abort();
                throw new RuntimeException("HttpClient,error status code :" + response.getStatusLine().getStatusCode());
            }
            HttpEntity entity = response.getEntity();
            String result = null;
            if (entity != null)
            {
                result = EntityUtils.toString(entity, CHARSET);
            }
            EntityUtils.consume(entity);
            response.close();
            return result;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

}