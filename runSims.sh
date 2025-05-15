mvn clean compile -pl modules/cloudsim-simulations/
mvn exec:java -pl modules/cloudsim-simulations/ -Dexec.mainClass=homogenous.baselineSingularDatacenter