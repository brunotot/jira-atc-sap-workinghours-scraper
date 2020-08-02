package com.amplexor.workinghours.jira;

import java.util.Date;

public class JiraLoggedHours {

	private String projectName;

	private String issueType;
	
	private String issueName;
	
	private String issueMessage;
		
	private Date date;
	
	private Double duration;

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public String getIssueType() {
		return issueType;
	}

	public void setIssueType(String issueType) {
		this.issueType = issueType;
	}

	public String getIssueName() {
		return issueName;
	}

	public void setIssueName(String issueName) {
		this.issueName = issueName;
	}

	public String getIssueMessage() {
		return issueMessage;
	}

	public void setIssueMessage(String issueMessage) {
		this.issueMessage = issueMessage;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public Double getDuration() {
		return duration;
	}

	public void setDuration(Double duration) {
		this.duration = duration;
	}

	public JiraLoggedHours(String projectName, String issueType, String issueName, String issueMessage, Date date, Double duration) {
		super();
		this.projectName = projectName;
		this.issueType = issueType;
		this.issueName = issueName;
		this.issueMessage = issueMessage;
		this.date = date;
		this.duration = duration;
	}
	
	public JiraLoggedHours() {
		
	}
	
}
