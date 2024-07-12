package nekit508;

import nekit508.extensions.MMPluginExtension;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class MMPlugin implements Plugin<Project>, MMPluginExtension {
    public Project project;

    @Override
    public void apply(Project project) {
        this.project = project;
    }

    @Override
    public Project project() {
        return project;
    }
}
