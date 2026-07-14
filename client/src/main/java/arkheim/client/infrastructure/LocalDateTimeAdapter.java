// VIBE CODED.
package arkheim.client.infrastructure;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LocalDateTimeAdapter extends TypeAdapter<LocalDateTime> {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Override
    public void write(JsonWriter out, LocalDateTime value) throws IOException {
        if (value == null) {
            out.nullValue();
        } else {
            out.value(value.format(formatter));
        }
    }

    @Override
    public LocalDateTime read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        } else if (in.peek() == JsonToken.BEGIN_OBJECT) {
            JsonObject obj = JsonParser.parseReader(in).getAsJsonObject();
            if (obj.has("date") && obj.has("time")) {
                JsonObject dateObj = obj.getAsJsonObject("date");
                JsonObject timeObj = obj.getAsJsonObject("time");
                int year = dateObj.get("year").getAsInt();
                int month = dateObj.get("month").getAsInt();
                int day = dateObj.get("day").getAsInt();
                int hour = timeObj.get("hour").getAsInt();
                int minute = timeObj.get("minute").getAsInt();
                int second = timeObj.get("second").getAsInt();
                int nano = timeObj.has("nano") ? timeObj.get("nano").getAsInt() : 0;
                return LocalDateTime.of(year, month, day, hour, minute, second, nano);
            } else {
                int year = obj.get("year").getAsInt();
                int month = obj.get("month").getAsInt();
                int day = obj.get("day").getAsInt();
                int hour = obj.has("hour") ? obj.get("hour").getAsInt() : 0;
                int minute = obj.has("minute") ? obj.get("minute").getAsInt() : 0;
                int second = obj.has("second") ? obj.get("second").getAsInt() : 0;
                int nano = obj.has("nano") ? obj.get("nano").getAsInt() : 0;
                return LocalDateTime.of(year, month, day, hour, minute, second, nano);
            }
        } else {
            String str = in.nextString();
            if (str.contains(" ")) {
                str = str.replace(" ", "T");
            }
            // Remove trailing timezone if present (e.g. Z or offset) to parse as LocalDateTime
            if (str.endsWith("Z")) {
                str = str.substring(0, str.length() - 1);
            }
            return LocalDateTime.parse(str, formatter);
        }
    }
}
