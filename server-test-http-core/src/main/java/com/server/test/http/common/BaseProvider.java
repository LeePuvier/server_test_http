package com.server.test.http.common;

import com.server.test.http.util.ExcelUtils;
import org.testng.annotations.DataProvider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
}
