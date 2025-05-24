package app.project_service.usecase;

import app.project_service.model.Project;

import java.util.List;

public interface ProjectService {

    public Project findOne(Long id);


    public List<Project> showAll();

    public Project create(Project project);

    public Project update(Project project, Long id);

    public void delete(Long id);

}
