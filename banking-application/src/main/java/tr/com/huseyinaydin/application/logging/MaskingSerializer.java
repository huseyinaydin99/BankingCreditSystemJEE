package tr.com.huseyinaydin.application.logging;

import java.lang.reflect.Field;
import java.util.Set;

/**
 * Bir nesneyi (genellikle command request) loglama için kompakt JSON string'e çeviren,
 * reflection tabanlı serileştirici. Adı gizli anahtar sözcük içeren alanlar ("password",
 * "passwordHash", "passwordSalt", "token" ve benzeri) değerini {@code "***"} ile maskeler,
 * böylece hassas veriler loglara sızmaz.
 *
 * Sığ (shallow) serileştirme yapar: alan değerleri {@code String.valueOf} ile yazılır
 * (iç içe nesnelerin kendi toString'i kullanılır). Salt loglama amaçlıdır.
 */
public final class MaskingSerializer {

    private static final Set<String> SENSITIVE_KEYWORDS =
            Set.of("password", "token", "secret", "salt", "hash", "pin");

    private static final String MASK = "***";

    private MaskingSerializer() {
    }

    public static String serialize(Object obj) {
        if (obj == null) {
            return "null";
        }

        StringBuilder sb = new StringBuilder("{");
        Field[] fields = obj.getClass().getDeclaredFields();
        boolean first = true;
        for (Field field : fields) {
            if (field.isSynthetic()) {
                continue;
            }
            if (!first) {
                sb.append(",");
            }
            first = false;

            String name = field.getName();
            sb.append("\"").append(escape(name)).append("\":");

            if (isSensitive(name)) {
                sb.append("\"").append(MASK).append("\"");
            } else {
                sb.append("\"").append(escape(readValue(field, obj))).append("\"");
            }
        }
        return sb.append("}").toString();
    }

    private static boolean isSensitive(String fieldName) {
        String lower = fieldName.toLowerCase();
        for (String keyword : SENSITIVE_KEYWORDS) {
            if (lower.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private static String readValue(Field field, Object obj) {
        try {
            field.setAccessible(true);
            return String.valueOf(field.get(obj));
        } catch (Exception e) {
            return "?";
        }
    }

    private static String escape(String s) {
        if (s == null) {
            return "null";
        }
        StringBuilder out = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"'  -> out.append("\\\"");
                case '\\' -> out.append("\\\\");
                case '\n' -> out.append("\\n");
                case '\r' -> out.append("\\r");
                case '\t' -> out.append("\\t");
                default   -> out.append(c);
            }
        }
        return out.toString();
    }
}
