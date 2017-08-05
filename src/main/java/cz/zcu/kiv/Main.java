package cz.zcu.kiv;


import cz.zcu.kiv.Pipeline.PipelineBuilder;

import java.util.Arrays;

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
 * Main, 2017/05/25 22:05 Dorian Beganovic
 *
 **********************************************************************************************************************/
public class Main {
    public static void main(String[] args) {
        //System.out.println("args are " + Arrays.toString(args));
        PipelineBuilder pipelineBuilder = new PipelineBuilder(args[0]);


        try {
            pipelineBuilder.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}