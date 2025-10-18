package isi.dan.ms_productos.conf;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
// import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.hibernate5.jakarta.Hibernate5JakartaModule;

@Configuration
public class JacksonConfig {

  @Bean
  public ObjectMapper objectMapper() {
    ObjectMapper mapper = new ObjectMapper();
    Hibernate5JakartaModule hibernateModule = new Hibernate5JakartaModule();
    hibernateModule.configure(Hibernate5JakartaModule.Feature.FORCE_LAZY_LOADING, false);
    mapper.registerModule(hibernateModule);
    return mapper;
  }
}