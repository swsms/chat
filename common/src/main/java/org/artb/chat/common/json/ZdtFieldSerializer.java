package org.artb.chat.common.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.artb.chat.common.Constants;

import java.io.IOException;
import java.time.ZonedDateTime;

public class ZdtFieldSerializer extends JsonSerializer<ZonedDateTime> {

    @Override
    public void serialize(
            ZonedDateTime zonedDateTime,
            JsonGenerator jsonGenerator,
            SerializerProvider serializerProvider) throws IOException {

        String isoTime = Constants.FORMATTER.format(zonedDateTime);
        jsonGenerator.writeObject(isoTime);
    }
}
