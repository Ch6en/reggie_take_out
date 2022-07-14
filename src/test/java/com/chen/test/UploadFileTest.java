package com.chen.test;

import org.junit.jupiter.api.Test;

public class UploadFileTest {
    @Test
    public void test1() {
        String fileName = "abcd.jpg";
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        System.out.println(suffix);
    }
}
