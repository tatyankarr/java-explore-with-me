package ru.practicum.ewm.stats.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ViewStatsTest {

    @Test
    void shouldCreateViewStats() {
        ViewStats stats = new ViewStats("ewm-main-service", "/events/1", 10L);

        assertThat(stats.getApp()).isEqualTo("ewm-main-service");
        assertThat(stats.getUri()).isEqualTo("/events/1");
        assertThat(stats.getHits()).isEqualTo(10L);
    }

    @Test
    void shouldSetAndGetFields() {
        ViewStats stats = new ViewStats();
        stats.setApp("test-app");
        stats.setUri("/test");
        stats.setHits(5L);

        assertThat(stats.getApp()).isEqualTo("test-app");
        assertThat(stats.getUri()).isEqualTo("/test");
        assertThat(stats.getHits()).isEqualTo(5L);
    }

    @Test
    void shouldBeEqualWithSameValues() {
        ViewStats stats1 = new ViewStats("app", "/uri", 1L);
        ViewStats stats2 = new ViewStats("app", "/uri", 1L);

        assertThat(stats1).isEqualTo(stats2);
        assertThat(stats1.hashCode()).hasSameHashCodeAs(stats2);
    }
}