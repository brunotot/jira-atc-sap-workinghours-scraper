package com.amplexor.workinghours.websites;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;
import static com.codeborne.selenide.WebDriverRunner.source;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.amplexor.workinghours.Main;
import com.amplexor.workinghours.interfaces.Website;
import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;

/**
 * Class that represents SAP website.
 * 
 * @author TotB
 *
 */
public class SapWebsite implements Website {
	
	/** Login to SAP url. */
	private static final String LOGIN_URL = "https://sap.amplexor.com/sap/bc/ui5_ui5/ui2/ushell/shells/abap/FioriLaunchpad.html#Shell-home";

	/** Website name. */
	public static final String NAME = "SAP";
	
	/** Client's username. */
	private String username;
	
	/** Client's password. */
	private String password;
	
	/** Client's verification code. */
	private String code;
	
	@Override
	public String getName() {
		return SapWebsite.NAME;
	}
	
	/**
	 * Getter method for code.
	 * 
	 * @return Verification code
	 */
	public String getCode() {
		return code;
	}

	/**
	 * Setter method for verification code.
	 * 
	 * @param code New Code
	 */
	public void setCode(String code) {
		this.code = code;
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
	public SapWebsite() {
		this.code = "";
		this.username = "";
		this.password = "";
		open();
	}
	
	@Override
	public boolean login() {
		open(LOGIN_URL);
		$(By.xpath("//*[@id=\"i0116\"]")).setValue(this.username).pressEnter();
		$(By.xpath("//*[@id=\"i0118\"]")).waitUntil(visible, 30000).setValue(this.password).pressEnter();
		SelenideElement elem = $(By.xpath("//*[@id=\"passwordError\"] | //*[@id=\"idSubmit_SAOTCC_Continue\"]")).waitUntil(visible, 30000);
		return elem.getAttribute("id").equals("idSubmit_SAOTCC_Continue");
	}
	
	/**
	 * Verifies if the given verification code is valid and proceeds with the login.
	 * 
	 * @return True if code is valid
	 */
	private boolean verifyCode() {
		$(By.id("idTxtBx_SAOTCC_OTC")).setValue(this.code);
		$(By.id("idSubmit_SAOTCC_Continue")).click();
		try { Thread.sleep(3000); } catch (Exception e) {}
		
		SelenideElement elem = $(By.xpath("//*[@id=\"idSubmit_SAOTCC_Continue\"] | //*[@id=\"idBtn_Back\"]")).waitUntil(visible, 30000);
		if (elem.getAttribute("id").equals("idBtn_Back")) {
			$(By.id("idBtn_Back")).click();
			return true;
		}
		return false;
	}

	/**
	 * Waits for user input on verification code that gets sent to his phone.
	 * 
	 * @return True if code verification is successful.
	 */
	public boolean handleVerification() {
		if (!source().contains("Enter code")) {
			return false;
		}
		while (true) {
			System.out.print("Please enter Microsoft verification code (check phone): ");
			String code = Main.scanner.next();
			this.setCode(code);
			
			if (!this.verifyCode()) {
				System.out.println("Wrong code! Try again.");
			} else {
				break;
			}
		}
		System.out.println("You have successfully verified your SAP identity.");
		return true;
	}

	/**
	 * Scrapes starting and ending date from the website.
	 * 
	 * @return Starting and ending date as Date array
	 */
	private Date[] getCurrentDates() {
		DateFormat format = new SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH);
		SelenideElement daysRow = $(By.className("sapMeCalendarMonthDays"));
		List<WebElement> daysCols = daysRow.findElements(By.className("sapMeCalendarMonthDay"));
		
		SelenideElement divMonthYear = $(By.className("sapMeCalendarMonthName"));
		String divMonthYearString = divMonthYear.getText();
		String[] params = divMonthYearString.split(" \\– ");
		
		String lastParam = params[0];
		if (params.length == 2) {
			lastParam = params[1];
		}
		String[] lastParams = lastParam.split(" ");
		String lastMonth = lastParams[0];
		String lastYear = lastParams[1];
		
		String firstParam = params[0];
		String[] firstParams = firstParam.split(" ");
		String firstMonth = firstParams[0]; 
		String firstYear = lastParams[1];
		if (firstParams.length == 2) {
			firstYear = firstParams[1];
		}
		
		String firstDay = daysCols.get(0).getText();
		String formattedFirstDate = firstDay + " " + firstMonth + " " + firstYear;
		String lastDay = daysCols.get(daysCols.size() - 1).getText();
		String formattedLastDate = lastDay + " " + lastMonth + " " + lastYear;
		
		Date firstDate = null;
		Date lastDate = null;
		try {
			firstDate = format.parse(formattedFirstDate);
			lastDate = format.parse(formattedLastDate);
		} catch (Exception e) {
			System.out.println("Wrong date format.");
		}
		return new Date[] {firstDate, lastDate};
	}
	
