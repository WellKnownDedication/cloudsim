package technicals;

import java.util.ArrayList;
import java.util.List;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletSchedulerSpaceShared;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.Vm;


/**
 * An example showing how to create
 * scalable simulations.
 */
public class envSetup {
	public static DatacenterBroker broker;

	private static List<Vm> createVMList(int userId, final int vms) {
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

	private static List<Cloudlet> createCloudletList(int userId, int cloudlets){
		// Creates a container to store Cloudlets
		List<Cloudlet> list = new ArrayList<>();

		//cloudlet parameters
		long length = 4000;
		long fileSize = 500;
		long outputSize = 400;
		int pesNumber = 2;
		UtilizationModel utilizationModel = new UtilizationModelFull();

		for(int i=0;i<cloudlets;i++){
			list.add(new Cloudlet(i, length, pesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel));
			list.getLast().setUserId(userId);
		}

		return list;
	}
}