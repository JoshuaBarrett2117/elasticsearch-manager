package com.code.common;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonStreamContext;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.dataformat.javaprop.JavaPropsFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.fasterxml.jackson.dataformat.yaml.YAMLParser;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

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
                ymlTokenParse(token, parser, propertiesClass);
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

    private static void ymlTokenParse(JsonToken token, YAMLParser parser, PropertiesClass properties) throws IOException {
        switch (token) {
            case START_OBJECT:
//                properties.key = "";
                break;
            case END_OBJECT:
                properties.resetUpLayerKey();
                break;
            case FIELD_NAME:
                String currentKey = parser.getCurrentName();
                if (StringUtils.isNoBlank(properties.key)) {
                    properties.pushKey(properties.key + DOT + currentKey);
                } else {
                    properties.pushKey(currentKey);
                }
                break;
            case VALUE_STRING:
            case VALUE_NUMBER_INT:
            case VALUE_NUMBER_FLOAT:
            case VALUE_TRUE:
            case VALUE_FALSE:
            case VALUE_NULL:
                properties.key = ymlParseValue(properties, parser);
                break;
            case START_ARRAY:
                properties.flag = "array";
                break;
            case END_ARRAY:
                properties.flag = "text";
                properties.key = ymlParseValue(properties, parser);
                break;
            case NOT_AVAILABLE:
                break;
            case VALUE_EMBEDDED_OBJECT:
                break;
            default:
                break;
        }
    }

    private static String ymlParseValue(PropertiesClass properties, YAMLParser parser) throws IOException {
        String text = parser.getText();
        switch (properties.flag) {
            case "text":
                String value = StringUtils.isNoBlank(properties.value) ? properties.value : text;
                if (StringUtils.isNoBlank(value.trim())) {
                    properties.lines.add(properties.key + "=" + value);
                    properties.properties.setProperty(properties.key, value);
                    properties.resetUpLayerKey();
                    properties.value = "";
                } else {
                    throw new RuntimeException("[" + properties.key + "]的值不允许为空");
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

    public static String properties2Yaml(String content) {
        return properties2Yaml(content, null);
    }

    public static String properties2Yaml(String content, OutputStream out) {
        JsonParser parser = null;
        JavaPropsFactory factory = new JavaPropsFactory();
        if (out == null) {
            out = new StringBufferOutputStream();
        }
        try {
            parser = factory.createParser(content);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            YAMLFactory yamlFactory = new YAMLFactory();
            YAMLGenerator generator = yamlFactory.createGenerator(
                    new OutputStreamWriter(out, Charset.forName(ENCODING)));
            JsonToken token = parser.nextToken();
            while (token != null) {
                propTokenPaser(token, generator, parser);
                token = parser.nextToken();
            }
            parser.close();
            generator.flush();
            generator.close();
        } catch (IOException e) {
            new RuntimeException(e);
        }
        try {
            if (out instanceof StringBufferOutputStream) {
                return ((StringBufferOutputStream) out).getContent();
            } else {
                return null;
            }
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    private static void propTokenPaser(JsonToken token, YAMLGenerator generator, JsonParser parser) throws IOException {
        if (JsonToken.START_OBJECT.equals(token)) {
            generator.writeStartObject();
        } else if (JsonToken.FIELD_NAME.equals(token)) {
            generator.writeFieldName(parser.getCurrentName());
        } else if (JsonToken.VALUE_STRING.equals(token)) {
            generator.writeString(parser.getText());
        } else if (JsonToken.END_OBJECT.equals(token)) {
            generator.writeEndObject();
        }
    }

    private static class PropertiesClass {
        List<String> lines = new LinkedList<>();
        Stack<String> lastLayerKeys = new Stack<>();
        String key;
        String value;
        String flag = "text";
        Properties properties = new Properties();

        private void resetUpLayerKey() {
            if (lastLayerKeys.empty()) {
                key = "";
            } else {
                key = lastLayerKeys.pop();
            }
        }

        private void pushKey(String key) {
            lastLayerKeys.push(this.key);
            this.key = key;
        }

    }

    public static class StringBufferOutputStream extends OutputStream {
        private int INIT_COUNT = 1000;
        private byte[] bytes;
        private Integer count = 0;

        public StringBufferOutputStream() {
            this.bytes = new byte[INIT_COUNT];
        }

        public StringBufferOutputStream(int initCount) {
            this.INIT_COUNT = initCount;
            this.bytes = new byte[initCount];
        }

        @Override
        public void write(int b) throws IOException {
            synchronized (count) {
                //满了，要扩展字节数组，每次拓展INIT_COUNT个
                if (count == bytes.length) {
                    byte[] bytes2 = bytes;
                    bytes = new byte[bytes.length + INIT_COUNT];
                    for (int i = 0; i < bytes2.length; i++) {
                        bytes[i] = bytes2[i];
                    }
                }
                bytes[count] = (byte) b;
                count++;
            }
        }

        private byte[] sumHex(int tu5) {
            int maxLength = (int) Math.ceil(Math.log(tu5) / Math.log(255));
            byte[] bytes5 = new byte[maxLength];
            for (int i = 0; i < maxLength; i++) {
                byte b = (byte) ((tu5 >> i) & 0xFF);
                bytes5[i] = b;
            }
            return bytes5;
        }

        public String getContent() throws UnsupportedEncodingException {
            return new String(bytes, ENCODING);
        }
    }


    public static void main(String[] args) throws IOException {
        String path = "C:\\Users\\joshua\\Desktop\\elasticsearch.yml";
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(path), Charset.forName(ENCODING)));
        StringBuffer sb = new StringBuffer();
        while (reader.ready()) {
            sb.append(reader.readLine());
            sb.append("\r\n");
        }
        Properties properties = yml2Properties(sb.toString());
        StringBuffer sb2 = new StringBuffer();
        for (Map.Entry<Object, Object> objectObjectEntry : properties.entrySet()) {
            sb2.append(objectObjectEntry.getKey() + "=" + objectObjectEntry.getValue());
            sb2.append("\r\n");
        }
        String s = properties2Yaml(sb2.toString());
        System.out.println(s);
    }

}