package com.code.common;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.dataformat.javaprop.JavaPropsFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.fasterxml.jackson.dataformat.yaml.YAMLParser;

import java.io.*;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

/**
 * @program: config
 * @description: 转换工具类
 * @author: xujingyang
 * @create: 2019-03-27 12:16
 **/
public class TransferUtils {

    private static final String ENCODING = "utf-8";
    private static final String DOT = ".";

    public static Properties yml2Properties(String content) {
        try {
            YAMLFactory yamlFactory = new YAMLFactory();
            YAMLParser parser = yamlFactory.createParser(content);
            JsonToken token = parser.nextToken();
            PropertiesClass propertiesClass = new PropertiesClass();
            while (token != null) {
                testParse(token, parser, propertiesClass);
                token = parser.nextToken();
            }
            parser.close();
            return propertiesClass.properties;
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void testParse(JsonToken token, YAMLParser parser, PropertiesClass properties) throws IOException {
        switch (token) {
            case START_OBJECT:
                break;
            case END_OBJECT:
                properties.key = resetKey(properties.lines, properties.key);
                break;
            case FIELD_NAME:
                String currentKey = parser.getCurrentName();
                if (StringUtils.isNoBlank(properties.key)) {
                    properties.key = properties.key + DOT + currentKey;
                } else {
                    properties.key = currentKey;
                }
                break;
            case VALUE_STRING:
            case VALUE_NUMBER_INT:
            case VALUE_NUMBER_FLOAT:
            case VALUE_TRUE:
            case VALUE_FALSE:
            case VALUE_NULL:
                properties.key = parseValue(properties, parser);
                break;
            case START_ARRAY:
                properties.flag = "array";
                break;
            case END_ARRAY:
                properties.flag = "text";
                properties.key = parseValue(properties, parser);
                break;
            case NOT_AVAILABLE:
                break;
            case VALUE_EMBEDDED_OBJECT:
                break;
            default:
                break;
        }
    }

    private static String parseValue(PropertiesClass properties, YAMLParser parser) throws IOException {
        String text = parser.getText();
        switch (properties.flag) {
            case "text":
                String value = StringUtils.isNoBlank(properties.value) ? properties.value : text;
                if (StringUtils.isNoBlank(value.trim())) {
                    properties.lines.add(properties.key + "=" + value);
                    properties.properties.setProperty(properties.key, value);
                    properties.key = resetKey(properties.lines, properties.key);
                    properties.value = "";
                }
                break;
            case "array":
                if (StringUtils.isNoBlank(properties.value)) {
                    properties.value = properties.value + "," + text;
                } else {
                    properties.value = text;
                }
                break;
        }
        return properties.key;
    }

    private static String resetKey(List<String> lines, String key) {
        int dotOffset = key.lastIndexOf(DOT);
        if (dotOffset > 0) {
            key = key.substring(0, dotOffset);
        } else {
            key = "";
            lines.add("");
        }
        return key;
    }

    public static void properties2Yaml(String path) {
        JsonParser parser = null;
        JavaPropsFactory factory = new JavaPropsFactory();
        try {
            parser = factory.createParser(
                    new InputStreamReader(new FileInputStream(path), Charset.forName(ENCODING)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            YAMLFactory yamlFactory = new YAMLFactory();
            YAMLGenerator generator = yamlFactory.createGenerator(
                    new OutputStreamWriter(new FileOutputStream(path), Charset.forName(ENCODING)));

            JsonToken token = parser.nextToken();

            while (token != null) {
                if (JsonToken.START_OBJECT.equals(token)) {
                    generator.writeStartObject();
                } else if (JsonToken.FIELD_NAME.equals(token)) {
                    generator.writeFieldName(parser.getCurrentName());
                } else if (JsonToken.VALUE_STRING.equals(token)) {
                    generator.writeString(parser.getText());
                } else if (JsonToken.END_OBJECT.equals(token)) {
                    generator.writeEndObject();
                }
                token = parser.nextToken();
            }
            parser.close();
            generator.flush();
            generator.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static class PropertiesClass {
        List<String> lines = new LinkedList<>();
        String key;
        String value;
        String flag = "text";
        Properties properties = new Properties();


    }

    public static void main(String[] args) throws IOException {
        String path = "C:\\Users\\joshua\\Desktop\\config.yml";
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(path), Charset.forName(ENCODING)));
        StringBuffer sb = new StringBuffer();
        while (reader.ready()) {
            sb.append(reader.readLine());
            sb.append("\r\n");
        }
        Properties properties = yml2Properties(sb.toString());
        System.out.println(1);
    }
}