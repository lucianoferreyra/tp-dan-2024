package isi.dan.ms_productos.conf;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "telegram")
@Data
public class TelegramConfig {
    private String botToken;
    private String chatId;
    private boolean enabled = true;
}
