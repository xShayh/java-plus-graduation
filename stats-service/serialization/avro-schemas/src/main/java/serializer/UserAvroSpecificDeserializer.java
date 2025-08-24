package serializer;

import ru.practicum.stats.avro.UserActionAvro;

public class UserAvroSpecificDeserializer extends BaseAvroDeserializer<UserActionAvro> {
    public UserAvroSpecificDeserializer() {
        super(UserActionAvro.getClassSchema());
    }
}
