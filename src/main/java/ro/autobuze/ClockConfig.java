package ro.autobuze;

import java.time.Clock;
import java.time.ZoneId;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Ceasul aplicației, pe ora României (D-047); testele îl înlocuiesc cu un ceas oprit. */
@Configuration(proxyBeanMethods = false)
class ClockConfig {

    @Bean
    Clock clock() {
        return Clock.system(ZoneId.of("Europe/Bucharest"));
    }
}
