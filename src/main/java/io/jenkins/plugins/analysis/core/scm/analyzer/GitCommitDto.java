package io.jenkins.plugins.analysis.core.scm.analyzer;

public class GitCommitDto {

    String authorId;
    String committerId;
    int commitTime;
    String message;

    public GitCommitDto(String authorId, String committerId, int commitTime, String message) {
        this.authorId = authorId;
        this.committerId = committerId;
        this.commitTime = commitTime;
        this.message = message;
    }

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public String getCommitterId() {
        return committerId;
    }

    public void setCommitterId(String committerId) {
        this.committerId = committerId;
    }

    public int getCommitTime() {
        return commitTime;
    }

    public void setCommitTime(int commitTime) {
        this.commitTime = commitTime;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
