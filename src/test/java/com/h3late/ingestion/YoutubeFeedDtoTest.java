package com.h3late.ingestion;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.h3late.ingestion.dto.YouTubeFeedDto;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class YouTubeMappingTest {
    private final XmlMapper xmlMapper = new XmlMapper();

    @Test
    void shouldMapXmlToDtoCorrectly() throws Exception {
        String xml = """
            <feed xmlns:yt="http://www.youtube.com/xml/schemas/2015">
                <entry>
                    <yt:videoId>abc123</yt:videoId>
                    <title>H3 Podcast</title>
                </entry>
            </feed>
            """;

        YouTubeFeedDto dto = xmlMapper.readValue(xml, YouTubeFeedDto.class);

        assertNotNull(dto.getEntry());
        assertEquals("abc123", dto.getEntry().getVideoId());
        assertEquals("H3 Podcast", dto.getEntry().getTitle());
    }
}