package de.game_coding.port_refresher;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.bitlet.weupnp.GatewayDevice;
import org.bitlet.weupnp.GatewayDiscover;
import org.bitlet.weupnp.PortMappingEntry;
import org.xml.sax.SAXException;

public class Refresher {

	private final GatewayDevice gateway;
	private final int port;
	private final String description;

	public Refresher(int port, String description) throws UnknownHostException, SocketException,
			IOException, SAXException, ParserConfigurationException {

		this.port = port;
		this.description = description;
		gateway = getGateway();
	}

	private GatewayDevice getGateway() throws UnknownHostException, SocketException, IOException,
			SAXException, ParserConfigurationException {
		System.out.println("Discovering gateways for "
				+ InetAddress.getLocalHost().getHostAddress());
		final GatewayDiscover gatewayDiscover = new GatewayDiscover();
		final Map<InetAddress, GatewayDevice> gateways = gatewayDiscover.discover();
		if (gateways.isEmpty()) {
			throw new IOException("No gateways found");
		}
		for (final GatewayDevice g : gateways.values()) {
			System.out.println("" + g.getFriendlyName() + ":" + g.getDeviceType() + ":"
					+ g.getManufacturer() + ":" + g.getPortMappingNumberOfEntries());
		}

		final GatewayDevice gateway = gateways.values().iterator().next();// gatewayDiscover.getValidGateway();
		if (gateway == null) {
			throw new IOException("No active gateway found");
		}

		System.out.println(gateway.getDeviceType());
		return gateway;
	}

	private boolean deletePortMapping(final GatewayDevice gateway) throws IOException, SAXException {
		final PortMappingEntry portMapping = new PortMappingEntry();
		final String ip = InetAddress.getLocalHost().getHostAddress();
		if (!gateway.getSpecificPortMappingEntry(port, "TCP", portMapping)
				|| !portMapping.getInternalClient().equals(ip)) {
			System.out.println("Deleting old port mapping");
			if (!gateway
					.deletePortMapping(portMapping.getExternalPort(), portMapping.getProtocol())) {
				System.out.println("Failed to delete old port mapping");
				return false;
			}
			sleep(10 * 1000);
			return true;
		}
		return false;
	}

	private void addPortMapping(final GatewayDevice gateway) throws IOException, SAXException {
		System.out.println("Adding new port mapping");
		if (!gateway.addPortMapping(port, port, gateway.getLocalAddress().getHostAddress(), "TCP",
				description)) {
			System.out.println("Failed to add new port mapping");
		}
	}

	private static void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (final InterruptedException e) {
		}
	}

	public void run() {
		String localhost = "";
		do {
			try {
				if (!localhost.equals(InetAddress.getLocalHost().getHostAddress())) {
					localhost = InetAddress.getLocalHost().getHostAddress();

					System.out.println("Refreshing VNC port");
					if (deletePortMapping(gateway)) {
						addPortMapping(gateway);
					}
				}
				sleep(60 * 1000);
			} catch (final UnknownHostException e) {
				System.out.println(e.getLocalizedMessage());
			} catch (final IOException e) {
				System.out.println(e.getLocalizedMessage());
			} catch (final SAXException e) {
				System.out.println(e.getLocalizedMessage());
			}
		} while (true);
	}
}
