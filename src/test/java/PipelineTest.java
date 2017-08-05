import cz.zcu.kiv.Pipeline.PipelineBuilder;
import org.junit.Test;

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
 * PipelineTest, 2017/07/11 23:11 Dorian Beganovic
 *
 **********************************************************************************************************************/
public class PipelineTest {
    @Test
    public void test() throws Exception {
        PipelineBuilder pipelineBuilder = new PipelineBuilder("" +
                "info_file=/user/digitalAssistanceSystem/data/numbers/infoTrain.txt" +
                "&fe=dwt-8" +
                "&train_clf=svm"+
                "&config_step_size=1.0"+
                "&config_num_iterations=10"+
                "&config_reg_param=0.01"+
                "&config_mini_batch_fraction=1.0"+
                "&result_path=/Users/dorianbeganovic/Desktop/10021.txt"
        );
        pipelineBuilder.execute();
    }

    @Test
    public void test2() throws Exception {
        PipelineBuilder pipelineBuilder = new PipelineBuilder("" +
                "info_file=/user/digitalAssistanceSystem/data/numbers/infoTrain.txt" +
                "&fe=dwt-8" +
                "&train_clf=logreg" +
                "&save_clf=true" +
                "&save_name=log_reg_demo_Dorian"
        );
        pipelineBuilder.execute();
    }

    @Test
    public void test2_svm() throws Exception {
        PipelineBuilder pipelineBuilder = new PipelineBuilder("" +
                "info_file=/user/digitalAssistanceSystem/data/numbers/infoTrain.txt" +
                "&fe=dwt-8" +
                "&train_clf=svm" +
                "&save_clf=true" +
                "&save_name=svm_demo_Dorian"
        );
        pipelineBuilder.execute();
    }


    @Test
    public void test3() throws Exception {
        PipelineBuilder pipelineBuilder = new PipelineBuilder("" +
                "info_file=/user/digitalAssistanceSystem/data/numbers/infoTrain.txt" +
                "&fe=dwt-8" +
                "&load_clf=logreg" +
                "&load_name=log_reg_demo_Dorian"
        );
        pipelineBuilder.execute();
    }
}
