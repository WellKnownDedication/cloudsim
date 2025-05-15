mvn clean compile -pl modules/cloudsim-simulations/

mvn exec:java -pl modules/cloudsim-simulations/ -Dexec.mainClass=homogenous.baselineSingularDatacenterHomogenous
mvn exec:java -pl modules/cloudsim-simulations/ -Dexec.mainClass=homogenous.baselineMultiDatacenterHomogenous


mvn exec:java -pl modules/cloudsim-simulations/ -Dexec.mainClass=heterogenous.baselineSingularDatacenterHeterogenous
mvn exec:java -pl modules/cloudsim-simulations/ -Dexec.mainClass=heterogenous.baselineMultiDatacenterHeterogenous