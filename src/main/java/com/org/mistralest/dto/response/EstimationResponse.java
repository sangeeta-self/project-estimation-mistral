package com.org.mistralest.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class EstimationResponse {

    private String markdown;
    private Map<String, Object> json;

    public EstimationResponse() {
    }

    public EstimationResponse(String markdown, Map<String, Object> json) {
        this.markdown = markdown;
        this.json = json;
    }

    public String getMarkdown() {
        return markdown;
    }
    public void setMarkdown(String markdown) {
        this.markdown = markdown;
    }

    public Map<String, Object> getJson() {
        return json;
    }

    public void setJson(Map<String, Object> json) {
        this.json = json;
    }

    @Override
    public String toString() {
        return "EstimationResponse{markdownLength=" + (markdown==null?0:markdown.length())
                + ", jsonKeys=" + (json==null?0:json.size()) + "}";
    }

}
