# EEG_Analysis

### Intro

This is a project focused providing capabilities for analyzing EEG data stored on the Hadoop Filesystem (HDFS)
using the Apache Spark library


### Data sources

The data must be stored on HDFS.

Each experiment must have 3 files: 
".vmrk" , ".vhdr" and ".eeg" file.

Because usually the ".vmrk" and  ".vhdr" and ".eeg" files have the same name but different suffix, 
pointing the application to just a ".eeg" will suffice. 

If either the ".vmrk" and  ".vhdr" related files are missing, the application will fail.

Another source type is the "info.txt" files which contain where the format is eg.  
    - Stankov/Stankov_2014_01.egg 2  
    - Stankov/Stankov_2014_02.egg 2  
    - <location of a .eeg file> <target number> 
    

So the input must follow this rule:

- Required input:
    1) < location of a .eeg file> < target number>  *< optional values>  
          - it will extract the .vhdr and .vmrk file  by substituting .eeg suffix with the appropriate  
        OR
    2) < location of info.txt file>  
          - it will loop over all of the files and also store the guessed number  
          

### Data processing 

The concerned steps are:  
- Channel selection -> selecting the Fz, Cz and Pz channels)  
- Epoch extraction -> the raw EEG signal is split into fixed-size segments, commonly referred to as epochs where length of epochs was set to 1000 ms  
- Baseline correction -> To compensate for possible shifts in the baseline, the
average of 100 ms before each epoch was subtracted from the whole epoch

The low level logic is implemented in the OffLineDataProvider class, specifically in the processEEGFiles() method.


### Feature extraction

The concerned steps are:  
- Interval selection -> In each epoch, only the 512ms time interval starting 175 ms after the beginning of each epoch were used.  
- Discrete wavelet transform -> for each EEG channel, 5-level DWT using Daubechies 8 mother wavelet was performed. Based on the results, 16 approximation coefficients of level 5 were used from each EEG channel. Finally, all three results of DVVT were concatenated to form a feature vector of dimension 48.  
- Vector normalizing -> the feature vectors were normalized to contain only samples between -1 and 1  
   
The low level implementation is found in the class WaveletTransform and specifically the method extractFeatures(double[][] epoch).  


### Classification

Various classifiers were implemented, each implementing the crucial IClassifier interface with methods: 
- void setFeatureExtraction(IFeatureExtraction fe);   -> to set the feature extraction method
- void train(List<double[][]> epochs, List<Double> targets, IFeatureExtraction fe);  -> to train the classifier
- ClassificationStatistics test(List<double[][]> epochs, List<Double> targets); -> to get the performance of a classifier  
- void save(String file);  -> to save a trained classifier
- void load(String file);  -> to load a saved classifier 


The various implementations are: 
- Logistic Regression Classifier  
- Support Vector Machine Classifier  
- Decision Tree Classifier  
- Random Forest Classifier  
- Neural Network Classifier  

The Machine learning classifier implementation can be found in the Classification package  


### Run-time configuration

This library was develop in a modular mannder so that different components could be placed together to make the whole pipeline.
The options the user has for specifying the workflow are the following: 
 

 0. Id (REQUIRED - the id of the job)  
 - id = {some_integer}*
 
 1. Input file (REQUIRED - choose what type of input you want to provide)
 - info_file={*path_to_file}  
 		OR  
 - eeg_file={*path_to_file} AND guessed_num={*number}  
 	
 2. Feature extraction (REQUIRED - choose the method used for classification)
 - fe = {dwt-8} 
 
 3. Classification  
 - a) (REQUIRED - choose whether to train or load a classifier)    
 	train_clf = {svm,dt,logreg,rf}   
 			OR  
 	load_clf={svm,dt,logreg,rf} AND load_name={src/main/resources/Classifiers/(*name*) }  
 		
 - b) (OPTIONAL - choose whether to save the classifier and give him a name)  
 	save_clf={true,false}   
 		AND  
 	save_name={*name}     
 
 - c) (REQUIRED - the specific configuration for each classifier)  
 	config_*clf_param*
 	-> can't be condensed as it's very specific for each classifier 
 
 4. Saving the results (REQUIRED - choose where to save the classifier performance)  
 - result_path={*path_to_file}     


The configuration must be passed in a special format of query parameters, ie.
 param1=value1&parama2=value2&...
 
