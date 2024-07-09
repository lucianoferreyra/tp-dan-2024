package isi.dan.ms_productos.servicio;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient("MS-CLIENTES")
public interface EchoClientFeign {
    
    @GetMapping(value="/api/clientes/echo")
    String echo();
        
}
