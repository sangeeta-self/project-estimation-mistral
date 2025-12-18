package com.org.mistralest.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileItem {
    private String fileName;
    private String absolutePath;
    private String uploadedDate;
    private String title;
    private String methodology;
}