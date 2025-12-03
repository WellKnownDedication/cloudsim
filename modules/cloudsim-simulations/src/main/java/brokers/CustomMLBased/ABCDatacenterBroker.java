package brokers.CustomMLBased;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudActionTags;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.GuestEntity;
import org.cloudbus.cloudsim.lists.VmList;

import java.util.*;

public class ABCDatacenterBroker extends DatacenterBroker {

    public ABCDatacenterBroker(String name) throws Exception {
        super(name);
    }

    @Override
protected void submitCloudlets() {

        for (Cloudlet cl : getCloudletList()) {

            int vmId = cl.getGuestId();

            Integer datacenterId = getVmsToDatacentersMap().get(vmId);

            if (datacenterId == null) {
                System.err.println("Skipping cloudlet " + cl.getCloudletId()
                        + " (invalid VM ID " + vmId + ")");
                continue;
            }

            sendNow(datacenterId, CloudActionTags.CLOUDLET_SUBMIT, cl);
            cloudletsSubmitted++;
            getCloudletSubmittedList().add(cl);
        }
    }


    public void runArtificialBeeColony() {
        List<Cloudlet> cloudlets = new ArrayList<>(getCloudletList());
        int numCloudlets = cloudlets.size();

        for (int i = 0; i < numCloudlets; i++) {
            Cloudlet tmp = cloudlets.get(i);
            int idx = i;
            for (int j = i + 1; j < numCloudlets; j++) {
                if (cloudlets.get(j).getCloudletLength() < tmp.getCloudletLength()) {
                    idx = j;
                    tmp = cloudlets.get(j);
                }
            }
            Cloudlet tmp2 = cloudlets.get(i);
            cloudlets.set(i, tmp);
            cloudlets.set(idx, tmp2);
        }

        List<Vm> vms = new ArrayList<>( (List<Vm>) (List<?>) getGuestsCreatedList() );
        int numVms = vms.size();

        for (int i = 0; i < numVms; i++) {
            Vm tmp = vms.get(i);
            int idx = i;
            for (int j = i + 1; j < numVms; j++) {
                if (vms.get(j).getMips() > tmp.getMips()) {
                    idx = j;
                    tmp = vms.get(j);
                }
            }
            Vm tmp2 = vms.get(i);
            vms.set(i, tmp);
            vms.set(idx, tmp2);
        }

        // ABC parameters (use same population size idea as PSO)
        final int NUM_BEES = 50;        // food sources = solutions (employed bees)
        final int MAX_ITER = 40;
        final int LIMIT = 5;           // scout threshold (iterations without improvement)

        // Hybrid guidance chance: probability that a generated neighbor will adopt some positions from the global best
        final double hybridProb = 0.40;

        // local search intensity: number of swap attempts in local repair
        final int LOCAL_SEARCH_TRIES = 6;

        // adaptive alpha baseline & scale (used for penalty)
        final double alpha0 = 0.2;
        final double alphaScale = 3.0;

        Random rand = new Random();

        // population: each bee is a solution mapping cloudlet -> vmIndex
        int[][] foods = new int[NUM_BEES][numCloudlets];
        double[] fitness = new double[NUM_BEES];
        double[] fitnessProbability = new double[NUM_BEES];
        int[] trial = new int[NUM_BEES]; // stagnation counters for scout
        int[] bestSolution = new int[numCloudlets];
        double bestScore = Double.MAX_VALUE;

        List<int[]> seeds = new ArrayList<>();
        seeds.add(heuristicMCT(cloudlets, vms));       // Min completion time per cloudlet
        seeds.add(heuristicLPT(cloudlets, vms));       // Longest Processing Time style seed
        seeds.add(heuristicMinLoad(cloudlets, vms));   // Greedy minimal estimated load

        int seedCount = Math.min(seeds.size(), NUM_BEES);
        for (int b = 0; b < NUM_BEES; b++) {
            if (b < seedCount && seeds.get(b) != null) {
                foods[b] = Arrays.copyOf(seeds.get(b), numCloudlets);
            } else {
                for (int i = 0; i < numCloudlets; i++) {
                    foods[b][i] = rand.nextInt(numVms);
                }
            }
            localSearchGreedy(foods[b], cloudlets, vms, LOCAL_SEARCH_TRIES, alpha0, rand);

            fitness[b] = computeFitnessWithAlpha(foods[b], cloudlets, vms, alpha0);
            if (fitness[b] < bestScore) {
                bestScore = fitness[b];
                System.arraycopy(foods[b], 0, bestSolution, 0, numCloudlets);
            }
            trial[b] = 0;
        }

        for (int iter = 0; iter < MAX_ITER; iter++) {

            // compute adaptive alpha for this iteration (increases over time)
            double alpha = alpha0 + alphaScale * ((double) iter / Math.max(1, (MAX_ITER - 1)));

            // employed bees phase
            for (int b = 0; b < NUM_BEES; b++) {
                int[] neighbor = Arrays.copyOf(foods[b], numCloudlets);

                // Multiple-neighbour move: pairwise swap + small random reassign
                // pick two cloudlets
                if (numCloudlets >= 2) {
                    int c1 = rand.nextInt(numCloudlets);
                    int c2 = rand.nextInt(numCloudlets);
                    while (c2 == c1 && numCloudlets > 1) c2 = rand.nextInt(numCloudlets);
                    // swap assigned VMs
                    int tmp = neighbor[c1];
                    neighbor[c1] = neighbor[c2];
                    neighbor[c2] = tmp;
                }
                // optionally reassign one random cloudlet to a random VM
                if (rand.nextDouble() < 0.4) {
                    int cr = rand.nextInt(numCloudlets);
                    int newVm = rand.nextInt(numVms);
                    neighbor[cr] = newVm;
                }

                // hybrid PSO-style guidance: copy a few positions from global best with hybridProb
                if (rand.nextDouble() < hybridProb) {
                    // copy a small fraction of positions
                    int copies = Math.max(1, numCloudlets / 10);
                    for (int k = 0; k < copies; k++) {
                        int idx = rand.nextInt(numCloudlets);
                        neighbor[idx] = bestSolution[idx];
                    }
                }

                // local search / repair to refine neighbor (greedy swap attempts)
                localSearchGreedy(neighbor, cloudlets, vms, LOCAL_SEARCH_TRIES, alpha, rand);

                double neighborFit = computeFitnessWithAlpha(neighbor, cloudlets, vms, alpha);

                // Greedy selection: replace if better
                if (neighborFit < fitness[b]) {
                    foods[b] = neighbor;
                    fitness[b] = neighborFit;
                    trial[b] = 0;
                    if (neighborFit < bestScore) {
                        bestScore = neighborFit;
                        System.arraycopy(neighbor, 0, bestSolution, 0, numCloudlets);
                    }
                } else {
                    trial[b]++;
                }
            }

            // calculate selection probabilities for onlookers
            double eps = 0.00000001;
            double sumNectar = 0.0;
            double[] nectar = new double[NUM_BEES];
            for (int b = 0; b < NUM_BEES; b++) {
                nectar[b] = 1.0 / (fitness[b] + eps);
                sumNectar += nectar[b];
            }
            for (int b = 0; b < NUM_BEES; b++) {
                fitnessProbability[b] = (sumNectar == 0.0) ? (1.0 / NUM_BEES) : (nectar[b] / sumNectar);
            }

            // onlooker bees phase
            int onlookers = NUM_BEES;
            int onlookerCount = 0;
            int onlookerAttempts = 0;
            while (onlookerCount < onlookers && onlookerAttempts < 3 * onlookers) {
                onlookerAttempts++;
                // roulette-wheel select
                double r = rand.nextDouble();
                double cumulative = 0;
                int selected = NUM_BEES - 1;
                for (int b = 0; b < NUM_BEES; b++) {
                    cumulative += fitnessProbability[b];
                    if (r <= cumulative) {
                        selected = b;
                        break;
                    }
                }

                // create neighbor from selected
                int[] neighbor = Arrays.copyOf(foods[selected], numCloudlets);

                // multiple-neighbour move: swap two cloudlets
                if (numCloudlets >= 2) {
                    int c1 = rand.nextInt(numCloudlets);
                    int c2 = rand.nextInt(numCloudlets);
                    while (c2 == c1 && numCloudlets > 1) c2 = rand.nextInt(numCloudlets);
                    int tmp = neighbor[c1];
                    neighbor[c1] = neighbor[c2];
                    neighbor[c2] = tmp;
                }
                // occasional random reassignment
                if (rand.nextDouble() < 0.45) {
                    int cr = rand.nextInt(numCloudlets);
                    neighbor[cr] = rand.nextInt(numVms);
                }

                // hybrid guidance
                if (rand.nextDouble() < hybridProb) {
                    int copies = Math.max(1, numCloudlets / 12);
                    for (int k = 0; k < copies; k++) {
                        int idx = rand.nextInt(numCloudlets);
                        neighbor[idx] = bestSolution[idx];
                    }
                }

                // local search
                localSearchGreedy(neighbor, cloudlets, vms, LOCAL_SEARCH_TRIES, alpha, rand);

                double neighborFit = computeFitnessWithAlpha(neighbor, cloudlets, vms, alpha);

                // greedy replacement
                if (neighborFit < fitness[selected]) {
                    foods[selected] = neighbor;
                    fitness[selected] = neighborFit;
                    trial[selected] = 0;
                    if (neighborFit < bestScore) {
                        bestScore = neighborFit;
                        System.arraycopy(neighbor, 0, bestSolution, 0, numCloudlets);
                    }
                } else {
                    trial[selected]++;
                }

                onlookerCount++;
            }

            // scout phase
            for (int b = 0; b < NUM_BEES; b++) {
                if (trial[b] >= LIMIT) {
                    // reinitialize this food RANDOMLY but biased by heuristics (mix)
                    int[] newFood = new int[numCloudlets];
                    if (rand.nextDouble() < 0.5) {
                        // generate a heuristic-like random: prefer higher MIPS for longer cloudlets
                        for (int i = 0; i < numCloudlets; i++) {
                            // probability proportional to VM MIPS, but weighted by cloudlet length
                            double[] weights = new double[numVms];
                            double len = cloudlets.get(i).getCloudletLength();
                            double totalW = 0;
                            for (int v = 0; v < numVms; v++) {
                                weights[v] = vms.get(v).getMips() * Math.log(1 + len);
                                totalW += weights[v];
                            }
                            double r2 = rand.nextDouble() * totalW;
                            double cum = 0;
                            int chosen = 0;
                            for (int v = 0; v < numVms; v++) {
                                cum += weights[v];
                                if (r2 <= cum) {
                                    chosen = v;
                                    break;
                                }
                            }
                            newFood[i] = chosen;
                        }
                    } else {
                        for (int i = 0; i < numCloudlets; i++) {
                            newFood[i] = rand.nextInt(numVms);
                        }
                    }

                    // local repair
                    localSearchGreedy(newFood, cloudlets, vms, LOCAL_SEARCH_TRIES, alpha, rand);

                    foods[b] = newFood;
                    fitness[b] = computeFitnessWithAlpha(newFood, cloudlets, vms, alpha);
                    trial[b] = 0;
                    if (fitness[b] < bestScore) {
                        bestScore = fitness[b];
                        System.arraycopy(newFood, 0, bestSolution, 0, numCloudlets);
                    }
                }
            }
        }

        List<Cloudlet> finalCloudletList = new ArrayList<>();
        List<Vm> finalVmList = new ArrayList<>();

        for (int i = 0; i < numCloudlets; i++) {
            finalCloudletList.add(cloudlets.get(i));
            finalVmList.add(vms.get(bestSolution[i]));
        }

        getGuestList().clear();
        getCloudletList().clear();
        getGuestList().addAll(finalVmList);
        getCloudletList().addAll(finalCloudletList);
    }

