package doritos.doriroom.quiz.exception;

import doritos.doriroom.global.exception.ApiException;
import org.springframework.http.HttpStatus;

public class QuizStatusException extends ApiException {
    public QuizStatusException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}