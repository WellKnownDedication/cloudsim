package brokers.CustomMLBased;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.Vm;

import java.util.*;

public class PSODatacenterBroker extends DatacenterBroker {

    public PSODatacenterBroker(String name) throws Exception {
        super(name);
    }

    @Override
    protected void submitCloudlets() {
        runPSO();
        super.submitCloudlets();
    }

    public void runPSO() {

        List<Cloudlet> cloudlets = new ArrayList<>(getCloudletList());
        int numCloudlets = cloudlets.size();

        for(int i=0;i<numCloudlets;i++){
            Cloudlet tmp = cloudlets.get(i);
            int idx = i;
            for(int j=i+1;j<numCloudlets;j++)
            {
                if(cloudlets.get(j).getCloudletLength() < tmp.getCloudletLength())
                {
                    idx = j;
                    tmp = cloudlets.get(j);
                }
            }
            Cloudlet tmp2 = cloudlets.get(i);
            cloudlets.set(i, tmp);
            cloudlets.set(idx,tmp2);
        }

        List<Vm> vms = new ArrayList<>((List<Vm>)(List<?>)getGuestList());

        int numVms=vms.size();

        for(int i=0;i<numVms;i++){
            Vm tmp=vms.get(i);
            int idx=i;

            for(int j=i+1;j<numVms;j++)
            {
                if(vms.get(j).getMips()>tmp.getMips())
                {
                    idx=j;
                    tmp=vms.get(j);
                }
            }
            Vm tmp2 = vms.get(i);
            vms.set(i, tmp);
            vms.set(idx,tmp2);
        }

        int NUM_PARTICLES = 30;
        int MAX_ITER = 40;

        double w = 0.4;      // inertia
        double c1 = 1.3;     // personal influence
        double c2 = 1.3;     // global influence

        Random rand = new Random();

        double[][][] prob = new double[NUM_PARTICLES][numCloudlets][numVms];

        int[][] particles = new int[NUM_PARTICLES][numCloudlets];

        int[][] pbest = new int[NUM_PARTICLES][numCloudlets];
        double[] pbestScore = new double[NUM_PARTICLES];

        int[] gbest = new int[numCloudlets];
        double gbestScore = Double.MAX_VALUE;

        for (int p = 0; p < NUM_PARTICLES; p++) {

            for (int i = 0; i < numCloudlets; i++) {
                for (int v = 0; v < numVms; v++) {
                    prob[p][i][v] = 1.0 / numVms;
                }

                // sample an initial assignment
                particles[p][i] = sampleFromDistribution(prob[p][i], rand);
                pbest[p][i] = particles[p][i];
            }

            double score = computeFitness(particles[p], cloudlets, vms);
            pbestScore[p] = score;

            if (score < gbestScore) {
                gbestScore = score;
                System.arraycopy(particles[p], 0, gbest, 0, numCloudlets);
            }
        }

        for (int iter = 0; iter < MAX_ITER; iter++) {

            for (int p = 0; p < NUM_PARTICLES; p++) {

                for (int i = 0; i < numCloudlets; i++) {

                    // Convert pbest and gbest to one-hot vectors
                    double[] pbestOneHot = oneHot(pbest[p][i], numVms);
                    double[] gbestOneHot = oneHot(gbest[i], numVms);

                    // Update probability vector
                    for (int v = 0; v < numVms; v++) {
                        prob[p][i][v] =
                                w * prob[p][i][v]
                                        + c1 * rand.nextDouble() * pbestOneHot[v]
                                        + c2 * rand.nextDouble() * gbestOneHot[v];
                    }

                    normalize(prob[p][i]);

                    // Resample new VM assignment
                    particles[p][i] = sampleFromDistribution(prob[p][i], rand);
                }

                double score = computeFitness(particles[p], cloudlets, vms);

                if (score < pbestScore[p]) {
                    pbestScore[p] = score;
                    System.arraycopy(particles[p], 0, pbest[p], 0, numCloudlets);
                }

                if (score < gbestScore) {
                    gbestScore = score;
                    System.arraycopy(particles[p], 0, gbest, 0, numCloudlets);
                }
            }
        }

        List<Cloudlet> finalCloudletList = new ArrayList<>();
        List<Vm> finalVmList = new ArrayList<>();

        for (int i = 0; i < numCloudlets; i++) {
            finalCloudletList.add(cloudlets.get(i));
            finalVmList.add(vms.get(gbest[i]));
        }

        getGuestList().clear();
        getCloudletList().clear();
        getGuestList().addAll(finalVmList);
        getCloudletList().addAll(finalCloudletList);
    }

    private double computeFitness(int[] mapping, List<Cloudlet> cl, List<Vm> vm) {
        double sum = 0.0;

        int numVms = vm.size();
        int[] loads = new int[numVms];

        for (int i = 0; i < mapping.length; i++) {
            Cloudlet c = cl.get(i);
            Vm v = vm.get(mapping[i]);

            sum += c.getCloudletLength() / v.getMips();
            loads[mapping[i]]++;
        }

        double penalty = 0;
        double alpha = 2.0;  // penalty strength (tuneable)

        for (int l : loads) {
            penalty += alpha * (l * l);
        }

        return sum + penalty;
    }

    private void normalize(double[] arr) {
        double sum = 0;
        for (double x : arr) sum += x;
        if (sum == 0) {
            double uniform = 1.0 / arr.length;
            Arrays.fill(arr, uniform);
            return;
        }
        for (int i = 0; i < arr.length; i++) {
            arr[i] /= sum;
        }
    }

    private double[] oneHot(int index, int length) {
        double[] vec = new double[length];
        vec[index] = 1.0;
        return vec;
    }

    private int sampleFromDistribution(double[] prob, Random rand) {
        double r = rand.nextDouble();
        double cumulative = 0;

        for (int i = 0; i < prob.length; i++) {
            cumulative += prob[i];
            if (r <= cumulative) return i;
        }
        return prob.length - 1;
    }
}
