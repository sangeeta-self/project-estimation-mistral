package com.org.mistralest.dto.request;

public class EstimationRequest {
    private String methodology;
    // optional: save file under this name
    private String filename;
    // optional: if true -> do not per
    private boolean history = false;
    public EstimationRequest() {}

    public String getMethodology() { return methodology; }
    public void setMethodology(String methodology) { this.methodology = methodology; }

    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }

    public boolean isHistory() { return history; }
    public void setHistory(boolean history) { this.history = history; }
}
