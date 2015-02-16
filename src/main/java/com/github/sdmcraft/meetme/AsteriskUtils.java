package com.github.sdmcraft.meetme;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.asteriskjava.live.MeetMeUser;
import org.asteriskjava.manager.ManagerConnection;
import org.asteriskjava.manager.ManagerConnectionFactory;
import org.asteriskjava.manager.action.OriginateAction;
import org.asteriskjava.manager.response.ManagerResponse;

// TODO: Auto-generated Javadoc
/**
 * The Class AsteriskUtils.
 */
public class AsteriskUtils {

	/**
	 * Verify meet me room.
	 * 
	 * @param roomNumber
	 *            the room number
	 * @param witness
	 *            the witness
	 * @param managerConnection
	 *            the manager connection
	 * @return true, if successful
	 * @throws Exception
	 *             the exception
	 */
	public static boolean verifyMeetMeRoom(String roomNumber, String witness,
			ManagerConnection managerConnection) throws Exception {
		boolean validRoom = false;
		OriginateAction originateAction;
		ManagerResponse originateResponse;

		originateAction = new OriginateAction();
		originateAction.setChannel("SIP/" + witness);
		originateAction.setContext("conferences");
		originateAction.setExten(roomNumber);
		originateAction.setPriority(new Integer(1));
		originateAction.setTimeout(new Long(30000));

		// send the originate action and wait for a maximum of 30 seconds for
		// Asterisk
		// to send a reply
		originateResponse = managerConnection
				.sendAction(originateAction, 30000);

		// print out whether the originate succeeded or not
		System.out.println(originateResponse.getResponse());

		return validRoom;
	}


	/**
	 * Gets the user phone number.
	 * 
	 * @param user
	 *            the user
	 * @return the user phone number
	 */
	public static final String getUserPhoneNumber(MeetMeUser user) {
//		String channel = user.getChannel().getName();
//		return getPhoneNumberFromChannel(channel);
		return user.getChannel().getCallerId().getNumber();
	}

	/**
	 * The main method.
	 * 
	 * @param args
	 *            the arguments
	 * @throws Exception
	 *             the exception
	 */
	public static void main(String[] args) throws Exception {
		ManagerConnectionFactory factory = new ManagerConnectionFactory(
				"192.168.1.103", "admin", "P@$$w0rd");

		ManagerConnection managerConnection = factory.createManagerConnection();
		managerConnection.login();
		verifyMeetMeRoom("6300", "6000", managerConnection);
		managerConnection.logoff();
	}
	
	//***********DEPRECATED-DO NOT USE*************//
	/**
	 * Gets the phone number from channel.
	 * 
	 * @param channel
	 *            the channel
	 * @return the phone number from channel
	 */
	public static final String getPhoneNumberFromChannel(String channel) {
		String phoneNumber = channel;
		if (channel.startsWith("Gtalk") && channel.contains(".com"))
			phoneNumber = channel.substring(channel.indexOf("Gtalk") + 6,
					channel.lastIndexOf(".com") + 4);
		else if (channel.startsWith("SIP")) {
			Pattern channelNamePattern = Pattern.compile("SIP/[0-9]+");
			Matcher matcher = channelNamePattern.matcher(channel);

			if (matcher.find()) {
				phoneNumber = matcher.group();
			}
		}
		return phoneNumber;
	}
	//***********DEPRECATED-DO NOT USE*************//

}
