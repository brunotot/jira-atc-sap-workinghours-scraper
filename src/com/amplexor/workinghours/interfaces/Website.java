package com.amplexor.workinghours.interfaces;

import java.util.Properties;

/**
 * Website interface.
 * Implementations: see com.amplexor.workinghours.websites package.
 * 
 * @author TotB
 *
 */
public interface Website {

	/**
	 * Getter method for username.
	 * 
	 * @return Username
	 */
	String getUsername();
	
	/**
	 * Setter method for username.
	 * 
	 * @param username New username
	 */
	void setUsername(String username);
	
	/**
	 * Getter method for password.
	 * 
	 * @return Password
	 */
	String getPassword();
	
	/**
	 * Setter method for password.
	 * 
	 * @param password New password
	 */
	void setPassword(String password);
	
	/**
	 * Logs user into the website with given credentials (if login exists).
	 * 
	 * @return True if login was valid
	 */
	boolean login();

	/**
	 * Gets the name of the website.
	 * 
	 * @return Website name
	 */
	String getName();
	
	/**
	 * Sets credentials if properties object is given.
	 * 
	 * @param properties Properties object
	 */
	void setCredentials(Properties properties);
	
}
