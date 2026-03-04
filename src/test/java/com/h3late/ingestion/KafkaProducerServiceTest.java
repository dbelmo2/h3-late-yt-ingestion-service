package com.h3late.ingestion;

import com.h3late.ingestion.dto.YouTubeFeedDto;
import com.h3late.ingestion.service.KafkaProducerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@DisplayName("KafkaProducerService Tests")
class KafkaProducerServiceTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    private KafkaProducerService kafkaProducerService;
    private static final String TEST_TOPIC = "youtube-events";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        kafkaProducerService = new KafkaProducerService(kafkaTemplate, TEST_TOPIC);
    }

    @Test
    @DisplayName("Should publish video event with correct topic, videoId key, and message body")
    void testPublishVideoEvent() {
        // Arrange
        String videoId = "test-video-123";
        YouTubeFeedDto.Entry entry = new YouTubeFeedDto.Entry();
        entry.setVideoId(videoId);
        entry.setTitle("Test Video Title");

        CompletableFuture<SendResult<String, Object>> completableFuture = CompletableFuture.completedFuture(null);
        when(kafkaTemplate.send(TEST_TOPIC, videoId, entry)).thenReturn(completableFuture);

        // Act
        kafkaProducerService.publishVideoEvent(videoId, entry);

        // Assert
        verify(kafkaTemplate, times(1)).send(TEST_TOPIC, videoId, entry);
        verifyNoMoreInteractions(kafkaTemplate);
    }

    @Test
    @DisplayName("Should publish video delete event with correct topic, videoId key, and null value")
    void testPublishVideoDeleteEvent() {
        // Arrange
        String videoId = "deleted-video-456";

        CompletableFuture<SendResult<String, Object>> completableFuture = CompletableFuture.completedFuture(null);
        when(kafkaTemplate.send(TEST_TOPIC, videoId, null)).thenReturn(completableFuture);

        // Act
        kafkaProducerService.publishVideoDeleteEvent(videoId);

        // Assert
        verify(kafkaTemplate, times(1)).send(TEST_TOPIC, videoId, null);
        verifyNoMoreInteractions(kafkaTemplate);
    }

    @Test
    @DisplayName("Should use the correct topic name for publishing video events")
    void testPublishVideoEventUsesCorrectTopic() {
        // Arrange
        String videoId = "video-789";
        YouTubeFeedDto.Entry entry = new YouTubeFeedDto.Entry();
        entry.setVideoId(videoId);
        entry.setTitle("Another Test Video");

        CompletableFuture<SendResult<String, Object>> completableFuture = CompletableFuture.completedFuture(null);
        when(kafkaTemplate.send(eq(TEST_TOPIC), eq(videoId), any())).thenReturn(completableFuture);

        // Act
        kafkaProducerService.publishVideoEvent(videoId, entry);

        // Assert
        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        verify(kafkaTemplate).send(topicCaptor.capture(), eq(videoId), any());
        assertThat(topicCaptor.getValue()).isEqualTo(TEST_TOPIC);
    }

    @Test
    @DisplayName("Should use the correct topic name for publishing delete events")
    void testPublishVideoDeleteEventUsesCorrectTopic() {
        // Arrange
        String videoId = "delete-video-999";

        CompletableFuture<SendResult<String, Object>> completableFuture = CompletableFuture.completedFuture(null);
        when(kafkaTemplate.send(eq(TEST_TOPIC), eq(videoId), isNull())).thenReturn(completableFuture);

        // Act
        kafkaProducerService.publishVideoDeleteEvent(videoId);

        // Assert
        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        verify(kafkaTemplate).send(topicCaptor.capture(), eq(videoId), isNull());
        assertThat(topicCaptor.getValue()).isEqualTo(TEST_TOPIC);
    }

    @Test
    @DisplayName("Should publish multiple video events without interfering with each other")
    void testPublishMultipleVideoEvents() {
        // Arrange
        String videoId1 = "video-1";
        String videoId2 = "video-2";
        YouTubeFeedDto.Entry entry1 = new YouTubeFeedDto.Entry();
        entry1.setVideoId(videoId1);
        entry1.setTitle("Video 1");

        YouTubeFeedDto.Entry entry2 = new YouTubeFeedDto.Entry();
        entry2.setVideoId(videoId2);
        entry2.setTitle("Video 2");

        CompletableFuture<SendResult<String, Object>> completableFuture = CompletableFuture.completedFuture(null);
        when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(completableFuture);

        // Act
        kafkaProducerService.publishVideoEvent(videoId1, entry1);
        kafkaProducerService.publishVideoEvent(videoId2, entry2);

        // Assert
        verify(kafkaTemplate, times(1)).send(TEST_TOPIC, videoId1, entry1);
        verify(kafkaTemplate, times(1)).send(TEST_TOPIC, videoId2, entry2);
        verify(kafkaTemplate, times(2)).send(eq(TEST_TOPIC), anyString(), any());
    }

    @Test
    @DisplayName("Should handle video events with null title")
    void testPublishVideoEventWithNullTitle() {
        // Arrange
        String videoId = "video-null-title";
        YouTubeFeedDto.Entry entry = new YouTubeFeedDto.Entry();
        entry.setVideoId(videoId);
        entry.setTitle(null);

        CompletableFuture<SendResult<String, Object>> completableFuture = CompletableFuture.completedFuture(null);
        when(kafkaTemplate.send(TEST_TOPIC, videoId, entry)).thenReturn(completableFuture);

        // Act
        kafkaProducerService.publishVideoEvent(videoId, entry);

        // Assert
        verify(kafkaTemplate, times(1)).send(TEST_TOPIC, videoId, entry);
    }

    @Test
    @DisplayName("Should use videoId as message key in Kafka")
    void testVideoIdUsedAsMessageKey() {
        // Arrange
        String videoId = "key-test-video";
        YouTubeFeedDto.Entry entry = new YouTubeFeedDto.Entry();
        entry.setVideoId(videoId);
        entry.setTitle("Key Test");

        CompletableFuture<SendResult<String, Object>> completableFuture = CompletableFuture.completedFuture(null);
        when(kafkaTemplate.send(eq(TEST_TOPIC), eq(videoId), any())).thenReturn(completableFuture);

        // Act
        kafkaProducerService.publishVideoEvent(videoId, entry);

        // Assert
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        verify(kafkaTemplate).send(eq(TEST_TOPIC), keyCaptor.capture(), any());
        assertThat(keyCaptor.getValue()).isEqualTo(videoId);
    }
}
