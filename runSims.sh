mvn clean compile -pl modules/cloudsim-simulations/

# Prepeare training datasets
#mvn exec:java -e -pl modules/cloudsim-simulations/ -Dexec.mainClass=environments.heterogenous.heterogeneousDataPrep.dataPrepSingularDatacenterHeterogenous

# PSO datacenter brocker
#mvn exec:java -pl modules/cloudsim-simulations/ -Dexec.mainClass=environments.homogenous.PSO.PSOSingularDatacenterHomogenous
mvn exec:java -pl modules/cloudsim-simulations/ -Dexec.mainClass=environments.heterogenous.PSO.PSOSingularDatacenterHeterogenous 

# ABC datacenter brocker
#mvn exec:java -pl modules/cloudsim-simulations/ -Dexec.mainClass=environments.homogenous.ABC.ABCSingularDatacenterHomogenous
mvn exec:java -pl modules/cloudsim-simulations/ -Dexec.mainClass=environments.heterogenous.ABC.ABCSingularDatacenterHeterogenous 

# # Baseline with built-in datacenter brokers
# mvn exec:java -pl modules/cloudsim-simulations/ -Dexec.mainClass=environments.homogenous.baseline.baselineSingularDatacenterHomogenous
# mvn exec:java -pl modules/cloudsim-simulations/ -Dexec.mainClass=environments.homogenous.baseline.baselineMultiDatacenterHomogenous

# mvn exec:java -pl modules/cloudsim-simulations/ -Dexec.mainClass=environments.heterogenous.baseline.baselineSingularDatacenterHeterogenous
# mvn exec:java -pl modules/cloudsim-simulations/ -Dexec.mainClass=environments.heterogenous.baseline.baselineMultiDatacenterHeterogenous

# # RoundRobin datacenter Brockers
#mvn exec:java -pl modules/cloudsim-simulations/ -Dexec.mainClass=environments.homogenous.roundRobin.roundRobinSingularDatacenterHomogenous
# mvn exec:java -pl modules/cloudsim-simulations/ -Dexec.mainClass=environments.homogenous.roundRobin.roundRobinMultiDatacenterHomogenous

#mvn exec:java -pl modules/cloudsim-simulations/ -Dexec.mainClass=environments.heterogenous.roundRobin.roundRobinSingularDatacenterHeterogenous
# mvn exec:java -pl modules/cloudsim-simulations/ -Dexec.mainClass=environments.heterogenous.roundRobin.roundRobinMultiDatacenterHeterogenous

# # GeneticAlgorythm datacenter Brockers
# mvn exec:java -pl modules/cloudsim-simulations/ -Dexec.mainClass=environments.homogenous.geneticAlgorythm.GASingularDatacenterHomogenous
# mvn exec:java -pl modules/cloudsim-simulations/ -Dexec.mainClass=environments.homogenous.geneticAlgorythm.GAMultiDatacenterHomogenous

# mvn exec:java -pl modules/cloudsim-simulations/ -Dexec.mainClass=environments.heterogenous.geneticAlgorythm.GASingularDatacenterHeterogenous
# mvn exec:java -pl modules/cloudsim-simulations/ -Dexec.mainClass=henvironments.eterogenous.geneticAlgorythm.GAMultiDatacenterHeterogenous