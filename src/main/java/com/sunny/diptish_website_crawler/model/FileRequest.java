package com.sunny.diptish_website_crawler.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileRequest {

    private String fileName;
    private Character letter;
}
