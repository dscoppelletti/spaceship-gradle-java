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

import java.net.URI;
import javax.annotation.Nonnull;
import it.scoppelletti.spaceship.gradle.ProjectTools;
import it.scoppelletti.spaceship.gradle.SpaceshipPlugin;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

/**
 * Plug-in for Java libraries.
 *
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public class LibraryPlugin implements Plugin<Project> {
    private static final Logger myLogger = Logging.getLogger(
            LibraryPlugin.class);

    @Override
    public void apply(@Nonnull Project project) {
        URI devRepoUrl;
        ProjectTools projectTools;
        JavaLibraryTools libTools;

        if (project.getPlugins().hasPlugin(SpaceshipPlugin.class)) {
            myLogger.info("Plugin {} already applied.", SpaceshipPlugin.class);
        } else {
            myLogger.info("Applying plugin {}.", SpaceshipPlugin.class);
            project.getPluginManager().apply(SpaceshipPlugin.class);
        }

        projectTools = new ProjectTools(project);
        devRepoUrl = projectTools.applyMavenPublish();
        libTools = new JavaLibraryTools(project);

        project.afterEvaluate(prj -> {
            libTools.generateMetainf();
            libTools.packageSources();

            if (projectTools.isKDocEnabled()) {
                libTools.generateKDoc();
                libTools.packageKDoc();
            }

            if (devRepoUrl != null) {
                libTools.publish();
                projectTools.definePublishingRepo(devRepoUrl,
                        JavaLibraryTaskNames.ASSEMBLE);
            }
        });
    }
}
