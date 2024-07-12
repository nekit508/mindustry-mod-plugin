package nekit508;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class MMPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        System.out.println(project.getRootDir());
    }
}
