package ru.practicum.ewm.main.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.practicum.ewm.stats.client.StatsClient;

@Configuration
public class StatsConfig {

    @Value("${stats.server.url}")
    private String serverUrl;

    @Bean
    public StatsClient statsClient() {
        return new StatsClient(serverUrl);
    }
}
