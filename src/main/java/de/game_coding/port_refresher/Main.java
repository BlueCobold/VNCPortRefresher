package de.game_coding.port_refresher;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

public class Main {

	private static final int VNC_PORT = 5900;

	private static final String VNC_PORT_DESCRIPTION = "VNC Port";

	public static void main(String[] args) throws UnknownHostException, SocketException,
			IOException, SAXException, ParserConfigurationException {

		final Refresher refresher = new Refresher(VNC_PORT, VNC_PORT_DESCRIPTION);
		refresher.run();
	}
}
