/*
 * TeleStax, Open Source Cloud Communications  Copyright 2012.
 * and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.mobicents.protocols.ss7.tools.simulator;
 
import java.lang.management.ManagementFactory;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;

import org.mobicents.protocols.ss7.tools.simulator.level1.DialogicManMBean;
import org.mobicents.protocols.ss7.tools.simulator.level1.DialogicManStandardMBean;
import org.mobicents.protocols.ss7.tools.simulator.level1.M3uaManMBean;
import org.mobicents.protocols.ss7.tools.simulator.level1.M3uaManStandardMBean;
import org.mobicents.protocols.ss7.tools.simulator.level2.SccpManMBean;
import org.mobicents.protocols.ss7.tools.simulator.level2.SccpManStandardMBean;
import org.mobicents.protocols.ss7.tools.simulator.level3.MapManMBean;
import org.mobicents.protocols.ss7.tools.simulator.level3.MapManStandardMBean;
import org.mobicents.protocols.ss7.tools.simulator.management.TesterHost;
import org.mobicents.protocols.ss7.tools.simulator.management.TesterHostMBean;
import org.mobicents.protocols.ss7.tools.simulator.management.TesterHostStandardMBean;
import org.mobicents.protocols.ss7.tools.simulator.tests.ussd.TestUssdClientManMBean;
import org.mobicents.protocols.ss7.tools.simulator.tests.ussd.TestUssdClientStandardManMBean;
import org.mobicents.protocols.ss7.tools.simulator.tests.ussd.TestUssdServerManMBean;
import org.mobicents.protocols.ss7.tools.simulator.tests.ussd.TestUssdServerStandardManMBean;

import com.sun.jdmk.comm.HtmlAdaptorServer;

/**
 * 
 * @author sergey vetyutnev
 * 
 */
public class Main {

	public static TesterHost mainGui(String appName) throws Exception {
		TesterHost host = new TesterHost(appName);

		return host;
	}

