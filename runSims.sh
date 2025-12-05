mvn clean compile -pl modules/cloudsim-simulations/

# PSO datacenter brocker
mvn exec:java -pl modules/cloudsim-simulations/ -Dexec.mainClass=environments.PSO.PSOSingularDatacenterHeterogenous 

# ABC datacenter brocker
mvn exec:java -pl modules/cloudsim-simulations/ -Dexec.mainClass=environments.ABC.ABCSingularDatacenterHeterogenous 

# # Baseline with built-in datacenter brokers
mvn exec:java -pl modules/cloudsim-simulations/ -Dexec.mainClass=environments.baseline.baselineSingularDatacenterHeterogenous
# mvn exec:java -pl modules/cloudsim-simulations/ -Dexec.mainClass=environments.baseline.baselineMultiDatacenterHeterogenous

# # RoundRobin datacenter Brockers
mvn exec:java -pl modules/cloudsim-simulations/ -Dexec.mainClass=environments.roundRobin.roundRobinSingularDatacenterHeterogenous
# mvn exec:java -pl modules/cloudsim-simulations/ -Dexec.mainClass=environments.roundRobin.roundRobinMultiDatacenterHeterogenous

# # GeneticAlgorythm datacenter Brockers
mvn exec:java -pl modules/cloudsim-simulations/ -Dexec.mainClass=environments.geneticAlgorythm.GASingularDatacenterHeterogenous
# mvn exec:java -pl modules/cloudsim-simulations/ -Dexec.mainClass=henvironments.eterogenous.geneticAlgorythm.GAMultiDatacenterHeterogenous