package com.server.test.http.common;

import com.server.test.http.util.ExcelUtils;
import org.testng.annotations.DataProvider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @Author : LeePuvier
 * @CreateTime : 2019/10/21  9:25 PM
 * @ContentUse :
 */
public class BaseProvider {

    @DataProvider(name = "getById")
    public Iterator<Object[]> getById() throws IOException {
        List<String> lables = new ArrayList<>();
        lables.add("0级");
        ArrayList<Object[]> testdata = ExcelUtils.getTestData(ExcelConstant.getById, "getById", lables);
        return testdata.iterator();
    }
    //TODO 上传复杂参数Case

    //创建渠道订单
    @DataProvider(name = "createChannelOrder")
    public Iterator<Object[]> createChannelOrder() throws IOException {
        List<String> lables = new ArrayList<>();
        lables.add("0级");
        lables.add("1级");
        lables.add("2级");
        ArrayList<Object[]> testdata = ExcelUtils.getTestData(ExcelConstant.createChannelOrder, "createChannelOrder", lables);
        ArrayList<Object[]> testDatawithCaseNo = getCaseNo(testdata);
        return testDatawithCaseNo.iterator();
    }

    // 获取每条case的编号，方便调试
    public static ArrayList<Object[]> getCaseNo(ArrayList<Object[]> sourceData) {
        try {
            Iterator iterator = sourceData.iterator();
            ArrayList<Object[]> testcase = new ArrayList<>();
            while (iterator.hasNext()) {
                Object[] ls = (Object[]) iterator.next();
                String caseNumber;
                for (int i = 0; i < ls.length; i++) {
                    Map<String, Object> paraMap = (Map<String, Object>) ls[i];
                    try {
                        caseNumber = paraMap.get("caseNumber").toString();
                    } catch (NullPointerException e) {
                        System.out.println("excel中的 caseNumber 列没有读取到数据，使用null值代替");
                        e.printStackTrace();
                        caseNumber = null;
                    }
                    Object[] objects = new Object[]{caseNumber, ls[i]};
                    testcase.add(objects);
                }
            }
            return testcase;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("获取订单编号时报错");
            return sourceData;
        }
    }

    // 获取BeforeClass所需要的执行的sql
    public Map<String, Object> getParaForBeforeAndAfter() throws IOException{
        List<String> lables = new ArrayList<>();
        lables.add("0级");
        ArrayList<Object[]> testdata = ExcelUtils.getTestData(ExcelConstant.getParamForBeforeAndAfter, "BeforeAndAfter", lables);
        Iterator iterator = testdata.iterator();
        while (iterator.hasNext()) {
            Object[] ls = (Object[]) iterator.next();
            if(ls.length==1){
                Map<String, Object> paraMap = (Map<String, Object>) ls[0];
                return paraMap;
            }else {
                System.out.println("从excel获取到的数据记录超过一行，无法处理");
                return null;
            }
        }
        return null;
    }
}
