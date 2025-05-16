mvn clean compile -pl modules/cloudsim-simulations/

mvn exec:java -pl modules/cloudsim-simulations/ -Dexec.mainClass=homogenous.baseline.baselineSingularDatacenterHomogenous
mvn exec:java -pl modules/cloudsim-simulations/ -Dexec.mainClass=homogenous.baseline.baselineMultiDatacenterHomogenous

mvn exec:java -pl modules/cloudsim-simulations/ -Dexec.mainClass=heterogenous.baseline.baselineSingularDatacenterHeterogenous
mvn exec:java -pl modules/cloudsim-simulations/ -Dexec.mainClass=heterogenous.baseline.baselineMultiDatacenterHeterogenous


mvn exec:java -pl modules/cloudsim-simulations/ -Dexec.mainClass=homogenous.roundRobin.roundRobinSingularDatacenterHomogenous
mvn exec:java -pl modules/cloudsim-simulations/ -Dexec.mainClass=homogenous.roundRobin.roundRobinMultiDatacenterHomogenous

mvn exec:java -pl modules/cloudsim-simulations/ -Dexec.mainClass=heterogenous.roundRobin.roundRobinSingularDatacenterHeterogenous
mvn exec:java -pl modules/cloudsim-simulations/ -Dexec.mainClass=heterogenous.roundRobin.roundRobinMultiDatacenterHeterogenous