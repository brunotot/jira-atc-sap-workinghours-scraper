package com.amplexor.workinghours.jira;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class JiraLoggedHoursMap { 
	
	private Map<String, List<JiraLoggedHours>> map;

	public Map<String, List<JiraLoggedHours>> getMap() {
		return map;
	}

	public void setMap(Map<String, List<JiraLoggedHours>> map) {
		this.map = map;
	}
	
	public JiraLoggedHoursMap(Map<String, List<JiraLoggedHours>> map) {
		super();
		this.map = map;
	}

	public JiraLoggedHoursMap() {
		this.map = new TreeMap<>();
	}
	
	public void add(JiraLoggedHours jiraLoggedHours) {
		try {
			DateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
			String dateAsString = formatter.format(jiraLoggedHours.getDate());
			List<JiraLoggedHours> list = this.map.get(dateAsString);
			if (list == null) {
				list = new ArrayList<JiraLoggedHours>();
				list.add(jiraLoggedHours);
				this.map.put(dateAsString, list);
			} else {
				list.add(jiraLoggedHours);
				this.map.put(dateAsString, list);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
