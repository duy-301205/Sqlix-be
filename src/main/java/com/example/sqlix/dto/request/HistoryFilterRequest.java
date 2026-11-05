package com.example.sqlix.dto.request;

import com.example.sqlix.enums.DifficultyLevel;

public class HistoryFilterRequest {
    private String keyword;
    private String status;
    private DifficultyLevel difficulty;
    private String sortBy = "created_at";
    private String sortDirection = "DESC";
    private int page = 0;
    private int size = 10;
    public String getKeyword(){return keyword;} public void setKeyword(String v){keyword=v;}
    public String getStatus(){return status;} public void setStatus(String v){status=v;}
    public DifficultyLevel getDifficulty(){return difficulty;} public void setDifficulty(DifficultyLevel v){difficulty=v;}
    public String getSortBy(){return sortBy;} public void setSortBy(String v){sortBy=v;}
    public String getSortDirection(){return sortDirection;} public void setSortDirection(String v){sortDirection=v;}
    public int getPage(){return page;} public void setPage(int v){page=v;}
    public int getSize(){return size;} public void setSize(int v){size=v;}
}
