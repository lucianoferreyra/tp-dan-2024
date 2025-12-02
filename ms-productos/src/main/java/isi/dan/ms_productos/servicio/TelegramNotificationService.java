package isi.dan.ms_productos.servicio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import isi.dan.ms_productos.conf.TelegramConfig;
import isi.dan.ms_productos.modelo.Producto;
import jakarta.annotation.PostConstruct;

@Service
public class TelegramNotificationService {

    private static final Logger log = LoggerFactory.getLogger(TelegramNotificationService.class);

    @Autowired
    private TelegramConfig telegramConfig;

    private TelegramClient telegramClient;

    @PostConstruct
    public void init() {
        if (telegramConfig.isEnabled() && telegramConfig.getBotToken() != null) {
            this.telegramClient = new OkHttpTelegramClient(telegramConfig.getBotToken());
            log.info("Cliente de Telegram inicializado correctamente");
        } else {
            log.warn("Notificaciones de Telegram deshabilitadas o token no configurado");
        }
    }

    @Async
    public void enviarAlertaStockMinimo(Producto producto) {
        if (!telegramConfig.isEnabled() || telegramClient == null) {
            log.debug("Notificaciones de Telegram deshabilitadas");
            return;
        }

        if (telegramConfig.getChatId() == null || telegramConfig.getChatId().isEmpty()) {
            log.error("Chat ID no configurado para notificaciones de Telegram");
            return;
        }

        try {
            String mensaje = String.format(
                "🔴 *ALERTA DE STOCK MÍNIMO* 🔴\n\n" +
                "📦 *Producto:* %s\n" +
                "🆔 *ID:* %d\n" +
                "📊 *Stock Actual:* %d unidades\n" +
                "⚠️ *Stock Mínimo:* %d unidades\n\n" +
                "⚡ Se requiere reposición urgente",
                escaparMarkdown(producto.getNombre()),
                producto.getId(),
                producto.getStockActual(),
                producto.getStockMinimo()
            );

            SendMessage sendMessage = SendMessage.builder()
                .chatId(telegramConfig.getChatId())
                .text(mensaje)
                .parseMode("Markdown")
                .build();

            telegramClient.execute(sendMessage);
            log.info("Alerta de stock mínimo enviada por Telegram para producto: {} (ID: {})",
                producto.getNombre(), producto.getId());

        } catch (TelegramApiException e) {
            log.error("Error al enviar mensaje de Telegram para producto {} (ID: {}): {}",
                producto.getNombre(), producto.getId(), e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error inesperado al enviar notificación de Telegram: {}", e.getMessage(), e);
        }
    }

    @Async
    public void enviarMensaje(String mensaje) {
        if (!telegramConfig.isEnabled() || telegramClient == null) {
            log.debug("Notificaciones de Telegram deshabilitadas");
            return;
        }

        if (telegramConfig.getChatId() == null || telegramConfig.getChatId().isEmpty()) {
            log.error("Chat ID no configurado para notificaciones de Telegram");
            return;
        }

        try {
            SendMessage sendMessage = SendMessage.builder()
                .chatId(telegramConfig.getChatId())
                .text(mensaje)
                .build();

            telegramClient.execute(sendMessage);
            log.info("Mensaje enviado por Telegram exitosamente");

        } catch (TelegramApiException e) {
            log.error("Error al enviar mensaje de Telegram: {}", e.getMessage(), e);
        }
    }

    /**
     * Escapa caracteres especiales de Markdown para evitar errores en el envío
     */
    private String escaparMarkdown(String texto) {
        if (texto == null) return "";
        return texto.replace("_", "\\_")
                   .replace("*", "\\*")
                   .replace("[", "\\[")
                   .replace("]", "\\]")
                   .replace("(", "\\(")
                   .replace(")", "\\)")
                   .replace("~", "\\~")
                   .replace("`", "\\`")
                   .replace(">", "\\>")
                   .replace("#", "\\#")
                   .replace("+", "\\+")
                   .replace("-", "\\-")
                   .replace("=", "\\=")
                   .replace("|", "\\|")
                   .replace("{", "\\{")
                   .replace("}", "\\}")
                   .replace(".", "\\.")
                   .replace("!", "\\!");
    }
}
