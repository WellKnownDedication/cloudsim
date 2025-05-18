/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation
 *               of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009, The University of Melbourne, Australia
 */


package heterogenous.geneticAlgorythm;

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

import brokers.GeneticAlgorithm.GeneticAlgorithmDatacenterBroker;

/**
 * An example showing how to create
 * scalable simulations.
 */
public class GAMultiDatacenterHeterogenous {
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


	////////////////////////// STATIC METHODS ///////////////////////

	/**
	 * Creates main() to run this example
	 */
	public static void main(String[] args) {
		Log.println("Starting baselineSingularDatacenter...");

		try {
			// First step: Initialize the CloudSim package. It should be called
			// before creating any entities.
			int num_user = 1;   // number of grid users
			Calendar calendar = Calendar.getInstance();
			boolean trace_flag = false;  // mean trace events

			// Initialize the CloudSim library
			CloudSim.init(num_user, calendar, trace_flag);

			// Second step: Create Datacenters
			//Datacenters are the resource providers in CloudSim. We need at least one of them to run a CloudSim simulation
			Datacenter datacenter0 = createDatacenter("Datacenter_0", 2, 1000, 0.8);
			Datacenter datacenter1 = createDatacenter("Datacenter_1", 4, 1200, 1.1);
			Datacenter datacenter2 = createDatacenter("Datacenter_2", 2, 900, 1);

			//Third step: Create Broker
			broker = new GeneticAlgorithmDatacenterBroker("Broker");;
			int brokerId = broker.getId();

			//Fourth step: Create VMs and Cloudlets and send them to broker
			int datacenterAmount = 3;
			int totalCloudlets = 1000;

			vmlist = createVM(brokerId,4*3);
			cloudletList = createCloudlet(brokerId,totalCloudlets); 

			broker.submitGuestList(vmlist);
			broker.submitCloudletList(cloudletList);

			// Fifth step: Starts the simulation
			CloudSim.startSimulation();

			// Final step: Print results when simulation is over
			List<Cloudlet> newList = broker.getCloudletReceivedList();

			CloudSim.stopSimulation();

			//printCloudletList(newList);
			String path = "modules/cloudsim-simulations/src/main/java/results/";
			writeCloudletListToCSV(newList, path + "GAMultiDatacenterHeterogenous.csv");

			Log.println("CloudSimExample6 finished!");
		}
		catch (Exception e)
		{
			e.printStackTrace();
			Log.println("The simulation has been terminated due to an unexpected error");
		}
	}

	private static Datacenter createDatacenter(String name, int hostNumber, int bw, double cost_multiplier){

		// Here are the steps needed to create a PowerDatacenter:
		// 1. We need to create a list to store one or more
		//    Machines
		List<Host> hostList = new ArrayList<>();

		//int hostNumber = 2;
		int PeNumber = hostNumber*4;

		int mips = 1000;

		//4. Create Hosts with its id and list of PEs and add them to the list of machines
		int hostId=0;
		int ram = 4000; //host memory (MB)
		long storage = 1000000; //host storage
		//int bw = 10000;

		for (int i = 0; hostNumber > i; i++){
			List<Pe> peList = new ArrayList<>();
			for(int j = 0; PeNumber > j; j++){
				peList.add(new Pe(j, new PeProvisionerSimple(mips)));
			}

			hostList.add(
    			new Host(
    				hostId,
    				new RamProvisionerSimple(ram),
    				new BwProvisionerSimple(bw),
    				storage,
    				peList,
    				new VmSchedulerTimeShared(peList)
    			)
    		); 

			hostId++;
		}

		// 5. Create a DatacenterCharacteristics object that stores the
		//    properties of a data center: architecture, OS, list of
		//    Machines, allocation policy: time- or space-shared, time zone
		//    and its price (G$/Pe time unit).
		String arch = "x86";      // system architecture
		String os = "Linux";          // operating system
		String vmm = "Xen";
		double time_zone = 10.0;         // time zone this resource located
		double cost = 3.0 * cost_multiplier;              // the cost of using processing in this resource
		double costPerMem = 0.05 * cost_multiplier;		// the cost of using memory in this resource
		double costPerStorage = 0.1 * cost_multiplier;	// the cost of using storage in this resource
		double costPerBw = 0.1 * cost_multiplier;			// the cost of using bw in this resource
		LinkedList<Storage> storageList = new LinkedList<>();	//we are not adding SAN devices by now

		DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
                arch, os, vmm, hostList, time_zone, cost, costPerMem, costPerStorage, costPerBw);


