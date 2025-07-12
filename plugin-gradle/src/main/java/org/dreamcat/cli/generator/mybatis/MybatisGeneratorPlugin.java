package org.dreamcat.cli.generator.mybatis;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;

/**
 * @author Jerry Will
 * @version 2024-12-22
 */
public class MybatisGeneratorPlugin implements Plugin<Project> {

    private static final String name = "mybatisGenerate";
    private static final String taskGroup = "other";

    @Override
    public void apply(Project project) {
        // Property<?> or getter/setter pojo
        project.getExtensions().create(name, MybatisGeneratorExtension.class);

        // project.getTasks().create(name, ApiDocGeneratorTask.class, project, extension);
        Task task = project.getTasks().create(name, MybatisGeneratorTask.class);
        task.setGroup(taskGroup);
    }
}
