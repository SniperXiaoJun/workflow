package com.nirmata.workflow;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.nirmata.workflow.executor.TaskExecutionStatus;
import com.nirmata.workflow.executor.TaskExecutor;
import com.nirmata.workflow.models.Task;
import com.nirmata.workflow.models.TaskExecutionResult;
import com.nirmata.workflow.models.TaskId;
import com.nirmata.workflow.models.TaskType;
import org.testng.Assert;
import org.testng.annotations.Test;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class TestDelayTasks extends BaseForTests
{
    @Test
    public void testDelay() throws Exception
    {
        final long delayMs = TimeUnit.SECONDS.toMillis(5);

        BlockingQueue<Long> queue = new LinkedBlockingQueue<>();
        TaskExecutor taskExecutor = (workflowManager, executableTask) -> () ->
        {
            queue.add(System.currentTimeMillis());
            return new TaskExecutionResult(TaskExecutionStatus.SUCCESS, "");
        };
        TaskType taskType = new TaskType("test", "1", true, true);
        try ( WorkflowManager workflowManager = WorkflowManagerBuilder.builder()
            .addingTaskExecutor(taskExecutor, 10, taskType)
            .withCurator(curator, "test", "1")
            .build() )
        {
            workflowManager.start();

            Task task = new Task(new TaskId(), taskType);
            long startMs = System.currentTimeMillis();
            workflowManager.submitTask(task);

            Long ticksMs = queue.poll(1, TimeUnit.SECONDS);
            Assert.assertNotNull(ticksMs);
            Assert.assertTrue((ticksMs - startMs) < 1000);  // should have executed immediately

            Map<String, String> metaData = Maps.newHashMap();
            metaData.put(Task.META_TASK_DELAY, Long.toString(System.currentTimeMillis() + delayMs));
            task = new Task(new TaskId(), taskType, Lists.newArrayList(), metaData);
            startMs = System.currentTimeMillis();
            workflowManager.submitTask(task);
            ticksMs = queue.poll(delayMs * 2, TimeUnit.MILLISECONDS);
            Assert.assertNotNull(ticksMs);
            Assert.assertTrue((ticksMs - startMs) >= delayMs, "Bad timing: " + (ticksMs - startMs));
        }
    }
}