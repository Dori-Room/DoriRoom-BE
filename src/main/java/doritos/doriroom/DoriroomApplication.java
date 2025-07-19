package doritos.doriroom;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DoriroomApplication {

	public static void main(String[] args) {
		SpringApplication.run(DoriroomApplication.class, args);
	}

}