    /**
     * Compute fitness using given alpha penalty.
     */
    private double computeFitnessWithAlpha(int[] mapping, List<Cloudlet> cl, List<Vm> vm, double alpha) {
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
        for (int l : loads) {
            penalty += alpha * (l * l);
        }

        return sum + penalty;
    }

    /**
     * Local greedy search: try pairwise swaps between cloudlets to improve fitness.
     * Small number of tries only (fast).
     */
    private void localSearchGreedy(int[] sol, List<Cloudlet> cl, List<Vm> vm, int tries, double alpha, Random rand) {
        int n = sol.length;
        double current = computeFitnessWithAlpha(sol, cl, vm, alpha);

        for (int t = 0; t < tries; t++) {
            if (n < 2) return;
            int a = rand.nextInt(n);
            int b = rand.nextInt(n);
            while (b == a && n > 1) b = rand.nextInt(n);

            if (sol[a] == sol[b]) continue; // swapping same VM does nothing

            // try swap
            int tmp = sol[a];
            sol[a] = sol[b];
            sol[b] = tmp;

            double nf = computeFitnessWithAlpha(sol, cl, vm, alpha);
            if (nf < current) {
                current = nf; // keep swap
            } else {
                // revert
                tmp = sol[a];
                sol[a] = sol[b];
                sol[b] = tmp;
            }
        }
    }

