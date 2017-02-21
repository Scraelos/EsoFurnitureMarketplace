/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.scraelos.esofurnituremp.model.tools;

import org.json.JSONObject;

/**
 *
 * @author scraelos
 */
public class LuaDecoder {

    public static JSONObject getJsonFromLua(String source) {
        JSONObject result = null;
        String[] lines = source.split("\n");
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < lines.length; i++) {
            String line = lines[i];
            sb.append(line.replaceAll("=", ":").replaceAll("[\\[\\]]", "").replaceAll("\" :", "\":").replaceAll(",\\n(.+)\\}", "\n$1}"));
        }
        String luaSource = sb.toString();
        String jsonString = luaSource.replaceAll("=", ":").replaceAll("[\\[\\]]", "").replaceAll("\" :", "\":").replaceAll(",\\n(.+)\\}", "\n$1}");
        result = new JSONObject(jsonString);
        return result;
    }

}
