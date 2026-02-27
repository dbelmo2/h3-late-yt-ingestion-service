package com.h3late.ingestion.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Getter;
import lombok.Setter;


@JacksonXmlRootElement(localName = "feed")
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class YouTubeFeedDto {
    @JacksonXmlProperty(localName = "entry")
    private Entry entry;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Getter
    @Setter
    public static class Entry {
        @JacksonXmlProperty(localName = "videoId", namespace = "yt")
        private String videoId;

        @JacksonXmlProperty(localName = "title")
        private String title;
    }
}
