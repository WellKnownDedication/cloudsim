mvn clean compile -pl modules/cloudsim-simulations/

# Prepeare training datasets
#mvn exec:java -e -pl modules/cloudsim-simulations/ -Dexec.mainClass=environments.heterogenous.heterogeneousDataPrep.dataPrepSingularDatacenterHeterogenous

# PSO broker
mvn exec:java -pl modules/cloudsim-simulations/ -Dexec.mainClass=environments.heterogenous.PSO.PSOSingularDatacenterHeterogenous 


# # Baseline with built-in datacenter brokers
# mvn exec:java -pl modules/cloudsim-simulations/ -Dexec.mainClass=homogenous.baseline.baselineSingularDatacenterHomogenous
# mvn exec:java -pl modules/cloudsim-simulations/ -Dexec.mainClass=homogenous.baseline.baselineMultiDatacenterHomogenous

# mvn exec:java -pl modules/cloudsim-simulations/ -Dexec.mainClass=heterogenous.baseline.baselineSingularDatacenterHeterogenous
# mvn exec:java -pl modules/cloudsim-simulations/ -Dexec.mainClass=heterogenous.baseline.baselineMultiDatacenterHeterogenous

# # RoundRobin datacenter Brockers
# mvn exec:java -pl modules/cloudsim-simulations/ -Dexec.mainClass=homogenous.roundRobin.roundRobinSingularDatacenterHomogenous
# mvn exec:java -pl modules/cloudsim-simulations/ -Dexec.mainClass=homogenous.roundRobin.roundRobinMultiDatacenterHomogenous

# mvn exec:java -pl modules/cloudsim-simulations/ -Dexec.mainClass=heterogenous.roundRobin.roundRobinSingularDatacenterHeterogenous
# mvn exec:java -pl modules/cloudsim-simulations/ -Dexec.mainClass=heterogenous.roundRobin.roundRobinMultiDatacenterHeterogenous

# # GeneticAlgorythm datacenter Brockers
# mvn exec:java -pl modules/cloudsim-simulations/ -Dexec.mainClass=homogenous.geneticAlgorythm.GASingularDatacenterHomogenous
# mvn exec:java -pl modules/cloudsim-simulations/ -Dexec.mainClass=homogenous.geneticAlgorythm.GAMultiDatacenterHomogenous

# mvn exec:java -pl modules/cloudsim-simulations/ -Dexec.mainClass=heterogenous.geneticAlgorythm.GASingularDatacenterHeterogenous
# mvn exec:java -pl modules/cloudsim-simulations/ -Dexec.mainClass=heterogenous.geneticAlgorythm.GAMultiDatacenterHeterogenous