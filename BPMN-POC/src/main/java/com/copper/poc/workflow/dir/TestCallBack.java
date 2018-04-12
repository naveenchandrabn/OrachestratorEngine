/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.copper.poc.workflow.dir;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author jaja0617
 */
public class TestCallBack implements Serializable {

    private static final long serialVersionUID = 1L;

    private Map<String, String> map = new HashMap<>();

    public Map firstcall() {
        map.put("1", "1");
        return map;
    }

    public Map secondCall() {
        map.put("2", "2");
        return map;
    }

}
