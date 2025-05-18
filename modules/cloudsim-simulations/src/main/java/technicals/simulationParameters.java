package technicals;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;

import org.cloudbus.cloudsim.Cloudlet;

public class simulationParameters {

    
    public static void writeCloudletListToCSV(List<Cloudlet> list, String filePath) {
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
}
