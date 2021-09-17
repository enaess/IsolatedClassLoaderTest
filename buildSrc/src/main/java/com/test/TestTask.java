package com.test;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;
import org.gradle.workers.WorkParameters;
import org.gradle.workers.WorkQueue;
import org.gradle.workers.WorkerExecutor;

import javax.inject.Inject;
import java.io.File;

public abstract class TestTask
        extends DefaultTask
{
    @Classpath
    public abstract ConfigurableFileCollection getClasspath();

    @InputFiles
    public abstract ConfigurableFileCollection getSrc();

    @OutputDirectory
    public abstract DirectoryProperty getOut();

    @Input
    public abstract Property<Integer> getIterations();

    @Input
    public abstract Property<String> getXsl();

    @Inject
    public abstract WorkerExecutor getWorkerExecutor();

    public TestTask() {
        getIterations().convention(48);
    }

    @TaskAction
    public void run() {
        WorkQueue queue = getWorkerExecutor().classLoaderIsolation(it ->
                it.getClasspath().from(getClasspath()));
        for (File src : getSrc().getFiles()) {
            int i = getIterations().get();
            while (i-- > 0) {
                int finalI = i;
                queue.submit(TestAction.class, t -> {
                    t.getInput().set(src);
                    t.getName().set(getXsl().get());
                    t.getOutput().set(new File(getOut().get().getAsFile(), src.getName().replace(".xml", String.format("%d.xml", finalI))));
                });
            }
        }
    }

    interface Parameters extends WorkParameters {
        RegularFileProperty getInput();
        RegularFileProperty getOutput();
        Property<String> getName();
    }
}
