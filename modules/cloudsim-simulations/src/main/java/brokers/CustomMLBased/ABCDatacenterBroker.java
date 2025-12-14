package brokers.CustomMLBased;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.Vm;

import java.util.*;

public class ABCDatacenterBroker extends DatacenterBroker {

    public ABCDatacenterBroker(String name) throws Exception {
        super(name);
    }

    @Override
    protected void submitCloudlets() {
        runArtificialBeeColony();
        super.submitCloudlets();
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

        List<Vm> vms = new ArrayList<>((List<Vm>) (List<?>) getGuestsCreatedList());
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

        final int NUM_BEES = 10;
        final int MAX_ITER = 50;
        final int LIMIT = numCloudlets % 5;

        final double hybridProb = 0.5;
        final int LOCAL_SEARCH_TRIES = 5;

        Random rand = new Random();

        int[][] foods = new int[NUM_BEES][numCloudlets];
        double[] fitness = new double[NUM_BEES];
        double[] fitnessProbability = new double[NUM_BEES];
        int[] trial = new int[NUM_BEES];
        int[] bestSolution = new int[numCloudlets];
        double bestScore = Double.MAX_VALUE;

        List<int[]> seeds = new ArrayList<>();
        seeds.add(heuristicMCT(cloudlets, vms));
        seeds.add(heuristicLPT(cloudlets, vms));
        seeds.add(heuristicMinLoad(cloudlets, vms));
        seeds.add(heuristicMCT(cloudlets, vms));
        seeds.add(heuristicLPT(cloudlets, vms));
        seeds.add(heuristicMinLoad(cloudlets, vms));

        int seedCount = Math.min(seeds.size(), NUM_BEES);

        for (int b = 0; b < NUM_BEES; b++) {
            if (b < seedCount && seeds.get(b) != null) {
                foods[b] = Arrays.copyOf(seeds.get(b % seedCount), numCloudlets);
            } else {
                for (int i = 0; i < numCloudlets; i++) {
                    foods[b][i] = rand.nextInt(numVms);
                }
            }

            localSearchGreedy(foods[b], cloudlets, vms, LOCAL_SEARCH_TRIES, rand);
            fitness[b] = computeFitness(foods[b], cloudlets, vms);

            if (fitness[b] < bestScore) {
                bestScore = fitness[b];
                System.arraycopy(foods[b], 0, bestSolution, 0, numCloudlets);
            }

            trial[b] = 0;
        }

        for (int iter = 0; iter < MAX_ITER; iter++) {

            for (int b = 0; b < NUM_BEES; b++) {

                int[] neighbor = Arrays.copyOf(foods[b], numCloudlets);

                if (numCloudlets >= 2) {
                    int c1 = rand.nextInt(numCloudlets);
                    int c2 = rand.nextInt(numCloudlets);
                    while (c2 == c1) c2 = rand.nextInt(numCloudlets);

                    int tmp = neighbor[c1];
                    neighbor[c1] = neighbor[c2];
                    neighbor[c2] = tmp;
                }

                if (rand.nextDouble() < hybridProb) {
                    neighbor[rand.nextInt(numCloudlets)] = rand.nextInt(numVms);
                }

                localSearchGreedy(neighbor, cloudlets, vms, LOCAL_SEARCH_TRIES, rand);
                double nfit = computeFitness(neighbor, cloudlets, vms);

                if (nfit < fitness[b]) {
                    foods[b] = neighbor;
                    fitness[b] = nfit;
                    trial[b] = 0;

                    if (nfit < bestScore) {
                        bestScore = nfit;
                        System.arraycopy(neighbor, 0, bestSolution, 0, numCloudlets);
                    }
                } else trial[b]++;
            }

            double sum = 0;
            double eps = 0.00001;

            double[] nectar = new double[NUM_BEES];
            for (int b = 0; b < NUM_BEES; b++) {
                nectar[b] = 1.0 / (fitness[b] + eps);
                sum += nectar[b];
            }

            for (int b = 0; b < NUM_BEES; b++) {
                fitnessProbability[b] = nectar[b] / sum;
            }

            int onlookers = NUM_BEES;
            int count = 0;
            int attempts = 0;

            while (count < onlookers && attempts < 3 * onlookers) {
                attempts++;

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

                int[] neighbor = Arrays.copyOf(foods[selected], numCloudlets);

                if (numCloudlets >= 2) {
                    int c1 = rand.nextInt(numCloudlets);
                    int c2 = rand.nextInt(numCloudlets);
                    while (c2 == c1) c2 = rand.nextInt(numCloudlets);

                    int tmp = neighbor[c1];
                    neighbor[c1] = neighbor[c2];
                    neighbor[c2] = tmp;
                }

                if (rand.nextDouble() < hybridProb) {
                    int copies = Math.max(1, numCloudlets / 12);
                    for (int k = 0; k < copies; k++) {
                        int idx = rand.nextInt(numCloudlets);
                        neighbor[idx] = bestSolution[idx];
                    }
                }

                if (rand.nextDouble() < 0.8) {
                    neighbor[rand.nextInt(numCloudlets)] = rand.nextInt(numVms);
                }
                if (rand.nextDouble() < 0.8) {
                    neighbor[rand.nextInt(numCloudlets)] = rand.nextInt(numVms);
                }

                localSearchGreedy(neighbor, cloudlets, vms, LOCAL_SEARCH_TRIES, rand);
                double nfit = computeFitness(neighbor, cloudlets, vms);

                if (nfit < fitness[selected]) {
                    foods[selected] = neighbor;
                    fitness[selected] = nfit;
                    trial[selected] = 0;

                    if (nfit < bestScore) {
                        bestScore = nfit;
                        System.arraycopy(neighbor, 0, bestSolution, 0, numCloudlets);
                    }
                } else trial[selected]++;

                count++;
            }

            for (int b = 0; b < NUM_BEES; b++) {
                if (trial[b] >= LIMIT) {
                    int[] newFood = new int[numCloudlets];
                    for (int i = 0; i < numCloudlets; i++) {
                        newFood[i] = rand.nextInt(numVms);
                    }

                    localSearchGreedy(newFood, cloudlets, vms, LOCAL_SEARCH_TRIES, rand);
                    foods[b] = newFood;
                    fitness[b] = computeFitness(newFood, cloudlets, vms);
                    trial[b] = 0;

                    if (fitness[b] < bestScore) {
                        bestScore = fitness[b];
                        System.arraycopy(newFood, 0, bestSolution, 0, numCloudlets);
                    }
                }
            }
        }

        for (int i = 0; i < numCloudlets; i++) {
            Vm chosenVm = vms.get(bestSolution[i]);
            cloudlets.get(i).setGuestId(chosenVm.getId());
        }

        getGuestList().clear();
        getCloudletList().clear();
        getGuestList().addAll(vms);
        getCloudletList().addAll(cloudlets);
    }

