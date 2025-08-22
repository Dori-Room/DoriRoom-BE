package doritos.doriroom.quiz.exception;

import doritos.doriroom.global.exception.ApiException;
import org.springframework.http.HttpStatus;

public class QuizNotFoundException extends ApiException {
    public QuizNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }
}