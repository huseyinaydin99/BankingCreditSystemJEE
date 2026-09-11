package tr.com.huseyinaydin.web.info;

import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.context.ApplicationContext;
import org.springframework.core.SpringVersion;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class BankingInfoContributor implements InfoContributor {

    private static final DateTimeFormatter ISO_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
                    .withZone(ZoneId.of("UTC"));

    private static final String APP_VERSION = "1.0.0";

    private final Environment environment;
    private final ApplicationContext applicationContext;

    public BankingInfoContributor(Environment environment,
                                   ApplicationContext applicationContext) {
        this.environment = environment;
        this.applicationContext = applicationContext;
    }

    @Override
    public void contribute(Info.Builder builder) {
        builder.withDetail("app",     buildAppInfo());
        builder.withDetail("runtime", buildRuntimeInfo());
        builder.withDetail("build",   buildBuildInfo());
    }

    private Map<String, Object> buildAppInfo() {
        Map<String, Object> app = new LinkedHashMap<>();
        app.put("name",        "Banking Credit System");
        app.put("version",     APP_VERSION);
        app.put("description", "Clean Architecture tabanlı kurumsal bankacılık/kredi sistemi");
        app.put("groupId",     "tr.com.huseyinaydin");
        app.put("artifactId",  "enterprise-java-template");
        return app;
    }

    private Map<String, Object> buildRuntimeInfo() {
        Map<String, Object> runtime = new LinkedHashMap<>();

        String[] activeProfiles = environment.getActiveProfiles();
        String activeProfile = activeProfiles.length > 0
                ? String.join(", ", Arrays.asList(activeProfiles))
                : "default";
        runtime.put("activeProfile", activeProfile);

        long startupEpochMs = applicationContext.getStartupDate();
        Instant startupInstant = Instant.ofEpochMilli(startupEpochMs);
        runtime.put("startupTime", ISO_FORMATTER.format(startupInstant));

        long uptimeSeconds = (System.currentTimeMillis() - startupEpochMs) / 1000;
        runtime.put("uptimeSeconds", uptimeSeconds);

        runtime.put("javaVersion",  System.getProperty("java.version", "unknown"));
        runtime.put("javaVendor",   System.getProperty("java.vendor", "unknown"));
        runtime.put("springVersion", SpringVersion.getVersion());

        return runtime;
    }

    private Map<String, Object> buildBuildInfo() {
        Map<String, Object> build = new LinkedHashMap<>();
        build.put("packaging",   "WAR");
        build.put("javaTarget",  "17");
        build.put("framework",
                "Spring Framework 6.1.8 · Hibernate ORM 6.4.4 · Jakarta EE 10 · Oracle 23c");
        build.put("architecture",
                "Clean Architecture · DDD · CQRS · Mediator · Pipeline");
        return build;
    }
}
