package com.nirmata.workflow.queue.zookeeper;

import com.nirmata.workflow.models.ExecutableTask;
import org.apache.curator.framework.recipes.queue.QueueSerializer;

public class TaskQueueSerializer implements QueueSerializer<ExecutableTask>
{
    @Override
    public byte[] serialize(ExecutableTask executableTask)
    {
        return null;// TODO toBytes(newExecutableTask(executableTask));
    }

    @Override
    public ExecutableTask deserialize(byte[] bytes)
    {
        return null; // TODO getExecutableTask(fromBytes(bytes));
    }
}