/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation
 *               of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009, The University of Melbourne, Australia
 */


package environments.PSO;

import technicals.simulationParameters;

import java.util.Calendar;
import java.util.List;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;

import brokers.CustomMLBased.PSODatacenterBroker;

/**
 * An example showing how to create
 * scalable simulations.
 */
public class PSOSingularDatacenterHeterogenous {
	public static DatacenterBroker broker;

	/** The cloudlet list. */
	private static List<Cloudlet> cloudletList;

	/** The vmlist. */
	private static List<Vm> vmlist;

	public static void main(String[] args) {
		Log.println("Starting PSOSingularDatacenter...");

		try {
			simulationParameters sp = new simulationParameters();
			// First step: Initialize the CloudSim package. It should be called
			// before creating any entities.
			int num_user = 1;   // number of grid users
			Calendar calendar = Calendar.getInstance();
			boolean trace_flag = false;  // mean trace events

			// Initialize the CloudSim library
			CloudSim.init(num_user, calendar, trace_flag);

			// Second step: Create Datacenters
			//Datacenters are the resource providers in CloudSim. We need at least one of them to run a CloudSim simulation
			 // num of VMs and hosts
			Datacenter datacenter0 = sp.createDatacenter("Datacenter_0", sp.num_vms_singleDC, sp.bw, 1);

			//Third step: Create Broker
			broker = new PSODatacenterBroker("Broker");;
			int brokerId = broker.getId();

			vmlist = sp.createVM(brokerId,sp.num_vms_singleDC);
			cloudletList = sp.createCloudletHeterogenous(brokerId,sp.cloudletNumber);

			broker.submitGuestList(vmlist);
			broker.submitCloudletList(cloudletList);

			// Fifth step: Starts the simulation
			CloudSim.startSimulation();

			// Final step: Print results when simulation is over
			List<Cloudlet> newList = broker.getCloudletReceivedList();

			CloudSim.stopSimulation();

			//printCloudletList(newList);
			String path = "modules/cloudsim-simulations/src/main/java/results/";
			sp.writeCloudletListToCSV(newList, path + "PSOSingularDatacenterHeterogenous.csv");

			Log.println("PSO finished!");
		}
		catch (Exception e)
		{
			e.printStackTrace();
			Log.println("The simulation has been terminated due to an unexpected error");
		}
	}

}
