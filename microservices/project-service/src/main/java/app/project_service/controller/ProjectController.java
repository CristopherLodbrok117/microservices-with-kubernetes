package app.project_service.controller;


import app.project_service.model.Project;
import app.project_service.usecase.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping
    public ResponseEntity<List<Project>> showAll(){
        return ResponseEntity.ok(projectService.showAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Project> findOne(@PathVariable(name = "id") Long id){
        return ResponseEntity.ok(projectService.findOne(id));
    }

    @PostMapping
    public ResponseEntity<Project> create(@RequestBody Project project,
                                          UriComponentsBuilder ucb){

        Project newProject = projectService.create(project);

        URI projectLocation = ucb
                .path("/api/projects/{id}")
                .buildAndExpand(newProject.getId())
                .toUri();

        return ResponseEntity.created(projectLocation).body(newProject);

    }

    @PutMapping("/{id}")
    public ResponseEntity<Project> update(@PathVariable Long id,
                                          @RequestBody Project project){

        return ResponseEntity.ok(projectService.update(project, id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id){
        projectService.delete(id);
        
        return ResponseEntity.noContent().build();
    }
}
