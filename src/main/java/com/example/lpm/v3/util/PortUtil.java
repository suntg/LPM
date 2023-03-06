package com.example.lpm.v3.util;

import java.util.ArrayList;
import java.util.List;

public class PortUtil {

    private final static List<Integer> COMMON_PORT = new ArrayList<>() {{
        add(21888);
        add(3306);
        add(6379);
        add(80);
        add(9999);
        add(28080);
        add(18080);
    }};


    public static Boolean contains(Integer port) {
        return COMMON_PORT.contains(port);
    }

}
