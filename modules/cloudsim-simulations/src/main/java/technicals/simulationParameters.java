package technicals;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

public class simulationParameters {
	public int cloudletNumber = 5000;
	public int bw = 1500;

	public Datacenter createDatacenter(String name, int hostNumber, int bw, double cost_multiplier){

		// Here are the steps needed to create a PowerDatacenter:
		// 1. We need to create a list to store one or more
		//    Machines
		List<Host> hostList = new ArrayList<>();

		//int hostNumber = 2;
		int PeNumber = 8;

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
    
    public void writeCloudletListToCSV(List<Cloudlet> list, String filePath) {
		StringBuilder sb = new StringBuilder();
		DecimalFormat dft = new DecimalFormat("###.##");
	
		// Header
		sb.append("Cloudlet ID,")
		  .append("User ID,")
		  .append("Status,")
		  .append("Data Center ID,")
		  .append("Submission Time,")
		  .append("Start Time,")
		  .append("Finish Time,")
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

	// public static void writeVMListToCSV(List<Vm> list, String filePath) {
	// 	StringBuilder sb = new StringBuilder();
	// 	DecimalFormat dft = new DecimalFormat("###.##");
	
	// 	// Header
	// 	sb.append("Cloudlet ID,")
	// 	  .append("User ID,")
	// 	  .append("Status,")
	// 	  .append("Data Center ID,")
	// 	  .append("Submission Time,")
	// 	  .append("Start Time,")
	// 	  .append("Finish Time,")
	// 	  .append("Cloudlet Length,Processing Cost,File Size,")
	// 	  .append("CPU Utilization,RAM Utilization,BW Utilization,Waiting Time\n");
		  
	
	// 	for (Vm vm : list) {
	// 		sb.append(vm.getId());
	// 		sb.append(vm.get);

	// 	}
	
	// 	try (FileWriter writer = new FileWriter(filePath)) {
	// 		writer.write(sb.toString());
	// 		System.out.println("Cloudlet results saved to " + filePath);
	// 	} catch (IOException e) {
	// 		e.printStackTrace();
	// 	}
	// }
}
