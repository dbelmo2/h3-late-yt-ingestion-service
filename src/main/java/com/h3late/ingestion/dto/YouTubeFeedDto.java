package com.h3late.ingestion.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@JacksonXmlRootElement(localName = "feed")
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class YouTubeFeedDto {
    @JacksonXmlProperty(localName = "entry")
    private Entry entry;

    @JacksonXmlProperty(localName = "deleted-entry", namespace = "http://purl.org/atompub/tombstones/1.0")
    private DeletedEntry deletedEntry;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Data
    public static class Entry {
        @JacksonXmlProperty(localName = "videoId", namespace = "yt")
        private String videoId;

        @JacksonXmlProperty(localName = "title")
        private String title;
    }

    @JsonIgnoreProperties
    @Data
    public static class DeletedEntry {
        @JacksonXmlProperty(isAttribute = true, localName = "ref")
        private String ref;

        @JacksonXmlProperty(localName = "when")
        private String when;

        // Helper method to extract just the ID
        public String getVideoId() {
            if (ref != null && ref.startsWith("yt:video:")) {
                return ref.replace("yt:video:", "");
            }
            return ref;
        }
    }
}