	/**
	 * Handles time logic.
	 * 
	 * @param startingDate Starting date
	 * @param endingDate Ending date
	 * @param takingScreenshots Is in process of taking screenshots
	 * @return True if it is the end of the loop
	 * @throws Exception If any
	 */
	private boolean handleTime(Date startingDate, Date endingDate, boolean takingScreenshots) throws Exception {
		Date[] dates = this.getCurrentDates();
		Date firstDate = dates[0];
		Date lastDate = dates[1];
		if (takingScreenshots) {
			if (lastDate.getTime() >= endingDate.getTime()) {
				return true;
			}
		} else {
			if (startingDate.getTime() >= firstDate.getTime()) {
				return true;	
			}
		}
		return false;
	}
	
	/**
	 * Saves screenshots to a ZIP file.
	 * 
	 * @param startingDate Starting date
	 * @param endingDate Ending date
	 * @param user Username
	 * @return Output zip file location
	 * @throws Exception If any
	 */
	public String saveScreenshotsToZip(Date startingDate, Date endingDate, String user) throws Exception {
		$(By.id("__tile3")).waitUntil(visible, 30000).click();
		
		try { Thread.sleep(2000); } catch (Exception e) {}
		$(By.id("sap-ui-blocklayer-popup")).waitWhile(Condition.visible, 30000);
		
		while (true) {
			boolean isLoopOver = handleTime(startingDate, endingDate, false);
			if (isLoopOver) {
				break;
			}
			
			$(By.className("sapMeCalendarPrevious")).click();
			try { Thread.sleep(500); } catch (Exception e) {}
			$(By.id("sap-ui-blocklayer-popup")).waitWhile(Condition.visible, 30000);
		}
		
		List<File> filesToZip = new ArrayList<>();
		while (true) {			
			SelenideElement ele = $(By.className("sapUiRespGrid"));
			File screenshot = ele.screenshot();
			BufferedImage fullImg = ImageIO.read(screenshot);
			ImageIO.write(fullImg, "png", screenshot);
			Date[] dates = this.getCurrentDates();
			Date firstDate = dates[0];
			Date lastDate = dates[1];
			DateFormat dateFormat = new SimpleDateFormat("dd_MM_yyyy");
			String value = dateFormat.format(firstDate) + "_to_" + dateFormat.format(lastDate);
			File screenshotLocation = new File(System.getProperty("user.dir") + File.separator + "temp" + File.separator + value + ".png");
			FileUtils.copyFile(screenshot, screenshotLocation);
			filesToZip.add(screenshotLocation);
			
			boolean isLoopOver = handleTime(startingDate, endingDate, true);
			if (isLoopOver) {
				break;
			}
			
			$(By.className("sapMeCalendarNext")).click();
			try { Thread.sleep(500); } catch (Exception e) {}
			$(By.id("sap-ui-blocklayer-popup")).waitWhile(Condition.visible, 30000);
			
			$(By.className("sapMeCalendarNext")).click();
			try { Thread.sleep(500); } catch (Exception e) {}
			$(By.id("sap-ui-blocklayer-popup")).waitWhile(Condition.visible, 30000);
		}

		File outputDir = new File("output");
        if (!outputDir.exists()) {
        	outputDir.mkdir();
        }
		String firstDate = filesToZip.get(0).getName().split("_to_")[0];
		String lastDate = filesToZip.get(filesToZip.size() - 1).getName().split("_to_")[1];
		lastDate = lastDate.substring(0, lastDate.length() - 4);
		String output = "output" + File.separator + firstDate + "_to_" + lastDate + "_" + user + ".zip";
        FileOutputStream fos = new FileOutputStream(output);
        ZipOutputStream zipOut = new ZipOutputStream(fos);
        for (int i = filesToZip.size() - 1; i >= 0; i--) { 
        	File fileToZip = filesToZip.get(i);
            FileInputStream fis = new FileInputStream(fileToZip);
            ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
            zipOut.putNextEntry(zipEntry);
 
            byte[] bytes = new byte[1024];
            int length;
            while((length = fis.read(bytes)) >= 0) {
                zipOut.write(bytes, 0, length);
            }
            fis.close();
            fileToZip.delete();
        }
        zipOut.close();
        fos.close();
        File file = new File("temp");
        if (file.exists() && file.isDirectory()) {
        	file.delete();
        }
        return output;
	}

	@Override
	public void setCredentials(Properties properties) {
		this.username = properties.getProperty("sapUsername");
		this.password = properties.getProperty("sapPassword");
	}
	
}
