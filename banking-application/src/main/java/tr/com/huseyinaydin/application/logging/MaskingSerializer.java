package tr.com.huseyinaydin.application.logging;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.util.List;
import java.util.Set;

public final class MaskingSerializer {

    private static final Set<String> SENSITIVE_KEYWORDS =
            Set.of("password", "token", "secret", "salt", "hash", "pin", 
                   "nationalid", "taxnumber", "phone", "email", "mothername", "fathername",
                   "tckn", "tckimlik", "vergino", "telefon", "eposta", "anneadi", "babaadi", "cardNumber", "cvv");

    private static final ObjectMapper MAPPER = new ObjectMapper();

    static {
        MAPPER.registerModule(new JavaTimeModule());
        MAPPER.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        MAPPER.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        
        SimpleModule maskingModule = new SimpleModule();
        maskingModule.setSerializerModifier(new BeanSerializerModifier() {
            @Override
            public List<BeanPropertyWriter> changeProperties(SerializationConfig config, BeanDescription beanDesc, List<BeanPropertyWriter> beanProperties) {
                for (BeanPropertyWriter writer : beanProperties) {
                    if (isSensitive(writer)) {
                        writer.assignSerializer(new JsonSerializer<Object>() {
                            @Override
                            public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws java.io.IOException {
                                gen.writeString("***");
                            }
                        });
                    }
                }
                return beanProperties;
            }
        });
        MAPPER.registerModule(maskingModule);
    }

    private MaskingSerializer() {
    }

    private static boolean isSensitive(BeanPropertyWriter writer) {
        if (writer.getAnnotation(SensitiveData.class) != null) {
            return true;
        }
        String lower = writer.getName().toLowerCase();
        for (String keyword : SENSITIVE_KEYWORDS) {
            if (lower.contains(keyword.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    public static String serialize(Object obj) {
        if (obj == null) {
            return "null";
        }
        try {
            return MAPPER.writeValueAsString(obj);
        } catch (Exception e) {
            return "{\"error\":\"Serialization failed\"}";
        }
    }
}
