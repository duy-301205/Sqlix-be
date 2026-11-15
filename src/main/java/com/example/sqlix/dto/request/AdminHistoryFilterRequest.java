package com.example.sqlix.dto.request;

import java.time.Instant;
import java.util.UUID;
import com.example.sqlix.enums.TestSessionStatus;

public class AdminHistoryFilterRequest {
    private String keyword;
    private Boolean isPassed;
    private TestSessionStatus status;
    public String getKeyword(){return keyword;} public void setKeyword(String v){keyword=v;}
    public Boolean getIsPassed(){return isPassed;} public void setIsPassed(Boolean v){isPassed=v;}
    public TestSessionStatus getStatus(){return status;} public void setStatus(TestSessionStatus v){status=v;}
    private UUID userId, testId;
    private Instant fromDate, toDate;
    private Integer minScore, maxScore;
    private int page=0, size=20;
    private String sortBy="created_at", sortDirection="DESC";
    public UUID getUserId(){return userId;} public void setUserId(UUID v){userId=v;}
    public UUID getTestId(){return testId;} public void setTestId(UUID v){testId=v;}
    public Instant getFromDate(){return fromDate;} public void setFromDate(Instant v){fromDate=v;}
    public Instant getToDate(){return toDate;} public void setToDate(Instant v){toDate=v;}
    public Integer getMinScore(){return minScore;} public void setMinScore(Integer v){minScore=v;}
    public Integer getMaxScore(){return maxScore;} public void setMaxScore(Integer v){maxScore=v;}
    public int getPage(){return page;} public void setPage(int v){page=v;}
    public int getSize(){return size;} public void setSize(int v){size=v;}
    public String getSortBy(){return sortBy;} public void setSortBy(String v){sortBy=v;}
    public String getSortDirection(){return sortDirection;} public void setSortDirection(String v){sortDirection=v;}
}
