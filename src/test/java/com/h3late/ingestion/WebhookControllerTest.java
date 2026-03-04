package com.h3late.ingestion;

import com.h3late.ingestion.controller.WebhookController;
import com.h3late.ingestion.dto.YouTubeFeedDto;
import com.h3late.ingestion.service.KafkaProducerService;
import com.h3late.ingestion.service.SignatureService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WebhookController.class)
class WebhookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private KafkaProducerService kafkaProducerService;

    @MockitoBean
    private SignatureService signatureService;

    @Test
    @DisplayName("Should return 204 No Content when request is valid and contains an entry")
    void shouldReturn204_WhenRequestIsValid() throws Exception {
        String mockXml = "<feed xmlns:yt=\"http://www.youtube.com/xml/schemas/2015\"\n" +
                "         xmlns=\"http://www.w3.org/2005/Atom\">\n" +
                "  <link rel=\"hub\" href=\"https://pubsubhubbub.appspot.com\"/>\n" +
                "  <link rel=\"self\" href=\"https://www.youtube.com/xml/feeds/videos.xml?channel_id=CHANNEL_ID\"/>\n" +
                "  <title>YouTube video feed</title>\n" +
                "  <updated>2015-04-01T19:05:24.552394234+00:00</updated>\n" +
                "  <entry>\n" +
                "    <id>yt:video:123456</id>\n" +
                "    <yt:videoId>123456</yt:videoId>\n" +
                "    <yt:channelId>CHANNEL_ID</yt:channelId>\n" +
                "    <title>Test</title>\n" +
                "    <link rel=\"alternate\" href=\"http://www.youtube.com/watch?v=VIDEO_ID\"/>\n" +
                "    <author>\n" +
                "     <name>Channel title</name>\n" +
                "     <uri>http://www.youtube.com/channel/CHANNEL_ID</uri>\n" +
                "    </author>\n" +
                "    <published>2015-03-06T21:40:57+00:00</published>\n" +
                "    <updated>2015-03-09T19:05:24.552394234+00:00</updated>\n" +
                "  </entry>\n" +
                "</feed>";

        when(signatureService.isValidSignature(anyString(), anyString())).thenReturn(true);

        mockMvc.perform(post("/api/webhooks/youtube")
                        .header("X-Hub-Signature", "sha1=fakehash")
                        .contentType(MediaType.APPLICATION_XML)
                        .content(mockXml))
                .andExpect(status().isNoContent());
        YouTubeFeedDto.Entry expectedEntry = new YouTubeFeedDto.Entry();
        expectedEntry.setVideoId("123456");
        expectedEntry.setTitle("Test");
        verify(kafkaProducerService, times(1)).publishVideoEvent("123456", expectedEntry);
    }

    @Test
    @DisplayName("Should return 204 No Content when request is valid and contains a deleted entry")
    void shouldReturn204_WhenDeleteEventIsValid() throws Exception {
        String mockXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<feed xmlns=\"http://www.w3.org/2005/Atom\" \n" +
                "      xmlns:yt=\"http://www.youtube.com/xml/schemas/2015\" \n" +
                "      xmlns:at=\"http://purl.org/atompub/tombstones/1.0\">\n" +
                "      \n" +
                "  <at:deleted-entry \n" +
                "      ref=\"yt:video:AcV6R-uv8FI\" \n" +
                "      when=\"2026-03-04T17:21:26Z\">\n" +
                "      \n" +
                "      <link href=\"https://www.youtube.com/watch?v=AcV6R-uv8FI\"/>\n" +
                "      <at:by>\n" +
                "          <name>YouTube</name>\n" +
                "      </at:by>\n" +
                "  </at:deleted-entry>\n" +
                "  \n" +
                "</feed>";

        when(signatureService.isValidSignature(anyString(), anyString())).thenReturn(true);

        mockMvc.perform(post("/api/webhooks/youtube")
                        .header("X-Hub-Signature", "sha1=fakehash")
                        .contentType(MediaType.APPLICATION_XML)
                        .content(mockXml))
                .andExpect(status().isNoContent());

        verify(kafkaProducerService, times(1)).publishVideoDeleteEvent("AcV6R-uv8FI");
    }

    @Test
    @DisplayName("Should return 500 Internal Server Error when XML parsing fails")
    void shouldReturn500_WhenXmlParsingFails() throws Exception {
        String invalidXml = "<feed><entry><videoId>123456</videoId><title>Test</title></entry>"; // Missing closing tags

        when(signatureService.isValidSignature(anyString(), anyString())).thenReturn(true);

        mockMvc.perform(post("/api/webhooks/youtube")
                        .header("X-Hub-Signature", "sha1=fakehash")
                        .contentType(MediaType.APPLICATION_XML)
                        .content(invalidXml))
                .andExpect(status().isInternalServerError());

        verify(kafkaProducerService, times(0)).publishVideoEvent(anyString(), any());
    }

    @Test
    @DisplayName("Should return 204 No Content when request is valid but contains no entry or deleted entry")
    void shouldReturn204_WhenNoEntryOrDeletedEntry() throws Exception {
        String mockXml = "<feed xmlns:yt=\"http://www.youtube.com/xml/schemas/2015\"\n" +
                "         xmlns=\"http://www.w3.org/2005/Atom\">\n" +
                "  <link rel=\"hub\" href=\"https://pubsubhubbub.appspot.com\"/>\n" +
                "  <link rel=\"self\" href=\"https://www.youtube.com/xml/feeds/videos.xml?channel_id=CHANNEL_ID\"/>\n" +
                "  <title>YouTube video feed</title>\n" +
                "  <updated>2015-04-01T19:05:24.552394234+00:00</updated>\n" +
                "</feed>";

        when(signatureService.isValidSignature(anyString(), anyString())).thenReturn(true);

        mockMvc.perform(post("/api/webhooks/youtube")
                        .header("X-Hub-Signature", "sha1=fakehash")
                        .contentType(MediaType.APPLICATION_XML)
                        .content(mockXml))
                .andExpect(status().isNoContent());

        verify(kafkaProducerService, times(0)).publishVideoEvent(anyString(), any());
        verify(kafkaProducerService, times(0)).publishVideoDeleteEvent(anyString());
    }

}