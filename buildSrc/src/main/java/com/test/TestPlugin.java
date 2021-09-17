package com.test;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.attributes.Usage;
import org.gradle.api.tasks.TaskProvider;

import java.util.Map;

import static org.gradle.api.attributes.Usage.JAVA_RUNTIME;

public class TestPlugin
        implements Plugin<Project>
{
    @Override
    public void apply(Project project) {
        final Configuration config = project.getConfigurations().create("isolation");
        config.getAttributes().attribute(Usage.USAGE_ATTRIBUTE, project.getObjects().named(Usage.class, JAVA_RUNTIME));
        config.setVisible(false);
        config.setCanBeConsumed(false);
        config.setCanBeResolved(true);
        config.getDependencies().add(
                // project.getDependencies().create("net.sf.saxon:Saxon-HE:10.5"));
                project.getDependencies().create("net.sf.okapi:okapi-lib:1.41.0"));
        config.getDependencies().add(
                project.getDependencies().create(project.files("../test.jar")));

        project.getPluginManager().withPlugin("java", p -> {
            TaskProvider <TestTask> task = project.getTasks().register("generateHtml", TestTask.class, t2 -> {
                t2.getClasspath().from(config);
                t2.getXsl().set("rule.xsl");
                t2.getSrc().from(project.fileTree(
                        Map.of("dir", project.getLayout().getProjectDirectory().dir("default/xml"), "include", "*.xml")));
                t2.getOut().set(project.getLayout().getBuildDirectory().dir("html"));
                t2.getOutputs().upToDateWhen(cl -> false);
            });
            project.getTasks().named("compileJava", t1 ->
                    t1.dependsOn(task));
        });
    }
}