package com.amplexor.workinghours.websites;

import static com.codeborne.selenide.Condition.appears;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;
import static com.codeborne.selenide.WebDriverRunner.url;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;

import org.openqa.selenium.By;

import com.amplexor.workinghours.interfaces.Website;
import com.codeborne.selenide.SelenideElement;

/**
 * Class that represents ATC website.
 * 
 * @author TotB
 *
 */
public class AtcWebsite implements Website {

	/** Login to ATC url. */
	private static final String LOGIN_URL = "https://atc.amplexor.com/atc/login.html";
	
	/** Website name. */
	public static final String NAME = "ATC";
	
	/** Client's username. */
	private String username;
	
	/** Client's password. */
	private String password;

	@Override
	public String getName() {
		return AtcWebsite.NAME;
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
	public AtcWebsite() {
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
		
		$(By.xpath("/html/body/div/form/fieldset/div[1]/input")).setValue(this.username);
		$(By.xpath("/html/body/div/form/fieldset/div[2]/input")).setValue(this.password).pressEnter();		
		
		SelenideElement elem = $(By.xpath("/html/body/div/form/fieldset/button | //*[@id=\"content\"]/div/div/div[2]/div/div[2]/button")).waitUntil(visible, 30000);
		$(By.id("loading")).waitWhile(visible, 30000);

		return !elem.getAttribute("type").equals("submit");
		
	}
	
	/**
	 * Scrapes label for current dates and returns it.
	 * 
	 * @return Starting and ending dates in Date array
	 */
	public Date[] getCurrentLabelDates() {
		SelenideElement labelTimeDiv = $(By.xpath("//*[@id=\"content\"]/div/div/div[1]/form/div[3]/div"));
		String labelTime = labelTimeDiv.getText();
		String[] labelTimeParams = labelTime.split(" - ");
		DateFormat labelTimeDateFormat = new SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH);
		Date labelTimeStartDate = null;
		Date labelTimeEndDate = null;
		if (labelTimeParams.length == 2) {
			try {
				labelTimeStartDate = labelTimeDateFormat.parse(labelTimeParams[0]);
				labelTimeEndDate = labelTimeDateFormat.parse(labelTimeParams[1]);
			} catch (Exception e) {
				System.out.println("Error occured while parsing dates.\n" + e);
				return null;
			}
			return new Date[] {labelTimeStartDate, labelTimeEndDate};
		} else {
			return null;
		}
	}

	/**
	 * Pushes hours from ATC to SAP with dates between startingDate and endingDate.
	 * 
	 * @param startingDate Starting date
	 * @param endingDate Ending date
	 */
	public void pushHoursToSap(Date startingDate, Date endingDate) {
		Date[] labelDates = null;
		Date labelStartingDate = null;
		Date labelEndingDate = null;
		
		while (true) {
			labelDates = this.getCurrentLabelDates();
			labelStartingDate = labelDates[0];
			labelEndingDate = labelDates[1];
			
			if (startingDate.getTime() >= labelStartingDate.getTime() && startingDate.getTime() <= labelEndingDate.getTime()) {
				break;
			}
			
			$(By.xpath("//*[@id=\"content\"]/div/div/div[1]/form/div[2]/button")).click();
			try { Thread.sleep(500); } catch (Exception e) {}
		}
		
		while (true) {
			$(By.xpath("//*[@id=\"content\"]/div/div/div[1]/form/div[6]/button")).click();
			$(By.id("loading")).waitUntil(appears, 30000);
			$(By.id("loading")).waitWhile(visible, 30000);

			$(By.xpath("//*[@id=\"content\"]/div/div/div[2]/div/div[2]/button")).click();
			$(By.xpath("/html/body/div[4]/div/table/tbody/tr[2]/td/table/tbody/tr/td[1]/button")).click();
			$(By.id("loading")).waitUntil(appears, 30000);
			$(By.id("loading")).waitWhile(visible, 30000);
			
			$(By.xpath("//*[@id=\"content\"]/div/div/div[1]/form/div[4]/button")).click();
			try { Thread.sleep(500); } catch (Exception e) {}
			labelDates = this.getCurrentLabelDates();
			labelStartingDate = labelDates[0];
			labelEndingDate = labelDates[1];
			
			if (endingDate.getTime() < labelStartingDate.getTime()) {
				break;
			}
		}
	}

	@Override
	public void setCredentials(Properties properties) {
		this.username = properties.getProperty("atcUsername");
		this.password = properties.getProperty("atcPassword");
	}

}
