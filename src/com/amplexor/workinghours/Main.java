package com.amplexor.workinghours;

import java.io.FileInputStream;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;
import java.util.Scanner;

import com.amplexor.workinghours.interfaces.Website;
import com.amplexor.workinghours.jira.JiraLoggedHoursMap;
import com.amplexor.workinghours.websites.AtcWebsite;
import com.amplexor.workinghours.websites.JiraWebsite;
import com.amplexor.workinghours.websites.SapWebsite;

/**
 * Starter class for the application.
 * 
 * @author TotB
 *
 */
public class Main {

	/** Scanner object. */
	public static Scanner scanner;
	
	/** Properties object. */
	public static Properties properties;
	
	/**
	 * Main method for the application.
	 * 
	 * @param args Args
	 */
	public static void main(String[] args) {
		Main.scanner = new Scanner(System.in);
		Main.properties = new Properties();
//		Configuration.headless = true;
		
		try (InputStream inputStream = new FileInputStream("resources/config/app.conf")) {
			Main.properties.load(inputStream);
			boolean useConfCredentials = false;
			String useConfCredentialsString = Main.properties.getProperty("useConfCredentials");
			if (useConfCredentialsString.equals("true")) {
				useConfCredentials = true;
			}
			boolean useDefaultDynamicDates = false;
			String useDefaultDynamicDatesString = Main.properties.getProperty("useDefaultDynamicDates");
			if (useDefaultDynamicDatesString.equals("true")) {
				useDefaultDynamicDates = true;
			}
			DateFormat format = new SimpleDateFormat("dd/MM/yyyy");
			Date startingDate = null;
			Date endingDate = null;
			if (useDefaultDynamicDates) {
				endingDate = format.parse(format.format(new Date()));
				int todaysMonth = endingDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().getMonthValue();
				int todaysYear = endingDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().getYear();
				startingDate = format.parse("01" + "/" + todaysMonth + "/" + todaysYear);
			} else {
				Date[] datesInput = Main.handleDatesInput();
				startingDate = datesInput[0];
				endingDate = datesInput[1];
			}
			
			/******************* JIRA *******************/
			JiraWebsite jiraWebsite = new JiraWebsite();
			login(jiraWebsite, useConfCredentials);
			JiraLoggedHoursMap jiraLoggedHoursMap = jiraWebsite.getJiraLoggedHoursMap(startingDate, endingDate);
			System.out.println("Acquired JIRA logged hours for " + jiraWebsite.getUsername());
			/******************* JIRA *******************/
			
			
			/******************* ATC ********************/
			AtcWebsite atcWebsite = new AtcWebsite();
			login(atcWebsite, useConfCredentials);
			System.out.println("Trying to push hours from ATC to SAP...");
			atcWebsite.pushHoursToSap(startingDate, endingDate);
			System.out.println("Pushed hours from ATC to SAP for dates " + format.format(startingDate) + " - " + format.format(endingDate));
			/******************* ATC ********************/
	
			
			/******************* SAP ********************/
			SapWebsite sapWebsite = new SapWebsite();
			login(sapWebsite, useConfCredentials);
			sapWebsite.handleVerification();
			System.out.println("Trying to create screenshots of hours logged and save them to ZIP file...");
			String outputZipLocation = sapWebsite.saveScreenshotsToZip(startingDate, endingDate, jiraWebsite.getUsername());
			System.out.println("Saved ZIP with hours to " + outputZipLocation);
			/******************* SAP ********************/
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			Main.scanner.close();
		}
	}

	/**
	 * Void method that accepts a website in which user gets logged in if he provides valid credentials.
	 * 
	 * @param website Website to login into
	 * @param useConfCredentials Indicates whether application gets credentials from config or user input
	 * @exception Exception If configuration file provides invalid credentials
	 */
	private static void login(Website website, boolean useConfCredentials) throws Exception {
		System.out.println("Trying to log in to " + website.getName() + "...");
		String websiteName = website.getName();
		boolean login = false;
		if (useConfCredentials) {
			website.setCredentials(Main.properties);
			login = website.login();
			if (!login) {
				throw new Exception("Invalid credentials for " + website.getName() + " website.");
			}
		} else {
			while (!login) {
				System.out.print(websiteName + " username: ");
				String username = Main.scanner.next();
				website.setUsername(username);
				
				System.out.print(websiteName + " password: ");
			//	String password = new String(System.console().readPassword(website.getName() + " password: "));
				String password = Main.scanner.next();
				website.setPassword(password);
				
				login = website.login();
				if (!login) {
					System.out.println("Wrong username/email or password! Try again.");
				}
			}
		}
		System.out.println("Logged in " + website.getName() + " as " + website.getUsername());
	}
	
	/**
	 * Gets starting and ending dates from user input.
	 * 
	 * @return Starting and ending dates
	 */
	public static Date[] handleDatesInput() {
		boolean success = false;
	    DateFormat format = new SimpleDateFormat("dd.MM.yyyy", Locale.ENGLISH);
	    Date startingDate = null;
		Date endingDate = null;
		do {
			try {
				System.out.print("Please input starting date (e.g. 01.05.2020): ");
				String startingDateAsString = Main.scanner.next();
				startingDate = format.parse(startingDateAsString);
				System.out.print("Please input ending date (e.g. 01.05.2020): ");
				String endingDateAsString = Main.scanner.next();
				endingDate = format.parse(endingDateAsString);
				success = true;
			} catch (Exception e) {
				System.out.println("Wrong date format! Try again.");
			}
		} while (!success);
		return new Date[] {startingDate, endingDate};
	}
	
}
