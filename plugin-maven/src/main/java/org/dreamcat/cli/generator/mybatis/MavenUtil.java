package org.dreamcat.cli.generator.mybatis;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.dreamcat.common.json.JsonUtil;
import org.dreamcat.common.net.UrlUtil;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Jerry Will
 * @version 2024-02-24
 */
public class MavenUtil {

    public static ClassLoader buildUserCodeClassLoader(
            MavenProject project, ArtifactRepository localRepository, Log log) throws Exception {
        Set<String> classDirs = new HashSet<>();
        classDirs.add(getClassDir(project));
        classDirs.addAll(orEmpty(getCompileClasspath(project)));
        classDirs.addAll(orEmpty(getRuntimeClasspath(project)));
        log.info("classDirs: " + JsonUtil.toJson(classDirs));

        List<File> dependencies = getDependencies(project, localRepository);
        log.info("dependencies: " + JsonUtil.toJson(dependencies));

        URL[] urls = Stream.concat(dependencies.stream(), classDirs.stream().map(File::new))
                .map(UrlUtil::toURL).toArray(URL[]::new);
        log.info("resolved classLoader urls: " + JsonUtil.toJson(urls));

        return new URLClassLoader(urls, Thread.currentThread().getContextClassLoader());
    }

    public static List<String> getCompileClasspath(MavenProject project) throws Exception {
        // target/classes
        return project.getCompileClasspathElements();
    }

    public static List<String> getRuntimeClasspath(MavenProject project) throws Exception {
        // target/classes
        return project.getRuntimeClasspathElements();
    }

    public static String getClassDir(MavenProject project) throws IOException {
        // target/classes
        return project.getBuild().getOutputDirectory();
    }

    public static String getSrcDir(MavenProject project) throws IOException {
        // src/main/java
        return project.getBuild().getSourceDirectory();
    }

    public static File getBaseDir(MavenProject project) {
        File basedir;
        while ((basedir = project.getBasedir()) == null) {
            project = project.getParent();
            if (project == null) break;
        }
        return basedir;
    }

    public static List<File> getDependencies(
            MavenProject project, ArtifactRepository localRepository) {
        String repoBaseDir = localRepository.getBasedir();
        return project.getDependencies().stream()
                .filter(dep -> "jar".equals(dep.getType()))
                .map(dep -> new File(repoBaseDir, getPath(dep)))
                .collect(Collectors.toList());
    }

    private static String getPath(Dependency dep) {
        return dep.getGroupId().replace('.', File.separatorChar) +
                File.separatorChar + dep.getArtifactId() + File.separatorChar +
                dep.getVersion() + File.separatorChar +
                dep.getArtifactId() + "-" + dep.getVersion() + ".jar";
    }

    private static <T> List<T> orEmpty(List<T> list) {
        return list != null ? list : Collections.emptyList();
    }
}
