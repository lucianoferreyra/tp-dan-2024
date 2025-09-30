package isi.dan.ms.pedidos.dao;

import java.util.List;

import isi.dan.ms.pedidos.modelo.Pedido;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface PedidoRepository extends MongoRepository<Pedido, String> {

  List<Pedido> findByClienteIdAndEstadoIn(Long clienteId, List<Pedido.EstadoPedido> estados);
}