		// 6. Finally, we need to create a PowerDatacenter object.
		Datacenter datacenter = null;
		try {
			datacenter = new Datacenter(name, characteristics, new VmAllocationPolicySimple(hostList), storageList, 0);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return datacenter;
	}

	/**
	 * Prints the Cloudlet objects
	 * @param list  list of Cloudlets
	 */
	private static void printCloudletList(List<Cloudlet> list) {
		Cloudlet cloudlet;

		String indent = "    ";
		Log.println();
		Log.println("========== OUTPUT ==========");
		Log.println("Cloudlet ID" + indent + "STATUS" + indent +
				"Data center ID" + indent + "VM ID" + indent + indent + "Time" + indent + "Start Time" + indent + "Finish Time");

		DecimalFormat dft = new DecimalFormat("###.##");
        for (Cloudlet value : list) {
            cloudlet = value;
            Log.print(indent + cloudlet.getCloudletId() + indent + indent);

            if (cloudlet.getStatus() == Cloudlet.CloudletStatus.SUCCESS) {
                Log.print("SUCCESS");

                Log.println(indent + indent + cloudlet.getResourceId() + indent + indent + indent + cloudlet.getGuestId() +
                        indent + indent + indent + dft.format(cloudlet.getActualCPUTime()) +
                        indent + indent + dft.format(cloudlet.getExecStartTime()) + indent + indent + indent + dft.format(cloudlet.getExecFinishTime()));
            }
        }

	}

	private static void writeCloudletListToCSV(List<Cloudlet> list, String filePath) {
		StringBuilder sb = new StringBuilder();
		DecimalFormat dft = new DecimalFormat("###.##");
	
		// Header
		sb.append("Cloudlet ID,User ID,Status,Data Center ID,Submission Time,Start Time,Finish Time,")
		  .append("Cloudlet Length,Processing Cost,File Size,")
		  .append("CPU Utilization,RAM Utilization,BW Utilization,Waiting Time\n");
	
		for (Cloudlet cloudlet : list) {
			//if (cloudlet.getStatus() == Cloudlet.CloudletStatus.SUCCESS) {
				sb.append(cloudlet.getCloudletId()).append(",");
				sb.append(cloudlet.getUserId()).append(",");
				sb.append(cloudlet.getStatus()).append(",");
				sb.append(cloudlet.getResourceId()).append(",");
				sb.append(dft.format(cloudlet.getSubmissionTime())).append(",");
				sb.append(dft.format(cloudlet.getExecStartTime())).append(",");
				sb.append(dft.format(cloudlet.getExecFinishTime())).append(",");
				sb.append(cloudlet.getCloudletLength()).append(",");
				sb.append(dft.format(cloudlet.getProcessingCost())).append(",");
				sb.append(cloudlet.getCloudletFileSize()).append(",");
				sb.append(cloudlet.getUtilizationModelCpu().getClass().getSimpleName()).append(",");
				sb.append(cloudlet.getUtilizationModelRam().getClass().getSimpleName()).append(",");
				sb.append(cloudlet.getUtilizationModelBw().getClass().getSimpleName()).append(",");
				sb.append(dft.format(cloudlet.getWaitingTime())).append("\n");
			//}
		}
	
		try (FileWriter writer = new FileWriter(filePath)) {
			writer.write(sb.toString());
			System.out.println("Cloudlet results saved to " + filePath);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
