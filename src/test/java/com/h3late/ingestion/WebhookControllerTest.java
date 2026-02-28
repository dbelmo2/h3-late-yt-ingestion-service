package com.h3late.ingestion;

import com.h3late.ingestion.controller.WebhookController;
import com.h3late.ingestion.service.KafkaProducerService;
import com.h3late.ingestion.service.SignatureService;
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
    void shouldReturn204_WhenRequestIsValid() throws Exception {
        String mockXml = "<feed><entry><title>Test</title></entry></feed>";

        // Tell Mockito to pretend the signature is always valid for this test
        when(signatureService.isValidSignature(anyString(), anyString())).thenReturn(true);

        mockMvc.perform(post("/api/webhooks/youtube")
                        .header("X-Hub-Signature", "sha1=fakehash")
                        .contentType(MediaType.APPLICATION_XML)
                        .content(mockXml))
                .andExpect(status().isNoContent());

        // Verify that the Kafka service was actually called
        verify(kafkaProducerService, times(1)).publishVideoEvent(any(), any());
    }
}