package org.artb.chat.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.artb.chat.common.message.Message;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class Utils {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
        objectMapper.enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL);
    }

    private Utils() { }

    public static String serialize(Message msg) throws JsonProcessingException {
        return objectMapper.writeValueAsString(msg);
    }

    public static Message deserialize(String json) throws IOException {
        return objectMapper.readValue(json, Message.class);
    }

    public static List<Message> deserializeList(String json) throws IOException {
        return Arrays.asList(objectMapper.readValue(json, Message[].class));
    }

    public static String createBatch(List<String> jsons) {
        return jsons.stream()
                .collect(Collectors.joining(",", "[", "]"));
    }

    public static boolean isBlank(String str) {
        return Objects.isNull(str) || str.trim().length() == 0;
    }

    public static boolean nonBlank(String str) {
        return !isBlank(str);
    }
}
