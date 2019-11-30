package com.master.config;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileReader;
public class ConfigReader {
    private JSONParser parser;
    private JSONObject fileAsJSON;
    public ConfigReader() {
         parser = new JSONParser();

        try {
            File file = new File(
                    getClass().getClassLoader().getResource("CONFIG.JSON").getFile()
            );
            FileReader reader = new FileReader(file);

            Object obj = parser.parse(reader);
            this.fileAsJSON = (JSONObject) obj;
        } catch (Exception e){
            e.printStackTrace();
        }
    }
    public Object getProperty(String key)
    {

        return this.fileAsJSON.get(key);
    }
}