	public static void main(String[] args) throws Exception {

		// ........................
		// SMSC: Hypersonic database

		// HTTP case:
		// MX4J - free http JMX adapter

		// SUN: jmxtools.jar
		// <dependency>
		// 		<groupId>com.sun.jdmk</groupId>
		// 		<artifactId>jmxtools</artifactId>
		// 		<version>1.2.1</version>
		// 		<scope>provided</scope>
		// </dependency>		

		// RMI case:
		// rmiregistry 9999
		// service:jmx:rmi:///jndi/rmi://localhost:9999/server

		
		// JCONSOLE case:
		// java -Dcom.sun.management.jmxremote Main
		// JMX repository:
		// rmiregistry 9999
		// VISTA:
		// Out-of-the-box it is not possible to connect to a local Java process from the JConsole using Windows Vista. 
		// The local process box just remains empty... The cause is Vista's security model. 
		// In default case every Java process creates a file in the folder: C:\Users\[LOGIN_NAME]\AppData\Local\Temp\hsperfdata_[LOGIN_NAME], 
		// in my case e.g.: C:\Users\adam\AppData\Local\Temp\hsperfdata_adam. The name of the file is the PID. 
		// For some strange reasons this directory is write-protected - it is not possible to create neither files nor folders 
		// in the hsperfdata_[LOGIN_NAME] directory. It is even not possible to change the write access, 
		// even not as an administrator (you can understand now, how secure Vista really is :-)). 
		// However if you delete this directory, and create a new one with the same name - then it works perfectly....

		// Custom Notification: http://marxsoftware.blogspot.com/2008/02/publishing-user-objects-in-jmx.html
		// ........................


		
		// parsing arguments, possible values:
		// name=a1 http=8000 rmi=9999
		int httpPort = -1;
		int rmiPort = -1;
		String appName = "main";
		if (args != null && args.length > 0) {
			for (String s : args) {
				if (s.length() > 5 && s.substring(0, 5).toLowerCase().equals("name=")) {
					appName = s.substring(5, s.length());
				}
				if (s.length() > 4 && s.substring(0, 4).toLowerCase().equals("rmi=")) {
					try {
						int porta = Integer.parseInt(s.substring(4, s.length()));
						if (porta > 0 && porta < 65000)
							rmiPort = porta;
						else
							System.out.println("Bad value of field \"rmi\"");
					} catch (Exception e) {
						System.out.println("Exception when parsing parameter \"rmi\"");
					}
				}
				if (s.length() > 5 && s.substring(0, 5).toLowerCase().equals("http=")) {
					try {
						int porta = Integer.parseInt(s.substring(5, s.length()));
						if (porta > 0 && porta < 65000)
							httpPort = porta;
						else
							System.out.println("Bad value of field \"http\"");
					} catch (Exception e) {
						System.out.println("Exception when parsing parameter \"http\"");
					}
				}
			}
		}

		System.out.println("Application has been loaded...");

		MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
		System.out.println("PlatformMBeanServer has been loaded...");

		// names initializing
		ObjectName adapterName = new ObjectName("SS7_Simulator_" + appName + ":name=htmladapter,port=" + httpPort);
		ObjectName nameTesterHost = new ObjectName("SS7_Simulator_" + appName + ":type=TesterHost");
		ObjectName nameM3uaMan = new ObjectName("SS7_Simulator_" + appName + ":type=M3uaMan");
		ObjectName nameDialogicMan = new ObjectName("SS7_Simulator_" + appName + ":type=DialogicMan");
		ObjectName nameSccpMan = new ObjectName("SS7_Simulator_" + appName + ":type=SccpMan");
		ObjectName nameMapMan = new ObjectName("SS7_Simulator_" + appName + ":type=MapMan");
		ObjectName nameUssdClientManMan = new ObjectName("SS7_Simulator_" + appName + ":type=TestUssdClientMan");
		ObjectName nameUssdServerManMan = new ObjectName("SS7_Simulator_" + appName + ":type=TestUssdServerMan");

		// HtmlAdaptorServer
		HtmlAdaptorServer adapter = null;
		if (httpPort > 0) {
			adapter = new HtmlAdaptorServer();
			System.out.println("HtmlAdaptorServer has been created...");
		} 

		// Tester host initializing
		TesterHost host = new TesterHost(appName);

		JMXConnectorServer cs = null;
		try {
			// registering managed beans
			Object mbean = new TesterHostStandardMBean(host, TesterHostMBean.class, host);
			mbs.registerMBean(mbean, nameTesterHost);

			M3uaManStandardMBean m3uaMBean = new M3uaManStandardMBean(host.getM3uaMan(), M3uaManMBean.class);
			mbs.registerMBean(m3uaMBean, nameM3uaMan);

			DialogicManStandardMBean dialogicMBean = new DialogicManStandardMBean(host.getDialogicMan(), DialogicManMBean.class);
			mbs.registerMBean(dialogicMBean, nameDialogicMan);

			SccpManStandardMBean sccpMBean = new SccpManStandardMBean(host.getSccpMan(), SccpManMBean.class);
			mbs.registerMBean(sccpMBean, nameSccpMan);

			MapManStandardMBean mapMBean = new MapManStandardMBean(host.getMapMan(), MapManMBean.class);
			mbs.registerMBean(mapMBean, nameMapMan);

			TestUssdClientStandardManMBean ussdClientManMBean = new TestUssdClientStandardManMBean(host.getTestUssdClientMan(), TestUssdClientManMBean.class);
			mbs.registerMBean(ussdClientManMBean, nameUssdClientManMan);

			TestUssdServerStandardManMBean ussdServerManMBean = new TestUssdServerStandardManMBean(host.getTestUssdServerMan(), TestUssdServerManMBean.class);
			mbs.registerMBean(ussdServerManMBean, nameUssdServerManMan);

			System.out.println("All beans have been loaded...");

			// starting rmi connector
			if (rmiPort > 0) {
				System.out.println("RMI connector initializing...");
				JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://localhost:" + rmiPort + "/server");
				cs = JMXConnectorServerFactory.newJMXConnectorServer(url, null, mbs);
				cs.start();
				System.out.println("RMI connector has been started...");
			}

			// starting html connector
			if (httpPort > 0) {
				System.out.println("Html connector initializing...");
				adapter.setPort(httpPort);
				mbs.registerMBean(adapter, adapterName);
				adapter.start();
				System.out.println("Html connector has been started...");
			}

		} catch (Exception ee) {
			System.out.println("Exception when initializing the managed beans or started connectors:");
			ee.printStackTrace();
		}

		System.out.println("Waiting for commands...");

		while (true) {
			Thread.sleep(500);
			if (host.isNeedQuit())
				break;

			host.checkStore();
			host.execute();
		}
		System.out.println("Terminating...");

		if (httpPort > 0) {
			adapter.stop();
			mbs.unregisterMBean(adapterName);
		}
		if (rmiPort > 0) {
			cs.stop();
		}
		
		mbs.unregisterMBean(nameTesterHost);
		mbs.unregisterMBean(nameM3uaMan);
		mbs.unregisterMBean(nameDialogicMan);
		mbs.unregisterMBean(nameSccpMan);
		mbs.unregisterMBean(nameMapMan);
		mbs.unregisterMBean(nameUssdClientManMan);
		mbs.unregisterMBean(nameUssdServerManMan);
	}
}

