/*
 * Copyright (C) 2019-2020 Dario Scoppelletti, <http://www.scoppelletti.it/>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package it.scoppelletti.spaceship.gradle.java;

import java.io.File;
import java.nio.file.Path;
import java.util.Objects;
import javax.annotation.Nonnull;
import it.scoppelletti.spaceship.gradle.LibraryTools;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.component.SoftwareComponent;
import org.gradle.api.file.FileCollection;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.publish.maven.MavenPublication;
import org.gradle.api.tasks.Copy;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.bundling.Jar;

/**
 * Library tools.
 */
final class JavaLibraryTools extends LibraryTools {
    private static final String COMPONENT_JAVA = "java";
    private static final String PACKAGING = "jar";
    private final Project myProject;

    /**
     * Constructor.
     *
     * @param project Project.
     */
    JavaLibraryTools(@Nonnull Project project) {
        super(project, JavaLibraryTools.PACKAGING,
                new JavaLibraryTaskNames(project));

        myProject = project;
    }

    /**
     * Defines the task {@code Copy} for generating the {@code META-INF} files.
     */
    void generateMetainf() {
        Path intoDir;
        Task jarTask;
        Copy metainfTask;

        // http://stackoverflow.com/questions/34306200 - December 16, 2015
        intoDir = myProject.getBuildDir().toPath()
                .resolve("resources")
                .resolve("main")
                .resolve("META-INF");

        metainfTask = doGenerateMetainf(intoDir);
        jarTask = Objects.requireNonNull(
                myProject.getTasks().findByName(JavaPlugin.JAR_TASK_NAME),
                () -> String.format("Task %1$s not found.",
                        JavaPlugin.JAR_TASK_NAME));
        jarTask.dependsOn(metainfTask);
    }

    /**
     * Defines the task {@code Jar} for packaging the source code files.
     */
    void packageSources() {
        Jar jarTask;
        FileCollection source;
        SourceSet mainSources;
        JavaPluginExtension convention;

        convention = myProject.getExtensions().getByType(
                JavaPluginExtension.class);

        source = myProject.files();
        mainSources = convention.getSourceSets().getByName("main");
        for (File file : mainSources.getAllJava().getSourceDirectories()) {
            source = source.plus(myProject.files(file));
        }

        jarTask = doPackageSources(source);
        jarTask.dependsOn(JavaPlugin.JAR_TASK_NAME);
    }

    /**
     * Defines the task {@code DokkaTask} for generating the KDoc documentation.
     */
    void generateKDoc() {
        Task kdocTask;

        kdocTask = doGenerateKDoc();
        kdocTask.dependsOn(JavaPlugin.JAR_TASK_NAME);
    }

    /**
     * Defines the Maven publication.
     */
    void publish() {
        MavenPublication publ;
        SoftwareComponent component;

        publ = doPublish();

        component = Objects.requireNonNull(
                myProject.getComponents().findByName(
                        JavaLibraryTools.COMPONENT_JAVA),
                () -> String.format("Software component %1$s not found.",
                        JavaLibraryTools.COMPONENT_JAVA));
        publ.from(component);
    }
}
