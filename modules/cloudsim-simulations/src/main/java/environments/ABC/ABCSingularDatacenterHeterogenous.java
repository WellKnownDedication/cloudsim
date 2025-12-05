/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation
 *               of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009, The University of Melbourne, Australia
 */


package environments.ABC;

import technicals.simulationParameters;

import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletSchedulerSpaceShared;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.UtilizationModelStochastic;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

import brokers.CustomMLBased.ABCDatacenterBroker;
import brokers.CustomMLBased.PSODatacenterBroker;

/**
 * An example showing how to create
 * scalable simulations.
 */
public class ABCSingularDatacenterHeterogenous {
	public static DatacenterBroker broker;

	/** The cloudlet list. */
	private static List<Cloudlet> cloudletList;

	/** The vmlist. */
	private static List<Vm> vmlist;

	private static List<Vm> createVM(int userId, final int vms) {
		//Creates a container to store VMs. This list is passed to the broker later
		List<Vm> list = new ArrayList<>();

		//VM Parameters
		long size = 10000; //image size (MB)
		int ram = 512; //vm memory (MB)
		int mips = 1000;
		long bw = 1000;
		int pesNumber = 2; //number of cpus
		String vmm = "Xen"; //VMM name

		//create VMs
		for(int i=0;i<vms;i++){
			list.add(new Vm(i, userId, mips, pesNumber, ram, bw, size, vmm, new CloudletSchedulerSpaceShared()));
		}

		return list;
	}

	private static List<Cloudlet> createCloudlet(int userId, int cloudlets){
		// Creates a container to store Cloudlets
		List<Cloudlet> list = new ArrayList<>();
		Random rand = new Random();

		//cloudlet parameters

		for(int i=0;i<cloudlets;i++){
			long length = 1000 + rand.nextInt(19000);;
			long fileSize = 300 + rand.nextInt(700); 
			long outputSize = 300 + rand.nextInt(700);
			int pesNumber = 1 + rand.nextInt(2);
			UtilizationModel utilizationModel = new UtilizationModelStochastic();
			list.add(new Cloudlet(i, length, pesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel));
			list.getLast().setUserId(userId);
		}

		return list;
	}

	public static void main(String[] args) {
		Log.println("Starting ABCSingularDatacenter...");

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
			int num_vms = 4; // num of VMs and hosts
			Datacenter datacenter0 = sp.createDatacenter("Datacenter_0", num_vms, sp.bw, 1);

			//Third step: Create Broker
			broker = new ABCDatacenterBroker("Broker");;
			int brokerId = broker.getId();

			vmlist = createVM(brokerId,num_vms);
			cloudletList = createCloudlet(brokerId,sp.cloudletNumber);

			broker.submitGuestList(vmlist);
			broker.submitCloudletList(cloudletList);

			// Fifth step: Starts the simulation
			CloudSim.startSimulation();

			// Final step: Print results when simulation is over
			List<Cloudlet> newList = broker.getCloudletReceivedList();

			CloudSim.stopSimulation();

			//printCloudletList(newList);
			String path = "modules/cloudsim-simulations/src/main/java/results/";
			sp.writeCloudletListToCSV(newList, path + "ABCSingularDatacenterHeterogenous.csv");

			Log.println("ABC finished!");
		}
		catch (Exception e)
		{
			e.printStackTrace();
			Log.println("The simulation has been terminated due to an unexpected error");
		}
	}

}
