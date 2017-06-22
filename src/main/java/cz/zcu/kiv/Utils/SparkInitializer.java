package cz.zcu.kiv.Utils;

import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaSparkContext;

/***********************************************************************************************************************
 *
 * This file is part of the Spark_EEG_Analysis project

 * ==========================================
 *
 * Copyright (C) 2017 by University of West Bohemia (http://www.zcu.cz/en/)
 *
 ***********************************************************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 ***********************************************************************************************************************
 *
 * SparkInitializer, 2017/06/11 16:03 Dorian Beganovic
 *
 **********************************************************************************************************************/
public class SparkInitializer {

    private static JavaSparkContext javaSparkContext = null;
    private static SparkContext sparkContext = null;

    private SparkInitializer(){
        // Exists only to defeat instantiation.
    }

    public static SparkContext getSparkContext() {
        if (sparkContext == null) {
            sparkContext = new SparkContext(new SparkConf().
                    setAppName("Guess the number").
                    setMaster("local[*]").
                    set("spark.driver.allowMultipleContexts", "true")
            );
        }
        return sparkContext;
    }

    public static JavaSparkContext getJavaSparkContext() {
        if (javaSparkContext == null) {
            javaSparkContext = new JavaSparkContext(getSparkContext());
        }
        return javaSparkContext;
    }

}
