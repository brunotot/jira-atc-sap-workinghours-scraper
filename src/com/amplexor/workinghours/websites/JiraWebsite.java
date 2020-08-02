package com.amplexor.workinghours.websites;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.download;
import static com.codeborne.selenide.Selenide.open;
import static com.codeborne.selenide.WebDriverRunner.url;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.Properties;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.joda.time.DateTime;
import org.joda.time.Weeks;
import org.openqa.selenium.By;

import com.amplexor.workinghours.interfaces.Website;
import com.amplexor.workinghours.jira.JiraLoggedHours;
import com.amplexor.workinghours.jira.JiraLoggedHoursMap;

/**
 * Class that represents JIRA website.
 * 
 * @author TotB
 *
 */
public class JiraWebsite implements Website {

	/** Login to JIRA url. */
	public static final String LOGIN_URL = "https://issuetracker.amplexor.com/jira/login.jsp";
	
	/** Website name. */
	public static final String NAME = "Jira";
	
	/** Client's username. */
	private String username;
	
	/** Client's password. */
	private String password;
	
	@Override
	public String getName() {
		return JiraWebsite.NAME;
	}
	
	@Override
	public String getUsername() {
		return username;
	}

	@Override
	public void setUsername(String username) {
		this.username = username;
	}

	@Override
	public String getPassword() {
		return password;
	}

	@Override
	public void setPassword(String password) {
		this.password = password;
	}
	
	/**
	 * Public constructor for this class (opens an empty tab with Selenide's open() method).
	 */
	public JiraWebsite() {
		this.username = "";
		this.password = "";
		open();
	}
	
	@Override
	public boolean login() {
		if (!url().startsWith(LOGIN_URL)) {
			open(LOGIN_URL);
			if (!url().startsWith(LOGIN_URL)) {
				return true;
			}
		}

		$(By.id("login-form-username")).setValue(this.username);
		$(By.id("login-form-password")).setValue(this.password).pressEnter();
		
		return $(By.xpath("//*[@id=\"login-form-submit\"] | //*[@id=\"header-details-user-fullname\"]"))
				.waitUntil(visible, 30000)
				.getAttribute("id")
				.equals("header-details-user-fullname");
	}
	
	/**
	 * Returns total jira logged hours for given dates.
	 * 
	 * @param startingDate Starting date
	 * @param endingDate Ending date
	 * @return JiraLoggedHoursMap object
	 * @throws Exception If any
	 */
	public JiraLoggedHoursMap getJiraLoggedHoursMap(Date startingDate, Date endingDate) throws Exception {
		int weeks = Weeks.weeksBetween(new DateTime(startingDate), new DateTime(new Date())).getWeeks() + 1;
		String csvDownloadLink  = "https://issuetracker.amplexor.com/jira/rest/timesheet-gadget/1.0/timesheet.json?" + 
				"csvExport=true&" + 
				"reportKey=jira-timesheet-plugin:report&" + 
				"targetUser=" + this.username + "&" + 
				"weekends=true&" + 
				"showDetails=false&" + 
				"sum=day&" + 
				"numOfWeeks=" + weeks + "&" + 
				"offset=0&" + 
				"reportingDay=0";
		File downloadedFile = download(csvDownloadLink);
		DateFormat datesFormat = new SimpleDateFormat("dd/MMMM/yy", Locale.ENGLISH);
		JiraLoggedHoursMap jiraLoggedHoursMap = new JiraLoggedHoursMap();
		BufferedReader reader = new BufferedReader(new FileReader(downloadedFile));
	    Iterable<CSVRecord> records = CSVFormat.DEFAULT.withHeader("Project", "Type", "Key", "Title", "Started", "Username", "Time", "Comment").parse(reader);
	    Iterator<CSVRecord> recordsIterator = records.iterator();
	    if (recordsIterator.hasNext()) {
	    	recordsIterator.next();
	    }
		while (recordsIterator.hasNext()) {
	    	CSVRecord record = recordsIterator.next();
	    	if (!recordsIterator.hasNext()) {
	    		break;
	    	}
	    	String dateAsString = record.get("Started").split(" ")[0];
	    	Date date = datesFormat.parse(dateAsString);
	    	if (date.getTime() >= startingDate.getTime() && date.getTime() <= endingDate.getTime()) {
		    	String project = record.get("Project");
		    	String type = record.get("Type");
		    	String key = record.get("Key");
		    	Double time = Double.parseDouble(record.get("Time"));
		    	String comment = record.get("Comment");
				JiraLoggedHours jiraLoggedHours = new JiraLoggedHours(project, type, key, comment, date, time);
				jiraLoggedHoursMap.add(jiraLoggedHours);
	    	}
	    }
	    reader.close();
		return jiraLoggedHoursMap;
	}

	@Override
	public void setCredentials(Properties properties) {
		this.username = properties.getProperty("jiraUsername");
		this.password = properties.getProperty("jiraPassword");
	}
	
}
