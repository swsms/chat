package org.artb.chat.common;

import com.beust.jcommander.JCommander;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import org.artb.chat.common.message.Message;
import org.artb.chat.common.configs.ConfigParseException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
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

    public static List<Message> deserializeMessageList(String json) throws IOException {
        return Arrays.asList(objectMapper.readValue(
                json.replaceAll("\\]\\[", ","),
                Message[].class));
    }

    public static String createBatch(List<String> jsons) {
        return jsons.stream()
                .collect(Collectors.joining(",", "[", "]"));
    }

    public static boolean isBlank(String str) {
        return Objects.isNull(str) || str.trim().length() == 0;
    }

    public static LocalDateTime toLocalTimeZoneWithoutNano(ZonedDateTime zdt) {
        return zdt.withZoneSameInstant(ZoneId.systemDefault())
                .toLocalDateTime()
                .withNano(0);
    }

    public static <T> List<T> createNewListWithMessage(T msg, List<T> messages) {
        List<T> temp = new ArrayList<>();
        temp.add(msg);
        temp.addAll(messages);
        return temp;
    }

    public static <T> T parseFromArgsArray(String[] args, Class<T> clazz) throws ConfigParseException {
        try {
            T obj = clazz.newInstance();
            JCommander.newBuilder()
                    .addObject(obj)
                    .build()
                    .parse(args);
            return obj;
        } catch (Exception e) {
            throw new ConfigParseException(e);
        }
    }

    /**
     * Input list format looks like [a, b, c, d]
     */
    public static List<String> parseList(String stringList) {
        String values = stringList.substring(1, stringList.length() - 1);
        return Arrays.asList(values.split("\\s*,\\s*"));
    }

    public static <T> String serializeList(List<T> items) throws IOException {
        return objectMapper.writeValueAsString(items);
    }


    public static <T> List<T> deserializeList(String json, Class<T> clazz) throws IOException {
        CollectionType type = objectMapper.getTypeFactory().constructCollectionType(ArrayList.class, clazz);
        return objectMapper.readValue(json, type);
    }
}