    private double computeFitness(int[] mapping, List<Cloudlet> cl, List<Vm> vm) {
        double sum = 0.0;
        int[] loads = new int[vm.size()];

        for (int i = 0; i < mapping.length; i++) {
            Cloudlet c = cl.get(i);
            Vm v = vm.get(mapping[i]);
            sum += c.getCloudletLength() / v.getMips();
            loads[mapping[i]]++;
        }

        double penalty = 0;
        for (int l : loads) {
            penalty += (l * l);
        }

        return sum + penalty;
    }

    private void localSearchGreedy(int[] sol, List<Cloudlet> cl, List<Vm> vm, int tries, Random rand) {
        double current = computeFitness(sol, cl, vm);

        for (int t = 0; t < tries; t++) {
            int a = rand.nextInt(sol.length);
            int b = rand.nextInt(sol.length);
            while (b == a) b = rand.nextInt(sol.length);

            int tmp = sol[a];
            sol[a] = sol[b];
            sol[b] = tmp;

            double nf = computeFitness(sol, cl, vm);
            if (nf < current) current = nf;
            else {
                tmp = sol[a];
                sol[a] = sol[b];
                sol[b] = tmp;
            }
        }
    }

    private int[] heuristicMCT(List<Cloudlet> cl, List<Vm> vm) {
        int[] sol = new int[cl.size()];
        for (int i = 0; i < cl.size(); i++) {
            double best = Double.MAX_VALUE;
            int chosen = 0;
            for (int v = 0; v < vm.size(); v++) {
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

    private int[] heuristicLPT(List<Cloudlet> cl, List<Vm> vm) {
        int n = cl.size();
        Integer[] idx = new Integer[n];
        for (int i = 0; i < n; i++) idx[i] = i;

        Arrays.sort(idx, (a, b) ->
                Long.compare(cl.get(b).getCloudletLength(), cl.get(a).getCloudletLength())
        );

        int[] sol = new int[n];
        for (int i = 0; i < n; i++) sol[idx[i]] = i % vm.size();
        return sol;
    }

    private int[] heuristicMinLoad(List<Cloudlet> cl, List<Vm> vm) {
        int n = cl.size();
        int m = vm.size();
        double[] load = new double[m];
        int[] sol = new int[n];

        Integer[] idx = new Integer[n];
        for (int i = 0; i < n; i++) idx[i] = i;

        Arrays.sort(idx, (a, b) ->
                Long.compare(cl.get(b).getCloudletLength(), cl.get(a).getCloudletLength())
        );

        for (int id : idx) {
            int best = 0;
            double bestVal = Double.MAX_VALUE;
            for (int v = 0; v < m; v++) {
                double est = load[v] + cl.get(id).getCloudletLength() / vm.get(v).getMips();
                if (est < bestVal) {
                    bestVal = est;
                    best = v;
                }
            }
            sol[id] = best;
            load[best] += cl.get(id).getCloudletLength() / vm.get(best).getMips();
        }
        return sol;
    }
}
