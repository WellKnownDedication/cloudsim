package brokers.CustomMLBased;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudActionTags;
import org.cloudbus.cloudsim.core.CloudSimTags;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PSODatacenterBroker extends DatacenterBroker {

    public PSODatacenterBroker(String name) throws Exception {
        super(name);
    }

    public void runPSO() {

        List<Cloudlet> cloudlets = new ArrayList<>(getCloudletList());
        List<Vm> vms = new ArrayList<>((List<Vm>)(List<?>)getGuestList());

        int numCloudlets = cloudlets.size();
        int numVms = vms.size();

        // PSO PARAMETERS
        int NUM_PARTICLES = 25;
        int MAX_ITER = 25;

        double w = 0.7;    // inertia weight
        double c1 = 1.4;   // cognitive coefficient
        double c2 = 1.4;   // social coefficient

        Random rand = new Random();

        // PARTICLE STRUCTURES
        int[][] particles = new int[NUM_PARTICLES][numCloudlets];
        double[][] velocities = new double[NUM_PARTICLES][numCloudlets];

        // personal best
        int[][] pbest = new int[NUM_PARTICLES][numCloudlets];
        double[] pbestScore = new double[NUM_PARTICLES];

        // global best
        int[] gbest = new int[numCloudlets];
        double gbestScore = Double.MAX_VALUE;

        // INITIALIZATION
        for (int p = 0; p < NUM_PARTICLES; p++) {

            for (int i = 0; i < numCloudlets; i++) {
                // Random VM assignment
                particles[p][i] = rand.nextInt(numVms);
                velocities[p][i] = 0.0;
                pbest[p][i] = particles[p][i];
            }

            double score = computeFitness(particles[p], cloudlets, vms);
            pbestScore[p] = score;

            if (score < gbestScore) {
                gbestScore = score;
                System.arraycopy(particles[p], 0, gbest, 0, numCloudlets);
            }
        }

        // PSO MAIN LOOP
        for (int iter = 0; iter < MAX_ITER; iter++) {
            for (int p = 0; p < NUM_PARTICLES; p++) {

                for (int i = 0; i < numCloudlets; i++) {

                    double r1 = rand.nextDouble();
                    double r2 = rand.nextDouble();

                    // velocity update
                    velocities[p][i] = w * velocities[p][i]
                            + c1 * r1 * (pbest[p][i] - particles[p][i])
                            + c2 * r2 * (gbest[i] - particles[p][i]);

                    // position update (VM index)
                    particles[p][i] += (int) Math.round(velocities[p][i]);

                    // enforce bounds
                    if (particles[p][i] < 0) particles[p][i] = 0;
                    if (particles[p][i] >= numVms) particles[p][i] = numVms - 1;
                }

                // evaluate fitness
                double score = computeFitness(particles[p], cloudlets, vms);

                // update personal best
                if (score < pbestScore[p]) {
                    pbestScore[p] = score;
                    System.arraycopy(particles[p], 0, pbest[p], 0, numCloudlets);
                }

                // update global best
                if (score < gbestScore) {
                    gbestScore = score;
                    System.arraycopy(particles[p], 0, gbest, 0, numCloudlets);
                }
            }
        }

        // APPLY BEST SOLUTION
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

        for (int i = 0; i < mapping.length; i++) {
            Cloudlet c = cl.get(i);
            Vm v = vm.get(mapping[i]);

            double execTime = c.getCloudletLength() / v.getMips();
            sum += execTime;
        }
        return sum;
    }

    // @Override
    // public void submitCloudletList(List<? extends Cloudlet> list) {
    //     getCloudletList().addAll(list);
    //     //runPSO();
    // }

    @Override
    protected void submitCloudlets() {
        runPSO();
        super.submitCloudlets();
    }

}
