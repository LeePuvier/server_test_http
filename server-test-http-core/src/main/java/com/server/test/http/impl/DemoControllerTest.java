package com.server.test.http.impl;

import com.alibaba.fastjson.JSONObject;
import com.server.test.http.common.BaseProvider;
import com.server.test.http.util.AopTargetUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.xmlbeans.impl.xb.xsdschema.Public;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.util.NestedServletException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.sql.SQLException;
import java.util.Map;

import static com.server.test.http.util.ComUtils.jsonCompairExpect;
import static com.server.test.http.util.DBUtils.clearData;
import static com.server.test.http.util.DBUtils.initData;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

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


    /**
     * 接口功能描述：
     * 涉及数据库表：
     * @Date 1:50 PM 2019/10/22
     * @Author LeePuvier
     **/

    @Test(dataProvider = "createChannelOrder", dataProviderClass = BaseProvider.class)
    public void createChannelOrderTest(String testNo, Map<String, Object> para) throws Exception {
        // 清理环境
        JSONObject requestData = JSONObject.parseObject(para.get("requestData").toString());
        clearRedis(requestData);


        // 打印订单编号
        log.info("用例编号：" + testNo);


        // 初始化
        clearData(para.get("clearDataSQL").toString());
        Assert.assertEquals(true, initData(para.get("preDataSQL").toString()));


        // mock三方服务
        ReflectionTestUtils.setField(AopTargetUtils.getTarget(channelProductVerifyService), "channelFundSchemeSnapshotApi", channelFundSchemeSnapshotApiMock);
        ReflectionTestUtils.setField(AopTargetUtils.getTarget(channelProductVerifyService), "channelApi", channelApiMock);
        ReflectionTestUtils.setField(AopTargetUtils.getTarget(channelOrderService), "rabbitUtils", rabbitUtilsMock);
        ReflectionTestUtils.setField(AopTargetUtils.getTarget(limitAmountService), "channelProductApi", channelProductApiMock);


        // 构造mock返回值
        // 构造mock返回值-构造渠道资金方案快照返回值
        String channelFundSchemeSnapshotStr = para.get("channelFundSchemeSnapshotMockData").toString();
        if (null != channelFundSchemeSnapshotStr && !"".equals(channelFundSchemeSnapshotStr)) {
            ChannelFundSchemeSnapshot channelFundSchemeSnapshot = JSONObject.parseObject(channelFundSchemeSnapshotStr, ChannelFundSchemeSnapshot.class);
            when(channelFundSchemeSnapshotApiMock.findLatestSnapshotBySchemeId(Mockito.anyLong())).thenReturn(channelFundSchemeSnapshot);
        } else {
            when(channelFundSchemeSnapshotApiMock.findLatestSnapshotBySchemeId(Mockito.anyLong())).thenReturn(null);
        }

        // 构造mock返回值-构造渠道返回值
        String channelStr = para.get("channelMockData").toString();
        if (null != channelStr && !"".equals(channelStr)) {
            Channel channel = JSONObject.parseObject(channelStr, Channel.class);
            when(channelApiMock.findOneChannel(Mockito.anyLong())).thenReturn(channel);
        } else {
            when(channelApiMock.findOneChannel(Mockito.anyLong())).thenReturn(null);
        }

        // 构造mock返回值-构造消息推送
        doNothing().when(rabbitUtilsMock).send(Mockito.anyString(), Mockito.anyString(), Mockito.any());

        // 构造mock返回值-构造渠道产品返回值
        String channelProductStr = para.get("channelProductMockData").toString();
        if (null != channelProductStr && !"".equals(channelProductStr)) {
            ChannelProduct channelProduct = JSONObject.parseObject(channelProductStr, ChannelProduct.class);
            when(channelProductApiMock.selectOneById(Mockito.anyLong())).thenReturn(channelProduct);
        } else {
            when(channelProductApiMock.selectOneById(Mockito.anyLong())).thenReturn(null);
        }

        try {
            // 构造请求
            String requestDataStr = para.get("requestData").toString();
            JSONObject requestJson = JSONObject.parseObject(requestDataStr);

            MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                    .post("/v1/order/channelorder/create")
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content(requestJson.toJSONString());
            // 执行请求
            MockHttpServletResponse response = mockMvc.perform(request)
                    .andReturn().getResponse();
            log.info("返回结果：" + response.getContentAsString());

            // 校验HTTP响应码
            Assert.assertEquals(para.get("httpCode").toString(), String.valueOf(response.getStatus()));

            // 校验接口响应内容
            String flag = para.get("flag").toString();
            String channelOrderNo = para.get("channelOrderNo").toString();

            // 获取渠道订单ID
            channelOrderId = getChannelOrderId(channelOrderNo);

            // 判定校验流程
            switch (flag) {
                // 提单成功
                case "0":
                    // 获取参数值
                    String responseExpectStr = para.get("expect").toString();
                    JSONObject responseExpectJson = JSONObject.parseObject(responseExpectStr);
                    JSONObject responseActualJson = JSONObject.parseObject(response.getContentAsString());

                    // 校验返回值的errcode和msg
                    Assert.assertEquals(responseExpectJson.getString("errcode"), responseActualJson.getString("errcode"));
                    Assert.assertEquals(responseExpectJson.getString("msg"), responseActualJson.getString("msg"));

                    // 校验mysql数据库中的数据
                    JSONObject expectedMysqlData = JSONObject.parseObject(para.get("mysqlData").toString());
                    String sql = "SELECT $field$ FROM zfpt_channel_order WHERE channel_order_no = \"" + channelOrderNo + "\"";
                    Assert.assertTrue(compareMysqlDataNew(expectedMysqlData, sql), "mysql数据库校验失败");

                    // 校验 mongo 数据库中的数据
                    JSONObject expectedMongoData = JSONObject.parseObject(para.get("mongoData").toString());
                    Assert.assertNotEquals(-2L, channelOrderId, "没有查到渠道订单ID");
                    Assert.assertTrue(compareMongoData(expectedMongoData, customerService, channelOrderId), "mongo 数据库校验失败");
                    break;

                // 提单失败，有响应
                case "1":
                    // 获取参数值
                    responseExpectStr = para.get("expect").toString();
                    responseExpectJson = JSONObject.parseObject(responseExpectStr);
                    responseActualJson = JSONObject.parseObject(response.getContentAsString());

                    // 校验返回值的errcode和msg
                    Assert.assertEquals(responseExpectJson.getString("errcode"), responseActualJson.getString("errcode"));
                    Assert.assertEquals(responseExpectJson.getString("msg"), responseActualJson.getString("msg"));
                    break;

                // 提单失败，无响应内容
                case "2":
                    break;

                // 从excel获取数据失败
                default:
                    log.info("excel中数据为" + flag);
                    Assert.assertTrue(false, "excel文件中flag列数据错误，不在可用范围内");
            }
        } catch (NestedServletException ne) {
            String[] tempList = ne.getMessage().split("BusinessException");
            if (tempList.length != 2) {
                ne.printStackTrace();
                Assert.assertTrue(false, "程序出现 测试范围内无法解决的NestedServletException异常");
            } else {
                String eMsg = tempList[1];
                JSONObject expectJsonObject = JSONObject.parseObject(para.get("exceptionData").toString());
                Assert.assertTrue(judgeExceptionRough(eMsg, expectJsonObject));
            }
        } catch (Exception e) {
            e.printStackTrace();
            Assert.assertTrue(false, "程序出现异常");
        } finally {
            try {
                // 还原三方服务
                ReflectionTestUtils.setField(AopTargetUtils.getTarget(channelProductVerifyService), "channelFundSchemeSnapshotApi", channelFundSchemeSnapshotApi);
                ReflectionTestUtils.setField(AopTargetUtils.getTarget(channelProductVerifyService), "channelApi", channelApi);
                ReflectionTestUtils.setField(AopTargetUtils.getTarget(channelOrderService), "rabbitUtils", rabbitUtils);
                ReflectionTestUtils.setField(AopTargetUtils.getTarget(limitAmountService), "channelProductApi", channelProductApi);

                // 清理数据
                clear(para, customerService);
            } catch (SQLException e4) {
                e4.printStackTrace();

            }
        }
    }
}