    /**
     * Heuristic: Min Completion Time (MCT) - assign each cloudlet to VM that minimizes length / mips
     */
    private int[] heuristicMCT(List<Cloudlet> cl, List<Vm> vm) {
        int n = cl.size();
        int m = vm.size();
        if (m == 0) return null;
        int[] sol = new int[n];
        for (int i = 0; i < n; i++) {
            double best = Double.MAX_VALUE;
            int chosen = 0;
            for (int v = 0; v < m; v++) {
                double val = cl.get(i).getCloudletLength() / vm.get(v).getMips();
                if (val < best) {
                    best = val;
                    chosen = v;
                }
            }
            sol[i] = chosen;
        }
        return sol;
    }

    /**
     * Heuristic: LPT-like seed — assign longest tasks preferentially to highest-MIPS VMs in round-robin
     */
    private int[] heuristicLPT(List<Cloudlet> cl, List<Vm> vm) {
        int n = cl.size();
        int m = vm.size();
        if (m == 0) return null;
        // get indices of tasks sorted descending by length
        Integer[] idx = new Integer[n];
        for (int i = 0; i < n; i++) idx[i] = i;
        Arrays.sort(idx, (a, b) -> Long.compare(cl.get(b).getCloudletLength(), cl.get(a).getCloudletLength()));

        int[] sol = new int[n];
        // round-robin to top VMs
        for (int i = 0; i < n; i++) {
            int taskIndex = idx[i];
            sol[taskIndex] = i % m; // since VMs are sorted by MIPS descending earlier, this assigns longest to best VMs first
        }
        return sol;
    }

    /**
     * Heuristic: Greedy min estimated load — iteratively assign each cloudlet to VM with smallest current estimated load (sum(length)/mips)
     */
    private int[] heuristicMinLoad(List<Cloudlet> cl, List<Vm> vm) {
        int n = cl.size();
        int m = vm.size();
        if (m == 0) return null;
        double[] load = new double[m]; // estimated time load
        Arrays.fill(load, 0.0);
        int[] sol = new int[n];

        // process longer tasks first for better packing
        Integer[] idx = new Integer[n];
        for (int i = 0; i < n; i++) idx[i] = i;
        Arrays.sort(idx, (a, b) -> Long.compare(cl.get(b).getCloudletLength(), cl.get(a).getCloudletLength()));

        for (int id : idx) {
            int best = 0;
            double bestVal = Double.MAX_VALUE;
            for (int v = 0; v < m; v++) {
                double est = load[v] + (cl.get(id).getCloudletLength() / vm.get(v).getMips());
                if (est < bestVal) {
                    bestVal = est;
                    best = v;
                }
            }
            sol[id] = best;
            load[best] += (cl.get(id).getCloudletLength() / vm.get(best).getMips());
        }
        return sol;
    }
}
