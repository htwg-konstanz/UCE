/*
    Copyright (c) 2012 Thomas Zink, 

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.fhkn.in.uce.reversal.mediator;

import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class which implements a list of all registered users.
 * Each user is mapped as a UserData-Object.
 * This class is for the managing the registered users
 * and provides all relevant methods therefore.
 * The class is implemented as Singleton.
 * 
 * @author thomas zink, stefan lohr
 */
public class UserList {
	
	/**
	 * Initializes the instance for the Singleton
	 */
	private static final UserList instance = new UserList();
	private ConcurrentHashMap<String, UserData> userMap;
	
	/**
	 * Constructor, creates ConcurrentHashMap for UserData Objects
	 */
	private UserList() {
		userMap = new ConcurrentHashMap<String, UserData>();
	}
	
	/**
	 * Returns the instance of the Singleton
	 * 
	 * @return UserList instance of the Singleton
	 */
	public static UserList getInstance() {
		return instance;
	}
	
	/**
	 * Method to get the UserData-Object by its unique userName.
	 * 
	 * @param userName String of the unique userName
	 * @return UserData Object of the unique UserName
	 */
	public UserData getUser(String userName) {
		return userMap.get(userName);
	}
	
	/**
	 * Method updates the handed over UserData Object
	 * 
	 * @param userData UserData Object to update
	 * @return updated UserData Object
	 */
	public UserData updateUser(UserData userData) {
		return userMap.put(userData.getUserName(), userData);
	}
	
	/**
	 * Method adds the handed over UserData Object to UserList
	 * 
	 * @param userData UserData Object to add
	 * @return added UserData Object
	 */
	public UserData addUser(UserData userData) {
		return userMap.put(userData.getUserName(), userData);
	}
	
	/**
	 * Method removes the handed over UserData Object from UserList
	 * 
	 * @param userName unique userName of the UserData Object
	 * @return removed UserData Object
	 */
	public UserData removeUser(String userName) {
		return userMap.remove(userName);
	}
	
	/**
	 * Removes all of the mappings from this UserList
	 */
	public void clearUserList() {
		userMap.clear();
	}
	
	/**
	 * Method returns a set of the unique userNames which are registered on the Mediator
	 * 
	 * @return Set of Strings with the unique userNames of the registered users on the Mediator
	 */
	public Set<String> getUserNames() {
		return userMap.keySet();
	}
	
	/**
	 * Method refreshes the TimeStamp of the handed over unique userName
	 * 
	 * @param userName unique UserName
	 */
	public void refreshUserTimeStamp(String userName) {
		UserData userData = userMap.get(userName);
		userData.refreshTimeStamp();
		userMap.replace(userName, userData, userData);
	}
	
	/**
	 * Method removes users with timeStamps older than the handed over parameter
	 * 
	 * @param timeStamp milliseconds since 1970-01-01 
	 */
	public synchronized void removeUsersByTimestamp(long timeStamp) {
		Iterator<UserData> userMapIterator = userMap.values().iterator();
		while (userMapIterator.hasNext()) {
			UserData userData = userMapIterator.next();
			if (userData.getTimeStamp() <= timeStamp) {
				System.out.println("remove idle user " + userData.getUserName() + " from UserList");
				userMapIterator.remove();
			}
		}
	}
}
