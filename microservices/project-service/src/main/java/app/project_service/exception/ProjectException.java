package app.project_service.exception;

public class ProjectException extends RuntimeException{

    public ProjectException(String msg){
        super(msg);
    }

    public ProjectException(Long id){
        super("Proyecto no registrado con ID: " + id);
    }
}