For example the query parameters might be:  
info_file=/user/digitalAssistanceSystem/data/numbers/info.txt&  
fe=dwt-8&  
train_clf=rf&  
save_clf=true&  
save_name=rf_random_forest_test_dorian&  
config_num_trees=100&  
config_max_depth=5&  
config_min_instances_per_node=1&  
config_max_bins=32&  
config_feature_subset=auto&  
config_impurity=gini&  
result_path=/Users/dorianbeganovic/spark_server/results/1000329970.txt  
 
 ### Specific per-classifier configuration options
 
 1. Logistic Regression Classifier  
 - config_step_size -> default=1.0
 - config_num_iterations ->  default=100
 - config_mini_batch_fraction ->  default = 1.0
 
  2. Support Vector Machine Classifier  
 - config_step_size -> default=1.0
 - config_num_iterations ->  default=100
 - config_mini_batch_fraction ->  default = 1.0
 - config_reg_param -> default = 0.01
 
 3. Decision Tree Classifier  
 - config_max_depth -> default=5
 - config_max_bins ->  default=32
 - config_min_instances_per_node ->  default = 1
 - config_impurity -> default = gini ; options={gini,entropy}
 
 4. Random Forest Classifier  
  - config_max_depth -> default=5
  - config_max_bins ->  default=32
  - config_min_instances_per_node ->  default = 1
  - config_impurity -> default = gini ; options={gini,entropy}
  - config_feature_subset -> default=auto ; options={auto,all,sqrt,log2} 
  - config_num_trees -> default=100 
 
 5. Neural Network Classifier  
 
    General classifier options  
     
 - config_seed -> default=12345
 - config_num_iterations -> default = 1000
 - config_learning_rate -> default = 0.1
 - config_momentum -> default = 0.5
 - config_weight_init -> default = xavier, options = {xavier, zero, sigmoid, uniform, relu}
 - config_updater -> default = nesterovs, options = {sgd, adam, nesterovs, adagrad, rmsprop} 
        (Good information: https://deeplearning4j.org/updater)
 - config_optimization_algo -> default = conjugate_gradient,  options = {line_gradient_descent, lbfgs, conjugate_gradient, stochastic_gradient_descent}  
 - config_loss_function -> default = xent, options = { mse, xent, squared_loss , negativeloglikelihood}
 - config_pretrain -> default = false
 - config_backprop -> default = true
 
     Per layer classifier options  

- config_layer(i)_layer_type -> default = output, options = {"output", "dense", "auto_encoder", "rbm", "graves_lstm"}
- config_layer(i)_drop_out -> default = 0.-
- config_layer(i)_activation_function -> default = sigmoid, options = {"sigmoid", "softmax", "relu", "tanh", "identity", "softplus", "elu"}
- config_layer(i)_n_out -> default = 2 

If you have trouble understanding what abbreviations like "sgd" mean, 
I suggest googleing with a query like "dl4j sgd neural network meaning" 
 
 
 ### Build process
 
 
 The application uses Apache Maven for dependency management and packaging.
 
 To build the fat jar file, please use the command: 
 - mvn package   
 
 To skip the tests during the compilation, use the command:  
 - mvn package -DskipTests

### Deployment

The project is compatible with Spark 1.6.2 version as well as Apache Hadoop 2.6.0


There are two common ways in which you can use this application:

1) Development on local machine
    - you need to have Hadoop file system running on your machine
    - you also need to install and configure Apache Spark
    - you can run the code easily with IntellijIdea

2) Production use on the server
    - you can use Spark Submit or a workflow manager like Oozie to submit jobs to the JAR file
 
This is an example of a script that will submit a job to Apache Spark, 
also note the specific --conf parameters which are passed (you should make sure to also pass them)

spark-submit   
--class cz.zcu.kiv.Main   
--master local[*]   
--conf spark.driver.host=localhost   
--conf spark.executor.extraJavaOptions=-Dlogfile.name=/Users/dorianbeganovic/spark_server/logs/1721658280   
--conf spark.driver.extraJavaOptions=-Dlogfile.name=/Users/dorianbeganovic/spark_server/logs/1721658280   
/Users/dorianbeganovic/gsoc/Spark_EEG_Analysis/target/spark_eeg-1.2-jar-with-dependencies.jar   
"info_file=/user/digitalAssistanceSystem/data/numbers/info.txt&fe=dwt-8&load_clf=logreg&load_name=logreg_0000&config_reg_param=0.01&config_mini_batch_fraction=1.0&config_num_iterations=100&config_step_size=1.0&result_path=/Users/dorianbeganovic/spark_server/results/1721658280.txt"  
