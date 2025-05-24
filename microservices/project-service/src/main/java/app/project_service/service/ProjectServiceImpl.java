package app.project_service.service;

import app.project_service.exception.ProjectException;
import app.project_service.model.Project;
import app.project_service.repository.ProjectRepository;
import app.project_service.usecase.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository repo;

    @Override
    public Project findOne(Long id) {

        return repo.findById(id)
                .orElseThrow(() -> new ProjectException(id));
    }

    @Override
    public List<Project> showAll() {
        return repo.findAll();
    }

    @Override
    public Project create(Project project) {
        if(repo.findByName(project.getName()).isPresent()){
            throw new ProjectException("Ya existe un proyecto con el nombre:" + project.getName());
        }

        return repo.save(project);
    }

    @Override
    public Project update(Project project, Long id) {
        Project dbProject = repo.findById(id).orElseThrow(() -> new ProjectException(id));

        project.setId(dbProject.getId());

        return repo.save(project);
    }

    @Override
    public void delete(Long id) {
        if(!repo.existsById(id)) {
            throw new ProjectException(id);
        }

        repo.deleteById(id);

    }
}
