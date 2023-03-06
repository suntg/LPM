package com.example.lpm.v3.util;

import java.util.Arrays;
import java.util.List;

public class PortUtil {

    private final static List<Integer> COMMON_PORT = Arrays.asList(21888, 3306, 6379, 80, 9999, 28080, 18080, 443);


    public static Boolean contains(Integer port) {
        return COMMON_PORT.contains(port);
    }

}
