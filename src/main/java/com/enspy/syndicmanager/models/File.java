package com.enspy.syndicmanager.models;


import lombok.Data;

@Data
public class File {
    private String filename;
    private String content;
    private long size;

}
