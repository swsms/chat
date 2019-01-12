package org.artb.chat.common.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import org.artb.chat.common.Constants;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;

public class ZdtFieldDeserializer extends JsonDeserializer<ZonedDateTime> {

    @Override
    public ZonedDateTime deserialize(
            JsonParser jsonParser,
            DeserializationContext deserializationContext) throws IOException {

        ObjectCodec codec = jsonParser.getCodec();
        JsonNode node = codec.readTree(jsonParser);
        String zdt = node.textValue();

        return ZonedDateTime.parse(zdt);
    }
}
