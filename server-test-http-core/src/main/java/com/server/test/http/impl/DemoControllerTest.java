package com.server.test.http.impl;

import com.alibaba.fastjson.JSONObject;
import com.server.test.http.common.BaseProvider;
import lombok.extern.slf4j.Slf4j;
import org.apache.xmlbeans.impl.xb.xsdschema.Public;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Map;

import static com.server.test.http.util.ComUtils.jsonCompairExpect;
import static com.server.test.http.util.DBUtils.clearData;
import static com.server.test.http.util.DBUtils.initData;

/**
 * @Author : LeePuvier
 * @CreateTime : 2019/10/21  9:33 PM
 * @ContentUse :
 */
@Slf4j
public class DemoControllerTest {

    private MockMvc mockMvc;

    @BeforeClass
    public void setUp(){
        MockitoAnnotations.initMocks(this);
        mockMvc= MockMvcBuilders.standaloneSetup("").build();
    }

    /**
     * 接口功能描述：
     * 涉及数据库表：
     * @Date 10:05 PM 2019/10/21
     * @Author LeePuvier
     **/
    @Test(dataProvider = "getById", dataProviderClass = BaseProvider.class )
    public void getByIdTest(Map<String ,Object> params){

        log.info("<--------> 开始getByIdTest!");
        //clean test data before init
        clearData(params.get("clearDataSQL").toString());
        //init test data
        Assert.assertEquals(true,initData(params.get("preDataSQL").toString()));
        //request data
        String id = params.get("id").toString();
        //build request obj
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get("/v1/order/capitalorder/getById")
                .param("Id", id)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED);
        MockHttpServletResponse response=null;
        try {
            response = mockMvc.perform(request).andReturn().getResponse();
            log.info("响应数据为:"+response.getContentAsString());
            //assert httpcode
            Assert.assertEquals(params.get("httpCode").toString(),String.valueOf(response.getStatus()));
            //assert response body
            Assert.assertEquals(true,jsonCompairExpect(JSONObject.parseObject(response.getContentAsString()),JSONObject.parseObject(params.get("expect").toString())));
        } catch (Exception e) {
            log.info("exec request error !");
            e.printStackTrace();
            Assert.assertEquals(true,false);
        }finally {
            //clear test data
            if(clearData(params.get("clearDataSQL").toString())){
                log.info("clear test data done!");
            }else {
                log.info("clear test data error!");
                Assert.assertEquals(true,false);
            }
        }
    }

}
